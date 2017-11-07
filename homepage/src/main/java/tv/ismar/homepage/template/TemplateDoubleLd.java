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
//import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.GridLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.open.androidtvwidget.leanback.recycle.StaggeredGridLayoutManagerTV;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

import tv.ismar.app.BaseControl;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.BigImage;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.app.widget.ListSpacesItemDecoration;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.DoubleLdAdapter;
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

/**
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: 横版双行模版
 */
public class TemplateDoubleLd extends Template
        implements BaseControl.ControlCallBack,
        OnItemClickListener,
        RecyclerViewTV.OnItemFocusChangeListener,
        RecyclerViewTV.PagingableListener,
        StaggeredGridLayoutManagerTV.FocusSearchFailedListener,
        View.OnHoverListener,
        View.OnClickListener {
    private int mSelectItemPosition = 1; // 标题--选中海报位置
    private TextView mTitleTv; // banner标题;
    private RecyclerImageView mVerticalImg; // 大图海报
    private RecyclerImageView mLtImage; // 左上角图标
    private TextView mRbImage; // 右下角图标
    private TextView mIgTitleTv; // 大图标题
/*delete by dragontec for bug 4332 start*/
//    private RecyclerViewTV mRecyclerView;
/*delete by dragontec for bug 4332 start*/
    private RecyclerImageView mRtImage;//右上角图标
    private DoubleLdAdapter mAdapter;
    private FetchDataControl mFetchDataControl = null;
    private BannerLinearLayout mBannerLinearLayout;
	/*delete by dragontec for bug 4332 start*/
//    private View navigationLeft;
//    private View navigationRight;
	/*delete by dragontec for bug 4332 end*/
	/*modify by dragontec for bug 4332 start*/
    private View mHeaderView; // recylview头view
	/*modify by dragontec for bug 4332 start*/
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

	/*modify by dragontec for bug 4334 start*/
    public TemplateDoubleLd(Context context, int position) {
        super(context, position);
        mFetchDataControl = new FetchDataControl(context, this);
        mNavigationtHandler = new NavigationtHandler();
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
        if (mNavigationtHandler.hasMessages(NAVIGATION_LEFT)){
            mNavigationtHandler.removeMessages(NAVIGATION_LEFT);
        }
        if (mNavigationtHandler.hasMessages(NAVIGATION_RIGHT)){
            mNavigationtHandler.removeMessages(NAVIGATION_RIGHT);
        }
    }

    @Override
    public void onDestroy() {
/*add by dragontec for bug 4205 start*/
        if (mFetchDataControl != null) {
            mFetchDataControl.clear();
        }
        if (mAdapter != null) {
            mAdapter.setHeaderView(null);
            mAdapter.setOnHoverListener(null);
            mAdapter.setOnItemClickListener(null);
            mAdapter.setOnItemSelectedListener(null);
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
        if (mNavigationtHandler != null){
            mNavigationtHandler = null;
        }
    }

    @Override
    public void getView(View view) {
        mTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
        mTitleCountTv = (TextView) view.findViewById(R.id.banner_title_count);
        mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.double_ld_recyclerview);
		/*modify by dragontec for bug 4221 start*/
        mRecyclerView.setTag("recycleView");
		/*modify by dragontec for bug 4221 end*/
/*modify by dragontec for bug 4332 start*/
        mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.banner_double_ld_head, null);
        mVerticalImg = (RecyclerImageView) mHeaderView.findViewById(R.id.double_ld_image_poster);
        mLtImage = (RecyclerImageView) mHeaderView.findViewById(R.id.double_ld_image_lt_icon);
        mRbImage = (TextView) mHeaderView.findViewById(R.id.double_ld_image_rb_icon);
        mIgTitleTv = (TextView) mHeaderView.findViewById(R.id.double_ld_image_title);
        mRtImage= (RecyclerImageView) mHeaderView.findViewById(R.id.guide_rt_icon);
/*modify by dragontec for bug 4332 end*/
        mDoubleLayoutManager =
                new StaggeredGridLayoutManagerTV(2, StaggeredGridLayoutManager.HORIZONTAL);
        mRecyclerView.addItemDecoration(new ListSpacesItemDecoration(mContext.getResources().getDimensionPixelOffset(R.dimen.double_ld_padding)));
        mDoubleLayoutManager.setOrientation(GridLayoutManagerTV.HORIZONTAL);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mDoubleLayoutManager);
        mRecyclerView.setSelectedItemAtCentered(false);
        mRecyclerView.setSelectedItemOffset(100, 100);
        mRecyclerView.setAdapter(new DoubleLdAdapter(mContext, new ArrayList<BannerPoster>()));
        navigationLeft = view.findViewById(R.id.navigation_left);
        navigationRight = view.findViewById(R.id.navigation_right);
        mBannerLinearLayout = (BannerLinearLayout) view.findViewById(R.id.banner_layout);
        mBannerLinearLayout.setNavigationLeft(navigationLeft);
        mBannerLinearLayout.setNavigationRight(navigationRight);
        mBannerLinearLayout.setRecyclerViewTV(mRecyclerView);
        mRecyclerView.setHasHeaderView(true);
