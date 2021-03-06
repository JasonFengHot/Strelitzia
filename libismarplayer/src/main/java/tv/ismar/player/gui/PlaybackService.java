package tv.ismar.player.gui;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.ismartv.truetime.TrueTime;
import okhttp3.ResponseBody;
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
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PlayCheckEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.bestvframework.BestActivator;
import tv.ismar.library.exception.ExceptionUtils;
import tv.ismar.library.network.HttpManager;
import tv.ismar.library.util.AppUtils;
import tv.ismar.library.util.DateUtils;
import tv.ismar.library.util.DeviceUtils;
import tv.ismar.library.util.LogUtils;
import tv.ismar.library.util.StringUtils;
import tv.ismar.player.IPlayer;
import tv.ismar.player.IsmartvPlayer;
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
    private String mQiyiUserType;// playerCheck ??????user??????
    // ????????????
    private HistoryManager historyManager;
    private History mHistory;
    public boolean hasHistory = false;
    // HLS?????????
    private IsmartvPlayer hlsPlayer;// HLS??????
    private ServiceCallback serviceCallback;
    private SurfaceView mSurfaceView;
    private ViewGroup mQiyiContainer;// ??????SDK?????????setDisplay??????????????????prepare

    private MediaEntity mPreloadMediaSource;
    private int mStartPosition;
    private int mDuration;
    private ClipEntity.Quality mCurrentQuality;
    private boolean mIsPreload;// ???????????????????????????????????????,?????????????????????????????????false
    private boolean mIsPlayerPrepared;// ????????????????????????????????????,onPrepared????????????true???
    private boolean mIsPreview;// ????????????
    private boolean isSwitchTelevision = false;// ???????????????????????????????????????
    private boolean mIsPlayingAd;// ??????????????????????????????
    private boolean mIsPlayerOnStarted;
    private boolean mIsPlayerStopping = false;// ?????????stop???release??????????????????
    public static long prepareStartTime;// ?????????????????????

/*add by dragontec for bug ?????????OTT?????????????????????????????? start*/
    private static int mBestAuthRequestCode = 0;
    private int mCurrentBestAuthRequestCode = 0;

    private class BestvAuthRunnable implements Runnable {
        private int requestCode;
        private ItemEntity itemEntity;

        public BestvAuthRunnable(int requestCode, ItemEntity itemEntity) {
            this.requestCode = requestCode;
            this.itemEntity = itemEntity;
        }

        @Override
        public void run() {
            Log.d(TAG, "BestvAuthRunnable current request code = " + mCurrentBestAuthRequestCode
                    + ", saved request code = " + requestCode);
            int episode = 1;
            if (itemEntity.getEpisode() == 0) {
                //???????????????
                episode = 1;
            } else if (itemEntity.getEpisode() > 1) {
                //????????????
                ItemEntity[] itemEntities = itemEntity.getSubitems();
                if (itemEntities != null) {
                    for (int i = 0; i < itemEntities.length; i++) {
                        if (itemEntity.getClip().getPk() == itemEntities[i].getClip().getPk()) {
                            episode = i + 1;
                            break;
                        }
                    }
                }
            }
            BestActivator.AuthProxyResultE result = BestActivator.getInstance(getApplicationContext()).
                    authProxy(itemEntity.getMedia_code(), String.valueOf(episode));
            if (mCurrentBestAuthRequestCode == requestCode) {
                if (BestActivator.authProxyResultISuccess(result)) {
                    if (mIsPreload) {
                        hlsPlayer.preparePreloadPlayer(mPreloadMediaSource);
                    } else {
                        hlsPlayer.prepare(mPreloadMediaSource, false);
                    }
                } else {
                    BestActivator.actionAuthProxyFailure();
                }
            }
        }
    }
