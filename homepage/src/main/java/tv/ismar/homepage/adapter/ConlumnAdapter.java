package tv.ismar.homepage.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 栏目RecyclerView
 */

public class ConlumnAdapter extends RecyclerView.Adapter<ConlumnAdapter.ConlumnViewHolder> {

    private Context mContext;
    private HomeEntity mData;

    public ConlumnAdapter(Context context, HomeEntity data){
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public ConlumnViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_conlumn_item,parent,false);
        return new ConlumnViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConlumnViewHolder holder, int position) {
        holder.mTitle.setText(mData.banner);
    }

    @Override
    public int getItemCount() {
        return (mData!=null)? mData.count : 0;
    }

    public static class ConlumnViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle;
        public ConlumnViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.conlumn_item_tv);
        }
    }
}
