//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.ismartv.truetime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.ismartv.truetime.TrueLog;
import cn.ismartv.truetime.TrueTime;

public class BootCompletedBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = BootCompletedBroadcastReceiver.class.getSimpleName();

    public BootCompletedBroadcastReceiver() {
    }

    public void onReceive(Context context, Intent intent) {
        TrueLog.i(TAG, "---- clearing TrueTime disk cache as we\'ve detected a boot");
        TrueTime.clearCachedInfo(context);
    }
}
