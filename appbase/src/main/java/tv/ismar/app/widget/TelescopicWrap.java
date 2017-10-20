package tv.ismar.app.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import tv.ismar.app.R;


/**
 * @AUTHOR: xi
 * @DATE: 2017/8/22
 * @DESC: 文字伸缩（带图片和文字）包装类
 */

public class TelescopicWrap {
    private static final String TAG = TelescopicWrap.class.getSimpleName();

    private Context mContext;
    private static int mTextWidth;
    private static int mIconWidth;

    private ViewGroup mLayout;//隐藏的layout

    public TelescopicWrap(Context context, ViewGroup viewGroup) {
        init(context);
        this.mLayout = viewGroup;
    }

    private TextView mTv;
    private ImageView mIconv;
    public void setTextView(TextView view){
        this.mTv = view;
    }

    public void setIcon(ImageView view){
        this.mIconv = view;
    }

    private void init(Context context){
        this.mContext = context;
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
            mTv.setVisibility(View.VISIBLE);
            mTv.setWidth(0);
            ValueAnimator animator = createDropAnimator(mLayout, 0,
                    mTextWidth, true);
            animator.setDuration(200);
            animator.start();
        }
    }

    private void animateClose() {
        if(mLayout != null){
            ValueAnimator animator = createDropAnimator(mLayout, mTextWidth, mTextWidth/3, false);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLayout.setBackgroundColor(Color.parseColor("#00000000"));
                    mTv.setVisibility(View.GONE);
                }
            });
            animator.setDuration(200);
            animator.start();
        }
    }

    private ValueAnimator createDropAnimator(final View v, int start, int end, final boolean isOpen) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                int value = (int) arg0.getAnimatedValue();
                Log.i(TAG, "value:"+value+" mIconWidth:"+mIconWidth);
                ViewGroup.LayoutParams tvParams = mTv.getLayoutParams();
                tvParams.width = value- mContext.getResources().getDimensionPixelOffset(R.dimen.guide_title_react_offset);
                mTv.setLayoutParams(tvParams);

                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.width = value;
                mLayout.setBackgroundResource(R.drawable.title_focuse_bg);
                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }
}
