package tv.ismar.adapter;

import android.app.Activity;
import android.content.Context;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemList;
import tv.ismar.app.ui.adapter.OnItemClickListener;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.listpage.R;

/**
 * Created by admin on 2017/5/27.
 */

public class FilterPosterAdapter extends RecyclerView.Adapter<FilterPosterAdapter.FilterPosterHolder> {


    private Context mContext;
    private ItemList mItemList;
    private boolean mIsVertical;
    private int spanCount;
    private OnItemClickListener itemClickListener;
    private OnItemFocusedListener itemFocusedListener;

    public OnItemClickListener getItemClickListener() {
        return itemClickListener;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public OnItemFocusedListener getItemFocusedListener() {
        return itemFocusedListener;
    }

    public void setItemFocusedListener(OnItemFocusedListener itemFocusedListener) {
        this.itemFocusedListener = itemFocusedListener;
    }

    public FilterPosterAdapter(Context context,ItemList itemList, boolean isVertical) {

        this.mContext = context;
        this.mItemList = itemList;
        this.mIsVertical=isVertical;
    }

    @Override
    public FilterPosterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FilterPosterHolder filterPosterHolder;
        if(mIsVertical){
            filterPosterHolder=new FilterPosterHolder(LayoutInflater.from(mContext).inflate(R.layout.filter_item_vertical_poster,null));
        }else{
            filterPosterHolder=new FilterPosterHolder(LayoutInflater.from(mContext).inflate(R.layout.filter_item_horizontal_poster,null));
        }
        return filterPosterHolder;
    }

    @Override
    public void onBindViewHolder(FilterPosterHolder holder, final int position) {
        Item item=mItemList.objects.get(position);
        if(mIsVertical){
            if(item.bean_score>0) {
                holder.item_vertical_poster_mark.setText(item.bean_score + "");
                holder.item_vertical_poster_mark.setVisibility(View.VISIBLE);
            }else{
                holder.item_vertical_poster_mark.setVisibility(View.INVISIBLE);
            }
            if(!TextUtils.isEmpty(item.title)){
                holder.item_vertical_poster_title.setText(item.title);
            }
            if(!TextUtils.isEmpty(item.vertical_url)){
                Picasso.with(mContext).load(item.vertical_url).error(R.drawable.list_item_ppreview_bg).placeholder(R.drawable.list_item_ppreview_bg).into(holder.item_vertical_poster_img);
            }
            if(item.expense!=null){
                Picasso.with(mContext).load(VipMark.getInstance().getImage((Activity) mContext,item.expense.pay_type,item.expense.cpid)).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.item_vertical_poster_vip);
                holder.item_vertical_poster_vip.setVisibility(View.VISIBLE);
            }else{
                holder.item_vertical_poster_vip.setVisibility(View.GONE);
            }
            spanCount=5;

        }else{
            if(item.bean_score>0) {
                holder.item_horizontal_poster_mark.setText(item.bean_score + "");
                holder.item_horizontal_poster_mark.setVisibility(View.VISIBLE);
            }else{
                holder.item_horizontal_poster_mark.setVisibility(View.INVISIBLE);
            }
            if(!TextUtils.isEmpty(item.title)){
                holder.item_horizontal_poster_title.setText(item.title);
            }
            if(!TextUtils.isEmpty(item.focus)){
                holder.item_horizontal_poster_des.setText(item.focus);
            }
            if(!TextUtils.isEmpty(item.poster_url)){
                Picasso.with(mContext).load(item.poster_url).error(R.drawable.list_item_preview_bg).placeholder(R.drawable.list_item_preview_bg).into(holder.item_horizontal_poster_img);
            }
            if(item.expense!=null){
                Picasso.with(mContext).load(VipMark.getInstance().getImage((Activity) mContext,item.expense.pay_type,item.expense.cpid)).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.item_horizontal_poster_vip);
                holder.item_horizontal_poster_vip.setVisibility(View.VISIBLE);
            }else{
                holder.item_horizontal_poster_vip.setVisibility(View.GONE);
            }
            spanCount=3;
        }
        int lastLineCount=mItemList.objects.size()%spanCount;
        if(lastLineCount==0){
            lastLineCount=spanCount;
        }
        for (int i = 1; i <=lastLineCount ; i++) {
            if(position==mItemList.objects.size()-i){
                holder.itemView.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if(keyCode==20) {
                            return true;
                        }else{
                            return false;
                        }
                    }
                });
            }

        }
        if(itemClickListener!=null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(v,position);
                }
            });
        }
        if(itemFocusedListener!=null){
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    itemFocusedListener.onItemfocused(v,position,hasFocus);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mItemList.objects.size();
    }

    public class FilterPosterHolder extends RecyclerView.ViewHolder{

        ImageView item_vertical_poster_img;
        ImageView item_vertical_poster_vip;
        TextView item_vertical_poster_mark;
        TextView item_vertical_poster_title;
        ImageView item_horizontal_poster_img;
        ImageView item_horizontal_poster_vip;
        TextView item_horizontal_poster_mark;
        TextView item_horizontal_poster_des;
        TextView item_horizontal_poster_title;

        public FilterPosterHolder(View itemView) {
            super(itemView);
            if(mIsVertical) {
                item_vertical_poster_img = (ImageView) itemView.findViewById(R.id.item_vertical_poster_img);
                item_vertical_poster_vip = (ImageView) itemView.findViewById(R.id.item_vertical_poster_vip);
                item_vertical_poster_mark = (TextView) itemView.findViewById(R.id.item_vertical_poster_mark);
                item_vertical_poster_title = (TextView) itemView.findViewById(R.id.item_vertical_poster_title);
            }else {
                item_horizontal_poster_img = (ImageView) itemView.findViewById(R.id.item_horizontal_poster_img);
                item_horizontal_poster_vip = (ImageView) itemView.findViewById(R.id.item_horizontal_poster_vip);
                item_horizontal_poster_mark = (TextView) itemView.findViewById(R.id.item_horizontal_poster_mark);
                item_horizontal_poster_des = (TextView) itemView.findViewById(R.id.item_horizontal_poster_des);
                item_horizontal_poster_title = (TextView) itemView.findViewById(R.id.item_horizontal_poster_title);
            }
        }
    }
}
