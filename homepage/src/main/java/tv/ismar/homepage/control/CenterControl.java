package tv.ismar.homepage.control;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import tv.ismar.app.BaseControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/5
 * @DESC: 居中业务类
 */

public class CenterControl extends BaseControl {

    public CenterControl(Context context, ControlCallBack callBack) {
        super(context, callBack);
    }

    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    public void scrollToCenter(RecyclerView recyclerView){

    }
}
