package tv.ismar.subject.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tv.ismar.searchpage.weight.RotateTextView;
import tv.ismar.subject.R;

/**
 * Created by admin on 2017/3/2.
 */
public class MovieViewHolder extends RecyclerView.ViewHolder{

    ImageView movie_item_poster;
    TextView movie_item_score;
    ImageView movie_item_mark;
    TextView movie_item_title;
    public MovieViewHolder(View itemView) {
        super(itemView);
        movie_item_poster= (ImageView) itemView.findViewById(R.id.movie_item_poster);
        movie_item_score= (TextView) itemView.findViewById(R.id.movie_item_score);
        movie_item_mark= (ImageView) itemView.findViewById(R.id.movie_item_mark);
        movie_item_title= (TextView) itemView.findViewById(R.id.movie_item_title);
    }
}
