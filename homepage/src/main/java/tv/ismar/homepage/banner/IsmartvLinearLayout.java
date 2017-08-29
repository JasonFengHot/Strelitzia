package tv.ismar.homepage.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by huibin on 25/08/2017.
 */

public class IsmartvLinearLayout extends LinearLayout {

    private Drawable foreground;

    public IsmartvLinearLayout(Context context) {
        super(context, null);
    }

    public IsmartvLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, com.example.foregroundimageview.R.styleable.ForegroundImageView);
        Drawable foreground = a.getDrawable(com.example.foregroundimageview.R.styleable.ForegroundImageView_android_foreground);
        if (foreground != null) {
            setForeground(foreground);
        }
        a.recycle();
    }


    public void setForegroundResource(int drawableResId) {
        setForeground(getContext().getResources().getDrawable(drawableResId));
    }

    /**
     * Supply a Drawable that is to be rendered on top of all of the child
     * views in the frame layout.
     *
     * @param drawable The Drawable to be drawn on top of the children.
     */
    public void setForeground(Drawable drawable) {
        if (foreground == drawable) {
            return;
        }
        if (foreground != null) {
            foreground.setCallback(null);
            unscheduleDrawable(foreground);
        }

        foreground = drawable;

        if (drawable != null) {
            drawable.setCallback(this);
            if (drawable.isStateful()) {
                drawable.setState(getDrawableState());
            }
        }
        requestLayout();
        invalidate();
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == foreground;
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (foreground != null) foreground.jumpToCurrentState();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (foreground != null && foreground.isStateful()) {
            foreground.setState(getDrawableState());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (foreground != null) {
            foreground.setBounds(0 - 19, 0 - 19, getMeasuredWidth() + 19, getMeasuredHeight() + 19);
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (foreground != null) {
            foreground.setBounds(-19, -19, w + 19, h + 19);
            invalidate();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (foreground != null) {
            foreground.draw(canvas);
        }
    }
}
