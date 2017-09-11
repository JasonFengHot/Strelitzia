package tv.ismar.homepage.control;

import android.content.Context;

import tv.ismar.app.BaseControl;

/**
 * Created by huibin on 05/09/2017.
 */

public class Horizontal519Control extends BaseControl {
    public FetchDataControl mFetchDataControl = null;

    public Horizontal519Control(Context context, ControlCallBack callBack) {
        super(context, callBack);
        mFetchDataControl = new FetchDataControl(context, callBack);
    }

    public void getBanners(int banner, int page){
        if(mFetchDataControl != null){
            mFetchDataControl.fetchBanners(banner, page);
        }
    }
}
