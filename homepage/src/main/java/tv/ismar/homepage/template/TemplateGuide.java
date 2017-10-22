package tv.ismar.homepage.template;

import android.content.Context;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseControl;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.cache.CacheManager;
import tv.ismar.app.core.cache.DownloadClient;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.util.HardwareUtils;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.OnItemSelectedListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.GuideAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.control.GuideControl;
import tv.ismar.homepage.view.BannerLinearLayout;
import tv.ismar.homepage.widget.DaisyVideoView;
	/*add by dragontec for bug 4077 start*/
import tv.ismar.homepage.widget.RecycleLinearLayout;
	/*add by dragontec for bug 4077 end*/
import tv.ismar.library.exception.ExceptionUtils;

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static tv.ismar.homepage.fragment.ChannelFragment.BANNER_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.CHANNEL_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.NAME_KEY;

/**
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: 导视模版
 */
public class TemplateGuide extends Template
        implements BaseControl.ControlCallBack,
        OnItemClickListener,
        RecyclerViewTV.PagingableListener,
        View.OnFocusChangeListener,
        View.OnHoverListener,
        LinearLayoutManagerTV.FocusSearchFailedListener,
        OnItemSelectedListener,
        View.OnClickListener {
    private static final int CAROUSEL_NEXT = 0x0001;
    private static final int START_PLAYBACK = 0x0002;
    public FetchDataControl mFetchDataControl = null;
    public GuideControl mControl;
    private DaisyVideoView mVideoView; // 导视view
    private ImageView mLoadingIg; // 加载提示logo
    private TextView mVideoTitleTv; // 导视标题
    private TextView mFirstIcon; // 第一个视频指示数字
    private TextView mSecondIcon;
    private TextView mThirdIcon;
    private TextView mFourIcon;
    private TextView mFiveIcon;
    private RecyclerViewTV mRecycleView; // 海报recycleview
    private LinearLayoutManagerTV mGuideLayoutManager;
    private GuideAdapter mAdapter;

    private BannerLinearLayout mBannerLinearLayout;
    private View navigationLeft;
    private View navigationRight;

    private int mCurrentCarouselIndex = -1;
    private boolean videoViewVisibility = true;
    private Subscription playSubscription;
    private Subscription checkVideoViewFullVisibilitySubsc;

    private View mVideoViewLayout;
    private View mHeadView; // recylview头view
    private String mBannerPk; // banner标记
    private String mName; // 频道名称（中文）
    private String mChannel; // 频道名称（英文）
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnErrorListener mVideoOnErrorListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private Handler mHandler =
            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case START_PLAYBACK:
                            // 如果视频没有下载完，播放图片
                            if (!startPlayback()) {
                                playImage();
                            }
                            break;
                        case CAROUSEL_NEXT:
                            playCarousel();
                            break;
                    }
                }
            };

    public TemplateGuide(Context context) {
        super(context);
        mFetchDataControl = new FetchDataControl(context, this);
        mControl = new GuideControl(mContext);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
        Logger.t(TAG).d("onPause");

        if (mHandler != null) {
            mHandler.removeMessages(CAROUSEL_NEXT);
            mHandler.removeMessages(START_PLAYBACK);
        }

        if (mVideoView != null) {
            if (mVideoView.isPlaying()) {
                mVideoView.stopPlayback();
            }
        }

        if (mFetchDataControl != null) {
            mFetchDataControl.stop();
        }

        if (playSubscription != null && !playSubscription.isUnsubscribed()) {
            playSubscription.unsubscribe();
        }

        if (checkVideoViewFullVisibilitySubsc != null
                && !checkVideoViewFullVisibilitySubsc.isUnsubscribed()) {
            checkVideoViewFullVisibilitySubsc.unsubscribe();
        }
	/*add by dragontec for bug 4077 start*/
        super.onPause();
	/*add by dragontec for bug 4077 end*/
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {
        mHandler = null;
        playSubscription = null;
        checkVideoViewFullVisibilitySubsc = null;
        mVideoView = null;
        mLoadingIg = null;
        mControl = null;

    }

    @Override
    public void getView(View view) {
        mHeadView = view.findViewById(R.id.banner_guide_head);
        //        mHeadView.findViewById(R.id.guide_head_ismartv_linearlayout).setFocusable(true);
        //        mHeadView.findViewById(R.id.guide_head_ismartv_linearlayout).requestFocus();
        mVideoView = (DaisyVideoView) view.findViewById(R.id.guide_daisy_video_view);
        mLoadingIg = (ImageView) view.findViewById(R.id.guide_video_loading_image);
        mVideoTitleTv = (TextView) view.findViewById(R.id.guide_video_title);
        mFirstIcon = (TextView) view.findViewById(R.id.first_video_icon);
        mSecondIcon = (TextView) view.findViewById(R.id.second_video_icon);
        mThirdIcon = (TextView) view.findViewById(R.id.third_video_icon);
        mFourIcon = (TextView) view.findViewById(R.id.four_video_icon);
        mFiveIcon = (TextView) view.findViewById(R.id.five_video_icon);
        mVideoViewLayout = view.findViewById(R.id.guide_head_ismartv_linearlayout);

        mRecycleView = (RecyclerViewTV) view.findViewById(R.id.guide_recyclerview);
        mGuideLayoutManager =
                new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecycleView.setLayoutManager(mGuideLayoutManager);
        mRecycleView.setSelectedItemOffset(100, 100);
        navigationLeft = view.findViewById(R.id.navigation_left);
        navigationRight = view.findViewById(R.id.navigation_right);
        mBannerLinearLayout = (BannerLinearLayout) view.findViewById(R.id.banner_layout);
        mBannerLinearLayout.setNavigationLeft(navigationLeft);
        mBannerLinearLayout.setNavigationRight(navigationRight);
    }

    @Override
    public void initData(Bundle bundle) {
        mBannerPk = bundle.getString(BANNER_KEY);
        mName = bundle.getString(NAME_KEY);
        mChannel = bundle.getString(CHANNEL_KEY);
        mFetchDataControl.fetchBanners(mBannerPk, 1, false);
    }

    @Override
    protected void initListener(View view) {
        navigationLeft.setOnClickListener(this);
        navigationRight.setOnClickListener(this);
        navigationRight.setOnHoverListener(this);
        navigationLeft.setOnHoverListener(this);
        //        mVideoView.setOnCompletionListener(this);
        //        mVideoView.setOnErrorListener(this);
        //        mVideoView.setOnPreparedListener(this);
        mRecycleView.setPagingableListener(this);
        mVideoView.setOnFocusChangeListener(this);
        mGuideLayoutManager.setFocusSearchFailedListener(this);
        mHeadView.findViewById(R.id.guide_head_ismartv_linearlayout).setOnHoverListener(this);
        mVideoViewLayout.setOnClickListener(this);
        mLoadingIg.setOnClickListener(this);
        mLoadingIg.setOnFocusChangeListener(this);
        mLoadingIg.setOnHoverListener(this);
    }

    /*更改图标背景*/
    private void changeCarouselIcon(int index) {
        mFirstIcon.setBackground(
                mContext.getResources().getDrawable(R.drawable.first_video_normal_icon));
        mSecondIcon.setBackground(
                mContext.getResources().getDrawable(R.drawable.second_video_normal_icon));
        mThirdIcon.setBackground(
                mContext.getResources().getDrawable(R.drawable.third_video_normal_icon));
        mFourIcon.setBackground(mContext.getResources().getDrawable(R.drawable.four_video_normal_icon));
        mFiveIcon.setBackground(mContext.getResources().getDrawable(R.drawable.five_video_normal_icon));
        switch (index) {
            case 0:
                mFirstIcon.setBackground(mContext.getResources().getDrawable(R.drawable.first_video_icon));
                break;
            case 1:
                mSecondIcon.setBackground(
                        mContext.getResources().getDrawable(R.drawable.second_video_icon));
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
    private void initCarousel() {
        mFirstIcon.setVisibility(View.GONE);
        mSecondIcon.setVisibility(View.GONE);
        mThirdIcon.setVisibility(View.GONE);
        mFourIcon.setVisibility(View.GONE);
        mFiveIcon.setVisibility(View.GONE);
        int size = mFetchDataControl.mCarousels.size();
        if (size >= 3) {
            mFirstIcon.setVisibility(View.VISIBLE);
            mSecondIcon.setVisibility(View.VISIBLE);
            mThirdIcon.setVisibility(View.VISIBLE);
        }
        if (size >= 4) {
            mFourIcon.setVisibility(View.VISIBLE);
        }
        if (size >= 5) {
            mFiveIcon.setVisibility(View.VISIBLE);
        }
    }

    /*初始化RecycleView*/
    private void initRecycleView(HomeEntity homeEntity) {
        if (homeEntity != null) {
            if (mAdapter == null) {
                mAdapter = new GuideAdapter(mContext, mFetchDataControl.mPoster);
                mAdapter.setMarginLeftEnable(true);
                mAdapter.setOnItemClickListener(this);
                mAdapter.setOnItemSelectedListener(this);
                mRecycleView.setAdapter(mAdapter);
	/*add by dragontec for bug 4077 start*/
				checkFocus(mRecycleView);
	/*add by dragontec for bug 4077 end*/
            } else {
                int start = mFetchDataControl.mPoster.size() - mFetchDataControl.mHomeEntity.posters.size();
                int end = mFetchDataControl.mPoster.size();
                mAdapter.notifyItemRangeChanged(start, end);
            }
        }
    }

    @Override
    public void callBack(int flags, Object... args) { // 获取网络数据回调
        if (flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG) { // 获取单个banner业务
            HomeEntity homeEntity = (HomeEntity) args[0];
            initRecycleView(homeEntity);
            //            playGuideVideo((int)mVideoView.getTag());
            playCarousel();
            initCarousel();
        }
    }

    @Override
    public void onLoadMoreItems() {
        Log.i(TAG, "onLoadMoreItems");
        HomeEntity homeEntity = mFetchDataControl.mHomeEntity;
        if (homeEntity != null) {
            if (homeEntity.page < homeEntity.num_pages) {
                mRecycleView.setOnLoadMoreComplete();
                mFetchDataControl.fetchBanners(mBannerPk, ++homeEntity.page, true);
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) { // item点击事件
        if (position == mFetchDataControl.mHomeEntity.count - 1) {
            new PageIntent()
                    .toListPage(
                            mContext,
                            mFetchDataControl.mHomeEntity.channel_title,
                            mFetchDataControl.mHomeEntity.channel,
                            mFetchDataControl.mHomeEntity.style,
                            mFetchDataControl.mHomeEntity.section_slug);
        } else {
            mControl.go2Detail(mFetchDataControl.mHomeEntity.posters.get(position));
        }
    }

    @Override
    public View onFocusSearchFailed(
            View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
            if (mRecycleView.getChildAt(0).findViewById(R.id.guide_ismartv_linear_layout) == focused) {
                mHeadView.requestFocus();
                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(mHeadView);
                return mHeadView;
            }
            if (mRecycleView
                    .getChildAt(mRecycleView.getChildCount() - 1)
                    .findViewById(R.id.guide_ismartv_linear_layout)
                    == focused) {
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
        if(mGuideLayoutManager.canScrollHorizontally()) {
            if (position < 1) {
                mHeadView.setVisibility(View.VISIBLE);
                videoViewVisibility = true;
            } else {
                mHeadView.setVisibility(View.GONE);
                videoViewVisibility = false;
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.navigation_left) {
            mGuideLayoutManager.setCanScroll(true);
            if (mGuideLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) { // 向左滑动
                int targetPosition = mGuideLayoutManager.findFirstCompletelyVisibleItemPosition() - 5;
                if (targetPosition <= 0) targetPosition = 0;
                mGuideLayoutManager.smoothScrollToPosition(mRecycleView, null, targetPosition);
            }
        } else if (i == R.id.navigation_right) {
            mGuideLayoutManager.setCanScroll(true);
            // 向右滑动
            mRecycleView.loadMore();
            mHeadView.setVisibility(View.GONE);
            if (mGuideLayoutManager.findLastCompletelyVisibleItemPosition()
                    <= mFetchDataControl.mPoster.size()) {
                int targetPosition = mGuideLayoutManager.findLastCompletelyVisibleItemPosition() + 10;
                if (targetPosition >= mFetchDataControl.mPoster.size()) {
                    targetPosition = mFetchDataControl.mPoster.size();
                }
                mGuideLayoutManager.smoothScrollToPosition(mRecycleView, null, targetPosition);
                if (targetPosition == mFetchDataControl.mPoster.size())
                    YoYo.with(Techniques.HorizontalShake)
                            .duration(1000)
                            .playOn(
                                    mRecycleView
                                            .getChildAt(mRecycleView.getChildCount() - 1)
                                            .findViewById(R.id.guide_ismartv_linear_layout));
            }
        } else if (i == R.id.guide_head_ismartv_linearlayout) {
            Log.d(TAG, "onClick goToNextPage");
            goToNextPage(v);
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
                return false;
            case MotionEvent.ACTION_HOVER_EXIT:
                if (event.getButtonState() != BUTTON_PRIMARY) {
//                    navigationLeft.setVisibility(View.INVISIBLE);
//                    navigationRight.setVisibility(View.INVISIBLE);
/*modify by dragontec for bug 4057 start*/
//                    HomeActivity.mHoverView.requestFocus(); // 将焦点放置到一块隐藏view中
                    v.clearFocus();
/*modify by dragontec for bug 4057 end*/
                }
                break;
        }
        return false;
    }

    private void playCarousel() {
        Log.d(TAG, "carousel size: " + mFetchDataControl.mCarousels.size());
        if (mCurrentCarouselIndex == mFetchDataControl.mCarousels.size() - 1) {
            mCurrentCarouselIndex = 0;
        } else {
            mCurrentCarouselIndex = mCurrentCarouselIndex + 1;
        }

        changeCarouselIcon(mCurrentCarouselIndex);

        Logger.t(TAG).d("play carousel position: " + mCurrentCarouselIndex);
        String videoUrl = mFetchDataControl.mCarousels.get(mCurrentCarouselIndex).getVideo_url();
        if (playSubscription != null && !playSubscription.isUnsubscribed()){
            playSubscription.unsubscribe();
        }
        playSubscription =
                Observable.just(videoUrl)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .map(
                                new Func1<String, Boolean>() {
                                    @Override
                                    public Boolean call(String s) {
                                        HttpUrl parsed = HttpUrl.parse(s);
                                        if (TextUtils.isEmpty(s) || parsed == null) {
                                            return false;
                                        }
                                        return externalStorageIsEnable();
                                    }
                                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Observer<Boolean>() {
                                    @Override
                                    public void onCompleted() {
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        playImage();
                                    }

                                    @Override
                                    public void onNext(Boolean enable) {
                                        if (enable) {
                                            playVideo(0);
                                        } else {
                                            playImage();
                                        }
                                    }
                                });
    }

    private boolean externalStorageIsEnable() {
        if (mChannel.equals("homepage")) {
            return true;
        }

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                final File file = new File(HardwareUtils.getSDCardCachePath(), "/text/test" + ".mp4");
                if (!file.getParentFile().exists()) {
                    boolean result = file.getParentFile().mkdirs();
                    if (!result) {
                        Log.i(TAG, "externalStorageIsEnable file.getParentFile().mkdirs()");
                        return false;
                    }
                }
                if (!file.exists()) {
                    boolean result = file.createNewFile();
                    if (!result) {
                        Log.i(TAG, "file.createNewFile()");
                        return false;
                    }
                }

                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write("hello world!!!");
                fileWriter.flush();
                fileWriter.close();

                FileReader fileReader = new FileReader(file);
                fileReader.read();
                fileReader.close();
                return true;
            } catch (IOException e) {
                ExceptionUtils.sendProgramError(e);
                Log.i(TAG, "externalStorageIsEnable IOException: " + e.getMessage());
                return false;
            }
        } else {
            Log.i(TAG, "externalStorageIsEnable not MEDIA_MOUNTED");
            return false;
        }
    }

    private void playImage() {
        if (mVideoView == null || mLoadingIg == null){
            return;
        }
        if (mVideoView.getVisibility() == View.VISIBLE) {
            mVideoView.setVisibility(View.GONE);
        }

        if (mLoadingIg.getVisibility() == View.GONE) {
            mLoadingIg.setVisibility(View.VISIBLE);
        }

        mVideoViewLayout.setTag(mFetchDataControl.mCarousels.get(mCurrentCarouselIndex));

        final String url = mFetchDataControl.mCarousels.get(mCurrentCarouselIndex).getVideo_image();
        String intro = mFetchDataControl.mCarousels.get(mCurrentCarouselIndex).getIntroduction();
        if (!StringUtils.isEmpty(intro)) {
            mVideoTitleTv.setVisibility(View.VISIBLE);
            mVideoTitleTv.setText(intro);
        } else {
            mVideoTitleTv.setVisibility(View.GONE);
        }
        final int pauseTime = mFetchDataControl.mCarousels.get(mCurrentCarouselIndex).getPause_time();
        Picasso.with(mContext)
                .load(url)
                .error(R.drawable.list_item_preview_bg)

                .into(
                        mLoadingIg,
                        new Callback() {

                            @Override
                            public void onSuccess() {
                                if (mHandler!= null){
                                    mHandler.sendEmptyMessageDelayed(CAROUSEL_NEXT, pauseTime * 1000);
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                if (mHandler != null){
                                    mHandler.sendEmptyMessageDelayed(CAROUSEL_NEXT, pauseTime * 1000);
                                }
                            }
                        });
    }

    private void playVideo(int delay) {
        if (mVideoView.getVisibility() == View.GONE) {
            mVideoView.setVisibility(View.VISIBLE);
        }

        if (mLoadingIg.getVisibility() == View.VISIBLE) {
            mLoadingIg.setVisibility(View.GONE);
        }

        mVideoViewLayout.setTag(mFetchDataControl.mCarousels.get(mCurrentCarouselIndex));

        String intro = mFetchDataControl.mCarousels.get(mCurrentCarouselIndex).getIntroduction();
        if (!StringUtils.isEmpty(intro)) {
            mVideoTitleTv.setVisibility(View.VISIBLE);
            mVideoTitleTv.setText(intro);
        } else {
            mVideoTitleTv.setVisibility(View.GONE);
        }
        mHandler.removeMessages(START_PLAYBACK);
        mHandler.sendEmptyMessageDelayed(START_PLAYBACK, delay);
    }

    // 视频播放
    private boolean startPlayback() {
        Log.d(TAG, "startPlayback is invoke...");

        mVideoView.setFocusable(false);
        mVideoView.setFocusableInTouchMode(false);
        String videoName = mChannel + "_" + mCurrentCarouselIndex + ".mp4";
        String videoPath;
        if (mChannel.equals("homepage")) {
            videoPath =
                    CacheManager.getInstance()
                            .doRequest(
                                    mFetchDataControl.mCarousels.get(mCurrentCarouselIndex).getVideo_url(),
                                    videoName,
                                    DownloadClient.StoreType.Internal);
        } else {
            videoPath =
                    CacheManager.getInstance()
                            .doRequest(
                                    mFetchDataControl.mCarousels.get(mCurrentCarouselIndex).getVideo_url(),
                                    videoName,
                                    DownloadClient.StoreType.External);
        }

        if (videoPath.startsWith("http://")) {
            return false;
        }

        Log.d(TAG, "current video path ====> " + videoPath);
        CallaPlay play = new CallaPlay();
        play.homepage_vod_trailer_play(videoPath, mChannel);
        mLoadingIg.setImageResource(R.drawable.guide_video_loading);
        mLoadingIg.setVisibility(View.VISIBLE);
        stopPlayback();
        initCallback();
        mVideoView.setVideoPath(videoPath);
        mVideoView.start();
        mVideoView.setFocusable(true);
        mVideoView.setFocusableInTouchMode(true);
        checkVideoViewFullVisibility();
        return true;
    }

    private void initCallback() {
        mOnCompletionListener =
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (checkVideoViewFullVisibilitySubsc != null
                                && checkVideoViewFullVisibilitySubsc.isUnsubscribed()) {
                            checkVideoViewFullVisibilitySubsc.unsubscribe();
                        }
                        stopPlayback();
                        mHandler.sendEmptyMessage(CAROUSEL_NEXT);
                    }
                };
        mVideoOnErrorListener =
                new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {

                        Log.e(TAG, "play video error!!!");
                        playCarousel();

                        return true;
                    }
                };
        mOnPreparedListener =
                new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        if (mp != null && !mp.isPlaying()) {
                            mp.start();
                        }
                        mLoadingIg.setVisibility(View.GONE);
                    }
                };

        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnErrorListener(mVideoOnErrorListener);
        mVideoView.setOnPreparedListener(mOnPreparedListener);
    }

    private void stopPlayback() {
        mVideoView.stopPlayback();
    }

    private void onVideoViewFullVisibility(boolean isFullVisibility) {
        if (isFullVisibility) {
            if (!mVideoView.isPlaying()) {
                mVideoView.start();
            }
        } else {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            }
        }
    }

    private void checkVideoViewFullVisibility() {
        if (checkVideoViewFullVisibilitySubsc!= null && !checkVideoViewFullVisibilitySubsc.isUnsubscribed()){
            checkVideoViewFullVisibilitySubsc.unsubscribe();
        }
        checkVideoViewFullVisibilitySubsc =
                Observable.interval(1, TimeUnit.SECONDS)
                        .takeUntil(
                                new Func1<Long, Boolean>() {
                                    @Override
                                    public Boolean call(Long aLong) {
                                        return false;
                                    }
                                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Observer<Long>() {
                                    @Override
                                    public void onCompleted() {
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                    }

                                    @Override
                                    public void onNext(Long aLong) {
                                        Rect rect = new Rect();
                                        mVideoView.getGlobalVisibleRect(rect);
                                        //                        Log.d(TAG, "mVideoView
                                        // getGlobalVisibleRect: " + rect);
                                        Rect rect2 = new Rect();
                                        mVideoView.getDrawingRect(rect2);
                                        //                        Log.d(TAG, "mVideoView
                                        // getDrawingRect: " + rect2);

                                        Rect rect3 = new Rect();
                                        mVideoView.getLocalVisibleRect(rect3);
                                        //                        Log.d(TAG, "mVideoView
                                        // getLocalVisibleRect: " + rect3);
                                        //                        Log.d(TAG, "mVideoView
                                        // ======================================================");
                                        if (videoViewVisibility) {
                                            if ((Math.abs(rect3.top - rect2.top)) > 10
                                                    || Math.abs(rect3.bottom - rect2.bottom) > 10
                                                    || Math.abs(rect3.left - rect2.left) > 10
                                                    || Math.abs(rect3.right - rect2.right) > 10) {
                                                onVideoViewFullVisibility(false);
                                            } else {
                                                onVideoViewFullVisibility(true);
                                            }
                                        } else {
                                            onVideoViewFullVisibility(false);
                                        }
                                    }
                                });
    }
}
