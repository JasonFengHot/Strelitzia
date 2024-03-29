package tv.ismar.homepage;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.*;
import cn.ismartv.truetime.TrueTime;
import com.squareup.leakcanary.RefWatcher;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import tv.ismar.account.ActiveService;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.BaseControl;
import tv.ismar.app.VodApplication;
import tv.ismar.app.ad.AdsUpdateService;
import tv.ismar.app.ad.AdvertiseManager;
import tv.ismar.app.ad.Advertisement;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.core.client.MessageQueue;
import tv.ismar.app.db.AdvertiseTable;
import tv.ismar.app.entity.ChannelEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.ui.ToastTip;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.app.widget.TelescopicWrap;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.control.HomeControl;
import tv.ismar.homepage.fragment.ChannelFragment;
import tv.ismar.homepage.view.AdvertiseActivity;
/*add by dragontec for bug 4350 start*/
import tv.ismar.homepage.view.FrameAnimation;
import tv.ismar.homepage.view.HomePageArrowButton;
/*add by dragontec for bug 4350 end*/
import tv.ismar.homepage.widget.DaisyVideoView;
import tv.ismar.homepage.widget.HomeRootRelativeLayout;
import tv.ismar.homepage.widget.HorizontalTabView;
import tv.ismar.player.gui.PlaybackService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static tv.ismar.app.BaseControl.TAB_CHANGE_FALG;
import static tv.ismar.homepage.control.FetchDataControl.FETCH_CHANNEL_TAB_FLAG;

/**
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: home页
 */
public class HomeActivity extends BaseActivity
        implements View.OnClickListener,
        BaseControl.ControlCallBack,
        View.OnFocusChangeListener,
        View.OnHoverListener ,MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener{
    private static final String TAG = "HomeActivity";

    public static final String HOME_PAGE_CHANNEL_TAG = "homepage";
/*modify by dragontec for bug 4057 start*/
//    public static View mHoverView;
/*modify by dragontec for bug 4205 start*/
//  public static HomeRootRelativeLayout mHoverView;
    public HomeRootRelativeLayout mHoverView;
/*modify by dragontec for bug 4205 end*/
/*modify by dragontec for bug 4057 end*/
/*modify by dragontec for bug 4205 start*/
//    public static View mLastFocusView;
    public View mLastFocusView;
/*modify by dragontec for bug 4205 end*/
    private final FetchDataControl mFetchDataControl = new FetchDataControl(this, this); // 业务类引用
    private final HomeControl mHomeControl = new HomeControl(this, this);
    private HorizontalTabView mChannelTab;
    private ViewGroup mViewGroup;
    private TextView mTimeTv; // 时间
    private TextView mCollectionTv; // 收藏tv
    private TextView mPersonCenterTv; // 个人中心tv
    private ViewGroup mCollectionRect;
    private ViewGroup mCenterRect;
    private ViewGroup mCollectionLayout; // 历史收藏layout
    private ViewGroup mPersonCenterLayout; // 个人中心
    private TelescopicWrap mCollectionTel; // 历史收藏伸缩包装类
    private TelescopicWrap mPersonCenterTel; // 个人中心包装类
    private BitmapDecoder mBitmapDecoder;
    private int mLastSelectedIndex = -1; // 记录上一次选中的位置
    private TimeTickBroadcast mTimeTickBroadcast = null;

/*add by dragontec for bug 4230 start*/
    private RecyclerImageView mPersonCollectionImg;
    private RecyclerImageView mPersonCenterImg;
/*add by dragontec for bug 4230 end*/

    private RecyclerImageView left_image, right_image; // 导航左右遮罩
    private Runnable mRunnable =
            new Runnable() {
                @Override
                public void run() {
                    showLoginHint();
                    registerConnectionReceiver();
                }
            };
    private long currentTime = 0;
    /*add by dragontec for bug 3983 start*/
    public static boolean isTitleHidden = false;
    private Object mTitleAnimLock = new Object();
    private ValueAnimator mTitleMoveOutAnimator;
    private ValueAnimator mTitleMoveInAnimator;
    private boolean isAnimationPlaying;
    /*add by dragontec for bug 3983 end*/
    /*modify by dragontec for bug 4350 start*/
    public HomePageArrowButton banner_arrow_up;
    public HomePageArrowButton banner_arrow_down;
    /*modify by dragontec for bug 4350 end*/

    //广告
    private static final int MSG_AD_COUNTDOWN = 0x01;

    /*add by dragontec for bug 3983 start*/
    private final int TITLE_ANIM_DURATION = 150;
    /*add by dragontec for bug 3983 end*/
/*add by dragontec for bug 4225, 4224, 4223 start*/
//    private final int SCROLL_TO_TOP_TOAST_DURATION = 600;
    private final int UP_KEY_LONG_PRESS_DURATION = 1000;
    private boolean mNeedShowScrollToTopTip = false;
    private boolean mAtScrollerBottom = false;
//    private long scrollTipCurrentTime = 0;
/*add by dragontec for bug 4225, 4224, 4223 end*/
/*add by dragontec for bug 4249 start*/
    private boolean mInAdvertisement = false;
/*add by dragontec for bug 4249 end*/
/*add by dragontec for bug 4259 start*/
    private boolean mRequestBannerFocusInterrupt = false;
    private final int RequestBannerFocusDelay = 500;
    private Handler mRequestBannerFocusHandler = null;
    private RequestFocusBannerRunnable mRequestBannerFocusRunnable = null;
/*add by dragontec for bug 4259 end*/

    private DaisyVideoView mVideoView;
    private RecyclerImageView mPicImg;
    private SeekBar mSeekBar;

    private int currentImageAdCountDown = 0;
    private boolean isStartImageCountDown = false;
    private List<AdvertiseTable> mAdsList;
    private AdvertiseManager mAdvertiseManager;
    private Advertisement mAdvertisement;
    private int mPlayIndex;
    private boolean mIsPlayingVideo = false;
    private int mCountAdTime = 0;
    private int mTotleTime = 0;
    private String fromPage="";
    private Button timeBtn;
    private RelativeLayout ad_layout;
    private LinearLayout home_layout;

    public RelativeLayout loading_layout;
    public ImageView loading_progress;

    private View currentFocus;

    private boolean isPaused;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AD_COUNTDOWN:
                    if (!mIsPlayingVideo && mCountAdTime == 0) {
                        mHandler.removeMessages(MSG_AD_COUNTDOWN);
                        mSeekBar.setProgress(mTotleTime);
                        go2HomeActivity();
                        return;
                    }
                    mSeekBar.setProgress(mTotleTime - mCountAdTime);
                    if (timeBtn.getVisibility() != View.VISIBLE) {
                        timeBtn.setVisibility(View.VISIBLE);
                    }
                    timeBtn.setTextColor(Color.WHITE);
                    timeBtn.setText(mCountAdTime + "s");
                    int refreshTime;
                    if (!mIsPlayingVideo) {
                        refreshTime = 1000;
                        if (currentImageAdCountDown == 0 && !isStartImageCountDown) {
                            currentImageAdCountDown = mAdsList.get(mPlayIndex).duration;
                            isStartImageCountDown = true;
                        } else {
                            if (currentImageAdCountDown == 0) {
                                mPlayIndex += 1;
                                playLaunchAd(mPlayIndex);
                                isStartImageCountDown = false;
                            } else {
                                currentImageAdCountDown--;
                            }
                        }
                        mCountAdTime--;
//                        if(mCountAdTime <= 0){
//                            go2HomeActivity();
//                        }
                    } else {
                        refreshTime = 500;
                        mCountAdTime = getAdCountDownTime();
                    }
                    sendEmptyMessageDelayed(MSG_AD_COUNTDOWN, refreshTime);
                    break;
            }
        }
    };

    private String homepageTemplate;
    private String homepageUrl;
	/*add by dragontec for bug 4294 start*/
    private LinearLayout mHeadLayout;
	/*add by dragontec for bug 4294 end*/
    private boolean isKeyDown = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(null);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        View contentview = LayoutInflater.from(this).inflate(R.layout.home_activity_layout, null);
        setContentView(contentview);
        homepageTemplate = getIntent().getStringExtra("homepage_template");
        homepageUrl = getIntent().getStringExtra("homepage_url");
        fromPage=getIntent().getStringExtra("fromPage");
        systemInit();
        findViews();
        initListener();
