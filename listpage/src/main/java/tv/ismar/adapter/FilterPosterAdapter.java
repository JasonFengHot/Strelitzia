package tv.ismar.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
//add by dragontec for bug 4310 start
import android.view.KeyEvent;
//add by dragontec for bug 4310 end
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ImageView;
import android.widget.TextView;

//import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemList;
import tv.ismar.app.ui.adapter.OnItemClickListener;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;
//add by dragontec for bug 4310 start
import tv.ismar.app.ui.adapter.OnItemKeyListener;
//add by dragontec for bug 4310 end
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.listpage.R;
import tv.ismar.view.FilterListRecyclerView;

import static tv.ismar.channel.FilterListActivity.PICASSO_TAG;

/**
 * Created by admin on 2017/5/27.
 * 筛选海报横竖版adapter
 */

public class FilterPosterAdapter extends RecyclerView.Adapter<FilterPosterAdapter.FilterPosterHolder> {


    private Context mContext;
    private ItemList mItemList;
    private static boolean mIsVertical;
    private OnItemClickListener itemClickListener;
    private OnItemFocusedListener itemFocusedListener;
	//add by dragontec for bug 4310 start
    private OnItemKeyListener itemKeyListener;
	//add by dragontec for bug 4310 end
    private int focusedPosition=-1;
    private Rect rect;


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

	//add by dragontec for bug 4310 start
	public OnItemKeyListener getItemKeyListener () {
		return itemKeyListener;
	}

