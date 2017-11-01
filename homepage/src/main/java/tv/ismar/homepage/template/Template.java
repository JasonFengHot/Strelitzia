package tv.ismar.homepage.template;

import android.content.Context;
import android.content.Intent;
/*add by dragontec for bug 4200 start*/
import android.graphics.Rect;
/*add by dragontec for bug 4200 end*/
import android.os.Bundle;
	/*add by dragontec for bug 4077 start*/
import android.os.Handler;
	/*add by dragontec for bug 4077 end*/
/*add by dragontec for bug 4332 start*/
import android.support.v7.widget.RecyclerView;
/*add by dragontec for bug 4332 end*/
import android.text.TextUtils;
/*add by dragontec for bug 4332 start*/
import android.view.MotionEvent;
/*add by dragontec for bug 4332 end*/
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

	/*add by dragontec for bug 4077 start*/
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
	/*add by dragontec for bug 4077 end*/
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.entity.banner.BannerCarousels;
import tv.ismar.app.entity.banner.BannerEntity;
	/*add by dragontec for bug 4077 start*/
/*add by dragontec for bug 4338 start*/
import tv.ismar.homepage.R;
/*add by dragontec for bug 4338 end*/
import tv.ismar.homepage.view.BannerLinearLayout;
import tv.ismar.homepage.widget.RecycleLinearLayout;

/*add by dragontec for bug 4332 start*/
import static android.view.MotionEvent.BUTTON_PRIMARY;
/*add by dragontec for bug 4332 end*/
	/*add by dragontec for bug 4077 end*/

/**
 * @AUTHOR: xi @DATE: 2017/8/29 @DESC: 模版基类(只负责模版约束，其他的一律不能加，不能参杂任何业务)
 */
public abstract class Template {
    protected final String TAG = this.getClass().getSimpleName();
    protected Context mContext;
    protected TextView mTitleCountTv; // 标题数量view
	/*add by dragontec for bug 4077 start*/
	protected View mParentView;
	protected Handler handler;
	protected CheckFocusRunnable mCheckFocusRunnable;
	/*add by dragontec for bug 4200 start*/
	protected boolean hasAppeared = false;
	/*add by dragontec for bug 4200 end*/
	/*add by dragontec for bug 4077 end*/
/*add by dragontec for bug 4332 start*/
	protected View mHeadView;
	protected RecyclerViewTV mRecyclerView;
	protected View mHoverView;
	protected View navigationLeft;
	protected View navigationRight;
	private final Object mLock = new Object();
	private boolean checkScrollEnd = false;
/*add by dragontec for bug 4332 end*/

/*add by dragontec for bug 4331 start*/
	public boolean isLastView = false;
/*add by dragontec for bug 4331 end*/

    public Template(Context context) {
        this.mContext = context;
	/*add by dragontec for bug 4077 start*/
		handler = new Handler();
	/*add by dragontec for bug 4077 end*/
    }

    /*在adapter中调用*/
    public Template setView(View view, Bundle bundle) {
	/*add by dragontec for bug 4077 start*/
		mParentView = view;
	/*add by dragontec for bug 4077 end*/
        getView(view);
        initListener(view);
        initData(bundle);
        return this;
    }

    /*设置数量view*/
    public Template setTitleCountView(TextView view) {
        mTitleCountTv = view;
        return this;
    }

    /**
     * 获取view
     *
     * @param view 视图
     */
    public abstract void getView(View view);

    /**
     * 处理数据
     *
     * @param bundle
     */
    public abstract void initData(Bundle bundle);

	/*add by dragontec for bug 4200 start*/
	public abstract void fetchData();
	/*add by dragontec for bug 4200 end*/

    protected void initListener(View view) {
/*add by dragontec for bug 4332 start*/
    	if (mHoverView != null) {
    		mHoverView.setOnHoverListener(new View.OnHoverListener() {
				@Override
				public boolean onHover(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_HOVER_ENTER:
						case MotionEvent.ACTION_HOVER_MOVE:
							checkNavigationButtonVisibility();
							break;
						case MotionEvent.ACTION_HOVER_EXIT:
							/*modify by dragontec for bug 4338 start*/
							//check left button
							if (event.getX() < navigationLeft.getLeft() || event.getX() > navigationLeft.getRight() || event.getY() < navigationLeft.getTop() || event.getY() > navigationRight.getBottom()) {
								//check right button
								if (event.getX() < navigationRight.getLeft() || event.getX() > navigationRight.getRight() || event.getY() < navigationRight.getTop() || event.getY() > navigationRight.getBottom()) {
									if (event.getButtonState() != BUTTON_PRIMARY) {
										dismissNavigationButton();
									}
								}
							}
							/*modify by dragontec for bug 4338 end*/
							break;
					}
					return false;
				}
			});
		}
		if (mRecyclerView != null) {
    		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
					synchronized (mLock) {
						if (checkScrollEnd && newState == RecyclerView.SCROLL_STATE_IDLE) {
							checkScrollEnd = false;
							checkNavigationButtonVisibility();
						}
					}
					super.onScrollStateChanged(recyclerView, newState);
				}

				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					super.onScrolled(recyclerView, dx, dy);
				}
			});
		}
