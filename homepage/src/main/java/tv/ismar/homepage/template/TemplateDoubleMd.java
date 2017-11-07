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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
//import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.open.androidtvwidget.leanback.recycle.StaggeredGridLayoutManagerTV;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

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
import tv.ismar.homepage.adapter.DoubleMdAdapter;
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

/** @AUTHOR: xi @DATE: 2017/8/29 @DESC: 竖版双行模版 */
public class TemplateDoubleMd extends Template
    implements BaseControl.ControlCallBack,
        OnItemClickListener,
        RecyclerViewTV.PagingableListener,
        RecyclerViewTV.OnItemFocusChangeListener,
        StaggeredGridLayoutManagerTV.FocusSearchFailedListener,
        View.OnClickListener,
        View.OnHoverListener {
  private TextView mTitleTv; // banner标题
  private RecyclerImageView mVerticalImg; // 大图海报
  private RecyclerImageView mLtImage; // 左上角图标
  private RecyclerImageView mRbImage; // 右下角图标
  private TextView mImgeTitleTv; // 大图标题
  private RecyclerImageView mRtImage;//右上图标
/*delete by dragontec for bug 4332 start*/
//  private RecyclerViewTV mRecyclerView;
/*delete by dragontec for bug 4332 end*/
  private DoubleMdAdapter mAdapter;
  private int mSelectItemPosition = 1; // 标题--选中海报位置
  private BannerLinearLayout mBannerLinearLayout;
	/*modify by dragontec for bug 4332 start*/
  private View mHeaderView; // recylview头view
	/*modify by dragontec for bug 4332 end*/
  private StaggeredGridLayoutManagerTV mDoubleLayoutManager;
  private String mName; // 频道名称（中文）
  private String mChannel; // 频道名称（英文）

  private static final int NAVIGATION_LEFT = 0x0001;
  private static final int NAVIGATION_RIGHT = 0x0002;

  private NavigationtHandler mNavigationtHandler;

  private class NavigationtHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
/*delete by dragontec for bug 4332 start*/
//      switch (msg.what){
//        case NAVIGATION_LEFT:
//          if (mRecyclerView!=null&&!mRecyclerView.cannotScrollBackward(-10)) {
//            navigationLeft.setVisibility(VISIBLE);
//          }else if (mRecyclerView!=null){
//            navigationLeft.setVisibility(INVISIBLE);
//          }
//          break;
//        case NAVIGATION_RIGHT:
//          if(mRecyclerView!=null&&!mRecyclerView.cannotScrollForward(10)){
//            navigationRight.setVisibility(VISIBLE);
//          }else if (mRecyclerView!=null){
//            navigationRight.setVisibility(INVISIBLE);
//          }
//          break;
//      }
/*delete by dragontec for bug 4332 end*/
    }
  }

  public TemplateDoubleMd(Context context, int position, FetchDataControl fetchDataControl) {
    super(context, position, fetchDataControl);
    mNavigationtHandler = new NavigationtHandler();
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
    if (mAdapter != null) {
      mAdapter.setHeaderView(null);
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
    if (mNavigationtHandler != null){
      mNavigationtHandler = null;
    }
	  super.onDestroy();
  }

  @Override
  public void getView(View view) {
    mTitleTv = (TextView) view.findViewById(R.id.banner_title_tv);
    mTitleCountTv = (TextView) view.findViewById(R.id.banner_title_count);
    mRecyclerView = (RecyclerViewTV) view.findViewById(R.id.double_md_recyclerview);
	/*modify by dragontec for bug 4221 start*/
    mRecyclerView.setTag("recycleView");
	/*modify by dragontec for bug 4221 end*/
/*modify by dragontec for bug 4332 start*/
    mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.banner_double_md_head, null);
    mVerticalImg = (RecyclerImageView) mHeaderView.findViewById(R.id.double_md_image_poster);
    mLtImage = (RecyclerImageView) mHeaderView.findViewById(R.id.double_md_image_lt_icon);
    mRbImage = (RecyclerImageView) mHeaderView.findViewById(R.id.double_md_image_rb_icon);
    mImgeTitleTv = (TextView) mHeaderView.findViewById(R.id.double_md_image_title);
/*modify by dragontec for bug 4332 end*/
    mRtImage= (RecyclerImageView) mHeaderView.findViewById(R.id.guide_rt_icon);
    mDoubleLayoutManager =
        new StaggeredGridLayoutManagerTV(2, StaggeredGridLayoutManager.HORIZONTAL);
    mRecyclerView.addItemDecoration(new ListSpacesItemDecoration(mContext.getResources().getDimensionPixelOffset(R.dimen.double_md_padding)));
    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    mRecyclerView.setLayoutManager(mDoubleLayoutManager);
    mRecyclerView.setSelectedItemAtCentered(false);
    mRecyclerView.setHasHeaderView(true);
    int selectedItemOffset =
        mContext.getResources().getDimensionPixelSize(R.dimen.banner_item_setSelectedItemOffset);
    mRecyclerView.setSelectedItemOffset(100, 100);
    /*delete by dragontec for bug 4334 start*/
//    mRecyclerView.setAdapter(new DoubleMdAdapter(mContext, new ArrayList<BannerPoster>()));
	/*delete by dragontec for bug 4334 end*/
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
  protected void initListener(View view) {
    super.initListener(view);
    navigationLeft.setOnClickListener(this);
    navigationRight.setOnClickListener(this);
//    navigationRight.setOnHoverListener(this);
//    navigationLeft.setOnHoverListener(this);
    mRecyclerView.setPagingableListener(this);
    mRecyclerView.setOnItemFocusChangeListener(this);
    mDoubleLayoutManager.setFocusSearchFailedListener(this);
  }

  @Override
  public void initData(Bundle bundle) {
  	initAdapter();
    mTitleTv.setText(bundle.getString(TITLE_KEY));
    mBannerPk = bundle.getString(BANNER_KEY);
    mName = bundle.getString(NAME_KEY);
    mChannel = bundle.getString(CHANNEL_KEY);
    mTitleCountTv.setText("00/00");
/*modify by dragontec for bug 4334 start*/
	if (mFetchControl.getHomeEntity(mBannerPk) != null) {
		isNeedFillData = true;
		checkViewAppear();
	}
  }

	@Override
	public void fetchData() {
		hasAppeared = true;
	}

	@Override
	public void fillData() {
  		if (isNeedFillData) {
			isNeedFillData= false;
			initTitle();
			initImage();
			initRecycleView();
		}
	}

/*modify by dragontec for bug 4334 end*/

  private void initTitle() {
  	if (mFetchControl.getHomeEntity(mBannerPk) != null) {
		if (mSelectItemPosition > mFetchControl.getHomeEntity(mBannerPk).count)
			mSelectItemPosition = mFetchControl.getHomeEntity(mBannerPk).count;
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
		mAdapter = new DoubleMdAdapter(mContext);
		mAdapter.setOnItemClickListener(this);
/*modify by dragontec for bug 4332 start*/
		mAdapter.setHeaderView(mHeaderView);
/*modify by dragontec for bug 4332 end*/
	}
  }

  private void initRecycleView() {
  	if (mAdapter != null) {
  		if (mAdapter.getData() == null) {
  			if (mFetchControl.mPosterMap.get(mBannerPk) != null) {
				mAdapter.setData(mFetchControl.mPosterMap.get(mBannerPk));
				mRecyclerView.setAdapter(mAdapter);
	/*add by dragontec for bug 4077 start*/
				checkFocus(mRecyclerView);
	/*add by dragontec for bug 4077 end*/
			}
		} else {
			int start = mFetchControl.mPosterMap.get(mBannerPk).size() - mFetchControl.getHomeEntity(mBannerPk).posters.size();
			int end = mFetchControl.mPosterMap.get(mBannerPk).size();
			mAdapter.notifyItemRangeChanged(start, end);
		}
	}
  }
  /*modify by dragontec for bug 4334 end*/

  private void initImage() {
    BigImage data = mFetchControl.getHomeEntity(mBannerPk).bg_image;
    if (data != null) {
      if (!TextUtils.isEmpty(data.vertical_url)) {
/*modify by dragontec for bug 4336 start*/
        Picasso.with(mContext).load(data.vertical_url).
                error(R.drawable.template_title_item_vertical_preview).
                placeholder(R.drawable.template_title_item_vertical_preview).
                into(mVerticalImg);
/*modify by dragontec for bug 4336 end*/
      } else {
/*modify by dragontec for bug 4336 start*/
        Picasso.with(mContext).
                load(R.drawable.template_title_item_vertical_preview).
                into(mVerticalImg);
/*modify by dragontec for bug 4336 end*/
      }
      Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(data.top_right_corner)).into(mRtImage);
      //        Picasso.with(mContext).load(data.poster_url).into(mLtImage);
      //        Picasso.with(mContext).load(data.poster_url).into(mRbImage);
      mImgeTitleTv.setText(data.title);
		/*add by dragontec for bug 卖点文字不正确的问题 start*/
      String focusStr = data.title;
      if(data.focus != null && !data.focus.equals("") && !data.focus.equals("null")){
        focusStr = data.focus;
      }
      mImgeTitleTv.setTag(new String[]{data.title,focusStr});
	  /*add by dragontec for bug 卖点文字不正确的问题 end*/
    }
  }

