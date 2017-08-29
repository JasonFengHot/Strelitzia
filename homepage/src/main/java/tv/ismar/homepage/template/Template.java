package tv.ismar.homepage.template;

import android.os.Bundle;
import android.view.View;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 模版基类
 */

public abstract class Template {

    /*在adapter中调用*/
    public void setView(View view, Bundle bundle){
        getView(view, bundle);
    }

    /**
     * 获取view
     * @param view 视图
     * @param bundle 数据
     */
    public abstract void getView(View view, Bundle bundle);
}
