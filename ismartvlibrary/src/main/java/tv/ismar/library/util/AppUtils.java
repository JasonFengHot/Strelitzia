package tv.ismar.library.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.File;

/**
 * Created by LongHai on 17-4-11.
 */

public class AppUtils {

    private static final String TAG = "LH/AppUtils";

    public static int getVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void installApp(Activity activity, File file, int requestCode) {
        if (FileUtils.isFileExists(file)) {
            activity.startActivityForResult(IntentUtils.getInstallAppIntent(file), requestCode);
        }
    }

    public static boolean isSystemApp(Context context) {
        return isSystemApp(context, context.getPackageName());
    }

    public static boolean isSystemApp(Context context, String packageName) {
        if (StringUtils.isEmpty(packageName)) {
            return false;
        } else {
            try {
                PackageManager e = context.getPackageManager();
                ApplicationInfo ai = e.getApplicationInfo(packageName, 0);
                return ai != null && (ai.flags & 1) != 0;
            } catch (PackageManager.NameNotFoundException var4) {
                var4.printStackTrace();
                return false;
            }
        }
    }

}
