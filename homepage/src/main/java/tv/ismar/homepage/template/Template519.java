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
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.VodApplication;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.banner.adapter.BannerHorizontal519Adapter;
import tv.ismar.homepage.fragment.ChannelFragment;
import tv.ismar.homepage.view.BannerLinearLayout;
	/*add by dragontec for bug 4077 start*/
import tv.ismar.homepage.widget.RecycleLinearLayout;
	/*add by dragontec for bug 4077 end*/

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: 519横图模版
 */
public class Template519 extends Template implements View.OnClickListener, View.OnHoverListener {
    private static final String TAG = Template519.class.getSimpleName();

/*delete by dragontec for bug 4332 start*/
//    private RecyclerViewTV horizontal519Banner;
/*delete by dragontec for bug 4332 end*/
    private BannerHorizontal519Adapter mHorizontal519Adapter;
    private String mBannerName;
    private String mBannerTitle;
    private TextView mTitleTv;

/*delete by dragontec for bug 4332 start*/
//    private View navigationLeft;
//    private View navigationRight;
/*delete by dragontec for bug 4332 end*/
    private BannerLinearLayout mBannerLinearLayout;
    private LinearLayoutManagerTV horizontal519LayoutManager;
    private String channelName;
    private String nameKey;
    private boolean isMore;
    private Subscription fetchHorizontal519Banner;

    private static final int NAVIGATION_LEFT = 0x0001;
    private static final int NAVIGATION_RIGHT = 0x0002;

    private NavigationtHandler mNavigationtHandler;

    private class NavigationtHandler extends Handler {
/*delete by dragontec for bug 4332 start*/
//        @Override
//        public void handleMessage(Message msg) {
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
//        }
/*delete by dragontec for bug 4332 end*/
    }


	/*modify by dragontec for bug 4334 start*/
    public Template519(Context context, int position) {
        super(context, position);
        Logger.t(TAG).d("Template519 construct");
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
        Log.d(TAG, "onPause()");
        if (fetchHorizontal519Banner != null && !fetchHorizontal519Banner.isUnsubscribed()) {
            fetchHorizontal519Banner.unsubscribe();
        }
	/*add by dragontec for bug 4077 start*/
        super.onPause();
	/*add by dragontec for bug 4077 end*/
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop()");
        if (mNavigationtHandler.hasMessages(NAVIGATION_LEFT)){
            mNavigationtHandler.removeMessages(NAVIGATION_LEFT);
        }
        if (mNavigationtHandler.hasMessages(NAVIGATION_RIGHT)){
            mNavigationtHandler.removeMessages(NAVIGATION_RIGHT);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
/*add by dragontec for bug 4205 start*/
        if (mHorizontal519Adapter != null) {
            mHorizontal519Adapter.setBannerClickListener(null);
            mHorizontal519Adapter.setHoverListener(null);
        }
        if (mRecyclerView != null) {
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
//        RefWatcher refWatcher = VodApplication.getRefWatcher(mContext);
//        refWatcher.watch(this);
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
		mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.horizontal_519_banner);
		/*modify by dragontec for bug 4221 start*/
		mRecyclerView.setTag("recycleView");
		/*modify by dragontec for bug 4221 end*/
        mBannerLinearLayout.setRecyclerViewTV(mRecyclerView);
        horizontal519LayoutManager =
                new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        int selectedItemSpace =
                mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_SelectedItemSpace);
        //        mRecyclerView.addItemDecoration(new
        // BannerHorizontal519Adapter.SpacesItemDecoration(selectedItemSpace));
		mRecyclerView.setLayoutManager(horizontal519LayoutManager);
        mRecyclerView.setSelectedItemAtCentered(false);
        int selectedItemOffset =
                mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
        mRecyclerView.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);

