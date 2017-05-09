package tv.ismar.player.media;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.SurfaceView;

import com.qiyi.sdk.player.IAdController;

import java.io.IOException;
import java.util.Map;

import tv.ismar.app.entity.ClipEntity;
import tv.ismar.library.util.DateUtils;
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

    private boolean isS3Seeking = false;// s3设备,seek后有1002表示bufferEnd
    private String logCurrentMediaUrl;
    private boolean isSwitchQuality = false;

    @Override
    protected void createPlayer(@NonNull MediaMeta mediaMeta) {
        mMediaMeta = mediaMeta;
        mCurrentState = STATE_IDLE;
        mDuration = 0;
        if (mediaMeta.getUrls().length > 1) {
            isPlayingAd = true;
        } else {
            isPlayingAd = false;
        }
        initPlayerType(mMediaMeta);
        mPlayer = new SmartPlayer();
        mPlayer.setSn(getSnToken());
        mPlayer.setScreenOnWhilePlaying(true);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setScreenOnWhilePlaying(true);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mPlayer.setSDCardisAvailable(true);
        } else {
            mPlayer.setSDCardisAvailable(false);
        }
        mPlayer.setOnPreparedListenerUrl(onPreparedListenerUrl);
        mPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        mPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
        mPlayer.setOnErrorListener(onErrorListener);
        mPlayer.setOnInfoListener(onInfoListener);
        mPlayer.setOnTsInfoListener(onTsInfoListener);
        mPlayer.setOnM3u8IpListener(onM3u8IpListener);
        mPlayer.setOnCompletionListenerUrl(onCompletionListenerUrl);
        mPlayer.setDataSource(mMediaMeta.getUrls());
        mPlayer.prepareAsync();
        mCurrentState = STATE_PREPARING;

    }

    @Override
    public void attachSurfaceView(SurfaceView surfaceView) {
        if (mPlayer == null || surfaceView == null) {
            LogUtils.e(TAG, "AttachSurface->Player : " + mPlayer + " SurfaceView : " + surfaceView);
            return;
        }
        super.attachSurfaceView(surfaceView);
        mSurfaceHelper = new SurfaceHelper(surfaceView, this);
        mSurfaceHelper.attachSurfaceView();
    }

    @Override
    public void detachViews() {
        if (mSurfaceHelper == null) {
            LogUtils.e(TAG, "SurfaceHelper is null");
            return;
        }
        mSurfaceAttached = false;
        mSurfaceHelper.release();
        mSurfaceView = null;
        logFirstOpenPlayer = true;
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

    @Override
    public void stop() {
        if (mPlayer != null) {
            mPlayer.stop();
        }
        mCurrentState = STATE_IDLE;

    }

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
            isSwitchQuality = true;
            stop();
            release();
            createPlayer(mMediaMeta);
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

    private SmartPlayer.OnPreparedListenerUrl onPreparedListenerUrl = new SmartPlayer.OnPreparedListenerUrl() {
        @Override
        public void onPrepared(SmartPlayer smartPlayer, String s) {
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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mPlayer != null && onStateChangedListener != null) {
                        if (isSwitchQuality) {
                            isSwitchQuality = false;
                            mPlayer.setDisplay(mSurfaceHelper.getSurfaceHolder());
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
            if (mPlayer == null) {
                return;
            }
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
                    if (onBufferChangedListener != null) {
                        onBufferChangedListener.onBufferEnd();
                    }
                    if (mSurfaceAttached && logFirstOpenPlayer) {
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
                                logSpeed, getCurrentPosition(),
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
            if (mPlayer == null) {
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
            mCurrentState = STATE_ERROR;
            PlayerEvent.videoExcept(
                    "mediaexception", String.valueOf(i),
                    logPlayerEvent, logSpeed,
                    getCurrentPosition(), logPlayerFlag);
            if (isPlayingAd) {
                String[] paths = new String[]{mMediaMeta.getUrls()[mMediaMeta.getUrls().length - 1]};
                mMediaMeta.setUrls(paths);
                createPlayer(mMediaMeta);
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
    public void onSurfaceCreated() {
        LogUtils.d(TAG, "Bestv onSurfaceCreated.");
        if (mPlayer != null) {
            mPlayer.setDisplay(mSurfaceHelper.getSurfaceHolder());
            start();
        }
    }

    @Override
    public void onSurfaceDestroyed() {
        LogUtils.d(TAG, "Bestv onSurfaceDestroyed.");
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

    private void initPlayerType(MediaMeta mediaMeta) {
        SmartPlayer.PlayerType player264Type = SmartPlayer.PlayerType.PlayerMedia;
        SmartPlayer.PlayerType player265Type = SmartPlayer.PlayerType.PlayerMedia;
        switch (mediaMeta.getPlayer264Type()) {
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
        switch (mediaMeta.getPlayer265Type()) {
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
        SmartPlayer.initPlayer(player264Type, player265Type, mediaMeta.getUrls());
    }

}
