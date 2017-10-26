package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
	/*add by dragontec for bug 4077 start*/
import android.os.Handler;
	/*add by dragontec for bug 4077 end*/
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.open.androidtvwidget.leanback.recycle.StaggeredGridLayoutManagerTV;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tv.ismar.app.BaseControl;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.BigImage;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.DoubleMdAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.view.BannerLinearLayout;
	/*add by dragontec for bug 4077 start*/
import tv.ismar.homepage.widget.RecycleLinearLayout;
	/*add by dragontec for bug 4077 end*/

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static tv.ismar.homepage.fragment.ChannelFragment.BANNER_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.CHANNEL_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.NAME_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.TITLE_KEY;

/** @AUTHOR: xi @DATE: 2017/8/29 @DESC: 竖版双行模版 */
public class TemplateDoubleMd extends Template
    implements BaseControl.ControlCallBack,
        OnItemClickListener,
        RecyclerViewTV.PagingableListener,
        RecyclerViewTV.OnItemFocusChangeListener,
        StaggeredGridLayoutManagerTV.FocusSearchFailedListener,
        View.OnClickListener,
        View.OnHoverListener {
  private TextView mTitleTv; // banner标题
  private ImageView mVerticalImg; // 大图海报
  private ImageView mLtImage; // 左上角图标
  private ImageView mRbImage; // 右下角图标
  private TextView mImgeTitleTv; // 大图标题
  private RecyclerViewTV mRecyclerView;
  private DoubleMdAdapter mAdapter;
  private FetchDataControl mFetchDataControl = null;
  private int mSelectItemPosition = 1; // 标题--选中海报位置
  private BannerLinearLayout mBannerLinearLayout;
  private View navigationLeft;
  private View navigationRight;
  private View mHeadView; // recylview头view
  private StaggeredGridLayoutManagerTV mDoubleLayoutManager;
  private String mBannerPk; // banner标记
  private String mName; // 频道名称（中文）
  private String mChannel; // 频道名称（英文）

  private static final int NAVIGATION_LEFT = 0x0001;
  private static final int NAVIGATION_RIGHT = 0x0002;

  private NavigationtHandler mNavigationtHandler;

  private class NavigationtHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what){
        case NAVIGATION_LEFT:
          if (mRecyclerView!=null&&!mRecyclerView.cannotScrollBackward(-10)) {
            navigationLeft.setVisibility(VISIBLE);
          }else if (mRecyclerView!=null){
            navigationLeft.setVisibility(INVISIBLE);
          }
          break;
        case NAVIGATION_RIGHT:
          if(mRecyclerView!=null&&!mRecyclerView.cannotScrollForward(10)){
            navigationRight.setVisibility(VISIBLE);
          }else if (mRecyclerView!=null){
            navigationRight.setVisibility(INVISIBLE);
          }
          break;
      }
    }
  }

  public TemplateDoubleMd(Context context) {
    super(context);
    mFetchDataControl = new FetchDataControl(context, this);
    mNavigationtHandler = new NavigationtHandler();
  }

  @Override
  public void onCreate() {}

  @Override
  public void onStart() {

  }

  @Override
  public void onResume() {}

  @Override
  public void onPause() {
    if (mFetchDataControl != null){
      mFetchDataControl.stop();
    }
	/*add by dragontec for bug 4077 start*/
	  super.onPause();
	/*add by dragontec for bug 4077 end*/
  }

  @Override
  public void onStop() {
    if (mNavigationtHandler.hasMessages(NAVIGATION_LEFT)){
      mNavigationtHandler.removeMessages(NAVIGATION_LEFT);
    }
    if (mNavigationtHandler.hasMessages(NAVIGATION_RIGHT)){
      mNavigationtHandler.removeMessages(NAVIGATION_RIGHT);
    }
  }

  @Override
  public void onDestroy() {
    if (mNavigationtHandler != null){
      mNavigationtHandler = null;
    }
  }

  @Override
  public void getView(View view) {
    mTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
    mTitleCountTv = (TextView) view.findViewById(R.id.banner_title_count);
    mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.double_md_recyclerview);
	/*modify by dragontec for bug 4221 start*/
    mRecyclerView.setTag("recycleView");
	/*modify by dragontec for bug 4221 end*/
    mHeadView = LayoutInflater.from(mContext).inflate(R.layout.banner_double_md_head, null);
    mVerticalImg = (ImageView) mHeadView.findViewById(R.id.double_md_image_poster);
    mLtImage = (ImageView) mHeadView.findViewById(R.id.double_md_image_lt_icon);
    mRbImage = (ImageView) mHeadView.findViewById(R.id.double_md_image_rb_icon);
    mImgeTitleTv = (TextView) mHeadView.findViewById(R.id.double_md_image_title);
    mDoubleLayoutManager =
        new StaggeredGridLayoutManagerTV(2, StaggeredGridLayoutManager.HORIZONTAL);
    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    mRecyclerView.setLayoutManager(mDoubleLayoutManager);
    mRecyclerView.setSelectedItemAtCentered(false);
    mRecyclerView.setHasHeaderView(true);
    int selectedItemOffset =
        mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
    mRecyclerView.setSelectedItemOffset(100, 100);
    mRecyclerView.setAdapter(new DoubleMdAdapter(mContext, new ArrayList<BannerPoster>()));
    navigationLeft = view.findViewById(R.id.navigation_left);
    navigationRight = view.findViewById(R.id.navigation_right);
    mBannerLinearLayout = (BannerLinearLayout) view.findViewById(R.id.banner_layout);
    mBannerLinearLayout.setNavigationLeft(navigationLeft);
    mBannerLinearLayout.setNavigationRight(navigationRight);
    mBannerLinearLayout.setRecyclerViewTV(mRecyclerView);
  }

  @Override
  protected void initListener(View view) {
    super.initListener(view);
    navigationLeft.setOnClickListener(this);
    navigationRight.setOnClickListener(this);
    navigationRight.setOnHoverListener(this);
    navigationLeft.setOnHoverListener(this);
    mRecyclerView.setPagingableListener(this);
    mRecyclerView.setOnItemFocusChangeListener(this);
    mDoubleLayoutManager.setFocusSearchFailedListener(this);
  }

  @Override
  public void initData(Bundle bundle) {
    mTitleTv.setText(bundle.getString(TITLE_KEY));
    mBannerPk = bundle.getString(BANNER_KEY);
    mName = bundle.getString(NAME_KEY);
    mChannel = bundle.getString(CHANNEL_KEY);
    mTitleCountTv.setText("00/00");
/*modify by dragontec for bug 4200 start*/
  }

	@Override
	public void fetchData() {
		hasAppeared = true;
		mFetchDataControl.fetchBanners(mBannerPk, 1, false);
	}
