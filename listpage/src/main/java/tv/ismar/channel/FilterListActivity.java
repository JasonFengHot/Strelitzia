package tv.ismar.channel;


import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
//import android.widget.ImageView;
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
import tv.ismar.adapter.SpecialPos;
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
import tv.ismar.app.ui.adapter.OnItemKeyListener;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.listpage.R;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.view.FilterConditionGroupView;
import tv.ismar.view.FilterListRecyclerView;
import tv.ismar.view.FullScrollView;
import tv.ismar.view.LocationRelativeLayout;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;

/**
 * Created by zhangjiqiang on 15-6-18.
 */
public class FilterListActivity extends BaseActivity implements View.OnClickListener, View.OnHoverListener, FilterListRecyclerView.OnRecyclerScrollListener {
    public static final String PICASSO_TAG = "filter_list";
	private final String TAG = this.getClass().getSimpleName();
    private static final long CLICK_BLOCK_TIME = 400;
    private static final long KEY_BLOCK_TIME = 300;
    private static final long KEY_BLOCK_LEFT_RIGHT_TIME = 100;
    private static final long CHECK_CHANGED_DELAYED = 300;
    public static final int HORIZONTAL_SPAN_COUNT = 3;
    public static final int VERTICAL_SPAN_COUNT = 5;

