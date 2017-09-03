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
import tv.ismar.app.widget.OpenView;

public class HeadFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener, View.OnHoverListener{

    private OpenView mHistoryTv;//历史记录
    private OpenView mPersonCenterTv;//个人中心
    private TextView mTimeTv;//时间

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
        initData();
        return view;
    }

    private void findView(View view){
        mHistoryTv = (OpenView) view.findViewById(R.id.guide_title_history_tv);
        mTimeTv = (TextView) view.findViewById(R.id.guide_title_time_tv);
        mPersonCenterTv = (OpenView) view.findViewById(R.id.guide_title_person_center_tv);
    }

    private void initListener(){
        mHistoryTv.setOnClickListener(this);
        mPersonCenterTv.setOnClickListener(this);
        mTimeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction("tv.ismar.daisy.listtest");
                startActivity(intent);
            }
        });
    }

    private void initData(){
        mTimeTv.setText(getNowTime());
    }

    @Override
    public void onClick(View v) {
        PageIntent pageIntent = new PageIntent();
        if(v == mHistoryTv){
            pageIntent.toHistory(getContext());
        } else if(v == mPersonCenterTv){
            pageIntent.toUserCenter(getContext());
        }
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
