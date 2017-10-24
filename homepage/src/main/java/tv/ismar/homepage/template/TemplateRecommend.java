package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
	/*add by dragontec for bug 4077 start*/
import android.os.Handler;
	/*add by dragontec for bug 4077 end*/
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import java.util.List;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.BannerRecommend;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.RecommendAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.view.BannerLinearLayout;
	/*add by dragontec for bug 4077 start*/
import tv.ismar.homepage.widget.RecycleLinearLayout;
	/*add by dragontec for bug 4077 end*/

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static tv.ismar.homepage.fragment.ChannelFragment.BANNER_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.CHANNEL_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.NAME_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.TITLE_KEY;

/** @AUTHOR: xi @DATE: 2017/9/15 @DESC: 推荐模版 */
public class TemplateRecommend extends Template
    implements BaseControl.ControlCallBack,
        RecyclerViewTV.PagingableListener,
        LinearLayoutManagerTV.FocusSearchFailedListener,
        OnItemClickListener,
        View.OnHoverListener,
        View.OnClickListener {
  private RecyclerViewTV mRecyclerView;
  private LinearLayoutManagerTV mRecommendLayoutManager;
  private RecommendAdapter mAdapter;
  private FetchDataControl mFetchDataControl = null;
  private BannerLinearLayout mBannerLinearLayout;
  private View navigationLeft;
  private View navigationRight;
    private String mBannerPk; // banner标记
    private String mName; // 频道名称（中文）
    private String mChannel; // 频道名称（英文）

  public TemplateRecommend(Context context) {
    super(context);
    mFetchDataControl = new FetchDataControl(context, this);
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
  public void onStop() {}

  @Override
  public void onDestroy() {}

  @Override
  public void getView(View view) {
    mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.recommend_recyclerview);
	/*modify by dragontec for bug 4221 start*/
    mRecyclerView.setTag("recycleView");
	/*modify by dragontec for bug 4221 start*/
    mRecommendLayoutManager =
        new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
    mRecyclerView.setLayoutManager(mRecommendLayoutManager);
    mRecyclerView.setSelectedItemAtCentered(false);
    int selectedItemOffset =
        mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
    mRecyclerView.setSelectedItemOffset(selectedItemOffset, selectedItemOffset);
    navigationLeft = view.findViewById(R.id.navigation_left);
    navigationRight = view.findViewById(R.id.navigation_right);
    mBannerLinearLayout = (BannerLinearLayout) view.findViewById(R.id.banner_layout);
    mBannerLinearLayout.setNavigationLeft(navigationLeft);
    mBannerLinearLayout.setNavigationRight(navigationRight);
    mBannerLinearLayout.setRecyclerViewTV(mRecyclerView);
  }

  @Override
  public void initData(Bundle bundle) {
      mBannerPk = bundle.getString(BANNER_KEY);
      mName = bundle.getString(NAME_KEY);
      mChannel = bundle.getString(CHANNEL_KEY);
/*modify by dragontec for bug 4200 start*/
  }

	@Override
	public void fetchData() {
		hasAppeared = true;
		mFetchDataControl.fetchBanners(mBannerPk, 1, false);
//		mFetchDataControl.fetchHomeRecommend(false);
	}
/*modify by dragontec for bug 4200 end*/

  @Override
  protected void initListener(View view) {
    navigationLeft.setOnClickListener(this);
    navigationRight.setOnClickListener(this);
    navigationRight.setOnHoverListener(this);
    navigationLeft.setOnHoverListener(this);
    mRecyclerView.setPagingableListener(this);
    mRecommendLayoutManager.setFocusSearchFailedListener(this);
  }

  @Override
  public void callBack(int flags, Object... args) {
    if (flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG) { // 获取推荐列表
      HomeEntity homeEntity = (HomeEntity) args[0];
      initRecycleView(homeEntity.posters);
    }
  }

  private void initRecycleView(List<BannerPoster> recommends) {
    if (mAdapter == null) {
      mAdapter = new RecommendAdapter(mContext, recommends);
      mRecyclerView.setAdapter(mAdapter);
      mAdapter.setOnItemClickListener(this);
	/*add by dragontec for bug 4077 start*/
		checkFocus(mRecyclerView);
	/*add by dragontec for bug 4077 end*/
    } else {
      int start = mFetchDataControl.mPoster.size() - mFetchDataControl.mHomeEntity.posters.size();
      int end = mFetchDataControl.mPoster.size();
      mAdapter.notifyItemRangeChanged(start, end);
    }
  }

  @Override
  public View onFocusSearchFailed(
      View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
    if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
      if (mRecyclerView.getChildAt(0).findViewById(R.id.conlumn_ismartv_linear_layout) == focused
          || mRecyclerView
                  .getChildAt(mRecyclerView.getChildCount() - 1)
                  .findViewById(R.id.conlumn_ismartv_linear_layout)
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
  public void onLoadMoreItems() {}

  @Override
  public void onItemClick(View view, int position) {
    if(mFetchDataControl.mPoster!=null) {
      BannerPoster bannerRecommend = mFetchDataControl.mPoster.get(position);
      if (bannerRecommend != null) {
        mFetchDataControl.go2Detail(bannerRecommend.pk,bannerRecommend.model_name,bannerRecommend.content_model,bannerRecommend.content_url,bannerRecommend.title,null,null,null);
      }
    }
  }

  @Override
  public void onClick(View v) {
    int i = v.getId();
    if (i == R.id.navigation_left) {
      mRecommendLayoutManager.setCanScroll(true);
      if (mRecommendLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) { // 向左滑动
        int targetPosition = mRecommendLayoutManager.findFirstCompletelyVisibleItemPosition() - 4;
        if (targetPosition <= 0) targetPosition = 0;
        mRecommendLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
      }
    } else if (i == R.id.navigation_right) { // 向右滑动
      mRecommendLayoutManager.setCanScroll(true);
      mRecyclerView.loadMore();
      if (mRecommendLayoutManager.findLastCompletelyVisibleItemPosition()
          <= mFetchDataControl.mHomeEntity.count) {
        int targetPosition = mRecommendLayoutManager.findLastCompletelyVisibleItemPosition() + 4;
        if (targetPosition >= mFetchDataControl.mHomeEntity.count) {
          targetPosition = mFetchDataControl.mHomeEntity.count;
        }
        mRecommendLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
        if (targetPosition == mFetchDataControl.mHomeEntity.count)
          YoYo.with(Techniques.HorizontalShake)
              .duration(1000)
              .playOn(
                  mRecyclerView
                      .getChildAt(mRecyclerView.getChildCount() - 1)
                      .findViewById(R.id.tv_player_ismartv_linear_layout));
      }
    }
  }

  @Override
  public boolean onHover(View v, MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_HOVER_MOVE:
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
