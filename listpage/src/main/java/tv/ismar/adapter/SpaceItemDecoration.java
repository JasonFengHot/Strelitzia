package tv.ismar.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;

import tv.ismar.Utils.PosterUtil;
import tv.ismar.app.ui.ToastTip;
import tv.ismar.channel.FilterListActivity;
import tv.ismar.listpage.R;
import tv.ismar.searchpage.utils.JasmineUtil;

/**
 * Created by admin on 2017/6/9.
 * 自定义ItemDecoration，分别控制在海报区和标题区不同的间隔
 */

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int screenHeight;
    private final int screenWidth;
    private Context mContext;
    private int mTextStartMargin;
    private int mTextBaselineOffset;
    private int mTextHeight;
    private Paint mTextPaint;
    private int spaceH;
    private int spaceV;
    private int mTitleHeight;
    private boolean isVertical;
    private ArrayList<SpecialPos> specialPos;
    private Rect lastRect;
    private Bitmap lastBitmap;
    private Rect lastFilterRect;
    private Bitmap lastFliterBitmap;


    public int getSpaceH() {
        return spaceH;
    }

    public int getSpaceV() {
        return spaceV;
    }

    public SpaceItemDecoration(Context mContext, WindowManager windowManager, boolean isVertical) {
        this.isVertical=isVertical;
        this.mContext = mContext;
        if(isVertical){
            spaceH = mContext.getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_hs);
            spaceV = mContext.getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_vs);
            mTitleHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.list_section_vertical_title_h) + spaceV;
        }else{
            spaceH = mContext.getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_hs);
            spaceV = mContext.getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_vs);
            mTitleHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.list_section_horizontal_title_h) + spaceV;
        }

        mTextPaint = new Paint();
        mTextPaint.setColor(ContextCompat.getColor(mContext, R.color.module_color_white));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.filter_layout_current_section_title_ts));

        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        mTextHeight = (int) (fm.bottom - fm.top);
        mTextBaselineOffset = (int) fm.bottom;
        mTextStartMargin = mContext.getResources().getDimensionPixelOffset(R.dimen.item_decoration_title_start_margin);
        screenHeight = getScreenDisplayMetrics(windowManager).heightPixels;
        screenWidth = getScreenDisplayMetrics(windowManager).widthPixels;
    }

    private Bitmap scaleBitmap(Rect newRect) {
        try {
            Bitmap origin = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.bg);
            int height = origin.getHeight();
            int width = origin.getWidth();
            float scaleWidth = ((float) screenWidth) / width;
            float scaleHeight = ((float) screenHeight) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
            Bitmap fullscreenBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
            if (!origin.isRecycled()) {
                origin.recycle();
            }

            Bitmap resultBM =Bitmap.createBitmap(fullscreenBM,newRect.left,newRect.top,newRect.width(),newRect.height());
            if (!fullscreenBM.isRecycled()) {
                fullscreenBM.recycle();
            }
            return resultBM;
        }catch (Exception e){
            ToastTip.showToast(mContext,"rect:" + newRect);
            return null;
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildLayoutPosition(view);
        if(specialPos!=null) {
            if (view instanceof TextView) {
                outRect.bottom = spaceV / 2;
            } else {
                if (isVertical && (specialPos.contains(new SpecialPos(position)) || specialPos.contains(new SpecialPos(position - 1)) || specialPos.contains(new SpecialPos(position - 2)) || specialPos.contains(new SpecialPos(position - 3)) || specialPos.contains(new SpecialPos(position - 4)))) {
                    outRect.bottom = spaceV / 2;
                    outRect.top = mTitleHeight +  spaceV / 2;
                } else if (!isVertical && (specialPos.contains(new SpecialPos(position)) || specialPos.contains(new SpecialPos(position - 1)) || specialPos.contains(new SpecialPos(position - 2)))) {
                    outRect.bottom = spaceV / 2;
                    outRect.top = mTitleHeight +  spaceV / 2;

                } else {
                    outRect.bottom = spaceV / 2;
                    outRect.top = spaceV / 2;
                }
                if (specialPos.contains(new SpecialPos(position + 1))) {
                    int defaultSpanCount = ((FocusGridLayoutManager)parent.getLayoutManager()).getDefaultSpanCount();
                    int spanCount = PosterUtil.computeSectionSpanSize(specialPos, position, defaultSpanCount);
                    int itemWidth = (parent.getWidth() - parent.getPaddingRight()- parent.getPaddingLeft())/defaultSpanCount;
                    if(isVertical){
                        outRect.right =itemWidth* (spanCount - 1);
                    }else{
                        outRect.right = itemWidth* (spanCount - 1);
                    }
                    Log.i("zzz","zzz position:"+ position+"rect:" + outRect);
                }
            }

        }else{
            if(isVertical){
                if(position< FilterListActivity.VERTICAL_SPAN_COUNT){
                    outRect.bottom = spaceV / 2;
                    outRect.top = mTitleHeight +  spaceV / 2;
                }else{
                    outRect.bottom = spaceV / 2;
                    outRect.top = spaceV / 2;
                }
            }else{
                if(position< FilterListActivity.HORIZONTAL_SPAN_COUNT){
                    outRect.bottom = spaceV / 2;
                    outRect.top = mTitleHeight +  spaceV / 2;
                }else{
                    outRect.bottom = spaceV / 2;
                    outRect.top = spaceV / 2;
                }
            }
        }
        outRect.left = spaceH;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if(specialPos == null){
            return;
        }
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int position = params.getViewAdapterPosition();
            if (!specialPos.contains(new SpecialPos(position))) {
                continue;
            }
            drawTitleArea(c, child, position, parent);
        }
