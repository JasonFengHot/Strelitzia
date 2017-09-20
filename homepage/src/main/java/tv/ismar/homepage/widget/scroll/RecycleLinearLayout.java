package tv.ismar.homepage.widget.scroll;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * @AUTHOR: xi
 * @DATE: 1017/9/17
 * @DESC: 可回收linear
 */

public class RecycleLinearLayout extends LinearLayout {
    private int mSelectedChildIndex = 0;
    private ArrayList<View> mAllViews = new ArrayList<>();

    public RecycleLinearLayout(Context context) {
        super(context);
        init();
    }

    public RecycleLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        setOrientation(LinearLayout.VERTICAL);
    }

    public void setView(View view){
        if(view==null) return;
        mAllViews.add(view);
    }

    public void initView(){
        mSelectedChildIndex = 0;
        addView(mAllViews.get(0));
        addView(mAllViews.get(1));
        addView(mAllViews.get(2));
    }

    public void downEvent(){//向下按键
        if(mSelectedChildIndex < mAllViews.size()-1){
            mSelectedChildIndex++;
            Log.i("RecycleLinearLayout", "down mSelectedChildIndex:"+mSelectedChildIndex);
            if(getChildCount() > 1){
                removeViewAt(0);
            }
            if(mSelectedChildIndex+2<mAllViews.size() &&
                    !mAllViews.get(mSelectedChildIndex+2).isAttachedToWindow()){
                addView(mAllViews.get(mSelectedChildIndex+2));
            }
            requestFirstFocuse();
        }
    }

    public void upEvent(){//向上
        if(mSelectedChildIndex > 0){
            mSelectedChildIndex--;
            Log.i("RecycleLinearLayout", "up mSelectedChildIndex:"+mSelectedChildIndex);
            if(getChildCount() > 1){
                removeViewAt(getChildCount()-1);
            }
            if(mSelectedChildIndex >= 0 &&
                    !mAllViews.get(mSelectedChildIndex).isAttachedToWindow()){
                addView(mAllViews.get(mSelectedChildIndex), 0);
            }
            requestFirstFocuse();
        }

    }

    private void requestFirstFocuse(){
//        getChildAt(0).setFocusable(true);
        getChildAt(0).requestFocus();
    }

    public void clearView(){
        mAllViews.clear();
        removeAllViews();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
                upEvent();
            } else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                downEvent();
            }
        }
        if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN || keyCode==KeyEvent.KEYCODE_DPAD_UP){
            if(mSelectedChildIndex!=0){
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
