package tv.ismar.player.media;

import java.util.List;

import tv.ismar.player.IsmartvPlayer;
import tv.ismar.player.model.ClipEntity;
import tv.ismar.player.model.MediaEntity;
import tv.ismar.player.widget.AspectRatioFrameLayout;

/**
 * Created by LongHai on 17-4-26.
 */

public class QiyiPlayer extends IsmartvPlayer {
    @Override
    public void attachToView(AspectRatioFrameLayout container) {

    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void seekTo(long position) {

    }

    @Override
    public void stopPlayBack() {

    }

    @Override
    public long getCurrentPosition() {
        return 0;
    }

    @Override
    public long getDuration() {
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

    @Override
    public void switchMediaSource(MediaEntity mediaEntity) {

    }
}
