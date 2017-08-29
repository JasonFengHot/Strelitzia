package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.ConlumnAdapter;
import tv.ismar.homepage.control.GuideControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 栏目模版
 */

public class TemplateConlumn extends Template implements BaseControl.ControlCallBack {
    private TextView mTitle;
    private RecyclerView mRecyclerView;
    private ConlumnAdapter mAdapter;
    private GuideControl mControl;

    public TemplateConlumn(Context context) {
        super(context);
        mControl = new GuideControl(mContext, this);
    }

    @Override
    public void getView(View view) {
        mTitle = (TextView) view.findViewById(R.id.conlumn_title);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.conlumn_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void initData(Bundle bundle) {
        mTitle.setText(bundle.getString("title"));
        mControl.fetchBanners(bundle.getString("url"), 1);
    }

    @Override
    public void callBack(int flags, Object... args) {
        if(flags == GuideControl.FETCH_BANNERS_LIST_FLAG){
            if(mAdapter == null){
                HomeEntity homeEntity = (HomeEntity) args[0];
                mRecyclerView.setAdapter(new ConlumnAdapter(mContext, homeEntity));
            }
        }
    }
}
