package tv.ismar.adapter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by admin on 2017/6/9.
 */

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int spaceH;
    private int spaceV;

    public SpaceItemDecoration(int spaceH,int spaceV) {
        this.spaceH= spaceH;
        this.spaceV=spaceV;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = spaceH;
        outRect.bottom = spaceV;
    }

}
