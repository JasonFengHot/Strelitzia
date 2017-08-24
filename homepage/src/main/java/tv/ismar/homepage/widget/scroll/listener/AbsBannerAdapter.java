package tv.ismar.homepage.widget.scroll.listener;

import android.content.Context;
import android.view.View;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/3
 * @DESC: 数据源adapter
 */

public abstract class AbsBannerAdapter {

    public AbsBannerAdapter(Context context){}

    /*视图数量*/
    public abstract int getCount();

    /*获取子视图*/
    public abstract Object getItem();

    /*加载布局*/
    public abstract View getView(int position);


}
