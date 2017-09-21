package tv.ismar.homepage.control;

import android.content.Context;
import android.util.Log;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.ChannelEntity;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.app.entity.banner.BannerCarousels;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.BannerRecommend;
import tv.ismar.app.entity.banner.ConnerEntity;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.app.network.SkyService;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 获取网络数据业务类
 */

public class FetchDataControl extends BaseControl{

    public static final int FETCH_CHANNEL_TAB_FLAG = 0X01;//获取频道列表
    public static final int FETCH_HOME_BANNERS_FLAG = 0X02;//获取首页下banners
    public static final int FETCH_CHANNEL_BANNERS_FLAG = 0X03;//获取指定频道下的banners
    public static final int FETCH_BANNERS_LIST_FLAG = 0X04;//获取指定banner下的海报列表
    public static final int FETCH_M_BANNERS_LIST_FLAG = 0X05;//获取影视内容多个banner
    public static final int FETCH_M_BANNERS_LIST_NEXTPAGE_FLAG = 0X06;//获取影视内容多个banner
    public static final int FETCH_HOME_RECOMMEND_LIST_FLAG = 0X07;//推荐列表
    public static final int FETCH_POSTER_CONNERS_FLAG = 0X08;//获取角标

    public HomeEntity mHomeEntity = new HomeEntity();//单个banner实体类，包含mCarousels和mPoster数据
    public List<BannerCarousels> mCarousels = new ArrayList<>();//导视数据
    public List<BannerPoster> mPoster = new ArrayList<>();//海报数据
    public List<BannerRecommend> mRecommends = new ArrayList<>();//首页推荐列表
    public List<ConnerEntity> mConners = new ArrayList<>();//角标
    public GuideBanner[] mGuideBanners = null;//首页banner列表
    public ChannelEntity[] mChannels = null;//频道列表

    public FetchDataControl(Context context, ControlCallBack callBack) {
        super(context, callBack);
    }

    /*获取首页下banner列表*/
    public void fetchHomeBanners(){
        try {
            SkyService.ServiceManager.getService().getGuideBanners()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<GuideBanner[]>() {
                        @Override
                        public void onCompleted() {}

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Log.i("onError", "onError");
                        }

                        @Override
                        public void onNext(GuideBanner[] guideBanners) {
                            //TODO 测试json假数据代码
//                            guideBanners = getChannels();
                            if(mCallBack!=null && guideBanners!=null && guideBanners.length>0){
                                mGuideBanners = guideBanners;
                                mCallBack.callBack(FETCH_HOME_BANNERS_FLAG, guideBanners);
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*获取角标接口*/
    public void fetchConners(){
        SkyService.ServiceManager.getService().getConner()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<ConnerEntity>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.i("onError", "onError");
                    }

                    @Override
                    public void onNext(List<ConnerEntity> conners) {
                        mConners.clear();
                        mConners.addAll(conners);
                        if (mCallBack != null && conners.size()>0) {
                            mCallBack.callBack(FETCH_POSTER_CONNERS_FLAG, conners);
                        }
                    }
                });
    }

    /*获取指定频道下的banner*/
    public void fetchChannelBanners(String channel){
        SkyService.ServiceManager.getService().getChannelBanners(channel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GuideBanner[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("onError", "onError");
                    }

                    @Override
                    public void onNext(GuideBanner[] guideBanners) {
                        if (mCallBack != null && guideBanners != null) {
                            mCallBack.callBack(FETCH_CHANNEL_BANNERS_FLAG, guideBanners);
                        }
                    }
                });
    }

