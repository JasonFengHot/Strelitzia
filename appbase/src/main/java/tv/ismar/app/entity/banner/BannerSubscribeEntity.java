package tv.ismar.app.entity.banner;

import java.util.List;

/**
 * Created by huibin on 25/08/2017.
 */

public class BannerSubscribeEntity {

    /**
     * count : 7
     * carousels : []
     * num_pages : 1
     * poster : [{"title":"奇异博士","introduction":"奇异博士","rating_average":8.9,"content_url":"/api/item/728354/","content_model":"movie","poster_url":"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg"},{"title":"奇异博士","introduction":"奇异博士","rating_average":8.9,"content_url":"/api/item/728354/","content_model":"movie","poster_url":"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg"},{"title":"奇异博士","introduction":"奇异博士","rating_average":8.9,"content_url":"/api/item/728354/","content_model":"movie","poster_url":"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg"},{"title":"奇异博士","introduction":"奇异博士","rating_average":8.9,"content_url":"/api/item/728354/","content_model":"movie","poster_url":"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg"},{"title":"奇异博士","introduction":"奇异博士","rating_average":8.9,"content_url":"/api/item/728354/","content_model":"movie","poster_url":"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg"},{"title":"奇异博士","introduction":"奇异博士","rating_average":8.9,"content_url":"/api/item/728354/","content_model":"movie","poster_url":"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg"},{"title":"奇异博士","introduction":"奇异博士","rating_average":8.9,"content_url":"/api/item/728354/","content_model":"movie","poster_url":"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg"},{"title":"奇异博士","introduction":"奇异博士","rating_average":8.9,"content_url":"/api/item/728354/","content_model":"movie","poster_url":"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg"}]
     * template : template2
     * pk : 2
     */

    private int count;
    private int num_pages;
    private String template;
    private int pk;
    private List<?> carousels;
    private List<PosterBean> poster;

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

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public List<?> getCarousels() {
        return carousels;
    }

    public void setCarousels(List<?> carousels) {
        this.carousels = carousels;
    }

    public List<PosterBean> getPoster() {
        return poster;
    }

    public void setPoster(List<PosterBean> poster) {
        this.poster = poster;
    }

    public static class PosterBean {
        /**
         * title : 奇异博士
         * introduction : 奇异博士
         * rating_average : 8.9
         * content_url : /api/item/728354/
         * content_model : movie
         * poster_url : http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg
         */

        private String title;
        private String introduction;
        private double rating_average;
        private String content_url;
        private String content_model;
        private String poster_url;

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

        public double getRating_average() {
            return rating_average;
        }

        public void setRating_average(double rating_average) {
            this.rating_average = rating_average;
        }

        public String getContent_url() {
            return content_url;
        }

        public void setContent_url(String content_url) {
            this.content_url = content_url;
        }

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
    }
}
