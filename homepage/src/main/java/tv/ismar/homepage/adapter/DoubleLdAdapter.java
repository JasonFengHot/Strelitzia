package tv.ismar.homepage.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.homepage.OnItemSelectedListener;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 横版双行适配器
 */

public class DoubleLdAdapter extends RecyclerView.Adapter<DoubleLdAdapter.DoubleLdViewHolder>{

    private Context mContext;
    private List<BannerPoster> mData;
    private OnItemSelectedListener mClickListener = null;
    private View mHeaderView;

    public DoubleLdAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener){
        this.mClickListener = listener;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    @Override
    public DoubleLdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER)
            return new DoubleLdAdapter.DoubleLdViewHolder(mHeaderView, mClickListener);
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_double_ld_item,parent,false);
        return new DoubleLdAdapter.DoubleLdViewHolder(view, mClickListener);
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
            BannerPoster poster = mData.get(position-1);
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

    public class DoubleLdViewHolder extends BaseViewHolder{
        public ImageView mPosterIg;//海报
        public ImageView mLtIconTv;//左上icon
        public ImageView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public int mPosition;//item位置

        public DoubleLdViewHolder(View itemView, OnItemSelectedListener listener) {
            super(itemView, listener);
            mPosterIg = (ImageView) itemView.findViewById(R.id.double_ld_item_poster);
            mLtIconTv = (ImageView) itemView.findViewById(R.id.double_ld_item_lt_icon);
            mRbIconTv = (ImageView) itemView.findViewById(R.id.double_ld_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.double_ld_item_title);
            itemView.findViewById(R.id.double_ld_ismartv_linear_layout).setOnClickListener(this);
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.double_ld_ismartv_linear_layout;
        }
    }
}