/*modify by dragontec for bug 4200 end*/

  private void initTitle() {
    if (mSelectItemPosition > mFetchDataControl.mHomeEntity.count)
      mSelectItemPosition = mFetchDataControl.mHomeEntity.count;
    mTitleCountTv.setText(
        String.format(
            mContext.getString(R.string.home_item_title_count),
            mSelectItemPosition + "",
            mFetchDataControl.mHomeEntity.count + ""));
  }

  private void initRecycleView() {
    if (mAdapter == null) {
      mAdapter = new DoubleMdAdapter(mContext, mFetchDataControl.mPoster);
      mAdapter.setOnItemClickListener(this);
      mAdapter.setHeaderView(mHeadView);
      mRecyclerView.setAdapter(mAdapter);
	/*add by dragontec for bug 4077 start*/
		checkFocus(mRecyclerView);
	/*add by dragontec for bug 4077 end*/
    } else {
      Log.i(
          TAG,
          "size:"
              + mFetchDataControl.mHomeEntity.posters.size()
              + " url:"
              + mFetchDataControl.mHomeEntity.posters.get(
                      mFetchDataControl.mHomeEntity.posters.size() - 1)
                  .poster_url);
      int start = mFetchDataControl.mPoster.size() - mFetchDataControl.mHomeEntity.posters.size();
      int end = mFetchDataControl.mPoster.size();
      mAdapter.notifyItemRangeChanged(start, end);
    }
  }

  private void initImage() {
    BigImage data = mFetchDataControl.mHomeEntity.bg_image;
    if (data != null) {
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
    if (flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG) { // 获取单个banner业务
      initTitle();
      initRecycleView();
      initImage();
    }
  }

  @Override
  public void onItemFocusGain(View itemView, int position) {
    mSelectItemPosition = position + 1;
    initTitle();
  }

  @Override
  public void onItemClick(View view, int position) {
    if (position == 0) { // 第一张大图
      mFetchDataControl.go2Detail(mFetchDataControl.mHomeEntity.bg_image);
    } else if (mFetchDataControl.mHomeEntity.is_more && position == mAdapter.getItemCount() - 1) {
      new PageIntent()
          .toListPage(
              mContext,
              mFetchDataControl.mHomeEntity.channel_title,
              mFetchDataControl.mHomeEntity.channel,
              mFetchDataControl.mHomeEntity.style,
                  mFetchDataControl.mHomeEntity.section_slug);
    } else {
      mFetchDataControl.go2Detail(mAdapter.getmData().get(position - 1));
    }
  }

  @Override
  public View onFocusSearchFailed(
      View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
    if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
      if (mRecyclerView.getChildAt(0).findViewById(R.id.double_md_ismartv_linear_layout) == focused
          || mRecyclerView
                  .getChildAt(mRecyclerView.getChildCount() - 1)
                  .findViewById(R.id.double_md_ismartv_linear_layout)
              == focused) {
        YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
      }
      return focused;
    }
    /*modify by dragontec for bug 4221 start*/
    return findNextUpDownFocus(focusDirection, mBannerLinearLayout);
    /*modify by dragontec for bug 4221 end*/
  }

  @Override
  public void onLoadMoreItems() {
    Log.i(TAG, "onLoadMoreItems");
    HomeEntity homeEntity = mFetchDataControl.mHomeEntity;
    if (homeEntity != null) {
      if (homeEntity.page < homeEntity.num_pages) {
        mRecyclerView.setOnLoadMoreComplete();
        mFetchDataControl.fetchBanners(mBannerPk, ++homeEntity.page, true);
      }
    }
  }

  @Override
  public void onClick(View v) {
    int i = v.getId();
    int[] positions = new int[] {0, 0};
    Log.i("onClick", "positions[0]:" + positions[0] + "positions[1]:" + positions[1]);
    if (i == R.id.navigation_left) {
      mDoubleLayoutManager.findFirstCompletelyVisibleItemPositions(positions);
      mDoubleLayoutManager.setCanScroll(true);
      if (positions[1] - 1 >= 0) { // 向左滑动
        int targetPosition = positions[1] - 12;
        if (targetPosition <= 0) targetPosition = 0;
        mSelectItemPosition = targetPosition;
        mDoubleLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
        if (targetPosition == 0){
          mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_LEFT,500);
        }
      }
    } else if (i == R.id.navigation_right) { // 向右滑动
      mDoubleLayoutManager.findLastCompletelyVisibleItemPositions(positions);
      mDoubleLayoutManager.setCanScroll(true);
      mRecyclerView.loadMore();
      if (positions[1] <= mFetchDataControl.mHomeEntity.count) {
        int targetPosition = positions[1] + 12;
        if (targetPosition >= mFetchDataControl.mHomeEntity.count) {
          targetPosition = mFetchDataControl.mHomeEntity.count;
        }
        mSelectItemPosition = targetPosition;
        mDoubleLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
        if (mNavigationtHandler.hasMessages(NAVIGATION_RIGHT)){
          mNavigationtHandler.removeMessages(NAVIGATION_RIGHT);
        }
        mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_RIGHT, 500);
        if (targetPosition == mFetchDataControl.mHomeEntity.count)
          YoYo.with(Techniques.HorizontalShake)
              .duration(1000)
              .playOn(
                  mRecyclerView
                      .getChildAt(mRecyclerView.getChildCount() - 1)
                      .findViewById(R.id.double_md_ismartv_linear_layout));
      }
      initTitle();
    }
  }

  @Override
  public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
		/*delete by dragontec for bug 4169 start*/
    	//case MotionEvent.ACTION_HOVER_MOVE:
		/*delete by dragontec for bug 4169 end*/
      case MotionEvent.ACTION_HOVER_ENTER:
        if (!v.hasFocus()) {
          v.requestFocus();
          v.requestFocusFromTouch();
        }
        break;
      case MotionEvent.ACTION_HOVER_EXIT:
        if (event.getButtonState() != BUTTON_PRIMARY) {
          navigationLeft.setVisibility(View.INVISIBLE);
          navigationRight.setVisibility(View.INVISIBLE);
/*modify by dragontec for bug 4057 start*/
//          HomeActivity.mHoverView.requestFocus(); // 将焦点放置到一块隐藏view中
          v.clearFocus();
/*modify by dragontec for bug 4057 end*/
        }
        break;
    }
    return false;
  }
}
