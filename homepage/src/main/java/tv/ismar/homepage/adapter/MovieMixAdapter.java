/*add by dragontec for bug 4362 start*/
package tv.ismar.homepage.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.List;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.entity.banner.BannerPoster;
/*add by dragontec for bug 4245 start*/
import tv.ismar.app.entity.banner.BigImage;
/*add by dragontec for bug 4245 end*/
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.homepage.R;

/**
 * Created by dragontec on 2017/11/6.
 */

public class MovieMixAdapter extends BaseRecycleAdapter<MovieMixAdapter.MovieMixViewHolder> {
	private Context mContext;
	/*add by dragontec for bug 4245 start*/
	private BigImage mBigImage;
	/*add by dragontec for bug 4245 end*/
	private List<BannerPoster> mData;

	public MovieMixAdapter(Context context) {
		mContext = context;
	}

	public MovieMixAdapter(Context context, List<BannerPoster> data){
		this.mContext = context;
		this.mData = data;
	}

	/*add by dragontec for bug 4245 start*/
	public void setBigImage(BigImage bigImage) {
		mBigImage = bigImage;
		if (mBigImage != null) {
			notifyItemInserted(0);
		}
	}

	public BigImage getBigImage() {
		return mBigImage;
	}
	/*add by dragontec for bug 4245 end*/

	public void setData(List<BannerPoster> data){
		if (mData == null) {
			mData = data;
			notifyDataSetChanged();
		}
	}

	public List<BannerPoster> getData() {
		return mData;
	}

	@Override
	public void clearData() {
		if (mData != null) {
			mData = null;
			notifyDataSetChanged();
		}
	}

