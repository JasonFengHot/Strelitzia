/*modify by dragontec for bug 4362 start*/
package tv.ismar.homepage.template;

import android.content.Context;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
//import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
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
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.util.HardwareUtils;
import tv.ismar.app.widget.RecyclerImageView;
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
	/*add by dragontec for bug 4077 start*/
import tv.ismar.homepage.widget.RecycleLinearLayout;
	/*add by dragontec for bug 4077 end*/
import tv.ismar.library.exception.ExceptionUtils;

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
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
		/*add by dragontec for bug 4242 start*/
		View.OnKeyListener,
		/*add by dragontec for bug 4242 end*/
        LinearLayoutManagerTV.FocusSearchFailedListener,
        OnItemSelectedListener,
        View.OnClickListener,
		Handler.Callback{
    private static final int CAROUSEL_NEXT = 0x0001;
    private static final int START_PLAYBACK = 0x0002;
    private DaisyVideoView mVideoView; // 导视view
    private RecyclerImageView mLoadingIg; // 加载提示logo
    private TextView mVideoTitleTv; // 导视标题
    private TextView mFirstIcon; // 第一个视频指示数字
    private TextView mSecondIcon;
    private TextView mThirdIcon;
    private TextView mFourIcon;
    private TextView mFiveIcon;
/*delete by dragontec for bug 4332 start*/
//    private RecyclerViewTV mRecycleView; // 海报recycleview
/*delete by dragontec for bug 4332 end*/
    private LinearLayoutManagerTV mGuideLayoutManager;
    private GuideAdapter mAdapter;

    private BannerLinearLayout mBannerLinearLayout;
/*delete by dragontec for bug 4332 start*/
//    private View navigationLeft;
//    private View navigationRight;
/*delete by dragontec for bug 4332 end*/

    private int mCurrentCarouselIndex = -1;
    private boolean videoViewVisibility = true;
    private Subscription playSubscription;
    private Subscription checkVideoViewFullVisibilitySubsc;

    private View mVideoViewLayout;
	/*delete by dragontec for bug 4332 start*/
//    private View mHeadView; // recylview头view
	/*delete by dragontec for bug 4332 end*/
    private String mName; // 频道名称（中文）
    private String mChannel; // 频道名称（英文）
    private int locationY;
    private Handler mHandler;

	/*add by dragontec for bug 4334 start*/
    private boolean isPlayed = false;
    /*add by dragontec for bug 4334 end*/

/*add by dragontec for bug 4415 start*/
    private Vector<String> mDownloadUrlVector = new Vector<>();
/*add by dragontec for bug 4415 end*/

/*add by dragontec for bug 4065 start*/
    private final static String PLAYER_HANDLER_THREAD_NAME = "PLAYER_HANDLER_THREAD_NAME";
    private static HandlerThread mPlayerHandlerThread = null;
    private static Handler mPlayerHandler = null;
    private static PlayerActionRunnable mPlayerActionRunnable = null;
    private final static Object stLock = new Object();

    static
    {
        synchronized (stLock)
        {
            if(mPlayerHandlerThread == null)
            {
                mPlayerHandlerThread = new HandlerThread(PLAYER_HANDLER_THREAD_NAME);
                mPlayerHandlerThread.start();
                mPlayerHandler = new Handler(mPlayerHandlerThread.getLooper());
            }
        }
    }

	@Override
	public boolean handleMessage(Message msg) {
    	boolean ret = false;
		try {
			switch (msg.what) {
				case START_PLAYBACK:
					// 如果视频没有下载完，播放图片
					if (!startPlayback()) {
						playImage();
					}
					ret = true;
					break;
				case CAROUSEL_NEXT:
					playCarousel();
					ret = true;
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	private class PlayerActionRunnable implements Runnable {

        private DaisyVideoView daisyView = null;
        public PlayerActionRunnable(DaisyVideoView videoView)
        {
            this.daisyView = videoView;
        }
        @Override
        public void run() {
            synchronized (stLock)
            {
                if (mPlayerHandler != null) {
                    mPlayerHandler.removeCallbacks(this);
                }
                if (this.daisyView != null) {
                    Log.d(TAG, "daisyView = " + daisyView + ", release daisyView");
                        this.daisyView.release(true);
                }
                mPlayerActionRunnable = null;
            }
        }

        public DaisyVideoView getVideoView()
        {
            return daisyView;
        }
    }

    private void stopVideoViewNormal()
    {
        if (mPlayerActionRunnable != null) {
            if (mPlayerHandler != null) {
                mPlayerHandler.removeCallbacks(mPlayerActionRunnable);
            }
            DaisyVideoView tempView = mPlayerActionRunnable.getVideoView();
            if(tempView != null) {
                if (tempView.isPlaying()) {
                    tempView.stopPlayback();
                }
            }
            mPlayerActionRunnable = null;
        }
    }

    private void sendStopPlayerMessage(DaisyVideoView videoView) {
        stopVideoViewNormal();
        mPlayerActionRunnable = new PlayerActionRunnable(videoView);
        if (mPlayerHandler != null) {
            mPlayerHandler.post(mPlayerActionRunnable);
        }
    }
/*add by dragontec for bug 4065 end*/

	/*modify by dragontec for bug 4334 start*/
    public TemplateGuide(Context context, int position, FetchDataControl fetchDataControl) {
        super(context, position, fetchDataControl);
        mHandler = new Handler(this);
    }
    /*modify by dragontec for bug 4334 end*/

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        synchronized (stLock)
        {
            stopVideoViewNormal();
			if (mFetchControl.mCarouselsMap.get(mBannerPk) != null && !mFetchControl.mCarouselsMap.get(mBannerPk).isEmpty()) {
                if (videoViewIsVisibility()){
                    playCarousel();
                    initCarousel();
                }else {
                    checkVideoViewFullVisibility();
                }
            }
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {
        Logger.t(TAG).d("onPause");

	/*add by dragontec for bug 4077 start*/
        super.onPause();
	/*add by dragontec for bug 4077 end*/
    }

    @Override
    public void onStop() {
        Logger.t(TAG).d("onStop");
        if (mHandler != null) {
            mHandler.removeMessages(CAROUSEL_NEXT);
            mHandler.removeMessages(START_PLAYBACK);
        }

/*modify by dragontec for bug 4065 start*/
//        if (mVideoView != null) {
//            if (mVideoView.isPlaying()) {
//                mVideoView.stopPlayback();
//            }
//        }
        sendStopPlayerMessage(mVideoView);
/*modify by dragontec for bug 4065 end*/

        if (playSubscription != null && !playSubscription.isUnsubscribed()) {
            playSubscription.unsubscribe();
        }

        if (checkVideoViewFullVisibilitySubsc != null
                && !checkVideoViewFullVisibilitySubsc.isUnsubscribed()) {
            checkVideoViewFullVisibilitySubsc.unsubscribe();
        }

/*add by dragontec for bug 4415 start*/
        stopRequestDownload();
/*add by dragontec for bug 4415 end*/
    }

/*add by dragontec for bug 4415 start*/
    private void stopRequestDownload() {
        if (mDownloadUrlVector != null) {
            for (String downloadUrl : mDownloadUrlVector) {
                if (downloadUrl != null) {
                    Log.d(TAG, "stopRequestDownload remove download url = " + downloadUrl);
                    CacheManager.getInstance().stopRequest(downloadUrl);
                }
            }
            mDownloadUrlVector.clear();
        }
    }
/*add by dragontec for bug 4415 end*/

    @Override
    public void onDestroy() {
        synchronized (stLock)
        {
            mHandler = null;
            playSubscription = null;
            checkVideoViewFullVisibilitySubsc = null;
            if (mPlayerActionRunnable != null) {
                if (mPlayerHandler != null) {
                    mPlayerHandler.removeCallbacks(mPlayerActionRunnable);
                }
                mPlayerActionRunnable = null;
            }
        }

		super.onDestroy();

    }

    @Override
    public void getView(View view) {
        mHeadView = view.findViewById(R.id.banner_guide_head);
        //        mHeadView.findViewById(R.id.guide_head_ismartv_linearlayout).setFocusable(true);
        //        mHeadView.findViewById(R.id.guide_head_ismartv_linearlayout).requestFocus();
        mVideoView = (DaisyVideoView) view.findViewById(R.id.guide_daisy_video_view);
/*add by dragontec for bug 4065 start*/
		mVideoView.setManualReset(true);
/*add by dragontec for bug 4065 end*/
        mLoadingIg = (RecyclerImageView) view.findViewById(R.id.guide_video_loading_image);
        mVideoTitleTv = (TextView) view.findViewById(R.id.guide_video_title);
        mFirstIcon = (TextView) view.findViewById(R.id.first_video_icon);
        mSecondIcon = (TextView) view.findViewById(R.id.second_video_icon);
        mThirdIcon = (TextView) view.findViewById(R.id.third_video_icon);
        mFourIcon = (TextView) view.findViewById(R.id.four_video_icon);
        mFiveIcon = (TextView) view.findViewById(R.id.five_video_icon);
        mVideoViewLayout = view.findViewById(R.id.guide_head_ismartv_linearlayout);

		mGuideLayoutManager =
				new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
/*modify by dragontec for bug 4332 start*/
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.guide_recyclerview);
		/*modify by dragontec for bug 4221 start*/
        mRecyclerView.setTag("recycleView");
		/*modify by dragontec for bug 4221 end*/
        mRecyclerView.setLayoutManager(mGuideLayoutManager);
        mRecyclerView.setSelectedItemOffset(100, 100);
        navigationLeft = view.findViewById(R.id.navigation_left);
        navigationRight = view.findViewById(R.id.navigation_right);
        mBannerLinearLayout = (BannerLinearLayout) view.findViewById(R.id.banner_layout);
        mBannerLinearLayout.setNavigationLeft(navigationLeft);
        mBannerLinearLayout.setNavigationRight(navigationRight);
        mBannerLinearLayout.setRecyclerViewTV(mRecyclerView);
/*modify by dragontec for bug 4332 end*/
/*add by dragontec for bug 4275 start*/
        mBannerLinearLayout.setHeadView(mHeadView);
/*add by dragontec for bug 4275 end*/
/*add by dragontec for bug 4332 start*/
		mHoverView = view.findViewById(R.id.hover_view);
/*add by dragontec for bug 4332 end*/
    }

	@Override
	public void clearView() {
		mHoverView = null;
		if (mBannerLinearLayout != null) {
			mBannerLinearLayout.setHeadView(null);
			mBannerLinearLayout.setRecyclerViewTV(null);
			mBannerLinearLayout.setNavigationRight(null);
			mBannerLinearLayout.setNavigationLeft(null);
			mBannerLinearLayout = null;
		}
		navigationRight = null;
		navigationLeft = null;
		if (mRecyclerView != null) {
			mRecyclerView.setLayoutManager(null);
			mRecyclerView.setAdapter(null);
			mRecyclerView = null;
		}
		mGuideLayoutManager = null;
		mVideoViewLayout = null;
		mFirstIcon = null;
		mFourIcon = null;
		mThirdIcon = null;
		mSecondIcon = null;
		mFirstIcon = null;
		mVideoTitleTv = null;
		mLoadingIg = null;
		if (mVideoView != null) {
			mVideoView.setOnFocusChangeListener(null);
			mVideoView.setOnCompletionListener(null);
			mVideoView.setOnErrorListener(null);
			mVideoView.setOnPreparedListener(null);
			mVideoView = null;
		}
		mHeadView = null;
	}

	@Override
    public void initData(Bundle bundle) {
    	initAdapter();
        mBannerPk = bundle.getString(BANNER_KEY);
        mName = bundle.getString("title");
        mChannel = bundle.getString(CHANNEL_KEY);
        locationY=bundle.getInt(BANNER_LOCATION,0);
/*modify by dragontec for bug 4334 start*/
		if (mFetchControl.getHomeEntity(mBannerPk) != null) {
			isNeedFillData = true;
			checkViewAppear();
		}
    }

	@Override
	public void unInitData() {
		unInitAdapter();
	}

	@Override
	public void fetchData() {
		hasAppeared = true;
	}

	@Override
	public void fillData() {
    	Log.d(TAG, "fillData isDataInited = " + isDataInited);
    	if (isNeedFillData) {
			isNeedFillData = false;
			initRecycleView();
			if (!isPlayed) {
				playCarousel();
				initCarousel();
			}
		}
	}

/*modify by dragontec for bug 4334 end*/

    @Override
    protected void initListener(View view) {
		super.initListener(view);
        navigationLeft.setOnClickListener(this);
        navigationRight.setOnClickListener(this);
        mRecyclerView.setPagingableListener(this);
        mVideoView.setOnFocusChangeListener(this);
        mGuideLayoutManager.setFocusSearchFailedListener(this);
        mHeadView.findViewById(R.id.guide_head_ismartv_linearlayout).setOnHoverListener(this);
        mVideoViewLayout.setOnClickListener(this);
		mVideoViewLayout.setOnKeyListener(this);
        mLoadingIg.setOnFocusChangeListener(this);
        mLoadingIg.setOnHoverListener(this);
    }

	@Override
	protected void unInitListener() {
    	mLoadingIg.setOnHoverListener(null);
    	mLoadingIg.setOnFocusChangeListener(null);
    	mVideoViewLayout.setOnKeyListener(null);
    	mVideoViewLayout.setOnClickListener(null);
		mHeadView.findViewById(R.id.guide_head_ismartv_linearlayout).setOnHoverListener(null);
		mGuideLayoutManager.setFocusSearchFailedListener(null);
		mVideoView.setOnFocusChangeListener(null);
		mRecyclerView.setPagingableListener(null);
		navigationRight.setOnClickListener(null);
		navigationLeft.setOnClickListener(null);
		super.unInitListener();
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
        int size = mFetchControl.mCarouselsMap.get(mBannerPk).size();
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

	/*modify by dragontec for bug 4334 start*/
    private void initAdapter() {
    	if (mAdapter == null) {
			mAdapter = new GuideAdapter(mContext);
			mAdapter.setMarginLeftEnable(true);
			mAdapter.setOnItemClickListener(this);
			mAdapter.setOnItemSelectedListener(this);
		}
	}

	private void unInitAdapter() {
    	if (mAdapter != null) {
    		mAdapter.setOnItemSelectedListener(null);
    		mAdapter.setOnItemClickListener(null);
    		mAdapter.clearData();
    		mAdapter = null;
		}
	}

    /*初始化RecycleView*/
    private void initRecycleView() {
    	if (mAdapter != null) {
    		if (mAdapter.getData() == null) {
    			if (mFetchControl.mPosterMap.get(mBannerPk) != null) {
					mAdapter.setData(mFetchControl.mPosterMap.get(mBannerPk));
					/*modify by dragontec for bug 4412 start*/
					if (mAdapter.getItemCount() > 0) {
						setVisibility(VISIBLE);
					}
					/*modify by dragontec for bug 4412 end*/
/*modify by dragontec for bug 4332 start*/
					mRecyclerView.setAdapter(mAdapter);
/*modify by dragontec for bug 4332 end*/
				}
			} else {
    			/*modify by dragontec for bug 4412 start*/
				if (mAdapter.getItemCount() > 0) {
					setVisibility(VISIBLE);
				}
				/*modify by dragontec for bug 4412 end*/
				int start = mFetchControl.mPosterMap.get(mBannerPk).size() - mFetchControl.getHomeEntity(mBannerPk).posters.size();
				int end = mFetchControl.mPosterMap.get(mBannerPk).size();
				mAdapter.notifyItemRangeInserted(start, end - start + 1);
			}
		}

    }
    /*modify by dragontec for bug 4334 end*/

//    @Override
//    public void callBack(int flags, Object... args) { // 获取网络数据回调
//		switch (flags) {
//			case FetchDataControl.FETCH_BANNERS_LIST_FLAG: {
//				isNeedFillData = true;
//				initAdapter();
//				checkViewAppear();
//				mRecyclerView.setOnLoadMoreComplete();
//			}
//			break;
//			case FetchDataControl.FETCH_DATA_FAIL_FLAG: {
//				if (mRecyclerView.isOnLoadMore()) {
//					mFetchControl.getHomeEntity(mBannerPk).page--;
//					mRecyclerView.setOnLoadMoreComplete();
//				}
//			}
//			break;
//		}
//    }

    @Override
    public void onLoadMoreItems() {
        Log.i(TAG, "onLoadMoreItems");
        HomeEntity homeEntity = mFetchControl.getHomeEntity(mBannerPk);
        if (homeEntity != null) {
            if (homeEntity.page < homeEntity.num_pages) {
	/* modify by dragontec for bug 4264 start */
                mFetchControl.fetchBanners(mBannerPk, ++homeEntity.page, true);
            } else {
/*modify by dragontec for bug 4332 start*/
				mRecyclerView.setOnLoadMoreComplete();
/*modify by dragontec for bug 4332 end*/
	/* modify by dragontec for bug 4264 end */
			}
        }
    }

    @Override
    public void onItemClick(View view, int position) { // item点击事件
        if (position == mFetchControl.getHomeEntity(mBannerPk).count - 1) {
            new PageIntent()
                    .toListPage(
                            mContext,
                            mFetchControl.getHomeEntity(mBannerPk).channel_title,
                            mFetchControl.getHomeEntity(mBannerPk).channel,
                            Integer.valueOf(mFetchControl.getHomeEntity(mBannerPk).style),
                            mFetchControl.getHomeEntity(mBannerPk).section_slug);
            mFetchControl.launcher_vod_click("section","-1","更多",locationY+","+(position+2),mChannel);
        } else {
            mFetchControl.go2Detail(mFetchControl.getHomeEntity(mBannerPk).posters.get(position));
            BannerPoster entity=mAdapter.getData().get(position);
            mFetchControl.launcher_vod_click(entity.model_name,entity.pk+"",entity.title,locationY+","+(position+2),mChannel);
        }
    }

    @Override
    public View onFocusSearchFailed(
            View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
/*modify by dragontec for bug 4332 start*/
            if (mRecyclerView.getChildAt(0).findViewById(R.id.guide_ismartv_linear_layout) == focused) {
                mHeadView.requestFocus();
        /*modify by dragontec for bug 4242 start*/
//                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(mHeadView);
        /*modify by dragontec for bug 4242 end*/
                return mHeadView;
            }
            if (mRecyclerView
                    .getChildAt(mRecyclerView.getChildCount() - 1)
                    .findViewById(R.id.guide_ismartv_linear_layout)
                    == focused) {
                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
                return focused;
            }
/*modify by dragontec for bug 4332 end*/
        }
/*add by dragontec for bug 4331 start*/
		if (isLastView && focusDirection == View.FOCUS_DOWN) {
			YoYo.with(Techniques.VerticalShake).duration(1000).playOn(focused);
		}
/*add by dragontec for bug 4331 end*/
        /*modify by dragontec for bug 4221 start*/
        /*modify by dragontec for bug 4338 start*/
        return findNextUpDownFocus(focusDirection, mBannerLinearLayout, focused);
        /*modify by dragontec for bug 4338 end*/
        /*modify by dragontec for bug 4221 end*/
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
			if (!mRecyclerView.isNotScrolling()) {
				return;
			}
			if (mGuideLayoutManager.findFirstCompletelyVisibleItemPosition() > 0) {
				mGuideLayoutManager.setCanScroll(true);
				int targetPosition = mGuideLayoutManager.findFirstCompletelyVisibleItemPosition() - 5;
				if (targetPosition < 0) {
					targetPosition = 0;
				}
				setNeedCheckScrollEnd();
				mGuideLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
			} else {
				mHeadView.setVisibility(View.VISIBLE);
				videoViewVisibility = true;
				checkNavigationButtonVisibility();
			}
//            mGuideLayoutManager.setCanScroll(true);
//            if (mGuideLayoutManager.findFirstCompletelyVisibleItemPosition() > 0) { // 向左滑动
//                int targetPosition = mGuideLayoutManager.findFirstCompletelyVisibleItemPosition() - 5;
//                if (targetPosition <= 0) targetPosition = 0;
///*add by dragontec for bug 4332 start*/
//				setNeedCheckScrollEnd();
///*add by dragontec for bug 4332 end*/
///*modify by dragontec for bug 4332 start*/
//                mGuideLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
///*modify by dragontec for bug 4332 end*/
//            }else{
//                mHeadView.setVisibility(View.VISIBLE);
//                videoViewVisibility = true;
///*add by dragontec for bug 4332 start*/
//				checkNavigationButtonVisibility();
///*add by dragontec for bug 4332 end*/
//            }
        } else if (i == R.id.navigation_right) {
			if (!mRecyclerView.isNotScrolling()) {
				return;
			}
            mGuideLayoutManager.setCanScroll(true);
            // 向右滑动
/*modify by dragontec for bug 4332 start*/
            mRecyclerView.loadMore();
/*modify by dragontec for bug 4332 end*/
			mHeadView.setVisibility(View.GONE);

        /*modify by dragontec for bug 4240 start*/
			Log.d(TAG, "findLastCompletelyVisibleItemPosition = " + mGuideLayoutManager.findLastCompletelyVisibleItemPosition() + ", count = " + mAdapter.getItemCount());
			if (mGuideLayoutManager.findLastCompletelyVisibleItemPosition() < mAdapter.getItemCount() - 1) {
				/*modify by dragontec for bug 4347 start*/
                int targetPosition = 0;
                Rect rectRecyclerView = new Rect();
                mRecyclerView.getGlobalVisibleRect(rectRecyclerView);
			    if(rectRecyclerView.left > 0){
                    if(mAdapter.getItemCount() > 1) {
                        View secondView = mRecyclerView.getChildAt(1);
                        Rect rect = new Rect();
                        secondView.getGlobalVisibleRect(rect);
                        int delta = rect.left - rectRecyclerView.left - mRecyclerView.getResources().getDimensionPixelSize(R.dimen.history_50);
                        setNeedCheckScrollEnd();
                        mRecyclerView.smoothScrollBy(delta,0);
                        return;
                    }
                }else{
                    targetPosition = mGuideLayoutManager.findLastCompletelyVisibleItemPosition() + 5;
                }
				/*modify by dragontec for bug 4347 end*/
				if (targetPosition >= mAdapter.getItemCount()) {
					targetPosition = mAdapter.getItemCount() - 1;
				}
/*add by dragontec for bug 4332 start*/
				setNeedCheckScrollEnd();
/*add by dragontec for bug 4332 end*/
/*modify by dragontec for bug 4332 start*/
				mGuideLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
/*modify by dragontec for bug 4332 end*/
			}
        /*modify by dragontec for bug 4240 end*/
        } else if (i == R.id.guide_head_ismartv_linearlayout) {
            Log.d(TAG, "onClick goToNextPage");
            goToNextPage(v);
            mFetchControl.launcher_vod_click("item",mBannerPk,mName,locationY+","+1,mChannel);
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
			/*delete by dragontec for bug 4169 start*/
        	//case MotionEvent.ACTION_HOVER_MOVE:
			/*delete by dragontec for bug 4169 end*/
            case MotionEvent.ACTION_HOVER_ENTER:
                if (!v.hasFocus()) {
                    v.requestFocus();
                    v.requestFocusFromTouch();
                }
                return false;
            case MotionEvent.ACTION_HOVER_EXIT:
                if (event.getButtonState() != BUTTON_PRIMARY) {
/*delete by dragontec for bug 4332 start*/
//                    navigationLeft.setVisibility(View.INVISIBLE);
//                    navigationRight.setVisibility(View.INVISIBLE);
/*delete by dragontec for bug 4332 end*/
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
    	//修正无海报数据时不显示的问题
    	if (mFetchControl.mCarouselsMap.get(mBannerPk) == null || mFetchControl.mCarouselsMap.get(mBannerPk).size() == 0) {
    		return;
		}
		setVisibility(VISIBLE);
		isPlayed = true;
        if (mCurrentCarouselIndex == mFetchControl.mCarouselsMap.get(mBannerPk).size() - 1) {
            mCurrentCarouselIndex = 0;
        } else {
            mCurrentCarouselIndex = mCurrentCarouselIndex + 1;
        }

        changeCarouselIcon(mCurrentCarouselIndex);

        Logger.t(TAG).d("play carousel position: " + mCurrentCarouselIndex);
        String videoUrl = mFetchControl.mCarouselsMap.get(mBannerPk).get(mCurrentCarouselIndex).video_url;
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

        mVideoViewLayout.setTag(mFetchControl.mCarouselsMap.get(mBannerPk).get(mCurrentCarouselIndex));

        final String url = mFetchControl.mCarouselsMap.get(mBannerPk).get(mCurrentCarouselIndex).video_image;
        String tilte = mFetchControl.mCarouselsMap.get(mBannerPk).get(mCurrentCarouselIndex).title;
        if (!StringUtils.isEmpty(tilte)) {
            mVideoTitleTv.setVisibility(View.VISIBLE);
            mVideoTitleTv.setText(tilte);
        } else {
            mVideoTitleTv.setVisibility(View.GONE);
        }
        final int pauseTime = mFetchControl.mCarouselsMap.get(mBannerPk).get(mCurrentCarouselIndex).pause_time;
 		/*modify by dragontec for bug 4428 start*/
        if(url == null || url.equals("")){
            Picasso.with(mContext)
                    .load(R.drawable.guide_video_loading)
                    .placeholder(R.drawable.guide_video_loading)
                    .error(R.drawable.guide_video_loading)
                    .into(
							mLoadingIg,
							new com.squareup.picasso.Callback() {
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
        }else{
			/*modify by dragontec for bug 4428 end*/
            /*modify by dragontec for bug 4336 start*/
            Picasso.with(mContext)
                    .load(url)
                    .placeholder(R.drawable.guide_video_loading)
                    .error(R.drawable.guide_video_loading)
                    .into(
                            mLoadingIg,
							new com.squareup.picasso.Callback() {
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
        /*modify by dragontec for bug 4336 end*/
        }

    }

    private void playVideo(int delay) {
        if (mVideoView.getVisibility() == View.GONE) {
            mVideoView.setVisibility(View.VISIBLE);
        }
        mLoadingIg.setImageResource(R.drawable.guide_video_loading);
        if (mLoadingIg.getVisibility() != View.VISIBLE) {
            mLoadingIg.setVisibility(View.VISIBLE);
        }

        mVideoViewLayout.setTag(mFetchControl.mCarouselsMap.get(mBannerPk).get(mCurrentCarouselIndex));

        String title = mFetchControl.mCarouselsMap.get(mBannerPk).get(mCurrentCarouselIndex).title;
        if (!StringUtils.isEmpty(title)) {
            mVideoTitleTv.setVisibility(View.VISIBLE);
            mVideoTitleTv.setText(title);
        } else {
            mVideoTitleTv.setVisibility(View.GONE);
        }
        mHandler.removeMessages(START_PLAYBACK);
        mHandler.sendEmptyMessageDelayed(START_PLAYBACK, delay);
    }

    // 视频播放
    private boolean startPlayback() {
        Log.d(TAG, "startPlayback is invoke...");
		if (mVideoView == null) {
			return false;
		}
        mVideoView.setFocusable(false);
        mVideoView.setFocusableInTouchMode(false);
        String videoName = mChannel + "_" + mCurrentCarouselIndex + ".mp4";
        String videoPath;
        if (mChannel.equals("homepage")) {
            videoPath =
                    CacheManager.getInstance()
                            .doRequest(
                                    mFetchControl.mCarouselsMap.get(mBannerPk).get(mCurrentCarouselIndex).video_url,
                                    videoName,
                                    DownloadClient.StoreType.Internal);
        } else {
            videoPath =
                    CacheManager.getInstance()
                            .doRequest(
                                    mFetchControl.mCarouselsMap.get(mBannerPk).get(mCurrentCarouselIndex).video_url,
                                    videoName,
                                    DownloadClient.StoreType.External);
        }
        /*add by dragontec for bug 4415 start*/
		Log.d(TAG, "startPlayback do request result url = " + videoPath);
		if (!videoPath.startsWith("file://") && !mDownloadUrlVector.contains(videoPath)) {
			Log.d(TAG, "startPlayback add to download url = " + videoPath);
			mDownloadUrlVector.add(videoPath);
		}
/*add by dragontec for bug 4415 end*/

        if (videoPath.startsWith("http://")) {
            return false;
        }

        Log.d(TAG, "current video path ====> " + videoPath);
        CallaPlay play = new CallaPlay();
        play.homepage_vod_trailer_play(videoPath, mChannel);
/*add by dragontec for bug 4205 start*/
        mLoadingIg.setImageDrawable(null);
/*add by dragontec for bug 4205 end*/
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
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (checkVideoViewFullVisibilitySubsc != null
						&& checkVideoViewFullVisibilitySubsc.isUnsubscribed()) {
					checkVideoViewFullVisibilitySubsc.unsubscribe();
				}
				stopPlayback();
				mHandler.sendEmptyMessage(CAROUSEL_NEXT);
			}
		});
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {

				Log.e(TAG, "play video error!!!");
/*modify by dragontec for bug 4507 start*/
//                playCarousel();
                playImage();
/*modify by dragontec for bug 4507 end*/

				return true;
			}
		});
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				synchronized (stLock) {
					if (mp != null && !mp.isPlaying()) {
						mp.start();
					}
					if (mLoadingIg != null) {
						mLoadingIg.setVisibility(View.GONE);
					}
				}
			}
		});
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
//                                                                Log.d(TAG, "mVideoView getGlobalVisibleRect: " + rect);
                                        Rect rect2 = new Rect();
                                        mVideoView.getDrawingRect(rect2);
//                                                                Log.d(TAG, "mVideoView getDrawingRect: " + rect2);

                                        Rect rect3 = new Rect();
                                        mVideoView.getLocalVisibleRect(rect3);
//                                                                Log.d(TAG, "mVideoView getLocalVisibleRect: " + rect3);
//                                                                Log.d(TAG, "mVideoView ======================================================");
                                        if (videoViewVisibility) {
                                            if ((Math.abs(rect3.top - rect2.top)) > 10
                                                    || Math.abs(rect3.bottom - rect2.bottom) > 10
                                                    || Math.abs(rect3.left - rect2.left) > 10
                                                    || Math.abs(rect3.right - rect2.right) > 10
                                                    || mHeadView.getVisibility() == View.INVISIBLE
                                                    || mHeadView.getVisibility() == View.GONE) {
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

	/*add by dragontec for bug 4242 start*/
    private View keyDownView = null;
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && v == mVideoViewLayout) {
                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(mHeadView);
                return true;
            }
		}
		return false;
	}
	/*add by dragontec for bug 4242 end*/

    private boolean videoViewIsVisibility(){
        Rect rect = new Rect();
        mVideoView.getGlobalVisibleRect(rect);
//        Log.d(TAG, "mVideoView getGlobalVisibleRect: " + rect);
        Rect rect2 = new Rect();
        mVideoView.getDrawingRect(rect2);
//        Log.d(TAG, "mVideoView getDrawingRect: " + rect2);

        Rect rect3 = new Rect();
        mVideoView.getLocalVisibleRect(rect3);
//        Log.d(TAG, "mVideoView getLocalVisibleRect: " + rect3);
//        Log.d(TAG, "mVideoView ======================================================");


            if ((Math.abs(rect3.top - rect2.top)) > 10
                    || Math.abs(rect3.bottom - rect2.bottom) > 10
                    || Math.abs(rect3.left - rect2.left) > 10
                    || Math.abs(rect3.right - rect2.right) > 10
                    || mHeadView.getVisibility() == View.INVISIBLE
                    || mHeadView.getVisibility() == View.GONE) {
               return false;
            } else {
                return true;
            }

    }
}
/*modify by dragontec for bug 4362 end*/
