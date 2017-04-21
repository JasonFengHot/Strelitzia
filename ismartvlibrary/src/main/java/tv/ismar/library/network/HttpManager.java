package tv.ismar.library.network;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import okhttp3.Cache;
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
import tv.ismar.library.exception.ParameterException;
import tv.ismar.library.util.EmptyUtils;

/**
 * Created by LongHai on 17-4-7.
 */

public class HttpManager {

    private static final int DEFAULT_CONNECT_TIMEOUT = 6;
    private static final int DEFAULT_READ_TIMEOUT = 15;
    private static HttpManager instance;
    private OkHttpClient okHttpClient;

    private String domain, upgrade_domain;
    private static final String HOST_WEATHER = "http://media.lily.tvxio.com/";
    private static final String HOST_WX_API = "http://wx.api.tvxio.com/";
    private static final String HOST_IRIS = "http://iris.tvxio.com/";
//    private static final String HOST_SPEED_CALLA = "http://speed.calla.tvxio.com/";
//    private static final String HOST_LILY = "http://lily.tvxio.com/";

    static String DEVICE_TOKEN, ACCESS_TOKEN;

    private Object domainService;
    private Object upgradeService;
    private Object weatherService;
    private Object wxApiService;
    private Object irisService;

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

    public void init(String domain, String upgrade_domain, String deviceToken) {
        this.domain = domain;
        this.upgrade_domain = upgrade_domain;
        DEVICE_TOKEN = deviceToken;
    }

    public void setAccessToken(String accessToken) {
        ACCESS_TOKEN = accessToken;
    }

    private HttpManager() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        HttpParamsInterceptor paramsInterceptor = new HttpParamsInterceptor.Builder()
                .build();

        SSLSocketFactory ssf = null;
        try {
            TrustManager[] tm = {new MyX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tm, new java.security.SecureRandom());
            ssf = sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .addInterceptor(paramsInterceptor)
                .sslSocketFactory(ssf)
                .build();
    }

    private static Retrofit getRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(appendProtocol(baseUrl))
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(getInstance().okHttpClient)
                .build();
    }

    @SuppressWarnings("unchecked")
    public synchronized static <T> T getDomainService(Class<T> service) {
        String domain = getInstance().domain;
        if (EmptyUtils.isEmptyText(domain)) {
            throw new ParameterException(HttpManager.class.getSimpleName() + " > getDomainService");
        }
        if (getInstance().domainService == null) {
            getInstance().domainService = getRetrofit(domain).create(service);
        }
        return (T) getInstance().domainService;
    }

    @SuppressWarnings("unchecked")
    public synchronized static <T> T getUpgradeService(Class<T> service) {
        String upgrade_domain = getInstance().upgrade_domain;
        if (EmptyUtils.isEmptyText(upgrade_domain)) {
            throw new ParameterException(HttpManager.class.getSimpleName() + " > getUpgradeService");
        }
        if (getInstance().upgradeService == null) {
            getInstance().upgradeService = getRetrofit(upgrade_domain).create(service);
        }
        return (T) getInstance().upgradeService;
    }

    @SuppressWarnings("unchecked")
    public synchronized static <T> T getWeatherService(Class<T> service) {
        if (getInstance().weatherService == null) {
            getInstance().weatherService = getRetrofit(HOST_WEATHER).create(service);
        }
        return (T) getInstance().weatherService;
    }

    @SuppressWarnings("unchecked")
    public synchronized static <T> T getWxApiService(Class<T> service) {
        if (getInstance().wxApiService == null) {
            getInstance().wxApiService = getRetrofit(HOST_WX_API).create(service);
        }
        return (T) getInstance().wxApiService;
    }

    @SuppressWarnings("unchecked")
    public synchronized static <T> T getIrisService(Class<T> service) {
        if (getInstance().irisService == null) {
            getInstance().irisService = getRetrofit(HOST_IRIS).create(service);
        }
        return (T) getInstance().irisService;
    }

    /**
     * @param context    Application Context
     * @param url        Url Address
     * @param requestTag Tag值，取消请求时用到
     * @param callback   异步回调
     */
    public static void asyncCacheRequest(Context context, String url, String requestTag, Callback callback) {
        File cacheFile = new File(context.getApplicationContext().getCacheDir(), "okhttp_disk_cache");
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 100);
        OkHttpClient cacheClient = getInstance().okHttpClient.newBuilder()
                .cache(cache)
                .addInterceptor(new HttpCacheInterceptor(context.getApplicationContext()))
                .addNetworkInterceptor(new HttpCacheInterceptor(context.getApplicationContext()))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .tag(requestTag)
                .build();
        Call call = cacheClient.newCall(request);
        call.enqueue(callback);
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

    public static void asyncGetRequest(String url, String requestTag, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .tag(requestTag)
                .build();
        Call call = getInstance().okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public static void asyncPostRequest(String url, String params, String requestTag, Callback callback) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .tag(requestTag)
                .build();
        Call call = getInstance().okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public static void asyncFormPostRequest(String url, Map<String, Object> params, String requestTag, Callback callback) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                formBuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        Request request = new Request.Builder()
                .url(url)
                .post(formBuilder.build())
                .tag(requestTag)
                .build();
        Call call = getInstance().okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    private static String appendProtocol(String host) {
        Uri uri = Uri.parse(host);
        String url = uri.toString();
        if (!uri.toString().startsWith("http://") && !uri.toString().startsWith("https://")) {
            url = "http://" + host;
        }
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    public void cancelTag(Object tag) {
        for (Call call : getInstance().okHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : getInstance().okHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    public void cancelAll() {
        for (Call call : getInstance().okHttpClient.dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : getInstance().okHttpClient.dispatcher().runningCalls()) {
            call.cancel();
        }
    }

}
