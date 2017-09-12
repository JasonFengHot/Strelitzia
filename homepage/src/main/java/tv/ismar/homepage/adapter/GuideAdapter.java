package tv.ismar.homepage.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
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
import tv.ismar.homepage.banner.IsmartvLinearLayout;
import tv.ismar.homepage.widget.HomeItemContainer;

import static android.view.View.SCALE_X;
import static android.view.View.SCALE_Y;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/30
 * @DESC: 导视recycleview适配器
 */

public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.GuideViewHolder>{
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
        BannerPoster poster = mData.get(position);
        Picasso.with(mContext).load(poster.poster_url).into(holder.mPosterIg);
//        Picasso.with(mContext).load(posters.poster_url).into(holder.mLtIconTv);
//        Picasso.with(mContext).load(posters.poster_url).into(holder.mRbIconTv);
        holder.mTitleTv.setText(poster.title);
    }

    @Override
    public int getItemCount() {
        return (mData!=null) ? mData.size():0;
    }

    public void setMarginLeftEnable(boolean enable){
        this.mMarginLeftEnable = enable;
    }

    public static class GuideViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener{
        public ImageView mPosterIg;//海报
        public ImageView mLtIconTv;//左上icon
        public ImageView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public View mMarginLeftView;//左边距

        public GuideViewHolder(View itemView) {
            super(itemView);
            mPosterIg = (ImageView) itemView.findViewById(R.id.guide_recycle_item_poster);
            mLtIconTv = (ImageView) itemView.findViewById(R.id.guide_recycle_item_lt_icon);
            mRbIconTv = (ImageView) itemView.findViewById(R.id.guide_recycle_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.guide_recycle_item_title);
            mMarginLeftView = itemView.findViewById(R.id.guide_margin_left);
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
