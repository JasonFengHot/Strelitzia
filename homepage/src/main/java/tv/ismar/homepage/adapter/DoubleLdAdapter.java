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
import tv.ismar.app.entity.banner.BigImage;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 横版双行适配器
 */

public class DoubleLdAdapter extends BaseRecycleAdapter<DoubleLdAdapter.DoubleLdViewHolder>{

    private Context mContext;
    private List<BannerPoster> mData;
    private BigImage mBigImage;

	/*add by dragontec for bug 4334 start*/
    public DoubleLdAdapter(Context context) {
    	mContext = context;
	}
	/*add by dragontec for bug 4334 end*/

    public DoubleLdAdapter(Context context, List<BannerPoster> data){
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

	public List<BannerPoster> getData(){
		return mData;
	}

	public void setBigImage(BigImage bigImage) {
		mBigImage = bigImage;
		notifyDataSetChanged();
	}

	public BigImage getBigImage() {
		return mBigImage;
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
    public DoubleLdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = null;
		if (viewType == TYPE_HEADER) {
			view = LayoutInflater.from(mContext).inflate(R.layout.banner_double_ld_head, parent,false);
		} else if (viewType == TYPE_NORMAL){
			view = LayoutInflater.from(mContext).inflate(R.layout.banner_double_ld_item, parent,false);
		}
		return new DoubleLdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DoubleLdViewHolder holder, int position) {
		holder.imageUrl = null;
		holder.isMore = false;
		switch (holder.getItemViewType()) {
			case TYPE_HEADER: {
				if (mBigImage != null) {
					if (!TextUtils.isEmpty(mBigImage.poster_url)) {
						holder.imageUrl = mBigImage.poster_url;
						Picasso.with(mContext).
								load(mBigImage.poster_url).
								memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).
								error(R.drawable.template_title_item_horizontal_preview).
								placeholder(R.drawable.template_title_item_horizontal_preview).
								tag("banner").
								into(holder.mPosterIg);
						if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
							Picasso.with(mContext).pauseTag("banner");
						}
					} else {
						Picasso.with(mContext).
								load(R.drawable.template_title_item_horizontal_preview).
								into(holder.mPosterIg);
					}
					Picasso.with(mContext).
							load(VipMark.getInstance().getBannerIconMarkImage(mBigImage.top_left_corner)).
							into(holder.mLtIconTv);
					Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(mBigImage.top_right_corner)).into(holder.mRtIconTv);
					holder.mRbIconTv.setText(new DecimalFormat("0.0").format(mBigImage.rating_average));
					holder.mRbIconTv.setVisibility((mBigImage.rating_average == 0) ? View.GONE : View.VISIBLE);
					holder.mTitleTv.setText(mBigImage.title);
					String focusStr = mBigImage.title;
					if (mBigImage.focus != null && !mBigImage.focus.equals("") && !mBigImage.focus.equals("null")) {
						focusStr = mBigImage.focus;
					}
					holder.mTitleTv.setTag(new String[]{mBigImage.title, focusStr});
				}
			}
				break;
			case TYPE_NORMAL:
			{
				if (position != 0 && getItemViewType(0) == TYPE_HEADER) {
					position--;
				}
				if (mData != null && position < mData.size()) {
					BannerPoster poster = mData.get(position);
					if (!TextUtils.isEmpty(poster.poster_url)) {
						if (poster.poster_url.equals("更多")) {
							holder.isMore = true;
							Picasso.with(mContext).load(R.drawable.banner_horizontal_more).into(holder.mPosterIg);
						} else {
							holder.imageUrl = poster.poster_url;
							Picasso.with(mContext).load(poster.poster_url).
									memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).
									error(R.drawable.template_title_item_horizontal_preview).
									placeholder(R.drawable.template_title_item_horizontal_preview).
									tag("banner").
									into(holder.mPosterIg);
							if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
								Picasso.with(mContext).pauseTag("banner");
							}
						}
					} else {
						Picasso.with(mContext).
								load(R.drawable.template_title_item_horizontal_preview).
								into(holder.mPosterIg);
					}
					Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_left_corner)).into(holder.mLtIconTv);
					holder.mRbIconTv.setText(new DecimalFormat("0.0").format(poster.rating_average));
					holder.mRbIconTv.setVisibility((poster.rating_average == 0) ? View.GONE : View.VISIBLE);
					if (!TextUtils.isEmpty(poster.poster_url) && poster.poster_url.equals("更多")) {
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
					Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_right_corner)).into(holder.mRtIconTv);
				}
			}
				break;
		}
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
		return count;
    }

    public List<BannerPoster> getmData() {
        return mData;
    }

    public static final int TYPE_HEADER = 0;//头部
    public static final int TYPE_NORMAL = 1;//一般item

    @Override
    public int getItemViewType(int position) {
    	if (position == 0 && mBigImage != null) {
    		return TYPE_HEADER;
		}
		return TYPE_NORMAL;
    }

    public class DoubleLdViewHolder extends BaseViewHolder{
        public RecyclerImageView mPosterIg;//海报
        public RecyclerImageView mLtIconTv;//左上icon
        public TextView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public RecyclerImageView mRtIconTv;
        public String imageUrl;
        public boolean isMore;

        public DoubleLdViewHolder(View itemView) {
            super(itemView, DoubleLdAdapter.this);
            mPosterIg = (RecyclerImageView) itemView.findViewById(R.id.double_ld_item_poster);
            mLtIconTv = (RecyclerImageView) itemView.findViewById(R.id.double_ld_item_lt_icon);
            mRbIconTv = (TextView) itemView.findViewById(R.id.double_ld_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.double_ld_item_title);
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
							load(R.drawable.template_title_item_horizontal_preview).
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
							error(R.drawable.template_title_item_horizontal_preview).
							placeholder(R.drawable.template_title_item_horizontal_preview).
							tag("banner").
							into(mPosterIg);
					if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
						Picasso.with(mContext).pauseTag("banner");
					}
				} else {
					Picasso.with(mContext).
							load(R.drawable.template_title_item_horizontal_preview).
							into(mPosterIg);
				}
			}
			super.restoreImage();
		}

		@Override
        protected int getScaleLayoutId() {
            return R.id.double_ld_ismartv_linear_layout;
        }
		/*add by dragontec for bug 4325 start*/
        @Override
        protected int getTitleId() {
            return R.id.double_ld_item_title;
        }
		/*add by dragontec for bug 4325 end*/
    }
}
