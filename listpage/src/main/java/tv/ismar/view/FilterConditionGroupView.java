package tv.ismar.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;

import tv.ismar.listpage.R;


/**
 * Created by admin on 2017/6/2.
 * 筛选浮层全部条件的自定义view
 */

public class FilterConditionGroupView extends LinearLayout implements View.OnHoverListener {

    private TextView filter_condition_group_title;
    private Button filter_condition_group_arrow_left;
    private Button filter_condition_group_arrow_right;
    private List<List<String>> values;
    private String label;
    private HorizontalScrollView filter_condition_group_scrollview;
    public RadioGroup filter_condition_radio_group;
    private RadioButton radio;
    private int totalwidth;
    private int rightLimit=0;
    private boolean canScroll=false;

    public Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.arg1!=-1){
                filter_condition_radio_group.getChildAt(msg.arg1).requestFocus();
                filter_condition_radio_group.getChildAt(msg.arg1).requestFocusFromTouch();
            }else {
                findViewById(filter_condition_radio_group.getCheckedRadioButtonId()).requestFocus();
                findViewById(filter_condition_radio_group.getCheckedRadioButtonId()).requestFocusFromTouch();
            }
            return false;
        }
    });
    private View left_layer;
    private View right_layer;
    public void setNextUpView(View nextUpView) {
        this.nextUpView = nextUpView;
    }

    public void setNextDownView(View nextDownView) {
        this.nextDownView = nextDownView;
    }

    private View nextUpView;
    private View nextDownView;

    public FilterConditionGroupView(Context context,List<List<String>> values,String label) {
        super(context);
        this.label = label;
        this.values = values;
        initView(context);
        initData(context);

    }


    private void initData(final Context context) {
        filter_condition_group_title.setText(label+":");
        for (int i = 0; i <values.size() ; i++) {
            radio = (RadioButton) View.inflate(context, R.layout.filter_group_radio_button,null);
            radio.setId(R.id.radio+i);
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
            if(i==0){
                radio.setNextFocusLeftId(radio.getId());
                radio.setChecked(true);
            }else if(i==values.size()-1){
                radio.setNextFocusRightId(radio.getId());
            }
            final int finalI = i;
            radio.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.requestFocus();
                    v.requestFocusFromTouch();
                }
            });
            radio.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == 20&&nextDownView!=null) {
                            ((FilterConditionGroupView) nextDownView).handler.sendEmptyMessage(0);
                            return true;
                        } else if (keyCode == 19&&nextUpView!=null) {
                            ((FilterConditionGroupView) nextUpView).handler.sendEmptyMessage(0);
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }

            });
            radio.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(filter_condition_group_scrollview.getScrollX()>0){
                        filter_condition_group_arrow_left.setVisibility(View.VISIBLE);
                        left_layer.setVisibility(View.VISIBLE);
                    }else{
                        filter_condition_group_arrow_left.setVisibility(View.INVISIBLE);
                    }
                    if(canScroll) {
                        if (finalI == values.size() - 1) {
                            filter_condition_group_arrow_right.setVisibility(View.INVISIBLE);
                            rightLimit = filter_condition_group_scrollview.getScrollX();
                        } else {
                            if (rightLimit != 0 && filter_condition_group_scrollview.getScrollX() == rightLimit) {
                                filter_condition_group_arrow_right.setVisibility(View.INVISIBLE);
                            } else {
                                filter_condition_group_arrow_right.setVisibility(View.VISIBLE);
                                right_layer.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    if(hasFocus&&canScroll){
                        int[] location=new int[2];
                        v.getLocationOnScreen(location);
                        if(location[0]-context.getResources().getDimensionPixelOffset(R.dimen.filter_condition_radio_mr)<left_layer.getLeft()+context.getResources().getDimensionPixelOffset(R.dimen.filter_condition_layout_filter_conditions_pl)){
                            left_layer.setVisibility(View.INVISIBLE);
                        }else{
                            if(filter_condition_group_arrow_left.getVisibility()==View.VISIBLE)
                                left_layer.setVisibility(View.VISIBLE);
                        }
                        if(location[0]+v.getWidth()+context.getResources().getDimensionPixelOffset(R.dimen.filter_condition_radio_mr)>right_layer.getRight()+context.getResources().getDimensionPixelOffset(R.dimen.filter_condition_layout_filter_conditions_pl)){
                            right_layer.setVisibility(View.INVISIBLE);
                        }else{
                            if(filter_condition_group_arrow_right.getVisibility()==View.VISIBLE)
                                right_layer.setVisibility(View.VISIBLE);
                        }
                    }
                }

            });
            filter_condition_radio_group.addView(radio);
        }
        filter_condition_group_arrow_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_condition_group_arrow_left.setVisibility(View.VISIBLE);
                filter_condition_group_arrow_right.setVisibility(View.INVISIBLE);
                filter_condition_group_scrollview.pageScroll(View.FOCUS_RIGHT);
                Message msg=new Message();
                msg.arg1=values.size()-1;
                handler.sendMessageDelayed(msg,300);
            }
        });
        filter_condition_group_arrow_left.setOnClickListener(new OnClickListener() {


            @Override
            public void onClick(View v) {
                filter_condition_group_arrow_right.setVisibility(View.VISIBLE);
                filter_condition_group_arrow_left.setVisibility(View.INVISIBLE);
                filter_condition_group_scrollview.pageScroll(View.FOCUS_LEFT);
                Message msg=new Message();
                msg.arg1=0;
                handler.sendMessageDelayed(msg,300);
            }
        });
        filter_condition_group_arrow_left.setOnHoverListener(this);
        filter_condition_group_arrow_right.setOnHoverListener(this);
    }

    private void initView(final Context context) {
        View  conditionGroup= LayoutInflater.from(context).inflate(R.layout.filter_condition_group,this);
        filter_condition_group_title = (TextView) conditionGroup.findViewById(R.id.filter_condition_group_title);
        filter_condition_group_arrow_left = (Button) conditionGroup.findViewById(R.id.filter_condition_group_arrow_left);
        filter_condition_group_arrow_right = (Button) conditionGroup.findViewById(R.id.filter_condition_group_arrow_right);
        filter_condition_group_scrollview = (HorizontalScrollView) conditionGroup.findViewById(R.id.filter_condition_group_scrollview);
        filter_condition_radio_group = (RadioGroup) conditionGroup.findViewById(R.id.filter_condition_radio_group);
        filter_condition_radio_group.getViewTreeObserver().addOnGlobalLayoutListener(new

                                                                                             ViewTreeObserver.OnGlobalLayoutListener(){

                                                                                                 @Override
                                                                                                 public void onGlobalLayout() {
                                                                                                     totalwidth=0;
                                                                                                     for (int i = 0; i <filter_condition_radio_group.getChildCount() ; i++) {
                                                                                                         totalwidth +=filter_condition_radio_group.getChildAt(i).getWidth();
                                                                                                         if(i!=0){
                                                                                                             totalwidth+=context.getResources().getDimensionPixelOffset(R.dimen.filter_condition_radio_mr);
                                                                                                         }
                                                                                                     }
                                                                                                     if(totalwidth>context.getResources().getDimensionPixelOffset(R.dimen.filter_condition_group_recycler_w)){
                                                                                                         filter_condition_group_arrow_right.setVisibility(View.VISIBLE);
                                                                                                         right_layer.setVisibility(View.VISIBLE);
                                                                                                         canScroll=true;
                                                                                                     }
                                                                                                 }

                                                                                             });
        left_layer = conditionGroup.findViewById(R.id.left_layer);
        right_layer = conditionGroup.findViewById(R.id.right_layer);
    }


    @Override
    public boolean onHover(View v, MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
            v.setFocusable(true);
            v.requestFocus();
            v.requestFocusFromTouch();
        }
        if(event.getAction()==MotionEvent.ACTION_HOVER_EXIT){
            v.setFocusable(false);
        }
        return false;
    }


    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if(gainFocus){
            filter_condition_group_scrollview.requestChildFocus(filter_condition_radio_group.getChildAt(0),filter_condition_group_scrollview);
        }
    }
}
