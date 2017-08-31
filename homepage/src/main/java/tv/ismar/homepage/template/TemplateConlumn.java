package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.ConlumnAdapter;
import tv.ismar.homepage.banner.adapter.BannerSubscribeAdapter;
import tv.ismar.homepage.control.GuideControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 栏目模版
 */

public class TemplateConlumn extends Template implements BaseControl.ControlCallBack {
    private RecyclerViewTV mRecyclerView;
    private ConlumnAdapter mAdapter;
    private GuideControl mControl;

    public TemplateConlumn(Context context) {
        super(context);
        mControl = new GuideControl(mContext, this);
    }

    @Override
    public void getView(View view) {
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.conlumn_recyclerview);
        mRecyclerView.addItemDecoration(new BannerSubscribeAdapter.SpacesItemDecoration(20));
        LinearLayoutManagerTV subscribeLayoutManager = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(subscribeLayoutManager);
        mRecyclerView.setSelectedItemOffset(10, 10);
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
//        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void initData(Bundle bundle) {
        mControl.fetchBanners(bundle.getString("banner"), 1);
    }

    @Override
    public void callBack(int flags, Object... args) {
        if(flags == GuideControl.FETCH_BANNERS_LIST_FLAG){//获取单个banner业务
            if(mAdapter == null){
                HomeEntity homeEntity = (HomeEntity) args[0];
                mAdapter = new ConlumnAdapter(mContext, homeEntity);
                mRecyclerView.setAdapter(mAdapter);
            }else {
                mAdapter.notifyDataSetChanged();
            }
        } else if(flags == GuideControl.FETCH_M_BANNERS_LIST_FLAG){//获取多个banner业务

        }
    }
}
