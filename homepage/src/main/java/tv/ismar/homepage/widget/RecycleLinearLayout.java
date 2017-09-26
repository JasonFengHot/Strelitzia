package tv.ismar.homepage.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import java.util.ArrayList;

import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 1017/9/17
 * @DESC: 可回收linear
 */

public class RecycleLinearLayout extends LinearLayout {

    private static final String TAG = RecycleLinearLayout.class.getSimpleName();

    private Context mContext;
    private int mSelectedChildIndex = 0;
    private ArrayList<View> mAllViews = new ArrayList<>();


    public RecycleLinearLayout(Context context) {
        super(context);
        initWindow(context);
    }

    public RecycleLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initWindow(context);
    }

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
        Log.i(TAG, "screenWidth:"+dm.widthPixels+" screenHeight:"+dm.heightPixels);
    }

    private boolean getVisibleOnScreen(View view){
        return view.getLocalVisibleRect(mWindowRect);
    }

    private static final int SCROLL_DURATION = 250;//默认滑动时间250ms
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
        mOverScroller.startScroll(mOverScroller.getFinalX(), mOverScroller.getFinalY(), dx, dy, SCROLL_DURATION);
        Log.i(TAG, "fX:"+mOverScroller.getFinalX() + "  fY:"+mOverScroller.getFinalY());
        invalidate();//保证computeScroll()执行
    }

    private void scrollToTop(View view){
        if(view != null){
            int[] location = new int[]{0, 0};
            view.getLocationOnScreen(location);
            Log.i(TAG, "top:"+location[1]);
            Log.i(TAG, "margin:"+mContext.getResources().getDimensionPixelOffset(R.dimen.banner_margin_top));
            smoothScrollBy(0, location[1]-mContext.getResources().getDimensionPixelOffset(R.dimen.banner_margin_top));
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
//        if(event.getAction()==KeyEvent.ACTION_UP && //处理顶部跳入到该view时的焦点
//                keyCode==KeyEvent.KEYCODE_DPAD_DOWN){
//            getChildAt(0).requestFocus();
//        }
        if(event.getAction() == KeyEvent.ACTION_DOWN){
        } else if(event.getAction() == KeyEvent.ACTION_UP){
            if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN || keyCode==KeyEvent.KEYCODE_DPAD_UP){
                View view = getFocusedChild();
                scrollToTop(/*findView(view)*/view);
                Log.i(TAG, "up view:"+view);
            }
        }
        return false;
    }
}
