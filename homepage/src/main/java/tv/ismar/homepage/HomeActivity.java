package tv.ismar.homepage;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
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
import tv.ismar.app.widget.TelescopicWrap;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.control.HomeControl;
import tv.ismar.homepage.fragment.ChannelFragment;
import tv.ismar.homepage.view.AdvertiseActivity;
import tv.ismar.homepage.widget.DaisyVideoView;
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
    private static View mHoverView;
/*modify by dragontec for bug 4057 end*/
    public static View mLastFocusView;
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
    private View headHoverd;

    private ImageView left_image, right_image; // 导航左右遮罩
    private Runnable mRunnable =
            new Runnable() {
                @Override
                public void run() {
                    showLoginHint();
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
    public Button banner_arrow_up;
    public Button banner_arrow_down;

    //广告
    private static final int MSG_AD_COUNTDOWN = 0x01;

    private DaisyVideoView mVideoView;
    private ImageView mPicImg;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate((savedInstanceState != null) ? null : savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        View contentview = LayoutInflater.from(this).inflate(R.layout.home_activity_layout, null);
        setContentView(contentview);
        homepageTemplate = getIntent().getStringExtra("homepage_template");
        homepageUrl = getIntent().getStringExtra("homepage_url");
        fromPage=getIntent().getStringExtra("fromPage");
        systemInit();
        findViews();
        initListener();
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
        if (mLastSelectedIndex == 0) {
            mChannelTab.setDefaultSelection(1);
        }
    }

    @Override
    protected void onPause() {
        if (mFetchDataControl!= null){
            mFetchDataControl.stop();
        }
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
        super.onDestroy();
        /*add by dragontec for bug 3983 start*/
        uninitTitleAnim();
        /*add by dragontec for bug 3983 end*/
        unregisterReceiver(mTimeTickBroadcast);
        mTimeTickBroadcast = null;
        RefWatcher refWatcher = VodApplication.getRefWatcher(this);
        refWatcher.watch(this);
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
        mHoverView = findViewById(R.id.home_view_layout);
        headHoverd = findViewById(R.id.hover_view);
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
        right_image = (ImageView) findViewById(R.id.guide_tab_right);
        left_image = (ImageView) findViewById(R.id.guide_tab_left);
        mChannelTab.leftbtn = left_image;
        mChannelTab.rightbtn = right_image;

        //广告
        mVideoView = (DaisyVideoView) findViewById(R.id.home_ad_video);
        mPicImg = (ImageView) findViewById(R.id.home_ad_pic);
        mSeekBar = (SeekBar) findViewById(R.id.home_ad_seekbar);
        timeBtn= (Button) findViewById(R.id.home_ad_timer);

//        banner_arrow_up = findView(R.id.banner_arrow_up);
//        banner_arrow_down = findView(R.id.banner_arrow_down);
//        banner_arrow_up.setOnHoverListener(this);
//        banner_arrow_down.setOnHoverListener(this);
    }

    private void setBackground(int id) {
        mBitmapDecoder = new BitmapDecoder();
        mBitmapDecoder.decode(
                this,
                id,
                new BitmapDecoder.Callback() {
                    @Override
                    public void onSuccess(BitmapDrawable bitmapDrawable) {
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
            if (!TextUtils.isEmpty(homepageUrl) && !TextUtils.isEmpty(channelEntity.getHomepage_url())
                    && channelEntity.getHomepage_url().contains(homepageUrl)) {
                defaultSelectPosition = i + 2;
            }
            HorizontalTabView.Tab tab = new HorizontalTabView.Tab("", channelEntity.getName());
            tabs.add(tab);
        }
        callBack( TAB_CHANGE_FALG, defaultSelectPosition);
        Log.d(TAG, "channel default position: " + defaultSelectPosition);
        mChannelTab.addAllViews(tabs, defaultSelectPosition);
    }

    /*add by dragontec for bug 3983 start 当动画执行过程中不响应按键*/
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(isAnimationPlaying){
            return true;
        }
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
                v.requestFocusFromTouch();
                v.requestFocus();
/*modify by dragontec for bug 4057 end*/
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                //                onFocusChange(v,  false);
/*add by dragontec for bug 4057 start*/
                if (event.getButtonState() != MotionEvent.BUTTON_PRIMARY) {
                    v.clearFocus();
                }
/*add by dragontec for bug 4057 end*/
                break;
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.i("favorite", "focus : " + hasFocus);
        if (v == mCollectionRect) { // 历史收藏伸缩处理

            mCollectionTel.openOrClose(hasFocus);
            return;
        }
        if (v == mCenterRect) { // 个人中心伸缩处理
            mPersonCenterTel.openOrClose(hasFocus);
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
            if (mFetchDataControl.mChannels != null && mFetchDataControl.mChannels.length > position) {
                ChannelFragment channelFragment = new ChannelFragment();
                switch (position) {
                    case 0: // 搜索
                        mLastSelectedIndex = position;
                        setBackground(R.drawable.homepage_background);
                        PageIntent intent = new PageIntent();
                        intent.toSearch(this);
                        return;
                    case 1: // 首页
                        setBackground(R.drawable.homepage_background);
                        channelFragment.setChannel("首页", HOME_PAGE_CHANNEL_TAG, "首页", 0);
                        break;
                    default: // 其他频道
                        if (position - 2 < 0) return;
                        if (mFetchDataControl.mChannels[position - 2].getChannel().equals("comic")) {
                            setBackground(R.drawable.juvenile_bg);
                        } else {
                            setBackground(R.drawable.homepage_background);
                        }
                        channelFragment.setChannel(
                                mFetchDataControl.mChannels[position - 2].getName(),
                                mFetchDataControl.mChannels[position - 2].getChannel(),
                                mFetchDataControl.mChannels[position - 2].getName(),
                                mFetchDataControl.mChannels[position - 2].getStyle());
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

    @Override
    public void onBackPressed() {
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

        ChannelFragment channelFragment = (ChannelFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_layout);
        if (channelFragment != null){
            channelFragment.onKeyDown(keyCode, event);
        }

        return super.onKeyDown(keyCode, event);
    }

    /*add by dragontec for bug 3983 start*/
    private void initTitleAnim(){
        int height = getResources().getDimensionPixelSize(R.dimen.banner_margin_top);
        TitleAnimUpdateListener titleAnimUpdateListener = new TitleAnimUpdateListener();
        TitleAnimStateListener titleAnimStateListener = new TitleAnimStateListener();

        mTitleMoveOutAnimator = ValueAnimator.ofInt(0, -height);
        mTitleMoveOutAnimator.setDuration(500);
        mTitleMoveOutAnimator.setTarget(home_layout);
        mTitleMoveOutAnimator.addUpdateListener(titleAnimUpdateListener);
        mTitleMoveOutAnimator.addListener(titleAnimStateListener);

        mTitleMoveInAnimator = ValueAnimator.ofInt(-height, 0);
        mTitleMoveInAnimator.setDuration(500);
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
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            isAnimationPlaying = false;
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
        setBackground(R.drawable.homepage_background);
        ad_layout.setVisibility(View.GONE);
        home_layout.setVisibility(View.VISIBLE);
        initServer();
        new Handler().postDelayed(mRunnable, 1000);
    }

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
