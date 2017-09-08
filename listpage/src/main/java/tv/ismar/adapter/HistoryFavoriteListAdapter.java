package tv.ismar.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.app.core.Source;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.Item;
import tv.ismar.app.ui.adapter.OnItemClickListener;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.entity.HistoryFavoriteEntity;
import tv.ismar.listpage.R;

/**
 * Created by liucan on 2017/8/29.
 */

public class HistoryFavoriteListAdapter extends RecyclerView.Adapter<HistoryFavoriteListAdapter.listViewholder>{
    private Context mContext;
    private List<HistoryFavoriteEntity> items;
    private int type;
    private String listsource;
    private OnItemClickListener itemClickListener;
    private OnItemFocusedListener itemFocusedListener;
    public HistoryFavoriteListAdapter(Context context,List<HistoryFavoriteEntity> item,int typeId,String source) {
        mContext=context;
        items=item;
        type=typeId;
        listsource=source;
    }
    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    public void setItemFocusedListener(OnItemFocusedListener itemFocusedListener) {
        this.itemFocusedListener = itemFocusedListener;
    }

    @Override
    public listViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        listViewholder viewholder=new listViewholder(LayoutInflater.from(mContext).inflate(R.layout.history_favorite_edit_item,parent,false));
        return viewholder;
    }

    @Override
    public void onBindViewHolder(final listViewholder holder, final int position) {
        HistoryFavoriteEntity item=items.get(position);
        if(item!=null) {
            if (item.getBean_score() > 0) {
                holder.marking.setText(item.getBean_score() + "");
                holder.marking.setVisibility(View.VISIBLE);
                holder.marking_bg.setVisibility(View.VISIBLE);
            } else {
                holder.marking_bg.setVisibility(View.GONE);
                holder.marking.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(item.getFocus())) {
                holder.intro.setText(item.getFocus());
            }
            if (!TextUtils.isEmpty(item.getTitle()))
                holder.title.setText(item.getTitle());
            if (!TextUtils.isEmpty(item.getAdlet_url())) {
                Picasso.with(mContext).load(item.getAdlet_url()).into(holder.item_detail_image);
            }
            if (item.getExpense() != null) {
                Picasso.with(mContext).load(VipMark.getInstance().getImage((Activity) mContext, item.getExpense().pay_type, item.getExpense().cpid)).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.vip_image);
                holder.vip_image.setVisibility(View.VISIBLE);
            } else {
                holder.vip_image.setVisibility(View.GONE);
            }
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        if (listsource.equals("edit")) {
                            holder.delete_bg.setVisibility(View.VISIBLE);
                        }
                        holder.title.setSelected(true);
                    } else {
                        holder.delete_bg.setVisibility(View.GONE);
                        holder.title.setSelected(false);
                    }
                    itemFocusedListener.onItemfocused(v, position, hasFocus);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(v, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class listViewholder extends RecyclerView.ViewHolder{
        private ImageView item_detail_image,vip_image,marking_bg,delete_bg;
        private TextView marking,intro,title;
        public listViewholder(View itemView) {
            super(itemView);
            item_detail_image= (ImageView) itemView.findViewById(R.id.item_detail_image);
            vip_image= (ImageView) itemView.findViewById(R.id.vip_image);
            marking= (TextView) itemView.findViewById(R.id.marking);
            marking_bg= (ImageView) itemView.findViewById(R.id.marking_bg);
            intro= (TextView) itemView.findViewById(R.id.intro);
            title= (TextView) itemView.findViewById(R.id.focus_title);
            delete_bg= (ImageView) itemView.findViewById(R.id.edit_bg);
        }
    }
}