//        View v = parent.getFocusedChild();
//        if(v!= null){
//            JasmineUtil.scaleOut3(v);
//        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        final int position = ((LinearLayoutManager) parent.getLayoutManager()).findFirstVisibleItemPosition();
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        View child = parent.findViewHolderForAdapterPosition(position).itemView;
        if(specialPos != null){
            String initial = getTag(position);
            if (initial == null) {
                return;
            }
            boolean flag = false;
            int defaultSpanCount = ((FocusGridLayoutManager)parent.getLayoutManager()).getDefaultSpanCount();
            String nextTag = getTag(position + defaultSpanCount);
            boolean needCheck = false;
            int nextLinePos = 0;
            if(nextTag != null){
                if(nextTag.equals(initial)){
                    nextLinePos = position + defaultSpanCount;
                    //do nothing
                }else{
                    needCheck = true;
                }
            }else{
                //over max count
                needCheck = true;
            }
            boolean isSameTitle = true;
            if(needCheck){
                for (int i = 0; i < defaultSpanCount; i++) {
                    String checkTag = getTag(position + i);
                    if(checkTag != null){
                        if(checkTag.equals(initial)){
                            continue;
                            //do nothing
                        }else{
                            isSameTitle = false;
                            break;
                        }
                    }else{
                        //over max count
                    }
                }
            }
            if (!isSameTitle) {
                if (child.getHeight() + child.getTop() < mTitleHeight + spaceV / 2) {
                    c.save();
                    flag = true;
                    c.translate(0, child.getHeight() + child.getTop() - mTitleHeight + spaceV / 2 );
                }
            }
            Rect bgRect = new Rect(parent.getPaddingLeft(), parent.getPaddingTop(),
                    parent.getRight() - parent.getPaddingRight(), parent.getPaddingTop() + mTitleHeight );

            Rect globalRect = new Rect();
            parent.getGlobalVisibleRect(globalRect);
            Rect fullscreenRect = new Rect(parent.getPaddingLeft() + globalRect.left, parent.getPaddingTop() + globalRect.top,
                    parent.getRight() - parent.getPaddingRight() + globalRect.left, parent.getPaddingTop() + mTitleHeight + globalRect.top);
            if( lastRect == null || !lastRect.equals(fullscreenRect)){
                Bitmap bitmap = scaleBitmap(fullscreenRect);
                if(bitmap!= null){
                    c.drawBitmap(bitmap,bgRect,bgRect,null);
                    if(lastBitmap != null && !lastBitmap.isRecycled()){
                        lastBitmap.recycle();
                    }
                    lastBitmap = bitmap;
                }
            }else{
                if(lastBitmap != null) {
                    c.drawBitmap(lastBitmap, bgRect, bgRect, null);
                }
            }
            lastRect = new Rect(fullscreenRect);
//            c.drawRect(bgRect, mBackgroundPaint);
            int y = parent.getPaddingTop() + mTitleHeight - spaceV/2 - mTextBaselineOffset;
            c.drawText(initial, child.getPaddingLeft() + mTextStartMargin,
                    parent.getPaddingTop() + mTitleHeight - spaceV/2 - mTextBaselineOffset, mTextPaint);
            Log.i("zzz","zzz draw overtitle y:" + y);
            if (flag) {
                c.restore();
            }
        }else{
            Rect bgRect = new Rect(parent.getPaddingLeft(), parent.getPaddingTop(),
                    parent.getRight() - parent.getPaddingRight(), parent.getPaddingTop() + mTitleHeight );
            Rect globalRect = new Rect();
            parent.getGlobalVisibleRect(globalRect);
            Rect fullscreenRect = new Rect(parent.getPaddingLeft() + globalRect.left, parent.getPaddingTop() + globalRect.top,
                    parent.getRight() - parent.getPaddingRight() + globalRect.left, parent.getPaddingTop() + mTitleHeight + globalRect.top);
            if( lastFilterRect == null || !lastFilterRect.equals(fullscreenRect)){
                Bitmap bitmap = scaleBitmap(fullscreenRect);
                if(bitmap!= null){
                    c.drawBitmap(bitmap,bgRect,bgRect,null);
                    if(lastFliterBitmap != null && !lastFliterBitmap.isRecycled()){
                        lastFliterBitmap.recycle();
                    }
                    lastFliterBitmap = bitmap;
                }
            }else{
                if(lastFliterBitmap != null) {
                    c.drawBitmap(lastFliterBitmap, bgRect, bgRect, null);
                }
            }
            lastFilterRect = new Rect(fullscreenRect);
        }

    }

    private void drawTitleArea(Canvas c, View child, int position, RecyclerView parent) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        final int rectBottom = child.getTop() - params.topMargin;
