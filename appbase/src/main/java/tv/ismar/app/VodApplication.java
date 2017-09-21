package tv.ismar.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;
import android.util.Log;

import com.blankj.utilcode.util.Utils;
import com.ismartv.lion.custom.ICallLog;
import com.ismartv.lion.custom.Parse;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.app.Application;
import cn.ismartv.truetime.TrueTime;
import okhttp3.Cache;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvHttpParamsInterceptor;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.account.IsmartvPlatform;
import tv.ismar.account.statistics.LogEntity;
import tv.ismar.account.statistics.LogQueue;
import tv.ismar.app.core.ImageCache;
import tv.ismar.app.core.InitializeProcess;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.client.MessageQueue;
import tv.ismar.app.core.preferences.AccountSharedPrefs;
import tv.ismar.app.db.DBHelper;
import tv.ismar.app.db.FavoriteManager;
import tv.ismar.app.db.HistoryManager;
import tv.ismar.app.db.LocalFavoriteManager;
import tv.ismar.app.db.LocalHistoryManager;
import tv.ismar.app.entity.ContentModel;
import tv.ismar.app.exception.CrashHandler;
import tv.ismar.app.network.HttpCacheInterceptor;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.service.HttpProxyService;
import tv.ismar.app.util.SPUtils;
import tv.ismar.library.exception.ExceptionUtils;
import tv.ismar.library.network.HttpManager;
import tv.ismar.library.network.UserAgentInterceptor;
import tv.ismar.library.util.C;
import tv.ismar.library.util.DeviceUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by beaver on 16-8-19.
 */
public class VodApplication extends Application {
    private static final String TAG = "VodApplication";
    private static IsmartvHttpParamsInterceptor mIsmartvHttpParamsInterceptor;
    private HttpCacheInterceptor mHttpCacheInterceptor;
    public static final boolean DEBUG = true;
    private ArrayList<WeakReference<OnLowMemoryListener>> mLowMemoryListeners;
    private static VodApplication appInstance;
    private HistoryManager mModuleHistoryManager;
    private FavoriteManager mModuleFavoriteManager;
    private DBHelper mModuleDBHelper;
    private ImageCache mImageCache;
    public ContentModel[] mContentModel;
    public static String DEVICE_TOKEN = "device_token";
    private static final int CORE_POOL_SIZE = 5;
    public static final String CACHED_LOG = "cached_log";
    private ExecutorService mExecutorService;
    private static SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    public static final String PREFERENCE_FILE_NAME = "Daisy";
    private boolean isFinish = true;


    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init();
        IsmartvPlatform.initPlatform(getApplicationContext());
        Log.i("LH/", "applicationOnCreate:" + TrueTime.now().getTime());
        initLogger();
        SPUtils.init(this);
        Utils.init(this);
        appInstance = this;
        ActiveAndroid.initialize(this);
        AccountSharedPrefs.initialize(this);
        load(this);
        initKKMediaPlayer();

        mIsmartvHttpParamsInterceptor = new IsmartvHttpParamsInterceptor.Builder()
                .build();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("DroidSansFallback.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        new Thread(new InitializeProcess(this)).start();
        BaseActivity.wasLoadSmartPlayerSo = false;
        Log.i("LH/", "applicationOnCreateEnd:" + TrueTime.now().getTime());
        Intent ootStartIntent = new Intent(this, HttpProxyService.class);
        this.startService(ootStartIntent);
        IsmartvActivator.initialize(this);
        initLogCallback();
        initConstants();
        initPicasso();
        File cacheFile = new File(VodApplication.getModuleAppContext().getCacheDir(), "");
        HttpManager.getInstance().initialize(mIsmartvHttpParamsInterceptor, getCacheInterceptor(), cacheFile);
    }

    private void initLogCallback() {
        Parse.iCallLog = new ICallLog() {
            @Override
            public void addParseError(String json, String msg) throws Exception {
                Log.e(TAG, "日志输出：内容：" + json);
                Log.e(TAG, "日志输出：异常：" + msg);
                LogEntity logEntity = new LogEntity("gson_error");
                LogEntity.LogContent logContent = new LogEntity.LogContent();
                logContent.setError_source(json);
                logContent.setError_info(msg);
                logEntity.setLog_content(logContent);
                LogQueue.getInstance().put(logEntity);
            }
        };
    }

