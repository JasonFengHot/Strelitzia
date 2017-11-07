package tv.ismar.homepage.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 竖版双行适配器
 */

public class DoubleMdAdapter extends BaseRecycleAdapter<DoubleMdAdapter.DoubleMdViewHolder>{

    public static final int TYPE_HEADER = 0;//头部
    public static final int TYPE_NORMAL = 1;//一般item

    private Context mContext;
    private List<BannerPoster> mData;

    private View mHeaderView;

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

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    public List<BannerPoster> getmData() {
        return mData;
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null) return TYPE_NORMAL;
        if (position == 0) return TYPE_HEADER;
        return TYPE_NORMAL;
    }

    @Override
    public DoubleMdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER)
            return new DoubleMdViewHolder(mHeaderView);
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_double_md_item,parent,false);
        return new DoubleMdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DoubleMdViewHolder holder, int position) {
        if(position != 0){
            holder.mPosition = position;
            /*modify by dragontec for bug 4334 start*/
            if (mData != null) {
				BannerPoster poster = mData.get(position - 1);
				holder.mTitleTv.setText(poster.title);
				if (!TextUtils.isEmpty(poster.vertical_url)) {
					if (poster.vertical_url.equals("更多")) {
						Picasso.with(mContext).load(R.drawable.banner_vertical_more).into(holder.mPosterIg);
					} else {
/*modify by dragontec for bug 4336 start*/
						Picasso.with(mContext).load(poster.vertical_url).
                                error(R.drawable.template_title_item_vertical_preview).
                                placeholder(R.drawable.template_title_item_vertical_preview).
                                into(holder.mPosterIg);
/*modify by dragontec for bug 4336 end*/
					}
				} else {
/*modify by dragontec for bug 4336 start*/
					Picasso.with(mContext).
                            load(R.drawable.template_title_item_vertical_preview).
                            into(holder.mPosterIg);
/*modify by dragontec for bug 4336 end*/
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
				/*add by dragontec for bug 4325 start*/
				String focusStr = poster.title;
				if (poster.focus != null && !poster.focus.equals("") && !poster.focus.equals("null")) {
					focusStr = poster.focus;
				}
				holder.mTitleTv.setTag(new String[]{poster.title, focusStr});
				/*add by dragontec for bug 4325 end*/
			}
			/*add by dragontec for bug 4334 end*/
        }
    }

    @Override
    public int getItemCount() {
        if(mData == null) return 0;
        return (mHeaderView==null) ? mData.size() : mData.size() + 1;
    }

    public class DoubleMdViewHolder extends BaseViewHolder {
        public RecyclerImageView mPosterIg;//海报
        public RecyclerImageView mLtIconTv;//左上icon
        public TextView mRbIconTv;//右下icon
        public TextView mTitleTv;//标题
        public RecyclerImageView mRtIconTv;

        public DoubleMdViewHolder(View itemView) {
            super(itemView, DoubleMdAdapter.this);
            mPosterIg = (RecyclerImageView) itemView.findViewById(R.id.double_md_item_poster);
            mLtIconTv = (RecyclerImageView) itemView.findViewById(R.id.double_md_item_lt_icon);
            mRbIconTv = (TextView) itemView.findViewById(R.id.double_md_item_rb_icon);
            mTitleTv = (TextView) itemView.findViewById(R.id.double_md_item_title);
            mRtIconTv= (RecyclerImageView) itemView.findViewById(R.id.guide_rt_icon);
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
