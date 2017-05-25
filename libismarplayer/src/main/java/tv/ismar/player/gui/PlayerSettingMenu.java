package tv.ismar.player.gui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;


import java.util.ArrayList;

import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.player.R;
import tv.ismar.player.listener.EpisodeOnFocusListener;
import tv.ismar.player.listener.EpisodeOnKeyListener;
import tv.ismar.player.listener.EpisodeOnclickListener;

/**
 * Created by liucan on 2017/5/23.
 */

public class PlayerSettingMenu extends PopupWindow implements EpisodeOnFocusListener,EpisodeOnKeyListener{
    private Context mContext;
    private EpisodeAdapter adapter;
    private ArrayList<ItemEntity> itemEntities;
    private ImageView arrow_left,arrow_right;
    private RecyclerView list;
    public PlayerSettingMenu(Context context, ArrayList<ItemEntity> entities, int subitem, EpisodeOnclickListener episodeOnclickListener){
        mContext=context;
        itemEntities=entities;
        int width=context.getResources().getDimensionPixelOffset(R.dimen.player_1920);
        int height=context.getResources().getDimensionPixelSize(R.dimen.player_350);

        setWidth(width);
        setHeight(height);

        View contentView = LayoutInflater.from(context).inflate(R.layout.player_setting_menu, null);
        arrow_left= (ImageView) contentView.findViewById(R.id.arrow_left);
        arrow_right= (ImageView) contentView.findViewById(R.id.arrow_right);
        arrow_left.setVisibility(View.INVISIBLE);
        if(entities.size()>7){
            arrow_right.setVisibility(View.VISIBLE);
        }else{
            arrow_right.setVisibility(View.INVISIBLE);
        }
        setArrowListener();
        list= (RecyclerView) contentView.findViewById(R.id.episode_list);
        list.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
     //   list.addItemDecoration(new SpaceItemDecoration(context.getResources().getDimensionPixelOffset(R.dimen.player_10)));
        adapter=new EpisodeAdapter(mContext,subitem,entities);
        adapter.setEpisodeOnclickListener(episodeOnclickListener);
        adapter.setOnFocusListener(this);
        adapter.setOnKeyListener(this);
        list.setAdapter(adapter);
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
                    if(index+7!=itemEntities.size()){
                        arrow_right.setVisibility(View.VISIBLE);
                    }else{
                        arrow_right.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });


        RelativeLayout relativeLayout = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams layoutParams;
        layoutParams = new RelativeLayout.LayoutParams(width, height);
        relativeLayout.addView(contentView, layoutParams);

        setContentView(relativeLayout);

        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable._000000));
        setFocusable(true);
    }

    private void setArrowListener() {

        arrow_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int child=list.getChildCount();
                int index=list.getChildLayoutPosition(list.getChildAt(child-1));
                if(itemEntities.size()-index>=7) {
                    list.smoothScrollToPosition(index + 7);
                }else{
                    list.smoothScrollToPosition(itemEntities.size()-1);
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
            if(index==pos&&pos!=itemEntities.size()-1){
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
        }
    }
}
