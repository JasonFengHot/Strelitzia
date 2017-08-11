package tv.ismar.app.widget;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.R;

/**
 * Created by liucan on 2017/5/11.
 */

public class Login_hint_dialog extends PopupWindow implements View.OnClickListener{
    private Button goLogin;
    private Button cancel;
    private Context mContext;
    public Login_hint_dialog(Context context) {
        super(context);
        mContext=context;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();
        setWidth(screenWidth);
        setHeight(screenHeight);

        View contentView = LayoutInflater.from(context).inflate(R.layout.login_hint_dialog, null);
        //   contentView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.pop_bg_drawable));
        cancel= (Button) contentView.findViewById(R.id.cancel);
        goLogin= (Button) contentView.findViewById(R.id.go_login);
        cancel.setOnClickListener(this);
        goLogin.setOnClickListener(this);
        goLogin.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    v.requestFocus();
                }
                return false;
            }
        });
        cancel.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    v.requestFocus();
                }
                return false;
            }
        });
        goLogin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    goLogin.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimensionPixelOffset(R.dimen.item_detail_detail_title_textSize));
                    goLogin.setTextColor(mContext.getResources().getColor(R.color.login_hint_focus));
                }else{
                    goLogin.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimensionPixelOffset(R.dimen.text_size_48sp));
                    goLogin.setTextColor(mContext.getResources().getColor(R.color.login_hint_nomarl));
                }
            }
        });
        cancel.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    cancel.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimensionPixelOffset(R.dimen.item_detail_detail_title_textSize));
                    cancel.setTextColor(mContext.getResources().getColor(R.color.login_hint_focus));
                }else{
                    cancel.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimensionPixelOffset(R.dimen.text_size_48sp));
                    cancel.setTextColor(mContext.getResources().getColor(R.color.login_hint_nomarl));
                }
            }
        });

        RelativeLayout relativeLayout = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams layoutParams;
        layoutParams = new RelativeLayout.LayoutParams(mContext.getResources().getDimensionPixelSize(R.dimen.hint_1014),mContext.getResources().getDimensionPixelOffset(R.dimen.hint_600));
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        relativeLayout.addView(contentView, layoutParams);
        setContentView(relativeLayout);
        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.pop_bg_drawable));
        setFocusable(true);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.go_login){
            BaseActivity.goLogin=true;
            Intent intent = new Intent();
            intent.setAction("tv.ismar.daisy.usercenter");
            intent.putExtra("flag","login");
            mContext.startActivity(intent);
            dismiss();
        }else{
            dismiss();
        }
    }
}
