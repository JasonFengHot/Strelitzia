package tv.ismar.pay;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
//import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import cn.ismartv.tvhorizontalscrollview.TvHorizontalScrollView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.core.Source;
import tv.ismar.app.network.entity.PayLayerVipEntity;
import tv.ismar.app.network.entity.SubjectPayLayerEntity;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.statistics.DetailPageStatistics;
import tv.ismar.statistics.PurchaseStatistics;

import static tv.ismar.app.AppConstant.Payment.PAYMENT_FAILURE_CODE;
import static tv.ismar.app.AppConstant.Payment.PAYMENT_REQUEST_CODE;
import static tv.ismar.app.AppConstant.Payment.PAYMENT_SUCCESS_CODE;

/**
 * Created by huaijie on 4/12/16.
 */
public class PayLayerVipActivity extends BaseActivity implements OnHoverListener, View.OnFocusChangeListener {
    private static final String TAG = "PayLayerVipActivity";
    private RecyclerImageView tmp;
    private TvHorizontalScrollView mTvHorizontalScrollView;
    private LinearLayout scrollViewLayout;
    private RecyclerImageView leftArrow;
    private RecyclerImageView rightArrow;
    private TextView vipDescriptionTextView;
    private DetailPageStatistics mPageStatistics;
    private int itemId;
    private Subscription paylayerVipSub;
    private int cpid;
    private String title;
    private String fromPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view=LayoutInflater.from(this).inflate(R.layout.activity_paylayervip,null);
        setContentView(view);
        mPageStatistics = new DetailPageStatistics();
        initViews();
        Intent intent = getIntent();
        cpid = intent.getIntExtra("cpid", -1);
        itemId = intent.getIntExtra("item_id", -1);
        title = intent.getStringExtra("title");
        fromPage = intent.getStringExtra("source");
        payLayerVip(String.valueOf(cpid), String.valueOf(itemId));
//        view.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
//            @Override
//            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
//                if(newFocus!=null){
//                    Log.i("VipFocus",newFocus.toString());
//                }
//            }
//        });
    }

    @Override
    protected void onResume() {
        if(fromPage!=null&&fromPage.equals("gather")){
            mPageStatistics.packageDetailIn(itemId + "",title, fromPage);
        }else {
            mPageStatistics.packageDetailIn(itemId + "", title, "detail");
        }
        super.onResume();
    }

    private void initViews() {
        mTvHorizontalScrollView = (TvHorizontalScrollView) findViewById(R.id.scroll_view);
        scrollViewLayout = (LinearLayout) findViewById(R.id.scroll_layout);
        leftArrow = (RecyclerImageView) findViewById(R.id.left_arrow);
        rightArrow = (RecyclerImageView) findViewById(R.id.right_arrow);
        mTvHorizontalScrollView.setLeftArrow(leftArrow);
        mTvHorizontalScrollView.setRightArrow(rightArrow);
        mTvHorizontalScrollView.setCoverOffset(10);
        tmp = (RecyclerImageView) findViewById(R.id.tmp);
        vipDescriptionTextView = (TextView) findViewById(R.id.vip_description);

        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvHorizontalScrollView.pageScroll(View.FOCUS_LEFT);
            }
        });

        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvHorizontalScrollView.pageScroll(View.FOCUS_RIGHT);
            }
        });

        leftArrow.setOnHoverListener(this);
        rightArrow.setOnHoverListener(this);
    }


    private void payLayerVip(String cpid, String itemId) {
        if(fromPage!=null&&fromPage.equals(Source.GATHER.getValue())){
            mSkyService.apiPaylayerVipSubject(itemId+"")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<SubjectPayLayerEntity>(){


                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(SubjectPayLayerEntity subjectPayLayerEntity) {
                            PayLayerVipEntity payLayerVipEntity=new PayLayerVipEntity();
                            payLayerVipEntity.setCpid(subjectPayLayerEntity.getCpid());
                            payLayerVipEntity.setCpname(subjectPayLayerEntity.getCpname());
                            payLayerVipEntity.setType(subjectPayLayerEntity.getPay_type());
                            payLayerVipEntity.setVip_list(subjectPayLayerEntity.getGather_vip_list());
                            fillLayout(payLayerVipEntity);
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                        }
                    });
        }else {
            paylayerVipSub = mSkyService.apiPaylayerVip(cpid, itemId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<PayLayerVipEntity>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(PayLayerVipEntity payLayerVipEntity) {
                            fillLayout(payLayerVipEntity);
                        }
                    });
        }

    }


    private void fillLayout(final PayLayerVipEntity payLayerVipEntity) {
//        if (payLayerVipEntity.getCpname().equals("iqiyi")) {
//            vipDescriptionTextView.setText(R.string.iqiyi_vip_des_content);
//        } else if (payLayerVipEntity.getCpname().equals("ismartv")) {
//            vipDescriptionTextView.setText(R.string.ismartv_vip_des_content);
//        }
        if (!payLayerVipEntity.getVip_list().isEmpty()) {
            vipDescriptionTextView.setText(payLayerVipEntity.getVip_list().get(0).getDescription());
        }
        scrollViewLayout.removeAllViews();
        int margin = (int) getResources().getDimension(R.dimen.newvip_paylayervip_margin);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(margin, 0, margin, 0);
        for (final PayLayerVipEntity.Vip_list vipList : payLayerVipEntity.getVip_list()) {
            RelativeLayout itemView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_paylayervip, null);
            RecyclerImageView imageView = (RecyclerImageView) itemView.findViewById(R.id.image);
            RecyclerImageView lable= (RecyclerImageView) itemView.findViewById(R.id.lable_image);
            if (TextUtils.isEmpty(vipList.getVertical_url())) {
                Picasso.with(this).load(R.drawable.error_ver).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageView);
            } else {
                Picasso.with(this).load(vipList.getVertical_url()).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).error(R.drawable.error_ver).into(imageView);
            }
            if(vipList.getPk()==1111){
                lable.setVisibility(View.VISIBLE);
            }else{
                lable.setVisibility(View.GONE);
            }

            TextView title = (TextView) itemView.findViewById(R.id.title);
