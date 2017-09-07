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
import tv.ismar.homepage.banner.adapter.BannerMovieMixAdapter;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 大横小竖模版
 */

public class TemplateBigSmallLd extends Template{
    private RecyclerViewTV movieMixBanner;

    private BannerMovieMixAdapter adapter;

    public TemplateBigSmallLd(Context context) {
        super(context);
        fetchMovieMixBanner(1);
    }

    @Override
    public void getView(View view) {
        movieMixBanner = (RecyclerViewTV) view.findViewById(R.id.movie_mix_banner);
        LinearLayoutManagerTV movieMixLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        movieMixBanner.addItemDecoration(new BannerMovieMixAdapter.SpacesItemDecoration(20));
        movieMixBanner.setLayoutManager(movieMixLayoutManager);
        movieMixBanner.setSelectedItemAtCentered(false);

        movieMixBanner.setPagingableListener(new RecyclerViewTV.PagingableListener() {
            @Override
            public void onLoadMoreItems() {
                Log.d("PagingableListener", "onLoadMoreItems");
                int currentPageNumber = adapter.getCurrentPageNumber();
                if (currentPageNumber < adapter.getTotalPageCount()){
                    fetchMovieMixBanner(currentPageNumber + 1);
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

    }

    @Override
    public void initData(Bundle bundle) {

    }

    private void fetchMovieMixBanner(final int pageNumber) {
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
        movieMixBanner.setAdapter(adapter);
    }

}
