package tv.ismar.homepage.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.entity.banner.BannerCarousels;
import tv.ismar.homepage.R;

import tv.ismar.homepage.banner.IsmartvLinearLayout;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 居中RecyclerView适配器
 */

public class CenterAdapter extends BaseRecycleAdapter<CenterAdapter.CenterViewHolder> {

    private Context mContext;
    private List<BannerCarousels> mData;

    public CenterAdapter(Context context, List<BannerCarousels> data){
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public CenterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_center_item,parent,false);
        return new CenterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CenterViewHolder holder, int position) {
        BannerCarousels carousels = mData.get(position%mData.size());
        holder.mTitle.setText(carousels.title);
		/*add by dragontec for bug 4307,4277 start*/
        holder.mLayout.setFocusableInTouchMode(false);
		/*add by dragontec for bug 4307,4277 end*/
        if (!TextUtils.isEmpty(carousels.video_image)) {
            Picasso.with(mContext).load(carousels.video_image).into(holder.mPosterIg);
        } else {
            Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mPosterIg);
        }
    }

    @Override
    public int getItemCount() {
        if (mData.size() == 0){
            return 0;
        }else {
            return Integer.MAX_VALUE;
        }
    }

    public class CenterViewHolder extends BaseViewHolder{
        public TextView mTitle;
        public ImageView mPosterIg;
		/*add by dragontec for bug 4307,4277 start*/
        public IsmartvLinearLayout mLayout;
		/*add by dragontec for bug 4307,4277 end*/

        public CenterViewHolder(View itemView) {
            super(itemView, CenterAdapter.this);
            mTitle = (TextView) itemView.findViewById(R.id.center_item_title);
            mPosterIg = (ImageView) itemView.findViewById(R.id.center_item_poster);
			/*add by dragontec for bug 4307,4277 start*/
            mLayout = (IsmartvLinearLayout) itemView.findViewById(R.id.center_ismartv_linear_layout);
			/*add by dragontec for bug 4307,4277 end*/
        }

        @Override
        protected float getScaleXY() {
            return 1.2F;
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.center_ismartv_linear_layout;
        }
    }
}
