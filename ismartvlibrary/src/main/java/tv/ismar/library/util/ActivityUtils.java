package tv.ismar.library.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class ActivityUtils {

    private ActivityUtils() {
        throw new UnsupportedOperationException("Can not instantiate ...");
    }

    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment);
        transaction.commit();
    }

    public static boolean isActivityExists(Context context, String packageName, String className) {
        Intent intent = new Intent();
        intent.setClassName(packageName, className);
        return context.getPackageManager().resolveActivity(intent, 0) != null && intent.resolveActivity(context.getPackageManager()) != null && context.getPackageManager().queryIntentActivities(intent, 0).size() != 0;
    }

    public static void launchActivity(Context context, String packageName, String className) {
        launchActivity(context, packageName, className, (Bundle) null);
    }

    public static void launchActivity(Context context, String packageName, String className, Bundle bundle) {
        context.startActivity(IntentUtils.getComponentIntent(packageName, className, bundle));
    }

}
