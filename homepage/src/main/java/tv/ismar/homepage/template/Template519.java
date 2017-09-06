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
import tv.ismar.homepage.banner.adapter.BannerHorizontal519Adapter;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 519横图模版
 */

public class Template519 extends Template{
    private RecyclerViewTV horizontal519Banner;

    public Template519(Context context) {
        super(context);
        fetchHorizontal519Banner();
    }

    @Override
    public void getView(View view) {
        horizontal519Banner = (RecyclerViewTV)view.findViewById(R.id.horizontal_519_banner);
        LinearLayoutManagerTV horizontal519LayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        horizontal519Banner.addItemDecoration(new BannerHorizontal519Adapter.SpacesItemDecoration(20));
        horizontal519Banner.setLayoutManager(horizontal519LayoutManager);
        horizontal519Banner.setSelectedItemAtCentered(false);
        horizontal519LayoutManager.setFocusSearchFailedListener(new LinearLayoutManagerTV.FocusSearchFailedListener() {
            @Override
            public View onFocusSearchFailed(View view, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
                if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
                    if (horizontal519Banner.getChildAt(0).findViewById(R.id.item_layout) == view ||
                            horizontal519Banner.getChildAt(horizontal519Banner.getChildCount() - 1).findViewById(R.id.item_layout) == view) {
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

    private void fetchHorizontal519Banner() {
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
                        fillHorizontal519Banner(posterBeanList);
                    }
                });
    }

    private void fillHorizontal519Banner(List<BannerEntity.PosterBean> posterBeanList) {
        BannerHorizontal519Adapter adapter = new BannerHorizontal519Adapter(mContext, posterBeanList);
        horizontal519Banner.setAdapter(adapter);
    }
}
