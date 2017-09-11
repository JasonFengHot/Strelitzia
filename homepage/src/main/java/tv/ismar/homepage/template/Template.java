package tv.ismar.homepage.template;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.entity.banner.BannerEntity;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 模版基类
 */

public abstract class Template {
    protected Context mContext;
    protected TextView mTitleCountTv;//标题数量view

    public Template(Context context) {
        this.mContext = context;
    }

    /*在adapter中调用*/
    public Template setView(View view, Bundle bundle) {
        getView(view);
        initListener(view);
        initData(bundle);
        return this;
    }

    /*设置数量view*/
    public Template setTitleCountView(TextView view) {
        mTitleCountTv = view;
        initTitle();
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

    public void initTitle() {
    }

    protected void initListener(View view) {
    }


    public void goToNextPage(View view) {
        String modelName = "item";
        String contentModel = null;
        int itemPk = -1;
        String url = null;

        Object tag = view.getTag();
        if (tag == null){
            return;
        }else {
            if (tag instanceof BannerEntity.PosterBean){
                BannerEntity.PosterBean bean = (BannerEntity.PosterBean) tag;
                contentModel = bean.getContent_model();
                url = bean.getContent_url();
                itemPk = getPostItemId(url);
            }
        }


        Intent intent = new Intent();
//        intent.putExtra("channel", channel);
        if (modelName.contains("item")) {
            if (contentModel.contains("gather")) {
//                PageIntent intent1 = new PageIntent();
//                intent1.toSubject(mContext, contentMode, itemPk, title, BaseActivity.baseChannel, "");
            } else {
                PageIntent pageIntent = new PageIntent();
                pageIntent.toDetailPage(mContext, "homepage", itemPk);
            }
        } else if (modelName.contains("topic")) {
//            intent.putExtra("url", url);
//            intent.setAction("tv.ismar.daisy.Topic");
//            mContext.startActivity(intent);
        } else if (modelName.contains("section")) {
//            intent.putExtra("title", title);
//            intent.putExtra("itemlistUrl", url);
//            intent.putExtra("lableString", title);
//            intent.putExtra("pk", pk);
//            intent.setAction("tv.ismar.daisy.packagelist");
            mContext.startActivity(intent);
        } else if (modelName.contains("package")) {
//            intent.setAction("tv.ismar.daisy.packageitem");
//            intent.putExtra("url", url);
        } else if (modelName.contains("clip")) {
//            PageIntent pageIntent = new PageIntent();
//            pageIntent.toPlayPage(mContext, pk, -1, Source.HOMEPAGE);
        } else if (modelName.contains("ismartv")) {
//            toIsmartvShop(mode_name, app_id, backgroundUrl, nameId, title);
        }else {
            if (contentModel.contains("gather")) {
//                PageIntent intent1 = new PageIntent();
//                intent1.toSubject(mContext, contentMode, itemPk, title, BaseActivity.baseChannel, "");
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

    private void toIsmartvShop(String modename, String app_id, String backgroudUrl, String nameId, String title) {
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
}
