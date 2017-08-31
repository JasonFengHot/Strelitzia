package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.DoubleMdAdapter;
import tv.ismar.homepage.adapter.TvPlayAdapter;
import tv.ismar.homepage.control.GuideControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 竖版双行模版
 */

public class TemplateDoubleMd extends Template implements BaseControl.ControlCallBack{
    private ImageView mVerticalImg;
    private DoubleMdAdapter mAdapter;
    private GuideControl mControl;
    private GridView mGridView;

    public TemplateDoubleMd(Context context) {
        super(context);
        mControl = new GuideControl(mContext, this);
    }

    @Override
    public void getView(View view) {
        mVerticalImg = (ImageView) view.findViewById(R.id.double_md_vertical_img);
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
                    mAdapter = new DoubleMdAdapter(mContext, homeEntity.poster);
                    mGridView.setAdapter(mAdapter);
                }
            }else {
                mAdapter.notifyDataSetChanged();
            }
        } else if(flags == GuideControl.FETCH_M_BANNERS_LIST_FLAG){//获取多个banner业务

        }
    }
}
