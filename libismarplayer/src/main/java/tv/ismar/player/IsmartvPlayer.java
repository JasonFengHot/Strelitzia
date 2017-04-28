package tv.ismar.player;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.qiyi.sdk.player.Parameter;
import com.qiyi.sdk.player.PlayerSdk;
import com.qiyi.sdk.player.SdkVideo;
import com.qiyi.tvapi.type.DrmType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tv.ismar.library.util.AESTool;
import tv.ismar.library.util.DateUtils;
import tv.ismar.library.util.MD5;
import tv.ismar.library.util.StringUtils;
import tv.ismar.player.event.PlayerEvent;
import tv.ismar.player.media.DaisyPlayer;
import tv.ismar.player.media.QiyiPlayer;
import tv.ismar.player.model.AdvEntity;
import tv.ismar.player.model.ClipEntity;
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

    protected MediaEntity mMediaEntity;
    protected boolean isPlayingAd;
    protected OnBufferChangedListener onBufferChangedListener;
    protected OnAdvertisementListener onAdvertisementListener;
    protected OnStateChangedListener onStateChangedListener;
    // 日志上报相关
    protected HashMap<String, Integer> logAdMap = new HashMap<>();
    protected String playerFlag;
    protected PlayerEvent playerEvent = new PlayerEvent();
    protected long mPlayerOpenTime = 0;
    private static boolean isFirstOpenPlayer = true;
    private int mSpeed = 0;

    // 视云
    protected ClipEntity.Quality mCurrentQuality;
    protected List<ClipEntity.Quality> mQualities;
    protected int[] mBestvAdTime;
    // 奇艺
    private boolean isQiyiSdkInit;

    @Override
    public void prepare(MediaEntity mediaSource, boolean resetPosition) {
        if (mediaSource == null || mediaSource.getClipEntity() == null) {
            throw new IllegalArgumentException("mediaSource can't be null");
        }
        mMediaEntity = mediaSource;
        playerEvent.pk = mediaSource.getPk();
        playerEvent.subItemPk = mediaSource.getSubItemPk();
        switch (playerMode) {
            case MODE_SMART_PLAYER:
                playerFlag = "bestv";
                MediaMeta mediaMeta = bestvUserInit();
                if (mediaMeta != null) {
                    createPlayer(mediaMeta);
                }
                break;
            case MODE_QIYI_PLAYER:
                playerFlag = "qiyi";
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
                extraParams.setShowAdCountDown(true);
                extraParams.addAdsHint(Parameter.HINT_TYPE_SKIP_AD, "下"); // 跳过悦享看广告
                extraParams.addAdsHint(Parameter.HINT_TYPE_HIDE_PAUSE_AD, "下"); // 消除暂停广告
                extraParams.addAdsHint(Parameter.HINT_TYPE_SHOW_CLICK_THROUGH_AD, "右"); // 前贴,中插广告跳转页面
                PlayerSdk.getInstance().initialize(context.getApplicationContext(), extraParams,
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

    }

    @Override
    public void start() {
        if (isFirstOpenPlayer) {
            isFirstOpenPlayer = false;
            mPlayerOpenTime = DateUtils.currentTimeMillis();
            String sid = MD5.getMd5ByString(snToken + mPlayerOpenTime);
            PlayerEvent.videoStart(playerEvent, snToken, mSpeed, sid, playerFlag);
        }
    }

    @Override
    public void stop() {
        isFirstOpenPlayer = true;
    }

    @Override
    public void switchQuality(int position, ClipEntity.Quality quality) {
        playerEvent.quality = qualityToInt(quality);
    }

    public void logVideoExit(int exitPosition, String source) {
        String sid = MD5.getMd5ByString(snToken + DateUtils.currentTimeMillis());
        PlayerEvent.videoExit(
                playerEvent,
                mSpeed,
                source,
                exitPosition,
                (DateUtils.currentTimeMillis() - mPlayerOpenTime),
                sid,
                playerFlag);
    }

    private MediaMeta bestvUserInit() {
        String mediaUrl = initSmartQuality(mMediaEntity.getInitQuality());
        if (TextUtils.isEmpty(mediaUrl)) {
            if (onStateChangedListener != null) {
                onStateChangedListener.onError("播放地址解析错误");
            }
            String sid = MD5.getMd5ByString(snToken + DateUtils.currentTimeMillis());
            PlayerEvent.videoExcept(
                    "noplayaddress", "noplayaddress",
                    playerEvent, 0, sid, 0, playerFlag);
            return null;
        }
        MediaMeta mediaMeta = new MediaMeta();
        List<AdvEntity> adList = mMediaEntity.getAdvStreamList();
        String[] mediaUrls;
        if (!adList.isEmpty()) {
            mediaUrls = new String[adList.size() + 1];
            mBestvAdTime = new int[adList.size()];
            int i = 0;
            for (AdvEntity element : adList) {
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

    protected void createPlayer(MediaMeta mediaMeta) {
    }

    protected void createPlayer(SdkVideo sdkVideo) {
    }

    protected IsmartvPlayer() {
        isQiyiSdkInit = false;
    }

    public static class Builder {

        private Context context;
        private byte playerMode = -1;
        // 视云片源所需变量
        private String deviceToken;
        // 奇艺片源所需变量
        private String snToken;
        private String modelName;
        private String versionCode;
        private String qiyiUserType;// qiyi login condition
        private String zDeviceToken;// qiyi login param
        private String zUserToken;// qiyi login param

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setPlayerMode(byte playerMode) {
            this.playerMode = playerMode;
            return this;
        }

        public Builder setDeviceToken(String deviceToken) {
            this.deviceToken = deviceToken;
            return this;
        }

        public Builder setSnToken(String snToken) {
            this.snToken = snToken;
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

        public IsmartvPlayer build() {
            if (context == null || playerMode <= 0 || TextUtils.isEmpty(snToken)) {
                throw new IllegalAccessError("Must call setPlayerMode first.");
            }
            IsmartvPlayer tvPlayer = null;
            switch (playerMode) {
                case MODE_SMART_PLAYER:
                    if (TextUtils.isEmpty(deviceToken)) {
                        throw new IllegalArgumentException("Must set deviceToken variable first.");
                    }
                    tvPlayer = new DaisyPlayer();
                    tvPlayer.setDeviceToken(deviceToken);
                    break;
                case MODE_QIYI_PLAYER:
                    if (TextUtils.isEmpty(snToken) || TextUtils.isEmpty(modelName)
                            || TextUtils.isEmpty(versionCode)) {
                        throw new IllegalArgumentException("Must set qiyi variable first.");
                    }
                    tvPlayer = new QiyiPlayer();
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
            tvPlayer.setContext(context);
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
            normal = AESTool.decrypt(normal, deviceToken);
            mMediaEntity.getClipEntity().setNormal(normal);
            mQualities.add(ClipEntity.Quality.QUALITY_NORMAL);
        }
        String medium = clipEntity.getMedium();
        if (!TextUtils.isEmpty(medium)) {
            medium = AESTool.decrypt(medium, deviceToken);
            mMediaEntity.getClipEntity().setNormal(medium);
            mQualities.add(ClipEntity.Quality.QUALITY_MEDIUM);
        }
        String high = clipEntity.getHigh();
        if (!TextUtils.isEmpty(high)) {
            high = AESTool.decrypt(high, deviceToken);
            mMediaEntity.getClipEntity().setNormal(high);
            mQualities.add(ClipEntity.Quality.QUALITY_HIGH);
        }
        String ultra = clipEntity.getUltra();
        if (!TextUtils.isEmpty(ultra)) {
            ultra = AESTool.decrypt(ultra, deviceToken);
            mMediaEntity.getClipEntity().setNormal(ultra);
            mQualities.add(ClipEntity.Quality.QUALITY_ULTRA);
        }
        String blueray = clipEntity.getBlueray();
        if (!TextUtils.isEmpty(blueray)) {
            blueray = AESTool.decrypt(blueray, deviceToken);
            mMediaEntity.getClipEntity().setNormal(blueray);
            mQualities.add(ClipEntity.Quality.QUALITY_BLUERAY);
        }
        String _4k = clipEntity.get_4k();
        if (!TextUtils.isEmpty(_4k)) {
            _4k = AESTool.decrypt(_4k, deviceToken);
            mMediaEntity.getClipEntity().setNormal(_4k);
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

    private Context context;
    private byte playerMode = -1;
    private String snToken;
    // 视云片源所需变量
    private String deviceToken;
    // 奇艺片源所需变量
    private String modelName;
    private String versionCode;
    private String qiyiUserType;// qiyi login condition
    private String zDeviceToken;// qiyi login param
    private String zUserToken;// qiyi login param

    private void setContext(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }

    private void setPlayerMode(byte playerMode) {
        this.playerMode = playerMode;
    }

    public byte getPlayerMode() {
        return playerMode;
    }

    private void setSnToken(String snToken) {
        this.snToken = snToken;
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

    private void setQiyiUserType(String qiyiUserType) {
        this.qiyiUserType = qiyiUserType;
    }

    private void setzDeviceToken(String zDeviceToken) {
        this.zDeviceToken = zDeviceToken;
    }

    private void setzUserToken(String zUserToken) {
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
    public void setPlayerEvent(String title, int clipPk, String channel, String section, String source, ClipEntity.Quality quality) {
        playerEvent.title = title;
        playerEvent.clipPk = clipPk;
        playerEvent.channel = channel;
        playerEvent.section = section;
        playerEvent.source = source;
        playerEvent.quality = qualityToInt(quality);
    }


}
