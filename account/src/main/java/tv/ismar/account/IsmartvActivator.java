package tv.ismar.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.internal.platform.Platform;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tv.ismar.account.core.Md5;
import tv.ismar.account.core.http.HttpService;
import tv.ismar.account.core.rsa.RSACoder;
import tv.ismar.account.core.rsa.SkyAESTool2;
import tv.ismar.account.data.ResultEntity;
import tv.ismar.library.exception.ExceptionUtils;
import tv.ismar.library.network.UserAgentInterceptor;
import tv.ismar.library.util.C;
import tv.ismar.library.util.DeviceUtils;

/**
 * Created by huaijie on 5/17/16.
 */
public final class IsmartvActivator {
    static {
        System.loadLibrary("ismartv-lib");
    }

    private static final String TAG = "IsmartvActivator";
    private static final String SKY_HOST = "http://sky.tvxio.com";
    private static final String SKY_HOST_TEST = "http://skypeach.test.tvxio.com/";
    private static final String SIGN_FILE_NAME = "sign1";
    private static final int DEFAULT_CONNECT_TIMEOUT = 2;
    private static final int DEFAULT_READ_TIMEOUT = 2;

    private ResultEntity mResult;


    private String manufacture;
    private String kind;
    private String version;
    private String location;
    private String sn;
    private static Context mContext;
    private String fingerprint;
    private Retrofit SKY_Retrofit;
    private String deviceId;

    private static IsmartvActivator mInstance;

    public static IsmartvActivator getInstance() {
        if (mInstance == null) {
            synchronized (IsmartvActivator.class) {
                if (mInstance == null) {
                    mInstance = new IsmartvActivator();
                }
            }
        }
        return mInstance;
    }

    private SharedPreferences mSharedPreferences;

