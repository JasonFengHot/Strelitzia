package tv.ismar.homepage.widget.scroll;

import android.content.Context;
import android.view.LayoutInflater;

/**
 * @AUTHOR: xi
 * @DATE: 2017/7/31
 * @DESC: banner单元
 */

public abstract class BannerCell {

    private Context mContext;

    public BannerCell(Context context){
        this.mContext = context;
    }

    public android.view.View createView(){
        if(mContext != null){
            int layout = this.onInflateLayoutById();
            if(layout != -1){
                LayoutInflater inflater = LayoutInflater.from(mContext);
                return inflater.inflate(layout, null);
            } else {
                return onInflateLayoutByView();
            }
        }
        return null;
    }


    protected int onInflateLayoutById(){
        return -1;
    }

    protected android.view.View onInflateLayoutByView(){
        return new android.view.View(mContext);
    }
}
