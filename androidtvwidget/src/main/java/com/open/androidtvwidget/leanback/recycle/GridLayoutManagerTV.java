package com.open.androidtvwidget.leanback.recycle;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by hailongqiu on 2016/8/25.
 */
public class GridLayoutManagerTV extends GridLayoutManager {
    public GridLayoutManagerTV(Context context, int spanCount) {
        super(context, spanCount);
    }

    public GridLayoutManagerTV(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    public GridLayoutManagerTV(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        View nextFocus = super.onFocusSearchFailed(focused, focusDirection, recycler, state);
        Log.d("LinearLayoutManagerTV", "onFocusSearchFailed: " + nextFocus);
        return this.focusSearchFailedListener != null ? this.focusSearchFailedListener.onFocusSearchFailed(focused, focusDirection, recycler, state) : null;
    }

    public interface FocusSearchFailedListener {

        View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state);
    }

    private LinearLayoutManagerTV.FocusSearchFailedListener focusSearchFailedListener;

    public void setFocusSearchFailedListener(LinearLayoutManagerTV.FocusSearchFailedListener focusSearchFailedListener) {
        this.focusSearchFailedListener = focusSearchFailedListener;
    }


}
