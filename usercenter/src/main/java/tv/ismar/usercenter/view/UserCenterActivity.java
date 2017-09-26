package tv.ismar.usercenter.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import cn.ismartv.truetime.TrueTime;
import okhttp3.ResponseBody;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.InitializeProcess;
import tv.ismar.app.entity.Favorite;
import tv.ismar.app.entity.History;
import tv.ismar.app.entity.HistoryFavoriteEntity;
import tv.ismar.app.entity.Item;
import tv.ismar.app.ui.HeadFragment;
import tv.ismar.app.util.ActivityUtils;
import tv.ismar.pay.LoginFragment;
import tv.ismar.usercenter.R;
import tv.ismar.usercenter.presenter.HelpPresenter;
import tv.ismar.usercenter.presenter.LocationPresenter;
import tv.ismar.usercenter.presenter.ProductPresenter;
import tv.ismar.usercenter.presenter.PurchaseHistoryPresenter;
import tv.ismar.usercenter.presenter.UserInfoPresenter;
import tv.ismar.usercenter.viewmodel.HelpViewModel;
import tv.ismar.usercenter.viewmodel.LocationViewModel;
import tv.ismar.usercenter.viewmodel.ProductViewModel;
import tv.ismar.usercenter.viewmodel.PurchaseHistoryViewModel;
import tv.ismar.usercenter.viewmodel.UserInfoViewModel;


/**
 * Created by huaijie on 7/3/15.
 */
public class UserCenterActivity extends BaseActivity implements LoginFragment.LoginCallback,
        IsmartvActivator.AccountChangeCallback {
    private static final String TAG = UserCenterActivity.class.getSimpleName();
    private static final int MSG_INDICATOR_CHANGE = 0x9b;

    private HelpFragment mHelpFragment;
    private LocationFragment mLocationFragment;
    private LoginFragment mLoginFragment;
    private ProductFragment mProductFragment;
    private PurchaseHistoryFragment mPurchaseHistoryFragment;
    private UserInfoFragment mUserInfoFragment;
    private CardPayFragment cardPayFragment;

    private ProductPresenter mProductPresenter;
    private LocationPresenter mLocationPresenter;
    private HelpPresenter mHelpPresenter;
    private PurchaseHistoryPresenter mPurchaseHistoryPresenter;
    private UserInfoPresenter mUserInfoPresenter;

    private ArrayList<View> indicatorView;


    private boolean isOnKeyDown = false;
    private boolean fargmentIsActive = false;

    private static final int[] INDICATOR_TEXT_RES_ARRAY = {
            R.string.usercenter_store,
            R.string.usercenter_userinfo,
            R.string.usercenter_login_register,
            R.string.usercenter_purchase_history,
            R.string.usercenter_help,
            R.string.usercenter_location,
            R.string.usercenter_cardActive
    };

    private static final int[] INDICATOR_ID_RES_ARRAY = {
            R.id.usercenter_store,
            R.id.usercenter_userinfo,
            R.id.usercenter_login_register,
            R.id.usercenter_purchase_history,
            R.id.usercenter_help,
            R.id.usercenter_location,
            R.id.usercenter_card
    };
    private LinearLayout userCenterIndicatorLayout;

    private View lastSelectedView;
    private View lastHoveredView;

    private View fragmentContainer;

    private View purchaseItem;

    public static final String LOCATION_FRAGMENT = "location";
    public static final String LOGIN_FRAGMENT = "login";

    private HeadFragment headFragment;

    private Subscription bookmarksSub;
    private Subscription historySub;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usercenter);
        if(!InitializeProcess.flag){
            new Thread(new InitializeProcess(this)).start();
        }
        IsmartvActivator.getInstance().addAccountChangeListener(this);
        addHeader();
        initViews();
