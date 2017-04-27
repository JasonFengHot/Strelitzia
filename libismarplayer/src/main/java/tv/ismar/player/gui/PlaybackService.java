package tv.ismar.player.gui;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.MainThread;

import tv.ismar.player.IsmartvPlayer;

public class PlaybackService extends Service {

    private static final String TAG = "LH/PlaybackService";

    private class LocalBinder extends Binder {
        PlaybackService getService() {
            return PlaybackService.this;
        }
    }

    public static PlaybackService getService(IBinder iBinder) {
        LocalBinder binder = (LocalBinder) iBinder;
        return binder.getService();
    }

    private final IBinder mBinder = new LocalBinder();

    private MediaPlayer mp4Player;// 频道首页导视或广告等mp4文件的播放
    private IsmartvPlayer hlsPlayer;// HLS播放
    private boolean mIsAudioTrack = false;
    private boolean mHasHdmiAudio = false;
    private boolean mHasAudioFocus = false;

    public PlaybackService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public static class Client {
        public static final String TAG = "LH/PlaybackService.Client";

        @MainThread
        public interface Callback {
            void onConnected(PlaybackService service);

            void onDisconnected();
        }

        private boolean mBound = false;
        private final Callback mCallback;
        private final Context mContext;

        private final ServiceConnection mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                if (!mBound)
                    return;

                final PlaybackService service = PlaybackService.getService(iBinder);
                if (service != null)
                    mCallback.onConnected(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBound = false;
                mCallback.onDisconnected();
            }
        };

        private static Intent getServiceIntent(Context context) {
            return new Intent(context, PlaybackService.class);
        }

        private static void startService(Context context) {
            context.startService(getServiceIntent(context));
        }

        private static void stopService(Context context) {
            context.stopService(getServiceIntent(context));
        }

        public Client(Context context, Callback callback) {
            if (context == null || callback == null)
                throw new IllegalArgumentException("Context and callback can't be null");
            mContext = context;
            mCallback = callback;
        }

        @MainThread
        public void connect() {
            if (mBound)
                throw new IllegalStateException("already connected");
            startService(mContext);
            mBound = mContext.bindService(getServiceIntent(mContext), mServiceConnection, Context.BIND_AUTO_CREATE);
        }

        @MainThread
        public void disconnect() {
            if (mBound) {
                mBound = false;
                mContext.unbindService(mServiceConnection);
            }
        }

        public static void restartService(Context context) {
            stopService(context);
            startService(context);
        }
    }

    private void changeAudioFocus(boolean acquire) {
        final AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am == null)
            return;

        if (acquire) {
            if (!mHasAudioFocus) {
                final int result = am.requestAudioFocus(mAudioFocusListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    am.setParameters("bgm_state=true");
                    mHasAudioFocus = true;
                }
            }
        } else {
            if (mHasAudioFocus) {
                final int result = am.abandonAudioFocus(mAudioFocusListener);
                am.setParameters("bgm_state=false");
                mHasAudioFocus = false;
            }
        }
    }

    private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener = createOnAudioFocusChangeListener();

    private AudioManager.OnAudioFocusChangeListener createOnAudioFocusChangeListener() {
        return new AudioManager.OnAudioFocusChangeListener() {

            private boolean mLossTransient = false;
            private boolean mLossTransientCanDuck = false;

            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS:
                        // Stop playback
                        changeAudioFocus(false);
//                        stop();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        // Pause playback
//                        if (mMediaPlayer.isPlaying()) {
//                            mLossTransient = true;
//                            mMediaPlayer.pause();
//                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        // Lower the volume
//                        if (mMediaPlayer.isPlaying()) {
//                            mMediaPlayer.setVolume(36);
//                            mLossTransientCanDuck = true;
//                        }
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        // Resume playback
//                        if (mLossTransientCanDuck) {
//                            mMediaPlayer.setVolume(100);
//                            mLossTransientCanDuck = false;
//                        }
//                        if (mLossTransient) {
//                            mMediaPlayer.play();
//                            mLossTransient = false;
//                        }
                        break;
                }
            }
        };
    }
}
