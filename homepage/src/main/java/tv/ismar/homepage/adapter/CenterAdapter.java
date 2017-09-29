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
 * @DATE: 2017/8/29
 * @DESC: 居中RecyclerView适配器
 */

public class CenterAdapter extends BaseRecycleAdapter<CenterAdapter.CenterViewHolder> {

    private Context mContext;
    private List<BannerPoster> mData;

    public CenterAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public CenterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_center_item,parent,false);
        return new CenterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CenterViewHolder holder, int position) {
        BannerPoster poster = mData.get(position);
        holder.mTitle.setText(poster.title);
        if (!TextUtils.isEmpty(poster.poster_url)) {
            if(poster.poster_url.equals("更多")){
                Picasso.with(mContext).load(R.drawable.banner_vertical_more).into(holder.mPosterIg);
            } else {
                Picasso.with(mContext).load(poster.poster_url).into(holder.mPosterIg);
            }
            Picasso.with(mContext).load(poster.poster_url).into(holder.mPosterIg);
        } else {
            Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mPosterIg);
        }
    }

    @Override
    public int getItemCount() {
        return (mData!=null)? mData.size() : 0;
    }

    public class CenterViewHolder extends BaseViewHolder{
        public TextView mTitle;
        public ImageView mPosterIg;

        public CenterViewHolder(View itemView) {
            super(itemView, CenterAdapter.this);
            mTitle = (TextView) itemView.findViewById(R.id.center_item_title);
            mPosterIg = (ImageView) itemView.findViewById(R.id.center_item_poster);
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.center_ismartv_linear_layout;
        }
    }
}
