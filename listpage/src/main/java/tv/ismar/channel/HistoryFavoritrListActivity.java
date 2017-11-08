package tv.ismar.channel;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
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
import tv.ismar.app.entity.Favorite;
import tv.ismar.app.entity.History;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.ui.ToastTip;
import tv.ismar.app.ui.adapter.OnItemClickListener;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.app.ui.adapter.OnItemOnhoverlistener;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.app.widget.MyRecyclerView;
import tv.ismar.app.entity.HistoryFavoriteEntity;
import tv.ismar.listpage.R;
import tv.ismar.searchpage.utils.JasmineUtil;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * Created by liucan on 2017/8/29.
 */

public class HistoryFavoritrListActivity extends BaseActivity implements OnItemClickListener,OnItemFocusedListener,OnItemOnhoverlistener,View.OnHoverListener,View.OnClickListener{
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
    private RelativeLayout parent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view= LayoutInflater.from(this).inflate(R.layout.history_favorite_list_layout,null);
        setContentView(view);
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
        parent= (RelativeLayout) findViewById(R.id.parent_view);
        parent.setOnHoverListener(this);
        arrow_down= (Button) findViewById(R.id.poster_arrow_down);
        arrow_up= (Button) findViewById(R.id.poster_arrow_up);
        mSpaceItemDecoration=new HistorySpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.history_30),0,getResources().getDimensionPixelSize(R.dimen.history_16),getResources().getDimensionPixelOffset(R.dimen.history_44));
        recyclerView=findView(R.id.history_favorite_list);
        recyclerView.addItemDecoration(mSpaceItemDecoration);
        focusGridLayoutManager=new FocusGridLayoutManager(this,4);
        focusGridLayoutManager.setFavorite(true);
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
        clearAll.setOnHoverListener(this);
        clearAll.setOnClickListener(this);
        arrow_down.setOnHoverListener(this);
        arrow_up.setOnHoverListener(this);
        arrow_down.setOnClickListener(this);
        arrow_up.setOnClickListener(this);
        loadData();
        recyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    if(recyclerView.getChildAt(0)!=null){
                        recyclerView.getChildAt(0).requestFocusFromTouch();
                    }
                }
            }
        });
        clearAll.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==20){
                    if(lastFocusView!=null){
                        lastFocusView.requestFocusFromTouch();
                    }
                }
                return false;
            }
        });
    }

    private void showPop() {
        pop=new ModuleMessagePopWindow(this);
        if(type==1) {
            pop.setMessage("您需要清空所有历史记录吗？");
        }else{
            pop.setMessage("您需要清空所有收藏记录吗？");
        }
        pop.setConfirmBtn("确认清空");
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
        adapter=new HistoryFavoriteListAdapter(HistoryFavoritrListActivity.this,mlists,type,source);
        adapter.setItemClickListener(HistoryFavoritrListActivity.this);
        adapter.setItemFocusedListener(HistoryFavoritrListActivity.this);
        adapter.setItemOnhoverlistener(HistoryFavoritrListActivity.this);
        recyclerView.setAdapter(adapter);
        if(mlists.size()>0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(recyclerView.getChildAt(0)!=null)
                    recyclerView.getChildAt(0).requestFocusFromTouch();
                }
            }, 500);
        }
        if(mlists.size()>8){
            arrow_down.setVisibility(View.VISIBLE);
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(newState==SCROLL_STATE_IDLE){
                    if(focusGridLayoutManager!=null){
                        int pos=focusGridLayoutManager.findFirstCompletelyVisibleItemPosition();
                        int endPos=focusGridLayoutManager.findLastCompletelyVisibleItemPosition();
                        if(pos!=0){
                            arrow_up.setVisibility(View.VISIBLE);
                        }else{
                            arrow_up.setVisibility(View.GONE);
                        }
                        if(endPos!=mlists.size()-1){
                            arrow_down.setVisibility(View.VISIBLE);
                        }else{
                            arrow_down.setVisibility(View.GONE);
                        }

                    }

                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }
    @Override
    public void onItemClick(View view, int position) {
        PageIntent intent=new PageIntent();
        HistoryFavoriteEntity entity=mlists.get(position);
        int pk=0;
        boolean[] isSubItem = new boolean[1];
         pk = SimpleRestClient.getItemId(entity.getUrl(), isSubItem);
        if(pk==0){
            pk=entity.getPk();
        }
        if(source.equals("edit")){
            if(type==1){
                deleteHistory(entity.getItem_pk(),pk,position,entity.getModel_name());
            }else{
                deleteBookmark(pk,position);
            }
        }else{
            if(type==1){
                intent.toPlayPage(this,pk,0, Source.HISTORY);
            }else{
                if(entity.getContent_model().contains("gather")){
                    intent.toSubject(this,entity.getContent_model(),pk,entity.getTitle(),"favorite","");
                }else {
                    intent.toDetailPage(this, "favorite", pk);
                }
            }
        }
    }
    private void deleteHistory(int pk, int item_pk, final int position,String modelName){
        if(!IsmartvActivator.getInstance().isLogin()) {
            DaisyUtils.getHistoryManager(this).deleteHistory(mlists.get(position).getUrl(),"no");
            mlists.remove(position);
            adapter.notifyDataSetChanged();
            if(mlists.size()==0){
                clearAll.setVisibility(View.GONE);
            }else{
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(recyclerView.getChildAt(0)!=null)
                            recyclerView.getChildAt(0).requestFocusFromTouch();
                    }
                },200);
            }
        }else{
            History history=DaisyUtils.getHistoryManager(this).getHistoryByUrl(mlists.get(position).getUrl(),"yes");
            if(history!=null){
                if(modelName!=null&&!modelName.equals("subitem")){
                    item_pk=0;
                }
                removeSub = skyService.apiHistoryRemove(pk, item_pk).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseObserver<ResponseBody>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onNext(ResponseBody responseBody) {
                                DaisyUtils.getHistoryManager(HistoryFavoritrListActivity.this).deleteHistory(mlists.get(position).getUrl(),"yes");
                                mlists.remove(position);
                                adapter.notifyDataSetChanged();
                                if(mlists.size()==0){
                                    clearAll.setVisibility(View.GONE);
                                }else{
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(recyclerView.getChildAt(0)!=null)
                                                recyclerView.getChildAt(0).requestFocusFromTouch();
                                        }
                                    },200);
                                }
                            }
                        });
            }else{
                DaisyUtils.getHistoryManager(HistoryFavoritrListActivity.this).deleteHistory(mlists.get(position).getUrl(),"no");
                mlists.remove(position);
                adapter.notifyDataSetChanged();
                if(mlists.size()==0){
                    clearAll.setVisibility(View.GONE);
                }else{
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(recyclerView.getChildAt(0)!=null)
                                recyclerView.getChildAt(0).requestFocusFromTouch();
                        }
                    },200);
                }
            }

        }
    }
    private void deleteBookmark(int pk, final int position){
        if(!IsmartvActivator.getInstance().isLogin()){
            DaisyUtils.getFavoriteManager(this).deleteFavoriteByUrl(mlists.get(position).getUrl(),"no");
            mlists.remove(position);
            adapter.notifyDataSetChanged();
            if(mlists.size()==0){
                clearAll.setVisibility(View.GONE);
            }else{
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(recyclerView.getChildAt(0)!=null)
                            recyclerView.getChildAt(0).requestFocusFromTouch();
                    }
                },200);
            }
        }else {
            Favorite favorite=DaisyUtils.getFavoriteManager(this).getFavoriteByUrl(mlists.get(position).getUrl(),"yes");
            if(favorite==null){
                DaisyUtils.getFavoriteManager(HistoryFavoritrListActivity.this).deleteFavoriteByUrl(mlists.get(position).getUrl(),"no");
                mlists.remove(position);
                adapter.notifyDataSetChanged();
                if(mlists.size()==0){
                    clearAll.setVisibility(View.GONE);
                }else{
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(recyclerView.getChildAt(0)!=null)
                                recyclerView.getChildAt(0).requestFocusFromTouch();
                        }
                    },200);
                }
            }else {
                removeSub = skyService.apiBookmarksRemove(pk + "").subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseObserver<ResponseBody>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onNext(ResponseBody responseBody) {
                                DaisyUtils.getFavoriteManager(HistoryFavoritrListActivity.this).deleteFavoriteByUrl(mlists.get(position).getUrl(), "yes");
                                mlists.remove(position);
                                adapter.notifyDataSetChanged();
                                if (mlists.size() == 0) {
                                    clearAll.setVisibility(View.GONE);
                                } else {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (recyclerView.getChildAt(0) != null)
                                                recyclerView.getChildAt(0).requestFocusFromTouch();
                                        }
                                    }, 200);
                                }
                            }
                        });
            }
        }
    }
    private void emptyHistories(){
        if(!IsmartvActivator.getInstance().isLogin()){
            DaisyUtils.getHistoryManager(this).deleteAll("no");
            DaisyUtils.getHistoryManager(this).deleteAll("yes");
            mlists.clear();
            adapter.notifyDataSetChanged();
            clearAll.setVisibility(View.GONE);
            if(pop!=null)
                pop.dismiss();
            ToastTip.showToast(this,"您已清空所有历史记录");
        }else {
            removeSub = skyService.emptyHistory(IsmartvActivator.getInstance().getDeviceToken()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<ResponseBody>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            DaisyUtils.getHistoryManager(HistoryFavoritrListActivity.this).deleteAll("no");
                            DaisyUtils.getHistoryManager(HistoryFavoritrListActivity.this).deleteAll("yes");
                            mlists.clear();
                            adapter.notifyDataSetChanged();
                            clearAll.setVisibility(View.GONE);
                            if(pop!=null)
                                pop.dismiss();
                            ToastTip.showToast(HistoryFavoritrListActivity.this,"您已清空所有历史记录");
                        }
                    });
        }
    }
    private void emptyFavorite(){
        if(!IsmartvActivator.getInstance().isLogin()){
            DaisyUtils.getFavoriteManager(this).deleteAll("no");
            DaisyUtils.getFavoriteManager(this).deleteAll("yes");
            clearAll.setVisibility(View.GONE);
            mlists.clear();
            adapter.notifyDataSetChanged();
            if(pop!=null)
                pop.dismiss();
            ToastTip.showToast(this,"您已清空所有收藏记录");
        }else {
            removeSub=skyService.emptyBookmarks(IsmartvActivator.getInstance().getDeviceToken())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<ResponseBody>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            DaisyUtils.getFavoriteManager(HistoryFavoritrListActivity.this).deleteAll("no");
                            DaisyUtils.getFavoriteManager(HistoryFavoritrListActivity.this).deleteAll("yes");
                            mlists.clear();
                            adapter.notifyDataSetChanged();
                            clearAll.setVisibility(View.GONE);
                            if(pop!=null)
                                pop.dismiss();
                            ToastTip.showToast(HistoryFavoritrListActivity.this,"您已清空所有收藏记录");
                        }
                    });
        }
    }

    @Override
    public void onItemfocused(View view, int position, boolean hasFocus) {
        if(hasFocus) {
            JasmineUtil.scaleOut3(view);
            Log.i("ViewY", view.getY() + "");
            if (!ishover) {
                if (view.getY() > getResources().getDimensionPixelSize(R.dimen.history_640)) {
                    focusGridLayoutManager.setCanScroll(true);
                    recyclerView.smoothScrollBy(0, getResources().getDimensionPixelOffset(R.dimen.history_732));
                } else if (view.getY() < 0) {
                    focusGridLayoutManager.setCanScroll(true);
                    recyclerView.smoothScrollBy(0, -getResources().getDimensionPixelOffset(R.dimen.history_732));
                }
            }
        }else{
            JasmineUtil.scaleIn3(view);
            lastFocusView=view;
        }
    }
    //防止recyclerview焦点乱跑
    long mDownTime=0;
    long mUpTime=0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //长按滑动 滑动时焦点不会乱跳，但是每隔400毫秒滑动一次
        ishover=false;
        arrow_up.setFocusableInTouchMode(false);
        arrow_up.setFocusable(false);
        arrow_down.setFocusable(false);
        arrow_down.setFocusableInTouchMode(false);
        parent.setFocusable(false);
        parent.setFocusableInTouchMode(false);
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
    private View lastFocusView;
    boolean ishover=false;
    @Override
    public void OnItemOnhoverlistener(View v, MotionEvent event, int position, int recommend) {
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                ishover=true;
                focusGridLayoutManager.setCanScroll(false);
                v.requestFocusFromTouch();
                break;
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    v.setFocusable(true);
                    v.setFocusableInTouchMode(true);
                    v.requestFocusFromTouch();
                    break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        if(id==R.id.poster_arrow_up){
            focusGridLayoutManager.setCanScroll(true);
            recyclerView.smoothScrollBy(0, -getResources().getDimensionPixelOffset(R.dimen.history_732));
        }else if(id==R.id.poster_arrow_down){
            focusGridLayoutManager.setCanScroll(true);
            recyclerView.smoothScrollBy(0, getResources().getDimensionPixelOffset(R.dimen.history_732));
        }else if(id==R.id.clear_all){
            showPop();
        }
    }
}
