package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
	/*add by dragontec for bug 4077 start*/
import android.os.Handler;
	/*add by dragontec for bug 4077 end*/
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.banner.adapter.BannerMovieMixAdapter;
import tv.ismar.homepage.fragment.ChannelFragment;
import tv.ismar.homepage.view.BannerLinearLayout;
	/*add by dragontec for bug 4077 start*/
import tv.ismar.homepage.widget.RecycleLinearLayout;
	/*add by dragontec for bug 4077 end*/

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: 大横小竖模版
 */
public class TemplateBigSmallLd extends Template
        implements View.OnHoverListener, View.OnClickListener {
    private static final String TAG = TemplateBigSmallLd.class.getSimpleName();
/*delete by dragontec for bug 4332 start*/
//    private RecyclerViewTV movieMixBanner;
/*delete by dragontec for bug 4332 end*/

    private BannerMovieMixAdapter adapter;
    private String mBannerName;

    private TextView mTitleTv;
    private String mBannerTitle;

/*delete by dragontec for bug 4332 start*/
//    private View navigationLeft;
//    private View navigationRight;
/*delete by dragontec for bug 4332 end*/
    private BannerLinearLayout mBannerLinearLayout;
    private LinearLayoutManagerTV movieMixLayoutManager;
    private String channelName;
    private String nameKey;
    private boolean isMore;
    private Subscription fetchMovieMixBanner;

    private static final int NAVIGATION_LEFT = 0x0001;
    private static final int NAVIGATION_RIGHT = 0x0002;

    private NavigationtHandler mNavigationtHandler;

    private class NavigationtHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
/*delete by dragontec for bug 4332 start*/
//            switch (msg.what){
//                case NAVIGATION_LEFT:
//                    if (mRecyclerView!=null&&!mRecyclerView.cannotScrollBackward(-10)) {
//                        navigationLeft.setVisibility(VISIBLE);
//                    }else if (mRecyclerView!=null){
//                        navigationLeft.setVisibility(INVISIBLE);
//                    }
//                    break;
//                case NAVIGATION_RIGHT:
//                    if(mRecyclerView!=null&&!mRecyclerView.cannotScrollForward(10)){
//                        navigationRight.setVisibility(VISIBLE);
//                    }else if (mRecyclerView!=null){
//                        navigationRight.setVisibility(INVISIBLE);
//                    }
//                    break;
//            }
/*delete by dragontec for bug 4332 end*/
        }
    }

	/*modify by dragontec for bug 4334 start*/
    public TemplateBigSmallLd(Context context, int position) {
        super(context, position);
        mNavigationtHandler = new NavigationtHandler();
    }
    /*modify by dragontec for bug 4334 end*/

    @Override
    public void onCreate() {
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
        if (fetchMovieMixBanner != null && !fetchMovieMixBanner.isUnsubscribed()){
            fetchMovieMixBanner.unsubscribe();
        }
	/*add by dragontec for bug 4077 start*/
		super.onPause();
	/*add by dragontec for bug end start*/
    }

    @Override
    public void onStop() {
        if (mNavigationtHandler.hasMessages(NAVIGATION_LEFT)){
            mNavigationtHandler.removeMessages(NAVIGATION_LEFT);
        }
        if (mNavigationtHandler.hasMessages(NAVIGATION_RIGHT)){
            mNavigationtHandler.removeMessages(NAVIGATION_RIGHT);
        }
    }

    @Override
    public void onDestroy() {
/*add by dragontec for bug 4205 start*/
        if (adapter != null) {
            adapter.setHoverListener(null);
            adapter.setSubscribeClickListener(null);
        }
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(null);
            mRecyclerView.setAdapter(null);
        }
        if (mBannerLinearLayout != null) {
            mBannerLinearLayout.setNavigationLeft(null);
            mBannerLinearLayout.setNavigationRight(null);
            mBannerLinearLayout.setRecyclerViewTV(null);
            mBannerLinearLayout.setHeadView(null);
        }