/*add by dragontec for bug 4259 start*/
        initHandlerAndRunnable();
/*add by dragontec for bug 4259 end*/
        initAd();
        initData();
        //        contentview.getViewTreeObserver().addOnGlobalFocusChangeListener(new
        // ViewTreeObserver.OnGlobalFocusChangeListener() {
        //            @Override
        //            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        //                if(newFocus!=null){
        //                    Log.i("collection",newFocus.toString());
        //                }
        //            }
        //        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mTimeTv.setText(mHomeControl.getNowTime());
        isKeyDown = false;
//        if (mLastSelectedIndex == 0) {
//            mChannelTab.setDefaultSelection(1);
//        }
		if (mLastSelectedIndex != -1) {
			mChannelTab.setDefaultSelection(mLastSelectedIndex);
		}
		if (currentFocus != null) {
			currentFocus.requestFocus();
			currentFocus = null;
		}
		isPaused = false;
    }

    @Override
    protected void onPause() {
		isPaused = true;
		currentFocus = getCurrentFocus();
		if (currentFocus != null && mChannelTab != null) {
            Object tag = currentFocus.getTag(mChannelTab.getId());
            if (tag != null && tag instanceof String && tag.equals("tab")) {
                currentFocus = null;
            }
        }
		mFetchDataControl.stop();
		super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //        HomeActivity.super.onBackPressed();
        //        android.os.Process.killProcess(android.os.Process.myPid());
        //        System.exit(0);
        if(mHandler!=null && mHandler.hasMessages(MSG_AD_COUNTDOWN)){
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
            mHandler = null;
        }
    }

    @Override
    protected void onDestroy() {
/*add by dragontec for bug 4205 start*/
        if (banner_arrow_up != null) {
            banner_arrow_up.setOnClickListener(null);
        }
        if (banner_arrow_down != null) {
            banner_arrow_down.setOnClickListener(null);
        }
        mAdvertisement = null;
		mHomeControl.clear();
		mFetchDataControl.clear();
		if (mChannelTab != null) {
            mChannelTab.leftbtn = null;
            mChannelTab.rightbtn = null;
            mChannelTab.removeAllViews();
        }
/*add by dragontec for bug 4205 end*/
/*add by dragontec for bug 4259 start*/
        if (mRequestBannerFocusHandler != null
                && mRequestBannerFocusRunnable != null) {
            mRequestBannerFocusHandler.removeCallbacks(mRequestBannerFocusRunnable);
        }
        mRequestBannerFocusHandler = null;
        mRequestBannerFocusRunnable = null;
/*add by dragontec for bug 4259 end*/
        /*add by dragontec for bug 3983 start*/
        uninitTitleAnim();
        /*add by dragontec for bug 3983 end*/
        unregisterReceiver(mTimeTickBroadcast);
        mTimeTickBroadcast = null;
/*add by dragontec for bug 4205 start*/
        if (mVideoView != null) {
            mVideoView.release(true);
        }
/*add by dragontec for bug 4205 end*/
/*delete by dragontec for bug 4205 start*/
//        RefWatcher refWatcher = VodApplication.getRefWatcher(this);
//        refWatcher.watch(this);
/*delete by dragontec for bug 4205 end*/
		super.onDestroy();
    }

    private void initServer(){
        startAdsService();
        startIntervalActive();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    /*初始化一些系统参数*/
    private void systemInit() {
        try {
            System.setProperty("http.keepAlive", "false");
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHomeControl.startTrueTimeService();
    }

    /*获取控件实例*/
    private void findViews() {
        ad_layout= (RelativeLayout) findViewById(R.id.advertisement);
        home_layout= (LinearLayout) findViewById(R.id.home_page);
        home_layout.setVisibility(View.GONE);
        mHoverView = (HomeRootRelativeLayout) findViewById(R.id.home_view_layout);
		/*add by dragontec for bug 4294 start*/
        mHeadLayout = (LinearLayout) findViewById(R.id.head_layout);
		/*add by dragontec for bug 4294 end*/
/*delete by dragontec for bug 4057 start*/
//        headHoverd.setOnHoverListener(this);
//        mHoverView.setOnHoverListener(this);
/*delete by dragontec for bug 4057 end*/
        mViewGroup = (ViewGroup) findViewById(R.id.home_view_layout);
        mChannelTab = (HorizontalTabView) findViewById(R.id.channel_tab);
        mTimeTv = (TextView) findViewById(R.id.guide_title_time_tv);
        mCollectionTv = (TextView) findViewById(R.id.collection_tv);
        mPersonCenterTv = (TextView) findViewById(R.id.center_tv);
        mCollectionRect = (ViewGroup) findViewById(R.id.collection_rect_layout);
        mCenterRect = (ViewGroup) findViewById(R.id.center_rect_layout);
        mCollectionLayout = (ViewGroup) findViewById(R.id.collection_layout);
        mPersonCenterLayout = (ViewGroup) findViewById(R.id.center_layout);
        mCollectionTel = new TelescopicWrap(this, mCollectionLayout);
        mCollectionTel.setTextView(mCollectionTv);
        mPersonCenterTel = new TelescopicWrap(this, mPersonCenterLayout);
        mPersonCenterTel.setTextView(mPersonCenterTv);
        mHoverView.setFocusableInTouchMode(true);
        mHoverView.setFocusable(true);
        right_image = (RecyclerImageView) findViewById(R.id.guide_tab_right);
        left_image = (RecyclerImageView) findViewById(R.id.guide_tab_left);
        mChannelTab.leftbtn = left_image;
        mChannelTab.rightbtn = right_image;

/*add by dragontec for bug 4230 start*/
        mPersonCollectionImg = (RecyclerImageView) findViewById(R.id.person_collection_img);
        mPersonCenterImg = (RecyclerImageView) findViewById(R.id.person_center_img);
/*add by dragontec for bug 4230 end*/
		/*add by dragontec for bug 4368 start*/
        mCollectionTel.setIcon(mPersonCollectionImg);
        mPersonCenterTel.setIcon(mPersonCenterImg);
		/*add by dragontec for bug 4368 end*/
        //广告
        mVideoView = (DaisyVideoView) findViewById(R.id.home_ad_video);
        mPicImg = (RecyclerImageView) findViewById(R.id.home_ad_pic);
        mSeekBar = (SeekBar) findViewById(R.id.home_ad_seekbar);
        timeBtn= (Button) findViewById(R.id.home_ad_timer);

        banner_arrow_up = findView(R.id.banner_arrow_up);
        banner_arrow_down = findView(R.id.banner_arrow_down);
        /*modify by dragontec for bug 4350 start*/
//        banner_arrow_up.setOnHoverListener(this);
//        banner_arrow_down.setOnHoverListener(this);
		/*modify by dragontec for bug 4350 end*/
        mHoverView.setUpArrow(banner_arrow_up);
        mHoverView.setDownArrow(banner_arrow_down);
        mHoverView.setShowUp(false);
        mHoverView.setShowDown(false);

		loading_layout = (RelativeLayout) findViewById(R.id.loading_layout);
		loading_progress = (ImageView) findViewById(R.id.loading_progress);
    }

    private void setBackground(int id) {
        mBitmapDecoder = new BitmapDecoder();
        mBitmapDecoder.decode(
                this,
                id,
                new BitmapDecoder.Callback() {
                    @Override
                    public void onSuccess(BitmapDrawable bitmapDrawable) {
/*add by dragontec for bug 4205 start*/
                        mViewGroup.setBackground(null);
/*add by dragontec for bug 4205 end*/
                        mViewGroup.setBackground(bitmapDrawable);
                        mBitmapDecoder = null;
                    }
                });
    }

    private void initListener() {
/*add by dragontec for bug 4057 start*/
        mCollectionRect.setFocusableInTouchMode(true);
        mCollectionRect.setFocusable(true);
        mCenterRect.setFocusableInTouchMode(true);
        mCenterRect.setFocusable(true);
/*add by dragontec for bug 4057 end*/
        mCenterRect.setOnFocusChangeListener(this);
        mCenterRect.setOnClickListener(this);
        mCollectionRect.setOnFocusChangeListener(this);
        mCollectionRect.setOnClickListener(this);

/*add by dragontec for bug 4230 start*/
        mPersonCollectionImg.setOnHoverListener(this);
        mPersonCenterImg.setOnHoverListener(this);
/*add by dragontec for bug 4230 end*/

        mHomeControl.setChannelChange(mChannelTab);
        mCollectionRect.setOnHoverListener(this);
        mCenterRect.setOnHoverListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        mTimeTickBroadcast = new TimeTickBroadcast();
        registerReceiver(mTimeTickBroadcast, filter);
        /*add by dragontec for bug 3983 start*/
        initTitleAnim();
        /*add by dragontec for bug 3983 end*/
		//广告部分
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mSeekBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return true;
            }
        });    }

    private void initData() {
		/*add by dragontec for bug 3983 start 画面退出后由于是静态变量，所以需要赋初值*/
        isTitleHidden = false;
		/*add by dragontec for bug 3983 end*/
        mFetchDataControl.fetchChannels();
    }

    private void initAd(){
/*add by dragontec for bug 4249 start*/
        mInAdvertisement = true;
/*add by dragontec for bug 4249 end*/
        mAdvertiseManager = new AdvertiseManager(this);
        mAdsList = mAdvertiseManager.getAppLaunchAdvertisement();
        mAdvertisement = new Advertisement(this);
        for (AdvertiseTable tab : mAdsList) {
            totalAdsMills = totalAdsMills + tab.duration * 1000;
        }
        for (AdvertiseTable adTable : mAdsList) {
            int duration = adTable.duration;
            mCountAdTime += duration;
        }
        mSeekBar.setMax(mCountAdTime);
        mTotleTime=mCountAdTime;
        if(fromPage==null||!fromPage.equals("launcher")) {
            playLaunchAd(0);
        }else {
            go2HomeActivity();
        }
    }

    private void replaceFragment(Fragment fragment, String scrollType) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (scrollType) {
            case "left":
                transaction.setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out);
                break;
            case "right":
                transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                break;
        }

        transaction.replace(R.id.fragment_layout, fragment, scrollType).commitAllowingStateLoss();
    }

    private void fillChannelTab(ChannelEntity[] channelEntities) {
        int defaultSelectPosition = 1;
        List<HorizontalTabView.Tab> tabs = new ArrayList<>();
        HorizontalTabView.Tab searchTab = new HorizontalTabView.Tab("", "搜索");
        tabs.add(searchTab);
        HorizontalTabView.Tab homepageTab = new HorizontalTabView.Tab("", "首页");
        tabs.add(homepageTab);
        if (!TextUtils.isEmpty(homepageUrl) && homepageUrl.equals("/api/tv/homepage/top")) {
            defaultSelectPosition = 1;
        }
        for (int i = 0; i < channelEntities.length; i++) {
            ChannelEntity channelEntity = channelEntities[i];
            if (!TextUtils.isEmpty(homepageUrl) && !TextUtils.isEmpty(channelEntity.homepage_url)
                    && channelEntity.homepage_url.contains(homepageUrl)) {
                defaultSelectPosition = i + 2;
            }
            HorizontalTabView.Tab tab = new HorizontalTabView.Tab("", channelEntity.name);
            tabs.add(tab);
        }
        callBack( TAB_CHANGE_FALG, defaultSelectPosition);
        Log.d(TAG, "channel default position: " + defaultSelectPosition);
        mChannelTab.addAllViews(tabs, defaultSelectPosition);
    }

	/*add by dragontec for bug 3983 start 当动画执行过程中不响应按键*/
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(isAnimationPlaying ||(getChannelFragment() != null && !getChannelFragment().isUpdateQueueEmpty())){
            return true;
        }
