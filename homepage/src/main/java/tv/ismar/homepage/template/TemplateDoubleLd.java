package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.squareup.picasso.Picasso;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.BigImage;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.OnItemSelectedListener;
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
        OnItemSelectedListener{
    private TextView mHeadTitleTv;
    private TextView mHeadCountTv;
    private ImageView mVerticalImg;//大图海报
    private ImageView mLtImage;//左上角图标
    private ImageView mRbImage;//右下角图标
    private TextView mTitleTv;//大图标题
    private RecyclerViewTV mRecyclerView;
    private DoubleLdAdapter mAdapter;
    private FetchDataControl mFetchDataControl = null;

    public TemplateDoubleLd(Context context) {
        super(context);
        mFetchDataControl = new FetchDataControl(context, this);
    }

    private View mHeadView;//recylview头view

    @Override
    public void getView(View view) {
        mHeadTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
        mHeadCountTv = (TextView) view.findViewById(R.id.banner_title_count);
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.double_ld_recyclerview);
        mHeadView = LayoutInflater.from(mContext).inflate(R.layout.banner_double_ld_head, null);
        mVerticalImg = (ImageView) mHeadView.findViewById(R.id.double_ld_image_poster);
        mLtImage = (ImageView) mHeadView.findViewById(R.id.double_ld_image_lt_icon);
        mRbImage = (ImageView) mHeadView.findViewById(R.id.double_ld_image_rb_icon);
        mTitleTv = (TextView) mHeadView.findViewById(R.id.double_ld_image_title);
        StaggeredGridLayoutManager doubleLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(doubleLayoutManager);
    }

    @Override
    protected void initListener(View view) {
        super.initListener(view);
    }

    @Override
    public void initData(Bundle bundle) {
        mHeadTitleTv.setText(bundle.getString("title"));
        mHeadCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), 1+"", 40+""));
        mFetchDataControl.fetchBanners(bundle.getInt("banner"), 1, false);
    }

    private void initRecycleView(HomeEntity homeEntity){
        if(mAdapter == null){
            mAdapter = new DoubleLdAdapter(mContext, homeEntity.posters);
            mAdapter.setLeftMarginEnable(true);
            mAdapter.setOnItemSelectedListener(this);
            mAdapter.setHeaderView(mHeadView);
            mRecyclerView.setAdapter(mAdapter);
        }else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void initImage(BigImage data){
        if(data != null){
            if (!TextUtils.isEmpty(data.poster_url)) {
                Picasso.with(mContext).load(data.poster_url).into(mVerticalImg);
            } else {
                Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(mVerticalImg);
            }
//        Picasso.with(mContext).load(data.poster_url).into(mLtImage);
//        Picasso.with(mContext).load(data.poster_url).into(mRbImage);
            mTitleTv.setText(data.title);
        }
    }

    @Override
    public void callBack(int flags, Object... args) {
        if(flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG){//获取单个banner业务
            HomeEntity homeEntity = (HomeEntity) args[0];
            initRecycleView(homeEntity);
            initImage(homeEntity.bg_image);
        }
    }

    @Override
    public void itemSelected(View view, int position) {

    }
}
