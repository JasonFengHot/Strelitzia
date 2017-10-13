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
 * @DATE: 2017/8/31
 * @DESC: 横版双行适配器
 */

public class DoubleLdAdapter extends BaseRecycleAdapter<DoubleLdAdapter.DoubleLdViewHolder>{

    private Context mContext;
    private List<BannerPoster> mData;
    private View mHeaderView;

    public DoubleLdAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    @Override
    public DoubleLdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER)
            return new DoubleLdViewHolder(mHeaderView);
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_double_ld_item,parent,false);
        return new DoubleLdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DoubleLdViewHolder holder, int position) {
        if(position != 0){
            holder.mPosition = position;
            BannerPoster poster = mData.get(position-1);
            if (!TextUtils.isEmpty(poster.poster_url)) {
                if(poster.poster_url.equals("更多")){
                    Picasso.with(mContext).load(R.drawable.banner_horizontal_more).into(holder.mPosterIg);
                } else {
                    Picasso.with(mContext).load(poster.poster_url).into(holder.mPosterIg);
                }
            } else {
                Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mPosterIg);
            }
            Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_left_corner)).into(holder.mLtIconTv);
            holder.mRbIconTv.setText(new DecimalFormat("0.0").format(poster.rating_average));
            holder.mRbIconTv.setVisibility((poster.rating_average==0) ? View.GONE:View.VISIBLE);
            if(poster.poster_url.equals("更多")){
                holder.mTitleTv.setVisibility(View.INVISIBLE);
            } else {
                holder.mTitleTv.setVisibility(View.VISIBLE);
            }
            holder.mTitleTv.setText(poster.title);
        }
    }

    @Override
    public int getItemCount() {
        if(mData == null) return 0;
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
        public TextView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public int mPosition;//item位置

        public DoubleLdViewHolder(View itemView) {
            super(itemView, DoubleLdAdapter.this);
            mPosterIg = (ImageView) itemView.findViewById(R.id.double_ld_item_poster);
            mLtIconTv = (ImageView) itemView.findViewById(R.id.double_ld_item_lt_icon);
            mRbIconTv = (TextView) itemView.findViewById(R.id.double_ld_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.double_ld_item_title);
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.double_ld_ismartv_linear_layout;
        }
    }
}
