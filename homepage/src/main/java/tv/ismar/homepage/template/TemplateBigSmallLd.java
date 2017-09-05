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
import tv.ismar.homepage.banner.adapter.BannerMovieMixAdapter;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 大横小竖模版
 */

public class TemplateBigSmallLd extends Template{
    private RecyclerViewTV movieMixBanner;

    public TemplateBigSmallLd(Context context) {
        super(context);
        fetchMovieMixBanner();
    }

    @Override
    public void getView(View view) {
        movieMixBanner = (RecyclerViewTV) view.findViewById(R.id.movie_mix_banner);
        LinearLayoutManagerTV movieMixLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        movieMixBanner.addItemDecoration(new BannerMovieMixAdapter.SpacesItemDecoration(20));
        movieMixBanner.setLayoutManager(movieMixLayoutManager);
        movieMixBanner.setSelectedItemAtCentered(false);
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

    private void fetchMovieMixBanner() {
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
                        fillMovieMixBanner(posterBeanList);
                    }
                });
    }

    private void fillMovieMixBanner(List<BannerEntity.PosterBean> posterBeanList) {
        BannerMovieMixAdapter adapter = new BannerMovieMixAdapter(mContext, posterBeanList);
        movieMixBanner.setAdapter(adapter);
    }

}
