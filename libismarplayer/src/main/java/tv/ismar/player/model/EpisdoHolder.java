package tv.ismar.player.model;

import android.view.View;
//import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.player.R;

/**
 * Created by liucan on 2017/5/31.
 */

public class EpisdoHolder {
    public TextView textView;
    public RecyclerImageView imageView;
    public LinearLayout list_item;
    public TextView subitem;
    public EpisdoHolder(View itemView){
        textView= (TextView) itemView.findViewById(R.id.episode_pk);
        imageView= (RecyclerImageView) itemView.findViewById(R.id.select_image);
        list_item= (LinearLayout) itemView.findViewById(R.id.episode_list_item);
        subitem= (TextView) itemView.findViewById(R.id.episode_subItem_title);
    }
}
