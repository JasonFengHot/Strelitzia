package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.squareup.picasso.Picasso;
import tv.ismar.adapter.FocusGridLayoutManager;
import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.BigImage;
import tv.ismar.app.entity.banner.HomeEntity;
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
        RecyclerViewTV.OnItemClickListener{
    private TextView mHeadTitleTv;
    private TextView mHeadCountTv;
    private ImageView mVerticalImg;//大图海报
    private ImageView mLtImage;//左上角图标
    private ImageView mRbImage;//右下角图标
    private TextView mTitleTv;//大图标题
    private RecyclerViewTV mRecyclerView;
    private DoubleMdAdapter mAdapter;
    private DoubleMdControl mControl;

    public TemplateDoubleMd(Context context) {
        super(context);
        mControl = new DoubleMdControl(mContext, this);
    }

    @Override
    public void getView(View view) {
        mHeadTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
        mHeadCountTv = (TextView) view.findViewById(R.id.banner_title_count);
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.double_md_recyclerview);
        mVerticalImg = (ImageView) view.findViewById(R.id.double_md_image_poster);
        mLtImage = (ImageView) view.findViewById(R.id.double_md_image_lt_icon);
        mRbImage = (ImageView) view.findViewById(R.id.double_md_image_rb_icon);
        mTitleTv = (TextView) view.findViewById(R.id.double_md_image_title);

        GridLayoutManager doubleLayoutManager = new GridLayoutManager(mContext, 2);
        doubleLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(doubleLayoutManager);
    }

    @Override
    protected void initListener(View view) {
        super.initListener(view);
        mRecyclerView.setOnItemClickListener(this);
    }

    @Override
    public void initData(Bundle bundle) {
        mHeadTitleTv.setText(bundle.getString("title"));
        mHeadCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), 1+"", 40+""));
        mControl.getBanners(bundle.getString("banner"), 1);
    }

    private void initAdapter(HomeEntity homeEntity){
        if(mAdapter == null){
            mAdapter = new DoubleMdAdapter(mContext, homeEntity.poster);
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
            initAdapter(homeEntity);
            initImage(homeEntity.big_image);
        } else if(flags == FetchDataControl.FETCH_M_BANNERS_LIST_FLAG){//获取多个banner业务

        }
    }

    @Override
    public void onItemClick(RecyclerViewTV parent, View itemView, int position) {
        if(position < 3){
            mVerticalImg.setVisibility(View.VISIBLE);
        } else if(position >= 3){
            mVerticalImg.setVisibility(View.GONE);
        }
    }
}
