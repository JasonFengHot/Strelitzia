package tv.ismar.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import java.util.List;

import tv.ismar.app.ui.adapter.OnItemClickListener;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.listpage.R;

/**
 * Created by admin on 2017/6/2.
 */

public class FilterConditionAdapter extends RecyclerView.Adapter<FilterConditionAdapter.MyViewHolder> {

    private Context mContext;
    private List<List<String>> mData;
    private OnItemClickListener itemClickListener;
    private OnItemFocusedListener itemFocusedListener;
    private boolean isFirst=true;

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


    public FilterConditionAdapter(Context mContext, List<List<String>> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder myViewHolder=new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.filter_group_radio_button,null));
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.radio.setText(mData.get(position).get(1));
        holder.itemView.setTag(mData.get(position).get(0));
        holder.radio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClickListener!=null)
                itemClickListener.onItemClick(v,position);
            }
        });
        holder.radio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(itemFocusedListener!=null) {
                    itemFocusedListener.onItemfocused(v, position, hasFocus);
                }
            }
        });

        if(isFirst&&position==0){
            holder.radio.setChecked(true);
            isFirst = false;
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private RadioButton radio;
        public MyViewHolder(View itemView) {
            super(itemView);
            radio= (RadioButton) itemView.findViewById(R.id.radio);
        }
    }
}
