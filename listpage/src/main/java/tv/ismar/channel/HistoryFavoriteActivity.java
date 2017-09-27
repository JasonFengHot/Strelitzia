package tv.ismar.channel;

import android.content.Intent;
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
import android.widget.ImageView;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
import tv.ismar.listener.LfListItemClickListener;
import tv.ismar.listpage.R;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.view.IsmartvLinearLayout;

import static tv.ismar.listpage.R.id.arrow_line_2;
import static tv.ismar.listpage.R.id.vip_image;


/**
 * Created by liucan on 2017/8/22.
 */

public class HistoryFavoriteActivity extends BaseActivity implements View.OnClickListener,OnItemFocusedListener,LfListItemClickListener,View.OnHoverListener,OnItemOnhoverlistener,OnItemKeyListener{
    private GetHistoryTask mGetHistoryTask;
    private Subscription historySub,favoriteSub;
    private SkyService skyService;
    private RecyclerViewTV historyRecycler,favoriteRecycler;
    private HistoryListAdapter historyAdapter;
    private HistoryListAdapter favoritAdapter;
    private LinearLayout edit_history;
    private LinearLayout recommend_list;
    private ImageView arrow_line1,arrow_line2;
    private TextView edit_text;
    private IsmartvLinearLayout delet_history,delete_favorite;
    private LinearLayout favorite_layout,list_layout;
    private LinearLayout no_data;
    private RelativeLayout history_relativelayout,favorite_relativeLayout;
    private static final int HISTORY=1;
    private static final int FAVORITE=2;
    private List<HistoryFavoriteEntity> historyLists=new ArrayList<>();
    private List<HistoryFavoriteEntity> favoriteLists=new ArrayList<>();
    private HistoryLinerlayoutMananger historyLayoutManager,favoriteManager;
    private GetFavoriteTask getFavoriteTask;
    private TextView favorite_title,history_title;
    private ImageView first_line_image,second_line_image;
    private ImageView edit_shadow;
    private HashMap<String, Object> mDataCollectionProperties;
    private Boolean isEdit=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_favorite_layout);
        skyService=SkyService.ServiceManager.getService();
        initView();
    }
    private void initView(){
        historyRecycler= (RecyclerViewTV) findViewById(R.id.history_list);
        favoriteRecycler= (RecyclerViewTV) findViewById(R.id.favorite_list);
        favorite_layout= (LinearLayout) findViewById(R.id.favorite_layout);
        list_layout= (LinearLayout) findViewById(R.id.list_layout);
        no_data= (LinearLayout) findViewById(R.id.no_data);
        delet_history= (IsmartvLinearLayout) findViewById(R.id.history_edit);
        favorite_title= (TextView) findViewById(R.id.favorite_lyout_title);
        first_line_image= (ImageView) findViewById(R.id.first_line_delete_image);
        recommend_list= (LinearLayout) findViewById(R.id.recommend_list);
        edit_text= (TextView) findViewById(R.id.edit_btn_text);
        second_line_image= (ImageView) findViewById(R.id.second_line_delete_image);
        history_title= (TextView) findViewById(R.id.history_layout_title);
        history_relativelayout= (RelativeLayout) findViewById(R.id.history_layout);
        favorite_relativeLayout= (RelativeLayout) findViewById(R.id.favorite_relateLayout);
        delete_favorite= (IsmartvLinearLayout) findViewById(R.id.favorite_edit);
        edit_shadow= (ImageView) findViewById(R.id.edit_shadow);
        arrow_line1= (ImageView) findViewById(R.id.arrow_line_1);
        arrow_line2= (ImageView) findViewById(arrow_line_2);
        delet_history.setOnClickListener(this);
        delete_favorite.setOnClickListener(this);
        historyLayoutManager=new HistoryLinerlayoutMananger(this);
        historyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        favoriteManager=new HistoryLinerlayoutMananger(this);
        favoriteManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        historyRecycler.setLayoutManager(historyLayoutManager);
        favoriteRecycler.setLayoutManager(favoriteManager);
        historyRecycler.setSelectedItemAtCentered(true);
//        historyRecycler.setSelectedItemOffset(251,250);
        edit_history= (LinearLayout) findViewById(R.id.edit_btn);
        edit_history.setOnClickListener(this);
        edit_history.setOnHoverListener(this);
        editBtnFocusListener();
        delet_history.setOnHoverListener(this);
        delete_favorite.setOnHoverListener(this);
       // edit_shadow.setOnHoverListener(this);

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
        AppConstant.purchase_referer = "history";
        AppConstant.purchase_page = "history";
        AppConstant.purchase_channel = "";
        AppConstant.purchase_entrance_channel = "";
        AppConstant.purchase_entrance_page = "history";
        BaseActivity.baseChannel="";
        BaseActivity.baseSection="";

        if(IsmartvActivator.getInstance().isLogin()){
            //登录，网络获取
            getHistoryByNet();
        }else{
            //没有登录，取本地设备信息
            mGetHistoryTask = new GetHistoryTask();
            mGetHistoryTask.execute();
        }
        edit_history.setFocusable(false);
        edit_history.setFocusableInTouchMode(false);
        super.onResume();
    }

    @Override
    protected void onPause() {
        historyLists.clear();
        favoriteLists.clear();

        HashMap<String, Object> properties = mDataCollectionProperties;
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_HISTORY_OUT, properties);
        mDataCollectionProperties = null;
        super.onPause();
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
                        Log.i("result",result);
                        historyLists= parseResult(result,HISTORY);
                        getFavoriteByNet();
