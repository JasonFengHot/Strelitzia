package tv.ismar.detailpage.view;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
//import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.GsonBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import cn.ismartv.truetime.TrueTime;
import okhttp3.ResponseBody;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.InitializeProcess;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.core.VodUserAgent;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PlayCheckEntity;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.ui.HeadFragment;
import tv.ismar.app.util.Constants;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.app.util.SPUtils;
import tv.ismar.app.util.SystemFileUtil;
import tv.ismar.app.util.Utils;
import tv.ismar.app.widget.LabelImageView;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.detailpage.DetailPageContract;
import tv.ismar.detailpage.R;
import tv.ismar.detailpage.databinding.FragmentDetailpageEntertainmentSharpBinding;
import tv.ismar.detailpage.databinding.FragmentDetailpageMovieSharpBinding;
import tv.ismar.detailpage.databinding.FragmentDetailpageNormalSharpBinding;
import tv.ismar.detailpage.presenter.DetailPagePresenter;
import tv.ismar.detailpage.viewmodel.DetailPageViewModel;
import tv.ismar.library.exception.ExceptionUtils;
import tv.ismar.statistics.DetailPageStatistics;

import static tv.ismar.app.core.PageIntentInterface.EXTRA_ITEM_JSON;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_SOURCE;
import static tv.ismar.app.core.PageIntentInterface.POSITION;
import static tv.ismar.app.core.PageIntentInterface.TYPE;

public class DetailPageFragment extends Fragment implements DetailPageContract.View, View.OnHoverListener {
    private static final String TAG = "LH/DetailPageFragment";
    private static final String ARG_PK = "ARG_PK";
    private static final String ARG_CONTENT_MODEL = "ARG_CONTENT_MODEL";

    private DetailPageViewModel mModel;
    private DetailPageContract.Presenter mPresenter;

    private FragmentDetailpageMovieSharpBinding mMovieBinding;
    private FragmentDetailpageEntertainmentSharpBinding mEntertainmentBinding;
    private FragmentDetailpageNormalSharpBinding mNormalBinding;
    private DetailPagePresenter mDetailPagePresenter;

    private int relViews;
    private int[] mRelImageViewIds = {R.id.rel_1_img, R.id.rel_2_img, R.id.rel_3_img, R.id.rel_4_img, R.id.rel_5_img, R.id.rel_6_img};
    private int[] mRelTextViewIds = {R.id.rel_1_text, R.id.rel_2_text, R.id.rel_3_text, R.id.rel_4_text, R.id.rel_5_text, R.id.rel_6_text};
    private int[] mRelTextViewFocusIds = {R.id.rel_1_focus_text, R.id.rel_2_focus_text, R.id.rel_3_focus_text, R.id.rel_4_focus_text};
    private int[] mRelItemViews = {R.id.related_item_layout_1, R.id.related_item_layout_2, R.id.related_item_layout_3, R.id.related_item_layout_4};

    private LabelImageView[] relRelImageViews;
    private TextView[] relTextViews;
    private TextView[] relFocusTextViews;

    //????????????
    private String fromPage;

    private HeadFragment headFragment;
    private String mHeadTitle;
    private volatile boolean itemIsLoad;
    private volatile boolean relateIsLoad;
    private volatile boolean expenseNotified=false;
    private ItemEntity mItemEntity;
    private ItemEntity[] relateItems;
    private int mRemandDay = 0;
    private BaseActivity mActivity;

    private View tmp;

    private View palyBtnView;
    private View purchaseBtnView;
    private View exposideBtnView;
    private View favoriteBtnView;
    private View moreBtnView;
    private View subscribeBtnView;


    private DetailPageStatistics mPageStatistics;
    private String isLogin = "no";
    private String to="";
    private int position;
    private String type="item";
    private PopupWindow popupWindow;

    public DetailPageFragment() {
        // Required empty public constructor
    }

