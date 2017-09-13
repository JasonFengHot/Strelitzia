package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import tv.ismar.homepage.control.TvPlayControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 电视剧模版
 */

public class TemplateTvPlay extends Template implements BaseControl.ControlCallBack{
    private TextView mTitleTv;//banner标题
    private TextView mIndexTv;//选中位置
    private RecyclerViewTV mRecycleView;
    private TvPlayAdapter mAdapter;
    private TvPlayControl mControl;

    public TemplateTvPlay(Context context) {
        super(context);
        mControl = new TvPlayControl(mContext, this);
    }

    private LinearLayoutManager mTvPlayerLayoutManager = null;

    @Override
    public void getView(View view) {
        mTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
        mTitleCountTv =(TextView) view.findViewById(R.id.banner_title_count);
        mRecycleView = (RecyclerViewTV) view.findViewById(R.id.tv_player_recyclerview);
        mTvPlayerLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecycleView.setLayoutManager(mTvPlayerLayoutManager);
        mRecycleView.setSelectedItemAtCentered(false);
        int selectedItemOffset = mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
        mRecycleView.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);
    }

    @Override
    protected void initListener(View view) {
        super.initListener(view);
    }

    @Override
    public void initData(Bundle bundle) {
        mTitleTv.setText(bundle.getString("title"));
        mTitleCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), 1+"", 40+""));
        mControl.getBanners(bundle.getInt("banner"), 1);
    }

    @Override
    public void callBack(int flags, Object... args) {
        if(flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG){//获取单个banner业务
            HomeEntity homeEntity = (HomeEntity) args[0];
            if(mAdapter == null){
                mAdapter = new TvPlayAdapter(mContext, homeEntity.posters);
                mAdapter.setMarginLeftEnable(true);
                mRecycleView.setAdapter(mAdapter);
            }else {
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
