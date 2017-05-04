package tv.ismar.player.gui;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.MainThread;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.VodApplication;
import tv.ismar.app.ad.Advertisement;
import tv.ismar.app.core.PlayCheckManager;
import tv.ismar.app.db.HistoryManager;
import tv.ismar.app.entity.DBQuality;
import tv.ismar.app.entity.History;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.library.injectdb.util.Log;
import tv.ismar.library.network.HttpManager;
import tv.ismar.library.util.AppUtils;
import tv.ismar.library.util.DeviceUtils;
import tv.ismar.library.util.LogUtils;
import tv.ismar.library.util.StringUtils;
import tv.ismar.player.IPlayer;
import tv.ismar.player.IsmartvPlayer;
import tv.ismar.player.model.MediaEntity;

public class PlaybackService extends Service implements Advertisement.OnVideoPlayAdListener {

    private static final String TAG = "LH/PlaybackService";

    private class LocalBinder extends Binder {
        PlaybackService getService() {
            return PlaybackService.this;
        }
    }

    public static PlaybackService getService(IBinder iBinder) {
        LocalBinder binder = (LocalBinder) iBinder;
        return binder.getService();
    }

    private final IBinder mBinder = new LocalBinder();

    private int itemPk, subItemPk;
    private String source;
    private String snToken, deviceToken, authToken, username, zuserToken, zdeviceToken;
    private Subscription mApiItemSubsc;
    private Subscription mApiMediaUrlSubsc;
    private Advertisement mAdvertisement;

    private ItemEntity mItemEntity;
    private ClipEntity mClipEntity;
    private boolean isBindActivity = false;
    private String mQiyiUserType;// playerCheck 返回user类型
    // 历史记录
    private HistoryManager historyManager;
    private History mHistory;
    // HLS播放器
    private IsmartvPlayer hlsPlayer;// HLS播放
    private ServiceCallback serviceCallback;
    private SurfaceView surfaceView;
    private FrameLayout container;

    private boolean mHasAudioFocus = false;
    private int mCurrentPosition;
    private ClipEntity.Quality mCurrentQuality;
    private boolean mIsPlayerPrepared;// 播放器是否处于可播放状态,onPrepared回调后为true,onError，onCompleted回调或stop()后为false
    private boolean mIsPreview;// 是否试看
    private boolean isSwitchTelevision = false;// 手动切换剧集，不查历史记录
    private boolean mIsPlayingAd;// 判断是否正在播放广告

    public PlaybackService() {
    }

    public IsmartvPlayer getMediaPlayer() {
        return hlsPlayer;
    }

    public ItemEntity getItemEntity() {
        return mItemEntity;
    }

    public boolean isPlayerPrepared() {
        return mIsPlayerPrepared;
    }

    public boolean isPreview() {
        return mIsPreview;
    }

    @Override
    public IBinder onBind(Intent intent) {
        isBindActivity = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBindActivity = false;
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        snToken = IsmartvActivator.getInstance().getSnToken();
        deviceToken = IsmartvActivator.getInstance().getDeviceToken();
        authToken = IsmartvActivator.getInstance().getAuthToken();
        username = IsmartvActivator.getInstance().getUsername();
        zuserToken = IsmartvActivator.getInstance().getZUserToken();
        zdeviceToken = IsmartvActivator.getInstance().getDeviceToken();
        mAdvertisement = new Advertisement(this);
        mAdvertisement.setOnVideoPlayListener(this);
        HttpManager.getInstance().setAccessToken(authToken);
        HttpManager.getInstance().init(IsmartvActivator.getInstance().getApiDomain(), IsmartvActivator.getInstance().getUpgradeDomain(), deviceToken);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止服务，播放器资源释放
        mIsPlayerPrepared = false;
        if (hlsPlayer != null) {
            hlsPlayer.setOnAdvertisementListener(null);
            hlsPlayer.setOnBufferChangedListener(null);
            hlsPlayer.setOnStateChangedListener(null);
            hlsPlayer.stop();
            hlsPlayer.release();
            hlsPlayer = null;
        }

    }

    public void preparePlayer(int itemPk, int subItemPk, String source) {
        if (itemPk <= 0) {
            LogUtils.e(TAG, "itemPk can't be null.");
            return;
        }
        this.itemPk = itemPk;// 当前影片pk值,通过/api/item/{pk}可获取详细信息
        this.subItemPk = subItemPk;// 当前多集片pk值,通过/api/subitem/{pk}可获取详细信息
        this.source = source;
        mIsPlayerPrepared = false;
        fetchPlayerItem(String.valueOf(itemPk));
    }

