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

import tv.ismar.app.BaseControl;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.OnItemHoverListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.TvPlayAdapter;
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
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: 电视剧模版
 */
public class TemplateTvPlay extends Template
        implements BaseControl.ControlCallBack,
        RecyclerViewTV.PagingableListener,
        LinearLayoutManagerTV.FocusSearchFailedListener,
        RecyclerViewTV.OnItemFocusChangeListener,
        OnItemClickListener,
        OnItemHoverListener,
        View.OnHoverListener,
        View.OnClickListener {
    private int mSelectItemPosition = 1; // 标题--选中海报位置
    private FetchDataControl mFetchDataControl = null; // 抓网络数据类
    private TextView mTitleTv; // banner标题
/*delete by dragontec for bug 4332 start*/
//    private RecyclerViewTV mRecycleView;
/*delete by dragontec for bug 4332 end*/
    private TvPlayAdapter mAdapter;
    private LinearLayoutManagerTV mTvPlayerLayoutManager = null;
    private BannerLinearLayout mBannerLinearLayout;
/*delete by dragontec for bug 4332 start*/
//    private View navigationLeft;
//    private View navigationRight;
/*delete by dragontec for bug 4332 end*/
    private String mBannerPk;
    private String mName; // 频道名称（中文）
    private String mChannel; // 频道名称（英文）

    private static final int NAVIGATION_LEFT = 0x0001;
    private static final int NAVIGATION_RIGHT = 0x0002;

    private NavigationtHandler mNavigationtHandler;

    private class NavigationtHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
/*delete by dragontec for bug 4332 start*/
//            switch (msg.what){
//                case NAVIGATION_LEFT:
//                    if (mRecyclerView!=null&&!mRecyclerView.cannotScrollBackward(-10)) {
//                        navigationLeft.setVisibility(VISIBLE);
//                    }else if (mRecyclerView!=null){
//                        navigationLeft.setVisibility(INVISIBLE);
//                    }
//                    break;
//                case NAVIGATION_RIGHT:
//                    if(mRecyclerView!=null&&!mRecyclerView.cannotScrollForward(10)){
//                        navigationRight.setVisibility(VISIBLE);
//                    }else if (mRecyclerView!=null){
//                        navigationRight.setVisibility(INVISIBLE);
//                    }
//                    break;
//            }
/*delete by dragontec for bug 4332 end*/
        }
    }

    public TemplateTvPlay(Context context) {
        super(context);
        mFetchDataControl = new FetchDataControl(context, this);
        mNavigationtHandler = new NavigationtHandler();
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
        if (mFetchDataControl != null) {
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
/*modify by dragontec for bug 4332 start*/
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.tv_player_recyclerview);
		/*modify by dragontec for bug 4221 start*/
        mRecyclerView.setTag("recycleView");
		/*modify by dragontec for bug 4221 end*/
        mTvPlayerLayoutManager =
                new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mTvPlayerLayoutManager);
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
        navigationRight.setOnHoverListener(this);
        navigationLeft.setOnHoverListener(this);
/*modify by dragontec for bug 4332 start*/
        mRecyclerView.setPagingableListener(this);
        mRecyclerView.setOnItemFocusChangeListener(this);
