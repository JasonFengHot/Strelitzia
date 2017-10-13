package tv.ismar.adapter;

import android.content.Context;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;

import tv.ismar.listpage.R;

/**
 * Created by admin on 2017/6/20.
 * 自定义recyclerview的layoutmanager
 * 定制焦点的特殊移动规则、控制recyclerview是否可以滚动、设置nextleftFocusView
 */

public class FocusGridLayoutManager extends GridLayoutManager {

    private ArrayList<Integer> specialPos;
    private View leftFocusView;
    private Context context;
    private boolean scroll=false;
    private boolean isFavorite=false;


    public FocusGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FocusGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
        this.context=context;
    }


    public FocusGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    public void setFavorite(boolean favorite){
        isFavorite=favorite;
    }
    @Override
    public int getChildCount() {
        return super.getChildCount();
    }


    @Override
    public View getChildAt(int index) {
        return super.getChildAt(index);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }


    @Override
    public View getFocusedChild() {
        return super.getFocusedChild();
    }

    @Override
    public int getPosition(View view) {
        if(view.getLayoutParams() instanceof RecyclerView.LayoutParams) {
            return super.getPosition(view);
        }else{
            return -1;
        }
    }

    @Override
    public int getSpanCount() {
        return super.getSpanCount();
    }

    @Override
    public View onInterceptFocusSearch(View focused, int direction) {
        int index=getPosition(focused);
        if(direction==View.FOCUS_RIGHT){
            if(specialPos!=null&&specialPos.contains(index+1)){
                int nextPos = getNextViewPos(getPosition(focused), direction);
                scrollToPositionWithOffset(nextPos,0);
                scroll=true;
                View nextView=findViewByPosition(nextPos+1);
                return nextView;
            }
            if(index==getItemCount()-1){
                YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
                return focused;
            }
        }else if(direction==View.FOCUS_UP){
            if(specialPos!=null?index<=getSpanCount():index<getSpanCount()){
                YoYo.with(Techniques.VerticalShake).duration(1000).playOn(focused);
                return focused;
            }
        }else if(direction==View.FOCUS_LEFT){
            if(isFavorite&&index==0){
                YoYo.with(Techniques.VerticalShake).duration(1000).playOn(focused);
                return focused;
            }
        }
        return super.onInterceptFocusSearch(focused, direction);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        if(scroll){
            scroll=false;
            getChildAt(1).requestFocus();
        }
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {

        // Need to be called in order to layout new row/column
        View nextFocus = super.onFocusSearchFailed(focused, focusDirection, recycler, state);
        if(!isFavorite) {
            if (nextFocus == null && focusDirection == View.FOCUS_LEFT) {
                return leftFocusView;
            }
        }
        /**
         * 获取当前焦点的位置
         */
        int fromPos = getPosition(focused);
        /**
         * 获取我们希望的下一个焦点的位置
         */
        int nextPos = getNextViewPos(fromPos, focusDirection);
        View nextView=findViewByPosition(nextPos);
        if(focusDirection==View.FOCUS_DOWN) {
            for (int i = fromPos; i < nextPos; i++) {
                if (specialPos!=null&&specialPos.contains(i)) {
                    int nextSpecialPos=specialPos.indexOf(i);
                    int lastColumnCount=(i-specialPos.get(nextSpecialPos-1)-1)%getSpanCount();
                    if(lastColumnCount==0){
                        lastColumnCount=getSpanCount();
                    }
                    int currentLine=fromPos-specialPos.get(nextSpecialPos)+lastColumnCount+1;
                    if(i+currentLine>=specialPos.get(nextSpecialPos+1)){
                        nextView = findViewByPosition(specialPos.get(nextSpecialPos+1)-1);
                    }else{
                        nextView = findViewByPosition(i+currentLine);
                    }
                    break;
                }
            }
        }else if(focusDirection==View.FOCUS_UP){
            for (int i = fromPos; i >= nextPos; i--) {
                if (specialPos!=null&&specialPos.contains(i)) {
                    int nextSpecialPos=specialPos.indexOf(i);
                    int lastColumnCount=(i-specialPos.get(nextSpecialPos-1)-1)%getSpanCount();
                    if(lastColumnCount==0){
                        lastColumnCount=getSpanCount();
                    }
                    int currentLine=fromPos-i;
                    if(currentLine>lastColumnCount){
                        nextView=findViewByPosition(i-1);
                    }else{
                        nextView = findViewByPosition(i-(lastColumnCount-currentLine)-1);
                    }
                    break;
                }
            }
        }
        if(nextView instanceof TextView){
            nextView=findViewByPosition(nextPos+1);
        }
        if(nextView==null&&focusDirection==View.FOCUS_RIGHT){
            nextView=focused;
        }
        if(nextView==null&&focusDirection==View.FOCUS_DOWN){
            nextView=focused;
            YoYo.with(Techniques.VerticalShake).duration(1000).playOn(nextView);
        }
        return nextView;

    }


    protected int getNextViewPos(int fromPos, int direction) {
        int offset = calcOffsetToNextView(direction);
        if(!isFavorite) {
            if (hitBorder(fromPos, offset)) {
                return fromPos + offset + 1;
            }
        }

        return fromPos + offset;
    }

    /**
     * Calculates position offset.
     *
     * @param direction regular {@code View.FOCUS_*}.
     * @return position offset according to {@code direction}.
     */
    protected int calcOffsetToNextView(int direction) {
        int spanCount = getSpanCount();
        int orientation = getOrientation();

        if (orientation == VERTICAL) {
            switch (direction) {
                case View.FOCUS_DOWN:
                    return spanCount;
                case View.FOCUS_UP:
                    return -spanCount;
                case View.FOCUS_RIGHT:
                    return 1;
                case View.FOCUS_LEFT:
                    return -1;
            }
        } else if (orientation == HORIZONTAL) {
            switch (direction) {
                case View.FOCUS_DOWN:
                    return 1;
                case View.FOCUS_UP:
                    return -1;
                case View.FOCUS_RIGHT:
                    return spanCount;
                case View.FOCUS_LEFT:
                    return -spanCount;
            }
        }

        return 0;
    }

    /**
     * Checks if we hit borders.
     *
     * @param from from what position.
     * @param offset offset to new position.
     * @return {@code true} if we hit border.
     */
    private boolean hitBorder(int from, int offset) {
        int spanCount = getSpanCount();

        if (offset== -1) {
            int spanIndex = from % spanCount;
            int newSpanIndex = spanIndex + offset;
            return newSpanIndex < 0 || newSpanIndex >= spanCount;
        } else {
            int newPos = from + offset;
            return newPos < 0 && newPos >= spanCount;
        }
    }

    public boolean isCanScroll() {
        return canScroll;
    }

    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    private boolean canScroll=false;
    @Override
    public boolean canScrollVertically() {
        return canScroll;
    }

    public void setSpecialPos(ArrayList<Integer> specialPos) {
        this.specialPos = specialPos;
    }

    public void setLeftFocusView(View leftFocusView) {
        this.leftFocusView = leftFocusView;
    }


}
