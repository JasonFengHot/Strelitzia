package tv.ismar.player.media;

import android.view.SurfaceView;

import com.qiyi.sdk.player.SdkVideo;

import java.util.List;

import tv.ismar.player.IsmartvPlayer;
import tv.ismar.player.model.ClipEntity;
import tv.ismar.player.model.MediaEntity;

/**
 * Created by LongHai on 17-4-26.
 */

public class QiyiPlayer extends IsmartvPlayer {

    @Override
    protected void createPlayer(SdkVideo sdkVideo) {

    }

    @Override
    public void attachSurfaceView(SurfaceView surfaceView) {

    }

    @Override
    public void detachViews() {

    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void seekTo(int position) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void release() {

    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getAdCountDownTime() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public ClipEntity.Quality getCurrentQuality() {
        return null;
    }

    @Override
    public List<ClipEntity.Quality> getQualities() {
        return null;
    }

    @Override
    public void switchQuality(ClipEntity.Quality quality) {

    }

}
