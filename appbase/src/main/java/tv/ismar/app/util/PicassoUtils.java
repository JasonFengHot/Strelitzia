package tv.ismar.app.util;

import android.content.Context;
import android.text.TextUtils;
//import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import tv.ismar.app.R;
import tv.ismar.app.widget.RecyclerImageView;

/**
 * Created by huaijie on 11/2/15.
 */
public class PicassoUtils {
    public static void load(final Context context, String path, final RecyclerImageView target) {
        if (TextUtils.isEmpty(path)) {
/*modify by dragontec for bug 4336 start*/
            Picasso.with(context).load(R.drawable.item_horizontal_preview).memoryPolicy(MemoryPolicy.NO_STORE).into(target);
/*modify by dragontec for bug 4336 end*/
        } else {
/*modify by dragontec for bug 4336 start*/
            Picasso.with(context).load(path).error(R.drawable.item_horizontal_preview).placeholder(R.drawable.item_horizontal_preview).memoryPolicy(MemoryPolicy.NO_STORE).into(target);
/*modify by dragontec for bug 4336 end*/
        }

    }

}
