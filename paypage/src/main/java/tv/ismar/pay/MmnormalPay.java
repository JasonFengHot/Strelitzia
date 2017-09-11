package tv.ismar.pay;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.GsonBuilder;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.ChoosewayEntity;

/**
 * Created by liucan on 2017/7/20.
 * 普通包月已开通免密协议
 *
 */

public class MmnormalPay extends Fragment implements View.OnClickListener,View.OnHoverListener{
    private TextView line1,line2,line3,waiting;
    private Button confirm;
    private PaymentActivity activity;
    private SkyService mSkyService;
    private String category;
    private String uid;
    private int pk;
    @Override
    public void onAttach(Activity activity1) {
        super.onAttach(activity);
        activity= (PaymentActivity) activity1;
        mSkyService=activity.mSkyService;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mm_normal_pay,null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        pk=bundle.getInt("pk");
        category=bundle.getString("category");
        uid=bundle.getString("uid");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        line1= (TextView) view.findViewById(R.id.line1);
        line2= (TextView) view.findViewById(R.id.line2);
        line3= (TextView) view.findViewById(R.id.line3);
        waiting= (TextView) view.findViewById(R.id.waiting);
        line1.setText(String.format(getString(R.string.pay_payinfo_price_label), activity.getmItemEntity().getExpense().getPrice()));
        line2.setText(String.format(getString(R.string.pay_payinfo_exprice_label), activity.getmItemEntity().getExpense().getDuration()));
        line3.setText(getString(R.string.pay_payinfo_introduce_label));

        confirm= (Button) view.findViewById(R.id.confirm_normal);
        confirm.setNextFocusLeftId(R.id.alipay);
        confirm.setNextFocusUpId(R.id.confirm_normal);
        confirm.setOnClickListener(this);
        confirm.setOnHoverListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()== R.id.confirm_normal){
            if(!activity.isbuying) {
                alipayChooseWay();
                activity.isbuying = true;
                waiting.setVisibility(View.VISIBLE);
            }else{
                waiting.setVisibility(View.VISIBLE);
            }
        }
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
        mSkyService.apiOrderCreate("chooseway", uid, waresId, waresType, source, null, null, action)
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
                        buyMonthly(choosewayEntity.getPay().getUrl());
                    }
                });
    }

    private void buyMonthly(String palyUrl){
        mSkyService.openRenew(palyUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(activity.new BaseObserver() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(Object o) {
//                        activity.setResult(PAYMENT_SUCCESS_CODE);
//                        Message message = new Message();
//                        message.what = 0;
//                        handler.sendMessageDelayed(message, 1000);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.purchaseCheck(PaymentActivity.CheckType.OrderPurchase);
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            activity.sendLog();
            return false;
        }
    });

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocus();
                v.requestFocusFromTouch();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                activity.tmp.requestFocus();
                break;
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