	@Override
	public MovieMixViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = null;
		if (viewType == TYPE_HEADER) {
			view = LayoutInflater.from(mContext).inflate(R.layout.item_banner_movie_mix_big, parent, false);
		} else if (viewType == TYPE_NORMAL){
			view = LayoutInflater.from(mContext).inflate(R.layout.item_banner_movie_mix, parent, false);
		}
		return new MovieMixViewHolder(view);
	}

	@Override
	public void onBindViewHolder(MovieMixViewHolder holder, int position) {
		holder.imageUrl = null;
		holder.isMore = false;
		holder.isBig = false;
		switch (holder.getItemViewType()) {
			case TYPE_HEADER: {
				holder.itemLayout.setBackgroundResource(android.R.color.transparent);
				holder.mTitle.setVisibility(View.VISIBLE);
				holder.itemWrapper.setVisibility(View.VISIBLE);
				holder.isBig = true;
				if (!TextUtils.isEmpty(mBigImage.poster_url)) {
					holder.imageUrl = mBigImage.poster_url;
					Picasso.with(mContext).load(mBigImage.poster_url).
							placeholder(R.drawable.template_title_item_horizontal_preview).
							error(R.drawable.template_title_item_horizontal_preview).
							tag("banner").
							into(holder.mImageView);
					if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
						Picasso.with(mContext).pauseTag("banner");
					}
				} else {
					Picasso.with(mContext).
							load(R.drawable.template_title_item_horizontal_preview).
							into(holder.mImageView);
				}
				holder.mTitle.setText(mBigImage.title + " ");
				holder.itemLayout.setTag(mBigImage);
				holder.itemLayout.setTag(R.id.banner_item_position, position);

				Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(mBigImage.top_left_corner)).tag("banner").into(holder.markLT);
				Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(mBigImage.top_right_corner)).tag("banner").into(holder.markRT);

				if (mBigImage.rating_average != 0) {
					holder.markRB.setText(new DecimalFormat("0.0").format(mBigImage.rating_average));
					holder.markRB.setVisibility(View.VISIBLE);
				} else {
					holder.markRB.setVisibility(View.INVISIBLE);
				}

				holder.mLeftSpace.setVisibility(View.GONE);
				String focusStr = mBigImage.title;
				if (mBigImage.focus != null && !mBigImage.focus.equals("") && !mBigImage.focus.equals("null")) {
					focusStr = mBigImage.focus;
				}
				holder.mTitle.setTag(new String[]{mBigImage.title, focusStr});
			}
				break;
			case TYPE_NORMAL: {
				if (position == 0) {
					holder.mLeftSpace.setVisibility(View.GONE);
				} else {
					holder.mLeftSpace.setVisibility(View.VISIBLE);
				}
				if (getItemViewType(0) == TYPE_HEADER) {
					position--;
				}
				BannerPoster poster = mData.get(position);
				if (!TextUtils.isEmpty(poster.vertical_url) && poster.vertical_url.equals("更多")) {
					holder.isMore = true;
					holder.itemLayout.setBackgroundResource(R.drawable.banner_vertical_more);
					holder.mTitle.setVisibility(View.INVISIBLE);
					holder.itemWrapper.setVisibility(View.INVISIBLE);
				} else {
					holder.itemLayout.setBackgroundResource(android.R.color.transparent);
					holder.mTitle.setVisibility(View.VISIBLE);
					holder.itemWrapper.setVisibility(View.VISIBLE);
					if (!TextUtils.isEmpty(poster.vertical_url)) {
						holder.imageUrl = poster.vertical_url;
						Picasso.with(mContext).load(poster.vertical_url).
								placeholder(R.drawable.template_title_item_vertical_preview).
								error(R.drawable.template_title_item_vertical_preview).
								tag("banner").
								into(holder.mImageView);
						if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
							Picasso.with(mContext).pauseTag("banner");
						}
					} else {
						Picasso.with(mContext).load(R.drawable.template_title_item_vertical_preview).
								into(holder.mImageView);
					}
				}
				holder.mTitle.setText(poster.title + " ");
				holder.itemLayout.setTag(poster);
				holder.itemLayout.setTag(R.id.banner_item_position, position);

				Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_left_corner)).tag("banner").into(holder.markLT);
				Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_right_corner)).tag("banner").into(holder.markRT);

				if (poster.rating_average != 0) {
					holder.markRB.setText(new DecimalFormat("0.0").format(poster.rating_average));
					holder.markRB.setVisibility(View.VISIBLE);
				} else {
					holder.markRB.setVisibility(View.INVISIBLE);
				}
				String focusStr = poster.title;
				if (poster.focus != null && !poster.focus.equals("") && !poster.focus.equals("null")) {
					focusStr = poster.focus;
				}
				holder.mTitle.setTag(new String[]{poster.title, focusStr});
			}
				break;
		}
	}

	@Override
	public int getItemCount() {
		/*modify by dragontec for bug 4245 start*/
		int count = 0;
		if (mBigImage != null) {
			count++;
		}
		if (mData != null) {
			count += mData.size();
		}
		return count;
		/*modify by dragontec for bug 4245 end*/
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0 && mBigImage != null) {
			return TYPE_HEADER;
		} else {
			return TYPE_NORMAL;
		}
	}

	public class MovieMixViewHolder extends BaseViewHolder {
		private final RecyclerImageView markRT;
		private Space mLeftSpace;
		private RecyclerImageView mImageView;
		private TextView mTitle;
		private View mItemView;
		private LinearLayout itemLayout;
		private RelativeLayout itemWrapper;
		private RecyclerImageView markLT;
		private TextView markRB;
		private String imageUrl;
		private boolean isMore;
		private boolean isBig;

		public MovieMixViewHolder(View itemView) {
			super(itemView, MovieMixAdapter.this);
			mItemView = itemView;
			itemLayout = (LinearLayout) mItemView.findViewById(R.id.item_layout);
			itemWrapper = (RelativeLayout) mItemView.findViewById(R.id.item_wrapper);
			mImageView = (RecyclerImageView) itemView.findViewById(R.id.image_view);
			mTitle = (TextView) itemView.findViewById(R.id.title);
			mLeftSpace = (Space)itemView.findViewById(R.id.left_space);
			markLT = (RecyclerImageView) itemView.findViewById(R.id.banner_mark_lt);
			markRB = (TextView)itemView.findViewById(R.id.banner_mark_br);
			markRT = (RecyclerImageView) itemView.findViewById(R.id.banner_mark_rt);
		}

		@Override
		public void clearImage() {
			super.clearImage();
			if (isMore) {
				//do nothing
			} else {
				if (itemLayout != null) {
					itemLayout.setBackgroundResource(R.drawable.transparent);
				}
				if (isBig) {
					Picasso.with(mContext).load(R.drawable.template_title_item_horizontal_preview).
							into(mImageView);
				} else {
					Picasso.with(mContext).load(R.drawable.template_title_item_vertical_preview).
							into(mImageView);
				}
			}
		}

		@Override
		public void restoreImage() {
			if (isMore) {
				//do nothing
			} else {
				if (itemLayout != null) {
					itemLayout.setBackgroundResource(R.drawable.transparent);
				}
				if (mImageView != null) {
					if (isBig) {
						if (imageUrl != null) {
							Picasso.with(mContext).load(imageUrl).
									placeholder(R.drawable.template_title_item_horizontal_preview).
									error(R.drawable.template_title_item_horizontal_preview).
									tag("banner").
									into(mImageView);
						} else {
							Picasso.with(mContext).load(R.drawable.template_title_item_horizontal_preview).
									into(mImageView);
						}
					} else {
						if (imageUrl != null) {
							Picasso.with(mContext).load(imageUrl).
									placeholder(R.drawable.template_title_item_vertical_preview).
									error(R.drawable.template_title_item_vertical_preview).
									tag("banner").
									into(mImageView);
						} else {
							Picasso.with(mContext).load(R.drawable.template_title_item_vertical_preview).
									into(mImageView);
						}
					}
				}
			}
			super.restoreImage();
		}

		@Override
		protected int getScaleLayoutId() {
			return R.id.item_layout;
		}

		@Override
		protected int getTitleId() {
			return R.id.title;
		}
	}
}
/*add by dragontec for bug 4362 end*/