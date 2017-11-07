package tv.ismar.homepage.widget.scroll;

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

import tv.ismar.homepage.widget.scroll.listener.OnItemClickListener;
import tv.ismar.homepage.widget.scroll.listener.OnItemFocuseChangeListener;
import tv.ismar.homepage.widget.scroll.listener.OnScrollListener;

/**
 * @AUTHOR: xi
 * @DATE: 2017/7/31
 * @DESC: 滚动容器
 */

public abstract class ScrollContainer extends LinearLayout implements ScrollAction {

    protected final String TAG = this.getClass().getSimpleName();

    private static final int SCROLL_DURATION = 250;//默认滑动时间250ms
    private OverScroller mOverScroller = null;
    private int mScreenWidth = 0;//屏幕宽度
    private int mScreenHeight = 0;
    private int mMarginL = 0;//左边距
    private int mMarginT = 0;//上边距
    private int mMarginR = 0;//右边距
    private int mMarginB = 0;//下边距

    private int mWidth = 0;
    private int mHeight = 0;
    private AbsBannerAdapter mAdapter = null;
    private OnItemClickListener mItemClickListener = null;
    private OnItemFocuseChangeListener mItemFocuseChangeListener = null;
    private OnScrollListener mScrollListener = null;

    protected int mFirstChildPosition =0;
    protected int mLastChildPosition = 0;
    protected int mSelectedChildIndex = 0;

    public ScrollContainer(Context context) {
        super(context);
        initScroll(null);
    }

