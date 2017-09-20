package tv.ismar.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import rx.Observer;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.entity.banner.BannerPoster;
import tv.ismar.app.entity.banner.BigImage;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.library.util.StringUtils;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/24
 * @DESC: 业务基类（抽离出的公共部分可以放在这里）
 */

public class BaseControl {
    private String TAG = this.getClass().getSimpleName();

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
