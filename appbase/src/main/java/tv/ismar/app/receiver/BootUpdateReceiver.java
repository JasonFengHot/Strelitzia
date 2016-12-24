package tv.ismar.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.Signature;
import android.os.Handler;
import android.util.Log;

import com.blankj.utilcode.utils.AppUtils;
import com.open.androidtvwidget.utils.NetWorkUtils;

import tv.ismar.app.update.UpdateService;

import static tv.ismar.app.update.UpdateService.INSTALL_SILENT;

/**
 * Created by huibin on 10/25/16.
 */

public class BootUpdateReceiver extends BroadcastReceiver {
    private static final String TAG = BootUpdateReceiver.class.getSimpleName();


    public static boolean checkUpdate = false;

    @Override
    public void onReceive(final Context context, Intent intent) {
        checkUpdate = true;
    }
}
