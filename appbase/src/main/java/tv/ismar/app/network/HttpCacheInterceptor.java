package tv.ismar.app.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import tv.ismar.account.IsmartvDns;
import tv.ismar.app.ui.ToastTip;
import tv.ismar.app.util.NetworkUtils;

/**
 * Created by beaver on 16-12-27.
 */

public class HttpCacheInterceptor implements Interceptor {
    private static final String TAG = HttpCacheInterceptor.class.getSimpleName();
    private static List<String> domainList;

    private static boolean occurError = false;
    private static ErrorHandler mErrorHandler = new ErrorHandler();
    private static final int CHANGE_STATUS = 0x0001;
    private static final int SHOW_TOAST = 0x0002;


    static {
        String[] domain = new String[]{
                "1.1.1.1",  //sky域名
                "1.1.1.2",  //广告域名
                "1.1.1.3",  //更新域名
                "1.1.1.4",  //日志域名
                "1.1.1.5",  //未知
                "1.1.1.6"   //爱奇艺购买域名
        };
        domainList = Arrays.asList(domain);
    }

    private static Context mContext;

    public HttpCacheInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String host = request.url().host();
        if (!NetworkUtils.isConnected(mContext)||domainList.indexOf(host) != -1) {
            HttpUrl httpUrl = request.url();
            String path = httpUrl.encodedPath();
            if (path.contains("/api/tv/channels/")
                    || path.contains("/api/tv/homepage/banner/")
                    || path.contains("/api/tv/banner/")
                    ||path.contains("/api/tv/banners/")) {
//                Log.d(TAG, "Not Connected intercept: " + path);
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
                showToast();
            }
        }

        try {
            return chain.proceed(request);
        }catch (Exception e){
            HttpUrl httpUrl = request.url();
            String path = httpUrl.encodedPath();
//            Log.d(TAG, "Exception intercept: " + path);
            if (path.contains("/api/tv/channels/")
                    || path.contains("/api/tv/homepage/banner/")
                    || path.contains("/api/tv/banner/")
                    ||path.contains("/api/tv/banners/")) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
                showToast();
                return chain.proceed(request);
            }else {
                throw e;
            }
        }
    }

    private void showToast(){
        if (!occurError){
            if (mContext!= null){
                occurError = true;
                mErrorHandler.sendEmptyMessage(SHOW_TOAST);
                mErrorHandler.sendEmptyMessageDelayed(CHANGE_STATUS, 2000);
            }
        }
    }

     static class ErrorHandler extends Handler {
        public ErrorHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CHANGE_STATUS:
                    occurError = false;
                    break;
                case SHOW_TOAST:
                    ToastTip.showToast(mContext, "网络连接超时，请重试");
                    break;
            }
        }
    }
}
