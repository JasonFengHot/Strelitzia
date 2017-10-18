package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.CenterAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.view.BannerLinearLayout;

import static android.view.MotionEvent.BUTTON_PRIMARY;

/**
 * @AUTHOR: xi @DATE: 2017/9/5 @DESC: 居中模版
 */
public class TemplateCenter extends Template
        implements BaseControl.ControlCallBack,
        RecyclerViewTV.PagingableListener,
        LinearLayoutManagerTV.FocusSearchFailedListener,
        OnItemClickListener,
        View.OnHoverListener {
    public FetchDataControl mFetchDataControl = null;
    private RecyclerViewTV mRecycleView; // 海报recycleview
    private LinearLayoutManagerTV mCenterLayoutManager;
    private CenterAdapter mAdapter;
    private BannerLinearLayout mBannerLinearLayout;
    private View navigationLeft;
    private View navigationRight;
    private int mBannerPk;

    public TemplateCenter(Context context) {
        super(context);
        mFetchDataControl = new FetchDataControl(context, this);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
        if (mFetchDataControl != null){
            mFetchDataControl.stop();
        }
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void getView(View view) {
        mRecycleView = (RecyclerViewTV) view.findViewById(R.id.center_recyclerview);
        mCenterLayoutManager =
                new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecycleView.setLayoutManager(mCenterLayoutManager);
        mRecycleView.setSelectedItemAtCentered(true);
        navigationLeft = view.findViewById(R.id.navigation_left);
        navigationRight = view.findViewById(R.id.navigation_right);
        mBannerLinearLayout = (BannerLinearLayout) view.findViewById(R.id.banner_layout);
        mBannerLinearLayout.setNavigationLeft(navigationLeft);
        mBannerLinearLayout.setNavigationRight(navigationRight);
    }

    @Override
    public void initData(Bundle bundle) {
        mBannerPk = bundle.getInt("banner");
        mFetchDataControl.fetchBanners(mBannerPk, 1, false);
    }

    @Override
    protected void initListener(View view) {
        super.initListener(view);
        navigationRight.setOnHoverListener(this);
        navigationLeft.setOnHoverListener(this);
        mRecycleView.setPagingableListener(this);
        mCenterLayoutManager.setFocusSearchFailedListener(this);
    }

    private void initRecycle() {
        if (mAdapter == null) {
            mAdapter = new CenterAdapter(mContext, mFetchDataControl.mCarousels);
            mRecycleView.setAdapter(mAdapter);
            mCenterLayoutManager.scrollToPositionWithOffset(
                    mFetchDataControl.mCarousels.size() * 100,
                    mContext.getResources().getDimensionPixelOffset(R.dimen.center_padding_offset));
            mAdapter.setOnItemClickListener(this);
        } else {
            int start =
                    mFetchDataControl.mCarousels.size() - mFetchDataControl.mHomeEntity.carousels.size();
            int end = mFetchDataControl.mPoster.size();
            mAdapter.notifyItemRangeChanged(start, end);
        }
    }

    @Override
    public void callBack(int flags, Object... args) {
        if (flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG) { // 获取单个banner业务
            initRecycle();
        }
    }

    @Override
    public void onLoadMoreItems() {
        Log.i(TAG, "onLoadMoreItems");
        HomeEntity homeEntity = mFetchDataControl.mHomeEntity;
        if (homeEntity != null) {
            if (homeEntity.page < homeEntity.num_pages) {
                mRecycleView.setOnLoadMoreComplete();
                mFetchDataControl.fetchBanners(mBannerPk, ++homeEntity.page, true);
            }
        }
    }

    @Override
    public View onFocusSearchFailed(
            View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
            if (mRecycleView.getChildAt(0).findViewById(R.id.center_ismartv_linear_layout) == focused
                    || mRecycleView
                    .getChildAt(mRecycleView.getChildCount() - 1)
                    .findViewById(R.id.center_ismartv_linear_layout)
                    == focused) {
                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
            }
            return focused;
        }
        return null;
    }

    @Override
    public void onItemClick(View view, int position) {
        mFetchDataControl.go2Detail(mFetchDataControl.mCarousels.get(position));
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
                    HomeActivity.mHoverView.requestFocus(); // 将焦点放置到一块隐藏view中
                }
                break;
        }
        return false;
    }
}
