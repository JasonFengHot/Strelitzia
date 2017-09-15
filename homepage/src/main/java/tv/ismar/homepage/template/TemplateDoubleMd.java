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
import tv.ismar.homepage.adapter.DoubleMdAdapter;
import tv.ismar.homepage.control.DoubleMdControl;
import tv.ismar.homepage.control.FetchDataControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 竖版双行模版
 */

public class TemplateDoubleMd extends Template implements BaseControl.ControlCallBack,
        OnItemSelectedListener,
        RecyclerViewTV.OnItemFocusChangeListener {
    private TextView mTitleTv;//banner标题
    private TextView mIndexTv;//选中位置
    private ImageView mVerticalImg;//大图海报
    private ImageView mLtImage;//左上角图标
    private ImageView mRbImage;//右下角图标
    private TextView mImgeTitleTv;//大图标题
    private RecyclerViewTV mRecyclerView;
    private DoubleMdAdapter mAdapter;
    private FetchDataControl mFetchDataControl = null;

    public TemplateDoubleMd(Context context) {
        super(context);
        mFetchDataControl = new FetchDataControl(context, this);
    }

    private View mHeadView;//recylview头view

    @Override
    public void getView(View view) {
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.double_md_recyclerview);
        mHeadView = LayoutInflater.from(mContext).inflate(R.layout.banner_double_md_head, null);
        mVerticalImg = (ImageView) mHeadView.findViewById(R.id.double_md_image_poster);
        mLtImage = (ImageView) mHeadView.findViewById(R.id.double_md_image_lt_icon);
        mRbImage = (ImageView) mHeadView.findViewById(R.id.double_md_image_rb_icon);
        mImgeTitleTv = (TextView) mHeadView.findViewById(R.id.double_md_image_title);
        StaggeredGridLayoutManager doubleLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(doubleLayoutManager);
    }

    @Override
    protected void initListener(View view) {
        super.initListener(view);
        mRecyclerView.setOnItemFocusChangeListener(this);
    }

    @Override
    public void initData(Bundle bundle) {
        mFetchDataControl.fetchBanners(bundle.getInt("banner"), 1, false);
    }

    private void initAdapter(HomeEntity homeEntity){
        if(mAdapter == null){
            mAdapter = new DoubleMdAdapter(mContext, homeEntity.posters);
            mAdapter.setOnItemSelectedListener(this);
            mAdapter.setLeftMarginEnable(true);
            mAdapter.setHeaderView(mHeadView);
            mRecyclerView.setAdapter(mAdapter);
        }else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private int mItemCount = 0;

    private void initTitle(int position){
        mTitleCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), (1 + position) + "", mItemCount + ""));
    }

    private void initImage(BigImage data){
        if(data != null){
            if (!TextUtils.isEmpty(data.vertical_url)) {
                Picasso.with(mContext).load(data.vertical_url).into(mVerticalImg);
            } else {
                Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(mVerticalImg);
            }
//        Picasso.with(mContext).load(data.poster_url).into(mLtImage);
//        Picasso.with(mContext).load(data.poster_url).into(mRbImage);
            mImgeTitleTv.setText(data.title);
        }
    }

    @Override
    public void callBack(int flags, Object... args) {
        if(flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG){//获取单个banner业务
            HomeEntity homeEntity = (HomeEntity) args[0];
            initAdapter(homeEntity);
            initImage(homeEntity.bg_image);
        } else if(flags == FetchDataControl.FETCH_M_BANNERS_LIST_FLAG){//获取多个banner业务

        }
    }

    @Override
    public void onItemFocusGain(View itemView, int position) {
//        if (itemView != null && mContext != null && mTitleCountTv != null){
//            mTitleCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), (1 + position) + "", mAdapter.getTatalItemCount() + ""));
//        }
    }

    @Override
    public void itemSelected(View view, int position) {
//        if(position < 3){
//            mVerticalImg.setVisibility(View.VISIBLE);
//        } else if(position >= 3){
//            mVerticalImg.setVisibility(View.GONE);
//        }
    }
}
