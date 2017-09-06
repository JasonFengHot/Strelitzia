package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
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
import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.homepage.R;
import tv.ismar.homepage.banner.adapter.BannerSubscribeAdapter;
import tv.ismar.homepage.control.OrderControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 预约模版
 */

public class TemplateOrder extends Template implements BaseControl.ControlCallBack{

    private static final String TAG = "TemplateOrder";
    private OrderControl mControl;

    private RecyclerViewTV subscribeBanner;
    private BannerSubscribeAdapter subscribeAdapter;

    private View subscribeArrowLeft;
    private View subscribeArrowRight;

    public TemplateOrder(Context context) {
        super(context);
        mControl = new OrderControl(mContext, this);
        fetchSubscribeBanner(1);
    }

    @Override
    public void getView(View view) {
        subscribeBanner = (RecyclerViewTV) view.findViewById(R.id.subscribe_banner);
        LinearLayoutManagerTV subscribeLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManagerTV.HORIZONTAL, false);
        subscribeBanner.addItemDecoration(new BannerSubscribeAdapter.SpacesItemDecoration(20));
        subscribeBanner.setLayoutManager(subscribeLayoutManager);
        subscribeBanner.setSelectedItemAtCentered(false);
        subscribeBanner.setSelectedItemOffset(100, 100);
        subscribeBanner.setPagingableListener(new RecyclerViewTV.PagingableListener() {
            @Override
            public void onLoadMoreItems() {
                Log.d("PagingableListener", "onLoadMoreItems");
                int currentPageNumber = subscribeAdapter.getCurrentPageNumber();
                if (currentPageNumber < subscribeAdapter.getTotalPageCount()){
                    fetchSubscribeBanner(currentPageNumber + 1);
                }
            }
        });

        subscribeLayoutManager.setFocusSearchFailedListener(new LinearLayoutManagerTV.FocusSearchFailedListener() {
            @Override
            public View onFocusSearchFailed(View view, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
                if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
                    Log.d(TAG, "onFocusSearchFailed");
                    if (subscribeBanner.getChildAt(0).findViewById(R.id.item_layout) == view ||
                            subscribeBanner.getChildAt(subscribeBanner.getChildCount() - 1).findViewById(R.id.item_layout) == view) {
                        YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(view);
                    } else {
//                        if (focusDirection == View.FOCUS_RIGHT){
//                            subscribeBanner.smoothScrollBy(10, 0);
//                        }else if (focusDirection == View.FOCUS_LEFT){
//                            subscribeBanner.smoothScrollBy(-10, 0);
//                        }
                    }
                    Log.d(TAG, "onFocusSearchFailed: " + view);
                    return view;
                }
                return null;
            }
        });

        subscribeBanner.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                    case MotionEvent.ACTION_HOVER_MOVE:
                        if (subscribeArrowLeft.getVisibility() == View.INVISIBLE){
                            subscribeArrowLeft.setVisibility(View.VISIBLE);
                        }
                        if (subscribeArrowRight.getVisibility() == View.INVISIBLE){
                            subscribeArrowRight.setVisibility(View.VISIBLE);
                        }
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        if (subscribeArrowLeft.getVisibility() == View.VISIBLE){
                            subscribeArrowLeft.setVisibility(View.INVISIBLE);
                        }
                        if (subscribeArrowRight.getVisibility() == View.VISIBLE){
                            subscribeArrowRight.setVisibility(View.INVISIBLE);
                        }
                        break;
                }
                return false;
            }
        });

        subscribeArrowLeft = view.findViewById(R.id.subscribe_arrow_left);
        subscribeArrowLeft.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_MOVE:
                    case MotionEvent.ACTION_HOVER_ENTER:
                        v.findViewById(R.id.banner_arrow_left_normal).setVisibility(View.INVISIBLE);
                        v.findViewById(R.id.banner_arrow_left_focus).setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        v.findViewById(R.id.banner_arrow_left_focus).setVisibility(View.INVISIBLE);
                        v.findViewById(R.id.banner_arrow_left_normal).setVisibility(View.VISIBLE);
                        break;
                }
                return false;
            }
        });
        subscribeArrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeBanner.smoothScrollBy(-400, 0);
            }
        });

        subscribeArrowRight = view.findViewById(R.id.subscribe_arrow_right);
        subscribeArrowRight.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_MOVE:
                    case MotionEvent.ACTION_HOVER_ENTER:
                        v.findViewById(R.id.banner_arrow_right_normal).setVisibility(View.INVISIBLE);
                        v.findViewById(R.id.banner_arrow_right_focus).setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        v.findViewById(R.id.banner_arrow_right_focus).setVisibility(View.INVISIBLE);
                        v.findViewById(R.id.banner_arrow_right_normal).setVisibility(View.VISIBLE);
                        break;
                }
                return false;
            }
        });

        subscribeArrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeBanner.smoothScrollBy(400, 0);
            }
        });

    }

    @Override
    public void initData(Bundle bundle) {
//        mControl.getBanners("overseasbanner", 1);
    }


    @Override
    public void callBack(int flags, Object... args) {

    }


    private void fetchSubscribeBanner(final int pageNumber) {
        if (pageNumber != 1){
            int startIndex = (pageNumber - 1) * 33;
            int endIndex;
            if (pageNumber == subscribeAdapter.getTotalPageCount()) {
                endIndex = subscribeAdapter.getTatalItemCount() - 1;
            } else {
                endIndex = pageNumber * 33 - 1;
            }

            BannerEntity.PosterBean emptyPostBean = new BannerEntity.PosterBean();
            List<BannerEntity.PosterBean> totalPostList = new ArrayList<>();
            for (int i = startIndex; i <= endIndex; i++) {
                totalPostList.add(emptyPostBean);
            }
            subscribeAdapter.addEmptyDatas(totalPostList);
            int mSavePos = subscribeBanner.getSelectPostion();
            subscribeAdapter.notifyItemRangeInserted(startIndex, endIndex - startIndex);
            subscribeBanner.setOnLoadMoreComplete();
            subscribeAdapter.setCurrentPageNumber(pageNumber);
//            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
        }

        String count = String.valueOf(pageNumber);
        SkyService.ServiceManager.getLocalTestService().apiTvBanner("overseasbanner", count)
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
                            fillSubscribeBanner(bannerEntity);
                        }else {
                            int mSavePos = subscribeBanner.getSelectPostion();
                            subscribeAdapter.addDatas(bannerEntity);
//                            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
                        }
                    }
                });
    }

    private void fillSubscribeBanner(BannerEntity bannerEntity) {
        subscribeAdapter = new BannerSubscribeAdapter(mContext, bannerEntity.getPoster());
        subscribeAdapter.setTotalPageCount(bannerEntity.getCount_pages());
        subscribeAdapter.setCurrentPageNumber(bannerEntity.getNum_pages());
        subscribeAdapter.setTatalItemCount(bannerEntity.getCount());
        subscribeAdapter.setSubscribeClickListener(new BannerSubscribeAdapter.OnSubscribeClickListener() {
            @Override
            public void onSubscribeClick(int pk, String contentModel) {
                Log.d("onSubscribeClick", "pk: " + pk);
                Log.d("onSubscribeClick", "contentModel: " + contentModel);
//                accountsItemSubscribe(pk, contentModel);
            }
        });

        subscribeBanner.setAdapter(subscribeAdapter);
    }
}
