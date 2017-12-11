/*modify by dragontec for bug 4362 start*/
package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseControl;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.OnItemHoverListener;
import tv.ismar.homepage.OnItemKeyListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.MovieAdapter;
import tv.ismar.homepage.adapter.SubscribeAdapter;
import tv.ismar.homepage.banner.adapter.BannerSubscribeAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.control.OrderControl;
import tv.ismar.homepage.fragment.ChannelFragment;
import tv.ismar.homepage.view.BannerLinearLayout;
	/*add by dragontec for bug 4077 start*/
/*add by dragontec for bug 4077 end*/

import static android.view.MotionEvent.BUTTON_PRIMARY;
/*delete by dragontec for bug 4057 start*/
//import static tv.ismar.homepage.HomeActivity.mHoverView;
/*delete by dragontec for bug 4057 end*/
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static tv.ismar.homepage.control.FetchDataControl.FETCH_BANNERS_LIST_FLAG;
import static tv.ismar.homepage.control.FetchDataControl.FETCH_M_BANNERS_LIST_NEXTPAGE_FLAG;
import static tv.ismar.homepage.fragment.ChannelFragment.CHANNEL_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.NAME_KEY;

/**
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: 预约模版
 */
public class TemplateOrder extends Template
		implements RecyclerViewTV.PagingableListener,
		LinearLayoutManagerTV.FocusSearchFailedListener,
		RecyclerViewTV.OnItemFocusChangeListener,
		OnItemClickListener,
		OnItemHoverListener,
		OnItemKeyListener,
		View.OnHoverListener,
		View.OnClickListener {
	private int mSelectItemPosition = 1; // 标题--选中海报位置
	private TextView mTitleTv; // banner标题
	/*delete by dragontec for bug 4332 start*/
//    private RecyclerViewTV mRecycleView;
/*delete by dragontec for bug 4332 end*/
	private SubscribeAdapter mAdapter;
	private LinearLayoutManagerTV mSubscribeLayoutManager = null;
	private BannerLinearLayout mBannerLinearLayout;
	/*delete by dragontec for bug 4332 start*/
//    private View navigationLeft;
//    private View navigationRight;
/*delete by dragontec for bug 4332 end*/
	private String mName; // 频道名称（中文）
	private String mChannel; // 频道名称（英文）
	private int locationY;

	/*modify by dragontec for bug 4334 start*/
	public TemplateOrder(Context context, int position, FetchDataControl fetchDataControl) {
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
		mTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
		mTitleCountTv = (TextView) view.findViewById(R.id.banner_title_count);
		mSubscribeLayoutManager =
				new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
		mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.subscribe_banner);
		mRecyclerView.setTag("recycleView");
		mRecyclerView.setLayoutManager(mSubscribeLayoutManager);
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
		mHoverView = view.findViewById(R.id.hover_view);
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
		mSubscribeLayoutManager = null;
		mTitleCountTv = null;
		mTitleTv = null;
	}

	@Override
	protected void initListener(View view) {
/*add by dragontec for bug 4332 start*/
		super.initListener(view);
/*add by dragontec for bug 4332 end*/
		navigationLeft.setOnClickListener(this);
		navigationRight.setOnClickListener(this);
//		navigationRight.setOnHoverListener(this);
//		navigationLeft.setOnHoverListener(this);
/*modify by dragontec for bug 4332 start*/
		mRecyclerView.setPagingableListener(this);
		mRecyclerView.setOnItemFocusChangeListener(this);
/*modify by dragontec for bug 4332 end*/
		mSubscribeLayoutManager.setFocusSearchFailedListener(this);
	}

	@Override
	protected void unInitListener() {
		mSubscribeLayoutManager.setFocusSearchFailedListener(null);
		mRecyclerView.setOnItemFocusChangeListener(null);
		mRecyclerView.setPagingableListener(null);
		navigationRight.setOnClickListener(null);
		navigationLeft.setOnClickListener(null);
		super.unInitListener();
	}

	@Override
	public void initData(Bundle bundle) {
		initAdapter();
		mTitleTv.setText(bundle.getString("title"));
		mBannerPk = bundle.getString("banner");
		mName = bundle.getString("title");
		mChannel = bundle.getString(CHANNEL_KEY);
		locationY=bundle.getInt(BANNER_LOCATION,0);
/*modify by dragontec for bug 4334 start*/
		mTitleCountTv.setText("00/00");
		if (mFetchControl.getHomeEntity(mBannerPk)!= null) {
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
	}

	@Override
	public void fillData() {
		if (isNeedFillData) {
			isNeedFillData = false;
			initTitle();
			initRecycle();
		}
	}

/*modify by dragontec for bug 4334 end*/

	private void initTitle() {
		if (mFetchControl.getHomeEntity(mBannerPk) != null) {
			if (mSelectItemPosition > mFetchControl.getHomeEntity(mBannerPk).count) {
				mSelectItemPosition = mFetchControl.getHomeEntity(mBannerPk).count;
			}
			mTitleCountTv.setText(
					String.format(
							mContext.getString(R.string.home_item_title_count),
							mSelectItemPosition + "",
							mFetchControl.getHomeEntity(mBannerPk).count + ""));
		}
	}

	/*modify by dragontec for bug 4334 start*/
	private void initAdapter() {
		if (mAdapter == null) {
			mAdapter = new SubscribeAdapter(mContext);
/*modify by dragontec for bug 4332 start*/
			mAdapter.setOnItemClickListener(this);
/*modify by dragontec for bug 4332 end*/
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

	private void initRecycle() {
		if (mAdapter != null) {
			if (mAdapter.getData() == null) {
				if (mFetchControl.getHomeEntity(mBannerPk) != null) {
					mAdapter.setData(mFetchControl.getHomeEntity(mBannerPk).posters);
					/*modify by dragontec for bug 4412 start*/
					if (mAdapter.getItemCount() > 0) {
						setVisibility(VISIBLE);
					}
					/*modify by dragontec for bug 4412 end*/
					mRecyclerView.setAdapter(mAdapter);
	/*add by dragontec for bug 4077 start*/
					checkFocus(mRecyclerView);
	/*add by dragontec for bug 4077 end*/
				}
			} else {
				mAdapter.getData().addAll(mFetchControl.getHomeEntity(mBannerPk).posters);
				/*modify by dragontec for bug 4412 start*/
				if (mAdapter.getItemCount() > 0) {
					setVisibility(VISIBLE);
				}
				/*modify by dragontec for bug 4412 end*/
				int start = mAdapter.getData().size() - mFetchControl.getHomeEntity(mBannerPk).posters.size();
				int end = mAdapter.getData().size();
				mAdapter.notifyItemRangeInserted(start, end - start + 1);
			}
		}
	}
    /*modify by dragontec for bug 4334 end*/

	@Override
	public void onLoadMoreItems() { // 加载更多数据
		Log.i(TAG, "onLoadMoreItems");
		HomeEntity homeEntity = mFetchControl.getHomeEntity(mBannerPk);
		if (homeEntity != null) {
			if (homeEntity.page < homeEntity.num_pages) {
	/* modify by dragontec for bug 4264 start */
				mFetchControl.fetchBanners(mBannerPk, ++homeEntity.page, true);
			} else {
/*modify by dragontec for bug 4332 start*/
				mRecyclerView.setOnLoadMoreComplete();
/*modify by dragontec for bug 4332 end*/
	/* modify by dragontec for bug 4264 end */
			}
		}
	}

	/*第1个和最后一个海报抖动功能*/
	@Override
	public View onFocusSearchFailed(
			View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
		if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
/*modify by dragontec for bug 4332 start*/
			if (mRecyclerView.getChildAt(0).findViewById(R.id.item_layout) == focused
					|| mRecyclerView
					.getChildAt(mRecyclerView.getChildCount() - 1)
					.findViewById(R.id.item_layout)
					== focused) {
				YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
			}
/*modify by dragontec for bug 4332 end*/
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
	public void onItemFocusGain(View itemView, int position) {
		mSelectItemPosition = position + 1;
		initTitle();
	}

	@Override
	public void onItemClick(View view, int position) {
		if (mFetchControl.getHomeEntity(mBannerPk).is_more && position == mAdapter.getItemCount() - 1) {
			new PageIntent()
					.toListPage(
							mContext,
							mFetchControl.getHomeEntity(mBannerPk).channel_title,
							mFetchControl.getHomeEntity(mBannerPk).channel,
							Integer.valueOf(mFetchControl.getHomeEntity(mBannerPk).style),
							mFetchControl.getHomeEntity(mBannerPk).section_slug);
			mFetchControl.launcher_vod_click("section",-1+"","更多",locationY+","+(position+1),mChannel);
		} else {
        	/*modify by dragontec for bug 4334 start*/
			mFetchControl.go2Detail(mAdapter.getData().get(position));
            /*modify by dragontec for bug 4334 end*/
			BannerPoster entity=mAdapter.getData().get(position);
			mFetchControl.launcher_vod_click(entity.model_name,entity.pk+"",entity.title,locationY+","+(position+1),mChannel);
		}
	}

	/*modify by dragontec for bug 4277 start*/
	@Override
	public boolean onHover(View v, int position, boolean hovered) {
/*modify by dragontec for bug 4332 start*/
		mRecyclerView.setHovered(hovered);
/*modify by dragontec for bug 4332 end*/
		return true;
	}
	/*modify by dragontec for bug 4277 end*/

	@Override
	public boolean onHover(View v, MotionEvent event) {
		Log.i("onHover", "Template action:" + event.getAction());
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
//                    navigationLeft.setVisibility(View.INVISIBLE);
//                    navigationRight.setVisibility(View.INVISIBLE);
/*delete by dragontec for bug 4332 end*/
/*modify by dragontec for bug 4057 start*/
//                    HomeActivity.mHoverView.requestFocus(); // 将焦点放置到一块隐藏view中
					v.clearFocus();
/*modify by dragontec for bug 4057 end*/
				}
				break;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.navigation_left) {
			if (!mRecyclerView.isNotScrolling()) {
				return;
			}
			if (mSubscribeLayoutManager.findFirstCompletelyVisibleItemPosition() > 0) {
				mSubscribeLayoutManager.setCanScroll(true);
				int targetPosition = mSubscribeLayoutManager.findFirstCompletelyVisibleItemPosition() - 4;
				if (targetPosition < 0) {
					targetPosition = 0;
				}
				mSelectItemPosition = targetPosition + 1;
				setNeedCheckScrollEnd();
				mSubscribeLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
				initTitle();
			}
//			mSubscribeLayoutManager.setCanScroll(true);
//			if (mSubscribeLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) { // 向左滑动
//				int targetPosition = mSubscribeLayoutManager.findFirstCompletelyVisibleItemPosition() - 4;
//				if (targetPosition <= 0) targetPosition = 0;
//				mSelectItemPosition = targetPosition + 1;
///*add by dragontec for bug 4332 start*/
//				setNeedCheckScrollEnd();
///*add by dragontec for bug 4332 end*/
///*modify by dragontec for bug 4332 start*/
//				mSubscribeLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
///*modify by dragontec for bug 4332 end*/
//				initTitle();
//			}
		} else if (i == R.id.navigation_right) { // 向右滑动
			if (!mRecyclerView.isNotScrolling()) {
				return;
			}
			if (mSubscribeLayoutManager.findLastCompletelyVisibleItemPosition() <= mFetchControl.getHomeEntity(mBannerPk).count) {
				mSubscribeLayoutManager.setCanScroll(true);
				mRecyclerView.loadMore();
				int targetPosition = mSubscribeLayoutManager.findLastCompletelyVisibleItemPosition() + 4;
				if (targetPosition > mSubscribeLayoutManager.getItemCount() - 1) {
					targetPosition = mSubscribeLayoutManager.getItemCount() - 1;
				}
				mSelectItemPosition = targetPosition + 1;
				setNeedCheckScrollEnd();
				mSubscribeLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
				initTitle();
			}
//			mSubscribeLayoutManager.setCanScroll(true);
///*modify by dragontec for bug 4332 start*/
//			mRecyclerView.loadMore();
///*modify by dragontec for bug 4332 end*/
//			if (mSubscribeLayoutManager.findLastCompletelyVisibleItemPosition()
//					<= mSubscribeLayoutManager.getItemCount() - 1) {
//				int targetPosition = mSubscribeLayoutManager.findLastCompletelyVisibleItemPosition() + 4;
//				if (targetPosition > mSubscribeLayoutManager.getItemCount() - 1) {
//					targetPosition = mSubscribeLayoutManager.getItemCount() - 1;
//				}
//				mSelectItemPosition = targetPosition + 1;
///*add by dragontec for bug 4332 start*/
//				setNeedCheckScrollEnd();
///*add by dragontec for bug 4332 end*/
///*modify by dragontec for bug 4332 start*/
//				mSubscribeLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
///*modify by dragontec for bug 4332 end*/
//				initTitle();
//			}
		}
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