    public ScrollContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScroll(attrs);
    }

    private void initScroll(AttributeSet attrs){
        this.mOverScroller = new OverScroller(getContext());
//        MarginLayoutParams params = generateLayoutParams(attrs);
//        mMarginL = params.leftMargin;
//        mMarginR = params.rightMargin;
//        mMarginT = params.topMargin;
//        mMarginB = params.bottomMargin;
        getScreenSize();

        setClipChildren(false);
        setOrientation((this instanceof VerticalBanner)? LinearLayout.VERTICAL: LinearLayout.HORIZONTAL);
    }

    private void getScreenSize(){
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        Log.i(TAG, "mScreenWidth:"+mScreenWidth+"  mScreenHeight:"+mScreenHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int[] location = new int[]{0, 0};
        getLocationOnScreen(location);
        mMarginL = location[0];
        mMarginT = location[1];
        Log.i(TAG, "mMarginL:"+mMarginL+ " mMarginT:"+mMarginT +" mMarginR:"+mMarginR+ " mMarginB:"+mMarginB);
    }

    @Override
    public void computeScroll() {
        if(mOverScroller.computeScrollOffset()){//判断滚动是否完毕
            scrollTo(mOverScroller.getCurrX(), mOverScroller.getCurrY());//调用view方法执行真实的滑动动作
            postInvalidate();
        }
        super.computeScroll();
    }

    public int getSelectedChildIndex(){
        return mSelectedChildIndex;
    }

    private void smoothScrollBy(int dx, int dy){
        Log.i(TAG, "dx:"+dx+"  dy:"+dy);
        mOverScroller.startScroll(mOverScroller.getFinalX(), mOverScroller.getFinalY(), dx, dy, SCROLL_DURATION);
        Log.i(TAG, "fX:"+mOverScroller.getFinalX() + "  fY:"+mOverScroller.getFinalY());
        invalidate();//保证computeScroll()执行
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if(!canHover(keyCode)){
                return true;
            }
            keyDown(keyCode, event);
            //Item单击事件
            if(keyCode == KeyEvent.KEYCODE_ENTER){
                if(mItemClickListener != null){
                    mItemClickListener.onItemClick(getChildAt(mSelectedChildIndex), mSelectedChildIndex);
                }
            }
        } else if(event.getAction() == KeyEvent.ACTION_UP){
            keyUp(keyCode, event);
        }
        return super.dispatchKeyEvent(event);
    }

    protected void keyUp(int keyCode, KeyEvent event){}

    protected void keyDown(int keyCode, KeyEvent event){}

    /*焦点在两端部下发左右事件*/
    protected boolean canHover(int keyCode){
        return true;
    }

    protected boolean canScroll(){
        return true;
    }

    @Override
    public void scrollX(int dx) {
        smoothScrollBy(dx, 0);
    }

    @Override
    public void scrollY(int dy) {
        smoothScrollBy(0, dy);
    }

    @Override
    public void scrollToCenterX() {
        int offSet = getCenterOffSet()[0];
        int firstLocation = getFirstChildLocation()[0];
        int lastLocation = getLastChildLocation()[0];
        int lastWidth = getChildAt(getChildCount()-1).getWidth();
        if(offSet>0 && lastLocation-offSet<mScreenWidth) {//向左滑动
            Log.i(TAG, "lastLocation:"+lastLocation+" lastWidth:"+lastWidth+ " mMarginL:"+mMarginL);
            smoothScrollBy(lastLocation+lastWidth+mMarginL-mScreenWidth, 0);
            return;
        }
        if(offSet<0 && firstLocation-offSet>0) {//向右滑动
            Log.i(TAG, "firstLocation:"+firstLocation + " mMarginR:"+mMarginR);
            smoothScrollBy(firstLocation-mMarginR, 0);
            return;
        }
        smoothScrollBy(offSet, 0);
    }


    @Override
    public void scrollToCenterY() {
        if(!canScroll()) return;
        int offSet = getCenterOffSet()[1];
        int firstLocation = getFirstChildLocation()[1];
        int lastLocation = getLastChildLocation()[1];
        int lastHeight = getChildAt(getChildCount()-1).getHeight();
        if(offSet>0 && lastLocation-offSet<mScreenHeight) {//向上滑动
            smoothScrollBy(0, lastLocation+lastHeight+mMarginT-mScreenHeight);
            return;
        }
        if(offSet<0 && firstLocation-offSet>0) {//向下滑动
            smoothScrollBy(0, firstLocation-mMarginB);
            return;
        }
        smoothScrollBy(0, offSet);
    }

    /*获取第一个元素屏幕坐标*/
    private int[] getFirstChildLocation(){
        if(getChildCount() <= 0) return null;

        int[] location = new int[]{0, 0};
        View view = getChildAt(0);
        view.getLocationOnScreen(location);
        return location;
    }

    /*获取最后一个元素屏幕坐标*/
    private int[] getLastChildLocation(){
        if(getChildCount() <= 0) return null;

        int[] location = new int[]{0, 0};
        View view = getChildAt(getChildCount()-1);
        view.getLocationOnScreen(location);
        return location;
    }

    /*设置适配器*/
    public void setAdapter(AbsBannerAdapter adapter){
        this.mAdapter = adapter;
        notifyInflateView();
    }

    /*通知加载子视图*/
    private void notifyInflateView(){
        if(mAdapter != null){
            for(int i=0; i<mAdapter.getCount(); i++){
                View childView = mAdapter.getView(i);
                addView(childView);
            }
        }
    }

    /*检测需要废弃的view*/
    private synchronized void checkScrapView(){

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = 0;
        mHeight = 0;
        for(int i=0; i<getChildCount(); i++){
            View childView = getChildAt(i);
            measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mWidth = mWidth+childView.getMeasuredWidth();
            mHeight = mHeight+childView.getMeasuredHeight();
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    public void addView(Context context, BannerCell banner){
        if(banner != null){
            addView(banner.createView());
        }
    }

    /*添加头视图*/
    public void addHeaderView(View view){
        addView(view, 0);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mItemClickListener = listener;
    }

    public void setOnItemFocuseChangeListener(OnItemFocuseChangeListener listener){
        this.mItemFocuseChangeListener = listener;
    }

    public void setOnScrollListener(OnScrollListener listener){
        this.mScrollListener = listener;
    }

    private void setFocuseListener(View view, final int position){
        view.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(mItemFocuseChangeListener != null){
                    mItemFocuseChangeListener.onFocuseChange(v, position, hasFocus);
                }
            }
        });
    }
    /*判断纵向是否超出屏幕范围*/
    protected boolean outScreenInVertical(){
        int[] location = new int[]{0, 0};
        getLocationOnScreen(location);
        if(location[1]+getHeight()<0 || location[1]>mScreenHeight){
            return true;
        }
        return false;
    }

    /*判断横向是否超出屏幕范围*/
    protected boolean outSreenInHorizontal(){
        int[] location = new int[]{0, 0};
        getLocationOnScreen(location);
        if(location[0]+getWidth()<0 || location[0]>mScreenWidth){
            return true;
        }
        return false;
    }

    /*计算距离屏幕中心的距离*/
    private int[] getCenterOffSet(){
        int[] offSet = new int[]{0, 0};
        android.view.View child = getChildAt(mSelectedChildIndex);
        Log.i(TAG, "selectChildIndex:"+mSelectedChildIndex);
        int childW = child.getWidth();
        int childH = child.getHeight();
        child.getLocationOnScreen(offSet);
        Log.i(TAG, "child[0]="+offSet[0]+"  child[1]="+offSet[1]);
        Log.i(TAG, "mScreenWidth:"+mScreenWidth+" mScreenHeight:"+mScreenHeight);
        offSet[0] = -(mScreenWidth/2 - offSet[0] - childW/2);
        offSet[1] = -(mScreenHeight/2 - offSet[1] - childH/2);

        Log.i(TAG, "offSet[0]:"+offSet[0]+" offSet[1]:"+offSet[1]);
        return offSet;
    }

    /*计算选中子view距离顶部的距离*/
    private int getTopOffSet(){
        int[] offSet = new int[]{0, 0};
        android.view.View child = getChildAt(mSelectedChildIndex);
        child.getLocationOnScreen(offSet);

        return offSet[1];
    }

    /*选中子view滑动到顶部*/
    @Override
    public void scrollToTop() {
        int offSet = getTopOffSet();

        int firstLocation = getFirstChildLocation()[1];
        int lastLocation = getLastChildLocation()[1];
        int lastHeight = getChildAt(getChildCount()-1).getHeight();

        Log.i(TAG, "lastLocation:"+lastLocation+" lastHeight:"+lastHeight+ " mMarginL:"+mMarginT);
//        if(offSet>0 && lastLocation-offSet<mScreenHeight) {//向下滑动
//            smoothScrollBy(0, lastLocation+lastHeight+mMarginT-mScreenHeight);
//            return;
//        }
        smoothScrollBy(0, offSet-mMarginT);
    }
}
