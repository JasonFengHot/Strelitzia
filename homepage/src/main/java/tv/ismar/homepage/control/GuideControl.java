package tv.ismar.homepage.control;

import android.content.Context;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.app.network.SkyService;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/24
 * @DESC: 首页业务类
 */

public class GuideControl extends BaseControl{

    public static final int FETCH_HOME_BANNERS_FLAG = 0X01;//获取首页下所有列表标记
    public static final int FETCH_BANNERS_LIST_FLAG = 0X02;//获取影视内容banner列表

    public GuideControl(Context context) {
        super(context);
    }

    public GuideControl(Context context, ControlCallBack callBack){
        super(context, callBack);

    }

    public void fetchBanners(String banner, int page){
        SkyService.ServiceManager.getCacheSkyService().getBanners(banner, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HomeEntity>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(HomeEntity homeEntities) {
                        if(mCallBack!=null && homeEntities!=null){
                            mCallBack.callBack(FETCH_BANNERS_LIST_FLAG, homeEntities);
                        }
                    }
                });
    }

    /**
     *  获取首页下所有列表
     */
    public void fetchBannerList(){
        SkyService.ServiceManager.getCacheSkyService().getGuideBanners()
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
                            mCallBack.callBack(FETCH_HOME_BANNERS_FLAG, guideBanners);
                        }
                    }
                });
    }

}