        mRecyclerView.setPagingableListener(
                new RecyclerViewTV.PagingableListener() {
                    @Override
                    public void onLoadMoreItems() {
                        Log.d("PagingableListener", "onLoadMoreItems");
                        if (mHorizontal519Adapter != null) {
                            int currentPageNumber = mHorizontal519Adapter.getCurrentPageNumber();
                            if (currentPageNumber < mHorizontal519Adapter.getTotalPageCount()) {
                                fetchHorizontal519Banner(mBannerName, currentPageNumber + 1);
                            }
                        }
                    }
                });
/*modify by dragontec for bug 4332 end*/
        horizontal519LayoutManager.setFocusSearchFailedListener(
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
/*modify by dragontec for bug 4332 end*/
                new RecyclerViewTV.OnItemFocusChangeListener() {
                    @Override
                    public void onItemFocusGain(View itemView, int position) {
                        if (itemView != null
                                && mContext != null
                                && mTitleCountTv != null
                                && mHorizontal519Adapter != null
                                && position < mHorizontal519Adapter.getTatalItemCount()) {
                            mTitleCountTv.setText(
                                    String.format(
                                            mContext.getString(R.string.home_item_title_count),
                                            (1 + position) + "",
                                            mHorizontal519Adapter.getTatalItemCount() + ""));
                        }
                    }
                });
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
		fetchHorizontal519Banner(mBannerName, 1);
    }

	@Override
	public void fetchData() {
		hasAppeared = true;
	}

	@Override
	public void fillData() {
		if (isNeedFillData) {
			isNeedFillData = false;
			fillHorizontal519Banner();
		}
	}