    public void setManufacture(String manufacture) {
        this.manufacture = manufacture;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public static void initialize(Context context) {
        mContext = context;
    }

    private IsmartvActivator() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                switch (key) {
                    case "ip":
                        C.ip = sharedPreferences.getString("ip", "");
                        break;
                    case "sn_token":
                        C.snToken = sharedPreferences.getString("sn_token", "");
                        break;
                }
            }
        });

        manufacture = Build.BRAND.replace(" ", "_");
        kind = IsmartvPlatform.getKind();
        version = String.valueOf(getAppVersionCode());
        deviceId = getDeviceId();
        sn = generateSn();
        fingerprint = Md5.md5(sn);

        IsmartvHttpLoggingInterceptor interceptor = new IsmartvHttpLoggingInterceptor();
        interceptor.setLevel(IsmartvHttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .addInterceptor(new UserAgentInterceptor())
//                .dns(new Dns() {
//                    @Override
//                    public List<InetAddress> lookup(String hostName) throws UnknownHostException {
//                        String ipAddress = getHostByName(hostName);
//                        Log.d(TAG, "ip: " + ipAddress);
//                        return Dns.SYSTEM.lookup(ipAddress);
//                    }
//                })
                .build();

        SKY_Retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(SKY_HOST_TEST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private String getDeviceId() {
        String deviceId = "test";
        try {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = tm.getDeviceId();
        } catch (Exception e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }
        return deviceId;
    }

    public synchronized ResultEntity execute() {
        ResultEntity resultEntity;
        if (isSignFileExists()) {
            resultEntity = active();
        } else {
            resultEntity = getLicence();
        }

        if (resultEntity == null) {
            Log.e(TAG, "激活失败!!!");
            initHttpCache();
            resultEntity = new ResultEntity();
        }
        saveAccountInfo(resultEntity);
        return resultEntity;
    }

    private int getAppVersionCode() {
        PackageManager packageManager = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String getAppVersionName() {
        String appVersionName = new String();
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            appVersionName = pi.versionName;
        } catch (Exception e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }
        return appVersionName;
    }

    private boolean isSignFileExists() {
        return mContext.getFileStreamPath(SIGN_FILE_NAME).exists();
    }


    private ResultEntity getLicence() {
        Logger.d("getLicence");
        try {
            Response<ResponseBody> response = SKY_Retrofit.create(HttpService.class).trustGetlicence(fingerprint, sn, manufacture, "1")
                    .execute();
            if (response.errorBody() == null) {
                String result = response.body().string();
                writeToSign(result.getBytes());
                return active();
            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            ExceptionUtils.sendProgramError(e);
            Log.e(TAG, "getLicence error!!!");
            return null;
        }
    }

    public static boolean isactive = false;

    public ResultEntity active() {
        Log.d(TAG, "active    " + "isactive: " + isactive);
        String sign = "ismartv=201415&kind=" + kind + "&sn=" + sn;
        String rsaEncryptResult = encryptWithPublic(sign);
        if (!isactive) {
            try {
                isactive = true;
                Response<ResultEntity> resultResponse = SKY_Retrofit.create(HttpService.class).
                        trustSecurityActive(sn, manufacture, kind, version, rsaEncryptResult,
                                fingerprint, "v4_0", getAndroidDevicesInfo(), DeviceUtils.getLocalwlanAddress(), DeviceUtils.getLocalHardwareAddress())
                        .execute();
                Log.i(TAG, DeviceUtils.getLocalwlanAddress() + "MAC: " + DeviceUtils.getLocalHardwareAddress());
                if (resultResponse.errorBody() == null) {
                    mResult = resultResponse.body();
                    saveAccountInfo(mResult);
                    reportIp(mResult.getSn_Token());
                    return mResult;
                } else if (resultResponse.code() == 424) {
                    isactive = false;
                    return getLicence();
                } else {
                    isactive = false;
                    return null;
                }
            } catch (IOException e) {
                isactive = false;
                e.printStackTrace();
                ExceptionUtils.sendProgramError(e);
                Log.e(TAG, "active error!!!");
                return null;
            }
        }
        if (mResult == null) {
            return null;
        } else {
            return mResult;
        }
    }


    private String getAndroidDevicesInfo() {
        try {
            JSONObject json = new JSONObject();
            String versionName = getAppVersionName();
            String serial = Build.SERIAL;
            String hh = Build.ID + "//" + Build.SERIAL;
            Md5.md5(Build.SERIAL + Build.ID);
            json.put("fingerprintE", Md5.md5(Build.SERIAL + Build.ID));
            json.put("fingerprintD", hh);
            json.put("versionName", versionName);
            json.put("serial", serial);
            json.put("deviceId", deviceId);
            return json.toString() + "///" + this.location;
        } catch (JSONException e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }
        return "";
    }

    private void writeToSign(byte[] bytes) {
        FileOutputStream fs;
        try {
            fs = mContext.openFileOutput(SIGN_FILE_NAME, Context.MODE_PRIVATE);
            fs.write(bytes);
            fs.flush();
            fs.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            ExceptionUtils.sendProgramError(e);
        }
    }

    public String decryptSign(String key, String ContentPath) {
        String decryptResult = new String();
        File file = new File(ContentPath);
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                int count = fileInputStream.available();
                byte[] bytes = new byte[count];
                fileInputStream.read(bytes);
                fileInputStream.close();
                decryptResult = SkyAESTool2.decrypt(key.substring(0, 16), Base64.decode(bytes, Base64.URL_SAFE));
            } catch (Exception e) {
                ExceptionUtils.sendProgramError(e);
                file.delete();
            }
        }
        return decryptResult;
    }


    public String encryptWithPublic(String string) {
        try {
            String signPath = mContext.getFileStreamPath(SIGN_FILE_NAME).getAbsolutePath();
            String result = decryptSign(sn, signPath);
            String publicKey = result.split("\\$\\$\\$")[1];

            String input = Md5.md5(string);
            Log.d(TAG, "md5: " + input);
            byte[] rsaResult = RSACoder.encryptByPublicKey(input.getBytes(), publicKey);
            return Base64.encodeToString(rsaResult, Base64.DEFAULT);
        } catch (Exception e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }
        return null;
    }


    public String getMacAddress() {
        return "mac_address";
    }

    public String getDeviceToken() {
        return SPUtils.getInstance().getString("device_token", "");
    }

    protected String getInternalDeviceToken() {
        String deviceToken = SPUtils.getInstance().getString("device_token", "");
        if (TextUtils.isEmpty(deviceToken)) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getDevice_token();
        } else {
            return deviceToken;
        }
    }

    public String getApiDomain() {
        return SPUtils.getInstance().getString("api_domain", "");
    }

    protected String getInternalApiDomain() {
        String apiDomain = SPUtils.getInstance().getString("api_domain", "");
        if (TextUtils.isEmpty(apiDomain) || apiDomain.equals("1.1.1.1")) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getDomain();
        } else {
            return apiDomain;
        }
    }

    public String getUpgradeDomain() {
        return SPUtils.getInstance().getString("upgrade_domain", "1.1.1.3");
    }

    protected String getInternalUpgradeDomain() {
        String upgradeDomain = mSharedPreferences.getString("upgrade_domain", "1.1.1.3");
        if (TextUtils.isEmpty(upgradeDomain) || upgradeDomain.equals("1.1.1.3")) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getUpgrade_domain();
        } else {
            return upgradeDomain;
        }
    }

    public String getAdDomain() {
        return SPUtils.getInstance().getString("ad_domain", "1.1.1.2");
    }

    protected String getInternalAdDomain() {
        // 广告测试地址
        String adDomain = SPUtils.getInstance().getString("ad_domain", "1.1.1.2");
        if (TextUtils.isEmpty(adDomain) || adDomain.equals("1.1.1.3")) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getAd_domain();
        } else {
            return adDomain;
        }
    }

    public String getLogDomain() {
        return SPUtils.getInstance().getString("log_domain", "1.1.1.4");
    }

    protected String getInternalLogDomain() {
        String logDomain = SPUtils.getInstance().getString("log_domain", "1.1.1.4");
        if (TextUtils.isEmpty(logDomain) || logDomain.equals("1.1.1.4")) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getLog_Domain();
        } else {
            return logDomain;
        }
    }

    public String getAuthToken() {
        return SPUtils.getInstance().getString("auth_token", "");
    }

    public int getH264PlayerType() {
//        return 0;
        return mSharedPreferences.getInt("h264_player", 0);// 0-smartplayer，1-系统mediaplayer,2-自有

    }

    public int getH265PlayerType() {
//        return 0;
        return mSharedPreferences.getInt("h265_player", 0);// 0-smartplayer，1-系统mediaplayer,2-自有

    }

    public int getLivePlayerType() {
//        return 1;
        return mSharedPreferences.getInt("live_player", 0);// 0-smartplayer，1-系统mediaplayer,2-自有

    }

    public String getZUserToken() {
        return SPUtils.getInstance().getString("zuser_token", "");
    }

    public String getZDeviceToken() {
        String zdeviceToken = mSharedPreferences.getString("zdevice_token", "");
        if (TextUtils.isEmpty(zdeviceToken)) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getDevice_token();
        } else {
            return zdeviceToken;
        }

    }

    protected String getInternalSnToken() {
        String snToken = mSharedPreferences.getString("sn_token", "");
        if (TextUtils.isEmpty(snToken)) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getSn_Token();
        } else {
            return snToken;
        }
    }

    public String getSnToken() {
        String snToken = mSharedPreferences.getString("sn_token", "");
        return snToken;
    }


    private void saveAccountInfo(ResultEntity resultEntity) {
        SPUtils spUtils = SPUtils.getInstance();
        spUtils.put("device_token", resultEntity.getDevice_token());
        spUtils.put("sn_token", resultEntity.getSn_Token());
        spUtils.put("api_domain", resultEntity.getDomain());
        spUtils.put("log_domain", resultEntity.getLog_Domain());
        spUtils.put("ad_domain", resultEntity.getAd_domain());
        spUtils.put("upgrade_domain", resultEntity.getUpgrade_domain());
        spUtils.put("zdevice_token", resultEntity.getZdevice_token());
        spUtils.put("smart_post_next_request_time", resultEntity.getSmart_post_next_request_time());
        C.SMART_POST_NEXT_REQUEST_TIME = resultEntity.getSmart_post_next_request_time();
        C.snToken = resultEntity.getSn_Token();
        C.isReportLog = resultEntity.getIs_report_log();
        C.report_log_size = resultEntity.getReport_log_size();
        C.report_log_time_interval = resultEntity.getReport_log_time_interval();
        spUtils.put("h264_player", resultEntity.getH264_player());
        spUtils.put("h265_player", resultEntity.getH265_player());
        spUtils.put("live_player", resultEntity.getLive_player());
        spUtils.put("is_report_log", resultEntity.getIs_report_log());
        spUtils.put("report_log_time_interval", resultEntity.getReport_log_time_interval());
        spUtils.put("report_log_size", resultEntity.getReport_log_size());

        // 获取老版本的
        // Daisy(auth_token, mobile_number, device_token),
        // account(sn_token, device_token, zdevice_token, zuser_token)
        SharedPreferences daisyPres = mContext.getSharedPreferences("Daisy", Context.MODE_PRIVATE);
        if (daisyPres != null) {
            String auth_token = daisyPres.getString("auth_token", null);
            String mobile_number = daisyPres.getString("mobile_number", null);
            if (!TextUtils.isEmpty(auth_token)) {
                setAuthToken(auth_token);
            }
            if (!TextUtils.isEmpty(mobile_number)) {
                setUsername(mobile_number);

                SharedPreferences.Editor daisyEditor = daisyPres.edit();
                daisyEditor.remove("auth_token");
                daisyEditor.remove("mobile_number");
                daisyEditor.apply();
            }
        }
        SharedPreferences accountPres = mContext.getSharedPreferences("account", Context.MODE_PRIVATE);
        if (accountPres != null) {
            String zuser_token = accountPres.getString("zuser_token", null);
            if (!TextUtils.isEmpty(zuser_token)) {
                setzUserToken(zuser_token);

                SharedPreferences.Editor accountEditor = accountPres.edit();
                accountEditor.remove("zuser_token");
                accountEditor.apply();
            }
        }
    }

    private void reportIp(String sn_token) {
        String url = "http://wx.api.tvxio.com/weixin4server/uploadclientip";
        try {
            Response<ResultEntity> resultResponse = SKY_Retrofit.create(HttpService.class).weixinIp(url, DeviceUtils.getLocalInetAddress().toString(), sn_token, Build.MODEL, DeviceUtils.getLocalMacAddress(mContext)).execute();
            Log.i("ismartvRIP", resultResponse.code() + "   code" + sn_token);
        } catch (IOException e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }
    }

    private void setAuthToken(String authToken) {
        mSharedPreferences.edit().putString("auth_token", authToken).commit();
    }


    private void setzUserToken(String authToken) {
        mSharedPreferences.edit().putString("zuser_token", authToken).commit();

    }

    private void setUsername(String username) {
        mSharedPreferences.edit().putString("username", username).commit();
    }


    public void saveUserInfo(String username, String authToken, String zUserhToken) {
        setUsername(username);
        setAuthToken(authToken);

        setzUserToken(zUserhToken);
    }

    public void setProvince(String name, String pinyin) {
        mSharedPreferences.edit().putString("province", name).commit();
        mSharedPreferences.edit().putString("province_py", pinyin).commit();

    }

    public HashMap<String, String> getProvince() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("province", mSharedPreferences.getString("province", ""));
        hashMap.put("province_py", mSharedPreferences.getString("province_py", ""));
        return hashMap;
    }

    public void setCity(String name, String geoId) {
        mSharedPreferences.edit().putString("city", name).commit();
        mSharedPreferences.edit().putString("geo_id", geoId).commit();
    }

    public HashMap<String, String> getCity() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("city", mSharedPreferences.getString("city", ""));
        hashMap.put("geo_id", mSharedPreferences.getString("geo_id", ""));
        return hashMap;
    }


    public void setIp(String ip) {
        mSharedPreferences.edit().putString("ip", ip).commit();
    }

    public String getIp() {
        return mSharedPreferences.getString("ip", "");
    }

    public void setIsp(String isp) {
        mSharedPreferences.edit().putString("isp", isp).commit();
    }

    public String getIsp() {
        return mSharedPreferences.getString("isp", "");
    }

    public void removeUserInfo() {
        mSharedPreferences.edit().putString("auth_token", "").commit();
        mSharedPreferences.edit().putString("zuser_token", "").commit();
        mSharedPreferences.edit().putString("username", "").commit();

        for (AccountChangeCallback callback : mAccountChangeCallbacks) {
            callback.onLogout();
        }
    }

    public String getUsername() {
        return mSharedPreferences.getString("username", "");
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public boolean isLogin() {
        return !TextUtils.isEmpty(getUsername());
    }

    private String generateSn() {
        String mysn;
        mysn = nativeMacAddress();
        Log.d(TAG, "stringFromJNI: " + mysn);
        if ("noaddress".equals(mysn)) {
            mysn = Md5.md5(getDeviceId() + Build.SERIAL);
        } else {
            mysn = Md5.md5(mysn);
        }
        Log.d(TAG, "sn: " + mysn);
        return mysn;
    }


    public interface AccountChangeCallback {
        void onLogout();
    }

    private List<AccountChangeCallback> mAccountChangeCallbacks = new ArrayList<>();

    public void addAccountChangeListener(AccountChangeCallback callback) {
        mAccountChangeCallbacks.add(callback);
    }

    public void removeAccountChangeListener(AccountChangeCallback callback) {
        mAccountChangeCallbacks.remove(callback);
    }

    private void initHttpCache() {
        //cache is empty file, just for test
        if (!new File(mContext.getCacheDir(), "cache").exists()) {
            int processorCount = Runtime.getRuntime().availableProcessors();
            ExecutorService executorService = Executors.newFixedThreadPool(100);
            for (final String file : getAllCacheFile()) {
//                executorService.execute(new Runnable() {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            RandomAccessFile writeFile = null;
                            InputStream assetsInputStream = mContext.getAssets().open(file);
                            byte[] buffer = new byte[512];
                            int readCount;

                            File cacheFile = new File(mContext.getCacheDir().getParentFile(), file);
                            if (!cacheFile.getParentFile().exists()) {
                                cacheFile.getParentFile().mkdirs();
                            }

                            writeFile = new RandomAccessFile(cacheFile, "rw");
                            while ((readCount = assetsInputStream.read(buffer)) != -1) {
                                writeFile.write(buffer, 0, readCount);
                            }
                            writeFile.close();
                            assetsInputStream.close();

                            String[] args2 = {"chmod", "604", cacheFile.getAbsolutePath()};
                            Runtime.getRuntime().exec(args2);
                        } catch (IOException e) {
                            ExceptionUtils.sendProgramError(e);
                            Log.e(TAG, "initialize http cache: " + e.getMessage());
                            e.printStackTrace();
                        }

                    }
                }.start();
            }
        }
    }

    private void chmodAllFiles(File file) throws IOException {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                chmodAllFiles(f);
            }
        } else if (file.isFile()) {
            String[] args2 = {"chmod", "604", file.getAbsolutePath()};
            Runtime.getRuntime().exec(args2);
        }
    }

    private String[] getAllCacheFile() {
        List<String> files = new ArrayList<>();
        files.add("cache/cache");
        try {
            for (String f1 : mContext.getAssets().list("cache/okhttp_cache")) {
                files.add("cache/okhttp_cache/" + f1);
            }
            for (String f2 : mContext.getAssets().list("cache/picasso_cache")) {
                files.add("cache/picasso_cache/" + f2);
            }

        } catch (IOException e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }
        return files.toArray(new String[files.size()]);
    }

    public native String nativeMacAddress();

    public static native String getHostByName(String hostName);
}
