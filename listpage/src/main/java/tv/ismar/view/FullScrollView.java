package tv.ismar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by admin on 2017/8/29.
 */

public class FullScrollView extends ScrollView {


    private OnScroll onScroll;
    public interface OnScroll{
        void onShowUp(boolean showUp);
        void onShowDown(boolean showDown);
    }
    public FullScrollView(Context context) {
        this(context,null);
    }

    public FullScrollView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FullScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnScroll(OnScroll onScroll) {
        this.onScroll = onScroll;
    }
    

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if(t==0){
            onScroll.onShowUp(false);
        }else{
            onScroll.onShowUp(true);
        }
        if(getScrollY()+getHeight()>=computeVerticalScrollRange()){
            onScroll.onShowDown(false);
        }else{
            onScroll.onShowDown(true);
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction()==MotionEvent.ACTION_MOVE) {
            return true;
        }else{
            return super.dispatchTouchEvent(ev);
        }
    }
}
