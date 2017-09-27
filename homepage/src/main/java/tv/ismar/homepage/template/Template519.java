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
import tv.ismar.homepage.banner.adapter.BannerSubscribeAdapter;
import tv.ismar.homepage.view.BannerLinearLayout;

import static android.view.MotionEvent.BUTTON_PRIMARY;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 519横图模版
 */

public class Template519 extends Template implements View.OnClickListener, View.OnHoverListener{
    private static final String TAG = Template519.class.getSimpleName();

    private RecyclerViewTV horizontal519Banner;
    private BannerHorizontal519Adapter mHorizontal519Adapter;
    private int mBannerName;
    private String mBannerTitle;
    private TextView mTitleTv;

    private View navigationLeft;
    private View navigationRight;
    private BannerLinearLayout mBannerLinearLayout;
    private LinearLayoutManagerTV horizontal519LayoutManager;

    public Template519(Context context) {
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
        horizontal519Banner = (RecyclerViewTV)view.findViewById(R.id.horizontal_519_banner);
         horizontal519LayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        int selectedItemSpace = mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_SelectedItemSpace);
//        horizontal519Banner.addItemDecoration(new BannerHorizontal519Adapter.SpacesItemDecoration(selectedItemSpace));
        horizontal519Banner.setLayoutManager(horizontal519LayoutManager);
        horizontal519Banner.setSelectedItemAtCentered(false);
        int selectedItemOffset = mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
        horizontal519Banner.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);

        horizontal519Banner.setPagingableListener(new RecyclerViewTV.PagingableListener() {
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

        horizontal519Banner.setOnItemFocusChangeListener(new RecyclerViewTV.OnItemFocusChangeListener() {
            @Override
            public void onItemFocusGain(View itemView, int position) {
                if (itemView != null && mContext != null && mTitleCountTv != null && mHorizontal519Adapter != null
                        && position < mHorizontal519Adapter.getTatalItemCount()) {
                    mTitleCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), (1 + position) + "", mHorizontal519Adapter.getTatalItemCount() + ""));
                }
            }
        });
    }

    @Override
    public void initData(Bundle bundle) {
        mBannerName = bundle.getInt("banner");
        mBannerTitle = bundle.getString("title");
        mTitleTv.setText(mBannerTitle);
        fetchHorizontal519Banner(mBannerName, 1);
    }

    private void fetchHorizontal519Banner(int bannerName, final int pageNumber) {
        if (pageNumber != 1){
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
//                        List<BannerEntity.PosterBean> posterBeanList = bannerSubscribeEntities.getPoster();
//                        fillHorizontal519Banner(posterBeanList);

                        if (pageNumber == 1){
                            fillHorizontal519Banner(bannerEntity);
                        }else {
                            int mSavePos = horizontal519Banner.getSelectPostion();
                            mHorizontal519Adapter.addDatas(bannerEntity);
//                            mFocusHandler.sendEmptyMessageDelayed(mSavePos, 10);
                        }
                    }
                });
    }

    private void fillHorizontal519Banner(BannerEntity bannerEntity) {
        mHorizontal519Adapter = new BannerHorizontal519Adapter(mContext, bannerEntity);
        mHorizontal519Adapter.setHoverListener(new BannerHorizontal519Adapter.OnBannerHoverListener() {
            @Override
            public void onBannerHover(View view, int position, boolean hovered) {
//                Log.d(TAG, view + " : " + hovered);
                if (hovered){
                    horizontal519Banner.setHovered(true);
                }else {
                    horizontal519Banner.setHovered(false);
                    HomeActivity.mHoverView.requestFocus();
//                    Log.d(TAG, view + " : " + hovered);
//                    Log.d(TAG, "view id: " + view.getId());
//                    HomeActivity.mHoverView.setNextFocusUpId(view.getId());
//                    HomeActivity.mHoverView.setNextFocusDownId(view.getId());
//                    HomeActivity.mHoverView.setNextFocusRightId(view.getId());
//                    HomeActivity.mHoverView.setNextFocusLeftId(view.getId());
                }
            }
        });
        mHorizontal519Adapter.setBannerClickListener(new BannerHorizontal519Adapter.OnBannerClickListener() {
            @Override
            public void onBannerClick(View view, int position) {
                goToNextPage(view);
            }
        });
        horizontal519Banner.setAdapter(mHorizontal519Adapter);
        mTitleCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), (1) + "", mHorizontal519Adapter.getTatalItemCount() + ""));
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.navigation_left) {
//            horizontal519LayoutManager.scrollToPositionWithOffset(horizontal519Banner.findFirstVisibleItemPosition() - 1, 0);

            if (horizontal519LayoutManager.findFirstCompletelyVisibleItemPosition() -1 >= 0){
                horizontal519LayoutManager.smoothScrollToPosition(horizontal519Banner, null, horizontal519LayoutManager.findFirstCompletelyVisibleItemPosition() - 1);
            }
        } else if (i == R.id.navigation_right) {
            horizontal519Banner.loadMore();

            if (horizontal519LayoutManager.findFirstCompletelyVisibleItemPosition() + 1 <= mHorizontal519Adapter.getTatalItemCount()){
                horizontal519LayoutManager.smoothScrollToPosition(horizontal519Banner, null, horizontal519LayoutManager.findLastCompletelyVisibleItemPosition() + 1);
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
