package tv.ismar.app.service;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.blankj.utilcode.util.DeviceUtils;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.network.SkyService;
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
    private int port;

    @Override
    public void onCreate() {
        super.onCreate();
        server = new AsyncHttpServer();
        audioManager= (AudioManager) getSystemService(AUDIO_SERVICE);
        new Thread(){
            @Override
            public void run() {
                port = getAvailablePort();
                server.listen(port);
                reportIp();
            }
        }.start();
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


    public boolean isPortUsing(int port) {
        boolean flag;
        try {
            InetAddress theAddress = InetAddress.getByName("127.0.0.1");
            new Socket(theAddress, port);
            flag = true;
        } catch (IOException e) {
            flag = false;
        }
        return flag;
    }

    private int getAvailablePort(){
        int defaultPort = 10114;
        for (int i = 10114; i < 65536; i++){
            if (!isPortUsing(i)){
                defaultPort = i;
                break;
            }
        }
        return defaultPort;
    }

    private void reportIp() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sn = sharedPreferences.getString("sn_token", "");
        String ip = "";
        if (sn == null || sn.equals("")) {
        } else {
            SkyService skyService = SkyService.ServiceManager.getService();
            String url = "http://wx.api.tvxio.com/weixin4server/uploadclientip";
            if (tv.ismar.library.util.DeviceUtils.getLocalInetAddress() != null) {
                ip = tv.ismar.library.util.DeviceUtils.getLocalInetAddress().toString() + ":" + port;
            }
            skyService.weixinIp(url, ip, sn, Build.MODEL, tv.ismar.library.util.DeviceUtils.getLocalMacAddress(this))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {

                        }
                    });
        }
    }
}
