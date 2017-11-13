package tv.ismar.homepage.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.List;

import tv.ismar.homepage.R;

import static android.view.MotionEvent.BUTTON_PRIMARY;


public class HorizontalTabView extends HorizontalScrollView
        implements View.OnClickListener,
        View.OnFocusChangeListener,
        View.OnKeyListener,
        View.OnHoverListener,
        View.OnTouchListener {

    private static final String TAG = "LH/HorizontalTabView";

    private int tabHeight;
    private int tabSpace;
    private int startEndPadding;
    private int textSize;
    private int textDefaultColor;
    private int textSelectColor;
    private int textFocusColor;

    private Context mContext;
    private LinearLayout linearContainer;
    private int mSelectedIndex = 1;
    private int mFocusedIndex = 1;
    private int mTabMargin;
    private OnItemSelectedListener onItemSelectedListener;
    private OnItemClickedListener onItemClickedListener;
    public View leftbtn;
    public View rightbtn;

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    private boolean isCanScroll = true;
    private boolean isOnKeyDown = false;
    private boolean isOnViewClick = false;

    Handler mScrollEventHandler =
            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    isCanScroll = true;
                }
            };

    public HorizontalTabView(Context context) {
        this(context, null);
    }

    public HorizontalTabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HorizontalTabView);
        tabHeight = typedArray.getDimensionPixelSize(R.styleable.HorizontalTabView_tvTabHeight, 0);
        tabSpace =
                typedArray.getDimensionPixelSize(R.styleable.HorizontalTabView_tvTabSpace, dp2px(10));
        startEndPadding =
                typedArray.getDimensionPixelSize(
                        R.styleable.HorizontalTabView_tvTabStartEndPadding, dp2px(16));
        textSize =
                typedArray.getDimensionPixelSize(R.styleable.HorizontalTabView_tvTabTextSize, dp2px(18));
        textDefaultColor =
                typedArray.getColor(R.styleable.HorizontalTabView_tvTabTextColor, Color.WHITE);
        textSelectColor =
                typedArray.getColor(R.styleable.HorizontalTabView_tvTabSelectTextColor, Color.WHITE);
        textFocusColor =
                typedArray.getColor(R.styleable.HorizontalTabView_tvTabFocusTextColor, Color.WHITE);
        typedArray.recycle();

        setFocusable(false);
        setFocusableInTouchMode(false);
        setHorizontalFadingEdgeEnabled(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setClipToPadding(false);
        setClipChildren(false);
        initializeView(context);
    }

    private void initializeView(Context context) {
        mContext = context;
        LayoutParams layoutParams =
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        linearContainer = new LinearLayout(mContext);
        linearContainer.setOrientation(LinearLayout.HORIZONTAL);
        linearContainer.setClipChildren(false);
        linearContainer.setClipChildren(false);
        linearContainer.setLayoutParams(layoutParams);
        addView(linearContainer);
    }

    public void addAllViews(List<Tab> datas, int initSelected) {
        if (datas.isEmpty() || initSelected < 0 || initSelected > datas.size()) {
            return;
        }
        int i = 0;
        for (Tab tab : datas) {
            addItemView(datas.size(), i, tab.getTabTitle());
            i++;
        }

        //最后一个焦点不能向右移动
        TextView lastItemView = (TextView) linearContainer.getChildAt(datas.size() -1);
        lastItemView.setNextFocusRightId(lastItemView.getId());

/*delete by dragontec for bug 4354 start*/
        //last item的监听器不能单独追加，会造成不进入该类的onKey函数，所以右键抖动动画移动到onKey执行
//        //最后一个按右键抖动动画
//        lastItemView.setOnKeyListener(new OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                switch (event.getAction()){
//                    case KeyEvent.ACTION_DOWN:
//                        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
//                            YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(v);
//                        }
//                        break;
//                }
//                return false;
//            }
//        });
/*delete by dragontec for bug 4354 end*/

        TextView firstItemView = (TextView) linearContainer.getChildAt(0);
        firstItemView.setNextFocusLeftId(firstItemView.getId());


        TextView initFocus = (TextView) linearContainer.getChildAt(initSelected);
        if (initFocus != null) {
            mSelectedIndex = initSelected;
            mFocusedIndex = initSelected;
            initFocus.requestFocus();
            initFocus.requestFocusFromTouch();
            changeViewStatus(initFocus, ViewStatus.Focused);
/*add by dragontec for bug 4298 start*/
            initFocus.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                    view.removeOnLayoutChangeListener(this);
                    int baseRightX = getWidth() - mTabMargin;
                    int[] currentRect = new int[2];
                    view.getLocationOnScreen(currentRect);
                    if (currentRect[0]  - tabSpace <= 0) { // current view left less than left margin
                        //暂定不考虑进入画面时，左侧超出屏幕
                    } else if (currentRect[0] + view.getWidth() >= baseRightX) { // current view right more than right margin
                        scrollBy(currentRect[0] + view.getWidth() - baseRightX, 0);
                    }
                }
            });
