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
    private boolean isVertical;
    private ArrayList<Integer> specialPos;

    public int getSpaceH() {
        return spaceH;
    }

    public int getSpaceV() {
        return spaceV;
    }

    public SpaceItemDecoration(int spaceH, int spaceV,boolean isVertical) {
        this.spaceH= spaceH;
        this.spaceV=spaceV;
        this.isVertical=isVertical;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if(view instanceof TextView){
            outRect.bottom=spaceV/2;
        }else if(isVertical&&(specialPos.contains(parent.getChildLayoutPosition(view)+1)||specialPos.contains(parent.getChildLayoutPosition(view)+2)||specialPos.contains(parent.getChildLayoutPosition(view)+3)||specialPos.contains(parent.getChildLayoutPosition(view)+4)||specialPos.contains(parent.getChildLayoutPosition(view)+5))) {
            outRect.bottom=spaceV/2;
        }else if(!isVertical&&(specialPos.contains(parent.getChildLayoutPosition(view)+1)||specialPos.contains(parent.getChildLayoutPosition(view)+2)||specialPos.contains(parent.getChildLayoutPosition(view)+3))){
            outRect.bottom=spaceV/2;
        }else {
            outRect.bottom=spaceV;
        }
        outRect.left = spaceH;
    }

    public void setSpecialPos(ArrayList<Integer> specialPos) {
        this.specialPos = specialPos;
    }
}
