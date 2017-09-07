package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

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
import tv.ismar.homepage.R;
import tv.ismar.homepage.banner.adapter.BannerMovieAdapter;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 电影模版
 */

public class TemplateMovie extends Template {

    private RecyclerViewTV movieBanner;
    private BannerMovieAdapter mMovieAdapter;

    public TemplateMovie(Context context) {
        super(context);
        fetchMovieBanner(1);
    }

    @Override
    public void getView(View view) {
        movieBanner = (RecyclerViewTV) view.findViewById(R.id.movie_banner);
        LinearLayoutManagerTV movieLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        int selectedItemSpace = mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_SelectedItemSpace);
        movieBanner.addItemDecoration(new BannerMovieAdapter.SpacesItemDecoration(selectedItemSpace));
        movieBanner.setLayoutManager(movieLayoutManager);
        movieBanner.setSelectedItemAtCentered(false);
        int selectedItemOffset = mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
        movieBanner.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);

        movieBanner.setPagingableListener(new RecyclerViewTV.PagingableListener() {
            @Override
            public void onLoadMoreItems() {
                Log.d("PagingableListener", "onLoadMoreItems");
                int currentPageNumber = mMovieAdapter.getCurrentPageNumber();
                if (currentPageNumber < mMovieAdapter.getTotalPageCount()){
                    fetchMovieBanner(currentPageNumber + 1);
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
    }

    @Override
    public void initData(Bundle bundle) {

    }


    private void fetchMovieBanner(final int pageNumber) {
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

        String pageCount = String.valueOf(pageNumber);

        SkyService.ServiceManager.getLocalTestService().apiTvBanner("overseasbanner", pageCount)
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

    private void fillMovieBanner(BannerEntity bannerEntity) {
        mMovieAdapter = new BannerMovieAdapter(mContext, bannerEntity);
        movieBanner.setAdapter(mMovieAdapter);
    }
}
