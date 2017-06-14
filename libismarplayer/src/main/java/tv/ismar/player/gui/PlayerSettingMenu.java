package tv.ismar.player.gui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.transition.Slide;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
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
    private LinearLayout list_layout,episode_layout,setting_layout;
    private ImageView top_shape,bottom_shape;
    private int pk;
    private ArrayList<QuailtyEntity> quailtyList=new ArrayList<>();
    private int currentQuailty=0;
    private EpisodeOnclickListener episodeOnclickListener;
    private OnMenuListItmeClickListener menuListener;
    private MenuHandler menuHandler;
    private Animation episode_hide;
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
        episode_layout= (LinearLayout) contentView.findViewById(R.id.episode_layout);
        setting_layout= (LinearLayout) contentView.findViewById(R.id.setting_layout);
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

     //   setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.transparent));
        setFocusable(true);
        menuHandler=new MenuHandler();
        episode_hide= AnimationUtils.loadAnimation(mContext,R.anim.episode_hide);
        
    }
    private void showEpisode(){
        list.setOnItemActionListener(this);
        list.addDatas(itemEntities);
        setting.setTextColor(mContext.getResources().getColor(R.color._666666));
        if(itemEntities.size()>7){
            arrow_right.setVisibility(View.VISIBLE);
        }else{
            arrow_right.setVisibility(View.INVISIBLE);
        }
        int index=0;
        for(int i=0;i<itemEntities.size();i++){
            if(pk==itemEntities.get(i).getPk()){
                index=i;
            }
        }
        if(index<=6){
            list.getChildViewAt(index).requestFocus();
            arrow_left.setVisibility(View.INVISIBLE);
        }else{
            list.toPlayingItem(index);
            arrow_left.setVisibility(View.VISIBLE);
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
        top_shape= (ImageView) menuView.findViewById(R.id.top_shape);
        bottom_shape= (ImageView) menuView.findViewById(R.id.bottom_shape);
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
                reSendMsg();
            }
        });
        menu_select.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i("menu_select","Select_keycode: "+keyCode);
                if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==20){
                    if(itemEntities!=null&&itemEntities.size()>1){
                        hideMenu();
                        reSendMsg();
                    }
                }else if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==4){
                    dismiss();
                    menuHandler.removeMessages(1);
                    return true;
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
        setting.setTextColor(mContext.getResources().getColor(R.color._f0f0f0));

    }
    private void hideMenu(){
        ObjectAnimator episode_show=ObjectAnimator.ofFloat(episode_layout,"translationY",mContext.getResources().getDimensionPixelSize(R.dimen.player_113),0);
        episode_show.setDuration(300);
        episode_show.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                player_episode.setTextColor(mContext.getResources().getColor(R.color._f0f0f0));
                setting.setTextColor(mContext.getResources().getColor(R.color._666666));
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        menu_layout.setVisibility(View.GONE);
        list_layout.setVisibility(View.VISIBLE);
        episode_show.start();
//        RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(mContext.getResources().getDimensionPixelSize(R.dimen.player_77),mContext.getResources().getDimensionPixelSize(R.dimen.player_52));
//        lp.setMargins(mContext.getResources().getDimensionPixelSize(R.dimen.player_51),mContext.getResources().getDimensionPixelSize(R.dimen.player_38),0,0);
//        lp.addRule(RelativeLayout.BELOW,R.id.player_setting);
//        player_episode.setLayoutParams(lp);

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
        arrow_left.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==22){
                    arrow_left.setHovered(false);
                }
                return false;
            }
        });
        arrow_right.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==21){
                    arrow_right.setHovered(false);
                }
                return false;
            }
        });
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
    View view;
    @Override
    public void onFocus(View v, int pos, boolean hasfocus) {
        if(hasfocus) {
             if(pos==0){
                RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(mContext.getResources().getDimensionPixelSize(R.dimen.player_250),mContext.getResources().getDimensionPixelSize(R.dimen.player_146));
                lp.setMargins(0,mContext.getResources().getDimensionPixelSize(R.dimen.player_43),0,0);
                menu_list.setLayoutParams(lp);
                top_shape.setVisibility(View.GONE);
                if(quailtyList.size()>1) {
                    bottom_shape.setVisibility(View.VISIBLE);
                }else{
                    bottom_shape.setVisibility(View.GONE);
                }
            }else if(pos==quailtyList.size()-1){
                RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(mContext.getResources().getDimensionPixelSize(R.dimen.player_250),mContext.getResources().getDimensionPixelSize(R.dimen.player_146));
                lp.setMargins(0,0,0,mContext.getResources().getDimensionPixelSize(R.dimen.player_43));
                menu_list.setLayoutParams(lp);
                bottom_shape.setVisibility(View.GONE);
                if (quailtyList.size()>1){
                    top_shape.setVisibility(View.VISIBLE);
                }else{
                    top_shape.setVisibility(View.GONE);
                }
            }else{
                 RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(mContext.getResources().getDimensionPixelSize(R.dimen.player_250),mContext.getResources().getDimensionPixelSize(R.dimen.player_146));
                 lp.setMargins(0,0,0,0);
                 menu_list.setLayoutParams(lp);
                 menu_list.smoothScrollBy(0, (int) (v.getY() - v.getHeight()));
                 top_shape.setVisibility(View.VISIBLE);
                 bottom_shape.setVisibility(View.VISIBLE);
             }
            reSendMsg();
        }else{
            view=v;
        }
    }

    @Override
    public void onkeyBack(int keycode) {
        Log.i("menu_select","WheelkeyBack");
        hideWheel();
    }
    private void hideWheel(){
        ObjectAnimator show_wheel=ObjectAnimator.ofFloat(setting_layout,"translationY",-mContext.getResources().getDimensionPixelSize(R.dimen.player_27),0);
        show_wheel.setDuration(300);
        show_wheel.start();
        wheel.setVisibility(View.GONE);
        menu_select.setVisibility(View.VISIBLE);
        menu_select.requestFocus();
    }
    private void showWheel(){
        ObjectAnimator show_wheel=ObjectAnimator.ofFloat(setting_layout,"translationY",0,-mContext.getResources().getDimensionPixelSize(R.dimen.player_27));
        show_wheel.setDuration(300);
        show_wheel.start();
        wheel.setVisibility(View.VISIBLE);
        menu_select.setVisibility(View.GONE);
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
        },50);
    }

    @Override
    public void onItemClick(View view, int position) {
        int index=list.getCurrentDataSelectPosition();
        episodeOnclickListener.onItemClick(itemEntities.get(index).getPk());
        dismiss();
        menuHandler.removeMessages(1);
    }

    @Override
    public void onItemFocusChanged(View view, boolean focused, int position) {
        if(focused){
            arrow_right.setFocusable(false);
            arrow_left.setFocusable(false);
            if (list.getFirstVisibleChildIndex()+7>=itemEntities.size()){
                arrow_right.setVisibility(View.INVISIBLE);
            }
            reSendMsg();
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
        String Title = object.getTitle();
        String subItemTitle=object.getSubtitle();
        if(subItemTitle!=null&&!subItemTitle.equals("")){
            holder.textView.setVisibility(View.GONE);
            holder.subitem.setVisibility(View.VISIBLE);
            holder.subitem.setText("第"+subItemTitle.substring(4));
        }else {
            if (Title.contains("第")) {
                int ind = Title.indexOf("第");
                Title = Title.substring(ind);
            }
            holder.textView.setVisibility(View.VISIBLE);
            holder.subitem.setVisibility(View.GONE);
            holder.textView.setText(Title);
        }
        if(pk==object.getPk()){
            holder.imageView.setVisibility(View.VISIBLE);
        }else{
            holder.imageView.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onKeyDown(View view, int keyCode, KeyEvent event) {
        if(keyCode==19){
            showQuality();
        }else if(keyCode==4){
            menuHandler.removeMessages(1);
            dismiss();
        }
    }
    public void showQuality(){
        ObjectAnimator episode_hide=ObjectAnimator.ofFloat(episode_layout,"translationY",0,mContext.getResources().getDimensionPixelSize(R.dimen.player_113));
        episode_hide.setDuration(300);
        episode_hide.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                menu_layout.setVisibility(View.VISIBLE);
                menu_select.setVisibility(View.VISIBLE);
                list_layout.setVisibility(View.GONE);
                player_episode.setTextColor(mContext.getResources().getColor(R.color._666666));
                setting.setTextColor(mContext.getResources().getColor(R.color._f0f0f0));
                menu_select.requestFocusFromTouch();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        episode_hide.start();
        reSendMsg();
    }
    private class MenuHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1){
                dismiss();
            }
            super.handleMessage(msg);
        }
    }
    public void sendMsg(){
        if(menuHandler!=null) {
            menuHandler.sendEmptyMessageDelayed(1, 10 * 1000);
        }
    }
    private void reSendMsg(){
        if(menuHandler!=null) {
            menuHandler.removeMessages(1);
            menuHandler.sendEmptyMessageDelayed(1, 10 * 1000);
        }
    }
}
