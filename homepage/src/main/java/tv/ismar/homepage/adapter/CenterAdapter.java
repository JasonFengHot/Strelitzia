package tv.ismar.homepage.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.entity.banner.BannerCarousels;
import tv.ismar.app.widget.MyRecyclerView;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.homepage.R;

import tv.ismar.homepage.banner.IsmartvLinearLayout;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 居中RecyclerView适配器
 */

public class CenterAdapter extends BaseRecycleAdapter<CenterAdapter.CenterViewHolder> {

    private Context mContext;
    private List<BannerCarousels> mData;

    /*add by dragontec for bug 4334 start*/
    public CenterAdapter(Context context) {
    	mContext = context;
	}
	/*add by dragontec for bug 4334 end*/

    public CenterAdapter(Context context, List<BannerCarousels> data){
        this.mContext = context;
        this.mData = data;
    }

	/*add by dragontec for bug 4334 start*/
    public void setData(List<BannerCarousels> data) {
    	if (mData == null) {
    		mData = data;
			notifyDataSetChanged();
		}
	}

	public List<BannerCarousels> getData() {
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
    public CenterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_center_item,parent,false);
        return new CenterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CenterViewHolder holder, int position) {
		holder.imageUrl = null;
    	/*modify by dragontec for bug 4334 start*/
    	if (mData != null) {
			BannerCarousels carousels = mData.get(position % mData.size());
			holder.mTitle.setText(carousels.title);
			/*add by dragontec for bug 4316,卖点文字不正确的问题 start*/
			holder.mIntroduction.setText(carousels.focus);
			/*add by dragontec for bug 4316,卖点文字不正确的问题 end*/
			/*add by dragontec for bug 4307,4277 start*/
			holder.mLayout.setFocusableInTouchMode(false);
			/*add by dragontec for bug 4307,4277 end*/
			if (!TextUtils.isEmpty(carousels.video_image)) {
				holder.imageUrl = carousels.video_image;
/*modify by dragontec for bug 4336 start*/
				Picasso.with(mContext).load(carousels.video_image).
/*add by dragontec for bug 4205 start*/
                        memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).
/*add by dragontec for bug 4205 end*/
                        placeholder(R.drawable.template_center_item_preview).
                        error(R.drawable.template_center_item_preview).
						tag("banner").
                        into(holder.mPosterIg);
/*modify by dragontec for bug 4336 end*/
				if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
					Picasso.with(mContext).pauseTag("banner");
				}
			} else {
/*modify by dragontec for bug 4336 start*/
				Picasso.with(mContext).
                        load(R.drawable.template_center_item_preview).
                        into(holder.mPosterIg);
/*modify by dragontec for bug 4336 end*/
			}
			/*add by dragontec for bug 4325,卖点文字不正确的问题 start*/
			String focusStr = carousels.title;
			if (carousels.focus != null && !carousels.focus.equals("") && !carousels.focus.equals("null")) {
				focusStr = carousels.focus;
			}
			holder.mTitle.setTag(new String[]{carousels.title, focusStr});
			/*add by dragontec for bug 4325,卖点文字不正确的问题 end*/
		}
		/*modify by dragontec for bug 4334 end*/
    }

	@Override
	public int getItemViewType(int position) {
		return TYPE_NORMAL;
	}

	@Override
    public int getItemCount() {
        if (mData.size() == 0){
            return 0;
        }else {
            return Integer.MAX_VALUE;
        }
    }

    public class CenterViewHolder extends BaseViewHolder{
        public TextView mTitle;
		/*add by dragontec for bug 4316 start*/
        public TextView mIntroduction;
		/*add by dragontec for bug 4316 end*/
        public RecyclerImageView mPosterIg;
		/*add by dragontec for bug 4307,4277 start*/
        public IsmartvLinearLayout mLayout;
		/*add by dragontec for bug 4307,4277 end*/
		/*add by dragontec for bug 4355 start*/
        public LinearLayout mTextLayout;
		/*add by dragontec for bug 4355 end*/
		public String imageUrl = null;

        public CenterViewHolder(View itemView) {
            super(itemView, CenterAdapter.this);
            mTitle = (TextView) itemView.findViewById(R.id.center_item_title);
			/*add by dragontec for bug 4316 start*/
			mIntroduction = (TextView) itemView.findViewById(R.id.center_item_introduction);
			/*add by dragontec for bug 4316 end*/
            mPosterIg = (RecyclerImageView) itemView.findViewById(R.id.center_item_poster);
			/*add by dragontec for bug 4307,4277 start*/
            mLayout = (IsmartvLinearLayout) itemView.findViewById(R.id.center_ismartv_linear_layout);
			/*add by dragontec for bug 4307,4277 end*/
			/*add by dragontec for bug 4355 start*/
            mTextLayout = (LinearLayout)itemView.findViewById(R.id.center_item_text_layout);
			/*add by dragontec for bug 4355 end*/
        }

		@Override
		public void clearImage() {
			super.clearImage();
			if (mPosterIg != null) {
				Picasso.with(mContext).
						load(R.drawable.template_center_item_preview).
						into(mPosterIg);
			}
		}

		@Override
		public void restoreImage() {
        	if (mPosterIg != null ) {
				if (imageUrl != null) {
					Picasso.with(mContext).
							load(imageUrl).
							memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).
							placeholder(R.drawable.template_center_item_preview).
							error(R.drawable.template_center_item_preview).
							tag("banner").
							into(mPosterIg);
					if (mScrollState != RecyclerView.SCROLL_STATE_IDLE || isParentScrolling) {
						Picasso.with(mContext).pauseTag("banner");
					}
				} else {
					Picasso.with(mContext).
							load(R.drawable.template_center_item_preview).
							into(mPosterIg);
				}
			}
			super.restoreImage();
		}

		@Override
        protected float getScaleXY() {
            return 1.2F;
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.center_ismartv_linear_layout;
        }
		/*add by dragontec for bug 4325 start*/
        @Override
        protected int getTitleId() {
            return R.id.center_item_title;
        }
		/*add by dragontec for bug 4325 end*/
    }
}
