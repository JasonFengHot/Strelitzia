package tv.ismar.homepage.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/*add by dragontec for bug 4065 start*/
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
/*add by dragontec for bug 4065 end*/

import android.widget.LinearLayout;

import com.orhanobut.logger.Logger;

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
import tv.ismar.homepage.widget.HomeRootRelativeLayout;
import tv.ismar.homepage.widget.RecycleLinearLayout;
import tv.ismar.library.util.StringUtils;

/**
 * @AUTHOR: xi @DATE: 2017/9/8 @DESC: 频道fragemnt
 */
public class ChannelFragment extends BaseFragment implements BaseControl.ControlCallBack
	/*add by dragontec for bug 3983,4077,4200 start*/
		, RecycleLinearLayout.OnDataFinishedListener, RecycleLinearLayout.OnPositionChangedListener, RecycleLinearLayout.OnScrollFinishedListener
	/*add by dragontec for bug 3983,4077,4200 end*/
{
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
	private static final int LOAD_BANNERS_COUNT = 5;
	/*add by dragontec for bug 4248,4334 start*/
	private static final int APPEND_LOAD_BANNERS_COUNT = 3;
	/*add by dragontec for bug 4248,4334 end*/
	private FetchDataControl mControl = null; // 业务类引用
    private RecycleLinearLayout mLinearContainer; // banner容器
    private List<Template> mTemplates;
    private String mChannel; // 频道
    private String mName; // 频道名称
    private String mTitle; // 标题
    private int mStyle; // 竖版或横版

	/*add by dragontec for bug 4077 start*/
	private GuideBanner[] mGuideBanners = null;

	private final Object bannerDataLock = new Object();

	private final Object templateDataLock = new Object();

	private final Object layoutLock = new Object();

	private int lastLoadedPostion = -1;

	private View mLastFocus;
	private HomeRootRelativeLayout homeRootRelativeLayout;
	/*add by dragontec for bug 4077 end*/

/*add by dragontec for bug 4065 start*/
	private Object mAniLock = new Object();
	private boolean mNeedAddBanner = false;
	private boolean mDoingFragmentFlipAni = false;
	private Handler mAniHandler = null;
	private AniFinishRunnable mAniFinishRunnable = null;
/*add by dragontec for bug 4065 end*/

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
        Logger.t(TAG).d("ChannelFragment onCreateView");
		/*add by dragontec for bug 3983 start*/
        mLinearContainer.setOnPositionChangedListener(this);
		/*add by dragontec for bug 3983 end*/
		/*add by dragontec for bug 4077 start*/
		mLinearContainer.setOnDataFinishedListener(this);
		/*add by dragontec for bug 4077 end*/
		/*add by dragontec for bug 4200 start*/
		mLinearContainer.setOnScrollFinishedListener(this);
		/*add by dragontec for bug 4200 end*/
/*add by dragontec for bug 4065 start*/
		mAniHandler = new Handler();
/*add by dragontec for bug 4065 end*/
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
		synchronized (templateDataLock) {
			if (mTemplates != null) {
				for (Template template : mTemplates) {
					template.onStart();
				}
			}
		}
	}

	@Override
    public void onResume() {
        super.onResume();

		synchronized (templateDataLock) {
			if (mTemplates != null) {
				for (Template template : mTemplates) {
					template.onResume();
				}
			}
		}
    }

    @Override
    public void onPause() {
		synchronized (templateDataLock) {
			if (mTemplates != null) {
				for (Template template : mTemplates) {
					template.onPause();
				}
			}
		}

        if (mControl != null) {
            mControl.stop();
        }
	/*add by dragontec for bug 4077 start*/
		mLastFocus = null;
	/*add by dragontec for bug 4077 end*/
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
		synchronized (templateDataLock) {
			if (mTemplates != null) {
				for (Template template : mTemplates) {
					template.onStop();
				}
			}
		}
        super.onStop();
    }

    @Override
    public void onDestroyView() {
		/*add by dragontec for bug 4200 start*/
		mLinearContainer.setOnScrollFinishedListener(null);
		/*add by dragontec for bug 4200 end*/
		mLinearContainer.setOnPositionChangedListener(null);
		mLinearContainer.setOnDataFinishedListener(null);
/*delete by dragontec for bug 4065 start*/
//        if (mLinearContainer != null){
//            for (int i = 0; i < mLinearContainer.getChildCount(); i++){
//                mLinearContainer.removeViewAt(i);
//            }
//        }
/*delete by dragontec for bug 4065 end*/
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
/*add by dragontec for bug 4065 start*/
		if (mLinearContainer != null){
			mLinearContainer.removeAllViews();
		}
/*add by dragontec for bug 4065 end*/
		synchronized (templateDataLock) {
			if (mTemplates != null) {
				for (Template template : mTemplates) {
					template.onDestroy();
				}
			}
			if (mTemplates != null) {
				mTemplates.clear();
			}
		}
        mControl = null;
        super.onDestroy();
    }

    private void findView(View view) {
        mLinearContainer = (RecycleLinearLayout) view.findViewById(R.id.scroll_linear_container);
/*add by dragontec for bug 4195 start*/
		mLinearContainer.enableChildrenDrawingOrder();
/*add by dragontec for bug 4195 end*/
        if(getActivity() instanceof HomeActivity){
            mLinearContainer.setArrow_up(((HomeActivity) getActivity()).banner_arrow_up);
            mLinearContainer.setArrow_down(((HomeActivity) getActivity()).banner_arrow_down);
			homeRootRelativeLayout = ((HomeActivity) getActivity()).mHoverView;
			mLinearContainer.setHomeRootRelativeLayout(homeRootRelativeLayout);
        }
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
/*modify by dragontec for bug 4065 start*/
//        GuideBanner[] banners = (GuideBanner[]) args;
//		/*modify by dragontec for bug 4178 start*/
//        mLinearContainer.setDataSize(banners.length);
//		/*modify by dragontec for bug 4178 end*/
//        initBanner(banners);
		synchronized (mAniLock) {
			GuideBanner[] banners = (GuideBanner[]) args;
			/*modify by dragontec for bug 4178 start*/
			if (banners != null) {
				mLinearContainer.setDataSize(banners.length);
			} else {
				mLinearContainer.setDataSize(0);
			}
			/*modify by dragontec for bug 4178 end*/
			synchronized (bannerDataLock) {
                mGuideBanners = banners;
			}
            if (mDoingFragmentFlipAni) {
                mNeedAddBanner = true;
            } else {
                initBanner(mGuideBanners);
            }
        }
/*modify by dragontec for bug 4065 end*/
    }

	/*modify by dragontec for bug 4077 start*/
	/*初始化banner视图*/
	private void initBanner(GuideBanner[] data) {
		if (data == null || data.length <= 0) {
			return;
		}
		synchronized (templateDataLock) {
			mTemplates = new ArrayList<>();
		}
/*delete by dragontec for bug 4065 start*/
//		synchronized (bannerDataLock) {
//			mGuideBanners = data;
//		}
/*delete by dragontec for bug 4065 end*/
		synchronized (layoutLock) {
			lastLoadedPostion = -1;
			mLinearContainer.removeAllViews();
			int size = data.length > LOAD_BANNERS_COUNT ? LOAD_BANNERS_COUNT : data.length;
			for (int position = 0; position < size; position++) {
				addBannerView(position, data[position]);
			}
			/*modify by dragontec for bug 4331 start*/
			if (lastLoadedPostion == data.length - 1) {
				if (!mChannel.equals("homepage")) {//首页频道最后不添加更多banner
					addMoreView(data.length);
				} else {
					synchronized (templateDataLock) {
						mTemplates.get(lastLoadedPostion).isLastView = true;
					}
				}
			/*modify by dragontec for bug 4331 end*/
			}
		}
		/*add by dragontec for bug 4200 start*/
		synchronized (templateDataLock) {
			for (Template template :
					mTemplates) {
				template.fetchData();
			}
		}
		/*add by dragontec for bug 4200 end*/
		//        mLinearContainer.initView();
	}

	private void appendBanner(int position) {
		GuideBanner[] data;
		synchronized (bannerDataLock) {
			data = mGuideBanners;
		}
		synchronized (layoutLock) {
			if (data != null) {
				if (position < data.length) {
					addBannerView(position, data[position]);
				}
				Log.d(TAG, "lastLoadedPostion = " + lastLoadedPostion + ", data.length = " + data.length);

			/*modify by dragontec for bug 4331 start*/
				if (lastLoadedPostion == data.length - 1) {
					if (!mChannel.equals("homepage")) {//首页频道最后不添加更多banner
						addMoreView(data.length);
					} else {
						synchronized (templateDataLock) {
							mTemplates.get(lastLoadedPostion).isLastView = true;
						}
					}
			/*modify by dragontec for bug 4331 end*/
				}
			}
		}
	}

	private void addBannerView(int position, GuideBanner guideBanner) {
		if (position <= lastLoadedPostion) {
			return;
		}
		lastLoadedPostion = position;
		Template templateObject = null;
		View bannerView = null;
		int layoutId = -1;
		boolean canScroll = true; // 是否可以滑动,默认可以滑动
		String template = guideBanner.template;
		Bundle bundle = new Bundle();
		bundle.putString(TITLE_KEY, guideBanner.title);
		bundle.putString(URL_KEY, guideBanner.banner_url);
		bundle.putString(BANNER_KEY, guideBanner.page_banner_pk);
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
			synchronized (templateDataLock) {
				if (mTemplates != null) {
					mTemplates.add(templateObject);
				}
			}
			mLinearContainer.addView(bannerView);
		}
	}
	/*modify by dragontec for bug 4077 end*/

    private int createTag(int position, boolean canScroll) {
        return canScroll ? (1 << 30 | position) : position;
    }

    /*添加更多内容模版*/
    private void addMoreView(int position) {
	/*add by dragontec for bug 4077 start*/
		if (position <= lastLoadedPostion) {
			return;
		}
		lastLoadedPostion = position;
	/*add by dragontec for bug 4077 end*/
        Bundle bundle = new Bundle();
        bundle.putString(MORE_TITLE_FLAG, mTitle);
        bundle.putString(MORE_CHANNEL_FLAG, mChannel);
        bundle.putInt(MORE_STYLE_FLAG, mStyle);
        View bannerView = createView(R.layout.banner_more);
        bannerView.setTag(R.layout.banner_more);
        bannerView.setTag(R.layout.banner_more, createTag(position, true));

        Template templateObject = new TemplateMore(getContext()).setView(bannerView, bundle);
		synchronized (templateDataLock) {
			if (mTemplates != null) {
				mTemplates.add(templateObject);
			}
		}
        mLinearContainer.addView(bannerView);
		mLinearContainer.setHasMore(true);
    }

    private View createView(int layoutId) {
        return LayoutInflater.from(getContext()).inflate(layoutId, null);
    }
	/*add by dragontec for bug 3983 start*/
    @Override
    public boolean onPositionChanged(int position, int direction, boolean canScroll) {
        boolean ret = false;
        if(direction == KeyEvent.KEYCODE_DPAD_DOWN){
			/*modify by dragontec for bug 3983 修正了快速移动焦点时position直接从3开始，导致往下按title没有隐藏的bug*/
            if((position >= 2 && canScroll) || (position == 1 && canScroll)){
                ((HomeActivity)getActivity()).titleMoveOut();
                ret = true;
            }
        }else  if(direction == KeyEvent.KEYCODE_DPAD_UP){
            if((position == 1 && !canScroll)|| position == 0){
                ((HomeActivity)getActivity()).titleMoveIn();
                ret = true;
            }
        }
        return ret;
    }
	/*add by dragontec for bug 3983 end*/
	/*add by dragontec for bug 4077 start*/
	@Override
	public void onDataFinished(View view) {
		if (mLastFocus == view) {
			return;
		}
		mLastFocus = view;
		int layoutId = (int) view.getTag();
		int positionTag = (int) view.getTag(layoutId);
		int position = (positionTag << 2) >> 2;
		Log.d(TAG,  "position = " + position );
		synchronized (bannerDataLock) {
			if (mGuideBanners == null || position > mGuideBanners.length + 1) {
				return;
			}
		}
		int last;
		synchronized (layoutLock) {
			last = lastLoadedPostion;
		}
		Log.d(TAG, "last = " + last);
		if (position > last - RecycleLinearLayout.BANNER_LOAD_AIMING_OFF) {
				/*modify by dragontec for bug 4248 start*/
				int number = position + RecycleLinearLayout.BANNER_LOAD_AIMING_OFF - last + (APPEND_LOAD_BANNERS_COUNT - 1);
				/*modify by dragontec for bug 4248 end*/
				for (int i = 0; i < number; i++) {
					appendBanner(last + i + 1);
				}
		}
	}
	/*add by dragontec for bug 4077 end*/

	public void onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "keydown: " + keyCode);
