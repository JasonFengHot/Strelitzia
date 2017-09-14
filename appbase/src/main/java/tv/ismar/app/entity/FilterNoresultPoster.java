package tv.ismar.app.entity;

/**
 * Created by admin on 2017/9/13.
 */

public class FilterNoresultPoster {


    private String content_model;
    private String poster_url;
    private String title;
    private boolean expense;
    private String url;
    private int corner;
    private String model_name;
    private String vertical_url;
    private int clip_id;
    private int pk;

    public int getBean_score() {
        return bean_score;
    }

    public void setBean_score(int bean_score) {
        this.bean_score = bean_score;
    }

    private int bean_score;
    private String introduction;
    private int order;
    private ExpenseInfoBean expense_info;

    public String getContent_model() {
        return content_model;
    }

    public void setContent_model(String content_model) {
        this.content_model = content_model;
    }

    public String getPoster_url() {
        return poster_url;
    }

    public void setPoster_url(String poster_url) {
        this.poster_url = poster_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isExpense() {
        return expense;
    }

    public void setExpense(boolean expense) {
        this.expense = expense;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getCorner() {
        return corner;
    }

    public void setCorner(int corner) {
        this.corner = corner;
    }

    public String getModel_name() {
        return model_name;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public String getVertical_url() {
        return vertical_url;
    }

    public void setVertical_url(String vertical_url) {
        this.vertical_url = vertical_url;
    }

    public int getClip_id() {
        return clip_id;
    }

    public void setClip_id(int clip_id) {
        this.clip_id = clip_id;
    }

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public ExpenseInfoBean getExpense_info() {
        return expense_info;
    }

    public void setExpense_info(ExpenseInfoBean expense_info) {
        this.expense_info = expense_info;
    }

    public static class ExpenseInfoBean {

        private String duration;
        private String cptitle;
        private String subprice;
        private String cpname;
        private int cpid;
        private String price;
        private int pay_type;

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getCptitle() {
            return cptitle;
        }

        public void setCptitle(String cptitle) {
            this.cptitle = cptitle;
        }

        public String getSubprice() {
            return subprice;
        }

        public void setSubprice(String subprice) {
            this.subprice = subprice;
        }

        public String getCpname() {
            return cpname;
        }

        public void setCpname(String cpname) {
            this.cpname = cpname;
        }

        public int getCpid() {
            return cpid;
        }

        public void setCpid(int cpid) {
            this.cpid = cpid;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public int getPay_type() {
            return pay_type;
        }

        public void setPay_type(int pay_type) {
            this.pay_type = pay_type;
        }
    }


}
