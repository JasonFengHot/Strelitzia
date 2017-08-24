package tv.ismar.app;

import android.app.Activity;
import android.content.Context;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/24
 * @DESC: 业务基类
 */

public class BaseControl {
    private String TAG = this.getClass().getSimpleName();

    private Context mContext;

    private Activity mActivity;

    public BaseControl(Context context){
        this.mContext = context;
    }

    public BaseControl(Activity activity){
        this.mActivity = activity;
    }

    interface ControlCallBack {
        void callBack();
    }
}
