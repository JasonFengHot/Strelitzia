package tv.ismar.app.models;

import java.util.List;

import tv.ismar.app.network.entity.YouHuiDingGouEntity;

/**
 * Created by admin on 2017/3/16.
 */

public class SubjectEntity {


    private int count;
    private String description;
    private String title;
    private boolean is_buy;
    private String bg_url;
    private List<ObjectsBean> objects;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isIs_buy() {
        return is_buy;
    }

    public void setIs_buy(boolean is_buy) {
        this.is_buy = is_buy;
    }

    public String getBg_url() {
        return bg_url;
    }

    public void setBg_url(String bg_url) {
        this.bg_url = bg_url;
    }

    public List<ObjectsBean> getObjects() {
        return objects;
    }

    public void setObjects(List<ObjectsBean> objects) {
        this.objects = objects;
    }

    public static class ObjectsBean {

        private String image;
        private String focus;
        private String content_model;
        private int quality;
        private int rated;
        private String title;
        private String msg2;
        private String adlet_url;
        private String list_url;
        private double bean_score;
        private String poster_url;
        private int pk;
        private String msg1;
        private String description;
        private String item_url;
        private AttributesBean attributes;
        private boolean live_video;
        private String thumb_url;
        private int episode;
        private String url;
        private String caption;
        private String publish_date;
        private boolean is_complex;
        private int position;
        private ExpenseBean expense;
        private int item_pk;
        private String model_name;
        private List<?> tags;

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getFocus() {
            return focus;
        }

        public void setFocus(String focus) {
            this.focus = focus;
        }

        public String getContent_model() {
            return content_model;
        }

        public void setContent_model(String content_model) {
            this.content_model = content_model;
        }

        public int getQuality() {
            return quality;
        }

        public void setQuality(int quality) {
            this.quality = quality;
        }

        public int getRated() {
            return rated;
        }

        public void setRated(int rated) {
            this.rated = rated;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMsg2() {
            return msg2;
        }

        public void setMsg2(String msg2) {
            this.msg2 = msg2;
        }

        public String getAdlet_url() {
            return adlet_url;
        }

        public void setAdlet_url(String adlet_url) {
            this.adlet_url = adlet_url;
        }

        public String getList_url() {
            return list_url;
        }

        public void setList_url(String list_url) {
            this.list_url = list_url;
        }

        public double getBean_score() {
            return bean_score;
        }

        public void setBean_score(double bean_score) {
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

        public String getMsg1() {
            return msg1;
        }

        public void setMsg1(String msg1) {
            this.msg1 = msg1;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getItem_url() {
            return item_url;
        }

        public void setItem_url(String item_url) {
            this.item_url = item_url;
        }

        public AttributesBean getAttributes() {
            return attributes;
        }

        public void setAttributes(AttributesBean attributes) {
            this.attributes = attributes;
        }

        public boolean isLive_video() {
            return live_video;
        }

        public void setLive_video(boolean live_video) {
            this.live_video = live_video;
        }

        public String getThumb_url() {
            return thumb_url;
        }

        public void setThumb_url(String thumb_url) {
            this.thumb_url = thumb_url;
        }

        public int getEpisode() {
            return episode;
        }

        public void setEpisode(int episode) {
            this.episode = episode;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public String getPublish_date() {
            return publish_date;
        }

        public void setPublish_date(String publish_date) {
            this.publish_date = publish_date;
        }

        public boolean isIs_complex() {
            return is_complex;
        }

        public void setIs_complex(boolean is_complex) {
            this.is_complex = is_complex;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public ExpenseBean getExpense() {
            return expense;
        }

        public void setExpense(ExpenseBean expense) {
            this.expense = expense;
        }

        public int getItem_pk() {
            return item_pk;
        }

        public void setItem_pk(int item_pk) {
            this.item_pk = item_pk;
        }

        public String getModel_name() {
            return model_name;
        }

        public void setModel_name(String model_name) {
            this.model_name = model_name;
        }

        public List<?> getTags() {
            return tags;
        }

        public void setTags(List<?> tags) {
            this.tags = tags;
        }

        public static class AttributesBean {

            private String air_date;
            private List<List<String>> director;
            private List<List<String>> genre;
            private List<List<String>> actor;
            private List<String> area;

            public String getAir_date() {
                return air_date;
            }

            public void setAir_date(String air_date) {
                this.air_date = air_date;
            }

            public List<List<String>> getDirector() {
                return director;
            }

            public void setDirector(List<List<String>> director) {
                this.director = director;
            }

            public List<List<String>> getGenre() {
                return genre;
            }

            public void setGenre(List<List<String>> genre) {
                this.genre = genre;
            }

            public List<List<String>> getActor() {
                return actor;
            }

            public void setActor(List<List<String>> actor) {
                this.actor = actor;
            }

            public List<String> getArea() {
                return area;
            }

            public void setArea(List<String> area) {
                this.area = area;
            }
        }

        public static class ExpenseBean {

            public int pay_type;
            public int cpid;
            public double price;
            public String cpname;
            public String duration;
            public double subprice;
            public String cptitle;

        }
    }
}
