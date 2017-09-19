package tv.ismar.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.databinding.adapters.ViewGroupBindingAdapter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemList;
import tv.ismar.app.entity.SectionList;
import tv.ismar.app.ui.adapter.OnItemClickListener;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.channel.FilterActivity;
import tv.ismar.listpage.R;

/**
 * Created by admin on 2017/5/27.
 */

public class FilterPosterAdapter extends RecyclerView.Adapter<FilterPosterAdapter.FilterPosterHolder> {


    private Context mContext;
    private ItemList mItemList;
    private static boolean mIsVertical;
    private OnItemClickListener itemClickListener;
    private OnItemFocusedListener itemFocusedListener;
    private ArrayList<Integer> mSpecialPos;
    private SectionList mSectionList;
    private int focusedPosition=-1;
    private Rect rect;

    public FilterPosterAdapter(Context context, ItemList itemList, boolean isVertical, ArrayList<Integer> specialPos, SectionList sectionList) {
        this.mContext = context;
        this.mItemList = itemList;
        this.mIsVertical=isVertical;
        this.mSpecialPos=specialPos;
        this.mSectionList=sectionList;
        rect=new Rect(0,0,1920,540);
    }

    public void setmSpecialPos(ArrayList<Integer> mSpecialPos) {
        this.mSpecialPos = mSpecialPos;
    }

    public void setmItemList(ItemList mItemList) {
        this.mItemList = mItemList;
    }

    public void setFocusedPosition(int focusedPosition) {
        this.focusedPosition = focusedPosition;
    }

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