    /*获取频道列表*/
    public void fetchChannels() {
        SkyService.ServiceManager.getService().apiTvChannels()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ChannelEntity[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ChannelEntity[] channelEntities) {
                        if (mCallBack != null && channelEntities != null) {
                            mChannels = channelEntities;
                            mCallBack.callBack(FETCH_CHANNEL_TAB_FLAG, channelEntities);
                        }
                    }
                });
    }

    /**
     *  获取影视内容（多个banner）
     * @param banner 组合方式{banner}|{banner}|{banner}
     * @param page
     */
    public synchronized void fetchMBanners(String banner, int page){
        SkyService.ServiceManager.getService().getMBanners(banner, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HomeEntity[]>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
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

    /**
     * 获取首页下的推荐列表
     * @param isMore 是否加载更多
     */
    public void fetchHomeRecommend(final boolean isMore){
        SkyService.ServiceManager.getService().getHomeRecommend()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<BannerRecommend>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(List<BannerRecommend> bannerRecommends) {
                        if(mCallBack != null){
                            if(!isMore){
                                mRecommends.clear();
                            }
                            mRecommends.addAll(bannerRecommends);
                            mCallBack.callBack(FETCH_HOME_RECOMMEND_LIST_FLAG, bannerRecommends);
                        }
                    }
                });
    }

    /**
     * 获取banner
     * @param banner
     * @param page
     * @param loadMore 是否增量加载
     */
    public synchronized void fetchBanners(int banner, int page, final boolean loadMore){
        SkyService.ServiceManager.getService().getBanners(banner, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HomeEntity>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.i("onError", "onError");
                        //TODO 测试
                        mCallBack.callBack(FETCH_BANNERS_LIST_FLAG, getEntity());
                    }

                    @Override
                    public void onNext(HomeEntity homeEntities) {
                            if(homeEntities != null){
                                mHomeEntity = homeEntities;
                                if(homeEntities.carousels != null){
                                    if(!loadMore){
                                        mCarousels.clear();
                                    }
                                    mCarousels.addAll(homeEntities.carousels);
                                }
                                if(homeEntities.posters != null){
                                    if(!loadMore){
                                        mPoster.clear();
                                    }
                                    mPoster.addAll(homeEntities.posters);
                                }
                            }
                            if(mCallBack != null){
                                mCallBack.callBack(FETCH_BANNERS_LIST_FLAG, homeEntities);
                            }
                        }
                });
    }

    public void fetchBanner(int bannerName, int pageNumber) {
        SkyService.ServiceManager.getService().apiTvBanner(bannerName, pageNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BannerEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(BannerEntity bannerEntity) {
                        if (bannerEntity != null){
                            mCallBack.callBack(FETCH_BANNERS_LIST_FLAG, bannerEntity);
                        }
                    }
                });
    }

    private GuideBanner[] getChannels(){
        String json = "[\n" +
                "    {\n" +
                "        \"banner\": \"chinesemoviebanner\",\n" +
                "        \"template\": \"template_guide\",\n" +
                "        \"banner_url\": \"api/tv/banner/chinesemoviebanner/1/\",\n" +
                "        \"title\": \"导视\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"banner\": \"overseasbanner\",\n" +
                "        \"template\": \"template_order\",\n" +
                "        \"banner_url\": \"api/tv/banner/overseasbanner/1/\",\n" +
                "        \"title\": \"预约\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"banner\": \"teleplaybanner\",\n" +
                "        \"template\": \"template_teleplay\",\n" +
                "        \"banner_url\": \"api/tv/banner/teleplaybanner/1/\",\n" +
                "        \"title\": \"电视剧\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"banner\": \"documentarybanner\",\n" +
                "        \"template\": \"template_double_md\",\n" +
                "        \"banner_url\": \"api/tv/banner/documentarybanner/1/\",\n" +
                "        \"title\": \"竖版双行\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"banner\": \"chinesemoviebanner\",\n" +
                "        \"template\": \"template_double_ld\",\n" +
                "        \"banner_url\": \"api/tv/banner/chinesemoviebanner/1/\",\n" +
                "        \"title\": \"横版双行\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"banner\": \"chinesemoviebanner\",\n" +
                "        \"template\": \"template_conlumn\",\n" +
                "        \"banner_url\": \"api/tv/banner/chinesemoviebanner/1/\",\n" +
                "        \"title\": \"栏目\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"banner\": \"chinesemoviebanner\",\n" +
                "        \"template\": \"template_center\",\n" +
                "        \"banner_url\": \"api/tv/banner/chinesemoviebanner/1/\",\n" +
                "        \"title\": \"居中模版\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"banner\": \"chinesemoviebanner\",\n" +
                "        \"template\": \"template_movie\",\n" +
                "        \"banner_url\": \"api/tv/banner/chinesemoviebanner/1/\",\n" +
                "        \"title\": \"电影\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"banner\": \"chinesemoviebanner\",\n" +
                "        \"template\": \"template_519\",\n" +
                "        \"banner_url\": \"api/tv/banner/chinesemoviebanner/1/\",\n" +
                "        \"title\": \"519横图模版\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"banner\": \"chinesemoviebanner\",\n" +
                "        \"template\": \"template_big_small_ld\",\n" +
                "        \"banner_url\": \"api/tv/banner/chinesemoviebanner/1/\",\n" +
                "        \"title\": \"大横小竖模版\"\n" +
                "    }\n" +
                "]";

        return new GsonBuilder().create().fromJson(json, GuideBanner[].class);
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
                "    \"posters\": [\n" +
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

        return new GsonBuilder().create().fromJson(json, HomeEntity.class);
    }
}