    private void initConstants() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        C.snToken = sharedPreferences.getString("sn_token", "");
        C.ip = sharedPreferences.getString("ip", "");
        C.isReportLog = sharedPreferences.getInt("is_report_log", 1);
        C.report_log_size = sharedPreferences.getInt("report_log_size", 256);
        C.report_log_time_interval = sharedPreferences.getInt("report_log_time_interval", 60);

        PackageManager packageManager = getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            C.versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            C.versionCode = 0;
        }
    }

    private void initPicasso(){
        /*图片加载线程池*/
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        /*普通图片缓存HttpClient*/
        File cacheFile = new File(getCacheDir(), "picasso_cache");
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 100); //100Mb
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new UserAgentInterceptor())
                .addInterceptor(new HttpCacheInterceptor(getApplicationContext()))
//                .dns(new Dns() {
//                    @Override
//                    public List<InetAddress> lookup(String s) throws UnknownHostException {
//                        String ipAddress = IsmartvActivator.getHostByName(s);
//                        return Dns.SYSTEM.lookup(ipAddress);
//                    }
//                })
                .cache(cache)
                .build();

        Picasso picasso = new Picasso.Builder(this)
                .executor(executorService)
                .downloader(new OkHttp3Downloader(client))
                .build();
        Picasso.setSingletonInstance(picasso);

        /*首页图片缓存HttpClient*/
        File homepageCacheFile = new File(getCacheDir(), "homepage_picasso_cache");
        Cache homepageCache = new Cache(homepageCacheFile , 1024 * 1024 * 100); //100Mb
        OkHttpClient homepageClient = new OkHttpClient.Builder()
                .addInterceptor(new UserAgentInterceptor())
                .addInterceptor(new HttpCacheInterceptor(getApplicationContext()))
