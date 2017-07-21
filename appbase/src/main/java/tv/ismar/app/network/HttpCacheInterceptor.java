package tv.ismar.app.network;

import android.content.Context;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import tv.ismar.app.util.NetworkUtils;

/**
 * Created by beaver on 16-12-27.
 */

public class HttpCacheInterceptor implements Interceptor {

    private Context mContext;

    public HttpCacheInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!NetworkUtils.isConnected(mContext)) {
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();

        }

        try {
            return chain.proceed(request);
        }catch (Exception e){
            e.printStackTrace();
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
            return chain.proceed(request);
        }
    }
}
