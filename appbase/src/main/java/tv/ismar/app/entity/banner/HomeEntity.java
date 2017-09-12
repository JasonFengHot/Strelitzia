package tv.ismar.app.entity.banner;

import java.util.List;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 大首页banner
 */

public class HomeEntity {
    private int count;
    private int page;
    private int num_pages;
    private String template;
    public List<BannerCarousels> carousels;
    public List<BannerPoster> posters;
}
