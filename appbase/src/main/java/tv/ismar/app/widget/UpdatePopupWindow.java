package tv.ismar.app.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;

import java.io.IOException;
import java.util.ArrayList;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.R;

/**
 * Created by huibin on 11/17/16.
 */

public class UpdatePopupWindow extends PopupWindow implements View.OnHoverListener {

    private View tmp;
    private final TextView update_msg1;
    private final TextView update_msg2;
    private final TextView update_msg3;
    private final TextView update_msg4;
    private final Button update_now_btn;
    private final Button update_later_btn;

    public UpdatePopupWindow(final Context context, Bundle bundle) {
        super(null, 0, 0);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();


        setWidth(screenWidth);
        setHeight(screenHeight);

        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_update_layout, null);
        update_msg1 = (TextView) contentView.findViewById(R.id.update_msg1);
        update_msg2 = (TextView) contentView.findViewById(R.id.update_msg2);
        update_msg3 = (TextView) contentView.findViewById(R.id.update_msg3);
        update_msg4 = (TextView) contentView.findViewById(R.id.update_msg4);
        update_now_btn = (Button) contentView.findViewById(R.id.update_now_btn);
        update_later_btn = (Button) contentView.findViewById(R.id.update_later_btn);
        tmp = contentView.findViewById(R.id.tmp);
        update_now_btn.setOnHoverListener(this);
        update_later_btn.setOnHoverListener(this);

        final String path = bundle.getString("path");
        final ArrayList<String> msgs = bundle.getStringArrayList("msgs");
        final Boolean force_upgrade = bundle.getBoolean("force_upgrade");

        if(msgs.size()>0){
            update_msg1.setText(msgs.get(0));
        }
        if(msgs.size()>1){
            update_msg2.setText(msgs.get(1));
        }
        if(msgs.size()>2){
            update_msg3.setText(msgs.get(2));
        }
        if(msgs.size()>3){
            update_msg4.setText(msgs.get(3));
        }


        if (force_upgrade){
            update_later_btn.setVisibility(View.GONE);
            update_now_btn.setNextFocusRightId(update_later_btn.getId());
        }else{
            setBackgroundDrawable(context.getResources().getDrawable(R.drawable.transparent));
        }

        setContentView(contentView);
        setOutsideTouchable(true);
        setFocusable(true);

        update_now_btn.setOnClickListener(new View.OnClickListener() {
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
//                if(sp!=null&&!IsmartvActivator.getInstance().isLogin()) {
//                    SharedPreferences.Editor editor = sp.edit();
//                    editor.putBoolean("fristopne", true).commit();
//                }
            }
        });
        update_later_btn.setOnClickListener(new View.OnClickListener() {
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
