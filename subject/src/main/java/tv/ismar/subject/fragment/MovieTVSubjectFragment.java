package tv.ismar.subject.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
//import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import cn.ismartv.truetime.TrueTime;
import okhttp3.ResponseBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.core.Source;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.db.FavoriteManager;
import tv.ismar.app.entity.Favorite;
import tv.ismar.app.models.SubjectEntity;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.network.entity.SubjectPayLayerEntity;
import tv.ismar.app.ui.adapter.OnItemClickListener;
import tv.ismar.app.ui.adapter.OnItemFocusedListener;
import tv.ismar.app.util.Utils;
import tv.ismar.app.widget.MyRecyclerView;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.library.exception.ExceptionUtils;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.statistics.PurchaseStatistics;
import tv.ismar.subject.R;
import tv.ismar.subject.SubjectActivity;
import tv.ismar.subject.adapter.SubjectMovieAdapter;
import tv.ismar.subject.adapter.SubjectTvAdapter;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static tv.ismar.app.core.PageIntentInterface.ProductCategory.item;

/**
 * Created by admin on 2017/3/2.
 */

public class MovieTVSubjectFragment extends Fragment implements View.OnClickListener, OnItemClickListener, View.OnFocusChangeListener, View.OnHoverListener {

    private MyRecyclerView movie_recyclerView;
    private MyRecyclerView tv_recyclerView;
    private TextView subject_actor;
    private TextView subject_description;
    private Button subject_btn_buy;
    private Button subject_btn_like;
    private SubjectMovieAdapter movieAdapter;
    private RecyclerImageView left_layer_movie;
    private RecyclerImageView right_layer_movie;
    private RecyclerImageView left_layer_tv;
    private RecyclerImageView right_layer_tv;
    private RecyclerImageView tv_poster_focus;
    private RecyclerImageView poster_focus;
    private List<SubjectEntity.ObjectsBean> list;
    private SubjectTvAdapter tvAdapter;
    private String type;
    private int id;
    private View subject_movie;
    private View subject_tv;
    private String isnet="no";
    private FavoriteManager mFavoriteManager;
    private RecyclerImageView subject_bg;
    private View focusView;
    private boolean btn_buy_focused=false;
    private boolean btn_like_focused=false;
    private SubjectEntity mSubjectEntity=new SubjectEntity();
    private boolean isScaledIn=true;
    private String channel="";
    private boolean showPayLayer=false;
    private boolean clickble=true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.movie_tv_subject_fragment,null);
        initView(content);
        return content;
    }

    private void initView(View content) {
        subject_movie = content.findViewById(R.id.subject_movie);
        movie_recyclerView = (MyRecyclerView) content.findViewById(R.id.movie_recyclerView);
        subject_tv = content.findViewById(R.id.subject_tv);
        tv_recyclerView = (MyRecyclerView) content.findViewById(R.id.tv_recyclerView);
        subject_actor = (TextView) content.findViewById(R.id.subject_actor);
        subject_description = (TextView) content.findViewById(R.id.subject_description);
        subject_btn_buy = (Button) content.findViewById(R.id.subject_btn_buy);
        subject_btn_like = (Button) content.findViewById(R.id.subject_btn_like);
        left_layer_movie = (RecyclerImageView) content.findViewById(R.id.left_layer_movie);
        right_layer_movie = (RecyclerImageView) content.findViewById(R.id.right_layer_movie);
        poster_focus = (RecyclerImageView) content.findViewById(R.id.poster_focus);
        left_layer_tv= (RecyclerImageView) content.findViewById(R.id.left_layer_tv);
        right_layer_tv = (RecyclerImageView) content.findViewById(R.id.right_layer_tv);
        tv_poster_focus= (RecyclerImageView) content.findViewById(R.id.tv_poster_focus);
        subject_bg = (RecyclerImageView) content.findViewById(R.id.subject_bg);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        subject_btn_buy.setOnClickListener(this);
        subject_btn_like.setOnClickListener(this);
        subject_btn_buy.setOnHoverListener(this);
        subject_btn_like.setOnHoverListener(this);
        movie_recyclerView.setOnFocusChangeListener(this);
        tv_recyclerView.setOnFocusChangeListener(this);
    }

    private void initData() {
        type = ((SubjectActivity)getActivity()).gather_type;
        id = ((SubjectActivity)getActivity()).itemid;
        if(((SubjectActivity)getActivity()).fromPage!=null&&((SubjectActivity)getActivity()).fromPage.equals(Source.LIST.getValue())){
            channel= BaseActivity.baseChannel;
        }
        mFavoriteManager = DaisyUtils.getFavoriteManager(getActivity());
        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken())) {
            isnet = "yes";
        } else {
            isnet = "no";
        }
        ((SubjectActivity)getActivity()).mSkyService.apiSubject(id+"")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((SubjectActivity)getActivity()).new BaseObserver<SubjectEntity>(){


                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(SubjectEntity subjectEntity) {
                        mSubjectEntity = subjectEntity;
                        video_gather_in(mSubjectEntity.getTitle(),((SubjectActivity)getActivity()).fromPage,channel);
                        processData(subjectEntity);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                });
    }
    private void processData(SubjectEntity subjectEntity) {
        if(subjectEntity.isIs_buy()){
            subject_btn_buy.setVisibility(View.VISIBLE);
        }else{
            subject_btn_buy.setVisibility(View.GONE);
        }
        if(subjectEntity.getBg_url()!=null&&!"".equals(subjectEntity.getBg_url()))
        Picasso.with(getActivity()).load(subjectEntity.getBg_url()).memoryPolicy(MemoryPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_STORE).into(subject_bg);
        list=subjectEntity.getObjects();
        if(type.contains("movie")){
            //电影专题
            subject_btn_buy.setNextFocusLeftId(R.id.movie_recyclerView);
            movieAdapter=new SubjectMovieAdapter(getActivity(),list);
            subject_movie.setVisibility(View.VISIBLE);
            subject_btn_buy.setNextFocusUpId(R.id.movie_recyclerView);
            subject_btn_like.setNextFocusUpId(R.id.movie_recyclerView);
            movie_recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL));
            movie_recyclerView.setAdapter(movieAdapter);
            movieAdapter.setOnItemClickListener(this);
            movie_recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    Log.i("MovieClick","scroll change: "+newState);
                    if(newState==SCROLL_STATE_IDLE){
                        if(recyclerView.getFocusedChild()!=null) {
                            int viewX = (int) recyclerView.getFocusedChild().getX();
                            if (viewX != getResources().getDimensionPixelOffset(R.dimen.subject_movie_recycleview_ml)) {
                                recyclerView.smoothScrollBy((int) (viewX - getResources().getDimensionPixelOffset(R.dimen.subject_movie_recycleview_ml)), 0);
                            }
                        }
                    }
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });

            movieAdapter.setOnItemFocusedListener(new OnItemFocusedListener() {



                @Override
                public void onItemfocused(View view, int position, boolean hasFocus) {
                    if(hasFocus) {
                        if(!isScaledIn){
                            JasmineUtil.scaleIn2(focusView);
                        }
                        Log.i("MovieFocus","hasFocus: "+position);
                        checkLayerIsShow(position);
                        poster_focus.setVisibility(View.VISIBLE);
                        movie_recyclerView.smoothScrollBy((int) (view.getX()-getResources().getDimensionPixelOffset(R.dimen.subject_movie_recycleview_ml)), 0);
                        JasmineUtil.scaleOut2(view);
                        subject_actor.setText(list.get(position).getMsg1());
                        subject_description.setText(list.get(position).getMsg2());
                    }else{
                        if(subject_btn_like.isFocused()||subject_btn_buy.isFocused()){
                            focusView = view;
                            isScaledIn = false;
                        }else{
                            isScaledIn = true;
                            JasmineUtil.scaleIn2(view);
                        }
                        poster_focus.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }else {
            //电视剧专题
            subject_btn_buy.setNextFocusLeftId(R.id.tv_recyclerView);
            tvAdapter=new SubjectTvAdapter(getActivity(),list);
            subject_tv.setVisibility(View.VISIBLE);
            subject_btn_buy.setNextFocusUpId(R.id.tv_recyclerView);
            subject_btn_like.setNextFocusUpId(R.id.tv_recyclerView);
            tv_recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
            tv_recyclerView.setAdapter(tvAdapter);
            tvAdapter.setOnItemClickListener(this);
            tvAdapter.setOnItemFocusedListener(new OnItemFocusedListener() {
                @Override
                public void onItemfocused(View view, int position, boolean hasFocus) {
                    if (hasFocus) {
                        if(!isScaledIn){
                            JasmineUtil.scaleIn2(focusView);
                        }
                        checkLayerIsShow(position);
                        tv_poster_focus.setVisibility(View.VISIBLE);
                        JasmineUtil.scaleOut2(view);
                        tv_recyclerView.smoothScrollBy((int) (view.getX()-getResources().getDimensionPixelOffset(R.dimen.subject_tv_recycleview_ml)), 0);
                        Log.e("position", view.getX() + "");
                        subject_actor.setText(list.get(position).getMsg1());
                        subject_description.setText(list.get(position).getMsg2());
                    } else {
                        if(subject_btn_like.isFocused()||subject_btn_buy.isFocused()){
                            focusView = view;
                            isScaledIn = false;
                        }else{
                            JasmineUtil.scaleIn2(view);
                            isScaledIn = true;
                        }
                        tv_poster_focus.setVisibility(View.INVISIBLE);
                    }
                }
            });
            tv_recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if(newState==SCROLL_STATE_IDLE){
                        if(recyclerView.getFocusedChild()!=null) {
                            int viewX = (int) recyclerView.getFocusedChild().getX();
                            if (viewX != getResources().getDimensionPixelOffset(R.dimen.subject_movie_recycleview_ml)) {
                                recyclerView.smoothScrollBy((int) (viewX - getResources().getDimensionPixelOffset(R.dimen.subject_tv_recycleview_ml)), 0);
                            }
                        }
                    }
                    super.onScrollStateChanged(recyclerView, newState);
                }
            });
        }
    }

    private void checkLayerIsShow(int position) {
        if(type.contains("movie")) {
            if (position > 0) {
                left_layer_movie.setVisibility(View.VISIBLE);
            } else {
                left_layer_movie.setVisibility(View.INVISIBLE);
            }
            if (position >= list.size() - 5) {
                right_layer_movie.setVisibility(View.INVISIBLE);
            } else {
                right_layer_movie.setVisibility(View.VISIBLE);
            }
        }else{
            if(position>0){
                left_layer_tv.setVisibility(View.VISIBLE);
            }else{
                left_layer_tv.setVisibility(View.INVISIBLE);
            }
            if(position>=list.size()-3){
                right_layer_tv.setVisibility(View.INVISIBLE);
            }else{
                right_layer_tv.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showPayLayer=false;
        if(btn_buy_focused){
            subject_btn_buy.requestFocus();
        }else if(btn_like_focused) {
            subject_btn_like.requestFocus();
        }else {
            if (type.contains("movie")) {
                movie_recyclerView.requestFocus();
            } else {
                tv_recyclerView.requestFocus();
            }
        }
        if(isFavorite()){
            subject_btn_like.setBackgroundResource(R.drawable.liked_btn_selector);
        }else{
            subject_btn_like.setBackgroundResource(R.drawable.like_btn_selector);
        }
        if(mSubjectEntity.getTitle()!=null)
        video_gather_in(mSubjectEntity.getTitle(),((SubjectActivity)getActivity()).fromPage,channel);
    }

    @Override
    public void onPause() {
        if(subject_btn_buy.isFocused()){
            btn_buy_focused=true;
            btn_like_focused=false;
        }else if(subject_btn_like.isFocused()){
            btn_like_focused=true;
            btn_buy_focused=false;
        }else {
            btn_like_focused=false;
            btn_buy_focused=false;
            if (type.contains("movie")) {
                focusView = movie_recyclerView.getFocusedChild();
            } else {
                focusView = tv_recyclerView.getFocusedChild();
            }
        }
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.subject_btn_like) {
            try {
                if (!isFavorite()) {
                    String url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + id + "/";
                    long time=0;
                    DateFormat format=new SimpleDateFormat("yyyy-MM-dd");
                    format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(TrueTime.now().getTime());
                    String date=format.format(calendar.getTime());
                    try {
                        time= format.parse(date).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Favorite favorite = new Favorite();
                    favorite.time=TrueTime.now().getTime();
                    favorite.title = mSubjectEntity.getTitle();
                    String adlet_url = mSubjectEntity.getAdlet_url();
                    if (adlet_url != null) {
                        if (adlet_url.equals("http://res.tvxio.bestv.com.cn/media/upload/20160321/36c8886fd5b4163ae48534a72ec3a555.png") ||
                                adlet_url.equals("http://res.tvxio.bestv.com.cn/media/upload/20160504/5eae6db53f065ff0269dfc71fb28a4ec.png")) {
                            adlet_url = null;
                        }
                    }
                    if (mSubjectEntity.getObjects().get(0) != null) {
                        favorite.adlet_url = (adlet_url == null || "".equals(adlet_url)) ? mSubjectEntity.getObjects().get(0).getAdlet_url() : adlet_url;
                    }
                    favorite.content_model = type;
                    favorite.url = url;
                    favorite.quality = 0;
                    favorite.is_complex = true;
                    favorite.isnet = isnet;
                    ArrayList<Favorite> favorites=new ArrayList<>();
                    if ("yes".equals(isnet)) {
                        createFavoriteByNet(id);
                        favorites=DaisyUtils.getFavoriteManager(getActivity()).getAllFavorites();
                        Collections.sort(favorites);
                        if (favorites.size() > 99) {
                            mFavoriteManager.deleteFavorite(favorites.get(favorites.size() - 1).url, isnet);
                        }
                    }else {
                        favorites = DaisyUtils.getFavoriteManager(getActivity()).getAllFavorites(isnet);
                        Collections.sort(favorites);
                        if (favorites.size() > 49) {
                            mFavoriteManager.deleteFavoriteByUrl(favorites.get(favorites.size() - 1).url, isnet);
                        }
                    }
                    mFavoriteManager.addFavorite(favorite, isnet);
                    subject_btn_like.setBackgroundResource(R.drawable.liked_btn_selector);
                    showToast("收藏成功");
                } else {
                    String url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + id + "/";
                    deleteFavoriteByNet(id);
                    mFavoriteManager.deleteFavoriteByUrl(url, "yes");
                    mFavoriteManager.deleteFavoriteByUrl(url, "no");
                    subject_btn_like.setBackgroundResource(R.drawable.like_btn_selector);
                    showToast("取消收藏成功");

                }
            }catch (Exception e){
                ExceptionUtils.sendProgramError(e);
                e.printStackTrace();
            }
        }else if (i == R.id.subject_btn_buy) {
            if (clickble) {
                buySubject();
                clickble = false;
            }
        }

    }

    //购买专题页
    private void buySubject() {
        //判断用户是否有最高的观影权限
        ((SubjectActivity)getActivity()).mSkyService.apiPaylayerVipSubject(id+"")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((SubjectActivity)getActivity()).new BaseObserver<SubjectPayLayerEntity>(){


                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(SubjectPayLayerEntity subjectPayLayerEntity) {
                        clickble=true;
                        if(subjectPayLayerEntity.gather_per){
                            showToast("您已拥有本专题所有影片观看权限");
                        }else{
                            if(!showPayLayer) {
                                showPayLayer = true;
                                AppConstant.purchase_entrance_page = "gather";
                                PageIntentInterface.PaymentInfo paymentInfo = new PageIntentInterface.PaymentInfo(item, subjectPayLayerEntity.getPk(), PageIntent.PAYVIP, subjectPayLayerEntity.getCpid(), mSubjectEntity.getTitle());
                                String userName = IsmartvActivator.getInstance().getUsername();
                                String title = mSubjectEntity.getTitle();
                                new PurchaseStatistics().expenseVideoClick(String.valueOf(id), userName, title, String.valueOf(id));
                                new PageIntent().toPaymentForResult(getActivity(), Source.GATHER.getValue(), paymentInfo);
                                video_gather_out(mSubjectEntity.getTitle(), "expense", "", "");
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        clickble=true;
                        super.onError(e);
                    }
                });

    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i("MovieClick","onItemClick");
            focusView=view;
            PageIntent intent = new PageIntent();
            intent.toPlayPage(getActivity(), list.get(position).getPk(), 0, Source.GATHER);
            video_gather_out(mSubjectEntity.getTitle(),"player",mSubjectEntity.getObjects().get(position).getPk()+"",mSubjectEntity.getObjects().get(position).getTitle());
    }

    private boolean isFavorite() {
            String url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + id + "/";
            Favorite favorite= mFavoriteManager.getFavoriteByUrl(url, "yes");
            Favorite local=mFavoriteManager.getFavoriteByUrl(url, "no");
            if(IsmartvActivator.getInstance().isLogin()) {
                if (favorite != null || local != null) {
                    return true;
                } else {
                    return false;
                }
            }else{
                if(local==null){
                    return false;
                }else{
                    return true;
                }
            }
    }

    private void createFavoriteByNet(int pk) {
        ((SubjectActivity)getActivity()).mSkyService.apiBookmarksCreate(pk+"")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((SubjectActivity)getActivity()).new BaseObserver<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        subject_btn_like.setBackgroundResource(R.drawable.liked_btn_selector);
                        showToast("收藏成功");                    }
                });
    }

    private void deleteFavoriteByNet(int pk) {

        ((SubjectActivity)getActivity()).mSkyService.apiBookmarksRemove(pk+"")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        subject_btn_like.setBackgroundResource(R.drawable.like_btn_selector);
                        showToast("取消收藏成功");
                    }
                });
    }

    private void showToast(String text) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast, (ViewGroup) getActivity().findViewById(R.id.simple_toast_root));
        TextView toastText = (TextView) layout.findViewById(R.id.toast_text);
        toastText.setText(text);
        Toast toast = new Toast(getActivity());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus&&focusView!=null){
            focusView.requestFocus();
        }
    }

    /**
     * 进入专题页日志
     */
    public static void video_gather_in(String title,String from,String channel){
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.FROM, from);
        if(channel!=null&&!"".equals(channel)) {
            tempMap.put(EventProperty.CHANNEL, channel);
        }
        String eventName = NetworkUtils.VIDEO_GATHER_IN;
        HashMap<String, Object> properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    /**
     * 退出专题页日志
     */
    public static void video_gather_out(String title,String to,String to_item,String to_title){
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.TO, to);
        if(to_item!=null&&!"".equals(to_item)) {
            tempMap.put(EventProperty.TO_ITEM, to_item);
        }
        if(to_title!=null&&!"".equals(to_title)) {
            tempMap.put(EventProperty.TO_TITLE, to_title);
        }
        String eventName = NetworkUtils.VIDEO_GATHER_OUT;
        HashMap<String, Object> properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        video_gather_out(mSubjectEntity.getTitle(),"exit","","");
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocus();
                v.requestFocusFromTouch();
                break;
            default:
                break;
        }
        return false;
    }
}