    private TextView filter_title;
    private RadioButton filter_tab;
    private LinearLayout filter_checked_conditiion;
    private FilterListRecyclerView poster_recyclerview;
    private FilterListRecyclerView list_poster_recyclerview;
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
    private ArrayList<SpecialPos> specialPos;
    private int totalItemCount;
    private ListSectionEntity mAllSectionItemList;
    private SectionList sectionList;
    private int sectionSize;
    private boolean[] sectionHasData;
    private TextView current_section_title;
    private View filter_noresult;
    private Button tab_arrow_up;
    private Button tab_arrow_down;
    private Button poster_arrow_up;
    private Button poster_arrow_down;
    private LocationRelativeLayout filter_root_view;
    private Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //?????????????????????tab????????????1s?????????????????????
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
    private String section="";
    private int firstInSection=-1;
    private View onKeyFocusView;
    private long lastClickTime = 0;
    private long lastKeyDownTime = 0 ;
    private Handler mClickRadioBtnHandler = null;
    private CheckedChangedRunnable mCheckedChangedRunnable;
    private int itemHeight = -1;
    private int onePageScrollY = -1;
    private boolean needRequestListFocusWhenGetData = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_layout);
        //??????intent?????????????????????????????????????????????????????????
        Intent intent=getIntent();
        title = intent.getStringExtra("title");
        channel = intent.getStringExtra("channel");
        int style = intent.getIntExtra("style",0);
        section=intent.getStringExtra("section");
        isVertical=style==1?false:true;
        mClickRadioBtnHandler = new Handler();
        //view???data?????????????????????????????????
        initView();
        initListener();
        initData();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //????????????
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
        //??????tab???view
        filter_title = (TextView) findViewById(R.id.filter_title);
        tab_scroll = (FullScrollView) findViewById(R.id.tab_scroll);
        section_group = (RadioGroup) findViewById(R.id.section_group);
        filter_tab = (RadioButton) findViewById(R.id.filter_tab);
        //?????????view
        filter_checked_conditiion = (LinearLayout) findViewById(R.id.filter_checked_conditiion);
        poster_recyclerview = (FilterListRecyclerView) findViewById(R.id.poster_recyclerview);
        poster_recyclerview.setHasFixedSize(true);
        filter_condition_layout = View.inflate(this, R.layout.filter_condition_layout,null);
        filter_conditions = (LinearLayout)filter_condition_layout.findViewById(R.id.filter_conditions);
        filter_noresult = findViewById(R.id.filter_noresult);
        //?????????view
        list_poster_recyclerview = (FilterListRecyclerView) findViewById(R.id.list_poster_recyclerview);
        list_poster_recyclerview.setHasFixedSize(true);
        current_section_title = (TextView) findViewById(R.id.current_section_title);
        //????????????view
        filter_root_view = (LocationRelativeLayout) findViewById(R.id.filter_root_view);
        tab_arrow_up = (Button)findViewById(R.id.tab_arrow_up);
        tab_arrow_down = (Button)findViewById(R.id.tab_arrow_down);
        poster_arrow_up = (Button)findViewById(R.id.poster_arrow_up);
        poster_arrow_down = (Button)findViewById(R.id.poster_arrow_down);
        filter_root_view.setArrow_up_left(tab_arrow_up);
        filter_root_view.setArrow_down_left(tab_arrow_down);
        filter_root_view.setArrow_up_right(poster_arrow_up);
        filter_root_view.setArrow_down_right(poster_arrow_down);
        full_view = findView(R.id.full_view);
        filter_root_view.setxBoundary(getResources().getDimensionPixelOffset(R.dimen.filter_layout_left_view_tab_w));
    }

    private void initListener() {
        tab_arrow_up.setOnHoverListener(this);
        tab_arrow_down.setOnHoverListener(this);
        poster_arrow_up.setOnHoverListener(this);
        poster_arrow_down.setOnHoverListener(this);
        poster_recyclerview.setOnHoverListener(this);
        list_poster_recyclerview.setOnHoverListener(this);
        tab_arrow_up.setOnClickListener(this);
        tab_arrow_down.setOnClickListener(this);
        poster_arrow_up.setOnClickListener(this);
        poster_arrow_down.setOnClickListener(this);
        full_view.setOnHoverListener(this);
        poster_recyclerview.setScrollListener(this);
        list_poster_recyclerview.setScrollListener(this);
            /*
         * ?????????????????????????????????????????????????????????????????????view???
         * ???????????????????????????????????????????????????view
         * ????????????????????????????????????view?????????????????????????????????????????????????????????????????????????????????????????????
         */
        filter_root_view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                try {
                    if(keyCode==19||keyCode==20||keyCode==21||keyCode==22) {
                        if (v.hasFocus()) {
/*modify by dragontec for bug 4267 start*/
//                            if (onKeyFocusView != v && onKeyFocusView != tab_arrow_up && onKeyFocusView != tab_arrow_down && onKeyFocusView != poster_arrow_up && onKeyFocusView != poster_arrow_down) {
                            if (onKeyFocusView != null && onKeyFocusView != v && onKeyFocusView != tab_arrow_up && onKeyFocusView != tab_arrow_down && onKeyFocusView != poster_arrow_up && onKeyFocusView != poster_arrow_down) {
/*modify by dragontec for bug 4267 end*/
                                onKeyFocusView.requestFocus();
                                onKeyFocusView.requestFocusFromTouch();
                            } else {
                                //???????????????????????????????????????????????????????????????????????????
                                if (filter_tab.isChecked()) {
                                    if (poster_recyclerview != null && poster_recyclerview.getChildAt(0) != null) {
                                        poster_recyclerview.getChildAt(0).requestFocus();
                                    } else if (noResultFetched && filter_noresult_first_line != null && filter_noresult_first_line.getChildAt(0) != null) {
/*modify by dragontec for bug 4267 start*/
//                                        filter_noresult_first_line.getChildAt(0).requestFocus();
                                        View firstChild = filter_noresult_first_line.getChildAt(0);
                                        if (firstChild != null) {
                                            firstChild.requestFocus();
                                        }
/*modify by dragontec for bug 4267 end*/
                                    }
                                } else {
                                    View firstView = null;
                                    if (checkedTab == sectionSize - 1 && mAllSectionItemList.getCount() - specialPos.get(checkedTab).startPosition < spanCount) {
                                        firstView = mFocusGridLayoutManager.findViewByPosition(specialPos.get(checkedTab).startPosition + 1);
                                    } else if (list_poster_recyclerview.getChildCount() > spanCount) {
                                        firstView = list_poster_recyclerview.getChildAt(spanCount);
                                    }
                                    if (list_poster_recyclerview.getChildAt(0) instanceof TextView) {
                                        firstView = list_poster_recyclerview.getChildAt(1);
                                    }
/*modify by dragontec for bug 4267 start*/
//                                    firstView.requestFocus();
                                    if (firstView != null) {
                                        firstView.requestFocus();
                                    }
/*modify by dragontec for bug 4267 end*/
                                }
                            }
                            return true;
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });

        RelativeLayout.LayoutParams recyclerParam= (RelativeLayout.LayoutParams) list_poster_recyclerview.getLayoutParams();
        if(isVertical){
            recyclerParam.setMargins(getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_vertical_recyclerview_ml),0,0,0);
            RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_arrow_w),getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_arrow_h));
            params.rightMargin=getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_arrow_mr);
            params.addRule(ALIGN_PARENT_RIGHT);
            params.addRule(ALIGN_PARENT_BOTTOM);
            poster_arrow_down.setBackgroundResource(R.drawable.poster_arrow_down_vselector);
            poster_arrow_down.setLayoutParams(params);
        }else{
            recyclerParam.setMargins(getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_horizontal_recyclerview_ml),0,0,0);
        }
        list_poster_recyclerview.setLayoutParams(recyclerParam);
        poster_recyclerview.setLayoutParams(recyclerParam);
        list_poster_recyclerview.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    filter_root_view.requestFocus();
                    filter_root_view.requestFocusFromTouch();
                }
            }
        });
        filter_tab.setOnClickListener(this);
        filter_tab.setOnHoverListener(this);
        filter_tab.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    onKeyFocusView=v;
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
                    current_section_title.setText("");
                    poster_recyclerview.setVisibility(View.VISIBLE);
                    list_poster_recyclerview.setVisibility(View.GONE);
                    if(mFilterItemList==null){
                        full_view.setVisibility(View.VISIBLE);
                        filter_tab.setBackgroundResource(R.drawable.section_checked_tab_selector);
                        full_view.requestFocus();
                        mFilterItemList=new ItemList();
                        mFilterItemList.objects=new ArrayList<>();
                        filter_root_view.setShow_right_up(false);
                        filter_root_view.setShow_right_down(false);
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
                //?????????????????????????????????

            }
        });
        list_poster_recyclerview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

            }
        });
        tab_scroll.setOnScroll(new FullScrollView.OnScroll() {
            @Override
            public void onShowUp(boolean showUp) {
                filter_root_view.setShow_left_up(showUp);
            }

            @Override
            public void onShowDown(boolean showDown) {
                int onePageMaxCount = 9;
                for (int i = 0; i < section_group.getChildCount(); i++) {
                    if(section_group.getChildAt(i).getVisibility() == View.GONE){
                        onePageMaxCount ++;
                    }
                }
                if(section_group.getChildCount()<=onePageMaxCount) {
                    filter_root_view.setShow_left_down(false);
                }else{
                    filter_root_view.setShow_left_down(showDown);
                }
            }
        });
    }

    private void updateArrowState() {
        if (filter_tab.isChecked()) {
            if(filterPosterAdapter != null && mFilterFocusGridLayoutManager != null && filterPosterAdapter.getItemCount() > 0) {
                int first = mFilterFocusGridLayoutManager.findFirstCompletelyVisibleItemPosition();
                int last = mFilterFocusGridLayoutManager.findLastCompletelyVisibleItemPosition();
                if (first != -1 && first > 0) {
                    filter_root_view.setShow_right_up(true);
                } else {
                    filter_root_view.setShow_right_up(false);
                }
                if (last != -1 && last < filterPosterAdapter.getItemCount() - 1) {
                    filter_root_view.setShow_right_down(true);
                } else {
                    filter_root_view.setShow_right_down(false);
                }
            }else{
                filter_root_view.setShow_right_up(false);
                filter_root_view.setShow_right_down(false);
            }
        }else{
            if(listPosterAdapter != null && mFocusGridLayoutManager != null && listPosterAdapter.getItemCount() > 0) {
                int first = mFocusGridLayoutManager.findFirstCompletelyVisibleItemPosition();
                int last = mFocusGridLayoutManager.findLastCompletelyVisibleItemPosition();

                if (first != -1 && first > 0) {
                    filter_root_view.setShow_right_up(true);
                } else {
                    filter_root_view.setShow_right_up(false);
                }
                if (last != -1 && last < listPosterAdapter.getItemCount() - 1) {
                    filter_root_view.setShow_right_down(true);
                } else {
                    filter_root_view.setShow_right_down(false);
                }
            }else{
                filter_root_view.setShow_right_up(false);
                filter_root_view.setShow_right_down(false);
            }
        }
    }

    private void uninitListener() {
        tab_arrow_up.setOnHoverListener(null);
        tab_arrow_down.setOnHoverListener(null);
        poster_arrow_up.setOnHoverListener(null);
        poster_arrow_down.setOnHoverListener(null);
        poster_recyclerview.setOnHoverListener(null);
        list_poster_recyclerview.setOnHoverListener(null);
        tab_arrow_up.setOnClickListener(null);
        tab_arrow_down.setOnClickListener(null);
        poster_arrow_up.setOnClickListener(null);
        poster_arrow_down.setOnClickListener(null);
        full_view.setOnHoverListener(null);
        poster_recyclerview.setScrollListener(null);
        list_poster_recyclerview.setScrollListener(null);
    }

    private void updateCurrentTab() {
        //??????????????????????????????????????????????????????????????????????????????(??????????????????????????????title?????????????????????tab??????)
        if(mFocusGridLayoutManager!=null) {
            int firstVisiablePos = mFocusGridLayoutManager.findFirstVisibleItemPosition();
            int lastVisiablePos = mFocusGridLayoutManager.findLastVisibleItemPosition();
            updateArrowState();
            if(poster_arrow_up.isFocused()||poster_arrow_down.isFocused()||filter_root_view.isFocused()) {
                    	/*modify by dragontec for bug 4468 start*/
                if (mFocusGridLayoutManager.findLastVisibleItemPosition() == mFocusGridLayoutManager.getItemCount() - 1) {
                    changeCheckedTab(mFocusGridLayoutManager.findLastVisibleItemPosition());
                } else {
                    changeCheckedTab(mFocusGridLayoutManager.findFirstCompletelyVisibleItemPosition());
                }
						/*modify by dragontec for bug 4468 end*/
            }
            showData(firstVisiablePos,true);
            showData(lastVisiablePos,false);
            for (int i = 0; i < sectionSize; i++) {
                if (i == sectionSize - 1) {
                    if(isVertical&&mFocusGridLayoutManager.findFirstCompletelyVisibleItemPosition()>=specialPos.get(i).startPosition){
                        current_section_title.setText(sectionList.get(i).title);
                    }else if(mFocusGridLayoutManager.findFirstCompletelyVisibleItemPosition()>specialPos.get(i).startPosition){
                        current_section_title.setText(sectionList.get(i).title);
                    }
                    break;
                }
                if (firstVisiablePos >= specialPos.get(i).startPosition && firstVisiablePos < specialPos.get(i + 1).startPosition) {
                    if (current_section_title.getText() != null && !sectionList.get(i).title.equals(current_section_title.getText()))
                        current_section_title.setText(sectionList.get(i).title);
                }
            }
        }
    }

    private void changeCheckedTab(int position) {
        try {
            for (int i = 0; i < sectionSize; i++) {
                if (i == sectionSize - 1) {
                    if (specialPos != null && position >= specialPos.get(i).startPosition) {
                        ((RadioButton) section_group.getChildAt(i + 1)).setChecked(true);
                        tab_scroll.smoothScrollTo(0, (int) section_group.getChildAt(i + 1).getY());
                    }
                    break;
                }
                if (specialPos != null && position >= specialPos.get(i).startPosition && position < specialPos.get(i + 1).startPosition) {

                    ((RadioButton) section_group.getChildAt(i + 1)).setChecked(true);
                    if (section_group.getChildAt(i + 1).getY() + section_group.getChildAt(i + 1).getHeight() > tab_scroll.getScrollY() + tab_scroll.getHeight()) {
                        tab_scroll.smoothScrollTo(0, (int) section_group.getChildAt(i + 1).getY());
                    } else if (section_group.getChildAt(i + 1).getY() < tab_scroll.getScrollY()) {
                        tab_scroll.smoothScrollBy(0, getResources().getDimensionPixelOffset(R.dimen.list_scroll_arrow_up_lenth));
                    }
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
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
        //?????????????????????view
        TextView checked= new TextView(this);
        checked.setBackgroundResource(R.drawable.filter_condition_checked2);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_h));
        params.rightMargin=getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_mr);
        checked.setPadding(getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_pl),0,getResources().getDimensionPixelOffset(R.dimen.filter_checked_condition_pr),0);
        checked.setLayoutParams(params);
        checked.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.filter_checked_condition_ts));
        checked.setTextColor(getResources().getColor(R.color._333333));
        checked.setText("??????");
        checked.setGravity(Gravity.CENTER);
        checked.setTag("");
        filter_checked_conditiion.addView(checked);
        filter_checked_conditiion.setVisibility(View.VISIBLE);
    }

    //??????list???????????????
    private void fetchChannelSection(String channel) {
        mSkyService.getSections(channel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<SectionList>() {

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(SectionList sections) {
                        //????????????????????????????????????????????????????????????0?????????
                        sectionList=new SectionList();
                        for (int i = 0; i <sections.size() ; i++) {
                            if(sections.get(i).count!=0){
                                sectionList.add(sections.get(i));
                            }
                        }
                        //??????????????????????????????????????????????????????????????????
                        sectionSize = sectionList.size();
                        sectionHasData = new boolean[sectionSize];
                        for (int i = 0; i <sectionSize; i++) {
                            sectionHasData[i]=false;
                        }
                        //??????????????????
                        fillSections(sectionList);
                    }
                });


    }

    //???????????????????????????
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
                        //??????specialPos
