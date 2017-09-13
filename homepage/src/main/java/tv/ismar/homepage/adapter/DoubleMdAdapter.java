package tv.ismar.homepage.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
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
import tv.ismar.homepage.R;

import static android.view.View.SCALE_X;
import static android.view.View.SCALE_Y;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 竖版双行适配器
 */

public class DoubleMdAdapter extends RecyclerView.Adapter<DoubleMdAdapter.DoubleMdViewHolder>{

    private Context mContext;
    private List<BannerPoster> mData;

    private boolean mTopMarginEnable = false;
    private boolean mLeftMarginEnable = false;

    public DoubleMdAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

    public void setTopMarginEnable(boolean enable){
        this.mTopMarginEnable = enable;
    }

    public void setLeftMarginEnable(boolean enable){
        this.mLeftMarginEnable = enable;
    }

    @Override
    public DoubleMdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_double_md_item,parent,false);
        return new DoubleMdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DoubleMdViewHolder holder, int position) {
        holder.mTopView.setVisibility(View.GONE);
        holder.mLeftView.setVisibility(View.GONE);
        holder.mTopView.setVisibility(mTopMarginEnable ? View.VISIBLE: View.GONE);
        holder.mLeftView.setVisibility(mLeftMarginEnable ? View.VISIBLE: View.GONE);
        BannerPoster poster = mData.get(position);
        holder.mTitleTv.setText(poster.title);
        if (!TextUtils.isEmpty(poster.poster_url)) {
            Picasso.with(mContext).load(poster.poster_url).into(holder.mPosterIg);
        } else {
            Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mPosterIg);
        }
    }

    @Override
    public int getItemCount() {
        return (mData!=null)? mData.size():0;
    }

    public static class DoubleMdViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener,
            View.OnClickListener{
        public ImageView mPosterIg;//海报
        public ImageView mLtIconTv;//左上icon
        public ImageView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public View mTopView;//上边距
        public View mLeftView;//左边距

        public DoubleMdViewHolder(View itemView) {
            super(itemView);
            mPosterIg = (ImageView) itemView.findViewById(R.id.double_md_item_poster);
            mLtIconTv = (ImageView) itemView.findViewById(R.id.double_md_item_lt_icon);
            mRbIconTv = (ImageView) itemView.findViewById(R.id.double_md_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.double_md_item_title);
            mTopView = itemView.findViewById(R.id.double_md_top_margin);
            mLeftView = itemView.findViewById(R.id.double_md_left_margin);
            itemView.findViewById(R.id.double_ld_ismartv_linear_layout).setOnFocusChangeListener(this);
            itemView.findViewById(R.id.double_ld_ismartv_linear_layout).setOnClickListener(this);
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
                scaleToLarge(v.findViewById(R.id.double_md_ismartv_linear_layout));
            } else {
                scaleToNormal(v.findViewById(R.id.double_md_ismartv_linear_layout));
            }
        }

        @Override
        public void onClick(View v) {
//            scaleToLarge(v.findViewById(R.id.double_md_ismartv_linear_layout));
        }
    }

}
