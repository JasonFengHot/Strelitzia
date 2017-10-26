package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.homepage.R;
import tv.ismar.homepage.banner.adapter.BannerHorizontal519Adapter;
import tv.ismar.homepage.fragment.ChannelFragment;
import tv.ismar.homepage.view.BannerLinearLayout;

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/*add by dragontec for bug 4077 start*/
/*add by dragontec for bug 4077 end*/
/*add by dragontec for bug 4077 start*/
/*add by dragontec for bug 4077 end*/

/**
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: 519横图模版
 */
public class Template519 extends Template implements View.OnClickListener, View.OnHoverListener {
    private static final String TAG = Template519.class.getSimpleName();

    private RecyclerViewTV horizontal519Banner;
    private BannerHorizontal519Adapter mHorizontal519Adapter;
    private String mBannerName;
    private String mBannerTitle;
    private TextView mTitleTv;

    private View navigationLeft;
    private View navigationRight;
    private BannerLinearLayout mBannerLinearLayout;
    private LinearLayoutManagerTV horizontal519LayoutManager;
    private String channelName;
    private String nameKey;
    private boolean isMore;
    private Subscription fetchHorizontal519Banner;

    private static final int NAVIGATION_LEFT = 0x0001;
    private static final int NAVIGATION_RIGHT = 0x0002;

    private NavigationtHandler mNavigationtHandler;

