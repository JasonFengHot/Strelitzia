package tv.ismar.subject.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.entity.Item;
import tv.ismar.app.models.SubjectEntity;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.subject.R;
import tv.ismar.subject.SubjectActivity;
import tv.ismar.subject.adapter.OnItemClickListener;
import tv.ismar.subject.adapter.OnItemFocusedListener;
import tv.ismar.subject.adapter.SubjectMovieAdapter;
import tv.ismar.subject.adapter.SubjectTvAdapter;

/**
 * Created by admin on 2017/3/2.
 */

public class MovieTVSubjectFragment extends Fragment implements View.OnFocusChangeListener, View.OnClickListener, OnItemClickListener {

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
    private boolean isFocused=true;
    private ArrayList<Item> list;
    private SubjectTvAdapter tvAdapter;
    private String type;
    private View subject_movie;
    private View subject_tv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content=inflater.inflate(R.layout.movie_tv_subject_fragment,null);
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
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        type = ((SubjectActivity)getActivity()).gather_type;
        if(type.contains("movie")){
            //电影专题
            subject_movie.setVisibility(View.VISIBLE);
            getView().setBackgroundResource(R.drawable.bg);
            movieAdapter = new SubjectMovieAdapter(getActivity(), list);
            movie_recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL));
            movie_recyclerView.setAdapter(movieAdapter);
            movieAdapter.setOnItemClickListener(this);
            movieAdapter.setOnItemFocusedListener(new OnItemFocusedListener() {
                @Override
                public void onItemfocused(View view, int position, boolean hasFocus) {
                    if(hasFocus) {
                        checkLayerIsShow(position);
                        poster_focus.setVisibility(View.VISIBLE);
                        if(!isFocused){
                            isFocused=true;
                            movie_recyclerView.getChildAt(1).requestFocus();
                            movie_recyclerView.smoothScrollBy(-1,0);
                            return;
                        }
                        JasmineUtil.scaleOut2(view);
                        movie_recyclerView.smoothScrollBy((int) (view.getX() - 1), 0);
                        subject_actor.setText(list.get(position).title+position);
                        subject_description.setText(list.get(position).title+position);
                    }else{
                        JasmineUtil.scaleIn2(view);
                        poster_focus.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }else{
            //电视剧专题
            subject_tv.setVisibility(View.VISIBLE);
            tvAdapter = new SubjectTvAdapter(getActivity(),list);
            tv_recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL));
            tv_recyclerView.setAdapter(tvAdapter);
            tvAdapter.setOnItemClickListener(this);
            tvAdapter.setOnItemFocusedListener(new OnItemFocusedListener() {
                @Override
                public void onItemfocused(View view, int position, boolean hasFocus) {
                    if(hasFocus) {
                        checkLayerIsShow(position);
                        if(!isFocused){
                            isFocused=true;
                            tv_recyclerView.getChildAt(1).requestFocus();
                            tv_recyclerView.smoothScrollBy(-1,0);
                            return;
                        }
                        JasmineUtil.scaleOut2(view);
                        tv_recyclerView.smoothScrollBy((int) (view.getX() - 1), 0);
                        Log.e("position",view.getX()+"");
                        subject_actor.setText(list.get(position).title+position);
                        subject_description.setText(list.get(position).title+position);
                    }else{
                        JasmineUtil.scaleIn2(view);
                        poster_focus.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }

        subject_btn_buy.setOnFocusChangeListener(this);
        subject_btn_like.setOnFocusChangeListener(this);
        subject_btn_buy.setOnClickListener(this);
        subject_btn_like.setOnClickListener(this);

    }

    private void initData() {
        list = new ArrayList<>();
        Item item;
        for (int i = 0; i <20 ; i++) {
            item=new Item();
            item.bean_score=5.3f;
            item.poster_url="http://res.tvxio.bestv.com.cn/media/upload/20140922/2016hyhsd160119shuban_list.jpg";
            item.title="三打白骨精";
            list.add(item);
        }
        ((SubjectActivity)getActivity()).mSkyService.apiFetchSubject(type,((SubjectActivity)getActivity()).itemid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((SubjectActivity)getActivity()).new BaseObserver<SubjectEntity>(){

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(SubjectEntity subjectEntity) {

                    }
                });
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
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                isFocused=false;
                if(type.contains("movie")) {
                    JasmineUtil.scaleOut2(movie_recyclerView.getChildAt(1));
                }else{
                    JasmineUtil.scaleOut2(tv_recyclerView.getChildAt(1));
                }

            }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.subject_btn_like) {
            subject_btn_like.setBackgroundResource(R.drawable.liked_btn_selector);
        } else if (i == R.id.subject_btn_buy) {
            subject_btn_buy.setEnabled(false);
            subject_btn_buy.setFocusable(false);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        PageIntent intent=new PageIntent();
        intent.toPlayPage(getActivity(),421263,421263, Source.LAUNCHER);
    }
}
