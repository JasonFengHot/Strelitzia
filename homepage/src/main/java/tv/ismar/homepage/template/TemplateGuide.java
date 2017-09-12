package tv.ismar.homepage.template;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import tv.ismar.app.BaseControl;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.GuideAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.control.GuideControl;
import tv.ismar.homepage.widget.DaisyVideoView;
import tv.ismar.homepage.widget.HomeItemContainer;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 导视模版
 */

public class TemplateGuide extends Template implements BaseControl.ControlCallBack,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener, RecyclerViewTV.OnItemClickListener,
        RecyclerViewTV.PagingableListener {
    private HomeItemContainer mGuideContainer;//导视视频容器
    private DaisyVideoView mVideoView;//导视view
    private ImageView mLoadingIg;//加载提示logo
    private TextView mVideTitleTv;//导视标题
    private TextView mFirstIcon;//第一个视频指示数字
    private TextView mSecondIcon;
    private TextView mThirdIcon;
    private TextView mFourIcon;
    private TextView mFiveIcon;
    private RecyclerViewTV mRecycleView;//海报recycleview

    private GuideControl mControl;
    private GuideAdapter mAdapter;

    private BitmapDecoder mBitmapDecoder;

    public TemplateGuide(Context context) {
        super(context);
        mControl = new GuideControl(mContext, this);
    }

    @Override
    public void getView(View view) {
        mGuideContainer = (HomeItemContainer) view.findViewById(R.id.guide_container);
        mGuideContainer.setFocusable(true);
        mGuideContainer.requestFocus();
        mVideoView = (DaisyVideoView) view.findViewById(R.id.guide_daisy_video_view);
        mLoadingIg = (ImageView) view.findViewById(R.id.guide_video_loading_image);
        mVideTitleTv = (TextView) view.findViewById(R.id.guide_video_title);
        mFirstIcon = (TextView) view.findViewById(R.id.first_video_icon);
        mSecondIcon = (TextView) view.findViewById(R.id.second_video_icon);
        mThirdIcon = (TextView) view.findViewById(R.id.third_video_icon);
        mFourIcon = (TextView) view.findViewById(R.id.four_video_icon);
        mFiveIcon = (TextView) view.findViewById(R.id.five_video_icon);
        mRecycleView = (RecyclerViewTV) view.findViewById(R.id.guide_recyclerview);
        LinearLayoutManagerTV guideLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecycleView.setLayoutManager(guideLayoutManager);
        mRecycleView.setSelectedItemOffset(10, 10);
    }

    @Override
    public void initData(Bundle bundle) {
        mControl.getBanners(bundle.getInt("banner"), 1);
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
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnPreparedListener(this);
        mRecycleView.setOnItemClickListener(this);
        mRecycleView.setPagingableListener(this);
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
            case 2:
                mSecondIcon.setBackground(mContext.getResources().getDrawable(R.drawable.second_video_icon));
                break;
            case 3:
                mThirdIcon.setBackground(mContext.getResources().getDrawable(R.drawable.third_video_icon));
                break;
            case 4:
                mFourIcon.setBackground(mContext.getResources().getDrawable(R.drawable.four_video_icon));
                break;
            case 5:
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
        int size = mControl.mFetchDataControl.mCarousels.size();
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
                mAdapter = new GuideAdapter(mContext, homeEntity.poster);
                mAdapter.setMarginLeftEnable(true);
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
            mVideTitleTv.setText(mControl.mFetchDataControl.mCarousels.get(index).title);
            mVideoView.setFocusable(false);
            mVideoView.setFocusableInTouchMode(false);
            String videoPath = mControl.getGuideVideoPath(index);
            if(videoPath == null){
                return;
            }
            CallaPlay play = new CallaPlay();
            play.homepage_vod_trailer_play(videoPath, "homepage");
            if (mVideoView.isPlaying() && mVideoView.getDataSource().equals(videoPath)) {
                return;
            }
            mLoadingIg.setVisibility(View.VISIBLE);
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
        if(index >= mControl.mFetchDataControl.mCarousels.size()){
            index = 0;
        }
        mVideoView.setTag(index);
        playGuideVideo(index);
        changeCarouselIcon(index);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {//播放出错
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
    public void onItemClick(RecyclerViewTV parent, View itemView, int position) {
        if(position >= 1){//第2个item被选中
            mGuideContainer.setVisibility(View.GONE);
        }else if(position == 0){
            mGuideContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadMoreItems() {

    }
}
