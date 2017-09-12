package tv.ismar.homepage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import cn.ismartv.truetime.TrueTime;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.BaseControl;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.entity.ChannelEntity;
import tv.ismar.app.receiver.TimeTickReceiver;
import tv.ismar.app.service.TrueTimeService;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.app.widget.TelescopicWrap;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.fragment.ChannelFragment;
import tv.ismar.homepage.view.ChannelChangeObservable;
import tv.ismar.homepage.widget.HorizontalTabView;
import tv.ismar.library.exception.ExceptionUtils;

import static tv.ismar.homepage.control.FetchDataControl.FETCH_CHANNEL_TAB_FLAG;


/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: home页
 */

public class HomeActivity extends BaseActivity implements View.OnClickListener, BaseControl.ControlCallBack,
        HorizontalTabView.OnItemSelectedListener,View.OnFocusChangeListener {

    public static final String HOME_PAGE_CHANNEL_TAG = "homepage";
    private final FetchDataControl mControl = new FetchDataControl(this, this);//业务类引用
    private HorizontalTabView channelTab;

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
        startTrueTimeService();
    }

    /*获取控件实例*/
    private void findViews(){
        mViewGroup = (ViewGroup) findViewById(R.id.home_view_layout);
        channelTab = (HorizontalTabView) findViewById(R.id.channel_tab);
        mTimeTv = (TextView) findViewById(R.id.guide_title_time_tv);
        mCollectionTv = (TextView) findViewById(R.id.collection_tv);
        mPersonCenterTv = (TextView) findViewById(R.id.center_tv);
        mCollectionLayout = (ViewGroup) findViewById(R.id.collection_layout);
        mPersonCenterLayout = (ViewGroup) findViewById(R.id.center_layout);
        mCollectionTel = new TelescopicWrap(this, mCollectionLayout);
        mPersonCenterTel = new TelescopicWrap(this, mPersonCenterLayout);
        Observable.create(new ChannelChangeObservable(channelTab))
                .throttleLast(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Integer position) {
                        Log.d("channelTab", "channelTab ChannelChangeObservable");
                    }
                });

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
        channelTab.setOnItemSelectedListener(this);
        mCollectionLayout.setOnFocusChangeListener(this);
        mPersonCenterLayout.setOnFocusChangeListener(this);
        mCollectionLayout.setOnClickListener(this);
        mPersonCenterLayout.setOnClickListener(this);
        TimeTickReceiver.register(this, new TimeTickBroadcast());
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
        mTimeTv.setText(getNowTime());
        mControl.fetchChannels();
        ChannelFragment channelFragment = new ChannelFragment();
        channelFragment.setChannel(HOME_PAGE_CHANNEL_TAG);
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

        transaction.replace(R.id.fragment_layout, fragment).commitAllowingStateLoss();
    }

    /*获取当前时间*/
    private String getNowTime(){
        Date now = TrueTime.now();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return dateFormat.format(now);
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
    public void onItemSelected(View v, int position) {
        if(mControl.mChannels!=null && mControl.mChannels.length>position){
            ChannelFragment channelFragment = new ChannelFragment();
            channelFragment.setChannel( mControl.mChannels[position].getChannel());
            if(position > mLastSelectedIndex){//右切
                replaceFragment(channelFragment, "right");
            }if(position < mLastSelectedIndex){//左切
                replaceFragment(channelFragment, "left");
            }
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
    }

    /*时间跳动广播*/
    private class TimeTickBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mTimeTv.setText(getNowTime());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TimeTickReceiver.unregisterAll(this);
    }

    private void startTrueTimeService() {
        Intent intent = new Intent();
        intent.setClass(this, TrueTimeService.class);
        startService(intent);
    }
}
