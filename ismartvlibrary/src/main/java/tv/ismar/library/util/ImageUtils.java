package tv.ismar.library.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

/**
 * Created by LongHai on 17-4-24.
 */

public class ImageUtils {

    public static void loadImage(Context context, int resId, ImageView imageView){
        Picasso.with(context).load(resId).centerCrop().into(imageView);
    }

    public static void loadImage(Context context, int resId, ImageView imageView, Callback callback){
        Picasso.with(context).load(resId).centerCrop().into(imageView, callback);
    }

    /**
     * 指定大小加载图片
     *
     * @param context   上下文
     * @param path      图片路径
     * @param width     宽
     * @param height    高
     * @param imageView 控件
     */
    public static void loadImageViewSize(Context context, String path, int width, int height, ImageView imageView) {
        Picasso.with(context).load(path).resize(width, height).centerCrop().into(imageView);
    }


    /**
     * 加载有默认图片
     *
     * @param context   上下文
     * @param path      图片路径
     * @param resId     默认图片资源
     * @param imageView 控件
     */
    public static void loadImageViewHolder(Context context, String path, int resId, ImageView imageView) {
        Picasso.with(context).load(path).fit().placeholder(resId).into(imageView);
    }


    /**
     * 裁剪图片
     *
     * @param context   上下文
     * @param path      图片路径
     * @param imageView 控件
     */
    public static void loadImageViewCrop(Context context, String path, ImageView imageView) {
        Picasso.with(context).load(path).transform(new CropImageView()).into(imageView);
    }

    /**
     * 自定义图片裁剪
     */
    public static class CropImageView implements Transformation {

        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;
            Bitmap newBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (newBitmap != null) {
                //内存回收
                source.recycle();
            }
            return newBitmap;
        }

        @Override
        public String key() {
            return "";
        }
    }
}