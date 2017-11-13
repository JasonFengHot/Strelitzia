package tv.ismar.homepage.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/*add by dragontec for fix ½¹µã´íÎó start*/
import tv.ismar.homepage.R;
/*add by dragontec for fix ½¹µã´íÎó end*/

/**
 * Created by admin on 2017/10/22.
 */

public class HomeRootRelativeLayout extends RelativeLayout {

    private View upArrow;
    private View downArrow;
    private boolean showUp=false;
    private boolean showDown=true;
	/*modify by dragontec for bug 4339 start*/
    private boolean isKeyMode = true;
	/*modify by dragontec for bug 4339 end*/

    public HomeRootRelativeLayout(Context context) {
        this(context,null);
    }

    public HomeRootRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HomeRootRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
		/*modify by dragontec for bug 4339 start*/
        if(event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
            isKeyMode = false;
            updateUpDownArrow();
        }
        return super.dispatchHoverEvent(event);
	
    }

    public void updateUpDownArrow() {
        if(isKeyMode){
            if(upArrow!=null) {
                upArrow.setVisibility(View.GONE);
            }
            if(downArrow!=null) {
                downArrow.setVisibility(View.GONE);
            }
        }else{
            if(upArrow!=null) {
                if (showUp) {
                    upArrow.setVisibility(View.VISIBLE);
                } else {
                    upArrow.setVisibility(View.GONE);
                }
            }
            if(downArrow!=null) {
                if (showDown) {
                    downArrow.setVisibility(View.VISIBLE);
                } else {
                    downArrow.setVisibility(View.GONE);
                }
            }
        }
    }
	/*modify by dragontec for bug 4339 end*/
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
		/*modify by dragontec for bug 4339 start*/
        isKeyMode = true;
		/*modify by dragontec for bug 4339 end*/
        if(upArrow!=null) {
            upArrow.setVisibility(View.GONE);
        }
        if(downArrow!=null) {
            downArrow.setVisibility(View.GONE);
        }
        return super.dispatchKeyEvent(event);
    }

    public void setUpArrow(View upArrow) {
        this.upArrow = upArrow;
    }

    public void setDownArrow(View downArrow) {
        this.downArrow = downArrow;
    }

    public void setShowUp(boolean showUp) {
        this.showUp = showUp;
    }

    public void setShowDown(boolean showDown) {
        this.showDown = showDown;
    }

    public View getUpArrow() {
        return upArrow;
    }

    public View getDownArrow() {
        return downArrow;
    }

	/*modify by dragontec for bug 4338 start*/
	@Override
	public View focusSearch(View focused, int direction) {
    	View result = null;
    	try {
			if (focused instanceof TextView && focused.getParent().getParent() instanceof HorizontalTabView) {
				if (mFocusSearchFailedListener != null) {
					result = mFocusSearchFailedListener.onFocusSearchFailed(focused, direction);
					if (result == null && direction == FOCUS_DOWN) {
						result = focused;
					}
				}
			}
			/*add by dragontec for fix ½¹µã´íÎó start*/
			if ((focused.getId() == R.id.collection_rect_layout || focused.getId() == R.id.center_rect_layout) && direction == FOCUS_DOWN) {
				View tabView = findViewById(R.id.channel_tab);
				if (tabView != null && tabView instanceof HorizontalTabView) {
					result = ((HorizontalTabView) tabView).getLastFocusView();
				}
			}
			/*add by dragontec for fix ½¹µã´íÎó end*/
		} catch (NullPointerException e) {
    		e.printStackTrace();
    		result = null;
		}

		return result != null ? result : super.focusSearch(focused, direction);
	}

	public interface FocusSearchFailedListener {
		View onFocusSearchFailed(View focused, int direction);
	}

	private FocusSearchFailedListener mFocusSearchFailedListener = null;

	public void setFocusSearchFailedListener(FocusSearchFailedListener focusSearchFailedListener) {
		mFocusSearchFailedListener = focusSearchFailedListener;
	}
	/*modify by dragontec for bug 4338 end*/
}