/*add by dragontec for bug 4332 start*/
        mHoverView = view.findViewById(R.id.hover_view);
/*add by dragontec for bug 4332 end*/
    }

    private void initTitle() {
        if (mSelectItemPosition > mFetchDataControl.mHomeEntity.count)
            mSelectItemPosition = mFetchDataControl.mHomeEntity.count;
        mTitleCountTv.setText(
                String.format(
                        mContext.getString(R.string.home_item_title_count),
                        mSelectItemPosition + "",
                        mFetchDataControl.mHomeEntity.count + ""));
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
			initTitle();
			initImage();
			initRecycleView();
		}
	}

/*modify by dragontec for bug 4334 end*/

	/*modify by dragontec for bug 4334 start*/
	private void initAdapter() {
		if (mAdapter == null) {
			mAdapter = new DoubleLdAdapter(mContext);
			mAdapter.setOnItemClickListener(this);
			/*modify by dragontec for bug 4332 start*/
			mAdapter.setHeaderView(mHeaderView);
			/*modify by dragontec for bug 4332 end*/
		}
	}

    private void initRecycleView() {
		if (mAdapter != null) {
			if (mAdapter.getData() == null) {
				mAdapter.setData(mFetchDataControl.mPoster);
				mRecyclerView.setAdapter(mAdapter);
				/*add by dragontec for bug 4077 start*/
				checkFocus(mRecyclerView);
				/*add by dragontec for bug 4077 end*/
			} else {
				int start = mFetchDataControl.mPoster.size() - mFetchDataControl.mHomeEntity.posters.size();
				int end = mFetchDataControl.mPoster.size();
				mAdapter.notifyItemRangeChanged(start, end);
			}
		}
    }
    /*modify by dragontec for bug 4334 end*/

    private void initImage() {
        BigImage data = mFetchDataControl.mHomeEntity.bg_image;
        if (data != null) {
            if (!TextUtils.isEmpty(data.poster_url)) {
/*modify by dragontec for bug 4336 start*/
                Picasso.with(mContext).load(data.poster_url).
                        error(R.drawable.template_title_item_horizontal_preview).
                        placeholder(R.drawable.template_title_item_horizontal_preview).
                        into(mVerticalImg);
/*modify by dragontec for bug 4336 end*/
            } else {
/*modify by dragontec for bug 4336 start*/
                Picasso.with(mContext).
                        load(R.drawable.template_title_item_horizontal_preview).
                        into(mVerticalImg);
/*modify by dragontec for bug 4336 end*/
            }
            Picasso.with(mContext)
                    .load(VipMark.getInstance().getBannerIconMarkImage(data.top_left_corner))
                    .into(mLtImage);
            Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(data.top_right_corner)).into(mRtImage);
            mRbImage.setText(new DecimalFormat("0.0").format(data.rating_average));
            mRbImage.setVisibility((data.rating_average == 0) ? View.GONE : View.VISIBLE);
            mIgTitleTv.setText(data.title);
			/*add by dragontec for bug 卖点文字不正确的问题 start*/
            String focusStr = data.title;
            if(data.focus != null && !data.focus.equals("") && !data.focus.equals("null")){
                focusStr = data.focus;
            }
            mIgTitleTv.setTag(new String[]{data.title,focusStr});
			/*add by dragontec for bug 卖点文字不正确的问题 end*/
        }
    }

    @Override
    public void callBack(int flags, Object... args) {
        if (flags == FetchDataControl.FETCH_BANNERS_LIST_FLAG) { // 获取单个banner业务
			/*modify by dragontec for bug 4334 start*/
			isNeedFillData = true;
			initAdapter();
			checkViewAppear();
			/*modify by dragontec for bug 4334 end*/
	/* modify by dragontec for bug 4264 start */
			mRecyclerView.setOnLoadMoreComplete();
        } else {
			if (mRecyclerView.isOnLoadMore()) {
				mFetchDataControl.mHomeEntity.page--;
				mRecyclerView.setOnLoadMoreComplete();
			}
	/* modify by dragontec for bug 4264 end */
		}
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
    public void onItemFocusGain(View itemView, int position) {
        mSelectItemPosition = position + 1;
        initTitle();
    }

    /*第1个和最后一个海报抖动功能*/
    @Override
    public View onFocusSearchFailed(
            View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
    	/*modify by dragontec for bug 4242 start*/
    	if (focusDirection == View.FOCUS_LEFT) {
			boolean cannotScrollBackward = mRecyclerView.cannotScrollBackward(-1);
			if (cannotScrollBackward) {
				View view = mRecyclerView.getChildAt(mRecyclerView.getFirstCompletelyVisiblePosition() - mRecyclerView.getFirstVisiblePosition());
				if (view != null && view.findViewById(R.id.double_ld_ismartv_linear_layout) == focused) {
					YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
				}
			}
			return focused;
		}
		if (focusDirection == View.FOCUS_RIGHT) {
			boolean cannotScrollForward = mRecyclerView.cannotScrollForward(1);
			if (cannotScrollForward) {
				int[] postions = mRecyclerView.findLastCompletelyVisibleItemPositions();
				if (postions != null) {
					for (int pos :
							postions) {
						int firstPos = mRecyclerView.getFirstVisiblePosition();
						View view = mRecyclerView.getChildAt(pos - firstPos);
						if (view != null && view.findViewById(R.id.double_ld_ismartv_linear_layout) == focused) {
							YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
							break;
						}
					}
				}

			}
			return focused;
		}

//        if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
//            if (mRecyclerView.getChildAt(0).findViewById(R.id.double_ld_ismartv_linear_layout) == focused
//                    || mRecyclerView
//                    .getChildAt(mRecyclerView.getChildCount() - 1)
//                    .findViewById(R.id.double_ld_ismartv_linear_layout)
//                    == focused) {
//                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
//            }
//            return focused;
//        }
		/*modify by dragontec for bug 4242 end*/
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
        Log.i(TAG, "onLoadMoreItems");
        Log.i("RecyclerViewTV", "onLoadMoreItems");
        HomeEntity homeEntity = mFetchDataControl.mHomeEntity;
        if (homeEntity != null) {
            if (homeEntity.page < homeEntity.num_pages) {
	/* modify by dragontec for bug 4264 start */
                mFetchDataControl.fetchBanners(mBannerPk, ++homeEntity.page, true);
            } else {
				mRecyclerView.setOnLoadMoreComplete();
	/* modify by dragontec for bug 4264 end */
			}
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        int[] positions = new int[]{0, 0};
        Log.i("onClick", "positions[0]:" + positions[0] + "positions[1]:" + positions[1]);
        if (i == R.id.navigation_left) {
            mDoubleLayoutManager.findFirstCompletelyVisibleItemPositions(positions);
            mDoubleLayoutManager.setCanScroll(true);
            if (positions[1] - 1 >= 0) { // 向左滑动
                int targetPosition = positions[1] - 8;
                if (targetPosition <= 0) targetPosition = 0;
                mSelectItemPosition = targetPosition;
/*add by dragontec for bug 4332 start*/
				setNeedCheckScrollEnd();
/*add by dragontec for bug 4332 end*/
                mDoubleLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
                if (mNavigationtHandler.hasMessages(NAVIGATION_LEFT)) {
                    mNavigationtHandler.removeMessages(NAVIGATION_LEFT);
                }
                    mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_LEFT,500);
            }
        } else if (i == R.id.navigation_right) { // 向右滑动
            mDoubleLayoutManager.findLastCompletelyVisibleItemPositions(positions);
            mDoubleLayoutManager.setCanScroll(true);
            mRecyclerView.setloadMoreType(true);
            mRecyclerView.loadMore();
            if (positions[1] <= mFetchDataControl.mHomeEntity.count) {
                int targetPosition = positions[1] + 8;
                if (targetPosition >= mFetchDataControl.mHomeEntity.count) {
                    targetPosition = mFetchDataControl.mHomeEntity.count;
                }
                mSelectItemPosition = targetPosition;
/*add by dragontec for bug 4332 start*/
				setNeedCheckScrollEnd();
/*add by dragontec for bug 4332 end*/
                mDoubleLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
                if (mNavigationtHandler.hasMessages(NAVIGATION_RIGHT)){
                    mNavigationtHandler.removeMessages(NAVIGATION_RIGHT);
                }
                mNavigationtHandler.sendEmptyMessageDelayed(NAVIGATION_RIGHT, 500);

			/*delete by dragontec for bug 4303 start*/
//                try {
//                    if (targetPosition == mFetchDataControl.mHomeEntity.count)
//                        YoYo.with(Techniques.HorizontalShake)
//                                .duration(1000)
//                                .playOn(
//                                        mRecyclerView
//                                                .getChildAt(mRecyclerView.getChildCount() - 1)
//                                                .findViewById(R.id.double_md_ismartv_linear_layout));
//                }catch(Exception e){
//                    e.printStackTrace();
//                }
			/*delete by dragontec for bug 4303 end*/
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
//                    HomeActivity.mHoverView.requestFocus(); // 将焦点放置到一块隐藏view中
                    v.clearFocus();
/*modify by dragontec for bug 4057 end*/
                }
                break;
        }
        return false;
    }
}
