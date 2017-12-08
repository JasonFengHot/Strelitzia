package tv.ismar.homepage.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/*add by dragontec for bug 4065 start*/
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
/*add by dragontec for bug 4065 end*/

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

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

import static tv.ismar.homepage.widget.RecycleLinearLayout.BANNER_LOAD_AIMING_OFF;

/**
 * @AUTHOR: xi @DATE: 2017/9/8 @DESC: 频道fragemnt
 */
public class ChannelFragment extends BaseFragment implements BaseControl.ControlCallBack
	/*add by dragontec for bug 3983,4077,4200 start*/
		, RecycleLinearLayout.OnDataFinishedListener, RecycleLinearLayout.OnPositionChangedListener, RecycleLinearLayout.OnScrollListener
	/*add by dragontec for bug 3983,4077,4200 end*/
	, Handler.Callback
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
    public static final String BANNER_LOCATION="location";
	public static final int LOAD_BANNERS_COUNT = 4;
	/*add by dragontec for bug 4248,4334 start*/
	private static final int APPEND_LOAD_BANNERS_COUNT = 1;
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
	private final Object mAniLock = new Object();
	private boolean mNeedAddBanner = false;
	private boolean mDoingFragmentFlipAni = false;
	private Handler mAniHandler = null;
	private Handler mHandler = null;
	private AniFinishRunnable mAniFinishRunnable = null;
/*add by dragontec for bug 4065 end*/

	/*add by dragontec for bug 4334 start*/
	private CheckViewAppearRunnable mCheckViewAppearRunnable = null;
	/*add by dragontec for bug 4334 end*/

	private ArrayBlockingQueue<List<String>> mFetchCallBannerQueue = null;

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
		initListener();
        Logger.t(TAG).d("ChannelFragment onCreateView");
        mHandler = new Handler(this);
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
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
		if (mAniFinishRunnable != null) {
			mAniHandler.removeCallbacks(mAniFinishRunnable);
			mAniHandler = null;
		}
		unInitListener();
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
/*add by dragontec for bug 4205 start*/
		/*delete by dragontec for bug 4362 start*/
//        if (mControl != null) {
//			mControl.clear();
//		}
		/*delete by dragontec for bug 4362 send*/
		if (mLinearContainer != null){
			mLinearContainer.resetArrowUp();
			mLinearContainer.resetArrowDown();
			mLinearContainer.setHomeRootRelativeLayout(null);
		}
		homeRootRelativeLayout = null;
/*add by dragontec for bug 4205 end*/
/*add by dragontec for bug 4065 start*/
		if (mLinearContainer != null){
			mLinearContainer.removeAllViews();
		}
/*add by dragontec for bug 4065 end*/
		synchronized (templateDataLock) {
			if (mTemplates != null) {
				for (Template template : mTemplates) {
					template.setCallback(null);
					template.onDestroy();
				}
			}
			if (mTemplates != null) {
				mTemplates.clear();
			}
		}
		/*modify by dragontec for bug 4362 start*/
		if (mControl != null) {
			mControl.clear();
			mControl = null;
		}
		/*modify by dragontec for bug 4362 end*/
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

	/*add by dragontec for bug 4338 start*/
    private void initListener() {
		if (homeRootRelativeLayout != null) {
			homeRootRelativeLayout.setFocusSearchFailedListener(new HomeRootRelativeLayout.FocusSearchFailedListener() {
				@Override
				public View onFocusSearchFailed(View focused, int direction) {
					if (direction == View.FOCUS_DOWN) {
						synchronized (templateDataLock) {
							if (mTemplates != null && mTemplates.size() > 0) {
								/*modify by dragontec for bug 4412 start*/
								int i = 0;
								View view;
								do {
									view = mTemplates.get(i).findNearestItemForPosition(focused, direction);
									i++;
								} while (view == null && i < mTemplates.size());
								return view;
								/*modify by dragontec for bug 4412 end*/
							}
						}
					}
					return null;
				}
			});
		}
		if (mLinearContainer != null) {
			/*add by dragontec for bug 3983 start*/
			mLinearContainer.setOnPositionChangedListener(this);
		/*add by dragontec for bug 3983 end*/
		/*add by dragontec for bug 4077 start*/
			mLinearContainer.setOnDataFinishedListener(this);
		/*add by dragontec for bug 4077 end*/
		/*add by dragontec for bug 4200 start*/
			mLinearContainer.setOnScrollListener(this);
		/*add by dragontec for bug 4200 end*/
			mLinearContainer.setCallback(new RecycleLinearLayout.Callback() {
				@Override
				public boolean focusOnFirstBanner(int position) {
					synchronized (templateDataLock) {
						if (position < mTemplates.size()) {
							Template template = mTemplates.get(position);
							if (template != null) {
								return template.requestFocus();
							}
						}
					}
					return false;
				}
			});
		}
	}

	private void unInitListener() {
		if (mLinearContainer != null) {
			mLinearContainer.setCallback(null);
		/*add by dragontec for bug 4200 start*/
			mLinearContainer.setOnScrollListener(null);
		/*add by dragontec for bug 4200 end*/
			mLinearContainer.setOnPositionChangedListener(null);
			mLinearContainer.setOnDataFinishedListener(null);
		}
		if (homeRootRelativeLayout != null) {
			homeRootRelativeLayout.setFocusSearchFailedListener(null);
		}
	}
	/*add by dragontec for bug 4338 end*/

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
    	/*modify by dragontec for bug 4362 start*/
