package tv.ismar.app;

import android.app.Activity;
import android.content.Context;

import rx.Observer;
import tv.ismar.app.network.SkyService;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/24
 * @DESC: 业务基类
 */

public class BaseControl {
    private String TAG = this.getClass().getSimpleName();

    public Context mContext;
    public Activity mActivity;
    public ControlCallBack mCallBack;

    public BaseControl(Context context){
        this.mContext = context;
    }

    public BaseControl(Activity activity){
        this.mActivity = activity;
    }

    public BaseControl(Context context, ControlCallBack callBack){
        this(context);
        setCallBack(callBack);
    }

    public BaseControl(Activity activity, ControlCallBack callBack){
        this(activity);
        setCallBack(callBack);
    }

    public void setCallBack(ControlCallBack callBack){
        this.mCallBack = callBack;
    }

    /*回调控制视图*/
    public interface ControlCallBack {
        void callBack(int flags, Object... args);
    }


}
