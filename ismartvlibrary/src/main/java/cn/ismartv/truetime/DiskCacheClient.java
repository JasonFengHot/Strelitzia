//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.ismartv.truetime;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

class DiskCacheClient {
    private static final String KEY_CACHED_SHARED_PREFS = "cn.ismartv.truetime.shared_preferences";
    private static final String KEY_CACHED_BOOT_TIME = "cn.ismartv.truetime.cached_boot_time";
    private static final String KEY_CACHED_DEVICE_UPTIME = "cn.ismartv.truetime.cached_device_uptime";
    private static final String KEY_CACHED_SNTP_TIME = "cn.ismartv.truetime.cached_sntp_time";
    private static final String TAG = DiskCacheClient.class.getSimpleName();
    private SharedPreferences _sharedPreferences = null;

    DiskCacheClient() {
    }

    void enableDiskCaching(Context context) {
        this._sharedPreferences = context.getSharedPreferences("cn.ismartv.truetime.shared_preferences", 0);
    }

    void clearCachedInfo(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("cn.ismartv.truetime.shared_preferences", 0);
        if (sharedPreferences != null) {
            sharedPreferences.edit().clear().apply();
        }
    }

    void cacheTrueTimeInfo(SntpClient sntpClient) {
        if (!this.sharedPreferencesUnavailable()) {
            long cachedSntpTime = sntpClient.getCachedSntpTime();
            long cachedDeviceUptime = sntpClient.getCachedDeviceUptime();
            long bootTime = cachedSntpTime - cachedDeviceUptime;
            TrueLog.d(TAG, String.format("Caching true time info to disk sntp [%s] device [%s] boot [%s]", new Object[]{Long.valueOf(cachedSntpTime), Long.valueOf(cachedDeviceUptime), Long.valueOf(bootTime)}));
            this._sharedPreferences.edit().putLong("cn.ismartv.truetime.cached_boot_time", bootTime).apply();
            this._sharedPreferences.edit().putLong("cn.ismartv.truetime.cached_device_uptime", cachedDeviceUptime).apply();
            this._sharedPreferences.edit().putLong("cn.ismartv.truetime.cached_sntp_time", cachedSntpTime).apply();
        }
    }

    boolean isTrueTimeCachedFromAPreviousBoot() {
        if (this.sharedPreferencesUnavailable()) {
            return false;
        } else {
            long cachedBootTime = this._sharedPreferences.getLong("cn.ismartv.truetime.cached_boot_time", 0L);
            if (cachedBootTime == 0L) {
                return false;
            } else {
                boolean bootTimeChanged = SystemClock.elapsedRealtime() < this.getCachedDeviceUptime();
                TrueLog.i(TAG, "---- boot time changed " + bootTimeChanged);
                return !bootTimeChanged;
            }
        }
    }

    long getCachedDeviceUptime() {
        return this.sharedPreferencesUnavailable() ? 0L : this._sharedPreferences.getLong("cn.ismartv.truetime.cached_device_uptime", 0L);
    }

    long getCachedSntpTime() {
        return this.sharedPreferencesUnavailable() ? 0L : this._sharedPreferences.getLong("cn.ismartv.truetime.cached_sntp_time", 0L);
    }

    private boolean sharedPreferencesUnavailable() {
        if (this._sharedPreferences == null) {
            TrueLog.w(TAG, "Cannot use disk caching strategy for TrueTime. SharedPreferences unavailable");
            return true;
        } else {
            return false;
        }
    }
}
