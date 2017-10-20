package tv.ismar.homepage.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.blankj.utilcode.util.StringUtils;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.ismartv.truetime.TrueTime;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.ActiveService;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.account.IsmartvPlatform;
import tv.ismar.account.statistics.LogQueue;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.VodApplication;
import tv.ismar.app.ad.AdsUpdateService;
import tv.ismar.app.ad.AdvertiseManager;
import tv.ismar.app.ad.Advertisement;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.InitializeProcess;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.VodUserAgent;
import tv.ismar.app.core.client.MessageQueue;
import tv.ismar.app.core.preferences.AccountSharedPrefs;
import tv.ismar.app.db.AdvertiseTable;
import tv.ismar.app.entity.ChannelEntity;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.service.TrueTimeService;
import tv.ismar.app.ui.HeadFragment;
import tv.ismar.app.ui.ToastTip;
import tv.ismar.app.update.UpdateService;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.app.util.NetworkUtils;
import tv.ismar.app.util.SPUtils;
import tv.ismar.app.util.SystemFileUtil;
import tv.ismar.app.widget.ExpireAccessTokenPop;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.banner.adapter.BannerHorizontal519Adapter;
import tv.ismar.homepage.banner.adapter.BannerMovieAdapter;
import tv.ismar.homepage.banner.adapter.BannerMovieMixAdapter;
import tv.ismar.homepage.banner.adapter.BannerSubscribeAdapter;
import tv.ismar.homepage.fragment.ChannelBaseFragment;
import tv.ismar.homepage.fragment.ChildFragment;
import tv.ismar.homepage.fragment.EntertainmentFragment;
import tv.ismar.homepage.fragment.FilmFragment;
import tv.ismar.homepage.fragment.GuideFragment;
import tv.ismar.homepage.fragment.SportFragment;
import tv.ismar.homepage.fragment.UpdateSlienceLoading;
import tv.ismar.homepage.widget.DaisyVideoView;
import tv.ismar.homepage.widget.HorizontalTabView;
import tv.ismar.library.exception.ExceptionUtils;
import tv.ismar.player.gui.PlaybackService;

/**
 * Created by huaijie on 5/18/15.
 */
public class HomePageActivity extends BaseActivity implements LinearLayoutManagerTV.FocusSearchFailedListener {
    private static final String TAG = "LH/HomePageActivity";
    private static final int SWITCH_PAGE = 0X01;
    private static final int SWITCH_PAGE_FROMLAUNCH = 0X02;
    private ChannelBaseFragment currentFragment;
    private boolean isLastFragmentChild = false;

    private View contentView;
    private FrameLayout large_layout;
    private HeadFragment headFragment;
    private ModuleMessagePopWindow exitPopup;
    private List<ChannelEntity> channelEntityList = new ArrayList<>();
    /**
     * advertisement start
     */
    private static final int MSG_AD_COUNTDOWN = 0x01;
    private static final int MSG_FETCH_CHANNELS = 0x02;
    private static final int MSG_SHOW_NO_NET = 0x03;
    private static final int MSG_SHOW_NET_ERROR = 0x04;
    private DaisyVideoView home_ad_video;
    private ImageView home_ad_pic;
    private Button home_ad_timer;
    private AdvertiseManager advertiseManager;
    private Advertisement advertisement;
    private List<AdvertiseTable> launchAds;
    private SeekBar ad_seek;
    private int countAdTime = 0;
    private int totleTime=0;
    private int currentImageAdCountDown = 0;
    private boolean isStartImageCountDown = false;
    private boolean isPlayingVideo = false;
    private int playIndex;
    private RelativeLayout home_layout_advertisement;
    private FrameLayout layout_homepage;
    public boolean isPlayingStartAd = false;
    private ModuleMessagePopWindow sanZhouPop;
    /**
     * advertisement end
     */
    /**
     * PopupWindow
     */
    PopupWindow updatePopupWindow;
    private ImageView home_scroll_left;
    private ImageView home_scroll_right;
    private String homepage_template;
    private String homepage_url;
    private boolean scrollFromBorder;
    private ScrollType scrollType = ScrollType.right;
    private String lastviewTag;
    private int lastchannelindex = -1;
    private boolean rightscroll;
    public boolean isneedpause = true;
    private FragmentSwitchHandler fragmentSwitch;
    private BitmapDecoder bitmapDecoder;
    private Subscription channelsSub;
    private Subscription bannerSubscribeSub;
    private String fromPage;
    private HorizontalTabView channelTab;
    private RecyclerViewTV subscribeBanner;
    private RecyclerViewTV movieBanner;
    private RecyclerViewTV horizontal519Banner;
    private RecyclerViewTV movieMixBanner;

    private List<BannerEntity.PosterBean> subscribePosterBeanList;
    private BannerSubscribeAdapter subscribeAdapter;
//    private int subscribeTotalCount;
//    private int subscribeTotalPage;


    private View subscribeArrowLeft;
    private View subscribeArrowRight;


    private void handlerSwitchPage(int position) {
        if (channelEntityList.isEmpty()) {
            return;
        }
        if (fragmentSwitch.hasMessages(SWITCH_PAGE))
            fragmentSwitch.removeMessages(SWITCH_PAGE);

        Message msg = fragmentSwitch.obtainMessage();
        msg.arg1 = position;
        msg.what = SWITCH_PAGE;
        fragmentSwitch.sendMessageDelayed(msg, 300);
    }

