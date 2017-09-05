package tv.ismar.homepage.control;

import android.content.Context;

import tv.ismar.app.BaseControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 横版双行业务类
 */

public class DoubleLdControl extends BaseControl {
    private FetchDataControl mFetchDataControl = null;

    public DoubleLdControl(Context context, ControlCallBack callBack) {
        super(context, callBack);
        mFetchDataControl = new FetchDataControl(context, callBack);
    }

    /*获取单个banner内容列表*/
    public void getBanners(String banner, int page){
        if(mFetchDataControl != null){
            mFetchDataControl.fetchBanners(banner, page);
        }
    }
}
