package tv.ismar.homepage.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import tv.ismar.app.entity.GuideBanner;
import tv.ismar.homepage.R;
import tv.ismar.homepage.template.Template1;
import tv.ismar.homepage.template.Template2;
import tv.ismar.homepage.template.Template3;

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

            viewHolder.mTemplate1 = convertView.findViewById(R.id.template1);
            viewHolder.mTemplate2 = convertView.findViewById(R.id.template2);
            viewHolder.mTemplate3 = convertView.findViewById(R.id.template3);
            viewHolder.mTemplate4 = convertView.findViewById(R.id.template4);
            viewHolder.mTemplate5 = convertView.findViewById(R.id.template5);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mTemplate1.setVisibility(View.GONE);
        viewHolder.mTemplate2.setVisibility(View.GONE);
        viewHolder.mTemplate3.setVisibility(View.GONE);
        viewHolder.mTemplate4.setVisibility(View.GONE);
        viewHolder.mTemplate5.setVisibility(View.GONE);

        //TODO 根据数据标记来确定用那个模版，置为可见即可
        Bundle bundle = new Bundle();
        bundle.putString("title", mData[position].title);
        bundle.putString("url", mData[position].banner_url);
        String template = mData[position].template;
        if(template.equals("template1")){//模版1
            viewHolder.mTemplate1.setVisibility(View.VISIBLE);
            new Template1().setView(viewHolder.mTemplate1, bundle);
        } else if(template.equals("template2")){
            viewHolder.mTemplate2.setVisibility(View.VISIBLE);
            new Template2().setView(viewHolder.mTemplate2, bundle);
        } else if(template.equals("template3")){
            viewHolder.mTemplate3.setVisibility(View.VISIBLE);
            new Template3().setView(viewHolder.mTemplate3, bundle);
        }

        return convertView;
    }

    class ViewHolder {
        View mTemplate1;//模版1
        View mTemplate2;//模版2
        View mTemplate3;//模版3
        View mTemplate4;//模版4
        View mTemplate5;//模版5
    }
}
