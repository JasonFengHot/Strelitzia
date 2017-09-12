package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

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
    private RecyclerViewTV mRecycleView;
    private TvPlayAdapter mAdapter;
    private TvPlayControl mControl;

    public TemplateTvPlay(Context context) {
        super(context);
        mControl = new TvPlayControl(mContext, this);
    }

    @Override
    public void getView(View view) {
        mRecycleView = (RecyclerViewTV) view.findViewById(R.id.tv_player_recyclerview);
        LinearLayoutManagerTV tvPlayerLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecycleView.setLayoutManager(tvPlayerLayoutManager);
        mRecycleView.setSelectedItemOffset(10, 10);
    }

    @Override
    public void initData(Bundle bundle) {
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
