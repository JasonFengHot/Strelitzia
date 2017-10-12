package tv.ismar.homepage.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.ScrollView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

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
//    private ScrollView mScrollView;
    private int mScreenHeight;
    private int mSelectedChildIndex = 0;
    private ArrayList<View> mAllViews = new ArrayList<>();


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
        mOverScroller.startScroll(mOverScroller.getFinalX(), mOverScroller.getFinalY(), dx, dy, SCROLL_DURATION);
        invalidate();//保证computeScroll()执行
    }

    private void scrollToVisiable(View view){
        if(view != null){
            int[] location = new int[]{0, 0};
            view.getLocationOnScreen(location);
            smoothScrollBy(0, location[1]-300);
        }
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

    private View mLastView;//记录焦点
    private static final int mScrollHeight = 10000;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return excuteKeyEvent(event);
    }

    private boolean excuteKeyEvent(KeyEvent event){
        int keyCode = event.getKeyCode();
        //测试删除view是否可以减少内存
//        if(event.getAction()==KeyEvent.ACTION_UP
//                && keyCode==KeyEvent.KEYCODE_DPAD_DOWN){
//            removeViewAt(0);
//            return true;
//        }
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            mLastView = getFocusedChild();
        }
        Log.i(TAG, "action:"+event.getAction()+" keyCode:"+keyCode);
        if(event.getAction() == KeyEvent.ACTION_UP){
            if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN || keyCode==KeyEvent.KEYCODE_DPAD_UP
                    || keyCode==KeyEvent.KEYCODE_DPAD_LEFT || keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
                View view = getFocusedChild();
                if(view == mLastView) return super.dispatchKeyEvent(event);//banner抖动问题
                int key = (int) view.getTag();
                int tag = (int) view.getTag(key);
                boolean canScroll = tag>>30==1;//1可滑动，0不可滑动
                int position = (tag<<2)>>2;
//                mHolder.onCreateView(position, keyCode);
                Log.i(TAG, "key:"+key+" canScroll:"+canScroll+" position:"+position);
                if(!canScroll){//限制滑动
                    Log.i(TAG, "canScroll");
                    if(position-1 < 0) return super.dispatchKeyEvent(event);//将不可滑动的banner和前一个banner绑定为一个banner
//                    mScrollView.setBottom(mScrollHeight+mScreenHeight);
                    scrollToTop(getChildAt(position-1));
                    return super.dispatchKeyEvent(event);
                }
                //滑动处理
                if(position==getChildCount()-1){
                    Log.i(TAG, "scrollToVisiable");
//                    mScrollView.setBottom(mScrollHeight+mScreenHeight);
                    scrollToVisiable(view);
                    YoYo.with(Techniques.VerticalShake).duration(1000).playOn(view);
                } else {
                    Log.i(TAG, "scrollToTop");
//                    mScrollView.setBottom(mScreenHeight);
                    scrollToTop(view);
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private ViewHolder mHolder;

    public void setHolder(ViewHolder holder){
        this.mHolder = holder;
    }

    public interface ViewHolder {
        void onCreateView(int position, int orientation);
    }
}
