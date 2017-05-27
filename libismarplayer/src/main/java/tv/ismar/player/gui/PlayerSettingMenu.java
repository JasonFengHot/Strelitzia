package tv.ismar.player.gui;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.ArrayList;

import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.player.R;
import tv.ismar.player.adapter.EpisodeAdapter;
import tv.ismar.player.adapter.MenuListAdapter;
import tv.ismar.player.listener.EpisodeOnFocusListener;
import tv.ismar.player.listener.EpisodeOnKeyListener;
import tv.ismar.player.listener.EpisodeOnclickListener;
import tv.ismar.player.listener.MenuOnFocuslistener;
import tv.ismar.player.listener.MenuOnKeyListener;
import tv.ismar.player.listener.OnMenuListItmeClickListener;
import tv.ismar.player.model.QuailtyEntity;

/**
 * Created by liucan on 2017/5/23.
 */

public class PlayerSettingMenu extends PopupWindow implements EpisodeOnFocusListener,EpisodeOnKeyListener,MenuOnFocuslistener,MenuOnKeyListener{
    private Context mContext;
    private EpisodeAdapter adapter;
    private ItemEntity[] itemEntities;
    private ImageView arrow_left,arrow_right;
    private LinearLayout menu_layout;
    private RecyclerView list;
    private RecyclerView menu_list;
    private TextView menu_title,menu_select;
    private RelativeLayout wheel;
    private TextView player_episode,setting;
    private LinearLayout list_layout;
    private int pk;
    private ArrayList<QuailtyEntity> quailtyList=new ArrayList<>();
    private int currentQuailty=0;
    private EpisodeOnclickListener episodeOnclickListener;
    private OnMenuListItmeClickListener menuListener;
    public PlayerSettingMenu(Context context, ItemEntity[] entities, int subitem, EpisodeOnclickListener episodeOnclickListener1, ArrayList<QuailtyEntity> quailist,int position,OnMenuListItmeClickListener listener1){
        mContext=context;
        itemEntities=entities;
        pk=subitem;
        episodeOnclickListener=episodeOnclickListener1;
        quailtyList=quailist;
        currentQuailty=position;
        menuListener=listener1;

        int width=context.getResources().getDimensionPixelOffset(R.dimen.player_1920);
        int height=context.getResources().getDimensionPixelSize(R.dimen.player_350);

        setWidth(width);
        setHeight(height);

        View contentView = LayoutInflater.from(context).inflate(R.layout.player_setting_menu, null);
        arrow_left= (ImageView) contentView.findViewById(R.id.arrow_left);
        arrow_right= (ImageView) contentView.findViewById(R.id.arrow_right);
        player_episode= (TextView) contentView.findViewById(R.id.player_episode);
        setting= (TextView) contentView.findViewById(R.id.player_setting);
        menu_layout= (LinearLayout) contentView.findViewById(R.id.menu_layout);
        list= (RecyclerView) contentView.findViewById(R.id.episode_list);
        list_layout= (LinearLayout) contentView.findViewById(R.id.list_layout);
        menu_layout.setVisibility(View.GONE);
        addmenu();
        if(itemEntities!=null){
            showEpisode();
        }else {
            hideEpisode();
        }


        RelativeLayout relativeLayout = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams layoutParams;
        layoutParams = new RelativeLayout.LayoutParams(width, height);
        relativeLayout.addView(contentView, layoutParams);

        setContentView(relativeLayout);

        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.transparent));
        setFocusable(true);
    }
    private void showEpisode(){
        list.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
        adapter=new EpisodeAdapter(mContext,pk,itemEntities);
        adapter.setEpisodeOnclickListener(episodeOnclickListener);
        adapter.setOnFocusListener(this);
        adapter.setOnKeyListener(this);
        list.setAdapter(adapter);
        int position=getPlayingPosition(pk,itemEntities);
        list.smoothScrollToPosition(position);
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState==0){
                    int child=list.getChildCount();
                    int index=list.getChildLayoutPosition(list.getChildAt(0));
                    if(index!=0){
                        arrow_left.setVisibility(View.VISIBLE);
                    }else{
                        arrow_left.setVisibility(View.INVISIBLE);
                    }
                    if(index+7!=itemEntities.length){
                        arrow_right.setVisibility(View.VISIBLE);
                    }else{
                        arrow_right.setVisibility(View.INVISIBLE);
                    }
                    list.getChildAt(0).requestFocus();
                }
            }
        });
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                list.getChildAt(0).requestFocus();
            }
        },1000);
        arrow_left.setVisibility(View.INVISIBLE);
        if(itemEntities.length>7){
            arrow_right.setVisibility(View.VISIBLE);
        }else{
            arrow_right.setVisibility(View.INVISIBLE);
        }
        setArrowListener();
    }
    private int getPlayingPosition(int pk,ItemEntity[] list){
        int position=0;
        for(int i=0;i<list.length;i++){
            if(pk==list[i].getPk()){
                position=i;
            }
        }
        return position;
    }

    private void addmenu() {
        View menuView=LayoutInflater.from(mContext).inflate(R.layout.setting_menu_item,null);
        wheel= (RelativeLayout) menuView.findViewById(R.id.wheel);
        menu_title= (TextView) menuView.findViewById(R.id.menu_title);
        menu_select= (TextView) menuView.findViewById(R.id.menu_select);
        menu_list= (RecyclerView) menuView.findViewById(R.id.menu_list);
        menu_list.setLayoutManager(new LinearLayoutManager(mContext));
        MenuListAdapter listAdapter=new MenuListAdapter(mContext,quailtyList);
        listAdapter.setMenuOnFocuslistener(this);
        listAdapter.setOnMenuListItmeClickListener(menuListener);
        listAdapter.setOnKeyListener(this);
        menu_list.setAdapter(listAdapter);
        menu_select.setText(quailtyList.get(currentQuailty).getName());
        menu_select.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    menu_select.setTextColor(mContext.getResources().getColor(R.color._ff9c3c));
                }else{
                    menu_select.setTextColor(mContext.getResources().getColor(R.color._f0f0f0));
                }
            }
        });
        menu_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWheel();
            }
        });
        menu_select.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i("menu_select","keycode: "+keyCode);
                if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==20){
                    if(itemEntities!=null){
                        hideMenu();
                    }
                }
                return false;
            }
        });
        menu_layout.addView(menuView);
    }
    private void hideEpisode(){
        list_layout.setVisibility(View.GONE);
        player_episode.setVisibility(View.GONE);
        menu_layout.setVisibility(View.VISIBLE);
        menu_select.setVisibility(View.VISIBLE);
    }
    private void hideMenu(){
        menu_layout.setVisibility(View.GONE);
        list_layout.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(mContext.getResources().getDimensionPixelSize(R.dimen.player_77),mContext.getResources().getDimensionPixelSize(R.dimen.player_52));
        lp.setMargins(mContext.getResources().getDimensionPixelSize(R.dimen.player_51),mContext.getResources().getDimensionPixelSize(R.dimen.player_38),0,0);
        lp.addRule(RelativeLayout.BELOW,R.id.player_setting);
        player_episode.setLayoutParams(lp);
        list.getChildAt(0).requestFocus();
    }
    private void setArrowListener() {

        arrow_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int child=list.getChildCount();
                int index=list.getChildLayoutPosition(list.getChildAt(child-1));
                if(itemEntities.length-index>=7) {
                    list.smoothScrollToPosition(index + 7);
                }else{
                    list.smoothScrollToPosition(itemEntities.length-1);
                    arrow_right.setVisibility(View.INVISIBLE);
                }
                arrow_left.setVisibility(View.VISIBLE);
            }
        });
        arrow_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int child=list.getChildCount();
                int index=list.getChildLayoutPosition(list.getChildAt(0));
                if(index-7>=0) {
                    list.smoothScrollToPosition(index - 7);
                }else{
                    list.smoothScrollToPosition(0);
                    arrow_left.setVisibility(View.INVISIBLE);
                }
                arrow_right.setVisibility(View.VISIBLE);
            }
        });
    }
    @Override
    public void onItemFocus(View view, boolean hasfocus,int pos) {
        if(hasfocus){
            int child=list.getChildCount();
            int index=list.getChildLayoutPosition(list.getChildAt(child-1));
            Log.i("SettingMenu","position: "+index);
        }
    }

    @Override
    public void onKeyListener(int keycode, int pos) {
        Log.i("SettingMenu","onkey"+keycode);
        if(keycode==22){
            int child=list.getChildCount();
            int index=list.getChildLayoutPosition(list.getChildAt(child-1));
            Log.i("OnkeyScrollRight","index="+index+"   pos="+pos);
            if(index==pos&&pos!=itemEntities.length-1){
              //   list.smoothScrollBy(mContext.getResources().getDimensionPixelOffset(R.dimen.player_270),0);
                list.smoothScrollToPosition(pos+1);
            }
        }else if(keycode==21){
            int index=list.getChildLayoutPosition(list.getChildAt(0));
            Log.i("OnkeyScrollLeft","index="+index+"   pos="+pos);
            if(pos==index&&pos!=0){
            //    list.smoothScrollBy(-mContext.getResources().getDimensionPixelOffset(R.dimen.player_270),0);
                list.smoothScrollToPosition(pos-1);
            }
        }else if(keycode==19){
            list_layout.setVisibility(View.GONE);
            menu_layout.setVisibility(View.VISIBLE);
            menu_select.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(mContext.getResources().getDimensionPixelSize(R.dimen.player_77),mContext.getResources().getDimensionPixelSize(R.dimen.player_52));
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
            lp.setMargins(mContext.getResources().getDimensionPixelSize(R.dimen.player_51),0,0,mContext.getResources().getDimensionPixelSize(R.dimen.player_41));
            player_episode.setLayoutParams(lp);
        }
    }

    @Override
    public void onFocus(View v, int pos, boolean hasfocus) {
        if(hasfocus) {
            if (pos != 0)
                menu_list.smoothScrollBy(0, (int) (v.getY() - v.getHeight()));
        }
    }

    @Override
    public void onkeyBack(int keycode) {
        Log.i("logBack",keycode+"");
        hideWheel();
    }
    private void hideWheel(){
        wheel.setVisibility(View.GONE);
        menu_select.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(mContext.getResources().getDimensionPixelSize(R.dimen.player_77),mContext.getResources().getDimensionPixelSize(R.dimen.player_52));
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);
        lp.setMargins(mContext.getResources().getDimensionPixelSize(R.dimen.player_51),mContext.getResources().getDimensionPixelSize(R.dimen.player_54),0,0);
        setting.setLayoutParams(lp);
        menu_select.requestFocus();
    }
    private void showWheel(){
        wheel.setVisibility(View.VISIBLE);
        menu_select.setVisibility(View.GONE);
        RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(mContext.getResources().getDimensionPixelSize(R.dimen.player_77),mContext.getResources().getDimensionPixelSize(R.dimen.player_52));
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);
        lp.setMargins(mContext.getResources().getDimensionPixelSize(R.dimen.player_51),mContext.getResources().getDimensionPixelSize(R.dimen.player_14),0,0);
        setting.setLayoutParams(lp);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(currentQuailty<=2){
                    menu_list.getChildAt(currentQuailty).requestFocus();
                }else{
                    menu_list.scrollToPosition(currentQuailty);
                    menu_list.getChildAt(1).requestFocus();
                }
            }
        },500);
    }
}