/*add by dragontec for bug 4259 start*/
        if (!mRequestBannerFocusInterrupt) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN)
                    && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP
                            || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN
                            || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
                            || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                stopRequestFocusBanner();
            }
        }
/*end by dragontec for bug 4259 end*/


        if(event.getAction() == KeyEvent.ACTION_DOWN){
            isKeyDown = true;
        }else  if(event.getAction() == KeyEvent.ACTION_UP){
            if(isKeyDown){
                isKeyDown = false;
            }else{
            	//temp solution, because we can not get key action down in some situation
				if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
					ChannelFragment fragment = getChannelFragment();
					if (fragment != null) {
						fragment.requestFocus();
					}
				}
				Log.d(TAG, "dispatchKeyEvent key up but not down");
                return true;
            }
        }
/*add by dragontec for bug 4225, 4224, 4223 start*/
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP) {
/*add by dragontec for bug 4249 start*/
            if (mInAdvertisement) {
                isCheckoutUpdate = true;
                SkyService.ServiceManager.executeActive = true;
                CallaPlay callaPlay = new CallaPlay();
                callaPlay.app_exit(TrueTime.now().getTime() - app_start_time, SimpleRestClient.appVersion);
                ArrayList<String> cache_log = MessageQueue.getQueueList();
                HashSet<String> hasset_log = new HashSet<String>();
                for (int i = 0; i < cache_log.size(); i++) {
                    hasset_log.add(cache_log.get(i));
                }
                DaisyUtils.getVodApplication(HomeActivity.this)
                        .getEditor()
                        .putStringSet(VodApplication.CACHED_LOG, hasset_log);
                DaisyUtils.getVodApplication(getApplicationContext()).save();
                BaseActivity.baseChannel = "";
                BaseActivity.baseSection = "";
                stopService(new Intent(HomeActivity.this, PlaybackService.class));
                HomeActivity.super.onBackPressed();
            } else
/*add by dragontec for bug 4249 end*/
            if (isScrollerAtTop()) {
                if (currentTime == 0 || System.currentTimeMillis() - currentTime > 4000) {
                    currentTime = System.currentTimeMillis();
                    ToastTip.showToast(this, "再次点击返回按键，退出应用");
                } else {
                    isCheckoutUpdate = true;
                    SkyService.ServiceManager.executeActive = true;
                    CallaPlay callaPlay = new CallaPlay();
                    callaPlay.app_exit(TrueTime.now().getTime() - app_start_time, SimpleRestClient.appVersion);
                    ArrayList<String> cache_log = MessageQueue.getQueueList();
                    HashSet<String> hasset_log = new HashSet<String>();
                    for (int i = 0; i < cache_log.size(); i++) {
                        hasset_log.add(cache_log.get(i));
                    }
                    DaisyUtils.getVodApplication(HomeActivity.this)
                            .getEditor()
                            .putStringSet(VodApplication.CACHED_LOG, hasset_log);
                    DaisyUtils.getVodApplication(getApplicationContext()).save();
                    BaseActivity.baseChannel = "";
                    BaseActivity.baseSection = "";
                    stopService(new Intent(HomeActivity.this, PlaybackService.class));
                    HomeActivity.super.onBackPressed();
                }
            } else {
//                long current = System.currentTimeMillis();
//                long delta = current - scrollTipCurrentTime;
//                if (scrollTipCurrentTime == 0 || delta > SCROLL_TO_TOP_TOAST_DURATION) {
//                    scrollTipCurrentTime = current;
//                } else {
//                    scrollTipCurrentTime = current;
                    //channelFragment存在，才能向上滑动到顶的场合下，才需要显示顶部title以及请求focus
                    ChannelFragment channelFragment = getChannelFragment();
                    if (channelFragment != null && channelFragment.scrollerScrollToTop()) {
                        mHoverView.setShowUp(false);
                        mHoverView.setShowDown(true);
                        titleMoveIn();
                        mChannelTab.requestLastFocus();
                    }
//                }
            }
            return true;
        }
