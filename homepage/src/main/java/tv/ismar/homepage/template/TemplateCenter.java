package tv.ismar.homepage.template;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.OnItemHoverListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.CenterAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.view.BannerLinearLayout;
import tv.ismar.homepage.widget.CenterRecyclerViewTV;

import static android.view.MotionEvent.BUTTON_PRIMARY;

/**
 * @AUTHOR: xi @DATE: 2017/9/5 @DESC: 居中模版
 */
public class TemplateCenter extends Template
        implements BaseControl.ControlCallBack,
        RecyclerViewTV.PagingableListener,
        LinearLayoutManagerTV.FocusSearchFailedListener,
        OnItemClickListener,
		/*modify by dragontec for bug 4277 start*/
        View.OnHoverListener, View.OnClickListener, OnItemHoverListener {
		/*modify by dragontec for bug 4277 end*/
    public FetchDataControl mFetchDataControl = null;
/*delete by dragontec for bug 4332 start*/
//    private RecyclerViewTV mRecycleView; // 海报recycleview
/*delete by dragontec for bug 4332 end*/
    private LinearLayoutManagerTV mCenterLayoutManager;
    private CenterAdapter mAdapter;
    private BannerLinearLayout mBannerLinearLayout;
/*delete by dragontec for bug 4332 start*/
//    private View navigationLeft;
//    private View navigationRight;
/*delete by dragontec for bug 4332 end*/
    private String mBannerPk;

	/*modify by dragontec for bug 4334 start*/
    public TemplateCenter(Context context, int position) {
        super(context, position);
        mFetchDataControl = new FetchDataControl(context, this);
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
        if (mFetchDataControl != null){
            mFetchDataControl.stop();
        }
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
        if (mFetchDataControl != null) {
            mFetchDataControl.clear();
        }
        if (mAdapter != null) {
            mAdapter.setOnItemSelectedListener(null);
            mAdapter.setOnHoverListener(null);
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
    }

    @Override
    public void getView(View view) {
/*modify by dragontec for bug 4332 start*/
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.center_recyclerview);
		/*modify by dragontec for bug 4221 start*/
        mRecyclerView.setTag("recycleView");
		/*modify by dragontec for bug 4221 end*/
        mCenterLayoutManager =
                new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mCenterLayoutManager);
        mRecyclerView.setSelectedItemAtCentered(true);
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
    public void initData(Bundle bundle) {
        mBannerPk = bundle.getString("banner");
/*modify by dragontec for bug 4334 start*/
		mFetchDataControl.fetchBanners(mBannerPk, 1, false);
    }

	@Override
	public void fetchData() {
		hasAppeared = true;
	}

	@Override
	public void fillData() {
		if (isNeedFillData) {
			isNeedFillData = false;
			initRecycle();
		}
	}

/*modify by dragontec for bug 4334 end*/

    @Override
    protected void initListener(View view) {
        super.initListener(view);
        navigationRight.setOnHoverListener(this);
        navigationLeft.setOnHoverListener(this);
        navigationLeft.setOnClickListener(this);
        navigationRight.setOnClickListener(this);
/*modify by dragontec for bug 4332 start*/
        mRecyclerView.setPagingableListener(this);
/*modify by dragontec for bug 4332 end*/
        mCenterLayoutManager.setFocusSearchFailedListener(this);
    }

	/*modify by dragontec for bug 4334 start*/
    private void initAdapter() {
    	if (mAdapter == null) {
			mAdapter = new CenterAdapter(mContext);
			mAdapter.setOnItemClickListener(this);
			/*modify by dragontec for bug 4277 start*/
			mAdapter.setOnHoverListener(this);
		}
	}

    private void initRecycle() {
    	if (mAdapter != null) {
    		if (mAdapter.getData() == null) {
    			mAdapter.setData(mFetchDataControl.mCarousels);
    			/*modify by dragontec for bug 4332 start*/
				mRecyclerView.setAdapter(mAdapter);
/*modify by dragontec for bug 4332 end*/
/*modify by dragontec for bug 4365 start*/
				mCenterLayoutManager.scrollToPositionWithOffset(
						mFetchDataControl.mCarousels.size() * 100,
                        ((CenterRecyclerViewTV)mRecyclerView).getCenterOffset());
/*modify by dragontec for bug 4365 end*/
			/*modify by dragontec for bug 4277 start*/
		/*add by dragontec for bug 4077 start*/
/*modify by dragontec for bug 4332 start*/
				checkFocus(mRecyclerView, mFetchDataControl.mCarousels.size() * 100);
/*modify by dragontec for bug 4332 end*/
		/*add by dragontec for bug 4077 end*/
			} else {
				int start =
						mFetchDataControl.mCarousels.size() - mFetchDataControl.mHomeEntity.carousels.size();
				int end = mFetchDataControl.mPoster.size();
				mAdapter.notifyItemRangeChanged(start, end);
			}
		}
    }
    /*modify by dragontec for bug 4334 end*/

    @Override
    public void callBack(int flags, Object... args) {
/*modify by dragontec for bug 4332 start*/
        if (flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG) { // 获取单个banner业务
			/*modify by dragontec for bug 4334 start*/
			isNeedFillData = true;
			initAdapter();
//            initRecycle();
			checkViewAppear();
			/*modify by dragontec for bug 4334 end*/
	/* modify by dragontec for bug 4264 start */
			mRecyclerView.setOnLoadMoreComplete();
        } else if (flags == FetchDataControl.FETCH_DATA_FAIL_FLAG) {
        	if (mRecyclerView.isOnLoadMore()) {
				mFetchDataControl.mHomeEntity.page--;
				mRecyclerView.setOnLoadMoreComplete();
			}
	/* modify by dragontec for bug 4264 end */
		}
/*modify by dragontec for bug 4332 end*/
    }

    @Override
    public void onLoadMoreItems() {
        Log.i(TAG, "onLoadMoreItems");
        HomeEntity homeEntity = mFetchDataControl.mHomeEntity;
        if (homeEntity != null) {
            if (homeEntity.page < homeEntity.num_pages) {
	/* modify by dragontec for bug 4264 start */
                mFetchDataControl.fetchBanners(mBannerPk, ++homeEntity.page, true);
            } else {
/*modify by dragontec for bug 4332 start*/
				mRecyclerView.setOnLoadMoreComplete();
/*modify by dragontec for bug 4332 end*/
			}
	/* modify by dragontec for bug 4264 end */
        }
    }

    @Override
    public View onFocusSearchFailed(
            View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
/*modify by dragontec for bug 4332 start*/
            if (mRecyclerView.getChildAt(0).findViewById(R.id.center_ismartv_linear_layout) == focused
                    || mRecyclerView
                    .getChildAt(mRecyclerView.getChildCount() - 1)
                    .findViewById(R.id.center_ismartv_linear_layout)
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
    public void onItemClick(View view, int position) {
		/*add by dragontec for bug 4307,4277 start*/
        if(view.hasFocus()) {
            mFetchDataControl.go2Detail(mFetchDataControl.mCarousels.get(position));
        }
		/*add by dragontec for bug 4307,4277 end*/
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
                    v.clearFocus();
                }
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.navigation_left){
/*add by dragontec for bug 4332 start*/
			setNeedCheckScrollEnd();
/*add by dragontec for bug 4332 end*/
            mCenterLayoutManager.setCanScroll(true);
/*modify by dragontec for bug 4332,4365 start*/
            mCenterLayoutManager.scrollToPositionWithOffset(mRecyclerView.findFirstVisibleItemPosition(), ((CenterRecyclerViewTV)mRecyclerView).getCenterOffset());
/*modify by dragontec for bug 4332,4365 end*/
        }else if(v.getId()==R.id.navigation_right){
/*add by dragontec for bug 4332 start*/
			setNeedCheckScrollEnd();
/*add by dragontec for bug 4332 end*/
            mCenterLayoutManager.setCanScroll(true);
/*modify by dragontec for bug 4332,4365 start*/
            mCenterLayoutManager.scrollToPositionWithOffset(mRecyclerView.findLastVisibleItemPosition(),((CenterRecyclerViewTV)mRecyclerView).getCenterOffset());
/*modify by dragontec for bug 4332,4365 end*/

        }
    }
	/*add by dragontec for bug 4277 start*/
    @Override
    public boolean onHover(View v, int position, boolean hovered) {
        Rect rect = new Rect();
        v.getGlobalVisibleRect(rect);
        int middle = mContext.getResources().getDisplayMetrics().widthPixels/2;
        if(rect.left< middle && rect.right> middle){
            return true;
        }else{
            return false;
        }
    }
	/*add by dragontec for bug 4277 end*/
}
