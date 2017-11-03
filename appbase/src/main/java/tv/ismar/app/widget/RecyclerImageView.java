/*add by dragontec for bug 4205 start*/
package tv.ismar.app.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by zhaoj on 17-11-2.
 */

public class RecyclerImageView extends ImageView {

    public RecyclerImageView(Context context) {
        super(context);
    }

    public RecyclerImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setImageDrawable(null);
    }
}
/*add by dragontec for bug 4205 end*/
