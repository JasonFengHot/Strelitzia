package tv.ismar.player;

import android.view.SurfaceView;

import java.util.List;

import tv.ismar.player.model.ClipEntity;
import tv.ismar.player.model.MediaEntity;

/**
 * Created by longhai on 16-9-12.
 */
public interface IPlayer {

    void prepare(MediaEntity mediaSource, boolean resetPosition);

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

    void switchQuality(ClipEntity.Quality quality);

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

        void onStarted();

        void onPaused();

        void onCompleted();

        void onInfo(int what, Object extra);

        void onVideoSizeChanged(int videoWidth, int videoHeight);

        boolean onError(String message);

    }

}
