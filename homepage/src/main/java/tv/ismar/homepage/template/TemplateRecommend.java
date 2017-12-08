/*modify by dragontec for bug 4362 start*/
package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
	/*add by dragontec for bug 4077 start*/
import android.os.Handler;
	/*add by dragontec for bug 4077 end*/
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
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
import tv.ismar.homepage.OnItemKeyListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.RecommendAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.fragment.ChannelFragment;
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

/**
 * @AUTHOR: xi @DATE: 2017/9/15 @DESC: 推荐模版
 */
public class TemplateRecommend extends Template
		implements BaseControl.ControlCallBack,
		RecyclerViewTV.PagingableListener,
		LinearLayoutManagerTV.FocusSearchFailedListener,
		OnItemClickListener,
		OnItemKeyListener,
		View.OnHoverListener,
		View.OnClickListener {
	/*delete by dragontec for bug 4332 start*/
//  private RecyclerViewTV mRecyclerView;
/*delete by dragontec for bug 4332 end*/
	private LinearLayoutManagerTV mRecommendLayoutManager;
	private RecommendAdapter mAdapter;
	private BannerLinearLayout mBannerLinearLayout;
	/*delete by dragontec for bug 4332 start*/
//  private View navigationLeft;
//  private View navigationRight;
/*delete by dragontec for bug 4332 end*/
	private String mName; // 频道名称（中文）
	private String mChannel; // 频道名称（英文）
	private int locationY;


	/*modify by dragontec for bug 4334 start*/
	public TemplateRecommend(Context context, int position, FetchDataControl fetchDataControl) {
		super(context, position, fetchDataControl);
	}
  /*modify by dragontec for bug 4334 end*/

	@Override
	public void onCreate() {
	}

	@Override
	public void onStart() {

	}

	@Override
	public void onResume() {
	}

	@Override
	public void onPause() {
	/*add by dragontec for bug 4077 start*/
		super.onPause();
	/*add by dragontec for bug 4077 end*/
	}

	@Override
	public void onStop() {
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void getView(View view) {
		mRecommendLayoutManager =
				new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
		mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.recommend_recyclerview);
	/*modify by dragontec for bug 4221 start*/
		mRecyclerView.setTag("recycleView");
	/*modify by dragontec for bug 4221 start*/
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
/*add by dragontec for bug 4332 start*/
		mHoverView = view.findViewById(R.id.hover_view);
/*add by dragontec for bug 4332 end*/
	}

	@Override
	public void clearView() {
		mHoverView = null;
		if (mBannerLinearLayout != null) {
			mBannerLinearLayout.setRecyclerViewTV(null);
			mBannerLinearLayout.setNavigationRight(null);
			mBannerLinearLayout.setNavigationLeft(null);
			mBannerLinearLayout = null;
		}
		navigationRight = null;
		navigationLeft = null;
		if (mRecyclerView != null) {
			mRecyclerView.setLayoutManager(null);
			mRecyclerView.setAdapter(null);
			mRecyclerView = null;
		}
		mRecommendLayoutManager = null;
	}

	@Override
	public void initData(Bundle bundle) {
		initAdapter();
		mBannerPk = bundle.getString(BANNER_KEY);
		mName = bundle.getString("title");
		mChannel = bundle.getString(CHANNEL_KEY);
		locationY = bundle.getInt(BANNER_LOCATION, 0);
/*modify by dragontec for bug 4334 start*/
		if (mFetchControl.getHomeEntity(mBannerPk) != null) {
			isNeedFillData = true;
			checkViewAppear();
		}
	}

	@Override
	public void unInitData() {
		unInitAdapter();
	}

	@Override
	public void fetchData() {
		hasAppeared = true;
//		mFetchDataControl.fetchHomeRecommend(false);
	}

	@Override
	public void fillData() {
		if (isNeedFillData) {
			isNeedFillData = false;
			initRecycleView();
		}
	}

/*modify by dragontec for bug 4334 end*/

	@Override
	protected void initListener(View view) {
/*add by dragontec for bug 4332 start*/
		super.initListener(view);
/*add by dragontec for bug 4332 end*/
		navigationLeft.setOnClickListener(this);
		navigationRight.setOnClickListener(this);
//    navigationRight.setOnHoverListener(this);
//    navigationLeft.setOnHoverListener(this);
		mRecyclerView.setPagingableListener(this);
		mRecommendLayoutManager.setFocusSearchFailedListener(this);
	}

	@Override
	protected void unInitListener() {
		mRecommendLayoutManager.setFocusSearchFailedListener(null);
		mRecyclerView.setPagingableListener(null);
		navigationRight.setOnClickListener(null);
		navigationLeft.setOnClickListener(null);
		super.unInitListener();
	}

	//  @Override
//  public void callBack(int flags, Object... args) {
//    if (flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG) { // 获取推荐列表
//		/*modify by dragontec for bug 4334 start*/
//		isNeedFillData  = true;
////      HomeEntity homeEntity = (HomeEntity) args[0];
////      initRecycleView(homeEntity.posters);
//		initAdapter();
//		checkViewAppear();
//		/*modify by dragontec for bug 4334 end*/
//    }
//  }

	/*modify by dragontec for bug 4334 start*/
	private void initAdapter() {
		if (mAdapter == null) {
			mAdapter = new RecommendAdapter(mContext);
			mAdapter.setOnItemClickListener(this);
			mAdapter.setOnItemKeyListener(this);
		}
	}

	private void unInitAdapter() {
		if (mAdapter != null) {
			mAdapter.setOnItemKeyListener(null);
			mAdapter.setOnItemClickListener(null);
			mAdapter.clearData();
			mAdapter = null;
		}
	}

	private void initRecycleView() {
		if (mAdapter != null) {
			if (mAdapter.getData() == null) {
				mAdapter.setData(mFetchControl.mPosterMap.get(mBannerPk));
  			/*modify by dragontec for bug 4412 start*/
				if (mAdapter.getItemCount() > 0) {
					setVisibility(VISIBLE);
				}
			/*modify by dragontec for bug 4412 end*/
				mRecyclerView.setAdapter(mAdapter);
	/*add by dragontec for bug 4077 start*/
				checkFocus(mRecyclerView);
	/*add by dragontec for bug 4077 end*/
			} else {
  			/*modify by dragontec for bug 4412 start*/
				if (mAdapter.getItemCount() > 0) {
					setVisibility(VISIBLE);
				}
			/*modify by dragontec for bug 4412 end*/
				int start = mFetchControl.mPosterMap.get(mBannerPk).size() - mFetchControl.getHomeEntity(mBannerPk).posters.size();
				int end = mFetchControl.mPosterMap.get(mBannerPk).size();
				mAdapter.notifyItemRangeInserted(start, end - start + 1);
			}
		}
	}
  /*modify by dragontec for bug 4334 end*/

	@Override
	public View onFocusSearchFailed(
			View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
		if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
	/*modify by dragontec for bug 4299 start*/
			if (mRecyclerView.getChildAt(0).findViewById(R.id.recommend_ismartv_linear_layout) == focused
					|| mRecyclerView
					.getChildAt(mRecyclerView.getChildCount() - 1)
					.findViewById(R.id.recommend_ismartv_linear_layout)
					== focused) {
				YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
			}
    /*modify by dragontec for bug 4299 end*/
			return focused;
		}
/*add by dragontec for bug 4331 start*/
		if (isLastView && focusDirection == View.FOCUS_DOWN) {
			YoYo.with(Techniques.VerticalShake).duration(1000).playOn(focused);
		}
/*add by dragontec for bug 4331 end*/

    /*modify by dragontec for bug 4221 start*/
    /*modify by dragontec for bug 4338 start*/
		return findNextUpDownFocus(focusDirection, mBannerLinearLayout, focused);
    /*modify by dragontec for bug 4338 end*/
    /*modify by dragontec for bug 4221 end*/
	}

	@Override
	public void onLoadMoreItems() {
	}

	@Override
	public void onItemClick(View view, int position) {
		if (mFetchControl.mPosterMap.get(mBannerPk) != null) {
			BannerPoster bannerRecommend = mFetchControl.mPosterMap.get(mBannerPk).get(position);
			if (bannerRecommend != null) {
				mFetchControl.go2Detail(bannerRecommend.pk, bannerRecommend.model_name, bannerRecommend.content_model, bannerRecommend.url, bannerRecommend.title, null, null, null, null, null, 0, null);
				mFetchControl.launcher_vod_click(bannerRecommend.model_name, bannerRecommend.pk + "", bannerRecommend.title, locationY + "," + (position + 1), mChannel);
			}
		}
	}

	@Override
	public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.navigation_left) {
			if (!mRecyclerView.isNotScrolling()) {
				return;
			}
			if (mRecommendLayoutManager.findFirstCompletelyVisibleItemPosition() > 0) {
				mRecommendLayoutManager.setCanScroll(true);
				int targetPosition = mRecommendLayoutManager.findFirstCompletelyVisibleItemPosition() - 4;
				if (targetPosition < 0) {
					targetPosition = 0;
				}
				setNeedCheckScrollEnd();
				mRecommendLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
			}

//			mRecommendLayoutManager.setCanScroll(true);
//			if (mRecommendLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) { // 向左滑动
//				int targetPosition = mRecommendLayoutManager.findFirstCompletelyVisibleItemPosition() - 4;
//				if (targetPosition <= 0) targetPosition = 0;
///*add by dragontec for bug 4332 start*/
//				setNeedCheckScrollEnd();
///*add by dragontec for bug 4332 end*/
//				mRecommendLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
//			}
		} else if (i == R.id.navigation_right) { // 向右滑动
			if (!mRecyclerView.isNotScrolling()) {
				return;
			}
			if (mRecommendLayoutManager.findLastCompletelyVisibleItemPosition() <= mFetchControl.getHomeEntity(mBannerPk).count) {
				mRecommendLayoutManager.setCanScroll(true);
				mRecyclerView.loadMore();
				int targetPosition = mRecommendLayoutManager.findLastCompletelyVisibleItemPosition() + 4;
				if (targetPosition > mRecommendLayoutManager.getItemCount() - 1) {
					targetPosition = mRecommendLayoutManager.getItemCount() - 1;
				}
				setNeedCheckScrollEnd();
				mRecommendLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
			}

//			mRecommendLayoutManager.setCanScroll(true);
//			mRecyclerView.loadMore();
//			if (mRecommendLayoutManager.findLastCompletelyVisibleItemPosition()
//					<= mRecommendLayoutManager.getItemCount() - 1) {
//				int targetPosition = mRecommendLayoutManager.findLastCompletelyVisibleItemPosition() + 4;
//				if (targetPosition > mRecommendLayoutManager.getItemCount() - 1) {
//					targetPosition = mRecommendLayoutManager.getItemCount() - 1;
//				}
///*add by dragontec for bug 4332 start*/
//				setNeedCheckScrollEnd();
///*add by dragontec for bug 4332 end*/
//				mRecommendLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
//
//			/*delete by dragontec for bug 4303 start*/
////        try {
////          if (targetPosition == mFetchControl.getHomeEntity(mBannerPk).count)
////            YoYo.with(Techniques.HorizontalShake)
////                    .duration(1000)
////                    .playOn(
////                            mRecyclerView
////                                    .getChildAt(mRecyclerView.getChildCount() - 1)
////                                    .findViewById(R.id.tv_player_ismartv_linear_layout));
////        }catch (Exception e){
////          e.printStackTrace();
////        }
//			/*delete by dragontec for bug 4303 end*/
//			}
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
/*delete by dragontec for bug 4332 start*/
//          navigationLeft.setVisibility(View.INVISIBLE);
//          navigationRight.setVisibility(View.INVISIBLE);
/*delete by dragontec for bug 4332 end*/
/*modify by dragontec for bug 4057 start*/
//          HomeActivity.mHoverView.requestFocus(); // 将焦点放置到一块隐藏view中
					v.clearFocus();
/*modify by dragontec for bug 4057 end*/
				}
				break;
		}
		return false;
	}

	@Override
	public void onKey(View v, int keyCode, KeyEvent event) {
		if (isParentScrolling) {
			return;
		}
		if (event.getAction() == KeyEvent.ACTION_UP) {
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				checkViewLocation(v);
			}
		}
	}
}
/*modify by dragontec for bug 4362 end*/