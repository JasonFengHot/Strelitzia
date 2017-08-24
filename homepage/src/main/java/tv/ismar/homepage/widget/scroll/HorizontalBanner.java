package tv.ismar.homepage.widget.scroll;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

/**
 * @AUTHOR: xi
 * @DATE: 2017/7/31
 * @DESC: 横向滑动view
 */

public class HorizontalBanner extends ScrollContainer {

    public HorizontalBanner(Context context) {
        super(context);
        init();
    }

    public HorizontalBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
    }

    public void keyUp(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_DPAD_UP || keyCode== KeyEvent.KEYCODE_DPAD_DOWN){//回到记录的上一次焦点
            View view = getChildAt(mSelectedChildIndex);
            if(view.isFocusable()){//判断是否可以获取焦点，默认focusable为false
                view.setFocusable(true);
                view.requestFocus();
                scrollToCenterX();
            }
        }
    }

    @Override
    protected boolean canHover(int keyCode){
        if(keyCode== KeyEvent.KEYCODE_DPAD_LEFT && mSelectedChildIndex==0){
            return false;
        }
        if(keyCode== KeyEvent.KEYCODE_DPAD_RIGHT && mSelectedChildIndex==getChildCount()-1){
            return false;
        }
        return super.canHover(keyCode);
    }

    public void keyDown(int keyCode, KeyEvent event) {
        boolean temp = false;
        if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
            if(mSelectedChildIndex > 0){
                mSelectedChildIndex--;
                temp = true;
            }
        } else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
            if(mSelectedChildIndex < getChildCount()-1){
                mSelectedChildIndex++;
                temp = true;
            }
        }
        Log.i(TAG, "mSelectedChildIndex:"+mSelectedChildIndex);
        if(temp){
            scrollToCenterX();
        }
    }
}
