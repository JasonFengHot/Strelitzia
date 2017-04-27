package tv.ismar.player.media;

import java.util.List;

import tv.ismar.player.IsmartvPlayer;
import tv.ismar.player.SmartPlayer;
import tv.ismar.player.model.ClipEntity;
import tv.ismar.player.model.MediaMeta;

/**
 * Created by LongHai on 17-4-26.
 */

public class DaisyPlayer extends IsmartvPlayer {

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
    private SmartPlayer mPlayer;

    @Override
    protected void createPlayer(MediaMeta mediaMeta) {

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

}
