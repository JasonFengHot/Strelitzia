package tv.ismar.homepage.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;

import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 1017/9/17
 * @DESC: 可回收linear
 */

public class RecycleLinearLayout extends LinearLayout {

	public static final int BANNER_LOAD_AIMING_OFF = 3;

    private static final String TAG = RecycleLinearLayout.class.getSimpleName();

    private Context mContext;
//    private ScrollView mScrollView;
    private int mScreenHeight;
    private int mSelectedChildIndex = 0;
    private ArrayList<View> mAllViews = new ArrayList<>();
	/*add by dragontec for bug 3983 start*/
    private OnPositionChangedListener mPositionChangeListener;
    private View lastScrollToTopView;
    private boolean isScrollDuringTitleHiddenState;
	/*add by dragontec for bug 3983 end*/
    private Button arrow_up;
    private Button arrow_down;
    private int dataSize;    
	private int currentBannerPos=0;
    private HomeRootRelativeLayout homeRootRelativeLayout;
    private boolean hasMore=false;
	/*modify by dragontec for bug 4296 start*/
    private boolean isHideTopOutSideChild = false;
    private Object hideTopOutSideChildLock = new Object();
	/*modify by dragontec for bug 4296 end*/

    public RecycleLinearLayout(Context context) {
        super(context);
        initWindow(context);
    }

    public RecycleLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWindow(context);
    }

/*add by dragontec for bug 4195 start*/
    public void enableChildrenDrawingOrder() {
        setChildrenDrawingOrderEnabled(true);
    }
/*add by dragontec for bug 4195 end*/

