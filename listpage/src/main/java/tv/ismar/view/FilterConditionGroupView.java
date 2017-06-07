package tv.ismar.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

import tv.ismar.adapter.FilterConditionAdapter;
import tv.ismar.app.ui.adapter.OnItemClickListener;
import tv.ismar.listpage.R;


/**
 * Created by admin on 2017/6/2.
 */

public class FilterConditionGroupView extends LinearLayout implements View.OnHoverListener {

    private TextView filter_condition_group_title;
    private Button filter_condition_group_arrow_left;
    private Button filter_condition_group_arrow_right;
    private RecyclerView filter_condition_group_recycler;
    private View checkedView;
    private List<List<String>> values;
    private String label;
    public boolean isHover;

    public interface OnCheckChange{
        void onCheckChange(View view,boolean isChecked);
    }

    private OnCheckChange onCheckChange;

    public FilterConditionGroupView(Context context,List<List<String>> values,String label) {
        super(context);
        this.label = label;
        this.values = values;
        initView(context);
        initData(context);

    }


    private void initData(Context context) {
        filter_condition_group_title.setText(label);
        FilterConditionAdapter filterConditionAdapter=new FilterConditionAdapter(context,values);
        filter_condition_group_recycler.setAdapter(filterConditionAdapter);
        filterConditionAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ((RadioButton)view.findViewById(R.id.radio)).setChecked(true);
                if(onCheckChange!=null){
                    onCheckChange.onCheckChange(view,true);
                }
                if(checkedView!=null){
                    ((RadioButton)checkedView.findViewById(R.id.radio)).setChecked(false);
                    if(onCheckChange!=null){
                        onCheckChange.onCheckChange(view,false);
                    }
                }
                checkedView = view;
            }
        });
        filter_condition_group_arrow_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isHover){
                    filter_condition_group_recycler.scrollToPosition(values.size()-1);
                }else {
                    filter_condition_group_recycler.smoothScrollBy(filter_condition_group_recycler.getChildAt(0).getWidth(), 0);
                }
            }
        });
        filter_condition_group_arrow_left.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isHover) {
                    filter_condition_group_recycler.smoothScrollToPosition(0);
                }else {
                    filter_condition_group_recycler.smoothScrollBy(-filter_condition_group_recycler.getChildAt(0).getWidth(), 0);
                }
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
        filter_condition_group_recycler = (RecyclerView) conditionGroup.findViewById(R.id.filter_condition_group_recycler);
        filter_condition_group_recycler.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
    }


    @Override
    public boolean onHover(View v, MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
            v.requestFocus();
            v.requestFocusFromTouch();
            isHover=true;
        }
        return false;
    }
    public void setOnCheckChangeListener(OnCheckChange onCheckChangeListener){
        onCheckChange=onCheckChangeListener;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("onkeydown",keyCode+"view");
        return super.onKeyDown(keyCode, event);
    }
}