    private View.OnFocusChangeListener scrollViewListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                int i = v.getId();
                if (i == R.id.home_scroll_left) {
                    scrollFromBorder = true;
                    scrollType = ScrollType.left;
                    rightscroll = true;
                } else if (i == R.id.home_scroll_right) {
                    scrollFromBorder = true;
                    scrollType = ScrollType.right;
                    rightscroll = false;

                }
            }
        }
    };

    // 定时器
    private Timer sensorTimer;
    private MyTimerTask myTimerTask;

    class MyTimerTask extends TimerTask {

        private int width;
        private int hoverOnArrow; // 0表示左侧，1表示右侧

        MyTimerTask(int arrow, int width) {
            this.hoverOnArrow = arrow;
            this.width = width;
        }

        @Override
        public void run() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    autoScroll(hoverOnArrow, width);
                    cancelTimer();
                }
            });
        }

    }

    private int scrollPosition = -1;

    private void autoScroll(int direction, int width) {
        switch (direction) {
            case 0:// left
                if (scrollPosition == 0) {
                } else {
                }
                break;
            case 1:// right
                break;
        }
    }


    private void cancelTimer() {
        if (myTimerTask != null) {
            myTimerTask.cancel();
            myTimerTask = null;
        }
        if (sensorTimer != null) {
            sensorTimer.cancel();
            sensorTimer = null;
            System.gc();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        if (savedInstanceState != null)
            savedInstanceState = null;
        super.onCreate(savedInstanceState);

        Log.i("LH/", "homepageOnCreate:" + TrueTime.now().getTime());
        BaseActivity.wasLoadSmartPlayerSo = false;// 退出应用再进入时可能需要切换播放器模式
        startTrueTimeService();
        contentView = LayoutInflater.from(this).inflate(R.layout.activity_tv_guide, null);
        setContentView(contentView);
        if (UpdateService.installAppLoading) {
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new UpdateSlienceLoading()).commit();
            return;
        }

        try {
            System.setProperty("http.keepAlive", "false");
        } catch (Exception e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }
        fromPage = getIntent().getStringExtra("fromPage");

        fragmentSwitch = new FragmentSwitchHandler(this);
        homepage_template = getIntent().getStringExtra("homepage_template");
        homepage_url = getIntent().getStringExtra("homepage_url");

        /**
         * advertisement start
         */
        home_layout_advertisement = (RelativeLayout) findViewById(R.id.home_layout_advertisement);
        layout_homepage = (FrameLayout) findViewById(R.id.layout_homepage);
        home_ad_video = (DaisyVideoView) findViewById(R.id.home_ad_video);
        home_ad_pic = (ImageView) findViewById(R.id.home_ad_pic);
        home_ad_timer = (Button) findViewById(R.id.home_ad_timer);
        ad_seek= (SeekBar) findViewById(R.id.home_ad_seekbar);

        advertiseManager = new AdvertiseManager(getApplicationContext());
        launchAds = advertiseManager.getAppLaunchAdvertisement();
        advertisement = new Advertisement(this);
        for (AdvertiseTable tab : launchAds) {
            totalAdsMills = totalAdsMills + tab.duration * 1000;
        }
        for (AdvertiseTable adTable : launchAds) {
            int duration = adTable.duration;
            Log.d("LH/", "GetStartAd:" + adTable.location);
            countAdTime += duration;
        }
        totleTime=countAdTime;
        ad_seek.setMax(countAdTime);
        /**
         * advertisement end
         */
        initViews();
        tempInitStaticVariable();
        Properties sysProperties = new Properties();
        try {
            InputStream is = getAssets().open("configure/setup.properties");
            sysProperties.load(is);
            brandName = sysProperties.getProperty("platform");
        } catch (IOException e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }

        large_layout = (FrameLayout) findViewById(R.id.large_layout);
        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(this, R.drawable.homepage_background, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                large_layout.setBackground(bitmapDrawable);
                bitmapDecoder = null;
                if (TextUtils.isEmpty(homepage_url)) {
                    playLaunchAd(0);

                    mHandler.sendEmptyMessageDelayed(MSG_FETCH_CHANNELS, 1000);
                }
            }
        });

        if (!TextUtils.isEmpty(homepage_url)) {
            home_layout_advertisement.setVisibility(View.GONE);
            large_layout.removeView(home_layout_advertisement);
            layout_homepage.setVisibility(View.VISIBLE);
            BaseActivity.baseChannel = "";
            BaseActivity.baseSection = "";
            fetchChannels();
            startAdsService();
        }
        startIntervalActive();

        app_start_time = TrueTime.now().getTime();
        final CallaPlay callaPlay = new CallaPlay();
        if (fromPage != null) {
            callaPlay.launcher_vod_click(
                    "section", -1, homepage_template, -1
            );
        }
        new Thread() {
            @Override
            public void run() {
                // 日志上报
                String province = (String) SPUtils.getValue(InitializeProcess.PROVINCE_PY, "");
                String city = (String) SPUtils.getValue(InitializeProcess.CITY, "");
                String isp = (String) SPUtils.getValue(InitializeProcess.ISP, "");
                String snToken = PreferenceManager.getDefaultSharedPreferences(HomePageActivity.this).getString("sn_token", "");
                callaPlay.app_start(snToken,
                        VodUserAgent.getModelName(), DeviceUtils.getScreenInch(HomePageActivity.this),
                        android.os.Build.VERSION.RELEASE,
                        SimpleRestClient.appVersion,
                        SystemFileUtil.getSdCardTotal(HomePageActivity.this),
                        SystemFileUtil.getSdCardAvalible(HomePageActivity.this),
                        IsmartvActivator.getInstance().getUsername(), province, city, isp, fromPage, DeviceUtils.getLocalMacAddress(HomePageActivity.this),
                        SimpleRestClient.app, getPackageName());
            }
        }.start();
        Log.i("MacLog", DeviceUtils.getLocalMacAddress(HomePageActivity.this));
    }

    private Boolean isSanzhou() {

        Log.i("SanZHou", "fromPage: " + fromPage + "  product: " + IsmartvPlatform.getKind());
        if ((fromPage == null || fromPage.equals("")) && IsmartvPlatform.isForbiddenLauncher()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean hoverOnArrow;

    private View.OnHoverListener onArrowHoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    hoverOnArrow = true;
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    hoverOnArrow = false;
                    break;
            }
            return false;
        }
    };

    private int mTabSpace;

    private void initViews() {
        Bundle bundle = new Bundle();
        bundle.putString("channel_name", getString(R.string.str_home));
        headFragment = new HeadFragment();
        headFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.home_head, headFragment)
                .commit();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mTabSpace = getResources().getDimensionPixelSize(R.dimen.home_tab_list_space);

        home_scroll_left = (ImageView) findViewById(R.id.home_scroll_left);
        home_scroll_right = (ImageView) findViewById(R.id.home_scroll_right);
        home_scroll_left.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // longhai add
                if (lastchannelindex != 0) {
                }
            }
        });
        home_scroll_right.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // longhai add
                if (lastchannelindex != channelEntityList.size() - 1) {
                }
            }
        });
        home_scroll_left.setOnHoverListener(onArrowHoverListener);
        home_scroll_right.setOnHoverListener(onArrowHoverListener);
        home_scroll_left.setOnFocusChangeListener(scrollViewListener);
        home_scroll_right.setOnFocusChangeListener(scrollViewListener);

        channelTab = (HorizontalTabView) findViewById(R.id.channel_tab);
        Observable.create(new ChannelChangeObservable(channelTab))
                .throttleLast(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
//                        Log.d("channelTab", "channelTab ChannelChangeObservable onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer position) {
                        Log.d("channelTab", "channelTab ChannelChangeObservable");
                    }
                });


        subscribeBanner = (RecyclerViewTV) findViewById(R.id.subscribe_banner);
        LinearLayoutManagerTV subscribeLayoutManager = new LinearLayoutManagerTV(this, LinearLayoutManager.HORIZONTAL, false);
        subscribeBanner.addItemDecoration(new BannerSubscribeAdapter.SpacesItemDecoration(20));
        subscribeBanner.setLayoutManager(subscribeLayoutManager);
        subscribeBanner.setSelectedItemAtCentered(false);
        subscribeBanner.setSelectedItemOffset(100, 100);
        subscribeBanner.setPagingableListener(new RecyclerViewTV.PagingableListener() {
            @Override
            public void onLoadMoreItems() {
                Log.d("PagingableListener", "onLoadMoreItems");
                int currentPageNumber = subscribeAdapter.getCurrentPageNumber();
                if (currentPageNumber < subscribeAdapter.getTotalPageCount()){
                    fetchSubscribeBanner(currentPageNumber + 1);
                }
            }
        });

        subscribeLayoutManager.setFocusSearchFailedListener(new LinearLayoutManagerTV.FocusSearchFailedListener() {
            @Override
            public View onFocusSearchFailed(View view, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
                if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
                    Log.d(TAG, "onFocusSearchFailed");
                    if (subscribeBanner.getChildAt(0).findViewById(R.id.item_layout) == view ||
                            subscribeBanner.getChildAt(subscribeBanner.getChildCount() - 1).findViewById(R.id.item_layout) == view) {
                        YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(view);
                    } else {
//                        if (focusDirection == View.FOCUS_RIGHT){
//                            subscribeBanner.smoothScrollBy(10, 0);
//                        }else if (focusDirection == View.FOCUS_LEFT){
//                            subscribeBanner.smoothScrollBy(-10, 0);
//                        }
                    }
                    Log.d(TAG, "onFocusSearchFailed: " + view);
                    return view;
                }
                return null;
            }
        });


        movieBanner = (RecyclerViewTV) findViewById(R.id.movie_banner);
        LinearLayoutManagerTV movieLayoutManager = new LinearLayoutManagerTV(this, LinearLayoutManager.HORIZONTAL, false);
        movieBanner.addItemDecoration(new BannerMovieAdapter.SpacesItemDecoration(20));
        movieBanner.setLayoutManager(movieLayoutManager);
        movieBanner.setSelectedItemAtCentered(false);
        movieLayoutManager.setFocusSearchFailedListener(new LinearLayoutManagerTV.FocusSearchFailedListener() {
            @Override
            public View onFocusSearchFailed(View view, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
                if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
                    if (movieBanner.getChildAt(0).findViewById(R.id.item_layout) == view ||
                            movieBanner.getChildAt(movieBanner.getChildCount() - 1).findViewById(R.id.item_layout) == view) {
                        YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(view);
                    }
                    return view;
                }
                return null;
            }
        });

        horizontal519Banner = (RecyclerViewTV) findViewById(R.id.horizontal_519_banner);
        LinearLayoutManagerTV horizontal519LayoutManager = new LinearLayoutManagerTV(this, LinearLayoutManager.HORIZONTAL, false);
        horizontal519LayoutManager.setFocusSearchFailedListener(this);
        horizontal519Banner.addItemDecoration(new BannerHorizontal519Adapter.SpacesItemDecoration(20));
        horizontal519Banner.setLayoutManager(horizontal519LayoutManager);
        horizontal519Banner.setSelectedItemAtCentered(false);
        horizontal519LayoutManager.setFocusSearchFailedListener(new LinearLayoutManagerTV.FocusSearchFailedListener() {
            @Override
            public View onFocusSearchFailed(View view, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
                if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
                    if (horizontal519Banner.getChildAt(0).findViewById(R.id.item_layout) == view ||
                            horizontal519Banner.getChildAt(horizontal519Banner.getChildCount() - 1).findViewById(R.id.item_layout) == view) {
                        YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(view);
                    }
                    return view;
                }
                return null;
            }
        });

        movieMixBanner = (RecyclerViewTV) findViewById(R.id.movie_mix_banner);
        LinearLayoutManagerTV movieMixLayoutManager = new LinearLayoutManagerTV(this, LinearLayoutManager.HORIZONTAL, false);
        movieMixLayoutManager.setFocusSearchFailedListener(this);
        movieMixBanner.addItemDecoration(new BannerMovieMixAdapter.SpacesItemDecoration(20));
        movieMixBanner.setLayoutManager(movieMixLayoutManager);
        movieMixBanner.setSelectedItemAtCentered(false);
        movieMixLayoutManager.setFocusSearchFailedListener(new LinearLayoutManagerTV.FocusSearchFailedListener() {
            @Override
            public View onFocusSearchFailed(View view, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
                if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
                    if (movieMixBanner.getChildAt(0).findViewById(R.id.item_layout) == view ||
                            movieMixBanner.getChildAt(movieMixBanner.getChildCount() - 1).findViewById(R.id.item_layout) == view) {
                        YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(view);
                    }
                    return view;
                }

                if (focusDirection == View.FOCUS_DOWN) {
                    YoYo.with(Techniques.VerticalShake).duration(1000).playOn(view);
                    return view;
                }
                return null;
            }
        });

        subscribeBanner.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                    case MotionEvent.ACTION_HOVER_MOVE:
                        if (subscribeArrowLeft.getVisibility() == View.INVISIBLE){
                            subscribeArrowLeft.setVisibility(View.VISIBLE);
                        }
                        if (subscribeArrowRight.getVisibility() == View.INVISIBLE){
                            subscribeArrowRight.setVisibility(View.VISIBLE);
                        }
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        if (subscribeArrowLeft.getVisibility() == View.VISIBLE){
                            subscribeArrowLeft.setVisibility(View.INVISIBLE);
                        }
                        if (subscribeArrowRight.getVisibility() == View.VISIBLE){
                            subscribeArrowRight.setVisibility(View.INVISIBLE);
                        }
                        break;
                }
                return false;
            }
        });

        subscribeArrowLeft = findViewById(R.id.subscribe_arrow_left);
        subscribeArrowLeft.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_MOVE:
                    case MotionEvent.ACTION_HOVER_ENTER:
                        v.findViewById(R.id.banner_arrow_left_normal).setVisibility(View.INVISIBLE);
                        v.findViewById(R.id.banner_arrow_left_focus).setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        v.findViewById(R.id.banner_arrow_left_focus).setVisibility(View.INVISIBLE);
                        v.findViewById(R.id.banner_arrow_left_normal).setVisibility(View.VISIBLE);
                        break;
                }
                return false;
            }
        });
        subscribeArrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeBanner.smoothScrollBy(-400, 0);
            }
        });

        subscribeArrowRight = findViewById(R.id.subscribe_arrow_right);
        subscribeArrowRight.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_MOVE:
                    case MotionEvent.ACTION_HOVER_ENTER:
                        v.findViewById(R.id.banner_arrow_right_normal).setVisibility(View.INVISIBLE);
                        v.findViewById(R.id.banner_arrow_right_focus).setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        v.findViewById(R.id.banner_arrow_right_focus).setVisibility(View.INVISIBLE);
                        v.findViewById(R.id.banner_arrow_right_normal).setVisibility(View.VISIBLE);
                        break;
                }
                return false;
            }
        });

        subscribeArrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeBanner.smoothScrollBy(400, 0);
            }
        });
    }

    @Override
    public View onFocusSearchFailed(View view, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {

            if (subscribeBanner.getChildAt(0).findViewById(R.id.item_layout) == view ||
                    subscribeBanner.getChildAt(subscribeBanner.getChildCount() - 1).findViewById(R.id.item_layout) == view) {
                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(view);
            }
            return view;
        }

        if (focusDirection == View.FOCUS_DOWN) {
            YoYo.with(Techniques.VerticalShake).duration(1000).playOn(view);
            return view;
        }

        return null;
    }

