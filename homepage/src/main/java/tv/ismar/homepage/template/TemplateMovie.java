package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
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

import static android.view.MotionEvent.BUTTON_PRIMARY;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 电影模版
 */

public class TemplateMovie extends Template implements View.OnClickListener, View.OnHoverListener{
    private static final String TAG = "TemplateMovie";

    private RecyclerViewTV movieBanner;
    private BannerMovieAdapter mMovieAdapter;
    private int mBannerName;
    private TextView mTitleTv;
    private String mBannerTitle;

    private View navigationLeft;
    private View navigationRight;
    private BannerLinearLayout mBannerLinearLayout;
    private LinearLayoutManagerTV movieLayoutManager;
    private String channelKey;
    private String nameKey;

    public TemplateMovie(Context context) {
        super(context);
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
         movieLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        int selectedItemSpace = mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_SelectedItemSpace);
//        movieBanner.addItemDecoration(new BannerMovieAdapter.SpacesItemDecoration(selectedItemSpace));
        movieBanner.setLayoutManager(movieLayoutManager);
        movieBanner.setSelectedItemAtCentered(false);
        int selectedItemOffset = mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
        movieBanner.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);

        movieBanner.setPagingableListener(new RecyclerViewTV.PagingableListener() {
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

        movieLayoutManager.setFocusSearchFailedListener(new LinearLayoutManagerTV.FocusSearchFailedListener() {
            @Override
            public View onFocusSearchFailed(View view, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
                if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
                    if (movieBanner.getChildAt(0).findViewById(R.id.item_layout) == view ||
                            movieBanner.getChildAt(movieBanner.getChildCount() - 1).findViewById(R.id.item_layout) == view) {
                        YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(view);
                    }
                    return view;
                }
                return null;
            }
        });

        movieBanner.setOnItemFocusChangeListener(new RecyclerViewTV.OnItemFocusChangeListener() {
            @Override
            public void onItemFocusGain(View itemView, int position) {
                if (itemView != null && mContext != null && mTitleCountTv != null && mMovieAdapter != null
                        && position < mMovieAdapter.getTatalItemCount()) {
                    mTitleCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), (1 + position) + "", mMovieAdapter.getTatalItemCount() + ""));                }
            }
        });
    }

    @Override
    public void initData(Bundle bundle) {
        mBannerName = bundle.getInt("banner");
        mBannerTitle = bundle.getString("title");
        channelKey = bundle.getString(ChannelFragment.CHANNEL_KEY);
        nameKey = bundle.getString(ChannelFragment.NAME_KEY);
        mTitleTv.setText(mBannerTitle);
        fetchMovieBanner(mBannerName, 1);
    }

    private void fetchMovieBanner(int bannerName, final int pageNumber) {
        if (pageNumber != 1){
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

        SkyService.ServiceManager.getService().apiTvBanner(bannerName, pageNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BannerEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(BannerEntity bannerEntity) {
                        if (pageNumber == 1){
                            fillMovieBanner(bannerEntity);
                        }else {
                            int mSavePos = movieBanner.getSelectPostion();
                            mMovieAdapter.addDatas(bannerEntity);
//                            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
                        }
                    }
                });
    }

    private void fillMovieBanner(final BannerEntity bannerEntity) {
        mMovieAdapter = new BannerMovieAdapter(mContext, bannerEntity);
        mMovieAdapter.setSubscribeClickListener(new BannerMovieAdapter.OnBannerClickListener() {
            @Override
            public void onBannerClick(View view, int position) {
                if (position < bannerEntity.getCount()){
                    goToNextPage(view);
                }else {
                    Logger.t(TAG).d("more click: title -> %s, channel -> %s", nameKey, channelKey);
                    new PageIntent().toListPage(mContext, nameKey, channelKey, 1);
                }
            }
        });
        mMovieAdapter.setHoverListener(new BannerMovieAdapter.OnBannerHoverListener() {
            @Override
            public void onBannerHover(View view, int position, boolean hovered) {
                Log.d(TAG, view + " : " + hovered);
                if (hovered){
                    movieBanner.setHovered(true);
                }else {
                    movieBanner.setHovered(false);
                    HomeActivity.mHoverView.requestFocus();
                }
            }
        });
        movieBanner.setAdapter(mMovieAdapter);
        mTitleCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), (1) + "", mMovieAdapter.getTatalItemCount() + ""));

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.navigation_left) {
            if (movieLayoutManager.findFirstCompletelyVisibleItemPosition() -1 >= 0){
                movieLayoutManager.smoothScrollToPosition(movieBanner, null, movieLayoutManager.findFirstCompletelyVisibleItemPosition() - 1);
            }
        } else if (i == R.id.navigation_right) {
            movieBanner.loadMore();
            if (movieLayoutManager.findFirstCompletelyVisibleItemPosition() + 1 <= mMovieAdapter.getTatalItemCount()){
                movieLayoutManager.smoothScrollToPosition(movieBanner, null, movieLayoutManager.findLastCompletelyVisibleItemPosition() + 1);
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
                }
                break;
        }
        return false;
    }
}
