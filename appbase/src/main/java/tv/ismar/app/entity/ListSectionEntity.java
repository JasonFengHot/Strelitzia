package tv.ismar.app.entity;

import java.util.List;

/**
 * Created by admin on 2017/9/21.
 */

public class ListSectionEntity {


    private int count;
    private int num_pages;
    private List<ObjectsBean> objects;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getNum_pages() {
        return num_pages;
    }

    public void setNum_pages(int num_pages) {
        this.num_pages = num_pages;
    }

    public List<ObjectsBean> getObjects() {
        return objects;
    }

    public void setObjects(List<ObjectsBean> objects) {
        this.objects = objects;
    }

    public static class ObjectsBean {

        private String vertical_url;
        private Object clip_id;
        private String content_model;
        private int corner;
        private String title;
        private String url;
        private String introduction;
        private float bean_score;
        private String poster_url;
        private int pk;
        private boolean expense;
        private String model_name;
        private Object custom_image;
        private ExpenseInfoBean expense_info;

        public String getVertical_url() {
            return vertical_url;
        }

        public void setVertical_url(String vertical_url) {
            this.vertical_url = vertical_url;
        }

        public Object getClip_id() {
            return clip_id;
        }

        public void setClip_id(Object clip_id) {
            this.clip_id = clip_id;
        }

        public String getContent_model() {
            return content_model;
        }

        public void setContent_model(String content_model) {
            this.content_model = content_model;
        }

        public int getCorner() {
            return corner;
        }

        public void setCorner(int corner) {
            this.corner = corner;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getIntroduction() {
            return introduction;
        }

        public void setIntroduction(String introduction) {
            this.introduction = introduction;
        }

        public float getBean_score() {
            return bean_score;
        }

        public void setBean_score(float bean_score) {
            this.bean_score = bean_score;
        }

        public String getPoster_url() {
            return poster_url;
        }

        public void setPoster_url(String poster_url) {
            this.poster_url = poster_url;
        }

        public int getPk() {
            return pk;
        }

        public void setPk(int pk) {
            this.pk = pk;
        }

        public boolean isExpense() {
            return expense;
        }

        public void setExpense(boolean expense) {
            this.expense = expense;
        }

        public String getModel_name() {
            return model_name;
        }

        public void setModel_name(String model_name) {
            this.model_name = model_name;
        }

        public Object getCustom_image() {
            return custom_image;
        }

        public void setCustom_image(Object custom_image) {
            this.custom_image = custom_image;
        }

        public ExpenseInfoBean getExpense_info() {
            return expense_info;
        }

        public void setExpense_info(ExpenseInfoBean expense_info) {
            this.expense_info = expense_info;
        }

        public static class ExpenseInfoBean {

            private int pay_type;
            private int cpid;
            private double price;
            private String cpname;
            private String duration;
            private double subprice;
            private String cptitle;

            public int getPay_type() {
                return pay_type;
            }

            public void setPay_type(int pay_type) {
                this.pay_type = pay_type;
            }

            public int getCpid() {
                return cpid;
            }

            public void setCpid(int cpid) {
                this.cpid = cpid;
            }

            public double getPrice() {
                return price;
            }

            public void setPrice(double price) {
                this.price = price;
            }

            public String getCpname() {
                return cpname;
            }

            public void setCpname(String cpname) {
                this.cpname = cpname;
            }

            public String getDuration() {
                return duration;
            }

            public void setDuration(String duration) {
                this.duration = duration;
            }

            public double getSubprice() {
                return subprice;
            }

            public void setSubprice(double subprice) {
                this.subprice = subprice;
            }

            public String getCptitle() {
                return cptitle;
            }

            public void setCptitle(String cptitle) {
                this.cptitle = cptitle;
            }
        }
    }
}
