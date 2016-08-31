package tv.ismar.app.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import tv.ismar.app.R;

/**
 * Created by beaver on 16-8-23.
 */
public class LabelImageView extends FrameLayout {

    private final String TAG = "LH/LabelImageView";

    private String livUrl;
    private NinePatchDrawable livSelectorDrawable;
    private Drawable livErrorDrawable;
    private int livContentPadding;
    private String livLabelText;
    private int livLabelColor;
    private int livLabelSize;
    private int livLabelBackColor;
    private int livVipPosition;
    private String livVipUrl;
    private int livVipSize;
    private float livRate;
    private int livRateColor;
    private int livRateSize;
    private int livRateMarginRight;
    private int livRateMarginBottom;

    private ImageView imageView, vipImageView;
    private TextView textView;

    public static final int LEFTTOP = 0;
    public static final int RIGHTTOP = 1;
    public static final int GONE = -1;
    private Animation scaleSmallAnimation;
    private Animation scaleBigAnimation;
    private Rect mBound;
    private Rect mRect;
    private boolean drawBorder;

    private Context mContext;

    public LabelImageView(Context context) {
        this(context, null);
    }

    public LabelImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LabelImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LabelImageView);
        livUrl = typedArray.getString(R.styleable.LabelImageView_livUrl);
        livSelectorDrawable = (NinePatchDrawable) typedArray.getDrawable(R.styleable.LabelImageView_livSelectorDrawable);
        livErrorDrawable = typedArray.getDrawable(R.styleable.LabelImageView_livErrorDrawable);
        livContentPadding = typedArray.getDimensionPixelSize(R.styleable.LabelImageView_livContentPadding, dp2px(5));
        livLabelText = typedArray.getString(R.styleable.LabelImageView_livLabelText);
        livLabelColor = typedArray.getColor(R.styleable.LabelImageView_livLabelColor, Color.WHITE);
        livLabelSize = typedArray.getDimensionPixelSize(R.styleable.LabelImageView_livLabelSize, sp2px(16));
        livLabelBackColor = typedArray.getColor(R.styleable.LabelImageView_livLabelBackColor, Color.parseColor("#33000000"));
        livVipPosition = typedArray.getInt(R.styleable.LabelImageView_livVipPosition, GONE);
        livVipUrl = typedArray.getString(R.styleable.LabelImageView_livVipUrl);
        livVipSize = typedArray.getDimensionPixelSize(R.styleable.LabelImageView_livVipSize, dp2px(40));
        livRate = typedArray.getFloat(R.styleable.LabelImageView_livRate, 0);
        livRateColor = typedArray.getColor(R.styleable.LabelImageView_livRateColor, Color.parseColor("#ff9000"));
        livRateSize = typedArray.getDimensionPixelSize(R.styleable.LabelImageView_livRateSize, sp2px(14));
        livRateMarginRight = typedArray.getDimensionPixelSize(R.styleable.LabelImageView_livRateMarginRight, dp2px(5));
        livRateMarginBottom = typedArray.getDimensionPixelSize(R.styleable.LabelImageView_livRateMarginBottom, dp2px(5));
        typedArray.recycle();
        setWillNotDraw(false);
        mRect = new Rect();
        mBound = new Rect();

        initView();

    }

    private void initView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        imageView = new ImageView(mContext);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(imageView);

        FrameLayout.LayoutParams ltparams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        ltparams.gravity = Gravity.LEFT | Gravity.TOP;
        vipImageView = new ImageView(mContext);
        vipImageView.setLayoutParams(ltparams);
        vipImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        vipImageView.setVisibility(View.INVISIBLE);
        addView(vipImageView);

        int livLabelHeight = (int) (livLabelSize * 1.5f);
        FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, livLabelHeight);
        labelParams.gravity = Gravity.BOTTOM;
        textView = new TextView(mContext);
        textView.setLayoutParams(labelParams);
        textView.setBackgroundColor(livLabelBackColor);
        textView.setGravity(Gravity.CENTER);
        textView.setText(livLabelText);
        textView.setTextSize(livLabelSize);
        textView.setTextColor(livLabelColor);
        addView(textView);

        if (livRate > 0) {
            FrameLayout.LayoutParams rateParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            rateParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            rateParams.bottomMargin = livRateMarginBottom;
            rateParams.rightMargin = livRateMarginRight;
            TextView rateTextView = new TextView(mContext);
            rateTextView.setText(String.valueOf(livRate));
            rateTextView.setTextColor(livRateColor);
            rateTextView.setTextSize(livRateSize);
            addView(rateTextView);
        }

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        super.getDrawingRect(mRect);
        if (drawBorder) {
            mBound.set(-dp2px(21) + mRect.left, -dp2px(21) + mRect.top, dp2px(21) + mRect.right, dp2px(21) + mRect.bottom);
            livSelectorDrawable.setBounds(mBound);
            canvas.save();
            livSelectorDrawable.draw(canvas);
            canvas.restore();
        }
        getRootView().requestLayout();
        getRootView().invalidate();
    }

    private void setBackgroundBorder(boolean focus) {
        if (focus) {
            drawBorder = true;
            getRootView().requestLayout();
            getRootView().invalidate();
            zoomOut();
        } else {
            drawBorder = false;
            zoomIn();
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        Log.i(TAG, "onFocusChanged:" + gainFocus);
        if (gainFocus) {
            setBackgroundBorder(true);
        } else {
            setBackgroundBorder(false);
        }
    }

    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(hovered);
        Log.i(TAG, "onHoverChanged:" + hovered);
        if (hovered) {
            setBackgroundBorder(true);
        } else {
            setBackgroundBorder(false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setBackgroundBorder(true);
                return true;
            case MotionEvent.ACTION_UP:
                setBackgroundBorder(false);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void asyncLoadImage() {
//        Log.i(TAG, "asyncLoadImage:" + livUrl);
        if (imageView != null) {
            if (TextUtils.isEmpty(livUrl)) {
                imageView.setImageDrawable(livErrorDrawable);
            } else {
                Picasso.with(mContext).load(livUrl)
                        .placeholder(livErrorDrawable)
                        .error(livErrorDrawable)
                        .into(imageView);
            }
        }
    }

    private void asyncLoadVipImage() {
        Log.i(TAG, "asyncLoadVipImage:" + livVipUrl);
        if (vipImageView != null && !TextUtils.isEmpty(livVipUrl)) {
            FrameLayout.LayoutParams ltparams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            if (livVipPosition == LEFTTOP) {
                ltparams.gravity = Gravity.LEFT | Gravity.TOP;
            } else if (livVipPosition == RIGHTTOP) {
                ltparams.gravity = Gravity.RIGHT | Gravity.TOP;
            }
            vipImageView.setLayoutParams(ltparams);
            vipImageView.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(livVipUrl)
                    .into(vipImageView);
        }
    }

    public String getLivUrl() {
        return livUrl;
    }

    public void setLivUrl(String livUrl) {
        this.livUrl = livUrl;
        asyncLoadImage();
    }

    public Drawable getLivSelectorDrawable() {
        return livSelectorDrawable;
    }

    public void setLivSelectorDrawable(NinePatchDrawable livSelectorDrawable) {
        this.livSelectorDrawable = livSelectorDrawable;
    }

    public Drawable getLivErrorDrawable() {
        return livErrorDrawable;
    }

    public void setLivErrorDrawable(Drawable livErrorDrawable) {
        this.livErrorDrawable = livErrorDrawable;
    }

    public int getLivContentPadding() {
        return livContentPadding;
    }

    public void setLivContentPadding(int livContentPadding) {
        this.livContentPadding = livContentPadding;
    }

    public String getLivLabelText() {
        return livLabelText;
    }

    public void setLivLabelText(String livLabelText) {
        this.livLabelText = livLabelText;
    }

    public int getLivLabelColor() {
        return livLabelColor;
    }

    public void setLivLabelColor(int livLabelColor) {
        this.livLabelColor = livLabelColor;
    }

    public int getLivLabelSize() {
        return livLabelSize;
    }

    public void setLivLabelSize(int livLabelSize) {
        this.livLabelSize = livLabelSize;
    }

    public int getLivLabelBackColor() {
        return livLabelBackColor;
    }

    public void setLivLabelBackColor(int livLabelBackColor) {
        this.livLabelBackColor = livLabelBackColor;
    }

    public int getLivVipPosition() {
        return livVipPosition;
    }

    public void setLivVipPosition(int livVipPosition) {
        this.livVipPosition = livVipPosition;
    }

    public String getLivVipUrl() {
        return livVipUrl;
    }

    public void setLivVipUrl(String livVipUrl) {
        this.livVipUrl = livVipUrl;
        asyncLoadVipImage();
    }

    public int getLivVipSize() {
        return livVipSize;
    }

    public void setLivVipSize(int livVipSize) {
        this.livVipSize = livVipSize;
    }

    public float getLivRate() {
        return livRate;
    }

    public void setLivRate(float livRate) {
        this.livRate = livRate;
    }

    public int getLivRateColor() {
        return livRateColor;
    }

    public void setLivRateColor(int livRateColor) {
        this.livRateColor = livRateColor;
    }

    public int getLivRateSize() {
        return livRateSize;
    }

    public void setLivRateSize(int livRateSize) {
        this.livRateSize = livRateSize;
    }

    public int getLivRateMarginRight() {
        return livRateMarginRight;
    }

    public void setLivRateMarginRight(int livRateMarginRight) {
        this.livRateMarginRight = livRateMarginRight;
    }

    public int getLivRateMarginBottom() {
        return livRateMarginBottom;
    }

    public void setLivRateMarginBottom(int livRateMarginBottom) {
        this.livRateMarginBottom = livRateMarginBottom;
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    private void zoomIn() {
        if (scaleSmallAnimation == null) {
            scaleSmallAnimation = AnimationUtils.loadAnimation(getContext(),
                    R.anim.anim_scale_small);
        }
        startAnimation(scaleSmallAnimation);
    }

    private void zoomOut() {
        if (scaleBigAnimation == null) {
            scaleBigAnimation = AnimationUtils.loadAnimation(getContext(),
                    R.anim.anim_scale_big);
        }
        startAnimation(scaleBigAnimation);
    }
}
