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

import java.util.List;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.BannerRecommend;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.OnItemSelectedListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.ConlumnAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.fragment.ChannelFragment;

import static tv.ismar.homepage.control.FetchDataControl.FETCH_HOME_RECOMMEND_LIST_FLAG;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 栏目模版
 */

public class TemplateConlumn extends Template implements BaseControl.ControlCallBack,
        RecyclerViewTV.PagingableListener, LinearLayoutManagerTV.FocusSearchFailedListener ,
        OnItemSelectedListener{
    private TextView mTitleTv;//banner标题
    private TextView mIndexTv;//选中位置
    private RecyclerViewTV mRecyclerView;
    private LinearLayoutManagerTV mConlumnLayoutManager;
    private ConlumnAdapter mAdapter;
    private FetchDataControl mFetchDataControl = null;

    public TemplateConlumn(Context context) {
        super(context);
        mFetchDataControl = new FetchDataControl(context, this);
    }

    @Override
    public void getView(View view) {
        mTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
        mTitleCountTv = (TextView) view.findViewById(R.id.banner_title_count);
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.conlumn_recyclerview);
        mConlumnLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mConlumnLayoutManager);
        mRecyclerView.setSelectedItemAtCentered(false);
        int selectedItemOffset = mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
        mRecyclerView.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);
    }

    private int mBannerPk;
    @Override
    public void initData(Bundle bundle) {
        mBannerPk = bundle.getInt(ChannelFragment.BANNER_KEY);
        mTitleTv.setText(bundle.getString(ChannelFragment.TITLE_KEY));
        mFetchDataControl.fetchBanners(mBannerPk, 1, false);
    }

    @Override
    protected void initListener(View view) {
        mRecyclerView.setPagingableListener(this);
        mConlumnLayoutManager.setFocusSearchFailedListener(this);
    }

    @Override
    public void callBack(int flags, Object... args) {
        if(flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG){//获取单个banner业务
            initRecycleView(mFetchDataControl.mPoster);
        }
    }

    private void initRecycleView(List<BannerPoster> posters){
        if(mAdapter == null){
            mAdapter = new ConlumnAdapter(mContext, posters);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemSelectedListener(this);
        }else {
            mAdapter.notifyDataSetChanged();
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
            if (mRecyclerView.getChildAt(0).findViewById(R.id.conlumn_ismartv_linear_layout) == focused ||
                    mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1).findViewById(R.id.conlumn_ismartv_linear_layout) == focused){
                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
            }
            return focused;
        }
        return null;
    }

    @Override
    public void itemSelected(View view, int position) {
        mFetchDataControl.go2Detail(mFetchDataControl.mHomeEntity.posters.get(position));
    }
}
