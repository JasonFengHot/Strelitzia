package tv.ismar.app.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.ismar.app.R;

/**
 * Created by liucan on 2017/1/5.
 */

public class NoNetConnectDialog extends Dialog implements View.OnClickListener{
    private Button confirmBtn;
    private Button cancelBtn;
    private ModuleMessagePopWindow.ConfirmListener confirmListener;
    private ModuleMessagePopWindow.CancelListener cancleListener;
    public boolean isConfirmClick = false;
    private Context mContext;
    private LinearLayout popup_content;


    public NoNetConnectDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);


        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_layout_style1, null);
        contentView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.transparent));
        confirmBtn = (Button) contentView.findViewById(R.id.popup_btn_confirm);
        cancelBtn = (Button) contentView.findViewById(R.id.popup_btn_cancel);
        popup_content = (LinearLayout) contentView.findViewById(R.id.popup_content);
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        confirmBtn.setOnHoverListener(new View.OnHoverListener() {

            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    v.requestFocus();
                }
                return false;
            }
        });
        cancelBtn.setOnHoverListener(new View.OnHoverListener() {

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
        if (i == R.id.popup_btn_confirm) {
            if (confirmListener != null) {
                isConfirmClick = true;
                confirmListener.confirmClick(v);
            }

        } else if (i == R.id.popup_btn_cancel) {
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
        confirmBtn.setText(text);
    }

    public void setCancelBtn(String text) {
        cancelBtn.setText(text);
    }
    public void setFirstMessage(String message) {
        View textLayout = LayoutInflater.from(mContext).inflate(R.layout.update_msg_text_item, null);
        TextView textContent= (TextView) textLayout.findViewById(R.id.msg_text);
        textContent.setText(message);
        popup_content.addView(textLayout);
    }

    @Override
    public void onBackPressed() {
    }
    public void keyListen(ModuleMessagePopWindow.ConfirmListener confirmListener, ModuleMessagePopWindow.CancelListener cancleListener){
        if (confirmListener == null) {
            confirmBtn.setVisibility(View.GONE);
        }

        if (cancleListener == null) {
            cancelBtn.setVisibility(View.GONE);
        }
        this.confirmListener = confirmListener;
        this.cancleListener = cancleListener;
        isConfirmClick = false;
    }
}
