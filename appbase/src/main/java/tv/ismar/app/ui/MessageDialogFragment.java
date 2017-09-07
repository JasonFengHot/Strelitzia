package tv.ismar.app.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.ismar.app.R;
import tv.ismar.library.exception.ExceptionUtils;


/**
 * Created by huaijie on 10/15/15.
 */
public class MessageDialogFragment extends PopupWindow implements View.OnClickListener {
    private Button confirmBtn;
    private Button cancelBtn;
    private ConfirmListener confirmListener;
    private CancelListener cancleListener;

    private String mFirstLineMessage;
    private String mSecondLineMessage;

    private Context mContext;
    private final LinearLayout popup_content;

    public interface CancelListener {
        void cancelClick(View view);
    }

    public interface ConfirmListener {
        void confirmClick(View view);
    }


    public MessageDialogFragment(Context context, String line1Message, String line2Message) {
        mFirstLineMessage = line1Message;
        mSecondLineMessage = line2Message;
        mContext = context;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();


        setWidth(screenWidth);
        setHeight(screenHeight);
        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_layout_style1, null);
        confirmBtn = (Button) contentView.findViewById(R.id.popup_btn_confirm);
        cancelBtn = (Button) contentView.findViewById(R.id.popup_btn_cancel);
        popup_content = (LinearLayout) contentView.findViewById(R.id.popup_content);
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        confirmBtn.setOnHoverListener(new OnHoverListener() {
			
			@Override
			public boolean onHover(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE){
					v.requestFocus();
				}
				return false;
			}
		});
        cancelBtn.setOnHoverListener(new OnHoverListener() {

			@Override
			public boolean onHover(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
						|| event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
					v.requestFocus();
				}
				return false;
			}
		});
        View textLayout = LayoutInflater.from(mContext).inflate(R.layout.update_msg_text_item, null);
        TextView textContent= (TextView) textLayout.findViewById(R.id.msg_text);
        textContent.setText(mFirstLineMessage);
        popup_content.addView(textLayout);

        if (!TextUtils.isEmpty(mSecondLineMessage)) {
            View textLayout2 = LayoutInflater.from(mContext).inflate(R.layout.update_msg_text_item, null);
            TextView textContent2= (TextView) textLayout2.findViewById(R.id.msg_text);
            textContent2.setText(mSecondLineMessage);
            popup_content.addView(textLayout2);
        }

        setContentView(contentView);
        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.transparent));
        setFocusable(true);
        

    }

    public void setButtonText(String btn1,String btn2){
    	confirmBtn.setText(btn1);
    	cancelBtn.setText(btn2);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.popup_btn_confirm) {
            if (confirmListener != null) {
                confirmListener.confirmClick(v);
            }

        } else if (i == R.id.popup_btn_cancel) {
            if (cancleListener != null) {
                cancleListener.cancelClick(v);
            }

        }
    }


    public void showAtLocation(View parent, int gravity, ConfirmListener confirmListener,
                               CancelListener cancleListener) {
        try {
            if (confirmListener == null) {
                confirmBtn.setVisibility(View.GONE);
            }

            if (cancleListener == null) {
                cancelBtn.setVisibility(View.GONE);
            }
            this.confirmListener = confirmListener;
            this.cancleListener = cancleListener;
            super.showAtLocation(parent, gravity, 0, 0);
        }catch (Exception e){
            ExceptionUtils.sendProgramError(e);
        }
    }
}
