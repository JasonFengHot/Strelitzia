/*add by dragontec for bug 4362 start*/
package tv.ismar.homepage.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

public class MovieMixAdapter extends BaseRecycleAdapter<MovieMixAdapter.MovieMixViewHolder> {
	private Context mContext;
	private List<BannerPoster> mData;
	private int bigWidth;
	private int smallWidth;

	public MovieMixAdapter(Context context) {
		mContext = context;
		bigWidth = context.getResources().getDimensionPixelSize(R.dimen.banner_item_movie_big_width);
		smallWidth = context.getResources().getDimensionPixelSize(R.dimen.banner_item_movie_small_width);
	}

	public MovieMixAdapter(Context context, List<BannerPoster> data){
		this.mContext = context;
		this.mData = data;
		bigWidth = context.getResources().getDimensionPixelSize(R.dimen.banner_item_movie_big_width);
		smallWidth = context.getResources().getDimensionPixelSize(R.dimen.banner_item_movie_small_width);
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
	public MovieMixViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner_movie_mix, parent, false);
		return new MovieMixViewHolder(view);
	}

	@Override
	public void onBindViewHolder(MovieMixViewHolder holder, int position) {
		if (position == 0){
			ViewGroup.LayoutParams itemLayoutParams = holder.itemLayout.getLayoutParams();
			itemLayoutParams.width = bigWidth;
			holder.itemLayout.setLayoutParams(itemLayoutParams);


			ViewGroup.LayoutParams wrapperLayoutParams = holder.itemWrapper.getLayoutParams();
			wrapperLayoutParams.width = bigWidth;
			holder.itemWrapper.setLayoutParams(wrapperLayoutParams);

			ViewGroup.LayoutParams imageLayoutParams = holder.mImageView.getLayoutParams();
			imageLayoutParams.width = bigWidth;
			holder.mImageView.setLayoutParams(imageLayoutParams);

			ViewGroup.LayoutParams titleLayoutParams = holder.mTitle.getLayoutParams();
			titleLayoutParams.width = bigWidth;
			holder.mTitle.setLayoutParams(titleLayoutParams);
		}else {
			ViewGroup.LayoutParams itemLayoutParams = holder.itemLayout.getLayoutParams();
			itemLayoutParams.width = smallWidth;
			holder.itemLayout.setLayoutParams(itemLayoutParams);

			ViewGroup.LayoutParams wrapperLayoutParams = holder.itemWrapper.getLayoutParams();
			wrapperLayoutParams.width = smallWidth;
			holder.itemWrapper.setLayoutParams(wrapperLayoutParams);

			ViewGroup.LayoutParams imageLayoutParams = holder.mImageView.getLayoutParams();
			imageLayoutParams.width = smallWidth;
			holder.mImageView.setLayoutParams(imageLayoutParams);

			ViewGroup.LayoutParams titleLayoutParams = holder.mTitle.getLayoutParams();
			titleLayoutParams.width = smallWidth;
			holder.mTitle.setLayoutParams(titleLayoutParams);
		}
		if (mData != null) {
			BannerPoster entity = mData.get(position);
			String title = entity.title;
			String imageUrl;
			if (position == 0) {
				imageUrl = entity.poster_url;
			} else {
				imageUrl = entity.vertical_url;
			}
			String targetImageUrl = TextUtils.isEmpty(imageUrl) ? null : imageUrl;

			if (position == 0) {
				holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(android.R.color.transparent);
				holder.mTitle.setVisibility(View.VISIBLE);
				holder.itemWrapper.setVisibility(View.VISIBLE);
/*modify by dragontec for bug 4336 start*/
				Picasso.with(mContext).load(targetImageUrl).placeholder(R.drawable.template_title_item_horizontal_preview)
						.error(R.drawable.template_title_item_horizontal_preview).into(holder.mImageView);
/*modify by dragontec for bug 4336 end*/
			} else {
				if ("更多".equals(title)) {
					holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(R.drawable.banner_vertical_more);
					holder.mTitle.setVisibility(View.INVISIBLE);
					holder.itemWrapper.setVisibility(View.INVISIBLE);
				} else {
					holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(android.R.color.transparent);
					holder.mTitle.setVisibility(View.VISIBLE);
					holder.itemWrapper.setVisibility(View.VISIBLE);
/*modify by dragontec for bug 4336 start*/
					Picasso.with(mContext).load(targetImageUrl).placeholder(R.drawable.template_title_item_vertical_preview)
							.error(R.drawable.template_title_item_vertical_preview).into(holder.mImageView);
/*modify by dragontec for bug 4336 end*/
				}

			}
			holder.mTitle.setText(entity.title + " ");
			holder.mItemView.findViewById(R.id.item_layout).setTag(entity);
			holder.mItemView.findViewById(R.id.item_layout).setTag(R.id.banner_item_position, position);

			Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(entity.top_left_corner)).into(holder.markLT);
			Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(entity.top_right_corner)).into(holder.markRT);

			if (entity.rating_average != 0) {
				holder.markRB.setText(new DecimalFormat("0.0").format(entity.rating_average));
				holder.markRB.setVisibility(View.VISIBLE);
			} else {
				holder.markRB.setVisibility(View.INVISIBLE);
			}

			if (position == 0) {
				holder.mLeftSpace.setVisibility(View.GONE);
			} else {
				holder.mLeftSpace.setVisibility(View.VISIBLE);
			}
			String focusStr = entity.title;
			if (entity.focus != null && !entity.focus.equals("") && !entity.focus.equals("null")) {
				focusStr = entity.focus;
			}
			holder.mTitle.setTag(new String[]{entity.title, focusStr});
		}
	}

	@Override
	public int getItemCount() {
		return (mData!=null) ? mData.size() : 0;
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