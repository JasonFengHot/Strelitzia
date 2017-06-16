package tv.ismar.player;

import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.qiyi.sdk.player.IAdController;
import com.qiyi.sdk.player.Parameter;
import com.qiyi.sdk.player.PlayerSdk;
import com.qiyi.sdk.player.SdkVideo;
import com.qiyi.tvapi.type.DrmType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tv.ismar.app.entity.ClipEntity;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.library.util.AESTool;
import tv.ismar.library.util.DateUtils;
import tv.ismar.library.util.LogUtils;
import tv.ismar.library.util.MD5;
import tv.ismar.library.util.StringUtils;
import tv.ismar.player.event.PlayerEvent;
import tv.ismar.player.media.DaisyPlayer;
import tv.ismar.player.media.QiyiPlayer;
import tv.ismar.player.model.MediaEntity;
import tv.ismar.player.model.MediaMeta;

/**
 * Created by LongHai on 17-4-26.
 */

public abstract class IsmartvPlayer implements IPlayer {

    private static final String TAG = "LH/IsmartvPlayer";
    // 视云片源
    public static final byte MODE_SMART_PLAYER = 0x01;
    // 奇艺片源
    public static final byte MODE_QIYI_PLAYER = 0x02;

    protected static final int STATE_ERROR = -1;
    protected static final int STATE_IDLE = 0;
    protected static final int STATE_PREPARING = 1;
    protected static final int STATE_PREPARED = 2;
    protected static final int STATE_PLAYING = 3;
    protected static final int STATE_PAUSED = 4;
    protected static final int STATE_COMPLETED = 5;
    protected int mCurrentState = STATE_IDLE;
    protected MediaEntity mMediaEntity;
    protected boolean isPlayingAd;
    protected boolean mSurfaceAttached = false;// 播放器SurfaceView是否已设置，true表示在播放器界面，false表示在详情页
    protected OnBufferChangedListener onBufferChangedListener;
    protected OnAdvertisementListener onAdvertisementListener;
    protected OnStateChangedListener onStateChangedListener;
    // 日志上报相关
    protected HashMap<String, Integer> logAdMap = new HashMap<>();
    protected String logPlayerFlag = "";
    protected PlayerEvent logPlayerEvent = new PlayerEvent();
    protected long logPlayerOpenTime = 0;
    protected boolean logFirstOpenPlayer = true;// 播放器打开日志上报，surfaceDestroy后该标志位需恢复
    protected int logSpeed = 0;
    protected String logMediaIp = "";
    protected int logAdMediaId = 0;
    protected boolean logSeekStartPosition = false;// 设置起播位置时调用seekTo
    protected long logBufferStartTime;
    protected boolean logFirstBufferEnd;

    // 视云
    protected ClipEntity.Quality mCurrentQuality;
    protected List<ClipEntity.Quality> mQualities;
    protected int[] mBestvAdTime;
    private MediaMeta preloadMediaMeta;
    // 奇艺
    private boolean isQiyiSdkInit;

    // 视云播放器从预加载播放视频，按照要求预加载只做加载
    public void preparePreloadPlayer(MediaEntity mediaSource) {
        if (playerMode != MODE_SMART_PLAYER || mediaSource == null || mediaSource.getClipEntity() == null) {
            throw new IllegalArgumentException("preparePreloadPlayer mediaSource can't be null");
        }
        mMediaEntity = mediaSource;
        logPlayerEvent.pk = mediaSource.getPk();
        logPlayerEvent.subItemPk = mediaSource.getSubItemPk();
        preloadMediaMeta = bestvUserInit();
        if (preloadMediaMeta != null) {
            createPreloadPlayer(preloadMediaMeta);
        }
    }

