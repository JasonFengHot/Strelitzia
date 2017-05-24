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

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.models.PlayRecommend;
import tv.ismar.app.models.PlayfinishedRecommend;
import tv.ismar.library.injectdb.util.Log;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.subject.adapter.OnItemClickListener;
import tv.ismar.subject.adapter.OnItemFocusedListener;
import tv.ismar.subject.views.MyRecyclerView;


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

    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_finished);
        Intent intent = getIntent();
        itemId = intent.getIntExtra("item_id", 0);
        playScale = intent.getIntExtra("play_scale", 0);
        if("homepage".equals(baseChannel)) {
            channel = intent.getStringExtra("channel");
        }
        initView();
//            initData();
        PlayfinishedRecommend play = new PlayfinishedRecommend();
        play.setList(new ArrayList<PlayfinishedRecommend.RecommendItem>());
        for (int i = 0; i < 12; i++) {
            PlayfinishedRecommend.RecommendItem item = new PlayfinishedRecommend.RecommendItem();
            item.setTitle("标题标题标题标题标题标题标题标题标题" + i);
            item.setContent_model("teleplay");
            item.setPk(696674);
            play.getList().add(item);
        }
        processData(play.getList());
        play_finished_title.setText("您可能对以下影片感兴趣:");
        if (itemId == 0) {
            play_finished_confirm_btn.setVisibility(View.GONE);
        }
    }




    private void initView() {
                play_finished_title = (TextView) findViewById(R.id.play_finished_title);
                play_finished_horizontal_recylerview = (MyRecyclerView) findViewById(R.id.play_finished_horizontal_recylerview);
                play_finished_vertical_recylerview = (MyRecyclerView) findViewById(R.id.play_finished_vertical_recylerview);
                play_finished_confirm_btn = (Button) findViewById(R.id.play_finished_confirm_btn);
                play_finished_cancel_btn = (Button) findViewById(R.id.play_finished_cancel_btn);
                vertical_poster_focus = findViewById(R.id.vertical_poster_focus);
                horizontal_poster_focus = findViewById(R.id.horizontal_poster_focus);
                if("chinesemovie".equals(channel)||"overseas".equals(channel)||"movie".equals(channel)){
                    play_finished_vertical_recylerview.setVisibility(View.VISIBLE);
                    vertical_poster_focus.setVisibility(View.VISIBLE);
                    play_finished_vertical_recylerview.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL));
                    isVertical=true;
                }else{
                    play_finished_horizontal_recylerview.setVisibility(View.VISIBLE);
                    horizontal_poster_focus.setVisibility(View.VISIBLE);
                    play_finished_horizontal_recylerview.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL));
                    isVertical=false;
                }
                play_finished_confirm_btn.setOnClickListener(this);
                play_finished_cancel_btn.setOnClickListener(this);
                play_finished_confirm_btn.setOnFocusChangeListener(this);
                play_finished_cancel_btn.setOnFocusChangeListener(this);
            }

        private void initData() {
            if(itemId==0) {
                play_finished_confirm_btn.setVisibility(View.GONE);
                play_finished_title.setText("您可能对以下影片感兴趣:");
                mSkyService.apiPlayFinishedRecommend(channel,SimpleRestClient.sn_token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseObserver<PlayfinishedRecommend>() {

                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onNext(PlayfinishedRecommend playfinishedRecommend) {
                                processData(playfinishedRecommend.getList());
                            }
                        });
            }else {
                mSkyService.apiPlayExitRecommend(SimpleRestClient.sn_token, itemId, playScale)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseObserver<PlayRecommend>() {

                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onNext(PlayRecommend playRecommend) {
                                if(playRecommend!=null) {
                                    if (TextUtils.isEmpty(playRecommend.getRecommend_title()))
                                        play_finished_title.setText(playRecommend.getRecommend_title());
                                    processData(playRecommend.getRecommend_items());
                                }
                            }
                        });
            }
        }

    private void processData(final ArrayList<PlayfinishedRecommend.RecommendItem> list) {
        PlayFinishedAdapter playFinishedAdapter=new PlayFinishedAdapter(this,list,isVertical);
        playFinishedAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                PlayfinishedRecommend.RecommendItem item=list.get(position);
                String contentModel=item.getContent_model();
                if(contentModel!=null) {
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
                        JasmineUtil.scaleOut3(view);
                        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(getResources().getDimensionPixelOffset(R.dimen.play_finished_vertical_poster_focus_w), getResources().getDimensionPixelOffset(R.dimen.play_finished_vertical_poster_focus_h));
                        params.topMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_238);
                        if(position==0){
                            params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_337);
                        }else if(position==list.size()-1&&list.size()>2){
                            params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_1127);
                        }else{
                            params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_732);
                        }
                        vertical_poster_focus.setLayoutParams(params);
                        vertical_poster_focus.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.item_vertical_poster_title).setSelected(true);
                        focusedPosition=position;
                    }else{
                        JasmineUtil.scaleIn3(view);
                        view.findViewById(R.id.item_vertical_poster_title).setVisibility(View.GONE);
                        vertical_poster_focus.setVisibility(View.INVISIBLE);
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
                        JasmineUtil.scaleOut3(view);
                        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(getResources().getDimensionPixelOffset(R.dimen.play_finished_horizontal_poster_focus_w),getResources().getDimensionPixelOffset(R.dimen.play_finished_horizontal_poster_focus_h));
                        params.topMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_307);
                        if(position==0){
                            params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_147);
                        }else if(position==list.size()-1&&list.size()>2){
                            params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_1183);
                        }else{
                            params.leftMargin=getResources().getDimensionPixelOffset(R.dimen.scroll_665);
                        }
                        horizontal_poster_focus.setLayoutParams(params);
                        horizontal_poster_focus.setVisibility(View.VISIBLE);
                        focusedPosition=position;
                    }else{
                        JasmineUtil.scaleIn3(view);
                        horizontal_poster_focus.setVisibility(View.GONE);
                    }
                }
            }
        });
        if(isVertical){
            play_finished_vertical_recylerview.setAdapter(playFinishedAdapter);
        }else{
            play_finished_horizontal_recylerview.setAdapter(playFinishedAdapter);
        }
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
}