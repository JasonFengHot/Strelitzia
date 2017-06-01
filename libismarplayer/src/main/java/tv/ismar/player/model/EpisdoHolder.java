package tv.ismar.player.model;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiyi.tvapi.tv2.model.Episode;

import tv.ismar.player.R;

/**
 * Created by liucan on 2017/5/31.
 */

public class EpisdoHolder {
    public TextView textView;
    public ImageView imageView;
    public EpisdoHolder(View itemView){
        textView= (TextView) itemView.findViewById(R.id.episode_pk);
        imageView= (ImageView) itemView.findViewById(R.id.select_image);
    }
}