/*add by dragontec for bug 4225, 4224, 4223 end*/
        return super.dispatchKeyEvent(event);
    }
	/*add by dragontec for bug 3983 end*/
    @Override
    public void onClick(View v) {
        PageIntent pageIntent = new PageIntent();
        if (v == mCollectionRect) {
            pageIntent.toHistory(this, "homePage");
        } else if (v == mCenterRect) {
            pageIntent.toUserCenter(this);
        }
    }

    @Override
    public boolean onHover(final View v, MotionEvent event) {
        //        if(mCenterRect == v){
        //            mPersonCenterLayout.setFocusable(true);
        //            mPersonCenterLayout.requestFocus();
        //        }
        //        if(mCollectionRect == v){
        //            mCollectionLayout.setFocusable(true);
        //            mCollectionLayout.requestFocus();
        //        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
			/*delete by dragontec for bug 4169 start*/
        	//case MotionEvent.ACTION_HOVER_MOVE:
			/*delete by dragontec for bug 4169 end*/
/*modify by dragontec for bug 4057 start*/
//                if (v.getId() != R.id.hover_view) {
//                    if (!v.hasFocus()) {
//                        new Handler()
//                                .postDelayed(
//                                        new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                v.setFocusable(true);
//                                                v.setFocusableInTouchMode(true);
//                                                v.requestFocusFromTouch();
//                                                v.requestFocus();
//                                            }
//                                        },
//                                        200);
//                    }
//                } else {
//                    v.setFocusable(true);
//                    v.setFocusableInTouchMode(true);
//                    v.requestFocusFromTouch();
//                    v.requestFocus();
//                }
/*modify by dragontec for bug 4230 start*/
//                if(!v.hasFocus()) {
//                    v.requestFocusFromTouch();
//                    v.requestFocus();
//                }
                if(!v.hasFocus()) {
                    if (v.getId() == R.id.person_collection_img) {
                        if (mCollectionRect != null) {
                            mCollectionRect.requestFocusFromTouch();
                            mCollectionRect.requestFocus();
                        }
                    } else if (v.getId() == R.id.person_center_img) {
                        if (mCenterRect != null) {
                            mCenterRect.requestFocusFromTouch();
                            mCenterRect.requestFocus();
                        }
                    } else if (v.getId() == R.id.collection_rect_layout || v.getId() == R.id.center_rect_layout) {
                        //do nothing
					/*modify by dragontec for bug 4350 start*/
                    } else if (v == banner_arrow_up || v == banner_arrow_down) {
                    	float margin = v.getResources().getDimensionPixelSize(R.dimen.home_page_banner_arrow_hover_margin) / v.getResources().getDisplayMetrics().density;
                    	if (event.getX() >= margin && event.getX() <= v.getResources().getDisplayMetrics().widthPixels - margin) {
                    		v.requestFocusFromTouch();
                    		v.requestFocus();
						}
					} else {
                    /*modify by dragontec for bug 4350 end*/
                        v.requestFocusFromTouch();
                        v.requestFocus();
                    }
/*modify by dragontec for bug 4230 end*/
                }
/*modify by dragontec for bug 4057 end*/
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                //                onFocusChange(v,  false);
/*add by dragontec for bug 4057 start*/
                if (event.getButtonState() != MotionEvent.BUTTON_PRIMARY) {
/*modify by dragontec for bug 4230 start*/
//                    v.clearFocus();
                    if (v.getId() == R.id.person_collection_img || v.getId() == R.id.person_center_img) {
                        //do nothing
                    } else if (v.getId() == R.id.collection_rect_layout || v.getId() == R.id.center_rect_layout) {
                        v.clearFocus();
                    } else {
                        v.clearFocus();
                    }
/*modify by dragontec for bug 4230 end*/
                }
/*add by dragontec for bug 4057 end*/
                break;
            /*modify by dragontec for bug 4350 start*/
			case MotionEvent.ACTION_HOVER_MOVE:
				if (v == banner_arrow_up || v == banner_arrow_down) {
					float margin = v.getResources().getDimensionPixelSize(R.dimen.home_page_banner_arrow_hover_margin) / v.getResources().getDisplayMetrics().density;
					if (event.getX() >= margin && event.getX() <= v.getResources().getDisplayMetrics().widthPixels - margin) {
						v.requestFocusFromTouch();
						v.requestFocus();
					} else {
						v.clearFocus();
					}
				}
				break;
			/*modify by dragontec for bug 4350 end*/
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.i("favorite", "focus : " + hasFocus);
        if (v == mCollectionRect) { // 历史收藏伸缩处理
            mCollectionTel.openOrClose(hasFocus);
/*add by dragontec for bug 4356 start*/
            if (hasFocus) {
                v.bringToFront();
            }
/*add by dragontec for bug 4356 end*/
            return;
        }
        if (v == mCenterRect) { // 个人中心伸缩处理
            mPersonCenterTel.openOrClose(hasFocus);
/*add by dragontec for bug 4356 start*/
            if (hasFocus) {
                v.bringToFront();
            }
/*add by dragontec for bug 4356 end*/
            return;
        }
    }

    @Override
    public void callBack(int flags, Object... args) {
        if (flags == FETCH_CHANNEL_TAB_FLAG) {
            ChannelEntity[] channelEntities = (ChannelEntity[]) args;
            fillChannelTab(channelEntities);
        }
        if (flags == TAB_CHANGE_FALG) { // 频道切换
            int position = (int) args[0];
            Log.d(TAG, "callback position:" + position);
            Log.d(TAG, "mChannels: " + mFetchDataControl.mChannels );
            if (mFetchDataControl.mChannels != null && mFetchDataControl.mChannels.length + 2  > position && position != mLastSelectedIndex) {
                ChannelFragment channelFragment = new ChannelFragment();
                switch (position) {
                    case 0: // 搜索
//                        mLastSelectedIndex = position;
						if (!isPaused) {
							setBackground(R.drawable.homepage_background);
							PageIntent intent = new PageIntent();
							intent.toSearch(this);
							isPaused = true;
						}
                        return;
                    case 1: // 首页
                        setBackground(R.drawable.homepage_background);
                        channelFragment.setChannel("首页", HOME_PAGE_CHANNEL_TAG, "首页", 0);
                        break;
                    default: // 其他频道
                        if (position - 2 < 0) return;
                        if (mFetchDataControl.mChannels[position - 2].channel.equals("comic")) {
							/*add by dragontec for bug 4346 start*/
                            setBackground(R.drawable.channel_child_bg);
                            right_image.post(new Runnable() {
                                @Override
                                public void run() {
                                    right_image.setBackground(null);
                                    right_image.setBackgroundResource(R.drawable.guide_right_child_arrow);
                                }
                            });
                            left_image.post(new Runnable() {
                                @Override
                                public void run() {
                                    left_image.setBackground(null);
                                    left_image.setBackgroundResource(R.drawable.guide_left_child_arrow);
                                }
                            });
							/*add by dragontec for bug 4346 end*/
                        } else {
                            setBackground(R.drawable.homepage_background);
							/*add by dragontec for bug 4346 start*/
                            right_image.post(new Runnable() {
                                @Override
                                public void run() {
                                    right_image.setBackground(null);
                                    right_image.setBackgroundResource(R.drawable.guide_right_arrow);
                                }
                            });
                            left_image.post(new Runnable() {
                                @Override
                                public void run() {
                                    left_image.setBackground(null);
                                    left_image.setBackgroundResource(R.drawable.guide_left_arrow);
                                }
                            });
							/*add by dragontec for bug 4346 end*/
                        }
                        channelFragment.setChannel(
                                mFetchDataControl.mChannels[position - 2].name,
                                mFetchDataControl.mChannels[position - 2].channel,
                                mFetchDataControl.mChannels[position - 2].name,
                                mFetchDataControl.mChannels[position - 2].style);
                        break;
                }
                if (position > mLastSelectedIndex) { // 右切
                    replaceFragment(channelFragment, "right");
                }
                if (position < mLastSelectedIndex) { // 左切
                    replaceFragment(channelFragment, "left");
                }
                mLastSelectedIndex = position;
            }
        }
    }

