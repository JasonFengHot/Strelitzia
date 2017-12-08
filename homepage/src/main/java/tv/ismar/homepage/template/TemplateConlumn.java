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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import java.util.List;

import tv.ismar.app.BaseControl;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.OnItemKeyListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.ConlumnAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.fragment.ChannelFragment;
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
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: 栏目模版
 */
public class TemplateConlumn extends Template
        implements BaseControl.ControlCallBack,
        RecyclerViewTV.PagingableListener,
        LinearLayoutManagerTV.FocusSearchFailedListener,
        OnItemClickListener,
		OnItemKeyListener,
        View.OnHoverListener,
        View.OnClickListener {
/*delete by dragontec for bug 4332 start*/
//    private RecyclerViewTV mRecyclerView;
/*delete by dragontec for bug 4332 end*/
    private LinearLayoutManagerTV mConlumnLayoutManager;
    private ConlumnAdapter mAdapter;
    private BannerLinearLayout mBannerLinearLayout;
/*delete by dragontec for bug 4332 start*/
//    private View navigationLeft;
//    private View navigationRight;
/*delete by dragontec for bug 4332 end*/
    private String mName; // 频道名称（中文）
    private String mChannel; // 频道名称（英文）
    private int locationY;


    public TemplateConlumn(Context context, int position, FetchDataControl fetchDataControl) {
        super(context, position, fetchDataControl);
    }

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
	/*add by dragontec for bug end start*/
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
        mTitleCountTv = (TextView) view.findViewById(R.id.banner_title_count);
		mConlumnLayoutManager =
				new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.conlumn_recyclerview);
        mRecyclerView.setTag("recycleView");
        mRecyclerView.setLayoutManager(mConlumnLayoutManager);
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
		mConlumnLayoutManager = null;
		mTitleCountTv = null;
	}

	@Override
    public void initData(Bundle bundle) {
    	initAdapter();
        mBannerPk = bundle.getString(BANNER_KEY);
        mName = bundle.getString("title");
        mChannel = bundle.getString(CHANNEL_KEY);
        locationY=bundle.getInt(BANNER_LOCATION,0);
/*modify by dragontec for bug 4334 start*/
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
			initRecycleView();
		}
	}

/*modify by dragontec for bug 4334 end*/

    @Override
    protected void initListener(View view) {
    	super.initListener(view);
        navigationLeft.setOnClickListener(this);
        navigationRight.setOnClickListener(this);
        mRecyclerView.setPagingableListener(this);
        mConlumnLayoutManager.setFocusSearchFailedListener(this);
    }

	@Override
	protected void unInitListener() {
    	mConlumnLayoutManager.setFocusSearchFailedListener(null);
    	mRecyclerView.setPagingableListener(null);
    	navigationRight.setOnClickListener(null);
    	navigationLeft.setOnClickListener(null);
		super.unInitListener();
	}

	//    @Override