    private Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.e("handler","handler");
            if (mItemEntity.is_order()){
                subscribeBtnView.requestFocus();
                subscribeBtnView.requestFocusFromTouch();
            }else {
                if (videoIsStart()&&palyBtnView.getVisibility()==View.VISIBLE) {
                    palyBtnView.requestFocus();
                    palyBtnView.requestFocusFromTouch();
                } else if(purchaseBtnView.getVisibility()== View.VISIBLE){
                    purchaseBtnView.requestFocus();
                    purchaseBtnView.requestFocusFromTouch();
                }else{
                    favoriteBtnView.requestFocus();
                    favoriteBtnView.requestFocusFromTouch();
                }
            }
            return false;
        }
    });

    public static DetailPageFragment newInstance(String fromPage, String itemJson) {
        DetailPageFragment fragment = new DetailPageFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_SOURCE, fromPage);
        args.putString(EXTRA_ITEM_JSON, itemJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageStatistics = new DetailPageStatistics();
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            fromPage = bundle.getString(EXTRA_SOURCE);
            String itemJson = bundle.getString(EXTRA_ITEM_JSON);
            position = bundle.getInt(POSITION,-1);
            type=bundle.getString(TYPE);
            mItemEntity = new GsonBuilder().create().fromJson(itemJson, ItemEntity.class);
        }

        if (!(getActivity() instanceof BaseActivity)) {
            getActivity().finish();
            Log.e(TAG, "Activity must be extends BaseActivity.");
            return;
        }
        mActivity = (BaseActivity) getActivity();
/*modify by dragontec for bug 4205 start*/
//        mDetailPagePresenter = new DetailPagePresenter((DetailPageActivity) getActivity(), this, mItemEntity.getContentModel());
        mDetailPagePresenter = new DetailPagePresenter((DetailPageActivity) mActivity, this, mItemEntity.getContentModel());
/*modify by dragontec for bug 4205 end*/
        mModel = new DetailPageViewModel(mActivity, mDetailPagePresenter);
        mDetailPagePresenter.setItemEntity(mItemEntity);
        String source=getActivity().getIntent().getStringExtra("fromPage");
        if(source!=null&&source.equals("launcher")) {
            tempInitStaticVariable();
            BaseActivity.baseSection="";
            BaseActivity.baseChannel="";
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.launcher_vod_click("item",mItemEntity.getPk(),mItemEntity.getTitle(),position);

            String province = (String) SPUtils.getValue(InitializeProcess.PROVINCE_PY, "");
            String city = (String) SPUtils.getValue(InitializeProcess.CITY, "");
            String isp = (String) SPUtils.getValue(InitializeProcess.ISP, "");
            callaPlay.app_start(IsmartvActivator.getInstance().getSnToken(),
                    VodUserAgent.getModelName(), DeviceUtils.getScreenInch(getActivity()),
                    android.os.Build.VERSION.RELEASE,
                    SimpleRestClient.appVersion,
                    SystemFileUtil.getSdCardTotal(getActivity().getApplicationContext()),
                    SystemFileUtil.getSdCardAvalible(getActivity().getApplicationContext()),
                    IsmartvActivator.getInstance().getUsername(), province, city, isp, source,
                    DeviceUtils.getLocalMacAddress(getActivity().getApplicationContext()),
                    SimpleRestClient.app, getActivity().getPackageName()
            );

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        return loadItemModel(inflater, container, mItemEntity);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, Constants.TEST);
        mPresenter.start();
//        mPresenter.fetchItem(String.valueOf(mItemEntity.getPk()));
//        loadItem(mItemEntity);

    }

    @Override
    public void onResume() {
        super.onResume();
        to="";
        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken())) {
            isLogin = "yes";
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadItem(mItemEntity);
            }
        }).start();
//        mPresenter.fetchSubscribeStatus(mItemEntity.getPk());
        mPageStatistics.videoDetailIn(mItemEntity, fromPage);

        mModel.notifyBookmark(true);
        mPresenter.fetchItemRelate(String.valueOf(mItemEntity.getPk()));
        to=fromPage;
    }

    @Override
    public void onPause() {
        mPresenter.stop();
        super.onPause();
    }

    @Override
    public void onStop() {
//        String sn = IsmartvActivator.getInstance().getSnToken();
//        Log.i("LH/", "sn:" + sn);
        if(!((DetailPageActivity)getActivity()).sendLog)
            mPageStatistics.videoDetailOut(mItemEntity,to);
        super.onStop();
    }

