/*modify by dragontec for bug 4362 start*/
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
import android.view.KeyEvent;
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
import tv.ismar.homepage.OnItemKeyListener;
import tv.ismar.homepage.OnItemSelectedListener;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.DoubleLdAdapter;
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
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: 横版双行模版
 */
public class TemplateDoubleLd extends Template
        implements BaseControl.ControlCallBack,
        OnItemClickListener,
		OnItemKeyListener,
        RecyclerViewTV.OnItemFocusChangeListener,
        RecyclerViewTV.PagingableListener,
        StaggeredGridLayoutManagerTV.FocusSearchFailedListener,
        View.OnHoverListener,
        View.OnClickListener {
    private int mSelectItemPosition = 1; // 标题--选中海报位置
    private TextView mTitleTv; // banner标题;
/*delete by dragontec for bug 4332 start*/
//    private RecyclerViewTV mRecyclerView;
/*delete by dragontec for bug 4332 start*/
    private DoubleLdAdapter mAdapter;
    private BannerLinearLayout mBannerLinearLayout;
	/*delete by dragontec for bug 4332 start*/
//    private View navigationLeft;
//    private View navigationRight;
	/*delete by dragontec for bug 4332 end*/
    private StaggeredGridLayoutManagerTV mDoubleLayoutManager;
    private String mName; // 频道名称（中文）
    private String mChannel; // 频道名称（英文）
    private int locationY;

    private static final int NAVIGATION_LEFT = 0x0001;
    private static final int NAVIGATION_RIGHT = 0x0002;

	/*modify by dragontec for bug 4334 start*/
    public TemplateDoubleLd(Context context, int position, FetchDataControl fetchDataControl) {
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
        mDoubleLayoutManager =
                new StaggeredGridLayoutManagerTV(2, StaggeredGridLayoutManager.HORIZONTAL);
		mDoubleLayoutManager.setOrientation(GridLayoutManagerTV.HORIZONTAL);
		mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.double_ld_recyclerview);
		mRecyclerView.setTag("recycleView");
        mRecyclerView.addItemDecoration(new ListSpacesItemDecoration(mContext.getResources().getDimensionPixelOffset(R.dimen.double_ld_padding)));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mDoubleLayoutManager);
        mRecyclerView.setSelectedItemAtCentered(false);
		/*modify by dragontec for bug 4434 start*/
        int selectedItemOffset =
                mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemDoubleOffset);
        mRecyclerView.setSelectedItemOffset(100, 100);
		/*modify by dragontec for bug 4434 end*/
        mRecyclerView.setAdapter(new DoubleLdAdapter(mContext, new ArrayList<BannerPoster>()));
		mRecyclerView.setHasHeaderView(true);
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
			mRecyclerView.setItemAnimator(null);
			mRecyclerView = null;
		}
		mDoubleLayoutManager = null;
		mTitleCountTv = null;
		mTitleTv = null;
	}

	private void initTitle() {
    	if (mFetchControl.getHomeEntity(mBannerPk) != null)
        if (mSelectItemPosition > mFetchControl.getHomeEntity(mBannerPk).count)
            mSelectItemPosition = mFetchControl.getHomeEntity(mBannerPk).count;
        mTitleCountTv.setText(
                String.format(
                        mContext.getString(R.string.home_item_title_count),
                        mSelectItemPosition + "",
						mFetchControl.getHomeEntity(mBannerPk).count + ""));
    }

    @Override
    protected void initListener(View view) {
        super.initListener(view);
        navigationLeft.setOnClickListener(this);
        navigationRight.setOnClickListener(this);
//        navigationRight.setOnHoverListener(this);
//        navigationLeft.setOnHoverListener(this);
        mRecyclerView.setPagingableListener(this);
        mRecyclerView.setOnItemFocusChangeListener(this);
        mDoubleLayoutManager.setFocusSearchFailedListener(this);
    }

	@Override
	protected void unInitListener() {
    	mDoubleLayoutManager.setFocusSearchFailedListener(null);
    	mRecyclerView.setOnItemFocusChangeListener(null);
    	mRecyclerView.setPagingableListener(null);
    	navigationRight.setOnClickListener(null);
    	navigationLeft.setOnClickListener(null);
		super.unInitListener();
	}

	@Override
    public void initData(Bundle bundle) {
    	initAdapter();
        mTitleTv.setText(bundle.getString(TITLE_KEY));
        mBannerPk = bundle.getString(BANNER_KEY);
        mName = bundle.getString("title");
        mChannel = bundle.getString(CHANNEL_KEY);
        locationY=bundle.getInt(BANNER_LOCATION,0);
        mTitleCountTv.setText("00/00");
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
	}

	@Override
	public void fillData() {
    	if (isNeedFillData) {
			isNeedFillData = false;
			initTitle();
			initRecycleView();
		}
	}

