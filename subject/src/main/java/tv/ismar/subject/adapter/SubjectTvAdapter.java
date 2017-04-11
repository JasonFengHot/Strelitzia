package tv.ismar.subject.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.entity.Item;
import tv.ismar.app.models.SubjectEntity;
import tv.ismar.searchpage.model.Expense;
import tv.ismar.subject.R;

/**
 * Created by admin on 2017/3/2.
 */

public class SubjectTvAdapter extends RecyclerView.Adapter<MovieViewHolder> {

    private Context mContext;
    private List<SubjectEntity.ObjectsBean> mList;
    private boolean isFirst=true;

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    private OnItemFocusedListener mOnItemFocusedListener;

    public void setOnItemFocusedListener(OnItemFocusedListener mOnItemFocusedListener) {
        this.mOnItemFocusedListener = mOnItemFocusedListener;
    }

    public SubjectTvAdapter(Context mContext, List<SubjectEntity.ObjectsBean> mList) {
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
        SubjectEntity.ObjectsBean item = mList.get(position);
        if (item.getBean_score() > 0) {
            holder.movie_item_score.setText(item.getBean_score() + "");
            holder.movie_item_score.setVisibility(View.VISIBLE);
        }
        holder.movie_item_title.setText(item.getTitle());
        Picasso.with(mContext).load(item.getPoster_url()).memoryPolicy(MemoryPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_STORE).placeholder(R.drawable.list_item_preview_bg).into(holder.movie_item_poster);
        if(item.getExpense()!=null){
            if(item.getExpense().cptitle!=null){
                holder.movie_item_mark.setText(item.getExpense().cptitle);
            }
            if(item.getExpense().pay_type== Expense.SEPARATE_CHARGE){
                holder.movie_item_mark.setBackgroundResource(R.drawable.ismartv);
            }else if((item.getExpense().cpid == Expense.ISMARTV_CPID)){
                holder.movie_item_mark.setBackgroundResource(R.drawable.ismartv);
            }else if((item.getExpense().cpid == Expense.IQIYI_CPID)){
                holder.movie_item_mark.setBackgroundResource(R.drawable.vip);
            }
        }
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
                    if(hasFocus){
                        holder.movie_item_title.setSelected(true);
                    }else{
                        holder.movie_item_title.setSelected(false);
                    }
                }
            });
        }
        if(isFirst){
            holder.itemView.requestFocus();
            isFirst=false;
        }
    }

}
