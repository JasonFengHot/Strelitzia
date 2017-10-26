package tv.ismar.pay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.ExplainEntity;

/**
 * Created by liucan on 2017/7/20.
 * 未免密未开通连续包月
 */

public class MmContinuousPayFragment extends Fragment implements View.OnClickListener,PaymentActivity.QrcodeCallback{
    private TextView line_1,line_2,line_3,line_4,line_5;
    private TextView agreementTextView;
    private PaymentActivity paymentActivity;
    private ImageView qrcodeview;
    private SkyService skyService;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        paymentActivity= (PaymentActivity) activity;
        skyService=paymentActivity.mSkyService;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mm_continuous_pay,null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        qrcodeview = (ImageView) view.findViewById(R.id.qrcodeview);
        line_1= (TextView) view.findViewById(R.id.text_line_1);
        line_2= (TextView) view.findViewById(R.id.text_line_2);
        line_3= (TextView) view.findViewById(R.id.text_line_3);
        line_4= (TextView) view.findViewById(R.id.text_line_4);
        line_5= (TextView) view.findViewById(R.id.text_line_5);
        setText();
        agreementTextView = (TextView) view.findViewById(R.id.agreement);
        agreementTextView.setText(Html.fromHtml("<u>《视云连续扣费协议》</u>"));
        agreementTextView.setOnClickListener(this);
/*add by dragontec for bug 4283 start*/
        agreementTextView.setFocusable(true);
        agreementTextView.setFocusableInTouchMode(true);
        agreementTextView.requestFocus();
        agreementTextView.requestFocusFromTouch();
/*add by dragontec for bug 4283 end*/
    }

    @Override
    public void onResume() {
        super.onResume();
        paymentActivity.purchaseCheck(PaymentActivity.CheckType.OrderPurchase);
    }

    @Override
    public void onClick(View v) {
        int i =v.getId();
        if (i == R.id.agreement) {
            Intent intent = new Intent();
            intent.setClass(getContext(), RenewalAgreementActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBitmap(Bitmap bitmap) {
        qrcodeview.setImageBitmap(bitmap);
        qrcodeview.setVisibility(View.VISIBLE);
    }
    private void setText(){
        skyService.explain().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(paymentActivity.new BaseObserver<ExplainEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ExplainEntity explainEntity) {
                        ArrayList<String> textList=explainEntity.getInfo();
                        if(textList.get(0)!=null)
                            line_1.setText(textList.get(0));
                        if(textList.get(1)!=null)
                            line_2.setText(textList.get(1));
                        if(textList.get(2)!=null)
                            line_3.setText(textList.get(2));
                    }
                });
    }



}
