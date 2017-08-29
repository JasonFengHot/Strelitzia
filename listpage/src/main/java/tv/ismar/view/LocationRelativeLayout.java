package tv.ismar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by admin on 2017/8/29.
 */

public class LocationRelativeLayout extends RelativeLayout {

    private View arrow_up_left;
    private View arrow_up_right;
    private View arrow_down_left;
    private View arrow_down_right;
    private boolean show_left_up=true;
    private boolean show_left_down=true;
    private boolean show_right_up=true;
    private boolean show_right_down=true;
    private int xBoundary=0;

    public void setArrow_up_left(View arrow_up_left) {
        this.arrow_up_left = arrow_up_left;
    }

    public void setArrow_up_right(View arrow_up_right) {
        this.arrow_up_right = arrow_up_right;
    }

    public void setArrow_down_left(View arrow_down_left) {
        this.arrow_down_left = arrow_down_left;
    }

    public void setArrow_down_right(View arrow_down_right) {
        this.arrow_down_right = arrow_down_right;
    }

    public void setxBoundary(int xBoundary) {
        this.xBoundary = xBoundary;
    }

    public void setShow_left_up(boolean show_left_up) {
        this.show_left_up = show_left_up;
    }

    public void setShow_left_down(boolean show_left_down) {
        this.show_left_down = show_left_down;
    }

    public void setShow_right_up(boolean show_right_up) {
        this.show_right_up = show_right_up;
    }

    public void setShow_right_down(boolean show_right_down) {
        this.show_right_down = show_right_down;
    }

    public LocationRelativeLayout(Context context) {
        super(context);
    }

    public LocationRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LocationRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
            if(event.getX()>xBoundary){
                if(arrow_down_right!=null&&show_right_down){
                    arrow_down_right.setVisibility(View.VISIBLE);
                }
                if(arrow_up_right!=null&&show_right_up){
                    arrow_up_right.setVisibility(View.VISIBLE);
                }
                if(arrow_down_left!=null){
                    arrow_down_left.setVisibility(View.GONE);
                }
                if(arrow_up_left!=null){
                    arrow_up_left.setVisibility(View.GONE);
                }
            }else{
                if(arrow_down_right!=null){
                    arrow_down_right.setVisibility(View.GONE);
                }
                if(arrow_up_right!=null){
                    arrow_up_right.setVisibility(View.GONE);
                }
                if(arrow_down_left!=null&&show_left_down){
                    arrow_down_left.setVisibility(View.VISIBLE);
                }
                if(arrow_up_left!=null&&show_left_up){
                    arrow_up_left.setVisibility(View.VISIBLE);
                }
            }
        }
        return super.dispatchHoverEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(arrow_down_right!=null){
            arrow_down_right.setVisibility(View.GONE);
        }
        if(arrow_up_right!=null){
            arrow_up_right.setVisibility(View.GONE);
        }
        if(arrow_down_left!=null){
            arrow_down_left.setVisibility(View.GONE);
        }
        if(arrow_up_left!=null){
            arrow_up_left.setVisibility(View.GONE);
        }
        return super.dispatchKeyEvent(event);
    }
}
