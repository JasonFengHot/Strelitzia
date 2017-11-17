package tv.ismar.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.squareup.picasso.Picasso;

import tv.ismar.adapter.HistoryFavoriteListAdapter;
import tv.ismar.app.widget.MyRecyclerView;

/**
 * Created by liucan on 2017/8/25.
 */

public class HistoryFavoriteRecyclerView extends MyRecyclerView {
    public HistoryFavoriteRecyclerView(Context context) {
        super(context);
        init();
    }

    public HistoryFavoriteRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HistoryFavoriteRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        addOnScrollListener(new CustomOnScrollListener());
    }


    private class CustomOnScrollListener extends OnScrollListener{
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState){
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
    }
}