//		if ("lcd_s3a01".equals(VodUserAgent.getModelName())) {
//			if (keyCode == 707 || keyCode == 774 || keyCode == 253) {
//				isneedpause = false;
//			}
//		} else if ("lx565ab".equals(VodUserAgent.getModelName())) {
//			if (keyCode == 82 || keyCode == 707 || keyCode == 253) {
//				isneedpause = false;
//			}
//		} else if ("lcd_xxcae5a_b".equals(VodUserAgent.getModelName())) {
//			if (keyCode == 497 || keyCode == 498 || keyCode == 490) {
//				isneedpause = false;
//			}
//		} else {
//			if (keyCode == 223 || keyCode == 499 || keyCode == 480) {
//				isneedpause = false;
//			}
//		}
	}

/*add by dragontec for bug 4065 start*/
	@Override
	public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
		Animation animation = AnimationUtils.loadAnimation(getActivity(), nextAnim);
		if (enter && (nextAnim == R.anim.push_left_in || nextAnim == R.anim.push_right_in)) {
            animation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					synchronized (mAniLock) {
						mDoingFragmentFlipAni = true;
					}
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					synchronized (mAniLock) {
						mDoingFragmentFlipAni = false;
						if (mNeedAddBanner) {
							mNeedAddBanner = false;
							if (mAniFinishRunnable != null) {
								if (mAniHandler != null) {
									mAniHandler.removeCallbacks(mAniFinishRunnable);
								}
							}
							mAniFinishRunnable = new AniFinishRunnable();
							if (mAniHandler != null) {
								mAniHandler.post(mAniFinishRunnable);
							}
						}
					}
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}
			});
		}
		return animation;
	}

	private class AniFinishRunnable implements Runnable {

		@Override
		public void run() {
			synchronized (mAniLock) {
				if (mAniHandler != null) {
					mAniHandler.removeCallbacks(this);
				}
				synchronized (bannerDataLock) {
					initBanner(mGuideBanners);
				}
				mAniFinishRunnable = null;
			}
		}
	}
