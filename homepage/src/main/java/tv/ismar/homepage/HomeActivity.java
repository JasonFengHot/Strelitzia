package tv.ismar.homepage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.BaseControl;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.entity.ChannelEntity;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.app.widget.TelescopicWrap;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.control.HomeControl;
import tv.ismar.homepage.fragment.ChannelFragment;
import tv.ismar.homepage.widget.HorizontalTabView;
import tv.ismar.library.exception.ExceptionUtils;

import static tv.ismar.app.BaseControl.TAB_CHANGE_FALG;
import static tv.ismar.homepage.control.FetchDataControl.FETCH_CHANNEL_TAB_FLAG;


/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: home页
 */

public class HomeActivity extends BaseActivity implements View.OnClickListener, BaseControl.ControlCallBack, View.OnFocusChangeListener {

    public static final String HOME_PAGE_CHANNEL_TAG = "homepage";
    private final FetchDataControl mFetchDataControl = new FetchDataControl(this, this);//业务类引用
    private final HomeControl mHomeControl = new HomeControl(this, this);
    private HorizontalTabView mChannelTab;

    private ViewGroup mViewGroup;
    private TextView mTimeTv;//时间
    private TextView mCollectionTv;//收藏tv
    private TextView mPersonCenterTv;//个人中心tv
    private ViewGroup mCollectionLayout;//历史收藏layout
    private ViewGroup mPersonCenterLayout;//个人中心
    private TelescopicWrap mCollectionTel;//历史收藏伸缩包装类
    private TelescopicWrap mPersonCenterTel;//个人中心包装类

    private BitmapDecoder mBitmapDecoder;
    private int mLastSelectedIndex = 0;//记录上一次选中的位置
    private TimeTickBroadcast mTimeTickBroadcast = null;

    public static View mHoverView;
    public static View mLastFocusView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate((savedInstanceState!=null)?null:savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.home_activity_layout);
        systemInit();
        findViews();
        initListener();
        initData();
    }

    /*初始化一些系统参数*/
    private void systemInit(){
        try {
            System.setProperty("http.keepAlive", "false");
        } catch (Exception e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }
        mHomeControl.startTrueTimeService();
    }

    /*获取控件实例*/
    private void findViews(){
        mHoverView = findViewById(R.id.hover_view);
        mViewGroup = (ViewGroup) findViewById(R.id.home_view_layout);
        mChannelTab = (HorizontalTabView) findViewById(R.id.channel_tab);
        mTimeTv = (TextView) findViewById(R.id.guide_title_time_tv);
        mCollectionTv = (TextView) findViewById(R.id.collection_tv);
        mPersonCenterTv = (TextView) findViewById(R.id.center_tv);
        mCollectionLayout = (ViewGroup) findViewById(R.id.collection_layout);
        mPersonCenterLayout = (ViewGroup) findViewById(R.id.center_layout);
        mCollectionTel = new TelescopicWrap(this, mCollectionLayout);
        mPersonCenterTel = new TelescopicWrap(this, mPersonCenterLayout);
        mHoverView.setFocusable(true);
        mHoverView.requestFocus();
        mBitmapDecoder = new BitmapDecoder();
        mBitmapDecoder.decode(this, R.drawable.homepage_background, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                mViewGroup.setBackground(bitmapDrawable);
                mBitmapDecoder = null;
            }
        });
    }

    private void initListener(){
        mCollectionLayout.setOnFocusChangeListener(this);
        mPersonCenterLayout.setOnFocusChangeListener(this);
        mCollectionLayout.setOnClickListener(this);
        mPersonCenterLayout.setOnClickListener(this);
        mHomeControl.setChannelChange(mChannelTab);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        mTimeTickBroadcast = new TimeTickBroadcast();
        registerReceiver(mTimeTickBroadcast, filter);
        mTimeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction("tv.ismar.daisy.listtest");
                startActivity(intent);
            }
        });
    }

    private void initData(){
        mTimeTv.setText(mHomeControl.getNowTime());
        mFetchDataControl.fetchChannels();
        ChannelFragment channelFragment = new ChannelFragment();
        channelFragment.setChannel(HOME_PAGE_CHANNEL_TAG, "首页", 0);
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
        if(v == mCollectionLayout){
            pageIntent.toHistory(this);
        } else if(v == mPersonCenterLayout){
            pageIntent.toUserCenter(this);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(v == mCollectionLayout){//历史收藏伸缩处理
            mCollectionTv.setVisibility(hasFocus?View.VISIBLE:View.GONE);
            mCollectionTel.openOrClose(hasFocus);
            return;
        }
        if(v == mPersonCenterLayout){//个人中心伸缩处理
            mPersonCenterTv.setVisibility(hasFocus?View.VISIBLE:View.GONE);
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
                        PageIntent intent = new PageIntent();
                        intent.toSearch(this);
                        break;
                    case 1://首页
                        channelFragment.setChannel(HOME_PAGE_CHANNEL_TAG, "首页", 0);
                        break;
                    default://其他频道
                        if(position-2<0) return;
                        channelFragment.setChannel( mFetchDataControl.mChannels[position-2].getChannel(),
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
