package tv.ismar.pay;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.WindowManager;
import android.widget.Button;
//import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cn.ismartv.truetime.TrueTime;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.network.entity.AccountBalanceEntity;
import tv.ismar.app.network.entity.ChoosewayEntity;
import tv.ismar.app.network.entity.ExtraPaymentChannelEntity;
import tv.ismar.app.network.entity.GoodsRenewStatusEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PayWhStatusEntity;
import tv.ismar.app.ui.ToastTip;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.pay.LoginFragment.LoginCallback;
import tv.ismar.pay.widget.PaymentChannelScrollView;
import tv.ismar.statistics.PurchaseStatistics;

import static tv.ismar.app.AppConstant.Payment.PAYMENT_SUCCESS_CODE;
import static tv.ismar.pay.PaymentActivity.OderType.alipay_renewal;
import static tv.ismar.pay.R.id.pay_type_layout;

/**
 * Created by huibin on 9/13/16.
 */
public class PaymentActivity extends BaseActivity implements View.OnClickListener, LoginCallback, OnHoverListener, View.OnFocusChangeListener, View.OnKeyListener{
    private static final String TAG = "PaymentActivity";
    public Button aliPayBtn;
    public boolean isbuying = false;
    public RecyclerImageView tmp;
    public String uuid;
    int lastfocusId = 0;
    public static boolean ishover = false;
    private LoginFragment loginFragment;
    private Fragment weixinFragment;
    private Fragment alipayFragment;
    private Fragment balanceFragment;
    private Fragment unipayFragment;
    private Button balancePayBtn;
    //    private Button uniPay;
    private LinearLayout payTypeLayout;
    private Subscription mOrderCheckLoopSubscription;
    private String category;
    private int pk;
    private Timer timer;
    private ItemEntity mItemEntity;
    private TextView title;
    private TextView loginTip;
    private TextView username;
    private Subscription accountsBalanceSub;
    private Subscription apiOrderCreateSub;
    private Subscription apiOptItemSub;
    private Subscription alipaySecrectSub;
//    private ImageView shadow;
    private int movieId;
    private boolean login_tag = false;
    private Subscription accountsPayWhStatusSub;
    private Subscription accountsGoodsRenewStatusSub;
    private Subscription fetchExtraPaymentChannel;
    private RecyclerImageView arrowUp;
    private RecyclerImageView arrowDown;
    private PaymentChannelScrollView mPaymentChannelScrollView;
    private Handler timeHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Log.i("TimerFinish", "finish");
                finish();
            }
            super.handleMessage(msg);
        }

    };
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = 1;
            timeHander.sendMessage(msg);
        }

    };

    public String getUuid() {
        uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid;
    }

    private boolean isStop = false;
    private View payment_channel_scrollview_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        category = intent.getStringExtra(PageIntentInterface.EXTRA_PRODUCT_CATEGORY);
        setContentView(R.layout.activity_payment);
        tmp = (RecyclerImageView) findViewById(R.id.tmp);
//        aliPayBtn = (Button) findViewById(R.id.alipay);
//        balancePayBtn = (Button) findViewById(R.id.balance_pay);
//        uniPay = (Button) findViewById(R.id.unipay);
//        aliPayBtn.setOnHoverListener(this);
//        balancePayBtn.setOnHoverListener(this);
//        uniPay.setOnHoverListener(this);
        title = (TextView) findViewById(R.id.payment_title);
        payTypeLayout = (LinearLayout) findViewById(pay_type_layout);
        loginTip = (TextView) findViewById(R.id.login_tip);
        username = (TextView) findViewById(R.id.username);
        arrowUp = (RecyclerImageView) findViewById(R.id.arrow_up);
        arrowDown = (RecyclerImageView) findViewById(R.id.arrow_down);
        payment_channel_scrollview_container = findViewById(R.id.payment_channel_scrollview_container);
        arrowUp.setOnHoverListener(this);
        arrowDown.setOnHoverListener(this);
        mPaymentChannelScrollView = (PaymentChannelScrollView) findViewById(R.id.payment_channel_scrollview);
        mPaymentChannelScrollView.setPayTypeLayout(payTypeLayout);
        mPaymentChannelScrollView.setArrowUp(arrowUp);
        mPaymentChannelScrollView.setArrowDown(arrowDown);
        int marginTop = getResources().getDimensionPixelSize(R.dimen.pay_channel_yue_marginTop);
        mPaymentChannelScrollView.setTabSpace(marginTop);
