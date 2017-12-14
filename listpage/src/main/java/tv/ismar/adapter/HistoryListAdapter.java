package tv.ismar.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.app.ui.adapter.OnItemKeyListener;
import tv.ismar.app.ui.adapter.OnItemOnhoverlistener;
import tv.ismar.app.entity.HistoryFavoriteEntity;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.listener.LfListItemClickListener;
import tv.ismar.listpage.R;
import tv.ismar.view.IsmartvLinearLayout;

/**
 * Created by liucan on 2017/8/24.
 */
public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.HistoryViewHolder> {
	private Context mContext;
	private List<HistoryFavoriteEntity> items;
	private boolean isVisibility = false;
	private OnItemFocusedListener itemFocusedListener;
	private LfListItemClickListener itemClickListener;
	private OnItemOnhoverlistener itemOnhoverlistener;
	private OnItemKeyListener itemKeyListener;
	private String type = "history";
	private String lastTime = "";

	public HistoryListAdapter(Context context, List<HistoryFavoriteEntity> items1, String itemType) {
		mContext = context;
		items = items1;
		type = itemType;
	}

	public void setItemClickListener(LfListItemClickListener itemClickListener) {
		this.itemClickListener = itemClickListener;
	}

	public void setItemOnhoverlistener(OnItemOnhoverlistener itemOnhoverlistener) {
		this.itemOnhoverlistener = itemOnhoverlistener;
	}

	public void setItemFocusedListener(OnItemFocusedListener itemFocusedListener) {
		this.itemFocusedListener = itemFocusedListener;
	}

	public void setItemKeyListener(OnItemKeyListener itemKeyListener) {
		this.itemKeyListener = itemKeyListener;
	}

	@Override
	public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new HistoryViewHolder(LayoutInflater.from(mContext).inflate(R.layout.history_favorite_list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(final HistoryViewHolder holder, final int position) {
		HistoryFavoriteEntity item = items.get(position);
		if (item.getType() != 2) {
			if (item.getAdlet_url() != null && !item.getAdlet_url().isEmpty()) {
/*modify by dragontec for bug 4336 start*/
				Picasso.with(mContext).load(item.getAdlet_url()).
/*add by dragontec for bug 4205 start*/
						memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).
/*add by dragontec for bug 4205 end*/
						placeholder(R.drawable.item_horizontal_preview).
						error(R.drawable.item_horizontal_preview).
						into(holder.item_detail_image);
/*modify by dragontec for bug 4336 end*/
			} else {
/*modify by dragontec for bug 4336 start*/
				Picasso.with(mContext).load(R.drawable.item_horizontal_preview).into(holder.item_detail_image);
/*modify by dragontec for bug 4336 end*/
			}
			holder.item_title.setText(item.getTitle());
			holder.item_title.setVisibility(View.VISIBLE);
			holder.item_title_layout.setVisibility(View.VISIBLE);
			holder.item_detail_image.setVisibility(View.VISIBLE);
			holder.more.setVisibility(View.GONE);
			holder.item_time_node.setVisibility(View.VISIBLE);
			if (item.getDate() != 0 && item.isShowDate()) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(item.getDate());
				calendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
				holder.item_time.setText(String.format(Locale.getDefault(), "%02d月%02d日", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
				holder.item_time_node.setVisibility(View.VISIBLE);
				holder.item_time.setVisibility(View.VISIBLE);
			} else {
				holder.item_time.setVisibility(View.GONE);
				holder.item_time_node.setVisibility(View.GONE);
			}

		} else {
			holder.item_title_layout.setVisibility(View.GONE);
			holder.item_detail_image.setVisibility(View.GONE);
			holder.more.setVisibility(View.VISIBLE);
			holder.item_time_node.setVisibility(View.GONE);
			holder.item_time.setVisibility(View.GONE);
		}

		holder.item_detail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				int pos = holder.getLayoutPosition();
				itemFocusedListener.onItemfocused(v, pos, hasFocus);
				if (hasFocus) {
					holder.item_title.setSelected(true);
					holder.item_title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
				} else {
					holder.item_title.setSelected(false);
					holder.item_title.setEllipsize(TextUtils.TruncateAt.END);
				}

			}
		});

		holder.item_detail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int[] location = new int[]{0, 0};
				v.getLocationOnScreen(location);
				int screenWidth = v.getResources().getDisplayMetrics().widthPixels;
				int screenHeight = v.getResources().getDisplayMetrics().heightPixels;
				if (location[0] >= 0 && location[1] >= 0 && location[0] + v.getWidth() <= screenWidth && location[1] + v.getHeight() <= screenHeight) {
					itemClickListener.onlfItemClick(v, holder.getAdapterPosition(), type);
				}
			}
		});
		holder.item_detail.setOnHoverListener(new View.OnHoverListener() {
			@Override
			public boolean onHover(View v, MotionEvent event) {
				if (type.equals("history")) {
					itemOnhoverlistener.OnItemOnhoverlistener(v, event, holder.getAdapterPosition(), 0);
				} else {
					itemOnhoverlistener.OnItemOnhoverlistener(v, event, holder.getAdapterPosition(), 1);
				}
				return false;
			}
		});
		holder.item_detail.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					itemKeyListener.onItemKeyListener(v, keyCode, event);
					if (holder.getAdapterPosition() == items.size() - 1 && keyCode == 22) {
						YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(holder.item_detail);
						return true;
					}
					if (holder.getAdapterPosition() == 0 && keyCode == 21) {
						YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(holder.item_detail);
						return true;
					}
				}
				return false;
			}
		});
	}

	@Override
	public void onViewRecycled(HistoryViewHolder holder) {
		if (holder.item_detail != null) {
			holder.item_detail.setOnKeyListener(null);
			holder.item_detail.setOnHoverListener(null);
			holder.item_detail.setOnClickListener(null);
			holder.item_detail.setOnFocusChangeListener(null);
		}
		super.onViewRecycled(holder);
	}

	public void setSettingVisibility(boolean visibility) {
		isVisibility = visibility;
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public static class HistoryViewHolder extends RecyclerView.ViewHolder {
		RecyclerImageView item_time_node;
		RecyclerImageView item_detail_image;
		TextView item_time;
		TextView item_title;
		IsmartvLinearLayout item_detail;
		RelativeLayout item_title_layout;
		RecyclerImageView more;


		public HistoryViewHolder(View itemView) {
			super(itemView);
			item_time = (TextView) itemView.findViewById(R.id.item_time);
			item_detail_image = (RecyclerImageView) itemView.findViewById(R.id.item_detail_image);
			item_time_node = (RecyclerImageView) itemView.findViewById(R.id.item_time_node);
			item_title = (TextView) itemView.findViewById(R.id.item_title);
			item_detail = (IsmartvLinearLayout) itemView.findViewById(R.id.item_detail);
			more = (RecyclerImageView) itemView.findViewById(R.id.more);
			item_title_layout = (RelativeLayout) itemView.findViewById(R.id.item_title_layout);
		}
	}
}
