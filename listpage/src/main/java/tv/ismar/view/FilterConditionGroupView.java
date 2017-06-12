package tv.ismar.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import tv.ismar.app.widget.MyRecyclerView;
import tv.ismar.listpage.R;


/**
 * Created by admin on 2017/6/2.
 */

public class FilterConditionGroupView extends LinearLayout implements View.OnHoverListener, View.OnKeyListener {

    private TextView filter_condition_group_title;
    private Button filter_condition_group_arrow_left;
    private Button filter_condition_group_arrow_right;
    private MyRecyclerView filter_condition_group_recycler;
    private View checkedView;
    private List<List<String>> values;
    private String label;
    public boolean isHover;
    private boolean isFirst=true;
    public Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            filter_condition_group_recycler.getChildAt(msg.arg1).requestFocus();
            filter_condition_group_recycler.getChildAt(msg.arg1).requestFocusFromTouch();
            return false;
        }
    });

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        isHover=false;
        return false;
    }

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
        filter_condition_group_title.setText(label+":");
        FilterConditionAdapter filterConditionAdapter=new FilterConditionAdapter(context,values);
        filter_condition_group_recycler.setAdapter(filterConditionAdapter);
        filterConditionAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(checkedView==view){
                    return;
                }
                ((RadioButton)view.findViewById(R.id.radio)).setChecked(true);
                if(onCheckChange!=null){
                    onCheckChange.onCheckChange(view,true);
                }
                if(checkedView!=null){
                    ((RadioButton)checkedView.findViewById(R.id.radio)).setChecked(false);
                    if(onCheckChange!=null){
                        onCheckChange.onCheckChange(checkedView,false);
                    }
                }
                checkedView = view;
            }
        });
        filter_condition_group_arrow_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_condition_group_arrow_left.setVisibility(View.VISIBLE);
                if(isHover){
                    filter_condition_group_recycler.scrollToPosition(values.size()-1);
                    filter_condition_group_arrow_right.setVisibility(View.INVISIBLE);
                    Message message=new Message();
                    message.arg1=filter_condition_group_recycler.getChildCount()-1;
                    handler.sendMessageDelayed(message,300);
                }else {
                    int position=filter_condition_group_recycler.getChildPosition(filter_condition_group_recycler.getChildAt(0));
                    if(position+filter_condition_group_recycler.getChildCount()>=values.size()-1){
                        filter_condition_group_arrow_right.setVisibility(View.INVISIBLE);
                        Message message=new Message();
                        message.arg1=filter_condition_group_recycler.getChildCount()-1;
                        handler.sendMessageDelayed(message,300);
                    }
                    filter_condition_group_recycler.scrollToPosition(filter_condition_group_recycler.getChildCount()+position>=values.size()-1?values.size()-1:filter_condition_group_recycler.getChildCount()+position);
                }
            }
        });
        filter_condition_group_arrow_left.setOnClickListener(new OnClickListener() {


            @Override
            public void onClick(View v) {
                filter_condition_group_arrow_right.setVisibility(View.VISIBLE);
                if(isHover) {
                    filter_condition_group_recycler.scrollToPosition(0);
                    filter_condition_group_arrow_left.setVisibility(View.INVISIBLE);
                    Message message=new Message();
                    message.arg1=0;
                    handler.sendMessageDelayed(message,300);
                }else {
                    int position=filter_condition_group_recycler.getChildPosition(filter_condition_group_recycler.getChildAt(0));
                    filter_condition_group_recycler.scrollToPosition(position-1>=0?position-1:0);
                    if(position-1<=0){
                        filter_condition_group_arrow_left.setVisibility(View.INVISIBLE);
                        Message message=new Message();
                        message.arg1=0;
                        handler.sendMessageDelayed(message,300);
                    }
                }
            }
        });
        filter_condition_group_arrow_left.setOnHoverListener(this);
        filter_condition_group_arrow_right.setOnHoverListener(this);
        filter_condition_group_recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(isFirst) {
                    Log.e("onscroll",recyclerView.getChildCount()+"&"+values.size());
                    if (recyclerView.getChildCount() < values.size()) {
                        filter_condition_group_arrow_right.setVisibility(View.VISIBLE);
                    } else {
                        filter_condition_group_arrow_right.setVisibility(View.INVISIBLE);
                    }
                    isFirst=false;
                }
            }
        });
    }

    private void initView(Context context) {
        View  conditionGroup= LayoutInflater.from(context).inflate(R.layout.filter_condition_group,this);
        filter_condition_group_title = (TextView) conditionGroup.findViewById(R.id.filter_condition_group_title);
        filter_condition_group_arrow_left = (Button) conditionGroup.findViewById(R.id.filter_condition_group_arrow_left);
        filter_condition_group_arrow_right = (Button) conditionGroup.findViewById(R.id.filter_condition_group_arrow_right);
        filter_condition_group_recycler = (MyRecyclerView) conditionGroup.findViewById(R.id.filter_condition_group_recycler);
        filter_condition_group_recycler.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
        filter_condition_group_arrow_left.setOnKeyListener(this);
        filter_condition_group_arrow_right.setOnKeyListener(this);
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
}
