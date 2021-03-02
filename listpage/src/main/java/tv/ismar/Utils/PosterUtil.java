package tv.ismar.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
//import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tv.ismar.adapter.SpecialPos;
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
/*modify by dragontec for  start*/
//                posterUrl=item.getVertical_url();
                posterUrl=item.getList_url();
/*modify by dragontec for  end*/
/*modify by dragontec for bug 4336 start*/
                previewId=R.drawable.item_vertical_preview;
/*modify by dragontec for bug 4336 end*/
            }else{
                posterUrl=item.getPoster_url();
/*modify by dragontec for bug 4336 start*/
                previewId=R.drawable.item_horizontal_preview;
/*modify by dragontec for bug 4336 end*/
            }
            if (!TextUtils.isEmpty(posterUrl))
                Picasso.with(context).load(posterUrl).error(previewId).placeholder(previewId).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).config(Bitmap.Config.RGB_565).
                        into(poster);
            if (item.isExpense()) {
                Picasso.with(context).load(VipMark.getInstance().getImage(context, item.getExpense_info().getPay_type(), item.getExpense_info().getCpid())).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).into(vipmark);
            }
            if (item.getBean_score() != null && Float.parseFloat(item.getBean_score()) > 0) {
                beanscore.setText(Float.parseFloat(item.getBean_score()) + "");
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

    public static int computeSectionSpanSize(ArrayList<SpecialPos> specialPos, int position, int defaultSpanCount){
        int index = specialPos.indexOf(new SpecialPos(position + 1));
        if(index > -1) {
            int pastSectionCount =specialPos.get(index-1).startPosition;
            pastSectionCount = pastSectionCount <0 ? 0 :pastSectionCount;
            int sectionCount = position - pastSectionCount + 1;
            int leftItem = sectionCount % defaultSpanCount;
            int span = (defaultSpanCount - leftItem) % defaultSpanCount + 1;
            Log.i("zzz","zzz position:"+ position+"span:" + span);
            return span;
        }else{
            return 1;
        }
    }

    public static int computePositionInLine(ArrayList<SpecialPos> specialPos, int position, int defaultSpanCount){
        int index = -1;
        for (int i = 0; i < specialPos.size(); i++) {
            if(position < specialPos.get(i).endPosition){
                index = i;
                break;
            }
        }
        if(index > -1) {
            int pastSectionCount = 0;
            if(index > 0){
                pastSectionCount =specialPos.get(index-1).endPosition;
            }
            pastSectionCount = pastSectionCount <0 ? 0 :pastSectionCount;
            int sectionCount = position - pastSectionCount;
            int positionInLine = sectionCount % defaultSpanCount;
            return positionInLine;
        }else{
            return -1;
        }
    }
}