//
//        aliPayBtn.setOnClickListener(this);
//        balancePayBtn.setOnClickListener(this);
//        uniPay.setOnClickListener(this);

//        aliPayBtn.setOnFocusChangeListener(this);
//        balancePayBtn.setOnFocusChangeListener(this);
//        uniPay.setOnFocusChangeListener(this);

//        aliPayBtn.setOnKeyListener(this);
//        balancePayBtn.setOnKeyListener(this);
//        uniPay.setOnKeyListener(this);

        loginFragment = new LoginFragment();
        loginFragment.setLoginCallback(this);
        weixinFragment = new WeixinPayFragment();
        alipayFragment = new AlipayFragment();
//        unipayFragment = new UniPayFragment();
        //   cardpayFragment = new CardPayFragment();
        balanceFragment = new BalancePayFragment();
        if (category == null) {
            category = "";
        }
        if (category.equals(PageIntentInterface.ProductCategory.charge.name())) {
            // changeChagrgeStatus();
            title.setText("充值");
        } else {
            String itemJson = intent.getStringExtra(PageIntent.EXTRA_ITEM_JSON);
            if (!TextUtils.isEmpty(itemJson)) {
                mItemEntity = new GsonBuilder().create().fromJson(itemJson, ItemEntity.class);
                pk = mItemEntity.getPk();
                purchaseCheck(CheckType.PlayCheck);
                if (category.equals("package") && pk == 1111) {
                    payTypeLayout.setVisibility(View.GONE);
                    payment_channel_scrollview_container.setVisibility(View.GONE);
                } else {
                    payTypeLayout.setVisibility(View.VISIBLE);
                    payment_channel_scrollview_container.setVisibility(View.VISIBLE);
                }

            } else {
                pk = intent.getIntExtra("pk", 0);
                movieId = intent.getIntExtra("movie_id", -1);
                fetchItem(pk, category);
            }
        }
        if (timer == null) {
            timer = new Timer(true);
            timer.schedule(timerTask, 2 * 60 * 1000, 2 * 60 * 1000);
        }
        fillLocalPaymentChannelLayout();
        fetchExtraPaymentChannel(category, pk);

    }

    @Override
    protected void onStart() {
        super.onStart();
        isStop = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (category.equals("package") && pk == 1111) {
            payTypeLayout.setVisibility(View.GONE);
            payment_channel_scrollview_container.setVisibility(View.GONE);
        } else if (mItemEntity != null){
            payTypeLayout.setVisibility(View.VISIBLE);
            payment_channel_scrollview_container.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int i = v.getId();
        if (hasFocus && !ishover) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (i == R.id.alipay) {
                if (lastfocusId != i) {
                    transaction.remove(balanceFragment).commitAllowingStateLoss();
                    alipayClick();
                }
            } else if (i == R.id.balance_pay) {
                if (lastfocusId != i) {
                    if (alipaySecrectSub != null && !alipaySecrectSub.isUnsubscribed()) {
                        alipaySecrectSub.unsubscribe();
                    }
                    transaction.replace(R.id.fragment_page, balanceFragment).commitAllowingStateLoss();
                }
            } else {
                if (!isStop){
                if (lastfocusId != i) {
                    if (alipaySecrectSub != null && !alipaySecrectSub.isUnsubscribed()) {
                        alipaySecrectSub.unsubscribe();
                    }

                    String url = (String) v.getTag();
                    if (!TextUtils.isEmpty(url)) {
                        Bundle bundle = new Bundle();
                        bundle.putString("url", url);
                        unipayFragment = new UniPayFragment();
                        unipayFragment.setArguments(bundle);
                        transaction.replace(R.id.fragment_page, unipayFragment).commitAllowingStateLoss();
                    }
//                    mPaymentChannelScrollView.scrollChildPosition(v);
                }
            }
            }
        }
        lastfocusId = i;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (i == R.id.alipay) {
            transaction.remove(balanceFragment).commitAllowingStateLoss();
            alipayClick();
        } else if (i == R.id.balance_pay) {
            if (alipaySecrectSub != null && !alipaySecrectSub.isUnsubscribed()) {
                alipaySecrectSub.unsubscribe();
            }
            transaction.replace(R.id.fragment_page, balanceFragment).commitAllowingStateLoss();
        } else {
            if (alipaySecrectSub != null && !alipaySecrectSub.isUnsubscribed()) {
                alipaySecrectSub.unsubscribe();
            }
            String url = (String) v.getTag();
            if (!TextUtils.isEmpty(url)) {
                Bundle bundle = new Bundle();
                bundle.putString("url", url);
                unipayFragment = new UniPayFragment();
                unipayFragment.setArguments(bundle);
                transaction.replace(R.id.fragment_page, unipayFragment).commitAllowingStateLoss();
            }
        }
        lastfocusId = i;
    }

    public void fetchAccountBalance() {
        accountsBalanceSub = mSkyService.accountsBalance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<AccountBalanceEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(AccountBalanceEntity entity) {
                        if (entity.getBalance().floatValue() < mItemEntity.getExpense().getPrice()) {
                            payTypeLayout.getChildAt(0).requestFocusFromTouch();
                        } else {
                            payTypeLayout.getChildAt(1).requestFocusFromTouch();
                        }
                        ishover = false;
                    }
                });
    }

    public void purchaseCheck(CheckType checkType) {
        purchaseCheck(checkType, false);
    }

    public void purchaseCheck(CheckType checkType, boolean forceCheck) {
        if (!forceCheck) {
            if (mItemEntity.isRepeat_buy() && checkType == CheckType.PlayCheck) {
                return;
            }
        }

        if ("package".equalsIgnoreCase(category)) {
            if (movieId != -1 && login_tag) {
                orderCheckLoop(checkType, String.valueOf(movieId), null, null);
                login_tag = false;
            } else {
                orderCheckLoop(checkType, null, String.valueOf(pk), null);
            }
        } else if ("subitem".equalsIgnoreCase(category)) {
            orderCheckLoop(checkType, null, null, String.valueOf(pk));
        } else {
            orderCheckLoop(checkType, String.valueOf(pk), null, null);
        }
    }

    @Override
    public void onSuccess() {
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_page) instanceof  LoginFragment){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(loginFragment)
                    .commitAllowingStateLoss();
        }
        login_tag = true;
        changeLoginStatus(true);
        purchaseCheck(PaymentActivity.CheckType.PlayCheck, true);
        if (category.equals("package") && pk == 1111) {
            payTypeLayout.setVisibility(View.GONE);
            payment_channel_scrollview_container.setVisibility(View.GONE);
            isSecretfree(mItemEntity.getPk());
        } else {
            payTypeLayout.setVisibility(View.VISIBLE);
            payment_channel_scrollview_container.setVisibility(View.VISIBLE);
            fetchAccountBalance();

        }
        if (!TextUtils.isEmpty(IsmartvActivator.getInstance().getAuthToken()) && payTypeLayout != null && payTypeLayout.getChildCount() > 4) {
            arrowDown.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                ishover = true;
                payTypeLayout.setHovered(true);
                v.requestFocus();
                v.requestFocusFromTouch();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                payTypeLayout.setHovered(false);
                break;
        }
        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        ishover = false;
        return false;
    }

