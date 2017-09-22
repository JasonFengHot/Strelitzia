/*
 * A modified version of the Android HorizontalScrollView
 *
 * Copyright 2017 Beaver Long
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.ismar.homepage.widget;

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
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.w3c.dom.Text;

import java.util.List;

import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.R;

/**
 * Created by beaver on 17-3-18.
 * <p>
 * Note that interface OnItemSelectedListener will always called when item focused, so you should delay process your logic.
 */

public class HorizontalTabView extends HorizontalScrollView implements View.OnClickListener, View.OnFocusChangeListener,
        View.OnKeyListener {

    private static final String TAG = "LH/HorizontalTabView";
    public static final byte SCALE_NONE = 0;
    public static final byte SCALE_FROM_CENTER = 1;
    public static final byte SCALE_FROM_TOP = 2;
    public static final byte SCALE_FROM_BOTTOM = 3;

    private static final int TIME_ANIMATION_IN = 200;
    private static final int TIME_ANIMATION_OUT = 200;
    private static final int STATE_FOCUS = 0;
    private static final int STATE_LEAVE = 1;

    private Drawable selectedDrawable;
    private int tabHeight;
    private int tabSpace;
    private int startEndPadding;
    private int scaleBaseline;
    private float scaleMutiple;
    private int textSize;
    private int defaultTextColor;
    private int textSelectColor;
    private int textFocusColor;

    private Context mContext;
    private LinearLayout linearContainer;
    private int mSelectedIndex = -1;
    private int mTabMargin;
    private OnItemSelectedListener onItemSelectedListener;
    private int mCurrentState;
    private int mCurrentPosition=-1;

    private int mClickPosition = -1;

    private boolean isScroll = true;

    private boolean isOnKeyDown = false;

    public HorizontalTabView(Context context) {
        this(context, null);
    }

    public HorizontalTabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HorizontalTabView);
        selectedDrawable = typedArray.getDrawable(R.styleable.HorizontalTabView_tvTabSelectedDrawable);
        tabHeight = typedArray.getDimensionPixelSize(R.styleable.HorizontalTabView_tvTabHeight, 0);
        tabSpace = typedArray.getDimensionPixelSize(R.styleable.HorizontalTabView_tvTabSpace, dp2px(10));
        startEndPadding = typedArray.getDimensionPixelSize(R.styleable.HorizontalTabView_tvTabStartEndPadding, dp2px(16));
        scaleBaseline = typedArray.getInt(R.styleable.HorizontalTabView_tvTabScaleBaseline, SCALE_NONE);
        scaleMutiple = typedArray.getFloat(R.styleable.HorizontalTabView_tvTabScaleMultiple, 1.2f);
        textSize = typedArray.getDimensionPixelSize(R.styleable.HorizontalTabView_tvTabTextSize, dp2px(18));
        defaultTextColor = typedArray.getColor(R.styleable.HorizontalTabView_tvTabTextColor, Color.WHITE);
        textSelectColor = typedArray.getColor(R.styleable.HorizontalTabView_tvTabSelectTextColor, Color.WHITE);
        textFocusColor = typedArray.getColor(R.styleable.HorizontalTabView_tvTabFocusTextColor, Color.WHITE);
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
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
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
        layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        final TextView item;
        if (label.equals("搜索")) {
            item = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.item_channel_search, null);
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
        item.setFocusableInTouchMode(true);
        item.setOnClickListener(this);
        item.setOnFocusChangeListener(this);
        item.setOnKeyListener(this);
        item.setOnHoverListener(new OnHoverListener() {
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
                        if (isScroll){
                            isScroll = false;
                            mCurrentState = STATE_FOCUS;
                            if (!v.hasFocus()) {
                                zoomInAnimation(v);
                                v.requestFocusFromTouch();
                                v.requestFocus();
                            }
                            mScrollHandler.sendEmptyMessageDelayed(0, 500);
                        }
                        return true;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        zoomOutAnimation(v);
                        if (HomeActivity.mHoverView != null){
                            HomeActivity.mHoverView.requestFocus();
                        }

                        TextView lastClickView = (TextView) linearContainer.getChildAt(mClickPosition);
                        if (itemView == lastClickView) {
                            lastClickView.setBackgroundResource(android.R.color.transparent);
                            lastClickView.setTextColor(textSelectColor);
                        }else {
                            itemView.setBackgroundResource(android.R.color.transparent);
                            itemView.setTextColor(defaultTextColor);
                        }
                        v.setHovered(false);
                        return true;
                }
                return false;
            }
        });
        item.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mCurrentState = STATE_FOCUS;
                        break;
                }
                return false;
            }
        });
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

