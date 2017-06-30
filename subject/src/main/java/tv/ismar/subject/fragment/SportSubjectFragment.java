package tv.ismar.subject.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;

import cn.ismartv.truetime.TrueTime;
import okhttp3.ResponseBody;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.Subject;
import tv.ismar.app.entity.Objects;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.network.entity.PlayCheckEntity;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.subject.R;
import tv.ismar.subject.Utils.LableImageSubject;
import tv.ismar.subject.Utils.PayCheckUtil;
import tv.ismar.subject.adapter.SportPresenterHolder;
import tv.libismar.pagerview.VerticalPagerView;


/**
 * Created by liucan on 2017/3/1.
 */

public class SportSubjectFragment extends Fragment implements View.OnHoverListener,View.OnClickListener,
        VerticalPagerView.OnItemActionListener {
    private ArrayList<Objects> list=new ArrayList<>();
    private Button buy,play,subscribe;
    private SkyService skyService;
    private LinearLayout relate_list;
    private ImageView detail_labelImage;
    private Subscription playCheckSubsc;
    private TextView price,hasbuy;
    private ImageView cp_title,up_arrow,down_arrow,bg;
    private LableImageSubject relate_image1,relate_image2,relate_image3;
    private TextView relate_text1,relate_text2,relate_text3;
    private TextView game_time,title;
    private Objects objects;
    private String subject_type="null";
    private PopupWindow popupWindow;
    private TextView divider,relate_title;
    public String channel;
    public String from;
    public int pk;
    public String subjectTitle;
    private LoadingDialog mLoadingDialog;
    private VerticalPagerView mVerticalPagerView;
    private int[] payState;
    private  HashMap<String, Object> out = new HashMap<String, Object>();
    private Handler dialogHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
                ((BaseActivity)getActivity()).showNetWorkErrorDialog(new TimeoutException());
            }
            return false;
        }
    });
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.sport_subject_fragment,null);
        mLoadingDialog = new LoadingDialog(getActivity(), tv.ismar.listpage.R.style.PageIntentDialog);
        mLoadingDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                dialogHandler.sendEmptyMessageDelayed(1,15000);
            }
        });
        mLoadingDialog.setTvText(getResources().getString(tv.ismar.listpage.R.string.loading));
        mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
        mLoadingDialog.showDialog();
        relate_title= (TextView) view.findViewById(R.id.relate_title);
        divider= (TextView) view.findViewById(R.id.sport_subject_divider);
        mVerticalPagerView = (VerticalPagerView) view.findViewById(R.id.sport_list);
        price= (TextView) view.findViewById(R.id.price);
        bg= (ImageView) view.findViewById(R.id.bg_fragment);
        game_time= (TextView) view.findViewById(R.id.game_time);
        title= (TextView) view.findViewById(R.id.title);
        hasbuy= (TextView) view.findViewById(R.id.havebuy);
        up_arrow= (ImageView) view.findViewById(R.id.up_image);
        down_arrow= (ImageView) view.findViewById(R.id.down_image);
        arrowListent();
        relate_list= (LinearLayout) view.findViewById(R.id.relate_list);
        relate_image1= (LableImageSubject) view.findViewById(R.id.relate_list_1_image);
        relate_image2= (LableImageSubject) view.findViewById(R.id.relate_list_2_image);
        relate_image3= (LableImageSubject) view.findViewById(R.id.relate_list_3_image);

        relate_text1= (TextView) view.findViewById(R.id.relate_list_1_text);
        relate_text2= (TextView) view.findViewById(R.id.relate_list_2_text);
        relate_text3= (TextView) view.findViewById(R.id.relate_list_3_text);

        buy= (Button) view.findViewById(R.id.buy);
        play= (Button) view.findViewById(R.id.play);
        subscribe= (Button) view.findViewById(R.id.subscribe);
        buy.setOnClickListener(this);
        play.setOnClickListener(this);
        subscribe.setOnClickListener(this);
        buy.setOnHoverListener(this);
        play.setOnHoverListener(this);
        subscribe.setOnHoverListener(this);

        cp_title= (ImageView) view.findViewById(R.id.cp_title);
        detail_labelImage= (ImageView) view.findViewById(R.id.detail_labelImage);
        skyService=SkyService.ServiceManager.getService();
        mVerticalPagerView.setOnItemActionListener(this);
        getData();
        return view;
    }

    @Override
    public void onResume() {
        if(!subject_type.equals("null")){
            sendLog();
        }
        super.onResume();
    }

    private void getData(){
        skyService.getSportSubjectInfo(pk).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(((BaseActivity) getActivity()).new BaseObserver<Subject>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onNext(Subject subject) {
                if(subject!=null){
                    list=subject.objects;
                    payState=new int[list.size()];
                    Collections.sort(list, new Comparator<Objects>() {
                        @Override
                        public int compare(Objects lhs, Objects rhs) {
                            if(lhs.start_time.getTime()!=rhs.start_time.getTime()){
                                    return (int) (lhs.start_time.getTime()-rhs.start_time.getTime());
                            }else{
                                    return -(lhs.recommend_status-rhs.recommend_status);
                            }
                        }
                    });
                    if(subject.content_model.contains("nba")){
                        subject_type="NBA";
                    }else{
                        subject_type="PL";
                    }
                    mVerticalPagerView.addDatas(list);
                    mLoadingDialog.dismiss();
                    Picasso.with(getActivity()).load(subject.bg_url).memoryPolicy(MemoryPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_STORE).into(bg);
                    down_arrow.setVisibility(View.VISIBLE);
                    up_arrow.setVisibility(View.VISIBLE);
                    up_arrow.setBackground(getActivity().getResources().getDrawable(R.drawable.up_nomral));
                    divider.setVisibility(View.VISIBLE);
                    relate_title.setVisibility(View.VISIBLE);
                    if(list.size()>6){
                        down_arrow.setBackground(getActivity().getResources().getDrawable(R.drawable.arrow_down_hover));
                    }else {
                        down_arrow.setBackground(getActivity().getResources().getDrawable(R.drawable.down_normal));
                    }
                    sendLog();
                }
            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                super.onError(e);
            }
        });
    }
    private Handler relateHandler=new Handler();
    private Handler payHandler=new Handler();
    private Runnable runnable=new Runnable() {
        @Override
        public void run() {
            getRelateData(objects.pk);
        }
    };
    private Runnable payRunnable=new Runnable() {
        @Override
        public void run() {
            payCheck();
        }
    };
    private void buildDetail(){
        objects=list.get(mVerticalPagerView.getCurrentDataSelectPosition());
        if(objects.poster_url!=null)
        Picasso.with(getActivity()).load(objects.poster_url).into(detail_labelImage);
        int index=mVerticalPagerView.getCurrentDataSelectPosition();
        payHandler.removeCallbacks(payRunnable);
        if(objects.expense!=null){
            if (playCheckSubsc != null && !playCheckSubsc.isUnsubscribed()) {
                playCheckSubsc.unsubscribe();
            }
//                if(objects.expense.cptitle!=null){
//                    cp_title.setVisibility(View.VISIBLE);
//                    String imageUrl= VipMark.getInstance().getImage(getActivity(),3,3);
//                    Picasso.with(getActivity()).load(imageUrl).rotate(90).into(cp_title);
//                }else{
//                    cp_title.setVisibility(View.GONE);
//                }
            if(payState!=null) {
                if(payState[index]==0) {
                    payHandler.postDelayed(payRunnable, 500);
                }else if(payState[index]==1){
                    play.setVisibility(View.GONE);
                    buy.setVisibility(View.VISIBLE);
                    hasbuy.setVisibility(View.GONE);
                    price.setVisibility(View.VISIBLE);
                    price.setText(objects.expense.price+"¥");
                }else{
                    play.setVisibility(View.VISIBLE);
                    buy.setVisibility(View.GONE);
                    price.setVisibility(View.GONE);
                    hasbuy.setVisibility(View.VISIBLE);
                    hasbuy.setText("已付费：有效期"+objects.expense.duration+"天");
                }
            }
        }else{
            play.setVisibility(View.VISIBLE);
            buy.setVisibility(View.GONE);
            hasbuy.setVisibility(View.INVISIBLE);
            price.setVisibility(View.INVISIBLE);
        }
        if(subscribeIsShow(objects.start_time)){
            subscribe.setVisibility(View.VISIBLE);
        }else{
            subscribe.setVisibility(View.GONE);
        }
        if(playIsShow(objects.start_time)){
            play.setEnabled(true);
            play.setClickable(true);
            play.setFocusable(true);
            play.setFocusableInTouchMode(true);
            play.setTextColor(getResources().getColor(R.color._ffffff));
            play.setBackground(getResources().getDrawable(R.drawable.play_selector));
        }else{
            play.setClickable(false);
            play.setFocusable(false);
            play.setFocusableInTouchMode(false);
            play.setBackground(getResources().getDrawable(R.drawable.play_disable));
            play.setTextColor(getResources().getColor(R.color._999999));
        }
        String[] titles=objects.title.split("-");
        game_time.setText(titles[0]);
        title.setText(titles[1]);
        relateHandler.removeCallbacks(runnable);
        relateHandler.postDelayed(runnable,500);
    }
    private void clearRelateDate(){
       relate_list.setVisibility(View.INVISIBLE);
    }
    private void payCheck (){
        playCheckSubsc=skyService.apiPlayCheck(String.valueOf(objects.item_pk), null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((BaseActivity) getActivity()).new BaseObserver<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            PayCheckUtil payCheck=new PayCheckUtil();
                            PlayCheckEntity  playCheckEntity =payCheck.calculateRemainDay(responseBody.string());
                            int index=mVerticalPagerView.getCurrentDataSelectPosition();
                            if (playCheckEntity.getRemainDay() == 0) {
                                payState[index]=1;
                                buy.setVisibility(View.VISIBLE);
                                play.setVisibility(View.GONE);
                                hasbuy.setVisibility(View.GONE);
                                price.setVisibility(View.VISIBLE);
                                price.setText(objects.expense.price+"¥");
                                                      // 过期了。认为没购买
                            } else {
                                payState[index]=2;
                                play.setVisibility(View.VISIBLE);
                                buy.setVisibility(View.GONE);
                                price.setVisibility(View.GONE);
                                hasbuy.setVisibility(View.VISIBLE);
                                hasbuy.setText("已付费：有效期"+objects.expense.duration+"天");  // 购买了，剩余天数大于0
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                });
    }
    private void getRelateData(int pk){
        skyService.getRelatedArray(pk).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(((BaseActivity) getActivity()).new BaseObserver<Item[]>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onNext(Item[] items) {
                if(items!=null&&items.length>=3) {
                    buildRelateList(items);
                }else{
                    play.setNextFocusDownId(R.id.play);
                    subscribe.setNextFocusDownId(R.id.subscribe);
                    buy.setNextFocusDownId(R.id.buy);
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        });
    }
    private void buildRelateList(final Item[] items){
        relate_image1.setLivUrl(items[0].adlet_url);
        relate_image2.setLivUrl(items[1].adlet_url);
        relate_image3.setLivUrl(items[2].adlet_url);

        relate_list.setVisibility(View.VISIBLE);

        play.setNextFocusDownId(R.id.relate_list_1_image);
        buy.setNextFocusDownId(R.id.relate_list_1_image);
        subscribe.setNextFocusDownId(R.id.relate_list_1_image);

        relate_text1.setText(items[0].title);
        relate_text2.setText(items[1].title);
        relate_text3.setText(items[2].title);
        relate_image1.setOnHoverListener(relateOnhoverListener);
        relate_image2.setOnHoverListener(relateOnhoverListener);
        relate_image3.setOnHoverListener(relateOnhoverListener);

        relate_image1.setOnFocusChangeListener(imageFocus);

        final PageIntent intent=new PageIntent();
        relate_image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveIndex=mVerticalPagerView.getCurrentDataSelectPosition();
                Log.i("Onclick","relateOnclick"+leaveIndex);
                intent.toDetailPage(getActivity(),"gather",items[0].pk);
                out.put("to","detail");
            }
        });
        relate_image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveIndex=mVerticalPagerView.getCurrentDataSelectPosition();
                intent.toDetailPage(getActivity(),"gather",items[1].pk);
                out.put("to","detail");
            }
        });
        relate_image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveIndex=mVerticalPagerView.getCurrentDataSelectPosition();
                intent.toDetailPage(getActivity(),"gather",items[2].pk);
                out.put("to","detail");
            }
        });

    }
    private boolean subscribeIsShow(Date time) {
        if (time != null) {
            Calendar currentCalendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
            currentCalendar.setTime(TrueTime.now());
            Calendar startCalendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
            startCalendar.setTime(time);
            if(startCalendar.getTimeInMillis()-currentCalendar.getTimeInMillis()>900000){
                return true;
            }else{
                return false;
            }
        } else {
            return false;
        }
    }
    private boolean playIsShow(Date time){
        if (time != null) {
            Calendar currentCalendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
            currentCalendar.setTime(TrueTime.now());
            Calendar startCalendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
            startCalendar.setTime(time);
            if(startCalendar.getTimeInMillis()-currentCalendar.getTimeInMillis()<15*60*1000){
                return true;
            }else{
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_MOVE:
                break;
            case MotionEvent.ACTION_HOVER_ENTER:
                    leaveIndex=mVerticalPagerView.getCurrentDataSelectPosition();
                    Log.i("btnHover","leaverIndex: "+leaveIndex);
                    v.requestFocus();
                    v.requestFocusFromTouch();
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
//                   Log.i("btnHover","currentPosition :"+mVerticalPagerView.getCurrentDataSelectPosition()+"  leaveIndex: "+leaveIndex);
//                    if(leaveIndex>=0){
//                        mVerticalPagerView.getChildViewAt(leaveIndex).requestFocusFromTouch();
//                    }
                default:
                    break;
        }

        return false;
    }
    private View.OnHoverListener relateOnhoverListener=new View.OnHoverListener() {
       @Override
       public boolean onHover(View v, MotionEvent event) {
           switch (event.getAction()){
               case MotionEvent.ACTION_HOVER_MOVE:
               case MotionEvent.ACTION_HOVER_ENTER:
                   down_arrow.setFocusable(false);
                   down_arrow.setFocusableInTouchMode(false);
                   leaveIndex=mVerticalPagerView.getCurrentDataSelectPosition();
                   v.requestFocus();
                   v.requestFocusFromTouch();
                   Log.i("btnHover","leaverIndex: "+leaveIndex);
                   break;
               case MotionEvent.ACTION_HOVER_EXIT:
                   Log.i("relateHover","relate exitHover"+ leaveIndex);
                   break;
               default:
                   break;
           }
           return false;
       }
   };
    private View.OnFocusChangeListener imageFocus=new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus){
                down_arrow.setFocusableInTouchMode(false);
                down_arrow.setFocusable(false);
            }
        }
    };
    private void arrowListent(){
        up_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // longhai TODO
                leaveIndex=-1;
                mVerticalPagerView.pageArrowUp();

            }
        });
        down_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // longhai TODO
                leaveIndex=-1;
                mVerticalPagerView.pageArrowDown();
            }
        });
        up_arrow.setOnHoverListener(arrowOnhover);
        down_arrow.setOnHoverListener(arrowOnhover);
    }

    private View.OnHoverListener arrowOnhover=new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_HOVER_ENTER:
                case  MotionEvent.ACTION_HOVER_MOVE:
                    leaveIndex=mVerticalPagerView.getCurrentDataSelectPosition();
                    v.setFocusableInTouchMode(true);
                    v.requestFocusFromTouch();
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    break;
            }
            return false;
        }
    };
    private void listItemToBig(View view,int position){
        if(view!=null) {
            RelativeLayout big= (RelativeLayout) view.findViewById(R.id.focus_tobig);
            RelativeLayout normal= (RelativeLayout) view.findViewById(R.id.nomarl);
            normal.setVisibility(View.GONE);
            big.setVisibility(View.VISIBLE);
            Objects ob=list.get(position);
            if(ob.recommend_status==1){
                big.setBackgroundResource(R.drawable.big_game_hover);
            }else{
                big.setBackgroundResource(R.drawable.emphasis_focus_hover);
            }
        }
    }
    private void listItemToNormal(View view,int position){
        if(view!=null) {
            RelativeLayout big= (RelativeLayout) view.findViewById(R.id.focus_tobig);
            RelativeLayout normal= (RelativeLayout) view.findViewById(R.id.nomarl);
            normal.setVisibility(View.VISIBLE);
            big.setVisibility(View.GONE);
            Objects ob=list.get(position);
            if(ob.recommend_status==1){
                normal.setBackgroundResource(R.drawable.normal_game);
            }else{
                normal.setBackgroundResource(R.drawable.emphasis_game_normal);
            }
        }
    }
    private void bigItemNoSelect(View view,int position){
        if(view!=null) {
            RelativeLayout big= (RelativeLayout) view.findViewById(R.id.focus_tobig);
            RelativeLayout normal= (RelativeLayout) view.findViewById(R.id.nomarl);
            if(big.getVisibility()==View.VISIBLE) {
                Objects ob = list.get(position);
                if (ob.recommend_status == 1) {
                    big.setBackgroundResource(R.drawable.normal_game_focus);
                } else {
                    big.setBackgroundResource(R.drawable.emphasis_game_focus);
                }
            }
        }
    }
    private void bigItemSelect(View view,int position){
        if(view!=null) {
            RelativeLayout big= (RelativeLayout) view.findViewById(R.id.focus_tobig);
            RelativeLayout normal= (RelativeLayout) view.findViewById(R.id.nomarl);
            if(big.getVisibility()==View.VISIBLE) {
                Objects ob = list.get(position);
                if (ob.recommend_status == 1) {
                    big.setBackgroundResource(R.drawable.big_game_hover);
                } else {
                    big.setBackgroundResource(R.drawable.emphasis_focus_hover);
                }
            }
        }
    }
    private void normalItemNoSelect(View view,int position){
        if(view!=null) {
            RelativeLayout normal = (RelativeLayout) view.findViewById(R.id.nomarl);
            if(normal.getVisibility()==View.VISIBLE){
                Objects ob = list.get(position);
                if (ob.recommend_status == 1) {
                    normal.setBackgroundResource(R.drawable.normal_game);
                } else {
                    normal.setBackgroundResource(R.drawable.emphasis_game_normal);
                }
            }
        }
    }
    @Override
    public void onClick(View v) {
        leaveIndex=mVerticalPagerView.getCurrentDataSelectPosition();
        int i = v.getId();
        if (i == R.id.buy) {
            PayCheckUtil pay=new PayCheckUtil();
            pay.handlePurchase(getActivity(),objects);
            out.put("to","expense");
        }else if(i==R.id.play){
            PageIntent intent=new PageIntent();
            intent.toPlayPage(getActivity(),objects.pk,0,Source.GATHER);
            out.put("to","player");
            out.put("to_item",objects.pk);
            out.put("to_title",objects.title);
        }else if(i==R.id.subscribe){
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put(EventProperty.TITLE, objects.title);
            properties.put(EventProperty.ITEM,objects.pk);
            properties.put(EventProperty.USER,IsmartvActivator.getInstance().getUsername());
            new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_RESERVE, properties);
            showDialog(objects.pk,subject_type);
        }
    }
    private void showDialog(int pk,String type) {
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.subscribe_dialog, null);
        popupWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
        popupWindow.setContentView(contentView);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
        View rootview = LayoutInflater.from(getActivity()).inflate(R.layout.sport_subject_fragment, null);
        popupWindow.showAtLocation(rootview, Gravity.CENTER, 0, 0);
        final ImageView code = (ImageView) contentView.findViewById(R.id.code_image);
        skyService.getSubscribeImage(pk, type).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(((BaseActivity) getActivity()).new BaseObserver<ResponseBody>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onNext(ResponseBody responseBody) {
                Bitmap bitmap = BitmapFactory.decodeStream(responseBody.byteStream());
                BitmapDrawable bd = new BitmapDrawable(bitmap);
                code.setBackground(bd);
            }
        });
        Button btn = (Button) contentView.findViewById(R.id.subscribe_back);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }
    @Override
    public void onPause() {
        relateHandler.removeCallbacks(runnable);
        payHandler.removeCallbacks(payRunnable);
        if(out.get("to")==null){
            out.put("to","exit");
        }
        out.put("title",subjectTitle);
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_GATHER_OUT, out);
        super.onPause();
    }
    private DialogInterface.OnCancelListener mLoadingCancelListener = new DialogInterface.OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
            getActivity().finish();
            dialog.dismiss();
        }
    };

    private void sendLog(){
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(EventProperty.CHANNEL,channel );
        properties.put(EventProperty.TITLE, subject_type);
        properties.put(EventProperty.FROM,from);
        properties.put(EventProperty.TITLE,subjectTitle);

        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_GATHER_IN, properties);
    }

    @Override
    public void onItemClick(View view, int i) {
        Log.i("OnItemclick","true"+ "position: "+i);
        view.requestFocusFromTouch();
    }

    @Override
    public void onItemFocusChanged(View view, boolean focused, int position) {
        Log.d("LH/", "onItemFocusChanged:" + focused + " - " + position+" - "+mVerticalPagerView.getCurrentDataSelectPosition());
        up_arrow.setFocusableInTouchMode(false);
        down_arrow.setFocusableInTouchMode(false);
        if (leaveIndex >= 0) {
            if (leaveIndex != position) {
                mVerticalPagerView.getChildViewAt(leaveIndex).requestFocus();
            }
            if (focused){
                leaveIndex = -1;
                bigItemSelect(view,position);
            }else{
                bigItemNoSelect(view,position);
            }
        } else {
            if (focused) {
                listItemToBig(view,position);
                buildDetail();
            } else {
                listItemToNormal(view,position);
                clearRelateDate();
            }
        }

    }
    private int lastHoverIndex=-1;
    @Override
    public void onItemHovered(View view, MotionEvent event, Object var3, int var4) {
        RelativeLayout big= (RelativeLayout) view.findViewById(R.id.focus_tobig);
        RelativeLayout normal= (RelativeLayout) view.findViewById(R.id.nomarl);
        Objects ob=list.get(var4);
        int recommand=ob.recommend_status;
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_MOVE:
                break;
            case MotionEvent.ACTION_HOVER_ENTER:
                if(leaveIndex>=0){
                    mVerticalPagerView.getChildViewAt(leaveIndex).requestFocusFromTouch();
                }
                if(view.hasFocus()){
                    if(recommand==1){
                        big.setBackgroundResource(R.drawable.big_game_hover);
                    }else{
                        big.setBackgroundResource(R.drawable.emphasis_focus_hover);
                    }
                }else{
                    if(recommand==1){
                        normal.setBackgroundResource(R.drawable.normal_game_hover);
                    }else{
                        normal.setBackgroundResource(R.drawable.emphasis_hover);
                    }
                    View view1=mVerticalPagerView.getChildViewAt(mVerticalPagerView.getCurrentDataSelectPosition());
                    bigItemNoSelect(view1,mVerticalPagerView.getCurrentDataSelectPosition());
                    lastHoverIndex=var4;
                }
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                if(view.hasFocus()){
                    if(recommand==1){
                        big.setBackgroundResource(R.drawable.normal_game_focus);
                    }else{
                        big.setBackgroundResource(R.drawable.emphasis_game_focus);
                    }
                }else{
                    if(recommand==1){
                        normal.setBackgroundResource(R.drawable.normal_game);
                    }else{
                        normal.setBackgroundResource(R.drawable.emphasis_game_normal);
                    }
                }
                lastHoverIndex=-1;
                break;
        }
    }
    private int leaveIndex=-1;
    @Override
    public void onKeyDown(View view, int i, KeyEvent keyEvent) {
        switch (i){
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                leaveIndex = mVerticalPagerView.getCurrentDataSelectPosition();
                if(lastHoverIndex>=0){
                    View view1=mVerticalPagerView.getChildViewAt(lastHoverIndex);
                    normalItemNoSelect(view1,lastHoverIndex);
                }
                if(leaveIndex>=0){
                    bigItemNoSelect(view,leaveIndex);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
                up_arrow.setFocusableInTouchMode(false);
                up_arrow.setFocusable(false);
                down_arrow.setFocusableInTouchMode(false);
                down_arrow.setFocusable(false);
                if(lastHoverIndex>=0){
                    View view1=mVerticalPagerView.getChildViewAt(lastHoverIndex);
                    normalItemNoSelect(view1,lastHoverIndex);
                }
                break;
        }
    }

    @Override
    public void onBindView(View itemView, Object object,int position) {
        SportPresenterHolder sportPresenterHolder = new SportPresenterHolder(itemView);
        setUIData(sportPresenterHolder, (Objects) object);
        if (mVerticalPagerView.getFirstVisibleChildIndex() == 0) {
            up_arrow.setBackground(getActivity().getResources().getDrawable(R.drawable.up_nomral));
            up_arrow.setHovered(false);
            down_arrow.setBackground(getActivity().getResources().getDrawable(R.drawable.arrow_down_hover));
        } else if (mVerticalPagerView.getLastVisibleChildIndex() == list.size() - 1) {
            up_arrow.setBackground(getActivity().getResources().getDrawable(R.drawable.arrow_hover_select));
            down_arrow.setBackground(getActivity().getResources().getDrawable(R.drawable.down_normal));
            down_arrow.setHovered(false);
        } else {
            up_arrow.setBackground(getActivity().getResources().getDrawable(R.drawable.arrow_hover_select));
            down_arrow.setBackground(getActivity().getResources().getDrawable(R.drawable.arrow_down_hover));
            down_arrow.setHovered(true);
            up_arrow.setHovered(true);
        }
        if(position==mVerticalPagerView.getCurrentDataSelectPosition()){
            buildDetail();
            if(mVerticalPagerView.getChildViewAt(position)!=null) {
                mVerticalPagerView.getChildViewAt(position).requestFocusFromTouch();
                bigItemSelect(mVerticalPagerView.getChildViewAt(position),position);
            }
        }

    }



    // TODO 后续设计模式替代
    private void setUIData(SportPresenterHolder holder, Objects objects){
        Context context = getActivity().getApplicationContext();
        if (subject_type.equals("NBA")) {
            Picasso.with(context).load(objects.at_home_logo).into(holder.home_logo);
            Picasso.with(context).load(objects.at_home_logo).into(holder.big_home_logo);
            Picasso.with(context).load(objects.be_away_logo).into(holder.away_loga);
            Picasso.with(context).load(objects.be_away_logo).into(holder.big_away_logo);
            holder.away_name.setText(objects.be_away_name);
            holder.home_name.setText(objects.at_home_name);
            holder.big_away_name.setText(objects.be_away_name);
            holder.big_home_name.setText(objects.at_home_name);
        } else {
            holder.big_home.setText("(客)");
            holder.big_away.setText("(主)");
            Picasso.with(context).load(objects.at_home_logo).into(holder.away_loga);
            Picasso.with(context).load(objects.at_home_logo).into(holder.big_away_logo);
            Picasso.with(context).load(objects.be_away_logo).into(holder.home_logo);
            Picasso.with(context).load(objects.be_away_logo).into(holder.big_home_logo);
            holder.away_name.setText(objects.at_home_name);
            holder.home_name.setText(objects.be_away_name);
            holder.big_away_name.setText(objects.at_home_name);
            holder.big_home_name.setText(objects.be_away_name);
        }
        Boolean is_alive = videoIsStart(objects.start_time);
        Log.i("subject", is_alive + "");
        if (objects.start_time != null) {
            if (is_alive) {
                holder.big_time.setText("直播中");
                holder.big_time.setTextColor(context.getResources().getColor(R.color._cc0033));
                holder.start_time_layout.setVisibility(View.GONE);
                holder.isalive.setText("直播中");
                holder.isalive.setVisibility(View.VISIBLE);
            } else {
                holder.isalive.setVisibility(View.GONE);
                holder.start_time_layout.setVisibility(View.VISIBLE);
                SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm");
                formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                String time = formatter.format(objects.start_time);
                String times[] = time.split(" ");
                String month[] = times[0].split("-");
                holder.big_time.setTextColor(context.getResources().getColor(R.color._333333));
                holder.big_time.setText(month[0] + "月" + month[1] + "日" + " " + times[1] + " 未开始");
                holder.start_time_ym.setText(month[0] + "月" + month[1] + "日");
                holder.start_time.setText(times[1]);
            }
        }
        if (objects.recommend_status == 1) {
            holder.nomarl.setBackgroundResource(R.drawable.item_normal_selector);
            holder.focus_tobig.setBackgroundResource(R.drawable.item_bg_selector);
        } else {
            holder.nomarl.setBackgroundResource(R.drawable.item_recommend_normal_hovered);
            holder.focus_tobig.setBackgroundResource(R.drawable.item_recommend_big_hoverd);
        }
    }

    private boolean videoIsStart(Date time) {
        if (time != null) {
            Calendar currentCalendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
            currentCalendar.setTime(TrueTime.now());
            Calendar startCalendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
            startCalendar.setTime(time);
            if (currentCalendar.after(startCalendar)) {
                if(currentCalendar.getTimeInMillis()-startCalendar.getTimeInMillis()<5400000){
                    return true;
                }else{
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public void clearPayState(){
        if(payState!=null) {
            for (int i = 0; i < payState.length; i++) {
                payState[i] = 0;
            }
        }
        if(payHandler!=null) {
            payHandler.removeCallbacks(payRunnable);
            payHandler.postDelayed(payRunnable, 500);
        }
    }
}
