package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.open.androidtvwidget.leanback.recycle.impl.PrvInterface;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.ConlumnAdapter;
import tv.ismar.homepage.control.ConlumnControl;
import tv.ismar.homepage.control.FetchDataControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 栏目模版
 */

public class TemplateConlumn extends Template implements BaseControl.ControlCallBack {
    private TextView mTitleTv;//banner标题
    private TextView mIndexTv;//选中位置
    private RecyclerViewTV mRecyclerView;
    private ConlumnAdapter mAdapter;
    private ConlumnControl mControl;

    public TemplateConlumn(Context context) {
        super(context);
        mControl = new ConlumnControl(mContext, this);
    }

    @Override
    public void getView(View view) {
        mTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
        mTitleCountTv = (TextView) view.findViewById(R.id.banner_title_count);
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.conlumn_recyclerview);
        LinearLayoutManager conlumnLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(conlumnLayoutManager);
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void initData(Bundle bundle) {
        mTitleTv.setText(bundle.getString("title"));
        mControl.getBanners(bundle.getInt("banner"), 1);
    }

    @Override
    public void callBack(int flags, Object... args) {
        if(flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG){//获取单个banner业务
            HomeEntity homeEntity = (HomeEntity) args[0];
            if(homeEntity.posters==null || homeEntity.posters.size()<=0){
                return;
            }
            Log.i(TAG, "posters size:" + homeEntity.posters.size());
            if(mAdapter == null){
                mAdapter = new ConlumnAdapter(mContext, homeEntity.posters);
                mRecyclerView.setAdapter(mAdapter);
            }else {
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
