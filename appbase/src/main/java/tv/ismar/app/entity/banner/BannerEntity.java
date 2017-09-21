package tv.ismar.app.entity.banner;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

/**
 * Created by huibin on 25/08/2017.
 */

public class BannerEntity {
    private int count;
    private int page;
    private int num_pages;
    private String template;
    private List<CarouselsBean> carousels;
    private List<PosterBean> posters;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount_pages() {
        return num_pages;
    }

    public void setCount_pages(int count_pages) {
        this.num_pages = count_pages;
    }

    public int getNum_pages() {
        return page;
    }

    public void setNum_pages(int num_pages) {
        this.page = num_pages;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public List<CarouselsBean> getCarousels() {
        return carousels;
    }

    public void setCarousels(List<CarouselsBean> carousels) {
        this.carousels = carousels;
    }

    public List<PosterBean> getPoster() {
        return posters;
    }

    public void setPosters(List<PosterBean> posters) {
        this.posters = posters;
    }

    public static class CarouselsBean {
        private Object video_image;
        private String video_url;
        private String content_model;
        private int pause_time;
        private String thumb_image;
        private String title;
        private String url;
        private String introduction;
        private int pk;
        private boolean expense;
        private String model_name;

        public Object getVideo_image() {
            return video_image;
        }

        public void setVideo_image(Object video_image) {
            this.video_image = video_image;
        }

        public String getVideo_url() {
            return video_url;
        }

        public void setVideo_url(String video_url) {
            this.video_url = video_url;
        }

        public String getContent_model() {
            return content_model;
        }

        public void setContent_model(String content_model) {
            this.content_model = content_model;
        }

        public int getPause_time() {
            return pause_time;
        }

        public void setPause_time(int pause_time) {
            this.pause_time = pause_time;
        }

        public String getThumb_image() {
            return thumb_image;
        }

        public void setThumb_image(String thumb_image) {
            this.thumb_image = thumb_image;
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
    }

    public static class PosterBean implements Comparable<PosterBean>{

        private String url;
        private String content_model;
        private String vertical_url;
        private String title;
        private String introduction;
        private int corner;
        private String poster_url;
        private String model_name;
        private int pk;
        private String custom_image;
        private Date order_date;
        private String display_order_date;

        public String getDisplay_order_date() {
            return display_order_date;
        }

        public void setDisplay_order_date(String display_order_date) {
            this.display_order_date = display_order_date;
        }

        public Date getOrder_date() {
            return order_date;
        }

        public void setOrder_date(Date order_date) {
            this.order_date = order_date;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getContent_model() {
            return content_model;
        }

        public void setContent_model(String content_model) {
            this.content_model = content_model;
        }

        public String getVertical_url() {
            return vertical_url;
        }

        public void setVertical_url(String vertical_url) {
            this.vertical_url = vertical_url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getIntroduction() {
            return introduction;
        }

        public void setIntroduction(String introduction) {
            this.introduction = introduction;
        }

        public int getCorner() {
            return corner;
        }

        public void setCorner(int corner) {
            this.corner = corner;
        }

        public String getPoster_url() {
            return poster_url;
        }

        public void setPoster_url(String poster_url) {
            this.poster_url = poster_url;
        }

        public String getModel_name() {
            return model_name;
        }

        public void setModel_name(String model_name) {
            this.model_name = model_name;
        }

        public int getPk() {
            return pk;
        }

        public void setPk(int pk) {
            this.pk = pk;
        }

        public String getCustom_image() {
            return custom_image;
        }

        public void setCustom_image(String custom_image) {
            this.custom_image = custom_image;
        }

        @Override
        public int compareTo(@NonNull PosterBean another) {
            if (this.getOrder_date().before(another.order_date)){
                return -1;
            }else if (this.getOrder_date().after(another.order_date)){
                return  1;
            }else {
                return 0;
            }
        }
    }
}
