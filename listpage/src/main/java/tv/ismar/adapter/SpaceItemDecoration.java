package tv.ismar.adapter;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by admin on 2017/6/9.
 */

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int spaceH;
    private int spaceV;
    private ArrayList<Integer> specialPos;

    public int getSpaceH() {
        return spaceH;
    }

    public int getSpaceV() {
        return spaceV;
    }

    public SpaceItemDecoration(int spaceH, int spaceV) {
        this.spaceH= spaceH;
        this.spaceV=spaceV;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if(view instanceof TextView){
            outRect.bottom=30;
        }else{
            outRect.bottom=spaceV;
        }
        outRect.left = spaceH;
    }

    public void setSpecialPos(ArrayList<Integer> specialPos) {
        this.specialPos = specialPos;
    }
}
