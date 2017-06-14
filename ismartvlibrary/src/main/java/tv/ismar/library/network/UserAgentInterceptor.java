package tv.ismar.library.network;

import android.os.Build;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import tv.ismar.library.util.C;

public class UserAgentInterceptor implements Interceptor {

    public UserAgentInterceptor() {

    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request requestWithUserAgent = originalRequest.newBuilder()
            .removeHeader("User-Agent")
            .addHeader("User-Agent", Build.MODEL.replaceAll(" ", "_") + "/" + C.versionCode + " " + C.snToken)
            .build();

        return chain.proceed(requestWithUserAgent);
    }
}