//  @Override
//  public void callBack(int flags, Object... args) {
//  	switch (flags) {
//		case FetchDataControl.FETCH_BANNERS_LIST_FLAG:
//		{
//			/*modify by dragontec for bug 4334 start*/
//			isNeedFillData = true;
//			initAdapter();
//			checkViewAppear();
//      		/*modify by dragontec for bug 4334 end*/
//			mRecyclerView.setOnLoadMoreComplete();
//		}
//		break;
//		case FetchDataControl.FETCH_DATA_FAIL_FLAG:
//		{
//			if (mRecyclerView.isOnLoadMore()) {
//				mFetchControl.getHomeEntity(mBannerPk).page--;
//				mRecyclerView.setOnLoadMoreComplete();
//			}
//		}
//		break;
//	}
//  }

  @Override
  public void onItemFocusGain(View itemView, int position) {
    mSelectItemPosition = position + 1;
    initTitle();
  }

  @Override
  public void onItemClick(View view, int position) {
    if (position == 0) { // 第一张大图
      mFetchControl.go2Detail(mFetchControl.getHomeEntity(mBannerPk).bg_image);
    } else if (mFetchControl.getHomeEntity(mBannerPk).is_more && position == mAdapter.getItemCount() - 1) {
      new PageIntent()
          .toListPage(
              mContext,
              mFetchControl.getHomeEntity(mBannerPk).channel_title,
              mFetchControl.getHomeEntity(mBannerPk).channel,
              mFetchControl.getHomeEntity(mBannerPk).style,
                  mFetchControl.getHomeEntity(mBannerPk).section_slug);
    } else {
		mFetchControl.go2Detail(mAdapter.getmData().get(position - 1));
    }
  }

  @Override
  public View onFocusSearchFailed(
      View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
  	/*modify by dragontec for bug 4242 start*/
	  if (focusDirection == View.FOCUS_LEFT) {
	  	boolean cannotScrollBackward = mRecyclerView.cannotScrollBackward(-1);
		  if (cannotScrollBackward) {
			  View view = mRecyclerView.getChildAt(mRecyclerView.getFirstCompletelyVisiblePosition() - mRecyclerView.getFirstVisiblePosition());
			  if (view != null && view.findViewById(R.id.double_md_ismartv_linear_layout) == focused) {
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
					  if (view != null && view.findViewById(R.id.double_md_ismartv_linear_layout) == focused) {
						  YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
						  break;
					  }
				  }
			  }
		  }
		  return focused;
	  }
