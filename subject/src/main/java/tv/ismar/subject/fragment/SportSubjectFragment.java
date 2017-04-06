package tv.ismar.subject.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import cn.ismartv.truetime.TrueTime;
import okhttp3.ResponseBody;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.core.Source;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.Subject;
import tv.ismar.app.entity.Objects;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.PlayCheckEntity;
import tv.ismar.app.network.entity.YouHuiDingGouEntity;
import tv.ismar.app.ui.view.AsyncImageView;
import tv.ismar.app.ui.view.LabelImageView;
import tv.ismar.homepage.widget.HomeItemContainer;
import tv.ismar.homepage.widget.LabelImageView3;
import tv.ismar.statistics.PurchaseStatistics;
import tv.ismar.subject.R;
import tv.ismar.subject.Utils.PayCheckUtil;
import tv.ismar.subject.Utils.SpacesItemDecoration;
import tv.ismar.subject.adapter.OnItemFocusedListener;
import tv.ismar.subject.adapter.SportViewHolder;
import tv.ismar.subject.adapter.SubjectSportAdapter;

import static tv.ismar.app.core.PageIntentInterface.FromPage.unknown;
import static tv.ismar.app.core.PageIntentInterface.ProductCategory.item;
/**
 * Created by liucan on 2017/3/1.
 */

public class SportSubjectFragment extends Fragment implements OnItemFocusedListener,View.OnFocusChangeListener,View.OnHoverListener,View.OnClickListener{
    private RecyclerView sportlist;
    private SubjectSportAdapter madpter;
    private ArrayList<Objects> list=new ArrayList<>();
    private Button buy,play,subscribe;
    private SkyService skyService;
    private LinearLayout relate_list;
    private LabelImageView detail_labelImage;
    private Subscription playCheckSubsc;
    private TextView price,hasbuy;
    private ImageView cp_title,up_arrow,down_arrow,bg;
    private LabelImageView3 relate_image1,relate_image2,relate_image3;
    private TextView relate_text1,relate_text2,relate_text3;
    private Objects objects;
    private String subject_type="NBA";
    private int lastselect=0;
    private int mselect=0;
    private View lastSelectView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.sport_subject_fragment,null);
        sportlist= (RecyclerView) view.findViewById(R.id.sport_list);
        price= (TextView) view.findViewById(R.id.price);
        bg= (ImageView) view.findViewById(R.id.bg_fragment);
        hasbuy= (TextView) view.findViewById(R.id.havebuy);
        up_arrow= (ImageView) view.findViewById(R.id.up_image);
        down_arrow= (ImageView) view.findViewById(R.id.down_image);
        up_arrow.setOnHoverListener(this);
        down_arrow.setOnHoverListener(this);
        arrowListent();
        relate_list= (LinearLayout) view.findViewById(R.id.relate_list);
        relate_image1= (LabelImageView3) view.findViewById(R.id.relate_list_1_image);
        relate_image2= (LabelImageView3) view.findViewById(R.id.relate_list_2_image);
        relate_image3= (LabelImageView3) view.findViewById(R.id.relate_list_3_image);

        relate_text1= (TextView) view.findViewById(R.id.relate_list_1_text);
        relate_text2= (TextView) view.findViewById(R.id.relate_list_2_text);
        relate_text3= (TextView) view.findViewById(R.id.relate_list_3_text);

        buy= (Button) view.findViewById(R.id.buy);
        play= (Button) view.findViewById(R.id.play);
        cp_title= (ImageView) view.findViewById(R.id.cp_title);
        subscribe= (Button) view.findViewById(R.id.subscribe);
        detail_labelImage= (LabelImageView) view.findViewById(R.id.detail_labelImage);
        sportlist.setLayoutManager(new LinearLayoutManager(getActivity()));
        sportlist.addItemDecoration(new SpacesItemDecoration(20));
        skyService=SkyService.ServiceManager.getService();
        madpter=new SubjectSportAdapter(getActivity());
        madpter.setOnItemFocusedListener(this);
