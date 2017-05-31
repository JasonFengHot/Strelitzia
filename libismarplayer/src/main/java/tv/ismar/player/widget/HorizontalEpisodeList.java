package tv.ismar.player.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.List;

import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.player.R;

/**
 * Created by liucan on 2017/5/31.
 */

public class HorizontalEpisodeList extends HorizontalScrollView {

        private static final String TAG = "LH/VerticalPagerView";
        private static final int UNIQUE_PAGE_ID = 0x80;
        private Context mContext;
        private LinearLayout linearContainer;

        private List mDataList;
        private int rowCount;
        private int itemLayoutId = -1;
        private int mSpace;
        private int mCurrentDataSelectPosition;
        private int currentViewIndex;
        private boolean focusMoveOut;

        private OnItemActionListener mOnItemActionListener;

        public HorizontalEpisodeList(Context context) {
            this(context, null);
        }

        public HorizontalEpisodeList(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public HorizontalEpisodeList(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            if (attrs != null) {
                TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HorizontalEpisodeList);
                rowCount = typedArray.getInt(R.styleable.HorizontalEpisodeList_heRowCount, 0);
                itemLayoutId = typedArray.getResourceId(R.styleable.HorizontalEpisodeList_heItemLayoutId, -1);
                mSpace = typedArray.getDimensionPixelSize(R.styleable.HorizontalEpisodeList_heItemSpace, 20);
                typedArray.recycle();

                setFocusable(false);
                setFocusableInTouchMode(false);
                setVerticalFadingEdgeEnabled(false);
                setVerticalScrollBarEnabled(false);
                setOverScrollMode(View.OVER_SCROLL_NEVER);
                setClipToPadding(false);
                setClipChildren(false);
                setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

                mContext = context;
                LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                linearContainer = new LinearLayout(mContext);
                linearContainer.setOrientation(LinearLayout.HORIZONTAL);
                linearContainer.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                linearContainer.setClipChildren(false);
                linearContainer.setClipChildren(false);
                linearContainer.setLayoutParams(layoutParams);
                addView(linearContainer);

            }
        }

        public void addDatas(List datas) {
            if (datas == null || datas.isEmpty() || mContext == null || itemLayoutId == -1) {
                Log.e(TAG, "Datas null.");
                return;
            }
            this.mDataList = datas;
            mCurrentDataSelectPosition = 0;
            currentViewIndex = 0;
            for (int i = 0; i < rowCount; i++) {
                if (i > mDataList.size() - 1) {
                    break;
                }
                View itemView = createItemView(i);
                if (mOnItemActionListener != null) {
                    mOnItemActionListener.onBindView(itemView, (ItemEntity) mDataList.get(i), i);
                }
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        getResources().getDimensionPixelSize(R.dimen.player_250), getResources().getDimensionPixelSize(R.dimen.player_80));
                if (i > 0) {
                    layoutParams.leftMargin = mSpace;
                }
                itemView.setLayoutParams(layoutParams);
                linearContainer.addView(itemView);
            }

