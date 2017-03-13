package tv.ismar.subject.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tv.ismar.app.entity.Item;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.subject.R;
import tv.ismar.subject.adapter.SubjectMovieAdapter;
import tv.ismar.subject.view.MyRecyclerView;

/**
 * Created by admin on 2017/3/2.
 */

public class MovieTVSubjectFragment extends Fragment {

    private MyRecyclerView movie_recyclerView;
    private RecyclerView tv_recyclerView;
    private TextView subject_actor;
    private TextView subject_description;
    private Button subject_btn_buy;
    private Button subject_btn_like;
    private SubjectMovieAdapter movieAdapter;
    private ImageView left_layer_movie;
    private ImageView left_layer_movie1;
    private ImageView right_layer_movie;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content=inflater.inflate(R.layout.movie_tv_subject_fragment,null);
        movie_recyclerView = (MyRecyclerView) content.findViewById(R.id.movie_recyclerView);
        tv_recyclerView = (RecyclerView) content.findViewById(R.id.tv_recyclerView);
        subject_actor = (TextView) content.findViewById(R.id.subject_actor);
        subject_description = (TextView) content.findViewById(R.id.subject_description);
        subject_btn_buy = (Button) content.findViewById(R.id.subject_btn_buy);
        subject_btn_like = (Button) content.findViewById(R.id.subject_btn_like);
        left_layer_movie1 = (ImageView) content.findViewById(R.id.left_layer_movie);
        right_layer_movie = (ImageView) content.findViewById(R.id.right_layer_movie);
        return content;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ArrayList<Item> list=new ArrayList<>();
        Item item;
        for (int i = 0; i <20 ; i++) {
            item=new Item();
            item.bean_score=5.3f;
            item.poster_url="http://res.tvxio.bestv.com.cn/media/upload/20140922/2016hyhsd160119shuban_list.jpg";
            item.title="三打白骨精";
            list.add(item);
        }
        movieAdapter = new SubjectMovieAdapter(getActivity(),list);
        movie_recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL));
        movie_recyclerView.setAdapter(movieAdapter);
        movieAdapter.setOnItemClickListener(new SubjectMovieAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        });
        movieAdapter.setOnItemFocusedListener(new SubjectMovieAdapter.OnItemFocusedListener() {
            @Override
            public void onItemfocused(View view, int position, boolean hasFocus) {
                if(hasFocus) {
                    JasmineUtil.scaleOut2(view);
                    movie_recyclerView.smoothScrollBy((int) (view.getX()-1), 0);
                    subject_actor.setText(list.get(position).title+position);
                    subject_description.setText(list.get(position).title+position);
                    if(position==0){
                        movie_recyclerView.setCurrentPosition(0);
                    }else{
                        movie_recyclerView.setCurrentPosition(1);
                    }
                }else{
                    JasmineUtil.scaleIn2(view);
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
