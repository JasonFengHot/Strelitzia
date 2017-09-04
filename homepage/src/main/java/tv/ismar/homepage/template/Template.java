package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 模版基类
 */

public abstract class Template {
    protected Context mContext;

    public Template(Context context){
        this.mContext = context;
    }

    /*在adapter中调用*/
    public void setView(View view, Bundle bundle){
        getView(view);
        initListener();
        initData(bundle);
    }

    /**
     * 获取view
     * @param view 视图
     */
    public abstract void getView(View view);

    /**
     * 处理数据
     * @param bundle
     */
    public abstract void initData(Bundle bundle);

    protected void initListener(){};
}