/*add by dragontec for bug 4205 start*/
    @Override
    public void onDestroyView() {
        if (relRelImageViews != null) {
            for (LabelImageView imageView : relRelImageViews) {
                if (imageView != null) {
                    imageView.setOnClickListener(null);
                    imageView.setOnHoverListener(null);
                    imageView.setOnFocusChangeListener(null);
                }
            }
        }
        relRelImageViews = null;
        relTextViews = null;
        relFocusTextViews = null;
        mMovieBinding = null;
        mEntertainmentBinding = null;
        mNormalBinding = null;
        super.onDestroyView();
    }
/*add by dragontec for bug 4205 end*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        handler=null;
/*add by dragontec for bug 4205 start*/
        mActivity = null;
        mModel = null;
        mItemEntity = null;
        relateItems = null;
        mPresenter = null;
        mDetailPagePresenter = null;
/*add by dragontec for bug 4205 end*/
    }

    @Override
    public void loadItem(ItemEntity itemEntity) {
        mPresenter.bookmarkCheck(itemEntity.getPk());
        if(isLogin.equals("yes")&&mItemEntity.getExpense()!=null&&mRemandDay<=0) {
            Log.e("refresh","true");
            if (itemEntity.getContentModel().equals("sport")) {
                mPresenter.requestPlayCheck(String.valueOf(mItemEntity.getPk()));
            } else {
                mPresenter.requestPlayCheck(String.valueOf(itemEntity.getPk()));
            }
        } else {
            notifyActivityPreload(false);
        }
        mModel.replaceItem(itemEntity);
        itemIsLoad = true;
        if(itemEntity.getExpense()==null){
            expenseNotified=true;
        }
        hideLoading();
        mItemEntity = itemEntity;
    }

    @Override
    public void loadItemRelate(ItemEntity[] itemEntities) {
        relateItems = itemEntities;
        int length=itemEntities.length;
        if (length< relViews) {
            for (int i = length; i < relViews; i++) {
                ((View) relRelImageViews[i].getParent()).setVisibility(View.INVISIBLE);
            }
            if(itemEntities.length>0) {
                if (mNormalBinding != null) {
                    mNormalBinding.getRoot().findViewById(mRelItemViews[itemEntities.length - 1]).setNextFocusDownId(R.id.detail_relative_button);
                    moreBtnView.setNextFocusUpId(mRelItemViews[itemEntities.length - 1]);
                }
            }
        }

        for (int i = 0; i < length && i < relViews; i++) {
            moreBtnView.setNextFocusLeftId(View.NO_ID);
            switch (mItemEntity.getContentModel()) {
                case "movie":
                    relRelImageViews[i].setLivUrl(itemEntities[i].getList_url());
                    break;
                default:
                    relRelImageViews[i].setLivUrl(itemEntities[i].getPosterUrl());
                    break;

            }
            if (mNormalBinding != null) {
                View itemView = mNormalBinding.getRoot().findViewById(mRelItemViews[i]);
                itemView.setTag(i);
                itemView.setOnClickListener(relateItemOnClickListener);
                itemView.setOnHoverListener(this);
                final int finalI = i;
                itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
/*add by dragontec for bug 4205 start*/
                        if (relTextViews != null && relTextViews[finalI] != null) {
/*add by dragontec for bug 4205 end*/
                            if (hasFocus) {
                                relTextViews[finalI].setSelected(true);
                            } else {
                                relTextViews[finalI].setSelected(false);
                            }
/*add by dragontec for bug 4205 start*/
                        }
/*add by dragontec for bug 4205 end*/
                    }
                });