/*add by dragontec for bug 4065 end*/

/*add by dragontec for bug 4225, 4224, 4223 start*/
    public boolean isScrollerAtTop() {
        float scaleY = 0f;
        if (mLinearContainer != null) {
            scaleY = mLinearContainer.getScrollerCurrentY();
        }
        if (scaleY <= 0.005) {
            return true;
        } else {
            return false;
        }
    }

    public boolean scrollerScrollToTop() {
        if (mLinearContainer != null) {
            return mLinearContainer.scrollerScrollToTop();
        }
        return false;
    }
/*add by dragontec for bug 4225, 4224, 4223 end*/

/*add by dragontec for bug 4200 start*/
	@Override
	public void onScrollFinished() {
/*add by dragontec for bug 4225, 4224, 4223 start*/
		if (mLinearContainer != null) {
			((HomeActivity)getActivity()).actionScrollerMoveToBottom(mLinearContainer.isScrollAtBottom());
		}
/*add by dragontec for bug 4225, 4224, 4223 end*/
		synchronized (templateDataLock) {
			if (mTemplates != null) {
				for (Template template :
						mTemplates) {
					template.checkViewAppear();
				}
			}
		}
	}
/*add by dragontec for bug 4200 end*/

/*add by dragontec for bug 4259 start*/
	public boolean requestFirstBannerFocus() {
		synchronized (templateDataLock) {
			if (mTemplates != null && !mTemplates.isEmpty()) {
				mTemplates.get(0).requestFocus();
				return true;
			}
		}
		return false;
	}
/*add by dragontec for bug 4259 end*/
}
