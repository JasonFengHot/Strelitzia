package tv.ismar.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
//import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.FilterNoresultPoster;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.listpage.R;

/**
 * Created by admin on 2017/8/28.
 */

public class PosterUtil {

    public static final int VERTICAL=0;
    public static final int HORIZONTAL=1;
    public static void fillPoster(Activity context, int orientation, FilterNoresultPoster item, RecyclerImageView poster, RecyclerImageView vipmark, TextView beanscore, TextView title, TextView description){
        if(item!=null) {
            String posterUrl="";
            int previewId;
            if(orientation==VERTICAL){
                posterUrl=item.getVertical_url();
                previewId=R.drawable.list_item_ppreview_bg;
            }else{
                posterUrl=item.getPoster_url();
                previewId=R.drawable.list_item_preview_bg;
            }
            if (!TextUtils.isEmpty(posterUrl) && posterUrl != null)
                Picasso.with(context).load(posterUrl).error(previewId).placeholder(previewId).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).config(Bitmap.Config.RGB_565).
                        into(poster);
            if (item.isExpense()) {
                Picasso.with(context).load(VipMark.getInstance().getImage(context, item.getExpense_info().getPay_type(), item.getExpense_info().getCpid())).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).into(vipmark);
            }
            if (item.getBean_score() > 0) {
                beanscore.setText(item.getBean_score() + "");
                beanscore.setVisibility(View.VISIBLE);
            } else {
                beanscore.setVisibility(View.INVISIBLE);
            }
            if (!TextUtils.isEmpty(item.getTitle())) {
                title.setText(item.getTitle());
            }
            if(item.getIntroduction()!=null&&description!=null){
                description.setText(item.getIntroduction());
            }
        }
    }

}
