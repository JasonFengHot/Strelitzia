package tv.ismar.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
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
    private Rect rect;
	/*add by dragontec for bug 4272 start*/
	private Rect tempRect;
	/*add by dragontec for bug 4272 end*/

    public Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.arg1!=-1){
                if(msg.arg1==0){
                    for (int i = values.size()-1; i >=0 ; i--) {
                        if(filter_condition_radio_group.getChildAt(i).getLocalVisibleRect(rect)){
                            filter_condition_radio_group.getChildAt(i).requestFocus();
                            filter_condition_radio_group.getChildAt(i).requestFocusFromTouch();
                            break;
                        }
                    }
                }else if(msg.arg1==1){
                    for (int i = 0; i<values.size() ; i++) {
                        if(filter_condition_radio_group.getChildAt(i).getLocalVisibleRect(rect)){
                            filter_condition_radio_group.getChildAt(i).requestFocus();
                            filter_condition_radio_group.getChildAt(i).requestFocusFromTouch();
                            break;
                        }
                    }
                }

            }else {
                findViewById(filter_condition_radio_group.getCheckedRadioButtonId()).requestFocus();
                findViewById(filter_condition_radio_group.getCheckedRadioButtonId()).requestFocusFromTouch();
            }
            return false;
        }
    });
    private View left_layer;
    private View right_layer;

    public FilterConditionGroupView(Context context,List<List<String>> values,String label) {
        super(context);
        this.label = label;
        this.values = values;
        initView(context);
        rect=new Rect(filter_condition_radio_group.getLeft(),filter_condition_radio_group.getTop(),filter_condition_radio_group.getLeft()+filter_condition_radio_group.getWidth(),filter_condition_radio_group.getTop()+filter_condition_radio_group.getHeight());
		/*add by dragontec for bug 4272 start*/
		tempRect = new Rect();
		/*add by dragontec for bug 4272 end*/
        initData(context);

    }

	/*add by dragontec for bug 4272 start*/
    private boolean checkRectOverlay(Rect rect1, Rect rect2) {
		if (rect1.contains(rect2) || rect2.contains(rect1) || rect1.contains(rect2.left, rect2.top) || rect1.contains(rect2.right, rect2.top) || rect1.contains(rect2.left, rect2.bottom) || rect1.contains(rect2.right, rect2.bottom)) {
			return true;
		}
		return false;
	}
	/*add by dragontec for bug 4272 end*/

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
            radio.setOnHoverListener(new OnHoverListener() {
                @Override
                public boolean onHover(View v, MotionEvent event) {
                    if(event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
                        if(v.getLocalVisibleRect(rect)) {
                            v.requestFocus();
                            v.requestFocusFromTouch();
                        }
                    }else if(event.getAction()==MotionEvent.ACTION_HOVER_EXIT){
                        v.clearFocus();
                    }
                    return false;
                }
            });
            radio.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus&&canScroll){
                        if(filter_condition_radio_group.getChildAt(0).getLocalVisibleRect(rect)){
							/*modify by dragontec for bug 4272 start*/
							//需要检查左右方向按键与item重叠的问题
							if (filter_condition_radio_group.getChildAt(0).getGlobalVisibleRect(rect) && left_layer.getGlobalVisibleRect(tempRect)) {
								if (checkRectOverlay(rect, tempRect)) {
									filter_condition_group_arrow_left.setVisibility(View.VISIBLE);
								} else {
									filter_condition_group_arrow_left.setVisibility(View.INVISIBLE);
								}
							} else {
								filter_condition_group_arrow_left.setVisibility(View.VISIBLE);
							}
							/*modify by dragontec for bug 4272 end*/
                        }else{
                            filter_condition_group_arrow_left.setVisibility(View.VISIBLE);
                        }
                        if(filter_condition_radio_group.getChildAt(values.size()-1).getLocalVisibleRect(rect)){
							/*modify by dragontec for bug 4272 start*/
							//需要检查左右方向按键与item重叠的问题
							if (filter_condition_radio_group.getChildAt(values.size()-1).getGlobalVisibleRect(rect) && right_layer.getGlobalVisibleRect(tempRect)) {
								if (checkRectOverlay(rect, tempRect)) {
									filter_condition_group_arrow_right.setVisibility(View.VISIBLE);
								} else {
									filter_condition_group_arrow_right.setVisibility(View.INVISIBLE);
								}
							} else {
								filter_condition_group_arrow_right.setVisibility(View.VISIBLE);
							}
							/*modify by dragontec for bug 4272 end*/
                        }else{
                            filter_condition_group_arrow_right.setVisibility(View.VISIBLE);
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
                filter_condition_group_scrollview.pageScroll(View.FOCUS_RIGHT);
                Message msg=new Message();
                msg.arg1=0;
                handler.sendMessageDelayed(msg,300);
            }
        });
        filter_condition_group_arrow_left.setOnClickListener(new OnClickListener() {


            @Override
            public void onClick(View v) {
                filter_condition_group_scrollview.pageScroll(View.FOCUS_LEFT);
                Message msg=new Message();
                msg.arg1=1;
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
                                                                                                         right_layer.setVisibility(View.VISIBLE);
                                                                                                         canScroll=true;
                                                                                                     }else{
                                                                                                            filter_condition_group_arrow_right.setVisibility(View.INVISIBLE);
                                                                                                             RadioButton view = new RadioButton(context);
                                                                                                             RadioGroup.LayoutParams params=new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.MATCH_PARENT);
                                                                                                             view.setLayoutParams(params);
                                                                                                             view.setClickable(false);
                                                                                                             view.setId(R.id.radio+values.size());
                                                                                                             view.setOnFocusChangeListener(new OnFocusChangeListener() {
                                                                                                                 @Override
                                                                                                                 public void onFocusChange(View v, boolean hasFocus) {
                                                                                                                     if(hasFocus)
                                                                                                                     filter_condition_radio_group.getChildAt(values.size()-1).requestFocus();
                                                                                                                 }
                                                                                                             });
                                                                                                         filter_condition_radio_group.addView(view);
                                                                                                         }
                                                                                                 }

                                                                                             });
        left_layer = conditionGroup.findViewById(R.id.left_layer);
        right_layer = conditionGroup.findViewById(R.id.right_layer);
    }


    @Override
    public boolean onHover(View v, MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
            canScroll=true;
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
