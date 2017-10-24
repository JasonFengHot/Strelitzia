package tv.ismar.homepage.banner.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.banner.BannerEntity;
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

        if ("更多".equals(title)){
            holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(R.drawable.banner_horizontal_more);
            holder.mItemView.findViewById(R.id.content_layout).setVisibility(View.INVISIBLE);
            holder.mTitle.setVisibility(View.INVISIBLE);
//            Picasso.with(mContext).load(R.drawable.banner_horizontal_more).into(holder.mImageView);
        }else {
            holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(android.R.color.transparent);
            holder.mItemView.findViewById(R.id.content_layout).setVisibility(View.VISIBLE);
            holder.mTitle.setVisibility(View.VISIBLE);

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
        //            holder.mTitle.setText("");
        //            loadSubscribeStatus(itemId, holder.mTitle, mSubscribeEntityList, position);
        //        } else if (entity.getSubscribeStatus() == BannerEntity.SubscribeStatus.Yes) {
        //            holder.mTitle.setText("已预约");
        //        } else if (entity.getSubscribeStatus() == BannerEntity.SubscribeStatus.No) {
        //            holder.mTitle.setText("预约");
        //        }

        holder.mTitle.setText("预约");

        //        if (itemId == subscribeStatusChangedItemId) {
        //            loadSubscribeStatus(itemId, holder.mTitle, mSubscribeEntityList, position);
        //        }

//            String timeString = entity.getOrder_date().getMonth() +"月" + entity.getOrder_date().getDate() + "日";
        holder.mPublishTime.setText(entity.getDisplay_order_date());
        Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(entity.getTop_left_corner())).into(holder.markLT);

        if (entity.getRating_average() != 0){
            holder.markRB.setText(new DecimalFormat("0.0").format(entity.getRating_average()));
            holder.markRB.setVisibility(View.VISIBLE);
        }else {
            holder.markRB.setVisibility(View.INVISIBLE);
        }

        holder.mIntroduction.setText(entity.getTitle() + " ");
        holder.mItemView.findViewById(R.id.item_layout).setTag(entity);
        holder.mItemView.findViewById(R.id.item_layout).setTag(R.id.banner_item_position, position);

        if (position == 0){
            holder.mLeftSpace.setVisibility(View.GONE);
        }else {
            holder.mLeftSpace.setVisibility(View.VISIBLE);
        }
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

        notifyItemRangeInserted(startIndex, endIndex - startIndex);
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
            implements View.OnClickListener, View.OnFocusChangeListener, View.OnHoverListener {

        private  Space mLeftSpace;
        private ImageView mImageView;
        private TextView mTitle;
        private TextView mPublishTime;
        private View mItemView;
        private TextView mIntroduction;
        private ImageView mTimeLine;
        private ImageView markLT;
        private TextView markRB;

        public SubscribeViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            mItemView.findViewById(R.id.item_layout).setOnClickListener(this);
            mItemView.findViewById(R.id.item_layout).setOnFocusChangeListener(this);
            mItemView.findViewById(R.id.item_layout).setOnHoverListener(this);
            mImageView = (ImageView) itemView.findViewById(R.id.image_view);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mPublishTime = (TextView) itemView.findViewById(R.id.publish_time);
            mIntroduction = (TextView) itemView.findViewById(R.id.introduction);
            mLeftSpace = (Space)itemView.findViewById(R.id.left_space);
            mTimeLine = (ImageView)itemView.findViewById(R.id.banner_item_timeline);
            markLT = (ImageView) itemView.findViewById(R.id.banner_mark_lt);
            markRB = (TextView)itemView.findViewById(R.id.banner_mark_br);
        }

        @Override
        public void onClick(View v) {
            if (mSubscribeClickListener != null) {
                int position = (int) v.getTag(R.id.banner_item_position);
                mSubscribeClickListener.onBannerClick(v, position);
            }
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                scaleToLarge(v.findViewById(R.id.item_layout));
                v.findViewById(R.id.title).setSelected(true);
                v.findViewById(R.id.title).setBackgroundResource(R.color._ff9c3c);
                v.findViewById(R.id.introduction).setSelected(true);
                v.requestFocus();
            } else {
                scaleToNormal(v.findViewById(R.id.item_layout));
                v.findViewById(R.id.title).setSelected(false);
                v.findViewById(R.id.title).setBackgroundResource(R.color._333333);
                v.findViewById(R.id.introduction).setSelected(false);
            }
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    if (mSubscribeHoverListener!= null){
                        int position = (int) v.getTag(R.id.banner_item_position);
/*modify by dragontec for bug 4057 start*/
//                        mSubscribeHoverListener.onBannerHover(v, position, true);
                        mSubscribeHoverListener.onBannerHover(v, position, true, event.getButtonState() == MotionEvent.BUTTON_PRIMARY);
/*modify by dragontec for bug 4057 end*/
                    }
                    v.requestFocus();
                    v.requestFocusFromTouch();
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
    }
}
