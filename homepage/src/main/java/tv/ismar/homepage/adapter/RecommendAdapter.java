package tv.ismar.homepage.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.entity.banner.BannerRecommend;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/15
 * @DESC: 说明
 */

public class RecommendAdapter extends BaseRecycleAdapter<RecommendAdapter.RecommendViewHolder>{

    private Context mContext;
    private List<BannerRecommend> mData;

    public RecommendAdapter(Context context, List<BannerRecommend> data){
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public RecommendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_recommend_item,parent,false);
        return new RecommendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecommendViewHolder holder, int position) {
        BannerRecommend poster = mData.get(position);
        holder.mTitle.setText(poster.title);
        if (!TextUtils.isEmpty(poster.poster_url)) {
            Picasso.with(mContext).load(poster.poster_url).into(holder.mPoster);
        } else {
            Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mPoster);
        }
    }

    @Override
    public int getItemCount() {
        return (mData!=null)? mData.size() : 0;
    }

    public class RecommendViewHolder extends BaseViewHolder{

        public TextView mTitle;
        public ImageView mPoster;

        public RecommendViewHolder(View itemView) {
            super(itemView, RecommendAdapter.this);
            mTitle = (TextView) itemView.findViewById(R.id.recommend_item_tv);
            mPoster = (ImageView) itemView.findViewById(R.id.recommend_item_poster);
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.recommend_ismartv_linear_layout;
        }
    }
}
