package tv.ismar.subject.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tv.ismar.app.entity.Item;
import tv.ismar.subject.R;

/**
 * Created by admin on 2017/3/2.
 */

public class SubjectTvAdapter extends RecyclerView.Adapter<MovieViewHolder> {

    private Context mContext;
    private ArrayList<Item> mList;

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    private OnItemFocusedListener mOnItemFocusedListener;

    public void setOnItemFocusedListener(OnItemFocusedListener mOnItemFocusedListener) {
        this.mOnItemFocusedListener = mOnItemFocusedListener;
    }

    public SubjectTvAdapter(Context mContext, ArrayList<Item> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MovieViewHolder viewHolder=new MovieViewHolder(LayoutInflater.from(mContext).inflate(R.layout.tv_item_subject,parent,false));
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onBindViewHolder(final MovieViewHolder holder, int position) {
        Item item = mList.get(position);
//        Picasso.with(mContext).load(item.poster_url).memoryPolicy(MemoryPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_STORE).into(holder.movie_item_poster);
        if (item.bean_score > 0) {
            holder.movie_item_score.setText(item.bean_score + "");
        }
        holder.movie_item_title.setText(item.title);
        holder.movie_item_mark.setText("视云VIP");
        holder.movie_item_mark.setBackgroundResource(R.drawable.ismartv);
        // 如果设置了回调，则设置点击事件
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(v, pos);
                }
            });
        }
        if (mOnItemFocusedListener != null) {
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    int pos = holder.getLayoutPosition();
                    mOnItemFocusedListener.onItemfocused(v, pos, hasFocus);
                }
            });
        }
        if(position==0){
            holder.itemView.requestFocus();
        }
    }

}