//    @Override
//    public void onItemSelected(View v, int position) {
//        Log.d(TAG, "name: " + v.getTag());
//    }


    private void tempInitStaticVariable() {
        new Thread() {
            @Override
            public void run() {
                DisplayMetrics metric = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metric);
                SimpleRestClient.densityDpi = metric.densityDpi;
                SimpleRestClient.screenWidth = metric.widthPixels;
                SimpleRestClient.screenHeight = metric.heightPixels;
                PackageManager manager = getPackageManager();
                try {
                    PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
                    SimpleRestClient.appVersion = info.versionCode;
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HomePageActivity.this);
                String apiDomain = sharedPreferences.getString("api_domain", "");
                String ad_domain = sharedPreferences.getString("ad_domain", "");
                String log_domain = sharedPreferences.getString("log_domain", "");
                String upgrade_domain = sharedPreferences.getString("upgrade_domain", "");

                AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs.getInstance();
                accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.APP_UPDATE_DOMAIN, upgrade_domain);
                accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.LOG_DOMAIN, log_domain);
                accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.API_DOMAIN, apiDomain);
                accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.ADVERTISEMENT_DOMAIN, ad_domain);
                accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.DEVICE_TOKEN, IsmartvActivator.getInstance().getDeviceToken());
                accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.SN_TOKEN, IsmartvActivator.getInstance().getSnToken());
                if (apiDomain != null && !apiDomain.contains("http")) {
                    apiDomain = "http://" + apiDomain;
                }
                if (ad_domain != null && !ad_domain.contains("http")) {
                    ad_domain = "http://" + ad_domain;
                }
                if (log_domain != null && !log_domain.contains("http")) {
                    log_domain = "http://" + log_domain;
                }
                if (upgrade_domain != null && !upgrade_domain.contains("http")) {
                    upgrade_domain = "http://" + upgrade_domain;
                }
                SimpleRestClient.root_url = apiDomain;
                SimpleRestClient.ad_domain = ad_domain;
                SimpleRestClient.log_domain = log_domain;
                SimpleRestClient.upgrade_domain = upgrade_domain;
                SimpleRestClient.device_token = IsmartvActivator.getInstance().getDeviceToken();
                SimpleRestClient.sn_token = IsmartvActivator.getInstance().getSnToken();
                SimpleRestClient.zuser_token = IsmartvActivator.getInstance().getZUserToken();
                SimpleRestClient.zdevice_token = IsmartvActivator.getInstance().getZDeviceToken();

            }
        }.start();

    }


    @Override
    public void onBackPressed() {
        if (countAdTime > 0) {
            if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
                mHandler.removeMessages(MSG_AD_COUNTDOWN);
            }
            finish();
        } else {
            showExitPopup(contentView);
        }
    }

    /**
     * fetch channel
     */
    private void fetchChannels() {
        Logger.d("fetch channel");
        channelsSub = SkyService.ServiceManager.getCacheSkyService().apiTvChannels()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ChannelEntity[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("ADBUg", isPlayingStartAd + "totalADs: " + totalAdsMills);
                        if (isPlayingStartAd) {
                            if (!NetworkUtils.isConnected(HomePageActivity.this) && !NetworkUtils.isWifi(HomePageActivity.this)) {
                                mHandler.sendEmptyMessageDelayed(MSG_SHOW_NO_NET, totalAdsMills);
                            } else {
                                mHandler.sendEmptyMessageDelayed(MSG_SHOW_NET_ERROR, totalAdsMills);
                            }
                        } else {
                            if (!NetworkUtils.isConnected(HomePageActivity.this) && !NetworkUtils.isWifi(HomePageActivity.this)) {
//                                showNoNetConnectDialog();
                            } else {
                                ToastTip.showToast(HomePageActivity.this,"网络连接失败，请重试");

                            }
                        }
                    }

                    @Override
                    public void onNext(ChannelEntity[] channelEntities) {
                        String apiDomain = PreferenceManager.getDefaultSharedPreferences(HomePageActivity.this).getString("api_domain", "");
                        if (!TextUtils.isEmpty(apiDomain)) {
                            LogQueue.getInstance().init(apiDomain);
                        }
//                        fillChannelLayout(channelEntities);
                        fillChannelTab(channelEntities);
                        fetchSubscribeBanner();
                        fetchMovieBanner();
                        fetchHorizontal519Banner();
                        fetchMovieMixBanner();
                    }
                });
    }


    private void fillChannelTab(ChannelEntity[] channelEntities) {
        List<HorizontalTabView.Tab> tabs = new ArrayList<>();
        HorizontalTabView.Tab searchTab = new HorizontalTabView.Tab("", "搜索");
        tabs.add(searchTab);
        for (ChannelEntity entity : channelEntities) {
            HorizontalTabView.Tab tab = new HorizontalTabView.Tab("", entity.getName());
            tabs.add(tab);
        }
        for (ChannelEntity entity : channelEntities) {
            HorizontalTabView.Tab tab = new HorizontalTabView.Tab("", entity.getName());
            tabs.add(tab);
        }
        channelTab.addAllViews(tabs, 0);
    }

    private void fetchSubscribeBanner() {
        fetchSubscribeBanner(1);
    }

    Handler mFocusHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int mSavePos = msg.what;
            subscribeBanner.setDefaultSelect(mSavePos + 1);
            Log.d("mFocusHandler", "setDefaultSelect: " + (mSavePos + 1));
        }
    };

    private void fetchSubscribeBanner(final int pageNumber) {
        if (pageNumber != 1){
            int startIndex = (pageNumber - 1) * 33;
            int endIndex;
            if (pageNumber == subscribeAdapter.getTotalPageCount()) {
                endIndex = subscribeAdapter.getTatalItemCount() - 1;
            } else {
                endIndex = pageNumber * 33 - 1;
            }

            BannerEntity.PosterBean emptyPostBean = new BannerEntity.PosterBean();
            List<BannerEntity.PosterBean> totalPostList = new ArrayList<>();
            for (int i = startIndex; i <= endIndex; i++) {
                totalPostList.add(emptyPostBean);
            }
            subscribeAdapter.addEmptyDatas(totalPostList);
            int mSavePos = subscribeBanner.getSelectPostion();
            subscribeAdapter.notifyItemRangeInserted(startIndex, endIndex - startIndex);
            subscribeBanner.setOnLoadMoreComplete();
//            subscribeAdapter.setCurrentPageNumber(pageNumber);
//            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
        }

        bannerSubscribeSub = SkyService.ServiceManager.getLocalTestService().apiTvBanner("1", pageNumber)
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
                        if (pageNumber == 1){
                            fillSubscribeBanner(bannerEntity);
                        }else {
                            int mSavePos = subscribeBanner.getSelectPostion();
                            subscribeAdapter.addDatas(bannerEntity);
//                            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
                        }
                    }
                });
    }


    private void fetchMovieBanner() {
        bannerSubscribeSub = SkyService.ServiceManager.getLocalTestService().apiTvBanner("1", 1)
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
//                        List<BannerEntity.PosterBean> posterBeanList = bannerSubscribeEntities.getPoster();
                        fillMovieBanner(bannerEntity);
                    }
                });
    }

    private void fetchHorizontal519Banner() {
        bannerSubscribeSub = SkyService.ServiceManager.getLocalTestService().apiTvBanner("1", 1)
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
                        fillHorizontal519Banner(bannerEntity);
                    }
                });
    }

    private void fetchMovieMixBanner() {
        bannerSubscribeSub = SkyService.ServiceManager.getLocalTestService().apiTvBanner("1", 1)
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
                        fillMovieMixBanner(bannerEntity);
                    }
                });
    }

    private void fillSubscribeBanner(BannerEntity bannerEntity) {
        subscribeAdapter = new BannerSubscribeAdapter(this, bannerEntity);
//        subscribeAdapter.setTotalPageCount(bannerEntity.getCount_pages());
//        subscribeAdapter.setCurrentPageNumber(bannerEntity.getNum_pages());
//        subscribeAdapter.setTatalItemCount(bannerEntity.getCount());

        subscribeBanner.setAdapter(subscribeAdapter);
    }

    private void fillMovieBanner(BannerEntity bannerEntity) {
        BannerMovieAdapter adapter = new BannerMovieAdapter(this, bannerEntity);
        movieBanner.setAdapter(adapter);
    }

    private void fillHorizontal519Banner(BannerEntity bannerEntity) {
        BannerHorizontal519Adapter adapter = new BannerHorizontal519Adapter(this, bannerEntity);
        horizontal519Banner.setAdapter(adapter);
    }

    private void fillMovieMixBanner(BannerEntity bannerEntity) {
        BannerMovieMixAdapter adapter = new BannerMovieMixAdapter(this, bannerEntity);
        movieMixBanner.setAdapter(adapter);
    }

    private void accountsItemSubscribe(final int itemId, String contentModel) {
        SkyService.ServiceManager.getService().accountsItemSubscribe(itemId, contentModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
//                        for (BannerEntity.PosterBean bean : subscribePosterBeanList) {
//                            if (getItemId(bean.getContent_url()) == itemId) {
//                                bean.setSubscribed(true);
//                            }
//                        }
//                        subscribeAdapter.setSubscribeEntityList(subscribePosterBeanList);
//                        subscribeAdapter.notifyDataSetChanged();
                    }
                });
    }

    private int getItemId(String url) {
        int id = 0;
        try {
            Pattern p = Pattern.compile("/(\\d+)/?$");
            Matcher m = p.matcher(url);
            if (m.find()) {
                String idStr = m.group(1);
                if (idStr != null) {
                    id = Integer.parseInt(idStr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

//    private void fillChannelLayout(ChannelEntity[] channelEntities) {
//        if (neterrorshow)
//            return;
//        home_scroll_right.setVisibility(View.VISIBLE);
//        ChannelEntity[] mChannelEntitys = channelEntities;
//        if (!channelEntityList.isEmpty()) {
//            return;
//        }
//
//        ChannelEntity launcher = new ChannelEntity();
//        launcher.setChannel("launcher");
//        launcher.setName("首页");
//        launcher.setHomepage_template("launcher");
//        channelEntityList.add(launcher);
//        int channelscrollIndex = 0;
//
//        for (ChannelEntity e : mChannelEntitys) {
//            channelEntityList.add(e);
//        }
//        recyclerAdapter.notifyDataSetChanged();
//        if (brandName != null && brandName.toLowerCase().contains("changhong")) {
//            homepage_template = "template3";
//        }
//        if (!StringUtils.isEmpty(homepage_template)) {
//            for (int i = 0; i < mChannelEntitys.length; i++) {
//                if (brandName != null && brandName.toLowerCase().contains("changhong")) {
//                    if ("sport".equalsIgnoreCase(mChannelEntitys[i].getChannel())) {
//                        channelscrollIndex = i + 1;
//                        scrollType = ScrollType.none;
//                        recyclerAdapter.setSelectedPosition(channelscrollIndex);
//                        handlerSwitchPage(channelscrollIndex);
//                        headFragment.setSubTitle(mChannelEntitys[i].getName());
//                    }
//                } else {
//                    if (homepage_template.equals(mChannelEntitys[i].getHomepage_template()) && mChannelEntitys[i].getHomepage_url().contains(homepage_url)) {
//                        channelscrollIndex = i + 1;
//                        Log.i("LH/", "channelscrollIndex:" + channelscrollIndex);
//                        if (channelscrollIndex > 0 && !fragmentSwitch.hasMessages(SWITCH_PAGE_FROMLAUNCH)) {
//                            scrollType = ScrollType.none;
//                            recyclerAdapter.setSelectedPosition(channelscrollIndex);
//                            handlerSwitchPage(channelscrollIndex);
//                        }
//                        headFragment.setSubTitle(mChannelEntitys[i].getName());
//                        break;
//                    }
//                }
//            }
//        }
//        if (currentFragment == null && !isFinishing() && channelscrollIndex <= 0) {
//            try {
//                currentFragment = new GuideFragment();
//                ChannelEntity channelEntity = new ChannelEntity();
//                launcher.setChannel("launcher");
//                launcher.setName("首页");
//                launcher.setHomepage_template("launcher");
//                currentFragment.setChannelEntity(channelEntity);
//                FragmentTransaction transaction = getSupportFragmentManager()
//                        .beginTransaction();
//                transaction.replace(R.id.home_container, currentFragment, "template").commitAllowingStateLoss();
//                home_tab_list.requestFocus();
//            } catch (IllegalStateException e) {
//                ExceptionUtils.sendProgramError(e);
//            }
//
//        }
//    }

    private void showExitPopup(View view) {
        exitPopup = new ModuleMessagePopWindow(this);
        exitPopup.setConfirmBtn(getString(R.string.vod_ok));
        exitPopup.setCancelBtn(getString(R.string.vod_cancel));
        exitPopup.setMessage(getString(R.string.str_exit));

        exitPopup.showAtLocation(view, Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        isCheckoutUpdate = true;
                        SkyService.ServiceManager.executeActive = true;
                        exitPopup.dismiss();
                        CallaPlay callaPlay = new CallaPlay();
//                        callaPlay.app_exit(TrueTime.now().getTime() - app_start_time, SimpleRestClient.appVersion);
                        callaPlay.app_exit(TrueTime.now().getTime() - app_start_time, SimpleRestClient.appVersion);
                        HomePageActivity.this.finish();
                        ArrayList<String> cache_log = MessageQueue.getQueueList();
                        HashSet<String> hasset_log = new HashSet<String>();
                        for (int i = 0; i < cache_log.size(); i++) {
                            hasset_log.add(cache_log.get(i));
                        }
                        DaisyUtils
                                .getVodApplication(HomePageActivity.this)
                                .getEditor()
                                .putStringSet(VodApplication.CACHED_LOG,
                                        hasset_log);
                        DaisyUtils.getVodApplication(getApplicationContext())
                                .save();
                        finish();
                        BaseActivity.baseChannel = "";
                        BaseActivity.baseSection = "";
                        stopService(new Intent(HomePageActivity.this, PlaybackService.class));
                    }
                },
                new ModuleMessagePopWindow.CancelListener() {
                    @Override
                    public void cancelClick(View view) {
                        exitPopup.dismiss();
                    }
                }
        );
    }

    private void showSanzhouPop() {
        sanZhouPop = ExpireAccessTokenPop.getInstance(this);
        sanZhouPop.setMessage("服务类程序禁止操作!");
        sanZhouPop.setBackground();
        sanZhouPop.setConfirmBtn(getString(tv.ismar.app.R.string.confirm));
        sanZhouPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                finish();
            }
        });
        sanZhouPop.showAtLocation(contentView, Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        sanZhouPop.dismiss();
                        finish();
                    }
                },
                null);
    }

    private boolean neterrorshow = false;

    private void showNetErrorPopup() {
        if (neterrorshow)
            return;
        final ModuleMessagePopWindow dialog = new ModuleMessagePopWindow(HomePageActivity.this);
        dialog.setMessage(getString(R.string.fetch_net_data_error));
        dialog.setConfirmBtn(getString(R.string.setting_network));
        dialog.setCancelBtn(getString(R.string.i_know));
        try {
            dialog.showAtLocation(getRootView(), Gravity.CENTER,0,0,
                    new ModuleMessagePopWindow.ConfirmListener() {
                        @Override
                        public void confirmClick(View view) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            HomePageActivity.this.startActivity(intent);
                        }
                    }, new ModuleMessagePopWindow.CancelListener() {

                        @Override
                        public void cancelClick(View view) {
                            dialog.dismiss();
                            neterrorshow = true;
                        }
                    });
            dialog.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    neterrorshow = true;
                }
            });
            neterrorshow = true;
        } catch (android.view.WindowManager.BadTokenException e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }
    }

    BitmapDecoder ddddBitmapDecoder;

    private void selectChannelByPosition(int position) {
        String tag;
        if (lastchannelindex != -1) {
            if (lastchannelindex < position) {
                scrollType = ScrollType.right;
            } else {
                scrollType = ScrollType.left;
            }
        }

        ChannelEntity channelEntity = channelEntityList.get(position);
        currentFragment = null;
        if ("template1".equals(channelEntity.getHomepage_template())) {
            currentFragment = new FilmFragment();
            tag = "template1";
        } else if ("template2".equals(channelEntity.getHomepage_template())) {
            currentFragment = new EntertainmentFragment();
            tag = "template2";
        } else if ("template3".equals(channelEntity.getHomepage_template())) {
            currentFragment = new SportFragment();
            tag = "template3";
        } else if ("template4".equals(channelEntity.getHomepage_template())) {
            currentFragment = new ChildFragment();
            tag = "template4";
        } else {
            currentFragment = new GuideFragment();
            tag = "template";
        }
        if (currentFragment instanceof ChildFragment) {
            isLastFragmentChild = true;
            if (ddddBitmapDecoder != null) {
                ddddBitmapDecoder.removeAllCallback();
            }
            ddddBitmapDecoder = new BitmapDecoder();
            ddddBitmapDecoder.decode(this,
                    R.drawable.channel_child_bg,
                    new BitmapDecoder.Callback() {
                        @Override
                        public void onSuccess(BitmapDrawable bitmapDrawable) {
                            contentView.setBackgroundDrawable(bitmapDrawable);
                        }
                    });
        } else {
            if (isLastFragmentChild) {
                if (ddddBitmapDecoder != null) {
                    ddddBitmapDecoder.removeAllCallback();
                }
                ddddBitmapDecoder = new BitmapDecoder();
                ddddBitmapDecoder.decode(this,
                        R.drawable.main_bg,
                        new BitmapDecoder.Callback() {
                            @Override
                            public void onSuccess(BitmapDrawable bitmapDrawable) {
                                contentView.setBackgroundDrawable(bitmapDrawable);
                            }
                        });
            }
            isLastFragmentChild = false;

        }

        currentFragment.setScrollFromBorder(scrollFromBorder);
        if (scrollFromBorder) {
            currentFragment.setRight(rightscroll);
            currentFragment.setBottomFlag(lastviewTag);
        }

        currentFragment.setChannelEntity(channelEntity);

        ChannelBaseFragment t = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template");
        ChannelBaseFragment t1 = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template1");
        ChannelBaseFragment t2 = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template2");
        ChannelBaseFragment t3 = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template3");
        ChannelBaseFragment t4 = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template4");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if ("template".equals(tag)) {
            if (t1 != null)
                transaction.hide(t1);
            if (t2 != null)
                transaction.hide(t2);
            if (t3 != null)
                transaction.hide(t3);
            if (t4 != null)
                transaction.hide(t4);
            if (t != null) {
                t.setScrollFromBorder(scrollFromBorder);
                if (scrollFromBorder) {
                    t.setRight(rightscroll);
                    t.setBottomFlag(lastviewTag);
                }
                transaction.show(t);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
        if ("template1".equals(tag)) {
            if (t != null)
                transaction.hide(t);
            if (t2 != null)
                transaction.hide(t2);
            if (t3 != null)
                transaction.hide(t3);
            if (t4 != null)
                transaction.hide(t4);
            if (t1 != null && !t1.isRemoving()) {
                t1.setScrollFromBorder(scrollFromBorder);
                if (scrollFromBorder) {
                    t1.setRight(rightscroll);
                    t1.setBottomFlag(lastviewTag);
                }
                t1.setChannelEntity(channelEntity);
                t1.refreshData();
                transaction.show(t1);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
        if ("template2".equals(tag)) {
            if (t != null)
                transaction.hide(t);
            if (t1 != null)
                transaction.hide(t1);
            if (t3 != null)
                transaction.hide(t3);
            if (t4 != null)
                transaction.hide(t4);
            if (t2 != null && !t2.isRemoving()) {
                t2.setChannelEntity(channelEntity);
                t2.refreshData();
                t2.setScrollFromBorder(scrollFromBorder);
                if (scrollFromBorder) {
                    t2.setRight(rightscroll);
                    t2.setBottomFlag(lastviewTag);
                }
                transaction.show(t2);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
        if ("template3".equals(tag)) {
            if (t != null)
                transaction.hide(t);
            if (t1 != null)
                transaction.hide(t1);
            if (t2 != null)
                transaction.hide(t2);
            if (t4 != null)
                transaction.hide(t4);
            if (t3 != null) {
                t3.setChannelEntity(channelEntity);
                t3.refreshData();
                t3.setScrollFromBorder(scrollFromBorder);
                if (scrollFromBorder) {
                    t3.setRight(rightscroll);
                    t3.setBottomFlag(lastviewTag);
                }
                transaction.show(t3);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
        if ("template4".equals(tag)) {
            if (t != null)
                transaction.hide(t);
            if (t1 != null)
                transaction.hide(t1);
            if (t2 != null)
                transaction.hide(t2);
            if (t3 != null)
                transaction.hide(t3);
            if (t4 != null && !t4.isRemoving()) {
                t4.setChannelEntity(channelEntity);
                t4.refreshData();
                t4.setScrollFromBorder(scrollFromBorder);
                if (scrollFromBorder) {
                    t4.setRight(rightscroll);
                    t4.setBottomFlag(lastviewTag);
                }
                transaction.show(t4);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
//        // longhai add
//        if (home_tab_list == null) {
//            return;
//        }
//        lastchannelindex = position;
//        switch (lastchannelindex) {
//            case 0:
//                home_tab_list.setNextFocusUpId(R.id.guidefragment_firstpost);
//                break;
//            case 1:
//                home_tab_list.setNextFocusUpId(R.id.filmfragment_secondpost);
//                break;
//            case 2:
//                home_tab_list.setNextFocusUpId(R.id.filmfragment_thirdpost);
//                break;
//            case 3:
//                home_tab_list.setNextFocusUpId(R.id.vaiety_channel2_image);
//                break;
//            case 4:
//                home_tab_list.setNextFocusUpId(R.id.vaiety_channel3_image);
//                break;
//            case 5:
//                home_tab_list.setNextFocusUpId(R.id.sport_channel4_image);
//                break;
//            case 6:
//                home_tab_list.setNextFocusUpId(R.id.vaiety_channel4_image);
//                break;
//            case 7:
//                home_tab_list.setNextFocusUpId(R.id.child_more);
//                break;
//            case 8:
//                home_tab_list.setNextFocusUpId(R.id.listmore);
//                break;
//            case 9:
//                home_tab_list.setNextFocusUpId(R.id.listmore);
//                break;
//            case 10:
//                home_tab_list.setNextFocusUpId(R.id.listmore);
//                break;
//            default:
//                break;
//        }
    }

    private void replaceFragment(Fragment fragment, String tag, FragmentTransaction transaction) {
        switch (scrollType) {
            case left:
                transaction.setCustomAnimations(
                        R.anim.push_right_in,
                        R.anim.push_right_out);
                break;
            case right:
                transaction.setCustomAnimations(
                        R.anim.push_left_in,
                        R.anim.push_left_out);
                break;
        }

        transaction.replace(R.id.home_container, fragment, tag).commitAllowingStateLoss();
    }

    public void channelRequestFocus(String channel) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppConstant.purchase_referer = "homepage";
        AppConstant.purchase_entrance_page = "homepage";
        if (!isneedpause) {
            return;
        }
        neterrorshow = false;
        if (!TextUtils.isEmpty(brandName) && brandName.equalsIgnoreCase("konka")) {
            setAspectRatio();
        }
        Log.e(TAG, "onresume Isnetwork" + Adend);
        if (!NetworkUtils.isConnected(this) && !NetworkUtils.isWifi(this) && Adend)
            showNoNetConnectDelay();
    }

    private void setAspectRatio() {
        try {
            Class<?> clazz = Class.forName("com.konka.android.media.KKMediaPlayer");
            Method method = clazz.getMethod("setAspectRatio", int.class);
            method.invoke(null, 0);
        } catch (Exception e) {
            Log.e("KKMediaPlayer", "setAspectRatio method.invoke error!");
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!DaisyUtils.isNetworkAvailable(this)) {
            Log.e(TAG, "onresume Isnetwork");
            //  showNetErrorPopup();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (channelsSub != null && channelsSub.isUnsubscribed()) {
            channelsSub.unsubscribe();
        }

        if (bannerSubscribeSub != null && bannerSubscribeSub.isUnsubscribed()) {
            bannerSubscribeSub.unsubscribe();
        }

        if (mHandler.hasMessages(MSG_FETCH_CHANNELS)) {
            mHandler.removeMessages(MSG_FETCH_CHANNELS);
        }
        if (mHandler.hasMessages(MSG_SHOW_NO_NET)) {
            mHandler.removeMessages(MSG_SHOW_NO_NET);
        }
        if (mHandler.hasMessages(MSG_SHOW_NET_ERROR)) {
            mHandler.removeMessages(MSG_SHOW_NET_ERROR);
        }
        if (!isneedpause) {
            return;
        }
        if (fragmentSwitch != null) {
            if (fragmentSwitch.hasMessages(SWITCH_PAGE))
                fragmentSwitch.removeMessages(SWITCH_PAGE);
            if (fragmentSwitch.hasMessages(SWITCH_PAGE_FROMLAUNCH))
                fragmentSwitch.removeMessages(SWITCH_PAGE_FROMLAUNCH);
        }
    }

    @Override
    protected void onDestroy() {
        if (bitmapDecoder != null && bitmapDecoder.isAlive()) {
            bitmapDecoder.interrupt();
        }
        if (ddddBitmapDecoder != null && ddddBitmapDecoder.isAlive()) {
            ddddBitmapDecoder.interrupt();
        }
        if (!(updatePopupWindow == null)) {
            updatePopupWindow.dismiss();
        }
        if (exitPopup != null && exitPopup.isShowing()) {
            exitPopup.dismiss();
        }
        if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
        }
        BaseActivity.baseChannel = "";
        BaseActivity.baseSection = "";
        mHandler.removeCallbacks(mRunnable);
        mHandler = null;
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!isneedpause) {
            return;
        }
        homepage_template = intent.getStringExtra("homepage_template");
        homepage_url = intent.getStringExtra("homepage_url");
        if (StringUtils.isEmpty(homepage_template)
                || StringUtils.isEmpty(homepage_url)) {
//            fetchChannels();
        } else {
            fetchChannels();
        }
    }


    public void setLastViewTag(String flag) {
        lastviewTag = flag;
    }

    public void resetBorderFocus() {
        scrollFromBorder = false;
    }

    private enum ScrollType {
        none,
        left,
        right;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (scrollFromBorder) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (scrollFromBorder) {
            return true;
        }
        if ("lcd_s3a01".equals(VodUserAgent.getModelName())) {
            if (keyCode == 707 || keyCode == 774 || keyCode == 253) {
                isneedpause = false;
            }
        } else if ("lx565ab".equals(VodUserAgent.getModelName())) {
            if (keyCode == 82 || keyCode == 707 || keyCode == 253) {
                isneedpause = false;
            }
        } else if ("lcd_xxcae5a_b".equals(VodUserAgent.getModelName())) {
            if (keyCode == 497 || keyCode == 498 || keyCode == 490) {
                isneedpause = false;
            }
        } else {
            if (keyCode == 223 || keyCode == 499 || keyCode == 480) {
                isneedpause = false;
            }
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                break;
            case KeyEvent.KEYCODE_HOME:
                finish();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    static class FragmentSwitchHandler extends Handler {
        private WeakReference<HomePageActivity> weakReference;

        public FragmentSwitchHandler(HomePageActivity activity) {
            weakReference = new WeakReference<HomePageActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            HomePageActivity activity = weakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case SWITCH_PAGE:
                        if (!activity.isFinishing()) {
                            int position = msg.arg1;
                            if (position == 0) {
                                activity.home_scroll_left.setVisibility(View.GONE);
                                if (activity.scrollFromBorder) {
                                    activity.large_layout.requestFocus();
                                }
                            } else {
                                activity.home_scroll_left.setVisibility(View.VISIBLE);
                            }

                            if (position == activity.channelEntityList.size() - 1) {
                                activity.home_scroll_right.setVisibility(View.GONE);
                                if (activity.scrollFromBorder) {
                                    activity.large_layout.requestFocus();
                                }
                            } else {
                                activity.home_scroll_right.setVisibility(View.VISIBLE);
                            }
                            if (!activity.scrollFromBorder) {
                            }

                            activity.selectChannelByPosition(position);
                        }
                        break;
                    case SWITCH_PAGE_FROMLAUNCH:
                        // longhai
//					if (nextselectflag == 0
//							|| (nextselectflag != activity.scroll
//									.getnextSelectPosition())) {
//						channelscrollIndex = channelscrollIndex - 1;
//					}
//
//					nextselectflag = activity.scroll.getnextSelectPosition();
//					activity.scroll.arrowScroll(View.FOCUS_RIGHT);
//					if (channelscrollIndex > 0) {
//						sendEmptyMessage(SWITCH_PAGE_FROMLAUNCH);
//					} else {
//						channelflag = false;
//					}
                        break;
                }
            }
        }
    }

    /**
     * advertisement start
     */
    private void playLaunchAd(final int index) {
        isPlayingStartAd = true;
        playIndex = index;
        if (!launchAds.get(index).location.equals(AdvertiseManager.DEFAULT_ADV_PICTURE)) {
            new CallaPlay().boot_ad_play(launchAds.get(index).title, launchAds.get(index).media_id,
                    launchAds.get(index).media_url, String.valueOf(launchAds.get(index).duration));
        }
        if (launchAds.get(index).media_type.equals(AdvertiseManager.TYPE_VIDEO)) {
            isPlayingVideo = true;
        }
        if (isPlayingVideo) {
            if (home_ad_video.getVisibility() != View.VISIBLE) {
                home_ad_pic.setVisibility(View.GONE);
                home_ad_video.setVisibility(View.VISIBLE);
            }
            home_ad_video.setVideoPath(launchAds.get(index).location);
            home_ad_video.setOnPreparedListener(onPreparedListener);
            home_ad_video.setOnCompletionListener(onCompletionListener);
            home_ad_video.setOnErrorListener(onErrorListener);
        } else {
            if (home_ad_pic.getVisibility() != View.VISIBLE) {
                home_ad_video.setVisibility(View.GONE);
                home_ad_pic.setVisibility(View.VISIBLE);
                ad_seek.setVisibility(View.VISIBLE);
            }
            Picasso.with(this)
                    .load(launchAds.get(index).location)

                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_CACHE)
                    .into(home_ad_pic, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (playIndex == 0) {
                                mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
                            }
                            if (launchAds.get(playIndex).media_id != null) {
                                int media_id = Integer.parseInt(launchAds.get(playIndex).media_id);
                                advertisement.getRepostAdUrl(media_id, "startAd");
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.with(HomePageActivity.this)
                                    .load("file:///android_asset/posters.png")

                                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_CACHE)
                                    .into(home_ad_pic);
                            if (playIndex == 0) {
                                mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
                            }
                        }
                    });
        }
    }

    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            home_ad_video.start();
            if (!mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
                mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
            }
            if (launchAds.get(playIndex).media_id != null) {
                int media_id = Integer.parseInt(launchAds.get(playIndex).media_id);
                advertisement.getRepostAdUrl(media_id, "startAd");
            }
        }
    };

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.i(TAG, "OnCompletionListener");
            if (isFinishing()) {
                return;
            }
            if (playIndex == launchAds.size() - 1) {
                mHandler.removeMessages(MSG_AD_COUNTDOWN);
                goNextPage();
                return;
            }
            playIndex += 1;
            playLaunchAd(playIndex);
        }
    };

    private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (isFinishing()) {
                return true;
            }
            Log.i(TAG, "onError : " + playIndex);
            if (playIndex == launchAds.size() - 1) {
                mHandler.removeMessages(MSG_AD_COUNTDOWN);
                goNextPage();
                return true;
            }
            playIndex += 1;
            playLaunchAd(playIndex);
            return true;
        }
    };

    private boolean Adend = false;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AD_COUNTDOWN:
                    Log.i(TAG, "ad handler");
                    if (isSanzhou()) {
                        showSanzhouPop();
                        return;
                    }
                    if (home_ad_timer == null) {
                        return;
                    }
                    if (!isPlayingVideo && countAdTime == 0) {
                        mHandler.removeMessages(MSG_AD_COUNTDOWN);
                        Adend = true;
                        ad_seek.setProgress(totleTime);
//                        goNextPage();
                        return;
                    }
                    if (home_ad_timer.getVisibility() != View.VISIBLE) {
                        home_ad_timer.setVisibility(View.VISIBLE);
                    }
                    home_ad_timer.setTextColor(Color.WHITE);
                    home_ad_timer.setText(countAdTime + "s");
                    ad_seek.setProgress(totleTime-countAdTime);
                    int refreshTime;
                    if (!isPlayingVideo) {
                        refreshTime = 1000;
                        if (currentImageAdCountDown == 0 && !isStartImageCountDown) {
                            currentImageAdCountDown = launchAds.get(playIndex).duration;
                            isStartImageCountDown = true;
                        } else {
                            if (currentImageAdCountDown == 0) {
                                playIndex += 1;
                                playLaunchAd(playIndex);
                                isStartImageCountDown = false;
                            } else {
                                currentImageAdCountDown--;
                            }
                        }
                        countAdTime--;
                    } else {
                        refreshTime = 500;
                        countAdTime = getAdCountDownTime();
                    }
                    sendEmptyMessageDelayed(MSG_AD_COUNTDOWN, refreshTime);
                    break;
                case MSG_FETCH_CHANNELS:
                    fetchChannels();
                    if (mHandler.hasMessages(MSG_FETCH_CHANNELS)) {
                        mHandler.removeMessages(MSG_FETCH_CHANNELS);
                    }
                    break;
                case MSG_SHOW_NO_NET:
//                    showNoNetConnectDialog();
                    break;
                case MSG_SHOW_NET_ERROR:
                    break;

            }
        }
    };

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        SkyService.ServiceManager.executeActive = true;
        super.onStop();
    }

    private void goNextPage() {
        Log.i(TAG, "goNextPage");
        isPlayingStartAd = false;
        if (home_ad_video != null) {
            home_ad_video.stopPlayback();
            home_ad_video = null;
        }
        home_layout_advertisement.setVisibility(View.GONE);
        large_layout.removeView(home_layout_advertisement);
        layout_homepage.setVisibility(View.VISIBLE);
        if (currentFragment != null) {
            currentFragment.playCarouselVideo();
        }
        if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
        }
        isneedpause = true;
//        if (!NetworkUtils.isConnected(this)) {// 首页有数据缓存
//            showNoNetConnectDialog();
//        }
        startAdsService();
        mHandler.sendEmptyMessageDelayed(0, 1000);

        new Handler().postDelayed(mRunnable, 1000);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            showLoginHint();
        }
    };

    private int getAdCountDownTime() {
        if (launchAds == null || launchAds.isEmpty() || !isPlayingVideo) {
            return 0;
        }
        int totalAdTime = 0;
        int currentAd = playIndex;
        if (currentAd == launchAds.size() - 1) {
            totalAdTime = launchAds.get(launchAds.size() - 1).duration;
        } else {
            for (int i = currentAd; i < launchAds.size(); i++) {
                totalAdTime += launchAds.get(i).duration;
            }
        }
        int countTime = totalAdTime - home_ad_video.getCurrentPosition() / 1000 - 1;
        if (countTime < 0) {
            countTime = 0;
        }
        return countTime;
    }

    /**
     * advertisement end
     */
    private void startAdsService() {
        Intent intent = new Intent();
        intent.setClass(this, AdsUpdateService.class);
        startService(intent);
    }

    private void startIntervalActive() {
        Intent intent = new Intent();
        intent.setClass(this, ActiveService.class);
        startService(intent);
    }

    private void startTrueTimeService() {
        Intent intent = new Intent();
        intent.setClass(this, TrueTimeService.class);
        startService(intent);
    }
}
