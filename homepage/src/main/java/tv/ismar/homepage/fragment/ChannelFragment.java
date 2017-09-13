package tv.ismar.homepage.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.app.util.BitmapDecoder;
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
import tv.ismar.homepage.template.TemplateTvPlay;
import tv.ismar.library.util.StringUtils;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/8
 * @DESC: 频道fragemnt
 */

public class ChannelFragment extends BaseFragment implements BaseControl.ControlCallBack{
    private FetchDataControl mControl = null;//业务类引用
    private String mChannel;//频道

    private View mTemplateGuide;//导视模版
    private View mTemplateOrder;//订阅模版
    private View mTemplateMovie;//电影模版
    private View mTemplateTvPlay;//电视剧模版
    private View mTemplate519;//519模版
    private View mTemplateConlumn;//栏目模版
    private View mTemplateBigSmallLd;//大横小竖模版
    private View mTemplateDoubleMd;//竖版双行模版
    private View mTemplateDoubleLd;//横版双行模版
    private View mTmeplateCenter;//居中模版

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mControl = new FetchDataControl(getContext(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.test_activity_layout, null);
        findView(view);
        initData();
        return view;
    }

    private void findView(View view){
        mTemplateGuide = view.findViewById(R.id.banner_guide);
        mTemplateOrder = view.findViewById(R.id.banner_order);
        mTemplateMovie = view.findViewById(R.id.banner_movie);
        mTemplateTvPlay = view.findViewById(R.id.banner_tv_play);
        mTemplate519 = view.findViewById(R.id.banner_519);
        mTemplateConlumn = view.findViewById(R.id.banner_conlumn);
        mTemplateBigSmallLd = view.findViewById(R.id.banner_big_small_ld);
        mTemplateDoubleMd = view.findViewById(R.id.banner_double_md);
        mTemplateDoubleLd = view.findViewById(R.id.banner_double_ld);
        mTmeplateCenter = view.findViewById(R.id.banner_center);
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

    private void initBanner(GuideBanner[] data){
        mTemplateGuide.setVisibility(View.GONE);
        mTemplateOrder.setVisibility(View.GONE);
        mTemplateMovie.setVisibility(View.GONE);
        mTemplateTvPlay.setVisibility(View.GONE);
        mTemplate519.setVisibility(View.GONE);
        mTemplateConlumn.setVisibility(View.GONE);
        mTemplateBigSmallLd.setVisibility(View.GONE);
        mTemplateDoubleMd.setVisibility(View.GONE);
        mTemplateDoubleLd.setVisibility(View.GONE);
        mTmeplateCenter.setVisibility(View.GONE);
        for(int position=0; position<data.length; position++){
            Bundle bundle = new Bundle();
            bundle.putString("title", data[position].title);
            bundle.putString("url", data[position].banner_url);
            bundle.putInt("banner", data[position].page_banner_pk);
            String template = data[position].template;
            if(template.equals("template_guide")){//导航
                mTemplateGuide.setVisibility(View.VISIBLE);
                new TemplateGuide(getContext()).setView(mTemplateGuide, bundle);
            } else if(template.equals("template_order")){//订阅模版
                mTemplateOrder.setVisibility(View.VISIBLE);
                new TemplateOrder(getContext()).setView(mTemplateOrder, bundle);
            } else if(template.equals("template_movie")){//电影模版
                mTemplateMovie.setVisibility(View.VISIBLE);
                new TemplateMovie(getContext()).setView(mTemplateMovie, bundle);
            } else if(template.equals("template_teleplay")){//电视剧模版
                mTemplateTvPlay.setVisibility(View.VISIBLE);
                new TemplateTvPlay(getContext()).setView(mTemplateTvPlay, bundle);
            } else if(template.equals("template_519")){//519横图模版
                mTemplate519.setVisibility(View.VISIBLE);
                new Template519(getContext()).setView(mTemplate519, bundle);
            }else if(template.equals("template_conlumn") || template.equals("template_recommend")){//栏目模版
                mTemplateConlumn.setVisibility(View.VISIBLE);
                new TemplateConlumn(getContext()).setView(mTemplateConlumn, bundle);
            }else if(template.equals("template_big_small_ld")){//大横小竖模版
                mTemplateBigSmallLd.setVisibility(View.VISIBLE);
                new TemplateBigSmallLd(getContext()).setView(mTemplateBigSmallLd, bundle);
            }else if(template.equals("template_double_md")){//竖版双行模版
                mTemplateDoubleMd.setVisibility(View.VISIBLE);
                new TemplateDoubleMd(getContext()).setView(mTemplateDoubleMd, bundle);
            }else if(template.equals("template_double_ld")){//横版双行模版
                mTemplateDoubleLd.setVisibility(View.VISIBLE);
                new TemplateDoubleLd(getContext()).setView(mTemplateDoubleLd, bundle);
            } else if(template.equals("template_center")){//居中模版
                mTmeplateCenter.setVisibility(View.VISIBLE);
                new TemplateCenter(getContext()).setView(mTmeplateCenter, bundle);
            }
        }
    }
}
