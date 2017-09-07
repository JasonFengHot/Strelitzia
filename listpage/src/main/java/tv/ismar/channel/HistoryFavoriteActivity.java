package tv.ismar.channel;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.gson.GsonBuilder;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemCollection;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.app.widget.OpenView;
import tv.ismar.entity.HistoryFavoriteEntity;
import tv.ismar.listener.LfListItemClickListener;
import tv.ismar.listpage.R;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.view.IsmartvLinearLayout;


/**
 * Created by liucan on 2017/8/22.
 */

public class HistoryFavoriteActivity extends BaseActivity implements View.OnClickListener,OnItemFocusedListener,LfListItemClickListener,View.OnHoverListener{
    private GetHistoryTask mGetHistoryTask;
    private Subscription historySub,favoriteSub;
    private SkyService skyService;
    private RecyclerViewTV historyRecycler,favoriteRecycler;
    private HistoryListAdapter historyAdapter;
    private HistoryListAdapter favoritAdapter;
    private LinearLayout edit_history;
    private IsmartvLinearLayout delet_history,delete_favorite;
    private LinearLayout favorite_layout,list_layout;
    private RelativeLayout history_relativelayout,favorite_relativeLayout;
    private static final int HISTORY=1;
    private static final int FAVORITE=2;
    private List<HistoryFavoriteEntity> historyLists=new ArrayList<>();
    private List<HistoryFavoriteEntity> favoriteLists=new ArrayList<>();
    private HistoryLinerlayoutMananger historyLayoutManager,favoriteManager;
    private GetFavoriteTask getFavoriteTask;
    private TextView favorite_title,history_title;
    private ImageView first_line_image,second_line_image;
    private HashMap<String, Object> mDataCollectionProperties;
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
        delet_history= (IsmartvLinearLayout) findViewById(R.id.history_edit);
        favorite_title= (TextView) findViewById(R.id.favorite_lyout_title);
        first_line_image= (ImageView) findViewById(R.id.first_line_delete_image);
        second_line_image= (ImageView) findViewById(R.id.second_line_delete_image);
        history_title= (TextView) findViewById(R.id.history_layout_title);
        history_relativelayout= (RelativeLayout) findViewById(R.id.history_layout);
        favorite_relativeLayout= (RelativeLayout) findViewById(R.id.favorite_relateLayout);
        delete_favorite= (IsmartvLinearLayout) findViewById(R.id.favorite_edit);
        delet_history.setOnClickListener(this);
        delete_favorite.setOnClickListener(this);
        editBtnFocusListener();
        historyLayoutManager=new HistoryLinerlayoutMananger(this);
        historyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        favoriteManager=new HistoryLinerlayoutMananger(this);
        favoriteManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        historyRecycler.setLayoutManager(historyLayoutManager);
        favoriteRecycler.setLayoutManager(favoriteManager);
        historyRecycler.setSelectedItemAtCentered(true);
        edit_history= (LinearLayout) findViewById(R.id.edit_btn);
        edit_history.setOnClickListener(this);
        edit_history.setOnHoverListener(this);

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
            edit_history.setVisibility(View.VISIBLE);
            if(favoriteLists.size()>0){
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getResources().getDimensionPixelSize(R.dimen.history_473));
                lp.setMargins(0,getResources().getDimensionPixelSize(R.dimen.history_10),0,0);
                history_relativelayout.setLayoutParams(lp);
                history_relativelayout.setVisibility(View.VISIBLE);
                favorite_relativeLayout.setVisibility(View.VISIBLE);
                favorite_title.setText("收藏");
                favorite_title.setVisibility(View.VISIBLE);
                second_line_image.setBackgroundResource(R.drawable.favorite_delete_image);
                favoritAdapter=new HistoryListAdapter(HistoryFavoriteActivity.this,favoriteLists,"favorite");
                favoritAdapter.setItemFocusedListener(HistoryFavoriteActivity.this);
                favoritAdapter.setItemClickListener(HistoryFavoriteActivity.this);
                favoriteRecycler.setAdapter(favoritAdapter);

