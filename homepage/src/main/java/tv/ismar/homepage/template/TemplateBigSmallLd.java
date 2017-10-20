package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
	/*add by dragontec for bug 4077 start*/
import android.os.Handler;
	/*add by dragontec for bug 4077 end*/
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

/**
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: 大横小竖模版
 */
public class TemplateBigSmallLd extends Template
        implements View.OnHoverListener, View.OnClickListener {
    private static final String TAG = TemplateBigSmallLd.class.getSimpleName();
    private RecyclerViewTV movieMixBanner;

    private BannerMovieMixAdapter adapter;
    private int mBannerName;

    private TextView mTitleTv;
    private String mBannerTitle;

    private View navigationLeft;
    private View navigationRight;
    private BannerLinearLayout mBannerLinearLayout;
    private LinearLayoutManagerTV movieMixLayoutManager;
    private String channelName;
    private String nameKey;
    private boolean isMore;
    private Subscription fetchMovieMixBanner;

    public TemplateBigSmallLd(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
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
    }

    @Override
    public void onDestroy() {
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
        movieMixBanner = (RecyclerViewTV) view.findViewById(R.id.movie_mix_banner);
        movieMixLayoutManager =
                new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        int selectedItemSpace =
                mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_SelectedItemSpace);
        //        movieMixBanner.addItemDecoration(new
        // BannerMovieMixAdapter.SpacesItemDecoration(selectedItemSpace));
        movieMixBanner.setLayoutManager(movieMixLayoutManager);
        movieMixBanner.setSelectedItemAtCentered(false);
        int selectedItemOffset =
                mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
        //        movieMixBanner.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);
        movieMixBanner.setPagingableListener(
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

        movieMixLayoutManager.setFocusSearchFailedListener(
                new LinearLayoutManagerTV.FocusSearchFailedListener() {
                    @Override
                    public View onFocusSearchFailed(
                            View view,
                            int focusDirection,
                            RecyclerView.Recycler recycler,
                            RecyclerView.State state) {
                        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
                            if (movieMixBanner.getChildAt(0).findViewById(R.id.item_layout) == view
                                    || movieMixBanner
                                    .getChildAt(movieMixBanner.getChildCount() - 1)
                                    .findViewById(R.id.item_layout)
                                    == view) {
                                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(view);
                            }
                            return view;
                        }

                        //                if (focusDirection == View.FOCUS_DOWN) {
                        //                    YoYo.with(Techniques.VerticalShake).duration(1000).playOn(view);
                        //                    return view;
                        //                }
                        return null;
                    }
                });

        movieMixBanner.setOnItemFocusChangeListener(
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
    }

    @Override
    public void initData(Bundle bundle) {
        mBannerName = bundle.getInt("banner");
        mBannerTitle = bundle.getString("title");
        channelName = bundle.getString(ChannelFragment.CHANNEL_KEY);
        nameKey = bundle.getString(ChannelFragment.NAME_KEY);
        mTitleTv.setText(mBannerTitle);
        mTitleCountTv.setText("00/00");
        fetchMovieMixBanner(mBannerName, 1);
    }

    private void fetchMovieMixBanner(int bannerName, final int pageNumber) {
        if (pageNumber != 1) {
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
            int mSavePos = movieMixBanner.getSelectPostion();
            adapter.notifyItemRangeInserted(startIndex, endIndex - startIndex);
            movieMixBanner.setOnLoadMoreComplete();
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
                                    fillMovieMixBanner(bannerEntity);
                                } else {
                                    int mSavePos = movieMixBanner.getSelectPostion();
                                    adapter.addDatas(bannerEntity);
                                    //                            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
                                }
                            }
                        });
    }

    private void fillMovieMixBanner(final BannerEntity bannerEntity) {
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
                        Log.d(TAG, view + " : " + hovered);
                        if (hovered) {
                            movieMixBanner.setHovered(true);
                            mTitleCountTv.setText(
                                    String.format(
                                            mContext.getString(R.string.home_item_title_count),
                                            (1 + position) + "",
                                            adapter.getTatalItemCount() + ""));
                        } else {
                            movieMixBanner.setHovered(false);
/*modify by dragontec for bug 4057 start*/
//                            HomeActivity.mHoverView.requestFocus();
                            if (!isPrimary) {
                                view.clearFocus();
                            }
/*modify by dragontec for bug 4057 end*/
                        }
                    }
                });
        movieMixBanner.setAdapter(adapter);
        mTitleCountTv.setText(
                String.format(
                        mContext.getString(R.string.home_item_title_count),
                        (1) + "",
                        adapter.getTatalItemCount() + ""));
	/*add by dragontec for bug 4077 start*/
		checkFocus(movieMixBanner);
	/*add by dragontec for bug 4077 end*/
    }

    @Override
    public void onClick(View v) {
        int totalItemCount = isMore ? adapter.getTatalItemCount() + 1 : adapter.getTatalItemCount();
        int i = v.getId();
        if (i == R.id.navigation_left) {
            movieMixLayoutManager.setCanScroll(true);
            if (movieMixLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) {
                int targetPosition = movieMixLayoutManager.findFirstCompletelyVisibleItemPosition() - 6;
                if (targetPosition >= 0) {
                    // 表示可以滑动
                } else {
                    targetPosition = 0;
                }
                setBannerItemCount(targetPosition);
                movieMixLayoutManager.smoothScrollToPosition(movieMixBanner, null, targetPosition);
            } else {
                //                View firstView =
                // movieMixBanner.getChildAt(0).findViewById(R.id.item_layout);
                //                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(firstView);
            }
        } else if (i == R.id.navigation_right) {
            movieMixLayoutManager.setCanScroll(true);
            movieMixBanner.loadMore();

            if (movieMixLayoutManager.findLastCompletelyVisibleItemPosition() + 1 <= totalItemCount) {
                int targetPosition = movieMixLayoutManager.findLastCompletelyVisibleItemPosition() + 6;
                if (targetPosition < totalItemCount) {
                    // 表示可以滑动
                } else {
                    targetPosition = totalItemCount - 1;
                }
                setBannerItemCount(
                        targetPosition >= adapter.getTatalItemCount()
                                ? adapter.getTatalItemCount() - 1
                                : targetPosition);
                movieMixLayoutManager.smoothScrollToPosition(movieMixBanner, null, targetPosition);
            } else {
                //                View lastView = movieMixBanner.getChildAt(totalItemCount -
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
/*add by dragontec for bug 4057 start*/
                    v.clearFocus();
/*add by dragontec for bug 4057 end*/
                }
                break;
        }
        return false;
    }
}
