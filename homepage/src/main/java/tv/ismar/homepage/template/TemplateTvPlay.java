package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.GuideAdapter;
import tv.ismar.homepage.adapter.TvPlayAdapter;
import tv.ismar.homepage.control.GuideControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 电视剧模版
 */

public class TemplateTvPlay extends Template implements BaseControl.ControlCallBack{
    private Context mContext;
    private RecyclerView mRecycleView;
    private TvPlayAdapter mAdapter;
    private GuideControl mControl;

    public TemplateTvPlay(Context context) {
        super(context);
        mContext = context;
        mControl = new GuideControl(mContext, this);
    }

    @Override
    public void getView(View view) {
        mRecycleView = (RecyclerView) view.findViewById(R.id.tv_player_recyclerview);
    }

    @Override
    public void initData(Bundle bundle) {
        mControl.fetchBannerList();
    }

    @Override
    public void callBack(int flags, Object... args) {
        if(flags == GuideControl.FETCH_BANNERS_LIST_FLAG){//获取单个banner业务
            if(mAdapter == null){
                HomeEntity homeEntity = (HomeEntity) args[0];
                if(homeEntity != null){
                    mAdapter = new TvPlayAdapter(mContext, homeEntity.poster);
                    mRecycleView.setAdapter(mAdapter);
                }
            }else {
                mAdapter.notifyDataSetChanged();
            }
        } else if(flags == GuideControl.FETCH_M_BANNERS_LIST_FLAG){//获取多个banner业务

        }
    }
}
