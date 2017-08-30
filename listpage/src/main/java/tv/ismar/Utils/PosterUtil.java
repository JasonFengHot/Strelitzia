package tv.ismar.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.Item;
import tv.ismar.channel.FilterActivity;
import tv.ismar.listpage.R;

/**
 * Created by admin on 2017/8/28.
 */

public class PosterUtil {

    public static final int VERTICAL=0;
    public static final int HORIZONTAL=1;
    public static void fillPoster(Activity context,int orientation, Item item, ImageView poster, ImageView vipmark, TextView beanscore, TextView title, TextView description){
        if(item!=null) {
            String posterUrl="";
            if(orientation==VERTICAL){
                posterUrl=item.list_url;
            }else{
                posterUrl=item.poster_url;
            }
            if (!TextUtils.isEmpty(posterUrl) && posterUrl != null)
                Picasso.with(context).load(posterUrl).error(R.drawable.list_item_ppreview_bg).placeholder(R.drawable.list_item_ppreview_bg).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).config(Bitmap.Config.RGB_565).
                        into(poster);
            if (item.expense != null) {
                Picasso.with(context).load(VipMark.getInstance().getImage(context, item.expense.pay_type, item.expense.cpid)).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).into(vipmark);
            }
            if (item.bean_score > 0) {
                beanscore.setText(item.bean_score + "");
                beanscore.setVisibility(View.VISIBLE);
            } else {
                beanscore.setVisibility(View.INVISIBLE);
            }
            if (!TextUtils.isEmpty(item.title)) {
                title.setText(item.title);
            }
            if(item.focus!=null&&description!=null){
                description.setText(item.focus);
            }
        }
    }

}
