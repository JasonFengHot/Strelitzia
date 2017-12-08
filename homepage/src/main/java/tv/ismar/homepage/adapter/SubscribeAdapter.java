/*add by dragontec for bug 4362 start*/
package tv.ismar.homepage.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
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
 * Created by dragontec on 2017/11/6.
 */

public class SubscribeAdapter extends BaseRecycleAdapter<SubscribeAdapter.SubscribeViewHolder> {
	private Context mContext;
	private List<BannerPoster> mData;

	public SubscribeAdapter(Context context) {
		mContext = context;
	}

	public SubscribeAdapter(Context context, List<BannerPoster> data){
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
	public SubscribeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner_subscribe, parent, false);
		return new SubscribeViewHolder(view);
	}

	@Override
	public void onBindViewHolder(SubscribeViewHolder holder, int position) {
		holder.imageUrl = null;
		holder.isMore = false;
		if (mData != null) {
			BannerPoster entity = mData.get(position);

			if (!TextUtils.isEmpty(entity.vertical_url) && entity.vertical_url.equals("更多")){
				holder.isMore = true;
				holder.mItemLayout.setBackgroundResource(R.drawable.banner_horizontal_more);
				holder.mContentLayout.setVisibility(View.INVISIBLE);
				holder.mOrderTitle.setVisibility(View.INVISIBLE);
//            Picasso.with(mContext).load(R.drawable.banner_horizontal_more).into(holder.mImageView);
			}else {
				holder.mItemLayout.setBackgroundResource(android.R.color.transparent);
				holder.mContentLayout.setVisibility(View.VISIBLE);
				holder.mOrderTitle.setVisibility(View.VISIBLE);
				if (!TextUtils.isEmpty(entity.poster_url)) {
					holder.imageUrl = entity.poster_url;
					Picasso.with(mContext).load(entity.poster_url).
							placeholder(R.drawable.template_title_item_horizontal_preview).
							memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).
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
			if (position == 0) {
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.mTimeLine.getLayoutParams();
				layoutParams.setMarginEnd(0);
			}

			holder.mOrderTitle.setText("预约");

			holder.mPublishTime.setText(entity.display_order_date);
			Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(entity.top_left_corner)).into(holder.markLT);
			Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(entity.top_right_corner)).into(holder.markRT);

			if (entity.rating_average != 0){
				holder.markRB.setText(new DecimalFormat("0.0").format(entity.rating_average));
				holder.markRB.setVisibility(View.VISIBLE);
			}else {
				holder.markRB.setVisibility(View.INVISIBLE);
			}

			holder.mTitle.setText(entity.title + " ");
			holder.mItemLayout.setTag(entity);
			holder.mItemLayout.setTag(R.id.banner_item_position, position);

			if (position == 0){
				holder.mLeftSpace.setVisibility(View.GONE);
				((RelativeLayout.LayoutParams)holder.mTimeDot.getLayoutParams()).leftMargin = 0;
			}else {
				holder.mLeftSpace.setVisibility(View.VISIBLE);
				((RelativeLayout.LayoutParams)holder.mTimeDot.getLayoutParams()).leftMargin = mContext.getResources().getDimensionPixelSize(R.dimen.space_banner_item_width);
			}
			String focusStr = entity.title;
			if(entity.focus != null && !entity.focus.equals("") && !entity.focus.equals("null")){
				focusStr = entity.focus;
			}
			/*modify by dragontec for bug 4325 start*/
			holder.mTitle.setTag(new String[]{entity.title,focusStr});
			/*modify by dragontec for bug 4325 end*/
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

	public class SubscribeViewHolder extends BaseViewHolder {
		private final RecyclerImageView markRT;
		private  Space mLeftSpace;
		private RecyclerImageView mImageView;
		private TextView mOrderTitle;
		private TextView mPublishTime;
		private TextView mTitle;
		private RecyclerImageView mTimeLine;
		/*add by dragontec for bug 4366 start*/
		private RecyclerImageView mTimeDot;
		/*add by dragontec for bug 4366 end*/
		private RecyclerImageView markLT;
		private TextView markRB;
		private LinearLayout mItemLayout;
		private RelativeLayout mContentLayout;
		private String imageUrl;
		private boolean isMore;

		public SubscribeViewHolder(View itemView) {
			super(itemView, SubscribeAdapter.this);
			mImageView = (RecyclerImageView) itemView.findViewById(R.id.image_view);
			mOrderTitle = (TextView) itemView.findViewById(R.id.order_title);
			mPublishTime = (TextView) itemView.findViewById(R.id.publish_time);
			mTitle = (TextView) itemView.findViewById(R.id.title);
			mLeftSpace = (Space)itemView.findViewById(R.id.left_space);
			mTimeLine = (RecyclerImageView)itemView.findViewById(R.id.banner_item_timeline);
			mTimeDot = (RecyclerImageView)itemView.findViewById(R.id.banner_item_time_dot);
			markLT = (RecyclerImageView) itemView.findViewById(R.id.banner_mark_lt);
			markRB = (TextView)itemView.findViewById(R.id.banner_mark_br);
			markRT = (RecyclerImageView) itemView.findViewById(R.id.banner_mark_rt);
			mItemLayout = (LinearLayout) itemView.findViewById(R.id.item_layout);
			mContentLayout = (RelativeLayout) itemView.findViewById(R.id.content_layout);
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
			} else {
				if (mItemLayout != null) {
					mItemLayout.setBackgroundResource(R.drawable.transparent);
				}
				if (mImageView != null) {
					if (imageUrl != null) {
						Picasso.with(mContext).
								load(imageUrl).
								placeholder(R.drawable.template_title_item_horizontal_preview).
								memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).
								error(R.drawable.template_title_item_horizontal_preview).
								tag("banner").
								into(mImageView);
						if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
							Picasso.with(mContext).pauseTag("banner");
						}
					} else {
						Picasso.with(mContext).
								load(R.drawable.template_title_item_horizontal_preview).
								into(mImageView);
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

		public int getOrderTitleId() {
			return R.id.order_title;
		}
	}
}
/*add by dragontec for bug 4362 end*/