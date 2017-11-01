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

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/15
 * @DESC: 说明
 */

public class RecommendAdapter extends BaseRecycleAdapter<RecommendAdapter.RecommendViewHolder>{

    private Context mContext;
    private List<BannerPoster> mData;

    public RecommendAdapter(Context context, List<BannerPoster> data){
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
        BannerPoster poster = mData.get(position);
        holder.mPosition=position;
        holder.mTitle.setText(poster.title);
        if (!TextUtils.isEmpty(poster.poster_url)) {
            Picasso.with(mContext).load(poster.poster_url).into(holder.mPoster);
        } else {
            Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mPoster);
        }
        Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_right_corner)).into(holder.mRtIconTv);
		/*add by dragontec for bug 4325 start*/
        String focusStr = poster.title;
        if(poster.focus != null && !poster.focus.equals("") && !poster.focus.equals("null")){
            focusStr = poster.focus;
        }
        holder.mTitle.setTag(new String[]{poster.title,focusStr});
		/*add by dragontec for bug 4325 end*/
    }

    @Override
    public int getItemCount() {
        return (mData!=null)? mData.size() : 0;
    }

    public class RecommendViewHolder extends BaseViewHolder{

        public TextView mTitle;
        public ImageView mPoster;
        public ImageView mRtIconTv;

        public RecommendViewHolder(View itemView) {
            super(itemView, RecommendAdapter.this);
            mTitle = (TextView) itemView.findViewById(R.id.recommend_item_tv);
            mPoster = (ImageView) itemView.findViewById(R.id.recommend_item_poster);
            mRtIconTv= (ImageView) itemView.findViewById(R.id.guide_rt_icon);
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.recommend_ismartv_linear_layout;
        }
		/*add by dragontec for bug 4325 start*/
        @Override
        protected int getTitleId() {
            return R.id.recommend_item_tv;
        }
		/*add by dragontec for bug 4325 end*/
    }
}
