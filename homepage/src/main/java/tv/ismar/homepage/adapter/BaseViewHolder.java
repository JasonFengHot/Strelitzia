package tv.ismar.homepage.adapter;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import tv.ismar.homepage.OnItemSelectedListener;

import static android.view.View.SCALE_X;
import static android.view.View.SCALE_Y;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/15
 * @DESC: ViewHolder基类
 */
public abstract class BaseViewHolder extends RecyclerView.ViewHolder implements
        View.OnFocusChangeListener, View.OnClickListener{

    public int mPosition;//item位置
    private OnItemSelectedListener mClickListener = null;

    public BaseViewHolder(View itemView, BaseRecycleAdapter baseAdapter) {
        super(itemView);
        this.mClickListener = baseAdapter.mClickListener;
        constructure();
    }

    public BaseViewHolder(View itemView, OnItemSelectedListener listener) {
        super(itemView);
        this.mClickListener = listener;
        constructure();
    }

    private void constructure(){
        itemView.findViewById(getScaleLayoutId()).setOnFocusChangeListener(this);
        itemView.findViewById(getScaleLayoutId()).setOnClickListener(this);
    }

    protected abstract int getScaleLayoutId();

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            scaleToLarge(v.findViewById(getScaleLayoutId()));
        } else {
            scaleToNormal(v.findViewById(getScaleLayoutId()));
        }
    }

    @Override
    public void onClick(View v) {
        if(mClickListener!=null && v.getId()==getScaleLayoutId()){//item选中事件
            mClickListener.itemSelected(v, mPosition);
        }
    }

    /*缩放到1.1倍*/
    protected void scaleToLarge(View view) {
        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(view, SCALE_X, 1.0F, 1.1F);
        objectAnimatorX.setDuration(100L);
        objectAnimatorX.start();
        ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(view, SCALE_Y, 1.0F, 1.1F);
        objectAnimatorY.setDuration(100L);
        objectAnimatorY.start();
    }

    /*缩放到正常*/
    protected void scaleToNormal(View view) {
        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(view, SCALE_X, 1.1F, 1.0F);
        objectAnimatorX.setDuration(100L);
        objectAnimatorX.start();
        ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(view, SCALE_Y, 1.1F, 1.0F);
        objectAnimatorY.setDuration(100L);
        objectAnimatorY.start();
    }
}
