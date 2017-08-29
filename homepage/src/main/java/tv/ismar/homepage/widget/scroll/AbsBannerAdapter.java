package tv.ismar.homepage.widget.scroll;

import android.content.Context;
import android.view.View;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/3
 * @DESC: 数据源adapter
 */

public abstract class AbsBannerAdapter {

    /*视图数量*/
    public abstract int getCount();

    /*获取子视图*/
    public abstract Object getItem(int position);

    /*加载布局*/
    public abstract View getView(int position);


}
