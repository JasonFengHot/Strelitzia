package tv.ismar.channel;

import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
//import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.gson.GsonBuilder;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import cn.ismartv.truetime.TrueTime;
import okhttp3.ResponseBody;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.adapter.HistoryLinerlayoutMananger;
import tv.ismar.adapter.HistoryListAdapter;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.entity.Expense;
import tv.ismar.app.entity.Favorite;
import tv.ismar.app.entity.History;
import tv.ismar.app.entity.VideoEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.app.ui.adapter.OnItemKeyListener;
import tv.ismar.app.ui.adapter.OnItemOnhoverlistener;
import tv.ismar.app.entity.HistoryFavoriteEntity;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.listener.LfListItemClickListener;
import tv.ismar.listpage.R;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.view.HistoryRecyclerViewTV;
import tv.ismar.view.IsmartvLinearLayout;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static tv.ismar.listpage.R.id.arrow_line_2;
import static tv.ismar.listpage.R.id.vip_image;


/**
 * Created by liucan on 2017/8/22.
 */

public class HistoryFavoriteActivity extends BaseActivity implements View.OnClickListener,OnItemFocusedListener,LfListItemClickListener,View.OnHoverListener,OnItemOnhoverlistener,OnItemKeyListener,View.OnKeyListener{
    private GetHistoryTask mGetHistoryTask;
    private Subscription historySub,favoriteSub;
    private SkyService skyService;
    private HistoryRecyclerViewTV historyRecycler,favoriteRecycler;
    private HistoryListAdapter historyAdapter;
    private HistoryListAdapter favoritAdapter;
    private LinearLayout edit_history;
    private LinearLayout recommend_list;
    private RecyclerImageView arrow_line1,arrow_line2;
    private TextView edit_text;
    private IsmartvLinearLayout delet_history,delete_favorite;
    private LinearLayout favorite_layout,list_layout;
    private LinearLayout no_data;
    private RelativeLayout history_relativelayout,favorite_relativeLayout;
    private static final int HISTORY=1;
    private static final int FAVORITE=2;
    private List<HistoryFavoriteEntity> historyLists=new ArrayList<>();
    private List<HistoryFavoriteEntity> favoriteLists=new ArrayList<>();
    private List<HistoryFavoriteEntity> allfavoriteLists=new ArrayList<>();
    private List<HistoryFavoriteEntity> allhistoryLists=new ArrayList<>();
    private HistoryLinerlayoutMananger historyLayoutManager,favoriteManager;
    private GetFavoriteTask getFavoriteTask;
    private TextView favorite_title,history_title;
    private RecyclerImageView first_line_image,second_line_image;
    private RecyclerImageView edit_shadow;
    private RelativeLayout empty;
    private Button history_left_arrow,history_right_arrow,favorite_left_arrow,favorite_right_arrow;
    private HashMap<String, Object> mDataCollectionProperties;
    private Boolean isEdit=false;
    private boolean isMore=false;
    private String fromPage="homePage";
    private RecyclerView.OnScrollListener mOnScrollListener;

	private static final int HistoryFavoriteShowLimitNumber = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_favorite_layout);
        skyService=SkyService.ServiceManager.getService();
        Intent intent=getIntent();
        if(intent!=null){
            fromPage=intent.getStringExtra("fromPage");
        }
        initView();
    }
    private void initView(){
        history_left_arrow= (Button) findViewById(R.id.history_left_arrow);
        history_right_arrow= (Button) findViewById(R.id.history_right_arrow);
        favorite_left_arrow= (Button) findViewById(R.id.favorite_left_arrow);
        favorite_right_arrow= (Button) findViewById(R.id.favorite_right_arrow);
        empty= (RelativeLayout) findViewById(R.id.empty);
        empty.setOnHoverListener(this);
		mOnScrollListener = new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					arrowState();
				}
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				//do nothing
				super.onScrolled(recyclerView, dx, dy);
			}
		};
        historyRecycler= (HistoryRecyclerViewTV) findViewById(R.id.history_list);
        historyRecycler.addOnScrollListener(mOnScrollListener);
        favoriteRecycler= (HistoryRecyclerViewTV) findViewById(R.id.favorite_list);
        favoriteRecycler.addOnScrollListener(mOnScrollListener);
        favorite_layout= (LinearLayout) findViewById(R.id.favorite_layout);
        list_layout= (LinearLayout) findViewById(R.id.list_layout);
        no_data= (LinearLayout) findViewById(R.id.no_data);
        delet_history= (IsmartvLinearLayout) findViewById(R.id.history_edit);
        favorite_title= (TextView) findViewById(R.id.favorite_lyout_title);
        first_line_image= (RecyclerImageView) findViewById(R.id.first_line_delete_image);
        recommend_list= (LinearLayout) findViewById(R.id.recommend_list);
        edit_text= (TextView) findViewById(R.id.edit_btn_text);
        second_line_image= (RecyclerImageView) findViewById(R.id.second_line_delete_image);
        history_title= (TextView) findViewById(R.id.history_layout_title);
        history_relativelayout= (RelativeLayout) findViewById(R.id.history_layout);
        favorite_relativeLayout= (RelativeLayout) findViewById(R.id.favorite_relateLayout);
        delete_favorite= (IsmartvLinearLayout) findViewById(R.id.favorite_edit);
        edit_shadow= (RecyclerImageView) findViewById(R.id.edit_shadow);
        arrow_line1= (RecyclerImageView) findViewById(R.id.arrow_line_1);
        arrow_line2= (RecyclerImageView) findViewById(arrow_line_2);
        delet_history.setOnClickListener(this);
        delete_favorite.setOnClickListener(this);
        historyLayoutManager=new HistoryLinerlayoutMananger(this);
        historyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        favoriteManager=new HistoryLinerlayoutMananger(this);
        favoriteManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        historyRecycler.setLayoutManager(historyLayoutManager);
        favoriteRecycler.setLayoutManager(favoriteManager);
        historyRecycler.setSelectedItemAtCentered(true);
