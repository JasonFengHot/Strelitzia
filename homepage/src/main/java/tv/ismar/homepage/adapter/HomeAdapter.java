package tv.ismar.homepage.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import tv.ismar.app.entity.GuideBanner;
import tv.ismar.homepage.R;
import tv.ismar.homepage.template.Template519;
import tv.ismar.homepage.template.TemplateBigSmallLd;
import tv.ismar.homepage.template.TemplateConlumn;
import tv.ismar.homepage.template.TemplateDoubleLd;
import tv.ismar.homepage.template.TemplateDoubleMd;
import tv.ismar.homepage.template.TemplateGuide;
import tv.ismar.homepage.template.TemplateOrder;
import tv.ismar.homepage.template.TemplateMovie;
import tv.ismar.homepage.template.TemplateTvPlay;

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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.guide_fragment_item_layout, null);

            viewHolder.mTemplateGuide = convertView.findViewById(R.id.banner_guide);
            viewHolder.mTemplateOrder = convertView.findViewById(R.id.banner_order);
            viewHolder.mTemplateMovie = convertView.findViewById(R.id.banner_movie);
            viewHolder.mTemplateTvPlay = convertView.findViewById(R.id.banner_tv_play);
            viewHolder.mTemplate519 = convertView.findViewById(R.id.banner_519);
            viewHolder.mTemplateConlumn = convertView.findViewById(R.id.banner_conlumn);
            viewHolder.mTemplateBigSmallLd = convertView.findViewById(R.id.banner_big_small_ld);
            viewHolder.mTemplateDoubleMd = convertView.findViewById(R.id.banner_double_md);
            viewHolder.mTemplateDoubleLd = convertView.findViewById(R.id.banner_double_ld);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mTemplateGuide.setVisibility(View.GONE);
        viewHolder.mTemplateOrder.setVisibility(View.GONE);
        viewHolder.mTemplateMovie.setVisibility(View.GONE);
        viewHolder.mTemplateTvPlay.setVisibility(View.GONE);
        viewHolder.mTemplate519.setVisibility(View.GONE);
        viewHolder.mTemplateConlumn.setVisibility(View.GONE);
        viewHolder.mTemplateBigSmallLd.setVisibility(View.GONE);
        viewHolder.mTemplateDoubleMd.setVisibility(View.GONE);
        viewHolder.mTemplateDoubleLd.setVisibility(View.GONE);


        //TODO 根据数据标记来确定用那个模版，置为可见即可
        Bundle bundle = new Bundle();
        bundle.putString("title", mData[position].title);
        bundle.putString("url", mData[position].banner_url);
        String template = mData[position].template;
        if(template.equals("template_guide")){//导航
            viewHolder.mTemplateGuide.setVisibility(View.VISIBLE);
            new TemplateGuide().setView(viewHolder.mTemplateGuide, bundle);
        } else if(template.equals("template_order")){//订阅模版
            viewHolder.mTemplateOrder.setVisibility(View.VISIBLE);
            new TemplateOrder().setView(viewHolder.mTemplateOrder, bundle);
        } else if(template.equals("template_movie")){//电影模版
            viewHolder.mTemplateMovie.setVisibility(View.VISIBLE);
            new TemplateMovie().setView(viewHolder.mTemplateMovie, bundle);
        } else if(template.equals("template_teleplay")){//电视剧模版
            viewHolder.mTemplateTvPlay.setVisibility(View.VISIBLE);
            new TemplateTvPlay().setView(viewHolder.mTemplateMovie, bundle);
        } else if(template.equals("template_519")){//519横图模版
            viewHolder.mTemplate519.setVisibility(View.VISIBLE);
            new Template519().setView(viewHolder.mTemplateMovie, bundle);
        }else if(template.equals("template_conlumn")){//栏目模版
            viewHolder.mTemplateConlumn.setVisibility(View.VISIBLE);//栏目
            new TemplateConlumn().setView(viewHolder.mTemplateMovie, bundle);
        }else if(template.equals("template_big_small_ld")){//大横小竖模版
            viewHolder.mTemplateBigSmallLd.setVisibility(View.VISIBLE);
            new TemplateBigSmallLd().setView(viewHolder.mTemplateMovie, bundle);
        }else if(template.equals("template_double_md")){//竖版双行模版
            viewHolder.mTemplateDoubleMd.setVisibility(View.VISIBLE);
            new TemplateDoubleMd().setView(viewHolder.mTemplateMovie, bundle);
        }else if(template.equals("template_double_ld")){//横版双行模版
            viewHolder.mTemplateDoubleLd.setVisibility(View.VISIBLE);
            new TemplateDoubleLd().setView(viewHolder.mTemplateMovie, bundle);
        }

        return convertView;
    }

    class ViewHolder {
        View mTemplateGuide;//导视模版
        View mTemplateOrder;//订阅模版
        View mTemplateMovie;//电影模版
        View mTemplateTvPlay;//电视剧模版
        View mTemplate519;//519模版
        View mTemplateConlumn;//栏目模版
        View mTemplateBigSmallLd;//大横小竖模版
        View mTemplateDoubleMd;//竖版双行模版
        View mTemplateDoubleLd;//横版双行模版
    }
}
