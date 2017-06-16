package tv.ismar.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;


/**
 * Created by Administrator on 2017-06-14.
 */

public class FocusGridLayoutManager extends GridLayoutManager {

    private  int mSpanCount;

    public FocusGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
        mSpanCount=spanCount;
    }

    @Override
    public View onInterceptFocusSearch(View focused, int direction) {
        if (direction == View.FOCUS_DOWN) {
            int pos = getPosition(focused);
            int nextPos = pos+mSpanCount;
            int size = getChildCount();
            int count = getItemCount();
            if (size > 0) {
                int startIndex = 0;
                if (size >= mSpanCount) {
                    startIndex = size - mSpanCount;
                }
                View view;
                for (int i = startIndex; i < size; i++) {
                    view = getChildAt(i);
                    if (view == focused) {
                        int lastVisibleItemPos = findLastCompletelyVisibleItemPosition();
                        if (pos > lastVisibleItemPos) { //lastVisibleItemPos==-1 ||
                            return focused;
                        } else {
                            int lastLineStartIndex = 0;
                            if (count >= mSpanCount) {
                                lastLineStartIndex = count - mSpanCount;
                            }
                            if (pos >= lastLineStartIndex && pos < count) { //最后一排的可见view时,返回当前view
                                return focused;
                            }
                            break;
                        }
                    }
                }
            } else {
                return focused;
            }
        }else if(direction==View.FOCUS_UP){

        }
        return super .onInterceptFocusSearch(focused, direction);
    }
}