//    @Override
//    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//        Rect scrollViewRect = new Rect();
//        v.getGlobalVisibleRect(scrollViewRect);
//
//        View firstItemView = payTypeLayout.getChildAt(0);
//        Rect firstItemViewRect = new Rect();
//        firstItemView.getLocalVisibleRect(firstItemViewRect);
//        if (firstItemViewRect.bottom - firstItemViewRect.top < firstItemView.getHeight() || firstItemViewRect.top < 0) {
//            arrowUp.setVisibility(View.VISIBLE);
//        } else {
//            arrowUp.setVisibility(View.INVISIBLE);
//        }
//
//        View lastItemView = payTypeLayout.getChildAt(payTypeLayout.getChildCount() - 1);
//        Rect lastItemViewRect = new Rect();
//        lastItemView.getGlobalVisibleRect(lastItemViewRect);
//        Log.d(TAG, "bottom: " + lastItemViewRect.bottom);
//        Log.d(TAG, "top: " + lastItemViewRect.top);
//        Log.d(TAG, "scrollViewRect bottom: " + scrollViewRect.bottom);
//
//        if (lastItemViewRect.bottom > scrollViewRect.bottom) {
//            arrowDown.setVisibility(View.VISIBLE);
//        } else {
//            arrowDown.setVisibility(View.INVISIBLE);
//        }
//    }

    private void orderCheckLoop(final CheckType checkType, final String item, final String pkg, final String subItem) {
        if (mOrderCheckLoopSubscription != null && !mOrderCheckLoopSubscription.isUnsubscribed()) {
            mOrderCheckLoopSubscription.unsubscribe();
        }
        mOrderCheckLoopSubscription = Observable.interval(0, 10, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .map(new Func1<Long, String>() {
                    @Override
                    public String call(Long aLong) {
                        switch (checkType) {
                            case PlayCheck:
                                try {
                                    return mSkyService.playcheck(item, pkg, subItem)
                                            .execute().body().string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            case OrderPurchase:
                                try {
                                    return mSkyService.orderpurchase(item, pkg, subItem)
                                            .execute().body().string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        }
                        return null;
                    }
                })
                .takeUntil(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String responseBody) {
                        if (TextUtils.isEmpty(responseBody.toString()) || "0".equals(responseBody.toString())) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                })
                .take(60)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "orderCheckLoop onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "orderCheckLoop onError: order check");

                    }

                    @Override
                    public void onNext(String responseBody) {
                        if (responseBody != null && !"0".equals(responseBody)) {
                            sendLog();
                            setResult(PAYMENT_SUCCESS_CODE);
                            Log.i("ALipay", "Finish");
                            finish();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        sendExpenseCancleLog();
        super.onBackPressed();
    }

    public void createOrder(final OderType type, final QrcodeCallback callback) {
        String waresId = String.valueOf(pk);
        String waresType = category;
        String source = "mix";
        String timestamp = null;
        String sign = null;
        String apiType = "create";

        if (type == alipay_renewal) {
//            source = "alipay_wh";
            apiType = "chooseway";
        }

        if (type == OderType.sky) {
            timestamp = TrueTime.now().getTime() + "";
            IsmartvActivator activator = IsmartvActivator.getInstance();
            String encode = "sn=" + activator.getSnToken()
                    + "&source=sky" + "&timestamp=" + timestamp
                    + "&wares_id=" + pk + "&wares_type="
                    + category;
            sign = activator.encryptWithPublic(encode);
        }


        apiOrderCreateSub = mSkyService.apiOrderCreate(apiType, getUuid(), waresId, waresType, source, timestamp, sign, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        switch (type) {
                            case alipay_renewal:
                                ChoosewayEntity choosewayEntity = new GsonBuilder().create().fromJson(responseBody.charStream(), ChoosewayEntity.class);
                                fetchImage(choosewayEntity.getAgreement().getUrl(), type, callback);
                                break;
                            default:
                                BitmapFactory.Options opt = new BitmapFactory.Options();
                                opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                opt.inPurgeable = true;
                                opt.inInputShareable = true;
                                switch (type) {
                                    case alipay:
                                        opt.inSampleSize = 2;
                                        break;
                                }
                                callback.onBitmap(BitmapFactory.decodeStream(responseBody.byteStream(), null, opt));

                        }

                    }
                });

    }

    public void alipayChooseWay() {
        String waresId = String.valueOf(pk);
        String waresType = category;
        String action = "";
        String source = "mix";
//        if (type == alipay_renewal) {
//            action = "new";
//            source = "alipay_wh";
//        }

        final String finalSource = source;
        final String finalAction = action;
        apiOrderCreateSub = mSkyService.apiOrderCreate("chooseway", getUuid(), waresId, waresType, source, null, null, action)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        ChoosewayEntity choosewayEntity = new GsonBuilder().create().fromJson(responseBody.charStream(), ChoosewayEntity.class);
                        if (category.equals("package") && pk == 1111) {
                            payTypeLayout.setVisibility(View.GONE);
                            payment_channel_scrollview_container.setVisibility(View.GONE);
                            AlipayYKTMMRenewalFragment yktRenewalFragment = new AlipayYKTMMRenewalFragment();
                            Bundle bundle3 = new Bundle();
                            bundle3.putString("url", choosewayEntity.getPay().getUrl());
                            yktRenewalFragment.setArguments(bundle3);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_page, yktRenewalFragment).commitAllowingStateLoss();
                        } else {
                            MmnormalPay mmnormalPay = new MmnormalPay();
                            Bundle bundle4 = new Bundle();
                            bundle4.putString("url", choosewayEntity.getPay().getUrl());
                            mmnormalPay.setArguments(bundle4);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_page, mmnormalPay).commitAllowingStateLoss();
                        }
                    }
                });
    }

    private void fetchImage(String url, final OderType type, final QrcodeCallback callback) {
        mSkyService.image(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        opt.inPurgeable = true;
                        opt.inInputShareable = true;
                        switch (type) {
                            case alipay:
                                opt.inSampleSize = 2;
                                break;
                        }
                        callback.onBitmap(BitmapFactory.decodeStream(responseBody.byteStream(), null, opt));
                    }
                });
    }

    public String getModel() {
        return category;
    }

    public int getPk() {
        return pk;
    }

    private void fetchItem(final int pk, String model) {
        String opt = model;

        apiOptItemSub = mSkyService.apiOptItem(String.valueOf(pk), opt)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ItemEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ItemEntity itemEntity) {
                        mItemEntity = itemEntity;
                        title.setText(itemEntity.getTitle());
                        purchaseCheck(CheckType.PlayCheck);
                        if (TextUtils.isEmpty(IsmartvActivator.getInstance().getAuthToken())) {

                            changeLoginStatus(false);
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_page, loginFragment)
                                    .commitAllowingStateLoss();
                        } else {
                            changeLoginStatus(true);
                            if (category.equals("package") && pk== 1111) {
                                payTypeLayout.setVisibility(View.GONE);
                                payment_channel_scrollview_container.setVisibility(View.GONE);
                                isSecretfree(mItemEntity.getPk());
                            } else {
                                fetchAccountBalance();
                            }
                        }

                    }
                });
    }

    public ItemEntity getmItemEntity() {
        return mItemEntity;
    }

    public void changeLoginStatus(boolean isLogin) {
        if (isLogin) {
            username.setText(String.format(getString(R.string.welocome_tip), IsmartvActivator.getInstance().getUsername()));
            loginTip.setVisibility(View.GONE);
            for (int i = 0; i < payTypeLayout.getChildCount(); i++) {
                Button button = (Button) payTypeLayout.getChildAt(i);
                button.setEnabled(true);
                button.setFocusable(true);
                button.setTextColor(getResources().getColor(R.color._FFF8F8FF));

            }


        } else {
            loginTip.setVisibility(View.VISIBLE);
            for (int i = 0; i < payTypeLayout.getChildCount(); i++) {
                Button button = (Button) payTypeLayout.getChildAt(i);
                button.setFocusable(false);
                button.setEnabled(false);
                button.setTextColor(getResources().getColor(R.color._4E4E4E));
            }
        }
//        if (payTypeLayout.getVisibility() == View.INVISIBLE)
//            payTypeLayout.setVisibility(View.VISIBLE);
    }

    public void changeChagrgeStatus() {
        loginTip.setVisibility(View.INVISIBLE);
        for (int i = 0; i < payTypeLayout.getChildCount(); i++) {
            if (i != 2) {
                Button button = (Button) payTypeLayout.getChildAt(i);
                button.setTextColor(getResources().getColor(R.color.paychannel_button_disable));
                button.setFocusable(false);
                button.setEnabled(false);
            }
        }
//        if (payTypeLayout.getVisibility() == View.INVISIBLE)
//            payTypeLayout.setVisibility(View.VISIBLE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("flag", "usercenter_charge");
//        cardpayFragment.setArguments(bundle);
//        transaction.replace(R.id.fragment_page, cardpayFragment)
//                .commitAllowingStateLoss();
    }

    @Override
    protected void onStop() {
        isStop = true;
        if (mOrderCheckLoopSubscription != null && !mOrderCheckLoopSubscription.isUnsubscribed()) {
            mOrderCheckLoopSubscription.unsubscribe();
        }
        mOrderCheckLoopSubscription = null;
        if (loginFragment != null){
            loginFragment.setLoginCallback(null);
            loginFragment = null;
        }
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (accountsBalanceSub != null && accountsBalanceSub.isUnsubscribed()) {
            accountsBalanceSub.unsubscribe();
        }

        if (apiOptItemSub != null && apiOptItemSub.isUnsubscribed()) {
            apiOptItemSub.unsubscribe();
        }

        if (apiOrderCreateSub != null && apiOrderCreateSub.isUnsubscribed()) {
            apiOrderCreateSub.unsubscribe();
        }

        if (accountsPayWhStatusSub != null && accountsPayWhStatusSub.isUnsubscribed()) {
            accountsPayWhStatusSub.unsubscribe();
        }

        if (accountsGoodsRenewStatusSub != null && accountsGoodsRenewStatusSub.isUnsubscribed()) {
            accountsGoodsRenewStatusSub.unsubscribe();
        }
        if (mOrderCheckLoopSubscription != null && !mOrderCheckLoopSubscription.isUnsubscribed()) {
            mOrderCheckLoopSubscription.unsubscribe();
        }
        if (fetchExtraPaymentChannel != null && !fetchExtraPaymentChannel.isUnsubscribed()) {
            fetchExtraPaymentChannel.unsubscribe();
        }

        if (timer != null) {
            timer.cancel();
        }

        super.onPause();
    }

    private void alipayClick() {
        alipaySecrectSub = mSkyService.accountsPayWhStatus(pk).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PayWhStatusEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(PayWhStatusEntity payWhStatusEntity) {
                        int code = payWhStatusEntity.getInfo().getStatus();
                        switch (code) {
                            case PayWhStatusEntity.Status.WITHOUT_OPEN:
                                AlipayFragment alipayFragment = new AlipayFragment();
                                createOrder(OderType.alipay, alipayFragment);
                                Bundle bundle = new Bundle();
                                bundle.putString("type", "alipay");
                                alipayFragment.setArguments(bundle);
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_page, alipayFragment).commitAllowingStateLoss();
                                break;
                            case PayWhStatusEntity.Status.OPEN:
                                MmnormalPay mmnormalPay = new MmnormalPay();
                                Bundle bundle2 = new Bundle();
                                bundle2.putInt("pk", pk);
                                bundle2.putString("uid", getUuid());
                                bundle2.putString("category", category);
                                mmnormalPay.setArguments(bundle2);
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_page, mmnormalPay).commitAllowingStateLoss();
                                break;
                        }
                    }
                });
    }

    private void isSecretfree(int packageid) {
        mSkyService.accountsPayWhStatus(packageid).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PayWhStatusEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(PayWhStatusEntity payWhStatusEntity) {
                        int code = payWhStatusEntity.getInfo().getStatus();
                        switch (code) {
                            case PayWhStatusEntity.Status.WITHOUT_OPEN:
                                MmContinuousPayFragment mmContinuousPayFragment = new MmContinuousPayFragment();
                                createOrder(OderType.alipay, mmContinuousPayFragment);
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_page, mmContinuousPayFragment).commitAllowingStateLoss();
                                break;
                            case PayWhStatusEntity.Status.OPEN:
                                goodsRenewStatus(pk);
                                break;
                        }
                    }
                });
    }

    private void goodsRenewStatus(int packageId) {
        accountsGoodsRenewStatusSub = mSkyService.accountsGoodsRenewStatus(packageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<GoodsRenewStatusEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(GoodsRenewStatusEntity goodsRenewStatusEntity) {
                        int code = goodsRenewStatusEntity.getInfo().getStatus();
                        switch (code) {
                            //已开通续订购
                            case GoodsRenewStatusEntity.Status.OPEN:
                                AlipayYKTRenewalFragment yktRenewalFragment = new AlipayYKTRenewalFragment();
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_page, yktRenewalFragment).commitAllowingStateLoss();
                                break;
                            //未开通续订购
                            case GoodsRenewStatusEntity.Status.WITHOUT_OPEN:
                                alipayChooseWay();
                                break;
                        }
                    }
                });
    }

    public void sendLog() {
        if (!login_tag) {
            if (category.equals("package")) {
                new PurchaseStatistics().expensePageExit("", "", IsmartvActivator.getInstance().getUsername(),
                        "package", String.valueOf(mItemEntity.getPk()), mItemEntity.getTitle(), "success", "", uuid);
            } else {
                if (mItemEntity != null) {
                    String type = "";
                    if (mItemEntity.getExpense() != null) {
                        if (mItemEntity.getExpense().getPay_type() == 1) {
                            type = "independent";
                        } else if (mItemEntity.getExpense().getPay_type() == 2) {
                            type = "vip";
                        }
                    }
                    new PurchaseStatistics().expensePageExit(String.valueOf(mItemEntity.getPk()), mItemEntity.getTitle(), IsmartvActivator.getInstance().getUsername(),
                            type, "", mItemEntity.getTitle(), "success", "", uuid);
                } else {
                    new PurchaseStatistics().expensePageExit("", "", IsmartvActivator.getInstance().getUsername(),
                            "", "", "", "success", "", uuid);
                }
            }
        } else {
            if (category.equals("package")) {
                new PurchaseStatistics().expensePageExit("", "", IsmartvActivator.getInstance().getUsername(),
                        "", String.valueOf(mItemEntity.getPk()), mItemEntity.getTitle(), "allow", "", uuid);
            } else {
                if (mItemEntity != null) {
                    String type = "";
                    if (mItemEntity.getExpense() != null) {
                        if (mItemEntity.getExpense().getPay_type() == 1) {
                            type = "independent";
                        } else if (mItemEntity.getExpense().getPay_type() == 2) {
                            type = "vip";
                        }
                    }
                    new PurchaseStatistics().expensePageExit(String.valueOf(mItemEntity.getPk()), mItemEntity.getTitle(), IsmartvActivator.getInstance().getUsername(),
                            type, "", mItemEntity.getTitle(), "allow", "", uuid);
                } else {
                    new PurchaseStatistics().expensePageExit("", "", IsmartvActivator.getInstance().getUsername(),
                            "", "", "", "allow", "", uuid);
                }
            }
        }

        Log.d(TAG, "view position: " + AppConstant.purchase_tab);
    }

    public void sendExpenseCancleLog() {
        if (mItemEntity != null) {
            if (category.equals("package")) {
                new PurchaseStatistics().expensePageExit("", "", IsmartvActivator.getInstance().getUsername(),
                        "package", String.valueOf(mItemEntity.getPk()), mItemEntity.getTitle(), "cancel", "", uuid);
            } else {
                String type = "";
                if (mItemEntity.getExpense() != null) {
                    if (mItemEntity.getExpense().getPay_type() == 1) {
                        type = "independent";
                    } else if (mItemEntity.getExpense().getPay_type() == 2) {
                        type = "vip";
                    }
                }
                new PurchaseStatistics().expensePageExit(String.valueOf(mItemEntity.getPk()), mItemEntity.getTitle(),
                        IsmartvActivator.getInstance().getUsername(), type, "", mItemEntity.getTitle(), "cancel", "", uuid);
            }
        }
        Log.d(TAG, "view position: " + AppConstant.purchase_tab);
    }

    private void fetchExtraPaymentChannel(String type, final int pk) {
        fetchExtraPaymentChannel = mSkyService.apiExtraPaymentChannel(category, pk)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ExtraPaymentChannelEntity>() {
                    @Override
                    public void onCompleted() {
                        if (pk == 1111 && category.equals("package")){
                            if (IsmartvActivator.getInstance().isLogin()){
                                payTypeLayout.setVisibility(View.GONE);
                                payment_channel_scrollview_container.setVisibility(View.GONE);
                            }else {
                                payTypeLayout.setVisibility(View.VISIBLE);
                                payment_channel_scrollview_container.setVisibility(View.VISIBLE);
                            }

                        }else{
                            payTypeLayout.setVisibility(View.VISIBLE);
                            payment_channel_scrollview_container.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (pk == 1111 && category.equals("package")){
                            if (IsmartvActivator.getInstance().isLogin()){
                                payTypeLayout.setVisibility(View.GONE);
                                payment_channel_scrollview_container.setVisibility(View.GONE);
                            }else {
                                payTypeLayout.setVisibility(View.VISIBLE);
                                payment_channel_scrollview_container.setVisibility(View.VISIBLE);
                            }
                        }else {
                            payTypeLayout.setVisibility(View.VISIBLE);
                            payment_channel_scrollview_container.setVisibility(View.VISIBLE);
                        }
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ExtraPaymentChannelEntity extraPaymentChannelEntity) {
                        fillExtraPaymentChannelLayout(extraPaymentChannelEntity);
                    }
                });
    }

