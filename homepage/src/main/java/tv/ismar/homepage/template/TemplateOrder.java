package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.homepage.R;
import tv.ismar.homepage.banner.adapter.BannerSubscribeAdapter;
import tv.ismar.homepage.control.OrderControl;
import tv.ismar.homepage.fragment.ChannelFragment;
import tv.ismar.homepage.view.BannerLinearLayout;
	/*add by dragontec for bug 4077 start*/
/*add by dragontec for bug 4077 end*/

import static android.view.MotionEvent.BUTTON_PRIMARY;
/*delete by dragontec for bug 4057 start*/
//import static tv.ismar.homepage.HomeActivity.mHoverView;
/*delete by dragontec for bug 4057 end*/
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static tv.ismar.homepage.control.FetchDataControl.FETCH_BANNERS_LIST_FLAG;
import static tv.ismar.homepage.control.FetchDataControl.FETCH_M_BANNERS_LIST_NEXTPAGE_FLAG;

/**
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: 预约模版
 */
public class TemplateOrder extends Template
        implements BaseControl.ControlCallBack, View.OnHoverListener, View.OnClickListener {

    private static final String TAG = "TemplateOrder";
    private OrderControl mControl;

    private RecyclerViewTV subscribeBanner;
    private BannerSubscribeAdapter subscribeAdapter;
    private String mBannerName;

    private boolean isViewInit = false;
    private TextView mTitleTv;
    private String mBannerTitle;

    private View navigationLeft;
    private View navigationRight;
    private BannerLinearLayout mBannerLinearLayout;

    private LinearLayoutManagerTV subscribeLayoutManager;
    private int selectedItemOffset;
    private static final int NAVIGATION_LEFT = 0x0001;
    private static final int NAVIGATION_RIGHT = 0x0002;

    private NavigationtHandler mNavigationtHandler;

    private class NavigationtHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case NAVIGATION_LEFT:
                    if (subscribeBanner!=null&&!subscribeBanner.cannotScrollBackward(-10)) {
                        navigationLeft.setVisibility(VISIBLE);
                    }else if (subscribeBanner!=null){
                        navigationLeft.setVisibility(INVISIBLE);
                    }
                    break;
                case NAVIGATION_RIGHT:
                    if(subscribeBanner!=null&&!subscribeBanner.cannotScrollForward(10)){
                        navigationRight.setVisibility(VISIBLE);
                    }else if (subscribeBanner!=null){
                        navigationRight.setVisibility(INVISIBLE);
                    }
                    break;
            }
        }
    }



    private String channelName;
    private Subscription fetchSubscribeBanner;

    public TemplateOrder(Context context) {
        super(context);
        mControl = new OrderControl(mContext, this);
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
        if (fetchSubscribeBanner != null && !fetchSubscribeBanner.isUnsubscribed()) {
            fetchSubscribeBanner.unsubscribe();
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
        if (!isViewInit) {
            isViewInit = true;
            navigationLeft = view.findViewById(R.id.navigation_left);
            navigationRight = view.findViewById(R.id.navigation_right);
            navigationLeft.setOnClickListener(this);
            navigationRight.setOnClickListener(this);
            navigationRight.setOnHoverListener(this);
            navigationLeft.setOnHoverListener(this);

            mBannerLinearLayout = (BannerLinearLayout) view.findViewById(R.id.banner_layout);
            mBannerLinearLayout.setNavigationLeft(navigationLeft);
            mBannerLinearLayout.setNavigationRight(navigationRight);

            //            mBannerLinearLayout.setOnHoverListener(new View.OnHoverListener() {
            //                @Override
            //                public boolean onHover(View v, MotionEvent event) {
            //                    Log.d(TAG , "mBannerLinearLayout  onHover");
            //                    return false;
            //                }
            //            });
            //            navigationLeft.setOnHoverListener(this);
            //            navigationRight.setOnHoverListener(this);

            //            arrowNormalLeft = view.findViewById(R.id.banner_arrow_left_normal);
            //            arrowNormalRight = view.findViewById(R.id.banner_arrow_right_normal);
            //
            //            arrowFocusLeft = view.findViewById(R.id.banner_arrow_left_focus);
            //            arrowFocusRight = view.findViewById(R.id.banner_arrow_right_focus);

            mTitleCountTv = (TextView) view.findViewById(R.id.banner_title_count);
            mTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
            subscribeBanner = (RecyclerViewTV) view.findViewById(R.id.subscribe_banner);
			/*modify by dragontec for bug 4221 start*/
            subscribeBanner.setTag("recycleView");
			/*modify by dragontec for bug 4221 end*/
            mBannerLinearLayout.setRecyclerViewTV(subscribeBanner);
            subscribeLayoutManager =
                    new LinearLayoutManagerTV(mContext, LinearLayoutManagerTV.HORIZONTAL, false);
            int selectedItemSpace =
                    mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_SelectedItemSpace);
            //            subscribeBanner.addItemDecoration(new
            // BannerSubscribeAdapter.SpacesItemDecoration(selectedItemSpace));
            subscribeBanner.setLayoutManager(subscribeLayoutManager);
            subscribeBanner.setSelectedItemAtCentered(false);
            selectedItemOffset =
                    mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
            subscribeBanner.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);
            subscribeBanner.setPagingableListener(
                    new RecyclerViewTV.PagingableListener() {
                        @Override
                        public void onLoadMoreItems() {
                            if (subscribeAdapter != null) {
                                int currentPageNumber = subscribeAdapter.getCurrentPageNumber();
                                if (currentPageNumber < subscribeAdapter.getTotalPageCount()) {
                                    fetchSubscribeBanner(mBannerName, currentPageNumber + 1);
                                }
                            }
                        }
                    });

            subscribeLayoutManager.setFocusSearchFailedListener(
                    new LinearLayoutManagerTV.FocusSearchFailedListener() {
                        @Override
                        public View onFocusSearchFailed(
                                View view,
                                int focusDirection,
                                RecyclerView.Recycler recycler,
                                RecyclerView.State state) {
                            if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
                                Log.d(TAG, "onFocusSearchFailed");
                                if (subscribeBanner.getChildAt(0).findViewById(R.id.item_layout) == view
                                        || subscribeBanner
                                        .getChildAt(subscribeBanner.getChildCount() - 1)
                                        .findViewById(R.id.item_layout)
                                        == view) {
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
                            /*modify by dragontec for bug 4221 start*/
                            return findNextUpDownFocus(focusDirection, mBannerLinearLayout);
                            /*modify by dragontec for bug 4221 end*/
                        }
                    });

            subscribeBanner.setOnItemFocusChangeListener(
                    new RecyclerViewTV.OnItemFocusChangeListener() {
                        @Override
                        public void onItemFocusGain(View itemView, int position) {
                            if (itemView != null
                                    && mContext != null
                                    && mTitleCountTv != null
                                    && subscribeAdapter != null) {
                                mTitleCountTv.setText(
                                        String.format(
                                                mContext.getString(R.string.home_item_title_count),
                                                (position + 1) + "",
                                                subscribeAdapter.getTatalItemCount() + ""));
                            }
                        }
                    });

            //        subscribeBanner.setOnHoverListener(new View.OnHoverListener() {
            //            @Override
            //            public boolean onHover(View v, MotionEvent event) {
            //                switch (event.getAction()) {
            //                    case MotionEvent.ACTION_HOVER_ENTER:
            //                    case MotionEvent.ACTION_HOVER_MOVE:
            //                        showNavigation(true);
            //                        break;
            //                    case MotionEvent.ACTION_HOVER_EXIT:
            //                        showNavigation(false);
            //                        break;
            //                }
            //                return false;
            //            }
            //        });

            //
            //        subscribeArrowLeft = view.findViewById(R.id.subscribe_arrow_left);
            //        subscribeArrowLeft.setOnHoverListener(new View.OnHoverListener() {
            //            @Override
            //            public boolean onHover(View v, MotionEvent event) {
            //                switch (event.getAction()) {
            //                    case MotionEvent.ACTION_HOVER_MOVE:
            //                    case MotionEvent.ACTION_HOVER_ENTER:
            //
            // v.findViewById(R.id.banner_arrow_left_normal).setVisibility(View.INVISIBLE);
            //
            // v.findViewById(R.id.banner_arrow_left_focus).setVisibility(View.VISIBLE);
            //                        break;
            //                    case MotionEvent.ACTION_HOVER_EXIT:
            //
            // v.findViewById(R.id.banner_arrow_left_focus).setVisibility(View.INVISIBLE);
            //
            // v.findViewById(R.id.banner_arrow_left_normal).setVisibility(View.VISIBLE);
            //                        break;
            //                }
            //                return false;
            //            }
            //        });
            //        subscribeArrowLeft.setOnClickListener(new View.OnClickListener() {
            //            @Override
            //            public void onClick(View v) {
            //                subscribeBanner.smoothScrollBy(-400, 0);
            //            }
            //        });
            //
            //        subscribeArrowRight = view.findViewById(R.id.subscribe_arrow_right);
            //        subscribeArrowRight.setOnHoverListener(new View.OnHoverListener() {
            //            @Override
            //            public boolean onHover(View v, MotionEvent event) {
            //                switch (event.getAction()) {
            //                    case MotionEvent.ACTION_HOVER_MOVE:
            //                    case MotionEvent.ACTION_HOVER_ENTER:
            //
            // v.findViewById(R.id.banner_arrow_right_normal).setVisibility(View.INVISIBLE);
            //
            // v.findViewById(R.id.banner_arrow_right_focus).setVisibility(View.VISIBLE);
            //                        break;
            //                    case MotionEvent.ACTION_HOVER_EXIT:
            //
            // v.findViewById(R.id.banner_arrow_right_focus).setVisibility(View.INVISIBLE);
            //
            // v.findViewById(R.id.banner_arrow_right_normal).setVisibility(View.VISIBLE);
            //                        break;
            //                }
            //                return false;
            //            }
            //        });
            //
            //        subscribeArrowRight.setOnClickListener(new View.OnClickListener() {
            //            @Override
            //            public void onClick(View v) {
            //                subscribeBanner.smoothScrollBy(400, 0);
            //            }
            //        });
        }
    }

    @Override
    public void initData(Bundle bundle) {
        mBannerName = bundle.getString("banner");
        mBannerTitle = bundle.getString("title");
        channelName = bundle.getString(ChannelFragment.CHANNEL_KEY);
        mTitleTv.setText(mBannerTitle);
        mTitleCountTv.setText("00/00");
/*modify by dragontec for bug 4200 start*/
    }

	@Override
	public void fetchData() {
		hasAppeared = true;
		fetchSubscribeBanner(mBannerName, 1);
	}