/*add by dragontec for bug 4298 end*/
        }
    }

    private void addItemView(int dataSize, int i, String label) {
        LinearLayout.LayoutParams layoutParams;
        layoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        final TextView item;
        if (label.equals("搜索")) {
            item =
                    (TextView) LayoutInflater.from(getContext()).inflate(R.layout.item_channel_search, null);
            item.setCompoundDrawablePadding(10);
        } else {
            item = new TextView(mContext);
        }
        if (tabHeight > 0) {
            item.setHeight(tabHeight);
        }
        item.setGravity(Gravity.CENTER);
        item.setId(View.generateViewId());
        item.setTag(i);
        item.setText(label);
        item.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        item.setTextColor(textDefaultColor);
        item.setFocusable(true);
        item.setClickable(true);
        item.setOnClickListener(this);
        item.setOnFocusChangeListener(this);
        item.setOnKeyListener(this);
        item.setOnHoverListener(this);
        item.setOnTouchListener(this);

        int[] size = getTextSize(item);
        mTabMargin = startEndPadding;
        if (i == 0) {
            layoutParams.leftMargin = mTabMargin;
        } else if (i == dataSize - 1) {
            layoutParams.leftMargin = tabSpace;
            layoutParams.rightMargin = mTabMargin;
        } else {
            layoutParams.leftMargin = tabSpace;
        }

        item.setLayoutParams(layoutParams);
        linearContainer.addView(item);
    }

    private int[] getTextSize(TextView textView) {
        int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        textView.measure(spec, spec);
        int measuredWidth = textView.getMeasuredWidth();
        int measuredHeight = textView.getMeasuredHeight();

        return new int[]{measuredWidth, measuredHeight};
    }

    private void scrollChildPosition(View view) {
        int baseRightX = getWidth() - mTabMargin;
        int[] currentRect = new int[2];
        view.getLocationOnScreen(currentRect);

        if (mFocusedIndex == 0) {
            if (canScrollHorizontally(FOCUS_LEFT)) {
                fullScroll(FOCUS_LEFT);
            }
        } else if (mFocusedIndex == linearContainer.getChildCount() - 1) {
            if (canScrollHorizontally(FOCUS_RIGHT)) {
                fullScroll(FOCUS_RIGHT);
            }
        } else if (currentRect[0]  - tabSpace <= 0) { // current view left less than left margin
            Log.d(TAG, "channel: 左滑");
            View lastIndexView = linearContainer.getChildAt(mFocusedIndex - 1);
            int lastIndexViewWidth = lastIndexView.getWidth();
            int[] lastIndexViewRect = new int[2];
            lastIndexView.getLocationOnScreen(lastIndexViewRect);
            smoothScrollBy(-(tabSpace + lastIndexViewWidth / 2), 0);
        } else if (currentRect[0] + view.getWidth() >= baseRightX) { // current view right more than right margin
            Log.d(TAG, "channel: 右滑");
            int rightViewWidth = linearContainer.getChildAt(mFocusedIndex + 1).getWidth();
            smoothScrollBy(currentRect[0] + view.getWidth() - baseRightX + rightViewWidth / 2, 0);
        }
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public void setTextColor(int textColor) {
        this.textDefaultColor = textColor;
    }

    private int dp2px(float dp) {
        return (int)
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    public void onClick(View v) {
        Log.d("onClick", "position:" + (int)v.getTag());
        isOnViewClick = true;
        changeViewDPadFocusStatus((TextView) v, true, true);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.d("onFocusChange","view: " + v + " hasFocus: " + hasFocus);
        TextView textView = (TextView) v;
        if (hasFocus) {
            //获取焦点的位置
            mFocusedIndex = (int) textView.getTag();
            if (v.isHovered()){
                //空鼠标获取焦点
                changeViewStatus(textView, ViewStatus.Hovered);
            }else {
                //确保焦点上移时还是在当前tab上，不会切换频道
                if(tag&&linearContainer.indexOfChild(v)!=mSelectedIndex){
                    isOnKeyDown=true;
                    changeViewStatus((TextView) linearContainer.getChildAt(mSelectedIndex), ViewStatus.Focused);
                }else {
                    changeViewStatus(textView, ViewStatus.Focused);
                }
            }
        } else {
            changeViewStatus(textView, ViewStatus.UnFocused);
        }
    }

    boolean tag=true;
    @Override
    public boolean onKey(final View v, int keyCode, KeyEvent event) {
        Log.d(TAG, "onkey: " + "view: " + v + " event: " + event);
        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT||keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
                    tag=false;
                }
                isOnKeyDown = true;
                isOnViewClick = false;
                Log.d(TAG, "onKey ACTION_DOWN");
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    tag=true;
                    Log.d(TAG, "KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP");
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            TextView itemView = (TextView) v;
//                            int position = (int) itemView.getTag();
//                            if (position == mSelectedIndex){
//                                itemView.setTextColor(textSelectColor);
//                                itemView.setBackgroundResource(android.R.color.transparent);
//                            }
//                        }
//                    }, 50);
                }
/*delete by dragontec for bug 4354 start*/
                else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                    TextView lastItemView = (TextView) linearContainer.getChildAt(linearContainer.getChildCount() - 1);
                    if (lastItemView == v) {
                        YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(v);
                    }
                }