            View initFocus = linearContainer.getChildAt(currentViewIndex);
            if (initFocus != null) {
                initFocus.requestFocusFromTouch();
            }
            requestLayout();
            invalidate();
        }

        private View createItemView(int index) {
            View itemView = LayoutInflater.from(mContext).inflate(itemLayoutId, null);
            itemView.setTag(R.id.lib_ver_pv_item_position, index);
            itemView.setFocusable(true);
            itemView.setFocusableInTouchMode(true);
            itemView.setOnClickListener(OnClickListener);
            itemView.setOnFocusChangeListener(OnFocusChangeListener);
            itemView.setOnHoverListener(OnHoverListener);
            itemView.setOnKeyListener(OnKeyListener);
            return itemView;
        }

        public void pageArrowUp() {
            refreshView(View.FOCUS_BACKWARD);
        }

        public void pageArrowDown() {
            refreshView(View.FOCUS_FORWARD);
        }

        public void toPlayingItem(int index){
            int page=index/7;
            int position=index%7;
            for(int i=0;i<page;i++){
                pageArrowDown();
            }
            for(int j=1;j<position;j++){
                refreshView(View.FOCUS_DOWN);
            }
        }

        public int getFirstVisibleChildIndex() {
            if (linearContainer != null && linearContainer.getChildAt(0) != null) {
                return (int) linearContainer.getChildAt(0).getTag(R.id.lib_ver_pv_item_position);
            }
            return -1;
        }

        public int getCurrentDataSelectPosition() {
//        Log.d(TAG, "currentPosition : " + mCurrentDataSelectPosition);
            return mCurrentDataSelectPosition;
        }

        public int getLastVisibleChildIndex() {
            if (linearContainer != null && linearContainer.getChildAt(rowCount - 1) != null) {
                return (int) linearContainer.getChildAt(rowCount - 1).getTag(R.id.lib_ver_pv_item_position);
            }
            return -1;
        }

        public View getChildViewAt(int index) {
            if (linearContainer == null) {
                return null;
            }
            int viewInLayoutIndex = -1;
            for (int i = 0; i < linearContainer.getChildCount(); i++) {
                View itemView = linearContainer.getChildAt(i);
                if (((int) itemView.getTag(R.id.lib_ver_pv_item_position)) == index) {
                    viewInLayoutIndex = i;
                    break;
                }
            }
            return linearContainer.getChildAt(viewInLayoutIndex);
        }

        private void refreshView(int direction) {
            if (linearContainer == null) {
                return;
            }
            boolean isAnim = false;
            int offsetIndex = 0;
            switch (direction) {
                case View.FOCUS_UP:
                    // 按上键
                    isAnim = true;
                    int firstViewIndex = (int) linearContainer.getChildAt(0).getTag(R.id.lib_ver_pv_item_position);
                    if (firstViewIndex > 0) {
                        offsetIndex = -1;
                        mCurrentDataSelectPosition--;
                    }
                    break;
                case View.FOCUS_DOWN:
                    // 按下键
                    isAnim = true;
                    int lastViewIndex = (int) linearContainer.getChildAt(rowCount - 1).getTag(R.id.lib_ver_pv_item_position);
                    if (lastViewIndex < mDataList.size() - 1) {
                        offsetIndex = 1;
                        mCurrentDataSelectPosition++;
                    }
                    break;
                case View.FOCUS_BACKWARD:
                    // 向上翻页
                    isAnim = false;
                    int firstViewPageIndex = (int) linearContainer.getChildAt(0).getTag(R.id.lib_ver_pv_item_position);
//                Log.d(TAG, "firstViewPageIndex:" + firstViewPageIndex);
                    if (firstViewPageIndex == 0) {
                        return;
                    }
                    if (firstViewPageIndex - rowCount >= 0) {
                        offsetIndex = -rowCount;
                    } else {
                        offsetIndex = -firstViewPageIndex;
                    }
                    // 翻页没有focus变化，需要改变currentSelection
                    mCurrentDataSelectPosition = mCurrentDataSelectPosition + offsetIndex;
                    break;
                case View.FOCUS_FORWARD:
                    // 向下翻页
                    isAnim = false;
                    int lastViewPageIndex = (int) linearContainer.getChildAt(rowCount - 1).getTag(R.id.lib_ver_pv_item_position);
//                Log.d(TAG, "lastViewPageIndex:" + lastViewPageIndex + " - dataSize: " + mDataList.size());
                    if (lastViewPageIndex == mDataList.size() - 1) {
                        return;
                    }
                    if (lastViewPageIndex + rowCount >= mDataList.size() - 1) {
                        offsetIndex = mDataList.size() - 1 - lastViewPageIndex;
                    } else {
                        offsetIndex = rowCount;
                    }
                    // 翻页没有focus变化，需要改变currentSelection
                    mCurrentDataSelectPosition = mCurrentDataSelectPosition + offsetIndex;
                    break;

            }
//        Log.i(TAG, "currentSelectPosition:" + mCurrentDataSelectPosition + " offset : " + offsetIndex);
            if (mCurrentDataSelectPosition < 0 || mCurrentDataSelectPosition > mDataList.size() - 1 || offsetIndex == 0) {
                Log.e(TAG, "Error position.");
                return;
            }
            for (int i = 0; i < linearContainer.getChildCount(); i++) {
                View itemView = linearContainer.getChildAt(i);
                int newIndex = (int) itemView.getTag(R.id.lib_ver_pv_item_position) + offsetIndex;
                itemView.setTag(R.id.lib_ver_pv_item_position, newIndex);

                if (mOnItemActionListener != null) {
                    mOnItemActionListener.onBindView(itemView, (ItemEntity) mDataList.get(newIndex), newIndex);
                    if (newIndex == mCurrentDataSelectPosition){
                        mOnItemActionListener.onItemFocusChanged(itemView, true, newIndex);
                    }
                }
            }
            requestLayout();
            invalidate();
        }

        private OnClickListener OnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                focusMoveOut = false;
                int viewPosition = (Integer) v.getTag(R.id.lib_ver_pv_item_position);
                mCurrentDataSelectPosition = viewPosition;
                for (int i = 0; i < linearContainer.getChildCount(); i++) {
                    View itemView = linearContainer.getChildAt(i);
                    if (((int) itemView.getTag(R.id.lib_ver_pv_item_position)) == viewPosition) {
                        currentViewIndex = i;
                        break;
                    }
                }
                if (mOnItemActionListener != null) {
                    mOnItemActionListener.onItemClick(v, viewPosition);
                }
            }
        };

        private OnFocusChangeListener OnFocusChangeListener = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                int viewPosition = (Integer) v.getTag(R.id.lib_ver_pv_item_position);
                if (hasFocus) {
                    if (!focusMoveOut) {
                        mCurrentDataSelectPosition = viewPosition;
                        for (int i = 0; i < linearContainer.getChildCount(); i++) {
                            View itemView = linearContainer.getChildAt(i);
                            if (((int) itemView.getTag(R.id.lib_ver_pv_item_position)) == viewPosition) {
                                currentViewIndex = i;
                                break;
                            }
                        }
                    }
                    focusMoveOut = false;
                }
                if (mOnItemActionListener != null) {
                    mOnItemActionListener.onItemFocusChanged(v, hasFocus, viewPosition);
                }
            }
        };

        private OnHoverListener OnHoverListener = new OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                focusMoveOut = false;
                if (mOnItemActionListener != null) {
                    int viewPosition = (Integer) v.getTag(R.id.lib_ver_pv_item_position);
                    mOnItemActionListener.onItemHovered(v, event, mDataList.get(viewPosition), viewPosition);
                }
                return false;
            }
        };

        private OnKeyListener OnKeyListener = new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "OnKeyListener : " + keyCode + " - lastSelect:" + mCurrentDataSelectPosition + " - lastViewIndex:" + currentViewIndex);
                    if (mOnItemActionListener != null) {
                        mOnItemActionListener.onKeyDown(v, keyCode, event);
                    }
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_UP:
//                            focusMoveOut = false;
//                            if (mCurrentDataSelectPosition > 0) {
//                                if (currentViewIndex > 0) {
//                                    currentViewIndex--;
//                                    mCurrentDataSelectPosition--;
//                                    linearContainer.getChildAt(currentViewIndex).requestFocus(View.FOCUS_UP);
//                                } else {
//                                    refreshView(View.FOCUS_UP);
//                                }
//                            }
                            focusMoveOut = true;
                            return true;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            focusMoveOut = true;
