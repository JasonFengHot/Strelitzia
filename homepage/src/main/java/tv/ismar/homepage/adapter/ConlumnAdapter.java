package tv.ismar.homepage.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 栏目RecyclerView适配器
 */

public class ConlumnAdapter extends BaseRecycleAdapter<ConlumnAdapter.ConlumnViewHolder> {

    private Context mContext;
    private List<BannerPoster> mData;

    public ConlumnAdapter(Context context, List<BannerPoster> data){
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
        BannerPoster poster = mData.get(position);
        Log.d("ConlumnAdapter", "position:"+position);
        holder.mTitle.setText(poster.title);
        if (!TextUtils.isEmpty(poster.poster_url)) {
            Picasso.with(mContext).load(poster.poster_url).into(holder.mPoster);
        } else {
            Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mPoster);
        }
    }

    @Override
    public int getItemCount() {
        return (mData!=null)? mData.size() : 0;
    }

    public class ConlumnViewHolder extends BaseViewHolder{
        public TextView mTitle;
        public ImageView mPoster;

        public ConlumnViewHolder(View itemView) {
            super(itemView, ConlumnAdapter.this);
            mTitle = (TextView) itemView.findViewById(R.id.conlumn_item_tv);
            mPoster = (ImageView) itemView.findViewById(R.id.conlumn_item_poster);
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.conlumn_ismartv_linear_layout;
        }
    }
}
