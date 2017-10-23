package tv.ismar.homepage.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by admin on 2017/10/22.
 */

public class HomeRootRelativeLayout extends RelativeLayout {

    private View upArrow;
    private View downArrow;
    private boolean showUp=false;
    private boolean showDown=true;

    public HomeRootRelativeLayout(Context context) {
        this(context,null);
    }

    public HomeRootRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HomeRootRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
            if(upArrow!=null) {
                if (showUp) {
                    upArrow.setVisibility(View.VISIBLE);
                } else {
                    upArrow.setVisibility(View.GONE);
                }
            }
            if(downArrow!=null) {
                if (showDown) {
                    downArrow.setVisibility(View.VISIBLE);
                } else {
                    downArrow.setVisibility(View.GONE);
                }
            }
        }
        return super.dispatchHoverEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(upArrow!=null) {
            upArrow.setVisibility(View.GONE);
        }
        if(downArrow!=null) {
            downArrow.setVisibility(View.GONE);
        }
        return super.dispatchKeyEvent(event);
    }

    public void setUpArrow(View upArrow) {
        this.upArrow = upArrow;
    }

    public void setDownArrow(View downArrow) {
        this.downArrow = downArrow;
    }

    public void setShowUp(boolean showUp) {
        this.showUp = showUp;
    }

    public void setShowDown(boolean showDown) {
        this.showDown = showDown;
    }
}