/*add by dragontec for bug ?????????OTT?????????????????????????????? end*/

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
        // ????????????????????????????????????
        mIsPlayerPrepared = false;
        if (hlsPlayer != null) {
            hlsPlayer.setOnAdvertisementListener(null);
            hlsPlayer.setOnBufferChangedListener(null);
            hlsPlayer.setOnStateChangedListener(null);
            hlsPlayer.release();
            hlsPlayer = null;
        }

    }

/*add by dragontec for bug ?????????OTT?????????????????????????????? start*/
    private int generateBestAuthRequestCode() {
        int requestCode = mBestAuthRequestCode;
        ++mBestAuthRequestCode;
        return requestCode;
    }

    private void startBestAuthRunnable(ItemEntity itemEntity) {
        int requestCode = generateBestAuthRequestCode();
        mCurrentBestAuthRequestCode = requestCode;
        new Thread(new BestvAuthRunnable(requestCode, itemEntity)).start();
    }
/*add by dragontec for bug ?????????OTT?????????????????????????????? end*/

    public void initUserInfo() {
        snToken = IsmartvActivator.getInstance().getSnToken();
        deviceToken = IsmartvActivator.getInstance().getDeviceToken();
        authToken = IsmartvActivator.getInstance().getAuthToken();
        username = IsmartvActivator.getInstance().getUsername();
        zuserToken = IsmartvActivator.getInstance().getZUserToken();
        zdeviceToken = IsmartvActivator.getInstance().getDeviceToken();
    }

    /**
     * ?????????????????????????????????????????????????????????0?????????????????????????????????????????????
     */
    public void preparePlayer(int itemPk, int subItemPk, String source) {
        if (itemPk <= 0 || mSurfaceView == null || mQiyiContainer == null) {
            LogUtils.e(TAG, "itemPk and qiyiContainer can't be null.");
            return;
        }
        this.itemPk = itemPk;// ????????????pk???,??????/api/item/{pk}?????????????????????
        this.subItemPk = subItemPk;// ???????????????pk???,??????/api/subitem/{pk}?????????????????????
        this.source = source;
        initUserInfo();
        fetchPlayerItem(String.valueOf(itemPk));
    }

    // ??????????????????
    public void preparePlayer(ItemEntity itemEntity, String source) {
        if (itemEntity == null) {
            LogUtils.e(TAG, "ItemEntity can't be null.");
            return;
        }
        this.itemPk = itemEntity.getPk();// ????????????pk???,??????/api/item/{pk}?????????????????????
        this.subItemPk = 0;// ???????????????pk???,??????/api/subitem/{pk}?????????????????????
        this.source = source;
        this.mItemEntity = itemEntity;
        prepareStartTime = DateUtils.currentTimeMillis();
        cancelRequest();
        initUserInfo();
        mIsPreload = true;
        loadPlayerItem();
    }

    // ??????????????????????????????createPlayer????????????????????????????????????
    public void startPlayWhenPrepared() {
        // ??????????????????????????????????????????
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
/*add by dragontec for bug 4322 start*/
        synchronized (mAdLock)
        {
/*add by dragontec for bug 4322 end*/
            this.serviceCallback = serviceCallback;
/*add by dragontec for bug 4322 start*/
        }
/*add by dragontec for bug 4322 end*/
    }
/*add by dragontec for bug 4405 start*/
    public ServiceCallback getCallback()
    {
        synchronized (mAdLock) {
            return serviceCallback;
        }
    }
