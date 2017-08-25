package tv.ismar.homepage.banner.subscribe;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.homepage.R;

/**
 * Created by huibin on 25/08/2017.
 */

public class BannerSubscribeAdapter extends RecyclerView.Adapter<BannerSubscribeAdapter.SubscribeViewHolder> {
    private Context mContext;

    private List<BannerSubscribeEntity> mSubscribeEntityList;


    public BannerSubscribeAdapter(Context context, List<BannerSubscribeEntity> subscribeEntityList) {
        mContext = context;
        mSubscribeEntityList = subscribeEntityList;
    }

    @Override
    public SubscribeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner_subscribe, parent, false);
        SubscribeViewHolder holder = new SubscribeViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(SubscribeViewHolder holder, int position) {
        BannerSubscribeEntity entity = mSubscribeEntityList.get(position);
        Picasso.with(mContext).load(entity.getImage_url()).into(holder.mImageView);
        holder.mTitle.setText(entity.getTitle());
    }

    @Override
    public int getItemCount() {
        return mSubscribeEntityList.size();
    }

    class SubscribeViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private TextView mTitle;


        public SubscribeViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.image_view);
            mTitle = (TextView) itemView.findViewById(R.id.title);
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

            // Add top margin only for the first item to avoid double space between items
//            int position = parent.getChildAdapterPosition(view);
//            if (position != parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = space;
            outRect.top = space;
            outRect.right = space;
            outRect.left = space;
//            }
        }
    }
}
