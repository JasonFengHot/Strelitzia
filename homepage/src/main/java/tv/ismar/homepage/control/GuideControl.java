package tv.ismar.homepage.control;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseControl;
import tv.ismar.app.core.cache.CacheManager;
import tv.ismar.app.core.cache.DownloadClient;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.app.entity.banner.BannerCarousels;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.app.network.SkyService;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/24
 * @DESC: 首页业务类
 */

public class GuideControl extends BaseControl{

    public FetchDataControl mFetchDataControl = null;

    public GuideControl(Context context, ControlCallBack callBack){
        super(context, callBack);
        mFetchDataControl = new FetchDataControl(context, callBack);
    }

    /*获取单个banner内容列表*/
    public void getBanners(String banner, int page){
        if(mFetchDataControl != null){
            mFetchDataControl.fetchBanners(banner, page);
        }
    }

    /**
     * 获取导视视频
     * @param index 1-5(最少3个，最多5个)
     */
    public String getGuideVideoPath(int index){
        if(mFetchDataControl.mCarousels!=null && index<mFetchDataControl.mCarousels.size()){
            String fileName = "guide_"+index+".mp4";
            return CacheManager.getInstance() //如果本地有缓存取本地，否则网络获取
                    .doRequest(mFetchDataControl.mCarousels.get(index).video_url, fileName, DownloadClient.StoreType.External);
        }
        return null;
    }

}
