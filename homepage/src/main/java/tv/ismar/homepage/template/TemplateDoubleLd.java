package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.BigImage;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.DoubleLdAdapter;
import tv.ismar.homepage.adapter.DoubleMdAdapter;
import tv.ismar.homepage.control.DoubleLdControl;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.control.GuideControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 横版双行模版
 */

public class TemplateDoubleLd extends Template implements BaseControl.ControlCallBack{
    private ImageView mVerticalImg;//大图海报
    private ImageView mLtImage;//左上角图标
    private ImageView mRbImage;//右下角图标
    private TextView mTitleTv;//大图标题
    private RecyclerViewTV mRecyclerView1;
    private RecyclerViewTV mRecyclerView2;
    private DoubleLdAdapter mAdapter1;
    private DoubleLdAdapter mAdapter2;
    private DoubleLdControl mControl;

    private List<BannerPoster> mAdapter1Data = new ArrayList<>();
    private List<BannerPoster> mAdapter2Data = new ArrayList<>();

    public TemplateDoubleLd(Context context) {
        super(context);
        mControl = new DoubleLdControl(mContext, this);
    }

    @Override
    public void getView(View view) {
        mRecyclerView1 = (RecyclerViewTV) view.findViewById(R.id.double_ld_recyclerview1);
        mRecyclerView2 = (RecyclerViewTV) view.findViewById(R.id.double_ld_recyclerview2);
        mVerticalImg = (ImageView) view.findViewById(R.id.double_ld_image_poster);
        mLtImage = (ImageView) view.findViewById(R.id.double_ld_image_lt_icon);
        mRbImage = (ImageView) view.findViewById(R.id.double_ld_image_rb_icon);
        mTitleTv = (TextView) view.findViewById(R.id.double_ld_image_title);

        LinearLayoutManagerTV doubleLayoutManager1 = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView1.setLayoutManager(doubleLayoutManager1);
        mRecyclerView1.setSelectedItemOffset(10, 10);

        LinearLayoutManagerTV doubleLayoutManager2 = new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView2.setLayoutManager(doubleLayoutManager2);
        mRecyclerView2.setSelectedItemOffset(10, 10);
    }

    @Override
    protected void initListener(View view) {
        super.initListener(view);
    }

    @Override
    public void initData(Bundle bundle) {
        mControl.getBanners(bundle.getString("banner"), 1);
    }

    /*分离adapter数据*/
    private void separateData(List<BannerPoster> posters){
        mAdapter1Data.clear();
        mAdapter2Data.clear();
        for(int i=0; i<posters.size(); i++){
            if((i%2) == 0){
                mAdapter1Data.add(posters.get(i));
            } else {
                mAdapter2Data.add(posters.get(i));
            }
        }
    }

    private void initAdapter(){
        if(mAdapter1 == null){
            mAdapter1 = new DoubleLdAdapter(mContext, mAdapter1Data);
            mAdapter1.setLeftMarginEnable(true);
            mRecyclerView1.setAdapter(mAdapter1);
        }else {
            mAdapter1.notifyDataSetChanged();
        }
        if(mAdapter2 == null){
            mAdapter2 = new DoubleLdAdapter(mContext, mAdapter2Data);
            mAdapter2.setLeftMarginEnable(true);
            mAdapter2.setTopMarginEnable(true);
            mRecyclerView2.setAdapter(mAdapter2);
        }else {
            mAdapter2.notifyDataSetChanged();
        }
    }

    private void initImage(BigImage data){
        if(data != null){
            Picasso.with(mContext).load(data.poster_url).into(mVerticalImg);
//        Picasso.with(mContext).load(data.poster_url).into(mLtImage);
//        Picasso.with(mContext).load(data.poster_url).into(mRbImage);
            mTitleTv.setText(data.title);
        }
    }

    @Override
    public void callBack(int flags, Object... args) {
        if(flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG){//获取单个banner业务
            HomeEntity homeEntity = (HomeEntity) args[0];
            separateData(homeEntity.poster);
            initAdapter();
            initImage(homeEntity.big_image);
        } else if(flags == FetchDataControl.FETCH_M_BANNERS_LIST_FLAG){//获取多个banner业务

        }
    }
}
