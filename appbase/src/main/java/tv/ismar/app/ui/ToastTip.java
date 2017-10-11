package tv.ismar.app.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import tv.ismar.app.R;

/**
 * Created by admin on 2017/9/29.
 */

public class ToastTip {

    public static void showToast(Context context, String tip){
        Toast toast=new Toast(context);
        View toastView=View.inflate(context, R.layout.tip_toast,null);
        TextView toast_content= (TextView) toastView.findViewById(R.id.toast_content);
        toast_content.setText(tip);
        toast.setView(toastView);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }
}