//        selectProduct();

        // Load previously saved state, if available.
        if (savedInstanceState != null) {
//            TasksFilterType currentFiltering =
//                    (TasksFilterType) savedInstanceState.getSerializable(CURRENT_FILTERING_KEY);
//            mTasksPresenter.setFiltering(currentFiltering);
        }

        selectIndicator(getIntent());
    }

    private void addHeader() {
        headFragment = new HeadFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", "usercenter");
        headFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.header, headFragment).commit();
    }

    private void initViews() {
        userCenterIndicatorLayout = (LinearLayout) findViewById(R.id.user_center_indicator_layout);

        fragmentContainer = findViewById(R.id.user_center_container);

//        fragmentContainer.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
//            @Override
//            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
//                Log.d(TAG, "new focus view: " + newFocus);
//                if (lastHoveredView != null) {
//                    lastHoveredView.setHovered(false);
//                }
//                clearTheLastHoveredVewState();
//                if (oldFocus != null && newFocus != null && oldFocus.getTag() != null && oldFocus.getTag().equals(newFocus.getTag())) {
//                    Log.d(TAG, "onGlobalFocusChanged same side");
//                    isFromRightToLeft = false;
//                } else {
//                    if (newFocus != null && newFocus.getTag() != null && ("left").equals(newFocus.getTag())) {
//                        Log.d(TAG, "onGlobalFocusChanged from right to left");
//                        isFromRightToLeft = true;
//                    } else {
//                        isFromRightToLeft = false;
//                    }
//
//                }
//            }
//        });

        createIndicatorView();
    }


    private void createIndicatorView() {
        indicatorView = new ArrayList<>();
        userCenterIndicatorLayout.removeAllViews();
        for (int i = 0; i < INDICATOR_TEXT_RES_ARRAY.length; i++) {
            View frameLayout = LayoutInflater.from(this).inflate(R.layout.item_usercenter_indicator, null);
            TextView textView = (TextView) frameLayout.findViewById(R.id.indicator_text);
            textView.setText(INDICATOR_TEXT_RES_ARRAY[i]);
            frameLayout.setTag("left");
            frameLayout.setId(INDICATOR_ID_RES_ARRAY[i]);
            frameLayout.setOnClickListener(indicatorViewOnClickListener);
            frameLayout.setOnFocusChangeListener(indicatorOnFocusListener);
            frameLayout.setOnHoverListener(indicatorOnHoverListener);
            if (i == 0){
                frameLayout.setNextFocusUpId(frameLayout.getId());
            }
            if (i == 1) {
                frameLayout.setNextFocusRightId(R.id.exit_account);
            }
            if (i == 6) {
                frameLayout.setNextFocusDownId(frameLayout.getId());
                frameLayout.setNextFocusRightId(R.id.shiyuncard_input);
            }
            if (i == 2){
                frameLayout.setNextFocusRightId(R.id.pay_edit_mobile);
            }
            indicatorView.add(frameLayout);
            userCenterIndicatorLayout.addView(frameLayout);
        }

        if (IsmartvActivator.getInstance().isLogin()) {
            changeViewState(indicatorView.get(2), ViewState.Disable);
            indicatorView.get(6).setVisibility(View.VISIBLE);
        }else {
            indicatorView.get(6).setVisibility(View.GONE);
        }

//        userCenterIndicatorLayout.getChildAt(0).callOnClick();

    }


    private void selectProduct() {

        // Create the fragment
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof ProductFragment) {
            return;
        }

        mProductFragment = ProductFragment.newInstance();

        // Create the presenter
        mProductPresenter = new ProductPresenter(mProductFragment);

        ProductViewModel productViewModel =
                new ProductViewModel(getApplicationContext(), mProductPresenter);

        mProductFragment.setViewModel(productViewModel);

        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mProductFragment, R.id.user_center_container);


    }

    private void selectUserInfo() {
        // Create the fragment
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof UserInfoFragment) {
            return;
        }
        // Create the fragment
        mUserInfoFragment = UserInfoFragment.newInstance();

        // Create the presenter
        mUserInfoPresenter = new UserInfoPresenter(mUserInfoFragment);

        UserInfoViewModel userInfoViewModel =
                new UserInfoViewModel(getApplicationContext(), mUserInfoPresenter);

        mUserInfoFragment.setViewModel(userInfoViewModel);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mUserInfoFragment, R.id.user_center_container);


    }

    private void selectLogin() {
        // Create the fragment
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof LoginFragment) {
            return;
        }
        // Create the fragment
        if(mLoginFragment==null)
            mLoginFragment = LoginFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putString("source", "usercenter");
        mLoginFragment.setArguments(bundle);
        mLoginFragment.setLoginCallback(this);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mLoginFragment, R.id.user_center_container);

    }

    private void selectPurchaseHistory() {
        // Create the fragment
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof PurchaseHistoryFragment) {
            return;
        }
        // Create the fragment
        mPurchaseHistoryFragment = PurchaseHistoryFragment.newInstance();
        // Create the presenter
        mPurchaseHistoryPresenter = new PurchaseHistoryPresenter(mPurchaseHistoryFragment);

        PurchaseHistoryViewModel purchaseHistoryViewModel =
                new PurchaseHistoryViewModel(getApplicationContext(), mPurchaseHistoryPresenter);

        mPurchaseHistoryFragment.setViewModel(purchaseHistoryViewModel);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mPurchaseHistoryFragment, R.id.user_center_container);


    }

    private void selectHelp() {
        // Create the fragment
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof HelpFragment) {
            return;
        }
        // Create the fragment
        mHelpFragment = HelpFragment.newInstance();
        // Create the presenter
        mHelpPresenter = new HelpPresenter(mHelpFragment);

        HelpViewModel helpViewModel =
                new HelpViewModel(getApplicationContext(), mHelpPresenter);

        mHelpFragment.setViewModel(helpViewModel);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mHelpFragment, R.id.user_center_container);

    }
    private void selectCardActive(){
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof PurchaseHistoryFragment) {
            return;
        }
        cardPayFragment=new CardPayFragment();
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), cardPayFragment, R.id.user_center_container);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppConstant.purchase_referer = "profile";
        fargmentIsActive = true;
        baseChannel="";
        baseSection="";
        if (IsmartvActivator.getInstance().isLogin()) {
            changeViewState(indicatorView.get(2), ViewState.Disable);
            indicatorView.get(6).setVisibility(View.VISIBLE);
        } else {
            indicatorView.get(6).setVisibility(View.GONE);
            changeViewState(indicatorView.get(2), ViewState.Enable);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showLoginHint();
            }
        },1000);
        if(BaseActivity.goLogin){
            indicatorView.get(2).callOnClick();
            indicatorView.get(2).requestFocus();
            changeViewState(indicatorView.get(2), ViewState.Select);
            selectLogin();
            BaseActivity.goLogin=false;
        }

    }

    private void selectLocation() {
        // Create the fragment
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof LocationFragment) {
            return;
        }
        mLocationFragment = LocationFragment.newInstance();
        // Create the presenter
        mLocationPresenter = new LocationPresenter(mLocationFragment);

        LocationViewModel locationViewModel =
                new LocationViewModel(getApplicationContext(), mLocationPresenter);

        mLocationFragment.setViewModel(locationViewModel);
        // Create the fragment

        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mLocationFragment, R.id.user_center_container);


    }

    @Override
    public void onSuccess() {
        changeViewState(indicatorView.get(2), ViewState.Disable);
        indicatorView.get(1).callOnClick();
        indicatorView.get(1).requestFocus();
        changeViewState(indicatorView.get(1), ViewState.Select);
        indicatorView.get(6).setVisibility(View.VISIBLE);
//        fetchFavorite();
//        getHistoryByNet();
    }

    private View.OnFocusChangeListener indicatorOnFocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (v.isHovered()) {
                return;
            }

            if (hasFocus) {
                if (isOnKeyDown) {
                    for (View myView : indicatorView) {
                        myView.setHovered(false);
                    }

                    changeViewState(v, ViewState.Select);
                    messageHandler.removeMessages(MSG_INDICATOR_CHANGE);
                    Message message = messageHandler.obtainMessage(MSG_INDICATOR_CHANGE, v);
                    messageHandler.sendMessageDelayed(message, 300);
                }
            } else {
                changeViewState(v, ViewState.Unfocus);
            }
        }
    };

    private View.OnClickListener indicatorViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mLocationFragment != null) {
                mLocationFragment.clearStatus();
            }

            int i = v.getId();
            if (i == R.id.usercenter_store) {
                selectProduct();
            } else if (i == R.id.usercenter_userinfo) {
                selectUserInfo();
            } else if (i == R.id.usercenter_login_register) {
                selectLogin();
            } else if (i == R.id.usercenter_purchase_history) {
                selectPurchaseHistory();
            } else if (i == R.id.usercenter_help) {
                selectHelp();
            } else if (i == R.id.usercenter_location) {
                selectLocation();
            }else if(i==R.id.usercenter_card){
                selectCardActive();
            }
            changeViewState(v, ViewState.Select);

        }
    };


    private View.OnHoverListener indicatorOnHoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(final View v, MotionEvent event) {
            isOnKeyDown = false;
            ImageView textHoverImage = (ImageView) v.findViewById(R.id.text_select_bg);
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    v.setHovered(true);
                    if (!v.hasFocus()) {
                        v.requestFocus();
                    }

                    if (lastHoveredView != null) {
                        ImageView lastTextSelectImage = (ImageView) lastHoveredView.findViewById(R.id.text_select_bg);
                        lastTextSelectImage.setVisibility(View.INVISIBLE);
                    }
                    textHoverImage.setVisibility(View.VISIBLE);
                    lastHoveredView = v;
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    textHoverImage.setVisibility(View.INVISIBLE);
                    v.setHovered(false);
                    break;
            }
            return false;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        isOnKeyDown = true;
        if (lastHoveredView != null) {
            lastHoveredView.setHovered(false);
        }
        return super.onKeyDown(keyCode, event);
    }


    private void changeViewState(View parentView, ViewState viewState) {
        TextView textView = (TextView) parentView.findViewById(R.id.indicator_text);
        ImageView textSelectImage = (ImageView) parentView.findViewById(R.id.text_select_bg);
        ImageView textFocusImage = (ImageView) parentView.findViewById(R.id.text_focus_bg);
        switch (viewState) {
            case Select:
                if (parentView.isEnabled()) {
                    if (lastSelectedView != null) {
                        ImageView lastTextSelectImage = (ImageView) lastSelectedView.findViewById(R.id.text_select_bg);
                        ImageView lastTextFocusImage = (ImageView) lastSelectedView.findViewById(R.id.text_focus_bg);

                        lastTextSelectImage.setVisibility(View.INVISIBLE);
                        lastTextFocusImage.setVisibility(View.INVISIBLE);
                    }

                    if (lastHoveredView != null) {
                        ImageView lastTextHoverImage = (ImageView) lastHoveredView.findViewById(R.id.text_select_bg);
                        lastTextHoverImage.setVisibility(View.INVISIBLE);
                    }

                    textSelectImage.setVisibility(View.VISIBLE);
                    textFocusImage.setImageResource(R.drawable.usercenter_indicator_focused);
                    textFocusImage.setVisibility(View.VISIBLE);
                    lastSelectedView = parentView;
                    lastHoveredView = parentView;
                }
                break;
            case Unfocus:
                textSelectImage.setVisibility(View.INVISIBLE);
                break;
            case Disable:
                parentView.setEnabled(false);
                textSelectImage.setVisibility(View.INVISIBLE);
                textFocusImage.setVisibility(View.INVISIBLE);
                textView.setText(R.string.usercenter_login);
                textView.setTextColor(getResources().getColor(R.color.personinfo_login_button_disable));
                parentView.setFocusable(false);
                parentView.setFocusableInTouchMode(false);
                parentView.setClickable(false);
                break;
            case Enable:
                parentView.setEnabled(true);
                parentView.setFocusable(true);
                parentView.setFocusableInTouchMode(true);
                textView.setText(R.string.usercenter_login_register);
                textView.setTextColor(getResources().getColor(R.color._ffffff));
                textSelectImage.setVisibility(View.INVISIBLE);
                textFocusImage.setVisibility(View.INVISIBLE);
                parentView.setBackgroundResource(R.drawable._000000000);
                parentView.setClickable(true);
                break;
            case Gone:
                parentView.setEnabled(false);
                textSelectImage.setVisibility(View.INVISIBLE);
                textFocusImage.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.GONE);
                textView.setTextColor(getResources().getColor(R.color.personinfo_login_button_disable));
                parentView.setFocusable(false);
                parentView.setFocusableInTouchMode(false);
                parentView.setClickable(false);
                break;

        }

    }

    @Override
    public void onLogout() {
        changeViewState(indicatorView.get(2), ViewState.Enable);
        indicatorView.get(1).callOnClick();
        indicatorView.get(1).requestFocus();
        changeViewState(indicatorView.get(1), ViewState.Select);
        indicatorView.get(6).setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        IsmartvActivator.getInstance().removeAccountChangeListener(this);
        super.onDestroy();
    }

    private enum ViewState {
        Enable,
        Disable,
        Select,
        Unfocus,
        Hover,
        None,
        Gone
    }

    public void clearTheLastHoveredVewState() {
        if (lastHoveredView != null) {
            ImageView lastTextSelectImage = (ImageView) lastHoveredView.findViewById(R.id.text_select_bg);
            lastTextSelectImage.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof LocationFragment) {
            if (!mLocationFragment.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INDICATOR_CHANGE:
                    if (fargmentIsActive) {
                        View view = (View) msg.obj;
                        view.callOnClick();
                    }
                    break;
            }
        }
    };

    private void selectIndicator(Intent intent) {
        String flag = intent.getStringExtra("flag");
        if (!TextUtils.isEmpty(flag)) {
            if (flag.equals(LOCATION_FRAGMENT)) {
                indicatorView.get(5).callOnClick();
                indicatorView.get(5).requestFocus();
                changeViewState(indicatorView.get(5), ViewState.Select);
                selectLocation();
            }
        } else {
            indicatorView.get(0).callOnClick();
            indicatorView.get(0).requestFocus();
            changeViewState(indicatorView.get(0), ViewState.Select);
        }
    }

    @Override
    protected void onPause() {
        fargmentIsActive = false;
        if (messageHandler.hasMessages(MSG_INDICATOR_CHANGE)) {
            messageHandler.removeMessages(MSG_INDICATOR_CHANGE);
        }

        if (bookmarksSub != null && bookmarksSub.isUnsubscribed()) {
            bookmarksSub.unsubscribe();
        }

        if (historySub != null && historySub.isUnsubscribed()) {
            historySub.unsubscribe();
        }
        super.onPause();
    }

    public void refreshWeather() {
        if (headFragment != null) {
            HashMap<String, String> hashMap = IsmartvActivator.getInstance().getCity();
            String geoId = hashMap.get("geo_id");
          //  headFragment.fetchWeatherInfo(geoId);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == CardPayFragment.CHARGE_MONEY_SUCCESS) {
            if (mUserInfoFragment != null) {
                mUserInfoFragment.setShowChargeSuccessPop(true);
            }
        }
    }



    private void fetchFavorite() {
        bookmarksSub = mSkyService.getBookmarksV3()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String result=null;
                        try {
                            result=responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.i("result",result);
                        List<HistoryFavoriteEntity> historyLists= parseResult(result);
                        for (HistoryFavoriteEntity historyFavoriteEntity:historyLists){
                            addFavorite(historyFavoriteEntity);
                        }
                    }
                });
    }


    private void addFavorite(HistoryFavoriteEntity mItem) {
        if (isFavorite(mItem)) {
            String url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + mItem.getPk() + "/";
            // DaisyUtils.getFavoriteManager(getContext())
            // .deleteFavoriteByUrl(url,"yes");
        } else {
            String url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + mItem.getPk() + "/";
            Favorite favorite = new Favorite();
            favorite.title = mItem.getTitle();
            favorite.adlet_url = mItem.getAdlet_url();
            favorite.content_model = mItem.getContent_model();
            favorite.url = url;
            favorite.quality = mItem.getQuality();
            favorite.is_complex = mItem.getIs_complex();
            favorite.isnet = "yes";
            DateFormat format=new SimpleDateFormat("MM-dd");
            format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            long time= TrueTime.now().getTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            favorite.time=format.format(calendar.getTime());
            DaisyUtils.getFavoriteManager(this).addFavorite(favorite, favorite.isnet);
        }
    }


    private boolean isFavorite(HistoryFavoriteEntity mItem) {
        if (mItem != null) {
            String url = mItem.getUrl();
            if (url == null && mItem.getPk() != 0) {
                url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + mItem.getPk() + "/";
            }
            Favorite favorite = DaisyUtils.getFavoriteManager(this).getFavoriteByUrl(url, "yes");
            if (favorite != null) {
                return true;
            }
        }

        return false;
    }

    private void getHistoryByNet() {
        historySub = mSkyService.getHistoryByNetV3()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String result=null;
                        try {
                            result=responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.i("result",result);
                        List<HistoryFavoriteEntity> historyLists= parseResult(result);
                        for (HistoryFavoriteEntity historyFavoriteEntity:historyLists){
                            addHistory(historyFavoriteEntity);
                        }
                    }

                });
    }
    private List<HistoryFavoriteEntity> parseResult(String result){
        List<HistoryFavoriteEntity> lists=new ArrayList<>();
        try {
            JSONObject jsonObject=new JSONObject(result);
            JSONObject info=jsonObject.getJSONObject("info");
            JSONArray date=info.getJSONArray("date");
            for(int i=0;i<date.length();i++){
                JSONArray element=info.getJSONArray(date.getString(i));
                for(int j=0;j<element.length();j++){
                    HistoryFavoriteEntity historyFavoriteEntity=new GsonBuilder().create().fromJson(element.get(j).toString(),HistoryFavoriteEntity.class);
                    historyFavoriteEntity.setDate(date.getString(i));
                    lists.add(historyFavoriteEntity);
                }
            }
            if(lists.size()>0) {
                HistoryFavoriteEntity end = new HistoryFavoriteEntity();
                lists.add(end);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lists;
    }

    private void addHistory(HistoryFavoriteEntity item) {
        History history = new History();
        history.title = item.getTitle();
        history.adlet_url = item.getAdlet_url();
        history.content_model = item.getContent_model();
        history.is_complex = item.getIs_complex();
        history.last_position = item.getOffset();
        history.last_quality = item.getQuality();
        if ("subitem".equals(item.getModel_name())) {
            history.sub_url = item.getUrl();
            history.url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + item.getItem_pk() + "/";
        } else {
            history.url = item.getUrl();
        }

        history.is_continue = true;
        if (IsmartvActivator.getInstance().isLogin())
            DaisyUtils.getHistoryManager(this).addHistory(history, "yes", -1);
        else
            DaisyUtils.getHistoryManager(this).addHistory(history, "no", -1);

    }

    public void changeUserInfoSelectStatus(){
        indicatorView.get(1).callOnClick();
        indicatorView.get(1).requestFocus();
        changeViewState(indicatorView.get(1), ViewState.Select);
    }
    public void cardActiveSuccess(){
        if (mUserInfoFragment != null) {
            mUserInfoFragment.setShowChargeSuccessPop(true);
        }
    }
}