    public void startPlayWhenPrepared(SurfaceView surfaceView, FrameLayout container) {
        this.surfaceView = surfaceView;
        this.container = container;
        if (mIsPlayerPrepared) {
            // Service 与 PlaybackActivity绑定，直接播放视频
            hlsPlayer.attachSurfaceView(surfaceView, container);
        }
    }

    public void setCallback(ServiceCallback serviceCallback) {
        this.serviceCallback = serviceCallback;
    }

    public void exitPlayerUI(String to) {
        if (hlsPlayer != null) {
            hlsPlayer.logVideoExit(mCurrentPosition, to);
            if (hlsPlayer.isPlaying()) {
                hlsPlayer.pause();
            }
            hlsPlayer.detachViews();
        }
    }

    public void setPlayerEvent() {
        // Called once in IsmartvPlayer.OnStateChangedListener.onStarted
        if (hlsPlayer != null) {
            hlsPlayer.setPlayerEvent(username, mItemEntity.getTitle(), mItemEntity.getClip().getPk(),
                    BaseActivity.baseChannel, BaseActivity.baseSection, source, mCurrentQuality);
        }
    }

    private void cancelRequest() {
        if (mApiItemSubsc != null && !mApiItemSubsc.isUnsubscribed()) {
            mApiItemSubsc.unsubscribe();
        }
        if (mApiMediaUrlSubsc != null && !mApiMediaUrlSubsc.isUnsubscribed()) {
            mApiMediaUrlSubsc.unsubscribe();
        }
    }

    private void loadPlayerItem(ItemEntity itemEntity) {
        // Get data from sharedPreference
        mItemEntity = itemEntity;
        if (historyManager == null) {
            historyManager = VodApplication.getModuleAppContext().getModuleHistoryManager();
        }
        String historyUrl = Utils.getItemUrl(itemPk);
        String isLogin = "no";
        if (!Utils.isEmptyText(authToken)) {
            isLogin = "yes";
        }
        mHistory = historyManager.getHistoryByUrl(historyUrl, isLogin);
        if (mHistory != null) {
            mCurrentPosition = (int) mHistory.last_position;
        }
        ItemEntity.Clip clip = itemEntity.getClip();
        ItemEntity[] subItems = itemEntity.getSubitems();
        if (subItems != null && subItems.length > 0) {
            int history_sub_pk = 0;
            if (mHistory != null) {
                history_sub_pk = Utils.getItemPk(mHistory.sub_url);
            }
            Log.i(TAG, "loadItem-ExtraSubItemPk:" + subItemPk + " historySubPk:" + history_sub_pk);
            if (subItemPk <= 0) {
                // 点击播放按钮时，如果有历史记录，应该播放历史记录的subItemPk,默认播放第一集
                if (history_sub_pk > 0) {
                    subItemPk = history_sub_pk;
                    mCurrentPosition = (int) mHistory.last_position;
                } else {
                    subItemPk = subItems[0].getPk();
                    mCurrentPosition = 0;
                }

            } else {
                if (subItemPk != history_sub_pk) {
                    mCurrentPosition = 0;
                }
            }
            // 获取当前要播放的电视剧Clip
            for (int i = 0; i < subItems.length; i++) {
                int _subItemPk = subItems[i].getPk();
                if (subItemPk == _subItemPk) {
                    clip = subItems[i].getClip();
                    mItemEntity.setTitle(subItems[i].getTitle());
                    mItemEntity.setClip(clip);
                    break;
                }
            }
        }
        // playCheck
        final String sign = "";
        final String code = "1";
        final ItemEntity.Clip playCheckClip = clip;
        mIsPreview = false;
        if (mItemEntity.getExpense() != null) {
            PlayCheckManager.getInstance(HttpManager.getDomainService(SkyService.class)).check(String.valueOf(mItemEntity.getPk()), new PlayCheckManager.Callback() {
                @Override
                public void onSuccess(boolean isBuy, int remainDay, String user) {
                    LogUtils.i(TAG, "play check isBuy:" + isBuy + " " + remainDay + " " + user);
                    if (!isBindActivity) {
                        LogUtils.d(TAG, "Activity unbind on play check");
                        return;
                    }
                    mQiyiUserType = user;
                    if (isBuy) {
                        fetchClipUrl(playCheckClip.getUrl(), sign, code);
                    } else {
                        videoPreview(sign, code);
                    }
                }

                @Override
                public void onFailure() {
                    LogUtils.e(TAG, "play check fail");
                    if (!isBindActivity) {
                        LogUtils.d(TAG, "Activity unbind on play check");
                        return;
                    }
                    videoPreview(sign, code);

                }
            });
        } else {
            fetchClipUrl(clip.getUrl(), sign, code);
        }
    }

