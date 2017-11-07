package tv.ismar.homepage.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.orhanobut.logger.Logger;
/*add by dragontec for bug 4338 start*/
import tv.ismar.homepage.R;
/*add by dragontec for bug 4338 end*/

import static android.view.MotionEvent.BUTTON_PRIMARY;

/**
 * Created by huibin on 19/09/2017.
 */

public class BannerLinearLayout extends LinearLayout {
    private static final String TAG = BannerLinearLayout.class.getSimpleName();

    private View navigationLeft;
    private View navigationRight;
    private RecyclerViewTV recyclerViewTV;
/*add by dragontec for bug 4275 start*/
    private View headView;
/*add by dragontec for bug 4275 end*/

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

    public void setRecyclerViewTV(RecyclerViewTV recyclerViewTV) {
        this.recyclerViewTV = recyclerViewTV;
    }

/*add by dragontec for bug 4275 start*/
    public void setHeadView(View headView) {
        this.headView = headView;
    }
/*add by dragontec for bug 4275 end*/

    public BannerLinearLayout(Context context) {
        super(context);
    }

    public BannerLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


/*delete by dragontec for bug 4332 start*/
//    @Override
//    protected boolean dispatchHoverEvent(MotionEvent event) {
//        switch (event.getAction()){
//            case MotionEvent.ACTION_HOVER_ENTER:
//            case MotionEvent.ACTION_HOVER_MOVE:
//            	if (recyclerViewTV != null) {
//            		if (recyclerViewTV.cannotScrollBackward(-10) && (headView == null || headView.getVisibility() == View.VISIBLE)) {
//            			navigationLeft.setVisibility(INVISIBLE);
//					} else {
//            			navigationLeft.setVisibility(VISIBLE);
//					}
//					if (recyclerViewTV.cannotScrollForward(10)) {
//						navigationRight.setVisibility(INVISIBLE);
//					} else {
//            			navigationRight.setVisibility(VISIBLE);
//					}
//				}
//                break;
//            case MotionEvent.ACTION_HOVER_EXIT:
//                if (event.getButtonState() != BUTTON_PRIMARY){
//                    navigationLeft.setVisibility(INVISIBLE);
//                    navigationRight.setVisibility(INVISIBLE);
//                }
//                break;
//        }
//        return super.dispatchHoverEvent(event);
//    }
/*delete by dragontec for bug 4332 end*/

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
/*add by dragontec for bug 4205 start*/
	    if (navigationLeft != null && navigationRight != null) {
/*add by dragontec for bug 4205 end*/
/*modify by dragontec for bug 4332 start*/
            if (navigationLeft.getVisibility() == VISIBLE || navigationRight.getVisibility() == VISIBLE) {
                navigationLeft.setVisibility(INVISIBLE);
                navigationRight.setVisibility(INVISIBLE);
            }
/*modify by dragontec for bug 4332 end*/
/*add by dragontec for bug 4205 start*/
        }
/*add by dragontec for bug 4205 end*/
		return super.dispatchKeyEvent(event);
	}

	/*add by dragontec for bug 4338 start*/
	@Override
	public View focusSearch(View focused, int direction) {
		View result = null;
		/*modify by dragontec for bug 4391 start*/
//		if (focused.getId() == R.id.guide_head_ismartv_linearlayout) {
			if (mFocusSearchFailedListener != null) {
				result = mFocusSearchFailedListener.onFocusSearchFailed(focused, direction);
			}
//		}
		/*modify by dragontec for bug 4391 start*/
		return result != null ? result : super.focusSearch(focused, direction);
	}

	public interface FocusSearchFailedListener {
		View onFocusSearchFailed(View focused, int direction);
	}

	private FocusSearchFailedListener mFocusSearchFailedListener = null;

	public void setFocusSearchFailedListener(FocusSearchFailedListener focusSearchFailedListener) {
		mFocusSearchFailedListener = focusSearchFailedListener;
	}
	/*add by dragontec for bug 4338 end*/

}