//        final int left = parent.getPaddingLeft();
//        final int right = parent.getWidth() - parent.getPaddingRight();
//        Rect bgRect = new Rect(left, rectBottom - mTitleHeight, right,
//                rectBottom);
//        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.top_bg);
//        if(bitmap!= null){
//            c.drawBitmap(bitmap,bgRect,bgRect,null);
//            if(!bitmap.isRecycled()){
//                bitmap.recycle();
//            }
//        }
//        c.drawRect(bgRect, mBackground2Paint);

        int index =specialPos.indexOf(new SpecialPos(position));
        String sections = "";
        if(index > -1){
            sections =  specialPos.get(index).sections;
        }
        int y = rectBottom -spaceV/2 - (mTitleHeight - mTextHeight) / 2 - mTextBaselineOffset + 4;
        c.drawText(sections, child.getPaddingLeft() + mTextStartMargin,
                y, mTextPaint);
//        Log.i("zzz","zzz draw title y:" + y);
    }

    public void setSpecialPos(ArrayList<SpecialPos> specialPos) {
        this.specialPos = specialPos;
    }

    private String getTag(int position) {
        int idx = getPositionSpecialIndex(position);
        if(idx != -1){
            return specialPos.get(idx).sections;
        }else{
            return null;
        }
    }

    private int getPositionSpecialIndex(int position) {
        for (int i = 0; i < specialPos.size(); i++) {
            if(position<= specialPos.get(i).endPosition){
                return i;
            }
        }
        return -1;
    }


    private DisplayMetrics getScreenDisplayMetrics(WindowManager wm){
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics",DisplayMetrics.class);
            method.invoke(display, dm);
        }catch(Exception e){
            e.printStackTrace();
        }
        return dm;
    }
}
