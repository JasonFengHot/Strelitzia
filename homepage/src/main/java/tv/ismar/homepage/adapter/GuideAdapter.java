package tv.ismar.homepage.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
 * @DATE: 2017/8/30
 * @DESC: 导视recycleview适配器
 */

public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.GuideViewHolder>{
    public static final int TYPE_HEADER = 0;//头部
    public static final int TYPE_NORMAL = 1;//一般item

    private Context mContext;
    private List<BannerPoster> mData;
    private boolean mMarginLeftEnable = false;
    private OnItemSelectedListener mClickListener = null;

    private View mHeaderView;

    public GuideAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener){
        this.mClickListener = listener;
    }

    @Override
    public GuideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER)
            return new GuideViewHolder(mHeaderView);
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_guide_item,parent,false);
        return new GuideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GuideViewHolder holder, int position) {
        if(position != 0){
            holder.mMarginLeftView.setVisibility(mMarginLeftEnable?View.VISIBLE:View.GONE);
            BannerPoster poster = mData.get(position);
            if (!TextUtils.isEmpty(poster.poster_url)) {
                Picasso.with(mContext).load(poster.poster_url).into(holder.mPosterIg);
            } else {
                Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mPosterIg);
            }
//        Picasso.with(mContext).load(posters.poster_url).into(holder.mLtIconTv);
//        Picasso.with(mContext).load(posters.poster_url).into(holder.mRbIconTv);

            holder.mTitleTv.setText(poster.title);
            holder.mPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return (mHeaderView==null) ? mData.size() : mData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null) return TYPE_NORMAL;
        if (position == 0) return TYPE_HEADER;
        return TYPE_NORMAL;
    }

    public void setMarginLeftEnable(boolean enable){
        this.mMarginLeftEnable = enable;
    }

    public class GuideViewHolder extends BaseViewHolder {
        public ImageView mPosterIg;//海报
        public ImageView mLtIconTv;//左上icon
        public ImageView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public View mMarginLeftView;//左边距

        public int mPosition;//item位置

        public GuideViewHolder(View itemView) {
            super(itemView);
            mPosterIg = (ImageView) itemView.findViewById(R.id.guide_recycle_item_poster);
            mLtIconTv = (ImageView) itemView.findViewById(R.id.guide_recycle_item_lt_icon);
            mRbIconTv = (ImageView) itemView.findViewById(R.id.guide_recycle_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.guide_recycle_item_title);
            mMarginLeftView = itemView.findViewById(R.id.guide_margin_left);
            itemView.findViewById(R.id.guide_ismartv_linear_layout).setOnClickListener(this);
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.guide_ismartv_linear_layout;
        }

        @Override
        public void onClick(View v) {//item选中
            mClickListener.itemSelected(v, mPosition);
        }
    }
}
