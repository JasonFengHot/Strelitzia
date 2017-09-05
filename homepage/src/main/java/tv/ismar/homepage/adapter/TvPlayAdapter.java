package tv.ismar.homepage.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.homepage.R;

import static android.view.View.SCALE_X;
import static android.view.View.SCALE_Y;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 电视剧适配器
 */

public class TvPlayAdapter extends RecyclerView.Adapter<TvPlayAdapter.TvPlayerViewHolder>{

    private Context mContext;
    private List<BannerPoster> mData;

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
        BannerPoster poster = mData.get(position);
        Picasso.with(mContext).load(poster.poster_url).into(holder.mPosterIg);
//        Picasso.with(mContext).load(poster.poster_url).into(holder.mLtIconTv);
//        Picasso.with(mContext).load(poster.poster_url).into(holder.mRbIconTv);
        holder.mTitleTv.setText(poster.title);
    }

    @Override
    public int getItemCount() {
        return (mData!=null)? mData.size():0;
    }

    public static class TvPlayerViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener{
        public ImageView mPosterIg;//海报
        public ImageView mLtIconTv;//左上icon
        public ImageView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题

        public TvPlayerViewHolder(View itemView) {
            super(itemView);
            mPosterIg = (ImageView) itemView.findViewById(R.id.tv_player_item_poster);
            mLtIconTv = (ImageView) itemView.findViewById(R.id.tv_player_item_lt_icon);
            mRbIconTv = (ImageView) itemView.findViewById(R.id.tv_player_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.tv_player_item_title);
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
                scaleToLarge(v.findViewById(R.id.guide_ismartv_linear_layout));
                v.findViewById(R.id.title).setSelected(true);
                v.findViewById(R.id.introduction).setSelected(true);
            } else {
                scaleToNormal(v.findViewById(R.id.guide_ismartv_linear_layout));
                v.findViewById(R.id.title).setSelected(false);
                v.findViewById(R.id.introduction).setSelected(false);
            }
        }
    }

}
