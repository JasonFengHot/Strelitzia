package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 模版基类
 */

public abstract class Template {
    protected Context mContext;
    protected TextView mTitleCountTv;//标题数量view

    public Template(Context context){
        this.mContext = context;
    }

    /*在adapter中调用*/
    public Template setView(View view, Bundle bundle){
        getView(view);
        initListener(view);
        initData(bundle);
        return this;
    }

    /*设置数量view*/
    public Template setTitleCountView(TextView view){
        initTitle();
        return this;
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

    public void initTitle(){}

    protected void initListener(View view){};
}
