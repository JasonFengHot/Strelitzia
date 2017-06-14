package tv.ismar.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;

import tv.ismar.listpage.R;


/**
 * Created by admin on 2017/6/2.
 */

public class FilterConditionGroupView extends LinearLayout implements View.OnHoverListener {

    private TextView filter_condition_group_title;
    private Button filter_condition_group_arrow_left;
    private Button filter_condition_group_arrow_right;
    private View checkedView;
    private List<List<String>> values;
    private String label;
    private boolean isFocus;
    private HorizontalScrollView filter_condition_group_scrollview;
    public RadioGroup filter_condition_radio_group;
    private RadioButton radio;

    public interface OnCheckChange{
        void onCheckChange(View view,boolean isChecked);
    }

    private OnCheckChange onCheckChange;

    public FilterConditionGroupView(Context context,List<List<String>> values,String label,boolean isFocus) {
        super(context);
        this.label = label;
        this.values = values;
        this.isFocus=isFocus;
        initView(context);
        initData(context);

    }


    private void initData(Context context) {
        filter_condition_group_title.setText(label+":");
        int totalWidth=0;
        for (int i = 0; i <values.size() ; i++) {
            radio = (RadioButton) View.inflate(context, R.layout.filter_group_radio_button,null);
            radio.setText(values.get(i).get(1));
            radio.setTag(values.get(i).get(0));
            int width=0;
            if(values.get(i).get(1).length()>2){
                radio.setBackgroundResource(R.drawable.filter_condition_checked_selector2);
                width= RadioGroup.LayoutParams.WRAP_CONTENT;
            }else{
                radio.setBackgroundResource(R.drawable.filter_condition_selector2);
                width=context.getResources().getDimensionPixelOffset(R.dimen.filter_condition_group_item_space_2);
            }

            RadioGroup.LayoutParams params=new RadioGroup.LayoutParams(width, RadioGroup.LayoutParams.MATCH_PARENT);
            if(i!=0)
            params.leftMargin=context.getResources().getDimensionPixelOffset(R.dimen.filter_condition_radio_mr);
            radio.setLayoutParams(params);
            totalWidth+=params.width+params.leftMargin;
            if(i==0){
                radio.setNextFocusLeftId(radio.getId());
                radio.callOnClick();
//                radio.setChecked(true);
//                checkedView=radio;
            }else if(i==values.size()-1){
                radio.setNextFocusRightId(radio.getId());
            }
            radio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    onCheckChange.onCheckChange(buttonView,isChecked);
                    if(checkedView!=null)
                    ((RadioButton) checkedView).setChecked(false);
                    if(isChecked)
                        checkedView=buttonView;
                }
            });
            filter_condition_radio_group.addView(radio);
        }
        for (int i = 0; i < filter_condition_radio_group.getChildCount(); i++) {
            totalWidth+=filter_condition_radio_group.getChildAt(i).getWidth();
            if(i!=0){
                totalWidth+=context.getResources().getDimensionPixelOffset(R.dimen.filter_condition_radio_mr);
            }
        }
        Log.e("radio",totalWidth+"");
        if(totalWidth>context.getResources().getDimensionPixelOffset(R.dimen.filter_condition_group_recycler_w)){
            filter_condition_group_arrow_right.setVisibility(View.VISIBLE);
        }
        filter_condition_group_arrow_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_condition_group_arrow_left.setVisibility(View.VISIBLE);
                filter_condition_group_arrow_right.setVisibility(View.INVISIBLE);
                filter_condition_group_scrollview.pageScroll(View.FOCUS_RIGHT);
            }
        });
        filter_condition_group_arrow_left.setOnClickListener(new OnClickListener() {


            @Override
            public void onClick(View v) {
                filter_condition_group_arrow_right.setVisibility(View.VISIBLE);
                filter_condition_group_arrow_left.setVisibility(View.INVISIBLE);
                filter_condition_group_scrollview.pageScroll(View.FOCUS_LEFT);
            }
        });
        filter_condition_group_arrow_left.setOnHoverListener(this);
        filter_condition_group_arrow_right.setOnHoverListener(this);
    }

    private void initView(Context context) {
        View  conditionGroup= LayoutInflater.from(context).inflate(R.layout.filter_condition_group,this);
        filter_condition_group_title = (TextView) conditionGroup.findViewById(R.id.filter_condition_group_title);
        filter_condition_group_arrow_left = (Button) conditionGroup.findViewById(R.id.filter_condition_group_arrow_left);
        filter_condition_group_arrow_right = (Button) conditionGroup.findViewById(R.id.filter_condition_group_arrow_right);
        filter_condition_group_scrollview = (HorizontalScrollView) conditionGroup.findViewById(R.id.filter_condition_group_scrollview);
        filter_condition_radio_group = (RadioGroup) conditionGroup.findViewById(R.id.filter_condition_radio_group);
    }


    @Override
    public boolean onHover(View v, MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
            v.requestFocus();
            v.requestFocusFromTouch();
        }
        return false;
    }
    public void setOnCheckChangeListener(OnCheckChange onCheckChangeListener){
        onCheckChange=onCheckChangeListener;
    }

}