/*add by dragontec for bug 4205 end*/
        if (mNavigationtHandler != null){
            mNavigationtHandler = null;
        }
    }

    @Override
    public void getView(View view) {
        navigationLeft = view.findViewById(R.id.navigation_left);
        navigationRight = view.findViewById(R.id.navigation_right);
        navigationLeft.setOnClickListener(this);
        navigationRight.setOnClickListener(this);
        navigationRight.setOnHoverListener(this);
        navigationLeft.setOnHoverListener(this);

        mBannerLinearLayout = (BannerLinearLayout) view.findViewById(R.id.banner_layout);
        mBannerLinearLayout.setNavigationLeft(navigationLeft);
        mBannerLinearLayout.setNavigationRight(navigationRight);

        mTitleCountTv = (TextView) view.findViewById(R.id.banner_title_count);
        mTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
/*modify by dragontec for bug 4332 start*/
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.movie_mix_banner);
		/*modify by dragontec for bug 4221 start*/
        mRecyclerView.setTag("recycleView");
		/*modify by dragontec for bug 4221 end*/
        mRecyclerView.setHasHeaderView(true);
        movieMixLayoutManager =
                new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mBannerLinearLayout.setRecyclerViewTV(mRecyclerView);
        int selectedItemSpace =
                mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_SelectedItemSpace);
        //        mRecyclerView.addItemDecoration(new
        // BannerMovieMixAdapter.SpacesItemDecoration(selectedItemSpace));
        mRecyclerView.setLayoutManager(movieMixLayoutManager);
        mRecyclerView.setSelectedItemAtCentered(false);
        int selectedItemOffset =
                mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
        //        mRecyclerView.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);
        mRecyclerView.setPagingableListener(
                new RecyclerViewTV.PagingableListener() {
                    @Override
                    public void onLoadMoreItems() {
                        Log.d("PagingableListener", "onLoadMoreItems");
                        if (adapter != null) {
                            int currentPageNumber = adapter.getCurrentPageNumber();
                            if (currentPageNumber < adapter.getTotalPageCount()) {
                                fetchMovieMixBanner(mBannerName, currentPageNumber + 1);
                            }
                        }
                    }
                });
/*modify by dragontec for bug 4332 end*/

        movieMixLayoutManager.setFocusSearchFailedListener(
                new LinearLayoutManagerTV.FocusSearchFailedListener() {
                    @Override
                    public View onFocusSearchFailed(
                            View view,
                            int focusDirection,
                            RecyclerView.Recycler recycler,
                            RecyclerView.State state) {
                        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
/*modify by dragontec for bug 4332 start*/
                            if (mRecyclerView.getChildAt(0).findViewById(R.id.item_layout) == view
                                    || mRecyclerView
                                    .getChildAt(mRecyclerView.getChildCount() - 1)
                                    .findViewById(R.id.item_layout)
                                    == view) {
                                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(view);
                            }
/*modify by dragontec for bug 4332 end*/
                            return view;
                        }

                        //                if (focusDirection == View.FOCUS_DOWN) {
                        //                    YoYo.with(Techniques.VerticalShake).duration(1000).playOn(view);
                        //                    return view;
                        //                }

/*add by dragontec for bug 4331 start*/
						if (isLastView && focusDirection == View.FOCUS_DOWN) {
							YoYo.with(Techniques.VerticalShake).duration(1000).playOn(view);
						}
/*add by dragontec for bug 4331 end*/

                        /*modify by dragontec for bug 4221 start*/
                        /*modify by dragontec for bug 4338 start*/
                        return findNextUpDownFocus(focusDirection, mBannerLinearLayout, view);
                        /*modify by dragontec for bug 4338 end*/
                        /*modify by dragontec for bug 4221 end*/
                    }
                });

/*modify by dragontec for bug 4332 start*/
        mRecyclerView.setOnItemFocusChangeListener(
                new RecyclerViewTV.OnItemFocusChangeListener() {
                    @Override
                    public void onItemFocusGain(View itemView, int position) {
                        if (itemView != null
                                && mContext != null
                                && mTitleCountTv != null
                                && adapter != null
                                && position < adapter.getTatalItemCount()) {
                            mTitleCountTv.setText(
                                    String.format(
                                            mContext.getString(R.string.home_item_title_count),
                                            (1 + position) + "",
                                            adapter.getTatalItemCount() + ""));
                        }
                    }
                });
/*modify by dragontec for bug 4332 end*/
/*add by dragontec for bug 4332 start*/
        mHoverView = view.findViewById(R.id.hover_view);