//                relRelImageViews[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                    @Override
//                    public void onFocusChange(View v, boolean hasFocus) {
//                        int position = (int) v.getTag();
//                        if (hasFocus) {
//                            relTextViews[position].setSelected(true);
//                        } else {
//                            relTextViews[position].setSelected(false);
//                        }
//                    }
//                });

            } else {
                relRelImageViews[i].setTag(i);
                relRelImageViews[i].setOnClickListener(relateItemOnClickListener);
                relRelImageViews[i].setOnHoverListener(this);
                relRelImageViews[i].setNextFocusDownId(relRelImageViews[i].getId());

                relRelImageViews[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        int position = (int) v.getTag();
/*add by dragontec for bug 4205 start*/
                        if (relTextViews != null && relTextViews[position] != null) {
/*add by dragontec for bug 4205 end*/
                            if (hasFocus) {
                                relTextViews[position].setSelected(true);
                            } else {
                                relTextViews[position].setSelected(false);
                            }
/*add by dragontec for bug 4205 start*/
                        }
/*add by dragontec for bug 4205 end*/
                    }
                });
            }


            ItemEntity.Expense expense = itemEntities[i].getExpense();
            if (expense != null && !Utils.isEmptyText(expense.getCptitle())) {
                relRelImageViews[i].setLivVipPosition(LabelImageView.LEFTTOP);
                String imageUrl = VipMark.getInstance().getImage(mActivity, expense.getPay_type(), expense.getCpid());
                relRelImageViews[i].setLivVipUrl(imageUrl);
            }
            String scoreStr = itemEntities[i].getBeanScore();
            if (!Utils.isEmptyText(scoreStr)) {
                float score = 0;
                try {
                    score = Float.parseFloat(scoreStr);
                } catch (NumberFormatException e) {
                    ExceptionUtils.sendProgramError(e);
                    e.printStackTrace();
                }
                if (score > 0) {
                    relRelImageViews[i].setLivRate(score);
                }
            }
