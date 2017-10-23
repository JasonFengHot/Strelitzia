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

    public RecycleLinearLayout(Context context) {
        super(context);
        initWindow(context);
    }

    public RecycleLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWindow(context);
    }

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

    private static final int SCROLL_DURATION = 150;//默认滑动时间
    private OverScroller mOverScroller = null;

    @Override
    public void computeScroll() {
        if(mOverScroller.computeScrollOffset()){//判断滚动是否完毕
            scrollTo(mOverScroller.getCurrX(), mOverScroller.getCurrY());//调用view方法执行真实的滑动动作
            postInvalidate();
        }
        super.computeScroll();
    }

    private void smoothScrollBy(int dx, int dy){
        Log.i(TAG, "dx:"+dx+"  dy:"+dy);
		/*modify by dragontec for bug 3983 start*/
        if(HomeActivity.isTitleHidden && isScrollDuringTitleHiddenState){
            dy +=  getResources().getDimensionPixelSize(R.dimen.banner_margin_top);
            /*modify by dragontec for bug 4178 start*/
            if(dataSize <= getChildCount()){
                int height = getHeight();
                if(mOverScroller.getFinalY() + dy > (height - mScreenHeight)){
                    dy = (height - mScreenHeight) - mOverScroller.getFinalY();
                }
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

    private void scrollToVisiable(View view){
        if(view != null){
            currentBannerPos=indexOfChild(view);
            if(homeRootRelativeLayout!=null) {
                if (currentBannerPos < 2) {
                    homeRootRelativeLayout.setShowUp(false);
                }else{
                    homeRootRelativeLayout.setShowUp(true);
                }
                if(currentBannerPos==getChildCount()-1||(int)view.getTag()==R.layout.banner_more){
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
                if (currentBannerPos < 2) {
                    homeRootRelativeLayout.setShowUp(false);
                }else{
                    homeRootRelativeLayout.setShowUp(true);
                }
                if(currentBannerPos==getChildCount()-1||(int)view.getTag()==R.layout.banner_more){
                    homeRootRelativeLayout.setShowDown(false);
                }else{
                    homeRootRelativeLayout.setShowDown(true);
                }
            }
            int[] location = new int[]{0, 0};
            view.getLocationOnScreen(location);
            Log.i(TAG, "top:"+location[1]);
            Log.i(TAG, "margin:"+mContext.getResources().getDimensionPixelOffset(R.dimen.banner_margin_top));
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
					if (key == R.layout.banner_more) {
						YoYo.with(Techniques.VerticalShake).duration(1000).playOn(view);
					}

					if (position >= getChildCount() - BANNER_LOAD_AIMING_OFF && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						if (mOnDataFinishedListener != null) {
							mOnDataFinishedListener.onDataFinished(view);
						}
					}
					return super.dispatchKeyEvent(event);//banner抖动问题
				}
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

    public void setArrow_up(Button arrow_up) {
        this.arrow_up = arrow_up;
        this.arrow_up.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getChildAt(currentBannerPos<=2?0:currentBannerPos-1);
                int key = (int) view.getTag();
                int tag = (int) view.getTag(key);
                boolean canScroll = tag>>30==1;//1可滑动，0不可滑动
                int position = (tag<<2)>>2;
                if(view==mLastView) {
                    if (key == R.layout.banner_more) {
                        YoYo.with(Techniques.VerticalShake).duration(1000).playOn(view);
                    }
                }
                boolean startTitleState = HomeActivity.isTitleHidden;
                if(mPositionChangeListener != null){
                    mPositionChangeListener.onPositionChanged(position,KeyEvent.KEYCODE_DPAD_UP,canScroll);
                }
                boolean endTitleState = HomeActivity.isTitleHidden;
                isScrollDuringTitleHiddenState =startTitleState && endTitleState;
                if (position >= getChildCount() - BANNER_LOAD_AIMING_OFF) {
                    if (mOnDataFinishedListener != null) {
                        mOnDataFinishedListener.onDataFinished(view);
                    }
                }
                //滑动处理
                if(position==getChildCount()-1){
                    if (key != R.layout.banner_more) {
                        scrollToVisiable(view);
                    }
                } else {
                    scrollToTop(view);
                }
            }
        });
    }

    public void setArrow_down(Button arrow_down) {
        this.arrow_down = arrow_down;
        this.arrow_down.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getChildAt(currentBannerPos==0?2:currentBannerPos+1>=getChildCount()?currentBannerPos:currentBannerPos+1);
                int key = (int) view.getTag();
                int tag = (int) view.getTag(key);
                boolean canScroll = tag>>30==1;//1可滑动，0不可滑动
                int position = (tag<<2)>>2;
                if(view==mLastView) {
                    if (key == R.layout.banner_more) {
                        YoYo.with(Techniques.VerticalShake).duration(1000).playOn(view);
                    }

                    if (position >= getChildCount() - BANNER_LOAD_AIMING_OFF) {
                        if (mOnDataFinishedListener != null) {
                            mOnDataFinishedListener.onDataFinished(view);
                        }
                    }
                }
                boolean startTitleState = HomeActivity.isTitleHidden;
                    if(mPositionChangeListener != null){
                        mPositionChangeListener.onPositionChanged(position,KeyEvent.KEYCODE_DPAD_DOWN,canScroll);
                    }
                boolean endTitleState = HomeActivity.isTitleHidden;
                isScrollDuringTitleHiddenState =startTitleState && endTitleState;
                if (position >= getChildCount() - BANNER_LOAD_AIMING_OFF) {
                    if (mOnDataFinishedListener != null) {
                        mOnDataFinishedListener.onDataFinished(view);
                    }
                }
                //滑动处理
                if(position==getChildCount()-1){
                    if (key != R.layout.banner_more) {
                        scrollToVisiable(view);
                    }
                } else {
                    scrollToTop(view);
                }
            }
        });
    }

    private ViewHolder mHolder;

    public void setHolder(ViewHolder holder){
        this.mHolder = holder;
    }
	/*modify by dragontec for bug 4178 start*/
    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }
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
}
