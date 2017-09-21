package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
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
import tv.ismar.homepage.control.FetchDataControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 横版双行模版
 */

public class TemplateDoubleLd extends Template implements BaseControl.ControlCallBack,
        OnItemSelectedListener, RecyclerViewTV.OnItemFocusChangeListener{
    private int mSelectItemPosition = 1;//标题--选中海报位置
    private TextView mTitleTv;//banner标题;
    private ImageView mVerticalImg;//大图海报
    private ImageView mLtImage;//左上角图标
    private ImageView mRbImage;//右下角图标
    private TextView mIgTitleTv;//大图标题
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
        mTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
        mTitleCountTv = (TextView) view.findViewById(R.id.banner_title_count);
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.double_ld_recyclerview);
        mHeadView = LayoutInflater.from(mContext).inflate(R.layout.banner_double_ld_head, null);
        mVerticalImg = (ImageView) mHeadView.findViewById(R.id.double_ld_image_poster);
        mLtImage = (ImageView) mHeadView.findViewById(R.id.double_ld_image_lt_icon);
        mRbImage = (ImageView) mHeadView.findViewById(R.id.double_ld_image_rb_icon);
        mIgTitleTv = (TextView) mHeadView.findViewById(R.id.double_ld_image_title);
        StaggeredGridLayoutManager doubleLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(doubleLayoutManager);
    }

    private void initTitle(){
        mTitleCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), mSelectItemPosition+"",
                mFetchDataControl.mHomeEntity.count+""));
    }

    @Override
    protected void initListener(View view) {
        super.initListener(view);
        mRecyclerView.setOnItemFocusChangeListener(this);
    }

    @Override
    public void initData(Bundle bundle) {
        mTitleTv.setText(bundle.getString("title"));
        mTitleCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count), 1+"", 40+""));
        mFetchDataControl.fetchBanners(bundle.getInt("banner"), 1, false);
    }

    private void initRecycleView(){
        HomeEntity homeEntity = mFetchDataControl.mHomeEntity;
        if(homeEntity != null){
            if(mAdapter == null){
                mAdapter = new DoubleLdAdapter(mContext, homeEntity.posters);
                mAdapter.setOnItemSelectedListener(this);
                mAdapter.setHeaderView(mHeadView);
                mRecyclerView.setAdapter(mAdapter);
            }else {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void initImage(){
        BigImage data = mFetchDataControl.mHomeEntity.bg_image;
        if(data != null){
            if (!TextUtils.isEmpty(data.poster_url)) {
                Picasso.with(mContext).load(data.poster_url).into(mVerticalImg);
            } else {
                Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(mVerticalImg);
            }
//        Picasso.with(mContext).load(data.poster_url).into(mLtImage);
//        Picasso.with(mContext).load(data.poster_url).into(mRbImage);
            mIgTitleTv.setText(data.title);
        }
    }

    @Override
    public void callBack(int flags, Object... args) {
        if(flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG){//获取单个banner业务
            initTitle();
            initRecycleView();
            initImage();
        }
    }

    @Override
    public void itemSelected(View view, int position) {
        if(position == 0){//第一张大图
            mFetchDataControl.go2Detail(mFetchDataControl.mHomeEntity.bg_image);
        } else {
            mFetchDataControl.go2Detail(mFetchDataControl.mHomeEntity.posters.get(position));
        }
    }

    @Override
    public void onItemFocusGain(View itemView, int position) {
        mSelectItemPosition = position+1;
        initTitle();
    }
}
