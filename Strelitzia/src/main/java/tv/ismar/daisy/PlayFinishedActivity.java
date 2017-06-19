package tv.ismar.daisy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.entity.Item;
import tv.ismar.app.models.PlayRecommend;
import tv.ismar.app.models.PlayfinishedRecommend;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.ui.adapter.OnItemClickListener;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.app.widget.MyRecyclerView;
import tv.ismar.searchpage.utils.JasmineUtil;


public class PlayFinishedActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private TextView play_finished_title;
    private MyRecyclerView play_finished_horizontal_recylerview;
    private MyRecyclerView play_finished_vertical_recylerview;
    private Button play_finished_confirm_btn;
    private Button play_finished_cancel_btn;
    private int itemId;
    private int playScale;
    private boolean isVertical;
    private String channel;
    private View vertical_poster_focus;
    private View horizontal_poster_focus;
    private static final int CONTINUE_PLAY=100;
    private static final int EXIT_PLAY=200;
    private int  focusedPosition=0;
    private boolean leftFocus=false;
    private boolean hasHistory;
    private PlayFinishedAdapter playFinishedAdapter;
    private Subscription playExitSub;
    private boolean isFirstIn=true;
    private View play_exit_error;


    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_finished);
        Intent intent = getIntent();
        itemId = intent.getIntExtra("item_id", 0);
        playScale = intent.getIntExtra("play_scale", 1);
        hasHistory = intent.getBooleanExtra("has_history", false);
        channel = intent.getStringExtra("channel");
        initView();
        initData();
        play_finished_cancel_btn.requestFocus();
    }


    private void initView() {
                play_finished_title = (TextView) findViewById(R.id.play_finished_title);
                play_finished_horizontal_recylerview = (MyRecyclerView) findViewById(R.id.play_finished_horizontal_recylerview);
                play_finished_vertical_recylerview = (MyRecyclerView) findViewById(R.id.play_finished_vertical_recylerview);
                play_finished_confirm_btn = (Button) findViewById(R.id.play_finished_confirm_btn);
                play_finished_cancel_btn = (Button) findViewById(R.id.play_finished_cancel_btn);
                vertical_poster_focus = findViewById(R.id.vertical_poster_focus);
                horizontal_poster_focus = findViewById(R.id.horizontal_poster_focus);
                play_exit_error = findViewById(R.id.play_exit_error);
                play_finished_confirm_btn.setOnClickListener(this);
                play_finished_cancel_btn.setOnClickListener(this);
                play_finished_confirm_btn.setOnFocusChangeListener(this);
                play_finished_cancel_btn.setOnFocusChangeListener(this);
            }

        private void initData() {
            if(playScale==100) {
                //播放完成页
                play_finished_confirm_btn.setVisibility(View.GONE);
                play_finished_cancel_btn.setNextFocusLeftId(R.id.play_finished_cancel_btn);
                if("movie".equals(channel)){
                    setOrientation(true);
                }else{
                    setOrientation(false);
                }
                play_finished_title.setText("您可能对以下影片感兴趣:");
                mSkyService.getRelatedArray(itemId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer <Item[]>() {

                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                play_exit_error.setVisibility(View.VISIBLE);
                                play_finished_cancel_btn.setNextFocusUpId(R.id.play_finished_cancel_btn);
                                play_finished_confirm_btn.setNextFocusUpId(R.id.play_finished_confirm_btn);
                            }

                            @Override
                            public void onNext(Item[] playfinishedRecommend) {
                                ArrayList<PlayfinishedRecommend.RecommendItem> list=new ArrayList<>();
                                for (int i = 0; i <playfinishedRecommend.length ; i++) {
                                    PlayfinishedRecommend.RecommendItem item=new PlayfinishedRecommend.RecommendItem();
                                    item.setPk(playfinishedRecommend[i].pk);
                                    item.setContent_model(playfinishedRecommend[i].content_model);
                                    item.setTitle(playfinishedRecommend[i].title);
                                    item.setPoster_url(playfinishedRecommend[i].poster_url);
                                    item.setVertical_url(playfinishedRecommend[i].vertical_url);
                                    list.add(item);
                                }
                                processData(list);
                            }
                        });
            }else {
                if(playScale<20){
                    setOrientation(false);
                    play_finished_title.setText("今日热播内容推荐");
                }else{
                    if("movie".equals(channel)){
                        setOrientation(true);
                    }else{
                        setOrientation(false);
                    }
                    if(hasHistory){
                        play_finished_title.setText("看过此片的用户还看过");
                    }else{
                        play_finished_title.setText("其他用户正在观看");
                    }

                }
                playExitSub = SkyService.ServiceManager.getCacheSkyService2().apiPlayExitRecommend(SimpleRestClient.sn_token, itemId,channel,playScale)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<PlayRecommend>() {

                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                play_exit_error.setVisibility(View.VISIBLE);
                                play_finished_cancel_btn.setNextFocusUpId(R.id.play_finished_cancel_btn);
                                play_finished_confirm_btn.setNextFocusUpId(R.id.play_finished_confirm_btn);
                            }

                            @Override
                            public void onNext(PlayRecommend playRecommend) {
                                if (playRecommend != null) {
                                    if (TextUtils.isEmpty(playRecommend.getRecommend_title()))
                                        play_finished_title.setText(playRecommend.getRecommend_title());
                                    processData(playRecommend.getRecommend_items());
                                }
                            }
                        });
            }
        }

    private void setOrientation(boolean Vertical) {
        if(Vertical){
            play_finished_vertical_recylerview.setVisibility(View.VISIBLE);
            play_finished_vertical_recylerview.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL));
            isVertical=true;
        }else{
            play_finished_horizontal_recylerview.setVisibility(View.VISIBLE);
            play_finished_horizontal_recylerview.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL));
            isVertical=false;
        }
    }

    private void processData(final ArrayList<PlayfinishedRecommend.RecommendItem> list) {
        playFinishedAdapter = new PlayFinishedAdapter(this,list,isVertical);
        if(isVertical){
            play_finished_vertical_recylerview.setAdapter(playFinishedAdapter);
        }else{
            play_finished_horizontal_recylerview.setAdapter(playFinishedAdapter);
        }
        playFinishedAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                PlayfinishedRecommend.RecommendItem item=list.get(position);
                String contentModel=item.getContent_model();
                if(contentModel!=null) {
                    Intent intent=new Intent("tv.ismar.daisy.closeplayer");
                    sendBroadcast(intent);
                    finish();
                    PageIntent pageIntent = new PageIntent();
                    if (contentModel.equals("music") || (contentModel.equals("sport") && item.getExpense_info() == null) || contentModel.equals("game")) {
                        pageIntent.toPlayPage(PlayFinishedActivity.this, item.getPk(), 0, Source.RELATED);
                    } else {
                        pageIntent.toDetailPage(PlayFinishedActivity.this, Source.RELATED.getValue(), item.getPk());
                    }
                }
            }
        });
        playFinishedAdapter.setItemFocusedListener(new OnItemFocusedListener() {
            @Override
            public void onItemfocused(View view, int position, boolean hasFocus) {
                if(isVertical){
                    if(hasFocus) {
                        if(leftFocus){
                            leftFocus=false;
                            if(focusedPosition==0&&play_finished_vertical_recylerview.getChildAt(0)!=null){
                                play_finished_vertical_recylerview.getChildAt(0).requestFocus();
                                return;
                            }else if(focusedPosition==list.size()-1&&play_finished_vertical_recylerview.getChildAt(3)!=null){
                                play_finished_vertical_recylerview.getChildAt(3).requestFocus();
                                return;
                            }
                        }
                        view.findViewById(R.id.item_vertical_poster_title).setVisibility(View.VISIBLE);
                        if(position==0||(position==1&&view.getX()-getResources().getDimensionPixelOffset(R.dimen.scroll_395)>0)||position==list.size()-1) {
                            play_finished_vertical_recylerview.smoothScrollBy(0, 0);
                        }else{
                            play_finished_vertical_recylerview.smoothScrollBy((int) view.getX() - getResources().getDimensionPixelOffset(R.dimen.scroll_396), 0);
                        }
                        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(getResources().getDimensionPixelOffset(R.dimen.play_finished_vertical_poster_focus_w), getResources().getDimensionPixelOffset(R.dimen.play_finished_vertical_poster_focus_h));
                        params.topMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_238);
                        if(position==0){
                            params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_337);
                        }else if(position==list.size()-1&&list.size()>2){
                            params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_1127);
                        }else{
                            params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_732);
                        }
                        if(isFirstIn) {
                            isFirstIn = false;
                        }else{
                            vertical_poster_focus.setLayoutParams(params);
                        }
                        vertical_poster_focus.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.item_vertical_poster_title).setSelected(true);
                        focusedPosition=position;
                        JasmineUtil.scaleOut3(view);
                    }else{
                        JasmineUtil.scaleIn3(view);
                        view.findViewById(R.id.item_vertical_poster_title).setVisibility(View.GONE);
                        vertical_poster_focus.setVisibility(View.GONE);
                        view.findViewById(R.id.item_vertical_poster_title).setSelected(false);
                    }
                }else{
                    if(hasFocus){
                        if(leftFocus){
                            leftFocus=false;
                            if(focusedPosition==0&&play_finished_horizontal_recylerview.getChildAt(0)!=null){
                                play_finished_horizontal_recylerview.getChildAt(0).requestFocus();
                                return;
                            }else if(focusedPosition==list.size()-1&&play_finished_horizontal_recylerview.getChildAt(3)!=null){
                                play_finished_horizontal_recylerview.getChildAt(3).requestFocus();
                                return;
                            }
                        }
                        if(position==0||(position==1&&view.getX()-getResources().getDimensionPixelOffset(R.dimen.scroll_519)>0)||position==list.size()-1) {
                            play_finished_horizontal_recylerview.smoothScrollBy(0, 0);
                        }else{
                            play_finished_horizontal_recylerview.smoothScrollBy((int) view.getX() - getResources().getDimensionPixelOffset(R.dimen.scroll_519), 0);
                        }
                        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(getResources().getDimensionPixelOffset(R.dimen.play_finished_horizontal_poster_focus_w),getResources().getDimensionPixelOffset(R.dimen.play_finished_horizontal_poster_focus_h));
                        params.topMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_307);
                        if(position==0){
                            params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_147);
                        }else if(position==list.size()-1&&list.size()>2){
                            params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_1183);
                        }else{
                            params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_665);
                        }
                        if(isFirstIn) {
                            isFirstIn = false;
                        }else{
                            horizontal_poster_focus.setLayoutParams(params);
                        }
                        horizontal_poster_focus.setVisibility(View.VISIBLE);
                        focusedPosition=position;
                        JasmineUtil.scaleOut3(view);
                    }else{
                        JasmineUtil.scaleIn3(view);
                        horizontal_poster_focus.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play_finished_confirm_btn:
                setResult(CONTINUE_PLAY);
                finish();
                break;
            case R.id.play_finished_cancel_btn:
                setResult(EXIT_PLAY);
                finish();
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus) {
            leftFocus = true;
        }
    }

    @Override
    public void onBackPressed() {
        if(playScale==100) {
            setResult(EXIT_PLAY);
        }else{
            setResult(CONTINUE_PLAY);
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playExitSub != null && playExitSub.isUnsubscribed()) {
            playExitSub.unsubscribe();
        }
    }
}