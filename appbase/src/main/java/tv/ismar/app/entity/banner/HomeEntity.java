package tv.ismar.app.entity.banner;

import java.util.List;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 大首页banner
 */

public class HomeEntity {
    public String template;
    public int pk;
    public String banner;
    public int count;
    public List<BannerCarousels> carousels;
    public List<BannerPoster> poster;
}
