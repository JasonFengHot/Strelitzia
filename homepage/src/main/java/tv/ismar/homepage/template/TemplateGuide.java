package tv.ismar.homepage.template;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.ConlumnAdapter;
import tv.ismar.homepage.adapter.GuideAdapter;
import tv.ismar.homepage.control.GuideControl;
import tv.ismar.homepage.widget.DaisyVideoView;
import tv.ismar.homepage.widget.HomeItemContainer;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 导视模版
 */

public class TemplateGuide extends Template implements BaseControl.ControlCallBack{
    private HomeItemContainer mGuideContainer;//导视视频容器
    private DaisyVideoView mVideView;//导视view
    private ImageView mLoadingIg;//加载提示logo
    private TextView mVideTitleTv;//导视标题
    private TextView mFirstIcon;//第一个视频指示数字
    private TextView mSecondIcon;
    private TextView mThirdIcon;
    private TextView mFourIcon;
    private TextView mFiveIcon;
    private RecyclerView mRecycleView;//海报recycleview

    private GuideControl mControl;
    private GuideAdapter mAdapter;

    public TemplateGuide(Context context) {
        super(context);
        mControl = new GuideControl(mContext, this);
    }

    @Override
    public void getView(View view) {
        mGuideContainer = (HomeItemContainer) view.findViewById(R.id.guide_container);
        mVideView = (DaisyVideoView) view.findViewById(R.id.guide_container);
        mLoadingIg = (ImageView) view.findViewById(R.id.guide_container);
        mVideTitleTv = (TextView) view.findViewById(R.id.guide_container);
        mFirstIcon = (TextView) view.findViewById(R.id.guide_container);
        mSecondIcon = (TextView) view.findViewById(R.id.guide_container);
        mThirdIcon = (TextView) view.findViewById(R.id.guide_container);
        mFourIcon = (TextView) view.findViewById(R.id.guide_container);
        mFiveIcon = (TextView) view.findViewById(R.id.guide_container);
        mRecycleView = (RecyclerView) view.findViewById(R.id.guide_recyclerview);
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
                    mAdapter = new GuideAdapter(mContext, homeEntity.poster);
                    mRecycleView.setAdapter(mAdapter);
                }
            }else {
                mAdapter.notifyDataSetChanged();
            }
        } else if(flags == GuideControl.FETCH_M_BANNERS_LIST_FLAG){//获取多个banner业务

        }
    }
}
