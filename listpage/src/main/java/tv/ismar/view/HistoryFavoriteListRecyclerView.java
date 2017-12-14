package tv.ismar.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.squareup.picasso.Picasso;

import tv.ismar.adapter.FocusGridLayoutManager;
import tv.ismar.adapter.HistoryFavoriteListAdapter;
import tv.ismar.app.widget.MyRecyclerView;
import tv.ismar.listpage.R;

/**
 * Created by liucan on 2017/8/25.
 */

public class HistoryFavoriteListRecyclerView extends MyRecyclerView {
	private final Object posLock = new Object();
	private int needFocusPos = -1;

	@Override
	public void onViewAdded(View child) {
		super.onViewAdded(child);
		synchronized (posLock) {
			if (needFocusPos != -1) {
				ViewHolder viewHolder = getChildViewHolder(child);
				if (viewHolder != null) {
					if (viewHolder.getAdapterPosition() == needFocusPos) {
						viewHolder.itemView.requestFocus();
					}
					needFocusPos = -1;
				}
			}
		}
	}

	@Override
	public void onChildAttachedToWindow(View child) {
		super.onChildAttachedToWindow(child);
		synchronized (posLock) {
			if (needFocusPos != -1) {
				ViewHolder viewHolder = getChildViewHolder(child);
				if (viewHolder != null) {
					if (viewHolder.getAdapterPosition() == needFocusPos) {
						viewHolder.itemView.requestFocus();
					}
					needFocusPos = -1;
				}
			}
		}
	}

	public HistoryFavoriteListRecyclerView(Context context) {
        this(context, null);
    }

    public HistoryFavoriteListRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public HistoryFavoriteListRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
    	if (event.getAction() == KeyEvent.ACTION_DOWN) {
    		if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && getChildCount() > 0) {
    			//fix preload error
				FocusGridLayoutManager layoutManager = (FocusGridLayoutManager) getLayoutManager();
				View currentFocus = getFocusedChild();
				View firstView = getChildAt(0);
				if (currentFocus == firstView && layoutManager.findFirstVisibleItemPosition() != 0) {
					synchronized (posLock) {
						needFocusPos = layoutManager.findFirstVisibleItemPosition() - 1;
						smoothScrollToPosition(needFocusPos);
					}
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        if (foreground != null) {
//            foreground.setBounds(0 - 19, 0 - 19, getMeasuredWidth() + 19, getMeasuredHeight() + 19);
//            invalidate();
//        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        if (foreground != null) {
//            foreground.setBounds(-19, -19, w + 19, h + 19);
//            invalidate();
//        }
    }

    private void init() {
    	setDrawingCacheEnabled(true);
    	setDrawingCacheQuality(DRAWING_CACHE_QUALITY_HIGH);
    }

	@Override
	public void onScrollStateChanged(int state) {
		super.onScrollStateChanged(state);
		switch (state){
			case SCROLL_STATE_IDLE:
				Picasso.with(getContext()).resumeTag(HistoryFavoriteListAdapter.PICASSO_TAG);
				break;
			case SCROLL_STATE_DRAGGING:
				Picasso.with(getContext()).pauseTag(HistoryFavoriteListAdapter.PICASSO_TAG);
				break;
			case SCROLL_STATE_SETTLING:
				Picasso.with(getContext()).pauseTag(HistoryFavoriteListAdapter.PICASSO_TAG);
				break;
			default:
				break;
		}
	}

	@Override
	public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
		final int parentLeft = getPaddingLeft();
		final int parentTop = (int) (getPaddingTop() - getResources().getDimensionPixelSize(R.dimen.history_list_recycler_margin) / getResources().getDisplayMetrics().density );
		final int parentRight = getWidth() - getPaddingRight();
		final int parentBottom = (int) (getHeight() - getPaddingBottom() + getResources().getDimensionPixelSize(R.dimen.history_list_recycler_margin) / getResources().getDisplayMetrics().density);
		final int childLeft = child.getLeft() + rect.left;
		final int childTop = child.getTop() + rect.top;
		final int childRight = childLeft + rect.width();
		final int childBottom = childTop + rect.height();

		final int offScreenLeft = Math.min(0, childLeft - parentLeft);
		final int offScreenTop = Math.min(0, childTop - parentTop);
		final int offScreenRight = Math.max(0, childRight - parentRight);
		final int offScreenBottom = Math.max(0, childBottom - parentBottom);

		// Favor the "start" layout direction over the end when bringing one side or the other
		// of a large rect into view. If we decide to bring in end because start is already
		// visible, limit the scroll such that start won't go out of bounds.
		final int dx;
		if (getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
			dx = offScreenRight != 0 ? offScreenRight
					: Math.max(offScreenLeft, childRight - parentRight);
		} else {
			dx = offScreenLeft != 0 ? offScreenLeft
					: Math.min(childLeft - parentLeft, offScreenRight);
		}

		// Favor bringing the top into view over the bottom. If top is already visible and
		// we should scroll to make bottom visible, make sure top does not go out of bounds.
		final int dy = offScreenTop != 0 ? offScreenTop
				: Math.min(childTop - parentTop, offScreenBottom);

		if (dx != 0 || dy != 0) {
			if (immediate) {
				scrollBy(dx, dy);
			} else {
				smoothScrollBy(dx, dy);
			}
			return true;
		}
		return false;
	}
}
