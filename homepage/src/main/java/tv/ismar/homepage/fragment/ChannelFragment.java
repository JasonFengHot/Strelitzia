package tv.ismar.homepage.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.template.Template519;
import tv.ismar.homepage.template.TemplateBigSmallLd;
import tv.ismar.homepage.template.TemplateCenter;
import tv.ismar.homepage.template.TemplateConlumn;
import tv.ismar.homepage.template.TemplateDoubleLd;
import tv.ismar.homepage.template.TemplateDoubleMd;
import tv.ismar.homepage.template.TemplateGuide;
import tv.ismar.homepage.template.TemplateMovie;
import tv.ismar.homepage.template.TemplateOrder;
import tv.ismar.homepage.template.TemplateRecommend;
import tv.ismar.homepage.template.TemplateTvPlay;
import tv.ismar.homepage.widget.scroll.RecycleLinearLayout;
import tv.ismar.library.util.StringUtils;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/8
 * @DESC: 频道fragemnt
 */

public class ChannelFragment extends BaseFragment implements BaseControl.ControlCallBack{
    private FetchDataControl mControl = null;//业务类引用
    private String mChannel;//频道

    private RecycleLinearLayout mLinearContainer;//banner容器

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mControl = new FetchDataControl(getContext(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.channel_fragment_layout, null);
        findView(view);
        initData();
        return view;
    }

    private void findView(View view){
        mLinearContainer = (RecycleLinearLayout) view.findViewById(R.id.scroll_linear_container);
    }

    public void setChannel(String channel){
        mChannel = channel;
    }

    private void initData(){
        if(!StringUtils.isEmpty(mChannel)){
            if(mChannel.equals(HomeActivity.HOME_PAGE_CHANNEL_TAG)){
                mControl.fetchHomeBanners();
            } else {
                mControl.fetchChannelBanners(mChannel);
            }
        }
    }

    @Override
    public void callBack(int flags, Object... args) {
        GuideBanner[] banners = (GuideBanner[]) args;
        initBanner(banners);
    }

    public static final String TITLE_KEY = "title";
    public static final String URL_KEY = "url";
    public static final String BANNER_KEY = "banner";
    public static final String TEMPLATE_KEY = "template";

    /*初始化banner视图*/
    private void initBanner(GuideBanner[] data){
        if(data==null || data.length<=0){
            return;
        }
        mLinearContainer.removeAllViews();
        for(int position=0; position<data.length; position++){
            View bannerView = null;
            String template = data[position].template;
            Bundle bundle = new Bundle();
            bundle.putString(TITLE_KEY, data[position].title);
            bundle.putString(URL_KEY, data[position].banner_url);
            bundle.putInt(BANNER_KEY, data[position].page_banner_pk);
            bundle.putString(TEMPLATE_KEY, template);
            if(template.equals("template_guide")){//导视
                bannerView = createView(R.layout.banner_guide);
                new TemplateGuide(getContext()).setView(bannerView, bundle);
            } else if(template.equals("template_order")){//订阅模版
                bannerView = createView(R.layout.banner_order);
                new TemplateOrder(getContext()).setView(bannerView, bundle);
            } else if(template.equals("template_movie")){//电影模版
                bannerView = createView(R.layout.banner_movie);
                new TemplateMovie(getContext()).setView(bannerView, bundle);
            } else if(template.equals("template_teleplay")){//电视剧模版
                bannerView = createView(R.layout.banner_tv_player);
                new TemplateTvPlay(getContext()).setView(bannerView, bundle);
            } else if(template.equals("template_519")){//519横图模版
                bannerView = createView(R.layout.banner_519);
                new Template519(getContext()).setView(bannerView, bundle);
            }else if(template.equals("template_conlumn")){//栏目模版
                bannerView = createView(R.layout.banner_conlumn);
                new TemplateConlumn(getContext()).setView(bannerView, bundle);
            }else if(template.equals("template_recommend")){//推荐模版
                bannerView = createView(R.layout.banner_conlumn);
                new TemplateRecommend(getContext()).setView(bannerView, bundle);
            } else if(template.equals("template_big_small_ld")){//大横小竖模版
                bannerView = createView(R.layout.banner_big_small_ld);
                new TemplateBigSmallLd(getContext()).setView(bannerView, bundle);
            }else if(template.equals("template_double_md")){//竖版双行模版
                bannerView = createView(R.layout.banner_double_md);
                new TemplateDoubleMd(getContext()).setView(bannerView, bundle);
            }else if(template.equals("template_double_ld")){//横版双行模版
                bannerView = createView(R.layout.banner_double_ld);
                new TemplateDoubleLd(getContext()).setView(bannerView, bundle);
            } else if(template.equals("template_center")){//居中模版
                bannerView = createView(R.layout.banner_center);
                new TemplateCenter(getContext()).setView(bannerView, bundle);
            }
            if(bannerView != null){
                mLinearContainer.setView(bannerView);
            }
        }
        mLinearContainer.initView();
    }

    private View createView(int layoutId){
        return LayoutInflater.from(getContext()).inflate(layoutId, null);
    }
}
