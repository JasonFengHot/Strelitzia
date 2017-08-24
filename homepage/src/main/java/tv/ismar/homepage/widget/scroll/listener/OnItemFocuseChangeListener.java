package tv.ismar.homepage.widget.scroll.listener;

import android.view.View;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/3
 * @DESC: 焦点变化监听
 */

public interface OnItemFocuseChangeListener {
    void onFocuseChange(View view, int position, boolean hasFocuse);
}
