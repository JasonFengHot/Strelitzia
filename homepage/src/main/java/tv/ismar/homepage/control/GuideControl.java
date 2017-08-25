package tv.ismar.homepage.control;

import android.content.Context;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.app.network.SkyService;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/24
 * @DESC: 首页业务类
 */

public class GuideControl extends BaseControl{

    public static final int FETCH_GUIDE_BANNERS_FLAG = 0X01;

    public GuideControl(Context context) {
        super(context);
    }

    public GuideControl(Context context, ControlCallBack callBack){
        super(context, callBack);

    }

    /*获取banner列表*/
    public void fetchBannerList(){
        SkyService.ServiceManager.getCacheSkyService().getGuideBanners()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GuideBanner>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(GuideBanner guideBanner) {
                        if(mCallBack!=null && guideBanner!=null){
                            mCallBack.callBack(FETCH_GUIDE_BANNERS_FLAG, guideBanner);
                        }
                    }
                });
    }

}
