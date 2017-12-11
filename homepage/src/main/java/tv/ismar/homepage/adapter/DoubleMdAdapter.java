package tv.ismar.homepage.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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
import tv.ismar.app.entity.banner.BigImage;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 竖版双行适配器
 */

public class DoubleMdAdapter extends BaseRecycleAdapter<DoubleMdAdapter.DoubleMdViewHolder>{

    private Context mContext = null;
    private List<BannerPoster> mData = null;
	private BigImage mBigImage = null;

	/*add by dragontec for bug 4334 start*/
    public DoubleMdAdapter(Context context) {
    	mContext = context;
	}
	/*add by dragontec for bug 4334 end*/

    public DoubleMdAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

	/*add by dragontec for bug 4334 start*/
	public void setBigImage(BigImage bigImage) {
		mBigImage = bigImage;
		notifyDataSetChanged();
	}

	public BigImage getBigImage() {
		return mBigImage;
	}


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
    public DoubleMdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = null;
		if (viewType == TYPE_HEADER) {
			view = LayoutInflater.from(mContext).inflate(R.layout.banner_double_md_head, parent, false);
		} else if (viewType == TYPE_NORMAL){
			view = LayoutInflater.from(mContext).inflate(R.layout.banner_double_md_item, parent,false);
		}
		return new DoubleMdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DoubleMdViewHolder holder, int position) {
		Log.d(TAG, "onBindViewHolder holder = " + holder +", position = " + position);
		holder.imageUrl = null;
		holder.isMore = false;

		switch (holder.getItemViewType()) {
			case TYPE_HEADER:
			{
				if (!TextUtils.isEmpty(mBigImage.vertical_url)) {
					holder.imageUrl = mBigImage.vertical_url;
					Picasso.with(mContext).
							load(mBigImage.vertical_url).
							memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).
							error(R.drawable.template_title_item_vertical_preview).
							placeholder(R.drawable.template_title_item_vertical_preview).
							tag("banner").
							into(holder.mPosterIg);
					if (mScrollState != RecyclerView.SCROLL_STATE_IDLE|| isParentScrolling) {
						Picasso.with(mContext).pauseTag("banner");
					}
				} else {
					Picasso.with(mContext).
							load(R.drawable.template_title_item_vertical_preview).
							tag("banner").
							into(holder.mPosterIg);
				}
				Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(mBigImage.top_right_corner)).tag("banner").into(holder.mRtIconTv);
				holder.mTitleTv.setText(mBigImage.title);
				String focusStr = mBigImage.title;
				if (mBigImage.focus != null && !mBigImage.focus.equals("") && !mBigImage.focus.equals("null")) {
					focusStr = mBigImage.focus;
				}
				holder.mTitleTv.setTag(new String[]{mBigImage.title, focusStr});
				break;
			}
			case TYPE_NORMAL:
			{
				if (getItemViewType(0) == TYPE_HEADER) {
					position--;
				}
				if (mData != null && position < mData.size()) {
					BannerPoster poster = mData.get(position);
					holder.mTitleTv.setText(poster.title);
					if (!TextUtils.isEmpty(poster.vertical_url)) {
						if (poster.vertical_url.equals("更多")) {
							holder.isMore = true;
							Picasso.with(mContext).load(R.drawable.banner_vertical_more).into(holder.mPosterIg);
						} else {
							holder.imageUrl = poster.vertical_url;
							Picasso.with(mContext).load(poster.vertical_url).
									memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).
									error(R.drawable.template_title_item_vertical_preview).
									placeholder(R.drawable.template_title_item_vertical_preview).
									tag("banner").
									into(holder.mPosterIg);
							if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
								Picasso.with(mContext).pauseTag("banner");
							}
						}
					} else {
						Picasso.with(mContext).
								load(R.drawable.template_title_item_vertical_preview).
								into(holder.mPosterIg);
					}
					Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_right_corner)).into(holder.mRtIconTv);
					Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_left_corner)).into(holder.mLtIconTv);
					holder.mRbIconTv.setText(new DecimalFormat("0.0").format(poster.rating_average));
					holder.mRbIconTv.setVisibility((poster.rating_average == 0) ? View.GONE : View.VISIBLE);
					if (!TextUtils.isEmpty(poster.vertical_url) && poster.vertical_url.equals("更多")) {
						holder.mTitleTv.setVisibility(View.INVISIBLE);
					} else {
						holder.mTitleTv.setVisibility(View.VISIBLE);
					}
					holder.mTitleTv.setText(poster.title);
					String focusStr = poster.title;
					if (poster.focus != null && !poster.focus.equals("") && !poster.focus.equals("null")) {
						focusStr = poster.focus;
					}
					holder.mTitleTv.setTag(new String[]{poster.title, focusStr});
				}
			}
			break;
		}
    }

	@Override
	public int getItemViewType(int position) {
		Log.d(TAG, "getItemViewType pos = " + position);
		if (position == 0 && mBigImage != null) {
			return TYPE_HEADER;
		}
		return TYPE_NORMAL;
	}

    @Override
    public int getItemCount() {
		int count = 0;
		if (mBigImage != null) {
			count++;
		}
		if (mData != null) {
			count += mData.size();
		}
		Log.d(TAG, "getItemCount = " + count);
        return count;
    }

    public class DoubleMdViewHolder extends BaseViewHolder {
        public RecyclerImageView mPosterIg;//海报
        public RecyclerImageView mLtIconTv;//左上icon
        public TextView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public RecyclerImageView mRtIconTv;
        public String imageUrl;
        public boolean isMore;

        public DoubleMdViewHolder(View itemView) {
            super(itemView, DoubleMdAdapter.this);
			mPosterIg = (RecyclerImageView) itemView.findViewById(R.id.double_md_item_poster);
			mLtIconTv = (RecyclerImageView) itemView.findViewById(R.id.double_md_item_lt_icon);
			mRbIconTv = (TextView) itemView.findViewById(R.id.double_md_item_rb_icon);
			mTitleTv = (TextView) itemView.findViewById(R.id.double_md_item_title);
			mRtIconTv = (RecyclerImageView) itemView.findViewById(R.id.guide_rt_icon);
        }

		@Override
		public void clearImage() {
			super.clearImage();
			if (mPosterIg != null) {
					if (isMore) {
						//do nothing
					} else {
						Picasso.with(mContext).
								load(R.drawable.template_title_item_vertical_preview).
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
							error(R.drawable.template_title_item_vertical_preview).
							placeholder(R.drawable.template_title_item_vertical_preview).
							tag("banner").
							into(mPosterIg);
					if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
						Picasso.with(mContext).pauseTag("banner");
					}
				} else {
					Picasso.with(mContext).
							load(R.drawable.template_title_item_vertical_preview).
							into(mPosterIg);
				}
			}
			super.restoreImage();
		}

		@Override
        protected int getScaleLayoutId() {
            return R.id.double_md_ismartv_linear_layout;
        }
		/*add by dragontec for bug 4325 start*/
        @Override
        protected int getTitleId() {
            return R.id.double_md_item_title;
        }
		/*add by dragontec for bug 4325 end*/
    }
}
