package tv.ismar.homepage.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.R;

import static android.view.View.SCALE_X;
import static android.view.View.SCALE_Y;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 栏目RecyclerView
 */

public class ConlumnAdapter extends RecyclerView.Adapter<ConlumnAdapter.ConlumnViewHolder> {

    private Context mContext;
    private HomeEntity mData;

    public ConlumnAdapter(Context context, HomeEntity data){
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
        holder.mTitle.setText(mData.carousels.get(position).title);
    }

    @Override
    public int getItemCount() {
        return (mData!=null)? mData.carousels.size() : 0;
    }

    public static class ConlumnViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener{
        public TextView mTitle;
        public ConlumnViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.conlumn_item_tv);
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
                scaleToLarge(v.findViewById(R.id.ismartv_linear_layout));
                v.findViewById(R.id.title).setSelected(true);
                v.findViewById(R.id.introduction).setSelected(true);
            } else {
                scaleToNormal(v.findViewById(R.id.ismartv_linear_layout));
                v.findViewById(R.id.title).setSelected(false);
                v.findViewById(R.id.introduction).setSelected(false);
            }
        }
    }
}
