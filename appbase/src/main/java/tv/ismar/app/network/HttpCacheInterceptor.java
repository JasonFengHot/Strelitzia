package tv.ismar.app.network;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import tv.ismar.app.util.NetworkUtils;

/**
 * Created by beaver on 16-12-27.
 */

public class HttpCacheInterceptor implements Interceptor {
    private static final String TAG = HttpCacheInterceptor.class.getSimpleName();

    private Context mContext;

    public HttpCacheInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!NetworkUtils.isConnected(mContext)) {
            HttpUrl httpUrl = request.url();
            String path = httpUrl.encodedPath();
            if (path.contains("/api/tv/channels/")
                    || path.contains("/api/tv/homepage/banner/")
                    || path.contains("/api/tv/banner/")
                    ||path.contains("/api/tv/banners/")) {
                Log.d(TAG, "Not Connected intercept: " + path);
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
            }
        }

        try {
            return chain.proceed(request);
        }catch (Exception e){
            HttpUrl httpUrl = request.url();
            String path = httpUrl.encodedPath();
            Log.d(TAG, "Exception intercept: " + path);
            if (path.contains("/api/tv/channels/")
                    || path.contains("/api/tv/homepage/banner/")
                    || path.contains("/api/tv/banner/")
                    ||path.contains("/api/tv/banners/")) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
                return chain.proceed(request);
            }else {
                throw e;
            }
        }
    }
}
