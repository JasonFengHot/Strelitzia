package tv.ismar.homepage.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.ViewGroup;

import tv.ismar.homepage.OnItemHoverListener;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.OnItemKeyListener;
import tv.ismar.homepage.OnItemSelectedListener;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/15
 * @DESC: 适配器基类
 */

public abstract class BaseRecycleAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{
	/*add by dragontec for bug 4316 start*/
	public static final int TYPE_HEADER = 0;//头部
	public static final int TYPE_NORMAL = 1;//一般item
	public final String TAG = this.getClass().getSimpleName();
	/*add by dragontec for bug 4316 end*/

    public OnItemClickListener mClickListener = null;
    public OnItemSelectedListener mSelectedListener = null;
    public OnItemHoverListener mHoverListener = null;
    public OnItemKeyListener mKeyListener = null;

	/*add by dragontec for bug 4265 start*/
    public RecyclerView mRecyclerView = null;
	/*add by dragontec for bug 4265 end*/
	public boolean isParentScrolling = false;

	public abstract void clearData();

	private RecyclerView.OnScrollListener mOnScrollListener;

	protected int mScrollState = RecyclerView.SCROLL_STATE_IDLE;


    @Override
    public void onViewAttachedToWindow(VH holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null
                && lp instanceof StaggeredGridLayoutManager.LayoutParams
                && getItemViewType(holder.getAdapterPosition()) == TYPE_HEADER) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);
        }
    }

	/*add by dragontec for bug 4265 start*/
	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		mRecyclerView = recyclerView;
		mOnScrollListener = new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				mScrollState = newState;
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
			}
		};
		mRecyclerView.addOnScrollListener(mOnScrollListener);
	}

	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
    	if (mRecyclerView == recyclerView) {
    		if (mOnScrollListener != null) {
    			mRecyclerView.removeOnScrollListener(mOnScrollListener);
				mOnScrollListener = null;
			}
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

	/*modify by dragontec for bug 4362 start*/
    public void setOnItemHoverListener(OnItemHoverListener listener){
        this.mHoverListener = listener;
    }
    /*modify by dragontec for bug 4362 end*/

    public void setOnItemKeyListener(OnItemKeyListener listener) {
    	this.mKeyListener = listener;
	}
}
