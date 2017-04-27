package tv.ismar.player.gui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import tv.ismar.player.R;

public class PlaybackActivity extends FragmentActivity implements PlaybackService.Client.Callback {

    private Fragment playbackFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        playbackFragment = getSupportFragmentManager().findFragmentById(R.id.playback_fragment);

    }

    @Override
    public void onConnected(PlaybackService service) {

    }

    @Override
    public void onDisconnected() {

    }
}
