package tv.ismar.account.statistics;

import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observer;
import rx.schedulers.Schedulers;
import tv.ismar.library.network.UserAgentInterceptor;
import tv.ismar.library.util.C;

/**
 * Created by huibin on 6/9/17.
 */

public class LogQueue {
    private static final String TAG = "LogQueue";
    private static final int DEFAULT_CONNECT_TIMEOUT = 6;
    private static final int DEFAULT_READ_TIMEOUT = 15;

    private volatile boolean isEmptyQueueImmediately = false;

    private static LogQueue ourInstance;
    private LinkedBlockingDeque<LogEntity> mLogQueue = new LinkedBlockingDeque<>(100);

    private Retrofit mRetrofit;

    public static LogQueue getInstance() {
        if (ourInstance == null) {
            synchronized (LogQueue.class) {
                if (ourInstance == null) {
                    ourInstance = new LogQueue();
                }
            }
        }
        return ourInstance;
    }

    private LogQueue() {
        okhttp3.logging.HttpLoggingInterceptor interceptor = new okhttp3.logging.HttpLoggingInterceptor();
        interceptor.setLevel(okhttp3.logging.HttpLoggingInterceptor.Level.BODY);
        OkHttpClient mClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new UserAgentInterceptor())
//                .addInterceptor(interceptor)
                .build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl("http://124.42.65.66:8082/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(mClient)
                .build();

        new Thread(new LogAsyncWrite()).start();
    }

    public void put(LogEntity entity) {
        if (mLogQueue.size() < 100) {
            try {
                mLogQueue.put(entity);
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "log queue is full !");
        }
    }

    private LogEntity take() {
        try {
            return mLogQueue.take();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private class LogAsyncWrite implements Runnable {
        private LogDataPackage dataPackage;

        public LogAsyncWrite() {
            dataPackage = new LogDataPackage();
        }

        public void run() {
            while (true) {
                try {
                    if (dataPackage.isFull()|| isEmptyQueueImmediately) {
                        sendLogPackageToServer(dataPackage);
                        dataPackage = new LogDataPackage();
                        if (isEmptyQueueImmediately){
                            isEmptyQueueImmediately = false;
                        }
                    } else {
                        if (!mLogQueue.isEmpty()){
                            LogEntity entity = take();
                            if (entity != null) {
                                dataPackage.put(entity);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendLogPackageToServer(LogDataPackage logDataPackage) throws IOException {
        String logData = new Gson().toJson(logDataPackage.getDataPackage());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
        gzipOutputStream.write(logData.getBytes());
        gzipOutputStream.flush();
        gzipOutputStream.close();

//        ByteString byteString = ByteString.encodeUtf8(new Gson().toJson(logDataPackage.getDataPackage()));
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/gzip"), outputStream.toByteArray());
        outputStream.flush();
        outputStream.close();

//        requestBody = gzip(requestBody);
//        MultipartBody.Part data = MultipartBody.Part.create(requestBody);

        String sn = C.snToken;
        String ip = C.ip;
        String modelType = Build.MODEL;
        String manufacture = Build.BRAND.replace(" ", "_");

        RequestBody parametersBody = new FormBody.Builder()
                .add("sn", sn)
                .add("ip", ip)
                .add("model_type", modelType)
                .add("manufacture", manufacture)
                .build();
//        MultipartBody.Part parameters = MultipartBody.Part.create(parametersBody);

        String url = "http://elderberry.test.tvxio.com/Elderberry/client/uploadLog";


        mRetrofit.create(UploadLogService.class).uploadLog(url, parametersBody, requestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "uploadLog: " + e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }
                });
    }

    private RequestBody gzip(final RequestBody body) {
        return new RequestBody() {

            @Override
            public MediaType contentType() {
                return body.contentType();
            }

            @Override
            public long contentLength() throws IOException {
                return -1;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                body.writeTo(gzipSink);
                gzipSink.close();
            }
        };
    }

    public void emptyQueue(){
        isEmptyQueueImmediately = true;
    }
}
