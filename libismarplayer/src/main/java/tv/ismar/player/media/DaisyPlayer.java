package tv.ismar.player.media;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.qiyi.sdk.player.IAdController;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.core.VodUserAgent;
import tv.ismar.app.entity.ClipEntity;
import tv.ismar.library.util.DateUtils;
import tv.ismar.library.util.DeviceUtils;
import tv.ismar.library.util.LogUtils;
import tv.ismar.library.util.StringUtils;
import tv.ismar.player.IsmartvPlayer;
import tv.ismar.player.SmartPlayer;
import tv.ismar.player.event.PlayerEvent;
import tv.ismar.player.model.MediaMeta;

/**
 * Created by LongHai on 17-4-26.
 * <p>
 * 需要实现预加载功能
 */

public class DaisyPlayer extends IsmartvPlayer implements SurfaceHelper.SurfaceCallback {

    private String TAG = "LH/DaisyPlayer";
    private MediaMeta mMediaMeta;
    private int mDuration;

    private SmartPlayer mPlayer;
    private SurfaceHelper mSurfaceHelper;

    private String logCurrentMediaUrl;
    private boolean isSwitchingQuality = false;// 切换码率后，不回调onPrepared,直接开始播放

    private SmartPlayer getSmartPlayerInstance(){
        if (mPlayer == null) {
            SmartPlayer.PlayerType player264Type = SmartPlayer.PlayerType.PlayerMedia;
            SmartPlayer.PlayerType player265Type = SmartPlayer.PlayerType.PlayerMedia;
            SmartPlayer.PlayerType playerliveType = SmartPlayer.PlayerType.PlayerMedia;
            int h264PlayerType =IsmartvActivator.getInstance().getH264PlayerType();
            int h265PlayerType =IsmartvActivator.getInstance().getH265PlayerType();
            int livePlayerType =IsmartvActivator.getInstance().getLivePlayerType();
            switch (h264PlayerType) {
                case 0:
                    player264Type = SmartPlayer.PlayerType.PlayerMedia;
                    break;
                case 1:
                    player264Type = SmartPlayer.PlayerType.PlayerSystem;
                    break;
                case 2:
                    player264Type = SmartPlayer.PlayerType.PlayerCodec;
                    break;
            }
            switch (h265PlayerType) {
                case 0:
                    player265Type = SmartPlayer.PlayerType.PlayerMedia;
                    break;
                case 1:
                    player265Type = SmartPlayer.PlayerType.PlayerSystem;
                    break;
                case 2:
                    player265Type = SmartPlayer.PlayerType.PlayerCodec;
                    break;
            }
            switch (livePlayerType) {
                case 0:
                    playerliveType = SmartPlayer.PlayerType.PlayerMedia;
                    break;
                case 1:
                    playerliveType = SmartPlayer.PlayerType.PlayerSystem;
                    break;
                case 2:
                    playerliveType = SmartPlayer.PlayerType.PlayerCodec;
                    break;
            }
            mPlayer = new SmartPlayer(player264Type, player265Type,playerliveType);
        }
        return mPlayer;
    }

    @Override
    protected void createPreloadPlayer(MediaMeta mediaMeta) {
        if (mediaMeta == null) {
            throw new IllegalArgumentException("DaisyPlayer create preload player null parameter");
        }
        super.createPreloadPlayer(mediaMeta);
        mPlayer = getSmartPlayerInstance();
        initPlayerType(mediaMeta,true);

    }

    @Override
    protected void createPlayer(@NonNull MediaMeta mediaMeta, boolean hasPreload) {
        if (hasPreload && mPlayer == null) {
            hasPreload = false;
            LogUtils.e(TAG, "Setup preload video but the player is null");
        }
        mMediaMeta = mediaMeta;
        mCurrentState = STATE_IDLE;
        mDuration = 0;
        if (mediaMeta.getUrls().length > 1) {
            isPlayingAd = true;
        } else {
            isPlayingAd = false;
        }
        mSurfaceHelper = new SurfaceHelper(mSurfaceView, this);
        mSurfaceHelper.attachSurfaceView(hasPreload);

    }

