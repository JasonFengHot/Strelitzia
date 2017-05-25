package tv.ismar.player.gui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tv.ismar.player.R;

/**
 * Created by liucan on 2017/5/24.
 */

public class EpisodeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView play_select;
        public EpisodeViewHolder(View itemView) {
            super(itemView);
            title= (TextView) itemView.findViewById(R.id.episode_pk);
            play_select= (ImageView) itemView.findViewById(R.id.select_image);
        }
}
