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
import android.view.ViewGroup;
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
import tv.ismar.homepage.banner.adapter.BannerMovieAdapter;
import tv.ismar.homepage.fragment.ChannelFragment;
import tv.ismar.homepage.view.BannerLinearLayout;
	/*add by dragontec for bug 4077 start*/
import tv.ismar.homepage.widget.RecycleLinearLayout;
	/*add by dragontec for bug 4077 end*/

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: 电影模版
 */
public class TemplateMovie extends Template implements View.OnClickListener, View.OnHoverListener {
    private static final String TAG = "TemplateMovie";

    private RecyclerViewTV movieBanner;
    private BannerMovieAdapter mMovieAdapter;
    private String mBannerName;
    private TextView mTitleTv;
    private String mBannerTitle;

    private View navigationLeft;
    private View navigationRight;
    private BannerLinearLayout mBannerLinearLayout;
    private LinearLayoutManagerTV movieLayoutManager;
    private String channelKey;
    private String nameKey;
    private boolean isMore;
    private Subscription fetchMovieBanner;

    private static final int NAVIGATION_LEFT = 0x0001;
    private static final int NAVIGATION_RIGHT = 0x0002;

    private NavigationtHandler mNavigationtHandler;

    private class NavigationtHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case NAVIGATION_LEFT:
                    if (movieBanner!=null&&!movieBanner.cannotScrollBackward(-10)) {
                        navigationLeft.setVisibility(VISIBLE);
                    }else if (movieBanner!=null){
                        navigationLeft.setVisibility(INVISIBLE);
                    }
                    break;
                case NAVIGATION_RIGHT:
                    if(movieBanner!=null&&!movieBanner.cannotScrollForward(10)){
                        navigationRight.setVisibility(VISIBLE);
                    }else if (movieBanner!=null){
                        navigationRight.setVisibility(INVISIBLE);
                    }
                    break;
            }
        }
    }

    public TemplateMovie(Context context) {
        super(context);
        mNavigationtHandler = new NavigationtHandler();
    }

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
        if (fetchMovieBanner != null && !fetchMovieBanner.isUnsubscribed()) {
            fetchMovieBanner.unsubscribe();
        }
	/*add by dragontec for bug 4077 start*/
		super.onPause();
	/*add by dragontec for bug 4077 end*/
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
        if (mNavigationtHandler !=null){
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
        movieBanner = (RecyclerViewTV) view.findViewById(R.id.movie_banner);
		/*modify by dragontec for bug 4221 start*/
        movieBanner.setTag("recycleView");
		/*modify by dragontec for bug 4221 end*/
        mBannerLinearLayout.setRecyclerViewTV(movieBanner);
        movieLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        int selectedItemSpace =
                mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_SelectedItemSpace);
        //        movieBanner.addItemDecoration(new
        // BannerMovieAdapter.SpacesItemDecoration(selectedItemSpace));
        movieBanner.setLayoutManager(movieLayoutManager);
        movieBanner.setSelectedItemAtCentered(false);
        int selectedItemOffset =
                mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
        movieBanner.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);

        movieBanner.setPagingableListener(
                new RecyclerViewTV.PagingableListener() {
                    @Override
                    public void onLoadMoreItems() {
                        Log.d("PagingableListener", "onLoadMoreItems");
                        if (mMovieAdapter != null) {
                            int currentPageNumber = mMovieAdapter.getCurrentPageNumber();
                            if (currentPageNumber < mMovieAdapter.getTotalPageCount()) {
                                fetchMovieBanner(mBannerName, currentPageNumber + 1);
                            }
                        }
                    }
                });

        movieLayoutManager.setFocusSearchFailedListener(
                new LinearLayoutManagerTV.FocusSearchFailedListener() {
                    @Override
                    public View onFocusSearchFailed(
                            View view,
                            int focusDirection,
                            RecyclerView.Recycler recycler,
                            RecyclerView.State state) {
                        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
                            if (movieBanner.getChildAt(0).findViewById(R.id.item_layout) == view
                                    || movieBanner
                                    .getChildAt(movieBanner.getChildCount() - 1)
                                    .findViewById(R.id.item_layout)
                                    == view) {
                                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(view);
                            }
                            return view;
                        }
                        /*modify by dragontec for bug 4221 start*/
                        return findNextUpDownFocus(focusDirection, mBannerLinearLayout);
                        /*modify by dragontec for bug 4221 end*/
                    }
                });

        movieBanner.setOnItemFocusChangeListener(
                new RecyclerViewTV.OnItemFocusChangeListener() {
                    @Override
                    public void onItemFocusGain(View itemView, int position) {
                        if (itemView != null
                                && mContext != null
                                && mTitleCountTv != null
                                && mMovieAdapter != null
                                && position <= mMovieAdapter.getTatalItemCount()-1) {
                            mTitleCountTv.setText(
                                    String.format(
                                            mContext.getString(R.string.home_item_title_count),
                                            (1 + position) + "",
                                            mMovieAdapter.getTatalItemCount() + ""));
                        }
                    }
                });
    }

    @Override
    public void initData(Bundle bundle) {
        mBannerName = bundle.getString("banner");
        mBannerTitle = bundle.getString("title");
        channelKey = bundle.getString(ChannelFragment.CHANNEL_KEY);
        nameKey = bundle.getString(ChannelFragment.NAME_KEY);
        mTitleTv.setText(mBannerTitle);
        mTitleCountTv.setText("00/00");
/*modify by dragontec for bug 4200 start*/
    }

	@Override
	public void fetchData() {
		hasAppeared = true;
		fetchMovieBanner(mBannerName, 1);
	}
