package tv.ismar.homepage;

import android.os.Bundle;
import android.view.View;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.GuideBanner;
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

import static tv.ismar.homepage.control.FetchDataControl.FETCH_CHANNEL_BANNERS_FLAG;
import static tv.ismar.homepage.control.FetchDataControl.FETCH_HOME_BANNERS_FLAG;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/13
 * @DESC: 将容器改为ScrollView
 */

public class TestActivity extends BaseActivity implements BaseControl.ControlCallBack {

    private FetchDataControl mControl = new FetchDataControl(this, this);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_layout);
        initView();
        initData();
    }

    private void initView(){
        mTemplateGuide = findViewById(R.id.banner_guide);
        mTemplateOrder = findViewById(R.id.banner_order);
        mTemplateMovie = findViewById(R.id.banner_movie);
        mTemplateTvPlay = findViewById(R.id.banner_tv_play);
        mTemplate519 = findViewById(R.id.banner_519);
        mTemplateConlumn = findViewById(R.id.banner_conlumn);
        mTemplateBigSmallLd = findViewById(R.id.banner_big_small_ld);
        mTemplateDoubleMd = findViewById(R.id.banner_double_md);
        mTemplateDoubleLd = findViewById(R.id.banner_double_ld);
        mTmeplateCenter = findViewById(R.id.banner_center);
    }

    private void initData(){
        mControl.fetchHomeBanners();
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
                new TemplateGuide(this).setView(mTemplateGuide, bundle);
            } else if(template.equals("template_order")){//订阅模版
                mTemplateOrder.setVisibility(View.VISIBLE);
                new TemplateOrder(this).setView(mTemplateOrder, bundle);
            } else if(template.equals("template_movie")){//电影模版
                mTemplateMovie.setVisibility(View.VISIBLE);
                new TemplateMovie(this).setView(mTemplateMovie, bundle);
            } else if(template.equals("template_teleplay")){//电视剧模版
                mTemplateTvPlay.setVisibility(View.VISIBLE);
                new TemplateTvPlay(this).setView(mTemplateTvPlay, bundle);
            } else if(template.equals("template_519")){//519横图模版
                mTemplate519.setVisibility(View.VISIBLE);
                new Template519(this).setView(mTemplate519, bundle);
            }else if(template.equals("template_conlumn") || template.equals("template_recommend")){//栏目模版
                mTemplateConlumn.setVisibility(View.VISIBLE);
                new TemplateConlumn(this).setView(mTemplateConlumn, bundle);
            }else if(template.equals("template_big_small_ld")){//大横小竖模版
                mTemplateBigSmallLd.setVisibility(View.VISIBLE);
                new TemplateBigSmallLd(this).setView(mTemplateBigSmallLd, bundle);
            }else if(template.equals("template_double_md")){//竖版双行模版
                mTemplateDoubleMd.setVisibility(View.VISIBLE);
                new TemplateDoubleMd(this).setView(mTemplateDoubleMd, bundle);
            }else if(template.equals("template_double_ld")){//横版双行模版
                mTemplateDoubleLd.setVisibility(View.VISIBLE);
                new TemplateDoubleLd(this).setView(mTemplateDoubleLd, bundle);
            } else if(template.equals("template_center")){//居中模版
                mTmeplateCenter.setVisibility(View.VISIBLE);
                new TemplateCenter(this).setView(mTmeplateCenter, bundle);
            }
        }
    }

    @Override
    public void callBack(int flags, Object... args) {
        GuideBanner[] banners = (GuideBanner[]) args;
        initBanner(banners);
    }
}
