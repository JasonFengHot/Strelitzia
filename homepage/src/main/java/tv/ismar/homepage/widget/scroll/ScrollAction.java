package tv.ismar.homepage.widget.scroll;

/**
 * @AUTHOR: xi
 * @DATE: 2017/7/31
 * @DESC: 滑动
 */

public interface ScrollAction {

    /**
     * x轴方向上滑动
     * @param dx
     */
    void scrollX(int dx);

    /**
     * y轴方向上滑动
     * @param dy
     */
    void scrollY(int dy);

    /*横向屏幕居中*/
    void scrollToCenterX();

    /*纵向屏幕居中*/
    void scrollToCenterY();

    /*滑动到顶部*/
    void scrollToTop();
}
