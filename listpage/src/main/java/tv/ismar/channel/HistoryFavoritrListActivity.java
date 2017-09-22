package tv.ismar.channel;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.adapter.FocusGridLayoutManager;
import tv.ismar.adapter.HistoryFavoriteListAdapter;
import tv.ismar.adapter.HistorySpaceItemDecoration;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.ui.adapter.OnItemClickListener;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.app.widget.MyRecyclerView;
import tv.ismar.app.entity.HistoryFavoriteEntity;
import tv.ismar.listpage.R;
import tv.ismar.searchpage.utils.JasmineUtil;

/**
 * Created by liucan on 2017/8/29.
 */

public class HistoryFavoritrListActivity extends BaseActivity implements OnItemClickListener,OnItemFocusedListener{
    private Subscription listSub;
    private Subscription removeSub;
    private SkyService skyService;
    private int type=0;
    private String source="";
    private MyRecyclerView recyclerView;
    private TextView title,clearAll;
    private List<HistoryFavoriteEntity> mlists=new ArrayList<>();
    private HistoryFavoriteListAdapter adapter;
    private FocusGridLayoutManager focusGridLayoutManager;
    private HistorySpaceItemDecoration mSpaceItemDecoration;
    private Button arrow_up,arrow_down;
    private ModuleMessagePopWindow pop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_favorite_list_layout);
        Intent intent=getIntent();
        type=intent.getIntExtra("type",0);
        source=intent.getStringExtra("source");
        mlists= (List<HistoryFavoriteEntity>) intent.getSerializableExtra("List");
        skyService=SkyService.ServiceManager.getService();
        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initView(){
        title= (TextView) findViewById(R.id.title);
        clearAll= (TextView) findViewById(R.id.clear_all);
        arrow_down= (Button) findViewById(R.id.poster_arrow_down);
        arrow_up= (Button) findViewById(R.id.poster_arrow_up);
        mSpaceItemDecoration=new HistorySpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.history_30),0,getResources().getDimensionPixelSize(R.dimen.history_16),getResources().getDimensionPixelOffset(R.dimen.history_44));
        recyclerView=findView(R.id.history_favorite_list);
        recyclerView.addItemDecoration(mSpaceItemDecoration);
        focusGridLayoutManager=new FocusGridLayoutManager(this,4);
        recyclerView.setLayoutManager(focusGridLayoutManager);
        if(source.equals("edit")){
            if(type==1) {
                title.setText("编辑历史记录");
            }else {
                title.setText("编辑收藏记录");
            }
            clearAll.setVisibility(View.VISIBLE);
        }else if(source.equals("list")){
            if(type==1) {
                title.setText("历史记录");
            }else {
                title.setText("收藏记录");
            }
            clearAll.setVisibility(View.GONE);
        }
        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPop();
            }
        });
        loadData();
    }

    private void showPop() {
        pop=new ModuleMessagePopWindow(this);
        pop.setMessage("确认删除全部记录!");
        pop.setConfirmBtn("确认");
        pop.setCancelBtn("取消");
        pop.showAtLocation(getRootView(), Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
            @Override
            public void confirmClick(View view) {
                if(type==1){
                    emptyHistories();
                }else{
                    emptyFavorite();
                }
            }
        }, new ModuleMessagePopWindow.CancelListener() {
            @Override
            public void cancelClick(View view) {
                pop.dismiss();
            }
        });
    }

    private void loadData(){
        mlists.remove(mlists.size()-1);
        adapter=new HistoryFavoriteListAdapter(HistoryFavoritrListActivity.this,mlists,type,source);
        adapter.setItemClickListener(HistoryFavoritrListActivity.this);
        adapter.setItemFocusedListener(HistoryFavoritrListActivity.this);
        recyclerView.setAdapter(adapter);
        if(mlists.size()>0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    recyclerView.getChildAt(0).requestFocusFromTouch();
                }
            }, 500);
        }
    }
    @Override
    public void onItemClick(View view, int position) {
        PageIntent intent=new PageIntent();
        HistoryFavoriteEntity entity=mlists.get(position);
        boolean[] isSubItem = new boolean[1];
        int pk = SimpleRestClient.getItemId(entity.getUrl(), isSubItem);
        if(source.equals("edit")){
            if(type==1){
                deleteHistory(pk,entity.getItem_pk(),position);
            }else{
                deleteBookmark(pk,position);
            }
        }else{
            if(type==1){
                intent.toPlayPage(this,pk,0, Source.HISTORY);
            }else{
                intent.toDetailPage(this,"history",pk);
            }
        }
    }
    private void deleteHistory(int pk, int item_pk, final int position){
        if(!IsmartvActivator.getInstance().isLogin()) {
            DaisyUtils.getHistoryManager(this).deleteHistory(mlists.get(position).getUrl(),"no");
            mlists.remove(position);
            adapter.notifyDataSetChanged();
        }else{
            removeSub = skyService.apiHistoryRemove(pk, item_pk).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<ResponseBody>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            mlists.remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    });
        }
    }
    private void deleteBookmark(int pk, final int position){
        if(!IsmartvActivator.getInstance().isLogin()){
            DaisyUtils.getFavoriteManager(this).deleteFavoriteByUrl(mlists.get(position).getUrl(),"no");
            mlists.remove(position);
            adapter.notifyDataSetChanged();
        }else {
            removeSub = skyService.apiBookmarksRemove(pk+"").subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<ResponseBody>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            mlists.remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    });
        }
    }
    private void emptyHistories(){
        if(!IsmartvActivator.getInstance().isLogin()){
            DaisyUtils.getHistoryManager(this).deleteAll("no");
            mlists.clear();
            adapter.notifyDataSetChanged();
            if(pop!=null)
                pop.dismiss();
        }else {
            DaisyUtils.getHistoryManager(this).deleteAll("no");
            removeSub = skyService.emptyHistory(IsmartvActivator.getInstance().getDeviceToken()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<ResponseBody>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            mlists.clear();
                            adapter.notifyDataSetChanged();
                            if(pop!=null)
                                pop.dismiss();
                        }
                    });
        }
    }
    private void emptyFavorite(){
        if(!IsmartvActivator.getInstance().isLogin()){
            DaisyUtils.getFavoriteManager(this).deleteAll("no");
            mlists.clear();
            adapter.notifyDataSetChanged();
            if(pop!=null)
                pop.dismiss();
        }else {
            DaisyUtils.getFavoriteManager(this).deleteAll("no");
            removeSub=skyService.emptyBookmarks(IsmartvActivator.getInstance().getDeviceToken())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<ResponseBody>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            mlists.clear();
                            adapter.notifyDataSetChanged();
                            if(pop!=null)
                                pop.dismiss();
                        }
                    });
        }
    }

    @Override
    public void onItemfocused(View view, int position, boolean hasFocus) {
        if(hasFocus){
            JasmineUtil.scaleOut3(view);
            Log.i("ViewY",view.getY()+"");
            if(view.getY()>getResources().getDimensionPixelSize(R.dimen.history_636)){
                focusGridLayoutManager.setCanScroll(true);
                recyclerView.smoothScrollBy(0,getResources().getDimensionPixelOffset(R.dimen.history_732));
            }else if(view.getY()<0){
                focusGridLayoutManager.setCanScroll(true);
                recyclerView.smoothScrollBy(0,-getResources().getDimensionPixelOffset(R.dimen.history_732));
            }
        }else{
            JasmineUtil.scaleIn3(view);
        }
    }
}
