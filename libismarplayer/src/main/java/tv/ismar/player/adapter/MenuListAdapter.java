package tv.ismar.player.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import tv.ismar.player.R;
import tv.ismar.player.listener.MenuOnFocuslistener;
import tv.ismar.player.listener.MenuOnKeyListener;
import tv.ismar.player.listener.OnMenuListItmeClickListener;
import tv.ismar.player.model.QuailtyEntity;

/**
 * Created by liucan on 2017/5/26.
 */

public class MenuListAdapter extends RecyclerView.Adapter<MenuItemHoder> {
    private Context mContext;
    private ArrayList<QuailtyEntity> mList=new ArrayList<>();
    private MenuOnFocuslistener menuOnFocuslistener;
    private OnMenuListItmeClickListener onMenuListItmeClickListener;
    private MenuOnKeyListener onKeyListener;
    public MenuListAdapter(Context context,ArrayList<QuailtyEntity> list){
        mContext=context;
        mList=list;
    }
    public void setOnMenuListItmeClickListener(OnMenuListItmeClickListener listener){
        onMenuListItmeClickListener=listener;
    }
    public void setOnKeyListener(MenuOnKeyListener listener){
        onKeyListener=listener;
    }

    public void setMenuOnFocuslistener(MenuOnFocuslistener menuOnFocuslistener1){
        menuOnFocuslistener=menuOnFocuslistener1;
    }
    @Override
    public MenuItemHoder onCreateViewHolder(ViewGroup parent, int viewType) {
        MenuItemHoder viewHolder=new MenuItemHoder(LayoutInflater.from(mContext).inflate(R.layout.menu_list_item,parent,false));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MenuItemHoder holder, final int position) {
        holder.textView.setText(mList.get(position).getName());
        holder.focus_text.setText(mList.get(position).getName());
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
//                    holder.textView.setTextColor(mContext.getResources().getColor(R.color._f0f0f0));
//                    holder.textView.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.text_size_24sp));
                    holder.line.setVisibility(View.GONE);
                    holder.focus_line.setVisibility(View.VISIBLE);
                }else{
                    holder.focus_line.setVisibility(View.GONE);
                    holder.line.setVisibility(View.VISIBLE);
//                    holder.textView.setTextColor(mContext.getResources().getColor(R.color._666666));
//                    holder.textView.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.text_size_24sp));
                }
                if(menuOnFocuslistener!=null) {
                    menuOnFocuslistener.onFocus(v, position, hasFocus);
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuListItmeClickListener.onMenuItemClick(mList.get(position).getValue(),mList.get(position).getName());
            }
        });
        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i("menu_select","itemOnkey"+keyCode);
                if(keyCode==21||keyCode==22){
                    return true;
                }else if(event.getAction()==KeyEvent.ACTION_DOWN&&keyCode==4){
                    onKeyListener.onkeyBack(keyCode);
                    return true;
                }

                return false;
            }
        });
        holder.itemView.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
