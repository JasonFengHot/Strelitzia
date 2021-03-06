package tv.ismar.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.StaggeredGridLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.impl.PrvInterface;
import com.open.androidtvwidget.utils.OPENLOG;

import java.util.ArrayList;

/**
 * RecyclerView TV适配版本.
 * https://github.com/zhousuqiang/TvRecyclerView(参考源码)
 */
public class HistoryRecyclerViewTV extends RecyclerView{
	/*add by dragontec for bug 4264 start*/
	private final String TAG = this.getClass().getSimpleName();
	/*add by dragontec for bug 4264 end*/
    private int firstCompletelyVisiblePosition;
    /*add by dragontec for bug 4221 start*/
    private View lastFocusChild;
    private long lastKeyEventTime = 0;
    /*add by dragontec for bug 4221 end*/

    public HistoryRecyclerViewTV(Context context) {
        this(context, null);
    }

    public HistoryRecyclerViewTV(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public HistoryRecyclerViewTV(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private View mItemView;
    private boolean mSelectedItemCentered = true;
    private int mSelectedItemOffsetStart;
    private int mSelectedItemOffsetEnd;
    private int position = 0;
    private OnItemListener mOnItemListener;
    private OnItemClickListener mOnItemClickListener; // item 单击事件.
    private ItemListener mItemListener;
    private int offset = -1;
    private boolean ishover=false;
    private boolean hasHeaderView=false;
	/*add by dragontec for bug 4265 start*/
    private int mScrollState = 0;
	/*add by dragontec for bug 4265 end*/

    private HistoryRecyclerViewTV.OnChildViewHolderSelectedListener mChildViewHolderSelectedListener;

    private void init(Context context) {
    	setDrawingCacheEnabled(true);
    	setDrawingCacheQuality(DRAWING_CACHE_QUALITY_HIGH);

        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setHasFixedSize(true);
        setWillNotDraw(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setChildrenDrawingOrderEnabled(true);
        //
        setClipChildren(false);
        setClipToPadding(false);

        setClickable(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        //
        mItemListener = new ItemListener() {
            /**
             * 子控件的点击事件
             * @param itemView
             */
            @Override
            public void onClick(View itemView) {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(HistoryRecyclerViewTV.this, itemView, getChildLayoutPosition(itemView));
                }
            }

            /**
             * 子控件的焦点变动事件
             * @param itemView
             * @param hasFocus
             */
            @Override
            public void onFocusChange(View itemView, boolean hasFocus) {
                if (null != mOnItemListener) {
                    if (null != itemView) {
                        mItemView = itemView; // 选中的item.
                        itemView.setSelected(hasFocus);
                        if (hasFocus) {
                            mOnItemListener.onItemSelected(HistoryRecyclerViewTV.this, itemView, getChildLayoutPosition(itemView));
                        } else {
                            mOnItemListener.onItemPreSelected(HistoryRecyclerViewTV.this, itemView, getChildLayoutPosition(itemView));
                        }
                    }
                }
            }
        };
    }

    private int getFreeWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getFreeHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    @Override
    public void onChildAttachedToWindow(View child) {
        // 设置单击事件，修复.
        if (!child.hasOnClickListeners()) {
            child.setOnClickListener(mItemListener);
        }
        // 设置焦点事件，修复.
        if (child.getOnFocusChangeListener() == null) {
            child.setOnFocusChangeListener(mItemListener);
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        OPENLOG.D("gainFocus:" + gainFocus + " ,direction=" + direction);
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public boolean hasFocus() {
        OPENLOG.D("hasFocus");
        return super.hasFocus();
    }

    @Override
    public boolean isInTouchMode() {
        // 解决4.4版本抢焦点的问题
        if (Build.VERSION.SDK_INT == 19) {
            return !(hasFocus() && !super.isInTouchMode());
        } else {
            return super.isInTouchMode();
        }
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        // 一行的选中.
        if (mChildViewHolderSelectedListener != null) {
            int pos = getPositionByView(child);
            ViewHolder vh = getChildViewHolder(child);
            mChildViewHolderSelectedListener.onChildViewHolderSelected(this, vh, pos);
        }
        //
        if (null != child) {
            /*add by dragontec for bug 4221 start*/
            lastFocusChild = child;
            /*add by dragontec for bug 4221 end*/
            if (mSelectedItemCentered) {
                mSelectedItemOffsetStart = !isVertical() ? (getFreeWidth() - child.getWidth()) : (getFreeHeight() - child.getHeight());
                mSelectedItemOffsetStart /= 2;
                mSelectedItemOffsetEnd = mSelectedItemOffsetStart;
            }
        }
        super.requestChildFocus(child, focused);
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        Log.i("recyclerViewTV","ishoverd: "+isHovered());
        if (isHovered()){
            return true;
        }
        if (mOnItemFocusChangeListener != null){
            mOnItemFocusChangeListener.onItemFocusGain(child, getPositionByView(child));
        }

        final int parentLeft = getPaddingLeft();
        final int parentTop = getPaddingTop();
        final int parentRight = getWidth() - getPaddingRight();
        final int parentBottom = getHeight() - getPaddingBottom();
		/*modify by dragontec for bug 4365 start*/
        final int childLeft = child.getLeft() + rect.left - getCenterSpace();
		/*modify by dragontec for bug 4365 end*/
        final int childTop = child.getTop() + rect.top;

//        final int childLeft = child.getLeft() + rect.left - child.getScrollX();
//        final int childTop = child.getTop() + rect.top - child.getScrollY();
		/*modify by dragontec for bug 4365 start*/
        final int childRight = childLeft + rect.width() + getCenterSpace() * 2;
		/*modify by dragontec for bug 4365 end*/
        final int childBottom = childTop + rect.height();

        final int offScreenLeft = Math.min(0, childLeft - parentLeft - mSelectedItemOffsetStart);
        final int offScreenTop = Math.min(0, childTop - parentTop - mSelectedItemOffsetStart);
        final int offScreenRight = Math.max(0, childRight - parentRight + mSelectedItemOffsetEnd);
        final int offScreenBottom = Math.max(0, childBottom - parentBottom + mSelectedItemOffsetEnd);

        final boolean canScrollHorizontal = getLayoutManager().canScrollHorizontally();
        final boolean canScrollVertical = getLayoutManager().canScrollVertically();

        // Favor the "start" layout direction over the end when bringing one side or the other
        // of a large rect into view. If we decide to bring in end because start is already
        // visible, limit the scroll such that start won't go out of bounds.
        final int dx;
        if (canScrollHorizontal) {

			int[] locations = new int[2];
			child.getLocationOnScreen(locations);
			dx = locations[0] + child.getWidth() / 2 - getResources().getDisplayMetrics().widthPixels / 2;

//		/*modify by dragontec for bug 4434 start*/
////            if(getScrollX() == 0 && childRight < getResources().getDisplayMetrics().widthPixels - 20 && getPositionByView(child) > 2){
////                dx = 0;
////            }else{
//                if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
//                    dx = offScreenRight != 0 ? offScreenRight
//                            : Math.max(offScreenLeft, childRight - parentRight);
//                } else {
//                    dx = offScreenLeft != 0 ? offScreenLeft
//                            : Math.min(childLeft - parentLeft, offScreenRight);
//                }
//            }
		/*modify by dragontec for bug 4434 end*/
        } else {
            dx = 0;
        }

        // Favor bringing the top into view over the bottom. If top is already visible and
        // we should scroll to make bottom visible, make sure top does not go out of bounds.
        final int dy;
        if (canScrollVertical) {
            dy = offScreenTop != 0 ? offScreenTop : Math.min(childTop - parentTop, offScreenBottom);
        } else {
            dy = 0;
        }
        if (cannotScrollForwardOrBackward(isVertical() ? dy : dx)) {
            offset = -1;
        } else {
            offset = isVertical() ? dy : dx;
            if (dx != 0 || dy != 0) {
                Log.i("recyclerViewTV","immediate: "+immediate);
                if (immediate) {
                    scrollBy(dx, dy);
                } else {
                    smoothScrollBy(dx, dy);
                }
                return true;
            }

        }

        // 重绘是为了选中item置顶，具体请参考getChildDrawingOrder方法
        postInvalidate();

        return false;
    }

    public int getFirstCompletelyVisiblePosition() {
        LayoutManager lm = getLayoutManager();
        /*modify by dragontec for bug 4242 start*/
        if (lm != null) {
            if (lm instanceof LinearLayoutManager) {
                return ((LinearLayoutManager) lm).findFirstCompletelyVisibleItemPosition();
            }
            if (lm instanceof GridLayoutManager) {
                return ((GridLayoutManager) lm).findFirstCompletelyVisibleItemPosition();
            }
            if(lm instanceof StaggeredGridLayoutManager){
                //temp fix crash by dragontec
                try {
                	int min = -1;
                	int[] positions = ((StaggeredGridLayoutManager) lm).findFirstCompletelyVisibleItemPositions(null);
					for (int pos:
						 positions) {
						if (min == -1 || min > pos) {
							min = pos;
						}
					}
					return min;

        /*modify by dragontec for bug 4242 end*/
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return RecyclerView.NO_POSITION;
    }

    public interface OnItemFocusChangeListener{
        void onItemFocusGain(View itemView, int position);
    }

    private  OnItemFocusChangeListener mOnItemFocusChangeListener;

    public void setOnItemFocusChangeListener(OnItemFocusChangeListener onItemFocusChangeListener) {
        mOnItemFocusChangeListener = onItemFocusChangeListener;
    }

    private boolean cannotScrollForwardOrBackward(int value) {
//        return cannotScrollBackward(value) || cannotScrollForward(value);
        return false;
    }

    /**
     * 判断第一个位置，没有移动.
     * getStartWithPadding --> return (mIsVertical ? getPaddingTop() : getPaddingLeft());
     */
    public boolean cannotScrollBackward(int delta) {
		//modify by dragontec guide有视频的时候拿到的position为-1
        return (getFirstCompletelyVisiblePosition() <= 0 && delta <= 0);
    }

    /**
     * 判断是否达到了最后一个位置，没有再移动了.
     * getEndWithPadding -->  mIsVertical ?  (getHeight() - getPaddingBottom()) :
     * (getWidth() - getPaddingRight());
     */
    public boolean cannotScrollForward(int delta) {
        int lastpos=findLastCompletelyVisibleItemPosition();
        LayoutManager lm = getLayoutManager();
//        if(hasHeaderView&& lm instanceof StaggeredGridLayoutManager){
//            lastpos+=1;
//        }
        int count = getLayoutManager().getItemCount();
        /*modify by dragontec for bug 4242 start*/
        return (lastpos >= getLayoutManager().getItemCount() - 1) && (delta >= 0);
        /*modify by dragontec for bug 4242 end*/
    }

    @Override
    public int getBaseline() {
        return offset;
    }

    @Override
    public void smoothScrollBy(int dx, int dy) {
        // ViewFlinger --> smoothScrollBy(int dx, int dy, int duration, Interpolator interpolator)
        //  ViewFlinger --> run --> hresult = mLayout.scrollHorizontallyBy(dx, mRecycler, mState);
        // LinearLayoutManager --> scrollBy --> mOrientationHelper.offsetChildren(-scrolled);
        super.smoothScrollBy(dx, dy);
    }
	/*modify by dragontec for bug 4365 start*/
    public int getCenterSpace() {
        //implement in child class
        return 0;
    }
	/*modify by dragontec for bug 4365 end*/
    public int getSelectedItemOffsetStart() {
        return mSelectedItemOffsetStart;
    }

    public int getSelectedItemOffsetEnd() {
        return mSelectedItemOffsetEnd;
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
    }

    /**
     * 判断是垂直，还是横向.
     */
    private boolean isVertical() {
        if(getLayoutManager() instanceof  LinearLayoutManager){
            LinearLayoutManager layoutManager = (LinearLayoutManager)getLayoutManager();
            return layoutManager.getOrientation() == LinearLayoutManager.VERTICAL;
        } else if(getLayoutManager() instanceof StaggeredGridLayoutManager){
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager)getLayoutManager();
            return layoutManager.getOrientation() == StaggeredGridLayoutManager.VERTICAL;
        }
        return true;
    }

    /**
     * 设置选中的Item距离开始或结束的偏移量；
     * 与滚动方向有关；
     * 与setSelectedItemAtCentered()方法二选一
     *
     * @param offsetStart
     * @param offsetEnd   从结尾到你移动的位置.
     */
    public void setSelectedItemOffset(int offsetStart, int offsetEnd) {
        setSelectedItemAtCentered(false);
        this.mSelectedItemOffsetStart = offsetStart;
        this.mSelectedItemOffsetEnd = offsetEnd;
    }

    /**
     * 设置选中的Item居中；
     * 与setSelectedItemOffset()方法二选一
     *
     * @param isCentered
     */
    public void setSelectedItemAtCentered(boolean isCentered) {
        this.mSelectedItemCentered = isCentered;
    }
    /*add by dragontec for bug 4270 start*/
    public boolean isSelectedItemAtCentered() {
        return mSelectedItemCentered;
    }
    /*add by dragontec for bug 4270 end*/
    public View getSelectView() {
        if (mItemView == null)
            mItemView = getFocusedChild();
        return mItemView;
    }

    public int getSelectPostion() {
        View view = getSelectView();
        if (view != null)
            return getPositionByView(view);
        return -1;
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        View view = getFocusedChild();
        if (null != view) {
            position = getChildAdapterPosition(view) - getFirstVisiblePosition();
            if (position < 0) {
                return i;
            } else {
                if (i == childCount - 1) {//这是最后一个需要刷新的item
                    if (position > i) {
                        position = i;
                    }
                    return position;
                }
                if (i == position) {//这是原本要在最后一个刷新的item
                    return childCount - 1;
                }
            }
        }
        return i;
    }

    public int getFirstVisiblePosition() {
        if (getChildCount() == 0)
            return 0;
        else
            return getChildLayoutPosition(getChildAt(0));
    }

    public int getLastVisiblePosition() {
        final int childCount = getChildCount();
        if (childCount == 0)
            return 0;
        else
            return getChildLayoutPosition(getChildAt(childCount - 1));
    }

    @Override
    public void onScrollStateChanged(int state) {
	/*add by dragontec for bug 4265 start*/
    	Log.d("RecyclerViewTV", "onScrollStateChanged : state = " + state);
		mScrollState = state;
	/*add by dragontec for bug 4265 end*/
        if (state == SCROLL_STATE_IDLE) {
            offset = -1;
            final View focuse = getFocusedChild();
            if (null != mOnItemListener && null != focuse) {
                mOnItemListener.onReviseFocusFollow(this, focuse, getChildLayoutPosition(focuse));
            }
        }
        super.onScrollStateChanged(state);
    }

    private interface ItemListener extends OnClickListener, OnFocusChangeListener {
    }

    public interface OnItemListener {
        void onItemPreSelected(HistoryRecyclerViewTV parent, View itemView, int position);

        void onItemSelected(HistoryRecyclerViewTV parent, View itemView, int position);

        void onReviseFocusFollow(HistoryRecyclerViewTV parent, View itemView, int position);
    }

    public interface OnChildViewHolderSelectedListener {
        public void onChildViewHolderSelected(RecyclerView parent, ViewHolder vh,
                                              int position);
    }

    public interface OnItemClickListener {
        void onItemClick(HistoryRecyclerViewTV parent, View itemView, int position);
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.mOnItemListener = onItemListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    /**
     * 控制焦点高亮问题.
     * 2016.08.29
     */
    public void setOnChildViewHolderSelectedListener(OnChildViewHolderSelectedListener listener) {
        mChildViewHolderSelectedListener = listener;
    }

    private int getPositionByView(View view) {
        if (view == null) {
            return NO_POSITION;
        }
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        if (params == null || params.isItemRemoved()) {
            // when item is removed, the position value can be any value.
            return NO_POSITION;
        }
        return params.getViewPosition();
    }

    /////////////////// 按键加载更多 start start start //////////////////////////

    private PagingableListener mPagingableListener;
    private boolean isLoading = false;
    private boolean isDoubleLd=false;
    //判断是MDbanner还是LDbanner
    public void setloadMoreType(boolean type){
        isDoubleLd=type;
    }
    public interface PagingableListener {
        void onLoadMoreItems();
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        setHovered(false);
        if(getLayoutManager() instanceof LinearLayoutManagerTV){
            ((LinearLayoutManagerTV) getLayoutManager()).setCanScroll(true);
        }else if(getLayoutManager() instanceof StaggeredGridLayoutManagerTV){
            ((StaggeredGridLayoutManagerTV) getLayoutManager()).setCanScroll(true);
        }
        int action = event.getAction();
        long current = System.currentTimeMillis();
        if(current - lastKeyEventTime <100){
            return true;
        }
        lastKeyEventTime = current;
        int keyCode = event.getKeyCode();
        if (action == KeyEvent.ACTION_DOWN) {
            if (!isHorizontalLayoutManger() && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                // 垂直布局向下按键.
                loadMore();
            } else if (isHorizontalLayoutManger() && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                // 横向布局向右按键.
                loadMore();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private static final int LOAD_MORE_VALUE = 10;
    public boolean loadMore() {
        int totalItemCount = getLayoutManager().getItemCount();
        int lastVisibleItem = findLastVisibleItemPosition();
        int lastComVisiPos = findLastCompletelyVisibleItemPosition();
        int visibleItemCount = getChildCount();
        int firstVisibleItem = findFirstVisibleItemPosition();

        if(getLayoutManager() instanceof StaggeredGridLayoutManager){
            View view = getLayoutManager().getFocusedChild();
            int adapterPosition = getChildAdapterPosition(view);
//            int layoutPosition = getChildLayoutPosition(view);
            if(view!=null) {
                if (!isLoading && totalItemCount - (adapterPosition + 3) <= LOAD_MORE_VALUE) {
                    isLoading = true;
                    if (mPagingableListener != null) mPagingableListener.onLoadMoreItems();
                }
            }else{
                if(!isDoubleLd) {
                    if (!isLoading && totalItemCount - lastComVisiPos <= 16) {
                        isLoading = true;
                        if (mPagingableListener != null) mPagingableListener.onLoadMoreItems();
                    }
                }else{
                    if (!isLoading && totalItemCount - lastComVisiPos <= 22) {
                        isLoading = true;
                        if (mPagingableListener != null) mPagingableListener.onLoadMoreItems();
                    }
                }
            }
            return true;
        }

        // 判断是否显示最底了.提前5个item预加载
		/*add by dragontec for bug 4264 start*/
		Log.d(TAG, "check loadMore (isLoading = " + isLoading + ", totalItemCount = " + totalItemCount + ", visibleItemCount = " + visibleItemCount + ", firstVisibleItem + " + firstVisibleItem + ")");
		/*add by dragontec for bug 4264 end*/
        if (!isLoading && totalItemCount - visibleItemCount <= firstVisibleItem + 10) {
            isLoading = true;
            if (mPagingableListener != null) {
//                OPENLOG.D(" totalItemCount: " + totalItemCount +
//                        " lastVisibleItem: " + lastVisibleItem +
//                        " lastComVisiPos: " + lastComVisiPos);
                mPagingableListener.onLoadMoreItems();
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为横向布局
     */
    private boolean isHorizontalLayoutManger() {
        LayoutManager lm = getLayoutManager();
        if (lm != null) {
            if (lm instanceof LinearLayoutManager) {
                LinearLayoutManager llm = (LinearLayoutManager) lm;
                return LinearLayoutManager.HORIZONTAL == llm.getOrientation();
            }
            if (lm instanceof GridLayoutManager) {
                GridLayoutManager glm = (GridLayoutManager) lm;
                return GridLayoutManager.HORIZONTAL == glm.getOrientation();
            }
            if(lm instanceof StaggeredGridLayoutManager){
                StaggeredGridLayoutManager slm = (StaggeredGridLayoutManager) lm;
                Log.i("RecyclerViewTV", "isHorizontalLayoutManger:"+(StaggeredGridLayoutManager.HORIZONTAL == slm.getOrientation()));
                return StaggeredGridLayoutManager.HORIZONTAL == slm.getOrientation();
            }
        }
        return false;
    }

    /**
     * 最后的位置.
     */
    public int findLastVisibleItemPosition() {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager != null) {
            if (layoutManager instanceof LinearLayoutManager) {
                return ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
            if (layoutManager instanceof GridLayoutManager) {
                return ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
/*add by dragontec for bug 4338 start*/
            if (layoutManager instanceof StaggeredGridLayoutManager) {
				try {
					int[] positions = ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null);
					int max = 0;
					for (int pos :
							positions) {
						if (max < pos) {
							max = pos;
						}
					}
					return max;
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
/*add by dragontec for bug 4338 end*/
        }
        return RecyclerView.NO_POSITION;
    }

    /**
     * 滑动到底部.
     */
    public int findLastCompletelyVisibleItemPosition() {
        LayoutManager layoutManager = getLayoutManager();
        /*modify by dragontec for bug 4242 start*/
        if (layoutManager != null) {
            if (layoutManager instanceof LinearLayoutManager) {
                return ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
            }
            if (layoutManager instanceof GridLayoutManager) {
                return ((GridLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
            }
            if(layoutManager instanceof StaggeredGridLayoutManager){
                //temp fix crash by dragontec
                try {
					int[] positions = ((StaggeredGridLayoutManager) layoutManager).findLastCompletelyVisibleItemPositions(null);
					int max = 0;
					for (int pos :
							positions) {
						if (max < pos) {
							max = pos;
						}
					}
					return max;
				}catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        }
        /*modify by dragontec for bug 4242 end*/
        return RecyclerView.NO_POSITION;
    }

	/*add by dragontec for bug 4242 start*/
    public int[] findLastCompletelyVisibleItemPositions() {
		LayoutManager layoutManager = getLayoutManager();
		if (layoutManager != null) {
			if(layoutManager instanceof StaggeredGridLayoutManager){
				//temp fix crash by dragontec
				try {
					return ((StaggeredGridLayoutManager) layoutManager).findLastCompletelyVisibleItemPositions(null);
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		return null;
	}
    /*add by dragontec for bug 4242 end*/

    public int findFirstVisibleItemPosition() {
        LayoutManager lm = getLayoutManager();
        if (lm != null) {
            if (lm instanceof LinearLayoutManager) {
                return ((LinearLayoutManager) lm).findFirstVisibleItemPosition();
            }
            if (lm instanceof GridLayoutManager) {
                return ((GridLayoutManager) lm).findFirstVisibleItemPosition();
            }
            /*add by dragontec for bug 4338 start*/
			if(lm instanceof StaggeredGridLayoutManager){
				//temp fix crash by dragontec
				try {
					int min = -1;
					int[] positions = ((StaggeredGridLayoutManager) lm).findFirstVisibleItemPositions(null);
					for (int pos:
							positions) {
						if (min == -1 || min > pos) {
							min = pos;
						}
					}
					return min;
				} catch (NullPointerException e){
					e.printStackTrace();
				}
			}
			/*add by dragontec for bug 4338 end*/
        }
        return RecyclerView.NO_POSITION;
    }

    /////////////////// 按键加载更多 end end end //////////////////////////

    /////////////////// 按键拖动 Item start start start ///////////////////////

    private final ArrayList<OnItemKeyListener> mOnItemKeyListeners =
            new ArrayList<OnItemKeyListener>();

    public static interface OnItemKeyListener {
        public boolean dispatchKeyEvent(KeyEvent event);
    }

    public void addOnItemKeyListener(OnItemKeyListener listener) {
        mOnItemKeyListeners.add(listener);
    }

    public void removeOnItemKeyListener(OnItemKeyListener listener) {
        mOnItemKeyListeners.remove(listener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return super.onInterceptTouchEvent(e);
    }

    ////////////////// 按键拖动 Item end end end /////////////////////////

    /**
     * 设置默认选中.
     */
    public void setDefaultSelect(int pos) {
        ViewHolder vh =  findViewHolderForAdapterPosition(pos);
//        requestFocusFromTouch();
        if (vh != null)
            vh.itemView.requestFocus();
    }

    /**
     * 延时选中默认.
     */
    public void setDelayDefaultSelect(int pos, int time) {
        Message msg = mHandler.obtainMessage();
        msg.arg1 = pos;
        mHandler.sendMessageDelayed(msg, time);
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos = msg.arg1;
            setDefaultSelect(pos);
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction()==MotionEvent.ACTION_MOVE) {
            return true;
        }else{
            return super.dispatchTouchEvent(ev);
        }
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
		setHovered(true);
        if(event.getAction()== MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
            if(getLayoutManager() instanceof LinearLayoutManagerTV){
                ((LinearLayoutManagerTV) getLayoutManager()).setCanScroll(false);
            }else if(getLayoutManager() instanceof StaggeredGridLayoutManagerTV){
                ((StaggeredGridLayoutManagerTV) getLayoutManager()).setCanScroll(false);
            }
        }
        return super.dispatchHoverEvent(event);
    }

    public void setHasHeaderView(boolean hasHeaderView) {
        this.hasHeaderView = hasHeaderView;
    }
    /*add by dragontec for bug 4221 start*/
    public View getLastFocusChild(){
        return lastFocusChild;
    }
    /*add by dragontec for bug 4221 end*/

	/*add by dragontec for bug 4265 start*/
    public boolean isNotScrolling() {
    	return mScrollState == SCROLL_STATE_IDLE;
	}
	/*add by dragontec for bug 4265 end*/

	@Override
	public void setHovered(boolean hovered) {
		super.setHovered(hovered);
		if (mOnHoverStateChangedListener != null) {
			mOnHoverStateChangedListener.onHoverStateChanged(hovered);
		}
	}

	public interface OnHoverStateChangedListener {
		void onHoverStateChanged(boolean hovered);
	}

	private OnHoverStateChangedListener mOnHoverStateChangedListener = null;

	public void setOnHoverStateChangedListener(OnHoverStateChangedListener onHoverStateChangedListener) {
		mOnHoverStateChangedListener = onHoverStateChangedListener;
	}
}
