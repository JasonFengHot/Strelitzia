package tv.ismar.app.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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


import com.blankj.utilcode.util.AppUtils;

import java.io.IOException;
import java.util.ArrayList;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.R;

import static android.widget.RelativeLayout.CENTER_IN_PARENT;

/**
 * Created by huibin on 11/17/16.
 */

public class UpdatePopupWindow extends PopupWindow implements View.OnHoverListener {

    private View tmp;

    public UpdatePopupWindow(final Context context, Bundle bundle) {
        super(null, 0, 0);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();


        setWidth(screenWidth);
        setHeight(screenHeight);

        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_layout_style1, null);
        TextView popup_title=(TextView)contentView.findViewById(R.id.popup_title);
        Button popup_btn_confirm = (Button) contentView.findViewById(R.id.popup_btn_confirm);
        Button popup_btn_cancel = (Button) contentView.findViewById(R.id.popup_btn_cancel);
        LinearLayout popup_title_view=(LinearLayout)contentView.findViewById(R.id.popup_title_view);
        LinearLayout popup_btns=(LinearLayout)contentView.findViewById(R.id.popup_btns);
        popup_title.setText("升级提示");
        popup_btn_confirm.setText("现在升级");
        popup_btn_cancel.setText("稍后升级");
        popup_btn_confirm.setOnHoverListener(this);
        popup_btn_cancel.setOnHoverListener(this);
        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.transparent));
        tmp = contentView.findViewById(R.id.tmp);


        LinearLayout popup_content = (LinearLayout) contentView.findViewById(R.id.popup_content);


        final String path = bundle.getString("path");

        final ArrayList<String> msgs = bundle.getStringArrayList("msgs");
        final Boolean force_upgrade = bundle.getBoolean("force_upgrade");

        if(msgs.size()>2){
            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) popup_title_view.getLayoutParams();
            params1.topMargin=130;
            popup_title_view.setLayoutParams(params1);
            RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) popup_btns.getLayoutParams();
            params2.bottomMargin=190;
            popup_btns.setLayoutParams(params2);
        }
        for (String msg : msgs) {
            View textLayout = LayoutInflater.from(context).inflate(R.layout.update_msg_text_item, null);
            TextView textView = (TextView) textLayout.findViewById(R.id.msg_text);
            textView.setText(msg);
            popup_content.addView(textLayout);
        }


        if (force_upgrade){
            popup_btn_cancel.setVisibility(View.GONE);
            popup_btn_confirm.setNextFocusRightId(popup_btn_confirm.getId());
        }

        setContentView(contentView);

        setFocusable(true);

        popup_btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!force_upgrade) {
                    dismiss();
                }
                try {
                    String[] args2 = {"chmod", "604", path};
                    Runtime.getRuntime().exec(args2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                AppUtils.installApp(path, "tv.ismar.daisy.provider");

                SharedPreferences sp=context.getSharedPreferences("Daisy",0);
                if(sp!=null&&!IsmartvActivator.getInstance().isLogin()) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("fristopne", true).commit();
                }
            }
        });
        popup_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocus();
                v.requestFocusFromTouch();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                tmp.requestFocus();
                break;
        }
        return true;
    }
}
