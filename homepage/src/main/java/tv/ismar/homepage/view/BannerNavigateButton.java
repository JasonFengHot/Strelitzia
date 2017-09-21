package tv.ismar.homepage.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by huibin on 20/09/2017.
 */

public class BannerNavigateButton extends android.support.v7.widget.AppCompatButton {
    public BannerNavigateButton(Context context) {
        super(context);
    }

    public BannerNavigateButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        return true;
    }
}
