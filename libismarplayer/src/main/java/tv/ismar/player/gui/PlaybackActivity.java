package tv.ismar.player.gui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.library.util.LogUtils;
import tv.ismar.player.R;

public class PlaybackActivity extends BaseActivity {

    private PlaybackFragment playbackFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        Intent intent = getIntent();
        int itemPK = intent.getIntExtra(PageIntentInterface.EXTRA_PK, 0);// 当前影片pk值,通过/api/item/{pk}可获取详细信息
        int subItemPk = intent.getIntExtra(PageIntentInterface.EXTRA_SUBITEM_PK, 0);// 当前多集片pk值,通过/api/subitem/{pk}可获取详细信息
        String source = intent.getStringExtra(PageIntentInterface.EXTRA_SOURCE);
        boolean qiyiflag = intent.getBooleanExtra(PageIntentInterface.QIYIFLAG,false);
        String contentMode=intent.getStringExtra("contentMode");
        if (itemPK <= 0) {
            finish();
            LogUtils.e("LH/PlaybackActivity", "itemId can't be null.");
            return;
        }
        playbackFragment = PlaybackFragment.newInstance(itemPK, subItemPk, source,qiyiflag,contentMode);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_player_container, playbackFragment)
                .commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.setPriority(1000);
        filter.addDataScheme("file");
        registerReceiver(mountReceiver, filter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mountReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (playbackFragment != null && playbackFragment.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (playbackFragment != null && playbackFragment.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private BroadcastReceiver mountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                if (playbackFragment != null) {
                    playbackFragment.mounted = true;
                }
            }
        }
    };

    // 移至PlaybackFragment
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode==200){
//            finish();
//        }
//    }
}