/*delete by dragontec for bug 4354 end*/
                break;
            case KeyEvent.ACTION_UP:
                Log.d(TAG, "onKey ACTION_UP");
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    Log.d(TAG, "onKey ACTION_UP: " + mSelectedIndex);
                    return true;
                }
                break;
        }
        return false;

    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        Log.d("MotionEvent", event.toString());
        return super.dispatchGenericMotionEvent(event);
    }
	
	/*modify by dragontec start 增加返回值*/
    private boolean changeViewStatus(TextView view, ViewStatus status) {
        boolean needSendMessage = false;
        switch (status) {
            case Hovered:
                needSendMessage = changeViewDPadFocusStatus(view, true, false);
                break;
            case UnHovered:
                needSendMessage = changeViewDPadFocusStatus(view, false, false);
                break;
//            case Selected:
//                changeViewSelectedStatus(view, true);
//                break;
//            case UnSelected:
//                changeViewSelectedStatus(view, false);
//                break;
            case Focused:
                needSendMessage = changeViewDPadFocusStatus(view, true, true);
                break;
            case UnFocused:
                needSendMessage = changeViewDPadFocusStatus(view, false, true);
                break;
            default:
                break;
        }
        return needSendMessage;
    }
		/*modify by dragontec end 增加返回值*/


    // 五向键获取焦点
    /**
     * @param view
     * @param isFocus
     * @param isDpad 是否回调
     */
	 /*modify by dragontec start 增加返回值*/
    private boolean  changeViewDPadFocusStatus(TextView view, boolean isFocus, boolean isDpad) {
        boolean needSendMessage = false;
		/*modify by dragontec end 增加返回值*/

        // 五向键时禁止所有空鼠
//        if (isOnKeyDown && isDpad) {

            for (int i = 0; i < linearContainer.getChildCount(); i++) {
                View itemView = linearContainer.getChildAt(i);
                itemView.setHovered(false);
            }
//        }

        if (isFocus) {
            // 获取焦点
            if (!view.hasFocus()){
                view.requestFocus();
                view.requestFocusFromTouch();
            }
/*modify by dragontec for bug 4048 start*/
//            zoomIn(view);
            if (view.getTag(R.id.horizontal_text_view_zoom_in) == null) {
                zoomIn(view);
            } else {
                if (!(Boolean) view.getTag(R.id.horizontal_text_view_zoom_in)) {
                    zoomIn(view);
                }
            }
/*modify by dragontec for bug 4048 end*/
            view.setTextColor(textFocusColor);
            view.setBackgroundResource(R.drawable.channel_indicator_focus);
            //五向键操作
            if (onItemSelectedListener != null && ((isOnKeyDown && isDpad)|| isOnViewClick)) {
                //清除上一次的选中效果
                TextView textView = (TextView) linearContainer.getChildAt(mSelectedIndex);
                if (textView != null){
                    textView.setTextColor(textDefaultColor);
/*add by dragontec for bug 4048 start*/
                    if (mSelectedIndex!= (int)view.getTag() && textView.getTag(R.id.horizontal_text_view_zoom_in) != null && (Boolean) textView.getTag(R.id.horizontal_text_view_zoom_in)) {
                        zoomOut(textView);
                    }
/*add by dragontec for bug 4048 end*/
                }
                mSelectedIndex = (int) view.getTag();
                //计算滑动位置
                scrollChildPosition(view);
                if (isOnViewClick){
                    if (onItemClickedListener != null){
                        onItemClickedListener.onItemClicked(view, mSelectedIndex);
                    }
                }else {
                    onItemSelectedListener.onItemSelected(view, mSelectedIndex);
                }
            }else if (onItemSelectedListener != null &&!isOnKeyDown && !isDpad){
                if (isCanScroll){
                    scrollChildPosition(view);
                    isCanScroll = false;
					/*modify by dragontec start 增加返回值*/
                    needSendMessage = true;
					/*modify by dragontec end 增加返回值*/
                }
                //处理选中态
                if (mSelectedIndex != mFocusedIndex) {
                    TextView textView = (TextView) linearContainer.getChildAt(mSelectedIndex);
                    if (textView != null) {
                        textView.setTextColor(textSelectColor);
                    }
                }

                //空鼠获取焦点
                if (!view.hasFocus()){
                    view.setHovered(true);
                    view.requestFocus();
                    view.requestFocusFromTouch();
                }
            }
            view.setTextColor(textFocusColor);
            if(rightbtn!=null){
                if(mFocusedIndex==linearContainer.getChildCount()-1){
                    rightbtn.setVisibility(GONE);
                }else{
                    if(linearContainer.getChildCount()>10)
                        rightbtn.setVisibility(VISIBLE);
                }
            }
        } else {
            //处理选中态
            //无向键
            if ((isOnKeyDown && isDpad)|| isOnViewClick){
/*modify by dragontec for bug 4048 start*/
//                view.setTextColor(textDefaultColor);
                if ((int) view.getTag() == mSelectedIndex){
                    view.setTextColor(textSelectColor);
                } else {
                    view.setTextColor(textDefaultColor);
                }
/*modify by dragontec for bug 4048 end*/
//                TextView textView = (TextView) linearContainer.getChildAt(mSelectedIndex);
//                if (textView != null){
//                    textView.setTextColor(textDefaultColor);
//                }
            }else if (!isOnKeyDown){
                Log.d(TAG,  "changeViewDPadFocusStatus: " + "空鼠丢失焦点");

                //空鼠
                int viewIndex = (int) view.getTag();
                Log.d(TAG,  "changeViewDPadFocusStatus: " + "viewIndex: " + viewIndex);
                Log.d(TAG,  "changeViewDPadFocusStatus: " + "mSelectedIndex: " + mSelectedIndex);
                if (viewIndex == mSelectedIndex){
                    view.setTextColor(textSelectColor);
                }else {
                    view.setTextColor(textDefaultColor);
                }
            }


            // 失去焦点
            view.setHovered(false);
/*modify by dragontec for bug 4048 start*/
//            zoomOut(view);
            if ((int) view.getTag() != mSelectedIndex) {
                zoomOut(view);
            }
/*modify by dragontec for bug 4048 end*/
            view.setBackgroundResource(android.R.color.transparent);

//            // 隐藏view获取焦点
//            if (HomeActivity.mHoverView != null) {
//                HomeActivity.mHoverView.requestFocus();
//                HomeActivity.mHoverView.requestFocusFromTouch();
//            }
        }
		/*modify by dragontec start 增加返回值*/
        return needSendMessage;
		/*modify by dragontec end 增加返回值*/
    }


    // 缩小
    private void zoomOut(View view) {
        Animator animator =
                AnimatorInflater.loadAnimator(
                        view.getContext(), tv.ismar.searchpage.R.animator.scalein_recomment_poster);
        animator.setTarget(view);
        animator.start();
/*add by dragontec for bug 4048 start*/
        view.setTag(R.id.horizontal_text_view_zoom_in, Boolean.valueOf(false));
/*add by dragontec for bug 4048 end*/
    }

    // 放大
    private void zoomIn(View view) {
        Animator animator =
                AnimatorInflater.loadAnimator(
                        view.getContext(), tv.ismar.searchpage.R.animator.scaleout_recommend_poster);
        animator.setTarget(view);
        animator.start();
/*add by dragontec for bug 4048 start*/
        view.setTag(R.id.horizontal_text_view_zoom_in, Boolean.valueOf(true));
/*add by dragontec for bug 4048 end*/
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        TextView textView = (TextView) v;
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            /*delete by dragontec for bug 4169 start*/
        	//case MotionEvent.ACTION_HOVER_MOVE:
			/*delete by dragontec for bug 4169 end*/
                isOnKeyDown = false;
                isOnViewClick = false;
                tag=false;
                v.setHovered(true);
				/*modify by dragontec start 原先代码逻辑不对，原本代码本意是防止频繁scroll，但会造成500ms以内其他分支的代码不执行，已修正*/
                boolean needSendMessage = changeViewStatus(textView, ViewStatus.Hovered);
                if (needSendMessage){
                    mScrollEventHandler.sendEmptyMessageDelayed(0, 500);
                }
				/*modify by dragontec end 原先代码逻辑不对，原本代码本意是防止频繁scroll，但会造成500ms以内其他分支的代码不执行，已修正*/
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                changeViewStatus(textView, ViewStatus.UnHovered);
                tag=true;
/*add by dragontec for bug 4057 start*/
                if (event.getButtonState() != BUTTON_PRIMARY) {
                    v.clearFocus();
                }
/*add by dragontec for bug 4057 end*/
                break;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
        }
        return false;
    }

    private enum ViewStatus {
        Hovered,
        Focused,
        UnHovered,
        UnFocused,
    }

    public interface OnItemSelectedListener {
        void onItemSelected(View v, int position);
    }

    public interface OnItemClickedListener {
        void onItemClicked(View v, int position);
    }

    public static class Tab {

        private String tabKey;
        private String tabTitle;

        private Tab() {
        }

        public Tab(String key, String title) {
            tabKey = key;
            tabTitle = title;
        }

        public String getTabKey() {
            return tabKey;
        }

        public String getTabTitle() {
            return tabTitle;
        }
    }

    public void setDefaultSelection(int position){
        TextView initFocus = (TextView) linearContainer.getChildAt(position);
        if (initFocus != null) {
            mSelectedIndex = position;
            mFocusedIndex = position;
            initFocus.requestFocus();
            initFocus.requestFocusFromTouch();
            changeViewStatus(initFocus, ViewStatus.Focused);
        }
    }

//    空鼠获取焦点时，禁止滑动
//    @Override
//    public void requestChildFocus(View child, View focused) {
//        super.requestChildFocus(child, linearContainer);
//    }

/*add by dragontec for bug 4225, 4224, 4223 start*/
    public void requestLastFocus() {
        TextView textView = (TextView) linearContainer.getChildAt(mSelectedIndex);
        if (textView != null) {
            textView.requestFocus();
            textView.requestFocusFromTouch();
        }
    }
/*add by dragontec for bug 4225, 4224, 4223 end*/

	/*add by dragontec for fix 焦点错误 start*/
	public View getLastFocusView() {
		return linearContainer.getChildAt(mSelectedIndex);
	}
	/*add by dragontec for fix 焦点错误 end`*/
}
