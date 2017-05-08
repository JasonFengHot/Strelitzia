package tv.ismar.player.media;

import android.util.Log;
import android.view.SurfaceView;

import com.qiyi.sdk.player.BitStream;
import com.qiyi.sdk.player.IAdController;
import com.qiyi.sdk.player.IMediaPlayer;
import com.qiyi.sdk.player.ISdkError;
import com.qiyi.sdk.player.IVideoOverlay;
import com.qiyi.sdk.player.PlayerSdk;
import com.qiyi.sdk.player.SdkVideo;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.app.entity.ClipEntity;
import tv.ismar.library.util.DateUtils;
import tv.ismar.library.util.LogUtils;
import tv.ismar.player.IsmartvPlayer;
import tv.ismar.player.event.PlayerEvent;

/**
 * Created by LongHai on 17-4-26.
 * <p>
 * 奇艺播放器不实现预加载功能
 */

public class QiyiPlayer extends IsmartvPlayer {

    private String TAG = "LH/QiyiPlayer";
    private IMediaPlayer mPlayer;
    private IVideoOverlay mVideoOverlay;
    private QiyiVideoSurfaceView mSurfaceView;
    private List<BitStream> mBitStreamList;
    private int mDuration;

    @Override
    protected void createPlayer(SdkVideo sdkVideo) {
        mCurrentState = STATE_IDLE;
        mDuration = 0;
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        mSurfaceView = new QiyiVideoSurfaceView(mQiyiContainer.getContext().getApplicationContext());
        mVideoOverlay = PlayerSdk.getInstance().createVideoOverlay(mQiyiContainer, mSurfaceView);
        mPlayer = PlayerSdk.getInstance().createMediaPlayer();
        //setVideo方法, 更名为setData, 必须调用, 需传入IMedia对象, 起播时间点修改为从IMedia对象获取, 不从setData传参
        mPlayer.setData(sdkVideo);
        mPlayer.setDisplay(mVideoOverlay);
        //设置播放状态回调监听器, 需要时设置
        mPlayer.setOnStateChangedListener(qiyiStateChangedListener);
        //设置码流信息回调监听器, 需要时设置
        mPlayer.setOnBitStreamInfoListener(qiyiBitStreamInfoListener);
        //设置VIP试看信息回调监听器, 需要时设置
        mPlayer.setOnPreviewInfoListener(qiyiPreviewInfoListener);
        //设置视频分辨率回调监听器, 需要时设置
        mPlayer.setOnVideoSizeChangedListener(qiyiVideoSizeChangedListener);
        //设置seek完成监听器, 需要时设置
        mPlayer.setOnSeekCompleteListener(qiyiSeekCompleteListener);
        //设置缓冲事件监听器, 需要时设置
        mPlayer.setOnBufferChangedListener(qiyiBufferChangedListener);
        mPlayer.setOnInfoListener(onInfoListener);
        //调用prepareAsync, 播放器开始准备, 必须调用
        mPlayer.prepareAsync();
        mCurrentState = STATE_PREPARING;

    }

    @Override
    public void attachSurfaceView(SurfaceView surfaceView) {
        LogUtils.i(TAG, "AttachSurface->Player : " + mPlayer);
        if (mPlayer == null) {
            return;
        }
        super.attachSurfaceView(surfaceView);
        start();
    }

    @Override
    public void detachViews() {
        LogUtils.i(TAG, "detachViews");
        mSurfaceAttached = false;
        mQiyiContainer = null;
        logFirstOpenPlayer = true;
    }

