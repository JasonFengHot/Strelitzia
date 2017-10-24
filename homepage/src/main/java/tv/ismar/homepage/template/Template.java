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
import android.text.TextUtils;
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
import tv.ismar.homepage.view.BannerLinearLayout;
import tv.ismar.homepage.widget.RecycleLinearLayout;
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

	protected class CheckFocusRunnable implements Runnable {

		private RecyclerViewTV mRecyclerViewTV;

		CheckFocusRunnable(RecyclerViewTV recyclerViewTV) {
			mRecyclerViewTV = recyclerViewTV;
		}

		@Override
		public void run() {
			RecycleLinearLayout layout = mParentView == null ? null : (RecycleLinearLayout) mParentView.getParent();
			View focusedChild = null;
			if (layout != null) {
				focusedChild = layout.getFocusedChild();
			}
			if (focusedChild == mParentView) {
				mRecyclerViewTV.setDefaultSelect(0);
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
    protected View findNextUpDownFocus(int focusDirection, ViewGroup mBannerLinearLayout) {
        if(focusDirection == View.FOCUS_UP){
            int key = (int) mBannerLinearLayout.getTag();
            int tag = (int) mBannerLinearLayout.getTag(key);
//                            boolean canScroll = tag>>30==1;//1可滑动，0不可滑动
            int position = (tag<<2)>>2;
            if(position > 0){
                BannerLinearLayout bannerLinearLayout = (BannerLinearLayout) ((ViewGroup)mBannerLinearLayout.getParent()).getChildAt(position - 1);
                if(bannerLinearLayout != null) {
                    View recycleView = bannerLinearLayout.findViewWithTag("recycleView");
                    if (recycleView != null && recycleView instanceof RecyclerViewTV) {
                        return ((RecyclerViewTV) recycleView).getLastFocusChild();
                    }
                }
            }
        }else if(focusDirection == View.FOCUS_DOWN){
            int key = (int) mBannerLinearLayout.getTag();
            int tag = (int) mBannerLinearLayout.getTag(key);
//                            boolean canScroll = tag>>30==1;//1可滑动，0不可滑动
            int position = (tag<<2)>>2;
            int count = ((ViewGroup)mBannerLinearLayout.getParent()).getChildCount();
            if(position < count -1){
                View view = ((ViewGroup)mBannerLinearLayout.getParent()).getChildAt(position + 1);
                if(view != null && view instanceof BannerLinearLayout){
                    BannerLinearLayout bannerLinearLayout = (BannerLinearLayout) view;
                    if(bannerLinearLayout != null) {
                        View recycleView = bannerLinearLayout.findViewWithTag("recycleView");
                        if (recycleView != null && recycleView instanceof RecyclerViewTV) {
                            return ((RecyclerViewTV) recycleView).getLastFocusChild();
                        }
                    }
                }

            }
        }
        return null;
    }

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
                    View targetFocus = ((RecyclerViewTV) recycleView).getLastFocusChild();
                    if(targetFocus == null && ((RecyclerViewTV) recycleView).getChildCount() > 0){
                        targetFocus = ((RecyclerViewTV) recycleView).getChildAt(0);
                    }
                    return targetFocus;
                }
            }
        }
        return null;
    }
    /*add by dragontec for bug 4221 end*/
}
