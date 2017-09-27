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

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.banner.adapter.BannerHorizontal519Adapter;
import tv.ismar.homepage.banner.adapter.BannerMovieMixAdapter;
import tv.ismar.homepage.view.BannerLinearLayout;

import static android.view.MotionEvent.BUTTON_PRIMARY;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 大横小竖模版
 */

public class TemplateBigSmallLd extends Template implements View.OnHoverListener, View.OnClickListener {
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

    public TemplateBigSmallLd(Context context) {
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
        movieMixBanner = (RecyclerViewTV) view.findViewById(R.id.movie_mix_banner);
         movieMixLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        int selectedItemSpace = mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_SelectedItemSpace);
//        movieMixBanner.addItemDecoration(new BannerMovieMixAdapter.SpacesItemDecoration(selectedItemSpace));
        movieMixBanner.setLayoutManager(movieMixLayoutManager);
        movieMixBanner.setSelectedItemAtCentered(false);
        int selectedItemOffset = mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
//        movieMixBanner.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);
        movieMixBanner.setPagingableListener(new RecyclerViewTV.PagingableListener() {
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

        movieMixLayoutManager.setFocusSearchFailedListener(new LinearLayoutManagerTV.FocusSearchFailedListener() {
            @Override
            public View onFocusSearchFailed(View view, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
                if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
                    if (movieMixBanner.getChildAt(0).findViewById(R.id.item_layout) == view ||
                            movieMixBanner.getChildAt(movieMixBanner.getChildCount() - 1).findViewById(R.id.item_layout) == view) {
                        YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(view);
                    }
                    return view;
                }

                if (focusDirection == View.FOCUS_DOWN) {
                    YoYo.with(Techniques.VerticalShake).duration(1000).playOn(view);
                    return view;
                }
                return null;
            }
        });

        movieMixBanner.setOnItemFocusChangeListener(new RecyclerViewTV.OnItemFocusChangeListener() {
            @Override
            public void onItemFocusGain(View itemView, int position) {
                if (itemView != null && mContext != null && mTitleCountTv != null && adapter != null
                        && position < adapter.getTatalItemCount()) {
                    mTitleCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), (1 + position) + "", adapter.getTatalItemCount() + ""));
                }
            }
        });
    }

    @Override
    public void initData(Bundle bundle) {
        mBannerName = bundle.getInt("banner");
        mBannerTitle = bundle.getString("title");
        mTitleTv.setText(mBannerTitle);
        fetchMovieMixBanner(mBannerName, 1);
    }

    private void fetchMovieMixBanner(int bannerName, final int pageNumber) {
        if (pageNumber != 1){
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
                            fillMovieMixBanner(bannerEntity);
                        }else {
                            int mSavePos = movieMixBanner.getSelectPostion();
                            adapter.addDatas(bannerEntity);
//                            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
                        }
                    }
                });
    }

    private void fillMovieMixBanner(BannerEntity bannerEntity) {
        adapter = new BannerMovieMixAdapter(mContext, bannerEntity);
        adapter.setSubscribeClickListener(new BannerMovieMixAdapter.OnBannerClickListener() {
            @Override
            public void onBannerClick(View view, int position) {
                goToNextPage(view);
            }
        });
        adapter.setHoverListener(new BannerMovieMixAdapter.OnBannerHoverListener() {
            @Override
            public void onBannerHover(View view, int position, boolean hovered) {
                Log.d(TAG, view + " : " + hovered);
                if (hovered){
                    movieMixBanner.setHovered(true);
                }else {
                    movieMixBanner.setHovered(false);
                    HomeActivity.mHoverView.requestFocus();
                }
            }
        });
        movieMixBanner.setAdapter(adapter);
        mTitleCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), (1) + "", adapter.getTatalItemCount() + ""));

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.navigation_left) {
            if (movieMixLayoutManager.findFirstCompletelyVisibleItemPosition() -1 >= 0){
                movieMixLayoutManager.smoothScrollToPosition(movieMixBanner, null, movieMixLayoutManager.findFirstCompletelyVisibleItemPosition() - 1);
            }
        } else if (i == R.id.navigation_right) {
            movieMixBanner.loadMore();
            if (movieMixLayoutManager.findFirstCompletelyVisibleItemPosition() + 1 <= adapter.getTatalItemCount()){
                movieMixLayoutManager.smoothScrollToPosition(movieMixBanner, null, movieMixLayoutManager.findLastCompletelyVisibleItemPosition() + 1);
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
