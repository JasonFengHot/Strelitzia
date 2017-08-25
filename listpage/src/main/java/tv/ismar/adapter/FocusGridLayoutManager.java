package tv.ismar.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by admin on 2017/6/20.
 */

public class FocusGridLayoutManager extends GridLayoutManager {
    public FocusGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FocusGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }


    public FocusGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }


    @Override
    public int getChildCount() {
        return super.getChildCount();
    }


    @Override
    public View getChildAt(int index) {
        return super.getChildAt(index);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }


    @Override
    public View getFocusedChild() {
        return super.getFocusedChild();
    }

    @Override
    public int getPosition(View view) {
        return super.getPosition(view);
    }

    @Override
    public int getSpanCount() {
        return super.getSpanCount();
    }

    @Override
    public View onInterceptFocusSearch(View focused, int direction) {
        if(direction==View.FOCUS_RIGHT){
            int index=getPosition(focused);
            if(index==getItemCount()-1){
                return focused;
            }
        }
        return super.onInterceptFocusSearch(focused, direction);
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {

        // Need to be called in order to layout new row/column
        View nextFocus = super.onFocusSearchFailed(focused, focusDirection, recycler, state);

        if (nextFocus == null&&focusDirection!=View.FOCUS_RIGHT) {
            return null;
        }
        /**
         * 获取当前焦点的位置
         */
        int fromPos = getPosition(focused);
        /**
         * 获取我们希望的下一个焦点的位置
         */
        int nextPos = getNextViewPos(fromPos, focusDirection);
        View nextView=findViewByPosition(nextPos);
        if(nextView==null&&focusDirection==View.FOCUS_RIGHT){
            nextView=focused;
        }
        return nextView;

    }


    protected int getNextViewPos(int fromPos, int direction) {
        int offset = calcOffsetToNextView(direction);

        if (hitBorder(fromPos, offset)) {
            return fromPos;
        }

        return fromPos + offset;
    }

    /**
     * Calculates position offset.
     *
     * @param direction regular {@code View.FOCUS_*}.
     * @return position offset according to {@code direction}.
     */
    protected int calcOffsetToNextView(int direction) {
        int spanCount = getSpanCount();
        int orientation = getOrientation();

        if (orientation == VERTICAL) {
            switch (direction) {
                case View.FOCUS_DOWN:
                    return spanCount;
                case View.FOCUS_UP:
                    return -spanCount;
                case View.FOCUS_RIGHT:
                    return 1;
                case View.FOCUS_LEFT:
                    return -1;
            }
        } else if (orientation == HORIZONTAL) {
            switch (direction) {
                case View.FOCUS_DOWN:
                    return 1;
                case View.FOCUS_UP:
                    return -1;
                case View.FOCUS_RIGHT:
                    return spanCount;
                case View.FOCUS_LEFT:
                    return -spanCount;
            }
        }

        return 0;
    }

    /**
     * Checks if we hit borders.
     *
     * @param from from what position.
     * @param offset offset to new position.
     * @return {@code true} if we hit border.
     */
    private boolean hitBorder(int from, int offset) {
        int spanCount = getSpanCount();

        if (offset== -1) {
            int spanIndex = from % spanCount;
            int newSpanIndex = spanIndex + offset;
            return newSpanIndex < 0 || newSpanIndex >= spanCount;
        } else {
            int newPos = from + offset;
            return newPos < 0 && newPos >= spanCount;
        }
    }

    public boolean isCanScroll() {
        return canScroll;
    }

    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    private boolean canScroll=false;
    @Override
    public boolean canScrollVertically() {
        return canScroll;
    }
}
