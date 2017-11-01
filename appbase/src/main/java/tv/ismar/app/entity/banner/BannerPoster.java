package tv.ismar.app.entity.banner;

import java.util.Date;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: banner Poster
 */

public class BannerPoster {
    public String poster_url;//海报横图
    public String vertical_url;//海报竖图
    public String title;//标题
	/*modify by dragontec for bug 4325 start*/
    public String introduce;//介绍
	/*modify by dragontec for bug 4325 end*/
    public String content_url;//详情页地址
    public String content_model;
    public int pk;//媒体id
    public int rating_average;//评分
    public String model_name;//表格名称
    public String top_left_corner;//左上角角标
    public String top_right_corner;
    public String nameId;
    public String app_id;
    public String backgroundUrl;
    public String channel;
    public String slug;
    public String image_url;
}