//    public void callBack(int flags, Object... args) {
//    	switch (flags) {
//			case FetchDataControl.FETCH_M_BANNERS_LIST_FLAG: {
//				if (args != null && args instanceof String[]) {
//					String[] banners = (String[]) args;
//					for (String banner :
//							banners) {
//						if (banner == null || banner.isEmpty()) {
//							continue;
//						}
//						if (banner.equals(mBannerPk)) {
//							isNeedFillData = true;
//							initAdapter();
//							checkViewAppear();
//							mRecyclerView.setOnLoadMoreComplete();
//							break;
//						}
//					}
//				}
//			}
//			break;
//			case FetchDataControl.FETCH_BANNERS_LIST_FLAG:
//			{
//				/*modify by dragontec for bug 4334 start*/
//				isNeedFillData = true;
//				initAdapter();
//				checkViewAppear();
//           		/*modify by dragontec for bug 4334 end*/
//				mRecyclerView.setOnLoadMoreComplete();
//			}
//			break;
//			case FetchDataControl.FETCH_DATA_FAIL_FLAG: {
//				if (mRecyclerView.isOnLoadMore()) {
//					mFetchDataControl.mHomeEntity.page--;
//					mRecyclerView.setOnLoadMoreComplete();
//				}
//			}
//			break;
//		}
//    }

	/*modify by dragontec for bug 4334 start*/
    private void initAdapter() {
    	if (mAdapter == null) {
			mAdapter = new ConlumnAdapter(mContext);
			mAdapter.setOnItemClickListener(this);
			mAdapter.setOnItemKeyListener(this);
		}
	}

	private void unInitAdapter() {
    	if (mAdapter != null) {
    		mRecyclerView.setAdapter(null);
			mAdapter.setOnItemKeyListener(null);
    		mAdapter.setOnItemClickListener(null);
    		mAdapter.clearData();
    		mAdapter = null;
		}
	}

	private void initRecycleView() {
    	if (mAdapter != null) {
    		if (mAdapter.getData() == null) {
    			if (mFetchControl.mPosterMap.get(mBannerPk) != null) {
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
				}
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
    public void onLoadMoreItems() {
        Log.i(TAG, "onLoadMoreItems");
        HomeEntity homeEntity = mFetchControl.getHomeEntity(mBannerPk);
        if (homeEntity != null) {
            if (homeEntity.page < homeEntity.num_pages) {
	/* modify by dragontec for bug 4264 start */
                mFetchControl.fetchBanners(mBannerPk, ++homeEntity.page, true);
            } else {
				mRecyclerView.setOnLoadMoreComplete();
			}
	/* modify by dragontec for bug 4264 end */
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
    public void onItemClick(View view, int position) {
//        if (position == mFetchDataControl.mHomeEntity.count - 1) {
//            new PageIntent()
//                    .toListPage(
//                            mContext,
//                            mFetchDataControl.mHomeEntity.channel_title,
//                            mFetchDataControl.mHomeEntity.channel,
//                            mFetchDataControl.mHomeEntity.style,
//                            mFetchDataControl.mHomeEntity.section_slug);
//        } else {
//            mFetchDataControl.go2Detail(mFetchDataControl.mHomeEntity.posters.get(position));
//        }
        BannerPoster poster = mFetchControl.getHomeEntity(mBannerPk).posters.get(position);
        if(poster.model_name.contains("item")){
            if(poster.content_model.contains("gather")){
                new PageIntent().toSubject(mContext,poster.content_model,poster.pk,poster.title,"homepage",poster.channel);
            }
        }else if(poster.model_name.equals("section")) {
            new PageIntent().toListPage(mContext,poster.channel_title,poster.channel,poster.style,poster.section_slug);
        }
        mFetchControl.launcher_vod_click(poster.model_name,poster.pk+"",poster.title,locationY+","+(position+1),mChannel);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.navigation_left) {
			if (!mRecyclerView.isNotScrolling()) {
				return;
			}
			if (mConlumnLayoutManager.findFirstCompletelyVisibleItemPosition()  > 0) {
				mConlumnLayoutManager.setCanScroll(true);
				int targetPosition = mConlumnLayoutManager.findFirstCompletelyVisibleItemPosition() - 4;
				if (targetPosition < 0) {
					targetPosition = 0;
				}
				setNeedCheckScrollEnd();
				mConlumnLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
			}
//            mConlumnLayoutManager.setCanScroll(true);
//            if (mConlumnLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) { // 向左滑动
//                int targetPosition = mConlumnLayoutManager.findFirstCompletelyVisibleItemPosition() - 4;
//                if (targetPosition <= 0) targetPosition = 0;
///*add by dragontec for bug 4332 start*/
//				setNeedCheckScrollEnd();
///*add by dragontec for bug 4332 end*/
//                mConlumnLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
//            }
        } else if (i == R.id.navigation_right) { // 向右滑动
			if (!mRecyclerView.isNotScrolling()) {
				return;
			}
			if (mConlumnLayoutManager.findLastCompletelyVisibleItemPosition()
					<= mFetchControl.getHomeEntity(mBannerPk).count) {
				mConlumnLayoutManager.setCanScroll(true);
				mRecyclerView.loadMore();
				int targetPosition = mConlumnLayoutManager.findLastCompletelyVisibleItemPosition() + 4;
				if (targetPosition > mConlumnLayoutManager.getItemCount() - 1) {
					targetPosition = mConlumnLayoutManager.getItemCount() - 1;
				}
				setNeedCheckScrollEnd();
				mConlumnLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
			}
//            mConlumnLayoutManager.setCanScroll(true);
//            if (mConlumnLayoutManager.findLastCompletelyVisibleItemPosition()
//                    <= mFetchControl.getHomeEntity(mBannerPk).count) {
//				mRecyclerView.loadMore();
//                int targetPosition = mConlumnLayoutManager.findLastCompletelyVisibleItemPosition() + 4;
//				if (targetPosition > mConlumnLayoutManager.getItemCount() - 1) {
//					targetPosition = mConlumnLayoutManager.getItemCount() - 1;
//				}
////                if (targetPosition >= mFetchControl.getHomeEntity(mBannerPk).count) {
////                    targetPosition = mFetchControl.getHomeEntity(mBannerPk).count;
////                }
///*add by dragontec for bug 4332 start*/
//				setNeedCheckScrollEnd();
///*add by dragontec for bug 4332 end*/
//                mConlumnLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
//
//			/*delete by dragontec for bug 4303 start*/
////                if (targetPosition == mFetchDataControl.mHomeEntity.count)
////                    YoYo.with(Techniques.HorizontalShake)
////                            .duration(1000)
////                            .playOn(
////                                    mRecyclerView
////                                            .getChildAt(mRecyclerView.getChildCount() - 1)
////                                            .findViewById(R.id.conlumn_ismartv_linear_layout));
//			/*delete by dragontec for bug 4303 end*/
//            }
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
//                    navigationLeft.setVisibility(View.INVISIBLE);
//                    navigationRight.setVisibility(View.INVISIBLE);
/*delete by dragontec for bug 4332 end*/
/*modify by dragontec for bug 4057 start*/
//                    HomeActivity.mHoverView.requestFocus(); // 将焦点放置到一块隐藏view中
                    v.clearFocus();
/*modify by dragontec for bug 4057 end*/
                }
                if (v == navigationLeft || v == navigationRight) {
                	dismissNavigationButton();
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