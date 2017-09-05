package tv.ismar.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by liucan on 2017/8/25.
 */

public class HistoryFavoriteRecyclerView extends RecyclerView {
    private int position=0;
    public void setCurrentPosition(int pos){//刷新adapter前，在activity中调用这句传入当前选中的item在屏幕中的次序
        this.position= pos;
    }
    protected void setChildrenDrawingOrderEnabled(boolean enabled)
    {
        //TODOAuto-generated method stub
        super.setChildrenDrawingOrderEnabled(enabled);
    }
    @Override

    protected int getChildDrawingOrder(int childCount,int i)
    {
        //return super.getChildDrawingOrder(childCount, i);

        if(i==childCount-1){//这是最后一个需要刷新的item
            return position;
         }

        if(i ==position){//这是原本要在最后一个刷新的item
            return childCount-1;
        }
        return i;//正常次序的item
    }


    public HistoryFavoriteRecyclerView(Context context) {
        super(context);
        setChildrenDrawingOrderEnabled(true);
    }

    public HistoryFavoriteRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HistoryFavoriteRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
