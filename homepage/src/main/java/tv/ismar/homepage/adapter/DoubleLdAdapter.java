package tv.ismar.homepage.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.homepage.OnItemSelectedListener;
import tv.ismar.homepage.R;

import static android.view.View.SCALE_X;
import static android.view.View.SCALE_Y;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 横版双行适配器
 */

public class DoubleLdAdapter extends RecyclerView.Adapter<DoubleLdAdapter.DoubleLdViewHolder>{

    private Context mContext;
    private List<BannerPoster> mData;
    private boolean mTopMarginEnable = false;
    private boolean mLeftMarginEnable = false;
    private OnItemSelectedListener mClickListener = null;
    private View mHeaderView;

    public DoubleLdAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener){
        this.mClickListener = listener;
    }

    public void setTopMarginEnable(boolean enable){
        this.mTopMarginEnable = enable;
    }

    public void setLeftMarginEnable(boolean enable){
        this.mLeftMarginEnable = enable;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    @Override
    public DoubleLdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER)
            return new DoubleLdAdapter.DoubleLdViewHolder(mHeaderView);
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_double_ld_item,parent,false);
        return new DoubleLdAdapter.DoubleLdViewHolder(view);
    }

    @Override
    public void onViewAttachedToWindow(DoubleLdViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null
                && lp instanceof StaggeredGridLayoutManager.LayoutParams
                && holder.getLayoutPosition() == 0) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);
        }
    }

    @Override
    public void onBindViewHolder(DoubleLdViewHolder holder, int position) {
        if(position != 0){
            holder.mPosition = position;
            holder.mTopMarginView.setVisibility(View.GONE);
            holder.mLeftMarginView.setVisibility(View.GONE);
            holder.mTopMarginView.setVisibility(mTopMarginEnable ? View.VISIBLE: View.GONE);
            holder.mLeftMarginView.setVisibility(mLeftMarginEnable ? View.VISIBLE: View.GONE);
            BannerPoster poster = mData.get(position);
            if (!TextUtils.isEmpty(poster.poster_url)) {
                Picasso.with(mContext).load(poster.poster_url).into(holder.mPosterIg);
            } else {
                Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mPosterIg);
            }
//        Picasso.with(mContext).load(posters.poster_url).into(holder.mLtIconTv);
//        Picasso.with(mContext).load(posters.poster_url).into(holder.mRbIconTv);
            holder.mTitleTv.setText(poster.title);
        }
    }

    @Override
    public int getItemCount() {
        return (mHeaderView==null) ? mData.size() : mData.size() + 1;
    }

    public static final int TYPE_HEADER = 0;//头部
    public static final int TYPE_NORMAL = 1;//一般item

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null) return TYPE_NORMAL;
        if (position == 0) return TYPE_HEADER;
        return TYPE_NORMAL;
    }

    public class DoubleLdViewHolder extends RecyclerView.ViewHolder implements
            View.OnFocusChangeListener, View.OnClickListener{
        public ImageView mPosterIg;//海报
        public ImageView mLtIconTv;//左上icon
        public ImageView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public View mTopMarginView;//上边距
        public View mLeftMarginView;//左边距
        public int mPosition;//item位置

        public DoubleLdViewHolder(View itemView) {
            super(itemView);
            mPosterIg = (ImageView) itemView.findViewById(R.id.double_ld_item_poster);
            mLtIconTv = (ImageView) itemView.findViewById(R.id.double_ld_item_lt_icon);
            mRbIconTv = (ImageView) itemView.findViewById(R.id.double_ld_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.double_ld_item_title);
            mTopMarginView = itemView.findViewById(R.id.double_ld_top_margin);
            mLeftMarginView = itemView.findViewById(R.id.double_ld_left_margin);
            itemView.findViewById(R.id.double_ld_ismartv_linear_layout).setOnFocusChangeListener(this);
            itemView.findViewById(R.id.double_ld_ismartv_linear_layout).setOnClickListener(this);
        }

        private void scaleToLarge(View view) {
            ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(view, SCALE_X, 1.0F, 1.1F);
            objectAnimatorX.setDuration(100L);
            objectAnimatorX.start();
            ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(view, SCALE_Y, 1.0F, 1.1F);
            objectAnimatorY.setDuration(100L);
            objectAnimatorY.start();
        }


        private void scaleToNormal(View view) {
            ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(view, SCALE_X, 1.1F, 1.0F);
            objectAnimatorX.setDuration(100L);
            objectAnimatorX.start();
            ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(view, SCALE_Y, 1.1F, 1.0F);
            objectAnimatorY.setDuration(100L);
            objectAnimatorY.start();
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                scaleToLarge(v.findViewById(R.id.double_ld_ismartv_linear_layout));
                mClickListener.itemSelected(v, mPosition);
            } else {
                scaleToNormal(v.findViewById(R.id.double_ld_ismartv_linear_layout));
            }
        }

        @Override
        public void onClick(View v) {
            mClickListener.itemSelected(v, mPosition);
        }
    }
}
