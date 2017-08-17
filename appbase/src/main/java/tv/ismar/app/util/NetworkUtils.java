package tv.ismar.app.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import okhttp3.HttpUrl;
import tv.ismar.account.IsmartvActivator;

public class NetworkUtils {
    private static final int TIMEOUT = 1000;

    /**
     * 是否已有网络连接
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo ethNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        boolean isConnected = (mobNetInfo != null && mobNetInfo.isConnected())
                || (wifiNetInfo != null && wifiNetInfo.isConnected())
                || (ethNetInfo != null && ethNetInfo.isConnected());

        if (isConnected) {
            return true;
        }
        return false;
    }


    public static boolean isReachability(String url) {
            String host = HttpUrl.parse(url).host();
            String ip = IsmartvActivator.getHostByName(host);
            return !ip.equals("0.0.0.0");
    }

    /**
     * 是否wifi连接
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (info != null && info.getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否3G连接
     */
    public static boolean isMobile(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (info != null && info.getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else {
            return false;
        }
    }
}