    private void videoPreview(String sign, String code) {
        ItemEntity.Preview preview = mItemEntity.getPreview();
        if (preview != null) {
            mIsPreview = true;
            mItemEntity.setLiveVideo(false);
            fetchClipUrl(preview.getUrl(), sign, code);
        } else {
            // TODO 进入购买页面
            // goOtherPage(EVENT_COMPLETE_BUY);
        }
    }

    private void loadPlayerClip(ClipEntity clipEntity) {
        mClipEntity = clipEntity;
        mIsPlayerPrepared = false;
        mIsPlayingAd = false;
        // 每次进入创建播放器前先获取历史记录，历史播放位置，历史分辨率，手动切换剧集例外
        if (!isSwitchTelevision) {
            if (mCurrentQuality == null) {
                DBQuality dbQuality = historyManager.getQuality();
                if (dbQuality != null) {
                    mCurrentQuality = ClipEntity.Quality.getQuality(dbQuality.quality);
                }
            }
            if (mIsPreview) {
                mCurrentPosition = 0;
            }
        }
        isSwitchTelevision = false;
        // TODO showBuffer
        String iqiyi = mClipEntity.getIqiyi_4_0();
        if (!mIsPreview && Utils.isEmptyText(iqiyi)) {
            // 视云影片获取前贴片广告
            mAdvertisement.fetchVideoStartAd(mItemEntity, Advertisement.AD_MODE_ONSTART, source);
        } else {
            createPlayer(null);
        }
    }

    @Override
    public void loadVideoStartAd(List<AdElementEntity> adList) {
        if (adList != null && !adList.isEmpty()) {
            mIsPlayingAd = true;
        }
        createPlayer(adList);
    }

    private void createPlayer(List<AdElementEntity> adList) {
        String iqiyi = mClipEntity.getIqiyi_4_0();
        if (hlsPlayer != null && hlsPlayer.getPlayerMode() == IsmartvPlayer.MODE_SMART_PLAYER) {
            // 当前已经有SmartPlayer实例，无需重新创建
        } else if (hlsPlayer != null && hlsPlayer.getPlayerMode() == IsmartvPlayer.MODE_QIYI_PLAYER) {
            // 当前已经有奇艺播放器实例，无需重新创建
            hlsPlayer.setQiyiUserType(mQiyiUserType);
            hlsPlayer.setzDeviceToken(zdeviceToken);
            hlsPlayer.setzUserToken(zuserToken);
        } else {
            if (hlsPlayer != null) {
                hlsPlayer.stop();
                hlsPlayer.release();
                hlsPlayer = null;
            }
            // 创建当前播放器
            IsmartvPlayer.Builder builder = new IsmartvPlayer.Builder();
            builder.setSnToken(snToken);
            if (Utils.isEmptyText(iqiyi)) {
                // 片源为视云
                builder.setPlayerMode(IsmartvPlayer.MODE_SMART_PLAYER);
                builder.setDeviceToken(deviceToken);
            } else {
                // 片源为爱奇艺
                builder.setPlayerMode(IsmartvPlayer.MODE_QIYI_PLAYER);
                builder.setModelName(DeviceUtils.getModelName());
                builder.setVersionCode(String.valueOf(AppUtils.getVersionCode(this)));
                builder.setQiyiUserType(mQiyiUserType);
                builder.setzDeviceToken(zdeviceToken);
                builder.setzUserToken(zuserToken);
            }
            hlsPlayer = builder.build();
            hlsPlayer.setOnAdvertisementListener(onAdvertisementListener);
            hlsPlayer.setOnBufferChangedListener(onBufferChangedListener);
            hlsPlayer.setOnStateChangedListener(onStateChangedListener);
        }
        MediaEntity mediaEntity = new MediaEntity(itemPk, subItemPk, mItemEntity.getLiveVideo(), mClipEntity);
        if (adList != null && !adList.isEmpty()) {
            mediaEntity.setAdvStreamList(adList);
        }
        mediaEntity.setInitQuality(mCurrentQuality);
        mediaEntity.setStartPosition(mCurrentPosition);
        hlsPlayer.prepare(mediaEntity);
    }