/*add by dragontec for bug 4225, 4224, 4223 start*/
    private ChannelFragment getChannelFragment() {
        ChannelFragment channelFragment = null;
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_layout);
        if (fragment != null && fragment instanceof ChannelFragment) {
            channelFragment = (ChannelFragment) fragment;
        }
        return channelFragment;
    }

    private boolean isScrollerAtTop() {
		/*modify by dragontec for bug 4376 start*/	
        boolean isScrollerAtTop = true;
		/*modify by dragontec for bug 4376 end*/	
        ChannelFragment channelFragment = getChannelFragment();
        if (channelFragment != null) {
            isScrollerAtTop = channelFragment.isScrollerAtTop();
        }
        return isScrollerAtTop;
    }
/*add by dragontec for bug 4225, 4224, 4223 end*/

    @Override
    public void onBackPressed() {
/*delete by dragontec for bug 4225, 4224, 4223 start*/
    //在有些场合下，迅速双击返回键不会进入到onBackPressed（疑似被劫持了），所以暂定放在dispatchKeyEvent中进行处理
//        if (currentTime == 0 || System.currentTimeMillis() - currentTime > 4000) {
//            currentTime = System.currentTimeMillis();
//            ToastTip.showToast(this, "再次点击返回按键，退出应用");
//        } else {
//            isCheckoutUpdate = true;
//            SkyService.ServiceManager.executeActive = true;
//            CallaPlay callaPlay = new CallaPlay();
//            callaPlay.app_exit(TrueTime.now().getTime() - app_start_time, SimpleRestClient.appVersion);
//            ArrayList<String> cache_log = MessageQueue.getQueueList();
//            HashSet<String> hasset_log = new HashSet<String>();
//            for (int i = 0; i < cache_log.size(); i++) {
//                hasset_log.add(cache_log.get(i));
//            }
//            DaisyUtils.getVodApplication(HomeActivity.this)
//                    .getEditor()
//                    .putStringSet(VodApplication.CACHED_LOG, hasset_log);
//            DaisyUtils.getVodApplication(getApplicationContext()).save();
//            BaseActivity.baseChannel = "";
//            BaseActivity.baseSection = "";
//            stopService(new Intent(HomeActivity.this, PlaybackService.class));
//            HomeActivity.super.onBackPressed();
//        }
/*delete by dragontec for bug 4225, 4224, 4223 start*/
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mLastFocusView != null && mHoverView != null && mHoverView.hasFocus()) {
            mLastFocusView.requestFocus();
            mLastFocusView.requestFocusFromTouch();
            //            mHoverView.setFocusable(false);
            //            mHoverView.setFocusableInTouchMode(false);
            return true;
        }
