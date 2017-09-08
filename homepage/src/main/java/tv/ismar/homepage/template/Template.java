package tv.ismar.homepage.template;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.util.Utils;

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


    public void onClick(int pk, String contentModel) {
//        String title = null;
//        String mode_name = contentModel;
//        String channel = "homepage";
//        String type;
//        String app_id = "";
//        String backgroundUrl = "";
//        String nameId = "";
//        boolean expense = false;
//        int position = -1;
//        BaseActivity.baseChannel = channel;
//        type = mode_name;
//        Intent intent = new Intent();
//        intent.putExtra("channel", channel);
//        if (mode_name.contains("ismartv")) {
//            toIsmartvShop(mode_name, app_id, backgroundUrl, nameId, title);
//        } else if (contentModel.contains("gather")) {
//            int itemPk = Utils.getItemPk(url);
//            PageIntent intent1 = new PageIntent();
//            intent1.toSubject(mContext, contentModel, itemPk, title, BaseActivity.baseChannel, "");
//        } else if ("item".equals(mode_name)) {
//            pk = SimpleRestClient.getItemId(url, new boolean[1]);
//            PageIntent pageIntent = new PageIntent();
//            pageIntent.toDetailPage(mContext, "homepage", pk);
//        } else if ("topic".equals(mode_name)) {
//            intent.putExtra("url", url);
//            intent.setAction("tv.ismar.daisy.Topic");
//            mContext.startActivity(intent);
//        } else if ("section".equals(mode_name)) {
//            intent.putExtra("title", title);
//            intent.putExtra("itemlistUrl", url);
//            intent.putExtra("lableString", title);
//            intent.putExtra("pk", pk);
//            intent.setAction("tv.ismar.daisy.packagelist");
//            mContext.startActivity(intent);
//        } else if ("package".equals(mode_name)) {
//            intent.setAction("tv.ismar.daisy.packageitem");
//            intent.putExtra("url", url);
//            mContext.startActivity(intent);
//        } else if ("clip".equals(mode_name)) {
//            int itemPk = Utils.getItemPk(url);
//            PageIntent pageIntent = new PageIntent();
//            pageIntent.toPlayPage(mContext, itemPk, -1, Source.HOMEPAGE);
//        }
//        CallaPlay play = new CallaPlay();
//        play.homepage_vod_click(pk, title, channel, position, type);
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
