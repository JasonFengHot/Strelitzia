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
import android.widget.Space;
import android.widget.TextView;

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

        holder.mTitle.setText(entity.getTitle() + " " + position);
        holder.mItemView.findViewById(R.id.item_layout).setTag(entity);
        holder.mItemView.findViewById(R.id.item_layout).setTag(R.id.banner_item_position, position);

        Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(entity.getTop_left_corner())).into(holder.markLT);

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
        notifyItemRangeInserted(startIndex, endIndex - startIndex);
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
        void onBannerHover(View view, int position, boolean hovered);
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

    class SubscribeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnFocusChangeListener, View.OnHoverListener {

        private Space mLeftSpace;
        private ImageView mImageView;
        private TextView mTitle;
        private View mItemView;
        private ImageView markLT;
        private TextView markRB;


        public SubscribeViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            View itemLayoutView = mItemView.findViewById(R.id.item_layout);
            mItemView.findViewById(R.id.item_layout).setOnClickListener(this);
            mItemView.findViewById(R.id.item_layout).setOnFocusChangeListener(this);
            mItemView.findViewById(R.id.item_layout).setOnHoverListener(this);
            mImageView = (ImageView) itemView.findViewById(R.id.image_view);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mLeftSpace = (Space) itemView.findViewById(R.id.left_space);
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
            } else {
                scaleToNormal(v.findViewById(R.id.item_layout));
                v.findViewById(R.id.title).setSelected(false);
            }
        }

        private void scaleToLarge(View view) {
            JasmineUtil.scaleOut3(view);
        }

        private void scaleToNormal(View view) {
            JasmineUtil.scaleIn3(view);
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    if (mHoverListener != null) {
                        int position = (int) v.getTag(R.id.banner_item_position);
                        mHoverListener.onBannerHover(v, position, true);
                    }
                    v.requestFocusFromTouch();
                    v.requestFocus();
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    if (mHoverListener != null) {
                        int position = (int) v.getTag(R.id.banner_item_position);
                        mHoverListener.onBannerHover(v, position, false);
                    }
                    break;
            }
            return false;
        }
    }
}
