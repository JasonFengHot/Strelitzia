package tv.ismar.app.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.ismar.app.R;

/**
 * Created by huaijie on 9/24/15.
 */
public class ModuleMessagePopWindow extends PopupWindow implements View.OnClickListener {
    private Button confirmBtn;
    private Button cancelBtn;
    private ConfirmListener confirmListener;
    private CancelListener cancleListener;
    public boolean isConfirmClick = false;

    private Context mContext;
    private LinearLayout popup_content;

    public interface CancelListener {
        void cancelClick(View view);
    }

    public interface ConfirmListener {
        void confirmClick(View view);
    }

    public ModuleMessagePopWindow(Context context) {
        mContext = context;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();


        setWidth(screenWidth);
        setHeight(screenHeight);

        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_layout_style1, null);
        TextView popup_title= (TextView) contentView.findViewById(R.id.popup_title);
        confirmBtn = (Button) contentView.findViewById(R.id.popup_btn_confirm);
        cancelBtn = (Button) contentView.findViewById(R.id.popup_btn_cancel);
        popup_content = (LinearLayout) contentView.findViewById(R.id.popup_content);
        popup_title.setText("提示");
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

        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.transparent));

        setFocusable(true);

    }


    public void setBackgroundRes(int resId) {
        setBackgroundDrawable(mContext.getResources().getDrawable(resId));
    }

    public void setMessage(String message) {
        View textLayout = LayoutInflater.from(mContext).inflate(R.layout.update_msg_text_item, null);
        TextView textContent= (TextView) textLayout.findViewById(R.id.msg_text);
        textContent.setText(message);
        popup_content.addView(textLayout);
    }


    public void setConfirmBtn(String text) {
        confirmBtn.setText(text);
    }

    public void setCancelBtn(String text) {
        cancelBtn.setText(text);
    }

    public void setBackground(){
        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.sanzhou_bg));
    }

    public void hideCancelBtn() {
        cancelBtn.setVisibility(View.GONE);
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


    public void showAtLocation(View parent, int gravity, int x, int y, ConfirmListener confirmListener,
                               CancelListener cancleListener) {
        if (confirmListener == null) {
            confirmBtn.setVisibility(View.GONE);
        }

        if (cancleListener == null) {
            cancelBtn.setVisibility(View.GONE);
        }
        this.confirmListener = confirmListener;
        this.cancleListener = cancleListener;
        isConfirmClick = false;
        super.showAtLocation(parent, gravity, x, y);
    }
}
