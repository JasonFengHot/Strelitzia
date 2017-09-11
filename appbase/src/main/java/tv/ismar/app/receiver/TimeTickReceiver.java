package tv.ismar.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/11
 * @DESC: 管理时间跳动广播
 */

public class TimeTickReceiver {

    private static List<BroadcastReceiver> mReceiver = new ArrayList<>();

    public static void register(Context context, BroadcastReceiver receiver){
        if(receiver != null){
            mReceiver.add(receiver);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            context.registerReceiver(receiver, filter);
        }
    }

    public static void unregisterAll(Context context){
        for(BroadcastReceiver receiver : mReceiver){
            if(receiver != null){
                context.unregisterReceiver(receiver);
            }
        }
    }

}
