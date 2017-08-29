package tv.ismar.homepage.control;

import android.content.Context;

import java.util.List;

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

    public static final int FETCH_GUIDE_BANNERS_FLAG = 0X01;//获取首页下所有列表标记

    public GuideControl(Context context) {
        super(context);
    }

    public GuideControl(Context context, ControlCallBack callBack){
        super(context, callBack);

    }

    /**
     *  获取首页下所有列表
     * @param platform tv or mobile
     */
    public void fetchBannerList(String platform){
        SkyService.ServiceManager.getCacheSkyService().getGuideBanners(platform)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GuideBanner[]>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(GuideBanner[] guideBanners) {
                        if(mCallBack!=null && guideBanners!=null && guideBanners.length>0){
                            mCallBack.callBack(FETCH_GUIDE_BANNERS_FLAG, guideBanners);
                        }
                    }
                });
    }

}
