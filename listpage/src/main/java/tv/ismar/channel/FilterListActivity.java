package tv.ismar.channel;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
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
import java.util.HashMap;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.Utils.PosterUtil;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.adapter.FilterPosterAdapter;
import tv.ismar.adapter.FocusGridLayoutManager;
import tv.ismar.adapter.ListPosterAdapter;
import tv.ismar.adapter.SpaceItemDecoration;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.entity.FilterNoresultPoster;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemList;
import tv.ismar.app.entity.ListSectionEntity;
import tv.ismar.app.entity.Section;
import tv.ismar.app.entity.SectionList;
import tv.ismar.app.models.FilterConditions;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.ui.adapter.OnItemClickListener;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.app.widget.MyRecyclerView;
import tv.ismar.listpage.R;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.view.FilterConditionGroupView;
import tv.ismar.view.FullScrollView;
import tv.ismar.view.LocationRelativeLayout;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;

/**
 * Created by zhangjiqiang on 15-6-18.
 */
public class FilterListActivity extends BaseActivity implements View.OnClickListener, View.OnHoverListener {

    private TextView filter_title;
    private RadioButton filter_tab;
    private LinearLayout filter_checked_conditiion;
    private MyRecyclerView poster_recyclerview;
    private MyRecyclerView list_poster_recyclerview;
    private LinearLayout filter_conditions;
    boolean isVertical;
    private View filter_condition_layout;
    private String title;
    private String channel;
    private String content_model;
    private FilterConditions mFilterConditions;
    private PopupWindow filterPopup;
    private int spanCount;
    private RadioGroup section_group;
    private FocusGridLayoutManager mFocusGridLayoutManager;
    private ArrayList<Integer> specialPos;
    private int totalItemCount;
    private ListSectionEntity mAllSectionItemList;
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
            lastFocusedView=null;
            return false;
        }
    });
    private FullScrollView tab_scroll;
    private FocusGridLayoutManager mFilterFocusGridLayoutManager;
    private LinearLayout filter_noresult_first_line;
    private LinearLayout filter_noresult_second_line;
    private View lastFocusedView;
    private boolean filterNoResult=false;
    private ListPosterAdapter listPosterAdapter;
    private FilterPosterAdapter filterPosterAdapter;
    private int nextPos;
    private boolean booleanFlag=true;
    private ItemList mFilterItemList;
    private String mFilterCondition;
    private int mFilterPage;
    private boolean noResultFetched=false;
    private HashMap<String, Object> mSectionProperties = new HashMap<>();
    private int pagesize;
    private int firstPos;
    private View full_view;
    private SpaceItemDecoration vSpaceItemDecoration;
    private SpaceItemDecoration hSpaceItemDecoration;
    private int checkedTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_layout);
        //获取intent传递的数据，从而判断加载的列表页的类型
        Intent intent=getIntent();
        title = intent.getStringExtra("title");
        channel = intent.getStringExtra("channel");
        int style = intent.getIntExtra("style",0);
        isVertical=style==1?false:true;
        //view和data、监听事件的初始化操作
        initView();
        initListener();
        initData();

    }


    @Override
    protected void onResume() {
        super.onResume();
        //日志相关
        AppConstant.purchase_entrance_page = "list";
        AppConstant.purchase_page = "list";
        BaseActivity.baseChannel="";
        BaseActivity.baseSection="";

        HashMap<String, Object> properties = new HashMap<>();
        properties.put(EventProperty.CATEGORY, channel);
        properties.put(EventProperty.TITLE, title);

        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_CHANNEL_IN, properties);
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
        full_view = findView(R.id.full_view);
        filter_root_view.setxBoundary(getResources().getDimensionPixelOffset(R.dimen.filter_layout_left_view_tab_w));
        tab_arrow_up.setOnHoverListener(this);
        tab_arrow_dowm.setOnHoverListener(this);
        poster_arrow_up.setOnHoverListener(this);
        poster_arrow_down.setOnHoverListener(this);
        tab_arrow_up.setOnClickListener(this);
        tab_arrow_dowm.setOnClickListener(this);
        poster_arrow_up.setOnClickListener(this);
        poster_arrow_down.setOnClickListener(this);
        full_view.setOnHoverListener(this);

        RelativeLayout.LayoutParams recyclerParam= (RelativeLayout.LayoutParams) list_poster_recyclerview.getLayoutParams();
        if(isVertical){
            recyclerParam.setMargins(0,getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_recyclerview_vmt),getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_recyclerview_mr),0);
            RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_arrow_w),getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_arrow_h));
            params.rightMargin=getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_arrow_mr);
            params.addRule(ALIGN_PARENT_RIGHT);
            params.addRule(ALIGN_PARENT_BOTTOM);
            poster_arrow_down.setBackgroundResource(R.drawable.poster_arrow_down_vselector);
            poster_arrow_down.setLayoutParams(params);
        }else{
            recyclerParam.setMargins(0,getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_recyclerview_hmt),getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_recyclerview_mr),0);
        }
        list_poster_recyclerview.setLayoutParams(recyclerParam);
    }

    private void initListener() {
        filter_tab.setOnClickListener(this);
        filter_tab.setOnHoverListener(this);
        filter_tab.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    filter_tab.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.filter_layout_left_view_tab_ts_scaled));
                    if(filter_tab.isChecked()){
                        return;
                    }
                    if(!filter_root_view.horving) {
                        Message msg = new Message();
                        msg.what = 0;
                        msg.obj = v;
                        mHandler.sendMessageDelayed(msg, 500);
                    }
                }else{
                    mHandler.removeMessages(0);
                    filter_tab.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.filter_layout_left_view_tab_ts));
                }
            }
        });
        filter_tab.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    poster_recyclerview.setVisibility(View.VISIBLE);
                    list_poster_recyclerview.setVisibility(View.GONE);
                    if(mFilterItemList==null){
                        full_view.setVisibility(View.VISIBLE);
                        full_view.requestFocus();
                        mFilterItemList=new ItemList();
                        mFilterItemList.objects=new ArrayList<>();
                        fetchFilterCondition(channel);
                    }else {
                        filter();
                    }

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
                    YoYo.with(Techniques.VerticalShake).duration(1000).playOn(v);
                    return true;
                }
                return false;
            }
        });
        poster_recyclerview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                //海报区上下箭头是否显示
                if(mFilterFocusGridLayoutManager!=null) {
                    if (mFilterFocusGridLayoutManager.findFirstVisibleItemPosition() == 0) {
                        filter_root_view.setShow_right_up(false);
                    } else {
                        filter_root_view.setShow_right_up(true);
                    }
                    if(filterPosterAdapter!=null&&mFilterFocusGridLayoutManager.findLastVisibleItemPosition()==filterPosterAdapter.getItemCount()-1){
                        filter_root_view.setShow_right_down(false);
                    }else{
                        filter_root_view.setShow_right_down(true);
                    }
                    if(mFilterFocusGridLayoutManager.findLastVisibleItemPosition()==mFilterItemList.objects.size()-1&&mFilterFocusGridLayoutManager.findLastVisibleItemPosition()!=-1){
                        if(mFilterPage+1<=mFilterItemList.num_pages) {
                            fetchFilterResult(content_model, mFilterCondition, mFilterPage + 1);
                        }
                    }
                }
            }
        });
        list_poster_recyclerview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                //通过滑动停止后判断当前所在列表位置，进行数据更新操作(列表数据更新、列表区title更新、当前选中tab更新)
                if(mFocusGridLayoutManager!=null) {
                    int firstVisiablePos = mFocusGridLayoutManager.findFirstVisibleItemPosition();
                    int lastVisiablePos = mFocusGridLayoutManager.findLastVisibleItemPosition();
                    //海报区上下箭头是否显示
                    if(firstVisiablePos==0){
                        filter_root_view.setShow_right_up(false);
                    }else{
                        filter_root_view.setShow_right_up(true);
                    }
                    if (lastVisiablePos == mAllSectionItemList.getCount()-1) {
                        filter_root_view.setShow_right_down(false);
                    } else {
                        filter_root_view.setShow_right_down(true);
                    }
                    if(poster_arrow_up.isFocused()||poster_arrow_down.isFocused()) {
                        changeCheckedTab(firstVisiablePos);
                    }
                    showData(firstVisiablePos,true);
                    showData(lastVisiablePos,false);
                    for (int i = 0; i < sectionSize; i++) {
                        if (i == sectionSize - 1) {
                            if(firstVisiablePos>=specialPos.get(i)){
                                current_section_title.setText(sectionList.get(i).title);
                            }
                            break;
                        }
                        if (firstVisiablePos >= specialPos.get(i) && firstVisiablePos < specialPos.get(i + 1)) {
                            if (current_section_title.getText() != null && !sectionList.get(i).title.equals(current_section_title.getText()))
                                current_section_title.setText(sectionList.get(i).title);
                        }
                    }
                }
            }
        });
        tab_scroll.setOnScroll(new FullScrollView.OnScroll() {
            @Override
            public void onShowUp(boolean showUp) {
                filter_root_view.setShow_left_up(showUp);
            }

            @Override
            public void onShowDown(boolean showDown) {
                filter_root_view.setShow_left_down(showDown);
            }
        });
    }

    private void changeCheckedTab(int position) {
        for (int i = 0; i < sectionSize; i++) {
            if (i == sectionSize - 1) {
                if(specialPos!=null&&position>=specialPos.get(i)){
                    ((RadioButton) section_group.getChildAt(i + 1)).setChecked(true);
                }
                break;
            }
            if (specialPos != null && position >= specialPos.get(i) && position < specialPos.get(i + 1)) {

                ((RadioButton) section_group.getChildAt(i + 1)).setChecked(true);
                if (section_group.getChildAt(i + 1).getY() + section_group.getChildAt(i + 1).getHeight() > tab_scroll.getScrollY() + tab_scroll.getHeight()) {
                    tab_scroll.smoothScrollBy(0, getResources().getDimensionPixelOffset(R.dimen.list_scroll_arrow_down_lenth));
                } else if (section_group.getChildAt(i + 1).getY() < tab_scroll.getScrollY()) {
                    tab_scroll.smoothScrollBy(0, getResources().getDimensionPixelOffset(R.dimen.list_scroll_arrow_up_lenth));
                }
                break;
            }
        }
    }

    private void initData() {
        filter_title.setText(title);
        filter_checked_conditiion.setVisibility(View.INVISIBLE);
        poster_recyclerview.setVisibility(View.GONE);
        list_poster_recyclerview.setVisibility(View.VISIBLE);
        if(("payment".equals(channel)||"shiyunshop".equals(channel))){
            filter_tab.setVisibility(View.GONE);
        }
        fetchChannelSection(channel);
        //初始化筛选头部view
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
        filter_checked_conditiion.setVisibility(View.VISIBLE);
    }

    //请求list的所有栏目
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
                        //筛选出分类下有内容的栏目，去除内容个数为0的栏目
                        sectionList=new SectionList();
                        for (int i = 0; i <sections.size() ; i++) {
                            if(sections.get(i).count!=0){
                                sectionList.add(sections.get(i));
                            }
                        }
                        //创建是否请求过栏目数据的记录数组并进行初始化
                        sectionSize = sectionList.size();
                        sectionHasData = new boolean[sectionSize];
                        for (int i = 0; i <sectionSize; i++) {
                            sectionHasData[i]=false;
                        }
                        //填充栏目界面
                        fillSections(sectionList);
                    }
                });


    }

    //请求单个栏目的数据
    private void fetchSectionData(String url, final int index, final boolean isFirstPos) {
        sectionHasData[index]=true;
        mSkyService.getListChannel(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ListSectionEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        sectionHasData[index]=false;
                        super.onError(e);
                    }

                    @Override
                    public void onNext(ListSectionEntity listSectionEntity) {
                        if(isFirst){
                            isFirst=false;
                        }
                        //将请求到的栏目数据替换之前占位的空数据
                        for (int i = 0; i <listSectionEntity.getObjects().size() ; i++) {
                            if(mAllSectionItemList.getCount()>specialPos.get(index)+i+1)
                                mAllSectionItemList.getObjects().set(specialPos.get(index)+i+1,listSectionEntity.getObjects().get(i));
                        }
                        //将占位和实际请求到的数据大小进行修正，并修正标题所在的位置
                        int removeCount=0;
                        if(index!=specialPos.size()-1&&specialPos.get(index)+listSectionEntity.getCount()<specialPos.get(index+1)-1) {
                                List<ListSectionEntity.ObjectsBean> list1=mAllSectionItemList.getObjects().subList(0,specialPos.get(index) + listSectionEntity.getCount()+1);
                                List<ListSectionEntity.ObjectsBean> list2=mAllSectionItemList.getObjects().subList(specialPos.get(index+1),mAllSectionItemList.getCount());
                                mAllSectionItemList.setObjects(new ArrayList<ListSectionEntity.ObjectsBean>());
                                mAllSectionItemList.getObjects().addAll(list1);
                                mAllSectionItemList.getObjects().addAll(list2);
                                removeCount=specialPos.get(index+1)-1-specialPos.get(index)-listSectionEntity.getCount();
                                mAllSectionItemList.setCount(mAllSectionItemList.getObjects().size());
                            Log.e("removecount", removeCount + "");
                            for (int i = index + 1; i < sectionSize; i++) {
                                specialPos.set(i, specialPos.get(i) - removeCount);
                            }
                                if(vSpaceItemDecoration!=null)
                                vSpaceItemDecoration.setSpecialPos(specialPos);
                                if(hSpaceItemDecoration!=null)
                                hSpaceItemDecoration.setSpecialPos(specialPos);
                                if(listPosterAdapter!=null)
                                    listPosterAdapter.setmSpecialPos(specialPos);
                                if(mFocusGridLayoutManager!=null)
                                mFocusGridLayoutManager.setSpecialPos(specialPos);
                        }else if(index==sectionSize-1&&mAllSectionItemList.getCount()>specialPos.get(index-1)+listSectionEntity.getCount()){
                            List<ListSectionEntity.ObjectsBean> list1=mAllSectionItemList.getObjects().subList(0,specialPos.get(index) + listSectionEntity.getCount()+1);
                            mAllSectionItemList.setObjects(new ArrayList<ListSectionEntity.ObjectsBean>());
                            mAllSectionItemList.getObjects().addAll(list1);
                            removeCount=mAllSectionItemList.getCount()-mAllSectionItemList.getObjects().size();
                            mAllSectionItemList.setCount(mAllSectionItemList.getObjects().size());
                        }else{
                            removeCount=0;
                        }
                        //数据大小修正之后填充海报区内容
                        processResultData(mAllSectionItemList,index,removeCount,isFirstPos);
                    }
                });


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
                        fetchFilterResult(filterConditions.getContent_model(),filterConditions.getDefaultX(),1);
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
                if(filterNoResult){
                    if(filter_noresult_first_line!=null&&filter_noresult_first_line.getChildAt(0)!=null)
                    filter_noresult_first_line.getChildAt(0).requestFocus();
                }else {
                    if (poster_recyclerview.getChildAt(0) != null) {
                        poster_recyclerview.getChildAt(0).requestFocus();
                    }
                }
                filter_tab.setFocusable(true);
                full_view.setVisibility(View.GONE);
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
        pagesize = 8;
        if(isVertical){
            pagesize =5;
        }
        mSkyService.getFilterRecommend(channel, IsmartvActivator.getInstance().getSnToken() ,pagesize).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<FilterNoresultPoster>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        noResultFetched=false;
                        super.onError(e);
                    }

                    @Override
                    public void onNext(List<FilterNoresultPoster> items) {
                        if(items!=null){
                            noResultFetched=true;
                            filter_noresult_first_line = (LinearLayout) filter_noresult.findViewById(R.id.filter_noresult_first_line);
                            filter_noresult_second_line = (LinearLayout) filter_noresult.findViewById(R.id.filter_noresult_second_line);
                            filter_noresult_first_line.removeAllViews();
                            filter_noresult_second_line.removeAllViews();
                            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            if(isVertical){
                                for (int i = 0; i <5 ; i++) {
                                    final FilterNoresultPoster item = items.get(i);
                                    if(item!=null){
                                        final View recommendView= View.inflate(FilterListActivity.this,R.layout.filter_item_vertical_poster,null);
                                        recommendView.setId(R.layout.filter_item_vertical_poster+i);
                                        PosterUtil.fillPoster(FilterListActivity.this,0,item,(ImageView)recommendView.findViewById(R.id.item_vertical_poster_img),(ImageView)recommendView.findViewById(R.id.item_vertical_poster_vip),(TextView)recommendView.findViewById(R.id.item_vertical_poster_mark),(TextView)recommendView.findViewById(R.id.item_vertical_poster_title),null);
                                        recommendView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                            @Override
                                            public void onFocusChange(View v, boolean hasFocus) {
                                                TextView title= (TextView) recommendView.findViewById(R.id.item_vertical_poster_title);
                                                if(hasFocus){
                                                    JasmineUtil.scaleOut3(v);
                                                    title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                                    title.setSelected(true);
                                                }else{
                                                    JasmineUtil.scaleIn3(v);
                                                    title.setEllipsize(TextUtils.TruncateAt.END);
                                                    title.setSelected(true);
                                                }
                                            }
                                        });
                                        recommendView.setOnHoverListener(FilterListActivity.this);
                                        recommendView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                new PageIntent().toDetailPage(FilterListActivity.this,Source.LIST.getValue(),item.getPk());
                                            }
                                        });
                                        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        p.rightMargin = getResources().getDimensionPixelOffset(R.dimen.filter_noresult_poster_vertical_mr);
                                        recommendView.setLayoutParams(p);
                                        recommendView.setNextFocusUpId(R.layout.filter_item_vertical_poster+i);
                                        recommendView.setNextFocusDownId(R.layout.filter_item_vertical_poster+i);
                                        if(i==0){
                                            recommendView.setNextFocusLeftId(R.id.filter_tab);
                                        }
                                        filter_noresult_first_line.addView(recommendView);
                                    }

                                }
                                params.topMargin=getResources().getDimensionPixelOffset(R.dimen.filter_noresult_vertical_mt);
                                params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.filter_noresult_vertical_ml);
                                filter_noresult_first_line.setLayoutParams(params);
                            }else{
                                for (int i = 0; i <8 ; i++) {
                                    final FilterNoresultPoster item = items.get(i);
                                    if(item!=null) {
                                        final View recommendView= View.inflate(FilterListActivity.this,R.layout.item_filter_noresult_poster,null);
                                        recommendView.setId(R.layout.item_filter_noresult_poster+i);
                                        PosterUtil.fillPoster(FilterListActivity.this,1,item,(ImageView)recommendView.findViewById(R.id.item_filter_noresult_img),(ImageView)recommendView.findViewById(R.id.item_filter_noresult_vip),(TextView)recommendView.findViewById(R.id.item_filter_noresult_mark),(TextView)recommendView.findViewById(R.id.item_filter_noresult_title),(TextView)recommendView.findViewById(R.id.item_filter_noresult_descrip));
                                        recommendView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                            @Override
                                            public void onFocusChange(View v, boolean hasFocus) {
                                                TextView title= (TextView) recommendView.findViewById(R.id.item_filter_noresult_title);
                                                if(hasFocus){
                                                    JasmineUtil.scaleOut3(v);
                                                    title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                                    title.setSelected(true);
                                                }else{
                                                    JasmineUtil.scaleIn3(v);
                                                    title.setEllipsize(TextUtils.TruncateAt.END);
                                                    title.setSelected(false);
                                                }
                                            }
                                        });
                                        recommendView.setOnHoverListener(FilterListActivity.this);
                                        recommendView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                new PageIntent().toDetailPage(FilterListActivity.this,Source.LIST.getValue(),item.getPk());
                                            }
                                        });
                                        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        p.rightMargin = getResources().getDimensionPixelOffset(R.dimen.filter_noresult_poster_horizontal_mr);
                                        recommendView.setLayoutParams(p);
                                        if(i==0||i==4){
                                            recommendView.setNextFocusLeftId(R.id.filter_tab);
                                        }
                                        if(i<4) {
                                            recommendView.setNextFocusUpId(R.layout.item_filter_noresult_poster+i);
                                            filter_noresult_first_line.addView(recommendView);
                                        }else {
                                            recommendView.setNextFocusDownId(R.layout.item_filter_noresult_poster+i);
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
                });
    }

    /**
     * 根据已选条件请求筛选结果
     * @param content_model
     * @param filterCondition
     */
    private void fetchFilterResult(String content_model, String filterCondition,int page) {
        mFilterCondition = filterCondition;
        mFilterPage = page;
        mSkyService.getFilterRequestHaveData(content_model,filterCondition,page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ItemList>(){


                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(final ItemList itemList) {
                        if(itemList==null||itemList.objects.size()==0){
                            filterNoResult = true;
                            filter_root_view.setShow_right_up(false);
                            filter_root_view.setShow_right_down(false);
                            if(noResultFetched){
                                filter_noresult.setVisibility(View.VISIBLE);
                                poster_recyclerview.setVisibility(View.GONE);
                            }else{
                                filter_noresult.setVisibility(View.VISIBLE);
                                poster_recyclerview.setVisibility(View.GONE);
                                fetchFilterNoResult();
                            }
                            poster_recyclerview.setVisibility(View.GONE);
                        }else {
                            filterNoResult = false;
                            mFilterItemList.num_pages=itemList.num_pages;
                            mFilterItemList.objects.addAll(itemList.objects);
                            processResultData(mFilterItemList);
                            filter_noresult.setVisibility(View.GONE);
                            poster_recyclerview.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void processResultData(final ItemList itemList) {
        if(filterPosterAdapter==null) {
            filterPosterAdapter=new FilterPosterAdapter(this,itemList,isVertical);
            poster_recyclerview.swapAdapter(filterPosterAdapter,false);
            filterPosterAdapter.setItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    baseSection="";
                    baseChannel=channel;
                    PageIntent intent = new PageIntent();
                    Item item=itemList.objects.get(position);
                    if(item.content_model!=null&&item.content_model.contains("gather")){
                        intent.toSubject(FilterListActivity.this,item.content_model,item.pk,item.title,Source.FILTER.getValue(),baseChannel);
                    }else if(item.model_name!=null&&item.model_name.equals("package")){
                        intent.toPackageDetail(FilterListActivity.this,Source.FILTER.getValue(),item.pk);
                    }else if(item.is_complex) {
                        intent.toDetailPage(FilterListActivity.this,Source.FILTER.getValue(),item.pk);
                    }else{
                        intent.toPlayPage(FilterListActivity.this,item.pk,0, Source.FILTER);
                    }
                }
            });
            filterPosterAdapter.setItemFocusedListener(new OnItemFocusedListener() {
                @Override
                public void onItemfocused(View view, int position, boolean hasFocus) {
                    if(hasFocus){
                        lastFocusedView = view;
                        if(!filter_root_view.horving) {
                            if (view.getY() > getResources().getDimensionPixelOffset(R.dimen.filter_poster_start_scroll_length)) {
                                mFilterFocusGridLayoutManager.scrollToPositionWithOffset(position, 0);
                            } else if (view.getY() < 0) {
                                if (isVertical) {
                                    mFilterFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_filter_offset_v));
                                } else {
                                    mFilterFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_filter_offset_h));
                                }
                            }
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
            if(lastFocusedView==null){
                filterPosterAdapter.setFocusedPosition(-1);
            }else{
                filterPosterAdapter.setFocusedPosition(poster_recyclerview.getChildLayoutPosition(lastFocusedView));
            }
            filterPosterAdapter.setmItemList(itemList);
            filterPosterAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 根据用户对筛选条件的选择实时更新筛选条件及筛选结果
     */
    private void filter() {
        mFilterItemList.objects.clear();
        mFilterPage=0;
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

        String condition = "";
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
            condition=mFilterConditions.getDefaultX()+"!";
        }


        for (int i = 1; i <filter_checked_conditiion.getChildCount() ; i++) {
            if(filter_checked_conditiion.getChildAt(i)!=null) {
                condition += filter_checked_conditiion.getChildAt(i).getTag().toString() + "!";
            }
        }
        AppConstant.purchase_entrance_keyword = conditionForLog.substring(0,conditionForLog.lastIndexOf(";"));
        fetchFilterResult(content_model,condition.substring(0,condition.lastIndexOf("!")),1);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i==R.id.filter_tab){
            current_section_title.setVisibility(View.INVISIBLE);
            if(filter_checked_conditiion.getChildCount()>1){
                filter_checked_conditiion.setVisibility(View.VISIBLE);
                current_section_title.setVisibility(View.INVISIBLE);
            }
            if(filterPopup!=null&&!filterPopup.isShowing()) {
                full_view.setVisibility(View.VISIBLE);
                filter_tab.setFocusable(false);
                full_view.requestFocus();
                showFilterPopup();
            }
        }else if(i==R.id.tab_arrow_up){
            tab_scroll.scrollBy(0, getResources().getDimensionPixelOffset(R.dimen.list_scroll_arrow_up_lenth));
            firstPos = tab_scroll.getScrollY()/section_group.getChildAt(0).getHeight();
            if(firstPos==0){
                firstPos=1;
            }
            ((RadioButton)section_group.getChildAt(firstPos)).setChecked(true);
            section_group.getChildAt(firstPos).callOnClick();
        }else if(i==R.id.tab_arrow_dowm){
            tab_scroll.scrollBy(0, getResources().getDimensionPixelOffset(R.dimen.list_scroll_arrow_down_lenth));
            firstPos=tab_scroll.getScrollY()/section_group.getChildAt(0).getHeight();
            ((RadioButton)section_group.getChildAt(firstPos)).setChecked(true);
            section_group.getChildAt(firstPos).callOnClick();

        }else if(i==R.id.poster_arrow_up){
            if(filter_tab.isChecked()){
                if (isVertical) {
                    mFilterFocusGridLayoutManager.scrollToPositionWithOffset(mFilterFocusGridLayoutManager.findFirstVisibleItemPosition() - 1, getResources().getDimensionPixelOffset(R.dimen.list_scroll_filter_offset_v));
                } else {
                    mFilterFocusGridLayoutManager.scrollToPositionWithOffset(mFilterFocusGridLayoutManager.findFirstVisibleItemPosition() - 1, getResources().getDimensionPixelOffset(R.dimen.list_scroll_filter_offset_h));
                }
            }else{
                nextPos = specialPos.contains(mFocusGridLayoutManager.findFirstVisibleItemPosition()) ? mFocusGridLayoutManager.findFirstVisibleItemPosition() - spanCount-1 : mFocusGridLayoutManager.findFirstVisibleItemPosition()-1 ;
                if (isVertical) {
                    mFocusGridLayoutManager.scrollToPositionWithOffset(nextPos, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_v));
                } else {
                    mFocusGridLayoutManager.scrollToPositionWithOffset(nextPos, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_h));
                }
            }
        }else if(i==R.id.poster_arrow_down){
            if(filter_tab.isChecked()){
                mFilterFocusGridLayoutManager.scrollToPositionWithOffset(mFilterFocusGridLayoutManager.findLastVisibleItemPosition(),0);
            }else{
                nextPos =mFocusGridLayoutManager.findLastVisibleItemPosition()+1;
                if(isVertical) {
                    if(specialPos.contains(mFocusGridLayoutManager.findLastVisibleItemPosition())){
                        mFocusGridLayoutManager.scrollToPositionWithOffset(nextPos-1, 0);
                    }else {
                        mFocusGridLayoutManager.scrollToPositionWithOffset(nextPos, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_v));
                    }
                }else{
                    if(specialPos.contains(mFocusGridLayoutManager.findLastVisibleItemPosition())){
                        mFocusGridLayoutManager.scrollToPositionWithOffset(nextPos-1, 0);
                    }else {
                        mFocusGridLayoutManager.scrollToPositionWithOffset(nextPos, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_h));
                    }
                }
            }
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
            v.requestFocus();
            lastFocusedView=null;
            if (filterPosterAdapter != null)
                filterPosterAdapter.setFocusedPosition(-1);

        }
        return true;
    }

    /**
     * 更新list数据区
     * @param position
     */
    private void showData(int position,boolean isFirstPos){
        Log.e("showdata",position+"");
        for (int i = 0; i <sectionSize ; i++) {
                if(i!=sectionSize-1){
                    booleanFlag=position<specialPos.get(i+1);
                }else{
                    booleanFlag=true;
                }
                if(position>=specialPos.get(i)&&booleanFlag&&!sectionHasData[i]){
                    if(position==specialPos.get(i)){
                        isFirstPos=false;
                    }
                    fetchSectionData(sectionList.get(i).url,i,isFirstPos);
                }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //日志
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(EventProperty.CATEGORY, channel);
        properties.put(EventProperty.TITLE, title);
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_CHANNEL_OUT, properties);
    }


    //填充list分类列表
    private boolean isFirst=true;
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
                        if(listPosterAdapter!=null)
                        listPosterAdapter.setFocusedPosition(-1);
                        radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.filter_layout_left_view_tab_ts_scaled));
                        radioButton.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                        if(radioButton.isChecked()){
                            return;
                        }
                        if(!filter_root_view.horving) {
                            Message msg = new Message();
                            msg.obj = v;
                            msg.what = finalI1 + 1;
                            mHandler.sendMessageDelayed(msg, 500);
                        }
                    }else{
                        mHandler.removeMessages(finalI1+1);
                        radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.filter_layout_left_view_tab_ts));
                        radioButton.setEllipsize(TextUtils.TruncateAt.END);
                    }
                }
            });
            final int finalI = i;
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!section.title.equals(current_section_title.getText())) {
                        mFocusGridLayoutManager.scrollToPositionWithOffset(specialPos.get(finalI), 0);
                    }
                    if(isFirst) {
                            fetchSectionData(section.url, finalI, false);
                    }
                    current_section_title.setText(sectionList.get(finalI).title);
                }
            });
            radioButton.setOnHoverListener(this);
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        checkedTab = finalI;
                        if(filterPopup!=null&&filterPopup.isShowing())
                            filterPopup.dismiss();
                        filter_checked_conditiion.setVisibility(View.INVISIBLE);
                        current_section_title.setVisibility(View.VISIBLE);
                        filter_checked_conditiion.requestLayout();
                        if(mFocusGridLayoutManager!=null){
                            mFocusGridLayoutManager.setLeftFocusView(radioButton);
                        }
                        filter_root_view.setShow_right_up(true);
                        //日志
                        mSectionProperties.put(EventProperty.SECTION, sectionList.get(finalI).slug);
                        mSectionProperties.put(EventProperty.TITLE, sectionList.get(finalI).title);
                        mSectionProperties.put(EventProperty.SOURCE,"list");
                        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_CATEGORY_IN, mSectionProperties);
                    }else{
                        mSectionProperties.put(EventProperty.SECTION, sectionList.get(finalI).slug);
                        mSectionProperties.put(EventProperty.TITLE, sectionList.get(finalI).title);
                        mSectionProperties.put(EventProperty.SOURCE,"list");
                        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_CATEGORY_OUT, mSectionProperties);
                    }
                }
            });
            if(i==sections.size()-1){
                radioButton.setNextFocusDownId(R.id.section_radiobtn+i);
            }
            final int[] location=new int[2];
            radioButton.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    v.getLocationOnScreen(location);
                    if(event.getAction()==KeyEvent.ACTION_DOWN) {
                        if (keyCode == 20) {
                            if(finalI == sectionSize - 1 ){
                                YoYo.with(Techniques.VerticalShake).duration(1000).playOn(v);
                                return true;
                            }else if(location[1]==tab_scroll.getBottom()-v.getHeight()){
                                tab_scroll.scrollBy(0, getResources().getDimensionPixelOffset(R.dimen.list_scroll_arrow_down_lenth));
                            }

                        } else if (keyCode == 19) {
                            if(location[1]==tab_scroll.getTop()){
                                tab_scroll.scrollBy(0, getResources().getDimensionPixelOffset(R.dimen.list_scroll_arrow_up_lenth));
                            }

                        }else if (keyCode == 22 ) {
                            if (lastFocusedView != null) {
                                lastFocusedView.requestFocus();
                            } else {
                                View firstView = null;
                                if(finalI==sectionSize-1&&mAllSectionItemList.getCount()-specialPos.get(finalI)<spanCount){
                                    firstView=mFocusGridLayoutManager.findViewByPosition(specialPos.get(finalI)+1);
                                }else if(list_poster_recyclerview.getChildCount()>spanCount) {
                                    firstView = list_poster_recyclerview.getChildAt(spanCount);
                                }
                                if(list_poster_recyclerview.getChildAt(0) instanceof TextView){
                                    firstView=list_poster_recyclerview.getChildAt(1);
                                }
                                if (firstView != null) {
                                    firstView.requestFocus();
                                }
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
            section_group.addView(radioButton);
        }
        if(sections.size()<9) {
            filter_root_view.setShow_left_down(false);
        }
        specialPos = new ArrayList<>();
        specialPos.add(0);
        totalItemCount = 0;
        for (int i = 0; i <sections.size() ; i++) {
            totalItemCount +=sections.get(i).count;
            if(i!=sections.size()-1)
                specialPos.add(totalItemCount+i+1);
        }
        //根据不同的板式(横、竖版)设置不同的列表样式
        if(isVertical) {
            spanCount = 5;
            SpaceItemDecoration vFilterSpaceItemDecoration = new SpaceItemDecoration(getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_hs),getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_vs),isVertical);
            poster_recyclerview.addItemDecoration(vFilterSpaceItemDecoration);
            vSpaceItemDecoration = new SpaceItemDecoration(getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_hs),getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_vs),isVertical);
            vSpaceItemDecoration.setSpecialPos(specialPos);
            poster_recyclerview.setPadding(0,0,0,getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_padding_bottom));
            list_poster_recyclerview.addItemDecoration(vSpaceItemDecoration);
            list_poster_recyclerview.setPadding(0,0,0,getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_padding_bottom));
        }else{
            spanCount = 3;
            SpaceItemDecoration hFilterSpaceItemDecoration=new SpaceItemDecoration(getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_hs),getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_vs),isVertical);
            poster_recyclerview.setPadding(0,0,0,getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_padding_bottom));
            poster_recyclerview.addItemDecoration(hFilterSpaceItemDecoration);
            hSpaceItemDecoration = new SpaceItemDecoration(getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_hs),getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_vs),isVertical);
            hSpaceItemDecoration.setSpecialPos(specialPos);
            list_poster_recyclerview.setPadding(0,0,0,getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_padding_bottom));
            list_poster_recyclerview.addItemDecoration(hSpaceItemDecoration);
        }
        mFocusGridLayoutManager = new FocusGridLayoutManager(this,spanCount);
        mFocusGridLayoutManager.setSpecialPos(specialPos);
        mFilterFocusGridLayoutManager = new FocusGridLayoutManager(this,spanCount);
        mFilterFocusGridLayoutManager.setLeftFocusView(filter_tab);
        poster_recyclerview.setLayoutManager(mFilterFocusGridLayoutManager);
        list_poster_recyclerview.setLayoutManager(mFocusGridLayoutManager);
        totalItemCount+=sections.size();
        mAllSectionItemList = new ListSectionEntity();
        mAllSectionItemList.setObjects(new ArrayList<ListSectionEntity.ObjectsBean>());
        for (int i = 0; i <totalItemCount ; i++) {
            ListSectionEntity.ObjectsBean item=new ListSectionEntity.ObjectsBean();
            mAllSectionItemList.getObjects().add(item);
        }
        mAllSectionItemList.setCount(mAllSectionItemList.getObjects().size());
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
        if(section_group.getChildAt(1)!=null) {
            section_group.getChildAt(1).callOnClick();
            ((RadioButton) section_group.getChildAt(1)).setChecked(true);
        }
    }


    /**
     * 处理请求到的列表页海报区数据
     */
    private void processResultData(final ListSectionEntity listSectionEntity,int index,int removeCount,boolean isFirstPos) {
        if(listPosterAdapter==null) {
            listPosterAdapter = new ListPosterAdapter(FilterListActivity.this, listSectionEntity.getObjects(), isVertical, specialPos, sectionList);
            list_poster_recyclerview.swapAdapter(listPosterAdapter,false);
            listPosterAdapter.setFocusedPosition(1);
            //设置海报点击事件监听
            listPosterAdapter.setItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    baseSection=sectionList.get(checkedTab).slug;
                    baseChannel=channel;
                    PageIntent intent = new PageIntent();
                    ListSectionEntity.ObjectsBean item=listSectionEntity.getObjects().get(position);
                    if(item.getContent_model()!=null&&item.getContent_model().contains("gather")){
                        intent.toSubject(FilterListActivity.this,item.getContent_model(),item.getPk(),item.getTitle(),Source.LIST.getValue(),baseChannel);
                    }else if(item.getModel_name()!=null&&item.getModel_name().equals("package")){
                        intent.toPackageDetail(FilterListActivity.this,Source.LIST.getValue(),item.getPk());
                    }else if("item".equals(item.getModel_name())) {
                        intent.toDetailPage(FilterListActivity.this,Source.LIST.getValue(),item.getPk());
                    }else{
                        intent.toPlayPage(FilterListActivity.this,item.getPk(),0, Source.LIST);
                    }
                }
            });
            //设置海报焦点事件监听
            listPosterAdapter.setItemFocusedListener(new OnItemFocusedListener() {
                @Override
                public void onItemfocused(View view, int position, boolean hasFocus) {
                    if(hasFocus){
                        lastFocusedView = view;
                        changeCheckedTab(position);
                        Log.e("onitemfocus", view.getY()+"");
                        if(!filter_root_view.horving) {
                            if (view.getY() > getResources().getDimensionPixelOffset(R.dimen.filter_poster_start_scroll_length)) {
                                if (isVertical) {
                                    mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_v));
                                } else {
                                    mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_h));
                                }
                            } else if (view.getY() < 0) {
                                if (isVertical) {
                                    if(specialPos!=null&&(specialPos.contains(position-1)||specialPos.contains(position-2)||specialPos.contains(position-3)||specialPos.contains(position-4)||specialPos.contains(position-5))){
                                        mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_v));
                                    }else{
                                        mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_v));
                                    }
                                } else {
                                    if(specialPos!=null&&(specialPos.contains(position-1)||specialPos.contains(position-2)||specialPos.contains(position-3))){
                                        mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_h));
                                    }else{
                                        mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_h));
                                    }

                                }
                            }else if(isVertical&&view.getY()>0&&view.getY()<current_section_title.getHeight()){
                                if(specialPos!=null&&(specialPos.contains(position-1)||specialPos.contains(position-2)||specialPos.contains(position-3)||specialPos.contains(position-4)||specialPos.contains(position-5))){
                                    mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_v));
                                }else{
                                    mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_v));
                                }
                            }
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
            int position=-1;
            if(lastFocusedView==null){
                listPosterAdapter.setFocusedPosition(-1);
                position=index+1==specialPos.size()?-1:specialPos.get(index+1)-1;
            }else{
                listPosterAdapter.setFocusedPosition(list_poster_recyclerview.getChildLayoutPosition(lastFocusedView)-removeCount);
                position=list_poster_recyclerview.getChildLayoutPosition(lastFocusedView) - removeCount;
            }
            listPosterAdapter.setmItemList(listSectionEntity.getObjects());
            //修正滚动位置
            if(isFirstPos) {
                if (isVertical) {
                    mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_v));
                } else {
                    mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_h));
                }
            }
            listPosterAdapter.notifyDataSetChanged();
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
