package tv.ismar.app.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import tv.ismar.app.R;


/**
 * @AUTHOR: xi
 * @DATE: 2017/8/22
 * @DESC: 文字伸缩（带图片和文字）包装类
 */

public class TelescopicWrap {
    private static final String TAG = TelescopicWrap.class.getSimpleName();

    private static int mTextWidth;
    private static int mIconWidth;

    private ViewGroup mLayout;//隐藏的layout

    public TelescopicWrap(Context context, ViewGroup viewGroup) {
        init(context);
        this.mLayout = viewGroup;
    }

    private void init(Context context){
        mIconWidth = context.getResources().getDimensionPixelSize(R.dimen.guide_title_icon_size)+36;
        mTextWidth = context.getResources().getDimensionPixelSize(R.dimen.guide_title_react_width);
    }

    public void setView(ViewGroup view){
        this.mLayout = view;
    }

    public void openOrClose(boolean open){
        if(open){
            animateOpen();
        } else {
            animateClose();
        }
    }

    private void animateOpen() {
        if(mLayout != null){
            ValueAnimator animator = createDropAnimator(mLayout, 0,
                    mTextWidth, true);
            animator.start();
        }
    }

    private void animateClose() {
        if(mLayout != null){
            ValueAnimator animator = createDropAnimator(mLayout, mTextWidth, 0, false);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLayout.setBackgroundColor(Color.parseColor("#00000000"));
                }
            });
            animator.start();
        }
    }

    private ValueAnimator createDropAnimator(final View v, int start, int end, final boolean isOpen) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                int value = (int) arg0.getAnimatedValue();
                if(!isOpen && value<=mIconWidth){
                    return;
                }
                Log.i(TAG, "value:"+value+" mIconWidth:"+mIconWidth);
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.width = value;
                mLayout.setBackgroundResource(R.drawable.title_focuse_bg);
                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }
}
