package tv.ismar.homepage.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;

import tv.ismar.app.entity.banner.BannerPoster;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/31
 * @DESC: 竖版双行适配器
 */

public class DoubleMdAdapter extends BaseAdapter{

    private Context mContext;
    private List<BannerPoster> mData;

    public DoubleMdAdapter(Context context, List<BannerPoster> data){
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public int getCount() {
        return (mData!=null) ? mData.size():0;
    }

    @Override
    public Object getItem(int position) {
        return (mData!=null)? mData.get(position) : position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
