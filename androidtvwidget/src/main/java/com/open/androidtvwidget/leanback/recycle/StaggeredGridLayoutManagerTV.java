package com.open.androidtvwidget.leanback.recycle;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/27
 * @DESC: 说明
 */

public class StaggeredGridLayoutManagerTV extends StaggeredGridLayoutManager {
    public StaggeredGridLayoutManagerTV(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public StaggeredGridLayoutManagerTV(int spanCount, int orientation) {
        super(spanCount, orientation);
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

    private FocusSearchFailedListener focusSearchFailedListener;

    public void setFocusSearchFailedListener(FocusSearchFailedListener focusSearchFailedListener) {
        this.focusSearchFailedListener = focusSearchFailedListener;
    }
}