//        item.setPadding(0,0,0,20);
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
            }else {
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


    private void changeSelection(final View v, final boolean callItemListener) {
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

//        Log.d(TAG, "tab change position: " + v);

        scrollChildPosition(v);

        ScaleAnimation scaleAnimation = null;
        if (currentTextView.getAnimation() == null) {
            scaleAnimation = zoomInAnimation(v);
        }


        if (onItemSelectedListener != null && callItemListener) {
            onItemSelectedListener.onItemSelected(v, mSelectedIndex);
        }

        if (scaleAnimation == null) {
//            if (onItemSelectedListener != null && callItemListener) {
//                onItemSelectedListener.onItemSelected(v, mSelectedIndex);
//            }
        } else {
            scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
//                    if (onItemSelectedListener != null && callItemListener) {
//                        onItemSelectedListener.onItemSelected(v, mSelectedIndex);
//                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }

    }

    private ScaleAnimation zoomInAnimation(View v) {
        ScaleAnimation zoomInAnimation = null;
        switch (scaleBaseline) {
            case SCALE_FROM_TOP:
                zoomInAnimation = new ScaleAnimation(1.0f, scaleMutiple, 1.0f, scaleMutiple,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
                break;
            case SCALE_FROM_BOTTOM:
                zoomInAnimation = new ScaleAnimation(1.0f, scaleMutiple, 1.0f, scaleMutiple,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
                break;
            case SCALE_FROM_CENTER:
                zoomInAnimation = new ScaleAnimation(1.0f, scaleMutiple, 1.0f, scaleMutiple,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                break;
        }
        if (zoomInAnimation != null) {
            zoomInAnimation.setDuration(TIME_ANIMATION_IN);
            zoomInAnimation.setFillAfter(true);
            v.setAnimation(zoomInAnimation);
            zoomInAnimation.start();
        }
        return zoomInAnimation;

    }

    private void zoomOutAnimation(final View v) {
        ScaleAnimation zoomOutAnimation = null;
        switch (scaleBaseline) {
            case SCALE_FROM_TOP:
                zoomOutAnimation = new ScaleAnimation(scaleMutiple, 1.0f, scaleMutiple, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
                break;
            case SCALE_FROM_BOTTOM:
                zoomOutAnimation = new ScaleAnimation(scaleMutiple, 1.0f, scaleMutiple, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
                break;
            case SCALE_FROM_CENTER:
                zoomOutAnimation = new ScaleAnimation(scaleMutiple, 1.0f, scaleMutiple, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                break;
        }
        if (zoomOutAnimation != null) {
            zoomOutAnimation.setDuration(TIME_ANIMATION_OUT);
            zoomOutAnimation.setFillAfter(true);
            v.startAnimation(zoomOutAnimation);
//            v.setAnimation(zoomOutAnimation);
//            zoomOutAnimation.start(); // It can't work use this method, when other view has focused, but not in this view.
            zoomOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    v.clearAnimation();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

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
        if (linearContainer.getChildCount() < 10) {
            return;
        }

        int baseRightX = getWidth() - mTabMargin;
        int[] currentRect = new int[2];
        view.getLocationOnScreen(currentRect);
        //左滑
//        Log.d(TAG, "currentRect[0] " + currentRect[0]);
//        Log.d(TAG, "mTabMargin: " + mTabMargin);
//        Log.d(TAG, "mTabSpace: " + tabSpace);


        if (currentRect[0] - view.getWidth() - tabSpace <= mTabMargin) {// current view left less than left margin
            if (mSelectedIndex == 0) {
                if (currentRect[0] - tabSpace > mTabMargin + tabSpace) {
                    scrollChildPosition(linearContainer.getChildAt(0));
                }
            } else {
                int leftViewWidth = linearContainer.getChildAt(mSelectedIndex - 1).getWidth();
                int[] lastleftRect = new int[2];
                linearContainer.getChildAt(mSelectedIndex - 1).getLocationOnScreen(lastleftRect);
                smoothScrollBy(-(currentRect[0] - (lastleftRect[0] + leftViewWidth) + leftViewWidth / 2), 0);
            }
            //右滑
        } else if (currentRect[0] + view.getWidth() >= baseRightX) {// current view right more than right margin
            if (mSelectedIndex == linearContainer.getChildCount() - 1) {
                smoothScrollBy(currentRect[0] + view.getWidth() - baseRightX, 0);
            } else {
                int rightViewWidth = linearContainer.getChildAt(mSelectedIndex + 1).getWidth();
                smoothScrollBy(currentRect[0] + view.getWidth() - baseRightX + rightViewWidth / 2, 0);
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

    public void setScaleBaseline(int scaleBaseline) {
        this.scaleBaseline = scaleBaseline;
    }

    public void setScaleMutiple(float scaleMutiple) {
        this.scaleMutiple = scaleMutiple;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public void setTextColor(int textColor) {
        this.defaultTextColor = textColor;
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    public void onClick(View v) {
        int tag = (int) v.getTag();
        if(mSelectedIndex==mCurrentPosition){
            return;
        }
        TextView lastClickView = (TextView) linearContainer.getChildAt(mClickPosition);
        if (lastClickView != null) {
                lastClickView.setBackgroundResource(android.R.color.transparent);
                lastClickView.setTextColor(defaultTextColor);

        }
        mCurrentState = STATE_FOCUS;
        mCurrentPosition= (int) v.getTag();
        mClickPosition = (int) v.getTag();
        changeSelection(v, tag != mSelectedIndex);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.isHovered()){
            return;
        }

        int tag = (int) v.getTag();
        if (hasFocus) {
            for (int i = 0; i < linearContainer.getChildCount(); i++){
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
                zoomOutAnimation(v);
            }
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Log.d(TAG, "onKey");

        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                isOnKeyDown = true;
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    mCurrentState = STATE_LEAVE;
                    changeLostFocusStatus(v);
                }

                if ((linearContainer.indexOfChild(v) == 0 && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) ||
                        (linearContainer.indexOfChild(v) == linearContainer.getChildCount() - 1 && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
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

    public interface OnItemSelectedListener {
        void onItemSelected(View v, int position);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;

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

    Handler mScrollHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            isScroll = true;
        }
    };

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return super.dispatchGenericMotionEvent(event);
    }

    private void changeViewStatus(TextView view, ViewStatus status){
        switch (status){
            case Hovered:
                break;
            case UnHovered:
                break;
            case Selected:
                changeViewSelectedStatus(view, true);
                break;
            case UnSelected:
                changeViewSelectedStatus(view, false);
                break;
            case Focused:
                break;
            case UnFocused:
                break;
        }
    }

    private void changeViewHoveredStatus(TextView view, boolean isHovered){

    }

    private void changeViewSelectedStatus(TextView view, boolean isSelected){
        if (isSelected){
            view.setSelected(true);
            view.setTextColor(textSelectColor);
        }else {
            view.setSelected(false);
            view.setTextColor(defaultTextColor);
        }
    }

    private enum ViewStatus{
        Hovered,
        Selected,
        Focused,
        UnHovered,
        UnSelected,
        UnFocused,
    }
}
