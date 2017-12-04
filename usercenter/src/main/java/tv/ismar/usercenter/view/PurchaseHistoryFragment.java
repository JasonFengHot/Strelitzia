package tv.ismar.usercenter.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseFragment;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Util;
import tv.ismar.app.network.entity.AccountsOrdersEntity;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.library.exception.ExceptionUtils;
import tv.ismar.usercenter.PurchaseHistoryContract;
import tv.ismar.usercenter.R;
import tv.ismar.usercenter.adapter.PurchaseHistoryListAdapter;
import tv.ismar.usercenter.databinding.FragmentPurchasehistoryBinding;
import tv.ismar.usercenter.viewmodel.PurchaseHistoryViewModel;
import tv.ismar.usercenter.widget.PurchaseHistoryRecyclerView;

/**
 * Created by huibin on 10/27/16.
 */

public class PurchaseHistoryFragment extends BaseFragment implements PurchaseHistoryContract.View {
    private static final String TAG = PurchaseHistoryFragment.class.getSimpleName();
    private PurchaseHistoryViewModel mViewModel;
    private PurchaseHistoryContract.Presenter mPresenter;


    public static PurchaseHistoryFragment newInstance() {
        return new PurchaseHistoryFragment();
    }

    private FragmentPurchasehistoryBinding purchasehistoryBinding;

    private PurchaseHistoryRecyclerView mRecyclerView;

    private boolean fragmentIsPause = false;


    private UserCenterActivity mUserCenterActivity;
    private Context mContext;

    private LinearLayoutManager mLayoutManager;
    private PurchaseHistoryListAdapter mAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
		mContext = context;
		mUserCenterActivity = (UserCenterActivity) getActivity();
		View purchaseHistoryIndicator = mUserCenterActivity.findViewById(R.id.usercenter_purchase_history);
		purchaseHistoryIndicator.setNextFocusRightId(purchaseHistoryIndicator.getId());
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        mUserCenterActivity = (UserCenterActivity) activity;
//        View purchaseHistoryIndicator = mUserCenterActivity.findViewById(R.id.usercenter_purchase_history);
//        purchaseHistoryIndicator.setNextFocusRightId(purchaseHistoryIndicator.getId());
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        purchasehistoryBinding = FragmentPurchasehistoryBinding.inflate(inflater, container, false);
        purchasehistoryBinding.setTasks(mViewModel);
        purchasehistoryBinding.setActionHandler(mPresenter);
        mRecyclerView = purchasehistoryBinding.recyclerview;
        mRecyclerView.setNextFocusLeftId(R.id.usercenter_purchase_history);
		return purchasehistoryBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
		mLayoutManager = new LinearLayoutManager(mContext);
		mRecyclerView.setLayoutManager(mLayoutManager);
		mAdapter = new PurchaseHistoryListAdapter();
		mAdapter.setOnItemHoveredListener(new PurchaseHistoryListAdapter.OnItemHoveredListener() {
			@Override
			public void onHovered(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_HOVER_ENTER:
					case MotionEvent.ACTION_HOVER_MOVE:
						if (!v.hasFocus()) {
							((UserCenterActivity) getActivity()).clearTheLastHoveredVewState();
							v.requestFocus();
							v.requestFocusFromTouch();
						}
						break;
					case MotionEvent.ACTION_HOVER_EXIT:
						if (!fragmentIsPause) {
//							purchasehistoryBinding.mainupView.requestFocus();
//							purchasehistoryBinding.mainupView.requestFocusFromTouch();
						}
						break;
				}
			}
		});
		mRecyclerView.setAdapter(mAdapter);
        mPresenter.start();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        AppConstant.purchase_page = "history";
        AppConstant.purchase_entrance_page = "expense_history";
        fragmentIsPause = false;
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
		Log.d(TAG, "onPause");
        fragmentIsPause = true;
        super.onPause();
    }

    @Override
    public void onStop() {
		Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		mPresenter.stop();
		mRecyclerView.setAdapter(null);
		mAdapter.setOnItemHoveredListener(null);
		mAdapter = null;
		mRecyclerView.setLayoutManager(null);
		mLayoutManager = null;
		mRecyclerView = null;
		purchasehistoryBinding = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
		Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
		mRecyclerView = null;
        Log.d(TAG, "onDetach");
        mUserCenterActivity = null;
        mContext = null;
		super.onDetach();
    }

    public void setViewModel(PurchaseHistoryViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public void setPresenter(PurchaseHistoryContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void loadAccountOrders(AccountsOrdersEntity accountsOrdersEntity) {


        ArrayList<AccountsOrdersEntity.OrderEntity> arrayList = new ArrayList<>();
        if (!TextUtils.isEmpty(IsmartvActivator.getInstance().getAuthToken()) && !TextUtils.isEmpty(IsmartvActivator.getInstance().getUsername())) {


            for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getOrder_list()) {
                entity.type = "order_list";
                arrayList.add(entity);
            }
            for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getSn_order_list()) {
                entity.type = "snorder_list";
                arrayList.add(entity);
            }

        } else {
            for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getSn_order_list()) {
                entity.type = "snorder_list";
                arrayList.add(entity);
            }
        }
        View purchaseHistoryIndicator = mUserCenterActivity.findViewById(R.id.usercenter_purchase_history);

        if (arrayList.isEmpty()) {
            purchaseHistoryIndicator.setNextFocusRightId(purchaseHistoryIndicator.getId());
        } else {
            purchaseHistoryIndicator.setNextFocusRightId(View.NO_ID);
        }
        createHistoryListView(arrayList);
    }

    /*add by dragontec for bug 4455 start*/
    private static final int History_List_Max_Count = 40;
    /*add by dragontec for bug 4455 end*/

    private void createHistoryListView(ArrayList<AccountsOrdersEntity.OrderEntity> orderEntities) {
    	if (mContext != null) {
			mAdapter.setData(orderEntities);
		}
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

    private int remaindDay(String exprieTime) {
        try {
            return Util.daysBetween(Util.getTime(), exprieTime) + 1;
        } catch (ParseException e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }
        return 0;
    }

}
