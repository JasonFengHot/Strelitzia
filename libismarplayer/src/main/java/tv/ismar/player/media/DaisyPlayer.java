package tv.ismar.player.media;

import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.SurfaceView;

import java.util.List;
import java.util.Map;

import tv.ismar.library.util.LogUtils;
import tv.ismar.player.IsmartvPlayer;
import tv.ismar.player.SmartPlayer;
import tv.ismar.player.model.ClipEntity;
import tv.ismar.player.model.MediaMeta;

/**
 * Created by LongHai on 17-4-26.
 */

public class DaisyPlayer extends IsmartvPlayer implements SurfaceHelper.SurfaceCallback {

    private String TAG = "LH/DaisyPlayer";
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_COMPLETED = 5;

    private int mCurrentState = STATE_IDLE;
    private MediaMeta mMediaMeta;
    private int mDuration;

    private SmartPlayer mPlayer;
    private SurfaceHelper mSurfaceHelper;
    private boolean isPreparedToStart;
    private boolean isSurfaceDetached;

    @Override
    protected void createPlayer(@NonNull MediaMeta mediaMeta) {
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
        mMediaMeta = mediaMeta;
        mCurrentState = STATE_IDLE;
        isSurfaceDetached = false;
        mDuration = 0;
        if (mediaMeta.getUrls().length > 1) {
            isPlayingAd = true;
        } else {
            isPlayingAd = false;
        }
        if (mPlayer == null) {
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
        mSurfaceHelper = new SurfaceHelper(surfaceView, this);
        mSurfaceHelper.attachSurfaceView();
    }

    @Override
    public void detachViews() {
        if (mSurfaceHelper == null) {
            LogUtils.e(TAG, "SurfaceHelper is null");
            return;
        }
        isSurfaceDetached = true;
        mSurfaceHelper.release();
    }

    @Override
    public void start() {
        if (isInPlaybackState() && !isPlaying()) {
            long delay = 0;
            // 播放正片，起播位置大于0，需要延迟调用start()
            if (mMediaMeta.getStartPosition() > 0 && !isPlayingAd && isPreparedToStart) {
                delay = 500;
                isPreparedToStart = false;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mPlayer != null && mSurfaceHelper != null && mSurfaceHelper.getSurfaceHolder() != null) {
                        mPlayer.start();
                    }
                }
            }, delay);
            mCurrentState = STATE_PLAYING;
            if (!isPlayingAd && onStateChangedListener != null) {
                onStateChangedListener.onStarted();
            }
        }
    }

    @Override
    public void pause() {
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
        if (isInPlaybackState()) {
            new Thread() {
                @Override
                public void run() {
                    mPlayer.seekTo(position);
                }
            }.start();
        }
    }

    @Override
    public void stop() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.setOnPreparedListenerUrl(null);
            mPlayer.setOnVideoSizeChangedListener(null);
            mPlayer.setOnSeekCompleteListener(null);
            mPlayer.setOnErrorListener(null);
            mPlayer.setOnInfoListener(null);
            mPlayer.setOnTsInfoListener(null);
            mPlayer.setOnM3u8IpListener(null);
            mPlayer.setOnCompletionListenerUrl(null);
        }
        mCurrentState = STATE_IDLE;

    }

    @Override
    public void release() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
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
    public ClipEntity.Quality getCurrentQuality() {
        return mCurrentQuality;
    }

    @Override
    public List<ClipEntity.Quality> getQualities() {
        return mQualities;
    }

    @Override
    public void switchQuality(ClipEntity.Quality quality) {

    }

    public boolean isInPlaybackState() {
        return (mPlayer != null
                && mCurrentState != STATE_ERROR
                && mCurrentState != STATE_IDLE
                && mCurrentState != STATE_PREPARING
                && mCurrentState != STATE_COMPLETED
                && !isSurfaceDetached);
    }

    private SmartPlayer.OnPreparedListenerUrl onPreparedListenerUrl = new SmartPlayer.OnPreparedListenerUrl() {
        @Override
        public void onPrepared(SmartPlayer smartPlayer, String s) {
            mCurrentState = STATE_PREPARED;
            if (mMediaMeta.getStartPosition() > 0 && !isPlayingAd) {
                seekTo(mMediaMeta.getStartPosition());
            }
            isPreparedToStart = true;
        }
    };

    private SmartPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new SmartPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(SmartPlayer smartPlayer, int i, int i1) {

        }
    };

    private SmartPlayer.OnInfoListener onInfoListener = new SmartPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(SmartPlayer smartPlayer, int i, int i1) {
            return false;
        }
    };

    private SmartPlayer.OnSeekCompleteListener onSeekCompleteListener = new SmartPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(SmartPlayer smartPlayer) {

        }
    };

    private SmartPlayer.OnCompletionListenerUrl onCompletionListenerUrl = new SmartPlayer.OnCompletionListenerUrl() {
        @Override
        public void onCompletion(SmartPlayer smartPlayer, String s) {

        }
    };

    private SmartPlayer.OnErrorListener onErrorListener = new SmartPlayer.OnErrorListener() {
        @Override
        public boolean onError(SmartPlayer smartPlayer, int i, int i1) {
            if (mPlayer != null) {
                mPlayer.reset();
            }
            return false;
        }
    };

    private SmartPlayer.OnTsInfoListener onTsInfoListener = new SmartPlayer.OnTsInfoListener() {
        @Override
        public void onTsInfo(SmartPlayer smartPlayer, Map<String, String> map) {

        }
    };

    private SmartPlayer.OnM3u8IpListener onM3u8IpListener = new SmartPlayer.OnM3u8IpListener() {
        @Override
        public void onM3u8TsInfo(SmartPlayer smartPlayer, String s) {

        }
    };

    @Override
    public void onSurfaceCreated() {
        if (mPlayer != null) {
            mPlayer.setDisplay(mSurfaceHelper.getSurfaceHolder());
        }
    }

    @Override
    public void onSurfaceDestroyed() {

    }
}