/*modify by dragontec for bug 4334 end*/

    private void fetchHorizontal519Banner(String bannerName, final int pageNumber) {
        if (pageNumber != 1) {
        	/*add by dragontec for bug 4334 start*/
        	if (mHorizontal519Adapter == null) {
        		return;
			}
			/*add by dragontec for bug 4334 end*/
            int startIndex = (pageNumber - 1) * 33;
            int endIndex;
            if (pageNumber == mHorizontal519Adapter.getTotalPageCount()) {
                endIndex = mHorizontal519Adapter.getTatalItemCount() - 1;
            } else {
                endIndex = pageNumber * 33 - 1;
            }

            BannerEntity.PosterBean emptyPostBean = new BannerEntity.PosterBean();
            List<BannerEntity.PosterBean> totalPostList = new ArrayList<>();
            for (int i = startIndex; i <= endIndex; i++) {
                totalPostList.add(emptyPostBean);
            }
            mHorizontal519Adapter.addEmptyDatas(totalPostList);
/*modify by dragontec for bug 4332 start*/
            int mSavePos = mRecyclerView.getSelectPostion();
/*modify by dragontec for bug 4317 start*/
//            mHorizontal519Adapter.notifyItemRangeInserted(startIndex, endIndex - startIndex);
            mHorizontal519Adapter.notifyItemRangeInserted(startIndex, endIndex - startIndex + 1);
/*modify by dragontec for bug 4317 end*/
            mRecyclerView.setOnLoadMoreComplete();
/*modify by dragontec for bug 4332 end*/
            //            mMovieAdapter.setCurrentPageNumber(pageNumber);
            //            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
        }

        fetchHorizontal519Banner = SkyService.ServiceManager.getService()
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
                                //                        List<BannerEntity.PosterBean> posterBeanList =
                                // bannerSubscribeEntities.getPoster();
                                //                        fillHorizontal519Banner(posterBeanList);

                                if (pageNumber == 1) {
                                	/*modify by dragontec for bug 4334 start*/
									isNeedFillData = true;
									initAdapter(bannerEntity);
                                	checkViewAppear();
//                                    fillHorizontal519Banner(bannerEntity);
									/*modify by dragontec for bug 4334 end*/
                                } else {
/*modify by dragontec for bug 4332 start*/
                                    int mSavePos = mRecyclerView.getSelectPostion();
/*modify by dragontec for bug 4332 end*/
                                    mHorizontal519Adapter.addDatas(bannerEntity);
                                    //                            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
                                }
                            }
                        });
    }

	/*modify by dragontec for bug 4334 start*/
    private void initAdapter(final BannerEntity bannerEntity) {
		mHorizontal519Adapter = new BannerHorizontal519Adapter(mContext, bannerEntity);
		mHorizontal519Adapter.setHoverListener(
				new BannerHorizontal519Adapter.OnBannerHoverListener() {
					@Override
/*modify by dragontec for bug 4057 start*/
//                    public void onBannerHover(View view, int position, boolean hovered) {
					public void onBannerHover(View view, int position, boolean hovered, boolean isPrimary) {
/*modify by dragontec for bug 4057 end*/
						//                Log.d(TAG, view + " : " + hovered);
						if (hovered) {
/*modify by dragontec for bug 4332 start*/
							mRecyclerView.setHovered(true);
/*modify by dragontec for bug 4332 end*/
							if(position<mHorizontal519Adapter.getTatalItemCount())
								mTitleCountTv.setText(
										String.format(
												mContext.getString(R.string.home_item_title_count),
												(1 + position) + "",
												mHorizontal519Adapter.getTatalItemCount() + ""));
						} else {
/*modify by dragontec for bug 4332 start*/
							mRecyclerView.setHovered(false);
/*modify by dragontec for bug 4332 end*/
/*modify by dragontec for bug 4057 start*/
//                            HomeActivity.mHoverView.requestFocus();
							if (!isPrimary) {
								view.clearFocus();
							}
/*modify by dragontec for bug 4057 end*/
							//                    Log.d(TAG, view + " : " + hovered);
							//                    Log.d(TAG, "view id: " + view.getId());
							//                    HomeActivity.mHoverView.setNextFocusUpId(view.getId());
							//                    HomeActivity.mHoverView.setNextFocusDownId(view.getId());
							//                    HomeActivity.mHoverView.setNextFocusRightId(view.getId());
							//                    HomeActivity.mHoverView.setNextFocusLeftId(view.getId());
						}
					}
				});
		mHorizontal519Adapter.setBannerClickListener(
				new BannerHorizontal519Adapter.OnBannerClickListener() {
					@Override
					public void onBannerClick(View view, int position) {
						if (position < bannerEntity.getCount()) {
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
	}

    private void fillHorizontal519Banner() {
/*modify by dragontec for bug 4332 start*/
        mRecyclerView.setAdapter(mHorizontal519Adapter);
/*modify by dragontec for bug 4332 end*/
        mTitleCountTv.setText(
                String.format(
                        mContext.getString(R.string.home_item_title_count),
                        (1) + "",
                        mHorizontal519Adapter.getTatalItemCount() + ""));
	/*add by dragontec for bug 4077 start*/
/*modify by dragontec for bug 4332 start*/
		checkFocus(mRecyclerView);
/*modify by dragontec for bug 4332 end*/
	/*add by dragontec for bug 4077 end*/
    }
    /*modify by dragontec for bug 4334 end*/

    @Override
    public void onClick(View v) {
        int totalItemCount =
                isMore
                        ? mHorizontal519Adapter.getTatalItemCount() + 1
                        : mHorizontal519Adapter.getTatalItemCount();
        int i = v.getId();
        if (i == R.id.navigation_left) {
            horizontal519LayoutManager.setCanScroll(true);
            //
            // horizontal519LayoutManager.scrollToPositionWithOffset(mRecyclerView.findFirstVisibleItemPosition() - 1, 0);

            if (horizontal519LayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) {
                int targetPosition =
                        horizontal519LayoutManager.findFirstCompletelyVisibleItemPosition() - 3;
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
                horizontal519LayoutManager.smoothScrollToPosition(
                        mRecyclerView, null, targetPosition);
/*modify by dragontec for bug 4332 end*/
                if (mNavigationtHandler.hasMessages(NAVIGATION_LEFT)) {
                    mNavigationtHandler.removeMessages(NAVIGATION_LEFT);
                }
                mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_LEFT, 500);
            }
        } else if (i == R.id.navigation_right) {
            horizontal519LayoutManager.setCanScroll(true);
/*modify by dragontec for bug 4332 start*/
            mRecyclerView.loadMore();
/*modify by dragontec for bug 4332 end*/

            if (horizontal519LayoutManager.findLastCompletelyVisibleItemPosition() + 1
                    <= mHorizontal519Adapter.getTatalItemCount()) {
                int targetPosition = horizontal519LayoutManager.findLastCompletelyVisibleItemPosition() + 3;
                if (targetPosition < totalItemCount) {
                    // 表示可以滑动
                } else {
                    targetPosition = totalItemCount - 1;
                }
                setBannerItemCount(
                        targetPosition >= mHorizontal519Adapter.getTatalItemCount()
                                ? mHorizontal519Adapter.getTatalItemCount() - 1
                                : targetPosition);
/*add by dragontec for bug 4332 start*/
				setNeedCheckScrollEnd();
/*add by dragontec for bug 4332 end*/
/*modify by dragontec for bug 4332 start*/
                horizontal519LayoutManager.smoothScrollToPosition(
                        mRecyclerView, null, targetPosition);
/*modify by dragontec for bug 4332 end*/
                if (mNavigationtHandler.hasMessages(NAVIGATION_RIGHT)) {
                    mNavigationtHandler.removeMessages(NAVIGATION_RIGHT);
                }
                mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_RIGHT, 500);
            }
        }
    }

    private void setBannerItemCount(int position) {
        mTitleCountTv.setText(
                String.format(
                        mContext.getString(R.string.home_item_title_count),
                        (position + 1) + "",
                        mHorizontal519Adapter.getTatalItemCount() + ""));
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
