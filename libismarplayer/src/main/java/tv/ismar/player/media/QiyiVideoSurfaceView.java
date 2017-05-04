package tv.ismar.player.media;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;

import com.qiyi.sdk.player.VideoSurfaceView;

/**
 * Created by LongHai on 17-5-2.
 * <p>
 * To change parent
 */

public class QiyiVideoSurfaceView extends VideoSurfaceView {

    public QiyiVideoSurfaceView(Context context) {
        super(context);
        init();
    }

    public QiyiVideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QiyiVideoSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        getHolder().setFormat(PixelFormat.RGBA_8888);
    }

    // begin {@
    private boolean mIgnoreWindowChange;

    public void setIgnoreWindowChange(boolean ignoreWindowChange) {
        mIgnoreWindowChange = ignoreWindowChange;
    }

    public boolean getIgnoreWindowChange() {
        return mIgnoreWindowChange;
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (!mIgnoreWindowChange) {
            super.onWindowVisibilityChanged(visibility);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (!mIgnoreWindowChange) {
            super.onDetachedFromWindow();
        }
    }
}
