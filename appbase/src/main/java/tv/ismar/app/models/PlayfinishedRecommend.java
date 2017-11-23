package tv.ismar.app.models;


import java.util.ArrayList;

/**
 * Created by admin on 2017/5/18.
 */

public class PlayfinishedRecommend {

    private ArrayList<RecommendItem> list;

    public ArrayList<RecommendItem> getList() {
        return list;
    }

    public void setList(ArrayList<RecommendItem> list) {
        this.list = list;
    }

    public static class RecommendItem{
        private  String vertical_url;
        private  Expense expense_info;
        private  String content_model;
        private  int corner;
        private  String title;
        private  String url;
        private  String introduction;
        private  String poster_url;
        private  int pk;
        private  boolean expense;
        private  String model_name;
        private  String custom_image;
        private  int order;
        private  int clip_id;
        private  float bean_score;

        public int getClip_id() {
            return clip_id;
        }

        public void setClip_id(int clip_id) {
            this.clip_id = clip_id;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }



        public String getVertical_url() {
            return vertical_url;
        }

        public void setVertical_url(String vertical_url) {
            this.vertical_url = vertical_url;
        }

        public Expense getExpense_info() {
            return expense_info;
        }

        public void setExpense_info(Expense expense_info) {
            this.expense_info = expense_info;
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

        public String getCustom_image() {
            return custom_image;
        }

        public void setCustom_image(String custom_image) {
            this.custom_image = custom_image;
        }

        public float getBean_score() { return bean_score; }

        public void setBean_score(float bean_score) { this.bean_score = bean_score; }
    }

}