/*modify by dragontec for bug 4065 start*/
		switch (flags) {
			case BaseControl.FETCH_HOME_BANNERS_FLAG:
			case BaseControl.FETCH_CHANNEL_BANNERS_FLAG: {
				synchronized (mAniLock) {
					GuideBanner[] banners = (GuideBanner[]) args;
			/*modify by dragontec for bug 4178 start*/
					if (banners != null) {
						mLinearContainer.setDataSize(banners.length);
						mFetchCallBannerQueue = new ArrayBlockingQueue<>(banners.length);
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
			}
			break;
			case BaseControl.FETCH_DATA_FAIL_FLAG: {
				synchronized (templateDataLock) {
					for (Template template:
							mTemplates) {
						template.callBack(flags, args);
					}
				}
			}
			break;
			case BaseControl.FETCH_BANNERS_LIST_FLAG:
			case BaseControl.FETCH_M_BANNERS_LIST_FLAG: {
				if (args != null) {
					List<String> list = new ArrayList<>();
					for (Object arg : args) {
						if (arg != null && arg instanceof String) {
							list.add((String) arg);
						}
					}
					if (mFetchCallBannerQueue != null) {
						try {
							Message message = new Message();
							message.what = flags;
							int delay = 0;
//							if (!mFetchCallBannerQueue.isEmpty()) {
//								delay = 500;
//							}
							mFetchCallBannerQueue.put(list);
							if (mHandler != null) {
								mHandler.sendMessageDelayed(message, delay);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			break;
		}

/*modify by dragontec for bug 4065 end*/
	/*modify by dragontec for bug 4362 end*/
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
			/*modify by dragontec for bug 4362 start*/
			String[] bannersStr = new String[size];
			for (int position = 0; position < size; position++) {
				bannersStr[position] = data[position].page_banner_pk;
				addBannerView(position, data[position]);
			}
			mControl.fetchMBanners(bannersStr, 1);
			/*modify by dragontec for bug 4362 end*/
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
		/*delete by dragontec for bug 4334 start*/
//		/*add by dragontec for bug 4200 start*/
//		synchronized (templateDataLock) {
//			for (Template template :
//					mTemplates) {
//				template.fetchData();
//			}
//		}
//		/*add by dragontec for bug 4200 end*/
		/*delete by dragontec for bug 4334 end*/
		//        mLinearContainer.initView();
		mLinearContainer.checkDownButton();
	}

	/*modify by dragontec for bug 4362 start*/
	/*modify by dragontec for bug 4472 start*/
	private void appendBanner(int position) {
		synchronized (layoutLock) {
			if (position > lastLoadedPostion && mHandler != null) {
				Message message = Message.obtain();
				message.what = BaseControl.ADD_BANNER;
				message.arg1 = position;
				mHandler.sendMessage(message);
			}
		}
	}
	/*modify by dragontec for bug 4472 end*/
	/*modify by dragontec for bug 4362 end*/
	private int locationY=2;
	/*modify by dragontec for bug 4472 start*/
	private boolean addBannerView(int position, GuideBanner guideBanner) {
		if (position <= lastLoadedPostion) {
			return false;
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
		/*modify by dragontec for bug 4334 start*/
		/*modify by dragontec for bug 4362 start*/
		if (template.equals("template_guide")) { // 导视
			layoutId = R.layout.banner_guide;
			bannerView = createView(R.layout.banner_guide);
			locationY=locationY+1;
			bundle.putInt(BANNER_LOCATION,locationY);
			templateObject = new TemplateGuide(getContext(), position, mControl).setView(bannerView, bundle);
		} else if (template.equals("template_order")) { // 订阅模版
			layoutId = R.layout.banner_order;
			bannerView = createView(R.layout.banner_order);
			locationY=locationY+1;
			bundle.putInt(BANNER_LOCATION,locationY);
			templateObject = new TemplateOrder(getContext(), position, mControl).setView(bannerView, bundle);
		} else if (template.equals("template_movie")) { // 电影模版
			layoutId = R.layout.banner_movie;
			locationY=locationY+1;
			bundle.putInt(BANNER_LOCATION,locationY);
			bannerView = createView(R.layout.banner_movie);
			templateObject = new TemplateMovie(getContext(), position, mControl).setView(bannerView, bundle);
		} else if (template.equals("template_teleplay")) { // 电视剧模版
			layoutId = R.layout.banner_tv_player;
			locationY=locationY+1;
			bundle.putInt(BANNER_LOCATION,locationY);
			bannerView = createView(R.layout.banner_tv_player);
			templateObject = new TemplateTvPlay(getContext(), position, mControl).setView(bannerView, bundle);
		} else if (template.equals("template_519")) { // 519横图模版
			layoutId = R.layout.banner_519;
			locationY=locationY+1;
			bundle.putInt(BANNER_LOCATION,locationY);
			bannerView = createView(R.layout.banner_519);
			templateObject = new Template519(getContext(), position, mControl).setView(bannerView, bundle);
		} else if (template.equals("template_conlumn")) { // 栏目模版
			layoutId = R.layout.banner_conlumn;
			locationY=locationY+1;
			bundle.putInt(BANNER_LOCATION,locationY);
			bannerView = createView(R.layout.banner_conlumn);
			templateObject = new TemplateConlumn(getContext(), position, mControl).setView(bannerView, bundle);
		} else if (template.equals("template_recommend")) { // 推荐模版
			canScroll = false;
			locationY=locationY+1;
			bundle.putInt(BANNER_LOCATION,locationY);
			layoutId = R.layout.banner_recommend;
			bannerView = createView(R.layout.banner_recommend);
			templateObject = new TemplateRecommend(getContext(), position, mControl).setView(bannerView, bundle);
		} else if (template.equals("template_big_small_ld")) { // 大横小竖模版
			layoutId = R.layout.banner_big_small_ld;
			locationY=locationY+1;
			bundle.putInt(BANNER_LOCATION,locationY);
			bannerView = createView(R.layout.banner_big_small_ld);
			templateObject = new TemplateBigSmallLd(getContext(), position, mControl).setView(bannerView, bundle);
		} else if (template.equals("template_double_md")) { // 竖版双行模版
			layoutId = R.layout.banner_double_md;
			bannerView = createView(R.layout.banner_double_md);
			locationY=locationY+2;
			bundle.putInt(BANNER_LOCATION,locationY);
			templateObject = new TemplateDoubleMd(getContext(), position, mControl).setView(bannerView, bundle);
		} else if (template.equals("template_double_ld")) { // 横版双行模版
			layoutId = R.layout.banner_double_ld;
			locationY=locationY+2;
			bundle.putInt(BANNER_LOCATION,locationY);
			bannerView = createView(R.layout.banner_double_ld);
			templateObject = new TemplateDoubleLd(getContext(), position, mControl).setView(bannerView, bundle);
		} else if (template.equals("template_teleplay_first")) { // 电视剧首行居中模版
			layoutId = R.layout.banner_center;
			locationY=locationY+1;
			bundle.putInt(BANNER_LOCATION,locationY);
			bannerView = createView(R.layout.banner_center);
			templateObject = new TemplateCenter(getContext(), position, mControl).setView(bannerView, bundle);
		}
		/*modify by dragontec for bug 4362 end*/
		/*modify by dragontec for bug 4334 end*/

		if (bannerView != null && templateObject != null) {
			int tag = createTag(position, canScroll);
			bannerView.setTag(layoutId);
			bannerView.setTag(layoutId, tag);
			templateObject.setCallback(new Template.Callback() {
				@Override
				public void scrollToTop(int position) {
					if (mLinearContainer != null) {
						mLinearContainer.scrollToTop(position);
					}
				}
			});
			synchronized (templateDataLock) {
				if (mTemplates != null) {
					/*modify by dragontec for bug 4412 start*/
					templateObject.setVisibility(View.GONE);
					/*modify by dragontec for bug 4412 end*/
					mTemplates.add(templateObject);
				}
			}
			if (mLinearContainer != null) {
				templateObject.setParentScrolling(mLinearContainer.isScrolling());
				mLinearContainer.addView(bannerView);
			}
			return true;
		}
		return false;
	}
	/*modify by dragontec for bug 4472 end*/
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

        /*modify by dragontec for bug 4334 start*/
        Template templateObject = new TemplateMore(getContext(), position).setView(bannerView, bundle);
        /*modify by dragontec for bug 4334 end*/
		synchronized (templateDataLock) {
			if (mTemplates != null) {
				mTemplates.add(templateObject);
			}
		}
		templateObject.setCallback(new Template.Callback() {
			@Override
			public void scrollToTop(int position) {
				if (mLinearContainer != null) {
					mLinearContainer.scrollToTop(position);
				}
			}
		});
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
		if (view == null || mLastFocus == view) {
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
		/*modify by dragontec for bug 4472 start*/
		if (position > last - BANNER_LOAD_AIMING_OFF) {
			/*modify by dragontec for bug 4362 start*/
				/*modify by dragontec for bug 4248 start*/
			int number;
				if (APPEND_LOAD_BANNERS_COUNT > 1) {
					number = position + BANNER_LOAD_AIMING_OFF - last + (APPEND_LOAD_BANNERS_COUNT - 1);
				} else {
					number = position + BANNER_LOAD_AIMING_OFF - last;
				}
				/*modify by dragontec for bug 4248 end*/
//			String[] strings = new String[number];
			GuideBanner[] datas;
			synchronized (bannerDataLock) {
				datas = mGuideBanners;
			}
			if (datas != null) {
				for (int i = 0; i < number; i++) {
					if (last + i + 1 >= datas.length) {
						break;
					}
					appendBanner(last + i + 1);
				}
			}
		}
	}
	/*add by dragontec for bug 4077 end*/

	public void requestFocus() {
		if (mLinearContainer != null && !mLinearContainer.hasFocus()) {
			if (!mLinearContainer.isScrolling()) {
				mLinearContainer.focusOnFirstBanner();
			}
		}
	}

	public void onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "keydown: " + keyCode);
		/*add by dragontec for bug 4338 start*/
//		if (mLinearContainer != null && !mLinearContainer.hasFocus()) {
//			if (!mLinearContainer.isScrolling()) {
//				mLinearContainer.focusOnFirstBanner();
//			}
//		}
		/*add by dragontec for bug 4338 end*/
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

	@Override
	public boolean handleMessage(Message msg) {
		boolean ret = false;
		switch (msg.what) {
			case BaseControl.FETCH_BANNERS_LIST_FLAG:
			case BaseControl.FETCH_M_BANNERS_LIST_FLAG: {
				if (mFetchCallBannerQueue != null && !mFetchCallBannerQueue.isEmpty()) {
					try {
						if (!isDetached()) {
							List<String> list = mFetchCallBannerQueue.take();
							for (String banner :
									list) {
								synchronized (templateDataLock) {
									for (Template template :
											mTemplates) {
										template.onFetchDataFinish(banner);
									}
								}
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				ret = true;
			}
			break;
			case BaseControl.ADD_BANNER: {
				int position = msg.arg1;
				synchronized (bannerDataLock) {
					if (mGuideBanners != null && position < mGuideBanners.length) {
						GuideBanner guideBanner = mGuideBanners[position];
						synchronized (layoutLock) {
							if (addBannerView(position, guideBanner)) {
								if (mControl != null) {
									mControl.fetchBanners(guideBanner.page_banner_pk, 1, false);
								}
								if (lastLoadedPostion == mGuideBanners.length - 1) {
									if (!mChannel.equals("homepage")) {//首页频道最后不添加更多banner
										addMoreView(mGuideBanners.length);
									} else {
										synchronized (templateDataLock) {
											mTemplates.get(lastLoadedPostion).isLastView = true;
										}
									}
								}
								if(mLinearContainer != null && mLinearContainer.getChildCount() > 0) {
									for (int i = 0; i < mLinearContainer.getChildCount(); i++) {
										View itemView = mLinearContainer.getChildAt(i);
										if(i == mLinearContainer.getChildCount() - 1 && !mLinearContainer.hasMore()){
											if(itemView.getPaddingBottom() != getResources().getDimensionPixelSize(R.dimen.banner_bottom_margin)) {
												itemView.setPadding(itemView.getPaddingLeft(), itemView.getPaddingTop(), itemView.getPaddingRight(), getResources().getDimensionPixelSize(R.dimen.banner_bottom_margin));
											}
										}else{
											if(itemView.getPaddingBottom() != 0) {
												itemView.setPadding(itemView.getPaddingLeft(), itemView.getPaddingTop(), itemView.getPaddingRight(), 0);
											}
										}
									}
								}
							}
						}
					}
				}
				ret = true;
			}
			break;
		}
		return ret;
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
/*add by dragontec for bug 4334 start*/
	@Override
	public void onScrollWillStart() {
		if (mCheckViewAppearRunnable != null) {
			mAniHandler.removeCallbacks(mCheckViewAppearRunnable);
			mCheckViewAppearRunnable = null;
		}
		synchronized (templateDataLock) {
			for (Template template :
					mTemplates) {
				template.setParentScrolling(true);
			}
		}
	}
/*add by dragontec for bug 4334 end*/

	@Override
	public void onScrollFinished() {
/*add by dragontec for bug 4225, 4224, 4223 start*/
		if (mLinearContainer != null) {
			onDataFinished(mLinearContainer.getChildAt(mLinearContainer.getCurrentBannerPos()));
			((HomeActivity)getActivity()).actionScrollerMoveToBottom(mLinearContainer.isScrollAtBottom());
		}

/*add by dragontec for bug 4225, 4224, 4223 end*/
		synchronized (templateDataLock) {
			for (Template template :
					mTemplates) {
				template.setParentScrolling(false);
			}
		}
		/*modify by dragontec for bug 4334 start*/
		if (mCheckViewAppearRunnable != null) {
			mAniHandler.removeCallbacks(mCheckViewAppearRunnable);
			mCheckViewAppearRunnable = null;
		}
		mCheckViewAppearRunnable = new CheckViewAppearRunnable();
		mAniHandler.postDelayed(mCheckViewAppearRunnable, 800);
		/*modify by dragontec for bug 4334 end*/
	}

	/*add by dragontec for bug 4334 start*/
	private class CheckViewAppearRunnable implements Runnable {

		@Override
		public void run() {
		/*modify by dragontec for bug 4408 end*/
		/*delete by dragontec for bug 4412 start*/
//			try {
//				synchronized (templateDataLock) {
//					if (mTemplates != null) {
//						if (mLinearContainer != null) {
//							int i = mLinearContainer.getCurrentBannerPos();
//							int[] location = new int[2];
//							View childView = mLinearContainer.getChildAt(i);
//							if(childView != null){
//								childView.getLocationOnScreen(location);
//								while (i != 0 && location[1] > 0) {
//									i--;
//									childView.getLocationOnScreen(location);
//								}
//								for (; i < mTemplates.size(); i++) {
//									if (mLinearContainer != null && mLinearContainer.isScrolling()) {
//										break;
//									}
//									if (!mTemplates.get(i).checkViewAppear()) {
//										break;
//									}
//								}
//							}
//						} else {
//							for (Template template :
//									mTemplates) {
//								if (!template.checkViewAppear()) {
//									break;
//								}
//							}
//						}
//					}
//				}
//				mAniHandler.removeCallbacks(this);
//			}catch (Exception e){
//				e.printStackTrace();
//			}
			/*delete by dragontec for bug 4412 end*/
			/*modify by dragontec for bug 4408 end*/
		}
	}
	/*add by dragontec for bug 4334 end*/
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
	/*add by dragontec for bug 4472 start*/
	public boolean isUpdateQueueEmpty(){
		return !(mFetchCallBannerQueue != null && mFetchCallBannerQueue.size() > 0);
	}
	/*add by dragontec for bug 4472 end*/
}