//    public void setScrollView(ScrollView view){
//        this.mScrollView = view;
//    }

    public void setView(View view){
        if(view==null) return;
        mAllViews.add(view);
    }

    /*初始化3个数据*/
    public void initView(){
        mSelectedChildIndex = 0;
        addView(mAllViews.get(0));
        addView(mAllViews.get(1));
        addView(mAllViews.get(2));
    }

    public void clearView(){

    }

    private Rect mWindowRect = null;
    private void initWindow(Context context){
        mContext = context;
        setOrientation(LinearLayout.VERTICAL);
        this.mOverScroller = new OverScroller(getContext());
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mWindowRect = new Rect(0, 0, dm.widthPixels, dm.heightPixels);
        mScreenHeight = dm.heightPixels;
        Log.i(TAG, "screenWidth:"+dm.widthPixels+" screenHeight:"+dm.heightPixels);
    }

    private boolean getVisibleOnScreen(View view){
        return view.getLocalVisibleRect(mWindowRect);
    }

	/*modify by dragontec for bug 4200,4285 start 加快滑动速度*/
    private static final int SCROLL_DURATION = 500;//默认滑动时间
	/*modify by dragontec for bug 4200,4285 end*/
    private OverScroller mOverScroller = null;

    @Override
    public void computeScroll() {
        if(mOverScroller.computeScrollOffset()){//判断滚动是否完毕
            scrollTo(mOverScroller.getCurrX(), mOverScroller.getCurrY());//调用view方法执行真实的滑动动作
/*modify by dragontec for bug 4200 start*/
//            postInvalidate();
			invalidate();
        } else {
			//滚动完毕后确认数据请求
			if (mOnScrollFinishedListener != null) {
				mOnScrollFinishedListener.onScrollFinished();
			}
		}
/*modify by dragontec for bug 4200 end*/
        super.computeScroll();
    }

    private void smoothScrollBy(int dx, int dy){
        Log.i(TAG, "dx:"+dx+"  dy:"+dy);
		/*modify by dragontec for bug 3983 start*/
        if(HomeActivity.isTitleHidden && isScrollDuringTitleHiddenState){
            dy +=  getResources().getDimensionPixelSize(R.dimen.banner_margin_top);
            /*modify by dragontec for bug 4178 start*/
			/*modify by dragontec for bug 4296 start*/
			/*modify by dragontec for bug 4339 start*/
            View lastView = getChildAt(getChildCount() - 1);
            int[] lastPoint = new int[2];
            lastView.getLocationOnScreen(lastPoint);
			if(dataSize <= getChildCount() && lastPoint[1] > 0) {
//                if (getFocusedChildPosition() >= getChildCount() - BANNER_LOAD_AIMING_OFF ) {
                int bottom = lastPoint[1] + lastView.getHeight();
			/*modify by dragontec for bug 4296 end*/
			    int maxScrollByBottom = bottom - mScreenHeight;
			    if(dy > maxScrollByBottom){
			        dy = maxScrollByBottom;
                }
				/*modify by dragontec for bug 4339 end*/
            }
            if(dy != 0){
                mOverScroller.startScroll(mOverScroller.getFinalX(), mOverScroller.getFinalY(), dx, dy, SCROLL_DURATION);
            }
            /*modify by dragontec for bug 4178 end*/
        }else{
            if(mOverScroller.getFinalY() + dy < 0){
                dy = -mOverScroller.getFinalY();
            }
            mOverScroller.startScroll(mOverScroller.getFinalX(), mOverScroller.getFinalY(), dx, dy, SCROLL_DURATION);
        }
		/*modify by dragontec for bug 3983 end*/
        invalidate();//保证computeScroll()执行
    }
	/*modify by dragontec for bug 4296 start*/
    private int getFocusedChildPosition() {
        View view = getFocusedChild();
        int position = 0;
        if(view != null) {
            int key = (int) view.getTag();
            int tag = (int) view.getTag(key);
            position = (tag << 2) >> 2;
        }
        return position;
    }
	/*modify by dragontec for bug 4296 end*/
    private void scrollToVisiable(View view){
        if(view != null){
            currentBannerPos=indexOfChild(view);
            if(homeRootRelativeLayout!=null) {
                if (currentBannerPos < 2) {
                    homeRootRelativeLayout.setShowUp(false);
                }else{
                    homeRootRelativeLayout.setShowUp(true);
                }
                int childCount=getChildCount();
                Log.e("childCount",childCount+"&"+currentBannerPos);
				/*modify by dragontec for bug 4339 start*/
                if(isScrollAtBottom()){
				/*modify by dragontec for bug 4339 end*/
                    homeRootRelativeLayout.setShowDown(false);
                }else{
                    homeRootRelativeLayout.setShowDown(true);
                }
            }
            int height = view.getHeight();
            int[] location = new int[]{0, 0};
            view.getLocationOnScreen(location);
            /*modify by dragontec for bug 4178 start*/
            int overScreenHeight = getResources().getDimensionPixelSize(R.dimen.over_screen_height);
            smoothScrollBy(0, location[1]-(overScreenHeight-height));
            Log.i("scrollToVisiable", "height:"+height+"  location[1]:"+location[1]+
            " location[1]-(mScreenHeight-height):"+(location[1]-(overScreenHeight-height)));
            /*modify by dragontec for bug 4178 end*/
        }
    }

    private void scrollToTop(View view){
        if(view != null){
            currentBannerPos=indexOfChild(view);
            if(homeRootRelativeLayout!=null) {
				/*modify by dragontec for bug 4248 start*/
				if (currentBannerPos < 1) {
					homeRootRelativeLayout.setShowUp(false);
				} else if (currentBannerPos == 1) {
					int key = (int) view.getTag();
					int tag = (int) view.getTag(key);
				/*modify by dragontec for bug 4077 start*/
					boolean canScroll = tag>>30==1;//1可滑动，0不可滑动
					if (canScroll) {
						homeRootRelativeLayout.setShowUp(true);
					} else {
						homeRootRelativeLayout.setShowUp(false);
					}
				} else {
					homeRootRelativeLayout.setShowUp(true);
				}
				/*modify by dragontec for bug 4248 end*/
                int childCount=getChildCount();
                Log.e("childCount",childCount+"&&&"+currentBannerPos);
				/*modify by dragontec for bug 4339 start*/
                if(isScrollAtBottom()){
				/*modify by dragontec for bug 4339 end*/
                    homeRootRelativeLayout.setShowDown(false);
                }else{
                    homeRootRelativeLayout.setShowDown(true);
                }
            }
            int[] location = new int[]{0, 0};
            view.getLocationOnScreen(location);
            Log.i(TAG, "top:"+location[1]);
            Log.i(TAG, "margin:"+mContext.getResources().getDimensionPixelOffset(R.dimen.banner_margin_top));
			/*modify by dragontec for bug 4296 start*/
            for (int i = 0; i < getChildCount(); i++) {
                View v = getChildAt(i);
                if(v.getVisibility() != View.VISIBLE){
                    v.setVisibility(View.VISIBLE);
                }
            }
			/*modify by dragontec for bug 4296 end*/
			/*modify by dragontec for bug 4149 start*/
			if (location[1] != 0) {
				smoothScrollBy(0, location[1] - mContext.getResources().getDimensionPixelOffset(R.dimen.banner_margin_top));
			}
			/*modify by dragontec for bug 4149 end*/
        }
    }

    private View findView(View view){
        if(view == null){
            View rootView = view.getRootView();
            Log.i(TAG, "rootView:"+rootView);
            if(rootView!=null){
//                return rootView.findViewById(R.id.banner_tile);
            }
        }
        return null;
    }

    private int mWidth = 0;
    private int mHeight = 0;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = 0;
        mHeight = 0;
        for(int i=0; i<getChildCount(); i++){
            View childView = getChildAt(i);
            measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mWidth = mWidth+childView.getMeasuredWidth();
            mHeight = mHeight+childView.getMeasuredHeight();
            Log.i("onMeasure", "childWidth:"+childView.getMeasuredWidth()+"  childHeight:"+childView.getMeasuredHeight());
        }
        Log.i("onMeasure", "mWidth:"+mWidth+"  mHeight:"+mHeight);
        setMeasuredDimension(mWidth, mHeight);
    }

    private View mLastView;//记录焦点
    private static final int mScrollHeight = 10000;

    private long mLastTime = 0;
    private long mCurrentTime = 0;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int count = event.getRepeatCount();
        boolean longPress = false;
        Log.i("RepeatCount", "RepeatCount:"+count);
        if(count == 0){
            mLastTime = System.currentTimeMillis();
            mCurrentTime = mLastTime;
        }
        if(count > 0){
            longPress = true;
            mCurrentTime = System.currentTimeMillis();
        }
        if(longPress && mCurrentTime-mLastTime<500){
            return true;
        } else {
            mLastTime = mCurrentTime;
        }
		/*modify by dragontec for bug 4296 start*/
        if(mOverScroller.computeScrollOffset() && event.getAction() == KeyEvent.ACTION_DOWN){
            return true;
        }
		/*modify by dragontec for bug 4296 end*/
        return excuteKeyEvent(event, longPress);
    }

    private boolean excuteKeyEvent(KeyEvent event, boolean longPress){
        int keyCode = event.getKeyCode();
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            mLastView = getFocusedChild();
        }
        Log.i(TAG, "action:"+event.getAction()+" keyCode:"+keyCode);
        if(longPress || event.getAction() == KeyEvent.ACTION_UP){
            if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN || keyCode==KeyEvent.KEYCODE_DPAD_UP
                    || keyCode==KeyEvent.KEYCODE_DPAD_LEFT || keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
                View view = getFocusedChild();
                View view1 = findFocus();
                Log.i(TAG, "debug1"+" view:"+view+" view1:"+view1);
				/*modify by dragontec for bug 4149 start*/
				int key = (int) view.getTag();
				int tag = (int) view.getTag(key);
				/*modify by dragontec for bug 4077 start*/
				boolean canScroll = tag>>30==1;//1可滑动，0不可滑动
				int position = (tag<<2)>>2;
                if(view==mLastView && !longPress) {
                	/*modify by dragontec for bug 4353 start*/
					if (key == R.layout.banner_more && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						YoYo.with(Techniques.VerticalShake).duration(1000).playOn(view);
					}
					/*modify by dragontec for bug 4353 end*/

					if (position >= getChildCount() - BANNER_LOAD_AIMING_OFF && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						if (mOnDataFinishedListener != null) {
							mOnDataFinishedListener.onDataFinished(view);
						}
					}
					return super.dispatchKeyEvent(event);//banner抖动问题
				}
				//fix bug by dragontec点击下箭头有时不生效的bug
				mLastView = view;
				/*modify by dragontec for bug 4149 end*/
				/*modify by dragontec for bug 4077 end*/
//                mHolder.onCreateView(position, keyCode);
                Log.i(TAG, "key:"+key+" canScroll:"+canScroll+" position:"+position);
				/*add by dragontec for bug 3983 start*/
                boolean startTitleState = HomeActivity.isTitleHidden;
                if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN || keyCode==KeyEvent.KEYCODE_DPAD_UP){
                    if(mPositionChangeListener != null){
                        mPositionChangeListener.onPositionChanged(position,keyCode,canScroll);
                    }
                }
                boolean endTitleState = HomeActivity.isTitleHidden;
                isScrollDuringTitleHiddenState =startTitleState && endTitleState;
				/*add by dragontec for bug 3983 end*/
                if(!canScroll){//限制滑动
                    Log.i(TAG, "canScroll");
                    if(position-1 < 0) return super.dispatchKeyEvent(event);//将不可滑动的banner和前一个banner绑定为一个banner
//                    mScrollView.setBottom(mScrollHeight+mScreenHeight);
                    scrollToTop(getChildAt(position-1));
                    return super.dispatchKeyEvent(event);
                }
	/*add by dragontec for bug 4077 start*/
				if (position >= getChildCount() - BANNER_LOAD_AIMING_OFF && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
					if (mOnDataFinishedListener != null) {
						mOnDataFinishedListener.onDataFinished(view);
					}
				}
	/*add by dragontec for bug 4077 end*/
	/*modify by dragontec for bug 4178 start 所有滑动事件都进scrollToTop 在smoothScrollBy中作滑动限制*/
                //滑动处理