/*modify by dragontec for bug 4501 start*/
                        int oldSectionCount = -1;
                        oldSectionCount = specialPos.get(index).count;
/*modify by dragontec for bug 4501 end*/
                        if(specialPos.get(index).count != listSectionEntity.getObjects().size()){
                            specialPos.get(index).count = listSectionEntity.getObjects().size();
                            int totalCount = specialPos.get(index).startPosition;
                            for (int i = index; i < specialPos.size(); i++) {
                                SpecialPos item = specialPos.get(i);
                                item.startPosition = totalCount;
                                item.endPosition = specialPos.get(i).startPosition + specialPos.get(i).count - 1;
                                totalCount += item.count;
                            }
                        }

                        mFocusGridLayoutManager.setSpecialPos(specialPos);
                        if(isVertical){
                            vSpaceItemDecoration.setSpecialPos(specialPos);
                        }else{
                            hSpaceItemDecoration.setSpecialPos(specialPos);
                        }
                        //?????????????????????????????????????????????????????????
                        List<ListSectionEntity.ObjectsBean> afterData = new ArrayList<>();
                        if(index < specialPos.size() - 1){
/*modify by dragontec for bug 4501 start*/
                            afterData = mAllSectionItemList.getObjects().subList(specialPos.get(index).startPosition + oldSectionCount, mAllSectionItemList.getObjects().size());
/*modify by dragontec for bug 4501 end*/
                        }
                        List<ListSectionEntity.ObjectsBean> beforeData = new ArrayList<>();
                        if(index >0){
/*modify by dragontec for bug 4501 start*/
                            beforeData = mAllSectionItemList.getObjects().subList(0, specialPos.get(index).startPosition);
/*modify by dragontec for bug 4501 end*/
                        }
                        List<ListSectionEntity.ObjectsBean> allData = new ArrayList<>();
                        allData.addAll(beforeData);
                        allData.addAll(listSectionEntity.getObjects());
                        allData.addAll(afterData);
                        mAllSectionItemList.setObjects(allData);
                        mAllSectionItemList.setCount(mAllSectionItemList.getObjects().size());
