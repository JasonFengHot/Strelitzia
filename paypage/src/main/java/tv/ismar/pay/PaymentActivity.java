package tv.ismar.pay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cn.ismartv.truetime.TrueTime;
import entity.Payments;
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
import tv.ismar.app.network.entity.GoodsRenewStatusEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PayWhStatusEntity;
import tv.ismar.pay.LoginFragment.LoginCallback;
import tv.ismar.statistics.PurchaseStatistics;

import static tv.ismar.app.AppConstant.Payment.PAYMENT_SUCCESS_CODE;
import static tv.ismar.pay.PaymentActivity.OderType.alipay_renewal;
import static tv.ismar.pay.R.id.pay_type_layout;
import static tv.ismar.pay.R.id.videocard;

/**
 * Created by huibin on 9/13/16.
 */
public class PaymentActivity extends BaseActivity implements View.OnClickListener, LoginCallback, OnHoverListener ,View.OnFocusChangeListener,View.OnKeyListener{
    private static final String TAG = "PaymentActivity";
    private LoginFragment loginFragment;
    private Fragment weixinFragment;
    private Fragment alipayFragment;
    private Fragment balanceFragment;

    public Button aliPayBtn;
    private Button balancePayBtn;
    private Button firstChannelBtn,lastChannelBtn;
    private ViewGroup payTypeLayout;

    private Subscription mOrderCheckLoopSubscription;

    private String category;
    private int pk;
    public boolean isbuying=false;
    private Timer timer;
    private ItemEntity mItemEntity;
    private TextView title;
    private TextView loginTip;
    private TextView username;

    private Subscription accountsBalanceSub;
    private Subscription apiOrderCreateSub;
    private Subscription apiOptItemSub;
    private Subscription alipaySecrectSub;
    private int movieId;
    private boolean login_tag = false;
    private Subscription accountsPayWhStatusSub;
    private Subscription accountsGoodsRenewStatusSub;
//    private ImageView shadow;
    private boolean isdestory=false;
    public ImageView tmp;
    public String uuid;
    private ArrayList<String> firstdescriptions=new ArrayList<>();
    private ArrayList<String> lastdescriptions=new ArrayList<>();