/*add by dragontec for bug 4405 end*/
    /**
     * @param detachView true?????????????????????UI????????????
     *                   false???????????????????????????
     */
    public void stopPlayer(boolean detachView) {
        // ????????????????????????????????????????????????Service,????????????????????????????????????
        mIsPreload = false;
        if (!mIsPlayerPrepared) {
            cancelRequest();
        }
        if (hlsPlayer != null) {
            mIsPlayerStopping = true;
            hlsPlayer.setOnAdvertisementListener(null);
            hlsPlayer.setOnBufferChangedListener(null);
            hlsPlayer.setOnStateChangedListener(null);

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
            // ???????????????
            if (historyManager == null) {
                historyManager = VodApplication.getModuleAppContext().getModuleHistoryManager();
            }
            historyManager.addOrUpdateQuality(new DBQuality(0, "", quality.getValue()));
        }
    }

    public void switchTelevision(int historyPosition, int subPk, String clipUrl) {
        isSwitchTelevision = true;
        subItemPk = subPk;
        addHistory(historyPosition, false);
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
        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
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
/*add by dragontec for bug 4312 start*/
        stopRetryLoadPlayItem();
/*add by dragontec for bug 4312 end*/
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

/*add by dragontec for bug 4312 start*/
    private static final long RETRY_DELAY_DURATION = 100;
    private static Object mRetryLock = new Object();
    private static Handler mRetryLoadItemHandle = null;
    private class RetryLoadItemRunnable implements Runnable
    {
        private boolean isDirectFetch = false;
        private String fetchUrl = null;

        public RetryLoadItemRunnable(boolean isDirect, String url)
        {
            isDirectFetch = isDirect;
            fetchUrl = url;
        }
        @Override
        public void run() {
            synchronized (mRetryLock) {
                if (mRetryLoadItemHandle != null) {
                    mRetryLoadItemHandle.removeCallbacks(this);
                }
                mRetryLoadItemHandle = null;
            }

            if(isBindActivity) {
                if(!isDirectFetch)
                {
                    loadPlayerItem();
                }
                else
                {
                    fetchClipUrl(fetchUrl);
                }

            }
            else
            {
                if(mRetryLoadItemHandle != null) {
                    mRetryLoadItemRunnable = new RetryLoadItemRunnable(isDirectFetch, fetchUrl);
                    mRetryLoadItemHandle.postDelayed(mRetryLoadItemRunnable, RETRY_DELAY_DURATION);
                }
            }

        }
    }

    private static RetryLoadItemRunnable mRetryLoadItemRunnable =null;

    private void retryLoadPlayItem(boolean isDirect, String url)
    {
        synchronized (mRetryLock)
        {
            if(mRetryLoadItemHandle == null)
            {
                mRetryLoadItemHandle = new Handler(Looper.getMainLooper());
            }
            if(mRetryLoadItemRunnable != null)
            {
                mRetryLoadItemHandle.removeCallbacks(mRetryLoadItemRunnable);
            }
            mRetryLoadItemRunnable = new RetryLoadItemRunnable(isDirect, url);

            mRetryLoadItemHandle.postDelayed(mRetryLoadItemRunnable, RETRY_DELAY_DURATION);
        }
    }

    private void stopRetryLoadPlayItem()
    {
        synchronized (mRetryLock)
        {
            if(mRetryLoadItemHandle != null && mRetryLoadItemRunnable != null)
            {
                mRetryLoadItemHandle.removeCallbacks(mRetryLoadItemRunnable);
            }
            mRetryLoadItemRunnable = null;
        }
    }
/*add by dragontec for bug 4312 end*/

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
/*add by dragontec for bug 4312 start*/
                                retryLoadPlayItem(false, null);
/*add by dragontec for bug 4312 end*/
                                return;
                            }
                            String result = null;
                            try {
                                result = responseBody.string();
                                LogUtils.i(TAG, "play check result:" + result);
                            } catch (IOException e) {
                                ExceptionUtils.sendProgramError(e);
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
                    ExceptionUtils.sendProgramError(e);
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
        String historyUrl = IsmartvActivator.getInstance().getApiDomain()+"/api/item/" + itemPk + "/";
//        String isLogin = "no";
//        if (!Utils.isEmptyText(authToken)) {
//            isLogin = "yes";
//        }
        mHistory = historyManager.getHistoryByUrl(historyUrl, "no");
        if(mHistory==null){
            if(!Utils.isEmptyText(authToken))
            mHistory = historyManager.getHistoryByUrl(historyUrl, "yes");
        }
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
                // ???????????????????????????????????????????????????????????????????????????subItemPk,?????????????????????
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
            // ?????????????????????????????????Clip
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
            // ????????????????????????????????????????????????????????????
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
            // ?????????????????????????????????
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
        // ?????????????????????
/*delete by dragontec for bug 4205 start*/
//        IsmartvPlayer.Builder builder = new IsmartvPlayer.Builder();
//        builder.setSnToken(snToken);
/*delete by dragontec for bug 4205 end*/
        if (Utils.isEmptyText(iqiyi)) {
            // ???????????????
            isSendlog=true;
/*add by dragontec for bug 4205 start*/
            IsmartvPlayer.Builder builder = new IsmartvPlayer.Builder();
            builder.setSnToken(snToken);
/*add by dragontec for bug 4205 end*/
            builder.setPlayerMode(IsmartvPlayer.MODE_SMART_PLAYER);
            builder.setDeviceToken(deviceToken);
            if (mIsPreload) {
                hlsPlayer = builder.buildPreloadPlayer();
            } else {
/*add by dragontec for bug 4205 start*/
				if (mSurfaceView == null) {
					LogUtils.i(TAG, "createPlayer mSurfaceView null return!");
					return;
				}
/*add by dragontec for bug 4205 end*/
                builder.setSurfaceView(mSurfaceView);
                hlsPlayer = builder.build();
            }
        } else {
/*modify by dragontec for bug 4205 start*/
//            isSendlog=false;
//            if (mQiyiContainer == null) {
//                throw new IllegalArgumentException("??????????????????????????????????????????");
//            }
            if (mQiyiContainer == null) {
                LogUtils.i(TAG, "createPlayer mQiyiContainer null return!");
                return;
            }
            isSendlog=false;
            IsmartvPlayer.Builder builder = new IsmartvPlayer.Builder();
            builder.setSnToken(snToken);
/*modify by dragontec for bug 4205 end*/
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
        // ????????????????????????
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
/*add by dragontec for bug ?????????OTT?????????????????????????????? start*/
        if (Utils.isEmptyText(iqiyi) && BestActivator.isEnabled()) {
            startBestAuthRunnable(mItemEntity);
        } else {
/*add by dragontec for bug ?????????OTT?????????????????????????????? end*/
            if (mIsPreload) {
                hlsPlayer.preparePreloadPlayer(mPreloadMediaSource);
            } else {
                hlsPlayer.prepare(mPreloadMediaSource, false);
            }
/*add by dragontec for bug ?????????OTT?????????????????????????????? start*/
        }
/*add by dragontec for bug ?????????OTT?????????????????????????????? end*/
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
/*modify by dragontec for bug 4312 start*/
//                        if (itemEntity == null || !isBindActivity) {
//                            Log.e(TAG, "Response item data null or activity unbind");
//                            return;
//                        }
//                        mItemEntity = itemEntity;
//                        loadPlayerItem();
                        if (itemEntity == null) {
                            Log.e(TAG, "Response item data null or activity unbind");
                            return;
                        }

                        mItemEntity = itemEntity;

                        loadPlayerItem();
/*modify by dragontec for bug 4312 end*/
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
/*add by dragontec for bug 4312 start*/
        final String tempClipUrl = clipUrl;
/*add by dragontec for bug 4312 end*/
        initUserInfo();
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
                            // TODO ???????????????????????????????????????????????????
                            mIsPreload = false;
                        }
                    }

                    @Override
                    public void onNext(ClipEntity clipEntity) {
/*modify by dragontec for bug 4312 start*/
//                        if (clipEntity == null || !isBindActivity) {
//                            Log.e(TAG, "Response clip data null or activity unbind");
//                            return;
//                        }
//                        loadPlayerClip(clipEntity);
                        if (clipEntity == null) {
                            Log.e(TAG, "clipEntity null do nothing");
                            return;
                        }

                        if(!isBindActivity)
                        {
                            retryLoadPlayItem(true, tempClipUrl);
                            return;
                        }
                        loadPlayerClip(clipEntity);
/*modify by dragontec for bug 4312 end*/
                    }
                });

    }

