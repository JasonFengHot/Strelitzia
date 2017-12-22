package tv.ismar.app.ui;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Vector;

import tv.ismar.app.R;

/**
 * Created by admin on 2017/9/29.
 */

public class ToastTip {
/*add by dragontec for bug 4474 start*/
    private static final int ToastShowDuration = 3500;
    private static Handler mHandler = new Handler();
    private static Vector<String> mShowingToastStrList = new Vector<>();
/*add by dragontec for bug 4474 start*/

    synchronized public static void showToast(Context context, String tip){
/*add by dragontec for bug 4474 start*/
        if (mShowingToastStrList.contains(tip)) {
            //the toast(tip string) is showing, do nothing
            return;
        }
/*add by dragontec for bug 4474 end*/
        Toast toast=new Toast(context);
        View toastView=View.inflate(context, R.layout.tip_toast,null);
        TextView toast_content= (TextView) toastView.findViewById(R.id.toast_content);
        toast_content.setText(tip);
        toast.setView(toastView);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
/*add by dragontec for bug 4474 start*/
        mShowingToastStrList.add(tip);
        ToastDismissRunnable runnable = new ToastDismissRunnable(tip);
        mHandler.postDelayed(runnable, ToastShowDuration);
/*add by dragontec for bug 4474 end*/
    }

/*add by dragontec for bug 4474 start*/
    static private class ToastDismissRunnable implements Runnable {
        private String tip;

        public ToastDismissRunnable(String tip) {
            this.tip = tip;
        }

        @Override
        public void run() {
            if (mHandler != null) {
                mHandler.removeCallbacks(this);
            }
            if (tip != null) {
                mShowingToastStrList.remove(tip);
            }
        }
    }
/*add by dragontec for bug 4474 end*/
}
