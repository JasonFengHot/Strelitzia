package tv.ismar.player;

import android.view.SurfaceView;
import android.view.ViewGroup;

import java.util.List;

import tv.ismar.app.entity.ClipEntity;
import tv.ismar.player.model.MediaEntity;

/**
 * Created by longhai on 16-9-12.
 */
public interface IPlayer {

    void prepare(MediaEntity mediaSource);

    /**
     * @param surfaceView 视云片源使用参数
     */
    void attachSurfaceView(SurfaceView surfaceView);

    void detachViews();

    void start();

    void pause();

    void seekTo(int position);

    void stop();

    void release();

    int getCurrentPosition();

    int getDuration();

    int getAdCountDownTime();

    boolean isPlaying();

    ClipEntity.Quality getCurrentQuality();

    List<ClipEntity.Quality> getQualities();

    /**
     * @param position 部分播放器在先调用了stop()之后，再调用getCurrentPosition导致的onError回调
     */
    void switchQuality(int position, ClipEntity.Quality quality);

    interface OnBufferChangedListener {

        void onBufferStart();

        void onBufferEnd();
    }

    interface OnAdvertisementListener {

        void onAdStart();

        void onAdEnd();

        void onMiddleAdStart();

        void onMiddleAdEnd();

    }

    interface OnStateChangedListener {

        void onPrepared();

        void onStarted();

        void onPaused();

        void onSeekCompleted();

        void onCompleted();

        void onInfo(int what, Object extra);

        void onVideoSizeChanged(int videoWidth, int videoHeight);

        boolean onError(String message);

    }

}
