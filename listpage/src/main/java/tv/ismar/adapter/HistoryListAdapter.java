package tv.ismar.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.app.ui.adapter.OnItemKeyListener;
import tv.ismar.app.ui.adapter.OnItemOnhoverlistener;
import tv.ismar.app.entity.HistoryFavoriteEntity;
import tv.ismar.listener.LfListItemClickListener;
import tv.ismar.listpage.R;
import tv.ismar.view.IsmartvLinearLayout;

/**
 * Created by liucan on 2017/8/24.
 */
public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.HistoryViewholder>{
    private Context mContext;
    private List<HistoryFavoriteEntity> items;
    private boolean isVisibility=false;
    private OnItemFocusedListener itemFocusedListener;
    private LfListItemClickListener itemClickListener;
    private OnItemOnhoverlistener itemOnhoverlistener;
    private OnItemKeyListener itemKeyListener;
    private String type="history";
    private String lastTime="";

    public HistoryListAdapter(Context context,List<HistoryFavoriteEntity> items1,String itemType){
        mContext=context;
        items=items1;
        type=itemType;
    }
    public void setItemClickListener(LfListItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setItemOnhoverlistener(OnItemOnhoverlistener itemOnhoverlistener){
        this.itemOnhoverlistener=itemOnhoverlistener;
    }

    public void setItemFocusedListener(OnItemFocusedListener itemFocusedListener) {
        this.itemFocusedListener = itemFocusedListener;
    }
    public void setItemKeyListener(OnItemKeyListener itemKeyListener){
        this.itemKeyListener=itemKeyListener;
    }

    @Override
    public HistoryViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        HistoryViewholder historyViewholder=new HistoryViewholder(LayoutInflater.from(mContext).inflate(R.layout.history_favorite_list_item,parent,false));
        return historyViewholder;
    }

    @Override
    public void onBindViewHolder(final HistoryViewholder holder, final int position) {
        HistoryFavoriteEntity item = items.get(position);
         if(item.getType()!=2){
                if(item.getAdlet_url()!=null&&!item.getAdlet_url().isEmpty()) {
                    Picasso.with(mContext).load(item.getAdlet_url()).error(R.drawable.list_item_preview_bg).into(holder.item_detail_image);
                }else{
                    Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.item_detail_image);
                }
                 holder.item_title.setText(item.getTitle());
                 holder.item_title.setVisibility(View.VISIBLE);
                 holder.item_title_layout.setVisibility(View.VISIBLE);
                 holder.item_detail_image.setVisibility(View.VISIBLE);
                 holder.more.setVisibility(View.GONE);
                 holder.item_time_node.setVisibility(View.VISIBLE);
            if(item.getDate()!=null&&item.getDate().contains("-")){
                String[] date=item.getDate().split("-");
                if(item.isShowDate()){
                    holder.item_time.setText(date[0]+"月"+date[1]+"日");
                    holder.item_time_node.setVisibility(View.VISIBLE);
                    holder.item_time.setVisibility(View.VISIBLE);
                }else{
                    holder.item_time.setVisibility(View.GONE);
                    holder.item_time_node.setVisibility(View.GONE);
                }
            }

        }else{
            holder.item_title_layout.setVisibility(View.GONE);
            holder.item_detail_image.setVisibility(View.GONE);
            holder.more.setVisibility(View.VISIBLE);
            holder.item_time_node.setVisibility(View.GONE);
            holder.item_time.setVisibility(View.GONE);
        }

        holder.item_detail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                int pos = holder.getLayoutPosition();
                itemFocusedListener.onItemfocused(v, pos, hasFocus);
                if(hasFocus){
                    holder.item_title.setSelected(true);
                    holder.item_title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                }else{
                    holder.item_title.setSelected(false);
                    holder.item_title.setEllipsize(TextUtils.TruncateAt.END);
                }

            }
        });

        holder.item_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onlfItemClick(v,position,type);
            }
        });
        holder.item_detail.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
               itemOnhoverlistener.OnItemOnhoverlistener(v,event,position,0);
                return false;
            }
        });
        holder.item_detail.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction()==KeyEvent.ACTION_DOWN){
                    itemKeyListener.onItemKeyListener(v,keyCode,event);
                    if(position==items.size()-1&&keyCode==22){
                        YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(holder.item_detail);
                        return true;
                    }
                    if(position==0&&keyCode==21){
                        YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(holder.item_detail);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void setSettingVisibility(boolean visibility){
        isVisibility=visibility;
    }
    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class HistoryViewholder extends RecyclerView.ViewHolder{
        ImageView item_time_node;
        ImageView item_detail_image;
        TextView item_time;
        TextView item_title;
        IsmartvLinearLayout item_detail;
        RelativeLayout item_title_layout;
        ImageView more;

        public HistoryViewholder(View itemView) {
            super(itemView);
            item_time= (TextView) itemView.findViewById(R.id.item_time);
            item_detail_image= (ImageView) itemView.findViewById(R.id.item_detail_image);
            item_time_node= (ImageView) itemView.findViewById(R.id.item_time_node);
            item_title= (TextView) itemView.findViewById(R.id.item_title);
            item_detail= (IsmartvLinearLayout) itemView.findViewById(R.id.item_detail);
            more= (ImageView) itemView.findViewById(R.id.more);
            item_title_layout= (RelativeLayout) itemView.findViewById(R.id.item_title_layout);
        }
    }
}
