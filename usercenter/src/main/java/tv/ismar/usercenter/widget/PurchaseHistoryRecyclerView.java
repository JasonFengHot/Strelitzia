package tv.ismar.usercenter.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.squareup.picasso.Picasso;

import tv.ismar.usercenter.R;
import tv.ismar.usercenter.adapter.PurchaseHistoryListAdapter;
import tv.ismar.usercenter.view.RelativeLayoutContainer;

/**
 * Created by liujy on 2017/11/16.
 */

public class PurchaseHistoryRecyclerView extends RecyclerView {
	public PurchaseHistoryRecyclerView(Context context) {
		super(context);
	}

	public PurchaseHistoryRecyclerView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public PurchaseHistoryRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private int getOffSet(View child) {
		int position = getChildAdapterPosition(child);
		if (position == 0 || position == getLayoutManager().getItemCount() -1) {
			return 0;
		} else {
			return (int) (getResources().getDimensionPixelSize(R.dimen.purchase_history_recycler_view_padding) / getResources().getDisplayMetrics().density);
		}
	}

	@Override
	public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
		if (isHovered()) {
			return true;
		}
		final int parentLeft = getPaddingLeft();
		final int parentTop = getPaddingTop() + getOffSet(child);
		final int parentRight = getWidth() - getPaddingRight();
		final int parentBottom = getHeight() - getPaddingBottom() - getOffSet(child);
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

	private long mLastPressDownTime = 0;

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (mScrollState != SCROLL_STATE_IDLE) {
			return true;
		}
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			long curTime = System.currentTimeMillis();
			if (curTime - mLastPressDownTime < 300) {
				return true;
			}
			mLastPressDownTime = curTime;
		}
		return super.dispatchKeyEvent(event);
	}

	int mScrollState = SCROLL_STATE_IDLE;

	@Override
	public void onScrollStateChanged(int state) {
		if (mScrollState != state) {
			if (state == SCROLL_STATE_IDLE) {
				Picasso.with(getContext()).resumeTag(PurchaseHistoryListAdapter.TAG);
			} else {
				Picasso.with(getContext()).pauseTag(PurchaseHistoryListAdapter.TAG);
			}
			mScrollState = state;
		}
		super.onScrollStateChanged(state);
	}
}