/*modify by dragontec for bug 4200 end*/

    @Override
    public void callBack(int flags, Object... args) {
        BannerEntity bannerEntity = (BannerEntity) args[0];
        if (flags == FETCH_BANNERS_LIST_FLAG) {
            fillSubscribeBanner(bannerEntity);
        } else if (flags == FETCH_M_BANNERS_LIST_NEXTPAGE_FLAG) {
            subscribeAdapter.addDatas(bannerEntity);
        }
    }

    private void fetchSubscribeBanner(String bannerName, final int pageNumber) {
        if (pageNumber != 1) {
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
            //            subscribeAdapter.setCurrentPageNumber(pageNumber);
            //            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
        }

        fetchSubscribeBanner = SkyService.ServiceManager.getService()
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
                                if (pageNumber == 1) {
                                    fillSubscribeBanner(bannerEntity);
                                } else {
                                    int mSavePos = subscribeBanner.getSelectPostion();
                                    subscribeAdapter.addDatas(bannerEntity);
                                    //                            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
                                }
                            }
                        });
    }

    private void fillSubscribeBanner(final BannerEntity bannerEntity) {
        subscribeAdapter = new BannerSubscribeAdapter(mContext, bannerEntity);
        subscribeAdapter.setSubscribeClickListener(
                new BannerSubscribeAdapter.OnBannerClickListener() {
                    @Override
                    public void onBannerClick(View view, int position) {
                        if (position < bannerEntity.getCount()) {
                            goToNextPage(view);
                        } else {
                        }
                    }
                });
        subscribeAdapter.setSubscribeHoverListener(
                new BannerSubscribeAdapter.OnBannerHoverListener() {
                    @Override
/*modify by dragontec for bug 4057 start*/
//                    public void onBannerHover(View view, int position, boolean hovered) {
                    public void onBannerHover(View view, int position, boolean hovered, boolean isPrimary) {
/*modify by dragontec for bug 4057 end*/
                        if (hovered) {
                            //                    mLastFocusView = view;
/*delete by dragontec for bug 4057 start*/
//                            mHoverView.setFocusable(true);
/*delete by dragontec for bug 4057 end*/
                            subscribeBanner.setHovered(true);
                            mTitleCountTv.setText(
                                    String.format(
                                            mContext.getString(R.string.home_item_title_count),
                                            (position + 1) + "",
                                            subscribeAdapter.getTatalItemCount() + ""));
                        } else {
                            subscribeBanner.setHovered(false);
/*modify by dragontec for bug 4057 start*/
//                            mHoverView.requestFocus();
                            if (!isPrimary) {
                                view.clearFocus();
                            }
/*modify by dragontec for bug 4057 end*/
                        }
                    }
                });
        subscribeBanner.setAdapter(subscribeAdapter);
        mTitleCountTv.setText(
                String.format(
                        mContext.getString(R.string.home_item_title_count),
                        (1) + "",
                        subscribeAdapter.getTatalItemCount() + ""));
	/*add by dragontec for bug 4077 start*/
		checkFocus(subscribeBanner);
	/*add by dragontec for bug 4077 end*/
    }

    private void showNavigation(boolean isHovered) {
        //        if (isHovered){
        //            navigationLeft.setVisibility(View.VISIBLE);
        //            navigationRight.setVisibility(View.VISIBLE);
        //        }else {
        //            navigationLeft.setVisibility(View.INVISIBLE);
        //            navigationRight.setVisibility(View.INVISIBLE);
        //        }
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
                    navigationLeft.setVisibility(INVISIBLE);
                    navigationRight.setVisibility(INVISIBLE);
/*add by dragontec for bug 4057 start*/
                    v.clearFocus();
/*add by dragontec for bug 4057 end*/
                }
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.navigation_left) {
            subscribeLayoutManager.setCanScroll(true);
            if (subscribeLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) {
                int targetPosition = subscribeLayoutManager.findFirstCompletelyVisibleItemPosition() - 4;
                if (targetPosition >= 0) {
                    // 表示可以滑动
                } else {
                    targetPosition = 0;
                }
                setBannerItemCount(targetPosition);
                subscribeLayoutManager.smoothScrollToPosition(subscribeBanner, null, targetPosition);
                if (targetPosition == 0){
                    mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_LEFT,500);
                }
            }
        } else if (i == R.id.navigation_right) {
            subscribeLayoutManager.setCanScroll(true);
            subscribeBanner.loadMore();
            if (subscribeLayoutManager.findFirstCompletelyVisibleItemPosition() + 1
                    <= subscribeAdapter.getTatalItemCount()) {

                int targetPosition = subscribeLayoutManager.findLastCompletelyVisibleItemPosition() + 4;
                if (targetPosition < subscribeAdapter.getTatalItemCount()) {
                    // 表示可以滑动
                } else {
                    targetPosition = subscribeAdapter.getTatalItemCount() - 1;
                }
                setBannerItemCount(targetPosition);
                subscribeLayoutManager.smoothScrollToPosition(subscribeBanner, null, targetPosition);
                if (targetPosition == subscribeAdapter.getTatalItemCount() - 1){
                    mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_RIGHT, 500);
                }
            }
        }
    }

    private void setBannerItemCount(int position) {
        mTitleCountTv.setText(
                String.format(
                        mContext.getString(R.string.home_item_title_count),
                        (position + 1) + "",
                        subscribeAdapter.getTatalItemCount() + ""));
    }
}
