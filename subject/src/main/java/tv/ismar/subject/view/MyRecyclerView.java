package tv.ismar.subject.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by admin on 2017/3/6.
 */

public class MyRecyclerView extends RecyclerView {

    private int position = 0;

    public MyRecyclerView(Context context) {
        this(context,null);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setChildrenDrawingOrderEnabled(true);
    }

    public void setCurrentPosition(int pos){//刷新adapter前，在activity中调用这句传入当前选中的item在屏幕中的次序
        this.position = pos;
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        // TODO Auto-generated method stub
        if(i == childCount - 1){//这是最后一个需要刷新的item
            return position;
        }
        if(i == position){//这是原本要在最后一个刷新的item
            return childCount - 1;
        }
        return i;
    }
}
