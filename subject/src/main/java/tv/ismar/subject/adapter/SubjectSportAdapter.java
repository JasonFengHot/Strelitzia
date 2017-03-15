package tv.ismar.subject.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import tv.ismar.app.entity.Item;

/**
 * Created by liucan on 2017/3/15.
 */

public class SubjectSportAdapter extends RecyclerView.Adapter {
    private ArrayList<Item> itemList;


    @Override
    public int getItemCount() {
        return itemList.size();
    }
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }


}
