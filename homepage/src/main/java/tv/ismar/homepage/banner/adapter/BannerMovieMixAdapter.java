package tv.ismar.homepage.banner.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.homepage.R;

import static android.view.View.SCALE_X;
import static android.view.View.SCALE_Y;

/**
 * Created by huibin on 25/08/2017.
 */

public class BannerMovieMixAdapter extends RecyclerView.Adapter<BannerMovieMixAdapter.SubscribeViewHolder>{
    private Context mContext;

    private List<BannerEntity.PosterBean> mSubscribeEntityList;


    public BannerMovieMixAdapter(Context context, List<BannerEntity.PosterBean> subscribeEntityList) {
        mContext = context;
        mSubscribeEntityList = subscribeEntityList;
    }

    @Override
    public SubscribeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner_movie, parent, false);
        SubscribeViewHolder holder = new SubscribeViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(SubscribeViewHolder holder, int position) {
        if (position == 0){
            ViewGroup.LayoutParams itemLayoutParams = holder.itemLayout.getLayoutParams();
            itemLayoutParams.width = 720;
            holder.itemLayout.setLayoutParams(itemLayoutParams);


            ViewGroup.LayoutParams wrapperLayoutParams = holder.itemWrapper.getLayoutParams();
            wrapperLayoutParams.width = 720;
            holder.itemWrapper.setLayoutParams(wrapperLayoutParams);

            ViewGroup.LayoutParams imageLayoutParams = holder.mImageView.getLayoutParams();
            imageLayoutParams.width = 720;
            holder.mImageView.setLayoutParams(imageLayoutParams);

            ViewGroup.LayoutParams titleLayoutParams = holder.mTitle.getLayoutParams();
            titleLayoutParams.width = 720;
            holder.mTitle.setLayoutParams(titleLayoutParams);
        }

        BannerEntity.PosterBean entity = mSubscribeEntityList.get(position);
        Picasso.with(mContext).load(entity.getPoster_url()).into(holder.mImageView);
        holder.mTitle.setText(entity.getTitle() + "大沙发发斯蒂芬骄傲的快速；房间爱的开始；就发阿打算开发阶段可莱丝；发简历；卡代课老师； ");
    }

    @Override
    public int getItemCount() {
        return mSubscribeEntityList.size();
    }


    class SubscribeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnFocusChangeListener {

        private ImageView mImageView;
        private TextView mTitle;
        private View mItemView;
        private LinearLayout itemLayout;
        private RelativeLayout itemWrapper;


        public SubscribeViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            itemLayout = (LinearLayout) mItemView.findViewById(R.id.item_layout);
            itemWrapper = (RelativeLayout) mItemView.findViewById(R.id.item_wrapper);
            mItemView.findViewById(R.id.item_layout).setOnClickListener(this);
            mItemView.findViewById(R.id.item_layout).setOnFocusChangeListener(this);
            mImageView = (ImageView) itemView.findViewById(R.id.image_view);
            mTitle = (TextView) itemView.findViewById(R.id.title);
        }

        @Override
        public void onClick(View v) {

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

        private void scaleToLarge(View view) {
            ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(view, SCALE_X, 1.0F, 1.1F);
            objectAnimatorX.setDuration(100L);
            objectAnimatorX.start();
            ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(view, SCALE_Y, 1.0F, 1.1F);
            objectAnimatorY.setDuration(100L);
            objectAnimatorY.start();
        }


        private void scaleToNormal(View view) {
            ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(view, SCALE_X, 1.1F, 1.0F);
            objectAnimatorX.setDuration(100L);
            objectAnimatorX.start();
            ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(view, SCALE_Y, 1.1F, 1.0F);
            objectAnimatorY.setDuration(100L);
            objectAnimatorY.start();
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
}