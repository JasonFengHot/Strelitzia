package tv.ismar.app.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private ValueAnimator openAnimator,closeAnimator;
    private ViewGroup mLayout;//隐藏的layout

    public TelescopicWrap(Context context, ViewGroup viewGroup) {
        init(context);
        this.mLayout = viewGroup;
    }

    private TextView mTv;
    private RecyclerImageView mIconv;
    public void setTextView(TextView view){
        this.mTv = view;
    }

    public void setIcon(RecyclerImageView view){
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
            if(closeAnimator!=null&&closeAnimator.isRunning()){
                closeAnimator.cancel();
            }
            openAnimator= createDropAnimator(mLayout, 0,
                    mTextWidth, true);
            openAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mTv.setVisibility(View.VISIBLE);
                    mTv.setWidth(0);
					/*add by dragontec for bug 4368 start*/
                    ((LinearLayout.LayoutParams)mIconv.getLayoutParams()).rightMargin = mIconv.getResources().getDimensionPixelSize(R.dimen.guide_title_react_icon_mr);
					/*add by dragontec for bug 4368 end*/
                    super.onAnimationStart(animation);
                }
				/*add by dragontec for bug 4368 start*/
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mTv.getLayoutParams().width = mTv.getResources().getDimensionPixelSize(R.dimen.guide_title_react_text_width);
                }
				/*add by dragontec for bug 4368 end*/
            });
            openAnimator.setDuration(100);
            openAnimator.start();
        }
    }

    private void animateClose() {
        if(mLayout != null){
            if(openAnimator!=null&&openAnimator.isRunning()){
                openAnimator.cancel();
            }
            closeAnimator = createDropAnimator(mLayout, mTextWidth, mTextWidth/3, false);
            closeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLayout.setBackgroundColor(Color.parseColor("#00000000"));
                    mTv.setVisibility(View.GONE);
					/*add by dragontec for bug 4368 start*/
                    ((LinearLayout.LayoutParams)mIconv.getLayoutParams()).rightMargin = 0;
					/*add by dragontec for bug 4368 start*/
                }
            });
            closeAnimator.setDuration(100);
            closeAnimator.start();
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
