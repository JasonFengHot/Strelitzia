package tv.ismar.homepage.widget.scroll;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

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

    private void shakeWhenBottom(){
        if(mSelectedChildIndex >= mAllViews.size()-1){
            YoYo.with(Techniques.VerticalShake).duration(1000).playOn(mAllViews.get(mAllViews.size()-1));
        }
    }

    /*向下按键*/
    public void downEvent(){
        if(mSelectedChildIndex < mAllViews.size()-1){
            mSelectedChildIndex++;
            Log.i("RecycleLinearLayout", "down listsize:"+mAllViews.size());
            Log.i("RecycleLinearLayout", "down mSelectedChildIndex:"+mSelectedChildIndex);
            if(mSelectedChildIndex+2<mAllViews.size() &&
                    (mAllViews.get(mSelectedChildIndex+2).getWindowVisibility()==View.GONE)){
                Log.i("RecycleLinearLayout", "down visibility:"+(mAllViews.get(mSelectedChildIndex+2).getWindowVisibility()==View.GONE));
                addView(mAllViews.get(mSelectedChildIndex+2));
            }
            if(getChildCount() > 2){//常驻2个子view
                getChildAt(1).requestFocus();
                removeViewAt(0);
            }
//            if(mSelectedChildIndex == mAllViews.size()-2){
//                getChildAt(1).requestFocus();
//            }
        }
    }

    /*向上按键*/
    public void upEvent(){
        if(mSelectedChildIndex > 0){
            mSelectedChildIndex--;
            Log.i("RecycleLinearLayout", "up mSelectedChildIndex:"+mSelectedChildIndex);
            if(getChildCount() > 2){
                removeViewAt(getChildCount()-1);
            }
            if(mSelectedChildIndex >= 0 &&
                    (mAllViews.get(mSelectedChildIndex).getWindowVisibility()==View.GONE)){
                addView(mAllViews.get(mSelectedChildIndex), 0);
            }
            getChildAt(0).requestFocus();
        }

    }

    private void removeViewWithAnimation(){

    }

    private void addViewWithAnimation(){

    }

    public void clearView(){
        mAllViews.clear();
        removeAllViews();
    }

    private boolean mIsNotFirst = false;//判断是否已经到达第一个位置

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        Log.i("RecycleLinearLayout", "action:"+event.getAction()+" keyCode:"+keyCode);
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
                upEvent();
            } else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                downEvent();
            }
        }
        //        if(mSelectedChildIndex>=mAllViews.size()-1 && keyCode==KeyEvent.KEYCODE_DPAD_DOWN){//处理底部跳出该view时，焦点的处理
//            setNextFocusDownId(R.id.get_more_btn);
//            return false;
//        }
        if(event.getAction()==KeyEvent.ACTION_UP && //处理顶部跳入到该view时的焦点
                keyCode==KeyEvent.KEYCODE_DPAD_DOWN &&
                mSelectedChildIndex==0){
            getChildAt(0).requestFocus();
        }
        if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN || keyCode==KeyEvent.KEYCODE_DPAD_UP){//如果没有到达view第一个或最后一个位置，焦点要始终在改容器view中
            if((mSelectedChildIndex>0) && (mSelectedChildIndex<mAllViews.size()-1)) return true;
        }
        if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN &&   //到最后一个子view会抖动
                mSelectedChildIndex>=mAllViews.size()-1){
            shakeWhenBottom();
        }
        return super.dispatchKeyEvent(event);
    }
}
