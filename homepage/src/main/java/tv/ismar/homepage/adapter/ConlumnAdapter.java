package tv.ismar.homepage.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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
 * @DATE: 2017/8/29
 * @DESC: 栏目RecyclerView适配器
 */

public class ConlumnAdapter extends RecyclerView.Adapter<ConlumnAdapter.ConlumnViewHolder> {

    private Context mContext;
    private List<BannerPoster> mData;

    public ConlumnAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public ConlumnViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_conlumn_item,parent,false);
        return new ConlumnViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConlumnViewHolder holder, int position) {
        BannerPoster poster = mData.get(position);
        Log.d("ConlumnAdapter", "position:"+position);
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

    public static class ConlumnViewHolder extends RecyclerView.ViewHolder implements
            View.OnFocusChangeListener, View.OnClickListener{
        public TextView mTitle;
        public ImageView mPoster;

        public ConlumnViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.conlumn_item_tv);
            mPoster = (ImageView) itemView.findViewById(R.id.conlumn_item_poster);
            itemView.findViewById(R.id.conlumn_ismartv_linear_layout).setOnFocusChangeListener(this);
            itemView.findViewById(R.id.conlumn_ismartv_linear_layout).setOnClickListener(this);
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
                scaleToLarge(v.findViewById(R.id.conlumn_ismartv_linear_layout));
            } else {
                scaleToNormal(v.findViewById(R.id.conlumn_ismartv_linear_layout));
            }
        }

        @Override
        public void onClick(View view) {

        }
    }
}
