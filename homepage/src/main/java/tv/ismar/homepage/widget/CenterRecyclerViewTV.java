package tv.ismar.homepage.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import tv.ismar.homepage.R;

/**
 * Created by zhaoji on 2017/11/6.
 */

public class CenterRecyclerViewTV extends RecyclerViewTV{
    public CenterRecyclerViewTV(Context context) {
        super(context);
    }

    public CenterRecyclerViewTV(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CenterRecyclerViewTV(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public int getCenterOffset() {
        int itemWidth = getResources().getDimensionPixelSize(R.dimen.center_padding)* 2 + getResources().getDimensionPixelSize(R.dimen.center_poster_width);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int offset = (screenWidth - itemWidth) /2;
        return offset;
    }

    @Override
    public int getCenterSpace() {
        int spaceWidth = getResources().getDimensionPixelSize(R.dimen.center_padding);
        return spaceWidth;
    }
}
