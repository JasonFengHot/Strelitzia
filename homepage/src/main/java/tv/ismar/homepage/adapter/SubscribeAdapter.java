/*add by dragontec for bug 4362 start*/
package tv.ismar.homepage.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

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
	public SubscribeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner_subscribe, parent, false);
		return new SubscribeViewHolder(view);
	}

	@Override
	public void onBindViewHolder(SubscribeViewHolder holder, int position) {
		holder.mPosition = position;
		if (mData != null) {
			BannerPoster entity = mData.get(position);
			String title = entity.title;

			String imageUrl = entity.poster_url;
			String targetImageUrl = TextUtils.isEmpty(imageUrl) ? null : imageUrl;
			if (!TextUtils.isEmpty(entity.vertical_url) && entity.vertical_url.equals("更多")){
				holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(R.drawable.banner_horizontal_more);
				holder.mItemView.findViewById(R.id.content_layout).setVisibility(View.INVISIBLE);
				holder.mOrderTitle.setVisibility(View.INVISIBLE);
//            Picasso.with(mContext).load(R.drawable.banner_horizontal_more).into(holder.mImageView);
			}else {
				holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(android.R.color.transparent);
				holder.mItemView.findViewById(R.id.content_layout).setVisibility(View.VISIBLE);
				holder.mOrderTitle.setVisibility(View.VISIBLE);

/*modify by dragontec for bug 4336 start*/
				Picasso.with(mContext).load(targetImageUrl).placeholder(R.drawable.template_title_item_horizontal_preview)
						.error(R.drawable.template_title_item_horizontal_preview).into(holder.mImageView);
/*modify by dragontec for bug 4336 end*/
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
			holder.mItemView.findViewById(R.id.item_layout).setTag(entity);
			holder.mItemView.findViewById(R.id.item_layout).setTag(R.id.banner_item_position, position);

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
			holder.mOrderTitle.setTag(new String[]{entity.title,focusStr});
		}
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
		private View mItemView;
		private TextView mTitle;
		private RecyclerImageView mTimeLine;
		/*add by dragontec for bug 4366 start*/
		private RecyclerImageView mTimeDot;
		/*add by dragontec for bug 4366 end*/
		private RecyclerImageView markLT;
		private TextView markRB;

		public SubscribeViewHolder(View itemView) {
			super(itemView, SubscribeAdapter.this);
			mItemView = itemView;
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