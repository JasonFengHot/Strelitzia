package tv.ismar.homepage.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tv.ismar.app.entity.GuideBanner;
import tv.ismar.homepage.R;
import tv.ismar.homepage.template.Template519;
import tv.ismar.homepage.template.TemplateBigSmallLd;
import tv.ismar.homepage.template.TemplateCenter;
import tv.ismar.homepage.template.TemplateConlumn;
import tv.ismar.homepage.template.TemplateDoubleLd;
import tv.ismar.homepage.template.TemplateDoubleMd;
import tv.ismar.homepage.template.TemplateGuide;
import tv.ismar.homepage.template.TemplateMovie;
import tv.ismar.homepage.template.TemplateOrder;
import tv.ismar.homepage.template.TemplateTvPlay;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/7
 * @DESC: 大首页recycleview适配器
 */

public class HomeReclAdapter extends RecyclerView.Adapter<HomeReclAdapter.HomeReclViewHolder>{
    private GuideBanner[] mData;
    private Context mContext;

    private static final int TEMPLATE_GUIDE = 0;
    private static final int TEMPLATE_ORDER = 1;
    private static final int TEMPLATE_MOVIE = 2;
    private static final int TEMPLATE_TELEPLAY = 3;
    private static final int TEMPLATE_519 = 4;
    private static final int TEMPLATE_CONLUMN = 5;
    private static final int TEMPLATE_SMALL_LD = 6;
    private static final int TEMPLATE_DOUBLE_MD = 7;
    private static final int TEMPLATE_DOUBLE_LD = 8;
    private static final int TEMPLATE_CENTER = 9;

    public HomeReclAdapter(Context context, GuideBanner[] data){
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public HomeReclAdapter.HomeReclViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return createView(viewType);
    }

    private View inflateView(int layoutId){
        return LayoutInflater.from(mContext).inflate(layoutId, null);
    }

    private HomeReclViewHolder createView(int viewType){
        View view = null;
        switch (viewType){
            case TEMPLATE_GUIDE:
                view = inflateView(R.layout.banner_guide);
                break;
            case TEMPLATE_ORDER:
                view = inflateView(R.layout.banner_order);
                break;
            case TEMPLATE_MOVIE:
                view = inflateView(R.layout.banner_movie);
                break;
            case TEMPLATE_TELEPLAY:
                view = inflateView(R.layout.banner_tv_player);
                break;
            case TEMPLATE_519:
                view = inflateView(R.layout.banner_519);
                break;
            case TEMPLATE_CONLUMN:
                view = inflateView(R.layout.banner_conlumn);
                break;
            case TEMPLATE_SMALL_LD:
                view = inflateView(R.layout.banner_big_small_ld);
                break;
            case TEMPLATE_DOUBLE_MD:
                view = inflateView(R.layout.banner_double_md);
                break;
            case TEMPLATE_DOUBLE_LD:
                view = inflateView(R.layout.banner_double_ld);
                break;
            case TEMPLATE_CENTER:
                view = inflateView(R.layout.banner_center);
                break;
            default:
                view = inflateView(R.layout.banner_conlumn);
                break;
        }

        return new HomeReclViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeReclAdapter.HomeReclViewHolder holder, int position) {
        Bundle bundle = new Bundle();
        bundle.putString("title", mData[position].title);
        bundle.putString("url", mData[position].banner_url);
        bundle.putString("banner", mData[position].banner);
        String template = mData[position].template;

        if(template.equals("template_guide")){//导航
            new TemplateGuide(mContext).setView(holder.mItemView, bundle);
        } else if(template.equals("template_order")){//订阅模版
            new TemplateOrder(mContext).setView(holder.mItemView, bundle);
        } else if(template.equals("template_movie")){//电影模版
            new TemplateMovie(mContext).setView(holder.mItemView, bundle);
        } else if(template.equals("template_teleplay")){//电视剧模版
            new TemplateTvPlay(mContext).setView(holder.mItemView, bundle);
        } else if(template.equals("template_519")){//519横图模版
            new Template519(mContext).setView(holder.mItemView, bundle);
        }else if(template.equals("template_conlumn")){//栏目模版
            new TemplateConlumn(mContext).setView(holder.mItemView, bundle);
        }else if(template.equals("template_big_small_ld")){//大横小竖模版
            new TemplateBigSmallLd(mContext).setView(holder.mItemView, bundle);
        }else if(template.equals("template_double_md")){//竖版双行模版
            new TemplateDoubleMd(mContext).setView(holder.mItemView, bundle);
        }else if(template.equals("template_double_ld")){//横版双行模版
            new TemplateDoubleLd(mContext).setView(holder.mItemView, bundle);
        } else if(template.equals("template_center")){//居中模版
            new TemplateCenter(mContext).setView(holder.mItemView, bundle);
        } else {//其他，默认为栏目模版
            new TemplateConlumn(mContext).setView(holder.mItemView, bundle);
        }
    }

    @Override
    public int getItemViewType(int position) {
        String template = mData[position].template;
        if(template.equals("template_guide")){//导航
            return TEMPLATE_GUIDE;
        } else if(template.equals("template_order")){//订阅模版
            return TEMPLATE_ORDER;
        } else if(template.equals("template_movie")){//电影模版
            return TEMPLATE_MOVIE;
        } else if(template.equals("template_teleplay")){//电视剧模版
            return TEMPLATE_TELEPLAY;
        } else if(template.equals("template_519")){//519横图模版
            return TEMPLATE_519;
        }else if(template.equals("template_conlumn")){//栏目模版
            return TEMPLATE_CONLUMN;
        }else if(template.equals("template_big_small_ld")){//大横小竖模版
            return TEMPLATE_SMALL_LD;
        }else if(template.equals("template_double_md")){//竖版双行模版
            return TEMPLATE_DOUBLE_MD;
        }else if(template.equals("template_double_ld")){//横版双行模版
            return TEMPLATE_DOUBLE_LD;
        } else if(template.equals("template_center")){//居中模版
            return TEMPLATE_CENTER;
        } else {
            return TEMPLATE_CONLUMN;
        }
    }

    public static class HomeReclViewHolder extends RecyclerView.ViewHolder {

        private View mItemView;

        public HomeReclViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
        }
    }

    @Override
    public int getItemCount() {
        return (mData!=null)?mData.length:0;
    }
}
