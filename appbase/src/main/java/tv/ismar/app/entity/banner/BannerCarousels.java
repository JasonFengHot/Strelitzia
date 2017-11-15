package tv.ismar.app.entity.banner;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: banner Carousels
 */

public class BannerCarousels {
	public String video_image;//视频图片地址
	public String video_url;//视频地址
	public String focus;//焦点文字
	public String content_model;//媒体类型
	public int pause_time;//暂停时间
	public String thumb_image;//缩略图地址
	public String title;//标题
	public String url;//详情地址
	public String top_left_corner;//左上角标
	public String top_right_corner;//右上角标
	public int pk;//ID（关联对象），唯一标识
	public String model_name; //判断点击打开方式（item、section、gather、clip等）
}
