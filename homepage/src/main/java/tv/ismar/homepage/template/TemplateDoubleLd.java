package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.adapter.FocusGridLayoutManager;
import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.BigImage;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.DoubleLdAdapter;
import tv.ismar.homepage.control.DoubleLdControl;
import tv.ismar.homepage.control.FetchDataControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 横版双行模版
 */

public class TemplateDoubleLd extends Template implements BaseControl.ControlCallBack,
        RecyclerViewTV.OnItemClickListener{
    private ImageView mVerticalImg;//大图海报
    private ImageView mLtImage;//左上角图标
    private ImageView mRbImage;//右下角图标
    private TextView mTitleTv;//大图标题
    private RecyclerViewTV mRecyclerView;
    private DoubleLdAdapter mAdapter;
    private DoubleLdControl mControl;

    private List<BannerPoster> mAdapter1Data = new ArrayList<>();
    private List<BannerPoster> mAdapter2Data = new ArrayList<>();

    public TemplateDoubleLd(Context context) {
        super(context);
        mControl = new DoubleLdControl(mContext, this);
    }

    @Override
    public void getView(View view) {
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.double_ld_recyclerview);
        mVerticalImg = (ImageView) view.findViewById(R.id.double_ld_image_poster);
        mLtImage = (ImageView) view.findViewById(R.id.double_ld_image_lt_icon);
        mRbImage = (ImageView) view.findViewById(R.id.double_ld_image_rb_icon);
        mTitleTv = (TextView) view.findViewById(R.id.double_ld_image_title);

        FocusGridLayoutManager doubleLayoutManager = new FocusGridLayoutManager(mContext, 2);
        doubleLayoutManager.setOrientation(FocusGridLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(doubleLayoutManager);
        mRecyclerView.setSelectedItemOffset(10, 10);
    }

    @Override
    protected void initListener(View view) {
        super.initListener(view);
        mRecyclerView.setOnItemClickListener(this);
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
        if(mAdapter == null){
            mAdapter = new DoubleLdAdapter(mContext, mAdapter1Data);
            mAdapter.setLeftMarginEnable(true);
            mRecyclerView.setAdapter(mAdapter);
        }else {
            mAdapter.notifyDataSetChanged();
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

    @Override
    public void onItemClick(RecyclerViewTV parent, View itemView, int position) {
        if(position < 2){
            mVerticalImg.setVisibility(View.VISIBLE);
        } else if(position >= 2){
            mVerticalImg.setVisibility(View.GONE);
        }
    }
}
