package tv.ismar.homepage.adapter;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import tv.ismar.homepage.OnItemSelectedListener;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/15
 * @DESC: 适配器基类
 */

public abstract class BaseRecycleAdapter{
    public OnItemSelectedListener mItemSelectedListener = null;

    protected void setOnItemSelectedListener(OnItemSelectedListener listener){
        this.mItemSelectedListener = listener;
    }
}
