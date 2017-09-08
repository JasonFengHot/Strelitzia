package tv.ismar.homepage;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

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
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.app.widget.TelescopicWrap;
import tv.ismar.homepage.adapter.HomeReclAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.view.ChannelChangeObservable;
import tv.ismar.homepage.widget.HorizontalTabView;

import static tv.ismar.homepage.control.FetchDataControl.FETCH_CHANNEL_BANNERS_FLAG;
import static tv.ismar.homepage.control.FetchDataControl.FETCH_CHANNEL_TAB_FLAG;
import static tv.ismar.homepage.control.FetchDataControl.FETCH_HOME_BANNERS_FLAG;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: home页
 */

public class HomeActivity extends BaseActivity implements BaseControl.ControlCallBack,
        View.OnClickListener, HorizontalTabView.OnItemSelectedListener,View.OnFocusChangeListener {

    private final FetchDataControl mControl = new FetchDataControl(this, this);//业务类引用

//    private ListView mListView;
    private RecyclerView mRecycleView;
    private HomeReclAdapter mAdapter;
    private HorizontalTabView channelTab;

    private TextView mTimeTv;//时间
    private ViewGroup mCollectionLayout;//历史收藏layout
    private ViewGroup mPersonCenterLayout;//个人中心
    private TelescopicWrap mCollectionTel;//历史收藏伸缩包装类
    private TelescopicWrap mPersonCenterTel;//个人中心包装类

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_layout);
        findViews();
        initListener();
        initData();
    }

    private ViewGroup mViewGroup;
    private BitmapDecoder mBitmapDecoder;

    /*获取控件实例*/
    private void findViews(){
        mViewGroup = (ViewGroup) findViewById(R.id.home_view_layout);
//        mListView = (ListView) findViewById(R.id.guide_container);
        mRecycleView = (RecyclerView) findViewById(R.id.home_activity_recycleview);
        channelTab = (HorizontalTabView) findViewById(R.id.channel_tab);
        mTimeTv = (TextView) findViewById(R.id.guide_title_time_tv);
        mCollectionLayout = (ViewGroup) findViewById(R.id.collection_layout);
        mPersonCenterLayout = (ViewGroup) findViewById(R.id.center_layout);
        mCollectionTel = new TelescopicWrap(this, mCollectionLayout);
        mPersonCenterTel = new TelescopicWrap(this, mPersonCenterLayout);
        Observable.create(new ChannelChangeObservable(channelTab))
                .throttleLast(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
//                        Log.d("channelTab", "channelTab ChannelChangeObservable onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Integer position) {
                        Log.d("channelTab", "channelTab ChannelChangeObservable");
                    }
                });

//        mListView.setItemsCanFocus(true);
        LinearLayoutManager linear = new LinearLayoutManager(this);
        linear.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(linear);

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
        mControl.fetchBannerList();
        mControl.fetchChannels();
    }

    /*获取当前时间*/
    private String getNowTime(){
        Date now = TrueTime.now();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return dateFormat.format(now);
    }

    /*用于业务类回调控制UI*/
    @Override
    public void callBack(int flag, Object... args) {
        //这里通过flag严格区分不同的业务流程，避免业务之间的耦合
        if(flag == FETCH_HOME_BANNERS_FLAG){//处理获取首页banner列表
            GuideBanner[] banners = (GuideBanner[]) args;
            if(mAdapter == null){
                mAdapter = new HomeReclAdapter(this, banners);
                mRecycleView.setAdapter(mAdapter);
//                mListView.setAdapter(mAdapter);
            }else{
                mAdapter.notifyDataSetChanged();
            }
        }else if (flag == FETCH_CHANNEL_TAB_FLAG){
            ChannelEntity[] channelEntities = (ChannelEntity[]) args;
            fillChannelTab(channelEntities);
        } else if(flag == FETCH_CHANNEL_BANNERS_FLAG){
            GuideBanner[] banners = (GuideBanner[]) args;
            if(mAdapter == null){
                mAdapter = new HomeReclAdapter(this, banners);
                mRecycleView.setAdapter(mAdapter);
            }else{
                mAdapter.notifyDataSetChanged();
            }
        }
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
        if(mControl.mChannels!=null && mControl.mChannels.length>0){//频道切换
            mControl.fetchChannelBanner(mControl.mChannels[position].getChannel());
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(v == mCollectionLayout){//历史收藏伸缩处理
            mCollectionTel.openOrClose(hasFocus);
            return;
        }
        if(v == mPersonCenterLayout){
            mPersonCenterTel.openOrClose(hasFocus);
            return;
        }
    }
}
