package tv.ismar.homepage.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.ViewGroup;

import tv.ismar.homepage.OnItemHoverListener;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.OnItemSelectedListener;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/15
 * @DESC: 适配器基类
 */

public abstract class BaseRecycleAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{
    public OnItemClickListener mClickListener = null;
    public OnItemSelectedListener mSelectedListener = null;
    public OnItemHoverListener mHoverListener = null;

	/*add by dragontec for bug 4265 start*/
    public RecyclerView mRecyclerView = null;
	/*add by dragontec for bug 4265 end*/

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

	/*add by dragontec for bug 4265 start*/
	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		mRecyclerView = recyclerView;
	}

	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
    	if (mRecyclerView == recyclerView) {
			mRecyclerView = null;
		}
		super.onDetachedFromRecyclerView(recyclerView);
	}
	/*add by dragontec for bug 4265 end*/

	public void setOnItemClickListener(OnItemClickListener listener){
        this.mClickListener = listener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener){
        this.mSelectedListener = listener;
    }

    public void setOnHoverListener(OnItemHoverListener listener){
        this.mHoverListener = listener;
    }
}