    public String getUuid() {
        uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        category = intent.getStringExtra(PageIntentInterface.EXTRA_PRODUCT_CATEGORY);
        setContentView(R.layout.activity_payment);
        tmp = (ImageView) findViewById(R.id.tmp);
        aliPayBtn = (Button) findViewById(R.id.alipay);
        balancePayBtn = (Button) findViewById(R.id.balance_pay);
        firstChannelBtn= (Button) findViewById(R.id.weixin);
        lastChannelBtn= (Button) findViewById(R.id.videocard);

        firstChannelBtn.setOnHoverListener(this);
        lastChannelBtn.setOnHoverListener(this);
        aliPayBtn.setOnHoverListener(this);
        balancePayBtn.setOnHoverListener(this);
        title = (TextView) findViewById(R.id.payment_title);
        payTypeLayout = (ViewGroup) findViewById(pay_type_layout);
        loginTip = (TextView) findViewById(R.id.login_tip);
        username = (TextView) findViewById(R.id.username);

        aliPayBtn.setOnClickListener(this);
        balancePayBtn.setOnClickListener(this);
        firstChannelBtn.setOnClickListener(this);
        lastChannelBtn.setOnClickListener(this);

        aliPayBtn.setOnFocusChangeListener(this);
        balancePayBtn.setOnFocusChangeListener(this);
        firstChannelBtn.setOnFocusChangeListener(this);
        lastChannelBtn.setOnFocusChangeListener(this);

        aliPayBtn.setOnKeyListener(this);
        balancePayBtn.setOnKeyListener(this);
        firstChannelBtn.setOnKeyListener(this);
        lastChannelBtn.setOnKeyListener(this);

        loginFragment = new LoginFragment();
        loginFragment.setLoginCallback(this);
        weixinFragment = new WeixinPayFragment();
        alipayFragment = new AlipayFragment();
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
                if (mItemEntity.getPk()==1111) {
                    payTypeLayout.setVisibility(View.GONE);
                } else {
                    payTypeLayout.setVisibility(View.VISIBLE);
                }

            } else {
                pk = intent.getIntExtra("pk", 0);
                movieId = intent.getIntExtra("movie_id", -1);
//                fetchChannel();
                fetchItem(pk, category);
            }
        }
        if(timer==null){
            timer=new Timer(true);
            timer.schedule(timerTask,2*60*1000,2*60*1000);
        }
    }
    private void fetchChannel(){
        String url="http://sky.test.tvxio.com:9933/api/paymentway/package/1101/";
        mSkyService.payChannel(url).subscribeOn(Schedulers.io())
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
                            JSONObject object=new JSONObject(result);
                            JSONArray payments=object.getJSONArray("payments");
                            if(payments.get(0)!=null) {
                                Payments payment = new GsonBuilder().create().fromJson(payments.get(0).toString(), Payments.class);
                                firstChannelBtn.setText(payment.getSource());
                                firstdescriptions=payment.getString();
                                firstChannelBtn.setVisibility(View.VISIBLE);
                            }
                            if(payments.get(1)!=null){
                                Payments payment = new GsonBuilder().create().fromJson(payments.get(1).toString(), Payments.class);
                                lastChannelBtn.setText(payment.getSource());
                                lastdescriptions=payment.getString();
                                lastChannelBtn.setVisibility(View.VISIBLE);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
    private void channelClick(String type,ArrayList<String> list){
        AlipayFragment alipayFragment = new AlipayFragment();
        createOrder(OderType.alipay, alipayFragment);
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putStringArrayList("descriptions",list);
        alipayFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_page, alipayFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    int lastfocusId=0;
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int i = v.getId();
        if(hasFocus&&!ishover){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
             if (i == R.id.alipay) {
                if(lastfocusId!=i&&!isdestory) {
                    transaction.remove(balanceFragment).commit();
                    alipayClick();
                }
            } else if (i == R.id.balance_pay) {
                if(lastfocusId!=i) {
                    if (alipaySecrectSub != null && !alipaySecrectSub.isUnsubscribed()) {
                        alipaySecrectSub.unsubscribe();
                    }
                    if(!isdestory)
                    transaction.replace(R.id.fragment_page, balanceFragment).commit();
                }
            }else if(i==R.id.weixin){
                 if(lastfocusId!=i){
                    channelClick("",firstdescriptions);
                 }
             }else if(i==R.id.videocard){
                 if(lastfocusId!=i){
                     channelClick("",lastdescriptions);
                 }
             }
            lastfocusId=i;
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
         if (i == R.id.alipay) {
             transaction.remove(balanceFragment).commit();
             alipayClick();
        }else if (i == R.id.balance_pay) {
             if (alipaySecrectSub != null && !alipaySecrectSub.isUnsubscribed()) {
                 alipaySecrectSub.unsubscribe();
             }
            transaction.replace(R.id.fragment_page, balanceFragment).commit();
        }else if(i==R.id.weixin){
             channelClick("",firstdescriptions);
         }else if(i==R.id.videocard){
             channelClick("",lastdescriptions);
         }
        lastfocusId=i;
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
                        if (entity.getBalance().floatValue() <mItemEntity.getExpense().getPrice()) {
                            aliPayBtn.requestFocusFromTouch();
                        } else {
                            balancePayBtn.requestFocusFromTouch();
                        }
                        tmp.setFocusable(false);
                        ishover=false;
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
        login_tag = true;
        changeLoginStatus(true);
        purchaseCheck(PaymentActivity.CheckType.PlayCheck, true);
        if (mItemEntity.getPk()==1111) {
            payTypeLayout.setVisibility(View.GONE);
            isSecretfree(mItemEntity.getPk());
        } else {
            payTypeLayout.setVisibility(View.VISIBLE);
            fetchAccountBalance();
        }
    }

    boolean ishover=false;
    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                ishover=true;
                v.requestFocus();
                v.requestFocusFromTouch();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                break;
        }
        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        ishover=false;
        return false;
    }


    public enum CheckType {
        PlayCheck,
        OrderPurchase
    }
    private Handler timeHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Log.i("TimerFinish","finish");
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
                            Log.i("ALipay","Finish");
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
                        if(mItemEntity.getPk()==1111) {
                            AlipayYKTMMRenewalFragment yktRenewalFragment = new AlipayYKTMMRenewalFragment();
                            Bundle bundle3 = new Bundle();
                            bundle3.putString("url", choosewayEntity.getPay().getUrl());
                            yktRenewalFragment.setArguments(bundle3);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_page, yktRenewalFragment).commit();
                        }else {
                            MmnormalPay mmnormalPay=new MmnormalPay();
                            Bundle bundle4 = new Bundle();
                            bundle4.putString("url", choosewayEntity.getPay().getUrl());
                            mmnormalPay.setArguments(bundle4);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_page, mmnormalPay).commit();
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

    public String getModel() {
        return category;
    }


    public int getPk() {
        return pk;
    }

    private void fetchItem(int pk, String model) {
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
                                    .commit();
                        } else {
                            changeLoginStatus(true);
                            if (mItemEntity.getPk()==1111) {
                                payTypeLayout.setVisibility(View.GONE);
                                isSecretfree(mItemEntity.getPk());
                            } else {
                                payTypeLayout.setVisibility(View.VISIBLE);
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
                button.setTextColor(getResources().getColor(R.color.color_base_white));
                button.setEnabled(true);
                button.setFocusable(true);

            }


        } else {
            loginTip.setVisibility(View.VISIBLE);
            for (int i = 0; i < payTypeLayout.getChildCount(); i++) {
                Button button = (Button) payTypeLayout.getChildAt(i);
                button.setTextColor(getResources().getColor(R.color.paychannel_button_disable));
                button.setFocusable(false);
                button.setEnabled(false);
            }
        }
        if (payTypeLayout.getVisibility() == View.INVISIBLE)
            payTypeLayout.setVisibility(View.VISIBLE);
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
        if (payTypeLayout.getVisibility() == View.INVISIBLE)
            payTypeLayout.setVisibility(View.VISIBLE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("flag", "usercenter_charge");
//        cardpayFragment.setArguments(bundle);
//        transaction.replace(R.id.fragment_page, cardpayFragment)
//                .commit();
    }


    @Override
    protected void onStop() {
        if (mOrderCheckLoopSubscription != null && !mOrderCheckLoopSubscription.isUnsubscribed()) {
            mOrderCheckLoopSubscription.unsubscribe();
        }
        mOrderCheckLoopSubscription = null;
        loginFragment.setLoginCallback(null);
        loginFragment = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        isdestory=true;
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
        if(timer!=null){
            timer.cancel();
        }

        super.onPause();
    }

    private void alipayClick() {
        alipaySecrectSub=mSkyService.accountsPayWhStatus(pk).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PayWhStatusEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(PayWhStatusEntity payWhStatusEntity) {
                        int code = payWhStatusEntity.getInfo().getStatus();
                        switch (code){
                            case PayWhStatusEntity.Status.WITHOUT_OPEN:
                                AlipayFragment alipayFragment = new AlipayFragment();
                                createOrder(OderType.alipay, alipayFragment);
                                Bundle bundle = new Bundle();
                                bundle.putString("type", "alipay");
                                alipayFragment.setArguments(bundle);
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_page, alipayFragment).commit();
                                break;
                            case PayWhStatusEntity.Status.OPEN:
                                MmnormalPay mmnormalPay=new MmnormalPay();
                                Bundle bundle2 = new Bundle();
                                bundle2.putInt("pk", pk);
                                bundle2.putString("uid",getUuid());
                                bundle2.putString("category",category);
                                mmnormalPay.setArguments(bundle2);
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_page, mmnormalPay).commit();
                                break;
                        }
                    }
                });
    }
    private void isSecretfree(int packageid){
        mSkyService.accountsPayWhStatus(packageid).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PayWhStatusEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(PayWhStatusEntity payWhStatusEntity) {
                        int code = payWhStatusEntity.getInfo().getStatus();
                        switch (code){
                            case PayWhStatusEntity.Status.WITHOUT_OPEN:
                                MmContinuousPayFragment mmContinuousPayFragment=new MmContinuousPayFragment();
                                createOrder(OderType.alipay, mmContinuousPayFragment);
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_page, mmContinuousPayFragment).commit();
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
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_page, yktRenewalFragment).commit();
                                break;
                            //未开通续订购
                            case GoodsRenewStatusEntity.Status.WITHOUT_OPEN:
                                alipayChooseWay();
                                break;
                        }
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
                }else {
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
}
