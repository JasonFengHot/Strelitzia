package tv.ismar.app.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.utils.AppUtils;

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

        int height = (int) (context.getResources().getDimension(R.dimen.app_update_bg_width));
        int width = (int) (context.getResources().getDimension(R.dimen.app_update_bg_height));

        setWidth(screenWidth);
        setHeight(screenHeight);

        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_update, null);
        Button updateNow = (Button) contentView.findViewById(R.id.update_now_bt);
        Button updateLater = (Button) contentView.findViewById(R.id.update_later_bt);
        updateNow.setOnHoverListener(this);
        updateLater.setOnHoverListener(this);

        tmp = contentView.findViewById(R.id.tmp);


        LinearLayout updateMsgLayout = (LinearLayout) contentView.findViewById(R.id.update_msg_layout);


        final String path = bundle.getString("path");

        final ArrayList<String> msgs = bundle.getStringArrayList("msgs");
        final Boolean force_upgrade = bundle.getBoolean("force_upgrade");


        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) (context.getResources().getDimension(R.dimen.app_update_content_margin_left));
        layoutParams.topMargin = (int) (context.getResources().getDimension(R.dimen.app_update_line_margin_));

        for (String msg : msgs) {
            View textLayout = LayoutInflater.from(context).inflate(R.layout.update_msg_text_item, null);
            TextView textView = (TextView) textLayout.findViewById(R.id.update_msg_text);
            textView.setText(msg);
            updateMsgLayout.addView(textLayout);
        }

        RelativeLayout relativeLayout = new RelativeLayout(context);
        RelativeLayout.LayoutParams contentLayoutParams = new RelativeLayout.LayoutParams(width, height);
        contentLayoutParams.addRule(CENTER_IN_PARENT);
        if (force_upgrade){
            relativeLayout.setBackground(contentView.getResources().getDrawable(R.drawable.pop_bg_drawable));
            updateLater.setVisibility(View.GONE);
            updateNow.setNextFocusRightId(updateNow.getId());
            RelativeLayout.LayoutParams l = new RelativeLayout.LayoutParams(
                    context.getResources().getDimensionPixelSize(R.dimen.update_confirm_button_width),
                    context.getResources().getDimensionPixelSize(R.dimen.update_confirm_button_height)
                    );
            l.addRule(CENTER_IN_PARENT);
            updateNow.setLayoutParams(l);

        }else {

            setBackgroundDrawable(contentView.getResources().getDrawable(R.drawable.pop_bg_drawable));
        }

        relativeLayout.addView(contentView, contentLayoutParams);
        setContentView(relativeLayout);

        setFocusable(true);

        updateNow.setOnClickListener(new View.OnClickListener() {
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
                AppUtils.installApp(context, path);

                SharedPreferences sp=context.getSharedPreferences("Daisy",0);
                if(sp!=null&&!IsmartvActivator.getInstance().isLogin()) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("fristopne", true).commit();
                }
            }
        });
        updateLater.setOnClickListener(new View.OnClickListener() {
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
