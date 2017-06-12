package tv.ismar.channel;


import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.adapter.FilterPosterAdapter;
import tv.ismar.adapter.SpaceItemDecoration;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemList;
import tv.ismar.app.models.FilterConditions;
import tv.ismar.app.ui.adapter.OnItemClickListener;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.app.widget.MyRecyclerView;
import tv.ismar.listpage.R;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.view.FilterConditionGroupView;

/**
 * Created by zhangjiqiang on 15-6-18.
 */
public class FilterActivity extends BaseActivity implements View.OnClickListener, View.OnHoverListener, View.OnFocusChangeListener {

    private TextView filter_title;
    private Button filter_tab;
    private LinearLayout filter_checked_conditiion;
    private MyRecyclerView poster_recyclerview;
    private LinearLayout filter_conditions;
    boolean isVertical;
    private View filter_condition_layout;
    private int focusedPos;
    private Button filter_arrow_up;
    private Button filter_arrow_down;
    private FilterPosterAdapter filterPosterAdapter;
    private String title;
    private String channel;
    private String content_model;
    private FilterConditions mFilterConditions;
    private PopupWindow filterPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_layout);
        Intent intent=getIntent();
        title = intent.getStringExtra("title");
        channel = intent.getStringExtra("channel");
        isVertical=intent.getBooleanExtra("isPortrait",true);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(filter_checked_conditiion.getChildCount()>1){
            filter_checked_conditiion.setVisibility(View.VISIBLE);
        }else{
            filter_checked_conditiion.setVisibility(View.INVISIBLE);
        }
    }



    private void initView() {
        filter_title = (TextView) findViewById(R.id.filter_title);
        filter_tab = (Button) findViewById(R.id.filter_tab);
        filter_checked_conditiion = (LinearLayout) findViewById(R.id.filter_checked_conditiion);
        poster_recyclerview = (MyRecyclerView) findViewById(R.id.poster_recyclerview);
        filter_condition_layout = View.inflate(this, R.layout.filter_condition_layout,null);
        filter_conditions = (LinearLayout)filter_condition_layout.findViewById(R.id.filter_conditions);
        filter_arrow_up = findView(R.id.filter_arrow_up);
        filter_arrow_down = findView(R.id.filter_arrow_down);
        if(isVertical) {
            poster_recyclerview.setLayoutManager(new GridLayoutManager(this, 5));
            poster_recyclerview.addItemDecoration(new SpaceItemDecoration(0,getResources().getDimensionPixelOffset(R.dimen.filter_item_vertical_poster_mb)));
            poster_recyclerview.setPadding(0,0,0,getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_padding_bottom));
        }else{
            poster_recyclerview.setLayoutManager(new GridLayoutManager(this, 3));
            poster_recyclerview.setPadding(0,0,0,getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_padding_bottom));
            poster_recyclerview.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelOffset(R.dimen.filter_item_horizontal_poster_mr),getResources().getDimensionPixelOffset(R.dimen.filter_item_horizontal_poster_mb)));
        }
        filter_tab.requestFocus();
        filter_tab.setOnClickListener(this);
        filter_arrow_up.setOnClickListener(this);
        filter_arrow_down.setOnClickListener(this);
        filter_arrow_up.setOnHoverListener(this);
        filter_arrow_down.setOnHoverListener(this);
        filter_arrow_up.setOnFocusChangeListener(this);
        filter_arrow_down.setOnFocusChangeListener(this);
    }

    private void initData() {
        fetchFilterCondition(channel);
        filter_title.setText(title);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(!filterPopup.isShowing()) {
            if (keyCode == 22) {
                if (isVertical) {
                    if ((focusedPos + 1) % 5 == 0) {
                        if (poster_recyclerview.getChildAt(focusedPos + 1) != null) {
                            poster_recyclerview.getChildAt(focusedPos + 1).requestFocus();
                        } else {
                            if (poster_recyclerview.getChildPosition(poster_recyclerview.getFocusedChild()) != filterPosterAdapter.getItemCount() - 1) {
                                poster_recyclerview.getChildAt(focusedPos - 4).requestFocus();
                            }
                        }
                        return true;
                    }
                } else {
                    if ((focusedPos + 1) % 3 == 0) {
                        if (poster_recyclerview.getChildAt(focusedPos + 1) != null) {
                            poster_recyclerview.getChildAt(focusedPos + 1).requestFocus();
                        } else {
                            if (poster_recyclerview.getChildPosition(poster_recyclerview.getFocusedChild()) != filterPosterAdapter.getItemCount() - 1) {
                                poster_recyclerview.getChildAt(focusedPos - 2).requestFocus();
                            }
                        }
                        return true;
                    }
                }
                if (poster_recyclerview.getChildPosition(poster_recyclerview.getFocusedChild()) == filterPosterAdapter.getItemCount() - 1) {
                    return true;
                }
            } else if (keyCode == 21) {
                if (isVertical) {
                    if (focusedPos % 5 == 0) {
                        if (poster_recyclerview.getChildAt(focusedPos - 1) != null) {
                            poster_recyclerview.getChildAt(focusedPos - 1).requestFocus();
                        } else {
                            if (poster_recyclerview.getChildPosition(poster_recyclerview.getFocusedChild()) != 0) {
                                poster_recyclerview.scrollBy(0, (int) (poster_recyclerview.getY() - poster_recyclerview.getChildAt(0).getHeight()));
                                poster_recyclerview.getChildAt(4).requestFocus();
                            } else {
                                return false;
                            }
                        }
                        return true;
                    }
                } else {
                    if ((focusedPos) % 3 == 0) {
                        if (poster_recyclerview.getChildAt(focusedPos - 1) != null) {
                            poster_recyclerview.getChildAt(focusedPos - 1).requestFocus();
                        } else {
                            if (poster_recyclerview.getChildPosition(poster_recyclerview.getFocusedChild()) != 0) {
                                poster_recyclerview.scrollBy(0, (int) (poster_recyclerview.getY() - poster_recyclerview.getChildAt(0).getHeight()));
                                poster_recyclerview.getChildAt(2).requestFocus();
                            } else {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void fetchFilterCondition(String channel) {
        mSkyService.getFilters(channel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<FilterConditions>() {


                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(FilterConditions filterConditions) {
                        content_model = filterConditions.getContent_model();
                        mFilterConditions = filterConditions;
                        fillConditionLayout(filterConditions.getAttributes().getGenre().getLabel(),filterConditions.getAttributes().getGenre().getValues());
                        fillConditionLayout(filterConditions.getAttributes().getArea().getLabel(),filterConditions.getAttributes().getArea().getValues());
                        fillConditionLayout(filterConditions.getAttributes().getAir_date().getLabel(),filterConditions.getAttributes().getAir_date().getValues());
                        fetchFilterResult(filterConditions.getContent_model(),filterConditions.getDefaultX());
                        showFilterPopup();
                    }
                });
    }

    private void showFilterPopup() {
        filterPopup = new PopupWindow(filter_condition_layout,getResources().getDimensionPixelOffset(R.dimen.filter_condition_popup_w),getResources().getDimensionPixelOffset(R.dimen.filter_condition_popup_h),true);
        filterPopup.setTouchable(true);
        filterPopup.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        filterPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
        filterPopup.showAtLocation(getRootView(),Gravity.NO_GRAVITY,0,getResources().getDimensionPixelOffset(R.dimen.filter_condition_popup_position));
        Message message=new Message();
        message.arg1=0;
        ((FilterConditionGroupView)filter_conditions.getChildAt(0)).handler.sendMessageDelayed(message,1000);
        filterPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if(poster_recyclerview.getChildAt(0)!=null)
                poster_recyclerview.getChildAt(0).requestFocus();
            }
        });
    }

    private void fillConditionLayout(String label, final List<List<String>> values) {
        List<String> no_limit=new ArrayList<>();
        no_limit.add("");
        no_limit.add("全部");
        values.add(0,no_limit);
        final FilterConditionGroupView filterConditionGroupView=new FilterConditionGroupView(this,values,label);
        filterConditionGroupView.setFocusable(true);
        filterConditionGroupView.setFocusableInTouchMode(true);
        filterConditionGroupView.setOnCheckChangeListener(new FilterConditionGroupView.OnCheckChange() {
            @Override
            public void onCheckChange(View view, boolean isChecked) {
                String text=((Button)view).getText().toString();
                filter(text,isChecked,view.getTag().toString());
            }
        });
        filter_conditions.addView(filterConditionGroupView);
    }


    private void fetchFilterResult(String content_model, String filterCondition) {
        mSkyService.getFilterRequest(content_model,filterCondition)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ItemList>(){


                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(final ItemList itemList) {
                        filterPosterAdapter = new FilterPosterAdapter(FilterActivity.this,itemList,isVertical);
                        poster_recyclerview.setAdapter(filterPosterAdapter);
                        filterPosterAdapter.setItemClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                PageIntent intent = new PageIntent();
                                Item item=itemList.objects.get(position);
                                if(item.content_model.contains("gather")){
                                    intent.toSubject(FilterActivity.this,item.content_model,item.pk,item.title,Source.LIST.getValue(),baseChannel);
                                }else if(item.is_complex) {
                                    intent.toDetailPage(FilterActivity.this,Source.LIST.getValue(),item.pk);
                                }else{
                                    intent.toPlayPage(FilterActivity.this,item.pk,0, Source.LIST);
                                }
                            }
                        });
                        filterPosterAdapter.setItemFocusedListener(new OnItemFocusedListener() {
                            @Override
                            public void onItemfocused(View view, int position, boolean hasFocus) {
                                if(hasFocus){
                                    focusedPos =poster_recyclerview.indexOfChild(view);
                                    if(view.getY()>getResources().getDimensionPixelOffset(R.dimen.filter_poster_start_scroll_length)){
                                        poster_recyclerview.smoothScrollBy(0, (int) (view.getY()-view.getHeight()-getResources().getDimensionPixelOffset(R.dimen.filter_item_vertical_poster_mb)));
                                    }
                                    JasmineUtil.scaleOut3(view);
                                    if(isVertical) {
                                        view.findViewById(R.id.item_vertical_poster_title).setSelected(true);
                                    }else {
                                        view.findViewById(R.id.item_horizontal_poster_title).setSelected(true);
                                    }
                                }else{
                                    JasmineUtil.scaleIn3(view);
                                    if(isVertical) {
                                        view.findViewById(R.id.item_vertical_poster_title).setSelected(false);
                                    }else {
                                        view.findViewById(R.id.item_horizontal_poster_title).setSelected(false);
                                    }
                                }
                            }
                        });

                    }
                });
    }

    private void filter(String text, boolean b, String tag) {
        if(!"全部".equals(text)){
            if(b){
                TextView checked=new TextView(this);
                checked.setBackgroundResource(R.drawable.filter_condition_checked2);
                LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_h));
                params.rightMargin=getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_mr);
                checked.setPadding(getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_pl),0,getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_pr),0);
                checked.setLayoutParams(params);
                checked.setTextSize(getResources().getDimensionPixelSize(R.dimen.filter_checked_condition_ts));
                checked.setTextColor(getResources().getColor(R.color._333333));
                checked.setText(text);
                checked.setGravity(Gravity.CENTER);
                checked.setTag(tag);
                filter_checked_conditiion.addView(checked);
            }else{
                for (int i = 0; i <filter_checked_conditiion.getChildCount() ; i++) {
                    if(text.equals(((TextView)filter_checked_conditiion.getChildAt(i)).getText().toString())){
                        filter_checked_conditiion.removeViewAt(i);
                    }
                }
            }
            if(filter_checked_conditiion.getChildCount()>1){
                filter_checked_conditiion.setVisibility(View.VISIBLE);
            }else{
                filter_checked_conditiion.setVisibility(View.INVISIBLE);
            }
        }
        String condition = "";

        for (int i = 1; i <filter_checked_conditiion.getChildCount() ; i++) {
            if(filter_checked_conditiion.getChildAt(i)!=null)
                condition +=filter_checked_conditiion.getChildAt(i).getTag().toString()+"!";
        }
        if(filter_checked_conditiion.getChildCount()==1){
            condition=mFilterConditions.getDefaultX()+"!";
        }
        fetchFilterResult(content_model,condition.substring(0,condition.lastIndexOf("!")));
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i==R.id.filter_arrow_down){
            JasmineUtil.scaleOut4(v);
            poster_recyclerview.smoothScrollBy(0, (int) (poster_recyclerview.getChildAt(0).getY()+poster_recyclerview.getChildAt(0).getHeight()*2+getResources().getDimensionPixelOffset(R.dimen.filter_poster_vertical_scroll_space)));
        }else if(i==R.id.filter_arrow_up)   {
            JasmineUtil.scaleOut4(v);
            poster_recyclerview.smoothScrollBy(0, (int) (poster_recyclerview.getChildAt(0).getY()-poster_recyclerview.getChildAt(0).getHeight()*2-getResources().getDimensionPixelOffset(R.dimen.filter_poster_vertical_scroll_space)));
        }else if(i==R.id.filter_tab){
            showFilterPopup();
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
            v.requestFocus();
            v.requestFocusFromTouch();
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            JasmineUtil.scaleOut4(v);
        }else{
            JasmineUtil.scaleIn4(v);
        }
    }

}
