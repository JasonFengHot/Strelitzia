package tv.ismar.homepage.control;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.app.entity.banner.BannerCarousels;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.app.network.SkyService;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 获取网络数据业务类
 */

public class FetchDataControl extends BaseControl{

    public static final int FETCH_HOME_BANNERS_FLAG = 0X01;//获取首页下所有列表标记
    public static final int FETCH_BANNERS_LIST_FLAG = 0X02;//获取影视内容banner列表
    public static final int FETCH_M_BANNERS_LIST_FLAG = 0X03;//获取影视内容多个banner

    public List<BannerCarousels> mCarousels = new ArrayList<>();//导视数据
    public List<BannerPoster> mPoster = new ArrayList<>();//海报数据
    public GuideBanner[] mBanners = null;//首页banner列表

    public FetchDataControl(Context context, ControlCallBack callBack) {
        super(context, callBack);
    }

    /**
     *  获取首页下所有列表
     */
    public void fetchBannerList(){
        try {
            SkyService.ServiceManager.getLocalTestService().getGuideBanners()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<GuideBanner[]>() {
                        @Override
                        public void onCompleted() {}

                        @Override
                        public void onError(Throwable e) {
                            Log.i("onError", "onError");
                        }

                        @Override
                        public void onNext(GuideBanner[] guideBanners) {
                            if(mCallBack!=null && guideBanners!=null && guideBanners.length>0){
                                mBanners = guideBanners;
                                mCallBack.callBack(FETCH_HOME_BANNERS_FLAG, guideBanners);
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  获取影视内容（多个banner）
     * @param banner 组合方式{banner}|{banner}|{banner}
     * @param page
     */
    public synchronized void fetchMBanners(String banner, int page){
        SkyService.ServiceManager.getLocalTestService().getMBanners(banner, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HomeEntity[]>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.i("onError", "onError");
                    }

                    @Override
                    public void onNext(HomeEntity[] homeEntities) {
                        if(mCallBack!=null && homeEntities!=null){
                            mCallBack.callBack(FETCH_M_BANNERS_LIST_FLAG, homeEntities);
                        }
                    }
                });
    }

    public synchronized void fetchBanners(String banner, int page){
        SkyService.ServiceManager.getLocalTestService().getBanners(banner, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HomeEntity>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.i("onError", "onError");
                        //TODO 测试
                        mCallBack.callBack(FETCH_BANNERS_LIST_FLAG, getEntity());
                    }

                    @Override
                    public void onNext(HomeEntity homeEntities) {
                        if(mCallBack!=null && homeEntities!=null){
                            if(homeEntities != null){
                                mCarousels.clear();
                                mCarousels.addAll(homeEntities.carousels);
                                mPoster.clear();
                                mPoster.addAll(homeEntities.poster);
                            }
                            mCallBack.callBack(FETCH_BANNERS_LIST_FLAG, homeEntities);
                        }
                    }
                });
    }

    private HomeEntity getEntity(){
        String json = "{\n" +
                "    \"count\": 7,\n" +
                "    \"carousels\": [\n" +
                "        {\n" +
                "            \"video_image\": \"http://res.tvxio.com/media/upload/20170421/upload/20160420/upload/20160711/upload/20160420/upload/20140922/shenghuaweijizhongzhang0505.jpg\",\n" +
                "            \"introduction\": \"生化危机：终章\",\n" +
                "            \"content_model\": \"movie\",\n" +
                "            \"video_url\": \"http://vdata.tvxio.com/topvideo/8301d3a14849a731edcea1a28e627d8d.mp4?sn=oncall\",\n" +
                "            \"title\": \"生化危机：终章\",\n" +
                "            \"pause_time\": 5,\n" +
                "            \"rating_average\": 8.7,\n" +
                "            \"content_url\": \"/api/item/1239319/\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"video_image\": \"http://res.tvxio.com/media/upload/20170421/upload/20160420/upload/20140922/cikexintiao0505.jpg\",\n" +
                "            \"introduction\": \"刺客信条\",\n" +
                "            \"content_model\": \"movie\",\n" +
                "            \"video_url\": \"http://vdata.tvxio.com/topvideo/0afb93dfd08d4b05c46625cc3412f375.mp4?sn=oncall\",\n" +
                "            \"title\": \"刺客信条\",\n" +
                "            \"pause_time\": 5,\n" +
                "            \"rating_average\": 8,\n" +
                "            \"content_url\": \"/api/item/1247745/\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"num_pages\": 1,\n" +
                "    \"poster\": [\n" +
                "        {\n" +
                "            \"title\": \"奇异博士\",\n" +
                "            \"introduction\": \"奇异博士\",\n" +
                "            \"rating_average\": 8.9,\n" +
                "            \"content_url\": \"/api/item/728354/\",\n" +
                "            \"content_model\": \"movie\",\n" +
                "            \"poster_url\": \"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"title\": \"奇异博士\",\n" +
                "            \"introduction\": \"奇异博士\",\n" +
                "            \"rating_average\": 8.9,\n" +
                "            \"content_url\": \"/api/item/728354/\",\n" +
                "            \"content_model\": \"movie\",\n" +
                "            \"poster_url\": \"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"title\": \"奇异博士\",\n" +
                "            \"introduction\": \"奇异博士\",\n" +
                "            \"rating_average\": 8.9,\n" +
                "            \"content_url\": \"/api/item/728354/\",\n" +
                "            \"content_model\": \"movie\",\n" +
                "            \"poster_url\": \"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"title\": \"奇异博士\",\n" +
                "            \"introduction\": \"奇异博士\",\n" +
                "            \"rating_average\": 8.9,\n" +
                "            \"content_url\": \"/api/item/728354/\",\n" +
                "            \"content_model\": \"movie\",\n" +
                "            \"poster_url\": \"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"title\": \"奇异博士\",\n" +
                "            \"introduction\": \"奇异博士\",\n" +
                "            \"rating_average\": 8.9,\n" +
                "            \"content_url\": \"/api/item/728354/\",\n" +
                "            \"content_model\": \"movie\",\n" +
                "            \"poster_url\": \"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"template\": \"template_guide\",\n" +
                "    \"pk\": 1,\n" +
                "    \"banner\": \"chinesemoviebanner\"\n" +
                "}";

        Gson gson = new Gson();
        return new GsonBuilder().create().fromJson(json, HomeEntity.class);
    }
}
