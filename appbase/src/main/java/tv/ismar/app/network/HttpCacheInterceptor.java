package tv.ismar.app.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by beaver on 16-12-27.
 */

public class HttpCacheInterceptor implements Interceptor {
    private static final String TAG = HttpCacheInterceptor.class.getSimpleName();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        return chain.proceed(request);
//        return originalResponse.newBuilder()
//                .header("Cache-Control", "public, only-if-cached, max-age=5")
//                .removeHeader("Pragma")
//                .build();
    }
}
