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

import java.util.List;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/15
 * @DESC: 说明
 */

public class RecommendAdapter extends BaseRecycleAdapter<RecommendAdapter.RecommendViewHolder>{

    private Context mContext;
    private List<BannerPoster> mData;

	/*add by dragontec for bug 4334 start*/
    public RecommendAdapter(Context context) {
    	mContext = context;
	}
	/*add by dragontec for bug 4334 end*/

    public RecommendAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

	/*add by dragontec for bug 4334 start*/
    public void setData(List<BannerPoster> data) {
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
    public RecommendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_recommend_item,parent,false);
        return new RecommendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecommendViewHolder holder, int position) {
		holder.imageUrl = null;
    	/*modify by dragontec for bug 4334 start*/
    	if (mData != null) {
			BannerPoster poster = mData.get(position);
			holder.mTitle.setText(poster.title);
			if (!TextUtils.isEmpty(poster.poster_url)) {
				holder.imageUrl = poster.poster_url;
/*modify by dragontec for bug 4336 start*/
				Picasso.with(mContext).load(poster.poster_url).
                        error(R.drawable.template_item_horizontal_preview).
                        placeholder(R.drawable.template_item_horizontal_preview).
						tag("banner").
                        into(holder.mPoster);
/*modify by dragontec for bug 4336 end*/
				if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
					Picasso.with(mContext).pauseTag("banner");
				}
			} else {
/*modify by dragontec for bug 4336 start*/
				Picasso.with(mContext).
                        load(R.drawable.template_item_horizontal_preview).
                        into(holder.mPoster);
/*modify by dragontec for bug 4336 end*/
			}
			Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_right_corner)).into(holder.mRtIconTv);
			/*add by dragontec for bug 4325 start*/
			String focusStr = poster.title;
			if (poster.focus != null && !poster.focus.equals("") && !poster.focus.equals("null")) {
				focusStr = poster.focus;
			}
			holder.mTitle.setTag(new String[]{poster.title, focusStr});
			/*add by dragontec for bug 4325 end*/
			/*modify by dragontec for bug 4434 start*/
            if (position == 0) {
                holder.mLeftSpace.setVisibility(View.GONE);
            } else {
                holder.mLeftSpace.setVisibility(View.VISIBLE);
            }
			/*modify by dragontec for bug 4434 end*/
		}
		/*modify by dragontec for bug 4334 end*/
    }

	@Override
	public int getItemViewType(int position) {
		return TYPE_NORMAL;
	}

    @Override
    public int getItemCount() {
        return (mData!=null)? mData.size() : 0;
    }

    public class RecommendViewHolder extends BaseViewHolder{

        public View mLeftSpace;
        public TextView mTitle;
        public RecyclerImageView mPoster;
        public RecyclerImageView mRtIconTv;
        public String imageUrl;

        public RecommendViewHolder(View itemView) {
            super(itemView, RecommendAdapter.this);
            mTitle = (TextView) itemView.findViewById(R.id.recommend_item_tv);
            mPoster = (RecyclerImageView) itemView.findViewById(R.id.recommend_item_poster);
            mRtIconTv= (RecyclerImageView) itemView.findViewById(R.id.guide_rt_icon);
            mLeftSpace= itemView.findViewById(R.id.left_space);
        }

		@Override
		public void clearImage() {
			super.clearImage();
			if (mPoster != null) {
				Picasso.with(mContext).
						load(R.drawable.template_item_horizontal_preview).
						into(mPoster);
			}
		}

		@Override
		public void restoreImage() {
        	if (mPoster != null) {
        		if (imageUrl != null) {
					Picasso.with(mContext).load(imageUrl).
							error(R.drawable.template_item_horizontal_preview).
							placeholder(R.drawable.template_item_horizontal_preview).
							tag("banner").
							into(mPoster);
				} else {
					Picasso.with(mContext).
							load(R.drawable.template_item_horizontal_preview).
							into(mPoster);
				}
			}
			super.restoreImage();
		}

		@Override
        protected int getScaleLayoutId() {
            return R.id.recommend_ismartv_linear_layout;
        }
		/*add by dragontec for bug 4325 start*/
        @Override
        protected int getTitleId() {
            return R.id.recommend_item_tv;
        }
		/*add by dragontec for bug 4325 end*/
    }
}