    @Override
    public void detachViews() {
        super.detachViews();
        if (mSurfaceHelper == null) {
            LogUtils.e(TAG, "SurfaceHelper is null");
            return;
        }
        mSurfaceHelper.release();
        mSurfaceView = null;
    }

    @Override
    public void start() {
        super.start();
        if (isInPlaybackState()) {
            if (!isPlaying() && mSurfaceHelper != null && mSurfaceHelper.getSurfaceHolder() != null) {
                mPlayer.start();
                mCurrentState = STATE_PLAYING;
            }
            if (!isPlayingAd && onStateChangedListener != null) {
                onStateChangedListener.onStarted();
            } else if (isPlayingAd && onAdvertisementListener != null) {
                onAdvertisementListener.onAdStart();
            }
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (isInPlaybackState()) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                mCurrentState = STATE_PAUSED;
                if (onStateChangedListener != null) {
                    onStateChangedListener.onPaused();
                }

            }
        }
    }

    @Override
    public void seekTo(final int position) {
        super.seekTo(position);
        if (isInPlaybackState()) {
            new Thread() {
                @Override
                public void run() {
                    mPlayer.seekTo(position);
                }
            }.start();
            isS3Seeking = true;
        }
    }

    // 退出播放器不释放资源
    @Override
    public void stop() {
        // 注意切换码率不能使用此函数停止播放器
        if (mPlayer != null) {
            mPlayer.setOnPreparedListenerUrl(null);
            mPlayer.setOnVideoSizeChangedListener(null);
            mPlayer.setOnSeekCompleteListener(null);
            mPlayer.setOnErrorListener(null);
            mPlayer.setOnInfoListener(null);
            mPlayer.setOnTsInfoListener(null);
            mPlayer.setOnM3u8IpListener(null);
            mPlayer.setOnCompletionListenerUrl(null);
            mPlayer.stop();
//            mPlayer.close();
//            mPlayer.reset();
        }
        mCurrentState = STATE_IDLE;

    }

    // 退出APP释放播放器资源
    @Override
    public void release() {
        if (mPlayer != null) {
            mPlayer.setOnPreparedListenerUrl(null);
            mPlayer.setOnVideoSizeChangedListener(null);
            mPlayer.setOnSeekCompleteListener(null);
            mPlayer.setOnErrorListener(null);
            mPlayer.setOnInfoListener(null);
            mPlayer.setOnTsInfoListener(null);
            mPlayer.setOnM3u8IpListener(null);
            mPlayer.setOnCompletionListenerUrl(null);
            mPlayer.setOnPreloadCompleteListener(null);
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        mCurrentState = STATE_IDLE;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mPlayer.getDuration();
            return mDuration;
        }
        mDuration = 0;
        return mDuration;
    }

    @Override
    public int getAdCountDownTime() {
        if (mBestvAdTime == null || mPlayer == null || !isPlayingAd) {
            return 0;
        }
        int totalAdTime = 0;
        int currentAd = mPlayer.getCurrentPlayUrl();
        if (currentAd == mMediaMeta.getUrls().length - 1) {
            return 0;
        } else if (currentAd == mBestvAdTime.length - 1) {
            totalAdTime = mBestvAdTime[mBestvAdTime.length - 1];
        } else {
            for (int i = currentAd; i < mBestvAdTime.length; i++) {
                totalAdTime += mBestvAdTime[i];
            }
        }
        return (totalAdTime * 1000 - getCurrentPosition()) < 0 ? 0 : (totalAdTime * 1000 - getCurrentPosition());
    }

    @Override
    public boolean isPlaying() {
        LogUtils.i(TAG, "Player:" + mPlayer + " " + mCurrentState);
        return isInPlaybackState() && mPlayer.isPlaying();
    }

    @Override
    public void switchQuality(int position, ClipEntity.Quality quality) {
        if (!isInPlaybackState()) {
            return;
        }
        super.switchQuality(position, quality);
        String mediaUrl = qualityToUrl(quality);
        if (!StringUtils.isEmpty(mediaUrl)) {
            mCurrentQuality = quality;
            String[] paths = new String[]{mediaUrl};
            mMediaMeta.setUrls(paths);
            if (!mMediaEntity.isLivingVideo()) {
                mMediaMeta.setStartPosition(position);
            }
            isSwitchingQuality = true;
            if (mSurfaceHelper != null) {
                mSurfaceHelper.release();
            }
//            stop();
            release();
            mSurfaceHelper = new SurfaceHelper(mSurfaceView, this);
            mSurfaceHelper.attachSurfaceView(false);
        }
    }

    protected boolean isInPlaybackState() {
        return (mPlayer != null
                && mCurrentState != STATE_ERROR
                && mCurrentState != STATE_IDLE
                && mCurrentState != STATE_PREPARING
                && mCurrentState != STATE_COMPLETED);
    }

    @Override
    public IAdController getAdController() {
        return null;
    }

    private SmartPlayer.OnPreloadCompleteListener smartPreloadCompleteListener = new SmartPlayer.OnPreloadCompleteListener() {
        @Override
        public void OnPreloadComplete(SmartPlayer smartPlayer) {
            LogUtils.d(TAG,"OnPreloadComplete");
            if (mPlayer == null || mPlayer != smartPlayer) {
                return;
            }
            if (preloadMediaMeta != null) {
                isPreloadCompleted = true;
                logPreloadEnd();
            }
        }
    };

    private SmartPlayer.OnPreparedListenerUrl onPreparedListenerUrl = new SmartPlayer.OnPreparedListenerUrl() {
        @Override
        public void onPrepared(SmartPlayer smartPlayer, String s) {
            LogUtils.d(TAG,"onPrepared");
            if (mPlayer == null) {
                return;
            }
            mCurrentState = STATE_PREPARED;
            logCurrentMediaUrl = s;
            logMediaIp = getMediaIp(s);
            if (isPlayingAd) {
                logAdMediaId = logAdMap.get(s);
            }
            long delay = 0;
            if (mMediaMeta.getStartPosition() > 0 && !isPlayingAd) {
                logSeekStartPosition = true;
                seekTo(mMediaMeta.getStartPosition());
                delay = 500;
            }
            // 在start之前调用seekTo不一定会有onSeekCompleted回调
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mPlayer != null && onStateChangedListener != null) {
                        if (isSwitchingQuality) {
                            isSwitchingQuality = false;
                            start();
                        } else {
                            onStateChangedListener.onPrepared();
                        }
                    }
                }
            }, delay);
        }
    };

    private SmartPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new SmartPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(SmartPlayer smartPlayer, int width, int height) {
            LogUtils.i(TAG, "onVideoSizeChanged:" + width + " " + height);
            if (mPlayer == null || mSurfaceHelper == null || !mSurfaceHelper.isReady()) {
                return;
            }
            int[] outputSize = computeVideoSize(width, height);
            LogUtils.i(TAG, "outSize:" + Arrays.toString(outputSize));
            mSurfaceHelper.getSurfaceHolder().setFixedSize(outputSize[0], outputSize[1]);
            smartPlayer.setDisplay(mSurfaceHelper.getSurfaceHolder());

            if (onStateChangedListener != null) {
                onStateChangedListener.onVideoSizeChanged(width, height);
            }
        }
    };

    private SmartPlayer.OnInfoListener onInfoListener = new SmartPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(SmartPlayer smartPlayer, int i, int i1) {
            if (mPlayer == null) {
                return false;
            }
            LogUtils.d(TAG, "onInfo:" + i + " " + onBufferChangedListener);
            switch (i) {
                case SmartPlayer.MEDIA_INFO_BUFFERING_START:
                case 809:
                    if (onBufferChangedListener != null) {
                        onBufferChangedListener.onBufferStart();
                    }
                    logBufferStartTime = DateUtils.currentTimeMillis();
                    break;
                case 1002:
                    if (isS3Seeking) {
                        isS3Seeking = false;
                        if (onBufferChangedListener != null) {
                            onBufferChangedListener.onBufferEnd();
                        }
                    }
                    break;
                case SmartPlayer.MEDIA_INFO_BUFFERING_END:
                case 3:
                    if (!mSurfaceAttached) {
                        return false;
                    }
                    if (onBufferChangedListener != null) {
                        onBufferChangedListener.onBufferEnd();
                    }
                    if (logFirstOpenPlayer) {
                        // 第一次进入播放器缓冲结束
                        logFirstOpenPlayer = false;
                        if (isPlayingAd) {
                            PlayerEvent.ad_play_load(
                                    logPlayerEvent,
                                    (DateUtils.currentTimeMillis() - logPlayerOpenTime),
                                    logMediaIp,
                                    logAdMediaId,
                                    logPlayerFlag);
                        } else {
                            PlayerEvent.videoPlayLoad(
                                    logPlayerEvent,
                                    (DateUtils.currentTimeMillis() - logPlayerOpenTime),
                                    logSpeed, logMediaIp, logCurrentMediaUrl, logPlayerFlag);
                        }
                    } else if (isPlayingAd && !logAdMap.isEmpty()) {
                        PlayerEvent.ad_play_blockend(
                                logPlayerEvent,
                                (DateUtils.currentTimeMillis() - logBufferStartTime),
                                logMediaIp, logAdMediaId, logPlayerFlag);
                    } else {
                        PlayerEvent.videoPlayBlockend(
                                logPlayerEvent,
                                logSpeed, (DateUtils.currentTimeMillis() - logBufferStartTime),
                                logMediaIp, logPlayerFlag);
                    }
                    break;
            }
            return false;
        }
    };

    private SmartPlayer.OnSeekCompleteListener onSeekCompleteListener = new SmartPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(SmartPlayer smartPlayer) {
            if (mPlayer == null) {
                return;
            }
            if (logSeekStartPosition) {
                logSeekStartPosition = false;
                return;
            }
            if (isInPlaybackState()) {
                PlayerEvent.videoPlaySeekBlockend(
                        logPlayerEvent,
                        logSpeed,
                        getCurrentPosition(),
                        DateUtils.currentTimeMillis() - logBufferStartTime,
                        logMediaIp, logPlayerFlag);
            }
            if (onStateChangedListener != null) {
                onStateChangedListener.onSeekCompleted();
            }

        }
    };

    private SmartPlayer.OnCompletionListenerUrl onCompletionListenerUrl = new SmartPlayer.OnCompletionListenerUrl() {
        @Override
        public void onCompletion(SmartPlayer smartPlayer, String s) {
            if (mPlayer == null || mCurrentState == STATE_COMPLETED || mCurrentState == STATE_ERROR || mSurfaceView == null) {
                return;
            }
            int currentIndex = smartPlayer.getCurrentPlayUrl();
            LogUtils.i(TAG, "onCompletion state url index==" + currentIndex);
            if (isPlayingAd && !logAdMap.isEmpty()) {
                logMediaIp = getMediaIp(s);
                logAdMediaId = logAdMap.get(s);
                logAdMap.remove(s);
                PlayerEvent.ad_play_exit(
                        logPlayerEvent,
                        (DateUtils.currentTimeMillis() - logPlayerOpenTime),
                        logMediaIp,
                        logAdMediaId,
                        logPlayerFlag);
                if (logAdMap.isEmpty()) {
                    if (onAdvertisementListener != null) {
                        onAdvertisementListener.onAdEnd();
                    }
                    isPlayingAd = false;
                }
                if (currentIndex >= 0 && currentIndex < mMediaMeta.getUrls().length - 1) { // 如果当前播放的为第一个影片的话，则准备播放第二个影片。
                    currentIndex++;
                    try {
                        smartPlayer.playUrl(currentIndex); // 准备播放第二个影片，传入参数为1，第二个影片在数组中的下标。
                    } catch (IOException e) {
                        e.printStackTrace();
                        LogUtils.e(TAG, "smartPlayer play next video IOException.");
                        if (onStateChangedListener != null) {
                            onStateChangedListener.onError("播放器错误");
                        }
                    }
                }
            } else {
                mCurrentState = STATE_COMPLETED;
                if (onStateChangedListener != null) {
                    onStateChangedListener.onCompleted();
                }
            }
        }
    };

    private SmartPlayer.OnErrorListener onErrorListener = new SmartPlayer.OnErrorListener() {
        @Override
        public boolean onError(SmartPlayer smartPlayer, int i, int i1) {
            if (i == 1010){
                return true;
            }
            mCurrentState = STATE_ERROR;
            PlayerEvent.videoExcept(
                    "mediaexception", String.valueOf(i),
                    logPlayerEvent, logSpeed,
                    getCurrentPosition(), logPlayerFlag);
            if (isPlayingAd) {
                String[] paths = new String[]{mMediaMeta.getUrls()[mMediaMeta.getUrls().length - 1]};
                mMediaMeta.setUrls(paths);
                createPlayer(mMediaMeta, false);
            } else {
                String errorMsg = "播放器错误";
                switch (i) {
                    case SmartPlayer.PROXY_DOWNLOAD_M3U8_ERROR:
                        errorMsg = "视频文件下载失败";
                        break;
                    case SmartPlayer.PROXY_PARSER_M3U8_ERROR:
                        errorMsg = "视频文件解析失败";
                        break;
                    case MediaPlayer.MEDIA_ERROR_IO:
                        errorMsg = "网络错误";
                        break;
                }
                if (onStateChangedListener != null) {
                    onStateChangedListener.onError(errorMsg);
                }
            }
            return true;
        }
    };

    private SmartPlayer.OnTsInfoListener onTsInfoListener = new SmartPlayer.OnTsInfoListener() {
        @Override
        public void onTsInfo(SmartPlayer smartPlayer, Map<String, String> map) {
            if (mPlayer == null) {
                return;
            }
            String spd = map.get("TsDownLoadSpeed");
            try {
                logSpeed = Integer.parseInt(spd) / (1024 * 8);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            String CacheTime = map.get("TsCacheTime");
            LogUtils.d(TAG,"CacheTime = "+CacheTime);
            if (CacheTime != null)
            {
                int nCacheTime = Integer.parseInt(CacheTime);
                LogUtils.i(TAG, "current cache total time:" + nCacheTime);
            }
            if (onStateChangedListener != null) {
                onStateChangedListener.onTsInfo(map);
            }
            logMediaIp = map.get(SmartPlayer.DownLoadTsInfo.TsIpAddr);
        }
    };

    private SmartPlayer.OnM3u8IpListener onM3u8IpListener = new SmartPlayer.OnM3u8IpListener() {
        @Override
        public void onM3u8TsInfo(SmartPlayer smartPlayer, String s) {
            if (mPlayer == null) {
                return;
            }
            logMediaIp = s;
        }
    };

    @Override
    public void onSurfaceCreated(boolean preload) {
        LogUtils.d(TAG, "Bestv onSurfaceCreated.");
        openVideo(preload);
    }

    @Override
    public void onSurfaceDestroyed() {
        mSurfaceHelper.release();
        LogUtils.d(TAG, "Bestv onSurfaceDestroyed.");
    }

    private void openVideo(boolean hasPreload) {
        mPlayer =  getSmartPlayerInstance();
        LogUtils.d(TAG,"openVideo");
        if(hasPreload){
            if (logFirstOpenPlayer) {
                logPlayerOpenTime = DateUtils.currentTimeMillis();
                PlayerEvent.videoStart(logPlayerEvent, logSpeed, logPlayerFlag);
            }
            mPlayer.createPlayer();
            mPlayer.setScreenOnWhilePlaying(true);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setOnPreparedListenerUrl(onPreparedListenerUrl);
            mPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
            mPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
            mPlayer.setOnErrorListener(onErrorListener);
            mPlayer.setOnInfoListener(onInfoListener);
            mPlayer.setOnTsInfoListener(onTsInfoListener);
            mPlayer.setOnM3u8IpListener(onM3u8IpListener);
            mPlayer.setOnCompletionListenerUrl(onCompletionListenerUrl);
            mPlayer.setDisplay(mSurfaceHelper.getSurfaceHolder());
            mPlayer.reprepareAsync();
        }else{
            initPlayerType(mMediaMeta,false);
        }
        mCurrentState = STATE_PREPARING;
    }

    /**
     * 获取媒体IP
     */
    private String getMediaIp(String str) {
        String ip = "";
        String tmp = str.substring(7, str.length());
        int index = tmp.indexOf("/");
        ip = tmp.substring(0, index);
        return ip;
    }

    private void initPlayerType(final MediaMeta mediaMeta,final boolean ispreload) {
        mPlayer.setOnInitCompleteListener(new SmartPlayer.OnInitCompleteListener() {
            @Override
            public void onInitComplete(SmartPlayer smartPlayer, boolean bSuccess) {
                LogUtils.d(TAG,"onInitComplete + ispreload="+ispreload);
                if (mPlayer != smartPlayer) {
                    LogUtils.e(TAG, "onInitComplete player object not equal");
                    return;
                }
                if (ispreload && mPlayer.getPlayerType() == SmartPlayer.PlayerType.PlayerSystem) {
                    LogUtils.e(TAG, "SmartPlayer.PlayerType.PlayerSystem can not used to preload");
                    return;
                }

                switch (mPlayer.getPlayerType()) {
                    case PlayerMedia:
                        logPlayerFlag = "bestv";
                        break;
                    case PlayerCodec:
                        logPlayerFlag = "smart";
                        break;
                    case PlayerSystem:
                        logPlayerFlag = "system";
                        break;
                }

                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    mPlayer.setSDCardisAvailable(true);
                } else {
                    mPlayer.setSDCardisAvailable(false);
                }
                mPlayer.setSn(getSnToken());
                String userAgent = VodUserAgent.getHttpUserAgent();
                LogUtils.d(TAG, "setUserAgent : " + userAgent);
                mPlayer.setUserAgent(userAgent);
                if (ispreload) {
                    mPlayer.setOnPreloadCompleteListener(smartPreloadCompleteListener);
                    mPlayer.setDataSource(mediaMeta.getUrls());
                    mPlayer.prepareAsync();
                } else {
                    if (logFirstOpenPlayer) {
                        logPlayerOpenTime = DateUtils.currentTimeMillis();
                        PlayerEvent.videoStart(logPlayerEvent, logSpeed, logPlayerFlag);
                    }
                    mPlayer.createPlayer();
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.setScreenOnWhilePlaying(true);
                    mPlayer.setOnPreparedListenerUrl(onPreparedListenerUrl);
                    mPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
                    mPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
                    mPlayer.setOnErrorListener(onErrorListener);
                    mPlayer.setOnInfoListener(onInfoListener);
                    mPlayer.setOnTsInfoListener(onTsInfoListener);
                    mPlayer.setOnM3u8IpListener(onM3u8IpListener);
                    mPlayer.setOnCompletionListenerUrl(onCompletionListenerUrl);
                    mPlayer.setDataSource(mediaMeta.getUrls());
                    mPlayer.setDisplay(mSurfaceHelper.getSurfaceHolder());
                    mPlayer.prepareAsync();
                }
            }
        });
        mPlayer.initPlayer(
                mediaMeta.getUrls(),
                mMediaEntity.isLivingVideo(),
                "265".equals(mMediaEntity.getClipEntity().getCode_version()),
                mMediaEntity.getStartPosition()
        );
    }

    protected int[] computeVideoSize(int videoWidth, int videoHeight) {
        if (mSurfaceView == null || mSurfaceView.getContext() == null) {
            return null;
        }
        int[] size = new int[2];
        int screenWidth = DeviceUtils.getDisplayPixelWidth(mSurfaceView.getContext().getApplicationContext());
        int screenHeight = DeviceUtils.getDisplayPixelHeight(mSurfaceView.getContext().getApplicationContext());
        double dw = screenWidth;
        double dh = screenHeight;
        if (videoWidth == videoHeight) {
            if (dw > dh) {
                dw = screenHeight;
            } else {
                dh = screenWidth;
            }
        } else {
            double dar = dw / dh;
            double ar = videoWidth / videoHeight;
            if (dar < ar) {
                double widthScale = videoWidth / dw;
                dh = videoHeight / widthScale;
            } else {
                double heightScale = videoHeight / dh;
                dw = videoWidth / heightScale;
            }
        }
        size[0] = (int) Math.ceil(dw);
        size[1] = (int) Math.ceil(dh);
        return size;
    }

}
