package tv.ismar.homepage.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import tv.ismar.app.entity.GuideBanner;
import tv.ismar.homepage.R;
import tv.ismar.homepage.widget.scroll.AbsBannerAdapter;
import tv.ismar.homepage.widget.scroll.listener.OnUseViewChange;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/28
 * @DESC: 说明
 */

public class GuideAdapter extends AbsBannerAdapter {
    private GuideBanner[] mData;
    private Context mContext;
    private OnUseViewChange mCallback;

    public GuideAdapter(Context context, GuideBanner[] data) {
        this.mData = data;
        this.mContext = context;
    }

    public void setCallback(OnUseViewChange callback){
        this.mCallback = callback;
    }

    @Override
    public int getCount() {
        return (mData!=null) ? mData.length : 0;
    }

    @Override
    public Object getItem(int position) {
        return (mData!=null&&mData.length>0) ? mData[position] : 0;
    }

    @Override
    public View getView(int position) {
        if(mData[position].template.equals("column")){//栏目布局
            //返回首页的view
            View view = createView(mContext, R.layout.guide_column_layout);
            if(mCallback != null){
                mCallback.useViewChange(view, position);
            }
            return view;
        }
        return null;
    }

    private View createView(Context context, int layoutId){
        return LayoutInflater.from(context).inflate(layoutId, null);
    }
}
