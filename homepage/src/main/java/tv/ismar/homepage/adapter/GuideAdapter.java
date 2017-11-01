package tv.ismar.homepage.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/30
 * @DESC: 导视recycleview适配器
 */

public class GuideAdapter extends BaseRecycleAdapter<GuideAdapter.GuideViewHolder>{
    private Context mContext;
    private List<BannerPoster> mData;
    private boolean mMarginLeftEnable = false;

    public GuideAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public GuideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_guide_item,parent,false);
        return new GuideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GuideViewHolder holder, int position) {
        holder.mMarginLeftView.setVisibility(mMarginLeftEnable?View.VISIBLE:View.GONE);
//        if(position==0) holder.mMarginLeftView.setVisibility(View.GONE);
        BannerPoster poster = mData.get(position);
        if (!TextUtils.isEmpty(poster.vertical_url)) {
            if(poster.vertical_url.equals("更多")){
                Picasso.with(mContext).load(R.drawable.banner_vertical_more).into(holder.mPosterIg);
            } else {
                Picasso.with(mContext).load(poster.vertical_url).error(R.drawable.list_item_ppreview_bg).into(holder.mPosterIg);
            }
        } else {
            Picasso.with(mContext).load(R.drawable.list_item_ppreview_bg).into(holder.mPosterIg);
        }
        Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_left_corner)).into(holder.mLtIconTv);
        holder.mRbIconTv.setText(new DecimalFormat("0.0").format(poster.rating_average));
        holder.mRbIconTv.setVisibility((poster.rating_average==0) ? View.GONE:View.VISIBLE);
        Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_right_corner)).into(holder.mRtIconTv);

        if(!TextUtils.isEmpty(poster.vertical_url) && poster.vertical_url.equals("更多")){
            holder.mTitleTv.setVisibility(View.INVISIBLE);
        } else {
            holder.mTitleTv.setVisibility(View.VISIBLE);
        }
		/*add by dragontec for bug 4325 start*/
        String title = poster.title;
        holder.mTitleTv.setText(title);
        holder.mPosition = position;
		
        String focusStr = title;
        if(poster.focus != null && !poster.focus.equals("") && !poster.focus.equals("null")){
            focusStr = poster.focus;
        }
        holder.mTitleTv.setTag(new String[]{title,focusStr});
		/*add by dragontec for bug 4325 end*/
    }

    @Override
    public int getItemCount() {
        return (mData!=null) ? mData.size() : 0;
    }

    public void setMarginLeftEnable(boolean enable){
        this.mMarginLeftEnable = enable;
    }

    public class GuideViewHolder extends BaseViewHolder {
        public ImageView mPosterIg;//海报
        public ImageView mLtIconTv;//左上icon
        public TextView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public View mMarginLeftView;//左边距
        public ImageView mRtIconTv;//右上icon

        public GuideViewHolder(View itemView) {
            super(itemView, GuideAdapter.this);
            mPosterIg = (ImageView) itemView.findViewById(R.id.guide_recycle_item_poster);
            mLtIconTv = (ImageView) itemView.findViewById(R.id.guide_recycle_item_lt_icon);
            mRbIconTv = (TextView) itemView.findViewById(R.id.guide_recycle_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.guide_recycle_item_title);
            mMarginLeftView = itemView.findViewById(R.id.guide_margin_left);
            mRtIconTv= (ImageView) itemView.findViewById(R.id.guide_rt_icon);
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.guide_ismartv_linear_layout;
        }
		/*add by dragontec for bug 4325 start*/
        @Override
        protected int getTitleId() {
            return R.id.guide_recycle_item_title;
        }
		/*add by dragontec for bug 4325 end*/
    }
}