//        favoriteRecycler.setSelectedItemOffset(100,165);
        edit_history= (LinearLayout) findViewById(R.id.edit_btn);
        edit_history.setOnClickListener(this);
        edit_history.setOnHoverListener(this);
        editBtnFocusListener();
        delet_history.setOnHoverListener(this);
        delete_favorite.setOnHoverListener(this);
       // edit_shadow.setListener(this);
        edit_shadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delet_history.requestFocusFromTouch();
            }
        });
        edit_shadow.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction()==KeyEvent.ACTION_DOWN){
                    delet_history.requestFocusFromTouch();
                }
                return false;
            }
        });
        history_right_arrow.setOnHoverListener(this);
        history_left_arrow.setOnHoverListener(this);
        favorite_right_arrow.setOnHoverListener(this);
        favorite_left_arrow.setOnHoverListener(this);

        history_right_arrow.setOnClickListener(this);
        history_left_arrow.setOnClickListener(this);
        favorite_right_arrow.setOnClickListener(this);
        favorite_left_arrow.setOnClickListener(this);

        history_right_arrow.setOnKeyListener(this);
        history_left_arrow.setOnKeyListener(this);
        favorite_right_arrow.setOnKeyListener(this);
        favorite_left_arrow.setOnKeyListener(this);


        historyRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(newState==SCROLL_STATE_IDLE){
                    if(historyLayoutManager!=null){
                        int pos=historyLayoutManager.findFirstCompletelyVisibleItemPosition();
                        int endPos=historyLayoutManager.findLastCompletelyVisibleItemPosition();
                        if(pos!=0){
                            if(!isEdit)
                            history_left_arrow.setVisibility(View.VISIBLE);
                        }else{
                            history_left_arrow.setVisibility(View.GONE);
                        }
                        if(historyLists.size()>0) {
                            if (endPos != historyLists.size() - 1) {
                                history_right_arrow.setVisibility(View.VISIBLE);
                            } else {
                                history_right_arrow.setVisibility(View.GONE);
                            }
                        }else{
                            if (endPos != favoriteLists.size() - 1) {
                                history_right_arrow.setVisibility(View.VISIBLE);
                            } else {
                                history_right_arrow.setVisibility(View.GONE);
                            }
                        }

                    }

                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        favoriteRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                Log.i("ScrollListener","newstate: "+newState);
                if(newState==SCROLL_STATE_IDLE){
                    if(favoriteManager!=null){
                        int pos=favoriteManager.findFirstCompletelyVisibleItemPosition();
                        int endPos=favoriteManager.findLastCompletelyVisibleItemPosition();
                        if(pos!=0){
                            favorite_left_arrow.setVisibility(View.VISIBLE);
                        }else{
                            favorite_left_arrow.setVisibility(View.GONE);
                        }
                        if (endPos != favoriteLists.size() - 1) {
                            favorite_right_arrow.setVisibility(View.VISIBLE);
                        } else {
                            favorite_right_arrow.setVisibility(View.GONE);
                        }
                    }

                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        empty.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction()==KeyEvent.ACTION_DOWN){
                    empty.setFocusable(false);

                }
                return false;
            }
        });

        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(EventProperty.TITLE, "history");
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_HISTORY_IN, properties);
    }
    private void editBtnFocusListener(){
        delete_favorite.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    JasmineUtil.scaleOut3(v);
                }else{
                    JasmineUtil.scaleIn3(v);
                }
            }
        });
        delet_history.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    JasmineUtil.scaleOut3(v);
                }else{
                    JasmineUtil.scaleIn3(v);
                }
            }
        });
        edit_history.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    edit_text.setVisibility(View.VISIBLE);
                }else{
                    edit_text.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    @Override
    protected void onResume() {
		super.onResume();
        AppConstant.purchase_referer = "history";
        AppConstant.purchase_page = "history";
        AppConstant.purchase_channel = "";
        AppConstant.purchase_entrance_channel = "";
        AppConstant.purchase_entrance_page = "history";
        BaseActivity.baseChannel="";
        BaseActivity.baseSection="";

//        if(IsmartvActivator.getInstance().isLogin()){
//            //登录，网络获取
//            getHistoryByNet();
//        }else{
            //没有登录，取本地设备信息
            mGetHistoryTask = new GetHistoryTask();
            mGetHistoryTask.execute();
//        }
        edit_history.setFocusable(false);
        edit_history.setFocusableInTouchMode(false);
    }

    @Override
    protected void onPause() {
    	Log.d("HistoryFavoriteActivity", "onPause begin");
        historyLists.clear();
        favoriteLists.clear();
        allhistoryLists.clear();
        allfavoriteLists.clear();

        HashMap<String, Object> properties = mDataCollectionProperties;
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_HISTORY_OUT, properties);
        mDataCollectionProperties = null;
        super.onPause();
		Log.d("HistoryFavoriteActivity", "onPause end");
    }

	@Override
	protected void onDestroy() {
    	if (mOnScrollListener != null) {
			if (favoriteRecycler != null) {
				favoriteRecycler.removeOnScrollListener(mOnScrollListener);
			}
			if (historyRecycler != null) {
				historyRecycler.removeOnScrollListener(mOnScrollListener);
			}
		}
		super.onDestroy();
	}

	private void getHistoryByNet(){
        historySub=skyService.getHistoryByNetV3().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String result=null;
                        try {
                            result=responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.i("historyRequest",result);
                        historyLists= parseResult(result,HISTORY);
                        getFavoriteByNet();
                    }
                });
    }
    private void getFavoriteByNet(){
        favoriteSub=skyService.getBookmarksV3().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String result=null;
                        try {
                            result=responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.i("historyRequest",result);
                        favoriteLists= parseResult(result,FAVORITE);
                        loadData();
                    }
                });
    }
    private List<HistoryFavoriteEntity> parseResult(String result,int type){
        List<HistoryFavoriteEntity> lists=new ArrayList<>();
        try {
            JSONObject jsonObject=new JSONObject(result);
            JSONObject info=jsonObject.getJSONObject("info");
            JSONArray date=info.getJSONArray("date");
            for(int i=0;i<date.length();i++){
                JSONArray element=info.getJSONArray(date.getString(i));
                if(i==3){
                    HistoryFavoriteEntity more = new HistoryFavoriteEntity();
                    more.setType(2);
                    lists.add(more);
                }
                for(int j=0;j<element.length();j++){
                    HistoryFavoriteEntity historyFavoriteEntity=new GsonBuilder().create().fromJson(element.get(j).toString(),HistoryFavoriteEntity.class);
                    if(j==0){
                        historyFavoriteEntity.setShowDate(true);
                    }else{
                        historyFavoriteEntity.setShowDate(false);
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
                    historyFavoriteEntity.setDate(sdf.parse(date.getString(i)).getTime());
                    if(i<3) {
                        lists.add(historyFavoriteEntity);
                        if(type==FAVORITE){
                            allfavoriteLists.add(historyFavoriteEntity);
                        }else{
                            allhistoryLists.add(historyFavoriteEntity);
                        }
                    } else{
                        if(type==FAVORITE){
                            allfavoriteLists.add(historyFavoriteEntity);
                        }else{
                            allhistoryLists.add(historyFavoriteEntity);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return lists;
    }

    private void loadData(){
        favorite_left_arrow.setVisibility(View.GONE);
        history_left_arrow.setVisibility(View.GONE);
        if(historyLists.size()>0){
            no_data.setVisibility(View.GONE);
            history_relativelayout.setVisibility(View.VISIBLE);

            if(historyLists.size()>=4){
                history_right_arrow.setVisibility(View.VISIBLE);
            }
            Log.i("favoriteaci","isEdit: "+isEdit);
            if(!isEdit)
            edit_history.setVisibility(View.VISIBLE);
            if(favoriteLists.size()>0){
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getResources().getDimensionPixelSize(R.dimen.history_473));
                lp.setMargins(0,getResources().getDimensionPixelSize(R.dimen.history_115),0,0);
                history_relativelayout.setLayoutParams(lp);
                favorite_relativeLayout.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams editLp = new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.history_432),getResources().getDimensionPixelSize(R.dimen.history_306));
                editLp.setMargins(getResources().getDimensionPixelSize(R.dimen.history_100),getResources().getDimensionPixelSize(R.dimen.history_259),0,0);
                delet_history.setLayoutParams(editLp);

                favorite_title.setText("收藏");
                favorite_title.setVisibility(View.VISIBLE);
                if(isEdit)
                delete_favorite.setVisibility(View.VISIBLE);
                if(favoriteLists.size()>=4){
                    favorite_right_arrow.setVisibility(View.VISIBLE);
                }
                second_line_image.setBackgroundResource(R.drawable.favorite_delete_image);
                favoritAdapter=new HistoryListAdapter(HistoryFavoriteActivity.this,favoriteLists,"favorite");
                favoritAdapter.setItemFocusedListener(HistoryFavoriteActivity.this);
                favoritAdapter.setItemClickListener(HistoryFavoriteActivity.this);
                favoritAdapter.setItemOnhoverlistener(HistoryFavoriteActivity.this);
                favoritAdapter.setItemKeyListener(HistoryFavoriteActivity.this);
                favoriteRecycler.setAdapter(favoritAdapter);

                history_title.setText("历史");
                history_title.setVisibility(View.VISIBLE);
                first_line_image.setBackgroundResource(R.drawable.history_delete_image);
                historyAdapter=new HistoryListAdapter(HistoryFavoriteActivity.this,historyLists,"history");
                setHistoryListen();
                historyRecycler.setAdapter(historyAdapter);

            }else{
                favorite_right_arrow.setVisibility(View.GONE);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getResources().getDimensionPixelSize(R.dimen.history_473));
                lp.setMargins(0,getResources().getDimensionPixelSize(R.dimen.history_155),0,0);
                history_relativelayout.setLayoutParams(lp);
                history_relativelayout.setVisibility(View.VISIBLE);
                favorite_relativeLayout.setVisibility(View.GONE);
                RelativeLayout.LayoutParams editLp = new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.history_432),getResources().getDimensionPixelSize(R.dimen.history_306));
                editLp.setMargins(getResources().getDimensionPixelSize(R.dimen.history_100),getResources().getDimensionPixelSize(R.dimen.history_299),0,0);
                delet_history.setLayoutParams(editLp);
                delete_favorite.setVisibility(View.GONE);

                history_title.setText("历史");
                history_title.setVisibility(View.VISIBLE);
                first_line_image.setBackgroundResource(R.drawable.history_delete_image);
                historyAdapter=new HistoryListAdapter(HistoryFavoriteActivity.this,historyLists,"history");
                setHistoryListen();
                historyRecycler.setAdapter(historyAdapter);

            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!isEdit&&historyRecycler.getChildAt(0)!=null)
                        historyRecycler.getChildAt(0).requestFocusFromTouch();
                    edit_history.setFocusable(true);
                    edit_history.setFocusableInTouchMode(true);
                }
            },600);
            if(isEdit){
                delet_history.requestFocusFromTouch();
            }
        }else{
            if(favoriteLists.size()>0){
                no_data.setVisibility(View.GONE);
                if(!isEdit)
                edit_history.setVisibility(View.VISIBLE);

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getResources().getDimensionPixelSize(R.dimen.history_473));
                lp.setMargins(0,getResources().getDimensionPixelSize(R.dimen.history_155),0,0);
                history_relativelayout.setVisibility(View.VISIBLE);
                history_relativelayout.setLayoutParams(lp);
                favorite_relativeLayout.setVisibility(View.GONE);
                RelativeLayout.LayoutParams editLp = new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.history_432),getResources().getDimensionPixelSize(R.dimen.history_306));
                editLp.setMargins(getResources().getDimensionPixelSize(R.dimen.history_100),getResources().getDimensionPixelSize(R.dimen.history_299),0,0);
                delet_history.setLayoutParams(editLp);
                delete_favorite.setVisibility(View.GONE);
                if(favoriteLists.size()>=4){
                    history_right_arrow.setVisibility(View.VISIBLE);
                }

                history_title.setText("收藏");
                history_title.setVisibility(View.VISIBLE);
                first_line_image.setBackgroundResource(R.drawable.favorite_delete_image);
                historyAdapter=new HistoryListAdapter(HistoryFavoriteActivity.this,favoriteLists,"favorite");
                setHistoryListen();
                historyRecycler.setAdapter(historyAdapter);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!isEdit&&historyRecycler.getChildAt(0)!=null)
                            historyRecycler.getChildAt(0).requestFocusFromTouch();
                        edit_history.setFocusable(true);
                        edit_history.setFocusableInTouchMode(true);
                    }
                },500);
                if(isEdit){
                    delet_history.requestFocusFromTouch();
                }
            }else{
                if(isEdit)
                    editRestore();
                showNoData();

            }
        }
        edit_history.setFocusable(true);
        edit_history.setFocusableInTouchMode(true);

    }
    private void setHistoryListen(){
        historyAdapter.setItemFocusedListener(HistoryFavoriteActivity.this);
        historyAdapter.setItemClickListener(HistoryFavoriteActivity.this);
        historyAdapter.setItemOnhoverlistener(HistoryFavoriteActivity.this);
        historyAdapter.setItemKeyListener(HistoryFavoriteActivity.this);
    }
    private int targetPosition;
    @Override
    public void onClick(View v) {
        int id=v.getId();
        Intent intent=new Intent();
        intent.setAction("tv.ismar.daisy.historyfavoriteList");
        intent.putExtra("source","edit");
        if(id==R.id.edit_btn){
            isEdit=true;
            historyRecycler.scrollToPosition(0);
            favoriteRecycler.scrollToPosition(0);
            history_left_arrow.setVisibility(View.GONE);
            favorite_left_arrow.setVisibility(View.GONE);
            history_right_arrow.setVisibility(View.GONE);
            favorite_right_arrow.setVisibility(View.GONE);

            edit_shadow.setVisibility(View.VISIBLE);
            edit_history.setVisibility(View.GONE);
            if(historyLists.size()>0){
                if(favoriteLists.size()>0){
                    delet_history.setVisibility(View.VISIBLE);
                    delete_favorite.setVisibility(View.VISIBLE);
                }else{
                    delet_history.setVisibility(View.VISIBLE);
                }
            }else{
                if(favoriteLists.size()>0){
                    delet_history.setVisibility(View.VISIBLE);
                }
            }

            delet_history.requestFocusFromTouch();

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins(getResources().getDimensionPixelSize(R.dimen.history_492),0,0,0);
            list_layout.setLayoutParams(lp);
            favorite_layout.setLayoutParams(lp);
            RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.history_1328), getResources().getDimensionPixelSize(R.dimen.history_2));
            lp2.setMargins(getResources().getDimensionPixelSize(R.dimen.history_592),getResources().getDimensionPixelSize(R.dimen.history_75),0,0);
            arrow_line2.setLayoutParams(lp2);
            arrow_line1.setLayoutParams(lp2);

            historyLayoutManager.setScrollEnabled(false);
            favoriteManager.setScrollEnabled(false);
        }else if(id==R.id.favorite_edit){
            intent.putExtra("type",2);
            intent.putExtra("List",(Serializable) allfavoriteLists);
            startActivity(intent);
        }else if(id==R.id.history_edit){
            if(historyLists.size()>0) {
                intent.putExtra("type", 1);
                intent.putExtra("List", (Serializable) allhistoryLists);
            }else{
                intent.putExtra("type",2);
                intent.putExtra("List",(Serializable) allfavoriteLists);
            }
            startActivity(intent);
        }else if(id==R.id.favorite_right_arrow){
        	if (favoriteRecycler.getScrollState() != SCROLL_STATE_IDLE) {
        		return;
			}
            favoriteManager.setScrollEnabled(true);
//            favorite_left_arrow.setVisibility(View.VISIBLE);
			if (favoriteManager.findLastCompletelyVisibleItemPosition() != favoriteManager.getItemCount() - 1) {
				RecyclerView.ViewHolder viewHolder = favoriteRecycler.findViewHolderForAdapterPosition(favoriteManager.findLastVisibleItemPosition());
				if (viewHolder != null && viewHolder.itemView != null) {
					int dx;
					int[] location = new int[2];
					viewHolder.itemView.getLocationOnScreen(location);
					if (favoriteManager.findLastVisibleItemPosition() == favoriteManager.findLastCompletelyVisibleItemPosition()) {
						dx = location[0] + viewHolder.itemView.getWidth() * 3 / 2 - getResources().getDisplayMetrics().widthPixels / 2;
					} else {
						dx = location[0] + viewHolder.itemView.getWidth() / 2 - getResources().getDisplayMetrics().widthPixels / 2;
					}
					if (dx != 0) {
						favoriteRecycler.smoothScrollBy(dx, 0);
					}
				}
			}
//            targetPosition=favoriteManager.findFirstCompletelyVisibleItemPosition()+3;
//            if(targetPosition+2>=favoriteLists.size()-1) {
//                favoriteManager.smoothScrollToPosition(favoriteRecycler, null, getResources().getDimensionPixelOffset(R.dimen.history_165));
//            }else if(targetPosition==3){
//                favorite_left_arrow.setVisibility(View.VISIBLE);
//                favoriteManager.scrollToPositionWithOffset(targetPosition,getResources().getDimensionPixelOffset(R.dimen.history_165));
//            }else{
//                favoriteManager.scrollToPositionWithOffset(targetPosition,getResources().getDimensionPixelOffset(R.dimen.history_165));
//                arrowState();
//            }
//            targetPosition=favoriteManager.findLastCompletelyVisibleItemPosition()+3;
//            favoriteManager.smoothScrollToPosition(favoriteRecycler,null,targetPosition);
        }else if(id==R.id.favorite_left_arrow){
			if (favoriteRecycler.getScrollState() != SCROLL_STATE_IDLE) {
				return;
			}
            favoriteManager.setScrollEnabled(true);
			if (favoriteManager.findFirstCompletelyVisibleItemPosition() != 0) {
				RecyclerView.ViewHolder viewHolder = favoriteRecycler.findViewHolderForAdapterPosition(favoriteManager.findFirstVisibleItemPosition());
				if (viewHolder != null && viewHolder.itemView != null) {
					int dx;
					int[] location = new int[2];
					viewHolder.itemView.getLocationOnScreen(location);
					if (favoriteManager.findFirstVisibleItemPosition() == favoriteManager.findFirstCompletelyVisibleItemPosition()) {
						dx = location[0] - viewHolder.itemView.getWidth() / 2 - getResources().getDisplayMetrics().widthPixels / 2;
					} else {
						dx = location[0] + viewHolder.itemView.getWidth() / 2 - getResources().getDisplayMetrics().widthPixels / 2;
					}
					if (dx != 0) {
						favoriteRecycler.smoothScrollBy(dx, 0);
					}
				}
			}
//            targetPosition=favoriteManager.findFirstCompletelyVisibleItemPosition()-3;
//            if(targetPosition<=0) {
//                favoriteManager.smoothScrollToPosition(favoriteRecycler,null,0);
//                favoriteManager.scrollToPositionWithOffset(targetPosition, getResources().getDimensionPixelOffset(R.dimen.history_165));
//            }else{
//                favoriteManager.scrollToPositionWithOffset(targetPosition, getResources().getDimensionPixelOffset(R.dimen.history_165));
//                arrowState();
//            }
//            if(favoriteManager.findLastVisibleItemPosition()==favoriteLists.size()-1){
//                favorite_right_arrow.setVisibility(View.VISIBLE);
//            }
        }else if(id==R.id.history_left_arrow){
			if (historyRecycler.getScrollState() != SCROLL_STATE_IDLE) {
				return;
			}
            historyLayoutManager.setScrollEnabled(true);
			if (historyLayoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
				RecyclerView.ViewHolder viewHolder = historyRecycler.findViewHolderForAdapterPosition(historyLayoutManager.findFirstVisibleItemPosition());
				if (viewHolder != null && viewHolder.itemView != null) {
					int dx = 0;
					int[] location = new int[2];
					viewHolder.itemView.getLocationOnScreen(location);
					if (historyLayoutManager.findFirstVisibleItemPosition() == historyLayoutManager.findFirstCompletelyVisibleItemPosition()) {
						dx = location[0] - viewHolder.itemView.getWidth() / 2 - getResources().getDisplayMetrics().widthPixels / 2;
					} else {
						dx = location[0] + viewHolder.itemView.getWidth() / 2 - getResources().getDisplayMetrics().widthPixels / 2;
					}
					if (dx != 0) {
						historyRecycler.smoothScrollBy(dx, 0);
					}
				}
			}

//            targetPosition=historyLayoutManager.findFirstCompletelyVisibleItemPosition()-3;
//            if(targetPosition<=0){
//                historyLayoutManager.smoothScrollToPosition(historyRecycler,null,0);
//            }else {
//                historyLayoutManager.scrollToPositionWithOffset(targetPosition, getResources().getDimensionPixelOffset(R.dimen.history_165));
//                arrowState();
//            }
//            history_right_arrow.setVisibility(View.VISIBLE);
        }else if(id==R.id.history_right_arrow){
			if (historyRecycler.getScrollState() != SCROLL_STATE_IDLE) {
				return;
			}
            historyLayoutManager.setScrollEnabled(true);
			if (historyLayoutManager.findLastCompletelyVisibleItemPosition() != historyLayoutManager.getItemCount() - 1) {
				RecyclerView.ViewHolder viewHolder = historyRecycler.findViewHolderForAdapterPosition(historyLayoutManager.findLastVisibleItemPosition());
				if (viewHolder != null && viewHolder.itemView != null) {
					int dx = 0;
					int[] location = new int[2];
					viewHolder.itemView.getLocationOnScreen(location);
					if (historyLayoutManager.findLastVisibleItemPosition() == historyLayoutManager.findLastCompletelyVisibleItemPosition()) {
						dx = location[0] + viewHolder.itemView.getWidth() * 3 / 2 - getResources().getDisplayMetrics().widthPixels / 2;
					} else {
						dx = location[0] + viewHolder.itemView.getWidth() / 2 - getResources().getDisplayMetrics().widthPixels / 2;
					}
					if (dx != 0) {
						historyRecycler.smoothScrollBy(dx, 0);
					}
				}
			}
//            targetPosition=historyLayoutManager.findFirstCompletelyVisibleItemPosition()+3;
//            if(historyLists.size()>0){
//                if(targetPosition+2>=historyLists.size()-1) {
//                    historyLayoutManager.smoothScrollToPosition(historyRecycler, null, getResources().getDimensionPixelOffset(R.dimen.history_165));
//                }else if(targetPosition==3) {
//                    history_left_arrow.setVisibility(View.VISIBLE);
//                    historyLayoutManager.scrollToPositionWithOffset(targetPosition,getResources().getDimensionPixelOffset(R.dimen.history_165));
//                }else{
//                    historyLayoutManager.scrollToPositionWithOffset(targetPosition, getResources().getDimensionPixelOffset(R.dimen.history_165));
//                    arrowState();
//                }
//            }else{
//                if(targetPosition+2>=favoriteLists.size()-1) {
//                    historyLayoutManager.smoothScrollToPosition(historyRecycler, null, getResources().getDimensionPixelOffset(R.dimen.history_165));
//                }else if(targetPosition==3) {
//                    history_left_arrow.setVisibility(View.VISIBLE);
//                    historyLayoutManager.scrollToPositionWithOffset(targetPosition,getResources().getDimensionPixelOffset(R.dimen.history_165));
//                }else{
//                    historyLayoutManager.scrollToPositionWithOffset(targetPosition, getResources().getDimensionPixelOffset(R.dimen.history_165));
//                    arrowState();
//                }
//            }
        }
    }
    private void arrowState(){
        if(favoriteManager!=null&&favoriteLists.size()>0){
            int pos=favoriteManager.findFirstCompletelyVisibleItemPosition();
            int endPos=favoriteManager.findLastCompletelyVisibleItemPosition();
            Log.i("ScrollListener","pos"+pos);
            if(pos!=0){
                favorite_left_arrow.setVisibility(View.VISIBLE);
            }else{
                favorite_left_arrow.setVisibility(View.GONE);
            }
            if (endPos != favoriteLists.size() - 1) {
                favorite_right_arrow.setVisibility(View.VISIBLE);
            } else {
                favorite_right_arrow.setVisibility(View.GONE);
            }
        }
        if(historyLayoutManager!=null){
            int pos=historyLayoutManager.findFirstCompletelyVisibleItemPosition();
            int endPos=historyLayoutManager.findLastCompletelyVisibleItemPosition();
            if(pos!=0){
                if(!isEdit)
                    history_left_arrow.setVisibility(View.VISIBLE);
            }else{
                history_left_arrow.setVisibility(View.GONE);
            }
            if(historyLists.size()>0) {
                if (endPos != historyLists.size() - 1) {
                    history_right_arrow.setVisibility(View.VISIBLE);
                } else {
                    history_right_arrow.setVisibility(View.GONE);
                }
            }else if(favoriteLists.size()>0){
                if (endPos != favoriteLists.size() - 1) {
                    history_right_arrow.setVisibility(View.VISIBLE);
                } else {
                    history_right_arrow.setVisibility(View.GONE);
                }
            }

        }



    }
    @Override
    public void onBackPressed() {
        if(isEdit){
            isEdit=false;
            editRestore();
        }else {
            super.onBackPressed();
        }
    }
    private void editRestore(){
        edit_history.setVisibility(View.VISIBLE);
        edit_shadow.setVisibility(View.GONE);
        delet_history.setVisibility(View.GONE);
        delete_favorite.setVisibility(View.GONE);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.setMargins(0,0,0,0);
        list_layout.setLayoutParams(lp);
        favorite_layout.setLayoutParams(lp);

        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.history_1820), getResources().getDimensionPixelSize(R.dimen.history_2));
        lp2.setMargins(getResources().getDimensionPixelSize(R.dimen.history_100),getResources().getDimensionPixelSize(R.dimen.history_75),0,0);
        arrow_line2.setLayoutParams(lp2);
        arrow_line1.setLayoutParams(lp2);

        historyLayoutManager.setScrollEnabled(true);
        favoriteManager.setScrollEnabled(true);

        if(historyLists.size()>=4){
            history_right_arrow.setVisibility(View.VISIBLE);
        }
        if(favoriteLists.size()>=4){
            favorite_right_arrow.setVisibility(View.VISIBLE);
        }
    }
    private void showNoData(){
        history_relativelayout.setVisibility(View.GONE);
        favorite_relativeLayout.setVisibility(View.GONE);
        history_title.setVisibility(View.GONE);
        favorite_title.setVisibility(View.GONE);
        edit_history.setVisibility(View.INVISIBLE);
        delete_favorite.setVisibility(View.GONE);
        delet_history.setVisibility(View.GONE);
        edit_shadow.setVisibility(View.GONE);
        isEdit=false;
        no_data.setVisibility(View.VISIBLE);
        getRecommend();
    }
    private void  getRecommend(){
        skyService.getTvHome().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<VideoEntity>() {
                    @Override
                    public void onCompleted() {

                    }
                    @Override
                    public void onNext(VideoEntity videoEntity) {
                        if(videoEntity!=null) {
                            setRecommend(videoEntity);
                        }
                    }
                });
    }

    private void setRecommend(final VideoEntity videoEntity) {
        recommend_list.removeAllViews();
        for (int i=0;i<=3;i++) {
            View container = LayoutInflater.from(this).inflate(R.layout.no_data_list_item, null);
            container.setId(R.layout.no_data_list_item+i);
            RecyclerImageView detail= (RecyclerImageView) container.findViewById(R.id.item_image);
            RecyclerImageView vip= (RecyclerImageView) container.findViewById(vip_image);
            IsmartvLinearLayout item= (IsmartvLinearLayout) container.findViewById(R.id.no_data_item);
            TextView focus= (TextView) container.findViewById(R.id.focus_title);
            TextView title= (TextView) container.findViewById(R.id.title);
            final VideoEntity.Objects object=videoEntity.getObjects().get(i);
            item.setOnHoverListener(this);
            if(object.getImage()==null||object.getImage().isEmpty()){
                Picasso.with(this).load(R.drawable.item_horizontal_preview).into(detail);
            }else {
                Picasso.with(this).load(object.getImage()).error(R.drawable.item_horizontal_preview).into(detail);
            }
            title.setText(object.getTitle());
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean[] isSubItem = new boolean[1];
                    int pk=SimpleRestClient.getItemId(object.getItem_url(),isSubItem);
                    String contentMode=object.getContent_model();
                    PageIntent intent=new PageIntent();
                    if(contentMode!=null&&contentMode.contains("gather")) {
                        intent.toSubject(HistoryFavoriteActivity.this, contentMode, pk, object.getTitle(), "tvhome", "");
                    }else {
                        if (object.isIs_complex()) {
                            intent.toDetailPage(HistoryFavoriteActivity.this, "tvhome", pk);
                        } else {
                            //	intent.toPlayPage(getActivity(),pk,0, Source.HISTORY);
                            intent.toPlayPageEpisode(HistoryFavoriteActivity.this, pk, 0, Source.TVHOME, object.getContent_model());
                        }
                    }
                }
            });
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(getResources().getDimensionPixelOffset(R.dimen.history_336),getResources().getDimensionPixelOffset(R.dimen.history_243));
            params.setMargins(getResources().getDimensionPixelOffset(R.dimen.history_44),getResources().getDimensionPixelOffset(R.dimen.history_20),0,0);
            container.setLayoutParams(params);
            recommend_list.addView(container);
            if(recommend_list.getChildAt(0)!=null)
            recommend_list.getChildAt(0).requestFocusFromTouch();
        }
    }

    @Override
    public void onItemfocused(View view, int position, boolean hasFocus) {
        Rect rect=new Rect();
        view.getGlobalVisibleRect(rect);
        if(hasFocus){
        	if (rect.width() == view.getWidth()) {
				JasmineUtil.scaleOut3(view);
			}
//            if(rect.left>=100&&rect.left<1576) {
//                JasmineUtil.scaleOut3(view);
//            }
        }else{
            JasmineUtil.scaleIn3(view);
        }

    }

    @Override
    public void onlfItemClick(View v, int postion, String type) {
        PageIntent intent=new PageIntent();
        mDataCollectionProperties=new HashMap<>();
        Log.i("hoverX","position: "+postion);
        int pk=0;
        Rect rect=new Rect();
        v.getGlobalVisibleRect(rect);
//        if(rect.left<100||rect.left>=1576){
//            return;
//        }
        if(type.equals("history")){
            if(postion<=historyLists.size()-1) {
                HistoryFavoriteEntity history = historyLists.get(postion);
                boolean[] isSubItem = new boolean[1];
                pk = SimpleRestClient.getItemId(history.getUrl(), isSubItem);
                Log.i("loginPlay","url: "+history.getUrl()+"  pk: "+history.getPk()+" sub_url: "+history.getSub_url());
                if (pk == 0) {
                    pk = history.getPk();
                }
                if (history.getType() != 2) {
                    mDataCollectionProperties.put("to_item", history.getItem_pk());
                    mDataCollectionProperties.put("to_subitem", pk);
                    mDataCollectionProperties.put("to_title", history.getTitle());
//                mDataCollectionProperties.put("position", history.get/1000);
                    if (history.getModel_name() != null && history.getModel_name().equals("subitem")) {
                        Log.i("loginPlay","modelName: "+history.getModel_name());
                        intent.toPlayPage(this, pk,0 , Source.HISTORY);
                    } else {
                        intent.toPlayPage(this, pk, 0, Source.HISTORY);
                    }
                } else {
                    Intent intent1 = new Intent();
                    intent1.setAction("tv.ismar.daisy.historyfavoriteList");
                    intent1.putExtra("source", "list");
                    intent1.putExtra("type", 1);
                    intent1.putExtra("List", (Serializable) allhistoryLists);
                    startActivity(intent1);
                }
            }
        }else {
            if (postion <= favoriteLists.size() - 1) {
                HistoryFavoriteEntity favoriteEntity = favoriteLists.get(postion);
                boolean[] isSubItem = new boolean[1];
                pk = SimpleRestClient.getItemId(favoriteEntity.getUrl(), isSubItem);
                if (pk == 0) {
                    pk = favoriteEntity.getPk();
                }
                if (favoriteEntity.getType() != 2) {
                    if(IsmartvActivator.getInstance().isLogin()) {
                        if (favoriteEntity.getModel_name() != null && favoriteEntity.getModel_name().equals("clip")) {
                            intent.toPlayPageEpisode(this, pk, 0, Source.FAVORITE, favoriteEntity.getContent_model());
                        } else {
                            if (favoriteEntity.getContent_model().contains("gather")) {
                                intent.toSubject(this, favoriteEntity.getContent_model(), pk, favoriteEntity.getTitle(), "favorite", "");
                            } else {
                                intent.toDetailPage(this, "favorite", pk);
                            }
                        }
                    }else{
                        if (favoriteEntity.getContent_model().contains("gather")) {
                            intent.toSubject(this, favoriteEntity.getContent_model(), pk, favoriteEntity.getTitle(), "favorite", "");
                        } else {
                            if (favoriteEntity.getIs_complex()) {
                                intent.toDetailPage(this, "favorite", pk);
                            } else {
                                intent.toPlayPageEpisode(this, pk, 0, Source.FAVORITE, favoriteEntity.getContent_model());
                            }
                        }
                    }
                } else {
                    Intent intent1 = new Intent();
                    intent1.setAction("tv.ismar.daisy.historyfavoriteList");
                    intent1.putExtra("source", "list");
                    intent1.putExtra("type", 2);
                    intent1.putExtra("List", (Serializable) allfavoriteLists);
                    startActivity(intent1);
                }
            }
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_ENTER:
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                v.requestFocusFromTouch();
                break;
        }
        return false;
    }
    @Override
    public void OnItemOnhoverlistener(View v, MotionEvent event, int position, int recommend) {
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_ENTER:
                Rect rect = new Rect();
                v.getGlobalVisibleRect(rect);
                Log.i("hoverX",rect.left+"");
                if(!isEdit) {
                    historyLayoutManager.setScrollEnabled(false);
                    favoriteManager.setScrollEnabled(false);
                    if(recommend==0){
                        if(rect.left>=100&&rect.left<1576){
                            historyRecycler.setHovered(true);
                            v.setFocusable(true);
                            v.requestFocusFromTouch();
                        }
                    }else{
                        if(rect.left>=100&&rect.left<1576){
                            favoriteRecycler.setHovered(true);
                            v.requestFocusFromTouch();
                        }
                    }
                }
                    break;

        }
    }

    @Override
    public void onItemKeyListener(View v, int keyCode, KeyEvent event) {
        Log.i("hoverX","onkey: "+keyCode);
        historyRecycler.setHovered(false);
        favoriteRecycler.setHovered(false);
        history_left_arrow.setFocusable(false);
        history_right_arrow.setFocusable(false);
        favorite_right_arrow.setFocusable(false);
        favorite_left_arrow.setFocusable(false);
        historyLayoutManager.setScrollEnabled(true);
        favoriteManager.setScrollEnabled(true);
        empty.setFocusable(false);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(event.getAction()==KeyEvent.ACTION_DOWN){
            v.setFocusable(false);
            v.setHovered(false);
        }
        return false;
    }


    class GetHistoryTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ArrayList<History> mHistories;
                if(IsmartvActivator.getInstance().isLogin()){
                    mHistories= DaisyUtils.getHistoryManager(HistoryFavoriteActivity.this).getAllHistories("yes");
                    ArrayList<History> localHistorys= DaisyUtils.getHistoryManager(HistoryFavoriteActivity.this).getAllHistories("no");
                    List<History> removeList = null;
					for (History localHistory :
							localHistorys) {
						for (History history:
							 mHistories) {
							if (localHistory.url.equals(history.url)) {
								if (removeList == null) {
									removeList = new ArrayList<>();
								}
								removeList.add(localHistory);
								break;
							}
						}
					}
					if (removeList != null) {
						localHistorys.removeAll(removeList);
					}
                    mHistories.addAll(localHistorys);
                }else{
                    mHistories= DaisyUtils.getHistoryManager(HistoryFavoriteActivity.this).getAllHistories("no");
                }

                Log.i("listSize","HistorySize: "+mHistories.size()+"");
                if(mHistories.size()>0) {
                    Collections.sort(mHistories);
                    for(int i=0;i<mHistories.size();i++) {
                        Log.i("listSize","time: "+mHistories.get(i).add_time+"");
                        History history = mHistories.get(i);
                        HistoryFavoriteEntity item = getItem(history);
                        allhistoryLists.add(item);
						if (allhistoryLists.size() == HistoryFavoriteShowLimitNumber) {
							break;
						}
                    }
                    Log.i("listSize","allhistoryLists: "+allhistoryLists.size()+"");
                    srotHistoryFavoriteList(allhistoryLists,historyLists);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            getFavoriteTask=new GetFavoriteTask();
            getFavoriteTask.execute();
        }

    }

    class GetFavoriteTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<Favorite> favorites;
            if(IsmartvActivator.getInstance().isLogin()){
                favorites= DaisyUtils.getFavoriteManager(HistoryFavoriteActivity.this).getAllFavorites("yes");
                ArrayList<Favorite> localFavorites=DaisyUtils.getFavoriteManager(HistoryFavoriteActivity.this).getAllFavorites("no");
				List<Favorite> removeList = null;
				for (Favorite localFavorite :
						localFavorites) {
					for (Favorite favorite:
							favorites) {
						if (localFavorite.url.equals(favorite.url)) {
							if (removeList == null) {
								removeList = new ArrayList<>();
							}
							removeList.add(localFavorite);
							break;
						}
					}
				}
				if (removeList != null) {
					localFavorites.removeAll(removeList);
				}
				favorites.addAll(localFavorites);
            }else{
                favorites= DaisyUtils.getFavoriteManager(HistoryFavoriteActivity.this).getAllFavorites("no");
            }
            if(favorites.size()>0) {
                Collections.sort(favorites);
                for (Favorite favorite : favorites) {
                    Log.i("listSize", "FavoriteTime: " + favorite.time / 1000);
                    HistoryFavoriteEntity item = getFavoriteItem(favorite);
                    allfavoriteLists.add(item);
                    if (allfavoriteLists.size() == HistoryFavoriteShowLimitNumber) {
                    	break;
					}
                }
            }
            srotHistoryFavoriteList(allfavoriteLists,favoriteLists);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
                loadData();
        }
    }

    private void srotHistoryFavoriteList(List<HistoryFavoriteEntity> list,List<HistoryFavoriteEntity> list2){
        int count=0;
        if(list.size()>0){
            for(int i=0;i<list.size();i++){
                if(count<3) {
                    HistoryFavoriteEntity item = list.get(i);
                    if (i == 0) {
                        item.setShowDate(true);
                    } else {
                    	Calendar calendar = Calendar.getInstance(Locale.CHINA);
                    	calendar.setTimeInMillis(item.getDate());
                    	Calendar lastCal = Calendar.getInstance(Locale.CHINA);
                    	lastCal.setTimeInMillis(list.get(i - 1).getDate());
                    	Log.i("favorite", String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
                    	if (calendar.get(Calendar.YEAR) == lastCal.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) == lastCal.get(Calendar.MONTH) && calendar.get(Calendar.DAY_OF_MONTH) == lastCal.get(Calendar.DAY_OF_MONTH)) {
							item.setShowDate(false);
						} else {
							item.setShowDate(true);
							count++;
						}
                    }
                    if(count<3) {
                        list2.add(item);
                    }else if(count==3){
                        HistoryFavoriteEntity more=new HistoryFavoriteEntity();
                        more.setType(2);
                        list2.add(more);
                        return;
                    }
                }
            }
        }
    }
    private HistoryFavoriteEntity getItem(History history){
        HistoryFavoriteEntity item=new HistoryFavoriteEntity();
        item.setAdlet_url(history.adlet_url);
        item.setIs_complex(history.is_complex);
        item.setContent_model(history.content_model);
        item.setQuality(history.quality);
        item.setTitle(history.title);
        item.setUrl(history.url);
        item.setModel_name(history.model_name);
        if(history.add_time==0){
            long time=0;
            DateFormat format=new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(TrueTime.now().getTime());
            String date=format.format(calendar.getTime());
            try {
                time= format.parse(date).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            history.add_time=time;
        }
        item.setDate(history.add_time);
        item.setSub_url(history.sub_url);
        item.setType(1);
//		if(history.price==0){
//			item.expense = null;
//		}
//		else{
        Expense expense = new Expense();
        if(history.price!=0)
            expense.price = history.price;
        if(history.cpid!=0)
            expense.cpid=history.cpid;
        if(history.cpname!=null)
            expense.cpname=history.cpname;
        if(history.cptitle!=null)
            expense.cptitle=history.cptitle;
        if(history.paytype!=-1)
            expense.pay_type=history.paytype;
        item.setExpense(expense);
        return item;
    }
    private HistoryFavoriteEntity getFavoriteItem(Favorite favorite){
        HistoryFavoriteEntity item=new HistoryFavoriteEntity();
        item.setAdlet_url(favorite.adlet_url);
        item.setIs_complex(favorite.is_complex);
        item.setContent_model(favorite.content_model);
        item.setQuality(favorite.quality);
        item.setTitle(favorite.title);
        item.setUrl(favorite.url);
        if(favorite.time==0){
            long time=0;
            DateFormat format=new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(TrueTime.now().getTime());
            String date=format.format(calendar.getTime());
            try {
                time= format.parse(date).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            favorite.time=time;
        }
        item.setDate(favorite.time);
        item.setType(1);
//		if(history.price==0){
//			item.expense = null;
//		}
//		else{
        Expense expense = new Expense();
        if(favorite.cpid!=0)
            expense.cpid=favorite.cpid;
        if(favorite.cpname!=null)
            expense.cpname=favorite.cpname;
        if(favorite.cptitle!=null)
            expense.cptitle=favorite.cptitle;
        if(favorite.paytype!=-1)
            expense.pay_type=favorite.paytype;
        item.setExpense(expense);
        return item;
    }

}
