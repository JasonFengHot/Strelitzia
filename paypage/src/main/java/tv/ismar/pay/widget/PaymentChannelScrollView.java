package tv.ismar.pay.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import tv.ismar.app.AppConstant;
import tv.ismar.pay.PaymentActivity;
import tv.ismar.pay.R;

/**
 * Created by huibin on 10/11/2017.
 */

public class PaymentChannelScrollView extends ScrollView implements View.OnClickListener {
    private static final String TAG = PaymentChannelScrollView.class.getSimpleName();

    private ImageView arrowUp;
    private ImageView arrowDown;
    private LinearLayout payTypeLayout;
    private int tabSpace;

    private int FOCUS_DIRECTION;
    private Runnable runnable;

    public PaymentChannelScrollView(Context context) {
        super(context);
    }

    public PaymentChannelScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaymentChannelScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTabSpace(int tabSpace) {
        this.tabSpace = tabSpace;
    }

    public void setPayTypeLayout(LinearLayout payTypeLayout) {
        this.payTypeLayout = payTypeLayout;
    }

    private Handler mHandler;

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
//            return true;
//        } else {
//            return super.dispatchTouchEvent(ev);
//        }
//    }

    @Override
    public void requestChildFocus(final View child, final View focused) {
        Log.d(TAG, "requestChildFocus: " + child);
        Log.d(TAG,"requestChildFocus: " + "ishover: " + child.isHovered());
        super.requestChildFocus(child, this);
        mHandler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if ((canScrollVertically(View.FOCUS_DOWN) ||  canScrollVertically(View.FOCUS_UP))){
                    if (!PaymentActivity.ishover){
                        scrollChildPosition(focused);
                    }
                }
            }
        };
        mHandler.postDelayed(runnable , 20);
//        .postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (!child.isHovered() && (canScrollVertically(View.FOCUS_DOWN) ||  canScrollVertically(View.FOCUS_UP))){
//                    scrollChildPosition(focused);
//                }
//            }
//        }, 20);
    }



    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        int[] scrollViewRect = new int[2];
        getLocationOnScreen(scrollViewRect);

        View firstItemView = payTypeLayout.getChildAt(0);
        Rect firstItemViewRect = new Rect();
        firstItemView.getLocalVisibleRect(firstItemViewRect);
        Rect firstItemViewGlobalRect = new Rect();
        firstItemView.getGlobalVisibleRect(firstItemViewGlobalRect);
        Rect payTypeLayoutGlobalRect = new Rect();
        getGlobalVisibleRect(payTypeLayoutGlobalRect);
//        Log.d(TAG, "bottom: " + firstItemViewRect.bottom );
//        Log.d(TAG, "top: " + firstItemViewRect.top );
//        Log.d(TAG, "height: " + firstItemView.getHeight() );

        Log.d(TAG, "firstItemViewGlobalRect: " + firstItemViewGlobalRect);
        Log.d(TAG, "payTypeLayoutGlobalRect: " + payTypeLayoutGlobalRect);

        if (firstItemViewRect.bottom - firstItemViewRect.top <= firstItemView.getHeight() || firstItemViewRect.top < 0) {
            if (firstItemView.hasFocus() || firstItemViewGlobalRect.top > payTypeLayoutGlobalRect.top){
                arrowUp.setVisibility(View.INVISIBLE);
            }else {
                arrowUp.setVisibility(View.VISIBLE);
            }
        } else {
            arrowUp.setVisibility(View.INVISIBLE);
        }

        View lastItemView = payTypeLayout.getChildAt(payTypeLayout.getChildCount() - 1);
        int[] lastItemViewRect = new int[2];
        lastItemView.getLocationOnScreen(lastItemViewRect);

        if (lastItemViewRect[1] + lastItemView.getHeight() > scrollViewRect[1] + getHeight()) {
            arrowDown.setVisibility(View.VISIBLE);
        } else {
            arrowDown.setVisibility(View.INVISIBLE);
        }
    }

    public void setArrowUp(ImageView arrowUp) {
        this.arrowUp = arrowUp;
        this.arrowUp.setOnClickListener(this);
    }

    public void setArrowDown(ImageView arrowDown) {
        this.arrowDown = arrowDown;
        this.arrowDown.setOnClickListener(this);
    }


    public void scrollChildPosition(View view) {
        int[] scrollViewRect = new int[2];
        getLocationOnScreen(scrollViewRect);
        int baseBottomY = scrollViewRect[1] + getHeight();

        int mFocusedIndex = payTypeLayout.indexOfChild(view);
        int[] currentRect = new int[2];
        view.getLocationOnScreen(currentRect);

        if (mFocusedIndex == 0) {
//            if (canScrollVertically(FOCUS_UP)) {
                fullScroll(FOCUS_UP);
                scrollBy(0 , 0);
//            }
        } else if (mFocusedIndex == payTypeLayout.getChildCount() - 1) {
//            if (canScrollVertically(FOCUS_DOWN)) {
                fullScroll(FOCUS_DOWN);
//            }
        } else if (currentRect[1] - tabSpace <= scrollViewRect[1]
                || currentRect[1] - tabSpace <= scrollViewRect[1] + 5
                || currentRect[1] - tabSpace <= scrollViewRect[1] - 5) { // current view left less than left margin
            Log.d(TAG, "channel: 上滑");
            View nextIndexView = payTypeLayout.getChildAt(mFocusedIndex - 1);
            int nextIndexViewHeight = nextIndexView.getHeight();
            int[] nextIndexViewRect = new int[2];
            nextIndexView.getLocationOnScreen(nextIndexViewRect);
            int dy = -(currentRect[1] - nextIndexViewRect[1] - nextIndexViewHeight / 2);
            Log.d(TAG, "dy: " + dy);
            smoothScrollBy(0, dy);
        } else if (currentRect[1] + view.getHeight() + tabSpace >= baseBottomY
                || currentRect[1] + view.getHeight() + tabSpace >= baseBottomY + 5
                || currentRect[1] + view.getHeight() + tabSpace >= baseBottomY - 5) { // current view right more than right margin
            Log.d(TAG, "channel: 下滑");
            View nextIndexView = payTypeLayout.getChildAt(mFocusedIndex + 1);
            int nextViewHeight = nextIndexView.getHeight();
            int[] nextIndexViewRect = new int[2];
            nextIndexView.getLocationOnScreen(nextIndexViewRect);
            int dy = nextIndexViewRect[1] - currentRect[1] - nextViewHeight / 2;
            Log.d(TAG, "dy: " + dy);
            smoothScrollBy(0, dy);
        }
    }

    @Override
    public void onClick(View v) {
        int itemHeight = payTypeLayout.getChildAt(0).getHeight();
        int i = v.getId();
        if (i == R.id.arrow_down) {
            smoothScrollBy(0, (itemHeight + tabSpace));
        } else if (i == R.id.arrow_up) {
            smoothScrollBy(0, -(itemHeight + tabSpace));
        }
    }

    @Override
    public void onViewRemoved(View child) {
        if (mHandler != null && runnable != null){
            mHandler.removeCallbacks(runnable);
        }
        super.onViewRemoved(child);
    }
}
