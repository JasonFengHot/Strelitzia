package tv.ismar.homepage.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
        implements View.OnClickListener, View.OnFocusChangeListener, View.OnKeyListener, View.OnHoverListener, View.OnTouchListener {

    private static final String TAG = "LH/HorizontalTabView";
    private static final int STATE_FOCUS = 0;
    private static final int STATE_LEAVE = 1;

    private Drawable selectedDrawable;
    private int tabHeight;
    private int tabSpace;
    private int startEndPadding;
    private int textSize;
    private int defaultTextColor;
    private int textSelectColor;
    private int textFocusColor;

    private Context mContext;
    private LinearLayout linearContainer;
    private int mSelectedIndex = -1;
    private int mTabMargin;
    private OnItemSelectedListener onItemSelectedListener;
    private int mCurrentState = STATE_LEAVE;
    private int mCurrentPosition = -1;

    private int mClickPosition = -1;

    private boolean isScroll = true;
    Handler mScrollHandler =
            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    isScroll = true;
                }
            };
    private boolean isOnKeyDown = false;

    public HorizontalTabView(Context context) {
        this(context, null);
    }

    public HorizontalTabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.HorizontalTabView);
        selectedDrawable =
                typedArray.getDrawable(R.styleable.HorizontalTabView_tvTabSelectedDrawable);
        tabHeight = typedArray.getDimensionPixelSize(R.styleable.HorizontalTabView_tvTabHeight, 0);
        tabSpace =
                typedArray.getDimensionPixelSize(
                        R.styleable.HorizontalTabView_tvTabSpace, dp2px(10));
        startEndPadding =
                typedArray.getDimensionPixelSize(
                        R.styleable.HorizontalTabView_tvTabStartEndPadding, dp2px(16));
        textSize =
                typedArray.getDimensionPixelSize(
                        R.styleable.HorizontalTabView_tvTabTextSize, dp2px(18));
        defaultTextColor =
                typedArray.getColor(R.styleable.HorizontalTabView_tvTabTextColor, Color.WHITE);
        textSelectColor =
                typedArray.getColor(
                        R.styleable.HorizontalTabView_tvTabSelectTextColor, Color.WHITE);
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

        TextView initFocus = (TextView) linearContainer.getChildAt(initSelected);
        if (initFocus != null) {
            mSelectedIndex = initSelected;
            mClickPosition = initSelected;
            changeViewStatus(initFocus, ViewStatus.Selected);
        }
    }

    private void addItemView(int dataSize, int i, String label) {
        LinearLayout.LayoutParams layoutParams;
        layoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        final TextView item;
        if (label.equals("搜索")) {
            item =
                    (TextView)
                            LayoutInflater.from(getContext())
                                    .inflate(R.layout.item_channel_search, null);
            item.setCompoundDrawablePadding(10);
        } else {
            item = new TextView(mContext);
        }
        if (tabHeight > 0) {
            item.setHeight(tabHeight);
        }
        item.setGravity(Gravity.CENTER);
        item.setId(R.id.libbeaver_tab_item + i);
        item.setTag(i);
        item.setText(label);
        item.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        item.setTextColor(defaultTextColor);
        item.setFocusable(true);
//        item.setFocusableInTouchMode(true);
        item.setOnClickListener(this);
        item.setOnFocusChangeListener(this);
        item.setOnKeyListener(this);
        item.setOnHoverListener(this);
        item.setOnTouchListener(this);

        if (i == 0) {
            item.setNextFocusLeftId(R.id.libbeaver_tab_item);
        } else if (i == dataSize - 1) {
            item.setNextFocusRightId(R.id.libbeaver_tab_item + dataSize - 1);
        }

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

    public void setSelectedPosition(int position, boolean requestFocus) {
        TextView currentTextView = (TextView) linearContainer.getChildAt(position);
        if (currentTextView != null) {
            changeSelection(currentTextView, false);
            if (requestFocus) {
                currentTextView.requestFocus();
            }
        }
    }

    public int getSelectedPosition() {
        if (linearContainer == null) {
            return -1;
        }
        return mSelectedIndex;
    }

    private void changeLostFocusStatus(View view) {
        if (view != null) {
            TextView lastClickView = (TextView) linearContainer.getChildAt(mClickPosition);
            if (view == lastClickView) {
                lastClickView.setBackgroundResource(android.R.color.transparent);
                lastClickView.setTextColor(textSelectColor);
            } else {
                TextView textView = (TextView) view;
                textView.setBackgroundResource(android.R.color.transparent);
                textView.setTextColor(defaultTextColor);
            }
        }
    }

    private void changeGainFocusStatus(View view) {
        if (view != null) {
            TextView textView = (TextView) view;
            textView.setBackground(selectedDrawable);
            textView.setTextColor(textFocusColor);
        }
    }

    public void changeSelection(final View v, final boolean callItemListener) {
        int tempIndex = (int) v.getTag();
        if (mSelectedIndex == tempIndex && v.getAnimation() != null) {
            return;
        }
        TextView lastSelectedView = (TextView) linearContainer.getChildAt(mSelectedIndex);
        if (lastSelectedView != null) {
            lastSelectedView.setBackgroundResource(android.R.color.transparent);
            lastSelectedView.setTextColor(defaultTextColor);
        }

        TextView lastClickView = (TextView) linearContainer.getChildAt(mClickPosition);
        if (lastClickView != null && mClickPosition != tempIndex) {
            lastClickView.setBackgroundResource(android.R.color.transparent);
            lastClickView.setTextColor(textSelectColor);
        }

        mSelectedIndex = tempIndex;
        TextView currentTextView = (TextView) linearContainer.getChildAt(mSelectedIndex);
        if (currentTextView == null) {
            Log.e(TAG, "error on null view selected.");
            return;
        }
        currentTextView.setBackground(selectedDrawable);
        currentTextView.setTextColor(textFocusColor);


        scrollChildPosition(v);

        zoomIn(v);

        if (onItemSelectedListener != null && callItemListener) {
            onItemSelectedListener.onItemSelected(v, mSelectedIndex);
        }
    }


    private int[] getTextSize(TextView textView) {
        int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        textView.measure(spec, spec);
        int measuredWidth = textView.getMeasuredWidth();
        int measuredHeight = textView.getMeasuredHeight();
        return new int[]{measuredWidth, measuredHeight};
    }

    private void scrollChildPosition(View view) {
        Logger.t(TAG).d("scrollChildPosition");

        if (linearContainer.getChildCount() < 10) {
            return;
        }

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
                        -(currentRect[0] - (lastleftRect[0] + leftViewWidth) + leftViewWidth / 2),
                        0);
            }
            //右滑
        } else if (currentRect[0] + view.getWidth()
                >= baseRightX) { // current view right more than right margin
            if (mSelectedIndex == linearContainer.getChildCount() - 1) {
                smoothScrollBy(currentRect[0] + view.getWidth() - baseRightX, 0);
            } else {
                int rightViewWidth = linearContainer.getChildAt(mSelectedIndex + 1).getWidth();
                smoothScrollBy(
                        currentRect[0] + view.getWidth() - baseRightX + rightViewWidth / 2, 0);
            }
        }
    }

    public void setSelectedDrawable(Drawable selectedDrawable) {
        this.selectedDrawable = selectedDrawable;
    }

    public void setTabHeight(int tabHeight) {
        this.tabHeight = tabHeight;
    }

    public void setTabSpace(int tabSpace) {
        this.tabSpace = tabSpace;
    }

    public void setStartEndPadding(int startEndPadding) {
        this.startEndPadding = startEndPadding;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public void setTextColor(int textColor) {
        this.defaultTextColor = textColor;
    }

    private int dp2px(float dp) {
        return (int)
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    public void onClick(View v) {
        int tag = (int) v.getTag();
        if (mSelectedIndex == mCurrentPosition) {
            return;
        }
        TextView lastClickView = (TextView) linearContainer.getChildAt(mClickPosition);
        if (lastClickView != null) {
            lastClickView.setBackgroundResource(android.R.color.transparent);
            lastClickView.setTextColor(defaultTextColor);
        }
        mCurrentState = STATE_FOCUS;
        mCurrentPosition = (int) v.getTag();
        mClickPosition = (int) v.getTag();
        changeSelection(v, tag != mSelectedIndex);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Logger.t(TAG).d("onFocusChange hovered: " + v.isHovered());

        int tag = (int) v.getTag();

        //空鼠操作状态
        if (v.isHovered()) {
            if (hasFocus) {
                if (mCurrentState == STATE_LEAVE && mSelectedIndex != tag) {
                    View selectedView = linearContainer.getChildAt(mSelectedIndex);
                    //                    selectedView.requestFocus();
                } else {
                    changeSelection(v, false);
                }

            } else {
                if (mCurrentState != STATE_LEAVE) {
                    changeLostFocusStatus(v);
                    zoomOut(v);
                }
            }
            return;
        }

        if (hasFocus) {
            for (int i = 0; i < linearContainer.getChildCount(); i++) {
                View subView = linearContainer.getChildAt(i);
                subView.setHovered(false);
            }

            if (mCurrentState == STATE_LEAVE && mSelectedIndex != tag) {
                View selectedView = linearContainer.getChildAt(mSelectedIndex);
                selectedView.requestFocus();
            } else {
                changeSelection(v, tag != mSelectedIndex);
            }

        } else {
            if (mCurrentState != STATE_LEAVE) {
                changeLostFocusStatus(v);
                zoomOut(v);
            }
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                isOnKeyDown = true;
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    mCurrentState = STATE_LEAVE;
                    changeLostFocusStatus(v);
                }

                if ((linearContainer.indexOfChild(v) == 0 && keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
                        || (linearContainer.indexOfChild(v) == linearContainer.getChildCount() - 1
                        && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                    YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(v);
                }
                break;
            case KeyEvent.ACTION_UP:
                changeGainFocusStatus(v);
                mCurrentState = STATE_FOCUS;
                break;
        }
        return false;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return super.dispatchGenericMotionEvent(event);
    }

    private void changeViewStatus(TextView view, ViewStatus status) {
        switch (status) {
            case Hovered:
                changeViewHoveredStatus(view, true);
                break;
            case UnHovered:
                changeViewHoveredStatus(view, false);
                break;
            case Selected:
                changeViewSelectedStatus(view, true);
                break;
            case UnSelected:
                changeViewSelectedStatus(view, false);
                break;
            case Focused:
                changeViewDPadFocusStatus(view, true);
                break;
            case UnFocused:
                changeViewDPadFocusStatus(view, false);
                break;
        }
    }

    private void changeViewHoveredStatus(TextView view, boolean isHovered) {
    }

    //五向键获取焦点
    private void changeViewDPadFocusStatus(TextView view, boolean isFocus) {
        //五向键时禁止所有空鼠
        for (int i = 0; i < linearContainer.getChildCount(); i++) {
            View itemView = linearContainer.getChildAt(i);
            itemView.setHovered(false);
        }

        if (isFocus) {
            //获取焦点
            view.requestFocus();

        } else {
            //失去焦点
            //隐藏view获取焦点
            if (HomeActivity.mHoverView != null) {
                HomeActivity.mHoverView.requestFocus();
            }
        }

        //        if (mCurrentState == STATE_LEAVE && mSelectedIndex != tag) {
        //            View selectedView = linearContainer.getChildAt(mSelectedIndex);
        //            selectedView.requestFocus();
        //        } else {
        //            changeSelection(v, tag != mSelectedIndex);
        //        }
    }

    private void changeViewSelectedStatus(TextView view, boolean isSelected) {
        if (isSelected) {
            view.setSelected(true);
            view.setTextColor(textSelectColor);
        } else {
            view.setSelected(false);
            view.setTextColor(defaultTextColor);
        }
    }

    //缩小
    private void zoomOut(View view) {
        Animator animator =
                AnimatorInflater.loadAnimator(
                        view.getContext(), tv.ismar.searchpage.R.animator.scalein_recomment_poster);
        animator.setTarget(view);
        animator.start();
    }

    //放大
    private void zoomIn(View view) {
        Animator animator =
                AnimatorInflater.loadAnimator(
                        view.getContext(),
                        tv.ismar.searchpage.R.animator.scaleout_recommend_poster);
        animator.setTarget(view);
        animator.start();
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        isOnKeyDown = false;

        TextView itemView = (TextView) v;
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.setHovered(true);
                itemView.setBackground(selectedDrawable);
                itemView.setTextColor(textFocusColor);
                if (isScroll) {
                    isScroll = false;
                    mCurrentState = STATE_FOCUS;
                    if (!v.hasFocus()) {
                        zoomIn(v);
                        v.requestFocusFromTouch();
                        v.requestFocus();
                    }
                    mScrollHandler.sendEmptyMessageDelayed(0, 500);
                }
                return true;
            case MotionEvent.ACTION_HOVER_EXIT:
                zoomOut(v);
                if (HomeActivity.mHoverView != null) {
                    HomeActivity.mHoverView.requestFocus();
                }

                TextView lastClickView =
                        (TextView) linearContainer.getChildAt(mClickPosition);
                if (itemView == lastClickView) {
                    lastClickView.setBackgroundResource(
                            android.R.color.transparent);
                    lastClickView.setTextColor(textSelectColor);
                } else {
                    itemView.setBackgroundResource(android.R.color.transparent);
                    itemView.setTextColor(defaultTextColor);
                }
                v.setHovered(false);
                return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCurrentState = STATE_FOCUS;
                break;
        }
        return false;
    }

    private enum ViewStatus {
        Hovered,
        Selected,
        Focused,
        UnHovered,
        UnSelected,
        UnFocused,
    }

    public interface OnItemSelectedListener {
        void onItemSelected(View v, int position);
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
}