//            title.setText(vipList.getTitle());
            TextView price = (TextView) itemView.findViewById(R.id.price);
//            price.setText((int)(vipList.getPrice()) + "元");
            TextView duration = (TextView) itemView.findViewById(R.id.duration);
//            duration.setText(vipList.getDuration() + "天");
            itemView.setTag(vipList);
            itemView.setOnFocusChangeListener(this);
            itemView.setOnHoverListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buyVideo(vipList.getPk(), payLayerVipEntity.getType(), Float.parseFloat(vipList.getPrice()), Integer.parseInt(vipList.getDuration()), vipList.getTitle());
                    new PurchaseStatistics().expensePacketChoose(
                            vipList.getPk(),
                            vipList.getTitle(),
                            vipList.getPrice(),
                            "enter");
                }
            });
            scrollViewLayout.addView(itemView, layoutParams);
        }

        scrollViewLayout.getChildAt(0).requestFocus();
        scrollViewLayout.getChildAt(0).requestFocusFromTouch();
        if (scrollViewLayout.getChildCount() <= 4) {
            mTvHorizontalScrollView.setLeftArrow(new RecyclerImageView(this));
            mTvHorizontalScrollView.setRightArrow(new RecyclerImageView(this));
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocusFromTouch();
                v.requestFocus();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                tmp.setFocusable(true);
                tmp.requestFocus();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            PayLayerVipEntity.Vip_list vipList = (PayLayerVipEntity.Vip_list) v.getTag();
            vipDescriptionTextView.setText(vipList.getDescription());
            tmp.setFocusable(false);
        }
    }

    private void buyVideo(int pk, String type, float price, int duration, String title) {
        Intent intent = new Intent();
        intent.setClass(this, PaymentActivity.class);
        intent.putExtra(PageIntent.EXTRA_PK, pk);
        intent.putExtra(PageIntentInterface.EXTRA_PRODUCT_CATEGORY, PageIntentInterface.ProductCategory.Package.toString());
        intent.putExtra("movie_id", itemId);
        startActivityForResult(intent, PAYMENT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == PAYMENT_SUCCESS_CODE) {
            setResult(PAYMENT_SUCCESS_CODE, data);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        new PurchaseStatistics().expensePacketChoose(
                0,
                "",
                "",
                "cancel"
               );
        setResult(PAYMENT_FAILURE_CODE);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        if (paylayerVipSub != null && paylayerVipSub.isUnsubscribed()) {
            paylayerVipSub.unsubscribe();
        }
        super.onPause();
    }
}
