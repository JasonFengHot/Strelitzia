package tv.ismar.homepage.adapter;

import android.animation.ObjectAnimator;
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

import static android.view.View.SCALE_X;
import static android.view.View.SCALE_Y;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 电视剧适配器
 */

public class TvPlayAdapter extends BaseRecycleAdapter<TvPlayAdapter.TvPlayerViewHolder>{

    private Context mContext;
    private List<BannerPoster> mData;
    private boolean mMarginLeftEnable = false;

    public TvPlayAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public TvPlayAdapter.TvPlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_tv_player_item,parent,false);
        return new TvPlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TvPlayerViewHolder holder, int position) {
        holder.mMarginLeftView.setVisibility(mMarginLeftEnable? View.VISIBLE:View.GONE);
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

    @Override
    public int getItemCount() {
        return (mData!=null)? mData.size():0;
    }

    public void setMarginLeftEnable(boolean enable){
        this.mMarginLeftEnable = enable;
    }

    public class TvPlayerViewHolder extends BaseViewHolder{
        public ImageView mPosterIg;//海报
        public ImageView mLtIconTv;//左上icon
        public ImageView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public View mMarginLeftView;//左边距

        public TvPlayerViewHolder(View itemView) {
            super(itemView, TvPlayAdapter.this);
            mPosterIg = (ImageView) itemView.findViewById(R.id.tv_player_item_poster);
            mLtIconTv = (ImageView) itemView.findViewById(R.id.tv_player_item_lt_icon);
            mRbIconTv = (ImageView) itemView.findViewById(R.id.tv_player_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.tv_player_item_title);
            mMarginLeftView = itemView.findViewById(R.id.tv_player_margin_left);
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.tv_player_ismartv_linear_layout;
        }
    }
}
