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

import java.util.List;

import tv.ismar.app.BaseControl;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.OnItemClickListener;
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
        View.OnHoverListener,
        View.OnClickListener {
    private RecyclerViewTV mRecyclerView;
    private LinearLayoutManagerTV mConlumnLayoutManager;
    private ConlumnAdapter mAdapter;
    private FetchDataControl mFetchDataControl = null;
    private BannerLinearLayout mBannerLinearLayout;
    private View navigationLeft;
    private View navigationRight;
    private String mBannerPk;
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


    public TemplateConlumn(Context context) {
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
        if (mFetchDataControl != null){
            mFetchDataControl.stop();
        }
	/*add by dragontec for bug 4077 start*/
		super.onPause();
	/*add by dragontec for bug end start*/
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
        mTitleCountTv = (TextView) view.findViewById(R.id.banner_title_count);
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.conlumn_recyclerview);
		/*modify by dragontec for bug 4221 start*/
        mRecyclerView.setTag("recycleView");
		/*modify by dragontec for bug 4221 end*/
        mConlumnLayoutManager =
                new LinearLayoutManagerTV(mContext, LinearLayoutManager.HORIZONTAL, false);
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
    }

    @Override
    public void initData(Bundle bundle) {
        mBannerPk = bundle.getString(ChannelFragment.BANNER_KEY);
        mName = bundle.getString(NAME_KEY);
        mChannel = bundle.getString(CHANNEL_KEY);
/*modify by dragontec for bug 4200 start*/
    }

	@Override
	public void fetchData() {
		hasAppeared = true;
		mFetchDataControl.fetchBanners(mBannerPk, 1, false);
	}
/*modify by dragontec for bug 4200 end*/

    @Override
    protected void initListener(View view) {
        navigationLeft.setOnClickListener(this);
        navigationRight.setOnClickListener(this);
        navigationRight.setOnHoverListener(this);
        navigationLeft.setOnHoverListener(this);
        mRecyclerView.setPagingableListener(this);
        mConlumnLayoutManager.setFocusSearchFailedListener(this);
    }

    @Override
    public void callBack(int flags, Object... args) {
        if (flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG) { // 获取单个banner业务
            initRecycleView(mFetchDataControl.mPoster);
        }
    }

    private void initRecycleView(List<BannerPoster> posters) {
        if (mAdapter == null) {
            mAdapter = new ConlumnAdapter(mContext, posters);
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
        BannerPoster poster = mFetchDataControl.mHomeEntity.posters.get(position);
        if(poster.model_name.contains("item")){
            if(poster.content_model.contains("gather")){
                new PageIntent().toSubject(mContext,poster.content_model,poster.pk,poster.title,"homepage",poster.channel);
            }
        }else if(poster.model_name.equals("section")) {
            new PageIntent().toListPage(mContext,poster.title,poster.channel,mFetchDataControl.mHomeEntity.style,poster.slug);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.navigation_left) {
            mConlumnLayoutManager.setCanScroll(true);
            if (mConlumnLayoutManager.findFirstCompletelyVisibleItemPosition() - 1 >= 0) { // 向左滑动
                int targetPosition = mConlumnLayoutManager.findFirstCompletelyVisibleItemPosition() - 4;
                if (targetPosition <= 0) targetPosition = 0;
                mConlumnLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
                if (mNavigationtHandler.hasMessages(NAVIGATION_LEFT)) {
                    mNavigationtHandler.removeMessages(NAVIGATION_LEFT);
                }
                    mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_LEFT,500);
            }
        } else if (i == R.id.navigation_right) { // 向右滑动
            mConlumnLayoutManager.setCanScroll(true);
            mRecyclerView.loadMore();
            if (mConlumnLayoutManager.findLastCompletelyVisibleItemPosition()
                    <= mFetchDataControl.mHomeEntity.count) {
                int targetPosition = mConlumnLayoutManager.findLastCompletelyVisibleItemPosition() + 4;
                if (targetPosition >= mFetchDataControl.mHomeEntity.count) {
                    targetPosition = mFetchDataControl.mHomeEntity.count;
                }
                mConlumnLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
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
//                                            .findViewById(R.id.conlumn_ismartv_linear_layout));
			/*delete by dragontec for bug 4303 end*/
            }
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
//                    HomeActivity.mHoverView.requestFocus(); // 将焦点放置到一块隐藏view中
                    v.clearFocus();
/*modify by dragontec for bug 4057 end*/
                }
                break;
        }
        return false;
    }
}
