package tv.ismar.homepage.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 频道首页适配器
 */

public class HomeAdapter extends BaseAdapter{
    private GuideBanner[] mData;
    private Context mContext;

    public HomeAdapter(Context context, GuideBanner[] data) {
        this.mData = data;
        this.mContext = context;
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
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_guide, null);

            viewHolder.mHome = convertView.findViewById(R.id.banner_home);
            viewHolder.mColumn = convertView.findViewById(R.id.banner_column);
            viewHolder.mTvPlay = convertView.findViewById(R.id.banner_tv_play);
            viewHolder.mHorizontal2Line = convertView.findViewById(R.id.banner_horizontal_tow_line);
            viewHolder.mVertical2Line = convertView.findViewById(R.id.banner_vertical_tow_line);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mHome.setVisibility(View.GONE);
        viewHolder.mColumn.setVisibility(View.GONE);
        viewHolder.mTvPlay.setVisibility(View.GONE);
        viewHolder.mHorizontal2Line.setVisibility(View.GONE);
        viewHolder.mVertical2Line.setVisibility(View.GONE);

        //TODO 根据数据标记来确定用那个模版，置为可见即可
        if(mData[0].template.equals("home")){//应用首页模版
            viewHolder.mHome.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    class ViewHolder {
        View mHome;//首页轮播banner
        View mColumn;//栏目banner
        View mTvPlay;//电视剧banner
        View mHorizontal2Line;//横向双行banner
        View mVertical2Line;//纵向双行banner
    }
}
