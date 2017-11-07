package tv.ismar.subject.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
//import android.widget.ImageView;
import android.widget.TextView;

import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.subject.R;

/**
 * Created by admin on 2017/3/2.
 */
public class MovieViewHolder extends RecyclerView.ViewHolder{

    RecyclerImageView movie_item_poster;
    TextView movie_item_score;
    RecyclerImageView movie_item_mark;
    TextView movie_item_title;
    public MovieViewHolder(View itemView) {
        super(itemView);
        movie_item_poster= (RecyclerImageView) itemView.findViewById(R.id.movie_item_poster);
        movie_item_score= (TextView) itemView.findViewById(R.id.movie_item_score);
        movie_item_mark= (RecyclerImageView) itemView.findViewById(R.id.movie_item_mark);
        movie_item_title= (TextView) itemView.findViewById(R.id.movie_item_title);
    }
}
