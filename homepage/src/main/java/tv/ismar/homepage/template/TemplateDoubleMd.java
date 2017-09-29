package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.open.androidtvwidget.leanback.recycle.StaggeredGridLayoutManagerTV;
import com.squareup.picasso.Picasso;

import tv.ismar.app.BaseControl;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.entity.banner.BigImage;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.DoubleMdAdapter;
import tv.ismar.homepage.control.FetchDataControl;

import static tv.ismar.homepage.fragment.ChannelFragment.BANNER_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.CHANNEL_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.NAME_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.TITLE_KEY;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 竖版双行模版
 */

public class TemplateDoubleMd extends Template implements BaseControl.ControlCallBack,
        OnItemClickListener, RecyclerViewTV.PagingableListener,
        RecyclerViewTV.OnItemFocusChangeListener,
        StaggeredGridLayoutManagerTV.FocusSearchFailedListener {
    private TextView mTitleTv;//banner标题
    private TextView mIndexTv;//选中位置
    private ImageView mVerticalImg;//大图海报
    private ImageView mLtImage;//左上角图标
    private ImageView mRbImage;//右下角图标
    private TextView mImgeTitleTv;//大图标题
    private RecyclerViewTV mRecyclerView;
    private DoubleMdAdapter mAdapter;
    private FetchDataControl mFetchDataControl = null;
    private int mSelectItemPosition = 1;//标题--选中海报位置

    public TemplateDoubleMd(Context context) {
        super(context);
        mFetchDataControl = new FetchDataControl(context, this);
    }

    private View mHeadView;//recylview头view
    private StaggeredGridLayoutManagerTV mDoubleLayoutManager;

    @Override
    public void getView(View view) {
        mTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
        mTitleCountTv = (TextView) view.findViewById(R.id.banner_title_count);
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.double_md_recyclerview);

        mHeadView = LayoutInflater.from(mContext).inflate(R.layout.banner_double_md_head, null);
        mVerticalImg = (ImageView) mHeadView.findViewById(R.id.double_md_image_poster);
        mLtImage = (ImageView) mHeadView.findViewById(R.id.double_md_image_lt_icon);
        mRbImage = (ImageView) mHeadView.findViewById(R.id.double_md_image_rb_icon);
        mImgeTitleTv = (TextView) mHeadView.findViewById(R.id.double_md_image_title);
        mDoubleLayoutManager = new StaggeredGridLayoutManagerTV(2, StaggeredGridLayoutManager.HORIZONTAL);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mDoubleLayoutManager);
    }

    @Override
    protected void initListener(View view) {
        super.initListener(view);
        mRecyclerView.setOnItemFocusChangeListener(this);
        mDoubleLayoutManager.setFocusSearchFailedListener(this);
    }

    private int mBannerPk;//banner标记
    private String mName;//频道名称（中文）
    private String mChannel;//频道名称（英文）
    @Override
    public void initData(Bundle bundle) {
        mTitleTv.setText(bundle.getString(TITLE_KEY));
        mBannerPk = bundle.getInt(BANNER_KEY);
        mName = bundle.getString(NAME_KEY);
        mChannel = bundle.getString(CHANNEL_KEY);
        mFetchDataControl.fetchBanners(mBannerPk, 1, false);
    }

    private void initTitle(){
        mTitleCountTv.setText(String.format(mContext.getString(R.string.home_item_title_count),
                mSelectItemPosition+"",
                mFetchDataControl.mHomeEntity.count+""));
    }

    private void initRecycleView(){
        if(mAdapter == null){
            mAdapter = new DoubleMdAdapter(mContext, mFetchDataControl.mHomeEntity.posters);
            mAdapter.setOnItemClickListener(this);
            mAdapter.setHeaderView(mHeadView);
            mRecyclerView.setAdapter(mAdapter);
        }else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void initImage(){
        BigImage data = mFetchDataControl.mHomeEntity.bg_image;
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
            initTitle();
            initRecycleView();
            initImage();
        }
    }

    @Override
    public void onItemFocusGain(View itemView, int position) {
        mSelectItemPosition = position+1;
        initTitle();
    }

    @Override
    public void onItemClick(View view, int position) {
        if(position == 0){//第一张大图
            mFetchDataControl.go2Detail(mFetchDataControl.mHomeEntity.bg_image);
        } else if(position == mFetchDataControl.mHomeEntity.count-1){
            new PageIntent().toListPage(mContext, mName, mChannel, 0);
        }else {
            mFetchDataControl.go2Detail(mFetchDataControl.mHomeEntity.posters.get(position));
        }
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT){
            if (mRecyclerView.getChildAt(0).findViewById(R.id.double_md_ismartv_linear_layout) == focused ||
                    mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1).findViewById(R.id.double_md_ismartv_linear_layout) == focused){
                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
            }
            return focused;
        }
        return null;
    }

    @Override
    public void onLoadMoreItems() {
        Log.i(TAG, "onLoadMoreItems");
        HomeEntity homeEntity = mFetchDataControl.mHomeEntity;
        if(homeEntity != null){
            if(homeEntity.page < homeEntity.num_pages){
                mRecyclerView.setOnLoadMoreComplete();
                mFetchDataControl.fetchBanners(mBannerPk, ++homeEntity.page, true);
            }
        }
    }
}