//    public void changeNormalAlipay() {
//        alipayChooseWay(OderType.alipay);
//    }
//
//    public void changeRenewalAlipay() {
//        alipayChooseWay(OderType.alipay_renewal);
//    }

    private void fillExtraPaymentChannelLayout(ExtraPaymentChannelEntity entity) {
        if (entity.getPayments() != null && !entity.getPayments().isEmpty()) {
            ArrayList<Integer> extraPayChannelIDs = new ArrayList<>();
            for (int i = 0; i < entity.getPayments().size(); i++) {
                extraPayChannelIDs.add(View.generateViewId());
            }
            balancePayBtn.setNextFocusDownId(View.NO_ID);
            for (int i = 0; i < entity.getPayments().size(); i++) {
                ExtraPaymentChannelEntity.PaymentsBean paymentsBean = entity.getPayments().get(i);
                Button button = new Button(this);
                button.setId(extraPayChannelIDs.get(i));
                if (entity.getPayments().indexOf(paymentsBean) == entity.getPayments().size() - 1) {
                    button.setNextFocusDownId(button.getId());
                }
                button.setTag(paymentsBean.getUrl());
                button.setNextFocusLeftId(button.getId());
                button.setNextFocusRightId(button.getId());
                button.setFocusable(true);
                button.setFocusableInTouchMode(true);
                int textSize = getResources().getDimensionPixelSize(R.dimen.pay_channel_weixin_textsize);
                button.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                button.setTextColor(getResources().getColor(R.color._4E4E4E));
                button.setBackgroundResource(R.drawable.paychannel_btn_selector);
                button.setText(paymentsBean.getTitle() + " ");
                button.setEnabled(false);
                button.setGravity(Gravity.CENTER);
                button.setOnClickListener(this);
                button.setOnFocusChangeListener(this);
                button.setOnHoverListener(this);
                button.setOnKeyListener(this);
                int width = getResources().getDimensionPixelSize(R.dimen.payment_type_btn_width);
                int height = getResources().getDimensionPixelSize(R.dimen.pay_channel_weixin_height);
                int marginTop = getResources().getDimensionPixelSize(R.dimen.pay_channel_yue_marginTop);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
                layoutParams.topMargin = marginTop;
                if (i == entity.getPayments().size() - 1 && entity.getPayments().size() > 2) {
                    layoutParams.bottomMargin = marginTop;
                }

                if (i == entity.getPayments().size() - 1){
                    button.setNextFocusDownId(button.getId());
                }

//                if (i == 0) {
//                    layoutParams.addRule(RelativeLayout.BELOW, R.id.balance_pay);
//                } else {
//                    layoutParams.addRule(RelativeLayout.BELOW, extraPayChannelIDs.get(i - 1));
//                }
                payTypeLayout.addView(button, layoutParams);

            }
        } else {
            balancePayBtn.setNextFocusDownId(balancePayBtn.getId());
        }
        payTypeLayout.requestLayout();
        if (payTypeLayout.getChildCount() > 4 && !TextUtils.isEmpty(IsmartvActivator.getInstance().getAuthToken())) {
            arrowDown.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(IsmartvActivator.getInstance().getAuthToken())) {
            changeLoginStatus(true);
        }
    }

    private void fillLocalPaymentChannelLayout() {
        for (int i = 0; i < 2; i++) {
            Button button = new Button(this);
            if (i == 0) {
                button.setId(R.id.alipay);
                aliPayBtn = button;
                button.setText("扫码支付");
                button.setNextFocusUpId(button.getId());
            } else if (i == 1) {
                button.setId(R.id.balance_pay);
                balancePayBtn = button;
                button.setText("余额支付");
                button.setNextFocusDownId(button.getId());
            }
            button.setNextFocusLeftId(button.getId());
            button.setNextFocusRightId(button.getId());

            if (i == 1) {
                button.setNextFocusRightId(View.NO_ID);
            }

            int textSize = getResources().getDimensionPixelSize(R.dimen.pay_channel_weixin_textsize);
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            button.setBackgroundResource(R.drawable.paychannel_btn_selector);
            button.setFocusable(true);
            button.setFocusableInTouchMode(true);
            button.setEnabled(false);
            button.setTextColor(getResources().getColor(R.color._4E4E4E));
            button.setGravity(Gravity.CENTER);
            button.setOnClickListener(this);
            button.setOnFocusChangeListener(this);
            button.setOnHoverListener(this);
            button.setOnKeyListener(this);
            int width = getResources().getDimensionPixelSize(R.dimen.payment_type_btn_width);
            int height = getResources().getDimensionPixelSize(R.dimen.pay_channel_weixin_height);
            int marginTop = getResources().getDimensionPixelSize(R.dimen.pay_channel_yue_marginTop);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
//            if (i == 1) {
//                layoutParams.addRule(RelativeLayout.BELOW, R.id.alipay);
//            }
            layoutParams.topMargin = marginTop;
            payTypeLayout.addView(button, layoutParams);
        }
    }

    public enum CheckType {
        PlayCheck,
        OrderPurchase
    }

    enum OderType {
        weixin,
        alipay,
        sky,
        alipay_renewal,
        alipay_wh
    }

    interface QrcodeCallback {
        void onBitmap(Bitmap bitmap);
    }

    @Override
    protected void onAuthAccountFailed() {
        super.onAuthAccountFailed();
        if (!isFinishing()) {
            finish();
        }
    }
}
