package tv.ismar.usercenter.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseFragment;
import tv.ismar.app.core.Util;
import tv.ismar.app.network.entity.AccountBalanceEntity;
import tv.ismar.app.network.entity.AccountPlayAuthEntity;
import tv.ismar.app.ui.MessageDialogFragment;
import tv.ismar.usercenter.R;
import tv.ismar.usercenter.UserInfoContract;
import tv.ismar.usercenter.databinding.FragmentUserinfoBinding;
import tv.ismar.usercenter.viewmodel.UserInfoViewModel;

/**
 * Created by huibin on 10/27/16.
 */

public class UserInfoFragment extends BaseFragment implements UserInfoContract.View, IsmartvActivator.AccountChangeCallback {
    private static final String TAG = UserInfoFragment.class.getSimpleName();
    private UserInfoViewModel mViewModel;
    private UserInfoContract.Presenter mPresenter;


    private RecyclerViewTV privilegeRecyclerView;


    public static UserInfoFragment newInstance() {
        return new UserInfoFragment();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        IsmartvActivator.getInstance().addAccountChangeListener(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        final FragmentUserinfoBinding userinfoBinding = FragmentUserinfoBinding.inflate(inflater, container, false);
        userinfoBinding.setTasks(mViewModel);
        userinfoBinding.setActionHandler(mPresenter);
        userinfoBinding.fragmentContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (userinfoBinding.exitAccount.getVisibility() == View.VISIBLE) {
                    userinfoBinding.exitAccount.requestFocus();
                }
            }
        });

        privilegeRecyclerView = userinfoBinding.privilegeRecycler;
        privilegeRecyclerView.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.privilege_item_margin_bottom)));
        View root = userinfoBinding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
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
        Log.d(TAG, "onResume");
        mPresenter.start();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        IsmartvActivator.getInstance().removeAccountChangeListener(this);
        super.onDestroy();
        Log.d(TAG, "onDestroy");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

    public void setViewModel(UserInfoViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public void setPresenter(UserInfoContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void loadPrivilege(AccountPlayAuthEntity entity) {
        mViewModel.refresh();
        ArrayList<AccountPlayAuthEntity.PlayAuth> playAuths = new ArrayList<>();
        playAuths.addAll(entity.getSn_playauth_list());
        playAuths.addAll(entity.getPlayauth_list());
        privilegeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        PrivilegeAdapter privilegeAdapter = new PrivilegeAdapter(getContext(), playAuths);
        privilegeRecyclerView.setAdapter(privilegeAdapter);
    }

    @Override
    public void loadBalance(AccountBalanceEntity entity) {
        mViewModel.refresh();
    }


    public void showExitAccountConfirmPop() {
        final MessageDialogFragment dialog = new MessageDialogFragment(getContext(), getString(R.string.confirm_exit_account_text), null);
        dialog.showAtLocation(getView(), Gravity.CENTER, new MessageDialogFragment.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        dialog.dismiss();
                        IsmartvActivator.getInstance().removeUserInfo();

                    }
                },
                new MessageDialogFragment.CancelListener() {
                    @Override
                    public void cancelClick(View view) {
                        dialog.dismiss();
                    }
                }

        );


    }

    private void showExitAccountMessagePop() {
        final MessageDialogFragment dialog = new MessageDialogFragment(getContext(), getString(R.string.exit_account_message_text), null);
        dialog.showAtLocation(getView(), Gravity.CENTER, new MessageDialogFragment.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        dialog.dismiss();

                    }
                },
                null
        );
    }

    @Override
    public void onLogout() {
        showExitAccountMessagePop();
        mPresenter.fetchBalance();
        mPresenter.fetchPrivilege();
        mViewModel.refresh();
    }


    private class PrivilegeAdapter extends RecyclerView.Adapter<PrivilegeViewHolder> {
        private Context mContext;

        private List<AccountPlayAuthEntity.PlayAuth> mPlayAuths;

        public PrivilegeAdapter(Context context, List<AccountPlayAuthEntity.PlayAuth> playAuths) {
            mContext = context;
            mPlayAuths = playAuths;
        }

        @Override
        public PrivilegeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.privilege_listview_item, parent, false);
            PrivilegeViewHolder holder = new PrivilegeViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(PrivilegeViewHolder holder, int position) {
            AccountPlayAuthEntity.PlayAuth playAuth = mPlayAuths.get(position);

            String remainday = mContext.getResources().getString(R.string.personcenter_orderlist_item_remainday);
            holder.date.setText(String.format(remainday, remaindDay(playAuth.getExpiry_date())));
            holder.title.setText(playAuth.getTitle());

        }

        @Override
        public int getItemCount() {
            return mPlayAuths.size();
        }

        private int remaindDay(String exprieTime) {
            try {
                return Util.daysBetween(Util.getTime(), exprieTime) + 1;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        }

    }

    private class PrivilegeViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView date;


        public PrivilegeViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title_txt);
            date = (TextView) itemView.findViewById(R.id.buydate_txt);
        }
    }


    private class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {

            // Add top margin only for the first item to avoid double space between items
            outRect.bottom = space;
        }
    }
}
