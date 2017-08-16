package tv.ismar.account;

import android.content.Context;
import android.os.Build;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by huibin on 16/08/2017.
 */

public class IsmartvPlatform {
    private static String platform;

    public static void initPlatform(Context context) {
        Properties sysProperties = new Properties();
        try {
            InputStream is = context.getAssets().open("configure/setup.properties");
            sysProperties.load(is);
            platform = sysProperties.getProperty("platform");
        } catch (IOException e) {
            platform = "sharp";
        }
    }

    public static String getKind() {
        switch (platform) {
            case "changhong":
                return platform;
            default:
                return Build.PRODUCT.replaceAll(" ", "_").toLowerCase();
        }
    }

    public static boolean isForbiddenLauncher() {
        switch (platform) {
            case "sanzhou":
                return true;
            default:
                return false;
        }
    }
}
