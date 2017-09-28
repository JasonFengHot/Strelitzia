package tv.ismar.homepage.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/30
 * @DESC: 导视recycleview适配器
 */

public class GuideAdapter extends BaseRecycleAdapter<GuideAdapter.GuideViewHolder>{
    private Context mContext;
    private List<BannerPoster> mData;
    private boolean mMarginLeftEnable = false;

    public GuideAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public GuideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_guide_item,parent,false);
        return new GuideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GuideViewHolder holder, int position) {
        holder.mMarginLeftView.setVisibility(mMarginLeftEnable?View.VISIBLE:View.GONE);
        if(position==0) holder.mMarginLeftView.setVisibility(View.GONE);
        BannerPoster poster = mData.get(position);
        if (!TextUtils.isEmpty(poster.poster_url)) {
            Picasso.with(mContext).load(poster.poster_url).into(holder.mPosterIg);
        } else {
            Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mPosterIg);
        }
        Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_left_corner)).into(holder.mLtIconTv);
        holder.mRbIconTv.setText(new DecimalFormat("0.0").format(poster.rating_average));
        holder.mRbIconTv.setVisibility((poster.rating_average==0) ? View.GONE:View.VISIBLE);

        holder.mTitleTv.setText(poster.title);
        holder.mPosition = position;
    }

    @Override
    public int getItemCount() {
        return (mData!=null) ? mData.size() : 0;
    }

    public void setMarginLeftEnable(boolean enable){
        this.mMarginLeftEnable = enable;
    }

    public class GuideViewHolder extends BaseViewHolder {
        public ImageView mPosterIg;//海报
        public ImageView mLtIconTv;//左上icon
        public TextView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public View mMarginLeftView;//左边距

        public GuideViewHolder(View itemView) {
            super(itemView, GuideAdapter.this);
            mPosterIg = (ImageView) itemView.findViewById(R.id.guide_recycle_item_poster);
            mLtIconTv = (ImageView) itemView.findViewById(R.id.guide_recycle_item_lt_icon);
            mRbIconTv = (TextView) itemView.findViewById(R.id.guide_recycle_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.guide_recycle_item_title);
            mMarginLeftView = itemView.findViewById(R.id.guide_margin_left);
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.guide_ismartv_linear_layout;
        }
    }
}
