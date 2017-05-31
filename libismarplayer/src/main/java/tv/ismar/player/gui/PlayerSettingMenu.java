package tv.ismar.player.gui;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

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
import tv.ismar.player.model.EpisdoHolder;
import tv.ismar.player.model.QuailtyEntity;
import tv.ismar.player.widget.HorizontalEpisodeList;

/**
 * Created by liucan on 2017/5/23.
 */

public class PlayerSettingMenu extends PopupWindow implements HorizontalEpisodeList.OnItemActionListener,MenuOnFocuslistener,MenuOnKeyListener{
    private Context mContext;
    private EpisodeAdapter adapter;
    private List<ItemEntity> itemEntities;
    private ImageView arrow_left,arrow_right;
    private LinearLayout menu_layout;
    private HorizontalEpisodeList list;
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
    public PlayerSettingMenu(Context context, List<ItemEntity> entities, int subitem, EpisodeOnclickListener episodeOnclickListener1, ArrayList<QuailtyEntity> quailist,int position,OnMenuListItmeClickListener listener1){
        mContext=context;
        pk=subitem;
        episodeOnclickListener=episodeOnclickListener1;
        quailtyList=quailist;
        currentQuailty=position;
        menuListener=listener1;
        itemEntities=entities;

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
        list= (HorizontalEpisodeList) contentView.findViewById(R.id.episode_list);
        list_layout= (LinearLayout) contentView.findViewById(R.id.list_layout);
        menu_layout.setVisibility(View.GONE);
        addmenu();
        if(itemEntities!=null&&itemEntities.size()>1){
            showEpisode();
        }else {
            hideEpisode();
        }
        RelativeLayout relativeLayout = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams layoutParams;
        layoutParams = new RelativeLayout.LayoutParams(width, height);
        relativeLayout.addView(contentView, layoutParams);
        setContentView(relativeLayout);

    //    setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.transparent));
        setFocusable(true);

    }
    private void showEpisode(){
        list.setOnItemActionListener(this);
        list.addDatas(itemEntities);
        int index=0;
        for(int i=0;i<itemEntities.size();i++){
            if(pk==itemEntities.get(i).getPk()){
                index=i+1;
            }
        }
        if(index<=6){
//            list.pageArrowDown();
//            list.pageArrowUp();
        }else{
            list.toPlayingItem(index);
        }
        arrow_left.setVisibility(View.INVISIBLE);
        if(itemEntities.size()>7){
            arrow_right.setVisibility(View.VISIBLE);
        }else{
            arrow_right.setVisibility(View.INVISIBLE);
        }
        setArrowListener();
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
        menu_select.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
                    v.requestFocusFromTouch();
                }
                return false;
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
                    if(itemEntities!=null&&itemEntities.size()>1){
                        hideMenu();
                    }
                }else if(keyCode==4&&v.getVisibility()==View.VISIBLE){
                    Log.i("menu_select",keyCode+v.getVisibility()+"");
                    dismiss();
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
                list.pageArrowDown();
                v.requestFocusFromTouch();
            }
        });
        arrow_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.pageArrowUp();
                v.requestFocusFromTouch();
            }
        });
        arrow_left.setOnHoverListener(arrowHover);
        arrow_right.setOnHoverListener(arrowHover);
    }
    View.OnHoverListener arrowHover=new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    v.setFocusable(true);
                    v.requestFocus();
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    break;
            }
            return false;
        }
    };

    @Override
    public void onFocus(View v, int pos, boolean hasfocus) {
        if(hasfocus) {
            if (pos != 0)
                menu_list.smoothScrollBy(0, (int) (v.getY() - v.getHeight()));
        }
    }

    @Override
    public void onkeyBack(int keycode) {
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
                    menu_list.getChildAt(currentQuailty).requestFocusFromTouch();
                }else{
                    menu_list.scrollToPosition(currentQuailty);
                    menu_list.getChildAt(1).requestFocusFromTouch();
                }
            }
        },500);
    }

    @Override
    public void onItemClick(View view, int position) {
        int index=list.getCurrentDataSelectPosition();
        episodeOnclickListener.onItemClick(itemEntities.get(index).getPk());
        dismiss();
    }

    @Override
    public void onItemFocusChanged(View view, boolean focused, int position) {
        if(focused){
            arrow_right.setFocusable(false);
            arrow_left.setFocusable(false);
        }
    }

    @Override
    public void onItemHovered(View view, MotionEvent event, Object object, int position) {
        if(event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
            view.requestFocusFromTouch();
        }
    }

    @Override
    public void onBindView(View itemView, ItemEntity object, int position) {
        EpisdoHolder holder=new EpisdoHolder(itemView);
        setUIData(holder,object);
        if(list.getFirstVisibleChildIndex()==0){
            if(list.getLastVisibleChildIndex()==itemEntities.size()-1){
                arrow_right.setVisibility(View.INVISIBLE);
            }else{
                arrow_right.setVisibility(View.VISIBLE);
            }
            arrow_left.setVisibility(View.INVISIBLE);
        }else if(list.getLastVisibleChildIndex()==itemEntities.size()-1){
            arrow_left.setVisibility(View.VISIBLE);
            arrow_right.setVisibility(View.INVISIBLE);
        }else{
            arrow_left.setVisibility(View.VISIBLE);
            arrow_right.setVisibility(View.VISIBLE);
        }
    }

    private void setUIData(EpisdoHolder holder,ItemEntity object){
        String subItemTitle = object.getTitle();
        if (subItemTitle.contains("第")) {
            int ind = subItemTitle.indexOf("第");
            subItemTitle = subItemTitle.substring(ind);
        }
        holder.textView.setText(subItemTitle);
        if(pk==object.getPk()){
            holder.imageView.setVisibility(View.VISIBLE);
        }else{
            holder.imageView.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onKeyDown(View view, int keyCode, KeyEvent event) {
        if(keyCode==19){
            list_layout.setVisibility(View.GONE);
            menu_layout.setVisibility(View.VISIBLE);
            menu_select.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(mContext.getResources().getDimensionPixelSize(R.dimen.player_77),mContext.getResources().getDimensionPixelSize(R.dimen.player_52));
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
            lp.setMargins(mContext.getResources().getDimensionPixelSize(R.dimen.player_51),0,0,mContext.getResources().getDimensionPixelSize(R.dimen.player_41));
            player_episode.setLayoutParams(lp);
            menu_select.requestFocusFromTouch();
        }else if(keyCode==4){
            dismiss();
        }
    }
}
