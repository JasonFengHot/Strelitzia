package tv.ismar.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import tv.ismar.app.BaseActivity;
import tv.ismar.listpage.R;

/**
 * Created by admin on 2017/5/17.
 */

public class ListPageActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener{

    private LinearLayout list_tab;
    private LinearLayout list_filter_condition;
    private RecyclerView list_recyclerview;
    private String[] filter_type={"年份:","类型:","国家:","演员:"};
    private String[] filter_type1={"2017","2016","2015","2014","2013","2012","更早"};
    private String[] filter_type2={"电影","电视剧","综艺娱乐","体育","纪录片","动漫","vip专享","音乐"};
    private String[] filter_type3={"大陆","港台","欧美","日本","韩国","其他"};
    private String[] filter_type4={"成龙","范冰冰","孙俪","赵薇"};
    private int[] count={7,8,6,4};
    private LinearLayout checked;
    private View filter_condition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listpage);
        initView();
        initData();
    }


    private void initView() {
        list_tab = (LinearLayout) findViewById(R.id.list_tab);
        list_filter_condition = (LinearLayout) findViewById(R.id.list_filter_condition);
        list_recyclerview = (RecyclerView) findViewById(R.id.list_recyclerview);
        checked = (LinearLayout) findViewById(R.id.checked_type);
    }


    private void initData() {
        for (int i = 0; i <20 ; i++) {
            View view=View.inflate(this,R.layout.item_listtab,null);
            ((Button)view.findViewById(R.id.list_tab_title)).setText("标题"+i);
            list_tab.addView(view);
        }
        for (int i = 0; i <4; i++) {
            filter_condition = View.inflate(this, R.layout.item_filter_condition,null);
            TextView filter_condition_type = (TextView) filter_condition.findViewById(R.id.filter_condition_type);
            RadioGroup filter_condition_group = (RadioGroup) filter_condition.findViewById(R.id.filter_condition_group);
            filter_condition_type.setText(filter_type[i]);
            for (int j = 0; j <count[i] ; j++) {
                RadioButton radioBtn= (RadioButton) View.inflate(this,R.layout.item_filter_condition_radiobtn,null);
                if(i==0) {
                    radioBtn.setText(filter_type1[j]);
                }else if(i==1){
                    radioBtn.setText(filter_type2[j]);
                }else if(i==2){
                    radioBtn.setText(filter_type3[j]);
                }else{
                    radioBtn.setText(filter_type4[j]);
                }
                radioBtn.setOnCheckedChangeListener(this);
                radioBtn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(!hasFocus){
                        }
                    }
                });
                filter_condition_group.addView(radioBtn);
            }
            filter_condition.setTop(25);
            list_filter_condition.addView(filter_condition);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            buttonView.setTextColor(Color.RED);
            TextView checkdType=new TextView(this);
            checkdType.setPadding(10,0,0,10);
            checkdType.setTextSize(32);
            checkdType.setTextColor(Color.BLACK);
            checkdType.setText(buttonView.getText());
            checkdType.setTag(buttonView.getText());
            checked.addView(checkdType);
            checked.setVisibility(View.VISIBLE);
        }else{
            buttonView.setTextColor(Color.BLACK);
            for (int i = 1; i <checked.getChildCount() ; i++) {
                if(checked.getChildAt(i).getTag().equals(buttonView.getText())){
                    checked.removeView(checked.getChildAt(i));
                }
            }
            if(checked.getChildCount()==1){
                checked.setVisibility(View.GONE);
            }
        }
    }

}