/*add by dragontec for bug 4332 end*/
    }

    @Override
    public void initData(Bundle bundle) {
        mBannerName = bundle.getString("banner");
        mBannerTitle = bundle.getString("title");
        channelName = bundle.getString(ChannelFragment.CHANNEL_KEY);
        nameKey = bundle.getString(ChannelFragment.NAME_KEY);
        mTitleTv.setText(mBannerTitle);
        mTitleCountTv.setText("00/00");
/*modify by dragontec for bug 4334 start*/
		fetchMovieMixBanner(mBannerName, 1);
    }

	@Override
	public void fetchData() {
		hasAppeared = true;
	}

	@Override
	public void fillData() {
    	if (isNeedFillData) {
			isNeedFillData = false;
			fillMovieMixBanner();
		}
	}
/*modify by dragontec for bug 4334 end*/

    private void fetchMovieMixBanner(String bannerName, final int pageNumber) {
        if (pageNumber != 1) {
        	/*add by dragontec for bug 4334 start*/
        	if (adapter == null) {
        		return;
			}
			/*add by dragontec for bug 4334 end*/
            int startIndex = (pageNumber - 1) * 33;
            int endIndex;
            if (pageNumber == adapter.getTotalPageCount()) {
                endIndex = adapter.getTatalItemCount() - 1;
            } else {
                endIndex = pageNumber * 33 - 1;
            }

            BannerEntity.PosterBean emptyPostBean = new BannerEntity.PosterBean();
            List<BannerEntity.PosterBean> totalPostList = new ArrayList<>();
            for (int i = startIndex; i <= endIndex; i++) {
                totalPostList.add(emptyPostBean);
            }
            adapter.addEmptyDatas(totalPostList);
/*modify by dragontec for bug 4332 start*/
            int mSavePos = mRecyclerView.getSelectPostion();
/*modify by dragontec for bug 4317 start*/
//            adapter.notifyItemRangeInserted(startIndex, endIndex - startIndex);
            adapter.notifyItemRangeInserted(startIndex, endIndex - startIndex + 1);
/*modify by dragontec for bug 4317 end*/
            mRecyclerView.setOnLoadMoreComplete();
/*modify by dragontec for bug 4332 end*/
            //            mMovieAdapter.setCurrentPageNumber(pageNumber);
            //            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
        }

        fetchMovieMixBanner =  SkyService.ServiceManager.getService()
                .apiTvBanner(bannerName, pageNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Observer<BannerEntity>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(BannerEntity bannerEntity) {
                                isMore = bannerEntity.is_more();
                                if (pageNumber == 1) {
                                	/*modify by dragontec for bug 4334 start*/
									isNeedFillData = true;
									initAdapter(bannerEntity);
									checkViewAppear();
									/*modify by dragontec for bug 4334 end*/
                                } else {
/*modify by dragontec for bug 4332 start*/
                                    int mSavePos = mRecyclerView.getSelectPostion();
/*modify by dragontec for bug 4332 end*/
                                    adapter.addDatas(bannerEntity);
                                    //                            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
                                }
                            }
                        });
    }

	/*modify by dragontec for bug 4334 start*/
    private void initAdapter(final BannerEntity bannerEntity) {
		adapter = new BannerMovieMixAdapter(mContext, bannerEntity);
		adapter.setSubscribeClickListener(
				new BannerMovieMixAdapter.OnBannerClickListener() {
					@Override
					public void onBannerClick(View view, int position) {
						if (bannerEntity.is_more() && position < bannerEntity.getCount() - 1) {
							goToNextPage(view);
						} else {
							Logger.t(TAG).d("more click: title -> %s, channel -> %s", nameKey, channelName);
							new PageIntent()
									.toListPage(
											mContext,
											bannerEntity.getChannel_title(),
											bannerEntity.getChannel(),
											bannerEntity.getStyle(),
											bannerEntity.getSection_slug());
						}
					}
				});
		adapter.setHoverListener(
				new BannerMovieMixAdapter.OnBannerHoverListener() {
					@Override
/*modify by dragontec for bug 4057 start*/
//                    public void onBannerHover(View view, int position, boolean hovered) {
					public void onBannerHover(View view, int position, boolean hovered, boolean isPrimary) {
/*modify by dragontec for bug 4057 end*/
/*modify by dragontec for bug 4332 start*/
						if (hovered) {
							mRecyclerView.setHovered(true);
							mTitleCountTv.setText(
									String.format(
											mContext.getString(R.string.home_item_title_count),
											(1 + position) + "",
											adapter.getTatalItemCount() + ""));
						} else {
							mRecyclerView.setHovered(false);
/*modify by dragontec for bug 4057 start*/
//                            HomeActivity.mHoverView.requestFocus();
							if (!isPrimary) {
								view.clearFocus();
							}
/*modify by dragontec for bug 4057 end*/
						}
/*modify by dragontec for bug 4332 end*/
					}
				});
	}

    private void fillMovieMixBanner() {
/*modify by dragontec for bug 4332 start*/
        mRecyclerView.setAdapter(adapter);
        mTitleCountTv.setText(
                String.format(
                        mContext.getString(R.string.home_item_title_count),
                        (1) + "",
                        adapter.getTatalItemCount() + ""));
	/*add by dragontec for bug 4077 start*/
		checkFocus(mRecyclerView);
	/*add by dragontec for bug 4077 end*/
/*modify by dragontec for bug 4332 end*/
    }
    /*modify by dragontec for bug 4334 end*/

    @Override
    public void onClick(View v) {
        int totalItemCount = isMore ? adapter.getTatalItemCount() + 1 : adapter.getTatalItemCount();
        int i = v.getId();
        if (i == R.id.navigation_left) {
            movieMixLayoutManager.setCanScroll(true);
            if (movieMixLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) {
                int targetPosition = movieMixLayoutManager.findFirstCompletelyVisibleItemPosition() - 5;
                if (targetPosition >= 0) {
                    // 表示可以滑动
                } else {
                    targetPosition = 0;
                }
                setBannerItemCount(targetPosition);
/*add by dragontec for bug 4332 start*/
				setNeedCheckScrollEnd();
/*add by dragontec for bug 4332 end*/
/*modify by dragontec for bug 4332 start*/
                movieMixLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
/*modify by dragontec for bug 4332 end*/
                    if (mNavigationtHandler.hasMessages(NAVIGATION_LEFT)){
                        mNavigationtHandler.removeMessages(NAVIGATION_LEFT);
                    }
                    mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_LEFT,500);
            } else {
                //                View firstView =
                // mRecyclerView.getChildAt(0).findViewById(R.id.item_layout);
                //                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(firstView);
            }
        } else if (i == R.id.navigation_right) {
            movieMixLayoutManager.setCanScroll(true);
/*modify by dragontec for bug 4332 start*/
            mRecyclerView.loadMore();
/*modify by dragontec for bug 4332 end*/

            if (movieMixLayoutManager.findLastCompletelyVisibleItemPosition() + 1 <= totalItemCount) {
                int targetPosition = movieMixLayoutManager.findLastCompletelyVisibleItemPosition() + 5;
                if (targetPosition < totalItemCount) {
                    // 表示可以滑动
                } else {
                    targetPosition = totalItemCount - 1;
                }
                setBannerItemCount(
                        targetPosition >= adapter.getTatalItemCount()
                                ? adapter.getTatalItemCount() - 1
                                : targetPosition);
/*add by dragontec for bug 4332 start*/
				setNeedCheckScrollEnd();
/*add by dragontec for bug 4332 end*/
/*modify by dragontec for bug 4332 start*/
                movieMixLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
/*modify by dragontec for bug 4332 start*/
                Log.d(TAG, "right total count: " + adapter.getTatalItemCount() );
                Log.d(TAG, "right target position: " + targetPosition);
                    if (mNavigationtHandler.hasMessages(NAVIGATION_RIGHT)){
                        mNavigationtHandler.removeMessages(NAVIGATION_RIGHT);
                    }
                    mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_RIGHT, 500);
            } else {
                //                View lastView = mRecyclerView.getChildAt(totalItemCount -
                // 1).findViewById(R.id.item_layout) ;
                //                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(lastView);
            }
        }
    }

    private void setBannerItemCount(int position) {
        mTitleCountTv.setText(
                String.format(
                        mContext.getString(R.string.home_item_title_count),
                        (position + 1) + "",
                        adapter.getTatalItemCount() + ""));
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
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                if (event.getButtonState() != BUTTON_PRIMARY) {
/*delete by dragontec for bug 4332 start*/
//                    navigationLeft.setVisibility(View.INVISIBLE);
//                    navigationRight.setVisibility(View.INVISIBLE);
/*delete by dragontec for bug 4332 end*/
/*add by dragontec for bug 4057 start*/
                    v.clearFocus();
/*add by dragontec for bug 4057 end*/
                }
                break;
        }
        return false;
    }
}
