package tv.ismar.app.entity.banner;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/14
 * @DESC: 推荐实体
 */

public class BannerRecommend {
    public String content_model;
    public String poster_url;
    public ExpenseInfo expense_info;
    public String title;
    public boolean expense;
    public String url;
    public int corner;
    public String model_name;
    public String vertical_url;
    public int clip_id;
    public String introduction;
    public String bean_score;
    public int order;
    public int pk;
}

class ExpenseInfo {
    public String duration;
    public String cptitle;
    public String subprice;
    public String cpname;
    public int cpid;
    public String price;
    public int pay_type;
}
