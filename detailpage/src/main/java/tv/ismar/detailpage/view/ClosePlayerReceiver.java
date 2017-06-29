package tv.ismar.detailpage.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by admin on 2017/6/28.
 */

public class ClosePlayerReceiver extends BroadcastReceiver {

    private Activity mActivity;
    private ItemEntity mItemEntity;
    ClosePlayerReceiver(Activity activity,ItemEntity itemEntity){
        mActivity=activity;
        mItemEntity=itemEntity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getIntExtra("closeid",0)==mItemEntity.getPk()) {
            mActivity.finish();
        }
    }
}
