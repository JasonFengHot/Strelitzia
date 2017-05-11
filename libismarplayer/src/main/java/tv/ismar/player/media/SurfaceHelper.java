package tv.ismar.player.media;

import android.support.annotation.MainThread;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import tv.ismar.library.util.LogUtils;

/**
 * Created by LongHai on 17-4-27.
 */

class SurfaceHelper {

    interface SurfaceCallback {
        @MainThread
        void onSurfaceCreated(boolean hasPreload);

        @MainThread
        void onSurfaceDestroyed();
    }

    private final SurfaceView mSurfaceView;
    private final SurfaceHolder mSurfaceHolder;
    private Surface mSurface;
    private final SurfaceCallback mSurfaceCallback;
    private boolean hasPreload;

    SurfaceHelper(SurfaceView surfaceView, SurfaceCallback surfaceCallback) {
        mSurfaceView = surfaceView;
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceCallback = surfaceCallback;
    }

    private void setSurface(Surface surface) {
        if (surface.isValid()) {
            mSurface = surface;
            if (mSurfaceCallback != null) {
                mSurfaceCallback.onSurfaceCreated(hasPreload);
            }
        }
    }

    void attachSurfaceView(boolean preload) {
        // 此处之前SurfaceView必须设置为VISIBLE
        hasPreload = preload;
        mSurfaceHolder.addCallback(mSurfaceHolderCallback);
        setSurface(mSurfaceHolder.getSurface());
    }

    void release() {
        hasPreload = false;
        mSurface = null;
        if (mSurfaceHolder != null)
            mSurfaceHolder.removeCallback(mSurfaceHolderCallback);
    }

    boolean isReady() {
        return mSurfaceView == null || mSurface != null;
    }

    Surface getSurface() {
        return mSurface;
    }

    SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }

    private final SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (holder != mSurfaceHolder)
                throw new IllegalStateException("holders are different");
            setSurface(holder.getSurface());
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            LogUtils.i("LH/", "surfaceChanged");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mSurfaceCallback != null) {
                mSurfaceCallback.onSurfaceDestroyed();
            }
        }
    };

}