    private void fetchPlayerItem(String itemPk) {
        if (mApiItemSubsc != null && !mApiItemSubsc.isUnsubscribed()) {
            mApiItemSubsc.unsubscribe();
        }
        mApiItemSubsc = HttpManager.getDomainService(SkyService.class).apiItem(itemPk)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ItemEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ItemEntity itemEntity) {
                        if (itemEntity == null || !isBindActivity) {
                            Log.e(TAG, "Response item data null or activity unbind");
                            return;
                        }
                        loadPlayerItem(itemEntity);
                    }
                });

    }

    private void fetchClipUrl(String clipUrl, String sign, String code) {
        if (mApiMediaUrlSubsc != null && !mApiMediaUrlSubsc.isUnsubscribed()) {
            mApiMediaUrlSubsc.unsubscribe();
        }
        if (StringUtils.isEmpty(clipUrl)) {
            LogUtils.e(TAG, "clipUrl is null.");
            return;
        }
        mApiMediaUrlSubsc = HttpManager.getDomainService(SkyService.class).fetchMediaUrl(clipUrl, sign, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ClipEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ClipEntity clipEntity) {
                        if (clipEntity == null || !isBindActivity) {
                            Log.e(TAG, "Response clip data null or activity unbind");
                            return;
                        }
                        loadPlayerClip(clipEntity);
                    }
                });

    }

    private void sendHistory(HashMap<String, Object> history) {
        Call<ResponseBody> call = HttpManager.getDomainService(SkyService.class).sendPlayHistory(history);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String result = null;
                try {
                    result = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LogUtils.i(TAG, "SendHistory : " + result);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private IPlayer.OnAdvertisementListener onAdvertisementListener = new IPlayer.OnAdvertisementListener() {
        @Override
        public void onAdStart() {

        }

        @Override
        public void onAdEnd() {

        }

        @Override
        public void onMiddleAdStart() {

        }

        @Override
        public void onMiddleAdEnd() {

        }
    };
    private IPlayer.OnBufferChangedListener onBufferChangedListener = new IPlayer.OnBufferChangedListener() {
        @Override
        public void onBufferStart() {

        }

        @Override
        public void onBufferEnd() {

        }
    };
    private IPlayer.OnStateChangedListener onStateChangedListener = new IPlayer.OnStateChangedListener() {
        @Override
        public void onPrepared() {
            if (hlsPlayer == null) {
                LogUtils.e(TAG, "Called onPrepared but player is null");
                return;
            }
            mIsPlayerPrepared = true;
            if (surfaceView != null && container != null) {
                // Service 与 PlaybackActivity绑定，直接播放视频
                hlsPlayer.attachSurfaceView(surfaceView, container);
            }

        }

        @Override
        public void onStarted() {
            if (serviceCallback != null) {
                serviceCallback.updatePlayPause(true);
            }

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onCompleted() {
            mIsPlayerPrepared = false;
        }

        @Override
        public void onInfo(int what, Object extra) {

        }

        @Override
        public void onVideoSizeChanged(int videoWidth, int videoHeight) {

        }

        @Override
        public boolean onError(String message) {
            mIsPlayerPrepared = false;
            return false;
        }
    };

    public interface ServiceCallback {

        void showAdCountDownTime(int count);// 单位为秒

        void updatePlayPause(boolean isPlaying);

        void showBuffering(boolean showBuffer);

    }

    public static class Client {

        @MainThread
        public interface Callback {
            void onConnected(PlaybackService service);

            void onDisconnected();
        }

        private boolean mBound = false;
        private final Callback mCallback;
        private final Context mContext;

        private final ServiceConnection mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                if (!mBound)
                    return;

                final PlaybackService service = PlaybackService.getService(iBinder);
                if (service != null)
                    mCallback.onConnected(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBound = false;
                mCallback.onDisconnected();
            }
        };

        private static Intent getServiceIntent(Context context) {
            return new Intent(context, PlaybackService.class);
        }

        private static void startService(Context context) {
            context.startService(getServiceIntent(context));
        }

        private static void stopService(Context context) {
            context.stopService(getServiceIntent(context));
        }

        public Client(Context context, Callback callback) {
            if (context == null || callback == null)
                throw new IllegalArgumentException("Context and callback can't be null");
            mContext = context;
            mCallback = callback;
        }

        @MainThread
        public void connect() {
            if (mBound)
                throw new IllegalStateException("already connected");
            startService(mContext);
            mBound = mContext.bindService(getServiceIntent(mContext), mServiceConnection, Context.BIND_AUTO_CREATE);
        }

        @MainThread
        public void disconnect() {
            if (mBound) {
                mBound = false;
                mContext.unbindService(mServiceConnection);
            }
        }

        public static void restartService(Context context) {
            stopService(context);
            startService(context);
        }
    }

    private void changeAudioFocus(boolean acquire) {
        final AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am == null)
            return;

        if (acquire) {
            if (!mHasAudioFocus) {
                final int result = am.requestAudioFocus(mAudioFocusListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    am.setParameters("bgm_state=true");
                    mHasAudioFocus = true;
                }
            }
        } else {
            if (mHasAudioFocus) {
                final int result = am.abandonAudioFocus(mAudioFocusListener);
                am.setParameters("bgm_state=false");
                mHasAudioFocus = false;
            }
        }
    }

    private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener = createOnAudioFocusChangeListener();

    private AudioManager.OnAudioFocusChangeListener createOnAudioFocusChangeListener() {
        return new AudioManager.OnAudioFocusChangeListener() {

            private boolean mLossTransient = false;
            private boolean mLossTransientCanDuck = false;

            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS:
                        // Stop playback
                        changeAudioFocus(false);
//                        stop();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        // Pause playback
//                        if (mMediaPlayer.isPlaying()) {
//                            mLossTransient = true;
//                            mMediaPlayer.pause();
//                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        // Lower the volume
//                        if (mMediaPlayer.isPlaying()) {
//                            mMediaPlayer.setVolume(36);
//                            mLossTransientCanDuck = true;
//                        }
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        // Resume playback
//                        if (mLossTransientCanDuck) {
//                            mMediaPlayer.setVolume(100);
//                            mLossTransientCanDuck = false;
//                        }
//                        if (mLossTransient) {
//                            mMediaPlayer.play();
//                            mLossTransient = false;
//                        }
                        break;
                }
            }
        };
    }

    public void addHistory(int last_position, boolean sendToServer, boolean isComplete) {
        if (mItemEntity == null || hlsPlayer == null || mIsPlayingAd) {
            return;
        }
        LogUtils.i(TAG, "addHistory");
        int completePosition = -1;
        if (isComplete) {
            completePosition = hlsPlayer.getDuration();
        }
        if (historyManager == null) {
            historyManager = VodApplication.getModuleAppContext().getModuleHistoryManager();
        }
        History history = new History();
        history.title = mItemEntity.getTitle();
        ItemEntity.Expense expense = mItemEntity.getExpense();
        if (expense != null) {
            history.price = (int) expense.getPrice();
            history.paytype = expense.getPay_type();
            history.cptitle = expense.getCptitle();
            history.cpid = expense.getCpid();
            history.cpname = expense.getCpname();
        } else
            history.price = 0;
        history.adlet_url = mItemEntity.getAdletUrl();
        history.content_model = mItemEntity.getContentModel();
        history.is_complex = mItemEntity.getIsComplex();
        history.last_position = last_position;
        ClipEntity.Quality quality = hlsPlayer.getCurrentQuality();
        if (quality != null) {
            history.last_quality = hlsPlayer.getCurrentQuality().getValue();
        }
        history.url = Utils.getItemUrl(itemPk);
        if (subItemPk > 0) {
            history.sub_url = Utils.getSubItemUrl(subItemPk);
        }
        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken()))
            historyManager.addHistory(history, "yes", completePosition);
        else
            historyManager.addHistory(history, "no", completePosition);

        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken()) && sendToServer) {
            int offset = last_position;
            if (last_position == hlsPlayer.getDuration()) {
                offset = -1;
            }
            HashMap<String, Object> params = new HashMap<>();
            params.put("offset", offset);
            if (subItemPk > 0) {
                params.put("subitem", subItemPk);
            } else {
                params.put("item", itemPk);
            }
            sendHistory(params);
        }

    }
}
