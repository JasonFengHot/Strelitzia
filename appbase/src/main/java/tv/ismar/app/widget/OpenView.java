package tv.ismar.app.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/22
 * @DESC: 可展开图片
 */

public class OpenView {
    private static final String TAG = OpenView.class.getSimpleName();
    private int mHiddenViewWidth = 0;//隐藏的宽度
    private TextView mView = null;
    private int mTextSize = 60;

    public OpenView(Context context, TextView textView){
        if(textView==null){
            throw new IllegalStateException("OpenView | invalide arguments");
        }
        this.mView = textView;
        calculateWidth(context);
    }

    public void openOrClose(boolean open){
        if(open){
            animateOpen();
        } else {
            animateClose();
        }
    }

    private void calculateWidth(Context context){
        float density = context.getResources().getDisplayMetrics().density;
        mHiddenViewWidth = (int) (mTextSize*density + 0.5);
    }

    private void animateOpen() {
        if(mView != null){
            mView.setVisibility(View.VISIBLE);
            ValueAnimator animator = createDropAnimator(mView, 0,
                    mHiddenViewWidth);
            animator.start();
        }
    }

    public void setTextSize(int size){
        this.mTextSize = size;
    }

    private void animateClose() {
        if(mView != null){
            int origHeight = mView.getWidth();
            ValueAnimator animator = createDropAnimator(mView, origHeight, 0);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mView.setVisibility(View.GONE);
                }

            });
            animator.start();
        }
    }

    private ValueAnimator createDropAnimator(final View v, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                int value = (int) arg0.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.width = value;
                v.setLayoutParams(layoutParams);

            }
        });
        return animator;
    }

}
