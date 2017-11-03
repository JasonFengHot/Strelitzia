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
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

/*add by dragontec for bug 4265 start*/
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
/*add by dragontec for bug 4265 end*/
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
public class BannerSubscribeAdapter
        extends RecyclerView.Adapter<BannerSubscribeAdapter.SubscribeViewHolder> {

    private static final String TAG = "BannerSubscribeAdapter";

    private Context mContext;

    private List<BannerEntity.PosterBean> mSubscribeEntityList;
    private OnBannerClickListener mSubscribeClickListener;
    private OnBannerHoverListener mSubscribeHoverListener;
    private int currentPageNumber;
    private int totalPageCount;
    private int tatalItemCount;

    public int getTatalItemCount() {
        return tatalItemCount;
    }


    private int subscribeStatusChangedItemId;

    public BannerSubscribeAdapter(
            Context context, BannerEntity bannerEntity) {
        mContext = context;
        currentPageNumber = 1;
        totalPageCount = bannerEntity.getCount_pages();
        tatalItemCount = bannerEntity.getCount();
        mSubscribeEntityList = bannerEntity.getPoster();
//        Collections.sort(mSubscribeEntityList);
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

    public int getCurrentPageNumber() {
        return currentPageNumber;
    }


    public int getTotalPageCount() {
        return totalPageCount;
    }


    public void setSubscribeStatusChangedItemId(int subscribeStatusChangedItemId) {
        this.subscribeStatusChangedItemId = subscribeStatusChangedItemId;
    }

    public void setSubscribeEntityList(List<BannerEntity.PosterBean> subscribeEntityList) {
        mSubscribeEntityList = subscribeEntityList;
    }

    @Override
    public SubscribeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(mContext).inflate(R.layout.item_banner_subscribe, parent, false);
        SubscribeViewHolder holder = new SubscribeViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(SubscribeViewHolder holder, int position) {
        BannerEntity.PosterBean entity = mSubscribeEntityList.get(position);
        String title = entity.getTitle();

        String imageUrl = entity.getPoster_url();
        String targetImageUrl = TextUtils.isEmpty(imageUrl) ? null : imageUrl;
		/*modify by dragontec 修改了变量名和layout_id避免产生误解*/
        if ("更多".equals(title)){
            holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(R.drawable.banner_horizontal_more);
            holder.mItemView.findViewById(R.id.content_layout).setVisibility(View.INVISIBLE);
            holder.mOrderTitle.setVisibility(View.INVISIBLE);
//            Picasso.with(mContext).load(R.drawable.banner_horizontal_more).into(holder.mImageView);
        }else {
            holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(android.R.color.transparent);
            holder.mItemView.findViewById(R.id.content_layout).setVisibility(View.VISIBLE);
            holder.mOrderTitle.setVisibility(View.VISIBLE);

            Picasso.with(mContext).load(targetImageUrl).placeholder(R.drawable.list_item_preview_bg)
                    .error(R.drawable.list_item_preview_bg).into(holder.mImageView);
        }
        if (position == 0) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.mTimeLine.getLayoutParams();
            layoutParams.setMarginEnd(0);
        }


        //        int itemId = getMovieItemId(entity.getContent_url());
        //
        //        if (entity.getSubscribeStatus() == BannerEntity.SubscribeStatus.None) {
        //            holder.mOrderTitle.setText("");
        //            loadSubscribeStatus(itemId, holder.mOrderTitle, mSubscribeEntityList, position);
        //        } else if (entity.getSubscribeStatus() == BannerEntity.SubscribeStatus.Yes) {
        //            holder.mOrderTitle.setText("已预约");
        //        } else if (entity.getSubscribeStatus() == BannerEntity.SubscribeStatus.No) {
        //            holder.mOrderTitle.setText("预约");
        //        }

        holder.mOrderTitle.setText("预约");

        //        if (itemId == subscribeStatusChangedItemId) {
        //            loadSubscribeStatus(itemId, holder.mOrderTitle, mSubscribeEntityList, position);
        //        }

//            String timeString = entity.getOrder_date().getMonth() +"月" + entity.getOrder_date().getDate() + "日";
        holder.mPublishTime.setText(entity.getDisplay_order_date());
        Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(entity.getTop_left_corner())).into(holder.markLT);
        Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(entity.getTop_right_corner())).into(holder.markRT);

        if (entity.getRating_average() != 0){
            holder.markRB.setText(new DecimalFormat("0.0").format(entity.getRating_average()));
            holder.markRB.setVisibility(View.VISIBLE);
        }else {
            holder.markRB.setVisibility(View.INVISIBLE);
        }

        holder.mTitle.setText(entity.getTitle() + " ");
        holder.mItemView.findViewById(R.id.item_layout).setTag(entity);
        holder.mItemView.findViewById(R.id.item_layout).setTag(R.id.banner_item_position, position);

        if (position == 0){
            holder.mLeftSpace.setVisibility(View.GONE);
			/*add by dragontec for bug 4366 start*/
            ((RelativeLayout.LayoutParams)holder.mTimeDot.getLayoutParams()).leftMargin = 0;
        }else {
            holder.mLeftSpace.setVisibility(View.VISIBLE);
            ((RelativeLayout.LayoutParams)holder.mTimeDot.getLayoutParams()).leftMargin = mContext.getResources().getDimensionPixelSize(R.dimen.space_banner_item_width);
			/*add by dragontec for bug 4366 end*/
        }
		/*add by dragontec for bug 4325,卖点文字不正确的问题 start*/
        String focusStr = entity.getTitle();
        if(entity.getFocus() != null && !entity.getFocus().equals("") && !entity.getFocus().equals("null")){
            focusStr = entity.getFocus();
        }
        holder.mOrderTitle.setTag(new String[]{entity.getTitle(),focusStr});
		/*add by dragontec for bug 4325,卖点文字不正确的问题 end*/
    }

    private int getMovieItemId(String url) {
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

    @Override
    public int getItemCount() {
        return mSubscribeEntityList.size();
    }

    public interface OnBannerClickListener {
        void onBannerClick(View view, int position);
    }

    public interface OnBannerHoverListener {
/*modify by dragontec for bug 4057 start*/
//        void onBannerHover(View view, int position, boolean hovered);
        void onBannerHover(View view, int position, boolean hovered, boolean isPrimary);
/*modify by dragontec for bug 4057 end*/
    }


    public void setSubscribeClickListener(OnBannerClickListener subscribeClickListener) {
        mSubscribeClickListener = subscribeClickListener;
    }

    public void setSubscribeHoverListener(OnBannerHoverListener subscribeHoverListener) {
        mSubscribeHoverListener = subscribeHoverListener;
    }

    public void addDatas(BannerEntity bannerEntity) {

        int startIndex = (bannerEntity.getNum_pages() - 1) * 33;
        int endIndex;
        if (bannerEntity.getNum_pages() == bannerEntity.getCount_pages()) {
                endIndex = bannerEntity.getCount() - 1;
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


    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(
                Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            outRect.bottom = space;
            outRect.top = space;
            outRect.right = space;
            outRect.left = space;
        }
    }

    class SubscribeViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnFocusChangeListener, View.OnHoverListener
	/*add by dragontec for bug 4265 start*/
			, View.OnKeyListener
	/*add by dragontec for bug 4265 end*/
	{

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
            super(itemView);
            mItemView = itemView;
            mItemView.findViewById(R.id.item_layout).setOnClickListener(this);
            mItemView.findViewById(R.id.item_layout).setOnFocusChangeListener(this);
            mItemView.findViewById(R.id.item_layout).setOnHoverListener(this);
	/*add by dragontec for bug 4265 start*/
            mItemView.findViewById(R.id.item_layout).setOnKeyListener(this);
	/*add by dragontec for bug 4265 end*/
            mImageView = (RecyclerImageView) itemView.findViewById(R.id.image_view);
            mOrderTitle = (TextView) itemView.findViewById(R.id.order_title);
            mPublishTime = (TextView) itemView.findViewById(R.id.publish_time);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mLeftSpace = (Space)itemView.findViewById(R.id.left_space);
            mTimeLine = (RecyclerImageView)itemView.findViewById(R.id.banner_item_timeline);
			/*add by dragontec for bug 4366 start*/
            mTimeDot = (RecyclerImageView)itemView.findViewById(R.id.banner_item_time_dot);
			/*add by dragontec for bug 4366 end*/
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
                v.findViewById(R.id.order_title).setSelected(true);
                v.findViewById(R.id.order_title).setBackgroundResource(R.color._ff9c3c);
                v.findViewById(R.id.title).setSelected(true);
                v.requestFocus();
            } else {
                scaleToNormal(v.findViewById(R.id.item_layout));
                v.findViewById(R.id.order_title).setSelected(false);
                v.findViewById(R.id.order_title).setBackgroundResource(R.color._333333);
                v.findViewById(R.id.title).setSelected(false);
            }
			/*add by dragontec for bug 4325 start*/
            updateTitleText(hasFocus);
			/*add by dragontec for bug 4325 end*/
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_HOVER_ENTER:
				/*delete by dragontec for bug 4169 start*/
            	//case MotionEvent.ACTION_HOVER_MOVE:
				/*delete by dragontec for bug 4169 end*/
                    if (mSubscribeHoverListener!= null){
                        int position = (int) v.getTag(R.id.banner_item_position);
/*modify by dragontec for bug 4057 start*/
//                        mSubscribeHoverListener.onBannerHover(v, position, true);
                        mSubscribeHoverListener.onBannerHover(v, position, true, event.getButtonState() == MotionEvent.BUTTON_PRIMARY);
/*modify by dragontec for bug 4057 end*/
                    }
                    /*modify by dragontec for bug 4265 start*/
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
					/*modify by dragontec for bug 4265 end*/
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    if (mSubscribeHoverListener!= null){
                        int position = (int) v.getTag(R.id.banner_item_position);
/*modify by dragontec for bug 4057 start*/
//                        mSubscribeHoverListener.onBannerHover(v, position, false);
                        mSubscribeHoverListener.onBannerHover(v, position, false, event.getButtonState() == MotionEvent.BUTTON_PRIMARY);
/*modify by dragontec for bug 4057 end*/
                    }
                    break;
            }
            return false;
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
                TextView textView = (TextView) view;
                Object tag = itemView.findViewById(R.id.order_title).getTag();
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

        int getItemId(String url) {
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
