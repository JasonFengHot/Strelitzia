package tv.ismar.player.gui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.player.R;
import tv.ismar.player.listener.EpisodeOnFocusListener;
import tv.ismar.player.listener.EpisodeOnKeyListener;
import tv.ismar.player.listener.EpisodeOnclickListener;

/**
 * Created by liucan on 2017/5/24.
 */

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeViewHolder> {
    private ArrayList<ItemEntity> itemEntities;
    private Context mContext;
    private int pk;
    private EpisodeOnclickListener onclickListener;
    private EpisodeOnFocusListener onFocusListener;
    private EpisodeOnKeyListener onKeyListener;

    public EpisodeAdapter(Context context,int subitmePk,ArrayList<ItemEntity> entities){
        mContext=context;
        pk=subitmePk;
        itemEntities=entities;
    }
    public void setEpisodeOnclickListener(EpisodeOnclickListener episodeOnclickListener){
        onclickListener=episodeOnclickListener;
    }
    public void setOnKeyListener(EpisodeOnKeyListener onkey){
        onKeyListener=onkey;
    }

    public void setOnFocusListener(EpisodeOnFocusListener listener){
        onFocusListener=listener;
    }

    @Override
    public EpisodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        EpisodeViewHolder viewHolder=new EpisodeViewHolder(LayoutInflater.from(mContext).inflate(R.layout.episode_tv_item,parent,false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EpisodeViewHolder holder, final int position) {
        final ItemEntity itemEntity=itemEntities.get(position);
        String subItemTitle = itemEntity.getTitle();
        if (subItemTitle.contains("第")) {
            int ind = subItemTitle.indexOf("第");
            subItemTitle = subItemTitle.substring(ind);
        }
        holder.title.setText(subItemTitle);
        if(pk==itemEntity.getPk()){
            holder.play_select.setVisibility(View.VISIBLE);
        }else{
            holder.play_select.setVisibility(View.INVISIBLE);
        }
        if(onclickListener!=null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onclickListener.onItemClick(itemEntity.getPk());
                }
            });
        }
        if(onFocusListener!=null){
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    onFocusListener.onItemFocus(v,hasFocus,position);

                }
            });
        }
        if(onKeyListener!=null){
            holder.itemView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if(event.getAction()==KeyEvent.ACTION_DOWN) {
                            onKeyListener.onKeyListener(keyCode, position);
                        }
                        return false;
                }
            });
        }
        holder.itemView.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemEntities.size();
    }
}

