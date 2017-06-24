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
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.google.gson.GsonBuilder;
import com.qiyi.sdk.player.IMediaPlayer;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import tv.ismar.app.db.HistoryManager;
import tv.ismar.app.entity.ClipEntity;
import tv.ismar.app.entity.DBQuality;
import tv.ismar.app.entity.History;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PlayCheckEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.library.network.HttpManager;
import tv.ismar.library.util.AppUtils;
import tv.ismar.library.util.DateUtils;
import tv.ismar.library.util.DeviceUtils;
import tv.ismar.library.util.LogUtils;
import tv.ismar.library.util.StringUtils;
import tv.ismar.player.IPlayer;
import tv.ismar.player.IsmartvPlayer;
import tv.ismar.player.event.PlayerEvent;
import tv.ismar.player.model.MediaEntity;
import tv.ismar.statistics.PurchaseStatistics;

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
    private Subscription mApiPlayCheckSubsc;
    private Advertisement mAdvertisement;

    private ItemEntity mItemEntity;
    private ClipEntity mClipEntity;
    private boolean isBindActivity = false;
    private String mQiyiUserType;// playerCheck 返回user类型
    // 历史记录
    private HistoryManager historyManager;
    private History mHistory;
    public boolean hasHistory = false;
    // HLS播放器
    private IsmartvPlayer hlsPlayer;// HLS播放
    private ServiceCallback serviceCallback;
    private SurfaceView mSurfaceView;
    private ViewGroup mQiyiContainer;// 奇艺SDK必须先setDisplay，然后再调用prepare

    private MediaEntity mPreloadMediaSource;
    private int mStartPosition;
    private int mDuration;
    private ClipEntity.Quality mCurrentQuality;
    private boolean mIsPreload;// 当前播放地址是否已经预加载,需要在详情页绑定前置为false
    private boolean mIsPlayerPrepared;// 播放器是否处于可播放状态,onPrepared回调后为true。
    private boolean mIsPreview;// 是否试看
    private boolean isSwitchTelevision = false;// 手动切换剧集，不查历史记录
    private boolean mIsPlayingAd;// 判断是否正在播放广告
    private boolean mIsPlayerOnStarted;
    private boolean mIsPlayerStopping = false;// 播放器stop，release需要时间较长
    private long prepareStartTime;// 预加载开始时间

    public PlaybackService() {
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        this.mSurfaceView = surfaceView;
    }

    public void setQiyiContainer(ViewGroup qiyiContainer) {
        this.mQiyiContainer = qiyiContainer;
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

    public ClipEntity.Quality getCurrentQuality() {
        return mCurrentQuality;
    }

    public boolean isPlayingAd() {
        return mIsPlayingAd;
    }

    public int getItemPk() {
        return itemPk;
    }

    public int getSubItemPk() {
        return subItemPk;
    }

    public boolean isPlayerStopping() {
        return mIsPlayerStopping;
    }

    public boolean isPreload() {
        return mIsPreload;
    }

    public void resetPreload() {
        mIsPreload = false;
    }

    public int getStartPosition() {
        return mStartPosition;
    }

    @Override
    public IBinder onBind(Intent intent) {
        isBindActivity = true;
        LogUtils.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        isBindActivity = true;
        LogUtils.d(TAG, "onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBindActivity = false;
        LogUtils.d(TAG, "onUnbind");
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAdvertisement = new Advertisement(this);
        mAdvertisement.setOnVideoPlayListener(this);
        initUserInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy");
        // 停止服务，播放器资源释放
        mIsPlayerPrepared = false;
        if (hlsPlayer != null) {
            hlsPlayer.setOnAdvertisementListener(null);
            hlsPlayer.setOnBufferChangedListener(null);
            hlsPlayer.setOnStateChangedListener(null);
            hlsPlayer.release();
            hlsPlayer = null;
        }

    }

    public void initUserInfo() {
        snToken = IsmartvActivator.getInstance().getSnToken();
        deviceToken = IsmartvActivator.getInstance().getDeviceToken();
        authToken = IsmartvActivator.getInstance().getAuthToken();
        username = IsmartvActivator.getInstance().getUsername();
        zuserToken = IsmartvActivator.getInstance().getZUserToken();
        zdeviceToken = IsmartvActivator.getInstance().getDeviceToken();
        HttpManager.getInstance().setAccessToken(authToken);
        HttpManager.getInstance().init(IsmartvActivator.getInstance().getApiDomain(), IsmartvActivator.getInstance().getUpgradeDomain(), deviceToken);
    }

    /**
     * 此方法调用必须是从播放器界面调用，没有0秒起播功能时才执行方法体内函数
     */
    public void preparePlayer(int itemPk, int subItemPk, String source) {
        if (itemPk <= 0 || mSurfaceView == null || mQiyiContainer == null) {
            LogUtils.e(TAG, "itemPk and qiyiContainer can't be null.");
            return;
        }
        this.itemPk = itemPk;// 当前影片pk值,通过/api/item/{pk}可获取详细信息
        this.subItemPk = subItemPk;// 当前多集片pk值,通过/api/subitem/{pk}可获取详细信息
        this.source = source;
        initUserInfo();
        fetchPlayerItem(String.valueOf(itemPk));
    }

    // 详情页预加载
    public void preparePlayer(ItemEntity itemEntity, String source) {
        if (itemEntity == null) {
            LogUtils.e(TAG, "ItemEntity can't be null.");
            return;
        }
        this.itemPk = itemEntity.getPk();// 当前影片pk值,通过/api/item/{pk}可获取详细信息
        this.subItemPk = 0;// 当前多集片pk值,通过/api/subitem/{pk}可获取详细信息
        this.source = source;
        this.mItemEntity = itemEntity;
        prepareStartTime = DateUtils.currentTimeMillis();
        cancelRequest();
        initUserInfo();
        mIsPreload = true;
        loadPlayerItem();
    }

    // 详情页点击播放，注意createPlayer方法中预加载是否已经完成
    public void startPlayWhenPrepared() {
        // 执行此方法时，预加载已经完成
        LogUtils.i(TAG, "startPlayWhenPrepared : " + hlsPlayer);
        if (hlsPlayer != null) {
            hlsPlayer.setSurfaceView(mSurfaceView);
        }
        if (mIsPreload && mPreloadMediaSource != null && hlsPlayer != null && hlsPlayer.getPlayerMode() == IsmartvPlayer.MODE_SMART_PLAYER) {
            hlsPlayer.prepare(mPreloadMediaSource, true);
        }
        mIsPreload = false;
    }

    public void setCallback(ServiceCallback serviceCallback) {
        this.serviceCallback = serviceCallback;
    }

    /**
     * @param detachView true：表示在播放器UI页面调用
     *                   false：表示在详情页调用
     */
    public void stopPlayer(boolean detachView) {
        // 无论从播放器界面，还是详情页解绑Service,都需要将当前网络请求取消
        mIsPreload = false;
        if (!mIsPlayerPrepared) {
            cancelRequest();
        }
        if (hlsPlayer != null) {
            mIsPlayerStopping = true;
            hlsPlayer.setOnAdvertisementListener(null);
            hlsPlayer.setOnBufferChangedListener(null);
            hlsPlayer.setOnStateChangedListener(null);
            hlsPlayer.setOnPreloadCompletedListener(null);

            hlsPlayer.release();
//            if (hlsPlayer.getPlayerMode() == IsmartvPlayer.MODE_QIYI_PLAYER) {
//                hlsPlayer.release();
//            } else {
//                hlsPlayer.stop();
//            }
            if (detachView) {
                hlsPlayer.detachViews();
            }
            hlsPlayer = null;
            mIsPlayerStopping = false;
        }
    }

    public void startPlayer() {
        if (hlsPlayer != null && !hlsPlayer.isPlaying()) {
            hlsPlayer.start();
        }
    }

    public void pausePlayer() {
        if (hlsPlayer != null) {
            hlsPlayer.pause();
        }
    }

    public void switchQuality(int historyPosition, ClipEntity.Quality quality) {
        if (hlsPlayer != null) {
            addHistory(historyPosition, false);
            mCurrentQuality = quality;
            hlsPlayer.switchQuality(historyPosition, quality);
            // 写入数据库
            if (historyManager == null) {
                historyManager = VodApplication.getModuleAppContext().getModuleHistoryManager();
            }
            historyManager.addOrUpdateQuality(new DBQuality(0, "", quality.getValue()));
        }
    }

    public void switchTelevision(int historyPosition, int subPk, String clipUrl) {
        addHistory(historyPosition, false);
        isSwitchTelevision = true;
        subItemPk = subPk;
        fetchClipUrl(clipUrl);
    }

    public void onResumeFromKefu() {
        initVariable();
        createPlayer(null);
    }

    private void initVariable() {
        mDuration = 0;
        mIsPlayerPrepared = false;
        mIsPlayingAd = false;
        mIsPlayerOnStarted = false;
        if (serviceCallback != null) {
            serviceCallback.updatePlayerStatus(PlayerStatus.CREATING, null);
        }
        // 每次进入创建播放器前先获取历史记录，历史播放位置，历史分辨率，手动切换剧集例外
        if (!isSwitchTelevision) {
            if (mCurrentQuality == null) {
                DBQuality dbQuality = historyManager.getQuality();
                if (dbQuality != null) {
                    mCurrentQuality = ClipEntity.Quality.getQuality(dbQuality.quality);
                }
            }
            if (mIsPreview) {
                mStartPosition = 0;
            }
        }
        if (mIsPreview || isSwitchTelevision) {
            mStartPosition = 0;
        }
        isSwitchTelevision = false;

    }

    private void cancelRequest() {
        if (mApiItemSubsc != null && !mApiItemSubsc.isUnsubscribed()) {
            mApiItemSubsc.unsubscribe();
        }
        if (mApiMediaUrlSubsc != null && !mApiMediaUrlSubsc.isUnsubscribed()) {
            mApiMediaUrlSubsc.unsubscribe();
        }
        if (mApiPlayCheckSubsc != null && !mApiPlayCheckSubsc.isUnsubscribed()) {
            mApiPlayCheckSubsc.unsubscribe();
        }
        if (mAdvertisement != null) {
            mAdvertisement.stopSubscription();
        }
    }

    private void loadPlayerItem() {
        initHistory();
        // playCheck
        final ItemEntity.Clip playCheckClip = mItemEntity.getClip();
        mIsPreview = false;
        if (mItemEntity.getExpense() != null) {
            if (mApiPlayCheckSubsc != null && !mApiPlayCheckSubsc.isUnsubscribed()) {
                mApiPlayCheckSubsc.unsubscribe();
            }
            mApiPlayCheckSubsc = HttpManager.getDomainService(SkyService.class).apiPlayCheck(String.valueOf(mItemEntity.getPk()), null, null)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            LogUtils.e(TAG, "play check onError");
                            if (!isBindActivity) {
                                LogUtils.d(TAG, "Activity unbind on play check");
                                return;
                            }
                            videoPreview();
                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            if (!isBindActivity) {
                                LogUtils.d(TAG, "Activity unbind on play check");
                                return;
                            }
                            String result = null;
                            try {
                                result = responseBody.string();
                                LogUtils.i(TAG, "play check result:" + result);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (!Utils.isEmptyText(result)) {
                                PlayCheckEntity playCheckEntity = calculateRemainDay(result);
                                mQiyiUserType = playCheckEntity.getUser();
                                if (playCheckEntity.getRemainDay() > 0) {
                                    fetchClipUrl(playCheckClip.getUrl());
                                    return;
                                }
                            }
                            videoPreview();
                        }
                    });
        } else {
            fetchClipUrl(playCheckClip.getUrl());
        }
    }

    private PlayCheckEntity calculateRemainDay(String info) {
        PlayCheckEntity playCheckEntity;
        switch (info) {
            case "0":
                playCheckEntity = new PlayCheckEntity();
                playCheckEntity.setRemainDay(0);
                break;
            default:
                playCheckEntity = new GsonBuilder().create().fromJson(info, PlayCheckEntity.class);
                int remainDay;
                try {
                    remainDay = Utils.daysBetween(Utils.getTime(), playCheckEntity.getExpiry_date()) + 1;
                } catch (ParseException e) {
                    remainDay = 0;
                }
                playCheckEntity.setRemainDay(remainDay);
                break;
        }
        return playCheckEntity;
    }

    private void initHistory() {
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
            mStartPosition = (int) mHistory.last_position;
            hasHistory = true;
        } else {
            mStartPosition = 0;
        }
        Log.i(TAG, "initHistory:" + mStartPosition);
        ItemEntity[] subItems = mItemEntity.getSubitems();
        if (subItems != null && subItems.length > 0) {
            int history_sub_pk = 0;
            if (mHistory != null) {
                history_sub_pk = Utils.getItemPk(mHistory.sub_url);
            }
            if (subItemPk <= 0) {
                // 点击播放按钮时，如果有历史记录，应该播放历史记录的subItemPk,默认播放第一集
                if (history_sub_pk > 0) {
                    subItemPk = history_sub_pk;
                    mStartPosition = (int) mHistory.last_position;
                } else {
                    subItemPk = subItems[0].getPk();
                    mStartPosition = 0;
                }
            } else {
                if (subItemPk != history_sub_pk) {
                    mStartPosition = 0;
                }
            }
            Log.i(TAG, "loadItem-ExtraSubItemPk:" + subItemPk + " historySubPk:" + history_sub_pk + " historyPosition:" + mStartPosition);
            // 获取当前要播放的电视剧Clip
            for (ItemEntity subItem : subItems) {
                int _subItemPk = subItem.getPk();
                if (subItemPk == _subItemPk) {
                    ItemEntity.Clip clip = subItem.getClip();
                    mItemEntity.setTitle(subItem.getTitle());
                    mItemEntity.setClip(clip);
                    break;
                }
            }
        }
    }

    private void videoPreview() {
        ItemEntity.Preview preview = mItemEntity.getPreview();
        if (preview != null) {
            mIsPreview = true;
            mItemEntity.setLiveVideo(false);
            fetchClipUrl(preview.getUrl());
        } else {
            // 试看影片，没有试看地址，直接回调试看结束
            if (serviceCallback != null) {
                serviceCallback.updatePlayerStatus(PlayerStatus.COMPLETED, true);
            }
        }
    }

    private void loadPlayerClip(ClipEntity clipEntity) {
        mClipEntity = clipEntity;
        initVariable();
        String iqiyi = mClipEntity.getIqiyi_4_0();
        if (!mIsPreview && Utils.isEmptyText(iqiyi) && !mItemEntity.getLiveVideo()) {
            // 视云影片获取前贴片广告
            mAdvertisement.fetchVideoStartAd(mItemEntity, Advertisement.AD_MODE_ONSTART, source);
        } else {
            createPlayer(null);
        }
    }
    private List<AdElementEntity> adElementEntityList=new ArrayList<>();
    private boolean isSendlog=false;
    @Override
    public void loadVideoStartAd(List<AdElementEntity> adList) {
        createPlayer(adList);
        adElementEntityList=adList;
    }

    private void createPlayer(List<AdElementEntity> adList) {
        LogUtils.i(TAG, "createPlayer 1 : " + mIsPreload);
        String iqiyi = mClipEntity.getIqiyi_4_0();
        // 创建当前播放器
        IsmartvPlayer.Builder builder = new IsmartvPlayer.Builder();
        builder.setSnToken(snToken);
        if (Utils.isEmptyText(iqiyi)) {
            // 片源为视云
            isSendlog=true;
            builder.setPlayerMode(IsmartvPlayer.MODE_SMART_PLAYER);
            builder.setDeviceToken(deviceToken);
            if (mIsPreload) {
                hlsPlayer = builder.buildPreloadPlayer();
            } else {
                if (mSurfaceView == null) {
                    throw new IllegalArgumentException("视云播放器，显示组件不能为空");
                }
                builder.setSurfaceView(mSurfaceView);
                hlsPlayer = builder.build();
            }
        } else {
            isSendlog=false;
            if (mQiyiContainer == null) {
                throw new IllegalArgumentException("奇艺播放器，显示组件不能为空");
            }
            builder.setPlayerMode(IsmartvPlayer.MODE_QIYI_PLAYER);
            builder.setQiyiContainer(mQiyiContainer);
            builder.setModelName(DeviceUtils.getModelName());
            builder.setVersionCode(String.valueOf(AppUtils.getVersionCode(this)));
            builder.setQiyiUserType(mQiyiUserType);
            builder.setzDeviceToken(zdeviceToken);
            builder.setzUserToken(zuserToken);
            hlsPlayer = builder.build();
        }
        hlsPlayer.setOnAdvertisementListener(onAdvertisementListener);
        hlsPlayer.setOnBufferChangedListener(onBufferChangedListener);
        hlsPlayer.setOnStateChangedListener(onStateChangedListener);
        // 日志上报用到变量
        int clipPk = mItemEntity.getClip() == null ? 0 : mItemEntity.getClip().getPk();
        hlsPlayer.setPlayerEvent(username, mItemEntity.getTitle(), clipPk,
                BaseActivity.baseChannel, BaseActivity.baseSection, source);
        LogUtils.i(TAG, "createPlayer 2 : " + mIsPreload);
        mPreloadMediaSource = new MediaEntity(itemPk, subItemPk, mItemEntity.getLiveVideo(), mClipEntity);
        if (adList != null && !adList.isEmpty()) {
            mPreloadMediaSource.setAdvStreamList(adList);
        }
        mPreloadMediaSource.setInitQuality(mCurrentQuality);
        mPreloadMediaSource.setStartPosition(mStartPosition);
        if (mIsPreload) {
            hlsPlayer.preparePreloadPlayer(mPreloadMediaSource, onPreloadCompletedListener);
        } else {
            hlsPlayer.prepare(mPreloadMediaSource, false);
        }
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
                        if (serviceCallback != null) {
                            serviceCallback.updatePlayerStatus(PlayerStatus.RESPONSE_ERROR, e);
                        }
                    }

                    @Override
                    public void onNext(ItemEntity itemEntity) {
                        if (itemEntity == null || !isBindActivity) {
                            Log.e(TAG, "Response item data null or activity unbind");
                            return;
                        }
                        mItemEntity = itemEntity;
                        loadPlayerItem();
                    }
                });

    }

    private void fetchClipUrl(String clipUrl) {
        if (mApiMediaUrlSubsc != null && !mApiMediaUrlSubsc.isUnsubscribed()) {
            mApiMediaUrlSubsc.unsubscribe();
        }
        if (StringUtils.isEmpty(clipUrl)) {
            LogUtils.e(TAG, "clipUrl is null.");
            return;
        }
        if (mStartPosition > 0 && !mItemEntity.getLiveVideo() && !mIsPreview && serviceCallback != null) {
            serviceCallback.updatePlayerStatus(PlayerStatus.CONTINUE_BUFFERING, mStartPosition);
        }
        String sign = "";
        String code = "1";
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
                        if (serviceCallback != null) {
                            serviceCallback.updatePlayerStatus(PlayerStatus.RESPONSE_ERROR, e);
                        }
                        LogUtils.i(TAG, "fetchClip onError");
                        if (mIsPreload) {
                            // TODO 预加载时，会出现此接口请求失败情况
                            mIsPreload = false;
                        }
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

    private IPlayer.OnPreloadCompletedListener onPreloadCompletedListener = new IPlayer.OnPreloadCompletedListener() {
        @Override
        public void onPreloadCompleted() {
            String quality = "";
            if (mCurrentQuality != null) {
                switch (mCurrentQuality) {
                    case QUALITY_NORMAL:
                        quality = "normal";
                        break;
                    case QUALITY_MEDIUM:
                        quality = "medium";
                        break;
                    case QUALITY_HIGH:
                        quality = "high";
                        break;
                    case QUALITY_ULTRA:
                        quality = "ultra";
                        break;
                    case QUALITY_BLUERAY:
                        quality = "blueray";
                        break;
                    case QUALITY_4K:
                        quality = "4k";
                        break;
                }
            }
            HashMap<String, Object> dataCollectionProperties = new HashMap<>();
            dataCollectionProperties.put(EventProperty.CLIP, mItemEntity.getClip().getPk());
            dataCollectionProperties.put(EventProperty.DURATION, DateUtils.currentTimeMillis() - prepareStartTime);
            dataCollectionProperties.put(EventProperty.QUALITY, quality);
            dataCollectionProperties.put(EventProperty.TITLE, mItemEntity.getTitle());
            dataCollectionProperties.put(EventProperty.ITEM, mItemEntity.getPk());
            dataCollectionProperties.put(EventProperty.SUBITEM, mItemEntity.getItemPk());
            dataCollectionProperties.put(EventProperty.LOCATION, "player");
            new PlayerEvent.DataCollectionTask().execute(PlayerEvent.DETAIL_PLAY_LOAD, dataCollectionProperties);
        }
    };

    private IPlayer.OnAdvertisementListener onAdvertisementListener = new IPlayer.OnAdvertisementListener() {
        @Override
        public void onAdStart() {
            if (hlsPlayer == null) {
                return;
            }
            mIsPlayingAd = true;
            if (serviceCallback != null) {
                serviceCallback.showAdvertisement(true);
                if(isSendlog) {
                    serviceCallback.sendAdlog(adElementEntityList);
                }
//                Advertisement advertisement=new Advertisement()
            }
        }

        @Override
        public void onAdEnd() {
            if (hlsPlayer == null) {
                return;
            }
            mIsPlayingAd = false;
            if (serviceCallback != null) {
                serviceCallback.showAdvertisement(false);
            }
        }

        @Override
        public void onMiddleAdStart() {
            if (hlsPlayer == null) {
                return;
            }
            mIsPlayingAd = true;
            if (serviceCallback != null) {
                serviceCallback.showAdvertisement(true);
            }
        }

        @Override
        public void onMiddleAdEnd() {
            if (hlsPlayer == null) {
                return;
            }
            mIsPlayingAd = false;
            if (serviceCallback != null) {
                serviceCallback.showAdvertisement(false);
            }
        }
    };
    private IPlayer.OnBufferChangedListener onBufferChangedListener = new IPlayer.OnBufferChangedListener() {
        @Override
        public void onBufferStart() {
            if (hlsPlayer == null) {
                return;
            }
            if (serviceCallback != null) {
                serviceCallback.showBuffering(true);
            }
        }

        @Override
        public void onBufferEnd() {
            if (hlsPlayer == null) {
                LogUtils.e(TAG, "onBufferEnd > player null");
                return;
            }
            boolean flag = hlsPlayer.getPlayerMode() == IsmartvPlayer.MODE_QIYI_PLAYER
                    || mItemEntity.getLiveVideo()
                    || (hlsPlayer.isPlaying() && mIsPlayerPrepared);
            LogUtils.i(TAG, "onBufferEnd : " + flag);
            if (flag && serviceCallback != null) {
                serviceCallback.showBuffering(false);
            }

        }
    };
    private IPlayer.OnStateChangedListener onStateChangedListener = new IPlayer.OnStateChangedListener() {
        @Override
        public void onPrepared() {
            LogUtils.d(TAG, "onPrepared");
            if (hlsPlayer == null) {
                LogUtils.e(TAG, "Called onPrepared but player is null");
                return;
            }
            mIsPlayerPrepared = true;
            changeAudioFocus(true);
            hlsPlayer.attachedView();

        }

        @Override
        public void onStarted() {
            LogUtils.d(TAG, "onStarted : " + mIsPlayerOnStarted);
            if (hlsPlayer == null) {
                LogUtils.e(TAG, "Called onStarted but player is null");
                return;
            }
            if (!mIsPlayerOnStarted) {
                mIsPlayerOnStarted = true;
                mCurrentQuality = hlsPlayer.getCurrentQuality();
                mDuration = hlsPlayer.getDuration();
                if (serviceCallback != null) {
                    serviceCallback.updatePlayerStatus(PlayerStatus.START, null);
                }
            }
            if (!mIsPlayingAd) {
                if (serviceCallback != null) {
                    serviceCallback.updatePlayerStatus(PlayerStatus.PLAY, null);
                }
            }
        }

        @Override
        public void onPaused() {
            LogUtils.d(TAG, "onPaused");
            if (hlsPlayer == null) {
                return;
            }
            if (serviceCallback != null) {
                serviceCallback.updatePlayerStatus(PlayerStatus.PAUSE, null);
            }
        }

        @Override
        public void onSeekCompleted() {
            LogUtils.d(TAG, "onSeekCompleted");
            if (hlsPlayer == null) {
                return;
            }
            if (serviceCallback != null) {
                serviceCallback.updatePlayerStatus(PlayerStatus.SEEK_COMPLETED, null);
            }
        }

        @Override
        public void onCompleted() {
            LogUtils.d(TAG, "onCompleted");
            if (hlsPlayer == null) {
                return;
            }
            if (serviceCallback != null) {
                serviceCallback.updatePlayerStatus(PlayerStatus.COMPLETED, isPreview());
            }
        }

        @Override
        public void onInfo(int what, Object extra) {
            if (hlsPlayer == null) {
                return;
            }
            LogUtils.i(TAG, "onInfo:" + what + " extra:" + extra);
            switch (what) {
                case IMediaPlayer.MEDIA_INFO_MIDDLE_AD_COMING:
                    // 即将进入爱奇艺广告,不可快进操作
                    if (serviceCallback != null) {
                        serviceCallback.tipsToShowMiddleAd(false);
                    }
                    break;
                case IMediaPlayer.MEDIA_INFO_MIDDLE_AD_SKIPPED:
                    // 爱奇艺中插广告播放结束
                    if (serviceCallback != null) {
                        serviceCallback.tipsToShowMiddleAd(true);
                    }
                    break;
            }
        }

        @Override
        public void onVideoSizeChanged(int videoWidth, int videoHeight) {
            if (hlsPlayer == null) {
                return;
            }
            if (serviceCallback != null) {
                serviceCallback.updatePlayerStatus(PlayerStatus.S3DEVICE_VIDEO_SIZE, null);
            }
        }

        @Override
        public boolean onError(String message) {
            if (hlsPlayer == null) {
                return true;
            }
            if (serviceCallback != null) {
                serviceCallback.updatePlayerStatus(PlayerStatus.ERROR, message);
            }
            return true;
        }

        @Override
        public void onTsInfo(Map<String, String> map) {
            String CacheTime = map.get("TsCacheTime");
            if (CacheTime != null) {
                Long nCacheTime = Long.parseLong(CacheTime);
                if (serviceCallback != null)
                    serviceCallback.onBufferUpdate(nCacheTime);
                Log.i(TAG, "current cache total time:" + nCacheTime);
            }
        }
    };

    public interface ServiceCallback {

        void updatePlayerStatus(PlayerStatus status, Object args);

        void tipsToShowMiddleAd(boolean end);

        void showAdvertisement(boolean isShow);

        void showBuffering(boolean showBuffer);

        void onBufferUpdate(long value);

        void sendAdlog(List<AdElementEntity> adlist);
    }

    enum PlayerStatus {

        CREATING, START, PLAY, PAUSE, SEEK_COMPLETED, COMPLETED, ERROR, S3DEVICE_VIDEO_SIZE, CONTINUE_BUFFERING, RESPONSE_ERROR

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

        public static void stopService(Context context) {
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
            LogUtils.d(TAG, "PlaybackService disconnect : " + mBound);
            if (mBound) {
                mBound = false;
                mContext.unbindService(mServiceConnection);
            }
        }
    }

    public void logVideoExit(int position, String to) {
        if (hlsPlayer != null) {
            hlsPlayer.logVideoExit(position, to);
        }
    }

    public void logExpenseVideoPreview(int position, String result) {
        if (hlsPlayer != null) {
            String player = hlsPlayer.getPlayerMode() == IsmartvPlayer.MODE_QIYI_PLAYER ? "qiyi" : "bestv";
            int clipPk = mItemEntity.getClip() == null ? 0 : mItemEntity.getClip().getPk();
            float price = mItemEntity.getExpense() == null ? 0 : mItemEntity.getExpense().getPrice();
            int duration = position;
            if (result.equals("purchase")) {
                duration = hlsPlayer.getDuration();
            }
            new PurchaseStatistics().expenseVideoPreview(
                    itemPk,
                    clipPk,
                    username,
                    mItemEntity.getTitle(),
                    mItemEntity.getVendor(),
                    price,
                    player,
                    result,
                    duration / 1000
            );
        }
    }

    // 添加历史播放数据
    public void addHistory(int position, boolean sendToServer) {
        if (mItemEntity == null || mIsPlayingAd || !mIsPlayerPrepared) {
            return;
        }
        LogUtils.i(TAG, "addHistory ： " + position);
        int last_position = position;
        int completePosition = -1;
        if (mDuration - last_position <= 3000) {
            if (!mIsPreview) {
                last_position = 0;// 当前播放结束
            }
            completePosition = mDuration;// 用于日志上报中
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
        ClipEntity.Quality quality = mCurrentQuality;
        if (quality != null) {
            history.last_quality = quality.getValue();
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
            if (last_position == mDuration) {
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

    private void sendHistory(HashMap<String, Object> history) {
        Call<ResponseBody> call = HttpManager.getDomainService(SkyService.class).sendPlayHistory(history);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String result = null;
                try {
                    result = response.body().string();
                } catch (Exception e) {
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

    // audio focus 相关
    private boolean mHasAudioFocus = false;

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
                        LogUtils.d(TAG, "onAudioFocusChange : " + AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);
                        if (hlsPlayer != null && hlsPlayer.isPlaying()) {
                            mLossTransient = true;
                            hlsPlayer.pause();
                        }
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
                        LogUtils.d(TAG, "onAudioFocusChange : " + AudioManager.AUDIOFOCUS_GAIN);
                        if (mLossTransient) {
                            if (hlsPlayer != null) {
                                hlsPlayer.start();
                            }
                            mLossTransient = false;
                        }
                        break;
                }
            }
        };
    }

}
