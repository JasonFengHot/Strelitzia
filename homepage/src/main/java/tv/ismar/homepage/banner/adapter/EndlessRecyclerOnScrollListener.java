package tv.ismar.homepage.banner.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created by mac on 15/10/28.
 */
public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private int current_page = 1;

    private LinearLayoutManager mLinearLayoutManager;
    private StaggeredGridLayoutManager mGridLayoutManager;

    private boolean isOnTouchToScroll = false;

    public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    public EndlessRecyclerOnScrollListener(StaggeredGridLayoutManager mGridLayoutManager) {
        visibleThreshold = 10;
        this.mGridLayoutManager = mGridLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (!isOnTouchToScroll) {
            return;
        }
        visibleItemCount = recyclerView.getChildCount();

        if (mLinearLayoutManager != null) {
            totalItemCount = mLinearLayoutManager.getItemCount();
            firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
        } else if (mGridLayoutManager != null) {
            totalItemCount = mGridLayoutManager.getItemCount();
            firstVisibleItem = findFirst(mGridLayoutManager.findFirstVisibleItemPositions(null));
        } else {
            throw new IllegalArgumentException("LayoutManager is null.");
        }

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached

            // Do something
            current_page++;

            onLoadMore(current_page);

            loading = true;
        }
    }

    private int findFirst(int[] firstPositions) {
        int min = firstPositions[0];
        for (int value : firstPositions) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        isOnTouchToScroll = true;
    }

    public abstract void onLoadMore(int current_page);
}
