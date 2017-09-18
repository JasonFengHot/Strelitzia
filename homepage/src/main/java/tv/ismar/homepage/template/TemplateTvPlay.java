package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.TvPlayAdapter;
import tv.ismar.homepage.control.FetchDataControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 电视剧模版
 */

public class TemplateTvPlay extends Template implements BaseControl.ControlCallBack,
        RecyclerViewTV.PagingableListener, LinearLayoutManagerTV.FocusSearchFailedListener,
        RecyclerViewTV.OnItemFocusChangeListener {
    private int mSelectItemPosition = 1;//标题--选中海报位置
    private FetchDataControl mFetchDataControl = null;//抓网络数据类
    private TextView mTitleTv;//banner标题
    private RecyclerViewTV mRecycleView;
    private TvPlayAdapter mAdapter;
    private LinearLayoutManagerTV mTvPlayerLayoutManager = null;

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
    }

    @Override
    protected void initListener(View view) {
        mRecycleView.setPagingableListener(this);
        mRecycleView.setOnItemFocusChangeListener(this);
        mTvPlayerLayoutManager.setFocusSearchFailedListener(this);
    }

    private int mBannerPk;
    @Override
    public void initData(Bundle bundle) {
        mTitleTv.setText(bundle.getString("title"));
        mBannerPk = bundle.getInt("banner");
        mFetchDataControl.fetchBanners(mBannerPk, 1, false);
    }

    private void initTitle(){
        mTitleCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), mSelectItemPosition+"",
                mFetchDataControl.mHomeEntity.count+""));
    }

    private void initRecycle(){
        if(mAdapter == null){
            mAdapter = new TvPlayAdapter(mContext, mFetchDataControl.mPoster);
            mAdapter.setMarginLeftEnable(true);
            mRecycleView.setAdapter(mAdapter);
        }else {
            mAdapter.notifyDataSetChanged();
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
}
