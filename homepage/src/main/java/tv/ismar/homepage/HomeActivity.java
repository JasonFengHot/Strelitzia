package tv.ismar.homepage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import cn.ismartv.truetime.TrueTime;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.BaseControl;
import tv.ismar.app.VodApplication;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.client.MessageQueue;
import tv.ismar.app.entity.ChannelEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.ui.ToastTip;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.app.widget.TelescopicWrap;
import tv.ismar.homepage.adapter.HomeAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.control.HomeControl;
import tv.ismar.homepage.fragment.ChannelFragment;
import tv.ismar.homepage.view.HomePageActivity;
import tv.ismar.homepage.widget.HorizontalTabView;
import tv.ismar.library.exception.ExceptionUtils;
import tv.ismar.player.gui.PlaybackService;

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static tv.ismar.app.BaseControl.TAB_CHANGE_FALG;
import static tv.ismar.homepage.control.FetchDataControl.FETCH_CHANNEL_TAB_FLAG;


/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: home页
 */

public class HomeActivity extends BaseActivity implements View.OnClickListener, BaseControl.ControlCallBack,
        View.OnFocusChangeListener,  View.OnHoverListener {

    public static final String HOME_PAGE_CHANNEL_TAG = "homepage";
    private final FetchDataControl mFetchDataControl = new FetchDataControl(this, this);//业务类引用
    private final HomeControl mHomeControl = new HomeControl(this, this);
    private HorizontalTabView mChannelTab;

    private ViewGroup mViewGroup;
    private TextView mTimeTv;//时间
    private TextView mCollectionTv;//收藏tv
    private TextView mPersonCenterTv;//个人中心tv
    private ViewGroup mCollectionRect;
    private ViewGroup mCenterRect;
    private ViewGroup mCollectionLayout;//历史收藏layout
    private ViewGroup mPersonCenterLayout;//个人中心
    private TelescopicWrap mCollectionTel;//历史收藏伸缩包装类
    private TelescopicWrap mPersonCenterTel;//个人中心包装类

    private BitmapDecoder mBitmapDecoder;
    private int mLastSelectedIndex = 1;//记录上一次选中的位置
    private TimeTickBroadcast mTimeTickBroadcast = null;

    public static View mHoverView;
    public static View mLastFocusView;

    private ImageView left_image,right_image; // 导航左右遮罩
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate((savedInstanceState!=null)?null:savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.home_activity_layout);
        systemInit();
        findViews();
        initListener();
        initData();
        new Handler().postDelayed(mRunnable,1000);
    }
    private Runnable mRunnable=new Runnable() {
        @Override
        public void run() {
            showLoginHint();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mTimeTv.setText(mHomeControl.getNowTime());
        if (mLastSelectedIndex == 0){
            mChannelTab.setDefaultSelection(1);
        }
    }

    /*初始化一些系统参数*/
    private void systemInit(){
        try {
            System.setProperty("http.keepAlive", "false");
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHomeControl.startTrueTimeService();
    }

    /*获取控件实例*/
    private void findViews(){
        mHoverView = findViewById(R.id.home_view_layout);
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
        mHoverView.setFocusable(true);
        mHoverView.requestFocus();
        setBackground(R.drawable.homepage_background);

        right_image= (ImageView) findViewById(R.id.guide_tab_right);
        left_image= (ImageView) findViewById(R.id.guide_tab_left);
        mChannelTab.leftbtn=left_image;
        mChannelTab.rightbtn=right_image;

    }

    private void setBackground(int id){
        mBitmapDecoder = new BitmapDecoder();
        mBitmapDecoder.decode(this, id, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                mViewGroup.setBackground(bitmapDrawable);
                mBitmapDecoder = null;
            }
        });
    }

    private void initListener(){
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
    }

    private void initData(){
        mFetchDataControl.fetchChannels();
        ChannelFragment channelFragment = new ChannelFragment();
        channelFragment.setChannel("首页", HOME_PAGE_CHANNEL_TAG, "首页", 0);
        replaceFragment(channelFragment, "none");
    }

    private void replaceFragment(Fragment fragment, String scrollType) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (scrollType) {
            case "left":
                transaction.setCustomAnimations(
                        R.anim.push_right_in,
                        R.anim.push_right_out);
                break;
            case "right":
                transaction.setCustomAnimations(
                        R.anim.push_left_in,
                        R.anim.push_left_out);
                break;
        }

        transaction.replace(R.id.fragment_layout, fragment, scrollType).commitAllowingStateLoss();
    }

    private void fillChannelTab(ChannelEntity[] channelEntities) {
        List<HorizontalTabView.Tab> tabs = new ArrayList<>();
        HorizontalTabView.Tab searchTab = new HorizontalTabView.Tab("", "搜索");
        tabs.add(searchTab);
        HorizontalTabView.Tab homepageTab=new HorizontalTabView.Tab("","首页");
        tabs.add(homepageTab);
        for (ChannelEntity entity : channelEntities) {
            HorizontalTabView.Tab tab = new HorizontalTabView.Tab("", entity.getName());
            tabs.add(tab);
        }
        mChannelTab.addAllViews(tabs, 1);
    }

    @Override
    public void onClick(View v) {
        PageIntent pageIntent = new PageIntent();
        if(v == mCollectionRect){
            pageIntent.toHistory(this,"homePage");
        } else if(v == mCenterRect){
            pageIntent.toUserCenter(this);
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
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
                if(!v.hasFocus()){
                    v.setFocusable(true);
                    v.requestFocus();
                }
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
//                onFocusChange(v,  false);
                break;
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(v == mCollectionRect){//历史收藏伸缩处理
            mCollectionTel.openOrClose(hasFocus);
            return;
        }
        if(v == mCenterRect){//个人中心伸缩处理
            mPersonCenterTel.openOrClose(hasFocus);
            return;
        }
    }

    @Override
    public void callBack(int flags, Object... args) {
        if (flags == FETCH_CHANNEL_TAB_FLAG){
            ChannelEntity[] channelEntities = (ChannelEntity[]) args;
            fillChannelTab(channelEntities);
        }
        if(flags == TAB_CHANGE_FALG){//频道切换
            int position = (int) args[0];
            if(mFetchDataControl.mChannels!=null && mFetchDataControl.mChannels.length>position){
                ChannelFragment channelFragment = new ChannelFragment();
                switch(position){
                    case 0://搜索
                        mLastSelectedIndex = position;
                        setBackground(R.drawable.homepage_background);
                        PageIntent intent = new PageIntent();
                        intent.toSearch(this);
                        return;
                    case 1://首页
                        setBackground(R.drawable.homepage_background);
                        channelFragment.setChannel("首页", HOME_PAGE_CHANNEL_TAG, "首页", 0);
                        break;
                    default://其他频道
                        if(position-2<0) return;
                        if(mFetchDataControl.mChannels[position-2].getChannel().equals("comic")) {
                            setBackground(R.drawable.juvenile_bg);
                        } else {
                            setBackground(R.drawable.homepage_background);
                        }
                        channelFragment.setChannel( mFetchDataControl.mChannels[position-2].getName(),
                                mFetchDataControl.mChannels[position-2].getChannel(),
                                mFetchDataControl.mChannels[position-2].getName(),
                                mFetchDataControl.mChannels[position-2].getStyle());
                        break;
                }
                if(position > mLastSelectedIndex){//右切
                    replaceFragment(channelFragment, "right");
                }if(position < mLastSelectedIndex){//左切
                    replaceFragment(channelFragment, "left");
                }
                mLastSelectedIndex = position;
            }
        }
    }

    /*时间跳动广播*/
    private class TimeTickBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mTimeTv.setText(mHomeControl.getNowTime());
        }
    }

    private long currentTime =0;
    @Override
    public void onBackPressed() {
        if(currentTime==0||System.currentTimeMillis()-currentTime>4000) {
            currentTime = System.currentTimeMillis();
            ToastTip.showToast(this, "再次点击返回按键，退出应用");
        }else {
            isCheckoutUpdate = true;
            SkyService.ServiceManager.executeActive = true;
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.app_exit(TrueTime.now().getTime() - app_start_time, SimpleRestClient.appVersion);
            ArrayList<String> cache_log = MessageQueue.getQueueList();
            HashSet<String> hasset_log = new HashSet<String>();
            for (int i = 0; i < cache_log.size(); i++) {
                hasset_log.add(cache_log.get(i));
            }
            DaisyUtils
                    .getVodApplication(HomeActivity.this)
                    .getEditor()
                    .putStringSet(VodApplication.CACHED_LOG,
                            hasset_log);
            DaisyUtils.getVodApplication(getApplicationContext())
                    .save();
            BaseActivity.baseChannel = "";
            BaseActivity.baseSection = "";
            stopService(new Intent(HomeActivity.this, PlaybackService.class));
            HomeActivity.super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        HomeActivity.super.onBackPressed();
//        android.os.Process.killProcess(android.os.Process.myPid());
//        System.exit(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mTimeTickBroadcast);
        mTimeTickBroadcast = null;

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mLastFocusView != null && mHoverView != null && mHoverView.hasFocus()){
            mLastFocusView.requestFocus();
            mLastFocusView.requestFocusFromTouch();
            mHoverView.setFocusable(false);
            mHoverView.setFocusableInTouchMode(false);
            return true;
        }
        mHoverView.setFocusable(false);
        mHoverView.setFocusableInTouchMode(false);
        return super.onKeyDown(keyCode, event);
    }

}
