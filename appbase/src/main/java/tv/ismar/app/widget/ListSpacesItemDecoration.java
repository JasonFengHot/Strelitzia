package tv.ismar.app.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by mac on 15/10/28.
 */
public class ListSpacesItemDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public ListSpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if(parent.getChildAdapterPosition(view)!=0&&parent.getChildAdapterPosition(view)%2==0){
            outRect.top=space;
            outRect.bottom=0;
            outRect.left=0;
            outRect.right=0;
        }else if(parent.getChildAdapterPosition(view)%2==1){
            outRect.bottom=space;
            outRect.top=0;
            outRect.left=0;
            outRect.right=0;
        }else{
            outRect.bottom=0;
            outRect.top=0;
            outRect.left=0;
            outRect.right=0;
        }

    }

}