    @Override
    public void prepare(MediaEntity mediaSource, boolean hasPreload) {
        if (mediaSource == null || mediaSource.getClipEntity() == null || (hasPreload && mSurfaceView == null)) {
            throw new IllegalArgumentException("prepare mediaSource can't be null");
        }
        if (!hasPreload) {
            mMediaEntity = mediaSource;
            logPlayerEvent.pk = mediaSource.getPk();
            logPlayerEvent.subItemPk = mediaSource.getSubItemPk();
        }
        logPlayerEvent.sid = MD5.getMd5ByString(snToken + DateUtils.currentTimeMillis());
        switch (playerMode) {
            case MODE_SMART_PLAYER:
                mSurfaceView.setVisibility(View.VISIBLE);
                if (mQiyiContainer != null) {
                    mQiyiContainer.setVisibility(View.GONE);
                }
                if (!hasPreload) {
                    preloadMediaMeta = bestvUserInit();
                }
                if (preloadMediaMeta != null) {
                    createPlayer(preloadMediaMeta, hasPreload);
                }
                break;
            case MODE_QIYI_PLAYER:
                mQiyiContainer.setVisibility(View.VISIBLE);
                if (mSurfaceView != null) {
                    mSurfaceView.setVisibility(View.GONE);
                }
                logPlayerFlag = "qiyi";
                if (isQiyiSdkInit) {
                    createPlayer(qiyiUserInit());
                    return;
                }
                if (TextUtils.isEmpty(versionCode) || TextUtils.isEmpty(snToken) || TextUtils.isEmpty(modelName)) {
                    throw new IllegalArgumentException("versionCode or snToken or modelName null.");
                }
                Parameter extraParams = new Parameter();
                extraParams.setInitPlayerSdkAfter(0);  //SDK初始化在调用initialize之后delay一定时间开始执行, 单位为毫秒.
                extraParams.setCustomerAppVersion(versionCode);      //传入客户App版本号
                extraParams.setDeviceId(snToken);   //传入deviceId, VIP项目必传, 登录和鉴权使用
                extraParams.setDeviceInfo(modelName);
                extraParams.setShowAdCountDown(false);
                extraParams.addAdsHint(Parameter.HINT_TYPE_SKIP_AD, "下"); // 跳过悦享看广告
                extraParams.addAdsHint(Parameter.HINT_TYPE_HIDE_PAUSE_AD, "下"); // 消除暂停广告
                extraParams.addAdsHint(Parameter.HINT_TYPE_SHOW_CLICK_THROUGH_AD, "右"); // 前贴,中插广告跳转页面
                PlayerSdk.getInstance().initialize(mQiyiContainer.getContext().getApplicationContext(), extraParams,
                        new PlayerSdk.OnInitializedListener() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "QiYiSdk init success.");
                                isQiyiSdkInit = true;
                                createPlayer(qiyiUserInit());
                            }

                            @Override
                            public void onFailed(int what, int extra) {
                                Log.e(TAG, "QiYiSdk init fail what = " + what + " extra = " + extra);
                                isQiyiSdkInit = false;
                                if (onStateChangedListener != null) {
                                    onStateChangedListener.onError("播放器初始化失败");
                                }
                            }
                        });
                break;
        }

        if (mSurfaceAttached && logFirstOpenPlayer) {
            // 沒有详情页，点击海报后直接进入播放器，第一次进入播放器
            logPlayerOpenTime = DateUtils.currentTimeMillis();
            PlayerEvent.videoStart(logPlayerEvent, logSpeed, logPlayerFlag);
        }

    }

    protected abstract boolean isInPlaybackState();

    @Override
    public void attachedView() {
        LogUtils.i(TAG, "AttachSurface->Player : " + playerMode);
        mSurfaceAttached = true;
        start();

    }

    @Override
    public void detachViews() {
        LogUtils.i(TAG, "detachViews");
        mSurfaceAttached = false;
        logFirstOpenPlayer = true;
        if (mSurfaceView != null) {
            mSurfaceView = null;
        }
        if (mQiyiContainer != null) {
            mQiyiContainer = null;
        }
    }

    @Override
    public void start() {
        if (mSurfaceAttached && logFirstOpenPlayer) {
            // 从详情页，点击播放按钮
            logFirstOpenPlayer = false;
            logPlayerOpenTime = DateUtils.currentTimeMillis();
            PlayerEvent.videoStart(logPlayerEvent, logSpeed, logPlayerFlag);
        }
        if (isInPlaybackState() && !isPlaying()) {
            if (mCurrentState == STATE_PAUSED) {
                PlayerEvent.videoPlayContinue(logPlayerEvent, logSpeed, getCurrentPosition(), logPlayerFlag);
            } else {
                PlayerEvent.videoPlayStart(logPlayerEvent, logSpeed, logPlayerFlag);
            }
        }
    }

    @Override
    public void pause() {
        if (isInPlaybackState() && mCurrentState == STATE_PLAYING) {
            PlayerEvent.videoPlayPause(logPlayerEvent, logSpeed, getCurrentPosition(), logPlayerFlag);
        }
    }

    @Override
    public void seekTo(int position) {
        if (isInPlaybackState()) {
            logBufferStartTime = DateUtils.currentTimeMillis();
            if (!logSeekStartPosition || playerMode == MODE_QIYI_PLAYER) {
                PlayerEvent.videoPlaySeek(logPlayerEvent, logSpeed, getCurrentPosition(), logPlayerFlag);
            }
        }
    }

    @Override
    public ClipEntity.Quality getCurrentQuality() {
        return mCurrentQuality;
    }

    @Override
    public List<ClipEntity.Quality> getQualities() {
        return mQualities;
    }

    @Override
    public void switchQuality(int position, ClipEntity.Quality quality) {
        logPlayerEvent.quality = qualityToInt(quality);
        PlayerEvent.videoSwitchStream(logPlayerEvent, "manual",
                logSpeed, logMediaIp, logPlayerFlag);
    }

    public abstract IAdController getAdController();

    public void logVideoExit(int exitPosition, String source) {
        PlayerEvent.videoExit(
                logPlayerEvent,
                logSpeed,
                source,
                exitPosition,
                (DateUtils.currentTimeMillis() - logPlayerOpenTime),
                logPlayerFlag);
    }

    public void logExpenseAdClick() {
        PlayerEvent.expenseAdClick(
                logPlayerEvent,
                logAdMediaId,
                DateUtils.currentTimeMillis(),
                logPlayerFlag
        );
    }

    private MediaMeta bestvUserInit() {
        String mediaUrl = initSmartQuality(mMediaEntity.getInitQuality());
        LogUtils.d(TAG, "clip:\n" + mMediaEntity.getClipEntity().toString());
        if (TextUtils.isEmpty(mediaUrl)) {
            if (onStateChangedListener != null) {
                onStateChangedListener.onError("播放地址解析错误");
            }
            PlayerEvent.videoExcept(
                    "noplayaddress", "noplayaddress",
                    logPlayerEvent, 0, 0, logPlayerFlag);
            return null;
        }
        MediaMeta mediaMeta = new MediaMeta();
        List<AdElementEntity> adList = mMediaEntity.getAdvStreamList();
        String[] mediaUrls;
        if (adList != null && !adList.isEmpty()) {
            mediaUrls = new String[adList.size() + 1];
            mBestvAdTime = new int[adList.size()];
            int i = 0;
            for (AdElementEntity element : adList) {
                if ("video".equals(element.getMedia_type())) {
                    mBestvAdTime[i] = element.getDuration();
                    mediaUrls[i] = element.getMedia_url();
                    // 日志上报
                    logAdMap.put(element.getMedia_url(), element.getMedia_id());
                    i++;
                }
            }
            mediaUrls[mediaUrls.length - 1] = mediaUrl;
        } else {
            mediaUrls = new String[]{mediaUrl};
        }
        mediaMeta.setUrls(mediaUrls);
        mediaMeta.setStartPosition(mMediaEntity.getStartPosition());
        return mediaMeta;
    }

    private SdkVideo qiyiUserInit() {
        String zdevice_token = zDeviceToken;
        String zuser_token = zUserToken;
        if (!TextUtils.isEmpty(qiyiUserType)) {
            if (qiyiUserType.equals("device") && !TextUtils.isEmpty(zdevice_token)) {
                PlayerSdk.getInstance().login(zdevice_token);
            } else if (qiyiUserType.equals("account") && !TextUtils.isEmpty(zuser_token)) {
                PlayerSdk.getInstance().login(zuser_token);
            }
        } else {
            if (!TextUtils.isEmpty(zuser_token)) {
                PlayerSdk.getInstance().login(zuser_token);
            } else if (!TextUtils.isEmpty(zdevice_token)) {
                PlayerSdk.getInstance().login(zdevice_token);
            }
        }
        ClipEntity clipEntity = mMediaEntity.getClipEntity();
        String[] array = clipEntity.getIqiyi_4_0().split(":");
        int drmType = DrmType.DRM_NONE;
        if (clipEntity.getDrm() != null && clipEntity.getDrm().equals("2")) {
            drmType = DrmType.DRM_INTERTRUST;
        }
        return new SdkVideo(array[0], array[1], clipEntity.is_vip(), drmType, mMediaEntity.getStartPosition(), null);
    }

    protected void createPlayer(MediaMeta mediaMeta, boolean hasPreload) {
    }

    protected void createPlayer(SdkVideo sdkVideo) {
    }

    protected void createPreloadPlayer(MediaMeta mediaMeta) {
    }

    protected IsmartvPlayer() {
        isQiyiSdkInit = false;
    }

    public static class Builder {

        private byte playerMode = -1;
        private String snToken;
        // 视云片源所需变量
        private String deviceToken;
        private SurfaceView surfaceView;
        // 奇艺片源所需变量
        private ViewGroup qiyiContainer;
        private String modelName;
        private String versionCode;
        private String qiyiUserType;// qiyi login condition
        private String zDeviceToken;// qiyi login param
        private String zUserToken;// qiyi login param

        public Builder setPlayerMode(byte playerMode) {
            this.playerMode = playerMode;
            return this;
        }

        public Builder setDeviceToken(String deviceToken) {
            this.deviceToken = deviceToken;
            return this;
        }

        public Builder setSurfaceView(SurfaceView surfaceView) {
            this.surfaceView = surfaceView;
            return this;
        }

        public Builder setSnToken(String snToken) {
            this.snToken = snToken;
            return this;
        }

        public Builder setQiyiContainer(ViewGroup viewGroup) {
            this.qiyiContainer = viewGroup;
            return this;
        }

        public Builder setModelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder setVersionCode(String versionCode) {
            this.versionCode = versionCode;
            return this;
        }

        public Builder setQiyiUserType(String qiyiUserType) {
            this.qiyiUserType = qiyiUserType;
            return this;
        }

        public Builder setzDeviceToken(String zDeviceToken) {
            this.zDeviceToken = zDeviceToken;
            return this;
        }

        public Builder setzUserToken(String zUserToken) {
            this.zUserToken = zUserToken;
            return this;
        }

        public IsmartvPlayer buildPreloadPlayer() {
            if (playerMode != MODE_SMART_PLAYER || TextUtils.isEmpty(snToken) || TextUtils.isEmpty(deviceToken)) {
                throw new IllegalAccessError("Must set variable before preload.");
            }
            IsmartvPlayer tvPlayer = new DaisyPlayer();
            tvPlayer.setPlayerMode(playerMode);
            tvPlayer.setSnToken(snToken);
            tvPlayer.setDeviceToken(deviceToken);
            return tvPlayer;
        }

        public IsmartvPlayer build() {
            if (playerMode <= 0 || TextUtils.isEmpty(snToken)) {
                throw new IllegalAccessError("Must call setPlayerMode first.");
            }
            IsmartvPlayer tvPlayer = null;
            switch (playerMode) {
                case MODE_SMART_PLAYER:
                    if (TextUtils.isEmpty(deviceToken) || surfaceView == null) {
                        throw new IllegalArgumentException("Must set deviceToken variable first.");
                    }
                    tvPlayer = new DaisyPlayer();
                    tvPlayer.setDeviceToken(deviceToken);
                    tvPlayer.setSurfaceView(surfaceView);
                    break;
                case MODE_QIYI_PLAYER:
                    if (TextUtils.isEmpty(snToken) || TextUtils.isEmpty(modelName)
                            || TextUtils.isEmpty(versionCode) || qiyiContainer == null) {
                        throw new IllegalArgumentException("Must set qiyi variable first.");
                    }
                    tvPlayer = new QiyiPlayer();
                    tvPlayer.setQiyiContainer(qiyiContainer);
                    tvPlayer.setModelName(modelName);
                    tvPlayer.setVersionCode(versionCode);
                    tvPlayer.setQiyiUserType(qiyiUserType);
                    tvPlayer.setzDeviceToken(zDeviceToken);
                    tvPlayer.setzUserToken(zUserToken);
                    break;
            }
            if (tvPlayer == null) {
                throw new IllegalArgumentException("Can't support player mode.");
            }
            tvPlayer.setPlayerMode(playerMode);
            tvPlayer.setSnToken(snToken);
            Log.d(TAG, "New Player Success : " + playerMode);
            return tvPlayer;
        }

    }

    private String initSmartQuality(ClipEntity.Quality initQuality) {
        if (mMediaEntity == null || mMediaEntity.getClipEntity() == null) {
            return null;
        }
        ClipEntity clipEntity = mMediaEntity.getClipEntity();
        mQualities = new ArrayList<>();
        String normal = clipEntity.getNormal();
        if (!TextUtils.isEmpty(normal)) {
            if (!normal.startsWith("http://") && !normal.startsWith("https://")) {
                // 如果normal地址中包含http地址表示该地址已经解密
                normal = AESTool.decrypt(normal, deviceToken);
            }
            clipEntity.setNormal(normal);
            mQualities.add(ClipEntity.Quality.QUALITY_NORMAL);
        }
        String medium = clipEntity.getMedium();
        if (!TextUtils.isEmpty(medium)) {
            if (!medium.startsWith("http://") && !medium.startsWith("https://")) {
                medium = AESTool.decrypt(medium, deviceToken);
            }
            clipEntity.setMedium(medium);
            mQualities.add(ClipEntity.Quality.QUALITY_MEDIUM);
        }
        String high = clipEntity.getHigh();
        if (!TextUtils.isEmpty(high)) {
            if (!high.startsWith("http://") && !high.startsWith("https://")) {
                high = AESTool.decrypt(high, deviceToken);
            }
            clipEntity.setHigh(high);
            mQualities.add(ClipEntity.Quality.QUALITY_HIGH);
        }
        String ultra = clipEntity.getUltra();
        if (!TextUtils.isEmpty(ultra)) {
            if (!ultra.startsWith("http://") && !ultra.startsWith("https://")) {
                ultra = AESTool.decrypt(ultra, deviceToken);
            }
            clipEntity.setUltra(ultra);
            mQualities.add(ClipEntity.Quality.QUALITY_ULTRA);
        }
        String blueray = clipEntity.getBlueray();
        if (!TextUtils.isEmpty(blueray)) {
            if (!blueray.startsWith("http://") && !blueray.startsWith("https://")) {
                blueray = AESTool.decrypt(blueray, deviceToken);
            }
            clipEntity.setBlueray(blueray);
            mQualities.add(ClipEntity.Quality.QUALITY_BLUERAY);
        }
        String _4k = clipEntity.get_4k();
        if (!TextUtils.isEmpty(_4k)) {
            if (!_4k.startsWith("http://") && !_4k.startsWith("https://")) {
                _4k = AESTool.decrypt(_4k, deviceToken);
            }
            clipEntity.set_4k(_4k);
            mQualities.add(ClipEntity.Quality.QUALITY_4K);
        }
        String defaultQualityUrl = null;
        if (!mQualities.isEmpty()) {
            // 历史记录保存的码率，需要先
            if (initQuality != null) {
                mCurrentQuality = initQuality;
                defaultQualityUrl = qualityToUrl(mCurrentQuality);
            }
            // 历史记录保存的码率，当前影片没有
            if (StringUtils.isEmpty(defaultQualityUrl)) {
                mCurrentQuality = mQualities.get(mQualities.size() - 1);
                defaultQualityUrl = qualityToUrl(mCurrentQuality);
            }
        }
        Log.i(TAG, "initDefaultQuality : " + mCurrentQuality.getValue());
        return defaultQualityUrl;
    }

    protected String qualityToUrl(ClipEntity.Quality quality) {
        if (mMediaEntity == null || mMediaEntity.getClipEntity() == null || quality == null) {
            return null;
        }
        ClipEntity clipEntity = mMediaEntity.getClipEntity();
        switch (quality) {
            case QUALITY_NORMAL:
                return clipEntity.getNormal();
            case QUALITY_MEDIUM:
                return clipEntity.getMedium();
            case QUALITY_HIGH:
                return clipEntity.getHigh();
            case QUALITY_ULTRA:
                return clipEntity.getUltra();
            case QUALITY_BLUERAY:
                return clipEntity.getBlueray();
            case QUALITY_4K:
                return clipEntity.get_4k();
            default:
                return null;
        }
    }

    private int qualityToInt(ClipEntity.Quality quality) {
        if (quality == null) {
            return -1;
        }
        switch (quality) {
            case QUALITY_NORMAL:
                return 0;
            case QUALITY_MEDIUM:
                return 1;
            case QUALITY_HIGH:
                return 2;
            case QUALITY_ULTRA:
                return 3;
            case QUALITY_BLUERAY:
                return 4;
            case QUALITY_4K:
                return 5;
            default:
                return -1;
        }
    }

    private byte playerMode = -1;
    private String snToken;
    // 视云片源创建播放器所需变量
    private String deviceToken;
    protected SurfaceView mSurfaceView;
    // 奇艺片源创建播放器所需变量
    protected ViewGroup mQiyiContainer;// 奇艺播放器prepared之前必须先setDisplay
    private String modelName;
    private String versionCode;
    private String qiyiUserType;// qiyi login condition
    private String zDeviceToken;// qiyi login param
    private String zUserToken;// qiyi login param

    public void setSurfaceView(SurfaceView surfaceView) {
        this.mSurfaceView = surfaceView;
    }

    private void setQiyiContainer(ViewGroup viewGroup) {
        this.mQiyiContainer = viewGroup;
    }

    private void setPlayerMode(byte playerMode) {
        this.playerMode = playerMode;
    }

    public byte getPlayerMode() {
        return playerMode;
    }

    private void setSnToken(String snToken) {
        this.snToken = snToken;
        logPlayerEvent.snToken = snToken;
    }

    protected String getSnToken() {
        return snToken;
    }

    private void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    private void setModelName(String modelName) {
        this.modelName = modelName;
    }

    private void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public void setQiyiUserType(String qiyiUserType) {
        this.qiyiUserType = qiyiUserType;
    }

    public void setzDeviceToken(String zDeviceToken) {
        this.zDeviceToken = zDeviceToken;
    }

    public void setzUserToken(String zUserToken) {
        this.zUserToken = zUserToken;
    }

    public void setOnBufferChangedListener(OnBufferChangedListener onBufferChangedListener) {
        this.onBufferChangedListener = onBufferChangedListener;
    }

    public void setOnAdvertisementListener(OnAdvertisementListener onAdvertisementListener) {
        this.onAdvertisementListener = onAdvertisementListener;
    }

    public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        this.onStateChangedListener = onStateChangedListener;
    }

    // 在播放器的 onStarted中调用一次
    public void setPlayerEvent(String username, String title, int clipPk, String channel, String section, String source, ClipEntity.Quality quality) {
        logPlayerEvent.username = username;
        logPlayerEvent.title = title;
        logPlayerEvent.clipPk = clipPk;
        logPlayerEvent.channel = channel;
        logPlayerEvent.section = section;
        logPlayerEvent.source = source;
        logPlayerEvent.quality = qualityToInt(quality);
    }


}