/*add by dragontec for bug 4332 emd*/
    }

    public void goToNextPage(View view) {
        String modelName = "item";
        String contentModel = null;
        int itemPk = -1;
        String url = null;
        String title = null;

        Object tag = view.getTag();
        if (tag == null) {
            return;
        } else {
            if (tag instanceof BannerEntity.PosterBean) {
                BannerEntity.PosterBean bean = (BannerEntity.PosterBean) tag;
                contentModel = bean.getContent_model();
                url = bean.getUrl();
                itemPk = bean.getPk();
                title = bean.getTitle();
            } else if (tag instanceof BannerCarousels) {
                BannerCarousels bean = (BannerCarousels) tag;
                contentModel = bean.getContent_model();
                url = bean.getUrl();
                itemPk = bean.getPk();
                title = bean.getTitle();
            }
        }

        Intent intent = new Intent();
        if (modelName.contains("item")) {
            if (!TextUtils.isEmpty(contentModel) && contentModel.contains("gather")) {
                PageIntent subjectIntent = new PageIntent();
                subjectIntent.toSubject(
                        mContext, contentModel, itemPk, title, BaseActivity.baseChannel, "");
            } else {
                PageIntent pageIntent = new PageIntent();
                pageIntent.toDetailPage(mContext, "homepage", itemPk);
            }
        } else if (modelName.contains("topic")) {
            intent.putExtra("url", url);
            intent.setAction("tv.ismar.daisy.Topic");
            mContext.startActivity(intent);
        } else if (modelName.contains("section")) {
            intent.putExtra("title", title);
            intent.putExtra("itemlistUrl", url);
            intent.putExtra("lableString", title);
            intent.putExtra("pk", itemPk);
            intent.setAction("tv.ismar.daisy.packagelist");
            mContext.startActivity(intent);
        } else if (modelName.contains("package")) {
            intent.setAction("tv.ismar.daisy.packageitem");
            intent.putExtra("url", url);
        } else if (modelName.contains("clip")) {
            PageIntent pageIntent = new PageIntent();
            pageIntent.toPlayPage(mContext, itemPk, -1, Source.HOMEPAGE);
        } else if (modelName.contains("ismartv")) {
            //            toIsmartvShop(mode_name, app_id, backgroundUrl, nameId, title);
        } else {
            if (contentModel.contains("gather")) {
                PageIntent intent1 = new PageIntent();
                intent1.toSubject(mContext, contentModel, itemPk, title, BaseActivity.baseChannel, "");
            } else {
                PageIntent pageIntent = new PageIntent();
                pageIntent.toDetailPage(mContext, "homepage", itemPk);
            }
        }
    }

    int getPostItemId(String url) {
        int id = 0;
        try {
            Pattern p = Pattern.compile("/(\\d+)/?$");
            Matcher m = p.matcher(url);
            if (m.find()) {
                String idStr = m.group(1);
                if (idStr != null) {
                    id = Integer.parseInt(idStr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    private void toIsmartvShop(
            String modename, String app_id, String backgroudUrl, String nameId, String title) {
        Intent appIntent = new Intent();
        try {
            if (modename.equals("ismartvgatherapp")) {
                appIntent.setAction("com.boxmate.tv.subjectdetail");
                appIntent.putExtra("title", title);
                appIntent.putExtra("nameId", nameId);
                appIntent.putExtra("backgroundUrl", backgroudUrl);
            } else if (modename.equals("ismartvhomepageapp")) {
                appIntent.setAction("android.intent.action.mainAty");
                appIntent.putExtra("type", 3);
            } else if (modename.equals("ismartvdetailapp")) {
                appIntent.setAction("com.boxmate.tv.detail");
                appIntent.putExtra("app_id", app_id);
            }
            mContext.startActivity(appIntent);
        } catch (Exception e) {

        }
    }

    public abstract void onCreate();

    public abstract void onStart();

    public abstract void onResume();

	/*modify by dragontec for bug 4077 start*/
    public void onPause() {
		if (mCheckFocusRunnable != null) {
			handler.removeCallbacks(mCheckFocusRunnable);
			mCheckFocusRunnable = null;
		}
	}
	/*modify by dragontec for bug 4077 start*/

    public abstract void onStop();

    public abstract void onDestroy();

	/*add by dragontec for bug 4077 start*/
	protected void checkFocus(RecyclerViewTV recyclerViewTV) {
		if (mCheckFocusRunnable != null) {
			handler.removeCallbacks(mCheckFocusRunnable);
			mCheckFocusRunnable = null;
		}
		mCheckFocusRunnable = new CheckFocusRunnable(recyclerViewTV);
		handler.postDelayed(mCheckFocusRunnable, 200);
	}

    protected void checkFocus(RecyclerViewTV recyclerViewTV, int select) {
        if (mCheckFocusRunnable != null) {
            handler.removeCallbacks(mCheckFocusRunnable);
            mCheckFocusRunnable = null;
        }
        mCheckFocusRunnable = new CheckFocusRunnable(recyclerViewTV);
        mCheckFocusRunnable.setDefaultSelect(select);
        handler.postDelayed(mCheckFocusRunnable, 200);
    }

	protected class CheckFocusRunnable implements Runnable {
	    private int defaultSelect = 0;

		private RecyclerViewTV mRecyclerViewTV;

		CheckFocusRunnable(RecyclerViewTV recyclerViewTV) {
			mRecyclerViewTV = recyclerViewTV;
		}

		public void setDefaultSelect(int select) {
            defaultSelect = select;
        }

		@Override
		public void run() {
			RecycleLinearLayout layout = mParentView == null ? null : (RecycleLinearLayout) mParentView.getParent();
			View focusedChild = null;
			if (layout != null) {
				focusedChild = layout.getFocusedChild();
			}
			if (focusedChild == mParentView) {
				mRecyclerViewTV.setDefaultSelect(defaultSelect);
			}
		}
	}
	/*add by dragontec for bug 4077 end*/

	/*add by dragontec for bug 4200 start*/
	public void checkViewAppear() {
		//等view第一次显示出画面的是以进行数据取得
		if (!hasAppeared && mParentView != null) {
			Rect rect = new Rect();
			if (mParentView.getGlobalVisibleRect(rect)) {
				int screenHeight = mParentView.getResources().getDisplayMetrics().heightPixels;
				if (rect.top < screenHeight) {
					fetchData();
				}
			}
		}
	}
	/*add by dragontec for bug 4200 end*/

    /*add by dragontec for bug 4221 start*/
    /*modify by dragontec for bug 4338 start*/
    protected View findNextUpDownFocus(int focusDirection, ViewGroup mBannerLinearLayout, View focused) {
    	View nextFocus = null;
        if(focusDirection == View.FOCUS_UP){
            int key = (int) mBannerLinearLayout.getTag();
            int tag = (int) mBannerLinearLayout.getTag(key);
//                            boolean canScroll = tag>>30==1;//1可滑动，0不可滑动
            int position = (tag<<2)>>2;
            if(position > 0){
            	View lastView = ((ViewGroup)mBannerLinearLayout.getParent()).getChildAt(position - 1);
                if(lastView != null && lastView instanceof BannerLinearLayout) {
					BannerLinearLayout bannerLinearLayout = (BannerLinearLayout)lastView;
					View headView = bannerLinearLayout.findViewById(R.id.banner_guide_head);
					if (headView != null) {
						Rect rect = new Rect();
						int[] location = new int[2];
						focused.getGlobalVisibleRect(rect);
						int middleX = rect.left + (focused.getWidth() / 2);
						headView.getLocationOnScreen(location);
						if (middleX >= location[0] && middleX <= location[0] + headView.getWidth()) {
							nextFocus = headView;
							return nextFocus;
						}

					}
                    View recycleView = bannerLinearLayout.findViewWithTag("recycleView");
                    if (recycleView != null && recycleView instanceof RecyclerViewTV) {
						/*modify by dragontec for bug 4270 start*/
                        if(((RecyclerViewTV) recycleView).isSelectedItemAtCentered()){
							nextFocus = ((RecyclerViewTV) recycleView).getLastFocusChild();
                        }else{
							Rect rect = new Rect();
							int[] location = new int[2];
							focused.getGlobalVisibleRect(rect);
							int middleX = rect.left + (focused.getWidth() / 2);
							for (int i = ((RecyclerViewTV) recycleView).findLastVisibleItemPosition(); i >= ((RecyclerViewTV) recycleView).findFirstVisibleItemPosition(); i--) {
								RecyclerView.ViewHolder viewHolder = ((RecyclerViewTV) recycleView).findViewHolderForAdapterPosition(i);
								if (viewHolder != null) {
									View item = viewHolder.itemView;
									item.getLocationOnScreen(location);
									if (middleX >= location[0] && middleX <= location[0] + item.getWidth()) {
										nextFocus = item;
										break;
									}
								} else {
									break;
								}
							}
							if (nextFocus == null) {
								nextFocus = recycleView;
							}
                        }
						/*modify by dragontec for bug 4270 end*/
                    }
                }
            }
        }else if(focusDirection == View.FOCUS_DOWN) {
			int key = (int) mBannerLinearLayout.getTag();
			int tag = (int) mBannerLinearLayout.getTag(key);
//                            boolean canScroll = tag>>30==1;//1可滑动，0不可滑动
			int position = (tag << 2) >> 2;
			int count = ((ViewGroup) mBannerLinearLayout.getParent()).getChildCount();
			if (position < count - 1) {
				View nextView = ((ViewGroup)mBannerLinearLayout.getParent()).getChildAt(position + 1);
				if(nextView != null && nextView instanceof BannerLinearLayout) {
					BannerLinearLayout bannerLinearLayout = (BannerLinearLayout)nextView;
					View headView = bannerLinearLayout.findViewById(R.id.banner_guide_head);
					if (headView != null) {
						Rect rect = new Rect();
						int[] location = new int[2];
						focused.getGlobalVisibleRect(rect);
						int middleX = rect.left + (focused.getWidth() / 2);
						headView.getLocationOnScreen(location);
						if (middleX >= location[0] && middleX <= location[0] + headView.getWidth()) {
							nextFocus = headView;
							return nextFocus;
						}

					}
					View recycleView = bannerLinearLayout.findViewWithTag("recycleView");
					if (recycleView != null && recycleView instanceof RecyclerViewTV) {
							/*modify by dragontec for bug 4270 start*/
						if (((RecyclerViewTV) recycleView).isSelectedItemAtCentered()) {
							nextFocus = ((RecyclerViewTV) recycleView).getLastFocusChild();
						} else {
							Rect rect = new Rect();
							int[] location = new int[2];
							focused.getGlobalVisibleRect(rect);
							int middleX = rect.left + (focused.getWidth() / 2);
							for (int i = ((RecyclerViewTV) recycleView).findFirstVisibleItemPosition(); i <= ((RecyclerViewTV) recycleView).findLastVisibleItemPosition(); i++) {
								RecyclerView.ViewHolder viewHolder = ((RecyclerViewTV) recycleView).findViewHolderForAdapterPosition(i);
								if (viewHolder != null) {
									View item = viewHolder.itemView;
									item.getLocationOnScreen(location);
									if (middleX >= location[0] && middleX <= location[0] + item.getWidth()) {
										nextFocus = item;
										break;
									}
								} else {
									break;
								}
							}
							if (nextFocus == null) {
								nextFocus = recycleView;
							}
						}
							/*modify by dragontec for bug 4270 end*/
					}

				}
			}
		}
        return nextFocus;
    }
    /*modify by dragontec for bug 4338 end*/

    protected View findMoreUpFocus(ViewGroup mBannerLinearLayout) {
        int key = (int) mBannerLinearLayout.getTag();
        int tag = (int) mBannerLinearLayout.getTag(key);
//                            boolean canScroll = tag>>30==1;//1可滑动，0不可滑动
        int position = (tag<<2)>>2;
        ViewGroup viewGroup = ((ViewGroup)mBannerLinearLayout.getParent());
        if(position > 0 && viewGroup.getChildCount() > 1){
            BannerLinearLayout bannerLinearLayout = (BannerLinearLayout)viewGroup.getChildAt(viewGroup.getChildCount()- 2);
            if(bannerLinearLayout != null) {
                View recycleView = bannerLinearLayout.findViewWithTag("recycleView");
                if (recycleView != null && recycleView instanceof RecyclerViewTV) {
					/*modify by dragontec for bug 4270 start*/
                    RecyclerViewTV mRecyclerViewTV = (RecyclerViewTV) recycleView;
                    int middle = mBannerLinearLayout.getContext().getResources().getDisplayMetrics().widthPixels/2;
                    View  targetFocus = null;
                    int maxY = 0;
                    for (int i = 0; i < mRecyclerViewTV.getChildCount(); i++) {
                        View view = mRecyclerViewTV.getChildAt(i);
                        Rect rect = new Rect();
                        view.getGlobalVisibleRect(rect);
                        if(rect.left < middle && rect.right> middle){
                            if(rect.top > maxY){
                                maxY = rect.top;
                                targetFocus = view;
                            }
                        }
                    }
                    if(((RecyclerViewTV) recycleView).isSelectedItemAtCentered() || targetFocus == null){
                        targetFocus = ((RecyclerViewTV) recycleView).getLastFocusChild();
                    }
					/*modify by dragontec for bug 4270 end*/
                    return targetFocus;
                }
            }
        }
        return null;
    }
    /*add by dragontec for bug 4221 end*/

/*add by dragontec for bug 4249 start*/
    public void requestFocus() {
        if (mParentView != null) {
            mParentView.requestFocus();
        }
    }
/*add by dragontec for bug 4249 end*/

/*add by dragontec for bug 4332 start*/
	public void checkNavigationButtonVisibility() {
		if (mRecyclerView != null) {
			if (navigationLeft != null) {
				if (mRecyclerView.cannotScrollBackward(-1) && (mHeadView == null || mHeadView.getVisibility() == View.VISIBLE)) {
					navigationLeft.setVisibility(View.INVISIBLE);
				} else {
					navigationLeft.setVisibility(View.VISIBLE);
				}
			}
			if (navigationRight != null) {
				if (mRecyclerView.cannotScrollForward(1)) {
					navigationRight.setVisibility(View.INVISIBLE);
				} else {
					navigationRight.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	public void dismissNavigationButton() {
		if (navigationLeft != null && navigationLeft.getVisibility() == View.VISIBLE) {
			navigationLeft.setVisibility(View.INVISIBLE);
		}
		if (navigationRight != null && navigationRight.getVisibility() == View.VISIBLE) {
			navigationRight.setVisibility(View.INVISIBLE);
		}
	}

	public void setNeedCheckScrollEnd() {
		synchronized (mLock) {
			checkScrollEnd = true;
		}
	}
/*add by dragontec for bug 4332 end*/
/*add by dragontec for bug 4338 start*/
	public View findNearestItemForPosition(View focused, int direction) {
		View item = null;
		Rect rect = new Rect();
		int[] location = new int[2];
		focused.getGlobalVisibleRect(rect);
		int middleX = rect.left + (focused.getWidth() / 2);
		switch (direction) {
			case View.FOCUS_DOWN: {
				if (mHeadView != null) {
					mHeadView.getLocationOnScreen(location);
					if (middleX >= location[0] && middleX <= location[0] + mHeadView.getWidth()) {
						item = mHeadView;
					}
					break;
				}
				if (mRecyclerView != null) {
					for (int i = mRecyclerView.findFirstVisibleItemPosition(); i <= mRecyclerView.findLastVisibleItemPosition(); i++) {
						RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(i);
						if (viewHolder != null) {
							View view = viewHolder.itemView;
							view.getLocationOnScreen(location);
							if (middleX >= location[0] && middleX <= location[0] + view.getWidth()) {
								item = view;
								break;
							}
						} else {
							break;
						}
					}
				}
				break;
			}
			case View.FOCUS_UP: {
				if (mHeadView != null) {
					mHeadView.getLocationOnScreen(location);
					if (middleX >= location[0] && middleX <= location[0] + mHeadView.getWidth()) {
						item = mHeadView;
					}
					break;
				}
				if (mRecyclerView != null) {
					for (int i = mRecyclerView.findLastVisibleItemPosition(); i >= mRecyclerView.findFirstVisibleItemPosition(); i--) {
						RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(i);
						if (viewHolder != null) {
							View view = viewHolder.itemView;
							view.getLocationOnScreen(location);
							if (middleX >= location[0] && middleX <= location[0] + view.getWidth()) {
								item = view;
								break;
							}
						} else {
							break;
						}
					}
					if (item == null) {
						item = mRecyclerView;
					}
				}
				break;
			}
		}

		return item;
	}
/*modify by dragontec for bug 4338 end*/
}
