package tv.ismar.homepage.template;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import tv.ismar.app.BaseControl;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.OnItemSelectedListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.GuideAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.control.GuideControl;
import tv.ismar.homepage.fragment.ChannelFragment;
import tv.ismar.homepage.view.BannerLinearLayout;
import tv.ismar.homepage.widget.DaisyVideoView;

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static tv.ismar.homepage.fragment.ChannelFragment.BANNER_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.CHANNEL_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.NAME_KEY;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 导视模版
 */

public class TemplateGuide extends Template implements BaseControl.ControlCallBack,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener, OnItemClickListener,
        RecyclerViewTV.PagingableListener,
        View.OnFocusChangeListener, View.OnHoverListener,
        LinearLayoutManagerTV.FocusSearchFailedListener,
        OnItemSelectedListener,View.OnClickListener {
    private DaisyVideoView mVideoView;//导视view
    private ImageView mLoadingIg;//加载提示logo
    private TextView mVideoTitleTv;//导视标题
    private TextView mFirstIcon;//第一个视频指示数字
    private TextView mSecondIcon;
    private TextView mThirdIcon;
    private TextView mFourIcon;
    private TextView mFiveIcon;
    private RecyclerViewTV mRecycleView;//海报recycleview
    private LinearLayoutManagerTV mGuideLayoutManager;

    public FetchDataControl mFetchDataControl = null;
    public GuideControl mControl;
    private GuideAdapter mAdapter;

    private BitmapDecoder mBitmapDecoder;//视频加载图片decoder
    private BannerLinearLayout mBannerLinearLayout;
    private View navigationLeft;
    private View navigationRight;

    public TemplateGuide(Context context) {
        super(context);
        mFetchDataControl = new FetchDataControl(context, this);
        mControl = new GuideControl(mContext);
    }

    private View mHeadView;//recylview头view

    @Override
    public void getView(View view) {
        mHeadView = view.findViewById(R.id.banner_guide_head);
        mVideoView = (DaisyVideoView) view.findViewById(R.id.guide_daisy_video_view);
        mLoadingIg = (ImageView) view.findViewById(R.id.guide_video_loading_image);
        mVideoTitleTv = (TextView) view.findViewById(R.id.guide_video_title);
        mFirstIcon = (TextView) view.findViewById(R.id.first_video_icon);
        mSecondIcon = (TextView) view.findViewById(R.id.second_video_icon);
        mThirdIcon = (TextView) view.findViewById(R.id.third_video_icon);
        mFourIcon = (TextView) view.findViewById(R.id.four_video_icon);
        mFiveIcon = (TextView) view.findViewById(R.id.five_video_icon);

        mRecycleView = (RecyclerViewTV) view.findViewById(R.id.guide_recyclerview);
        mGuideLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecycleView.setLayoutManager(mGuideLayoutManager);
        mRecycleView.setSelectedItemOffset(100, 100);
        navigationLeft = view.findViewById(R.id.navigation_left);
        navigationRight = view.findViewById(R.id.navigation_right);
        mBannerLinearLayout = (BannerLinearLayout) view.findViewById(R.id.banner_layout);
        mBannerLinearLayout.setNavigationLeft(navigationLeft);
        mBannerLinearLayout.setNavigationRight(navigationRight);
    }

    private int mBannerPk;//banner标记
    private String mName;//频道名称（中文）
    private String mChannel;//频道名称（英文）
    @Override
    public void initData(Bundle bundle) {
        mBannerPk = bundle.getInt(BANNER_KEY);
        mName = bundle.getString(NAME_KEY);
        mChannel = bundle.getString(CHANNEL_KEY);
        mFetchDataControl.fetchBanners(mBannerPk, 1, false);
        mVideoView.setTag(0);
        mBitmapDecoder = new BitmapDecoder();
        mBitmapDecoder.decode(mContext, R.drawable.guide_video_loading, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                mLoadingIg.setBackgroundDrawable(bitmapDrawable);
            }
        });
    }

    @Override
    protected void initListener(View view){
        navigationLeft.setOnClickListener(this);
        navigationRight.setOnClickListener(this);
        navigationRight.setOnHoverListener(this);
        navigationLeft.setOnHoverListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnPreparedListener(this);
        mRecycleView.setPagingableListener(this);
        mVideoView.setOnFocusChangeListener(this);
        mGuideLayoutManager.setFocusSearchFailedListener(this);
        mHeadView.findViewById(R.id.guide_head_ismartv_linearlayout).setOnHoverListener(this);
    }

    /*更改图标背景*/
    private void changeCarouselIcon(int index){
        mFirstIcon.setBackground(mContext.getResources().getDrawable(R.drawable.first_video_normal_icon));
        mSecondIcon.setBackground(mContext.getResources().getDrawable(R.drawable.second_video_normal_icon));
        mThirdIcon.setBackground(mContext.getResources().getDrawable(R.drawable.third_video_normal_icon));
        mFourIcon.setBackground(mContext.getResources().getDrawable(R.drawable.four_video_normal_icon));
        mFiveIcon.setBackground(mContext.getResources().getDrawable(R.drawable.five_video_normal_icon));
        switch (index) {
            case 0:
                mFirstIcon.setBackground(mContext.getResources().getDrawable(R.drawable.first_video_icon));
                break;
            case 1:
                mSecondIcon.setBackground(mContext.getResources().getDrawable(R.drawable.second_video_icon));
                break;
            case 2:
                mThirdIcon.setBackground(mContext.getResources().getDrawable(R.drawable.third_video_icon));
                break;
            case 3:
                mFourIcon.setBackground(mContext.getResources().getDrawable(R.drawable.four_video_icon));
                break;
            case 4:
                mFiveIcon.setBackground(mContext.getResources().getDrawable(R.drawable.five_video_icon));
                break;
        }
    }

    /*初始化导视数字指示*/
    private void initCarousel(){
        mFirstIcon.setVisibility(View.GONE);
        mSecondIcon.setVisibility(View.GONE);
        mThirdIcon.setVisibility(View.GONE);
        mFourIcon.setVisibility(View.GONE);
        mFiveIcon.setVisibility(View.GONE);
        int size = mFetchDataControl.mCarousels.size();
        if(size >= 3){
            mFirstIcon.setVisibility(View.VISIBLE);
            mSecondIcon.setVisibility(View.VISIBLE);
            mThirdIcon.setVisibility(View.VISIBLE);
        }
        if(size >= 4){
            mFourIcon.setVisibility(View.VISIBLE);
        }
        if(size >= 5){
            mFiveIcon.setVisibility(View.VISIBLE);
        }
    }

    /*初始化RecycleView*/
    private void initRecycleView(HomeEntity homeEntity){
        if(homeEntity != null){
            if(mAdapter == null){
                mAdapter = new GuideAdapter(mContext, homeEntity.posters);
                mAdapter.setMarginLeftEnable(true);
                mAdapter.setOnItemClickListener(this);
                mAdapter.setOnItemSelectedListener(this);
                mRecycleView.setAdapter(mAdapter);
            }else {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void callBack(int flags, Object... args) {//获取网络数据回调
        if(flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG){//获取单个banner业务
            HomeEntity homeEntity = (HomeEntity) args[0];
            initRecycleView(homeEntity);
            playGuideVideo((int)mVideoView.getTag());
            initCarousel();
        }
    }

    /*播放导视*/
    private void playGuideVideo(int index){
        try {
            mVideoTitleTv.setText(mFetchDataControl.mCarousels.get(index).title);
            mVideoView.setFocusable(false);
            mVideoView.setFocusableInTouchMode(false);
            String videoPath = mControl.getGuideVideoPath(index, mFetchDataControl.mCarousels);
            if(videoPath == null){
                return;
            }
            CallaPlay play = new CallaPlay();
            play.homepage_vod_trailer_play(videoPath, "homepage");
            if (mVideoView.isPlaying() && mVideoView.getDataSource().equals(videoPath)) {
                return;
            }
            mVideoView.stopPlayback();
            mVideoView.setVideoPath(videoPath);
            mVideoView.setTag(index);
            mVideoView.setFocusable(true);
            mVideoView.setFocusableInTouchMode(true);
            mVideoView.start();
        } catch (Exception e) {
            e.printStackTrace();
            new CallaPlay().exception_except("launcher", "launcher", "homepage",
                    "", 0, "",
                    SimpleRestClient.appVersion, "client", "");
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {//播放结束
        int index = (int) mVideoView.getTag();
        index++;
        if(index >= mFetchDataControl.mCarousels.size()){
            index = 0;
        }
        mVideoView.setTag(index);
        playGuideVideo(index);
        changeCarouselIcon(index);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {//播放出错
        mLoadingIg.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {//准备播放
        mLoadingIg.setVisibility(View.GONE);
        if (mBitmapDecoder != null && mBitmapDecoder.isAlive()) {
            mBitmapDecoder.interrupt();
        }
    }

    @Override
    public void onLoadMoreItems() {
        Log.i(TAG, "onLoadMoreItems");
        HomeEntity homeEntity = mFetchDataControl.mHomeEntity;
        if(homeEntity != null){
            if(homeEntity.page < homeEntity.num_pages){
                mRecycleView.setOnLoadMoreComplete();
                mFetchDataControl.fetchBanners(mBannerPk, ++homeEntity.page, true);
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {//item点击事件
        if(position == 0){//第一张大图
            mControl.go2Detail(mFetchDataControl.mHomeEntity.bg_image);
        } else if(position == mFetchDataControl.mHomeEntity.count-1){
            new PageIntent().toListPage(mContext, mName, mChannel, 0);
        }else {
            mControl.go2Detail(mFetchDataControl.mHomeEntity.posters.get(position));
        }
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT){
            if (mRecycleView.getChildAt(0).findViewById(R.id.guide_ismartv_linear_layout) == focused){
                mHeadView.requestFocus();
                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(mHeadView);
                return mHeadView;
            }
            if(mRecycleView.getChildAt(mRecycleView.getChildCount() - 1).findViewById(R.id.guide_ismartv_linear_layout) == focused){
                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
                return focused;
            }
        }
        return null;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        mHeadView.findViewById(R.id.guide_head_ismartv_linearlayout).setFocusable(true);
        mHeadView.findViewById(R.id.guide_head_ismartv_linearlayout).requestFocus();
    }

    @Override
    public void onItemSelect(View view, int position) {
        if(position < 1){
            mHeadView.setVisibility(View.VISIBLE);
        } else {
            mHeadView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.navigation_left) {
            if (mGuideLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) {//向左滑动
                int targetPosition = mGuideLayoutManager.findFirstCompletelyVisibleItemPosition() - 5;
                if (targetPosition <= 0) targetPosition = 0;
                mGuideLayoutManager.smoothScrollToPosition(mRecycleView, null, targetPosition);
            }
        } else if (i == R.id.navigation_right) {//向右滑动
            mRecycleView.loadMore();
            mHeadView.setVisibility(View.GONE);
            if (mGuideLayoutManager.findLastCompletelyVisibleItemPosition()  <= mFetchDataControl.mHomeEntity.count) {
                int targetPosition = mGuideLayoutManager.findLastCompletelyVisibleItemPosition() + 5;
                if (targetPosition >= mFetchDataControl.mHomeEntity.count) {
                    targetPosition = mFetchDataControl.mHomeEntity.count;
                }
                mGuideLayoutManager.smoothScrollToPosition(mRecycleView, null, targetPosition);
                if(targetPosition==mFetchDataControl.mHomeEntity.count)
                    YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(mRecycleView.getChildAt(mRecycleView.getChildCount() - 1).findViewById(R.id.tv_player_ismartv_linear_layout));
            }
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_MOVE:
            case MotionEvent.ACTION_HOVER_ENTER:
                if (!v.hasFocus()) {
                    v.requestFocus();
                    v.requestFocusFromTouch();
                }
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                if (event.getButtonState() != BUTTON_PRIMARY) {
                    navigationLeft.setVisibility(View.INVISIBLE);
                    navigationRight.setVisibility(View.INVISIBLE);
                    HomeActivity.mHoverView.requestFocus();//将焦点放置到一块隐藏view中
                }
                break;
        }
        return false;
    }
}
