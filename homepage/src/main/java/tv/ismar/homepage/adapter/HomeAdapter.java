package tv.ismar.homepage.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import tv.ismar.app.entity.GuideBanner;
import tv.ismar.homepage.R;
import tv.ismar.homepage.template.Template519;
import tv.ismar.homepage.template.TemplateBigSmallLd;
import tv.ismar.homepage.template.TemplateCenter;
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
        Log.i("getView", "position:"+position);
        ViewHolder viewHolder = null;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.guide_fragment_item_layout, null);

            viewHolder.mTitle = (TextView) convertView.findViewById(R.id.home_item_title);
            viewHolder.mCount = (TextView) convertView.findViewById(R.id.home_item_count);
            viewHolder.mTitleView = convertView.findViewById(R.id.home_title_view);
            viewHolder.mTemplateGuide = convertView.findViewById(R.id.banner_guide);
            viewHolder.mTemplateOrder = convertView.findViewById(R.id.banner_order);
            viewHolder.mTemplateMovie = convertView.findViewById(R.id.banner_movie);
            viewHolder.mTemplateTvPlay = convertView.findViewById(R.id.banner_tv_play);
            viewHolder.mTemplate519 = convertView.findViewById(R.id.banner_519);
            viewHolder.mTemplateConlumn = convertView.findViewById(R.id.banner_conlumn);
            viewHolder.mTemplateBigSmallLd = convertView.findViewById(R.id.banner_big_small_ld);
            viewHolder.mTemplateDoubleMd = convertView.findViewById(R.id.banner_double_md);
            viewHolder.mTemplateDoubleLd = convertView.findViewById(R.id.banner_double_ld);
            viewHolder.mTmeplateCenter = convertView.findViewById(R.id.banner_center);

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
        viewHolder.mTmeplateCenter.setVisibility(View.GONE);

        //TODO 根据数据标记来确定用那个模版，置为可见即可
        Bundle bundle = new Bundle();
        bundle.putString("title", mData[position].title);
        bundle.putString("url", mData[position].banner_url);
        bundle.putInt("banner", mData[position].page_banner_pk);
        String template = mData[position].template;

        viewHolder.mTitleView.setVisibility(View.VISIBLE);
        viewHolder.mTitle.setVisibility(View.VISIBLE);
        viewHolder.mTitle.setText(mData[position].title);
        if(position == 0){
            viewHolder.mTitleView.setVisibility(View.GONE);
        }
        if(template.equals("template_guide")){//导航
            viewHolder.mTemplateGuide.setVisibility(View.VISIBLE);
            new TemplateGuide(mContext).setView(viewHolder.mTemplateGuide, bundle)
                    .setTitleCountView(viewHolder.mCount);
            //TODO 测试代码
//            viewHolder.mTemplateDoubleMd.setVisibility(View.VISIBLE);
//            new TemplateDoubleMd(mContext).setView(viewHolder.mTemplateDoubleMd, bundle);
        } else if(template.equals("template_order")){//订阅模版
//            viewHolder.mTitleView.setVisibility(View.INVISIBLE);
            viewHolder.mTemplateOrder.setVisibility(View.VISIBLE);
            new TemplateOrder(mContext).setView(viewHolder.mTemplateOrder, bundle)
                    .setTitleCountView(viewHolder.mCount);;
        } else if(template.equals("template_movie")){//电影模版
            viewHolder.mTemplateMovie.setVisibility(View.VISIBLE);
            new TemplateMovie(mContext).setView(viewHolder.mTemplateMovie, bundle)
                    .setTitleCountView(viewHolder.mCount);;
        } else if(template.equals("template_teleplay")){//电视剧模版
            viewHolder.mTemplateTvPlay.setVisibility(View.VISIBLE);
            new TemplateTvPlay(mContext).setView(viewHolder.mTemplateTvPlay, bundle)
                    .setTitleCountView(viewHolder.mCount);
        } else if(template.equals("template_519")){//519横图模版
            viewHolder.mTemplate519.setVisibility(View.VISIBLE);
            new Template519(mContext).setView(viewHolder.mTemplate519, bundle)
                    .setTitleCountView(viewHolder.mCount);;
        }else if(template.equals("template_conlumn")){//栏目模版
//            viewHolder.mTitleView.setVisibility(View.INVISIBLE);
            viewHolder.mTemplateConlumn.setVisibility(View.VISIBLE);
            new TemplateConlumn(mContext).setView(viewHolder.mTemplateConlumn, bundle)
                    .setTitleCountView(viewHolder.mCount);;
        }else if(template.equals("template_big_small_ld")){//大横小竖模版
            viewHolder.mTemplateBigSmallLd.setVisibility(View.VISIBLE);
            new TemplateBigSmallLd(mContext).setView(viewHolder.mTemplateBigSmallLd, bundle)
                    .setTitleCountView(viewHolder.mCount);;
        }else if(template.equals("template_double_md")){//竖版双行模版
            viewHolder.mTemplateDoubleMd.setVisibility(View.VISIBLE);
            new TemplateDoubleMd(mContext).setView(viewHolder.mTemplateDoubleMd, bundle)
                    .setTitleCountView(viewHolder.mCount);;
        }else if(template.equals("template_double_ld")){//横版双行模版
            viewHolder.mTemplateDoubleLd.setVisibility(View.VISIBLE);
            new TemplateDoubleLd(mContext).setView(viewHolder.mTemplateDoubleLd, bundle)
                    .setTitleCountView(viewHolder.mCount);;
        } else if(template.equals("template_center")){//居中模版
//            viewHolder.mTitleView.setVisibility(View.INVISIBLE);
            viewHolder.mTmeplateCenter.setVisibility(View.VISIBLE);
            new TemplateCenter(mContext).setView(viewHolder.mTmeplateCenter, bundle)
                    .setTitleCountView(viewHolder.mCount);;
        }

        return convertView;
    }

    class ViewHolder {
        TextView mTitle;//标题
        TextView mCount;//数量
        View mTitleView;
        View mTemplateGuide;//导视模版
        View mTemplateOrder;//订阅模版
        View mTemplateMovie;//电影模版
        View mTemplateTvPlay;//电视剧模版
        View mTemplate519;//519模版
        View mTemplateConlumn;//栏目模版
        View mTemplateBigSmallLd;//大横小竖模版
        View mTemplateDoubleMd;//竖版双行模版
        View mTemplateDoubleLd;//横版双行模版
        View mTmeplateCenter;//居中模版
    }
}
