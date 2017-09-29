package tv.ismar.app.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import tv.ismar.app.R;

/**
 * Created by liucan on 2017/1/5.
 */

public class NoNetConnectDialog extends Dialog implements View.OnClickListener{
    private Button popup_tip_confirm;
    private Button popup_tip_cancel;
    private ModuleMessagePopWindow.ConfirmListener confirmListener;
    private ModuleMessagePopWindow.CancelListener cancleListener;
    public boolean isConfirmClick = false;
    private Context mContext;
    private final TextView popup_tip_msg;


    public NoNetConnectDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_tip_layout, null);
        popup_tip_confirm = (Button) contentView.findViewById(R.id.popup_tip_confirm);
        popup_tip_cancel = (Button) contentView.findViewById(R.id.popup_tip_cancel);
        popup_tip_msg = (TextView) contentView.findViewById(R.id.popup_tip_msg);
        popup_tip_msg.setText(R.string.no_connectNet);
        popup_tip_confirm.setOnClickListener(this);
        popup_tip_cancel.setOnClickListener(this);
        popup_tip_confirm.setOnHoverListener(new View.OnHoverListener() {

            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    v.requestFocus();
                }
                return false;
            }
        });
        popup_tip_cancel.setOnHoverListener(new View.OnHoverListener() {

            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    v.requestFocus();
                }
                return false;
            }
        });

        setContentView(contentView);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.popup_tip_confirm) {
            if (confirmListener != null) {
                isConfirmClick = true;
                confirmListener.confirmClick(v);
            }

        } else if (i == R.id.popup_tip_cancel) {
            if (cancleListener != null) {
                isConfirmClick = false;
                cancleListener.cancelClick(v);
            }

        }
    }

    public interface CancelListener {
        void cancelClick(View view);
    }

    public interface ConfirmListener {
        void confirmClick(View view);
    }

    public void setConfirmBtn(String text) {
        popup_tip_confirm.setText(text);
    }

    public void setCancelBtn(String text) {
        popup_tip_cancel.setText(text);
    }

    @Override
    public void onBackPressed() {
    }
    public void keyListen(ModuleMessagePopWindow.ConfirmListener confirmListener, ModuleMessagePopWindow.CancelListener cancleListener){
        if (confirmListener == null) {
            popup_tip_confirm.setVisibility(View.GONE);
        }

        if (cancleListener == null) {
            popup_tip_cancel.setVisibility(View.GONE);
        }
        this.confirmListener = confirmListener;
        this.cancleListener = cancleListener;
        isConfirmClick = false;
    }
}