                history_title.setText("历史");
                history_title.setVisibility(View.VISIBLE);
                first_line_image.setBackgroundResource(R.drawable.history_delete_image);
                historyAdapter=new HistoryListAdapter(HistoryFavoriteActivity.this,historyLists,"history");
                historyAdapter.setItemFocusedListener(HistoryFavoriteActivity.this);
                historyAdapter.setItemClickListener(HistoryFavoriteActivity.this);
                historyRecycler.setAdapter(historyAdapter);

            }else{
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getResources().getDimensionPixelSize(R.dimen.history_473));
                lp.setMargins(0,getResources().getDimensionPixelSize(R.dimen.history_50),0,0);
                history_relativelayout.setLayoutParams(lp);
                history_relativelayout.setVisibility(View.VISIBLE);
                favorite_relativeLayout.setVisibility(View.GONE);
                history_title.setText("历史");
                first_line_image.setBackgroundResource(R.drawable.history_delete_image);
                historyAdapter=new HistoryListAdapter(HistoryFavoriteActivity.this,historyLists,"history");
                historyAdapter.setItemFocusedListener(HistoryFavoriteActivity.this);
                historyAdapter.setItemClickListener(HistoryFavoriteActivity.this);
                historyRecycler.setAdapter(historyAdapter);

            }
        }else{
            if(favoriteLists.size()>0){
                edit_history.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getResources().getDimensionPixelSize(R.dimen.history_473));
                lp.setMargins(0,getResources().getDimensionPixelSize(R.dimen.history_50),0,0);
                history_relativelayout.setLayoutParams(lp);
                favorite_relativeLayout.setVisibility(View.GONE);
                history_title.setText("收藏");
                history_title.setVisibility(View.VISIBLE);
                first_line_image.setBackgroundResource(R.drawable.favorite_delete_image);
                historyAdapter=new HistoryListAdapter(HistoryFavoriteActivity.this,favoriteLists,"favorite");
                historyAdapter.setItemFocusedListener(HistoryFavoriteActivity.this);
                historyAdapter.setItemClickListener(HistoryFavoriteActivity.this);
                historyRecycler.setAdapter(historyAdapter);
            }else{
                history_relativelayout.setVisibility(View.GONE);
                favorite_relativeLayout.setVisibility(View.GONE);
                history_title.setVisibility(View.GONE);
                favorite_title.setVisibility(View.GONE);
                edit_history.setVisibility(View.INVISIBLE);
            }
        }
    }
    @Override
    public void onClick(View v) {
        int id=v.getId();
        Intent intent=new Intent();
        intent.setAction("tv.ismar.daisy.historyfavoriteList");
        intent.putExtra("source","edit");
        if(id==R.id.edit_btn){
            historyRecycler.scrollToPosition(0);
            favoriteRecycler.scrollToPosition(0);
            delet_history.setVisibility(View.VISIBLE);
            delete_favorite.setVisibility(View.VISIBLE);
            edit_history.setVisibility(View.GONE);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins(getResources().getDimensionPixelSize(R.dimen.history_492),0,0,0);
            list_layout.setLayoutParams(lp);
            favorite_layout.setLayoutParams(lp);

            historyLayoutManager.setScrollEnabled(false);

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
        if(edit_history.getVisibility()==View.GONE){
            edit_history.setVisibility(View.VISIBLE);
            delet_history.setVisibility(View.GONE);
            delete_favorite.setVisibility(View.GONE);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins(0,0,0,0);
            list_layout.setLayoutParams(lp);
            favorite_layout.setLayoutParams(lp);
            historyLayoutManager.setScrollEnabled(true);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void onItemfocused(View view, int position, boolean hasFocus) {
        RecyclerView.LayoutManager layoutManager = historyRecycler.getLayoutManager();
        Log.i("onItemfocus","position: "+position+"  lastPosition: "+((LinearLayoutManager) layoutManager).findLastVisibleItemPosition());
        if(position==((LinearLayoutManager) layoutManager).findLastVisibleItemPosition()){
        }
        if(hasFocus){
            JasmineUtil.scaleOut3(view);
        }else{
            JasmineUtil.scaleIn3(view);
        }

    }

    @Override
    public void onlfItemClick(View v, int postion, String type) {
        PageIntent intent=new PageIntent();
        if(type.equals("history")){
            HistoryFavoriteEntity history=historyLists.get(postion);
            boolean[] isSubItem = new boolean[1];
            int pk = SimpleRestClient.getItemId(history.getUrl(), isSubItem);
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
            int pk = SimpleRestClient.getItemId(favoriteEntity.getUrl(), isSubItem);
            if(postion!=favoriteLists.size()-1) {
                intent.toDetailPage(this, "favorite", pk);
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
        item.setDate(history.last_played_time+"");
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
