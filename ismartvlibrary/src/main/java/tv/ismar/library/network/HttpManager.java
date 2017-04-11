package tv.ismar.library.network;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by LongHai on 17-4-7.
 */

public class HttpManager {

    private static final int DEFAULT_CONNECT_TIMEOUT = 6;
    private static final int DEFAULT_READ_TIMEOUT = 15;
    private static HttpManager instance;
    private OkHttpClient okHttpClient;

    private String domain;
    private static final String API_WX_HOST = "http://wx.api.tvxio.com/";
    private static final String API_IRIS_HOST = "http://iris.tvxio.com/";
    private static final String API_SPEED_CALLA_HOST = "http://speed.calla.tvxio.com/";
    private static final String API_LILY_HOST = "http://lily.tvxio.com/";

    private Object domainService;

    public static HttpManager getInstance() {
        if (instance == null) {
            synchronized (HttpManager.class) {
                if (instance == null) {
                    instance = new HttpManager();
                }
            }
        }
        return instance;
    }

    private HttpManager() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        SSLSocketFactory ssf = null;
        try {
            TrustManager[] tm = {new MyX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            ssf = sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .sslSocketFactory(ssf)
                .build();
    }

    public void init(String domain) {

    }

    @SuppressWarnings("unchecked")
    public synchronized static <T> T getDomainService(Class<T> service) {
        if (getInstance().domainService == null) {
            ObjectMapper objectMapper = new ObjectMapper();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://sky.tvxio.com/v3_0/SKY2/tou/")
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(getInstance().okHttpClient)
                    .build();
            getInstance().domainService = retrofit.create(service);
        }
        return (T) getInstance().domainService;
    }

    public static String syncGetRequest(String url) throws IOException {
        String resultString = null;
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = getInstance().okHttpClient.newCall(request);
        Response response = call.execute();
        if (response.isSuccessful()) {
            resultString = response.body().string();
        }
        return resultString;
    }

    public static void asyncGetRequest(String urlString, String requestTag, Callback okHttpCallback) {
        Request request = new Request.Builder()
                .url(urlString)
                .tag(requestTag)
                .build();
        Call call = getInstance().okHttpClient.newCall(request);
        call.enqueue(okHttpCallback);
    }

    public static void asyncPostRequest(String url, String params, String requestTag, Callback okHttpCallback) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .tag(requestTag)
                .build();
        Call call = getInstance().okHttpClient.newCall(request);
        call.enqueue(okHttpCallback);
    }

    public static void asyncFormPostRequest(String url, Map<String, Object> params, Callback okHttpCallback) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                formBuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        Request request = new Request.Builder()
                .url(url)
                .post(formBuilder.build())
                .build();
        Call call = getInstance().okHttpClient.newCall(request);
        call.enqueue(okHttpCallback);
    }

}