//        buy.setOnFocusChangeListener(this);
//        play.setOnFocusChangeListener(this);
        buy.setNextFocusLeftId(R.id.sport_list);
        relate_image1.setNextFocusLeftId(R.id.sport_list);
        sportlist.setOnFocusChangeListener(this);
        getData();
        setScrollListen(sportlist);
        return view;
    }
    private void getData(){
        skyService.getSportSubjectInfo(709763).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(((BaseActivity) getActivity()).new BaseObserver<Subject>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onNext(Subject subject) {
                if(subject!=null){
                    list=subject.objects;
                    if(subject.content_model.contains("nba")){
                        subject_type="NBA";
                    }else{
                        subject_type="PL-英超";
                    }
                    madpter.setData(list,subject_type);
                    sportlist.setAdapter(madpter);
                    madpter.notifyDataSetChanged();
                    Picasso.with(getActivity()).load(subject.bg_url).memoryPolicy(MemoryPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_STORE).into(bg);


                }
            }
        });
    }
    @Override
    public void onItemfocused(View view, int position, boolean hasFocus) {
        if(!hasFocus){
            lastselect=position;
            lastSelectView=view;
            Log.i("sportlist0","onItemfocus false : "+lastselect);
        }else{
            mselect=position;
            Log.i("sportlist0","onItemfocus true : "+position);
            objects=list.get(position);
            Picasso.with(getActivity()).load(objects.poster_url).into(detail_labelImage);
            if(objects.expense!=null){
                if (playCheckSubsc != null && !playCheckSubsc.isUnsubscribed()) {
                    playCheckSubsc.unsubscribe();
                }
                playCheckSubsc=skyService.apiPlayCheck(String.valueOf(objects.pk), null, null)
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
                                    if (playCheckEntity.getRemainDay() == 0) {
                                        buy.setVisibility(View.VISIBLE);
                                        play.setVisibility(View.GONE);
                                        hasbuy.setVisibility(View.GONE);
                                        price.setVisibility(View.VISIBLE);
                                        price.setText(objects.expense.price+"¥");                           // 过期了。认为没购买
                                    } else {
                                        buy.setVisibility(View.GONE);
                                        play.setVisibility(View.VISIBLE);
                                        price.setVisibility(View.GONE);
                                        hasbuy.setVisibility(View.VISIBLE);
                                        hasbuy.setText("已付费：有效期"+objects.expense.duration+"天");                 // 购买了，剩余天数大于0
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                super.onError(e);
                            }
                        });
                if(objects.expense.cptitle!=null){
                    cp_title.setVisibility(View.VISIBLE);
                    String imageUrl= VipMark.getInstance().getImage(getActivity(),3,3);
                    Picasso.with(getActivity()).load(imageUrl).rotate(90).into(cp_title);
                }else{
                    cp_title.setVisibility(View.GONE);
                }
            }else{
                play.setVisibility(View.VISIBLE);
                hasbuy.setVisibility(View.INVISIBLE);
                price.setVisibility(View.INVISIBLE);
            }
            if(subscribeIsShow(objects.start_time)){
                subscribe.setVisibility(View.VISIBLE);
            }else{
                subscribe.setVisibility(View.GONE);
            }
           getRelateData(objects.pk);
        }
    }
    private void getRelateData(int pk){
        skyService.getRelatedArray(pk).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(((BaseActivity) getActivity()).new BaseObserver<Item[]>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onNext(Item[] items) {
                try {
                    if(items!=null) {
                        buildRelateList(items);
                    }
                }catch (Exception e){

                }
            }
        });
    }
    private void buildRelateList(Item[] items){
        Picasso.with(getActivity()).load(items[0].adlet_url).memoryPolicy(MemoryPolicy.NO_STORE).into(relate_image1);
        Picasso.with(getActivity()).load(items[1].adlet_url).memoryPolicy(MemoryPolicy.NO_STORE).into(relate_image2);
        Picasso.with(getActivity()).load(items[2].adlet_url).memoryPolicy(MemoryPolicy.NO_STORE).into(relate_image3);

        relate_text1.setText(items[0].title);
        relate_text2.setText(items[1].title);
        relate_text3.setText(items[2].title);
    }
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            if(lastSelectView!=null){
                sportlist.getChildViewHolder(lastSelectView).itemView.requestFocus();
            }
        }
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

    @Override
    public boolean onHover(View v, MotionEvent event) {
        return false;
    }
    private void arrowListent(){
        up_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("subjectAdapter","lastselect:  "+lastselect);
                if(lastselect-6>=1){
                    sportlist.scrollToPosition(lastselect-6);
                    listItemToBig(sportlist.getChildAt(lastselect-6));
                    listItemToNormal(sportlist.getChildAt(lastselect));
                    sportlist.getChildAt(lastselect-6).requestFocus();

                }
            }
        });
        down_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(list.size()-mselect>6){
                    sportlist.scrollToPosition(lastselect+6);
                    sportlist.getChildAt(lastselect+6).requestFocus();
                }

            }
        });
    }
    private void listItemToBig(View view){
        if(view!=null) {
            SportViewHolder holder = new SportViewHolder(view);
            holder.nomarl.setVisibility(View.GONE);
            holder.focus_tobig.setVisibility(View.VISIBLE);
        }
    }
    private void listItemToNormal(View view){
        if(view!=null) {
            SportViewHolder holder = new SportViewHolder(view);
            holder.nomarl.setVisibility(View.VISIBLE);
            holder.focus_tobig.setVisibility(View.GONE);
        }
    }

    private void setScrollListen(RecyclerView scrollListen) {
        scrollListen.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                View lastItemView= recyclerView.getLayoutManager().getChildAt(recyclerView.getLayoutManager().getChildCount()-1);
                int lastChildBottom = lastItemView.getBottom();
                Log.i("Bootm","lastChildBottom"+lastChildBottom);
                if(dy>0) {
                    if (lastChildBottom != 692) {
                        recyclerView.smoothScrollBy(0, lastChildBottom - 692);
                    }
                    if(list.size()-mselect<6){
                        down_arrow.setBackground(getActivity().getResources().getDrawable(R.drawable.down_normal));
                    }
                    if(mselect>6){
                        up_arrow.setBackground(getActivity().getResources().getDrawable(R.drawable.up_have_data));
                    }
                }else{
                    if(mselect<6){
                        up_arrow.setBackground(getActivity().getResources().getDrawable(R.drawable.up_nomral));
                    }
                    if(list.size()-mselect>6){
                        down_arrow.setBackground(getActivity().getResources().getDrawable(R.drawable.down_have_data));
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.buy) {
            PayCheckUtil pay=new PayCheckUtil();
            pay.handlePurchase(getActivity(),objects);
        }else if(i==R.id.play){
            PageIntent intent=new PageIntent();
            intent.toPlayPage(getActivity(),objects.pk,objects.item_pk, Source.LIST);
        }else if(i==R.id.subscribe){
            showDialog(objects.pk,subject_type);
        }
    }
    private void showDialog(int pk,String type){
        final AlertDialog builder=new AlertDialog.Builder(getActivity()).create();
        builder.setContentView(R.layout.subscribe_dialog);
        builder.show();
        final ImageView code= (ImageView) builder.findViewById(R.id.code_image);
        skyService.getSubscribeImage(pk,type).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(((BaseActivity) getActivity()).new BaseObserver<ResponseBody>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onNext(ResponseBody responseBody) {
                Bitmap bitmap=BitmapFactory.decodeStream(responseBody.byteStream());
                BitmapDrawable bd=new BitmapDrawable(bitmap);
                code.setBackground(bd);
            }
        });
        Button back= (Button) builder.findViewById(R.id.subscribe_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
            }
        });
    }
}
