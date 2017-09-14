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
import tv.ismar.homepage.adapter.CenterAdapter;
import tv.ismar.homepage.control.FetchDataControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/5
 * @DESC: 居中模版
 */

public class TemplateCenter extends Template implements BaseControl.ControlCallBack,
        RecyclerViewTV.PagingableListener, LinearLayoutManagerTV.FocusSearchFailedListener {
    public FetchDataControl mFetchDataControl = null;
    private TextView mHeadTitleTv;
    private TextView mHeadCountTv;
    private RecyclerViewTV mRecycleView;//海报recycleview
    private LinearLayoutManagerTV mCenterLayoutManager;
    private CenterAdapter mAdapter;

    public TemplateCenter(Context context) {
        super(context);
        mFetchDataControl = new FetchDataControl(context, this);
    }

    @Override
    public void getView(View view) {
        mHeadTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
        mHeadCountTv = (TextView) view.findViewById(R.id.banner_title_count);
        mRecycleView = (RecyclerViewTV) view.findViewById(R.id.center_recyclerview);
        mCenterLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecycleView.setLayoutManager(mCenterLayoutManager);
        mRecycleView.setSelectedItemAtCentered(false);
        int selectedItemOffset = mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
        mRecycleView.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);
    }

    private int mBannerPk;
    @Override
    public void initData(Bundle bundle) {
        mBannerPk = bundle.getInt("banner");
        mHeadTitleTv.setText(bundle.getString("title"));
        mHeadCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), 1+"", 40+""));
        mFetchDataControl.fetchBanners(mBannerPk, 1, false);
    }

    @Override
    protected void initListener(View view) {
        super.initListener(view);
        mRecycleView.setPagingableListener(this);
        mCenterLayoutManager.setFocusSearchFailedListener(this);
    }

    @Override
    public void callBack(int flags, Object... args) {
        if(flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG){//获取单个banner业务
            HomeEntity homeEntity = (HomeEntity) args[0];
            if(mAdapter == null){
                mAdapter = new CenterAdapter(mContext, homeEntity.posters);
                mRecycleView.setAdapter(mAdapter);
            }else {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoadMoreItems() {
        Log.i(TAG, "onLoadMoreItems");
        HomeEntity homeEntity = mFetchDataControl.mHomeEntity;
        if(homeEntity != null){
            if(homeEntity.page < homeEntity.num_pages){
                mFetchDataControl.fetchBanners(mBannerPk, ++homeEntity.page, true);
            }
        }
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT){
            if (mRecycleView.getChildAt(0).findViewById(R.id.center_ismartv_linear_layout) == focused ||
                    mRecycleView.getChildAt(mRecycleView.getChildCount() - 1).findViewById(R.id.center_ismartv_linear_layout) == focused){
                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
            }
            return focused;
        }
        return null;
    }
}
