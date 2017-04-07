package tv.ismar.subject.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.db.FavoriteManager;
import tv.ismar.app.entity.Favorite;
import tv.ismar.app.models.SubjectEntity;
import tv.ismar.app.network.entity.PayLayerVipEntity;
import tv.ismar.app.network.entity.SubjectPayLayerEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.statistics.PurchaseStatistics;
import tv.ismar.subject.R;
import tv.ismar.subject.SubjectActivity;
import tv.ismar.subject.adapter.OnItemClickListener;
import tv.ismar.subject.adapter.OnItemFocusedListener;
import tv.ismar.subject.adapter.SubjectMovieAdapter;
import tv.ismar.subject.adapter.SubjectTvAdapter;

import static tv.ismar.app.core.PageIntentInterface.FromPage.unknown;
import static tv.ismar.app.core.PageIntentInterface.ProductCategory.item;

/**
 * Created by admin on 2017/3/2.
 */

public class MovieTVSubjectFragment extends Fragment implements View.OnClickListener, OnItemClickListener {

    private RecyclerView movie_recyclerView;
    private RecyclerView tv_recyclerView;
    private TextView subject_actor;
    private TextView subject_description;
    private Button subject_btn_buy;
    private Button subject_btn_like;
    private SubjectMovieAdapter movieAdapter;
    private ImageView left_layer_movie;
    private ImageView right_layer_movie;
    private ImageView left_layer_tv;
    private ImageView right_layer_tv;
    private ImageView tv_poster_focus;
    private ImageView poster_focus;
    private List<SubjectEntity.ObjectsBean> list;
    private SubjectTvAdapter tvAdapter;
    private String type;
    private int id;
    private View subject_movie;
    private View subject_tv;
    private String isnet="no";
    final SimpleRestClient simpleRest = new SimpleRestClient();
    private FavoriteManager mFavoriteManager;
    private ImageView subject_bg;
    private View focusView;
    private SubjectEntity mSubjectEntity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.movie_tv_subject_fragment,null);
        initView(content);
        return content;
    }

    private void initView(View content) {
        subject_movie = content.findViewById(R.id.subject_movie);
        movie_recyclerView = (RecyclerView) content.findViewById(R.id.movie_recyclerView);
        subject_tv = content.findViewById(R.id.subject_tv);
        tv_recyclerView = (RecyclerView) content.findViewById(R.id.tv_recyclerView);
        subject_actor = (TextView) content.findViewById(R.id.subject_actor);
        subject_description = (TextView) content.findViewById(R.id.subject_description);
        subject_btn_buy = (Button) content.findViewById(R.id.subject_btn_buy);
        subject_btn_like = (Button) content.findViewById(R.id.subject_btn_like);
        left_layer_movie = (ImageView) content.findViewById(R.id.left_layer_movie);
        right_layer_movie = (ImageView) content.findViewById(R.id.right_layer_movie);
        poster_focus = (ImageView) content.findViewById(R.id.poster_focus);
        left_layer_tv= (ImageView) content.findViewById(R.id.left_layer_tv);
        right_layer_tv = (ImageView) content.findViewById(R.id.right_layer_tv);
        tv_poster_focus= (ImageView) content.findViewById(R.id.tv_poster_focus);
        subject_bg = (ImageView) content.findViewById(R.id.subject_bg);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        subject_btn_buy.setOnClickListener(this);
        subject_btn_like.setOnClickListener(this);
        movie_recyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus&&focusView!=null){
                      focusView.requestFocus();
                }
            }
        });
        tv_recyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus&&focusView!=null){
                    focusView.requestFocus();
                }
            }
        });
    }

    private void initData() {
        type = ((SubjectActivity)getActivity()).gather_type;
        id = ((SubjectActivity)getActivity()).itemid;
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
                        processData(subjectEntity);
                    }

                    @Override
                    public void onError(Throwable e) {
//                        hideLoading();
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
            movieAdapter.setOnItemFocusedListener(new OnItemFocusedListener() {


                @Override
                public void onItemfocused(View view, int position, boolean hasFocus) {
                    if(hasFocus) {
                        checkLayerIsShow(position);
                        poster_focus.setVisibility(View.VISIBLE);
                        movie_recyclerView.smoothScrollBy((int) (view.getX()-getResources().getDimensionPixelOffset(R.dimen.subject_movie_recycleview_ml)), 0);
                        JasmineUtil.scaleOut2(view);
                        subject_actor.setText(list.get(position).getMsg1());
                        subject_description.setText(list.get(position).getMsg2());
                    }else{
                        if(subject_btn_like.isFocused()||subject_btn_buy.isFocused()){
                            focusView = view;
                        }else{
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
                        }else{
                            JasmineUtil.scaleIn2(view);
                        }
                        tv_poster_focus.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
//        hideLoading();
    }

    private void hideLoading() {
        if(((SubjectActivity)getActivity()).mLoadingDialog!=null&&((SubjectActivity)getActivity()).mLoadingDialog.isShowing()){
            ((SubjectActivity)getActivity()).mLoadingDialog.dismiss();
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
        if(isFavorite()){
            subject_btn_like.setBackgroundResource(R.drawable.liked_btn_selector);
        }else{
            subject_btn_like.setBackgroundResource(R.drawable.like_btn_selector);
        }
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.subject_btn_like) {
            if(!isFavorite()){
                String url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + id+ "/";
                Favorite favorite = new Favorite();
                favorite.title = mSubjectEntity.getTitle();
                favorite.adlet_url = mSubjectEntity.getBg_url();
                favorite.content_model = type;
                favorite.url = url;
                favorite.quality = 0;
                favorite.is_complex = true;
                favorite.isnet = isnet;
                if ("yes".equals(isnet)) {
                    createFavoriteByNet();
                }
                ArrayList<Favorite> favorites = DaisyUtils.getFavoriteManager(getActivity().getApplicationContext()).getAllFavorites("no");
                if(favorites.size()>49){
                    mFavoriteManager.deleteFavoriteByUrl(favorites.get(favorites.size()-1).url, "no");
                }
                mFavoriteManager.addFavorite(favorite, isnet);
                subject_btn_like.setBackgroundResource(R.drawable.liked_btn_selector);
                showToast("收藏成功");
            }else{
                String url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + id + "/";
                if (IsmartvActivator.getInstance().isLogin()) {
                    deleteFavoriteByNet();
                    mFavoriteManager.deleteFavoriteByUrl(url, "yes");
                } else {
                    mFavoriteManager.deleteFavoriteByUrl(url, "no");
                }
                subject_btn_like.setBackgroundResource(R.drawable.like_btn_selector);
                showToast("取消收藏成功");
            }
        } else if (i == R.id.subject_btn_buy) {

                buySubject();
        }
    }

    //购买专题页
    private void buySubject() {
        final int jumpTo =PageIntent.PAYVIP;
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
                        if(subjectPayLayerEntity.gather_per){
                            showToast("您已拥有本专题所有影片观看权限");
                        }else{
                            PageIntentInterface.PaymentInfo paymentInfo = new PageIntentInterface.PaymentInfo(item, subjectPayLayerEntity.getPk(), jumpTo, subjectPayLayerEntity.getCpid());
                            String userName = IsmartvActivator.getInstance().getUsername();
                            String title = mSubjectEntity.getTitle();

                            String clip = "";
                            new PurchaseStatistics().videoExpenseClick(String.valueOf(id), userName, title, clip);
                            new PageIntent().toPayment(getActivity(), unknown.name(), paymentInfo);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                });

    }

    @Override
    public void onItemClick(View view, int position) {
            PageIntent intent = new PageIntent();
            intent.toPlayPage(getActivity(), list.get(position).getPk(), list.get(position).getItem_pk(), Source.LAUNCHER);
    }

    private boolean isFavorite() {

            String url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + id + "/";
            Favorite favorite;
            if (IsmartvActivator.getInstance().isLogin()) {
                favorite = mFavoriteManager.getFavoriteByUrl(url, "yes");
            } else {
                favorite = mFavoriteManager.getFavoriteByUrl(url, "no");
            }
            if (favorite != null) {
                return true;
            }
        return false;
    }

    private void createFavoriteByNet() {
        simpleRest.doSendRequest("/api/bookmarks/create/", "post", "access_token=" + IsmartvActivator.getInstance().getAuthToken() + "&device_token=" + SimpleRestClient.device_token + "&item=" + id, new SimpleRestClient.HttpPostRequestInterface() {

            @Override
            public void onSuccess(String info) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPrepare() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFailed(String error) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void deleteFavoriteByNet() {
        simpleRest.doSendRequest("/api/bookmarks/remove/", "post", "access_token=" +
                IsmartvActivator.getInstance().getAuthToken() + "&device_token=" + SimpleRestClient.device_token + "&item=" + id, new SimpleRestClient.HttpPostRequestInterface() {

            @Override
            public void onSuccess(String info) {
                // TODO Auto-generated method stub
                if ("200".equals(info)) {

                }
            }

            @Override
            public void onPrepare() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFailed(String error) {
                // TODO Auto-generated method stub

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
}
