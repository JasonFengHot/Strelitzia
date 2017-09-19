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

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.BannerRecommend;
import tv.ismar.homepage.OnItemSelectedListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.RecommendAdapter;
import tv.ismar.homepage.control.FetchDataControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/15
 * @DESC: 推荐模版
 */

public class TemplateRecommend extends Template implements BaseControl.ControlCallBack,
        RecyclerViewTV.PagingableListener, LinearLayoutManagerTV.FocusSearchFailedListener,
        OnItemSelectedListener{
    private RecyclerViewTV mRecyclerView;
    private LinearLayoutManagerTV mRecommendLayoutManager;
    private RecommendAdapter mAdapter;
    private FetchDataControl mFetchDataControl = null;

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
    }

    @Override
    public void initData(Bundle bundle) {
        mFetchDataControl.fetchHomeRecommend(false);
    }

    @Override
    protected void initListener(View view) {
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
            mAdapter.setOnItemSelectedListener(this);
        }else {
            mAdapter.notifyDataSetChanged();
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
    public void itemSelected(View view, int position) {
        mFetchDataControl.go2Detail(mFetchDataControl.mHomeEntity.posters.get(position));
    }
}