/*modify by dragontec for bug 4200 end*/

    private void fetchMovieBanner(String bannerName, final int pageNumber) {
        if (pageNumber != 1) {
            int startIndex = (pageNumber - 1) * 33;
            int endIndex;
            if (pageNumber == mMovieAdapter.getTotalPageCount()) {
                endIndex = mMovieAdapter.getTatalItemCount() - 1;
            } else {
                endIndex = pageNumber * 33 - 1;
            }

            BannerEntity.PosterBean emptyPostBean = new BannerEntity.PosterBean();
            List<BannerEntity.PosterBean> totalPostList = new ArrayList<>();
            for (int i = startIndex; i <= endIndex; i++) {
                totalPostList.add(emptyPostBean);
            }
            mMovieAdapter.addEmptyDatas(totalPostList);
            int mSavePos = movieBanner.getSelectPostion();
            mMovieAdapter.notifyItemRangeInserted(startIndex, endIndex - startIndex);
            movieBanner.setOnLoadMoreComplete();
            //            mMovieAdapter.setCurrentPageNumber(pageNumber);
            //            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
        }

        fetchMovieBanner = SkyService.ServiceManager.getService()
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
                                    fillMovieBanner(bannerEntity);
                                } else {
                                    int mSavePos = movieBanner.getSelectPostion();
                                    mMovieAdapter.addDatas(bannerEntity);
                                    //                            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
                                }
                            }
                        });
    }

    private void fillMovieBanner(final BannerEntity bannerEntity) {
        mMovieAdapter = new BannerMovieAdapter(mContext, bannerEntity);
        mMovieAdapter.setSubscribeClickListener(
                new BannerMovieAdapter.OnBannerClickListener() {
                    @Override
                    public void onBannerClick(View view, int position) {
                        if (position < bannerEntity.getCount()) {
                            goToNextPage(view);
                        } else {
                            Logger.t(TAG).d("more click: title -> %s, channel -> %s", nameKey, channelKey);
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
        mMovieAdapter.setHoverListener(
                new BannerMovieAdapter.OnBannerHoverListener() {
                    @Override
/*modify by dragontec for bug 4057 start*/
//                    public void onBannerHover(View view, int position, boolean hovered) {
                    public void onBannerHover(View view, int position, boolean hovered, boolean isPrimary) {
/*modify by dragontec for bug 4057 end*/
                        Log.d(TAG, view + " : " + hovered);
                        if (hovered) {
                            movieBanner.setHovered(true);
                            if(position<=mMovieAdapter.getTatalItemCount()-1)
                            mTitleCountTv.setText(
                                    String.format(
                                            mContext.getString(R.string.home_item_title_count),
                                            (1 + position) + "",
                                            mMovieAdapter.getTatalItemCount() + ""));
                        } else {
                            movieBanner.setHovered(false);
/*modify by dragontec for bug 4057 start*/
//                            HomeActivity.mHoverView.requestFocus();
                            if (!isPrimary) {
                                view.clearFocus();
                            }
/*modify by dragontec for bug 4057 end*/
                        }
                    }
                });
        movieBanner.setAdapter(mMovieAdapter);
        mTitleCountTv.setText(
                String.format(
                        mContext.getString(R.string.home_item_title_count),
                        (1) + "",
                        mMovieAdapter.getTatalItemCount() + ""));
	/*add by dragontec for bug 4077 start*/
		checkFocus(movieBanner);
	/*add by dragontec for bug 4077 end*/
    }

    @Override
    public void onClick(View v) {
        int totalItemCount =
                isMore ? mMovieAdapter.getTatalItemCount() + 1 : mMovieAdapter.getTatalItemCount();
        int i = v.getId();
        if (i == R.id.navigation_left) {
            movieLayoutManager.setCanScroll(true);
            if (movieLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) {
                int targetPosition = movieLayoutManager.findFirstCompletelyVisibleItemPosition() - 6;
                if (targetPosition >= 0) {
                    // 表示可以滑动
                } else {
                    targetPosition = 0;
                }
                setBannerItemCount(targetPosition);
                movieLayoutManager.smoothScrollToPosition(movieBanner, null, targetPosition);
                if (targetPosition == 0){
                    mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_LEFT,500);
                }
            } else {
                //                View firstView = movieBanner.getChildAt(0).findViewById(R.id.item_layout)
                // ;
                //                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(firstView);
            }
        } else if (i == R.id.navigation_right) {
            movieLayoutManager.setCanScroll(true);
            movieBanner.loadMore();
            if (movieLayoutManager.findLastCompletelyVisibleItemPosition() + 1 <= totalItemCount) {
                int targetPosition = movieLayoutManager.findLastCompletelyVisibleItemPosition() + 6;
                if (targetPosition < totalItemCount) {
                    // 表示可以滑动
                } else {
                    targetPosition = totalItemCount - 1;
                }
                setBannerItemCount(
                        targetPosition >= mMovieAdapter.getTatalItemCount()
                                ? mMovieAdapter.getTatalItemCount() - 1
                                : targetPosition);
                movieLayoutManager.smoothScrollToPosition(movieBanner, null, targetPosition);

                if (targetPosition == mMovieAdapter.getTatalItemCount() - 1){
                    mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_RIGHT, 500);
                }
            } else {
                //                View lastView = movieBanner.getChildAt(totalItemCount -
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
                        mMovieAdapter.getTatalItemCount() + ""));
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
