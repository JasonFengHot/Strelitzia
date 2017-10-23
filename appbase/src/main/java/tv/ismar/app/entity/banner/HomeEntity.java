package tv.ismar.app.entity.banner;

import java.util.List;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 大首页banner
 */

public class HomeEntity {
    public int count;//总记录数
    public int num_pages;//总页数
    public int page;//当前页数
    public boolean is_more;//是否有更多按钮
    public String template;//banner模版
    public BigImage bg_image;
    public List<BannerCarousels> carousels;//轮播列表
    public List<BannerPoster> posters;//海报列表
    public String section_slug;  //更多跳转栏目名称
    public int style; //跳转的列表页的横竖版标记
    public String channel_title; //section名字
    public String channel; //跳转列表页title
    public String url;
}
