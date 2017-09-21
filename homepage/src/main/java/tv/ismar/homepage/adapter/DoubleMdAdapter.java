package tv.ismar.homepage.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 竖版双行适配器
 */

public class DoubleMdAdapter extends BaseRecycleAdapter<DoubleMdAdapter.DoubleMdViewHolder>{

    public static final int TYPE_HEADER = 0;//头部
    public static final int TYPE_NORMAL = 1;//一般item

    private Context mContext;
    private List<BannerPoster> mData;

    private View mHeaderView;

    public DoubleMdAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null) return TYPE_NORMAL;
        if (position == 0) return TYPE_HEADER;
        return TYPE_NORMAL;
    }

    @Override
    public DoubleMdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER)
            return new DoubleMdViewHolder(mHeaderView);
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_double_md_item,parent,false);
        return new DoubleMdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DoubleMdViewHolder holder, int position) {
        if(position != 0){
            holder.mPosition = position;
            BannerPoster poster = mData.get(position-1);
            holder.mTitleTv.setText(poster.title);
            if (!TextUtils.isEmpty(poster.poster_url)) {
                Picasso.with(mContext).load(poster.poster_url).into(holder.mPosterIg);
            } else {
                Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mPosterIg);
            }
        }
    }

    @Override
    public int getItemCount() {
        if(mData == null) return 0;
        return (mHeaderView==null) ? mData.size() : mData.size() + 1;
    }

    public class DoubleMdViewHolder extends BaseViewHolder {
        public ImageView mPosterIg;//海报
        public ImageView mLtIconTv;//左上icon
        public ImageView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public int mPosition;

        public DoubleMdViewHolder(View itemView) {
            super(itemView, DoubleMdAdapter.this);
            mPosterIg = (ImageView) itemView.findViewById(R.id.double_md_item_poster);
            mLtIconTv = (ImageView) itemView.findViewById(R.id.double_md_item_lt_icon);
            mRbIconTv = (ImageView) itemView.findViewById(R.id.double_md_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.double_md_item_title);
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.double_md_ismartv_linear_layout;
        }
    }

}
