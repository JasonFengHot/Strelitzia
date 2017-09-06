package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

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

    public TemplateMovie(Context context) {
        super(context);
        fetchMovieBanner();
    }

    @Override
    public void getView(View view) {
        movieBanner = (RecyclerViewTV) view.findViewById(R.id.movie_banner);
        LinearLayoutManagerTV movieLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        movieBanner.addItemDecoration(new BannerMovieAdapter.SpacesItemDecoration(20));
        movieBanner.setLayoutManager(movieLayoutManager);
        movieBanner.setSelectedItemAtCentered(false);
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


    private void fetchMovieBanner() {
        SkyService.ServiceManager.getLocalTestService().apiTvBanner("chinesemoviebanner", "1")
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
                    public void onNext(BannerEntity bannerSubscribeEntities) {
                        List<BannerEntity.PosterBean> posterBeanList = bannerSubscribeEntities.getPoster();
                        fillMovieBanner(posterBeanList);
                    }
                });
    }

    private void fillMovieBanner(List<BannerEntity.PosterBean> posterBeanList) {
        BannerMovieAdapter adapter = new BannerMovieAdapter(mContext, posterBeanList);
        movieBanner.setAdapter(adapter);
    }
}