    private class NavigationtHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case NAVIGATION_LEFT:
                    if (horizontal519Banner!=null&&!horizontal519Banner.cannotScrollBackward(-10)) {
                        navigationLeft.setVisibility(VISIBLE);
                    }else if (horizontal519Banner!=null){
                        navigationLeft.setVisibility(INVISIBLE);
                    }
                    break;
                case NAVIGATION_RIGHT:
                    if(horizontal519Banner!=null&&!horizontal519Banner.cannotScrollForward(10)){
                        navigationRight.setVisibility(VISIBLE);
                    }else if (horizontal519Banner!=null){
                        navigationRight.setVisibility(INVISIBLE);
                    }
                    break;
            }
        }
    }


    public Template519(Context context) {
        super(context);
        Logger.t(TAG).d("Template519 construct");
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
        Log.d(TAG, "onPause()");
        if (fetchHorizontal519Banner != null && !fetchHorizontal519Banner.isUnsubscribed()) {
            fetchHorizontal519Banner.unsubscribe();
        }
	/*add by dragontec for bug 4077 start*/
        super.onPause();
	/*add by dragontec for bug 4077 end*/
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop()");
        if (mNavigationtHandler.hasMessages(NAVIGATION_LEFT)){
            mNavigationtHandler.removeMessages(NAVIGATION_LEFT);
        }
        if (mNavigationtHandler.hasMessages(NAVIGATION_RIGHT)){
            mNavigationtHandler.removeMessages(NAVIGATION_RIGHT);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        if (mNavigationtHandler != null){
            mNavigationtHandler = null;
        }
//        RefWatcher refWatcher = VodApplication.getRefWatcher(mContext);
//        refWatcher.watch(this);
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
        horizontal519Banner = (RecyclerViewTV) view.findViewById(R.id.horizontal_519_banner);
		/*modify by dragontec for bug 4221 start*/
        horizontal519Banner.setTag("recycleView");
		/*modify by dragontec for bug 4221 end*/
        mBannerLinearLayout.setRecyclerViewTV(horizontal519Banner);
        horizontal519LayoutManager =
                new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        int selectedItemSpace =
                mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_SelectedItemSpace);
        //        horizontal519Banner.addItemDecoration(new
        // BannerHorizontal519Adapter.SpacesItemDecoration(selectedItemSpace));
        horizontal519Banner.setLayoutManager(horizontal519LayoutManager);
        horizontal519Banner.setSelectedItemAtCentered(false);
        int selectedItemOffset =
                mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
        horizontal519Banner.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);

        horizontal519Banner.setPagingableListener(
                new RecyclerViewTV.PagingableListener() {
                    @Override
                    public void onLoadMoreItems() {
                        Log.d("PagingableListener", "onLoadMoreItems");
                        if (mHorizontal519Adapter != null) {
                            int currentPageNumber = mHorizontal519Adapter.getCurrentPageNumber();
                            if (currentPageNumber < mHorizontal519Adapter.getTotalPageCount()) {
                                fetchHorizontal519Banner(mBannerName, currentPageNumber + 1);
                            }
                        }
                    }
                });
        horizontal519LayoutManager.setFocusSearchFailedListener(
                new LinearLayoutManagerTV.FocusSearchFailedListener() {
                    @Override
                    public View onFocusSearchFailed(
                            View view,
                            int focusDirection,
                            RecyclerView.Recycler recycler,
                            RecyclerView.State state) {
                        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
                            if (horizontal519Banner.getChildAt(0).findViewById(R.id.item_layout) == view
                                    || horizontal519Banner
                                    .getChildAt(horizontal519Banner.getChildCount() - 1)
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

        horizontal519Banner.setOnItemFocusChangeListener(
                new RecyclerViewTV.OnItemFocusChangeListener() {
                    @Override
                    public void onItemFocusGain(View itemView, int position) {
                        if (itemView != null
                                && mContext != null
                                && mTitleCountTv != null
                                && mHorizontal519Adapter != null
                                && position < mHorizontal519Adapter.getTatalItemCount()) {
                            mTitleCountTv.setText(
                                    String.format(
                                            mContext.getString(R.string.home_item_title_count),
                                            (1 + position) + "",
                                            mHorizontal519Adapter.getTatalItemCount() + ""));
                        }
                    }
                });
    }

    @Override
    public void initData(Bundle bundle) {
        mBannerName = bundle.getString("banner");
        mBannerTitle = bundle.getString("title");
        channelName = bundle.getString(ChannelFragment.CHANNEL_KEY);
        nameKey = bundle.getString(ChannelFragment.NAME_KEY);
        mTitleTv.setText(mBannerTitle);
        mTitleCountTv.setText("00/00");
/*modify by dragontec for bug 4200 start*/
    }

	@Override
	public void fetchData() {
		hasAppeared = true;
		fetchHorizontal519Banner(mBannerName, 1);
	}
/*modify by dragontec for bug 4200 end*/

    private void fetchHorizontal519Banner(String bannerName, final int pageNumber) {
        if (pageNumber != 1) {
            int startIndex = (pageNumber - 1) * 33;
            int endIndex;
            if (pageNumber == mHorizontal519Adapter.getTotalPageCount()) {
                endIndex = mHorizontal519Adapter.getTatalItemCount() - 1;
            } else {
                endIndex = pageNumber * 33 - 1;
            }

            BannerEntity.PosterBean emptyPostBean = new BannerEntity.PosterBean();
            List<BannerEntity.PosterBean> totalPostList = new ArrayList<>();
            for (int i = startIndex; i <= endIndex; i++) {
                totalPostList.add(emptyPostBean);
            }
            mHorizontal519Adapter.addEmptyDatas(totalPostList);
            int mSavePos = horizontal519Banner.getSelectPostion();
            mHorizontal519Adapter.notifyItemRangeInserted(startIndex, endIndex - startIndex);
            horizontal519Banner.setOnLoadMoreComplete();
            //            mMovieAdapter.setCurrentPageNumber(pageNumber);
            //            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
        }

        fetchHorizontal519Banner = SkyService.ServiceManager.getService()
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
                                //                        List<BannerEntity.PosterBean> posterBeanList =
                                // bannerSubscribeEntities.getPoster();
                                //                        fillHorizontal519Banner(posterBeanList);

                                if (pageNumber == 1) {
                                    fillHorizontal519Banner(bannerEntity);
                                } else {
                                    int mSavePos = horizontal519Banner.getSelectPostion();
                                    mHorizontal519Adapter.addDatas(bannerEntity);
                                    //                            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
                                }
                            }
                        });
    }

    private void fillHorizontal519Banner(final BannerEntity bannerEntity) {
        mHorizontal519Adapter = new BannerHorizontal519Adapter(mContext, bannerEntity);
        mHorizontal519Adapter.setHoverListener(
                new BannerHorizontal519Adapter.OnBannerHoverListener() {
                    @Override
/*modify by dragontec for bug 4057 start*/
//                    public void onBannerHover(View view, int position, boolean hovered) {
                    public void onBannerHover(View view, int position, boolean hovered, boolean isPrimary) {
/*modify by dragontec for bug 4057 end*/
                        //                Log.d(TAG, view + " : " + hovered);
                        if (hovered) {
                            horizontal519Banner.setHovered(true);
                            mTitleCountTv.setText(
                                    String.format(
                                            mContext.getString(R.string.home_item_title_count),
                                            (1 + position) + "",
                                            mHorizontal519Adapter.getTatalItemCount() + ""));
                        } else {
                            horizontal519Banner.setHovered(false);
/*modify by dragontec for bug 4057 start*/
//                            HomeActivity.mHoverView.requestFocus();
                            if (!isPrimary) {
                                view.clearFocus();
                            }
/*modify by dragontec for bug 4057 end*/
                            //                    Log.d(TAG, view + " : " + hovered);
                            //                    Log.d(TAG, "view id: " + view.getId());
                            //                    HomeActivity.mHoverView.setNextFocusUpId(view.getId());
                            //                    HomeActivity.mHoverView.setNextFocusDownId(view.getId());
                            //                    HomeActivity.mHoverView.setNextFocusRightId(view.getId());
                            //                    HomeActivity.mHoverView.setNextFocusLeftId(view.getId());
                        }
                    }
                });
        mHorizontal519Adapter.setBannerClickListener(
                new BannerHorizontal519Adapter.OnBannerClickListener() {
                    @Override
                    public void onBannerClick(View view, int position) {
                        if (position < bannerEntity.getCount()) {
                            goToNextPage(view);
                        } else {
                            Logger.t(TAG).d("more click: title -> %s, channel -> %s", nameKey, channelName);
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
        horizontal519Banner.setAdapter(mHorizontal519Adapter);
        mTitleCountTv.setText(
                String.format(
                        mContext.getString(R.string.home_item_title_count),
                        (1) + "",
                        mHorizontal519Adapter.getTatalItemCount() + ""));
	/*add by dragontec for bug 4077 start*/
		checkFocus(horizontal519Banner);
	/*add by dragontec for bug 4077 end*/
    }

    @Override
    public void onClick(View v) {
        int totalItemCount =
                isMore
                        ? mHorizontal519Adapter.getTatalItemCount() + 1
                        : mHorizontal519Adapter.getTatalItemCount();
        int i = v.getId();
        if (i == R.id.navigation_left) {
            horizontal519LayoutManager.setCanScroll(true);
            //
            // horizontal519LayoutManager.scrollToPositionWithOffset(horizontal519Banner.findFirstVisibleItemPosition() - 1, 0);

            if (horizontal519LayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) {
                int targetPosition =
                        horizontal519LayoutManager.findFirstCompletelyVisibleItemPosition() - 3;
                if (targetPosition >= 0) {
                    // 表示可以滑动
                } else {
                    targetPosition = 0;
                }
                setBannerItemCount(targetPosition);
                horizontal519LayoutManager.smoothScrollToPosition(
                        horizontal519Banner, null, targetPosition);
                if (mNavigationtHandler.hasMessages(NAVIGATION_LEFT)) {
                    mNavigationtHandler.removeMessages(NAVIGATION_LEFT);
                }
                mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_LEFT, 500);
            }
        } else if (i == R.id.navigation_right) {
            horizontal519LayoutManager.setCanScroll(true);
            horizontal519Banner.loadMore();

            if (horizontal519LayoutManager.findLastCompletelyVisibleItemPosition() + 1
                    <= mHorizontal519Adapter.getTatalItemCount()) {
                int targetPosition = horizontal519LayoutManager.findLastCompletelyVisibleItemPosition() + 3;
                if (targetPosition < totalItemCount) {
                    // 表示可以滑动
                } else {
                    targetPosition = totalItemCount - 1;
                }
                setBannerItemCount(
                        targetPosition >= mHorizontal519Adapter.getTatalItemCount()
                                ? mHorizontal519Adapter.getTatalItemCount() - 1
                                : targetPosition);
                horizontal519LayoutManager.smoothScrollToPosition(
                        horizontal519Banner, null, targetPosition);
                if (mNavigationtHandler.hasMessages(NAVIGATION_RIGHT)) {
                    mNavigationtHandler.removeMessages(NAVIGATION_RIGHT);
                }
                mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_RIGHT, 500);
            }
        }
    }

    private void setBannerItemCount(int position) {
        mTitleCountTv.setText(
                String.format(
                        mContext.getString(R.string.home_item_title_count),
                        (position + 1) + "",
                        mHorizontal519Adapter.getTatalItemCount() + ""));
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
