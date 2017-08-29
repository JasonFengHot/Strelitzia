package tv.ismar.app.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import tv.ismar.app.R;


/**
 * @AUTHOR: xi
 * @DATE: 2017/8/22
 * @DESC: 可展开图片
 */

public class OpenView extends LinearLayout implements View.OnFocusChangeListener{
    private static final String TAG = OpenView.class.getSimpleName();
    private int mHiddenViewWidth = 0;//隐藏的宽度

    private Context mContext;
    private TextView mTv;
    private TextView mIconTv;

    public OpenView(Context context) {
        super(context);
        init(context, null);
    }

    public OpenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        this.mContext = context;
        setOrientation(LinearLayout.HORIZONTAL);
        setFocusable(true);
        setOnFocusChangeListener(this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OpenView);
        String text = typedArray.getString(R.styleable.OpenView_leftText);
        Drawable icon = typedArray.getDrawable(R.styleable.OpenView_rightIcon);
        int iconSize = typedArray.getDimensionPixelSize(R.styleable.OpenView_rIconSize, 0);
        float textSize = typedArray.getDimension(R.styleable.OpenView_lTextSize, 0);
        int paddingLeft = typedArray.getDimensionPixelSize(R.styleable.OpenView_iconPaddingLeft, 0);

        mTv = new TextView(mContext);
        mTv.setText(text);
        mTv.setSingleLine(true);
        mTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mTv.setVisibility(INVISIBLE);

        mIconTv = new TextView(mContext);
        MarginLayoutParams iconParams = new LayoutParams(iconSize, iconSize);
        iconParams.leftMargin = paddingLeft;
        mIconTv.setLayoutParams(iconParams);
        mIconTv.setBackground(icon);

        addView(mTv);
        addView(mIconTv);
    }


    public void openOrClose(boolean open){
        if(open){
//            setBackground(getResources().getDrawable(R.drawable.title_focuse_bg));
            animateOpen();
        } else {
//            setBackground(null);
            animateClose();
        }
    }

    private void animateOpen() {
        if(mTv != null){
            mTv.setVisibility(View.VISIBLE);
            ValueAnimator animator = createDropAnimator(mTv, 0,
                    mHiddenViewWidth);
            animator.start();
        }
    }

    private void animateClose() {
        if(mTv != null){
            int origHeight = mTv.getWidth();
            ValueAnimator animator = createDropAnimator(mTv, origHeight, 0);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mTv.setVisibility(View.INVISIBLE);
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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.i(TAG, "layout width:"+mTv.getWidth());
        if(mTv.getWidth()>0 && mHiddenViewWidth==0){
            Log.i(TAG, "layout width:"+mTv.getWidth()+ " minWidth:"+mTv.getMinWidth());
            mHiddenViewWidth = mTv.getWidth();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        openOrClose(hasFocus);
    }
}
