package tv.ismar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import tv.ismar.listpage.R;

/**
 * Created by admin on 2017/8/29.
 */

public class FullScrollView extends ScrollView {

    public FullScrollView(Context context) {
        this(context,null);
    }

    public FullScrollView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FullScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {

        if(Math.abs(oldt-t)==1){
            return;
        }
        if(t<oldt){
            //向上滚
            smoothScrollBy(0,getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_lenth));
        }else{
            //向下滚
            smoothScrollBy(0,getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_lenth));
        }
    }
}
