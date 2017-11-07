package tv.ismar.helperpage.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import tv.ismar.app.widget.RecyclerImageView;
//import android.widget.ImageView;

/**
 * Created by huaijie on 2015/4/8.
 */
public class SakuraImageView extends RecyclerImageView {
    public SakuraImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onHoverChanged(boolean hovered) {
        if (hovered) {
            requestFocus();
        } else {
            clearFocus();
        }
    }

}
