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
import com.orhanobut.logger.Logger;

import java.util.List;

import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.R;

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

        //最后一个按右键抖动动画
        lastItemView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (event.getAction()){
                    case KeyEvent.ACTION_DOWN:
                        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                            YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(v);
                        }
                        break;
                }
                return false;
            }
        });

        TextView firstItemView = (TextView) linearContainer.getChildAt(0);
        firstItemView.setNextFocusLeftId(firstItemView.getId());


        TextView initFocus = (TextView) linearContainer.getChildAt(initSelected);
        if (initFocus != null) {
            mSelectedIndex = initSelected;
            mFocusedIndex = initSelected;
            changeViewStatus(initFocus, ViewStatus.Focused);
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

//        if (i == 0) {
//            item.setNextFocusLeftId(R.id.libbeaver_tab_item);
//        } else if (i == dataSize - 1) {
//            item.setNextFocusRightId(R.id.libbeaver_tab_item + dataSize - 1);
//        }

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

        if (currentRect[0] - view.getWidth() - tabSpace
                <= mTabMargin) { // current view left less than left margin
            if (mSelectedIndex == 0) {
                if (currentRect[0] - tabSpace > mTabMargin + tabSpace) {
                    scrollChildPosition(linearContainer.getChildAt(0));
                }
            } else {
                int leftViewWidth = linearContainer.getChildAt(mSelectedIndex - 1).getWidth();
                int[] lastleftRect = new int[2];
                linearContainer.getChildAt(mSelectedIndex - 1).getLocationOnScreen(lastleftRect);
                smoothScrollBy(
                        -(currentRect[0] - (lastleftRect[0] + leftViewWidth) + leftViewWidth / 2), 0);
            }
            // 右滑
        } else if (currentRect[0] + view.getWidth()
                >= baseRightX) { // current view right more than right margin
            if (mSelectedIndex == linearContainer.getChildCount() - 1) {
                smoothScrollBy(currentRect[0] + view.getWidth() - baseRightX, 0);
            } else {
                int rightViewWidth = linearContainer.getChildAt(mSelectedIndex + 1).getWidth();
                smoothScrollBy(currentRect[0] + view.getWidth() - baseRightX + rightViewWidth / 2, 0);
            }
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
                changeViewStatus(textView, ViewStatus.Focused);
            }
        } else {
            changeViewStatus(textView, ViewStatus.UnFocused);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Log.d(TAG, "onkey: " + "view: " + v + " event: " + event);
        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                isOnKeyDown = true;
                isOnViewClick = false;
                //按下按键,频道栏获取焦点
                Log.d(TAG, "onKey ACTION_DOWN");
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {

                }
                break;
            case KeyEvent.ACTION_UP:
                //松开按鍵,频道栏获取焦点
                Log.d(TAG, "onKey ACTION_UP");
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    TextView lastSelectedView = (TextView) linearContainer.getChildAt(mSelectedIndex);
                    Log.d(TAG, "onKey ACTION_UP: " + mSelectedIndex);
                    lastSelectedView.requestFocus();
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

    private void changeViewStatus(TextView view, ViewStatus status) {
        switch (status) {
            case Hovered:
                changeViewDPadFocusStatus(view, true, false);
                break;
            case UnHovered:
                changeViewDPadFocusStatus(view, false, false);
                break;
//            case Selected:
//                changeViewSelectedStatus(view, true);
//                break;
//            case UnSelected:
//                changeViewSelectedStatus(view, false);
//                break;
            case Focused:
                changeViewDPadFocusStatus(view, true, true);
                break;
            case UnFocused:
                changeViewDPadFocusStatus(view, false, true);
                break;
        }
//        for (int i = 0; i < linearContainer.getChildCount(); i++){
//            if (i == mSelectedIndex){
//                ((TextView)linearContainer.getChildAt(i)).setTextColor(textSelectColor);
//            }else if (i == mFocusedIndex){
//                ((TextView)linearContainer.getChildAt(i)).setTextColor(textFocusColor);
//            }else {
//                ((TextView)linearContainer.getChildAt(i)).setTextColor(textDefaultColor);
//            }
//        }
    }


    // 五向键获取焦点
    /**
     * @param view
     * @param isFocus
     * @param isDpad 是否回调
     */
    private void changeViewDPadFocusStatus(TextView view, boolean isFocus, boolean isDpad) {

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
//                return;
            }
            zoomIn(view);
            view.setTextColor(textFocusColor);
            view.setBackgroundResource(R.drawable.channel_indicator_focus);
            if (!view.hasFocus()){
                view.requestFocus();
            }
            //五向键操作
            if (onItemSelectedListener != null && ((isOnKeyDown && isDpad)|| isOnViewClick)) {
                //清除上一次的选中效果
                TextView textView = (TextView) linearContainer.getChildAt(mSelectedIndex);
                if (textView != null){
                    textView.setTextColor(textDefaultColor);
                }
                mSelectedIndex = (int) view.getTag();
                Logger.t(TAG).d("onItemSelectedListener.onItemSelected(view, mSelectedIndex);");
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
                //处理选中态
                if (mSelectedIndex != mFocusedIndex) {
                    TextView textView = (TextView) linearContainer.getChildAt(mSelectedIndex);
                    if (textView != null) {
                        textView.setTextColor(textSelectColor);
                    }
                }

                //空鼠获取焦点
                if (!view.hasFocus()){
                    Logger.t(TAG).d("空鼠获取焦点");
                    view.setHovered(true);
                    view.requestFocus();
                }
            }

        } else {

            Logger.t(TAG).d("changeViewDPadFocusStatus: 丢失焦点" + " isOnKeyDown: " + isOnKeyDown + " isDpad:" + isDpad + " isOnViewClick:" + isOnViewClick);
            //处理选中态
            //无向键
            if ((isOnKeyDown && isDpad)|| isOnViewClick){
                view.setTextColor(textDefaultColor);
//                TextView textView = (TextView) linearContainer.getChildAt(mSelectedIndex);
//                if (textView != null){
//                    textView.setTextColor(textDefaultColor);
//                }
            }else if (!isOnKeyDown && !isDpad){
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
            zoomOut(view);
            view.setBackgroundResource(android.R.color.transparent);

            // 隐藏view获取焦点
            if (HomeActivity.mHoverView != null) {
                HomeActivity.mHoverView.requestFocus();
            }
        }


//        if (!isDpad){
//            if (mSelectedIndex != mFocusedIndex) {
//                view.setTextColor(textSelectColor);
//            }
//            for (int i = 0; i < linearContainer.getChildCount(); i++) {
//                if (i != mSelectedIndex && i != mFocusedIndex) {
//                    TextView textView = (TextView) linearContainer.getChildAt(i);
//                    textView.setTextColor(textDefaultColor);
//                }
//            }
//        }
    }