//    if (focusDirection == View.FOCUS_RIGHT || focusDirection == View.FOCUS_LEFT) {
//      if (mRecyclerView.getChildAt(0).findViewById(R.id.double_md_ismartv_linear_layout) == focused
//          || mRecyclerView
//                  .getChildAt(mRecyclerView.getChildCount() - 1)
//                  .findViewById(R.id.double_md_ismartv_linear_layout)
//              == focused) {
//        YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
//      }
//      return focused;
//    }
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
    int[] positions = new int[] {0, 0};
    Log.i("onClick", "positions[0]:" + positions[0] + "positions[1]:" + positions[1]);
    if (i == R.id.navigation_left) {
      mDoubleLayoutManager.findFirstCompletelyVisibleItemPositions(positions);
      mDoubleLayoutManager.setCanScroll(true);
      if (positions[1] - 1 >= 0) { // 向左滑动
        int targetPosition = positions[1] - 12;
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
      mRecyclerView.loadMore();
      if (positions[1] <= mFetchControl.getHomeEntity(mBannerPk).count) {
        int targetPosition = positions[1] + 12;
        if (targetPosition >= mFetchControl.getHomeEntity(mBannerPk).count) {
          targetPosition = mFetchControl.getHomeEntity(mBannerPk).count;
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
//        if (targetPosition == mFetchControl.getHomeEntity(mBannerPk).count)
//          YoYo.with(Techniques.HorizontalShake)
//              .duration(1000)
//              .playOn(
//                  mRecyclerView
//                      .getChildAt(mRecyclerView.getChildCount() - 1)
//                      .findViewById(R.id.double_md_ismartv_linear_layout));
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
}
/*modify by dragontec for bug 4362 end*/