//                        historyAdapter=new HistoryListAdapter(HistoryFavoriteActivity.this,mlists);
//                        historyAdapter.setItemFocusedListener(HistoryFavoriteActivity.this);
//                        historyRecycler.setAdapter(historyAdapter);
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
                        Log.i("result",result);
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
                for(int j=0;j<element.length();j++){
                    HistoryFavoriteEntity historyFavoriteEntity=new GsonBuilder().create().fromJson(element.get(j).toString(),HistoryFavoriteEntity.class);
                    historyFavoriteEntity.setDate(date.getString(i));
                    lists.add(historyFavoriteEntity);
                }
            }
            if(lists.size()>0) {
                HistoryFavoriteEntity end = new HistoryFavoriteEntity();
                lists.add(end);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lists;
    }

    private void loadData(){
        if(historyLists.size()>0){
            no_data.setVisibility(View.GONE);
            history_relativelayout.setVisibility(View.VISIBLE);
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
                    if(!isEdit)
                        historyRecycler.getChildAt(0).requestFocusFromTouch();
                    edit_history.setFocusable(true);
                    edit_history.setFocusableInTouchMode(true);
                }
            },600);
        }else{
            if(favoriteLists.size()>0){
                no_data.setVisibility(View.GONE);
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

                history_title.setText("收藏");
                history_title.setVisibility(View.VISIBLE);
                first_line_image.setBackgroundResource(R.drawable.favorite_delete_image);
                historyAdapter=new HistoryListAdapter(HistoryFavoriteActivity.this,favoriteLists,"favorite");
                setHistoryListen();
                historyRecycler.setAdapter(historyAdapter);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!isEdit)
                            historyRecycler.getChildAt(0).requestFocusFromTouch();
                        edit_history.setFocusable(true);
                        edit_history.setFocusableInTouchMode(true);
                    }
                },500);
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
            intent.putExtra("List",(Serializable) favoriteLists);
            startActivity(intent);
        }else if(id==R.id.history_edit){
            if(historyLists.size()>1) {
                intent.putExtra("type", 1);
                intent.putExtra("List", (Serializable) historyLists);
            }else{
                intent.putExtra("type",2);
                intent.putExtra("List",(Serializable) favoriteLists);
            }
            startActivity(intent);
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
            ImageView detail= (ImageView) container.findViewById(R.id.item_image);
            ImageView vip= (ImageView) container.findViewById(vip_image);
            IsmartvLinearLayout item= (IsmartvLinearLayout) container.findViewById(R.id.no_data_item);
            TextView focus= (TextView) container.findViewById(R.id.focus_title);
            TextView title= (TextView) container.findViewById(R.id.title);
            final VideoEntity.Objects object=videoEntity.getObjects().get(i);

            Picasso.with(this).load(object.getImage()).into(detail);
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
                            intent.toPlayPageEpisode(HistoryFavoriteActivity.this, pk, 0, Source.HISTORY, object.getContent_model());
                        }
                    }
                }
            });
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(getResources().getDimensionPixelOffset(R.dimen.history_336),getResources().getDimensionPixelOffset(R.dimen.history_243));
            params.setMargins(getResources().getDimensionPixelOffset(R.dimen.history_44),getResources().getDimensionPixelOffset(R.dimen.history_20),0,0);
            container.setLayoutParams(params);
            recommend_list.addView(container);
        }
    }

    @Override
    public void onItemfocused(View view, int position, boolean hasFocus) {
        RecyclerView.LayoutManager layoutManager = historyRecycler.getLayoutManager();
        Log.i("onItemfocus","position: "+position+"  lastPosition: "+((LinearLayoutManager) layoutManager).findLastVisibleItemPosition());
        if(hasFocus){
            JasmineUtil.scaleOut3(view);
        }else{
            JasmineUtil.scaleIn3(view);
        }

    }

    @Override
    public void onlfItemClick(View v, int postion, String type) {
        PageIntent intent=new PageIntent();
        int pk=0;
        if(type.equals("history")){
            HistoryFavoriteEntity history=historyLists.get(postion);
            boolean[] isSubItem = new boolean[1];
             pk = SimpleRestClient.getItemId(history.getUrl(), isSubItem);
            if(pk==0){
                pk=history.getPk();
            }
            if(postion!=historyLists.size()-1) {
                intent.toPlayPage(this, pk, 0, Source.HISTORY);
            }else{
                Intent intent1=new Intent();
                intent1.setAction("tv.ismar.daisy.historyfavoriteList");
                intent1.putExtra("source","list");
                intent1.putExtra("type",1);
                intent1.putExtra("List",(Serializable) historyLists);
                startActivity(intent1);
            }
        }else {
            HistoryFavoriteEntity favoriteEntity=favoriteLists.get(postion);
            boolean[] isSubItem = new boolean[1];
            pk = SimpleRestClient.getItemId(favoriteEntity.getUrl(), isSubItem);
            if(pk==0){
                pk=favoriteEntity.getPk();
            }
            if(postion!=favoriteLists.size()-1) {
                if(favoriteEntity.getContent_model().contains("gather")){
                    intent.toSubject(this,favoriteEntity.getContent_model(),pk,favoriteEntity.getTitle(),"favorite","");
                }else {
                    if (favoriteEntity.getIs_complex()) {
                        intent.toDetailPage(this, "favorite", pk);
                    } else {
                        intent.toPlayPageEpisode(this, pk, 0, Source.FAVORITE,favoriteEntity.getContent_model());
                    }
                }
            }else{
                Intent intent1=new Intent();
                intent1.setAction("tv.ismar.daisy.historyfavoriteList");
                intent1.putExtra("source","list");
                intent1.putExtra("type",2);
                intent1.putExtra("List",(Serializable) favoriteLists);
                startActivity(intent1);
            }
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_ENTER:
                v.requestFocusFromTouch();
                break;
        }
        return false;
    }

    @Override
    public void OnItemOnhoverlistener(View v, MotionEvent event, int position, int recommend) {
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_ENTER:
                    historyRecycler.setHoverd(true);
                    favoriteRecycler.setHoverd(true);
                    v.requestFocusFromTouch();
                    break;

        }
    }

    @Override
    public void onItemKeyListener(View v, int keyCode, KeyEvent event) {
        historyRecycler.setHoverd(false);
        favoriteRecycler.setHoverd(false);
    }


    class GetHistoryTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ArrayList<History> mHistories = DaisyUtils.getHistoryManager(HistoryFavoriteActivity.this).getAllHistories("no");
                Log.i("listSize","HistorySize: "+mHistories.size()+"");
                if(mHistories.size()>0) {
                    Collections.sort(mHistories);
                    for(int i=0;i<mHistories.size();++i) {
                        History history = mHistories.get(i);
                        HistoryFavoriteEntity item = getItem(history);
                        historyLists.add(item);
                    }
                    historyLists.add(new HistoryFavoriteEntity());
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
            ArrayList<Favorite> favorites = DaisyUtils.getFavoriteManager(HistoryFavoriteActivity.this).getAllFavorites("no");
            Log.i("listSize","Favorite: "+favorites.size()+"");
            for(Favorite favorite:favorites){
                HistoryFavoriteEntity item=getFavoriteItem(favorite);
                favoriteLists.add(item);
            }
            if(favoriteLists.size()>0)
            favoriteLists.add(new HistoryFavoriteEntity());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
                loadData();
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
        item.setDate(history.add_time);
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
        item.setDate(favorite.time);
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
