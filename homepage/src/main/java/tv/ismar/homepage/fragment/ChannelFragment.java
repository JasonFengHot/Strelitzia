package tv.ismar.homepage.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.homepage.template.Template;
import tv.ismar.homepage.template.Template519;
import tv.ismar.homepage.template.TemplateBigSmallLd;
import tv.ismar.homepage.template.TemplateCenter;
import tv.ismar.homepage.template.TemplateConlumn;
import tv.ismar.homepage.template.TemplateDoubleLd;
import tv.ismar.homepage.template.TemplateDoubleMd;
import tv.ismar.homepage.template.TemplateGuide;
import tv.ismar.homepage.template.TemplateMore;
import tv.ismar.homepage.template.TemplateMovie;
import tv.ismar.homepage.template.TemplateOrder;
import tv.ismar.homepage.template.TemplateRecommend;
import tv.ismar.homepage.template.TemplateTvPlay;
import tv.ismar.homepage.widget.RecycleLinearLayout;
import tv.ismar.library.util.StringUtils;

/**
 * @AUTHOR: xi @DATE: 2017/9/8 @DESC: 频道fragemnt
 */
public class ChannelFragment extends BaseFragment implements BaseControl.ControlCallBack {
    private static final String TAG = "ChannelFragment";
    public static final String TITLE_KEY = "title";
    public static final String URL_KEY = "url";
    public static final String BANNER_KEY = "banner";
    public static final String TEMPLATE_KEY = "template";
    public static final String CHANNEL_KEY = "channel";
    public static final String NAME_KEY = "name";
    public static final String MORE_TITLE_FLAG = "title";
    public static final String MORE_CHANNEL_FLAG = "channel";
    public static final String MORE_STYLE_FLAG = "style";
    private FetchDataControl mControl = null; // 业务类引用
    private RecycleLinearLayout mLinearContainer; // banner容器
    private List<Template> mTemplates;
    private String mChannel; // 频道
    private String mName; // 频道名称
    private String mTitle; // 标题
    private int mStyle; // 竖版或横版

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mControl = new FetchDataControl(getContext(), this);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.channel_fragment_layout, null);
        findView(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTemplates != null) {
            for (Template template : mTemplates) {
                template.onResume();
            }
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        if (mControl != null) {
            mControl.stop();
        }

        if (mTemplates != null) {
            for (Template template : mTemplates) {
                template.onPause();
            }
            mTemplates.clear();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        if (mTemplates != null) {
            for (Template template : mTemplates) {
                template.onStop();
            }
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mTemplates != null) {
            for (Template template : mTemplates) {
                template.onDestroy();
            }
        }
        super.onDestroy();
    }

    private void findView(View view) {
        mLinearContainer = (RecycleLinearLayout) view.findViewById(R.id.scroll_linear_container);
    }

    public void setChannel(String name, String channel, String title, int style) {
        mName = name;
        mChannel = channel;
        mTitle = title;
        mStyle = style;
    }

    private void initData() {
        if (!StringUtils.isEmpty(mChannel)) {
            if (mChannel.equals(HomeActivity.HOME_PAGE_CHANNEL_TAG)) { // 首页数据
                mControl.fetchHomeBanners();
            } else { // 其他频道数据
                mControl.fetchChannelBanners(mChannel);
            }
        }
    }

    @Override
    public void callBack(int flags, Object... args) {
        GuideBanner[] banners = (GuideBanner[]) args;
        initBanner(banners);
    }

    /*初始化banner视图*/
    private void initBanner(GuideBanner[] data) {
        if (data == null || data.length <= 0) {
            return;
        }
        mTemplates = new ArrayList<>();
        mLinearContainer.removeAllViews();

        for (int position = 0; position < data.length; position++) {
            Template templateObject = null;
            View bannerView = null;
            int layoutId = -1;
            boolean canScroll = true; // 是否可以滑动,默认可以滑动
            String template = data[position].template;
            Bundle bundle = new Bundle();
            bundle.putString(TITLE_KEY, data[position].title);
            bundle.putString(URL_KEY, data[position].banner_url);
            bundle.putInt(BANNER_KEY, data[position].page_banner_pk);
            bundle.putString(TEMPLATE_KEY, template);
            bundle.putString(CHANNEL_KEY, mChannel);
            bundle.putString(NAME_KEY, mName);
            if (template.equals("template_guide")) { // 导视
                layoutId = R.layout.banner_guide;
                bannerView = createView(R.layout.banner_guide);
                templateObject = new TemplateGuide(getContext()).setView(bannerView, bundle);
            } else if (template.equals("template_order")) { // 订阅模版
                layoutId = R.layout.banner_order;
                bannerView = createView(R.layout.banner_order);
                templateObject = new TemplateOrder(getContext()).setView(bannerView, bundle);
            } else if (template.equals("template_movie")) { // 电影模版
                layoutId = R.layout.banner_movie;
                bannerView = createView(R.layout.banner_movie);
                templateObject = new TemplateMovie(getContext()).setView(bannerView, bundle);
            } else if (template.equals("template_teleplay")) { // 电视剧模版
                layoutId = R.layout.banner_tv_player;
                bannerView = createView(R.layout.banner_tv_player);
                templateObject = new TemplateTvPlay(getContext()).setView(bannerView, bundle);
            } else if (template.equals("template_519")) { // 519横图模版
                layoutId = R.layout.banner_519;
                bannerView = createView(R.layout.banner_519);
                templateObject = new Template519(getContext()).setView(bannerView, bundle);
            } else if (template.equals("template_conlumn")) { // 栏目模版
                layoutId = R.layout.banner_conlumn;
                bannerView = createView(R.layout.banner_conlumn);
                templateObject = new TemplateConlumn(getContext()).setView(bannerView, bundle);
            } else if (template.equals("template_recommend")) { // 推荐模版
                canScroll = false;
                layoutId = R.layout.banner_recommend;
                bannerView = createView(R.layout.banner_recommend);
                templateObject = new TemplateRecommend(getContext()).setView(bannerView, bundle);
            } else if (template.equals("template_big_small_ld")) { // 大横小竖模版
                layoutId = R.layout.banner_big_small_ld;
                bannerView = createView(R.layout.banner_big_small_ld);
                templateObject = new TemplateBigSmallLd(getContext()).setView(bannerView, bundle);
            } else if (template.equals("template_double_md")) { // 竖版双行模版
                layoutId = R.layout.banner_double_md;
                bannerView = createView(R.layout.banner_double_md);
                templateObject = new TemplateDoubleMd(getContext()).setView(bannerView, bundle);
            } else if (template.equals("template_double_ld")) { // 横版双行模版
                layoutId = R.layout.banner_double_ld;
                bannerView = createView(R.layout.banner_double_ld);
                templateObject = new TemplateDoubleLd(getContext()).setView(bannerView, bundle);
            } else if (template.equals("template_teleplay_first")) { // 电视剧首行居中模版
                layoutId = R.layout.banner_center;
                bannerView = createView(R.layout.banner_center);
                templateObject = new TemplateCenter(getContext()).setView(bannerView, bundle);
            }

            if (bannerView != null && templateObject != null) {
                int tag = createTag(position, canScroll);
                bannerView.setTag(layoutId);
                bannerView.setTag(layoutId, tag);
                mTemplates.add(templateObject);
                mLinearContainer.addView(bannerView);
            }
        }

        addMoreView(data.length);
        //        mLinearContainer.initView();
    }

    private int createTag(int position, boolean canScroll) {
        return canScroll ? (1 << 30 | position) : position;
    }

    /*添加更多内容模版*/
    private void addMoreView(int position) {
        Bundle bundle = new Bundle();
        bundle.putString(MORE_TITLE_FLAG, mTitle);
        bundle.putString(MORE_CHANNEL_FLAG, mChannel);
        bundle.putInt(MORE_STYLE_FLAG, mStyle);
        View bannerView = createView(R.layout.banner_more);
        bannerView.setTag(R.layout.banner_more);
        bannerView.setTag(R.layout.banner_more, createTag(position, true));

        Template templateObject = new TemplateMore(getContext()).setView(bannerView, bundle);
        if (mTemplates != null){
            mTemplates.add(templateObject);
        }
        mLinearContainer.addView(bannerView);
    }

    private View createView(int layoutId) {
        return LayoutInflater.from(getContext()).inflate(layoutId, null);
    }

}
