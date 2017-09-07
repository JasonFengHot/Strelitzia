package tv.ismar.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cn.ismartv.truetime.TrueTime;
import tv.ismar.app.R;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.widget.TelescopicWrap;

public class HeadFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener, View.OnHoverListener{

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_head, container, false);
        findView(view);
        initListener();
        return view;
    }

    private void findView(View view){
    }

    private void initListener(){
    }


    @Override
    public void onClick(View v) {
    }

    /*获取当前时间*/
    private String getNowTime(){
        Date now = TrueTime.now();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return dateFormat.format(now);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        return false;
    }
}
