package tv.ismar.daisy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tv.ismar.app.models.PlayfinishedRecommend;
import tv.ismar.app.ui.adapter.OnItemClickListener;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;

public class PlayFinishedAdapter extends RecyclerView.Adapter<PlayFinishedAdapter.PlayViewHolder>{

		private Context mContext;
		private ArrayList<PlayfinishedRecommend.RecommendItem> mData;
	    private boolean mIsVertical;
		private OnItemClickListener itemClickListener;
	    private OnItemFocusedListener itemFocusedListener;
		private boolean firstIn=true;

	public OnItemClickListener getItemClickListener() {
		return itemClickListener;
	}

	public void setItemClickListener(OnItemClickListener itemClickListener) {
		this.itemClickListener = itemClickListener;
	}

	public OnItemFocusedListener getItemFocusedListener() {
		return itemFocusedListener;
	}

	public void setItemFocusedListener(OnItemFocusedListener itemFocusedListener) {
		this.itemFocusedListener = itemFocusedListener;
	}

	public PlayFinishedAdapter(Context mContext, ArrayList<PlayfinishedRecommend.RecommendItem> mData, boolean mIsVertical) {
		this.mContext = mContext;
		this.mData = mData;
		this.mIsVertical = mIsVertical;
	}

		@Override
		public PlayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			PlayViewHolder playViewHolder;
			if(mIsVertical) {
				 playViewHolder = new PlayViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_vertical_poster, null));
			}else{
				 playViewHolder = new PlayViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_horizontal_poster, null));
			}
				return playViewHolder;
		}


	@Override
	public void onBindViewHolder(final PlayViewHolder holder, final int position) {
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				itemClickListener.onItemClick(v,position);
			}
		});
		holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				itemFocusedListener.onItemfocused(v,position,hasFocus);
			}
		});
		PlayfinishedRecommend.RecommendItem item=mData.get(position);
		if(mIsVertical){
			holder.item_vertical_poster_title.setText(item.getTitle());
			if(!TextUtils.isEmpty(item.getVertical_url())){
				Picasso.with(mContext).load(item.getVertical_url()).placeholder(R.drawable.list_item_ppreview_bg)
						.error(R.drawable.list_item_ppreview_bg).into(holder.item_vertical_poster_image);
			}
		}else {
			holder.item_horizontal_poster_title.setText(item.getTitle());
			if(item.getTitle().toCharArray().length<=16){
				holder.item_horizontal_poster_title.setGravity(Gravity.CENTER);
			}else{
				holder.item_horizontal_poster_title.setGravity(Gravity.CENTER_VERTICAL);
			}
			if (!TextUtils.isEmpty(item.getPoster_url())) {
				Picasso.with(mContext).load(item.getPoster_url()).placeholder(R.drawable.list_item_preview_bg)
						.error(R.drawable.list_item_preview_bg).into(holder.item_horizontal_poster_image);

			}
		}
		if(firstIn&&position==0){
			firstIn=false;
			holder.itemView.requestFocus();
		}
	}

		@Override
		public int getItemCount() {
			return mData.size();
		}

	public class PlayViewHolder extends RecyclerView.ViewHolder{

		private final ImageView item_horizontal_poster_image;
		private final TextView item_horizontal_poster_title;
		private final ImageView item_vertical_poster_image;
		private final TextView item_vertical_poster_title;

		public PlayViewHolder(View itemView) {
			super(itemView);
			item_horizontal_poster_image = (ImageView) itemView.findViewById(R.id.item_horizontal_poster_image);
			item_horizontal_poster_title = (TextView) itemView.findViewById(R.id.item_horizontal_poster_title);
			item_vertical_poster_image = (ImageView) itemView.findViewById(R.id.item_vertical_poster_image);
			item_vertical_poster_title = (TextView) itemView.findViewById(R.id.item_vertical_poster_title);
		}

	}
}
