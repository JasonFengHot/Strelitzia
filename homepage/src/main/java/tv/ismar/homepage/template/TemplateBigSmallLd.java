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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.OnItemHoverListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.MovieMixAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.view.BannerLinearLayout;
	/*add by dragontec for bug 4077 start*/
import tv.ismar.homepage.widget.RecycleLinearLayout;
	/*add by dragontec for bug 4077 end*/

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static tv.ismar.homepage.fragment.ChannelFragment.CHANNEL_KEY;
import static tv.ismar.homepage.fragment.ChannelFragment.NAME_KEY;

/**
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: 大横小竖模版
 */
public class TemplateBigSmallLd extends Template
		implements RecyclerViewTV.PagingableListener,
		LinearLayoutManagerTV.FocusSearchFailedListener,
		RecyclerViewTV.OnItemFocusChangeListener,
		OnItemClickListener,
		OnItemHoverListener,
		View.OnHoverListener,
		View.OnClickListener {
	private int mSelectItemPosition = 1; // 标题--选中海报位置
	private TextView mTitleTv; // banner标题
	/*delete by dragontec for bug 4332 start*/
//    private RecyclerViewTV mRecycleView;
/*delete by dragontec for bug 4332 end*/
	private MovieMixAdapter mAdapter;
	private LinearLayoutManagerTV mMovieMixLayoutManager = null;
	private BannerLinearLayout mBannerLinearLayout;
	/*delete by dragontec for bug 4332 start*/
//    private View navigationLeft;
//    private View navigationRight;
/*delete by dragontec for bug 4332 end*/
	private String mName; // 频道名称（中文）
	private String mChannel; // 频道名称（英文）

	private static final int NAVIGATION_LEFT = 0x0001;
	private static final int NAVIGATION_RIGHT = 0x0002;

	/*modify by dragontec for bug 4334 start*/
	public TemplateBigSmallLd(Context context, int position, FetchDataControl fetchDataControl) {
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
/*add by dragontec for bug 4205 start*/
		if (mAdapter != null) {
			mAdapter.setOnItemClickListener(null);
		}
		if (mRecyclerView != null) {
			mRecyclerView.setLayoutManager(null);
			mRecyclerView.setAdapter(null);
		}
		if (mBannerLinearLayout != null) {
			mBannerLinearLayout.setNavigationLeft(null);
			mBannerLinearLayout.setNavigationRight(null);
			mBannerLinearLayout.setRecyclerViewTV(null);
			mBannerLinearLayout.setHeadView(null);
		}
/*add by dragontec for bug 4205 end*/
		super.onDestroy();
	}

	@Override
	public void getView(View view) {
		mTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
		mTitleCountTv = (TextView) view.findViewById(R.id.banner_title_count);
/*modify by dragontec for bug 4332 start*/
		mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.movie_mix_banner);
		/*modify by dragontec for bug 4221 start*/
		mRecyclerView.setTag("recycleView");
		/*modify by dragontec for bug 4221 end*/
		mMovieMixLayoutManager =
				new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
		mRecyclerView.setLayoutManager(mMovieMixLayoutManager);
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
/*modify by dragontec for bug 4332 end*/
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
		mMovieMixLayoutManager.setFocusSearchFailedListener(this);
	}

	@Override
	public void initData(Bundle bundle) {
		mTitleTv.setText(bundle.getString("title"));
		mBannerPk = bundle.getString("banner");
		mName = bundle.getString(NAME_KEY);
		mChannel = bundle.getString(CHANNEL_KEY);
/*modify by dragontec for bug 4334 start*/
		mTitleCountTv.setText("00/00");
		initAdapter();
		checkViewAppear();
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
			mAdapter = new MovieMixAdapter(mContext);
/*modify by dragontec for bug 4332 start*/
			mAdapter.setOnItemClickListener(this);
/*modify by dragontec for bug 4332 end*/
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
				mAdapter.notifyItemRangeChanged(start, end);
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
							mFetchControl.getHomeEntity(mBannerPk).style,
							mFetchControl.getHomeEntity(mBannerPk).section_slug);
		} else {
        	/*modify by dragontec for bug 4334 start*/
			mFetchControl.go2Detail(mAdapter.getData().get(position));
            /*modify by dragontec for bug 4334 end*/
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
			mMovieMixLayoutManager.setCanScroll(true);
			if (mMovieMixLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) { // 向左滑动
				int targetPosition = mMovieMixLayoutManager.findFirstCompletelyVisibleItemPosition() - 4;
				if (targetPosition <= 0) targetPosition = 0;
				mSelectItemPosition = targetPosition;
/*add by dragontec for bug 4332 start*/
				setNeedCheckScrollEnd();
/*add by dragontec for bug 4332 end*/
/*modify by dragontec for bug 4332 start*/
				mMovieMixLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
/*modify by dragontec for bug 4332 end*/
			}
		} else if (i == R.id.navigation_right) { // 向右滑动
			mMovieMixLayoutManager.setCanScroll(true);
/*modify by dragontec for bug 4332 start*/
			mRecyclerView.loadMore();
/*modify by dragontec for bug 4332 end*/
			if (mMovieMixLayoutManager.findLastCompletelyVisibleItemPosition()
					<= mFetchControl.getHomeEntity(mBannerPk).count) {
				int targetPosition = mMovieMixLayoutManager.findLastCompletelyVisibleItemPosition() + 4;
				if (targetPosition >= mFetchControl.getHomeEntity(mBannerPk).count) {
					targetPosition = mFetchControl.getHomeEntity(mBannerPk).count;
				}
				mSelectItemPosition = targetPosition;
/*add by dragontec for bug 4332 start*/
				setNeedCheckScrollEnd();
/*add by dragontec for bug 4332 end*/
/*modify by dragontec for bug 4332 start*/
				mMovieMixLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
/*modify by dragontec for bug 4332 end*/
			}
			initTitle();
		}
	}
}
/*modify by dragontec for bug 4362 end*/