//            relTextViews[i].setMarqueeRepeatLimit(-1);
//            relTextViews[i].setEllipsize(TextUtils.TruncateAt.MARQUEE);
            relTextViews[i].setText(itemEntities[i].getTitle());

            if (mEntertainmentBinding != null) {
                relRelImageViews[i].setLivLabelText(itemEntities[i].getFocus());

            } else if(mNormalBinding!=null){
                relFocusTextViews[i].setText(itemEntities[i].getFocus());
            }
        }
        relateIsLoad = true;
        hideLoading();
        if (mMovieBinding != null && mMovieBinding.detailBtnLinear != null)
            mMovieBinding.detailBtnLinear.setVisibility(View.VISIBLE);
    }


    @Override
    public void notifyPlayCheck(PlayCheckEntity playCheckEntity) {
        mModel.notifyPlayCheck(playCheckEntity);
        mRemandDay = playCheckEntity.getRemainDay();
        handler.sendEmptyMessageDelayed(0,100);

        // 0?????????????????????
        boolean isBuy;
        if (playCheckEntity.getRemainDay() == 0) {
            isBuy = false;// ???????????????????????????
        } else {
            isBuy = true;// ??????????????????????????????0
        }
        notifyActivityPreload(isBuy);
    }

    private void notifyActivityPreload(boolean permission){
        ((DetailPageActivity)getActivity()).playCheckResult(permission);
        expenseNotified=true;
        hideLoading();
    }

    @Override
    public void notifyBookmark(boolean mark, boolean isSuccess) {
        mModel.notifyBookmark(isSuccess);
        if (mark) {
            if (isSuccess) {
                showToast(getString(R.string.vod_bookmark_add_success));
            } else {
                showToast(getString(R.string.vod_bookmark_add_unsuccess));
            }
        } else {
            if (isSuccess) {
                showToast(getString(R.string.vod_bookmark_remove_success));
            } else {
                showToast(getString(R.string.vod_bookmark_remove_unsuccess));
            }
        }
    }

    @Override
    public void notifySubscribeStatus(boolean isSubscribed) {
        mModel.notifySubscibeStatus();
    }

    @Override
    public void onError() {
        try {
            if (((DetailPageActivity) getActivity()).mLoadingDialog != null && ((DetailPageActivity) getActivity()).mLoadingDialog.isShowing()) {
                ((DetailPageActivity) getActivity()).mLoadingDialog.dismiss();
            }
        }catch (Exception e){
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }

    }


    @Override
    public void setPresenter(DetailPageContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private void hideLoading() {
        if (((DetailPageActivity) getActivity()).mLoadingDialog != null && ((DetailPageActivity) getActivity()).mLoadingDialog.isShowing() && itemIsLoad && relateIsLoad&&expenseNotified) {
            if(isLogin.equals("no")||mItemEntity.getExpense()==null||mRemandDay>0)
                handler.sendEmptyMessageDelayed(0,100);
            ((DetailPageActivity) getActivity()).mLoadingDialog.dismiss();

            String quality = "";
            switch (mItemEntity.getQuality()) {
                case 2:
                    quality = "normal";
                    break;
                case 3:
                    quality = "medium";
                    break;
                case 4:
                    quality = "high";
                    break;
                case 5:
                    quality = "ultra";
                    break;
                default:
                    quality = "adaptive";
                    break;
            }

            //??????clip??????????????????????????????????????????????????????
            if (mItemEntity.getClip() != null) {
                HashMap<String, Object> dataCollectionProperties = new HashMap<>();
                dataCollectionProperties.put(EventProperty.CLIP, mItemEntity.getClip().getPk());
                dataCollectionProperties.put(EventProperty.DURATION, (int) ((System.currentTimeMillis() - ((DetailPageActivity) getActivity()).start_time) / 1000));
                dataCollectionProperties.put(EventProperty.QUALITY, quality);
                dataCollectionProperties.put(EventProperty.TITLE, mItemEntity.getTitle());
                dataCollectionProperties.put(EventProperty.ITEM, mItemEntity.getPk());
                dataCollectionProperties.put(EventProperty.SUBITEM, mItemEntity.getItemPk());
                dataCollectionProperties.put(EventProperty.LOCATION, "detail");
                new NetworkUtils.DataCollectionTask().execute(NetworkUtils.DETAIL_PLAY_LOAD, dataCollectionProperties);
            }
        }

        mModel.showLayout();
    }

    private View.OnClickListener relateItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((DetailPageActivity) getActivity()).stopPreload();
            AppConstant.purchase_entrance_page = "related";
            ItemEntity item = relateItems[(int) v.getTag()];
            AppConstant.purchase_entrance_related_item = String.valueOf(mItemEntity.getItemPk());
            AppConstant.purchase_entrance_related_title = mItemEntity.getTitle();
            AppConstant.purchase_entrance_related_channel = AppConstant.purchase_channel;
            mPageStatistics.videoRelateClick(mItemEntity.getPk(), item);
            DetailPageActivity act = (DetailPageActivity)getActivity();
            new PageIntent().toDetailPage(getContext(), Source.RELATED.getValue(),act.to, item.getPk());
            to="relate";
            getActivity().finish();
        }
    };

    private String getModelType(String content_model) {
        String resourceType = null;
        if (content_model.equals("movie")) {
            resourceType = "??????";
            // teleplay ????????????trailer
        } else if (content_model.equals("teleplay")) {
            resourceType = "?????????";
            // variety ?????????
        } else if (content_model.equals("variety")) {
            resourceType = "??????";
            // documentary ????????????
        } else if (content_model.equals("documentary")) {
            resourceType = "?????????";
            // entertainment ?????????
        } else if (content_model.equals("entertainment")) {
            resourceType = "??????";
            // trailer ?????????
        } else if (content_model.equals("trailer")) {
            resourceType = "??????";
            // music ?????????
        } else if (content_model.equals("music")) {
            resourceType = "??????";
            // comic ?????????
        } else if (content_model.equals("comic")) {
            resourceType = "??????";
            // sport ?????????
        } else if (content_model.equals("sport")) {
            resourceType = "??????";
        }
        return resourceType;
    }

    private void showToast(String text) {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.simple_toast, null);
        TextView toastText = (TextView) layout.findViewById(R.id.toast_text);
        toastText.setText(text);
        Toast toast = new Toast(mActivity.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private View loadItemModel(LayoutInflater inflater, ViewGroup container, ItemEntity itemEntity) {
        View contentView;
        String content_model = itemEntity.getContentModel();
        mHeadTitle = getModelType(content_model);
/*modify by dragontec for bug 4336 start*/
        Drawable placeHoldDrawable = null;
/*modify by dragontec for bug 4336 end*/
        if ((("variety".equals(content_model) && mItemEntity.getExpense() == null)) || ("entertainment".equals(content_model) && mItemEntity.getExpense() == null)) {
            relViews = 4;
/*modify by dragontec for bug 4336 start*/
            placeHoldDrawable = getResources().getDrawable(R.drawable.item_horizontal_preview);
/*modify by dragontec for bug 4336 end*/
            mEntertainmentBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detailpage_entertainment_sharp, container, false);
            mEntertainmentBinding.setTasks(mModel);
            mEntertainmentBinding.setActionHandler(mPresenter);
            contentView = mEntertainmentBinding.getRoot();
            tmp = mEntertainmentBinding.tmp;
            tmp.requestFocus();
            tmp.requestFocusFromTouch();
            palyBtnView = mEntertainmentBinding.detailBtnPlay;
//            purchaseBtnView = mEntertainmentBinding.
            exposideBtnView = mEntertainmentBinding.detailBtnDrama;
            favoriteBtnView = mEntertainmentBinding.detailBtnCollect;
            moreBtnView = mEntertainmentBinding.detailRelativeButton;
            palyBtnView.setNextFocusDownId(R.id.detail_relative_button);
            subscribeBtnView = mEntertainmentBinding.subscribeStatusBtn;
        } else if ("movie".equals(content_model)) {
            relViews = 6;
/*modify by dragontec for bug 4336 start*/
            placeHoldDrawable = getResources().getDrawable(R.drawable.item_vertical_preview);
/*modify by dragontec for bug 4336 end*/
            mMovieBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detailpage_movie_sharp, container, false);
            mMovieBinding.setTasks(mModel);
            mMovieBinding.setActionHandler(mPresenter);
            contentView = mMovieBinding.getRoot();
            tmp = mMovieBinding.tmp;
            tmp.requestFocus();
            tmp.requestFocusFromTouch();
            palyBtnView = mMovieBinding.detailBtnPlay;
            purchaseBtnView = mMovieBinding.detailBtnBuy;
//            exposideBtnView = mMovieBinding.detailBtnDrama;
            favoriteBtnView = mMovieBinding.detailBtnCollect;
            moreBtnView = mMovieBinding.detailRelativeButton;
            subscribeBtnView = mMovieBinding.subscribeStatusBtn;
        } else {
            relViews = 4;
/*modify by dragontec for bug 4336 start*/
            placeHoldDrawable = getResources().getDrawable(R.drawable.item_horizontal_preview);
/*modify by dragontec for bug 4336 end*/
            relFocusTextViews = new TextView[relViews];
            mNormalBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detailpage_normal_sharp, container, false);
            mNormalBinding.setTasks(mModel);
            mNormalBinding.setActionHandler(mPresenter);
            contentView = mNormalBinding.getRoot();
            tmp = mNormalBinding.tmp;
            tmp.requestFocus();
            tmp.requestFocusFromTouch();
            palyBtnView = mNormalBinding.detailBtnPlay;
            purchaseBtnView = mNormalBinding.detailBtnBuy;
            exposideBtnView = mNormalBinding.detailBtnDrama;
            favoriteBtnView = mNormalBinding.detailBtnCollect;
            moreBtnView = mNormalBinding.detailRelativeButton;
            subscribeBtnView = mNormalBinding.subscribeStatusBtn;
        }

        subscribeBtnView.setOnHoverListener(this);
        palyBtnView.setOnHoverListener(this);
        palyBtnView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    to="play";
                }
            }
        });
        if (purchaseBtnView != null) {
            purchaseBtnView.setOnHoverListener(this);
            purchaseBtnView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        to="pay";
                    }
                }
            });
        }

        if (exposideBtnView != null) {
            exposideBtnView.setOnHoverListener(this);
        }
        favoriteBtnView.setOnHoverListener(this);
        moreBtnView.setOnHoverListener(this);
        moreBtnView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    to="relate";
                }
            }
        });
        relRelImageViews = new LabelImageView[relViews];
        relTextViews = new TextView[relViews];

        for (int i = 0; i < relViews; i++) {
            relRelImageViews[i] = (LabelImageView) contentView.findViewById(mRelImageViewIds[i]);
            relRelImageViews[i].setVisibility(View.VISIBLE);
/*modify by dragontec for bug 4336 start*/
            if (placeHoldDrawable != null) {
                relRelImageViews[i].setLivErrorDrawable(placeHoldDrawable);
            }
/*modify by dragontec for bug 4336 end*/
            relTextViews[i] = (TextView) contentView.findViewById(mRelTextViewIds[i]);
            if (!(content_model.equals("variety") && itemEntity.getExpense() == null) &&
                    !(content_model.equals("entertainment") && itemEntity.getExpense() == null)
                    && !content_model.equals("movie")) {
                relFocusTextViews[i] = (TextView) contentView.findViewById(mRelTextViewFocusIds[i]);
            }
        }
        headFragment = new HeadFragment();
        getChildFragmentManager().beginTransaction().add(R.id.detail_head, headFragment).commit();
        return contentView;
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocus();
                v.requestFocusFromTouch();
                break;

            case MotionEvent.ACTION_HOVER_EXIT:
                tmp.requestFocus();
                tmp.requestFocusFromTouch();
                break;
        }

        return false;
    }

    private boolean videoIsStart() {
        if (Utils.isEmptyText(mItemEntity.getStartTime())) {
            return true;
        }
        Date startDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(mItemEntity.getStartTime()!=null)
                startDate = sdf.parse(mItemEntity.getStartTime());
        } catch (ParseException e) {
            ExceptionUtils.sendProgramError(e);
            System.out.println(e.getMessage());
        }
        if (startDate != null) {
            Calendar currentCalendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
            currentCalendar.setTime(TrueTime.now());
            Calendar startCalendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
            startCalendar.setTime(startDate);
            if (startCalendar.getTimeInMillis()-currentCalendar.getTimeInMillis()<15*60*1000) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    // ???launcher???????????????????????????????????????
    private void tempInitStaticVariable() {
        new Thread() {
            @Override
            public void run() {
                DisplayMetrics metric = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
                SimpleRestClient.densityDpi = metric.densityDpi;
                SimpleRestClient.screenWidth = metric.widthPixels;
                SimpleRestClient.screenHeight = metric.heightPixels;
                PackageManager manager = getActivity().getPackageManager();
                try {
                    PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
                    SimpleRestClient.appVersion = info.versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    ExceptionUtils.sendProgramError(e);
                    e.printStackTrace();
                }
                String apiDomain = IsmartvActivator.getInstance().getApiDomain();
                String ad_domain = IsmartvActivator.getInstance().getAdDomain();
                String log_domain = IsmartvActivator.getInstance().getLogDomain();
                String upgrade_domain = IsmartvActivator.getInstance().getUpgradeDomain();
                if (apiDomain != null && !apiDomain.contains("http")) {
                    apiDomain = "http://" + apiDomain;
                }
                if (ad_domain != null && !ad_domain.contains("http")) {
                    ad_domain = "http://" + ad_domain;
                }
                if (log_domain != null && !log_domain.contains("http")) {
                    log_domain = "http://" + log_domain;
                }
                if (upgrade_domain != null && !upgrade_domain.contains("http")) {
                    upgrade_domain = "http://" + upgrade_domain;
                }
                SimpleRestClient.root_url = apiDomain;
                SimpleRestClient.ad_domain = ad_domain;
                SimpleRestClient.log_domain = log_domain;
                SimpleRestClient.upgrade_domain = upgrade_domain;
                SimpleRestClient.device_token = IsmartvActivator.getInstance().getDeviceToken();
                SimpleRestClient.sn_token = IsmartvActivator.getInstance().getSnToken();
                SimpleRestClient.zuser_token = IsmartvActivator.getInstance().getZUserToken();
                SimpleRestClient.zdevice_token = IsmartvActivator.getInstance().getZDeviceToken();
            }
        }.start();

    }

    @Override
    public void showSubscribeDialog(ResponseBody responseBody) {
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.detail_subscribe_dialog, null);
        popupWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
        popupWindow.setContentView(contentView);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
        final RecyclerImageView code = (RecyclerImageView) contentView.findViewById(R.id.code_image);
        Bitmap bitmap = BitmapFactory.decodeStream(responseBody.byteStream());
        BitmapDrawable bd = new BitmapDrawable(bitmap);
        code.setBackground(bd);
        Button btn = (Button) contentView.findViewById(R.id.subscribe_back);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    @Override
    public void notifyBookmarkCheck() {
        mModel.notifyBookMarkStatus();
    }
}
