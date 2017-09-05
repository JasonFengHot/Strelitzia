package tv.ismar.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;

/**
 * Created by liucan on 2017/8/28.
 */

public class HistoryLinerlayoutMananger extends LinearLayoutManagerTV {
    private boolean isScrollEnabled = true;

    public HistoryLinerlayoutMananger(Context context) {
        super(context);
    }

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean canScrollVertically() {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollVertically();
    }

    @Override
    public boolean canScrollHorizontally() {
        return isScrollEnabled && super.canScrollHorizontally();
    }
}
