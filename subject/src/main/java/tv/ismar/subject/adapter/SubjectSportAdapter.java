package tv.ismar.subject.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import tv.ismar.app.entity.Item;
import tv.ismar.subject.R;

/**
 * Created by liucan on 2017/3/15.
 */

public class SubjectSportAdapter extends RecyclerView.Adapter<SportViewHolder> {
    private ArrayList<Item> itemList;
    private Context mContext;

    @Override
    public int getItemCount() {
        return itemList.size();
    }
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
    @Override
    public SportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SportViewHolder viewHolder=new SportViewHolder(LayoutInflater.from(mContext).inflate(R.layout.sport_list_item,parent,false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SportViewHolder holder, int position) {

    }

}
