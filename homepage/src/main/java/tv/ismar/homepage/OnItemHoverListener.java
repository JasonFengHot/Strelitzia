package tv.ismar.homepage;

import android.view.View;

/**
 * @AUTHOR: xi
 * @DATE: 2017/10/9
 * @DESC: 空鼠事件
 */

public interface OnItemHoverListener {
	/*modify by dragontec for bug 4277 start*/
    boolean onHover(View v, int position, boolean hovered);
	/*modify by dragontec for bug 4277 end*/
}
