package tv.ismar.homepage.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.entity.banner.BannerCarousels;
import tv.ismar.app.widget.MyRecyclerView;
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
	/*add by dragontec for bug 4316,卖点文字不正确的问题 start*/
        holder.mIntroduction.setText(carousels.getFocus());
	/*add by dragontec for bug 4316,卖点文字不正确的问题 end*/
		/*add by dragontec for bug 4307,4277 start*/
        holder.mLayout.setFocusableInTouchMode(false);
		/*add by dragontec for bug 4307,4277 end*/
        if (!TextUtils.isEmpty(carousels.video_image)) {
            Picasso.with(mContext).load(carousels.video_image).into(holder.mPosterIg);
        } else {
            Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mPosterIg);
        }
		/*add by dragontec for bug 4325,卖点文字不正确的问题 start*/
        String focusStr = carousels.title;
        if(carousels.getFocus() != null && !carousels.getFocus().equals("") && !carousels.getFocus().equals("null")){
            focusStr = carousels.getFocus();
        }
        holder.mTitle.setTag(new String[]{carousels.title,focusStr});
		/*add by dragontec for bug 4325,卖点文字不正确的问题 end*/
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
		/*add by dragontec for bug 4316 start*/
        public TextView mIntroduction;
		/*add by dragontec for bug 4316 end*/
        public ImageView mPosterIg;
		/*add by dragontec for bug 4307,4277 start*/
        public IsmartvLinearLayout mLayout;
		/*add by dragontec for bug 4307,4277 end*/
		/*add by dragontec for bug 4355 start*/
        public LinearLayout mTextLayout;
		/*add by dragontec for bug 4355 end*/

        public CenterViewHolder(View itemView) {
            super(itemView, CenterAdapter.this);
            mTitle = (TextView) itemView.findViewById(R.id.center_item_title);
			/*add by dragontec for bug 4316 start*/
			mIntroduction = (TextView) itemView.findViewById(R.id.center_item_introduction);
			/*add by dragontec for bug 4316 end*/
            mPosterIg = (ImageView) itemView.findViewById(R.id.center_item_poster);
			/*add by dragontec for bug 4307,4277 start*/
            mLayout = (IsmartvLinearLayout) itemView.findViewById(R.id.center_ismartv_linear_layout);
			/*add by dragontec for bug 4307,4277 end*/
			/*add by dragontec for bug 4355 start*/
            mTextLayout = (LinearLayout)itemView.findViewById(R.id.center_item_text_layout);
			/*add by dragontec for bug 4355 end*/
        }

        @Override
        protected float getScaleXY() {
            return 1.2F;
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.center_ismartv_linear_layout;
        }
		/*add by dragontec for bug 4325 start*/
        @Override
        protected int getTitleId() {
            return R.id.center_item_title;
        }
		/*add by dragontec for bug 4325 end*/
    }
}