//                if(position==getChildCount()-1){
//                    Log.i(TAG, "scrollToVisiable");
////                    mScrollView.setBottom(mScrollHeight+mScreenHeight);
//					/*modify by dragontec for bug 4149 start*/
//					//最后一个banner不是更多按钮的时候banner不需要抖动
//                    scrollToVisiable(view);
//                    if (key != R.layout.banner_more) {
////						YoYo.with(Techniques.VerticalShake).duration(1000).playOn(view);
//					}
//					/*modify by dragontec for bug 4149 end*/
//                } else {
                    Log.i(TAG, "scrollToTop");
                    //                    mScrollView.setBottom(mScreenHeight);
                    scrollToTop(view);
//                }
                /*modify by dragontec for bug 4178 end*/
            }
        }
        return super.dispatchKeyEvent(event);
    }

	/*add by dragontec for bug 4248 start*/
    private View findFirstViewOnLastPage() {
		View view = null;
		boolean getBottom = false;
		int plusHeight = 0;
		for (int i = currentBannerPos; i >= 0; i--) {
			View v = getChildAt(i);
			int layoutId = (int) v.getTag();
			int positionTag = (int) v.getTag(layoutId);
			boolean canScroll = positionTag >> 30 == 1;
			//不滑动的banner需要特殊处理，当前top不是此banner下方一个的时候需要显示一次下方的banner
			if (i == 1) {
                if (!canScroll) {
                    if (i != currentBannerPos - 1) {
                        Log.d(TAG, "findFirstViewOnLastPage find view[" + (i + 1) +"]");
                        view = getChildAt(i + 1);
                    }
                    break;
                }
            }
			int[] location = new int[]{0, 0};
			v.getLocationOnScreen(location);
			Log.d(TAG, "findFirstViewOnLastPage view[" + i + "] x = " + location[0] + " y = " + location[1]);
			/*modify by dragontec for bug 4339 start 可能由于dimen四舍五入导致的顶部的view scroll 到 -1 的位置*/
			if (location[1] < -1) {
			/*modify by dragontec for bug 4339 end*/
				getBottom = true;
			}
			if (getBottom) {
				plusHeight += v.getHeight();
			}
			int height = mScreenHeight;
			if (i == 0) {
				height = mScreenHeight - getResources().getDimensionPixelSize(R.dimen.banner_margin_top);
			}
			if (plusHeight > height) {
				if (i != currentBannerPos - 1) {
					Log.d(TAG, "findFirstViewOnLastPage find view[" + (i + 1) +"]");
					view = getChildAt(i + 1);
				} else {
					Log.d(TAG, "findFirstViewOnLastPage find view[" + i +"]");
					view = v;
				}
				break;
			} else if (plusHeight == height){
				Log.d(TAG, "findFirstViewOnLastPage find view[" + i +"]");
				view = v;
				break;
			}
		}
		Log.d(TAG, "findFirstViewOnLastPage view = " + view);
		if (view == null) {
			view = getChildAt(0);
		}
		return view;
	}

    private View findFirstViewOnNextPage() {
		View view = null;
		Rect rect = new Rect();
		for (int i = currentBannerPos; i < getChildCount(); i++) {
			View v = getChildAt(i);
			int[] location = new int[]{0, 0};
			v.getLocationOnScreen(location);
			Log.d(TAG, "findFirstViewOnNextPage view[" + i + "] x = " + location[0] + " y = " + location[1]);
			if (location[1] >= mScreenHeight || location[1] + v.getHeight() > mScreenHeight) {
				Log.d(TAG, "findFirstViewOnNextPage find view[" + i +"]");
				view = v;
				break;
			}
		}
		Log.d(TAG, "findFirstViewOnNextPage view = " + view);
		if (view == null) {
			view = getChildAt(getChildCount() - 1);
		}
		return view;
	}
	/*add by dragontec for bug 4248 end*/

    public void setArrow_up(Button arrow_up) {
        this.arrow_up = arrow_up;
        this.arrow_up.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
					/*modify by dragontec for bug 4296 start*/
                    if(mOverScroller.computeScrollOffset()){
                        return ;
                    }
					/*modify by dragontec for bug 4296 end*/
                    View view = findFirstViewOnLastPage();
                    if(view!=null) {
                        int key = (int) view.getTag();
                        int tag = (int) view.getTag(key);
                        boolean canScroll = tag >> 30 == 1;//1可滑动，0不可滑动
                        int position = (tag << 2) >> 2;
                        if (view == mLastView) {
                            if (key == R.layout.banner_more) {
                                YoYo.with(Techniques.VerticalShake).duration(1000).playOn(view);
                            }
                            //处理同excuteKeyEvent
                            return;
                        }
                        mLastView = view;
                        boolean startTitleState = HomeActivity.isTitleHidden;
                        if (mPositionChangeListener != null) {
                            mPositionChangeListener.onPositionChanged(position, KeyEvent.KEYCODE_DPAD_UP, canScroll);
                        }
                        boolean endTitleState = HomeActivity.isTitleHidden;
                        isScrollDuringTitleHiddenState = startTitleState && endTitleState;
				/*modify by dragontec for bug 4178 start 所有滑动事件都进scrollToTop 在smoothScrollBy中作滑动限制*/
                        //滑动处理
//                if(position==getChildCount()-1){
//                    if (key != R.layout.banner_more) {
//                        scrollToVisiable(view);
//                    }
//                } else {
//                    scrollToTop(view);
//                }
                        scrollToTop(view);
                    }
				/*modify by dragontec for bug 4178 end*/
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void setArrow_down(Button arrow_down) {
        this.arrow_down = arrow_down;
        this.arrow_down.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
					/*modify by dragontec for bug 4296 start*/
                    if(mOverScroller.computeScrollOffset()){
                        return ;
                    }
					/*modify by dragontec for bug 4296 end*/
                    /*modify by dragontec for bug 4248 start*/
                    View view = findFirstViewOnNextPage();
//                View view = getChildAt(currentBannerPos==0?2:currentBannerPos+1>=getChildCount()?currentBannerPos:currentBannerPos+1);
				    /*modify by dragontec for bug 4248 end*/
                    if(view!=null) {
                        int key = (int) view.getTag();
                        int tag = (int) view.getTag(key);
                        boolean canScroll = tag >> 30 == 1;//1可滑动，0不可滑动
                        int position = (tag << 2) >> 2;
                        if (view == mLastView) {
                            if (key == R.layout.banner_more) {
                                YoYo.with(Techniques.VerticalShake).duration(1000).playOn(view);
                            }

                            if (position >= getChildCount() - BANNER_LOAD_AIMING_OFF) {
                                if (mOnDataFinishedListener != null) {
                                    mOnDataFinishedListener.onDataFinished(view);
                                }
                            }
                            //处理同excuteKeyEvent
                            return;
                        }
                        mLastView = view;
                        boolean startTitleState = HomeActivity.isTitleHidden;
                        if (mPositionChangeListener != null) {
                            mPositionChangeListener.onPositionChanged(position, KeyEvent.KEYCODE_DPAD_DOWN, canScroll);
                        }
                        boolean endTitleState = HomeActivity.isTitleHidden;
                        isScrollDuringTitleHiddenState = startTitleState && endTitleState;
                        if (position >= getChildCount() - BANNER_LOAD_AIMING_OFF) {
                            if (mOnDataFinishedListener != null) {
                                mOnDataFinishedListener.onDataFinished(view);
                            }
                        }
                /*modify by dragontec for bug 4178 start 所有滑动事件都进scrollToTop 在smoothScrollBy中作滑动限制*/
//                //滑动处理
//                if(position==getChildCount()-1){
//                    if (key != R.layout.banner_more) {
//                        scrollToVisiable(view);
//                    }
//                } else {
//                    scrollToTop(view);
//                }
                        scrollToTop(view);
                    }
				/*modify by dragontec for bug 4178 end*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

/*add by dragontec for bug 4205 start*/
    public void resetArrowUp() {
        if (this.arrow_up != null) {
            this.arrow_up = null;
        }
    }

    public void resetArrowDown() {
        if (this.arrow_down != null) {
            this.arrow_down = null;
        }
    }
/*add by dragontec for bug 4205 end*/

    private ViewHolder mHolder;

    public void setHolder(ViewHolder holder){
        this.mHolder = holder;
    }
	/*modify by dragontec for bug 4178 start*/
    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
/*modify by dragontec for bug 4335 start*/
    public boolean hasMore(){
        return  hasMore;
    }
/*modify by dragontec for bug 4335 end*/
    /*modify by dragontec for bug 4178 end*/
    public interface ViewHolder {
        void onCreateView(int position, int orientation);
    }

	/*add by dragontec for bug 4077 start*/
	public interface OnDataFinishedListener {
		void onDataFinished(View view);
	}

	private OnDataFinishedListener mOnDataFinishedListener;

	public void setOnDataFinishedListener(OnDataFinishedListener onDataFinishedListener) {
		mOnDataFinishedListener = onDataFinishedListener;
	}
	/*add by dragontec for bug 4077 end*/

	/*add by dragontec for bug 3983 start*/
    public void  setOnPositionChangedListener(OnPositionChangedListener mPositionChangeListener){
        this.mPositionChangeListener = mPositionChangeListener;
    }
    public interface OnPositionChangedListener {
        boolean onPositionChanged(int position, int direction, boolean canScroll);
    }
	/*add by dragontec for bug 3983 end*/

    public void setHomeRootRelativeLayout(HomeRootRelativeLayout homeRootRelativeLayout) {
        this.homeRootRelativeLayout = homeRootRelativeLayout;
    }

/*add by dragontec for bug 4225, 4224, 4223 start*/
    public float getScrollerCurrentY() {
        if (mOverScroller != null) {
            return mOverScroller.getCurrY();
        } else {
            return 0f;
        }
    }

    public boolean scrollerScrollToTop() {
        if (mOverScroller != null && !mOverScroller.computeScrollOffset()) {
            currentBannerPos = 0;
            //fix bug by dragontec点击下箭头有时不生效的bug
            mLastView = null;
            mOverScroller.startScroll(
                    mOverScroller.getFinalX(),
                    mOverScroller.getFinalY(),
                    0,
                    -mOverScroller.getFinalY(),
                    SCROLL_DURATION);
            invalidate();
            return true;
        }
        return false;
    }

    public boolean isScrollAtBottom() {
        boolean isScrollAtBottom = false;
        //加载个数已经全部加载完成（包含“更多”）
        if (dataSize <= getChildCount()) {
            int height = getHeight();
            int currY = mOverScroller.getCurrY();
            int screenHeight = mScreenHeight;
			/*modify by dragontec for bug 4339 start*/
            View lastView = getChildAt(getChildCount() - 1);
            if(lastView != null) {
                int[] lastPoint = new int[2];
                lastView.getLocationOnScreen(lastPoint);
                if (lastPoint[1] > 0) {
                    int bottom = lastPoint[1] + lastView.getHeight();
                    if (bottom == screenHeight) {
                        isScrollAtBottom = true;
                    }
                }
            }
			/*modify by dragontec for bug 4339 end*/
        }
        Log.i(TAG, "isScrollAtBottom:" + isScrollAtBottom);
        return isScrollAtBottom;
    }
/*add by dragontec for bug 4225, 4224, 4223 end*/

/*modify by dragontec for bug 4200 start*/
	public interface OnScrollFinishedListener {
		void onScrollFinished();
	}
	private OnScrollFinishedListener mOnScrollFinishedListener;
	public void setOnScrollFinishedListener(OnScrollFinishedListener onScrollFinishedListener) {
		mOnScrollFinishedListener = onScrollFinishedListener;
	}
/*modify by dragontec for bug 4200 end*/

/*add by dragontec for bug 4195 start*/
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        return childCount - i - 1;
    }
/*add by dragontec for bug 4195 end*/
}
