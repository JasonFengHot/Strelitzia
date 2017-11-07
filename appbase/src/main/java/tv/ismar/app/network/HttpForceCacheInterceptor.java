package tv.ismar.app.network;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by beaver on 16-12-27.
 */

public class HttpForceCacheInterceptor implements Interceptor {
    private static final String TAG = HttpForceCacheInterceptor.class.getSimpleName();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        request = request.newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build();
        return chain.proceed(request);
    }
}
