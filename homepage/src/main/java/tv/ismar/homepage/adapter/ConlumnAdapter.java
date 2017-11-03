package tv.ismar.homepage.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 栏目RecyclerView适配器
 */

public class ConlumnAdapter extends BaseRecycleAdapter<ConlumnAdapter.ConlumnViewHolder> {

    private Context mContext;
    private List<BannerPoster> mData;

	/*add by dragontec for bug 4334 start*/
    public ConlumnAdapter(Context context) {
    	mContext = context;
	}
	/*add by dragontec for bug 4334 end*/

    public ConlumnAdapter(Context context, List<BannerPoster> data){
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

    @Override
    public ConlumnViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_conlumn_item,parent,false);
        return new ConlumnViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConlumnViewHolder holder, int position) {
    	/*modify by dragontec for bug 4334 start*/
    	if (mData != null) {
			BannerPoster poster = mData.get(position);
			Log.d("ConlumnAdapter", "position:" + position);
			holder.mPosition = position;
			holder.mTitle.setText(poster.title);
			if (!TextUtils.isEmpty(poster.image_url)) {
				Picasso.with(mContext).load(poster.image_url).error(R.drawable.list_item_preview_bg).into(holder.mPoster);
			} else {
				Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mPoster);
			}
			/*add by dragontec for bug 4325 start*/
			String focusStr = poster.title;
			if (poster.focus != null && !poster.focus.equals("") && !poster.focus.equals("null")) {
				focusStr = poster.focus;
			}
			holder.mTitle.setTag(new String[]{poster.title, focusStr});
			/*add by dragontec for bug 4325 end*/
		}
		/*add by dragontec for bug 4334 end*/
    }

    @Override
    public int getItemCount() {
        return (mData!=null)? mData.size() : 0;
    }

    public class ConlumnViewHolder extends BaseViewHolder{
        public TextView mTitle;
        public RecyclerImageView mPoster;

        public ConlumnViewHolder(View itemView) {
            super(itemView, ConlumnAdapter.this);
            mTitle = (TextView) itemView.findViewById(R.id.conlumn_item_tv);
            mPoster = (RecyclerImageView) itemView.findViewById(R.id.conlumn_item_poster);
        }

        @Override
        protected int getScaleLayoutId() {
            return R.id.conlumn_ismartv_linear_layout;
        }
		/*add by dragontec for bug 4325 start*/
        @Override
        protected int getTitleId() {
            return R.id.conlumn_item_tv;
        }
		/*add by dragontec for bug 4325 end*/
    }
}
