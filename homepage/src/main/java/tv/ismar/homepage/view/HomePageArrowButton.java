/*add by dragontec for bug 4350 start*/
package tv.ismar.homepage.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.support.v7.widget.AppCompatButton;

import tv.ismar.homepage.R;

/**
 * Created by liujy on 2017/11/6.
 */

public class HomePageArrowButton extends AppCompatButton {
	public HomePageArrowButton(Context context) {
		super(context);
		setClickable(false);
	}

	public HomePageArrowButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClickable(false);
	}

	public HomePageArrowButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setClickable(false);
	}
	@Override
	public boolean onHoverEvent(MotionEvent event) {
		/*modify by dragontec for bug 4350 start*/
		switch (event.getAction()) {
			case MotionEvent.ACTION_HOVER_ENTER:
			case MotionEvent.ACTION_HOVER_MOVE: {
				float margin = getResources().getDimensionPixelSize(R.dimen.home_page_banner_arrow_hover_margin) / getResources().getDisplayMetrics().density;
				if (event.getX() >= margin && event.getX() <= getResources().getDisplayMetrics().widthPixels - margin) {
					requestFocus();
					requestFocusFromTouch();
					setClickable(true);
					return true;
				} else {
					clearFocus();
					setClickable(false);
					return false;
				}
			}
			case MotionEvent.ACTION_HOVER_EXIT:
			{
				if (event.getButtonState() != MotionEvent.BUTTON_PRIMARY) {
					clearFocus();
					setClickable(false);
					return false;
				}
			}
		}
		return super.onHoverEvent(event);
		/*modify by dragontec for bug 4350 end*/
	}
}
/*add by dragontec for bug 4350 end*/