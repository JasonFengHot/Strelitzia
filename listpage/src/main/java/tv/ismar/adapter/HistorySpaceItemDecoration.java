package tv.ismar.adapter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by liucan on 2017/8/30.
 */

public class HistorySpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int spaceL,spaceR,spaceT,spaceB;
    public HistorySpaceItemDecoration(int l,int r,int t,int b) {
        spaceL=l;
        spaceR=r;
        spaceT=t;
        spaceB=b;
    }
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = spaceL;
        outRect.bottom = spaceB;
        outRect.right=spaceR;
        outRect.top=spaceT;
    }
}