//    private void changeViewSelectedStatus(TextView view, boolean isSelected) {
//        if (isSelected) {
//            view.setSelected(true);
//            view.setTextColor(textSelectColor);
//        } else {
//            view.setSelected(false);
//            view.setTextColor(textDefaultColor);
//        }
//    }

    // 缩小
    private void zoomOut(View view) {
        Animator animator =
                AnimatorInflater.loadAnimator(
                        view.getContext(), tv.ismar.searchpage.R.animator.scalein_recomment_poster);
        animator.setTarget(view);
        animator.start();
    }

    // 放大
    private void zoomIn(View view) {
        Animator animator =
                AnimatorInflater.loadAnimator(
                        view.getContext(), tv.ismar.searchpage.R.animator.scaleout_recommend_poster);
        animator.setTarget(view);
        animator.start();
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        TextView textView = (TextView) v;
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                isOnKeyDown = false;
                isOnViewClick = false;
                v.setHovered(true);
                if (isCanScroll){
                    isCanScroll = false;
                    changeViewStatus(textView, ViewStatus.Hovered);
                    mScrollEventHandler.sendEmptyMessageDelayed(0, 500);
                }
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                changeViewStatus(textView, ViewStatus.UnHovered);
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
//        Selected,
//        UnSelected,
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
            changeViewStatus(initFocus, ViewStatus.Focused);
        }
    }

    //空鼠获取焦点时，禁止滑动
//    @Override
//    public void requestChildFocus(View child, View focused) {
//        super.requestChildFocus(child, linearContainer);
//    }
}
