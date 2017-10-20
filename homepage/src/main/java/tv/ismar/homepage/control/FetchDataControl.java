package tv.ismar.homepage.control;

import android.content.Context;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.ChannelEntity;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.app.entity.banner.BannerCarousels;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.BannerRecommend;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.app.network.SkyService;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 获取网络数据业务类
 */

public class FetchDataControl extends BaseControl{

    public HomeEntity mHomeEntity = new HomeEntity();//包含非完整的carousel和poster数据（每次请求接口的数据）
    public List<BannerCarousels> mCarousels = new ArrayList<>();//完整的carousel数据
    public List<BannerPoster> mPoster = new ArrayList<>();//完整的poster数据
    public List<BannerRecommend> mRecommends = new ArrayList<>();//首页推荐列表
    public GuideBanner[] mGuideBanners = null;//首页banner列表
    public ChannelEntity[] mChannels = null;//频道列表
    private Subscription fetchHomeBanners;
    private Subscription fetchChannelBanners;
    private Subscription fetchChannels;
    private Subscription fetchMBanners;
    private Subscription fetchHomeRecommend;
    private Subscription fetchBanners;

    public FetchDataControl(Context context, ControlCallBack callBack) {
        super(context, callBack);
    }

    /*获取首页下banner列表*/
    public void fetchHomeBanners(){
        Logger.t(TAG).d("FetchDataControl fetchHomeBanners");
        try {
            fetchHomeBanners = SkyService.ServiceManager.getService().getGuideBanners()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<GuideBanner[]>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Log.i("onError", "onError");
                        }

                        @Override
                        public void onNext(GuideBanner[] guideBanners) {
                            Logger.t(TAG).d("FetchDataControl fetchHomeBanners onNext");
                            if (mCallBack != null && guideBanners != null && guideBanners.length > 0) {
                                mGuideBanners = guideBanners;
                                mCallBack.callBack(FETCH_HOME_BANNERS_FLAG, guideBanners);
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*获取指定频道下的banner*/
    public void fetchChannelBanners(String channel){
        fetchChannelBanners = SkyService.ServiceManager.getService().getChannelBanners(channel)
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
                            mGuideBanners = guideBanners;
                            mCallBack.callBack(FETCH_CHANNEL_BANNERS_FLAG, guideBanners);
                        }
                    }
                });
    }

    /*获取频道列表*/
    public void fetchChannels() {
        fetchChannels = SkyService.ServiceManager.getService().apiTvChannels()
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
    public  void fetchMBanners(String banner, int page){
        fetchMBanners = SkyService.ServiceManager.getService().getMBanners(banner, page)
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
        fetchHomeRecommend = SkyService.ServiceManager.getService().getHomeRecommend()
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
    public synchronized void fetchBanners(String banner, int page, final boolean loadMore){
        fetchBanners = SkyService.ServiceManager.getService().getBanners(banner, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HomeEntity>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.i("onError", "onError");
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
                                    if(mPoster.size()>=mHomeEntity.count-2 && mHomeEntity.is_more){//最后一页更多按钮
                                        BannerPoster morePoster = new BannerPoster();
                                        morePoster.poster_url = "更多";
                                        morePoster.vertical_url = "更多";
                                        morePoster.title = "";
                                        mHomeEntity.posters.add(morePoster);
                                        mPoster.add(morePoster);
                                    }
                                }
                            }
                            if(mCallBack != null){
                                mCallBack.callBack(FETCH_BANNERS_LIST_FLAG, mHomeEntity);
                            }
                        }
                });
    }

    public void stop() {
        if (fetchHomeBanners != null && !fetchHomeBanners.isUnsubscribed()) {
            fetchHomeBanners.unsubscribe();
        }

        if (fetchChannelBanners != null && !fetchChannelBanners.isUnsubscribed()) {
            fetchChannelBanners.unsubscribe();
        }
        if (fetchChannels != null && !fetchChannels.isUnsubscribed()) {
            fetchChannels.unsubscribe();
        }
        if (fetchMBanners != null && !fetchMBanners.isUnsubscribed()) {
            fetchMBanners.unsubscribe();
        }

        if (fetchHomeRecommend != null && !fetchHomeRecommend.isUnsubscribed()) {
            fetchHomeRecommend.unsubscribe();
        }
        if (fetchBanners != null && !fetchBanners.isUnsubscribed()) {
            fetchBanners.unsubscribe();
        }
    }
}
