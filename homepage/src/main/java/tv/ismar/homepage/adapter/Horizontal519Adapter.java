/*add by dragontec for bug 4362 start*/
package tv.ismar.homepage.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.homepage.R;

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
	public Horizontal519ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner_horizontal_519, parent, false);
		return new Horizontal519ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(Horizontal519ViewHolder holder, int position) {
		holder.mPosition = position;
		if (mData != null) {
			BannerPoster poster = mData.get(position);

			String title = poster.title;
			String imageUrl = poster.poster_url;
			String targetImageUrl = TextUtils.isEmpty(imageUrl) ? null : imageUrl;

			if (!TextUtils.isEmpty(poster.vertical_url) && poster.vertical_url.equals("更多")) {
				holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(R.drawable.banner_horizontal_more);
				holder.mTitle.setVisibility(View.INVISIBLE);
				holder.mItemView.findViewById(R.id.content_layout).setVisibility(View.INVISIBLE);
			} else {
				holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(android.R.color.transparent);
				holder.mTitle.setVisibility(View.VISIBLE);
				holder.mItemView.findViewById(R.id.content_layout).setVisibility(View.VISIBLE);
/*modify by dragontec for bug 4336 start*/
				Picasso.with(mContext).load(targetImageUrl).placeholder(R.drawable.template_title_item_horizontal_preview)
						.error(R.drawable.template_title_item_horizontal_preview).into(holder.mImageView);
/*modify by dragontec for bug 4336 end*/
			}

			holder.mTitle.setText(title + " ");
			holder.mItemView.findViewById(R.id.item_layout).setTag(poster);
			holder.mItemView.findViewById(R.id.item_layout).setTag(R.id.banner_item_position, position);

			Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_left_corner)).into(holder.markLT);
			Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(poster.top_right_corner)).into(holder.markRT);

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
	public int getItemCount() {
		return (mData!=null) ? mData.size() : 0;
	}
	
	public class Horizontal519ViewHolder extends BaseViewHolder {
		private Space mLeftSpace;
		private RecyclerImageView mImageView;
		private TextView mTitle;
		private View mItemView;
		private RecyclerImageView markLT;
		private TextView markRB;
		private RecyclerImageView markRT;

		public Horizontal519ViewHolder(View itemView) {
			super(itemView, Horizontal519Adapter.this);
			mItemView = itemView;
			View itemLayoutView = mItemView.findViewById(R.id.item_layout);
			mImageView = (RecyclerImageView) itemView.findViewById(R.id.image_view);
			mTitle = (TextView) itemView.findViewById(R.id.title);
			mLeftSpace = (Space) itemView.findViewById(R.id.left_space);
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
	}
}
/*add by dragontec for bug 4362 end*/