/*modify by dragontec for bug 4332 end*/
        mTvPlayerLayoutManager.setFocusSearchFailedListener(this);
    }

    @Override
    public void initData(Bundle bundle) {
        mTitleTv.setText(bundle.getString("title"));
        mBannerPk = bundle.getString("banner");
        mName = bundle.getString(NAME_KEY);
        mChannel = bundle.getString(CHANNEL_KEY);
/*modify by dragontec for bug 4200 start*/
		mTitleCountTv.setText("00/00");
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

    private void initRecycle() {
        if (mAdapter == null) {
            mAdapter = new TvPlayAdapter(mContext, mFetchDataControl.mPoster);
            mAdapter.setMarginLeftEnable(true);
/*modify by dragontec for bug 4332 start*/
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(this);
	/*add by dragontec for bug 4077 start*/
			checkFocus(mRecyclerView);
	/*add by dragontec for bug 4077 end*/
/*modify by dragontec for bug 4332 end*/
        } else {
            int start = mFetchDataControl.mPoster.size() - mFetchDataControl.mHomeEntity.posters.size();
            int end = mFetchDataControl.mPoster.size();
            mAdapter.notifyItemRangeChanged(start, end);
        }
    }

    @Override
    public void callBack(int flags, Object... args) {
        if (flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG) { // 获取单个banner业务
            initTitle();
            initRecycle();
	/* modify by dragontec for bug 4264 start */
/*modify by dragontec for bug 4332 start*/
			mRecyclerView.setOnLoadMoreComplete();
        } else if (flags == FetchDataControl.FETCH_DATA_FAIL_FLAG) {
			mFetchDataControl.mHomeEntity.page--;
			mRecyclerView.setOnLoadMoreComplete();
/*modify by dragontec for bug 4332 end*/
	/* modify by dragontec for bug 4264 end */
		}
    }

    @Override
    public void onLoadMoreItems() { // 加载更多数据
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
            if (mRecyclerView.getChildAt(0).findViewById(R.id.tv_player_ismartv_linear_layout) == focused
                    || mRecyclerView
                    .getChildAt(mRecyclerView.getChildCount() - 1)
                    .findViewById(R.id.tv_player_ismartv_linear_layout)
                    == focused) {
                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
            }
/*modify by dragontec for bug 4332 end*/
            return focused;
        }
/*add by dragontec for bug 4331 start*/
		if (isLastView && focusDirection == View.FOCUS_DOWN) {
			YoYo.with(Techniques.VerticalShake).duration(1000).playOn(mParentView);
		}
/*add by dragontec for bug 4331 end*/
        /*modify by dragontec for bug 4221 start*/
        return findNextUpDownFocus(focusDirection, mBannerLinearLayout);
        /*modify by dragontec for bug 4221 end*/
    }

    @Override
    public void onItemFocusGain(View itemView, int position) {
        mSelectItemPosition = position + 1;
        initTitle();
    }

    @Override
    public void onItemClick(View view, int position) {
        if (mFetchDataControl.mHomeEntity.is_more && position == mAdapter.getItemCount() - 1) {
            new PageIntent()
                    .toListPage(
                            mContext,
                            mFetchDataControl.mHomeEntity.channel_title,
                            mFetchDataControl.mHomeEntity.channel,
                            mFetchDataControl.mHomeEntity.style,
                            mFetchDataControl.mHomeEntity.section_slug);
        } else {
            mFetchDataControl.go2Detail(mAdapter.getmData().get(position));
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
            mTvPlayerLayoutManager.setCanScroll(true);
            if (mTvPlayerLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) { // 向左滑动
                int targetPosition = mTvPlayerLayoutManager.findFirstCompletelyVisibleItemPosition() - 4;
                if (targetPosition <= 0) targetPosition = 0;
                mSelectItemPosition = targetPosition;
/*add by dragontec for bug 4332 start*/
				setNeedCheckScrollEnd();
/*add by dragontec for bug 4332 end*/
/*modify by dragontec for bug 4332 start*/
                mTvPlayerLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
/*modify by dragontec for bug 4332 end*/
                if (mNavigationtHandler.hasMessages(NAVIGATION_LEFT)) {
                    mNavigationtHandler.removeMessages(NAVIGATION_LEFT);
                }
                    mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_LEFT,500);
            }
        } else if (i == R.id.navigation_right) { // 向右滑动
            mTvPlayerLayoutManager.setCanScroll(true);
/*modify by dragontec for bug 4332 start*/
            mRecyclerView.loadMore();
/*modify by dragontec for bug 4332 end*/
            if (mTvPlayerLayoutManager.findLastCompletelyVisibleItemPosition()
                    <= mFetchDataControl.mHomeEntity.count) {
                int targetPosition = mTvPlayerLayoutManager.findLastCompletelyVisibleItemPosition() + 4;
                if (targetPosition >= mFetchDataControl.mHomeEntity.count) {
                    targetPosition = mFetchDataControl.mHomeEntity.count;
                }
                mSelectItemPosition = targetPosition;
/*add by dragontec for bug 4332 start*/
				setNeedCheckScrollEnd();
/*add by dragontec for bug 4332 end*/
/*modify by dragontec for bug 4332 start*/
                mTvPlayerLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
/*modify by dragontec for bug 4332 end*/
                if (mNavigationtHandler.hasMessages(NAVIGATION_RIGHT)){
                    mNavigationtHandler.removeMessages(NAVIGATION_RIGHT);
                }
                mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_RIGHT, 500);
			/*delete by dragontec for bug 4303 start*/
//                if (targetPosition == mFetchDataControl.mHomeEntity.count)
//                    YoYo.with(Techniques.HorizontalShake)
//                            .duration(1000)
//                            .playOn(
//                                    mRecyclerView
//                                            .getChildAt(mRecyclerView.getChildCount() - 1)
//                                            .findViewById(R.id.tv_player_ismartv_linear_layout));
			/*delete by dragontec for bug 4303 end*/
            }
            initTitle();
        }
    }
}
