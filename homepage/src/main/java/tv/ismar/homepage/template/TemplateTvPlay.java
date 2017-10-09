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

import tv.ismar.app.BaseControl;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.OnItemHoverListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.TvPlayAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.view.BannerLinearLayout;

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static tv.ismar.homepage.fragment.ChannelFragment.CHANNEL_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.NAME_KEY;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 电视剧模版
 */

public class TemplateTvPlay extends Template implements BaseControl.ControlCallBack,
        RecyclerViewTV.PagingableListener, LinearLayoutManagerTV.FocusSearchFailedListener,
        RecyclerViewTV.OnItemFocusChangeListener, OnItemClickListener, OnItemHoverListener,
        View.OnHoverListener, View.OnClickListener {
    private int mSelectItemPosition = 1;//标题--选中海报位置
    private FetchDataControl mFetchDataControl = null;//抓网络数据类
    private TextView mTitleTv;//banner标题
    private RecyclerViewTV mRecycleView;
    private TvPlayAdapter mAdapter;
    private LinearLayoutManagerTV mTvPlayerLayoutManager = null;
    private BannerLinearLayout mBannerLinearLayout;
    private View navigationLeft;
    private View navigationRight;

    public TemplateTvPlay(Context context) {
        super(context);
        mFetchDataControl = new FetchDataControl(context, this);
    }

    @Override
    public void getView(View view) {
        mTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
        mTitleCountTv =(TextView) view.findViewById(R.id.banner_title_count);
        mRecycleView = (RecyclerViewTV) view.findViewById(R.id.tv_player_recyclerview);
        mTvPlayerLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecycleView.setLayoutManager(mTvPlayerLayoutManager);
        mRecycleView.setSelectedItemAtCentered(false);
        int selectedItemOffset = mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
        mRecycleView.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);
        navigationLeft = view.findViewById(R.id.navigation_left);
        navigationRight = view.findViewById(R.id.navigation_right);
        mBannerLinearLayout = (BannerLinearLayout) view.findViewById(R.id.banner_layout);
        mBannerLinearLayout.setNavigationLeft(navigationLeft);
        mBannerLinearLayout.setNavigationRight(navigationRight);
    }

    @Override
    protected void initListener(View view) {
        navigationLeft.setOnClickListener(this);
        navigationRight.setOnClickListener(this);
        navigationRight.setOnHoverListener(this);
        navigationLeft.setOnHoverListener(this);
        mRecycleView.setPagingableListener(this);
        mRecycleView.setOnItemFocusChangeListener(this);
        mTvPlayerLayoutManager.setFocusSearchFailedListener(this);
    }

    private int mBannerPk;
    private String mName;//频道名称（中文）
    private String mChannel;//频道名称（英文）
    @Override
    public void initData(Bundle bundle) {
        mTitleTv.setText(bundle.getString("title"));
        mBannerPk = bundle.getInt("banner");
        mName = bundle.getString(NAME_KEY);
        mChannel = bundle.getString(CHANNEL_KEY);
        mFetchDataControl.fetchBanners(mBannerPk, 1, false);
    }

    private void initTitle(){
        if(mSelectItemPosition > mFetchDataControl.mHomeEntity.count)
            mSelectItemPosition  = mFetchDataControl.mHomeEntity.count;
        mTitleCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), mSelectItemPosition+"",
                mFetchDataControl.mHomeEntity.count+""));
    }

    private void initRecycle(){
        if(mAdapter == null){
            mAdapter = new TvPlayAdapter(mContext, mFetchDataControl.mPoster);
            mAdapter.setMarginLeftEnable(true);
            mRecycleView.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(this);
        }else {
            int start = mFetchDataControl.mPoster.size() - mFetchDataControl.mHomeEntity.posters.size();
            int end = mFetchDataControl.mPoster.size();
            mAdapter.notifyItemRangeChanged(start, end);
        }
    }

    @Override
    public void callBack(int flags, Object... args) {
        if(flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG){//获取单个banner业务
            initTitle();
            initRecycle();
        }
    }

    @Override
    public void onLoadMoreItems() {//加载更多数据
        Log.i(TAG, "onLoadMoreItems");
        HomeEntity homeEntity = mFetchDataControl.mHomeEntity;
        if(homeEntity != null){
            if(homeEntity.page < homeEntity.num_pages){
                mRecycleView.setOnLoadMoreComplete();
                mFetchDataControl.fetchBanners(mBannerPk, ++homeEntity.page, true);
            }
        }
    }

    /*第1个和最后一个海报抖动功能*/
    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT){
            if (mRecycleView.getChildAt(0).findViewById(R.id.tv_player_ismartv_linear_layout) == focused ||
                    mRecycleView.getChildAt(mRecycleView.getChildCount() - 1).findViewById(R.id.tv_player_ismartv_linear_layout) == focused){
                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
            }
            return focused;
        }
        return null;
    }

    @Override
    public void onItemFocusGain(View itemView, int position) {
        mSelectItemPosition = position + 1;
        initTitle();
    }

    @Override
    public void onItemClick(View view, int position) {
        if(position == mFetchDataControl.mHomeEntity.count-1){
            new PageIntent().toListPage(mContext, mName, mChannel, 0);
        } else {
            mFetchDataControl.go2Detail(mFetchDataControl.mHomeEntity.posters.get(position));
        }
    }

    @Override
    public void onHover(View v, int position, boolean hovered) {
        mRecycleView.setHovered(hovered);
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        Log.i("onHover", "Template action:"+event.getAction());
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

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.navigation_left) {
            if (mTvPlayerLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) {//向左滑动
                int targetPosition = mTvPlayerLayoutManager.findFirstCompletelyVisibleItemPosition() - 5;
                if (targetPosition <= 0) targetPosition = 0;
                mSelectItemPosition = targetPosition;
                mTvPlayerLayoutManager.smoothScrollToPosition(mRecycleView, null, targetPosition);
            }
        } else if (i == R.id.navigation_right) {//向右滑动
            mRecycleView.loadMore();
            if (mTvPlayerLayoutManager.findLastCompletelyVisibleItemPosition()  <= mFetchDataControl.mHomeEntity.count) {
                int targetPosition = mTvPlayerLayoutManager.findLastCompletelyVisibleItemPosition() + 5;
                if (targetPosition >= mFetchDataControl.mHomeEntity.count) {
                    targetPosition = mFetchDataControl.mHomeEntity.count;
                }
                mSelectItemPosition = targetPosition;
                mTvPlayerLayoutManager.smoothScrollToPosition(mRecycleView, null, targetPosition);
                if(targetPosition==mFetchDataControl.mHomeEntity.count)
                    YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(mRecycleView.getChildAt(mRecycleView.getChildCount() - 1).findViewById(R.id.tv_player_ismartv_linear_layout));
            }
            initTitle();
        }
    }
}
