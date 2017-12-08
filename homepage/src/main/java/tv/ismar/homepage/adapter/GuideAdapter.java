package tv.ismar.homepage.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.widget.RecyclerImageView;
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

	/*add by dragontec for bug 4334 start*/
    public GuideAdapter(Context context) {
    	mContext = context;
	}
	/*add by dragontec for bug 4334 end*/

    public GuideAdapter(Context context, List<BannerPoster> data){
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

	public List<BannerPoster> getData() {
    	return mData;
	}
	/*add by dragontec for bug 4334 end*/

	@Override
	public void clearData() {
		if (mData != null) {
			mData = null;
			notifyDataSetChanged();
		}
	}

    @Override
    public GuideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_guide_item,parent,false);
        return new GuideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GuideViewHolder holder, int position) {
		holder.imageUrl = null;
		holder.isMore = false;
        holder.mMarginLeftView.setVisibility(mMarginLeftEnable?View.VISIBLE:View.GONE);
//        if(position==0) holder.mMarginLeftView.setVisibility(View.GONE);
		/*modify by dragontec for bug 4334 start*/
		if (mData != null) {
			/*modify by dragontec for bug 4362 start*/
			BannerPoster poster = mData.get(holder.getAdapterPosition());
			/*modify by dragontec for bug 4362 end*/
			if (!TextUtils.isEmpty(poster.vertical_url)) {
				if (poster.vertical_url.equals("更多")) {
					holder.isMore = true;
					Picasso.with(mContext).load(R.drawable.banner_vertical_more).into(holder.mPosterIg);
				} else {
					holder.imageUrl = poster.vertical_url;
/*modify by dragontec for bug 4336,4407 start*/
					Picasso.with(mContext).load(poster.vertical_url).
/*add by dragontec for bug 4205 start*/
                            memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).
/*add by dragontec for bug 4205 end*/
                            error(R.drawable.template_item_vertical_preview).
                            placeholder(R.drawable.template_item_vertical_preview).
							tag("banner").
                            into(holder.mPosterIg);
/*modify by dragontec for bug 4336,4407 end*/
					if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
						Picasso.with(mContext).pauseTag("banner");
					}
				}
			} else {
/*modify by dragontec for bug 4336,4407 start*/
				Picasso.with(mContext).
                        load(R.drawable.template_item_vertical_preview).
                        into(holder.mPosterIg);
/*modify by dragontec for bug 4336,4407 end*/
			}
			Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_left_corner)).into(holder.mLtIconTv);
			holder.mRbIconTv.setText(new DecimalFormat("0.0").format(poster.rating_average));
			holder.mRbIconTv.setVisibility((poster.rating_average == 0) ? View.GONE : View.VISIBLE);
			Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_right_corner)).into(holder.mRtIconTv);

			if (!TextUtils.isEmpty(poster.vertical_url) && poster.vertical_url.equals("更多")) {
				holder.mTitleTv.setVisibility(View.INVISIBLE);
			} else {
				holder.mTitleTv.setVisibility(View.VISIBLE);
			}
			/*add by dragontec for bug 4325 start*/
			String title = poster.title;
			holder.mTitleTv.setText(title);

			String focusStr = title;
			if (poster.focus != null && !poster.focus.equals("") && !poster.focus.equals("null")) {
				focusStr = poster.focus;
			}
			holder.mTitleTv.setTag(new String[]{title, focusStr});
			/*add by dragontec for bug 4325 end*/
		}
		/*modify by dragontec for bug 4334 end*/
    }

	@Override
	public int getItemViewType(int position) {
		return TYPE_NORMAL;
	}

    @Override
    public int getItemCount() {
        return (mData!=null) ? mData.size() : 0;
    }

    public void setMarginLeftEnable(boolean enable){
        this.mMarginLeftEnable = enable;
    }

    public class GuideViewHolder extends BaseViewHolder {
        public RecyclerImageView mPosterIg;//海报
        public RecyclerImageView mLtIconTv;//左上icon
        public TextView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public View mMarginLeftView;//左边距
        public RecyclerImageView mRtIconTv;//右上icon
		public String imageUrl;
		public boolean isMore;

        public GuideViewHolder(View itemView) {
            super(itemView, GuideAdapter.this);
            mPosterIg = (RecyclerImageView) itemView.findViewById(R.id.guide_recycle_item_poster);
            mLtIconTv = (RecyclerImageView) itemView.findViewById(R.id.guide_recycle_item_lt_icon);
            mRbIconTv = (TextView) itemView.findViewById(R.id.guide_recycle_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.guide_recycle_item_title);
            mMarginLeftView = itemView.findViewById(R.id.guide_margin_left);
            mRtIconTv= (RecyclerImageView) itemView.findViewById(R.id.guide_rt_icon);
        }

		@Override
		public void clearImage() {
			super.clearImage();
			if (mPosterIg != null) {
				if (isMore) {
					//do nothing
				} else {
					Picasso.with(mContext).
							load(R.drawable.template_item_vertical_preview).
							into(mPosterIg);
				}
			}
		}

		@Override
		public void restoreImage() {
			if (mPosterIg != null) {
				if (isMore) {
					//do nothing
				} else if (imageUrl != null) {
					Picasso.with(mContext).load(imageUrl).
							memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).
							error(R.drawable.template_item_vertical_preview).
							placeholder(R.drawable.template_item_vertical_preview).
							tag("banner").
							into(mPosterIg);
					if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
						Picasso.with(mContext).pauseTag("banner");
					}
				} else {
					Picasso.with(mContext).
							load(R.drawable.template_item_vertical_preview).
							into(mPosterIg);
				}
			}
			super.restoreImage();
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
