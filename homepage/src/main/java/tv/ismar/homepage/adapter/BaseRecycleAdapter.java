package tv.ismar.homepage.adapter;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import tv.ismar.homepage.OnItemSelectedListener;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/15
 * @DESC: 适配器基类
 */

public abstract class BaseRecycleAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{
    public OnItemSelectedListener mClickListener = null;

    @Override
    public void onViewAttachedToWindow(VH holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null
                && lp instanceof StaggeredGridLayoutManager.LayoutParams
                && holder.getLayoutPosition() == 0) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener){
        this.mClickListener = listener;
    }
}