    public FilterPosterAdapter(Context context, ItemList itemList, boolean isVertical) {

        this.mContext = context;
        this.mItemList = itemList;
        this.mIsVertical=isVertical;
        rect=new Rect(0,0,1920,1080);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public FilterPosterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FilterPosterHolder filterPosterHolder;
        if(viewType==0) {
            if (mIsVertical) {
                filterPosterHolder = new FilterPosterHolder(LayoutInflater.from(mContext).inflate(R.layout.filter_item_vertical_poster, null));
            } else {
                filterPosterHolder = new FilterPosterHolder(LayoutInflater.from(mContext).inflate(R.layout.filter_item_horizontal_poster, null));
            }
        }else{
            TextView textView=new TextView(mContext);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimensionPixelSize(R.dimen.filter_layout_current_section_title_ts));
            textView.setWidth(mContext.getResources().getDimensionPixelOffset(R.dimen.list_section_title_w));
            if(mIsVertical) {
                textView.setHeight(mContext.getResources().getDimensionPixelOffset(R.dimen.list_section_vertical_title_h));
            }else {
                textView.setHeight(mContext.getResources().getDimensionPixelOffset(R.dimen.list_section_horizontal_title_h));
            }
            textView.setGravity(Gravity.CENTER_VERTICAL);
            filterPosterHolder=new FilterPosterHolder(textView);
        }
        return filterPosterHolder;
    }

    @Override
    public void onBindViewHolder(FilterPosterHolder holder, final int position) {
        if(getItemViewType(position)==0) {
            if(position<mItemList.objects.size()) {
                Item item = mItemList.objects.get(position);
                if (mIsVertical) {
                    if (item.bean_score > 0) {
                        holder.item_vertical_poster_mark.setText(item.bean_score + "");
                        holder.item_vertical_poster_mark.setVisibility(View.VISIBLE);
                    } else {
                        holder.item_vertical_poster_mark.setVisibility(View.INVISIBLE);
                    }
                    if (!TextUtils.isEmpty(item.title)) {
                        holder.item_vertical_poster_title.setText(item.title);
                        holder.item_vertical_title_bg.setVisibility(View.VISIBLE);
                    } else {
                        holder.item_vertical_poster_title.setText("");
                        holder.item_vertical_title_bg.setVisibility(View.INVISIBLE);
                    }
                    if (!TextUtils.isEmpty(item.list_url)) {
                        Picasso.with(mContext).load(item.list_url).error(R.drawable.list_item_ppreview_bg).placeholder(R.drawable.list_item_ppreview_bg).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).config(Bitmap.Config.RGB_565).
                                into(holder.item_vertical_poster_img);
                    } else {
                        Picasso.with(mContext).load(R.drawable.list_item_ppreview_bg).error(R.drawable.list_item_ppreview_bg).placeholder(R.drawable.list_item_ppreview_bg).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).config(Bitmap.Config.RGB_565).
                                into(holder.item_vertical_poster_img);
                    }
                    if (item.expense != null) {
                        Picasso.with(mContext).load(VipMark.getInstance().getImage((Activity) mContext, item.expense.pay_type, item.expense.cpid)).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.item_vertical_poster_vip);
                        holder.item_vertical_poster_vip.setVisibility(View.VISIBLE);
                    } else {
                        holder.item_vertical_poster_vip.setVisibility(View.GONE);
                    }

                } else {
                    if (item.bean_score > 0) {
                        holder.item_horizontal_poster_mark.setText(item.bean_score + "");
                        holder.item_horizontal_poster_mark.setVisibility(View.VISIBLE);
                    } else {
                        holder.item_horizontal_poster_mark.setVisibility(View.INVISIBLE);
                    }
                    if (!TextUtils.isEmpty(item.title)) {
                        holder.item_horizontal_poster_title.setText(item.title);
                    } else {
                        holder.item_horizontal_poster_title.setText("");
                        holder.item_horizontal_title_bg.setVisibility(View.INVISIBLE);
                    }
                    if (!TextUtils.isEmpty(item.focus)) {
                        holder.item_horizontal_poster_des.setText(item.focus);
                    }else {
                        holder.item_horizontal_poster_des.setVisibility(View.INVISIBLE);
                    }
                    if (!TextUtils.isEmpty(item.poster_url)) {
                        Picasso.with(mContext).load(item.poster_url).error(R.drawable.list_item_preview_bg).placeholder(R.drawable.list_item_preview_bg).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).config(Bitmap.Config.RGB_565)
                                .into(holder.item_horizontal_poster_img);
                    }else{
                        Picasso.with(mContext).load(R.drawable.list_item_preview_bg).error(R.drawable.list_item_preview_bg).placeholder(R.drawable.list_item_preview_bg).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).config(Bitmap.Config.RGB_565)
                                .into(holder.item_horizontal_poster_img);
                    }
                    if (item.expense != null) {
                        Picasso.with(mContext).load(VipMark.getInstance().getImage((Activity) mContext, item.expense.pay_type, item.expense.cpid)).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.item_horizontal_poster_vip);
                        holder.item_horizontal_poster_vip.setVisibility(View.VISIBLE);
                    } else {
                        holder.item_horizontal_poster_vip.setVisibility(View.GONE);
                    }
                }
                if (itemClickListener != null) {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            itemClickListener.onItemClick(v, position);
                        }
                    });
                }
                if (itemFocusedListener != null) {
                    holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            itemFocusedListener.onItemfocused(v, position, hasFocus);
                        }
                    });
                }
            }
            holder.itemView.setOnHoverListener(new View.OnHoverListener() {
                @Override
                public boolean onHover(View v, MotionEvent event) {
                    if((event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE)&&v.getLocalVisibleRect(rect))
                    v.requestFocus();
                    return false;
                }
            });
        }else{
            if(mSpecialPos.indexOf(position)<mSectionList.size())
                ((TextView)holder.itemView).setText(mSectionList.get(mSpecialPos.indexOf(position)).title);
        }
        if(position==focusedPosition){
            holder.itemView.requestFocus();
        }
    }


    @Override
    public int getItemCount() {
            return mItemList.objects.size();
    }


    @Override
    public int getItemViewType(int position) {
        if(mSpecialPos!=null&&mSpecialPos.contains(position)){
            return 1;
        }else{
            return 0;
        }
    }


    public static class FilterPosterHolder extends RecyclerView.ViewHolder{

        ImageView item_vertical_poster_img;
        ImageView item_vertical_poster_vip;
        TextView item_vertical_poster_mark;
        TextView item_vertical_poster_title;
        View item_vertical_title_bg;
        ImageView item_horizontal_poster_img;
        ImageView item_horizontal_poster_vip;
        TextView item_horizontal_poster_mark;
        TextView item_horizontal_poster_des;
        TextView item_horizontal_poster_title;
        View item_horizontal_title_bg;

        public FilterPosterHolder(View itemView) {
            super(itemView);
            if(mIsVertical) {
                item_vertical_poster_img = (ImageView) itemView.findViewById(R.id.item_vertical_poster_img);
                item_vertical_poster_vip = (ImageView) itemView.findViewById(R.id.item_vertical_poster_vip);
                item_vertical_poster_mark = (TextView) itemView.findViewById(R.id.item_vertical_poster_mark);
                item_vertical_poster_title = (TextView) itemView.findViewById(R.id.item_vertical_poster_title);
                item_vertical_title_bg=itemView.findViewById(R.id.item_vertical_title_bg);
            }else {
                item_horizontal_poster_img = (ImageView) itemView.findViewById(R.id.item_horizontal_poster_img);
                item_horizontal_poster_vip = (ImageView) itemView.findViewById(R.id.item_horizontal_poster_vip);
                item_horizontal_poster_mark = (TextView) itemView.findViewById(R.id.item_horizontal_poster_mark);
                item_horizontal_poster_des = (TextView) itemView.findViewById(R.id.item_horizontal_poster_des);
                item_horizontal_poster_title = (TextView) itemView.findViewById(R.id.item_horizontal_poster_title);
                item_horizontal_title_bg = itemView.findViewById(R.id.item_horizontal_title_bg);
            }
        }
    }
}