/*delete by dragontec for bug 4057 start*/
//        mHoverView.setFocusable(false);
//        mHoverView.setFocusableInTouchMode(false);
//        headHoverd.setFocusableInTouchMode(false);
//        headHoverd.setFocusable(false);
/*delete by dragontec for bug 4057 end*/
/*add by dragontec for bug 4225, 4224, 4223 start*/
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP && !isScrollerAtTop()) {
            long keyDownDuration = event.getEventTime() - event.getDownTime();
            if (keyDownDuration > UP_KEY_LONG_PRESS_DURATION) {
                if (mNeedShowScrollToTopTip) {
                    //already show scroll to top tip, do nothing
                } else {
                    mNeedShowScrollToTopTip = true;
                    showScrollToTopTip();
                }
            }
        }
/*add by dragontec for bug 4225, 4224, 4223 end*/

        ChannelFragment channelFragment = (ChannelFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_layout);
        if (channelFragment != null){
			channelFragment.onKeyDown(keyCode, event);
        }

        return super.onKeyDown(keyCode, event);
    }

/*add by dragontec for bug 4225, 4224, 4223 start*/
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            mNeedShowScrollToTopTip = false;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void actionScrollerMoveToBottom(boolean bottom) {
        if (bottom) {
            if (!mAtScrollerBottom) {
                mAtScrollerBottom = true;
                showScrollToTopTip();
                mHoverView.setShowDown(false);
                banner_arrow_down.setVisibility(View.GONE);
            }
        } else {
            mAtScrollerBottom = bottom;
			/*modify by dragontec for bug 4339 start*/
            mHoverView.setShowDown(true);
        }
        mHoverView.updateUpDownArrow();
		/*modify by dragontec for bug 4339 end*/
    }

    public void showScrollToTopTip() {
        ToastTip.showToast(this, getResources().getString(R.string.click_back_to_top));
    }
