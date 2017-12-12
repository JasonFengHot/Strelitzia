package tv.ismar.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tv.ismar.Utils.PosterUtil;
import tv.ismar.adapter.FocusGridLayoutManager;
import tv.ismar.adapter.SpecialPos;
import tv.ismar.app.widget.MyRecyclerView;
import tv.ismar.channel.FilterListActivity;
import tv.ismar.listpage.R;

public class FilterListRecyclerView extends MyRecyclerView {
    private int itemHeight;
    private boolean blockFocusScrollWhenManualScroll = true;
    private OnRecyclerScrollListener mScrollListener = null;

    public FilterListRecyclerView(Context context) {
        super(context);
        init();
    }

    public FilterListRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FilterListRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public interface OnRecyclerScrollListener{
        void onScrollIdle();
    }

    private void init() {
        addOnScrollListener(new FilterListRecyclerView.CustomOnScrollListener());
    }

    private class CustomOnScrollListener extends OnScrollListener{
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            FocusGridLayoutManager layoutManager = (FocusGridLayoutManager)getLayoutManager();
            switch (newState){
                case SCROLL_STATE_IDLE:
                    Picasso.with(getContext()).resumeTag(FilterListActivity.PICASSO_TAG);
                    layoutManager.setCanScroll(false);
                    blockFocusScrollWhenManualScroll = false;
                    if(mScrollListener != null){
                        mScrollListener.onScrollIdle();
                    }
                    break;
                case SCROLL_STATE_DRAGGING:
                    Picasso.with(getContext()).pauseTag(FilterListActivity.PICASSO_TAG);
                    layoutManager.setCanScroll(true);
                    break;
                case SCROLL_STATE_SETTLING:
                    Picasso.with(getContext()).pauseTag(FilterListActivity.PICASSO_TAG);
                    layoutManager.setCanScroll(true);
                    break;
                default:
                    break;
            }

        }
    }

    @Override
    public void scrollBy(int x, int y) {
        super.scrollBy(x, y);
    }

    @Override
    public void smoothScrollBy(int dx, int dy) {
        if(dy < 0){
            FocusGridLayoutManager layoutManager = (FocusGridLayoutManager)getLayoutManager();
            View focusdChild = getFocusedChild();
            if(focusdChild != null){
                int currentFocusPos = layoutManager.getPosition(focusdChild);
                int defaultSpanCount = layoutManager.getDefaultSpanCount();
                boolean isVertical = defaultSpanCount ==FilterListActivity.VERTICAL_SPAN_COUNT;
                int scrollOffset = getScrollOffset();
                if(itemHeight == -1) {
                    int firstCompletelyVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                    View firstCompletelyVisibleView = layoutManager.findViewByPosition(firstCompletelyVisibleItemPosition);
                    Rect itemRect = new Rect();
                    firstCompletelyVisibleView.getGlobalVisibleRect(itemRect);
                    itemHeight = itemRect.height();
                }
                ArrayList<SpecialPos> specialPos = layoutManager.getSpecialPos();
                int targetPos;
                if(specialPos != null){
                    //在title下面一行
                    //计算向上目标的状态
                    int curFoucsPositionInLine = PosterUtil.computePositionInLine(specialPos, currentFocusPos, defaultSpanCount);
                    targetPos = currentFocusPos - curFoucsPositionInLine - 1;

                    int targetIndex = -1;
                    for (int i = 0; i < defaultSpanCount; i++) {
                        targetIndex = specialPos.indexOf(new SpecialPos(targetPos - i));
                        if(targetIndex> -1){
                            break;
                        }
                    }
                    if(targetIndex == -1){
                        //not Title
                        dy = dy -scrollOffset + getSpaceV()/2;
                    }
                }else{
                    targetPos = currentFocusPos;
                    if(isVertical){
                        if(targetPos >= FilterListActivity.VERTICAL_SPAN_COUNT){
                            targetPos -= FilterListActivity.VERTICAL_SPAN_COUNT;
                        }
                        if(targetPos >= FilterListActivity.VERTICAL_SPAN_COUNT){
                            dy -= scrollOffset;
                        }else{
                           //nothing
                        }
                    }else{
                        if(targetPos >= FilterListActivity.HORIZONTAL_SPAN_COUNT){
                            targetPos -= FilterListActivity.HORIZONTAL_SPAN_COUNT;
                        }
                        if(targetPos >= FilterListActivity.HORIZONTAL_SPAN_COUNT){
                            dy -= scrollOffset;
                        }else{
                            //nothing
                        }
                    }
                }
            }
        }
        super.smoothScrollBy(dx, dy);
    }

    public void directSmoothScrollBy(int dx, int dy) {
        this.blockFocusScrollWhenManualScroll = true;
        ((FocusGridLayoutManager)getLayoutManager()).setCanScroll(true);
        super.smoothScrollBy(dx, dy);
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        if(blockFocusScrollWhenManualScroll){
            return false;
        }
        return super.requestChildRectangleOnScreen(child, rect, immediate);
    }

    public int getScrollOffset() {
        FocusGridLayoutManager layoutManager = (FocusGridLayoutManager)getLayoutManager();
        int defaultSpanCount = layoutManager.getDefaultSpanCount();
        boolean isVertical = defaultSpanCount ==FilterListActivity.VERTICAL_SPAN_COUNT;
        int mTitleHeight = 0;
        int spaceV = 0;
        if(isVertical){
            spaceV = getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_vs);
            mTitleHeight = getResources().getDimensionPixelOffset(R.dimen.list_section_vertical_title_h) + spaceV;
        }else{
            spaceV = getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_vs);
            mTitleHeight = getResources().getDimensionPixelOffset(R.dimen.list_section_horizontal_title_h) + spaceV;
        }
        int scrollOffset = mTitleHeight + spaceV/2;
        return scrollOffset;
    }

    public int getSpaceV() {
        FocusGridLayoutManager layoutManager = (FocusGridLayoutManager)getLayoutManager();
        int defaultSpanCount = layoutManager.getDefaultSpanCount();
        boolean isVertical = defaultSpanCount ==FilterListActivity.VERTICAL_SPAN_COUNT;
        int spaceV = 0;
        if(isVertical){
            spaceV = getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_vs);
        }else{
            spaceV = getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_vs);
        }
        return spaceV;
    }

    public boolean isVertical(){
        boolean isVertical = false;
        if(getLayoutManager() != null && getLayoutManager() instanceof  FocusGridLayoutManager) {
           int defaultSpanCount =  ((FocusGridLayoutManager) getLayoutManager()).getDefaultSpanCount();
            isVertical = defaultSpanCount == FilterListActivity.VERTICAL_SPAN_COUNT;
        }
        return isVertical;
    }

    public void setBlockFocusScrollWhenManualScroll(boolean value){
        this.blockFocusScrollWhenManualScroll = value;
    }

    public void setScrollListener(OnRecyclerScrollListener mScrollListener){
        this.mScrollListener = mScrollListener;
    }
}
