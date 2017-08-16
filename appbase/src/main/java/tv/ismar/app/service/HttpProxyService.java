package tv.ismar.app.service;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import com.blankj.utilcode.util.DeviceUtils;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import tv.ismar.library.exception.ExceptionUtils;

/**
 * Created by liucan on 2017/5/18.
 */

public class HttpProxyService extends Service implements HttpServerRequestCallback{
    private AudioManager audioManager;
    private AsyncHttpServer server;
    private static final String HTTP_ACTIOIN = "/keyevent";
    private static final String PING = "/ping";
    private static final String MODEL = "/model";
    private static final String TAG = "HttpProxyService";

    private static final int BUTTON_KEY_EVENT = 1;
    private static final int VOL_SEEK_EVENT = 2;
    private static final int PLAY_VIDEO_EVENT = 3;
    private static final int DELETE_CDN = 4;

    @Override
    public void onCreate() {
        server = new AsyncHttpServer();
        server.listen(10114);
        audioManager= (AudioManager) getSystemService(AUDIO_SERVICE);
        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        server.get(HTTP_ACTIOIN,this);
        Log.i("VoiceMax", audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)+"  :voice");
        Log.i("VoiceMax", audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)+"  :current Voice");
        return super.onStartCommand(intent, flags, startId);
    }
    public static void simulateKeystroke(final int KeyCode) {

        new Thread(new Runnable() {
            public void run() {
                // TODO Auto-generated method stub
                try {
                    Instrumentation inst=new Instrumentation();
                    inst.sendKeyDownUpSync(KeyCode);
                } catch (Exception e) {
                    ExceptionUtils.sendProgramError(e);
                    // TODO: handle exception
                }
            }
        }).start();
    }

    @Override
    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        Log.i("weixin","keycode= "+ request.getQuery().getString("keycode"));
        if (MODEL.equals(request.getPath())) {
            response.send(DeviceUtils.getModel());
            response.writeHead();
        } else if (PING.equals(request.getPath())) {
            response.send("OK!");
            response.writeHead();
        } else if (HTTP_ACTIOIN.equals(request.getPath())) {
            int actionCode = Integer.parseInt(request.getQuery().getString("action"));
            switch (actionCode) {
                case BUTTON_KEY_EVENT:
                    simulateKeystroke(Integer.parseInt( request.getQuery().getString("keycode")));
                    break;
                case VOL_SEEK_EVENT:
                    int index=Integer.parseInt(request.getQuery().getString("seek"));
                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,index,AudioManager.FLAG_SHOW_UI);
                    break;
                case PLAY_VIDEO_EVENT:
                    break;
                case DELETE_CDN:
                    break;
                default:
                    break;
            }
            response.send("OK!");

        }
        response.end();
    }
}
