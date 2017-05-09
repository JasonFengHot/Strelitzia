//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.ismartv.truetime;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.util.Date;

public class TrueTime {
    private static final String TAG = TrueTime.class.getSimpleName();
    private static final TrueTime INSTANCE = new TrueTime();
    private static final SntpClient SNTP_CLIENT = new SntpClient();
    private static final DiskCacheClient DISK_CACHE_CLIENT = new DiskCacheClient();
    private static int _udpSocketTimeoutInMillis = 30000;
    private String _ntpHost = "1.us.pool.ntp.org";

    public TrueTime() {
    }

    public static Date now() {
        if (!isInitialized()) {
            Log.e(TAG, "You need to call init() on TrueTime at least once.");
            return new Date();
        } else {
            long cachedSntpTime = _getCachedSntpTime();
            long cachedDeviceUptime = _getCachedDeviceUptime();
            long deviceUptime = SystemClock.elapsedRealtime();
            long now = cachedSntpTime + (deviceUptime - cachedDeviceUptime);
            return new Date(now);
        }
    }

    public static boolean isInitialized() {
        return SNTP_CLIENT.wasInitialized() || DISK_CACHE_CLIENT.isTrueTimeCachedFromAPreviousBoot();
    }

    public static TrueTime build() {
        return INSTANCE;
    }

    public static void clearCachedInfo(Context context) {
        DISK_CACHE_CLIENT.clearCachedInfo(context);
    }

    public void initialize() throws IOException {
        this.initialize(this._ntpHost);
        cacheTrueTimeInfo();
    }

    public synchronized TrueTime withSharedPreferences(Context context) {
        DISK_CACHE_CLIENT.enableDiskCaching(context);
        return INSTANCE;
    }

    public synchronized TrueTime withConnectionTimeout(int timeoutInMillis) {
        _udpSocketTimeoutInMillis = timeoutInMillis;
        return INSTANCE;
    }

    public synchronized TrueTime withNtpHost(String ntpHost) {
        this._ntpHost = ntpHost;
        return INSTANCE;
    }

    public synchronized TrueTime withLoggingEnabled(boolean isLoggingEnabled) {
        TrueLog.setLoggingEnabled(isLoggingEnabled);
        return INSTANCE;
    }

    protected void initialize(String ntpHost) throws IOException {
        SNTP_CLIENT.requestTime(ntpHost, _udpSocketTimeoutInMillis);
    }

    protected static synchronized void cacheTrueTimeInfo() {
        if (!SNTP_CLIENT.wasInitialized()) {
            TrueLog.i(TAG, "---- SNTP client not available. not caching TrueTime info in disk");
        } else {
            DISK_CACHE_CLIENT.cacheTrueTimeInfo(SNTP_CLIENT);
        }
    }

    private static long _getCachedDeviceUptime() {
        long cachedDeviceUptime = SNTP_CLIENT.wasInitialized() ? SNTP_CLIENT.getCachedDeviceUptime() : DISK_CACHE_CLIENT.getCachedDeviceUptime();
        if (cachedDeviceUptime == 0L) {
            throw new RuntimeException("expected SNTP time from last boot to be cached. couldn\'t find it.");
        } else {
            return cachedDeviceUptime;
        }
    }

    private static long _getCachedSntpTime() {
        long cachedSntpTime = SNTP_CLIENT.wasInitialized() ? SNTP_CLIENT.getCachedSntpTime() : DISK_CACHE_CLIENT.getCachedSntpTime();
        if (cachedSntpTime == 0L) {
            throw new RuntimeException("expected SNTP time from last boot to be cached. couldn\'t find it.");
        } else {
            return cachedSntpTime;
        }
    }
}
