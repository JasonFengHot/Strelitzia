package tv.ismar.homepage.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 电视剧适配器
 */

public class TvPlayAdapter extends BaseRecycleAdapter<TvPlayAdapter.TvPlayerViewHolder>{

    private Context mContext;
    private List<BannerPoster> mData;
    private boolean mMarginLeftEnable = false;

	/*add by dragontec for bug 4334 start*/
    public TvPlayAdapter(Context context) {
    	mContext = context;
	}
	/*add by dragontec for bug 4334 end*/

    public TvPlayAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

	/*add by dragontec for bug 4334 start*/
    public void setData(List<BannerPoster> data){
    	if (mData == null) {
    		mData = data;
    		notifyDataSetChanged();
		}
	}
	/*add by dragontec for bug 4334 end*/

	/*modify by dragontec for bug 4334 start*/
    public List<BannerPoster> getData() {
        return mData;
    }
    /*modify by dragontec for bug 4334 end*/

    @Override
    public TvPlayAdapter.TvPlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_tv_player_item,parent,false);
        return new TvPlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TvPlayerViewHolder holder, int position) {
        holder.mPosition = position;
        holder.mMarginLeftView.setVisibility(mMarginLeftEnable? View.VISIBLE:View.GONE);
        BannerPoster poster = mData.get(position);
        if (!TextUtils.isEmpty(poster.poster_url)) {
             if(poster.poster_url.equals("更多")){
                 Picasso.with(mContext).load(R.drawable.banner_horizontal_more).into(holder.mPosterIg);
             } else {
/*modify by dragontec for bug 4336 start*/
                 Picasso.with(mContext).load(poster.poster_url).
                         error(R.drawable.template_title_item_horizontal_preview).
                         placeholder(R.drawable.template_title_item_horizontal_preview).
                         into(holder.mPosterIg);
/*modify by dragontec for bug 4336 end*/
             }
        } else {
/*modify by dragontec for bug 4336 start*/
            Picasso.with(mContext).
                    load(R.drawable.template_title_item_horizontal_preview).
                    into(holder.mPosterIg);
/*modify by dragontec for bug 4336 end*/
        }
        Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_right_corner)).into(holder.mRtIconTv);
        Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_left_corner)).into(holder.mLtIconTv);
        holder.mRbIconTv.setText(new DecimalFormat("0.0").format(poster.rating_average));
        holder.mRbIconTv.setVisibility((poster.rating_average==0) ? View.GONE:View.VISIBLE);
        if(!TextUtils.isEmpty(poster.poster_url) && poster.poster_url.equals("更多")){
            holder.mTitleTv.setVisibility(View.INVISIBLE);
        } else {
            holder.mTitleTv.setVisibility(View.VISIBLE);
        }
		/*add by dragontec for bug 4325 start*/
        String focusStr = poster.title;
        if(poster.focus != null && !poster.focus.equals("") && !poster.focus.equals("null")){
            focusStr = poster.focus;
        }
        holder.mTitleTv.setTag(new String[]{poster.title,focusStr});
		/*add by dragontec for bug 4325 end*/
        holder.mTitleTv.setText(poster.title);
    }

    @Override
    public int getItemCount() {
        return (mData!=null)? mData.size():0;
    }

    public void setMarginLeftEnable(boolean enable){
        this.mMarginLeftEnable = enable;
    }

    public class TvPlayerViewHolder extends BaseViewHolder{
        public RecyclerImageView mPosterIg;//海报
        public RecyclerImageView mLtIconTv;//左上icon
        public TextView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public View mMarginLeftView;//左边距
        public RecyclerImageView mRtIconTv;

        public TvPlayerViewHolder(View itemView) {
            super(itemView, TvPlayAdapter.this);
            mPosterIg = (RecyclerImageView) itemView.findViewById(R.id.tv_player_item_poster);
            mLtIconTv = (RecyclerImageView) itemView.findViewById(R.id.tv_player_item_lt_icon);
            mRbIconTv = (TextView) itemView.findViewById(R.id.tv_player_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.tv_player_item_title);
            mMarginLeftView = itemView.findViewById(R.id.tv_player_margin_left);
            mRtIconTv= (RecyclerImageView) itemView.findViewById(R.id.guide_rt_icon);
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.tv_player_ismartv_linear_layout;
        }
		/*add by dragontec for bug 4325 start*/
        @Override
        protected int getTitleId() {
            return R.id.tv_player_item_title;
        }
		/*add by dragontec for bug 4325 end*/
    }
}
