package tv.ismar.homepage.banner.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
/*add by dragontec for bug 4265 start*/
import android.view.KeyEvent;
/*add by dragontec for bug 4265 end*/
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

/*add by dragontec for bug 4265 start*/
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
/*add by dragontec for bug 4265 end*/
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.homepage.R;
import tv.ismar.searchpage.utils.JasmineUtil;

/**
 * Created by huibin on 25/08/2017.
 */

public class BannerHorizontal519Adapter extends RecyclerView.Adapter<BannerHorizontal519Adapter.SubscribeViewHolder> {
    private Context mContext;

    private List<BannerEntity.PosterBean> mSubscribeEntityList;

    private int currentPageNumber;
    private int totalPageCount;
    private int totalItemCount;

    private BannerHorizontal519Adapter.OnBannerHoverListener mHoverListener;
    private BannerHorizontal519Adapter.OnBannerClickListener mSubscribeClickListener;

    public BannerHorizontal519Adapter(Context context, BannerEntity bannerEntity) {
        mContext = context;
        currentPageNumber = 1;
        totalPageCount = bannerEntity.getCount_pages();
        totalItemCount = bannerEntity.getCount();

        //如果存在更多按钮，并且是在加载最后一页数据时，添加更多按钮的空数据
//        if (bannerEntity.is_more() && bannerEntity.getNum_pages() == bannerEntity.getCount_pages()){
        if (bannerEntity.getNum_pages() == bannerEntity.getCount_pages()) {
            BannerEntity.PosterBean posterBean = new BannerEntity.PosterBean();
            posterBean.setTitle("更多");
            //横版海报更多按钮
            posterBean.setPoster_url("more");
            bannerEntity.getPoster().add(posterBean);
        }
        mSubscribeEntityList = bannerEntity.getPoster();
    }