//                .dns(new Dns() {
//                    @Override
//                    public List<InetAddress> lookup(String s) throws UnknownHostException {
//                        String ipAddress = IsmartvActivator.getHostByName(s);
//                        return Dns.SYSTEM.lookup(ipAddress);
//                    }
//                })
                .cache(homepageCache)
                .build();

        Picasso homepagePicasso = new Picasso.Builder(this)
                .executor(executorService)
                .downloader(new OkHttp3Downloader(homepageClient))
                .build();
        Picasso.setSingletonInstanceHomepage(homepagePicasso);
    }


    public SharedPreferences.Editor getEditor() {
        return mEditor;
    }

    public static void setDevice_Token() {
        SimpleRestClient.device_token = mPreferences.getString(VodApplication.DEVICE_TOKEN, "");
    }

    public VodApplication() {
        mLowMemoryListeners = new ArrayList<>();
    }

    public static VodApplication get(Context context) {
        return (VodApplication) context.getApplicationContext();
    }

    public static IsmartvHttpParamsInterceptor getHttpParamsInterceptor() {
        return mIsmartvHttpParamsInterceptor;
    }

    public HttpCacheInterceptor getCacheInterceptor() {
        if (mHttpCacheInterceptor == null) {
            mHttpCacheInterceptor = new HttpCacheInterceptor(this);
        }
        return mHttpCacheInterceptor;
    }

    public static VodApplication getModuleAppContext() {
        return appInstance;
    }

    public void load(Context a) {
        try {
            mPreferences = a.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
            mEditor = mPreferences.edit();
            Set<String> cached_log = mPreferences.getStringSet(CACHED_LOG, null);
            mEditor.remove(CACHED_LOG).commit();
            new Thread(mUpLoadLogRunnable).start();
            isFinish = true;
            if (cached_log != null) {
                Iterator<String> it = cached_log.iterator();
                while (it.hasNext()) {
                    MessageQueue.addQueue(it.next());
                }
            }
        } catch (Exception e) {
            System.out.println("load(Activity a)=" + e);
        }
    }

    /**
     * Return this application {@link DBHelper}
     *
     * @return The application {@link DBHelper}
     */
    public DBHelper getModuleDBHelper() {
        if (mModuleDBHelper == null) {
            mModuleDBHelper = new DBHelper(this);
        }
        return mModuleDBHelper;
    }

    /**
     * Return this application {@link HistoryManager}
     *
     * @return The application {@link HistoryManager}
     */
    public HistoryManager getModuleHistoryManager() {
        if (mModuleHistoryManager == null) {
            mModuleHistoryManager = new LocalHistoryManager(this);
        }
        return mModuleHistoryManager;
    }

    public FavoriteManager getModuleFavoriteManager() {
        if (mModuleFavoriteManager == null) {
            mModuleFavoriteManager = new LocalFavoriteManager(this);
        }
        return mModuleFavoriteManager;
    }

    public float getRate(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
        float rate = (float) densityDpi / (float) 160;
        return rate;
    }

    /**
     * Return this application {@link HistoryManager}
     *
     * @return The application {@link HistoryManager}
     */
    public static interface OnLowMemoryListener {

        /**
         * Callback to be invoked when the system needs memory.
         */
        public void onLowMemoryReceived();
    }

    /**
     * Add a new listener to registered {@link OnLowMemoryListener}.
     *
     * @param listener The listener to unregister
     * @see OnLowMemoryListener
     */
    public void registerOnLowMemoryListener(OnLowMemoryListener listener) {
        if (listener != null) {
            mLowMemoryListeners.add(new WeakReference<OnLowMemoryListener>(listener));
        }
    }

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "GreenDroid thread #" + mCount.getAndIncrement());
        }
    };

    /**
     * Return an ExecutorService (global to the entire application) that may be
     * used by clients when running long tasks in the background.
     *
     * @return An ExecutorService to used when processing long running tasks
     */
    public ExecutorService getExecutor() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newFixedThreadPool(CORE_POOL_SIZE, sThreadFactory);
        }
        return mExecutorService;
    }

    /**
     * Return this application {@link ImageCache}.
     *
     * @return The application {@link ImageCache}
     */
    public ImageCache getImageCache() {
        if (mImageCache == null) {
            mImageCache = new ImageCache(this);
        }
        return mImageCache;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        int i = 0;
        while (i < mLowMemoryListeners.size()) {
            final OnLowMemoryListener listener = mLowMemoryListeners.get(i).get();
            if (listener == null) {
                mLowMemoryListeners.remove(i);
            } else {
                listener.onLowMemoryReceived();
                i++;
            }
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    private Runnable mUpLoadLogRunnable = new Runnable() {
        @Override
        public void run() {
            while (isFinish) {
                try {
                    Thread.sleep(1 * 30 * 1000);
                    ArrayList<String> list = MessageQueue.getQueueList();
                    int i;
                    JSONArray s = new JSONArray();
                    if (list.size() > 0) {
                        for (i = 0; i < list.size(); i++) {
                            JSONObject obj;
                            try {
                                obj = new JSONObject(list.get(i).toString());
                                s.put(obj);
                            } catch (JSONException e) {
                                ExceptionUtils.sendProgramError(e);
                                e.printStackTrace();
                            }

                        }
                        if (i == list.size()) {
                            MessageQueue.remove();
                            tv.ismar.app.core.client.NetworkUtils.LogSender(s.toString());
                        }
                    }
                } catch (InterruptedException | IndexOutOfBoundsException e) {
                    ExceptionUtils.sendProgramError(e);
                    e.printStackTrace();
                }
            }
        }

    };

    public boolean save() {
        return mEditor.commit();
    }

    @Override
    protected void attachBaseContext(Context base) {
        Log.i("LH/", "attachBaseContext:" + TrueTime.now().getTime());
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void initLogger() {
        Logger
                .init("VOD_APPLICATION")                 // default PRETTYLOGGER or use just init()
                .methodCount(10)                         // default 2
                .logLevel(LogLevel.FULL)                 // default LogLevel.FULL
                .methodOffset(1);                        // default 0
    }

    private void initKKMediaPlayer() {
        try {
            Class<?> clazz = Class.forName("com.konka.android.media.KKMediaPlayer");
            Method method = clazz.getMethod("setContext", Context.class);
            method.invoke(null, getApplicationContext());
        } catch (Exception e) {
            Log.e("KKMediaPlayer", "setContext method.invoke error!");
        }
    }

}
