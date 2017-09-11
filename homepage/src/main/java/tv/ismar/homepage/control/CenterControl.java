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
    public FetchDataControl mFetchDataControl = null;

    public CenterControl(Context context, ControlCallBack callBack) {
        super(context, callBack);
        mFetchDataControl = new FetchDataControl(context, callBack);
    }

    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    public void scrollToCenter(RecyclerView recyclerView){

    }

    /*获取单个banner内容列表*/
    public void getBanners(int banner, int page){
        if(mFetchDataControl != null){
            mFetchDataControl.fetchBanners(banner, page);
        }
    }
}
