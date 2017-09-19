package tv.ismar.homepage.control;

import android.content.Context;

import java.util.List;

import tv.ismar.app.BaseControl;
import tv.ismar.app.core.cache.CacheManager;
import tv.ismar.app.core.cache.DownloadClient;
import tv.ismar.app.entity.banner.BannerCarousels;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/24
 * @DESC: 首页业务类
 */

public class GuideControl extends BaseControl{

    public GuideControl(Context context){
        super(context);
    }

    /**
     * 获取导视视频
     * @param index 1-5(最少3个，最多5个)
     */
    public String getGuideVideoPath(int index, List<BannerCarousels> carousels){
        if(carousels!=null && index<carousels.size()){
            String fileName = "guide_"+index+".mp4";
            return CacheManager.getInstance() //如果本地有缓存取本地，否则网络获取
                    .doRequest(carousels.get(index).video_url, fileName, DownloadClient.StoreType.External);
        }
        return null;
    }
}
