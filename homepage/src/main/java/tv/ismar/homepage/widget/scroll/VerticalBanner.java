package tv.ismar.homepage.widget.scroll;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * @AUTHOR: xi
 * @DATE: 2017/7/31
 * @DESC: 竖向滑动view
 */

public class VerticalBanner extends ScrollContainer {

    private boolean mCanScroll = true;

    public VerticalBanner(Context context) {
        super(context);
        init();
    }

    public VerticalBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
    }

    /*设置控件是否可以滑动*/
    public void setCanScroll(boolean canScroll){
        this.mCanScroll = canScroll;
    }

    @Override
    protected boolean canScroll() {
        return mCanScroll;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void keyDown(int keyCode, KeyEvent event) {
        boolean temp = false;
        if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
            if(mSelectedChildIndex > 0){
                mSelectedChildIndex--;
                temp = true;
            }
        } else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
            if(mSelectedChildIndex < getChildCount()-1){
                mSelectedChildIndex++;
                temp = true;
            }
        }
        if(temp && mCanScroll){
//            scrollToCenterY();
            scrollToTop();
        }
    }


}
