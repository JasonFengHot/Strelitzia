package tv.ismar.homepage.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.orhanobut.logger.Logger;

import static android.view.MotionEvent.BUTTON_PRIMARY;

/**
 * Created by huibin on 19/09/2017.
 */

public class BannerLinearLayout extends LinearLayout {
    private static final String TAG = BannerLinearLayout.class.getSimpleName();

    private View navigationLeft;
    private View navigationRight;

    private boolean isDpadCenterClick = false;

    public void setNavigationLeft(View navigationLeft) {
        this.navigationLeft = navigationLeft;
//        this.navigationLeft.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN){
//                    isDpadCenterClick = true;
//                    v.callOnClick();
//                    return true;
//                }
//                return false;
//            }
//        });
    }

    public void setNavigationRight(View navigationRight) {
        this.navigationRight = navigationRight;
    }

    public BannerLinearLayout(Context context) {
        super(context);
    }

    public BannerLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                if (navigationLeft.getVisibility() == INVISIBLE){
                    navigationLeft.setVisibility(VISIBLE);
                    navigationRight.setVisibility(VISIBLE);
                }
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                if (event.getButtonState() != BUTTON_PRIMARY){
                    navigationLeft.setVisibility(INVISIBLE);
                    navigationRight.setVisibility(INVISIBLE);
                }
                break;
        }
        return super.dispatchHoverEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        Log.d(TAG, "dispatchGenericMotionEvent: " + event);
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
            if (navigationLeft.getVisibility() == VISIBLE){
                navigationLeft.setVisibility(INVISIBLE);
                navigationRight.setVisibility(INVISIBLE);
            }
        return super.dispatchKeyEvent(event);
    }
}
