package tv.ismar.homepage.control;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
/*modify by dragontec for bug 4362 start*/
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*modify by dragontec for bug 4362 end*/

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.BaseControl;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.entity.ChannelEntity;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.app.entity.banner.BannerCarousels;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.BannerRecommend;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.ui.ToastTip;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 获取网络数据业务类
 */

public class FetchDataControl extends BaseControl{

    public HomeEntity mHomeEntity = new HomeEntity();//包含非完整的carousel和poster数据（每次请求接口的数据）
	/*add by dragontec for bug 4362 start*/
	private Map<String, HomeEntity> mHomeEntities = new HashMap<>();
	private final Object mDataLock = new Object();
	public Map<String, List<BannerCarousels>> mCarouselsMap = new HashMap<>();
	public Map<String, List<BannerPoster>> mPosterMap = new HashMap<>();
	public Map<String, List<BannerRecommend>> mRecommendsMap = new HashMap<>();
	/*add by dragontec for bug 4362 end*/
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

	/*add by dragontec for bug 4362 start*/
	public HomeEntity getHomeEntity(String banner) {
		synchronized (mDataLock) {
			return mHomeEntities.get(banner);
		}
	}
	/*add by dragontec for bug 4362 end*/

    /*获取首页下banner列表*/
    public void fetchHomeBanners(){
        try {
            fetchHomeBanners = SkyService.ServiceManager.getService().getGuideBanners()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(((BaseActivity)mContext).new BaseObserver<GuideBanner[]>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            forceFetchHomeBanners();
                        }

                        @Override
                        public void onNext(GuideBanner[] guideBanners) {
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

    public void forceFetchHomeBanners() {
        fetchHomeBanners = SkyService.ServiceManager.getForceCacheService().getGuideBanners()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GuideBanner[]>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(GuideBanner[] guideBanners) {
                        if (mCallBack != null && guideBanners != null && guideBanners.length > 0) {
                            mGuideBanners = guideBanners;
                            mCallBack.callBack(FETCH_HOME_BANNERS_FLAG, guideBanners);
                        }
                    }
                });
    }

    /*获取指定频道下的banner*/
    public void fetchChannelBanners(final String channel) {
        fetchChannelBanners = SkyService.ServiceManager.getService().getChannelBanners(channel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((BaseActivity)mContext).new BaseObserver<GuideBanner[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        forceFetchChannelBanners(channel);
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

    public void forceFetchChannelBanners(String channel) {
        fetchChannelBanners = SkyService.ServiceManager.getForceCacheService().getChannelBanners(channel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GuideBanner[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
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
                        forceFetchChannels();
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


    public void forceFetchChannels() {
        fetchChannels = SkyService.ServiceManager.getForceCacheService().apiTvChannels()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ChannelEntity[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
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

    /*modify by dragontec for bug 4362 start*/
    /**
     *  获取影视内容（多个banner）
     * @param banners {banner}数组
     * @param page
     */
    public  void fetchMBanners(final String[] banners, final int page){
		StringBuilder bannersStr = new StringBuilder();
		for (String banner :
				banners) {
			if (banner == null || banner.isEmpty()) {
				continue;
			}
			if (bannersStr.length() > 0) {
				bannersStr.append("|");
			}
			bannersStr.append(banner);
		}
		if (bannersStr.length() == 0) {
			return;
		}
		Log.d(TAG, "fetchMBanners("+ bannersStr.toString() + ", page = " + page + ")");
        fetchMBanners = SkyService.ServiceManager.getService().getMBanners(bannersStr.toString(), page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HomeEntity[]>() {
                    @Override
                    public void onCompleted() {
                    	Log.i(TAG, "fetchMBanners onCompleted");
					}

                    @Override
                    public void onError(Throwable e) {
                        forceFetchMBanners(banners, page);
                    }

                    @Override
                    public void onNext(HomeEntity[] homeEntities) {
						Log.i(TAG, "fetchMBanners onNext");
                    	synchronized (mDataLock) {
                    		if (homeEntities != null && homeEntities.length > 0) {
								int minSize = banners.length < homeEntities.length ? banners.length : homeEntities.length;
                    			for (int i = 0; i < minSize; i++) {
                    				mHomeEntities.put(banners[i], homeEntities[i]);
								}
							}
						}
						if (homeEntities != null) {
                    		for (int i = 0; i < homeEntities.length; i++) {
                    			HomeEntity homeEntity = homeEntities[i];
								if(homeEntity.carousels != null){
									List<BannerCarousels> list = mCarouselsMap.get(banners[i]);
									if (list == null) {
										list = new ArrayList<>();
									}
									list.addAll(homeEntity.carousels);
									if (!list.isEmpty() && list.size() > 5){
										list = list.subList(0, 5);
									}
									mCarouselsMap.put(banners[i], list);
								}
								if(homeEntity.posters != null){
									List<BannerPoster> list = mPosterMap.get(banners[i]);
									if (list == null) {
										list = new ArrayList<>();
									}
									list.addAll(homeEntity.posters);
									if (list.size() >= homeEntity.count - 2 && homeEntity.is_more) {
										BannerPoster morePoster = new BannerPoster();
										morePoster.poster_url = "更多";
										morePoster.vertical_url = "更多";
										morePoster.title = "";
										homeEntity.posters.add(morePoster);
										list.add(morePoster);
									}
									mPosterMap.put(banners[i], list);
								}
							}
						}
						if(mCallBack!=null){
                            mCallBack.callBack(FETCH_M_BANNERS_LIST_FLAG, (Object[]) banners);
                        }
                    }
                });
    }
    /*modify by dragontec for bug 4362 end*/

    /**
     *  获取影视内容（多个banner）
     * @param banners {banner}数组
     * @param page
     */
    public  void forceFetchMBanners(final String[] banners, int page){
        StringBuilder bannersStr = new StringBuilder();
        for (String banner :
                banners) {
            if (banner == null || banner.isEmpty()) {
                continue;
            }
            if (bannersStr.length() > 0) {
                bannersStr.append("|");
            }
            bannersStr.append(banner);
        }
        if (bannersStr.length() == 0) {
            return;
        }
        Log.d(TAG, "fetchMBanners("+ bannersStr.toString() + ", page = " + page + ")");
        fetchMBanners = SkyService.ServiceManager.getForceCacheService().getMBanners(bannersStr.toString(), page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HomeEntity[]>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "fetchMBanners onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.i(TAG, "fetchMBanners onError");
                        if (mCallBack != null) {
                            mCallBack.callBack(FETCH_DATA_FAIL_FLAG);
                        }
                    }

                    @Override
                    public void onNext(HomeEntity[] homeEntities) {
                        Log.i(TAG, "fetchMBanners onNext");
                        synchronized (mDataLock) {
                            if (homeEntities != null && homeEntities.length > 0) {
                                int minSize = banners.length < homeEntities.length ? banners.length : homeEntities.length;
                                for (int i = 0; i < minSize; i++) {
                                    mHomeEntities.put(banners[i], homeEntities[i]);
                                }
                            }
                        }
                        if (homeEntities != null) {
                            for (int i = 0; i < homeEntities.length; i++) {
                                HomeEntity homeEntity = homeEntities[i];
                                if(homeEntity.carousels != null){
                                    List<BannerCarousels> list = mCarouselsMap.get(banners[i]);
                                    if (list == null) {
                                        list = new ArrayList<>();
                                    }
                                    list.addAll(homeEntity.carousels);
                                    if (!list.isEmpty() && list.size() > 5){
                                        list = list.subList(0, 5);
                                    }
                                    mCarouselsMap.put(banners[i], list);
                                }
                                if(homeEntity.posters != null){
                                    List<BannerPoster> list = mPosterMap.get(banners[i]);
                                    if (list == null) {
                                        list = new ArrayList<>();
                                    }
                                    list.addAll(homeEntity.posters);
                                    if (list.size() >= homeEntity.count - 2 && homeEntity.is_more) {
                                        BannerPoster morePoster = new BannerPoster();
                                        morePoster.poster_url = "更多";
                                        morePoster.vertical_url = "更多";
                                        morePoster.title = "";
                                        homeEntity.posters.add(morePoster);
                                        list.add(morePoster);
                                    }
                                    mPosterMap.put(banners[i], list);
                                }
                            }
                        }
                        if(mCallBack!=null){
                            mCallBack.callBack(FETCH_M_BANNERS_LIST_FLAG, (Object[]) banners);
                        }
                    }
                });
    }

    /**
     * 获取首页下的推荐列表
     * @param url
     * @param isMore 是否加载更多
     */
    public void fetchHomeRecommend(String url, final boolean isMore){
        fetchHomeRecommend = SkyService.ServiceManager.getService().getHomeRecommend(IsmartvActivator.getInstance().getApiDomain()+url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<BannerRecommend>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<BannerRecommend> bannerRecommends) {
                        if(mCallBack != null){
                            if(!isMore){
                                mRecommends.clear();
                            }
                            mRecommends.addAll(bannerRecommends);
                            if (mCallBack != null) {
                                mCallBack.callBack(FETCH_HOME_RECOMMEND_LIST_FLAG, bannerRecommends);
                            }
                        }
                    }
                });
    }

    /*modify by dragontec for bug 4362 start*/
    /**
     * 获取banner
     * @param banner
     * @param page
     * @param loadMore 是否增量加载
     */
    public synchronized void fetchBanners(final String banner, final int page, final boolean loadMore){
        fetchBanners = SkyService.ServiceManager.getService().getBanners(banner, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HomeEntity>() {
                    @Override
                    public void onCompleted() {
	/* add by dragontec for bug 4264 start */
                    	Log.i("fetchBanners", "onCompleted");
	/* add by dragontec for bug 4264 end */
					}

                    @Override
                    public void onError(Throwable e) {
                        forceFetchBanners(banner, page, loadMore);
                    }

                    @Override
                    public void onNext(HomeEntity homeEntity) {
                            if(homeEntity != null){
                            	mHomeEntities.put(banner, homeEntity);
                            	List<BannerCarousels> carousels = mCarouselsMap.get(banner);
                                if(homeEntity.carousels != null){
                                    if(!loadMore){
										carousels.clear();
                                    }
									carousels.addAll(homeEntity.carousels);
                                    if (!carousels.isEmpty() && carousels.size() > 5){
										carousels = carousels.subList(0, 5);
                                    }
                                }
								List<BannerPoster> posters = mPosterMap.get(banner);
                                if(homeEntity.posters != null){
                                    if(!loadMore){
										posters.clear();
                                    }
									posters.addAll(homeEntity.posters);
                                    if(posters.size()>=mHomeEntity.count-2 && mHomeEntity.is_more){//最后一页更多按钮
                                        BannerPoster morePoster = new BannerPoster();
                                        morePoster.poster_url = "更多";
                                        morePoster.vertical_url = "更多";
                                        morePoster.title = "";
                                        mHomeEntities.get(banner).posters.add(morePoster);
										posters.add(morePoster);
                                    }
                                }
                            }
							if (mCallBack != null) {
								mCallBack.callBack(FETCH_BANNERS_LIST_FLAG, banner);
							}
                        }
                });
    }
    /*modify by dragontec for bug 4362 end*/

	/*modify by dragontec for bug 4362 start*/
    public synchronized void forceFetchBanners(final String banner, int page, final boolean loadMore){
        fetchBanners = SkyService.ServiceManager.getForceCacheService().getBanners(banner, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HomeEntity>() {
                    @Override
                    public void onCompleted() {
	/* add by dragontec for bug 4264 start */
                        Log.i("fetchBanners", "onCompleted");
	/* add by dragontec for bug 4264 end */
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.i("onError", "onError");
	/* add by dragontec for bug 4264 start */
                        if (mCallBack != null) {
                            mCallBack.callBack(FETCH_DATA_FAIL_FLAG);
                        }
	/* add by dragontec for bug 4264 end */
                    }

                    @Override
                    public void onNext(HomeEntity homeEntity) {
                        if(homeEntity != null){
							mHomeEntities.put(banner, homeEntity);
							List<BannerCarousels> carousels = mCarouselsMap.get(banner);
							if(homeEntity.carousels != null){
								if(!loadMore){
									carousels.clear();
								}
								carousels.addAll(homeEntity.carousels);
								if (!carousels.isEmpty() && carousels.size() > 5){
									carousels = carousels.subList(0, 5);
								}
							}
							List<BannerPoster> posters = mPosterMap.get(banner);
							if(homeEntity.posters != null){
								if(!loadMore){
									posters.clear();
								}
								posters.addAll(homeEntity.posters);
								if(posters.size()>=mHomeEntity.count-2 && mHomeEntity.is_more){//最后一页更多按钮
									BannerPoster morePoster = new BannerPoster();
									morePoster.poster_url = "更多";
									morePoster.vertical_url = "更多";
									morePoster.title = "";
									mHomeEntities.get(banner).posters.add(morePoster);
									posters.add(morePoster);
								}
							}
                        }
//                            if(mHomeEntity!=null&&"template_recommend".equals(mHomeEntity.template)){
//                                fetchHomeRecommend(mHomeEntity.url,mHomeEntity.is_more);
//                            }else {
                        if (mCallBack != null) {
                            mCallBack.callBack(FETCH_BANNERS_LIST_FLAG, banner);
                        }
//                            }
                    }
                });
    }
    /*modify by dragontec for bug 4362 end*/

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
    public void launcher_vod_click(String type, String pk, String title, String position) {
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put("type", type);
        tempMap.put("pk", pk);
        tempMap.put("title", title);
        tempMap.put("location", position);
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.LAUNCHER_VOD_CLICK, tempMap);

    }
}