//                            if (mCurrentDataSelectPosition < mDataList.size() - 1) {
//                                    if (currentViewIndex < rowCount - 1) {
//                                        currentViewIndex++;
//                                        mCurrentDataSelectPosition++;
//                                        linearContainer.getChildAt(currentViewIndex).requestFocus(View.FOCUS_DOWN);
//                                    } else {
//                                        refreshView(View.FOCUS_DOWN);
//                                    }
//                            }
                            return true;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            focusMoveOut = false;
                            if (mCurrentDataSelectPosition > 0) {
                                if (currentViewIndex > 0) {
                                  //  linearContainer.getChildAt(currentViewIndex-1).requestFocus(View.FOCUS_UP);
//                                    currentViewIndex--;
//                                    mCurrentDataSelectPosition--;
                                    Log.i("currentViewIndex",currentViewIndex+"==index"+ mCurrentDataSelectPosition+"==mCurrentDataSelectPosition");
                                } else {
                                    refreshView(View.FOCUS_UP);
                                }
                            }
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            focusMoveOut = false;
                            if (mCurrentDataSelectPosition < mDataList.size() - 1) {
                                if (currentViewIndex < rowCount - 1) {
                                  //  linearContainer.getChildAt(currentViewIndex).requestFocus(View.FOCUS_DOWN);
//                                    currentViewIndex++;
//                                    mCurrentDataSelectPosition++;
                                    Log.i("currentViewIndex",currentViewIndex+"==index"+ mCurrentDataSelectPosition+"==mCurrentDataSelectPosition");

                                } else {
                                    refreshView(View.FOCUS_DOWN);
                                }
                            }
                            break;
                    }
                }
                return false;
            }
        };

        public void setOnItemActionListener(OnItemActionListener onItemActionListener) {
            this.mOnItemActionListener = onItemActionListener;
        }

        public interface OnItemActionListener {

            void onItemClick(View view, int position);

            void onItemFocusChanged(View view, boolean focused, int position);

            void onItemHovered(View view, MotionEvent event, Object object, int position);

            void onBindView(View itemView, ItemEntity object, int position);

            void onKeyDown(View view, int keyCode, KeyEvent event);
        }
}