/*modify by dragontec for bug 4334 end*/

	/*modify by dragontec for bug 4334 start*/
	private void initAdapter() {
		if (mAdapter == null) {
			mAdapter = new DoubleLdAdapter(mContext);
			mAdapter.setOnItemClickListener(this);
			mAdapter.setOnItemKeyListener(this);
		}
	}

	private void unInitAdapter() {
		if (mAdapter != null) {
			mRecyclerView.setAdapter(null);
			mAdapter.setOnItemKeyListener(null);
			mAdapter.setOnItemClickListener(null);
			mAdapter.setBigImage(null);
			mAdapter.clearData();
			mAdapter = null;
		}
	}

    private void initRecycleView() {
		if (mAdapter != null) {
			if (mAdapter.getData() == null) {
				if (mFetchControl.getHomeEntity(mBannerPk) != null) {
					mAdapter.setBigImage(mFetchControl.getHomeEntity(mBannerPk).bg_image);
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
				if (mAdapter.getBigImage() != null) {
					start++;
					end++;
				}
				mAdapter.notifyItemRangeInserted(start, end - start + 1);
			}
		}
    }
    /*modify by dragontec for bug 4334 end*/

//    @Override
//    public void callBack(int flags, Object... args) {
//    	switch (flags) {
//			case FetchDataControl.FETCH_BANNERS_LIST_FLAG:
//			{
//				/*modify by dragontec for bug 4334 start*/
//				isNeedFillData = true;
//				initAdapter();
//				checkViewAppear();
//				/*modify by dragontec for bug 4334 end*/
//				mRecyclerView.setOnLoadMoreComplete();
//			}
//			break;
//			case BaseControl.FETCH_DATA_FAIL_FLAG: {
//				if (mRecyclerView.isOnLoadMore()) {
//					mFetchControl.getHomeEntity(mBannerPk).page--;
//					mRecyclerView.setOnLoadMoreComplete();
//				}
//			}
//			break;
//		}
//    }

    @Override
    public void onItemClick(View view, int position) {
        if (position == 0) { // 第一张大图
            mFetchControl.go2Detail(mFetchControl.getHomeEntity(mBannerPk).bg_image);
            BigImage entity=mFetchControl.getHomeEntity(mBannerPk).bg_image;
            mFetchControl.launcher_vod_click(entity.model_name,entity.pk+"",entity.title,(locationY-1)+",1",mChannel);
        } else if (mFetchControl.getHomeEntity(mBannerPk).is_more && position == mAdapter.getItemCount() - 1) {
            new PageIntent()
                    .toListPage(
                            mContext,
							mFetchControl.getHomeEntity(mBannerPk).channel_title,
                            mFetchControl.getHomeEntity(mBannerPk).channel,
							Integer.valueOf(mFetchControl.getHomeEntity(mBannerPk).style),
                            mFetchControl.getHomeEntity(mBannerPk).section_slug);
            mFetchControl.launcher_vod_click("section","-1","更多",locationY+",1",mChannel);
        } else {
            mFetchControl.go2Detail(mAdapter.getmData().get(position - 1));
            int Y=locationY;
            if(position%2!=0){
                Y=locationY-1;
            }
            int locationX=(position+1)/2+1;
            BannerPoster poster=mAdapter.getData().get(position);
            if(position<mAdapter.getItemCount())
                mFetchControl.launcher_vod_click(poster.model_name,poster.pk+"",poster.title,Y+","+locationX,mChannel);
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
        HomeEntity homeEntity = mFetchControl.getHomeEntity(mBannerPk);
        if (homeEntity != null) {
            if (homeEntity.page < homeEntity.num_pages) {
	/* modify by dragontec for bug 4264 start */
                mFetchControl.fetchBanners(mBannerPk, ++homeEntity.page, true);
            } else {
				mRecyclerView.setOnLoadMoreComplete();
	/* modify by dragontec for bug 4264 end */
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
			int position = mRecyclerView.getFirstCompletelyVisiblePosition();
			if (position > 0) {
				mDoubleLayoutManager.setCanScroll(true);
				int targetPosition = position - 8;
				if (targetPosition < 0) {
					targetPosition = 0;
				}
				mSelectItemPosition = targetPosition + 1;
				setNeedCheckScrollEnd();
				mDoubleLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
				initTitle();
			}
//            int[] positions = mDoubleLayoutManager.findFirstCompletelyVisibleItemPositions(null);
//            mDoubleLayoutManager.setCanScroll(true);
//            if (positions[1] - 1 >= 0) { // 向左滑动
//                int targetPosition = positions[1] - 8;
//                if (targetPosition <= 0) targetPosition = 0;
//                mSelectItemPosition = targetPosition + 1;
///*add by dragontec for bug 4332 start*/
//				setNeedCheckScrollEnd();
///*add by dragontec for bug 4332 end*/
//                mDoubleLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
//                initTitle();
//            }
        } else if (i == R.id.navigation_right) { // 向右滑动
			if (!mRecyclerView.isNotScrolling()) {
				return;
			}
			int position = mRecyclerView.findLastCompletelyVisibleItemPosition();
			if (position <= mFetchControl.getHomeEntity(mBannerPk).count) {
				mDoubleLayoutManager.setCanScroll(true);
				mRecyclerView.setloadMoreType(true);
				mRecyclerView.loadMore();
				int targetPosition = position + 8;
				if (targetPosition > mDoubleLayoutManager.getItemCount() - 1) {
					targetPosition = mDoubleLayoutManager.getItemCount() - 1;
				}
				mSelectItemPosition = targetPosition + 1;
				setNeedCheckScrollEnd();
				mDoubleLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
				initTitle();
			}
//            int[] positions = mDoubleLayoutManager.findLastCompletelyVisibleItemPositions(null);
//            mDoubleLayoutManager.setCanScroll(true);
//            mRecyclerView.setloadMoreType(true);
//            if (positions[1] <= mFetchControl.getHomeEntity(mBannerPk).count) {
//				mRecyclerView.loadMore();
//                int targetPosition = positions[1] + 8;
//				if (targetPosition > mDoubleLayoutManager.getItemCount() - 1) {
//					targetPosition = mDoubleLayoutManager.getItemCount() - 1;
//				}
//				mSelectItemPosition = targetPosition + 1;
///*add by dragontec for bug 4332 start*/
//				setNeedCheckScrollEnd();
///*add by dragontec for bug 4332 end*/
//                mDoubleLayoutManager.smoothScrollToPosition(mRecyclerView, null, targetPosition);
//
//			/*delete by dragontec for bug 4303 start*/
////                try {
////                    if (targetPosition == mFetchDataControl.mHomeEntity.count)
////                        YoYo.with(Techniques.HorizontalShake)
////                                .duration(1000)
////                                .playOn(
////                                        mRecyclerView
////                                                .getChildAt(mRecyclerView.getChildCount() - 1)
////                                                .findViewById(R.id.double_md_ismartv_linear_layout));
////                }catch(Exception e){
////                    e.printStackTrace();
////                }
//			/*delete by dragontec for bug 4303 end*/
//				initTitle();
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