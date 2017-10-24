package tv.ismar.homepage.banner.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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

public class BannerMovieAdapter extends RecyclerView.Adapter<BannerMovieAdapter.SubscribeViewHolder>{
    private Context mContext;

    private List<BannerEntity.PosterBean> mSubscribeEntityList;
    private int currentPageNumber;
    private int totalPageCount;
    private int tatalItemCount;

    private BannerMovieAdapter.OnBannerHoverListener mHoverListener;

    public void setHoverListener(BannerMovieAdapter.OnBannerHoverListener hoverListener) {
        mHoverListener = hoverListener;
    }

    public interface OnBannerHoverListener {
/*modify by dragontec for bug 4057 start*/
//        void onBannerHover(View view, int position, boolean hovered);
        void onBannerHover(View view, int position, boolean hovered, boolean isPrimary);
/*modify by dragontec for bug 4057 end*/
    }



    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    public int getTotalPageCount() {
        return totalPageCount;
    }

    public int getTatalItemCount() {
        return tatalItemCount;
    }

    public BannerMovieAdapter(Context context, BannerEntity bannerEntity) {
        mContext = context;
        currentPageNumber = 1;
        totalPageCount = bannerEntity.getCount_pages();
        tatalItemCount = bannerEntity.getCount();

        //如果存在更多按钮，并且是在加载最后一页数据时，添加更多按钮的空数据
        if (bannerEntity.is_more() && bannerEntity.getNum_pages() == bannerEntity.getCount_pages()){
            BannerEntity.PosterBean posterBean = new BannerEntity.PosterBean();
            posterBean.setTitle("更多");
            //横版海报更多按钮
            posterBean.setPoster_url("more");
            bannerEntity.getPoster().add(posterBean);
        }

        mSubscribeEntityList = bannerEntity.getPoster();
    }

    @Override
    public SubscribeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner_movie, parent, false);
        SubscribeViewHolder holder = new SubscribeViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(SubscribeViewHolder holder, int position) {
        BannerEntity.PosterBean entity = mSubscribeEntityList.get(position);

        String imageUrl = entity.getVertical_url();
        String targetImageUrl = TextUtils.isEmpty(imageUrl) ? null : imageUrl;

        String title = entity.getTitle();



        holder.mTitle.setText(entity.getTitle() + " ");
        holder.mItemView.findViewById(R.id.item_layout).setTag(entity);
        holder.mItemView.findViewById(R.id.item_layout).setTag(R.id.banner_item_position, position);

        if ("更多".equals(title)){
            holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(R.drawable.banner_vertical_more);
            holder.mTitle.setVisibility(View.INVISIBLE);
            holder.itemWrapper.setVisibility(View.INVISIBLE);

        }else {
            holder.mItemView.findViewById(R.id.item_layout).setBackgroundResource(android.R.color.transparent);
            holder.mTitle.setVisibility(View.VISIBLE);
            holder.itemWrapper.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(targetImageUrl).placeholder(R.drawable.list_item_ppreview_bg)
                    .error(R.drawable.list_item_ppreview_bg).into(holder.mImageView);
        }

        Picasso.with(mContext).load(VipMark.getInstance().getBannerIconMarkImage(entity.getTop_left_corner())).into(holder.markLT);

        if (entity.getRating_average() != 0){
            holder.markRB.setText(new DecimalFormat("0.0").format(entity.getRating_average()));
            holder.markRB.setVisibility(View.VISIBLE);
        }else {
            holder.markRB.setVisibility(View.INVISIBLE);
        }

        if (position == 0){
            holder.mLeftSpace.setVisibility(View.GONE);
        }else {
            holder.mLeftSpace.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mSubscribeEntityList.size();
    }


    class SubscribeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnFocusChangeListener,View.OnHoverListener {

        private ImageView mImageView;
        private TextView mTitle;
        private View mItemView;
        private Space mLeftSpace;
        private ImageView markLT;
        private TextView markRB;
        private RelativeLayout itemWrapper;


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
        public SubscribeViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            itemWrapper = (RelativeLayout) mItemView.findViewById(R.id.item_wrapper);
            mItemView.findViewById(R.id.item_layout).setOnClickListener(this);
            mItemView.findViewById(R.id.item_layout).setOnFocusChangeListener(this);
            mItemView.findViewById(R.id.item_layout).setOnHoverListener(this);
            mImageView = (ImageView) itemView.findViewById(R.id.image_view);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mLeftSpace = (Space)itemView.findViewById(R.id.left_space);
            markLT = (ImageView) itemView.findViewById(R.id.banner_mark_lt);
            markRB = (TextView)itemView.findViewById(R.id.banner_mark_br);
        }

        @Override
        public void onClick(View v) {
            Log.d("BannerMovieAdapter", "onClick");
            if (mSubscribeClickListener != null) {
                int position = (int) v.getTag(R.id.banner_item_position);
                mSubscribeClickListener.onBannerClick(v, position);
            }
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {

            if (hasFocus){
                scaleToLarge(v.findViewById(R.id.item_layout));
                v.findViewById(R.id.title).setSelected(true);
            }else {
                scaleToNormal(v.findViewById(R.id.item_layout));
                v.findViewById(R.id.title).setSelected(false);
            }
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    if (mHoverListener!= null){
                        int position = (int) v.getTag(R.id.banner_item_position);
/*modify by dragontec for bug 4057 start*/
//                        mHoverListener.onBannerHover(v, position, true);
                        mHoverListener.onBannerHover(v, position, true, event.getButtonState() == MotionEvent.BUTTON_PRIMARY);
/*modify by dragontec for bug 4057 end*/
                    }
                    v.requestFocusFromTouch();
                    v.requestFocus();
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    if (mHoverListener!= null){
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
        private void scaleToLarge(View view) {
            JasmineUtil.scaleOut3(view);
        }

        private void scaleToNormal(View view) {
            JasmineUtil.scaleIn3(view);
        }
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

    private OnBannerClickListener mSubscribeClickListener;

    public interface OnBannerClickListener {
        void onBannerClick(View view, int position);
    }

    public void setSubscribeClickListener(OnBannerClickListener subscribeClickListener) {
        mSubscribeClickListener = subscribeClickListener;
    }
}
