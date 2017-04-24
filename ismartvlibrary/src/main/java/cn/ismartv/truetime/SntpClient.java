//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.ismartv.truetime;

import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.OkHttpClient.Builder;
import tv.ismar.library.network.HttpLoggingInterceptor;

public class SntpClient {
    private static final String TAG = SntpClient.class.getSimpleName();
    private static final int NTP_PORT = 123;
    private static final int NTP_MODE = 3;
    private static final int NTP_VERSION = 3;
    private static final int NTP_PACKET_SIZE = 48;
    private static final int INDEX_VERSION = 0;
    private static final int INDEX_ROOT_DELAY = 4;
    private static final int INDEX_ROOT_DISPERSION = 8;
    private static final int INDEX_ORIGINATE_TIME = 24;
    private static final int INDEX_RECEIVE_TIME = 32;
    private static final int INDEX_TRANSMIT_TIME = 40;
    private static final long OFFSET_1900_TO_1970 = 2208988800L;
    private long _cachedDeviceUptime;
    private long _cachedSntpTime;
    private boolean _sntpInitialized = false;

    public SntpClient() {
    }

    void requestTime(String ntpHost, int timeoutInMillis) throws IOException {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = (new Builder()).connectTimeout((long) timeoutInMillis, TimeUnit.MILLISECONDS).addInterceptor(interceptor).build();
        Request request = (new okhttp3.Request.Builder()).url(ntpHost).build();
        long responseTicks = SystemClock.elapsedRealtime();
        Response response = client.newCall(request).execute();
        long sentTime = response.sentRequestAtMillis();
        long receivedTime = response.receivedResponseAtMillis();
        long responseTime = (new BigDecimal(response.body().string())).longValue();
        long clockOffset = receivedTime - sentTime;
        this._sntpInitialized = true;
        Log.i(TAG, "---- SNTP successful response from " + ntpHost);
        this._cachedSntpTime = responseTime + clockOffset;
        this._cachedDeviceUptime = responseTicks;
    }

    long getCachedSntpTime() {
        return this._cachedSntpTime;
    }

    long getCachedDeviceUptime() {
        return this._cachedDeviceUptime;
    }

    boolean wasInitialized() {
        return this._sntpInitialized;
    }

    private void _writeVersion(byte[] buffer) {
        buffer[0] = 27;
    }

    private void _writeTimeStamp(byte[] buffer, int offset, long time) {
        long seconds = time / 1000L;
        long milliseconds = time - seconds * 1000L;
        seconds += 2208988800L;
        buffer[offset++] = (byte) ((int) (seconds >> 24));
        buffer[offset++] = (byte) ((int) (seconds >> 16));
        buffer[offset++] = (byte) ((int) (seconds >> 8));
        buffer[offset++] = (byte) ((int) (seconds >> 0));
        long fraction = milliseconds * 4294967296L / 1000L;
        buffer[offset++] = (byte) ((int) (fraction >> 24));
        buffer[offset++] = (byte) ((int) (fraction >> 16));
        buffer[offset++] = (byte) ((int) (fraction >> 8));
        buffer[offset++] = (byte) ((int) (Math.random() * 255.0D));
    }

    private long _readTimeStamp(byte[] buffer, int offset) {
        long seconds = this._read(buffer, offset);
        long fraction = this._read(buffer, offset + 4);
        return (seconds - 2208988800L) * 1000L + fraction * 1000L / 4294967296L;
    }

    private long _read(byte[] buffer, int offset) {
        byte b0 = buffer[offset];
        byte b1 = buffer[offset + 1];
        byte b2 = buffer[offset + 2];
        byte b3 = buffer[offset + 3];
        return ((long) this.ui(b0) << 24) + ((long) this.ui(b1) << 16) + ((long) this.ui(b2) << 8) + (long) this.ui(b3);
    }

    private int ui(byte b) {
        return (b & 128) == 128 ? (b & 127) + 128 : b;
    }
}
