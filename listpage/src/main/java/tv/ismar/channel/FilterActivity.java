package tv.ismar.channel;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.Utils.PosterUtil;
import tv.ismar.adapter.FilterPosterAdapter;
import tv.ismar.adapter.FocusGridLayoutManager;
import tv.ismar.adapter.SpaceItemDecoration;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemList;
import tv.ismar.app.entity.Section;
import tv.ismar.app.entity.SectionList;
import tv.ismar.app.models.FilterConditions;
import tv.ismar.app.ui.adapter.OnItemClickListener;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.app.widget.MyRecyclerView;
import tv.ismar.listpage.R;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.view.FilterConditionGroupView;
import tv.ismar.view.FullScrollView;
import tv.ismar.view.LocationRelativeLayout;

/**
 * Created by zhangjiqiang on 15-6-18.
 */
public class FilterActivity extends BaseActivity implements View.OnClickListener, View.OnHoverListener {

    private TextView filter_title;
    private RadioButton filter_tab;
    private LinearLayout filter_checked_conditiion;
    private MyRecyclerView poster_recyclerview;
    private MyRecyclerView list_poster_recyclerview;
    private LinearLayout filter_conditions;
    boolean isVertical;
    private View filter_condition_layout;
    private int focusedPos;
    private String title;
    private String channel;
    private String content_model;
    private FilterConditions mFilterConditions;
    private PopupWindow filterPopup;
    private int spanCount;
    private boolean canScroll=true;
    private boolean isFocused;
    private RadioGroup section_group;
    private FocusGridLayoutManager mFocusGridLayoutManager;
    private SpaceItemDecoration mSpaceItemDecoration;
    private String checkedTitle;
    private ArrayList<Integer> specialPos;
    private int totalItemCount;
    private static final int FLAG_FILTER=0;
    private static final int FLAG_SECTION=1;
    private ItemList mAllSectionItemList;
    private int checkedPos;
    private SectionList sectionList;
    private int sectionSize;
    private boolean[] sectionHasData;
    private TextView current_section_title;
    private View filter_noresult;
    private Button tab_arrow_up;
    private Button tab_arrow_dowm;
    private Button poster_arrow_up;
    private Button poster_arrow_down;
    private LocationRelativeLayout filter_root_view;
    private Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //控制列表页左侧tab获取焦点1s后自动加载数据
            RadioButton radioButton= (RadioButton) msg.obj;
            radioButton.setChecked(true);
            radioButton.callOnClick();
            return false;
        }
    });
    private FullScrollView tab_scroll;
    private FocusGridLayoutManager mFilterFocusGridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_layout);
        //获取intent传递的数据，从而判断加载的列表页的类型
        Intent intent=getIntent();
        title = intent.getStringExtra("title");
        channel = intent.getStringExtra("channel");
        isVertical=intent.getBooleanExtra("isPortrait",true);
        //view和data、监听事件的初始化操作
        initView();
        initListener();
        initData();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //日志相关
        AppConstant.purchase_entrance_page = "filter";
        AppConstant.purchase_page = "filter";
        //判断筛选页已选筛选条件的显示与隐藏
        if(filter_checked_conditiion.getChildCount()>1){
            filter_checked_conditiion.setVisibility(View.VISIBLE);
        }else{
            filter_checked_conditiion.setVisibility(View.INVISIBLE);
        }
    }



    private void initView() {
        //左侧tab区view
        filter_title = (TextView) findViewById(R.id.filter_title);
        tab_scroll = (FullScrollView) findViewById(R.id.tab_scroll);
        section_group = (RadioGroup) findViewById(R.id.section_group);
        filter_tab = (RadioButton) findViewById(R.id.filter_tab);
        //筛选区view
        filter_checked_conditiion = (LinearLayout) findViewById(R.id.filter_checked_conditiion);
        poster_recyclerview = (MyRecyclerView) findViewById(R.id.poster_recyclerview);
        poster_recyclerview.setHasFixedSize(true);
        filter_condition_layout = View.inflate(this, R.layout.filter_condition_layout,null);
        filter_conditions = (LinearLayout)filter_condition_layout.findViewById(R.id.filter_conditions);
        filter_noresult = findViewById(R.id.filter_noresult);
        //列表区view
        list_poster_recyclerview = (MyRecyclerView) findViewById(R.id.list_poster_recyclerview);
        list_poster_recyclerview.setHasFixedSize(true);
        current_section_title = (TextView) findViewById(R.id.current_section_title);
        //空鼠箭头view
        filter_root_view = (LocationRelativeLayout) findViewById(R.id.filter_root_view);
        tab_arrow_up = (Button)findViewById(R.id.tab_arrow_up);
        tab_arrow_dowm = (Button)findViewById(R.id.tab_arrow_dowm);
        poster_arrow_up = (Button)findViewById(R.id.poster_arrow_up);
        poster_arrow_down = (Button)findViewById(R.id.poster_arrow_down);
        filter_root_view.setArrow_up_left(tab_arrow_up);
        filter_root_view.setArrow_down_left(tab_arrow_dowm);
        filter_root_view.setArrow_up_right(poster_arrow_up);
        filter_root_view.setArrow_down_right(poster_arrow_down);
        filter_root_view.setxBoundary(getResources().getDimensionPixelOffset(R.dimen.filter_layout_left_view_tab_w));
        tab_arrow_up.setOnHoverListener(this);
        tab_arrow_dowm.setOnHoverListener(this);
        poster_arrow_up.setOnHoverListener(this);
        poster_arrow_down.setOnHoverListener(this);
        tab_arrow_up.setOnClickListener(this);
        tab_arrow_dowm.setOnClickListener(this);
        poster_arrow_up.setOnClickListener(this);
        poster_arrow_down.setOnClickListener(this);

        RelativeLayout.LayoutParams recyclerParam= (RelativeLayout.LayoutParams) list_poster_recyclerview.getLayoutParams();
        if(isVertical){
            recyclerParam.setMargins(0,getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_recyclerview_vmt),getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_recyclerview_mr),0);
        }else{
            recyclerParam.setMargins(0,getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_recyclerview_hmt),getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_recyclerview_mr),0);
        }
        list_poster_recyclerview.setLayoutParams(recyclerParam);
    }

    private void initListener() {
        filter_tab.setOnClickListener(this);
        filter_tab.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    if(filter_tab.isChecked()){
                        return;
                    }
                    Message msg=new Message();
                    msg.what=0;
                    msg.obj=v;
                    mHandler.sendMessageDelayed(msg,1000);
                }else{
                    mHandler.removeMessages(0);
                }
            }
        });
        filter_tab.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    poster_recyclerview.setVisibility(View.VISIBLE);
                    list_poster_recyclerview.setVisibility(View.GONE);
                    if(mFilterFocusGridLayoutManager!=null){
                        mFilterFocusGridLayoutManager.setLeftFocusView(filter_tab);
                    }
                    filter();
                }else{
                    poster_recyclerview.setVisibility(View.GONE);
                    filter_noresult.setVisibility(View.GONE);
                    list_poster_recyclerview.setVisibility(View.VISIBLE);
                }
            }
        });
        filter_tab.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==19&&event.getAction()==KeyEvent.ACTION_DOWN){
                    YoYo.with(Techniques.VerticalShake).duration(500).playOn(v);
                    return true;
                }
                return false;
            }
        });
        list_poster_recyclerview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                //通过滑动停止后判断当前所在列表位置，进行数据更新操作(列表数据更新、列表区title更新、当前选中tab更新)
                int firstVisiablePos=mFocusGridLayoutManager.findFirstVisibleItemPosition();
                showData(firstVisiablePos);
                showData(mFocusGridLayoutManager.findLastVisibleItemPosition());
                for (int i = 0; i <sectionSize ; i++) {
                    if(i==sectionSize-1){
                        break;
                    }
                    if(firstVisiablePos>=specialPos.get(i)&&firstVisiablePos<specialPos.get(i+1)) {
                        if(current_section_title.getText()!=null&&!sectionList.get(i).title.equals(current_section_title.getText()))
                            current_section_title.setText(sectionList.get(i).title);
                    }
                }
            }
        });

    }

    private void initData() {
        filter_title.setText(title);
        fetchFilterCondition(channel);
        fetchChannelSection(channel);
    }

    //请求list分类tab
    private void fetchChannelSection(String channel) {
        mSkyService.getSections(channel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<SectionList>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(SectionList sections) {
                        sectionList=new SectionList();
                        for (int i = 0; i <sections.size() ; i++) {
                            if(sections.get(i).count!=0){
                                sectionList.add(sections.get(i));
                            }
                        }
//                        sectionList = sections;
                        sectionSize = sectionList.size();
                        sectionHasData = new boolean[sectionSize];
                        for (int i = 0; i <sectionSize; i++) {
                            sectionHasData[i]=false;
                        }
                        fillSections(sectionList);
                    }
                });
    }

    //填充list分类列表
    private void fillSections(SectionList sections) {
        RadioGroup.LayoutParams params=new RadioGroup.LayoutParams(getResources().getDimensionPixelOffset(R.dimen.filter_layout_left_view_tab_w),getResources().getDimensionPixelOffset(R.dimen.filter_layout_left_view_tab_h));
        for (int i = 0; i <sections.size() ; i++) {
            final Section section = sections.get(i);
            final RadioButton radioButton= (RadioButton) View.inflate(this,R.layout.item_section_radiobtn,null);
            radioButton.setLayoutParams(params);
            radioButton.setText(section.title);
            radioButton.setTag(section.url);
            radioButton.setId(R.id.section_radiobtn+i);
            final int finalI1 = i;
            radioButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        radioButton.setTextSize(getResources().getDimensionPixelSize(R.dimen.filter_layout_left_view_tab_ts_scaled));
                        radioButton.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                        Log.e("tabposition",v.getY()+"");
                        if(radioButton.isChecked()){
                            return;
                        }
                        Message msg=new Message();
                        msg.obj=v;
                        msg.what= finalI1+1;
                        mHandler.sendMessageDelayed(msg,1000);
                    }else{
                        mHandler.removeMessages(finalI1+1);
                        radioButton.setTextSize(getResources().getDimensionPixelSize(R.dimen.filter_layout_left_view_tab_ts));
                        radioButton.setEllipsize(TextUtils.TruncateAt.END);
                    }
                }
            });
            final int finalI = i;
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFocusGridLayoutManager.scrollToPositionWithOffset(specialPos.get(checkedPos),0);
                    if(isFirst) {
                        fetchSectionData(section.url);
                        isFirst=false;
                    }
                    current_section_title.setText(sectionList.get(finalI).title);
                }
            });
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        checkedPos = finalI;
                        checkedTitle = section.title;
                        if(filterPopup!=null&&filterPopup.isShowing())
                            filterPopup.dismiss();
                        filter_checked_conditiion.setVisibility(View.INVISIBLE);
                        current_section_title.setVisibility(View.VISIBLE);
                        filter_checked_conditiion.requestLayout();
                        if(mFocusGridLayoutManager!=null){
                            mFocusGridLayoutManager.setLeftFocusView(radioButton);
                        }
                    }
                }
            });
            if(i==sections.size()-1){
                radioButton.setNextFocusDownId(R.id.section_radiobtn+i);
            }
            radioButton.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if(finalI==sectionSize-1&&keyCode==20&&event.getAction()==KeyEvent.ACTION_DOWN){
                        YoYo.with(Techniques.VerticalShake).delay(500).playOn(v);
                        return true;
                    }
                    return false;
                }
            });
            section_group.addView(radioButton);
        }
        specialPos = new ArrayList<>();
        specialPos.add(0);
        totalItemCount = 0;
        for (int i = 0; i <sections.size() ; i++) {
            totalItemCount +=sections.get(i).count;
            specialPos.add(totalItemCount+i+1);
        }
        //根据不同的板式(横、竖版)设置不同的列表样式
        if(isVertical) {
            spanCount = 5;
            mSpaceItemDecoration = new SpaceItemDecoration(50,40,isVertical);
            poster_recyclerview.addItemDecoration(mSpaceItemDecoration);
            mSpaceItemDecoration.setSpecialPos(specialPos);
//            poster_recyclerview.setPadding(0,0,0,getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_padding_bottom));
            list_poster_recyclerview.addItemDecoration(mSpaceItemDecoration);
//          list_poster_recyclerview.setPadding(0,0,0,getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_padding_bottom));
        }else{
            spanCount = 3;
            mSpaceItemDecoration=new SpaceItemDecoration(60,60,isVertical);
//            poster_recyclerview.setPadding(0,0,0,getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_padding_bottom));
            poster_recyclerview.addItemDecoration(mSpaceItemDecoration);
            mSpaceItemDecoration.setSpecialPos(specialPos);
//            list_poster_recyclerview.setPadding(0,0,0,getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_padding_bottom));
            list_poster_recyclerview.addItemDecoration(mSpaceItemDecoration);
        }
        mFocusGridLayoutManager = new FocusGridLayoutManager(this,spanCount);
        mFocusGridLayoutManager.setSpecialPos(specialPos);
        mFilterFocusGridLayoutManager = new FocusGridLayoutManager(this,spanCount);
        poster_recyclerview.setLayoutManager(mFilterFocusGridLayoutManager);
        list_poster_recyclerview.setLayoutManager(mFocusGridLayoutManager);
        totalItemCount+=sections.size();
        mAllSectionItemList = new ItemList();
        mAllSectionItemList.objects=new ArrayList<Item>();
        for (int i = 0; i <totalItemCount ; i++) {
            Item item=new Item();
            mAllSectionItemList.objects.add(item);
        }
        mFocusGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(specialPos.contains(position)) {
                    return spanCount;
                }else{
                    return 1;
                }
            }
        });

    }

    //请求每个section的数据
    private void fetchSectionData(String url) {
        mSkyService.getItemListChannel(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ItemList>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ItemList itemList) {
                        for (int i = 0; i <itemList.objects.size() ; i++) {
                            mAllSectionItemList.objects.set(specialPos.get(checkedPos)+i+1,itemList.objects.get(i));
                        }
                        sectionHasData[checkedPos]=true;
                        processResultData(mAllSectionItemList, FLAG_SECTION);
                    }
                });

    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    //防止recyclerview焦点乱跑
    long mDownTime=0;
    long mUpTime=0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //长按滑动 滑动时焦点不会乱跳，但是每隔400毫秒滑动一次
        if (keyCode == 20) {
            long downTime =System.currentTimeMillis();
            if(mDownTime==0){
                mDownTime=downTime;
                return false;
            }
            if(downTime-mDownTime>400){
                mDownTime=downTime;
                return false;
            }
            return true;
        }
        if (keyCode == 19) {
            long upTime =System.currentTimeMillis();
            if(mUpTime==0){
                mUpTime=upTime;
                return false;
            }
            if(upTime-mUpTime>400){
                mUpTime=upTime;
                return false;
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    /**
     * 筛选条件数据请求
     */
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
                        //填充筛选条件view
                        if(filterConditions.getAttributes().getGenre()!=null)
                            fillConditionLayout(filterConditions.getAttributes().getGenre().getLabel(),filterConditions.getAttributes().getGenre().getValues());
                        if(filterConditions.getAttributes().getArea()!=null)
                            fillConditionLayout(filterConditions.getAttributes().getArea().getLabel(),filterConditions.getAttributes().getArea().getValues());
                        if(filterConditions.getAttributes().getAir_date()!=null)
                            fillConditionLayout(filterConditions.getAttributes().getAir_date().getLabel(),filterConditions.getAttributes().getAir_date().getValues());
                        if(filterConditions.getAttributes().getAge()!=null)
                            fillConditionLayout(filterConditions.getAttributes().getAge().getLabel(),filterConditions.getAttributes().getAge().getValues());
                        if(filterConditions.getAttributes().getFeature()!=null)
                            fillConditionLayout(filterConditions.getAttributes().getFeature().getLabel(),filterConditions.getAttributes().getFeature().getValues());
                        fetchFilterResult(filterConditions.getContent_model(),filterConditions.getDefaultX());
                        //筛选条件popup焦点控制
                        String conditionsForLog="";
                        for (int i = 0; i <filter_conditions.getChildCount() ; i++) {
                            FilterConditionGroupView filter= (FilterConditionGroupView) filter_conditions.getChildAt(i);
                            if(filter_conditions.getChildAt(i-1)!=null)
                                filter.setNextUpView(filter_conditions.getChildAt(i-1));
                            if(filter_conditions.getChildAt(i+1)!=null)
                                filter.setNextDownView(filter_conditions.getChildAt(i+1));
                            conditionsForLog+=";";
                        }
                        AppConstant.purchase_entrance_keyword = conditionsForLog.substring(0,conditionsForLog.lastIndexOf(";"));
                        showFilterPopup();
                    }

                });
    }

    /**
     * 显示筛选条件popup
     */
    private void showFilterPopup() {
        filterPopup = new PopupWindow(filter_condition_layout, getResources().getDimensionPixelOffset(R.dimen.filter_condition_popup_w), getResources().getDimensionPixelOffset(R.dimen.filter_condition_popup_h), true);
        filterPopup.setTouchable(true);
        filterPopup.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        filterPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
        filterPopup.showAtLocation(getRootView(), Gravity.BOTTOM, 0, 0);
        Message msg = new Message();
        msg.arg1 = -1;
        ((FilterConditionGroupView) filter_conditions.getChildAt(0)).handler.sendMessage(msg);
        filterPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
//                filter_tab.setFocusable(true);
                if (poster_recyclerview.getChildAt(0) != null) {
                    canScroll = false;
                    poster_recyclerview.getChildAt(0).requestFocus();
                }else{
                    filter_tab.requestFocus();
                }
            }
        });
    }

    /**
     * 填充筛选条件layout
     * @param label
     * @param values
     */
    private void fillConditionLayout(String label, final List<List<String>> values) {

        List<String> no_limit=new ArrayList<>();
        no_limit.add("");
        no_limit.add("全部");
        values.add(0,no_limit);
        final FilterConditionGroupView filterConditionGroupView=new FilterConditionGroupView(this,values,label);
        filterConditionGroupView.filter_condition_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                filter();
            }
        });
        filterConditionGroupView.setId(R.id.filter_conditions+filter_conditions.getChildCount());
        filter_conditions.addView(filterConditionGroupView);
    }

    /**
     * 筛选无结果时显示默认推荐数据
     */
        private void fetchFilterNoResult(){
            mSkyService.getFilterRequestNodata("movie","area*10022$10261$10263$10378$10479$10483$10484$10494",1).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<ItemList>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(ItemList itemList) {
                            if(itemList!=null){
                                filter_noresult.setVisibility(View.VISIBLE);
                                poster_recyclerview.setVisibility(View.INVISIBLE);
                                LinearLayout filter_noresult_first_line= (LinearLayout) filter_noresult.findViewById(R.id.filter_noresult_first_line);
                                LinearLayout filter_noresult_second_line= (LinearLayout) filter_noresult.findViewById(R.id.filter_noresult_second_line);
                                filter_noresult_first_line.removeAllViews();
                                filter_noresult_second_line.removeAllViews();
                                LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                if(isVertical){
                                    for (int i = 0; i <5 ; i++) {
                                        final Item item = itemList.objects.get(i);
                                        if(item!=null){
                                            final View recommendView= View.inflate(FilterActivity.this,R.layout.filter_item_vertical_poster,null);
                                            PosterUtil.fillPoster(FilterActivity.this,0,item,(ImageView)recommendView.findViewById(R.id.item_vertical_poster_img),(ImageView)recommendView.findViewById(R.id.item_vertical_poster_vip),(TextView)recommendView.findViewById(R.id.item_vertical_poster_mark),(TextView)recommendView.findViewById(R.id.item_vertical_poster_title),null);
                                            recommendView.setOnFocusChangeListener(mOnFocusChangeListener);
                                            recommendView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    new PageIntent().toDetailPage(FilterActivity.this,Source.LIST.getValue(),item.pk);
                                                }
                                            });
                                                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                p.rightMargin = 50;
                                                recommendView.setLayoutParams(p);
                                            filter_noresult_first_line.addView(recommendView);
                                        }

                                    }
                                    params.topMargin=getResources().getDimensionPixelOffset(R.dimen.filter_noresult_vertical_mt);
                                    params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.filter_noresult_vertical_ml);
                                    filter_noresult_first_line.setLayoutParams(params);
                                }else{
                                    for (int i = 0; i <8 ; i++) {
                                        final Item item = itemList.objects.get(i);
                                        if(item!=null) {
                                            View recommendView= View.inflate(FilterActivity.this,R.layout.item_filter_noresult_poster,null);
                                            PosterUtil.fillPoster(FilterActivity.this,1,item,(ImageView)recommendView.findViewById(R.id.item_filter_noresult_img),(ImageView)recommendView.findViewById(R.id.item_filter_noresult_vip),(TextView)recommendView.findViewById(R.id.item_filter_noresult_mark),(TextView)recommendView.findViewById(R.id.item_filter_noresult_title),(TextView)recommendView.findViewById(R.id.item_filter_noresult_descrip));
                                            recommendView.setOnFocusChangeListener(mOnFocusChangeListener);
                                            recommendView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    new PageIntent().toDetailPage(FilterActivity.this,Source.LIST.getValue(),item.pk);
                                                }
                                            });
                                            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                            p.rightMargin = 40;
                                            recommendView.setLayoutParams(p);
                                            if(i<4) {
                                                filter_noresult_first_line.addView(recommendView);
                                            }else {
                                                filter_noresult_second_line.addView(recommendView);
                                            }
                                        }
                                    }
                                    params.topMargin=getResources().getDimensionPixelOffset(R.dimen.filter_noresult_horizontal_mt1);
                                    params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.filter_noresult_horizontal_ml);
                                    filter_noresult_first_line.setLayoutParams(params);
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                        }
                    });
        }

        private View.OnFocusChangeListener mOnFocusChangeListener=new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    JasmineUtil.scaleOut3(v);
                }else{
                    JasmineUtil.scaleIn3(v);
                }
            }
        };

    /**
     * 根据已选条件请求筛选结果
     * @param content_model
     * @param filterCondition
     */
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
                        if(itemList==null||itemList.objects.size()==0){
                            fetchFilterNoResult();
                            poster_recyclerview.setVisibility(View.INVISIBLE);
                        }else {
                            processResultData(itemList, FLAG_FILTER);
                            filter_noresult.setVisibility(View.GONE);
                            poster_recyclerview.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    /**
     * 处理请求到的数据
     */
    private boolean isFirst=true;
    private void processResultData(final ItemList itemList, final int flagSection) {
        if(flagSection==FLAG_FILTER){
            FilterPosterAdapter filterPosterAdapter = new FilterPosterAdapter(FilterActivity.this,itemList,isVertical);
            poster_recyclerview.swapAdapter(filterPosterAdapter, true);
            filterPosterAdapter.setItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    baseSection="";
                    PageIntent intent = new PageIntent();
                    Item item=itemList.objects.get(position);
                    if(item.content_model.contains("gather")){
                        intent.toSubject(FilterActivity.this,item.content_model,item.pk,item.title,Source.RETRIEVAL.getValue(),baseChannel);
                    }else if(item.is_complex) {
                        intent.toDetailPage(FilterActivity.this,Source.RETRIEVAL.getValue(),item.pk);
                    }else{
                        intent.toPlayPage(FilterActivity.this,item.pk,0, Source.RETRIEVAL);
                    }
                }
            });
            filterPosterAdapter.setItemFocusedListener(new OnItemFocusedListener() {
                @Override
                public void onItemfocused(View view, int position, boolean hasFocus) {
                    if(hasFocus){
                        isFocused = true;
                        focusedPos = poster_recyclerview.indexOfChild(view);
                        if(view.getY()>getResources().getDimensionPixelOffset(R.dimen.filter_poster_start_scroll_length)){
//                            mFilterFocusGridLayoutManager.setCanScroll(true);
                            if(canScroll) {
                                mFilterFocusGridLayoutManager.scrollToPositionWithOffset(position,0);
                            }else{
                                canScroll=true;
                            }
                        }else if(view.getY()<0){
//                            mFilterFocusGridLayoutManager.setCanScroll(true);
                            mFilterFocusGridLayoutManager.scrollToPositionWithOffset(position,430);
                        }else{
//                            mFilterFocusGridLayoutManager.setCanScroll(false);
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
        }else{
            final FilterPosterAdapter listPosterAdapter = new FilterPosterAdapter(FilterActivity.this,itemList,isVertical,totalItemCount,specialPos,sectionList);
            list_poster_recyclerview.swapAdapter(listPosterAdapter,false);
            listPosterAdapter.setItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    baseSection="";
                    PageIntent intent = new PageIntent();
                    Item item=itemList.objects.get(position);
                    if(item.content_model.contains("gather")){
                        intent.toSubject(FilterActivity.this,item.content_model,item.pk,item.title,Source.RETRIEVAL.getValue(),baseChannel);
                    }else if(item.is_complex) {
                        intent.toDetailPage(FilterActivity.this,Source.RETRIEVAL.getValue(),item.pk);
                    }else{
                        intent.toPlayPage(FilterActivity.this,item.pk,0, Source.RETRIEVAL);
                    }
                }
            });
            listPosterAdapter.setItemFocusedListener(new OnItemFocusedListener() {
                @Override
                public void onItemfocused(View view, int position, boolean hasFocus) {
                    if(hasFocus){
                        Log.e("postery",view.getY()+"");
                            for (int i = 0; i <sectionSize ; i++) {
                                if(i==sectionSize-1){
                                    break;
                                }
                                if(position>specialPos.get(i)&&position<specialPos.get(i+1)){
                                    ((RadioButton)section_group.getChildAt(i+1)).setChecked(true);
                                    break;
                                }
                            }
                        isFocused = true;
                        focusedPos = list_poster_recyclerview.indexOfChild(view);
                        if(view.getY()>getResources().getDimensionPixelOffset(R.dimen.filter_poster_start_scroll_length)){
//                            mFocusGridLayoutManager.setCanScroll(true);
                            if(canScroll) {
                                 mFocusGridLayoutManager.scrollToPositionWithOffset(position,getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset));
                            }else{
                                canScroll=true;
                            }
                        }else if(view.getY()<0) {
//                            mFocusGridLayoutManager.setCanScroll(true);
                            if(canScroll) {
                                mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset));
                            }else{
                                canScroll=true;
                            }
                        }else{
//                            mFocusGridLayoutManager.setCanScroll(false);
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


    }

    /**
     * 根据用户对筛选条件的选择实时更新筛选条件及筛选结果
     */
    private void filter() {
        View view=filter_checked_conditiion.getChildAt(0);
        filter_checked_conditiion.removeAllViews();
        filter_checked_conditiion.addView(view);
        String conditionForLog = "";
        for (int i = 0; i <filter_conditions.getChildCount() ; i++) {
            FilterConditionGroupView filterConditionGroupView=(FilterConditionGroupView)filter_conditions.getChildAt(i);
            RadioButton radio= (RadioButton) filterConditionGroupView.filter_condition_radio_group.findViewById(filterConditionGroupView.filter_condition_radio_group.getCheckedRadioButtonId());
            if(!"全部".equals(radio.getText())){
                TextView checked= new TextView(this);
                checked.setBackgroundResource(R.drawable.filter_condition_checked2);
                LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_h));
                params.rightMargin=getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_mr);
                checked.setPadding(getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_pl),0,getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_pr),0);
                checked.setLayoutParams(params);
                checked.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.filter_checked_condition_ts));
                checked.setTextColor(getResources().getColor(R.color._333333));
                checked.setText(radio.getText());
                checked.setGravity(Gravity.CENTER);
                checked.setTag(radio.getTag());
                filter_checked_conditiion.addView(checked);
                conditionForLog+=radio.getText()+";";
            }else{
                conditionForLog+=";";
            }
        }
        filter_checked_conditiion.setVisibility(View.VISIBLE);
        current_section_title.setVisibility(View.INVISIBLE);
        if(filter_checked_conditiion.getChildCount()==1){

            TextView checked= new TextView(this);
            checked.setBackgroundResource(R.drawable.filter_condition_checked2);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_h));
            params.rightMargin=getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_mr);
            checked.setPadding(getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_pl),0,getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_pr),0);
            checked.setLayoutParams(params);
            checked.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.filter_checked_condition_ts));
            checked.setTextColor(getResources().getColor(R.color._333333));
            checked.setText("全部");
            checked.setGravity(Gravity.CENTER);
            checked.setTag("");
            filter_checked_conditiion.addView(checked);
        }

        String condition = "";

        for (int i = 1; i <filter_checked_conditiion.getChildCount() ; i++) {
            if(filter_checked_conditiion.getChildAt(i)!=null) {
                condition += filter_checked_conditiion.getChildAt(i).getTag().toString() + "!";
            }
        }
        if(filter_checked_conditiion.getChildCount()==2){
            condition=mFilterConditions.getDefaultX()+"!";
        }
        AppConstant.purchase_entrance_keyword = conditionForLog.substring(0,conditionForLog.lastIndexOf(";"));
        fetchFilterResult(content_model,condition.substring(0,condition.lastIndexOf("!")));
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i==R.id.filter_tab){
//            filter_tab.setFocusable(false);
            filter_root_view.requestFocus();
            filter_root_view.requestFocusFromTouch();
            current_section_title.setVisibility(View.INVISIBLE);
            if(filter_checked_conditiion.getChildCount()>1){
                filter_checked_conditiion.setVisibility(View.VISIBLE);
                current_section_title.setVisibility(View.INVISIBLE);
            }
            if(filterPopup!=null&&!filterPopup.isShowing()) {
                showFilterPopup();
            }
        }else if(i==R.id.tab_arrow_up){
            tab_scroll.pageScroll(View.FOCUS_UP);
        }else if(i==R.id.tab_arrow_dowm){
            tab_scroll.pageScroll(View.FOCUS_DOWN);
        }else if(i==R.id.poster_arrow_up){
            if(filter_tab.isChecked()){
                mFilterFocusGridLayoutManager.scrollToPositionWithOffset(mFilterFocusGridLayoutManager.findLastVisibleItemPosition(),430);
            }else{
                mFocusGridLayoutManager.scrollToPositionWithOffset(mFocusGridLayoutManager.findLastVisibleItemPosition(),getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset));
            }
        }else if(i==R.id.poster_arrow_down){
            if(filter_tab.isChecked()){
                mFilterFocusGridLayoutManager.scrollToPositionWithOffset(mFilterFocusGridLayoutManager.findLastVisibleItemPosition(),0);
            }else{
                mFocusGridLayoutManager.scrollToPositionWithOffset(mFocusGridLayoutManager.findLastVisibleItemPosition(),getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset));
            }
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

    /**
     * 更新list数据区
     * @param position
     */
    private void showData(int position){
        Log.e("showdata",position+"");
        for (int i = 0; i <sectionSize ; i++) {
            if(i==sectionSize-1){
                break;
            }
            if(sectionHasData[i]) {
                continue;
            }else{
                if(position>specialPos.get(i)&&position<specialPos.get(i+1)){
                    fetchSectionData(sectionList.get(i).url);
                    checkedPos=i;
                }
            }

        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        baseSection="";
        filter_conditions.removeAllViews();
        poster_recyclerview.swapAdapter(null,true);
        mHandler=null;
    }
}