/*add by dragontec for bug 4225, 4224, 4223 end*/

    /*add by dragontec for bug 3983 start*/
    private void initTitleAnim(){
        int height = getResources().getDimensionPixelSize(R.dimen.banner_margin_top);
        TitleAnimUpdateListener titleAnimUpdateListener = new TitleAnimUpdateListener();
        TitleAnimStateListener titleAnimStateListener = new TitleAnimStateListener();

        mTitleMoveOutAnimator = ValueAnimator.ofInt(0, -height);
        mTitleMoveOutAnimator.setDuration(TITLE_ANIM_DURATION);
        mTitleMoveOutAnimator.setTarget(home_layout);
        mTitleMoveOutAnimator.addUpdateListener(titleAnimUpdateListener);
        mTitleMoveOutAnimator.addListener(titleAnimStateListener);

        mTitleMoveInAnimator = ValueAnimator.ofInt(-height, 0);
        mTitleMoveInAnimator.setDuration(TITLE_ANIM_DURATION);
        mTitleMoveInAnimator.setTarget(home_layout);
        mTitleMoveInAnimator.addUpdateListener(titleAnimUpdateListener);
        mTitleMoveInAnimator.addListener(titleAnimStateListener);
    }

    private void uninitTitleAnim(){
        if(mTitleMoveOutAnimator != null){
            mTitleMoveOutAnimator.removeAllListeners();
            mTitleMoveOutAnimator.removeAllUpdateListeners();
        }
        if(mTitleMoveInAnimator != null){
            mTitleMoveInAnimator.removeAllListeners();
            mTitleMoveInAnimator.removeAllUpdateListeners();
        }
    }
    public void titleMoveOut() {
        synchronized (mTitleAnimLock) {
            if (isTitleHidden) {
                return;
            }
            if(mTitleMoveOutAnimator != null) {
                mTitleMoveOutAnimator.start();
            }
            isTitleHidden = true;
        }
    }

    public void titleMoveIn() {
        synchronized (mTitleAnimLock) {
            if (!isTitleHidden) {
                return;
            }
            if(mTitleMoveInAnimator != null) {
                mTitleMoveInAnimator.start();
            }
            isTitleHidden = false;
        }
    }

    private class TitleAnimUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            int animatorValue = (int) animation.getAnimatedValue();
            home_layout.setTranslationY(animatorValue);
        }
    }

    private class TitleAnimStateListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {
            isAnimationPlaying = true;
			/*add by dragontec for bug 4294 start*/
            if(animation == mTitleMoveInAnimator){
                mHeadLayout.setVisibility(View.VISIBLE);
                int height = getResources().getDimensionPixelSize(R.dimen.banner_margin_top);
                home_layout.setTranslationY(-height);
            }
			/*add by dragontec for bug 4294 end*/
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            isAnimationPlaying = false;
			/*add by dragontec for bug 4294 start*/
            if(animation == mTitleMoveOutAnimator){
                mHeadLayout.setVisibility(View.GONE);
                home_layout.setTranslationY(0);
            }
			/*add by dragontec for bug 4294 end*/
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
    /*add by dragontec for bug 3983 end*/

    /*时间跳动广播*/
    private class TimeTickBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mTimeTv.setText(mHomeControl.getNowTime());
        }
    }

    //广告
    private void playLaunchAd(final int index) {
        if(index >= mAdsList.size()){
            return;
        }
        mPlayIndex = index;
        if (!mAdsList.get(index).location.equals(AdvertiseManager.DEFAULT_ADV_PICTURE)) {
            new CallaPlay().boot_ad_play(mAdsList.get(index).title, mAdsList.get(index).media_id,
                    mAdsList.get(index).media_url, String.valueOf(mAdsList.get(index).duration));
        }
        if (mAdsList.get(index).media_type.equals(AdvertiseManager.TYPE_VIDEO)) {
            mIsPlayingVideo = true;
        }
        String path="file://" + getFilesDir() + "/" + AdvertiseManager.AD_DIR + "/" +mAdsList.get(index).location;
        if (mIsPlayingVideo) {
            if (mVideoView.getVisibility() != View.VISIBLE) {
                mPicImg.setVisibility(View.GONE);
                mVideoView.setVisibility(View.VISIBLE);
            }
            mVideoView.setVideoPath(path);
            mVideoView.setOnPreparedListener(this);
            mVideoView.setOnCompletionListener(this);
            mVideoView.setOnErrorListener(this);
        } else {
            if (mPicImg.getVisibility() != View.VISIBLE) {
                mVideoView.setVisibility(View.GONE);
                mPicImg.setVisibility(View.VISIBLE);
                //  mSeekBar.setVisibility(View.VISIBLE);
            }

            Picasso.with(this)
                    .load(path)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_CACHE)
                    .into(mPicImg, new Callback() {
                        @Override
                        public void onSuccess() {//图片加载成功启动倒计时
                            if (mPlayIndex == 0) {
                                mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
                            }
                            if (mAdsList.get(mPlayIndex).media_id != null) {
                                int media_id = Integer.parseInt(mAdsList.get(mPlayIndex).media_id);
                                mAdvertisement.getRepostAdUrl(media_id, "startAd");
                            }
                        }

                        @Override
                        public void onError(Exception e) {//图片加载失败启动倒计时
                            Picasso.with(HomeActivity.this)
                                    .load("file:///android_asset/posters.png")
                                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_CACHE)
                                    .into(mPicImg);
                            if (mPlayIndex == 0) {
                                mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
                            }
                        }
                    });
        }
    }

    private int getAdCountDownTime() {
        if (mAdsList == null || mAdsList.isEmpty() || !mIsPlayingVideo) {
            return 0;
        }
        int totalAdTime = 0;
        int currentAd = mPlayIndex;
        if (currentAd == mAdsList.size() - 1) {
            totalAdTime = mAdsList.get(mAdsList.size() - 1).duration;
        } else {
            for (int i = currentAd; i < mAdsList.size(); i++) {
                totalAdTime += mAdsList.get(i).duration;
            }
        }
        int countTime = totalAdTime - mVideoView.getCurrentPosition() / 1000 - 1;
        if (countTime < 0) {
            countTime = 0;
        }
        return countTime;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mVideoView.start();
        if (!mHandler.hasMessages(MSG_AD_COUNTDOWN)) {//开始播放，启动倒计时
            mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
        }
        if (mAdsList.get(mPlayIndex).media_id != null) {
            int media_id = Integer.parseInt(mAdsList.get(mPlayIndex).media_id);
            mAdvertisement.getRepostAdUrl(media_id, "startAd");
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {//广告视频播放完成
        if(!playNextVideo()){
            go2HomeActivity();
        }
    }

    /**
     * 播放下一个视频或跳转
     * @return false跳转到首页
     */
    private boolean playNextVideo(){
        if (mPlayIndex == mAdsList.size() - 1) {//所有广告播放完，就释放掉当前页
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
            return false;
        } else {
            mPlayIndex += 1;
            playLaunchAd(mPlayIndex);
            return true;
        }
    }

    private void go2HomeActivity(){
/*add by dragontec for bug 4249 start*/
        mInAdvertisement = false;
/*add by dragontec for bug 4249 end*/
        setBackground(R.drawable.homepage_background);
        ad_layout.setVisibility(View.GONE);
        home_layout.setVisibility(View.VISIBLE);
        /*modify by dragontec for bug 4350 start*/
        banner_arrow_down.setBackgroundResource(R.drawable.homepage_arrow_down_selector);
        /*modify by dragontec for bug 4350 end*/
        initServer();
        new Handler().postDelayed(mRunnable, 1000);

/*add by dragontec for bug 4259 start*/
        startRequestFocusBanner();
/*add by dragontec for bug 4259 end*/
    }

/*add by dragontec for bug 4259 start*/
    private void initHandlerAndRunnable() {
        mRequestBannerFocusHandler = new Handler();
        mRequestBannerFocusRunnable = new RequestFocusBannerRunnable();
    }

    private boolean requestFirstBannerFocus() {
        ChannelFragment channelFragment = getChannelFragment();
        if (channelFragment != null) {
            return channelFragment.requestFirstBannerFocus();
        }
        return false;
    }

    private void startRequestFocusBanner() {
        if (mRequestBannerFocusHandler != null
                && mRequestBannerFocusRunnable != null) {
            if (!mRequestBannerFocusInterrupt) {
                mRequestBannerFocusHandler.postDelayed(mRequestBannerFocusRunnable, RequestBannerFocusDelay);
            }
        }
    }

    private void stopRequestFocusBanner() {
        mRequestBannerFocusInterrupt = true;
        if (mRequestBannerFocusHandler != null
                && mRequestBannerFocusRunnable != null) {
            mRequestBannerFocusHandler.removeCallbacks(mRequestBannerFocusRunnable);
        }
    }

    private class RequestFocusBannerRunnable implements Runnable {
        @Override
        public void run() {
            if (mRequestBannerFocusHandler != null) {
                mRequestBannerFocusHandler.removeCallbacks(this);
            }
            if (!mRequestBannerFocusInterrupt) {
                boolean success = requestFirstBannerFocus();
                if (!success) {
                    startRequestFocusBanner();
                } else {
                    Log.e(TAG, "startRequestFocusBanner success");
                }
            }
        }
    }
/*add by dragontec for bug 4259 end*/

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {//播放出现错误
        if(!playNextVideo()){
            go2HomeActivity();
        }
        return true;
    }

    private void startIntervalActive() {
        Intent intent = new Intent();
        intent.setClass(this, ActiveService.class);
        startService(intent);
    }

    private void startAdsService() {
        Intent intent = new Intent();
        intent.setClass(this, AdsUpdateService.class);
        startService(intent);
    }

    private void setDisplayMetrics(){
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;  // 屏幕宽度（像素）
        int height = metric.heightPixels;  // 屏幕高度（像素）
        float density = metric.density;  // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
        VipMark.getInstance().setHeight(height);
    }
}