	public void setItemKeyListener(OnItemKeyListener itemKeyListener) {
		this.itemKeyListener = itemKeyListener;
	}
	//add by dragontec for bug 4310 end

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
            if (mIsVertical) {
                filterPosterHolder = new FilterPosterHolder(LayoutInflater.from(mContext).inflate(R.layout.filter_item_vertical_poster, null));
            } else {
                filterPosterHolder = new FilterPosterHolder(LayoutInflater.from(mContext).inflate(R.layout.filter_item_horizontal_poster, null));
            }
        return filterPosterHolder;
    }

    @Override
    public void onBindViewHolder(final FilterPosterHolder holder, final int position) {
    	/*modify by dragontec for bug 4343 start*/
            if(holder.getAdapterPosition()<mItemList.objects.size()) {
                final Item item = mItemList.objects.get(holder.getAdapterPosition());
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
/*modify by dragontec for bug 4336 start*/
                        Picasso.with(mContext).load(item.list_url).tag(PICASSO_TAG).error(R.drawable.item_vertical_preview).placeholder(R.drawable.item_vertical_preview).config(Bitmap.Config.RGB_565).
                                into(holder.item_vertical_poster_img);
/*modify by dragontec for bug 4336 end*/
                    } else {
/*modify by dragontec for bug 4336 start*/
                        Picasso.with(mContext).load(R.drawable.item_vertical_preview).tag(PICASSO_TAG).config(Bitmap.Config.RGB_565).
                                into(holder.item_vertical_poster_img);
/*modify by dragontec for bug 4336 end*/
                    }
                    if (item.expense != null) {
                        Picasso.with(mContext).load(VipMark.getInstance().getImage((Activity) mContext, item.expense.pay_type, item.expense.cpid)).tag(PICASSO_TAG).into(holder.item_vertical_poster_vip);
                        holder.item_vertical_poster_vip.setVisibility(View.VISIBLE);
                    } else {
                        holder.item_vertical_poster_vip.setVisibility(View.GONE);
                    }
                    if (itemFocusedListener != null) {
                        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if(hasFocus){
                                    if(!TextUtils.isEmpty(item.focus)){
                                        holder.item_vertical_poster_title.setText(item.focus);
                                    }
                                    holder.item_vertical_poster_title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                }else{
                                    if(!TextUtils.isEmpty(item.title)){
                                        holder.item_vertical_poster_title.setText(item.title);
                                    }
                                    holder.item_vertical_poster_title.setEllipsize(TextUtils.TruncateAt.END);
                                }
                                itemFocusedListener.onItemfocused(v, holder.getAdapterPosition(), hasFocus);
                            }
                        });
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
                        holder.item_horizontal_title_bg.setVisibility(View.VISIBLE);
                    } else {
                        holder.item_horizontal_poster_title.setText("");
                        holder.item_horizontal_title_bg.setVisibility(View.INVISIBLE);
                    }
                    if (!TextUtils.isEmpty(item.focus)) {
                        holder.item_horizontal_poster_des.setText(item.focus);
                        holder.item_horizontal_poster_des.setVisibility(View.VISIBLE);
                    }else {
                        holder.item_horizontal_poster_des.setVisibility(View.INVISIBLE);
                    }
                    if (!TextUtils.isEmpty(item.poster_url)) {
                        Picasso.with(mContext).load(item.poster_url).tag(PICASSO_TAG).error(R.drawable.item_horizontal_preview).placeholder(R.drawable.item_horizontal_preview).config(Bitmap.Config.RGB_565)
                                .into(holder.item_horizontal_poster_img);
                    }else{
                        Picasso.with(mContext).load(R.drawable.item_horizontal_preview).tag(PICASSO_TAG).error(R.drawable.item_horizontal_preview).placeholder(R.drawable.item_horizontal_preview).config(Bitmap.Config.RGB_565)
                                .into(holder.item_horizontal_poster_img);
                    }
                    if (item.expense != null) {
                        Picasso.with(mContext).load(VipMark.getInstance().getImage((Activity) mContext, item.expense.pay_type, item.expense.cpid)).tag(PICASSO_TAG).into(holder.item_horizontal_poster_vip);
                        holder.item_horizontal_poster_vip.setVisibility(View.VISIBLE);
                    } else {
                        holder.item_horizontal_poster_vip.setVisibility(View.GONE);
                    }
                    if (itemFocusedListener != null) {
                        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if(hasFocus){
                                    holder.item_horizontal_poster_title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                }else{
                                    holder.item_horizontal_poster_title.setEllipsize(TextUtils.TruncateAt.END);
                                }
                                itemFocusedListener.onItemfocused(v, holder.getAdapterPosition(), hasFocus);
                            }
                        });
                    }
                }
				//add by dragontec for bug 4310 start
                if (itemKeyListener != null) {
                	holder.itemView.setOnKeyListener(new View.OnKeyListener() {
						@Override
						public boolean onKey(View v, int keyCode, KeyEvent event) {
							itemKeyListener.onItemKeyListener(v, keyCode, event);
							return false;
						}
					});
				}
				//add by dragontec for bug 4310 end
                if (itemClickListener != null) {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            itemClickListener.onItemClick(v, holder.getAdapterPosition());
                        }
                    });
                }

            }
            holder.itemView.setOnHoverListener(new View.OnHoverListener() {
                @Override
                public boolean onHover(View v, MotionEvent event) {
                    if((event.getAction()==MotionEvent.ACTION_HOVER_ENTER||event.getAction()==MotionEvent.ACTION_HOVER_MOVE)&&v.getLocalVisibleRect(rect)){
                        if(v.getParent() != null && v.getParent() instanceof FilterListRecyclerView){
                            if(!((FilterListRecyclerView)v.getParent()).isScrolling()){
                                v.requestFocus();
                            }
                        }
                    }
                    return false;
                }
            });
        if(position==focusedPosition){
            holder.itemView.requestFocus();
        }
        /*modify by dragontec for bug 4343 end*/
    }


    @Override
    public int getItemCount() {
            return mItemList.objects.size();
    }



    public static class FilterPosterHolder extends RecyclerView.ViewHolder{

        RecyclerImageView item_vertical_poster_img;
        RecyclerImageView item_vertical_poster_vip;
        TextView item_vertical_poster_mark;
        TextView item_vertical_poster_title;
        View item_vertical_title_bg;
        RecyclerImageView item_horizontal_poster_img;
        RecyclerImageView item_horizontal_poster_vip;
        TextView item_horizontal_poster_mark;
        TextView item_horizontal_poster_des;
        TextView item_horizontal_poster_title;
        View item_horizontal_title_bg;

        public FilterPosterHolder(View itemView) {
            super(itemView);
            if(mIsVertical) {
                item_vertical_poster_img = (RecyclerImageView) itemView.findViewById(R.id.item_vertical_poster_img);
                item_vertical_poster_vip = (RecyclerImageView) itemView.findViewById(R.id.item_vertical_poster_vip);
                item_vertical_poster_mark = (TextView) itemView.findViewById(R.id.item_vertical_poster_mark);
                item_vertical_poster_title = (TextView) itemView.findViewById(R.id.item_vertical_poster_title);
                item_vertical_title_bg=itemView.findViewById(R.id.item_vertical_title_bg);
            }else {
                item_horizontal_poster_img = (RecyclerImageView) itemView.findViewById(R.id.item_horizontal_poster_img);
                item_horizontal_poster_vip = (RecyclerImageView) itemView.findViewById(R.id.item_horizontal_poster_vip);
                item_horizontal_poster_mark = (TextView) itemView.findViewById(R.id.item_horizontal_poster_mark);
                item_horizontal_poster_des = (TextView) itemView.findViewById(R.id.item_horizontal_poster_des);
                item_horizontal_poster_title = (TextView) itemView.findViewById(R.id.item_horizontal_poster_title);
                item_horizontal_title_bg = itemView.findViewById(R.id.item_horizontal_title_bg);
            }
        }
    }
}
