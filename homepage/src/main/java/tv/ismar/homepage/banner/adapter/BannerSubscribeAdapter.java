package tv.ismar.homepage.banner.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.entity.banner.AccountsItemSubscribeExistsEntity;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.homepage.R;

import static android.view.View.SCALE_X;
import static android.view.View.SCALE_Y;

/**
 * Created by huibin on 25/08/2017.
 */

public class BannerSubscribeAdapter extends RecyclerView.Adapter<BannerSubscribeAdapter.SubscribeViewHolder> {
    private Context mContext;

    private List<BannerEntity.PosterBean> mSubscribeEntityList;
    private OnSubscribeClickListener mSubscribeClickListener;

    private int subscribeStatusChangedItemId;

    public void setSubscribeStatusChangedItemId(int subscribeStatusChangedItemId) {
        this.subscribeStatusChangedItemId = subscribeStatusChangedItemId;
    }

    public void setSubscribeEntityList(List<BannerEntity.PosterBean> subscribeEntityList) {
        mSubscribeEntityList = subscribeEntityList;
    }

    public BannerSubscribeAdapter(Context context, List<BannerEntity.PosterBean> subscribeEntityList) {
        mContext = context;
//        mSubscribeEntityList = subscribeEntityList;
        mSubscribeEntityList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            mSubscribeEntityList.addAll(subscribeEntityList);
        }

    }

    @Override
    public SubscribeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner_subscribe, parent, false);
        SubscribeViewHolder holder = new SubscribeViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(SubscribeViewHolder holder, int position) {
        BannerEntity.PosterBean entity = mSubscribeEntityList.get(position);
        Picasso.with(mContext).load(entity.getPoster_url()).into(holder.mImageView);

        int itemId = getMovieItemId(entity.getContent_url());

        if (entity.getSubscribeStatus() == BannerEntity.SubscribeStatus.None) {
            holder.mTitle.setText("");
            loadSubscribeStatus(itemId, holder.mTitle, mSubscribeEntityList, position);
        } else if (entity.getSubscribeStatus() == BannerEntity.SubscribeStatus.Yes) {
            holder.mTitle.setText("已预约");
        } else if (entity.getSubscribeStatus() == BannerEntity.SubscribeStatus.No) {
            holder.mTitle.setText("预约");
        }

        if (itemId == subscribeStatusChangedItemId){
            loadSubscribeStatus(itemId, holder.mTitle, mSubscribeEntityList, position);
        }
        holder.mPublishTime.setText("6月30日");
        holder.mIntroduction.setText(entity.getIntroduction() + "打飞机；大街上；发动快速路附近啊代课老师； ");
        holder.mItemView.findViewById(R.id.item_layout).setTag(entity);

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

    private void loadSubscribeStatus(int itemId, final TextView textView, final List<BannerEntity.PosterBean> subscribeEntityList, final int position) {
        SkyService.ServiceManager.getService().accountsItemSubscribeExists(itemId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AccountsItemSubscribeExistsEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        textView.setText("预约");
                    }

                    @Override
                    public void onNext(AccountsItemSubscribeExistsEntity entity) {
                        if (entity.getInfo().getStatus() == 1) {
                            subscribeEntityList.get(position).setSubscribeStatus(BannerEntity.SubscribeStatus.Yes);
                            textView.setText("已预约");
                        } else if (entity.getInfo().getStatus() == 0) {
                            subscribeEntityList.get(position).setSubscribeStatus(BannerEntity.SubscribeStatus.No);
                            textView.setText("预约");
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mSubscribeEntityList.size();
    }


    class SubscribeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnFocusChangeListener {

        private ImageView mImageView;
        private TextView mTitle;
        private TextView mPublishTime;
        private View mItemView;
        private TextView mIntroduction;


        public SubscribeViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            mItemView.findViewById(R.id.item_layout).setOnClickListener(this);
            mItemView.findViewById(R.id.item_layout).setOnFocusChangeListener(this);
            mImageView = (ImageView) itemView.findViewById(R.id.image_view);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mPublishTime = (TextView) itemView.findViewById(R.id.publish_time);
            mIntroduction = (TextView) itemView.findViewById(R.id.introduction);
        }

        @Override
        public void onClick(View v) {
            if (mSubscribeClickListener != null) {
                BannerEntity.PosterBean posterBean = (BannerEntity.PosterBean) v.getTag();
                int itemId = getItemId(posterBean.getContent_url());
                String contentModel = posterBean.getContent_model();
                mSubscribeClickListener.onSubscribeClick(itemId, contentModel);
            }
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                scaleToLarge(v.findViewById(R.id.item_layout));
                v.findViewById(R.id.title).setSelected(true);
                v.findViewById(R.id.introduction).setSelected(true);
            } else {
                scaleToNormal(v.findViewById(R.id.item_layout));
                v.findViewById(R.id.title).setSelected(false);
                v.findViewById(R.id.introduction).setSelected(false);
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

    public void setSubscribeClickListener(OnSubscribeClickListener subscribeClickListener) {
        mSubscribeClickListener = subscribeClickListener;
    }

    public interface OnSubscribeClickListener {
        void onSubscribeClick(int pk, String contentModel);
    }

}
