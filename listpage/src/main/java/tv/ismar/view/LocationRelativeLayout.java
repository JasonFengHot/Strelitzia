package tv.ismar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by admin on 2017/8/29.
 * 整个列表页的自定义RelativeLayout，用于控制左右界面上下箭头的显示与隐藏
 * 以及空鼠在空白处焦点隐藏的功能
 */

public class LocationRelativeLayout extends RelativeLayout {

    private View arrow_up_left;
    private View arrow_up_right;
    private View arrow_down_left;
    private View arrow_down_right;
    private boolean show_left_up=false;
    private boolean show_left_down=true;
    private boolean show_right_up=false;
    private boolean show_right_down=true;
    private int xBoundary=0;
	/*modify by dragontec for bug 4468 start*/
    public boolean horving = true;
    /*modify by dragontec for bug 4468 end*/

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
        if(horving){
            if(show_left_up) {
                arrow_up_left.setVisibility(View.VISIBLE);
            }else{
                arrow_up_left.setVisibility(View.GONE);
            }
        }else{
            arrow_up_left.setVisibility(View.GONE);
        }
    }

    public void setShow_left_down(boolean show_left_down) {
        this.show_left_down = show_left_down;
        if(horving){
            if(show_left_down) {
                arrow_down_left.setVisibility(View.VISIBLE);
            }else{
                arrow_down_left.setVisibility(View.GONE);
            }
        }else{
            arrow_down_left.setVisibility(View.GONE);
        }
    }

    public void setShow_right_up(boolean show_right_up) {
        this.show_right_up = show_right_up;
        if(horving){
            if(show_right_up) {
                arrow_up_right.setVisibility(View.VISIBLE);
            }else{
                arrow_up_right.setVisibility(View.GONE);
            }
        }else{
            arrow_up_right.setVisibility(View.GONE);
        }
    }

    public void setShow_right_down(boolean show_right_down) {
        this.show_right_down = show_right_down;
        if(horving){
            if(show_right_down) {
                arrow_down_right.setVisibility(View.VISIBLE);
            }else{
                arrow_down_right.setVisibility(View.GONE);
            }
        }else{
            arrow_down_right.setVisibility(View.GONE);
        }
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
            horving = true;
            if(event.getX()>xBoundary){
                if(arrow_down_right!=null){
                    if(show_right_down) {
                        arrow_down_right.setVisibility(View.VISIBLE);
                    }else{
                        arrow_down_right.setVisibility(View.GONE);
                    }
                }
                if(arrow_up_right!=null){
                    if(show_right_up) {
                        arrow_up_right.setVisibility(View.VISIBLE);
                    }else{
                        arrow_up_right.setVisibility(View.GONE);
                    }
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
                if(arrow_down_left!=null){
                    if(show_left_down) {
                        arrow_down_left.setVisibility(View.VISIBLE);
                    }else{
                        arrow_down_left.setVisibility(View.GONE);
                    }
                }
                if(arrow_up_left!=null){
                    if(show_left_up) {
                        arrow_up_left.setVisibility(View.VISIBLE);
                    }else{
                        arrow_up_left.setVisibility(View.GONE);
                    }
                }
            }
        }
        return super.dispatchHoverEvent(event);
    }


    @Override
    public boolean onHoverEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE) {
            requestFocus();
        }
            return super.onHoverEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        horving=false;
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