    @Override
    public void start() {
        super.start();
        if (isInPlaybackState()) {
            if (!isPlaying()) {
                mPlayer.start();
            }
        }

    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
            }
        }
    }

    @Override
    public void seekTo(int position) {
        super.seekTo(position);
        if (isInPlaybackState()) {
            mPlayer.seekTo(position);
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
            mPlayer.setOnStateChangedListener(null);
            mPlayer.setOnBitStreamInfoListener(null);
            mPlayer.setOnPreviewInfoListener(null);
            mPlayer.setOnVideoSizeChangedListener(null);
            mPlayer.setOnSeekCompleteListener(null);
            mPlayer.setOnBufferChangedListener(null);
            mPlayer.setOnInfoListener(null);
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
        if (isInPlaybackState()) {
            return mPlayer.getAdCountDownTime();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mPlayer.isPlaying();
    }

    @Override
    public void switchQuality(int position, ClipEntity.Quality quality) {
        super.switchQuality(position, quality);
        BitStream bitStream = qualityConvertToBitStream(quality);
        if (bitStream != null) {
            mPlayer.switchBitStream(bitStream);
            mCurrentQuality = quality;
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
        if (mPlayer == null) {
            return null;
        }
        return mPlayer.getAdController();
    }

    private IMediaPlayer.OnStateChangedListener qiyiStateChangedListener = new IMediaPlayer.OnStateChangedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            mCurrentState = STATE_PREPARED;
            if (onStateChangedListener != null) {
                onStateChangedListener.onPrepared();
            }
        }

        @Override
        public void onAdStart(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            isPlayingAd = true;
            if (onAdvertisementListener != null) {
                onAdvertisementListener.onAdStart();
            }
            PlayerEvent.ad_play_load(
                    logPlayerEvent,
                    (DateUtils.currentTimeMillis() - logPlayerOpenTime),
                    "", 0, logPlayerFlag);
        }

        @Override
        public void onAdEnd(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            isPlayingAd = false;
            if (onAdvertisementListener != null) {
                onAdvertisementListener.onAdEnd();
            }
            PlayerEvent.ad_play_exit(
                    logPlayerEvent,
                    (DateUtils.currentTimeMillis() - logPlayerOpenTime),
                    "", 0, logPlayerFlag);
        }

        @Override
        public void onMiddleAdStart(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            //中插广告开始播放
            isPlayingAd = true;
            if (onAdvertisementListener != null) {
                onAdvertisementListener.onMiddleAdStart();
            }
            PlayerEvent.ad_play_load(
                    logPlayerEvent,
                    (DateUtils.currentTimeMillis() - logPlayerOpenTime),
                    "", 0, logPlayerFlag);

        }

        @Override
        public void onMiddleAdEnd(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            //中插广告播放结束
            isPlayingAd = false;
            if (onAdvertisementListener != null) {
                onAdvertisementListener.onMiddleAdEnd();
            }
            PlayerEvent.ad_play_exit(
                    logPlayerEvent,
                    (DateUtils.currentTimeMillis() - logPlayerOpenTime),
                    "", 0, logPlayerFlag);

        }

        @Override
        public void onStarted(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            mCurrentState = STATE_PLAYING;
            if (onStateChangedListener != null) {
                onStateChangedListener.onStarted();
            }
        }

        @Override
        public void onPaused(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            mCurrentState = STATE_PAUSED;
            if (onStateChangedListener != null) {
                onStateChangedListener.onPaused();
            }
        }

        @Override
        public void onCompleted(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            mCurrentState = STATE_COMPLETED;
            if (onStateChangedListener != null) {
                onStateChangedListener.onCompleted();
            }
        }

        @Override
        public void onStopped(IMediaPlayer iMediaPlayer) {
            if (mPlayer != null && getCurrentPosition() >= mDuration) {
                // 奇艺试看结束后也会触发此回调
                mCurrentState = STATE_COMPLETED;
                if (onStateChangedListener != null) {
                    onStateChangedListener.onCompleted();
                }
            }

        }

        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, ISdkError iSdkError) {
            if (mPlayer == null) {
                return true;
            }
            mCurrentState = STATE_ERROR;
            Log.e(TAG, "QiYiPlayer onError:" + iSdkError.getCode() + " " + iSdkError.getMsgFromError());
            PlayerEvent.videoExcept(
                    "mediaexception", iSdkError.getCode(),
                    logPlayerEvent, 0,
                    getCurrentPosition(), logPlayerFlag);
            if (onStateChangedListener != null) {
                onStateChangedListener.onError(iSdkError.getMsgFromError());
            }
            return true;
        }
    };

    private IMediaPlayer.OnBitStreamInfoListener qiyiBitStreamInfoListener = new IMediaPlayer.OnBitStreamInfoListener() {
        @Override
        public void onPlayableBitStreamListUpdate(IMediaPlayer iMediaPlayer, List<BitStream> list) {
            if (mPlayer == null) {
                return;
            }
            mQualities = new ArrayList<>();
            mBitStreamList = list;
            for (BitStream bitStream : list) {
                Log.i(TAG, "bitStream:" + bitStream.getValue());
                // 只显示对应视云，流畅，高清，超清码率
                if (bitStream.getValue() > 1) {
                    mQualities.add(bitStreamConvertToQuality(bitStream));
                }
            }
        }

        @Override
        public void onVipBitStreamListUpdate(IMediaPlayer iMediaPlayer, List<BitStream> list) {
            Log.i(TAG, "bitStream:onVipBitStreamListUpdate");

        }

        @Override
        public void onBitStreamSelected(IMediaPlayer iMediaPlayer, BitStream bitStream) {
            if (mPlayer == null) {
                return;
            }
            mCurrentQuality = bitStreamConvertToQuality(bitStream);
        }
    };

    private IMediaPlayer.OnPreviewInfoListener qiyiPreviewInfoListener = new IMediaPlayer.OnPreviewInfoListener() {
        @Override
        public void onPreviewInfoReady(IMediaPlayer iMediaPlayer, boolean isPreview, int length) {
            Log.d(TAG, "QiYiOnPreview: " + isPreview + ", length = " + length);
            if (mPlayer == null) {
                return;
            }
            if (isPreview) {
                mDuration = length;
            }
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener qiyiVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height) {
            Log.i("LH/", "onVideoSizeChangedQiYi:" + width + " " + height);
            if (mPlayer == null) {
                return;
            }
            if (onStateChangedListener != null) {
                onStateChangedListener.onVideoSizeChanged(width, height);
            }
        }
    };

    private IMediaPlayer.OnSeekCompleteListener qiyiSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekCompleted(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            if (isInPlaybackState()) {
                PlayerEvent.videoPlaySeekBlockend(
                        logPlayerEvent,
                        0,
                        getCurrentPosition(),
                        DateUtils.currentTimeMillis() - logBufferStartTime,
                        logMediaIp, logPlayerFlag);
            }
            if (onStateChangedListener != null) {
                onStateChangedListener.onSeekCompleted();
            }
        }
    };

    private IMediaPlayer.OnBufferChangedListener qiyiBufferChangedListener = new IMediaPlayer.OnBufferChangedListener() {
        @Override
        public void onBufferStart(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            if (onBufferChangedListener != null) {
                onBufferChangedListener.onBufferStart();
            }
            logBufferStartTime = DateUtils.currentTimeMillis();
        }

        @Override
        public void onBufferEnd(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            if (onBufferChangedListener != null) {
                onBufferChangedListener.onBufferEnd();
            }
            if (mSurfaceAttached && logFirstOpenPlayer) {
                logFirstOpenPlayer = false;
                if (!isPlayingAd) {
                    PlayerEvent.videoPlayLoad(
                            logPlayerEvent,
                            (DateUtils.currentTimeMillis() - logPlayerOpenTime),
                            0, "", "", logPlayerFlag);
                }
            } else if (isPlayingAd) {
                PlayerEvent.ad_play_blockend(
                        logPlayerEvent,
                        (DateUtils.currentTimeMillis() - logBufferStartTime),
                        "", 0, logPlayerFlag);
            } else {
                PlayerEvent.videoPlayBlockend(
                        logPlayerEvent,
                        0, getCurrentPosition(),
                        "", logPlayerFlag);
            }
        }
    };

    private IMediaPlayer.OnInfoListener onInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public void onInfo(IMediaPlayer iMediaPlayer, int i, Object o) {
            if (mPlayer == null) {
                return;
            }
            if (onStateChangedListener != null) {
                onStateChangedListener.onInfo(i, o);
            }

        }
    };

    private BitStream qualityConvertToBitStream(ClipEntity.Quality quality) {
        // 更改为new_vip分支显示样式
        switch (quality) {
            case QUALITY_NORMAL:// 流畅
                return BitStream.BITSTREAM_HIGH;
            case QUALITY_MEDIUM:// 高清
                if (!mBitStreamList.isEmpty()) {
                    if (mBitStreamList.contains(BitStream.BITSTREAM_720P)) {
                        return BitStream.BITSTREAM_720P;
                    } else if (mBitStreamList.contains(BitStream.BITSTREAM_720P_DOLBY)) {
                        return BitStream.BITSTREAM_720P_DOLBY;
                    } else if (mBitStreamList.contains(BitStream.BITSTREAM_720P_H265)) {
                        return BitStream.BITSTREAM_720P_H265;
                    }
                }
                return BitStream.BITSTREAM_720P;
            case QUALITY_HIGH:// 超清
                if (!mBitStreamList.isEmpty()) {
                    if (mBitStreamList.contains(BitStream.BITSTREAM_1080P)) {
                        return BitStream.BITSTREAM_1080P;
                    } else if (mBitStreamList.contains(BitStream.BITSTREAM_1080P_DOLBY)) {
                        return BitStream.BITSTREAM_1080P_DOLBY;
                    } else if (mBitStreamList.contains(BitStream.BITSTREAM_1080P_H265)) {
                        return BitStream.BITSTREAM_1080P_H265;
                    }
                }
                return BitStream.BITSTREAM_1080P;
            case QUALITY_ULTRA:
                Log.e(TAG, "Only support normal, medium, high quality.");
                break;
            case QUALITY_BLUERAY:
            case QUALITY_4K:
                if (!mBitStreamList.isEmpty()) {
                    if (mBitStreamList.contains(BitStream.BITSTREAM_4K)) {
                        return BitStream.BITSTREAM_4K;
                    } else if (mBitStreamList.contains(BitStream.BITSTREAM_4K_DOLBY)) {
                        return BitStream.BITSTREAM_4K_DOLBY;
                    } else if (mBitStreamList.contains(BitStream.BITSTREAM_4K_H265)) {
                        return BitStream.BITSTREAM_4K_H265;
                    }
                }
                return BitStream.BITSTREAM_4K;
        }
        return null;
    }

    private ClipEntity.Quality bitStreamConvertToQuality(BitStream bitStream) {
//        for (BitStream d : mBitStreamList) {
//            if (d.equals(BitStream.BITSTREAM_HIGH)) {
//                avalibleRate[0] = true;
//                // currQuality = 0;
//            } else if (d.equals(BitStream.BITSTREAM_720P)) {
//                avalibleRate[1] = true;
//                // currQuality = 1;
//            } else if (d.equals(BitStream.BITSTREAM_1080P)) {
//                avalibleRate[2] = true;
//                // currQuality = 2;
//            }
//        }
        // 更改为以上显示方式
        if (bitStream == BitStream.BITSTREAM_HIGH) {
            return ClipEntity.Quality.QUALITY_NORMAL;
        } else if (bitStream == BitStream.BITSTREAM_720P) {
            return ClipEntity.Quality.QUALITY_MEDIUM;
        } else if (bitStream == BitStream.BITSTREAM_1080P) {
            return ClipEntity.Quality.QUALITY_HIGH;
        }
        return null;
    }

}