/*add by dragontec for bug 4322 start*/
    private Object mAdLock = new Object();
    private static final int AD_START = 0;
    private static final int AD_END = 1;
    private static final int MIDDLE_AD_START = 2;
    private static final int MIDDLE_AD_END = 3;

    private final long AD_DELAY_DURATION = 500;

    private Handler mAdHandle = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            synchronized (mAdLock)
            {
                if(serviceCallback == null)
                {
                    return;
                }
                int code = msg.what;
                switch (code)
                {
                    case AD_START:
                    {
                        if (hlsPlayer == null) {
                            return;
                        }
                        mIsPlayingAd = true;
                        if (serviceCallback != null && serviceCallback.isPrepared()) {
                            serviceCallback.showAdvertisement(true);
                            if(isSendlog) {
                                serviceCallback.sendAdlog(adElementEntityList);
                            }
//                Advertisement advertisement=new Advertisement()
                        }
                    }
                    break;
                    case AD_END:
                    {
                        if (hlsPlayer == null) {
                            return;
                        }
                        mIsPlayingAd = false;
                        if (serviceCallback != null && serviceCallback.isPrepared()) {
                            serviceCallback.showAdvertisement(false);
                        }
                    }
                    break;
                    case MIDDLE_AD_START:
                    {
                        if (hlsPlayer == null) {
                            return;
                        }
                        mIsPlayingAd = true;
                        if (serviceCallback != null && serviceCallback.isPrepared()) {
                            serviceCallback.showAdvertisement(true);
                        }
                    }
                    break;
                    case MIDDLE_AD_END:
                    {
                        if (hlsPlayer == null) {
                            return;
                        }
                        mIsPlayingAd = false;
                        if (serviceCallback != null && serviceCallback.isPrepared()) {
                            serviceCallback.showAdvertisement(false);
                        }
                    }
                    break;
                    default:
                        break;
                }
            }
        }
    };
