package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import java.util.List;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.BannerRecommend;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.RecommendAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.view.BannerLinearLayout;

import static android.view.MotionEvent.BUTTON_PRIMARY;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/15
 * @DESC: 推荐模版
 */

public class TemplateRecommend extends Template implements BaseControl.ControlCallBack,
        RecyclerViewTV.PagingableListener, LinearLayoutManagerTV.FocusSearchFailedListener,
        OnItemClickListener, View.OnHoverListener, View.OnClickListener {
    private RecyclerViewTV mRecyclerView;
    private LinearLayoutManagerTV mRecommendLayoutManager;
    private RecommendAdapter mAdapter;
    private FetchDataControl mFetchDataControl = null;
    private BannerLinearLayout mBannerLinearLayout;
    private View navigationLeft;
    private View navigationRight;

    public TemplateRecommend(Context context) {
        super(context);
        mFetchDataControl = new FetchDataControl(context, this);
    }

    @Override
    public void getView(View view) {
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.conlumn_recyclerview);
        mRecommendLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mRecommendLayoutManager);
        mRecyclerView.setSelectedItemAtCentered(false);
        int selectedItemOffset = mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
        mRecyclerView.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);
        navigationLeft = view.findViewById(R.id.navigation_left);
        navigationRight = view.findViewById(R.id.navigation_right);
        mBannerLinearLayout = (BannerLinearLayout) view.findViewById(R.id.banner_layout);
        mBannerLinearLayout.setNavigationLeft(navigationLeft);
        mBannerLinearLayout.setNavigationRight(navigationRight);
        mRecyclerView.requestFocus();
    }

    @Override
    public void initData(Bundle bundle) {
        mFetchDataControl.fetchHomeRecommend(false);
    }

    @Override
    protected void initListener(View view) {
        navigationLeft.setOnClickListener(this);
        navigationRight.setOnClickListener(this);
        navigationRight.setOnHoverListener(this);
        navigationLeft.setOnHoverListener(this);
        mRecyclerView.setPagingableListener(this);
        mRecommendLayoutManager.setFocusSearchFailedListener(this);
    }

    @Override
    public void callBack(int flags, Object... args) {
        if(flags == FetchDataControl.FETCH_HOME_RECOMMEND_LIST_FLAG){//获取推荐列表
            initRecycleView(mFetchDataControl.mRecommends);
        }
    }

    private void initRecycleView(List<BannerRecommend> recommends){
        if(mAdapter == null){
            mAdapter = new RecommendAdapter(mContext, recommends);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(this);
        }else {
            int start = mFetchDataControl.mPoster.size() - mFetchDataControl.mHomeEntity.posters.size();
            int end = mFetchDataControl.mPoster.size();
            mAdapter.notifyItemRangeChanged(start, end);
        }
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT){
            if (mRecyclerView.getChildAt(0).findViewById(R.id.conlumn_ismartv_linear_layout) == focused ||
                    mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1).findViewById(R.id.conlumn_ismartv_linear_layout) == focused){
                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
            }
            return focused;
        }
        return null;
    }

    @Override
    public void onLoadMoreItems() {
    }

    @Override
    public void onItemClick(View view, int position) {
        mFetchDataControl.go2Detail(mFetchDataControl.mHomeEntity.posters.get(position));
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.navigation_left) {
            if (mRecommendLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) {//向左滑动
                int targetPosition = mRecommendLayoutManager.findFirstCompletelyVisibleItemPosition() - 5;
                if (targetPosition <= 0) targetPosition = 0;
                mRecommendLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
            }
        } else if (i == R.id.navigation_right) {//向右滑动
            mRecyclerView.loadMore();
            if (mRecommendLayoutManager.findLastCompletelyVisibleItemPosition() <= mFetchDataControl.mHomeEntity.count) {
                int targetPosition = mRecommendLayoutManager.findLastCompletelyVisibleItemPosition() + 5;
                if (targetPosition >= mFetchDataControl.mHomeEntity.count) {
                    targetPosition = mFetchDataControl.mHomeEntity.count;
                }
                mRecommendLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
                if (targetPosition == mFetchDataControl.mHomeEntity.count)
                    YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1).findViewById(R.id.tv_player_ismartv_linear_layout));
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
                    HomeActivity.mHoverView.requestFocus();//将焦点放置到一块隐藏view中
                }
                break;
        }
        return false;
    }
}
