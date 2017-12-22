/*add by dragontec for bug 4362 start*/
package tv.ismar.homepage.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.homepage.R;
import tv.ismar.homepage.banner.IsmartvLinearLayout;

/**
 * Created by dragontec on 2017/11/6.
 */

public class Horizontal519Adapter extends BaseRecycleAdapter<Horizontal519Adapter.Horizontal519ViewHolder> {
	private Context mContext;
	private List<BannerPoster> mData;

	public Horizontal519Adapter(Context context) {
		mContext = context;
	}

	public Horizontal519Adapter(Context context, List<BannerPoster> data){
		this.mContext = context;
		this.mData = data;
	}

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
	public Horizontal519ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner_horizontal_519, parent, false);
		return new Horizontal519ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(Horizontal519ViewHolder holder, int position) {
		holder.imageUrl = null;
		holder.isMore = false;
		if (mData != null) {
			BannerPoster poster = mData.get(position);

			String title = poster.title;

			if (!TextUtils.isEmpty(poster.vertical_url) && poster.vertical_url.equals("更多")) {
				holder.isMore = true;
				holder.mItemLayout.setBackgroundResource(R.drawable.banner_horizontal_more);
				holder.mTitle.setVisibility(View.INVISIBLE);
				holder.mContentLayout.setVisibility(View.INVISIBLE);
			} else {
				holder.mItemLayout.setBackgroundResource(android.R.color.transparent);
				holder.mTitle.setVisibility(View.VISIBLE);
				holder.mContentLayout.setVisibility(View.VISIBLE);
				if (!TextUtils.isEmpty(poster.poster_url)) {
					holder.imageUrl = poster.poster_url;
					Picasso.with(mContext).load(poster.poster_url).
							placeholder(R.drawable.template_title_item_horizontal_preview).
							error(R.drawable.template_title_item_horizontal_preview).
							tag("banner").
							into(holder.mImageView);
					if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
						Picasso.with(mContext).pauseTag("banner");
					}
				} else {
					Picasso.with(mContext).load(R.drawable.template_title_item_horizontal_preview).
							into(holder.mImageView);
				}
			}

			holder.mTitle.setText(poster.title + " ");
			holder.mItemLayout.setTag(poster);
			holder.mItemLayout.setTag(R.id.banner_item_position, position);

			Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_left_corner)).tag("banner").into(holder.markLT);
			Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_right_corner)).tag("banner").into(holder.markRT);

			if (poster.rating_average != 0) {
				holder.markRB.setText(new DecimalFormat("0.0").format(poster.rating_average));
				holder.markRB.setVisibility(View.VISIBLE);
			} else {
				holder.markRB.setVisibility(View.INVISIBLE);
			}


			if (position == 0) {
				holder.mLeftSpace.setVisibility(View.GONE);
			} else {
				holder.mLeftSpace.setVisibility(View.VISIBLE);
			}
			String focusStr = title;
			if (poster.focus != null && !poster.focus.equals("") && !poster.focus.equals("null")) {
				focusStr = poster.focus;
			}
			holder.mTitle.setTag(new String[]{title, focusStr});
		}
	}

	@Override
	public int getItemViewType(int position) {
		return TYPE_NORMAL;
	}

	@Override
	public int getItemCount() {
		return (mData!=null) ? mData.size() : 0;
	}
	
	public class Horizontal519ViewHolder extends BaseViewHolder {
		private Space mLeftSpace;
		private RecyclerImageView mImageView;
		private TextView mTitle;
		private RecyclerImageView markLT;
		private TextView markRB;
		private RecyclerImageView markRT;
		private IsmartvLinearLayout mItemLayout;
		private RelativeLayout mContentLayout;
		private String imageUrl;
		private boolean isMore;

		public Horizontal519ViewHolder(View itemView) {
			super(itemView, Horizontal519Adapter.this);
			mItemLayout = (IsmartvLinearLayout) itemView.findViewById(R.id.item_layout);
			mContentLayout = (RelativeLayout) itemView.findViewById(R.id.content_layout);
			mImageView = (RecyclerImageView) itemView.findViewById(R.id.image_view);
			mTitle = (TextView) itemView.findViewById(R.id.title);
			mLeftSpace = (Space) itemView.findViewById(R.id.left_space);
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
				if (mItemLayout != null) {
					mItemLayout.setBackgroundResource(R.drawable.transparent);
				}
				if (mImageView != null) {
					Picasso.with(mContext).
							load(R.drawable.template_title_item_horizontal_preview).
							into(mImageView);
				}
			}
		}

		@Override
		public void restoreImage() {
			if (isMore) {
				//do nothing
			} else if (imageUrl != null) {
				if (mItemLayout != null) {
					mItemLayout.setBackgroundResource(R.drawable.transparent);
				}
				if (mImageView != null) {
					Picasso.with(mContext).
							load(imageUrl).
							placeholder(R.drawable.template_title_item_horizontal_preview).
							error(R.drawable.template_title_item_horizontal_preview).
							tag("banner").
							into(mImageView);
					if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
						Picasso.with(mContext).pauseTag("banner");
					}
				}
			} else {
				if (mItemLayout != null) {
					mItemLayout.setBackgroundResource(R.drawable.transparent);
				}
				if (mImageView != null) {
					Picasso.with(mContext).
							load(R.drawable.template_title_item_horizontal_preview).
							into(mImageView);
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