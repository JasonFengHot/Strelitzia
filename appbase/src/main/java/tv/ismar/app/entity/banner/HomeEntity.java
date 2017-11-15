package tv.ismar.app.entity.banner;

import java.util.List;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 大首页banner
 */

public class HomeEntity {
	public int count;//返回对象个数
	public String style; //更多返回海报横竖类型（0是竖版，1是横版）
	public String channel_title; //更多返回的频道名称
	public int num_pages;//页码
	public String template;//模板类型
	public boolean is_more;//是否有更多按钮
	public int page;//当前页码
	public String channel; //更多频道标识
	public String section_slug;//更多栏目标识
	public List<BannerCarousels> carousels;//轮播列表
	public List<BannerPoster> posters;//海报列表
	public BigImage bg_image;//首张大图对象
}