/*add by dragontec for bug 4322 end*/


    private IPlayer.OnAdvertisementListener onAdvertisementListener = new IPlayer.OnAdvertisementListener() {
        @Override
        public void onAdStart() {
/*modify by dragontec for bug 4322 start*/
//            if (hlsPlayer == null) {
//                return;
//            }
//            mIsPlayingAd = true;
//            if (serviceCallback != null) {
//                serviceCallback.showAdvertisement(true);
//                if(isSendlog) {
//                    serviceCallback.sendAdlog(adElementEntityList);
//                }
////                Advertisement advertisement=new Advertisement()
//            }
            if (serviceCallback != null) {
                if (serviceCallback.isPrepared()) {
                    mAdHandle.sendEmptyMessage(AD_START);
                } else {
                    mAdHandle.sendEmptyMessageDelayed(AD_START, AD_DELAY_DURATION);
                }
            }
/*modify by dragontec for bug 4322 end*/
        }

        @Override
        public void onAdEnd() {
/*modify by dragontec for bug 4322 start*/
//            if (hlsPlayer == null) {
//                return;
//            }
//            mIsPlayingAd = false;
//            if (serviceCallback != null) {
//                serviceCallback.showAdvertisement(false);
//            }
            if (serviceCallback != null) {
                if (serviceCallback.isPrepared()) {
                    mAdHandle.sendEmptyMessage(AD_END);
                } else {
                    mAdHandle.sendEmptyMessageDelayed(AD_END, AD_DELAY_DURATION);
                }
            }
/*modify by dragontec for bug 4322 end*/
        }

        @Override
        public void onMiddleAdStart() {
/*modify by dragontec for bug 4322 start*/
//            if (hlsPlayer == null) {
//                return;
//            }
//            mIsPlayingAd = true;
//            if (serviceCallback != null) {
//                serviceCallback.showAdvertisement(true);
//            }
            if (serviceCallback != null) {
                if (serviceCallback.isPrepared()) {
                    mAdHandle.sendEmptyMessage(MIDDLE_AD_START);
                } else {
                    mAdHandle.sendEmptyMessageDelayed(MIDDLE_AD_START, AD_DELAY_DURATION);
                }
            }
/*modify by dragontec for bug 4322 end*/
        }

        @Override
        public void onMiddleAdEnd() {
/*modify by dragontec for bug 4322 start*/
//            if (hlsPlayer == null) {
//                return;
//            }
//            mIsPlayingAd = false;
//            if (serviceCallback != null) {
//                serviceCallback.showAdvertisement(false);
//            }
            if (serviceCallback != null) {
                if (serviceCallback.isPrepared()) {
                    mAdHandle.sendEmptyMessage(MIDDLE_AD_END);
                } else {
                    mAdHandle.sendEmptyMessageDelayed(MIDDLE_AD_END, AD_DELAY_DURATION);
                }
            }
/*modify by dragontec for bug 4322 end*/
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

/*add by dragontec for bug 4322 start*/
        boolean isPrepared();
/*add by dragontec for bug 4322 end*/
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
            String player = hlsPlayer.getPlayerType();
            int clipPk = mItemEntity.getClip() == null ? 0 : mItemEntity.getClip().getPk();
            float price = mItemEntity.getExpense() == null ? 0 : mItemEntity.getExpense().getPrice();
            new PurchaseStatistics().expenseVideoPreview(
                    itemPk,
                    clipPk,
                    username,
                    mItemEntity.getTitle(),
                    mItemEntity.getVendor(),
                    price,
                    player,
                    result,
                    position / 1000
            );
        }
    }

    // ????????????????????????
    public void addHistory(int position, boolean sendToServer) {
        if (mItemEntity == null || mIsPlayingAd || !mIsPlayerPrepared) {
            return;
        }
        LogUtils.i(TAG, "addHistory ??? " + position);
        int last_position = position;
        int completePosition = -1;
        if (mDuration - last_position <= 3000) {
            if (!mIsPreview) {
                last_position = 0;// ??????????????????
            }
            completePosition = mDuration;// ?????????????????????
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
        history.add_time=TrueTime.now().getTime();
        history.model_name=mItemEntity.getModel_name();
        ClipEntity.Quality quality = mCurrentQuality;
        if (quality != null) {
            history.last_quality = quality.getValue();
        }
        String url = "/api/item/" + itemPk + "/";
        String apiDomain = IsmartvActivator.getInstance().getApiDomain();
        history.url = apiDomain + url;
        if (subItemPk > 0) {
            history.sub_url = Utils.getSubItemUrl(subItemPk);
        }
        String isnet="no";
        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken())) {
            isnet = "yes";
            List<History> histories = historyManager.getAllHistories();
            Collections.sort(histories);
            if (histories.size() > 99) {
                historyManager.deleteHistoryByUrl(histories.get(histories.size() - 1).url, isnet);
            }
        }else {
            isnet = "no";
            List<History> histories = historyManager.getAllHistories(isnet);
            Collections.sort(histories);
            if (histories.size() > 49) {
                historyManager.deleteHistory(histories.get(histories.size() - 1).url, isnet);
            }
        }
        historyManager.addHistory(history, isnet, completePosition);
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
        SkyService.ServiceManager.getService().sendPlayHistory(history)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String result = responseBody.string();
                            Log.i(TAG, "SendHistory : " + result);
                        } catch (IOException e) {
                            ExceptionUtils.sendProgramError(e);
                            e.printStackTrace();
                        }
                    }
                });
    }

    // audio focus ??????
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