//                        for (int i = 0; i <listSectionEntity.getObjects().size() ; i++) {
//                            if(mAllSectionItemList.getCount()>specialPos.get(index).startPosition + i){
//                                if(oldCount > -1){
//
//                                }else{
//                                    mAllSectionItemList.getObjects().set(specialPos.get(index).startPosition + i,listSectionEntity.getObjects().get(i));
//                                }
//                            } else{
//                                mAllSectionItemList.getObjects().add(listSectionEntity.getObjects().get(i));
//                            }
//                        }
//                        mAllSectionItemList.setCount(mAllSectionItemList.getObjects().size());
                        //?????????????????????????????????????????????
                        processResultData(mAllSectionItemList,index,0,isFirstPos);
                        mFocusGridLayoutManager.setmItemCount(mAllSectionItemList.getCount());
                    }
                });


    }

    //dragontec 4440, 4463??????????????????
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        long current = System.currentTimeMillis();
        if(event.getAction() == KeyEvent.ACTION_DOWN) {
            long keyDelay = KEY_BLOCK_TIME;
            if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT){
                keyDelay =KEY_BLOCK_LEFT_RIGHT_TIME;
            }
            if (current - lastKeyDownTime < keyDelay) {
                return true;
            } else {
                lastKeyDownTime = current;
            }
        }
        if (event.getKeyCode() != KeyEvent.KEYCODE_BACK) {
            if (onKeyFocusView == null) {
                return true;
            }
        }
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            needRequestListFocusWhenGetData = false;
            if(poster_recyclerview != null && list_poster_recyclerview != null) {
                poster_recyclerview.setBlockFocusScrollWhenManualScroll(false);
                list_poster_recyclerview.setBlockFocusScrollWhenManualScroll(false);
            }
            if(mFilterFocusGridLayoutManager != null && mFocusGridLayoutManager != null){
                mFilterFocusGridLayoutManager.setCanScroll(true);
                mFocusGridLayoutManager.setCanScroll(true);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    //??????recyclerview????????????
    long mDownTime=0;
    long mUpTime=0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
/*add by dragontec for bug 4267 start*/
//        if (keyCode != KeyEvent.KEYCODE_BACK) {
//            if (onKeyFocusView == null) {
//                return true;
//            }
//        }
/*add by dragontec for bug 4267 end*/
        //???????????? ??????????????????????????????????????????400??????????????????
//        if (keyCode == 20) {
//            long downTime =System.currentTimeMillis();
//            if(mDownTime==0){
//                mDownTime=downTime;
//                return false;
//            }
//            if(downTime-mDownTime>200){
//                mDownTime=downTime;
//                return false;
//            }
//            return true;
//        }
//        if (keyCode == 19) {
//            long upTime =System.currentTimeMillis();
//            if(mUpTime==0){
//                mUpTime=upTime;
//                return false;
//            }
//            if(upTime-mUpTime>200){
//                mUpTime=upTime;
//                return false;
//            }
//            return true;
//        }

        return super.onKeyDown(keyCode, event);
    }


    /**
     * ????????????????????????
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
                    public void onError(Throwable e) {
                        mFilterItemList=null;
                        full_view.setVisibility(View.GONE);
                        filter_tab.setBackgroundResource(R.drawable.section_tab_selector);
                        super.onError(e);
                    }

                    @Override
                    public void onNext(FilterConditions filterConditions) {
                        if (filterConditions != null) {
                            content_model = filterConditions.getContent_model();
                            mFilterConditions = filterConditions;
                            //??????????????????view
                            if (filterConditions.getAttributes().getGenre() != null)
                                fillConditionLayout(filterConditions.getAttributes().getGenre().getLabel(), filterConditions.getAttributes().getGenre().getValues());
                            if (filterConditions.getAttributes().getArea() != null)
                                fillConditionLayout(filterConditions.getAttributes().getArea().getLabel(), filterConditions.getAttributes().getArea().getValues());
                            if (filterConditions.getAttributes().getAir_date() != null)
                                fillConditionLayout(filterConditions.getAttributes().getAir_date().getLabel(), filterConditions.getAttributes().getAir_date().getValues());
                            if (filterConditions.getAttributes().getAge() != null)
                                fillConditionLayout(filterConditions.getAttributes().getAge().getLabel(), filterConditions.getAttributes().getAge().getValues());
                            if (filterConditions.getAttributes().getFeature() != null)
                                fillConditionLayout(filterConditions.getAttributes().getFeature().getLabel(), filterConditions.getAttributes().getFeature().getValues());
                            fetchFilterResult(filterConditions.getContent_model(), filterConditions.getDefaultX(), 1);
                            //????????????popup????????????
                            String conditionsForLog = "";
                            for (int i = 0; i < filter_conditions.getChildCount(); i++) {
                                conditionsForLog += ";";
                            }
                            AppConstant.purchase_entrance_keyword = conditionsForLog.substring(0, conditionsForLog.lastIndexOf(";"));
                            full_view.setVisibility(View.VISIBLE);
                            filter_tab.setBackgroundResource(R.drawable.section_checked_tab_selector);
                            full_view.requestFocus();
                            showFilterPopup();
                        }
                    }

                });
    }

    /**
     * ??????????????????popup
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
        filterPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.section_checked_tab_selector));
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
                full_view.setVisibility(View.GONE);
                filter_tab.setBackgroundResource(R.drawable.section_tab_selector);
            }
        });
    }

    /**
     * ??????????????????layout
     * @param label
     * @param values
     */
    private void fillConditionLayout(String label, final List<List<String>> values) {

        List<String> no_limit=new ArrayList<>();
        no_limit.add("");
        no_limit.add("??????");
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
     * ??????????????????????????????????????????
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
                            int recommendCount=items.size()>=pagesize?pagesize:items.size();
                            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            if(isVertical){
                                for (int i = 0; i <recommendCount ; i++) {
                                    final FilterNoresultPoster item = items.get(i);
                                    if(item!=null){
                                        final View recommendView= View.inflate(FilterListActivity.this,R.layout.filter_item_vertical_poster,null);
                                        recommendView.setId(R.layout.filter_item_vertical_poster+i);
                                        PosterUtil.fillPoster(FilterListActivity.this,0,item,(RecyclerImageView)recommendView.findViewById(R.id.item_vertical_poster_img),(RecyclerImageView)recommendView.findViewById(R.id.item_vertical_poster_vip),(TextView)recommendView.findViewById(R.id.item_vertical_poster_mark),(TextView)recommendView.findViewById(R.id.item_vertical_poster_title),null);
                                        recommendView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                            @Override
                                            public void onFocusChange(View v, boolean hasFocus) {
                                                TextView title= (TextView) recommendView.findViewById(R.id.item_vertical_poster_title);
                                                if(hasFocus){
                                                    onKeyFocusView = v;
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
                                        if(i==recommendCount-1){
                                            recommendView.setNextFocusRightId(R.layout.filter_item_vertical_poster+i);
                                        }
                                        filter_noresult_first_line.addView(recommendView);
                                    }

                                }
                                params.topMargin=getResources().getDimensionPixelOffset(R.dimen.filter_noresult_vertical_mt);
                                params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.filter_noresult_vertical_ml);
                                filter_noresult_first_line.setLayoutParams(params);
                            }else{
                                for (int i = 0; i <recommendCount ; i++) {
                                    final FilterNoresultPoster item = items.get(i);
                                    if(item!=null) {
                                        final View recommendView= View.inflate(FilterListActivity.this,R.layout.item_filter_noresult_poster,null);
                                        recommendView.setId(R.layout.item_filter_noresult_poster+i);
                                        PosterUtil.fillPoster(FilterListActivity.this,1,item,(RecyclerImageView)recommendView.findViewById(R.id.item_filter_noresult_img),(RecyclerImageView)recommendView.findViewById(R.id.item_filter_noresult_vip),(TextView)recommendView.findViewById(R.id.item_filter_noresult_mark),(TextView)recommendView.findViewById(R.id.item_filter_noresult_title),(TextView)recommendView.findViewById(R.id.item_filter_noresult_descrip));
                                        recommendView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                            @Override
                                            public void onFocusChange(View v, boolean hasFocus) {
                                                TextView title= (TextView) recommendView.findViewById(R.id.item_filter_noresult_title);
                                                if(hasFocus){
                                                    onKeyFocusView=v;
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
                                        if(i==recommendCount-1){
                                            recommendView.setNextFocusRightId(R.layout.item_filter_noresult_poster+i);
                                        }
                                        if(i<4) {
                                            recommendView.setNextFocusUpId(R.layout.item_filter_noresult_poster+i);
                                            if(recommendCount<=4){
                                                recommendView.setNextFocusDownId(R.layout.item_filter_noresult_poster+i);
                                            }
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
     * ????????????????????????????????????
     * @param content_model
     * @param filterCondition
     */
    private void fetchFilterResult(String content_model, final String filterCondition, int page) {

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
                        if (filter_tab != null && filter_tab.isChecked()) {
                            if (itemList == null || itemList.objects.size() == 0) {
                                Log.e("filterResult",filterCondition);
                                filterNoResult = true;
                                filter_root_view.setShow_right_up(false);
                                filter_root_view.setShow_right_down(false);
                                if (noResultFetched) {
                                    filter_noresult.setVisibility(View.VISIBLE);
                                    poster_recyclerview.setVisibility(View.GONE);
                                } else {
                                    filter_noresult.setVisibility(View.VISIBLE);
                                    poster_recyclerview.setVisibility(View.GONE);
                                    fetchFilterNoResult();
                                }
                                poster_recyclerview.setVisibility(View.GONE);
                            } else {
                                Log.e("filterResultnone",filterCondition);
                                filterNoResult = false;
                                mFilterItemList.num_pages = itemList.num_pages;
                                mFilterItemList.objects.addAll(itemList.objects);
                                processResultData(mFilterItemList);
                                filter_noresult.setVisibility(View.GONE);
                                poster_recyclerview.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }

	/*add by dragontec for bug 4398 start*/
    private void checkFilterItemScroll(View v, int position, GridLayoutManager gridLayoutManager) {
//			if (v.getY() > getResources().getDimensionPixelOffset(R.dimen.filter_poster_start_scroll_length)) {
//				gridLayoutManager.scrollToPositionWithOffset(position, 0);
//			} else if (v.getY() < 0) {
//				if (isVertical) {
//					gridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_filter_offset_v));
//				} else {
//					gridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_filter_offset_h));
//				}
//			}
	}
	/*add by dragontec for bug 4398 end*/

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
                    if(item.model_name!=null&&"clip".equals(item.model_name)){
                        intent.toPlayPage(FilterListActivity.this,item.pk,0, Source.RETRIEVAL);
                    }else if(item.content_model!=null&&item.content_model.contains("gather")){
                        intent.toSubject(FilterListActivity.this,item.content_model,item.pk,item.title,Source.RETRIEVAL.getValue(),baseChannel);
                    }else if(item.model_name!=null&&item.model_name.equals("package")){
                        intent.toPackageDetail(FilterListActivity.this,Source.RETRIEVAL.getValue(),item.pk);
                    }else{
                        intent.toDetailPage(FilterListActivity.this,Source.RETRIEVAL.getValue(),item.pk);
                    }
                }
            });
			//add by dragontec for bug 4310 start
			filterPosterAdapter.setItemKeyListener(new OnItemKeyListener() {
				@Override
				public void onItemKeyListener(View v, int keyCode, KeyEvent event) {
					/*modify by dragontec for bug 4398 start*/
					if (event.getAction() == KeyEvent.ACTION_UP) {
						int position = poster_recyclerview.getChildAdapterPosition(v);
						if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
							if(!filter_root_view.horving) {
								checkFilterItemScroll(v, position, mFilterFocusGridLayoutManager);
							}
						}
					} else if (event.getAction() == KeyEvent.ACTION_DOWN) {
						int position = poster_recyclerview.getChildAdapterPosition(v);
						if (!filter_root_view.horving) {
							int[] location = new int[]{0, 0};
							v.getLocationOnScreen(location);
							int screenWidth = v.getResources().getDisplayMetrics().widthPixels;
							int screenHeight = v.getResources().getDisplayMetrics().heightPixels;
							if (location[0] < 0 || location[1] < 0 || location[0] + v.getWidth() > screenWidth || location[1] + v.getHeight() > screenHeight) {
								checkFilterItemScroll(v, position, mFilterFocusGridLayoutManager);
							}
						}
					}
					/*modify by dragontec for bug 4398 end*/
				}
			});
			//add by dragontec for bug 4310 end

            filterPosterAdapter.setItemFocusedListener(new OnItemFocusedListener() {
                @Override
                public void onItemfocused(View view, int position, boolean hasFocus) {
                    if(hasFocus){
                        onKeyFocusView=view;
                        lastFocusedView = view;
                        /*modify by dragontec for bug 4398 start*/
						if (!poster_recyclerview.isScrolling()) {
							if (!filter_root_view.horving) {
								int[] location = new int[]{0, 0};
								view.getLocationOnScreen(location);
								int screenWidth = view.getResources().getDisplayMetrics().widthPixels;
								int screenHeight = view.getResources().getDisplayMetrics().heightPixels;
								if (location[0] < 0 || location[1] < 0 || location[0] + view.getWidth() > screenWidth || location[1] + view.getHeight() > screenHeight) {
									checkFilterItemScroll(view, position, mFilterFocusGridLayoutManager);
								}
							}
						}
						/*modify by dragontec for bug 4398 end*/
						//modify by dragontec for bug 4310 start
//                        if(!filter_root_view.horving) {
//                            if (view.getY() > getResources().getDimensionPixelOffset(R.dimen.filter_poster_start_scroll_length)) {
//                                mFilterFocusGridLayoutManager.scrollToPositionWithOffset(position, 0);
//                            } else if (view.getY() < 0) {
//                                if (isVertical) {
//                                    mFilterFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_filter_offset_v));
//                                } else {
//                                    mFilterFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_filter_offset_h));
//                                }
//                            }
//                        }
						//modify by dragontec for bug 4310 end
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
            poster_recyclerview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if(onePageScrollY == -1){
                        int position = mFilterFocusGridLayoutManager.findLastCompletelyVisibleItemPosition()+ 1;

                        if(position < poster_recyclerview.getAdapter().getItemCount()){
                            View outsideView = mFilterFocusGridLayoutManager.findViewByPosition(position);
                            if(outsideView != null) {
                                Rect rect = new Rect();
                                outsideView.getGlobalVisibleRect(rect);
                                Rect recycleRect = new Rect();
                                poster_recyclerview.getGlobalVisibleRect(recycleRect);
                                int titleOffset = poster_recyclerview.getScrollOffset();
                                onePageScrollY = rect.top - recycleRect.top - titleOffset;
                            }
                        }
                    }
                    poster_recyclerview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    updateArrowState();
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
            updateArrowState();
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????
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
            if(!"??????".equals(radio.getText())){
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
            checked.setText("??????");
            checked.setGravity(Gravity.CENTER);
            checked.setTag("");
            filter_checked_conditiion.addView(checked);
            if(mFilterConditions!=null)
                condition=mFilterConditions.getDefaultX()+"!";
        }


        for (int i = 1; i <filter_checked_conditiion.getChildCount() ; i++) {
            if(filter_checked_conditiion.getChildAt(i)!=null) {
                condition += filter_checked_conditiion.getChildAt(i).getTag().toString() + "!";
            }
        }
        if(!TextUtils.isEmpty(conditionForLog))
            AppConstant.purchase_entrance_keyword = conditionForLog.substring(0,conditionForLog.lastIndexOf(";"));
        if(!TextUtils.isEmpty(condition))
            fetchFilterResult(content_model,condition.substring(0,condition.lastIndexOf("!")),1);
    }

    @Override
    public void onClick(View v) {
        try {
            long current = System.currentTimeMillis();
            if(current - lastClickTime < CLICK_BLOCK_TIME){
                return;
            }
            lastClickTime = current;
            int id = v.getId();
            if (id == R.id.filter_tab) {
                current_section_title.setVisibility(View.INVISIBLE);
                if (filter_checked_conditiion.getChildCount() > 1) {
                    filter_checked_conditiion.setVisibility(View.VISIBLE);
                    current_section_title.setVisibility(View.INVISIBLE);
                }
                if (filterPopup != null && !filterPopup.isShowing()) {
                    full_view.setVisibility(View.VISIBLE);
                    v.setBackgroundResource(R.drawable.section_checked_tab_selector);
                    full_view.requestFocus();
                    showFilterPopup();
                }
            } else if (id == R.id.tab_arrow_up) {
                tab_scroll.scrollBy(0, getResources().getDimensionPixelOffset(R.dimen.list_scroll_arrow_up_lenth));
                firstPos = tab_scroll.getScrollY() / section_group.getChildAt(0).getHeight();
                if (checkedTab < firstPos || checkedTab >= firstPos + 8) {
                    if (firstPos == 0) {
                        firstPos = 1;
                    }
                    ((RadioButton) section_group.getChildAt(firstPos)).setChecked(true);
                    section_group.getChildAt(firstPos).callOnClick();
                }
            } else if (id == R.id.tab_arrow_down) {
                tab_scroll.scrollBy(0, getResources().getDimensionPixelOffset(R.dimen.list_scroll_arrow_down_lenth));
                firstPos = tab_scroll.getScrollY() / section_group.getChildAt(0).getHeight();
                if (checkedTab < firstPos || checkedTab > firstPos + 8) {
                    ((RadioButton) section_group.getChildAt(firstPos)).setChecked(true);
                    section_group.getChildAt(firstPos).callOnClick();
                }
            } else if (id == R.id.poster_arrow_up) {
                if (filter_tab.isChecked()) {
                    int firstVisible = mFilterFocusGridLayoutManager.findFirstCompletelyVisibleItemPosition();
                    int spaceV = poster_recyclerview.getSpaceV();
                    if(itemHeight == -1){
						RecyclerView.ViewHolder viewHolder = poster_recyclerview.findViewHolderForAdapterPosition(firstVisible);
						if (viewHolder != null && viewHolder.itemView != null) {
							itemHeight = viewHolder.itemView.getHeight();
						}
                    }
                    int scrollOffset = -(itemHeight + spaceV/2) * 2;
                    if(firstVisible <= mFilterFocusGridLayoutManager.getDefaultSpanCount()*2){
                        scrollOffset -= spaceV;
                    }
                    poster_recyclerview.directSmoothScrollBy(0, scrollOffset);
                } else {
                    int firstVisible = mFocusGridLayoutManager.findFirstCompletelyVisibleItemPosition();
                    nextPos = firstVisible-1;
                    if(nextPos < 0){
                        return;
                    }
                    int titleOffset = list_poster_recyclerview.getScrollOffset();
                    if(itemHeight == -1){
						RecyclerView.ViewHolder viewHolder = list_poster_recyclerview.findViewHolderForAdapterPosition(firstVisible);
						if (viewHolder != null && viewHolder.itemView != null) {
							itemHeight = viewHolder.itemView.getHeight();
						}
                    }
                    int spaceV = list_poster_recyclerview.getSpaceV();
                    int belongSection = -1;
                    for (int i = 0; i < specialPos.size(); i++) {
                        if(firstVisible < specialPos.get(i).endPosition){
                            belongSection = i;
                            break;
                        }
                    }
                    if(belongSection > -1){
                        int baseOffset = -(itemHeight + spaceV)*2;
                        View firstVisibleView = mFocusGridLayoutManager.findViewByPosition(firstVisible);
                        //????????????????????????
                        if(specialPos.contains(new SpecialPos(firstVisible))){
                            baseOffset = baseOffset - (firstVisibleView.getTop());
                        }else{
                            baseOffset= baseOffset - (titleOffset - firstVisibleView.getTop());
                        }
                        int sectionStartCount = firstVisible - specialPos.get(belongSection).startPosition;
                        if(sectionStartCount>= mFocusGridLayoutManager.getDefaultSpanCount()*2){
                            // -2 line height
                            baseOffset -= spaceV/2*2;
                            list_poster_recyclerview.directSmoothScrollBy(0,baseOffset);
                        }else{
                            if(belongSection == 0){
                                View firstView = list_poster_recyclerview.getLayoutManager().findViewByPosition(0);
                                if(firstView != null){
                                    baseOffset = firstView.getTop() - titleOffset;
                                }else{
                                    return;
                                }
                            }else{
                                baseOffset -=spaceV/2;
                                if(sectionStartCount>=mFocusGridLayoutManager.getDefaultSpanCount()){
                                    baseOffset -= titleOffset;
                                }
                                int count = specialPos.get(belongSection-1).endPosition - specialPos.get(belongSection-1).startPosition;
                                if(count < mFocusGridLayoutManager.getDefaultSpanCount()){
                                    baseOffset -=spaceV/2;
                                }
                            }
                            list_poster_recyclerview.directSmoothScrollBy(0,baseOffset);
                        }
                    }
                }
            } else if (id == R.id.poster_arrow_down) {
                if (filter_tab.isChecked()) {
//                    poster_recyclerview.directSmoothScrollBy(0, onePageScrollY);
                    int firstVisible = mFilterFocusGridLayoutManager.findFirstCompletelyVisibleItemPosition();
                    int spaceV = poster_recyclerview.getSpaceV();
                    if(itemHeight == -1){
						RecyclerView.ViewHolder viewHolder = poster_recyclerview.findViewHolderForAdapterPosition(firstVisible);
						if (viewHolder != null && viewHolder.itemView != null) {
							itemHeight = viewHolder.itemView.getHeight();
						}
                    }
                    int scrollOffset = (itemHeight + spaceV) * 2;
                    poster_recyclerview.directSmoothScrollBy(0, scrollOffset);
                } else {
                    int lastVisible = mFocusGridLayoutManager.findLastCompletelyVisibleItemPosition();
                    int firstVisible = mFocusGridLayoutManager.findFirstCompletelyVisibleItemPosition();
                    nextPos = lastVisible + 1;
                    if(nextPos>mAllSectionItemList.getCount()-1){
                       return;
                    }
                    if(itemHeight == -1){
						RecyclerView.ViewHolder viewHolder = list_poster_recyclerview.findViewHolderForAdapterPosition(firstVisible);
						if (viewHolder != null && viewHolder.itemView != null) {
							itemHeight = viewHolder.itemView.getHeight();
						}
                    }
                    int titleOffset = list_poster_recyclerview.getScrollOffset();
                    View view = mFocusGridLayoutManager.findViewByPosition(nextPos);
                    Rect rect = new Rect();
                    view.getGlobalVisibleRect(rect);
                    int firstSectionIndex = -1;
                    int lastSectionIndex = -1;
                    for (int i = 0; i < specialPos.size(); i++) {
                        if(firstSectionIndex == -1 && firstVisible < specialPos.get(i).endPosition){
                            firstSectionIndex = i;
                        }
                        if(lastSectionIndex == -1 && nextPos < specialPos.get(i).endPosition){
                            lastSectionIndex = i;
                        }
                        if(firstSectionIndex != -1 && lastSectionIndex != -1){
                            break;
                        }
                    }
                    View lastView = mFocusGridLayoutManager.findViewByPosition(nextPos);
                    int baseOffset = lastView.getTop() - titleOffset;
                    View finalView = mFocusGridLayoutManager.findViewByPosition(specialPos.get(specialPos.size() - 1).endPosition);
                    if(finalView != null){
                        int delta = finalView.getBottom() - list_poster_recyclerview.getBottom();
                        if(delta < list_poster_recyclerview.getHeight()){
                            baseOffset = delta;
                        }
                    }
                    list_poster_recyclerview.directSmoothScrollBy(0, baseOffset);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
            if(v.getId() == R.id.poster_recyclerview ){
                if(poster_recyclerview != null && mFilterFocusGridLayoutManager != null) {
                    poster_recyclerview.setBlockFocusScrollWhenManualScroll(true);
					mFilterFocusGridLayoutManager.setCanScroll(false);
                }
            }else if(v.getId() == R.id.list_poster_recyclerview){
                if(list_poster_recyclerview != null && mFocusGridLayoutManager != null) {
                    list_poster_recyclerview.setBlockFocusScrollWhenManualScroll(true);
                    mFocusGridLayoutManager.setCanScroll(false);
                }
            }else{
                v.requestFocus();
                onKeyFocusView=v;
                lastFocusedView=null;
                if (filterPosterAdapter != null)
                    filterPosterAdapter.setFocusedPosition(-1);
            }
        }
        return true;
    }

    /**
     * ??????list?????????
     * @param position
     */
    private void showData(int position,boolean isFirstPos){
        Log.e("showdata",position+"");
        for (int i = 0; i <sectionSize ; i++) {
            if(i!=sectionSize-1){
                booleanFlag=position<specialPos.get(i+1).startPosition;
            }else{
                booleanFlag=true;
            }
            if(position>=specialPos.get(i).startPosition&&booleanFlag&&!sectionHasData[i]){
                if(position==specialPos.get(i).startPosition){
                    isFirstPos=false;
                }
                fetchSectionData(sectionList.get(i).url,i,isFirstPos);
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //??????
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(EventProperty.CATEGORY, channel);
        properties.put(EventProperty.TITLE, title);
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_CHANNEL_OUT, properties);
    }


    //??????list????????????
    private boolean isFirst=true;
    private void fillSections(SectionList sections) {
        final RadioGroup.LayoutParams params=new RadioGroup.LayoutParams(getResources().getDimensionPixelSize(R.dimen.filter_layout_left_view_tab_w),getResources().getDimensionPixelOffset(R.dimen.filter_layout_left_view_tab_h));
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
                        onKeyFocusView=v;
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
            /*modify by dragontec for bug 4468 start*/
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
					if(!section.title.equals(current_section_title.getText())||(finalI!=sectionSize-1&&mFocusGridLayoutManager.findLastCompletelyVisibleItemPosition()>specialPos.get(sectionSize-1).startPosition)) {
						mFocusGridLayoutManager.scrollToPositionWithOffset(specialPos.get(finalI).startPosition, 0);
					}
					if(isFirst) {
						fetchSectionData(section.url, finalI, false);

					}
					current_section_title.setText(sectionList.get(finalI).title);
                }
            });
            /*modify by dragontec for bug 4468 end*/
            radioButton.setOnHoverListener(this);
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //check changed??????
                    if(isChecked){
                        if(mClickRadioBtnHandler != null){
                            if(mCheckedChangedRunnable != null){
                                mClickRadioBtnHandler.removeCallbacks(mCheckedChangedRunnable);
                                mCheckedChangedRunnable = null;
                            }
                            mCheckedChangedRunnable = new CheckedChangedRunnable(finalI,section, buttonView);
                            mClickRadioBtnHandler.postDelayed(mCheckedChangedRunnable,CHECK_CHANGED_DELAYED);
                        }
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
/*add by dragontec for bug 4310 start*/
                	if (list_poster_recyclerview.isScrolling() || poster_recyclerview.isScrolling()) {
                		return true;
					}
/*add by dragontec for bug 4310 end*/
                    v.getLocationOnScreen(location);
                    if(event.getAction()==KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                            if(finalI == sectionSize - 1 ){
                                YoYo.with(Techniques.VerticalShake).duration(1000).playOn(v);
                                return true;
                            }else if(location[1]==tab_scroll.getBottom()-v.getHeight()){
                                tab_scroll.scrollBy(0, getResources().getDimensionPixelOffset(R.dimen.list_scroll_arrow_down_lenth));
                            }

                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                            if(location[1]==tab_scroll.getTop()){
                                tab_scroll.scrollBy(0, getResources().getDimensionPixelOffset(R.dimen.list_scroll_arrow_up_lenth));
                            }

                        }else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ) {
                            if (lastFocusedView != null) {
                                lastFocusedView.requestFocus();
                            } else {
                                View firstView = null;
                                if(finalI==sectionSize-1&&mAllSectionItemList.getCount()-specialPos.get(finalI).startPosition-1<=spanCount){
                                    firstView=mFocusGridLayoutManager.findViewByPosition(specialPos.get(finalI).startPosition+1);
                                }else if(list_poster_recyclerview.getChildCount()>spanCount) {
                                    firstView = list_poster_recyclerview.getChildAt(0);
                                    if(mFocusGridLayoutManager.findViewByPosition(mFocusGridLayoutManager.findFirstCompletelyVisibleItemPosition()) instanceof TextView){
                                        firstView=mFocusGridLayoutManager.findViewByPosition(mFocusGridLayoutManager.findFirstCompletelyVisibleItemPosition()+1);
                                    }
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
        int onePageMaxCount = 9;
        for (int i = 0; i < section_group.getChildCount(); i++) {
            if(section_group.getChildAt(i).getVisibility() == View.GONE){
                onePageMaxCount ++;
            }
        }
        if(section_group.getChildCount()<= onePageMaxCount) {
            filter_root_view.setShow_left_down(false);
        }

        specialPos = new ArrayList<>();
//        SpecialPos first = new SpecialPos();
//        specialPos.add(first);
        totalItemCount = 0;
//        for (int i = 0; i <sections.size() ; i++) {
//            totalItemCount +=sections.get(i).count;
//            if(i!=sections.size()-1){
//                SpecialPos item = new SpecialPos();
//                item.position = totalItemCount+i+1;
//                item.sections = sections.get(i+1).title;
//                specialPos.add(item);
//            }
//        }
        for (int i = 0; i < sections.size(); i++) {
            SpecialPos item = new SpecialPos();
            item.startPosition = totalItemCount;
            item.endPosition = item.startPosition + sections.get(i).count - 1;
            item.sections = sections.get(i).title;
            item.count = sections.get(i).count;
            specialPos.add(item);
            totalItemCount += sections.get(i).count;
        }

        //?????????????????????(????????????)???????????????????????????
        if(isVertical) {
            spanCount = VERTICAL_SPAN_COUNT;
            SpaceItemDecoration vFilterSpaceItemDecoration = new SpaceItemDecoration(getApplicationContext(),getWindowManager(),isVertical);
            poster_recyclerview.addItemDecoration(vFilterSpaceItemDecoration);
            vSpaceItemDecoration = new SpaceItemDecoration(getApplicationContext(),getWindowManager(),isVertical);
            vSpaceItemDecoration.setSpecialPos(specialPos);
            poster_recyclerview.setPadding(0,0,getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_recyclerview_mr),getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_padding_bottom));
            list_poster_recyclerview.addItemDecoration(vSpaceItemDecoration);
            list_poster_recyclerview.setPadding(0,0,getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_recyclerview_mr),getResources().getDimensionPixelOffset(R.dimen.vertical_recycler_padding_bottom));
        }else{
            spanCount = HORIZONTAL_SPAN_COUNT;
            SpaceItemDecoration hFilterSpaceItemDecoration=new SpaceItemDecoration(getApplicationContext(),getWindowManager(), isVertical);
            poster_recyclerview.setPadding(0,0,getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_recyclerview_mr),getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_padding_bottom));
            poster_recyclerview.addItemDecoration(hFilterSpaceItemDecoration);
            hSpaceItemDecoration = new SpaceItemDecoration(getApplicationContext(),getWindowManager(),isVertical);
            hSpaceItemDecoration.setSpecialPos(specialPos);
            list_poster_recyclerview.setPadding(0,0,getResources().getDimensionPixelOffset(R.dimen.filter_layout_poster_recyclerview_mr),getResources().getDimensionPixelOffset(R.dimen.horizontal_recycler_padding_bottom));
            list_poster_recyclerview.addItemDecoration(hSpaceItemDecoration);
        }
        mFocusGridLayoutManager = new FocusGridLayoutManager(this,spanCount);
        mFocusGridLayoutManager.setCanScroll(true);
        mFocusGridLayoutManager.setSpecialPos(specialPos);
        mFilterFocusGridLayoutManager = new FocusGridLayoutManager(this,spanCount);
        mFilterFocusGridLayoutManager.setCanScroll(true);
        mFilterFocusGridLayoutManager.setLeftFocusView(filter_tab);
        poster_recyclerview.setLayoutManager(mFilterFocusGridLayoutManager);
        list_poster_recyclerview.setLayoutManager(mFocusGridLayoutManager);
/*modify by dragontec for bug 4501 start*/
//        totalItemCount+=sections.size();
/*modify by dragontec for bug 4501 end*/
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
                return PosterUtil.computeSectionSpanSize(specialPos, position, spanCount);
            }
        });

        if(!TextUtils.isEmpty(section)){
            for (int i = 0; i <sections.size() ; i++) {
                if(section.equals(sections.get(i).slug)&&section_group.getChildAt(i+1)!=null){
                    section_group.getChildAt(i+1).callOnClick();
                    section_group.getChildAt(i+1).requestFocus();
                    ((RadioButton) section_group.getChildAt(i+1)).setChecked(true);
                    firstInSection = i;
                    break;
                }
            }
        }
        if (section_group.getChildAt(1) != null&&firstInSection==-1) {
            section_group.getChildAt(1).callOnClick();
            section_group.getChildAt(1).requestFocus();
            ((RadioButton) section_group.getChildAt(1)).setChecked(true);
        }
    }

    @Override
    public void onScrollIdle() {
        if(filter_tab.isChecked()) {
            updateArrowState();
            if (mFilterFocusGridLayoutManager.findLastVisibleItemPosition() == mFilterItemList.objects.size() - 1 && mFilterFocusGridLayoutManager.findLastVisibleItemPosition() != -1) {
                if (mFilterPage + 1 <= mFilterItemList.num_pages) {
                    fetchFilterResult(content_model, mFilterCondition, mFilterPage + 1);
                }
            }
        }else{
            updateCurrentTab();
        }
    }

    private class CheckedChangedRunnable implements Runnable{

        private CompoundButton radioButton;
        private Section section;
        private int finalI;

        public CheckedChangedRunnable(int finalI, Section section, CompoundButton radioButton) {
            this.finalI = finalI;
            this.section = section;
            this.radioButton = radioButton;
        }

        @Override
        public void run() {
            if(mClickRadioBtnHandler != null){
                mClickRadioBtnHandler.removeCallbacks(this);
            }
            /*delete by dragontec for bug 4468 start*/
//            if(!section.title.equals(current_section_title.getText())||(finalI!=sectionSize-1&&mFocusGridLayoutManager.findLastCompletelyVisibleItemPosition()>specialPos.get(sectionSize-1))) {
//                mFocusGridLayoutManager.scrollToPositionWithOffset(specialPos.get(finalI), 0);
//            }
//            if(isFirst) {
//                fetchSectionData(section.url, finalI, false);
//
//            }
//            current_section_title.setText(sectionList.get(finalI).title);
			/*delete by dragontec for bug 4468 end*/
            updateCurrentTab();
            checkedTab = finalI;
            if(filterPopup!=null&&filterPopup.isShowing())
                filterPopup.dismiss();
            filter_checked_conditiion.setVisibility(View.INVISIBLE);
//            current_section_title.setVisibility(View.VISIBLE);
            filter_checked_conditiion.requestLayout();
            if(mFocusGridLayoutManager!=null){
                mFocusGridLayoutManager.setLeftFocusView(radioButton);
            }
//            filter_root_view.setShow_right_up(true);
            //??????
            mSectionProperties.put(EventProperty.SECTION, sectionList.get(finalI).slug);
            mSectionProperties.put(EventProperty.TITLE, sectionList.get(finalI).title);
            mSectionProperties.put(EventProperty.SOURCE,"list");
            new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_CATEGORY_IN, mSectionProperties);
        }
    }
	/*add by dragontec for bug 4343 start*/
    private void checkItemScroll(View v, int position, GridLayoutManager layoutManager) {
        return;
//		if (v.getY() > getResources().getDimensionPixelOffset(R.dimen.filter_poster_start_scroll_length)) {
//			if (isVertical) {
//				layoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_v));
//			} else {
//				list_poster_recyclerview.smoothScrollToPosition(position);
//			}
//		} else if (v.getY() < 0) {
//			if (isVertical) {
//				if (specialPos != null && (specialPos.contains(position - 1) || specialPos.contains(position - 2) || specialPos.contains(position - 3) || specialPos.contains(position - 4) || specialPos.contains(position - 5))) {
//					layoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_v));
//				} else {
//					layoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_v));
//				}
//			} else {
//				if (specialPos != null && (specialPos.contains(position - 1) || specialPos.contains(position - 2) || specialPos.contains(position - 3))) {
//					layoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_h));
//				} else {
//					layoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_h));
//				}
//
//			}
//		} else if (isVertical && v.getY() > 0 && v.getY() < current_section_title.getHeight()) {
//			if (specialPos != null && (specialPos.contains(position - 1) || specialPos.contains(position - 2) || specialPos.contains(position - 3) || specialPos.contains(position - 4) || specialPos.contains(position - 5))) {
//				mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_v));
//			} else {
//				mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_v));
//			}
//		}
	}
	/*add by dragontec for bug 4343 end*/

    /**
     * ??????????????????????????????????????????
     */
    private void processResultData(final ListSectionEntity listSectionEntity,int index,int removeCount,boolean isFirstPos) {
        if(listPosterAdapter==null) {
            listPosterAdapter = new ListPosterAdapter(FilterListActivity.this, listSectionEntity.getObjects(), isVertical, specialPos, sectionList);
            list_poster_recyclerview.swapAdapter(listPosterAdapter,false);
            if(needRequestListFocusWhenGetData) {
                if (firstInSection != -1) {
                    listPosterAdapter.setFocusedPosition(specialPos.get(firstInSection).startPosition);
                } else {
                    listPosterAdapter.setFocusedPosition(0);
                }
            }

            //??????????????????????????????
            listPosterAdapter.setItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    baseSection=sectionList.get(checkedTab).slug;
                    baseChannel=channel;
                    PageIntent intent = new PageIntent();
                    ListSectionEntity.ObjectsBean item=listSectionEntity.getObjects().get(position);
                    if(item.getModel_name()!=null&&"clip".equals(item.getModel_name())){
                        intent.toPlayPage(FilterListActivity.this,item.getPk(),0, Source.LIST);
                    }else if(item.getContent_model()!=null&&item.getContent_model().contains("gather")){
                        intent.toSubject(FilterListActivity.this,item.getContent_model(),item.getPk(),item.getTitle(),Source.LIST.getValue(),baseChannel);
                    }else if(item.getModel_name()!=null&&item.getModel_name().equals("package")){
                        intent.toPackageDetail(FilterListActivity.this,Source.LIST.getValue(),item.getPk());
                    }else {
                        intent.toDetailPage(FilterListActivity.this,Source.LIST.getValue(),item.getPk());
                    }
                }
            });
			//add by dragontec for bug 4310 start
            listPosterAdapter.setItemKeyListener(new OnItemKeyListener() {
				@Override
				public void onItemKeyListener(View v, int keyCode, KeyEvent event) {
					/*modify by dragontec for bug 4343 start*/
					if (event.getAction() == KeyEvent.ACTION_UP) {
						int position = list_poster_recyclerview.getChildAdapterPosition(v);
						if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
							if (!filter_root_view.horving) {
								checkItemScroll(v, position, mFocusGridLayoutManager);
							}
						}
					} else if (event.getAction() == KeyEvent.ACTION_DOWN) {
						int position = list_poster_recyclerview.getChildAdapterPosition(v);
						if (!filter_root_view.horving) {
							int[] location = new int[]{0, 0};
							v.getLocationOnScreen(location);
							int screenWidth = v.getResources().getDisplayMetrics().widthPixels;
							int screenHeight = v.getResources().getDisplayMetrics().heightPixels;
							if (location[0] < 0 || location[1] < 0 || location[0] + v.getWidth() > screenWidth || location[1] + v.getHeight() > screenHeight) {
								checkItemScroll(v, position, mFocusGridLayoutManager);
							}
						}
					}
					/*modify by dragontec for bug 4343 end*/
				}
			});
			//add by dragontec for bug 4310 end

            //??????????????????????????????
            listPosterAdapter.setItemFocusedListener(new OnItemFocusedListener() {
                @Override
                public void onItemfocused(View view, int position, boolean hasFocus) {
                    if(hasFocus){
                        onKeyFocusView=view;
                        lastFocusedView = view;
                        changeCheckedTab(position);
                        Log.e("onitemfocus", view.getY()+"");
                        /*modify by dragontec for bug 4343 start*/
                        if (!list_poster_recyclerview.isScrolling()) {
							if (!filter_root_view.horving) {
								int[] location = new int[]{0, 0};
								view.getLocationOnScreen(location);
								int screenWidth = view.getResources().getDisplayMetrics().widthPixels;
								int screenHeight = view.getResources().getDisplayMetrics().heightPixels;
								if (location[0] < 0 || location[1] < 0 || location[0] + view.getWidth() > screenWidth || location[1] + view.getHeight() > screenHeight) {
									checkItemScroll(view, position, mFocusGridLayoutManager);
								}
							}
						}
						/*modify by dragontec for bug 4343 end*/
						//modify by dragontec for bug 4310 start
//                        if(!filter_root_view.horving) {
//                            if (view.getY() > getResources().getDimensionPixelOffset(R.dimen.filter_poster_start_scroll_length)) {
//                                if (isVertical) {
//                                    mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_v));
//                                } else {
//                                    mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_h));
//                                }
//                            } else if (view.getY() < 0) {
//                                if (isVertical) {
//                                    if(specialPos!=null&&(specialPos.contains(position-1)||specialPos.contains(position-2)||specialPos.contains(position-3)||specialPos.contains(position-4)||specialPos.contains(position-5))){
//                                        mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_v));
//                                    }else{
//                                        mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_v));
//                                    }
//                                } else {
//                                    if(specialPos!=null&&(specialPos.contains(position-1)||specialPos.contains(position-2)||specialPos.contains(position-3))){
//                                        mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_h));
//                                    }else{
//                                        mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_h));
//                                    }
//
//                                }
//                            }else if(isVertical&&view.getY()>0&&view.getY()<current_section_title.getHeight()){
//                                if(specialPos!=null&&(specialPos.contains(position-1)||specialPos.contains(position-2)||specialPos.contains(position-3)||specialPos.contains(position-4)||specialPos.contains(position-5))){
//                                    mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_up_offset_v));
//                                }else{
//                                    mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_v));
//                                }
//                            }
//                        }
						//modify by dragontec for bug 4310 end
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
            list_poster_recyclerview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    updateArrowState();
                    list_poster_recyclerview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }else{
            int position=-1;
            if(lastFocusedView==null){
                listPosterAdapter.setFocusedPosition(-1);
                position=index+1==specialPos.size()?-1:specialPos.get(index+1).startPosition-1;
            }else{
                //?????????????????????????????????????????????????????????
                if(isFirstPos) {
                    listPosterAdapter.setFocusedPosition(list_poster_recyclerview.getChildLayoutPosition(lastFocusedView) - removeCount);
                    position = list_poster_recyclerview.getChildLayoutPosition(lastFocusedView) - removeCount;
                }else {
                    listPosterAdapter.setFocusedPosition(list_poster_recyclerview.getChildLayoutPosition(lastFocusedView));
                    position = list_poster_recyclerview.getChildLayoutPosition(lastFocusedView);
                }
            }
            listPosterAdapter.setmItemList(listSectionEntity.getObjects());
            //??????????????????
			/*modify by dragontec for bug 4468 start*/
//            if(isFirstPos && !filter_root_view.horving) {
//                if (isVertical) {
//                    mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_v));
//                } else {
//                    mFocusGridLayoutManager.scrollToPositionWithOffset(position, getResources().getDimensionPixelOffset(R.dimen.list_scroll_down_offset_h));
//                }
//            }
            /*modify by dragontec for bug 4468 end*/
            listPosterAdapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uninitListener();
        baseSection="";
        filter_conditions.removeAllViews();
        poster_recyclerview.swapAdapter(null,true);
        mHandler=null;
    }



}
