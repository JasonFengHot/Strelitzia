package tv.ismar.app.entity.banner;

import java.util.List;

/**
 * Created by huibin on 25/08/2017.
 */

public class BannerMovieEntity {


    /**
     * count : 7
     * carousels : [{"video_image":"http://res.tvxio.com/media/upload/20170421/upload/20160420/upload/20160711/upload/20160420/upload/20140922/shenghuaweijizhongzhang0505.jpg","introduction":"生化危机：终章","content_model":"movie","video_url":"http://vdata.tvxio.com/topvideo/8301d3a14849a731edcea1a28e627d8d.mp4?sn=oncall","title":"生化危机：终章","pause_time":5,"rating_average":8.7,"content_url":"/api/item/1239319/"},{"video_image":"http://res.tvxio.com/media/upload/20170421/upload/20160420/upload/20140922/cikexintiao0505.jpg","introduction":"刺客信条","content_model":"movie","video_url":"http://vdata.tvxio.com/topvideo/0afb93dfd08d4b05c46625cc3412f375.mp4?sn=oncall","title":"刺客信条","pause_time":5,"rating_average":8,"content_url":"/api/item/1247745/"}]
     * num_pages : 1
     * poster : [{"title":"奇异博士","introduction":"奇异博士","rating_average":8.9,"content_url":"/api/item/728354/","content_model":"movie","poster_url":"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg"},{"title":"奇异博士","introduction":"奇异博士","rating_average":8.9,"content_url":"/api/item/728354/","content_model":"movie","poster_url":"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg"},{"title":"奇异博士","introduction":"奇异博士","rating_average":8.9,"content_url":"/api/item/728354/","content_model":"movie","poster_url":"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg"},{"title":"奇异博士","introduction":"奇异博士","rating_average":8.9,"content_url":"/api/item/728354/","content_model":"movie","poster_url":"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg"},{"title":"奇异博士","introduction":"奇异博士","rating_average":8.9,"content_url":"/api/item/728354/","content_model":"movie","poster_url":"http://res.tvxio.com/media/upload/20160420/qiyiboshi0106y_poster.jpg"}]
     * template : template1
     * pk : 1
     * banner : chinesemovie
     */

    private int count;
    private int num_pages;
    private String template;
    private int pk;
    private String banner;
    private List<CarouselsBean> carousels;
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

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public List<CarouselsBean> getCarousels() {
        return carousels;
    }

    public void setCarousels(List<CarouselsBean> carousels) {
        this.carousels = carousels;
    }

    public List<PosterBean> getPoster() {
        return poster;
    }

    public void setPoster(List<PosterBean> poster) {
        this.poster = poster;
    }

    public static class CarouselsBean {
        /**
         * video_image : http://res.tvxio.com/media/upload/20170421/upload/20160420/upload/20160711/upload/20160420/upload/20140922/shenghuaweijizhongzhang0505.jpg
         * introduction : 生化危机：终章
         * content_model : movie
         * video_url : http://vdata.tvxio.com/topvideo/8301d3a14849a731edcea1a28e627d8d.mp4?sn=oncall
         * title : 生化危机：终章
         * pause_time : 5
         * rating_average : 8.7
         * content_url : /api/item/1239319/
         */

        private String video_image;
        private String introduction;
        private String content_model;
        private String video_url;
        private String title;
        private int pause_time;
        private double rating_average;
        private String content_url;

        public String getVideo_image() {
            return video_image;
        }

        public void setVideo_image(String video_image) {
            this.video_image = video_image;
        }

        public String getIntroduction() {
            return introduction;
        }

        public void setIntroduction(String introduction) {
            this.introduction = introduction;
        }

        public String getContent_model() {
            return content_model;
        }

        public void setContent_model(String content_model) {
            this.content_model = content_model;
        }

        public String getVideo_url() {
            return video_url;
        }

        public void setVideo_url(String video_url) {
            this.video_url = video_url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getPause_time() {
            return pause_time;
        }

        public void setPause_time(int pause_time) {
            this.pause_time = pause_time;
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
