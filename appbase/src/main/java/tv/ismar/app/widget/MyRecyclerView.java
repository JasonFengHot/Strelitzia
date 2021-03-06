package tv.ismar.app.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Created by admin on 2017/4/11.
 */

public class MyRecyclerView extends RecyclerView {
/*add by dragontec for bug 4310 start*/
	private int mScrollState = SCROLL_STATE_IDLE;
/*add by dragontec for bug 4310 end*/

    public MyRecyclerView(Context context) {
        this(context,null);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction()==MotionEvent.ACTION_MOVE) {
            return true;
        }else{
            return super.dispatchTouchEvent(ev);
        }
    }

/*add by dragontec for bug 4310 start*/
	@Override
	public void onScrollStateChanged(int state) {
		mScrollState = state;
		super.onScrollStateChanged(state);
	}

	public boolean isScrolling() {
    	return mScrollState != SCROLL_STATE_IDLE;
	}
/*add by dragontec for bug 4310 end*/

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		setHovered(false);
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected boolean dispatchHoverEvent(MotionEvent event) {
		setHovered(true);
		return super.dispatchHoverEvent(event);
	}

	@Override
	public void setHovered(boolean hovered) {
		super.setHovered(hovered);
		if (mOnHoverStateChangedListener != null) {
			mOnHoverStateChangedListener.onHoverStateChanged(hovered);
		}
	}

	public interface OnHoverStateChangedListener {
		void onHoverStateChanged(boolean hovered);
	}

	private OnHoverStateChangedListener mOnHoverStateChangedListener = null;

	public void setOnHoverStateChangedListener(OnHoverStateChangedListener onHoverStateChangedListener) {
		mOnHoverStateChangedListener = onHoverStateChangedListener;
	}
}
