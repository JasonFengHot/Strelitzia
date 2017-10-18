package tv.ismar.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.entity.banner.BannerCarousels;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.BigImage;
import tv.ismar.library.util.StringUtils;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/24
 * @DESC: 业务基类（抽离出的公共部分可以放在这里）
 */

public class BaseControl {
    protected String TAG = this.getClass().getSimpleName();

    public static final int FETCH_CHANNEL_TAB_FLAG = 0X01;//获取频道列表
    public static final int FETCH_HOME_BANNERS_FLAG = 0X02;//获取首页下banners
    public static final int FETCH_CHANNEL_BANNERS_FLAG = 0X03;//获取指定频道下的banners
    public static final int FETCH_BANNERS_LIST_FLAG = 0X04;//获取指定banner下的海报列表
    public static final int FETCH_M_BANNERS_LIST_FLAG = 0X05;//获取影视内容多个banner
    public static final int FETCH_M_BANNERS_LIST_NEXTPAGE_FLAG = 0X06;//获取影视内容多个banner
    public static final int FETCH_HOME_RECOMMEND_LIST_FLAG = 0X07;//推荐列表
    public static final int FETCH_POSTER_CONNERS_FLAG = 0X08;//获取角标
    public static final int TAB_CHANGE_FALG = 0x09;//首页tab变化

    public Context mContext;
    public Activity mActivity;
    public ControlCallBack mCallBack;

    public BaseControl(Context context){
        this.mContext = context;
    }

    public BaseControl(Activity activity){
        this.mActivity = activity;
    }

    public BaseControl(Context context, ControlCallBack callBack){
        this(context);
        setCallBack(callBack);
    }

    public BaseControl(Activity activity, ControlCallBack callBack){
        this(activity);
        setCallBack(callBack);
    }

    public void setCallBack(ControlCallBack callBack){
        this.mCallBack = callBack;
    }

    /*回调控制视图*/
    public interface ControlCallBack {
        void callBack(int flags, Object... args);
    }

    /**
     * 跳转到详情页
     * @param entity
     */
    public void go2Detail(BigImage entity){
        if(entity == null) return;
        go2Detail(entity.pk, entity.model_name, entity.content_model, entity.content_url, entity.title);
    }

    public void go2Detail(BannerPoster entity){
        if(entity == null) return;
        go2Detail(entity.pk, entity.model_name, entity.content_model, entity.content_url, entity.title);
    }

    public void go2Detail(BannerCarousels entity){
        if(entity == null) return;
        go2Detail(entity.pk, entity.model_name, entity.content_model, entity.url, entity.title);
    }

    /**
     * 在原有代码中扒出来的代码，标记不清楚啥意思，暂不注释
     * @param pk
     * @param modelName
     * @param contentModel
     * @param url
     * @param title
     */
    public void go2Detail(int pk, String modelName, String contentModel, String url, String title) {
        if(StringUtils.isEmpty(modelName) || StringUtils.isEmpty(contentModel)
                /*|| StringUtils.isEmpty(url)*/) return;
        Intent intent = new Intent();
        if (modelName.contains("item")) {
            if (contentModel.contains("gather")) {
                PageIntent subjectIntent = new PageIntent();
                subjectIntent.toSubject(mContext, contentModel, pk, title, BaseActivity.baseChannel, "");
            } else {
                PageIntent pageIntent = new PageIntent();
                pageIntent.toDetailPage(mContext, "homepage", pk);
            }
        } else if (modelName.contains("topic")) {
            intent.putExtra("url", url);
            intent.setAction("tv.ismar.daisy.Topic");
            mContext.startActivity(intent);
        } else if (modelName.contains("section")) {
            intent.putExtra("title", title);
            intent.putExtra("itemlistUrl", url);
            intent.putExtra("lableString", title);
            intent.putExtra("pk", pk);
            intent.setAction("tv.ismar.daisy.packagelist");
            mContext.startActivity(intent);
        } else if (modelName.contains("package")) {
            intent.setAction("tv.ismar.daisy.packageitem");
            intent.putExtra("url", url);
        } else if (modelName.contains("clip")) {
            PageIntent pageIntent = new PageIntent();
            pageIntent.toPlayPage(mContext, pk, -1, Source.HOMEPAGE);
        } else if (modelName.contains("ismartv")) {
            Intent appIntent=new Intent();
            try {
                if (modelName.equals("ismartvgatherapp")) {
                    appIntent.setAction("com.boxmate.tv.subjectdetail");
                            appIntent.putExtra("title", title);
                    appIntent.putExtra("nameId", pk);
                    appIntent.putExtra("backgroundUrl", "");
                } else if (modelName.equals("ismartvhomepageapp")) {
                    appIntent.setAction("android.intent.action.mainAty");
                    appIntent.putExtra("type", 3);
                } else if (modelName.equals("ismartvdetailapp")) {
                    appIntent.setAction("com.boxmate.tv .detail");
                            appIntent.putExtra("app_id", pk);
                }
                mContext.startActivity(appIntent);
            }catch (Exception e){

            }
        }else {
            if (contentModel.contains("gather")) {
                PageIntent intent1 = new PageIntent();
                intent1.toSubject(mContext, contentModel, pk, title, BaseActivity.baseChannel, "");
            } else {
                PageIntent pageIntent = new PageIntent();
                pageIntent.toDetailPage(mContext, "homepage", pk);
            }
        }
    }


}
