package tv.ismar.usercenter.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Util;
import tv.ismar.app.network.entity.AccountsOrdersEntity;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.library.exception.ExceptionUtils;
import tv.ismar.usercenter.R;
import tv.ismar.usercenter.view.RelativeLayoutContainer;

/**
 * Created by liujy on 2017/11/16.
 */

public class PurchaseHistoryListAdapter extends RecyclerView.Adapter<PurchaseHistoryListAdapter.PurchaseHistoryListViewHolder> {

	public static final String TAG = PurchaseHistoryListAdapter.class.getSimpleName();

	private ArrayList<AccountsOrdersEntity.OrderEntity> mData = null;
	private Context mContext;
	private OnItemHoveredListener mOnItemHoveredListener;

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		mContext = recyclerView.getContext();
	}

	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
		mContext = null;
		super.onDetachedFromRecyclerView(recyclerView);
	}

	public void setData(ArrayList<AccountsOrdersEntity.OrderEntity> data) {
		mData = data;
		notifyDataSetChanged();
	}

	public ArrayList<AccountsOrdersEntity.OrderEntity> getData() {
		return mData;
	}

	private int remaindDay(String exprieTime) {
		try {
			return Util.daysBetween(Util.getTime(), exprieTime) + 1;
		} catch (ParseException e) {
			ExceptionUtils.sendProgramError(e);
			e.printStackTrace();
		}
		return 0;
	}

	private String getValueBySource(String source) {
		if (source.equals("weixin")) {
			return "微信";
		} else if (source.equals("alipay")) {
			return "支付宝";
		} else if (source.equals("balance")) {
			return "余额";
		} else if (source.equals("card")) {
			return "卡";
		} else {
			return source;
		}
	}

	@Override
	public PurchaseHistoryListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = null;
		if (mContext != null) {
		 	view = LayoutInflater.from(mContext).inflate(R.layout.item_purchase_history, parent, false);
		}
		return view != null ? new PurchaseHistoryListViewHolder(view) : null;
	}

	@Override
	public void onBindViewHolder(PurchaseHistoryListViewHolder holder, int position) {
		try {
			AccountsOrdersEntity.OrderEntity item = mData.get(position);
			String orderday = holder.itemView.getResources().getString(R.string.personcenter_orderlist_item_orderday);
			String remainday = holder.itemView.getResources().getString(R.string.personcenter_orderlist_item_remainday);
			String cost = holder.itemView.getResources().getString(R.string.personcenter_orderlist_item_cost);
			String paySource = holder.itemView.getResources().getString(R.string.personcenter_orderlist_item_paysource);
			holder.title.setText(item.getTitle());
			holder.buydate_txt.setText(String.format(orderday, item.getStart_date()));
			holder.orderlistitem_remainday.setText(String.format(remainday, remaindDay(item.getExpiry_date())));
			holder.totalfee.setText(String.format(cost, item.getTotal_fee()));
			holder.orderlistitem_paychannel.setText(String.format(paySource, getValueBySource(item.getSource())));
			if (!TextUtils.isEmpty(item.getThumb_url()))
				Picasso.with(mContext).load(item.getThumb_url())
						.memoryPolicy(MemoryPolicy.NO_STORE)
						.memoryPolicy(MemoryPolicy.NO_CACHE)
						.placeholder(R.drawable.item_horizontal_preview)
						.error(R.drawable.item_horizontal_preview)
						.config(Bitmap.Config.RGB_565)
						.tag(TAG)
						.into(holder.icon);
			if (!TextUtils.isEmpty(item.getInfo())) {
				String account = item.getInfo().split("@")[0];
				String mergedate = item.getInfo().split("@")[1];
				SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
				String mergeTime = time.format(Timestamp.valueOf(mergedate));
				if (item.type.equals("order_list")) {
					holder.purchaseExtra.setText("( " + mergeTime + "合并至视云账户" + IsmartvActivator.getInstance().getUsername() + " )");
				} else if (item.type.equals("snorder_list")) {
					holder.purchaseExtra.setText(mergeTime + "合并至视云账户" + account);
				}
				holder.purchaseExtra.setVisibility(View.VISIBLE);
				holder.mergeTxt.setVisibility(View.INVISIBLE);
			} else {
				holder.purchaseExtra.setVisibility(View.INVISIBLE);
				holder.mergeTxt.setVisibility(View.INVISIBLE);
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		holder.initListener();
	}

	@Override
	public void onViewRecycled(PurchaseHistoryListViewHolder holder) {
		holder.unInitListener();
		super.onViewRecycled(holder);
	}

	@Override
	public int getItemCount() {
		return mData != null ? mData.size() : 0;
	}

	public class PurchaseHistoryListViewHolder extends RecyclerView.ViewHolder implements View.OnHoverListener, View.OnKeyListener, View.OnClickListener {
		public RelativeLayoutContainer container;
		public TextView title;
		public TextView buydate_txt;
		public TextView orderlistitem_remainday;
		public TextView totalfee;
		public RecyclerImageView icon;
		public TextView orderlistitem_paychannel;
		public TextView purchaseExtra;
		public TextView mergeTxt;


		public PurchaseHistoryListViewHolder(View itemView) {
			super(itemView);
			container = (RelativeLayoutContainer) itemView.findViewById(R.id.item_container);
			container.setNextFocusLeftId(R.id.usercenter_purchase_history);
			title = (TextView) itemView.findViewById(R.id.orderlistitem_title);
            buydate_txt = (TextView) itemView.findViewById(R.id.orderlistitem_time);
            orderlistitem_remainday = (TextView) itemView.findViewById(R.id.orderlistitem_remainday);
            totalfee = (TextView) itemView.findViewById(R.id.orderlistitem_cost);
            icon = (RecyclerImageView) itemView.findViewById(R.id.orderlistitem_icon);
            orderlistitem_paychannel = (TextView) itemView.findViewById(R.id.orderlistitem_paychannel);
            purchaseExtra = (TextView) itemView.findViewById(R.id.purchase_extra);
            mergeTxt = (TextView) itemView.findViewById(R.id.orderlistitem_merge);
		}

		public void initListener() {
			container.setOnHoverListener(this);
			container.setOnKeyListener(this);
			container.setOnClickListener(this);
		}

		public void unInitListener() {
			container.setOnHoverListener(null);
			container.setOnKeyListener(null);
			container.setOnClickListener(null);
		}

		@Override
		public boolean onHover(View v, MotionEvent event) {
			if (mOnItemHoveredListener != null) {
				mOnItemHoveredListener.onHovered(v, event);
			}
			return false;
		}

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (getAdapterPosition() == 0 && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				return true;
			}
			if (getAdapterPosition() == mData.size() - 1 && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				return true;
			}
			return false;
		}

		@Override
		public void onClick(View v) {
			try {
				AccountsOrdersEntity.OrderEntity orderEntity = mData.get(getAdapterPosition());
				if (TextUtils.isEmpty(orderEntity.getUrl())) {
					Toast.makeText(mContext, "url is empty!!!", Toast.LENGTH_SHORT).show();
					return;
				}
				List<String> pathSegments = Uri.parse(orderEntity.getUrl()).getPathSegments();
				String pk = pathSegments.get(pathSegments.size() - 1);
				String type = pathSegments.get(pathSegments.size() - 2);
				PageIntent pageIntent = new PageIntent();
				switch (type) {
					case "package":
						pageIntent.toPackageDetail(mContext, "history", Integer.parseInt(pk));
						break;
					case "item":
						pageIntent.toDetailPage(mContext, "history", Integer.parseInt(pk));
						break;
					default:
						throw new IllegalArgumentException(orderEntity.getUrl() + " type not support!!!");
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			}

		}
	}

	public interface OnItemHoveredListener {
		void onHovered(View v, MotionEvent event);
	}

	public void setOnItemHoveredListener(OnItemHoveredListener onItemHoveredListener) {
		mOnItemHoveredListener = onItemHoveredListener;
	}
}