	/*add by dragontec for bug 4265 start*/
	private RecyclerView mRecyclerView = null;

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		mRecyclerView = recyclerView;
	}

	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
    	if (mRecyclerView == recyclerView) {
			mRecyclerView = null;
		}
		super.onDetachedFromRecyclerView(recyclerView);
	}
	/*add by dragontec for bug 4265 end*/

	public void setHoverListener(OnBannerHoverListener hoverListener) {
        mHoverListener = hoverListener;
    }

    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    public void setCurrentPageNumber(int currentPageNumber) {
        this.currentPageNumber = currentPageNumber;
    }

    public int getTotalPageCount() {
        return totalPageCount;
    }

    public void setTotalPageCount(int totalPageCount) {
        this.totalPageCount = totalPageCount;
    }

    public int getTatalItemCount() {
        return totalItemCount;
    }

    public void setTatalItemCount(int tatalItemCount) {
        this.totalItemCount = tatalItemCount;
    }

    @Override
    public SubscribeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner_horizontal_519, parent, false);
        SubscribeViewHolder holder = new SubscribeViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(SubscribeViewHolder holder, int position) {
        BannerEntity.PosterBean entity = mSubscribeEntityList.get(position);

        String title = entity.getTitle();
        String imageUrl = entity.getPoster_url();
        String targetImageUrl = TextUtils.isEmpty(imageUrl) ? null : imageUrl;

        if ("更多".equals(title)){
            holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(R.drawable.banner_horizontal_more);
            holder.mTitle.setVisibility(View.INVISIBLE);
            holder.mItemView.findViewById(R.id.content_layout).setVisibility(View.INVISIBLE);
        }else {
            holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(android.R.color.transparent);
            holder.mTitle.setVisibility(View.VISIBLE);
            holder.mItemView.findViewById(R.id.content_layout).setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(targetImageUrl).placeholder(R.drawable.list_item_preview_bg)
                    .error(R.drawable.list_item_preview_bg).into(holder.mImageView);
        }

        holder.mTitle.setText(entity.getTitle() + " ");
        holder.mItemView.findViewById(R.id.item_layout).setTag(entity);
        holder.mItemView.findViewById(R.id.item_layout).setTag(R.id.banner_item_position, position);

        Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(entity.getTop_left_corner())).into(holder.markLT);
        Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(entity.getTop_right_corner())).into(holder.markRT);

        if (entity.getRating_average() != 0){
            holder.markRB.setText(new DecimalFormat("0.0").format(entity.getRating_average()));
            holder.markRB.setVisibility(View.VISIBLE);
        }else {
            holder.markRB.setVisibility(View.INVISIBLE);
        }


        if (position == 0) {
            holder.mLeftSpace.setVisibility(View.GONE);
        } else {
            holder.mLeftSpace.setVisibility(View.VISIBLE);
        }
		/*add by dragontec for bug 4325,卖点文字不正确的问题 start*/
        String focusStr = entity.getTitle();
        if(entity.getFocus() != null && !entity.getFocus().equals("") && !entity.getFocus().equals("null")){
            focusStr = entity.getFocus();
        }
        holder.mTitle.setTag(new String[]{entity.getTitle(),focusStr});
		/*add by dragontec for bug 4325,卖点文字不正确的问题 end*/
    }

    @Override
    public int getItemCount() {
        return mSubscribeEntityList.size();
    }

    public void addDatas(BannerEntity bannerEntity) {
        //如果存在更多按钮，并且是在加载最后一页数据时，添加更多按钮的空数据
        if (bannerEntity.is_more() && bannerEntity.getNum_pages() == bannerEntity.getCount_pages()){
            BannerEntity.PosterBean posterBean = new BannerEntity.PosterBean();
            posterBean.setTitle("更多");
            //横版海报更多按钮
            posterBean.setPoster_url("more");
            bannerEntity.getPoster().add(posterBean);
        }
        int startIndex = (bannerEntity.getNum_pages() - 1) * 33;
        int endIndex;
        if (bannerEntity.getNum_pages() == bannerEntity.getCount_pages()) {
            if (bannerEntity.is_more()){
                mSubscribeEntityList.add(new BannerEntity.PosterBean());
                endIndex = bannerEntity.getCount() - 1 + 1;
            }else {
                endIndex = bannerEntity.getCount() - 1;
            }
        } else {
            endIndex = bannerEntity.getNum_pages() * 33 - 1;
        }

        for (int i = 0; i < bannerEntity.getPoster().size(); i++) {
            mSubscribeEntityList.set(startIndex + i, bannerEntity.getPoster().get(i));
        }
/*modify by dragontec for bug 4317 start*/
//        notifyItemRangeInserted(startIndex, endIndex - startIndex);
        notifyItemRangeChanged(startIndex, endIndex - startIndex + 1);
/*modify by dragontec for bug 4317 end*/
    }

    public void addEmptyDatas(List<BannerEntity.PosterBean> emptyList) {
        currentPageNumber = currentPageNumber + 1;
        mSubscribeEntityList.addAll(emptyList);
    }

    public void setBannerClickListener(BannerHorizontal519Adapter.OnBannerClickListener subscribeClickListener) {
        mSubscribeClickListener = subscribeClickListener;
    }

    int getPostItemId(String url) {
        int id = 0;
        try {
            Pattern p = Pattern.compile("/(\\d+)/?$");
            Matcher m = p.matcher(url);
            if (m.find()) {
                String idStr = m.group(1);
                if (idStr != null) {
                    id = Integer.parseInt(idStr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public interface OnBannerHoverListener {
/*modify by dragontec for bug 4057 start*/
//        void onBannerHover(View view, int position, boolean hovered);
        void onBannerHover(View view, int position, boolean hovered, boolean isPrimary);
/*modify by dragontec for bug 4057 end*/
    }

    public interface OnBannerClickListener {
        void onBannerClick(View view, int position);
    }

    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {

            outRect.bottom = space;
            outRect.top = space;
            outRect.right = space;
            outRect.left = space;
        }
    }

    class SubscribeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnFocusChangeListener, View.OnHoverListener,
/*add by dragontec for bug 4265 start*/
			View.OnKeyListener
/*add by dragontec for bug 4265 end*/
	{

        private Space mLeftSpace;
        private RecyclerImageView mImageView;
        private TextView mTitle;
        private View mItemView;
        private RecyclerImageView markLT;
        private TextView markRB;
        private RecyclerImageView markRT;


        public SubscribeViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            View itemLayoutView = mItemView.findViewById(R.id.item_layout);
            mItemView.findViewById(R.id.item_layout).setOnClickListener(this);
            mItemView.findViewById(R.id.item_layout).setOnFocusChangeListener(this);
            mItemView.findViewById(R.id.item_layout).setOnHoverListener(this);
/*add by dragontec for bug 4265 start*/
			mItemView.findViewById(R.id.item_layout).setOnKeyListener(this);
/*add by dragontec for bug 4265 end*/
            mImageView = (RecyclerImageView) itemView.findViewById(R.id.image_view);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mLeftSpace = (Space) itemView.findViewById(R.id.left_space);
            markLT = (RecyclerImageView) itemView.findViewById(R.id.banner_mark_lt);
            markRB = (TextView)itemView.findViewById(R.id.banner_mark_br);
            markRT = (RecyclerImageView) itemView.findViewById(R.id.banner_mark_rt);

        }

        @Override
        public void onClick(View v) {
/*modify by dragontec for bug 4330 start*/
			int[] location = new int[]{0, 0};
			v.getLocationOnScreen(location);
			int screenWidth = v.getResources().getDisplayMetrics().widthPixels;
			int screenHeight = v.getResources().getDisplayMetrics().heightPixels;
			if (location[0] >= 0 && location[1] >= 0 && location[0] + v.getWidth() <= screenWidth && location[1] + v.getHeight() <= screenHeight) {
				if (mSubscribeClickListener != null) {
					int position = (int) v.getTag(R.id.banner_item_position);
					mSubscribeClickListener.onBannerClick(v, position);
				}
			}
/*modify by dragontec for bug 4330 end*/
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {

            if (hasFocus) {
                scaleToLarge(v.findViewById(R.id.item_layout));
                v.findViewById(R.id.title).setSelected(true);
            } else {
                scaleToNormal(v.findViewById(R.id.item_layout));
                v.findViewById(R.id.title).setSelected(false);
            }
			/*add by dragontec for bug 4325 start*/
            updateTitleText(hasFocus);
			/*add by dragontec for bug 4325 end*/
        }

        private void scaleToLarge(View view) {
            JasmineUtil.scaleOut3(view);
        }

        private void scaleToNormal(View view) {
            JasmineUtil.scaleIn3(view);
        }

		/*add by dragontec for bug 4325 start*/
        private void updateTitleText(boolean hasFocus) {
            View view = itemView.findViewById(R.id.title);
            if(view != null && view instanceof TextView) {
                TextView textView = (TextView) itemView.findViewById(R.id.title);
                Object tag = itemView.findViewById(R.id.title).getTag();
                if (tag != null && tag instanceof String[] && ((String[]) tag).length == 2) {
                    String title = ((String[]) tag)[0];
                    String focusTitle = ((String[]) tag)[1];
                    if (hasFocus) {
                        textView.setText(focusTitle);
                        textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                        textView.setMarqueeRepeatLimit(-1);
                        textView.setHorizontallyScrolling(true);
                        textView.setSelected(true);
                    } else {
                        textView.setText(title);
                        textView.setEllipsize(TextUtils.TruncateAt.END);
                        textView.setMarqueeRepeatLimit(0);
                        textView.setHorizontallyScrolling(false);
                        textView.setSelected(false);
                    }
                }
            }
        }
		/*add by dragontec for bug 4325 end*/

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
				/*delete by dragontec for bug 4169 start*/
            	//case MotionEvent.ACTION_HOVER_MOVE:
				/*delete by dragontec for bug 4169 end*/
                    if (mHoverListener != null) {
                        int position = (int) v.getTag(R.id.banner_item_position);
/*modify by dragontec for bug 4057 start*/
//                        mHoverListener.onBannerHover(v, position, true);
                        mHoverListener.onBannerHover(v, position, true, event.getButtonState() == MotionEvent.BUTTON_PRIMARY);
/*modify by dragontec for bug 4057 end*/
                    }
                    /*add by dragontec for bug 4265 start*/
                    if (!v.hasFocus()) {
						int[] location = new int[]{0, 0};
						v.getLocationOnScreen(location);
						int screenWidth = v.getResources().getDisplayMetrics().widthPixels;
						int screenHeight = v.getResources().getDisplayMetrics().heightPixels;
						if (location[0] >= 0 && location[1] >= 0 && location[0] + v.getWidth() <= screenWidth && location[1] + v.getHeight() <= screenHeight) {
							v.requestFocus();
							v.requestFocusFromTouch();
						}
					}
					/*add by dragontec for bug 4265 end*/
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    if (mHoverListener != null) {
                        int position = (int) v.getTag(R.id.banner_item_position);
/*modify by dragontec for bug 4057 start*/
//                        mHoverListener.onBannerHover(v, position, false);
                        mHoverListener.onBannerHover(v, position, false, event.getButtonState() == MotionEvent.BUTTON_PRIMARY);
/*modify by dragontec for bug 4057 end*/
                    }
                    break;
            }
            return false;
        }

		/*add by dragontec for bug 4265 start*/
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_UP) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
					if (mRecyclerView != null && mRecyclerView instanceof RecyclerViewTV) {
						if (((RecyclerViewTV) mRecyclerView).isNotScrolling()) {
							//check item
							int[] location = new int[]{0, 0};
							v.getLocationOnScreen(location);
							int screenWidth = v.getResources().getDisplayMetrics().widthPixels;
							if (location[0] < 0 || location[0] + v.getWidth() > screenWidth) {
								if (mRecyclerView.getLayoutManager() != null) {
									mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, getAdapterPosition());
								}
							}
						}
					}
				}
			}
			return false;
		}
		/*add by dragontec for bug 4265 end*/
	}
}
