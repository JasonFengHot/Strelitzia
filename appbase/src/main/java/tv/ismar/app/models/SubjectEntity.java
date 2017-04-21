package tv.ismar.app.models;

import java.util.List;

/**
 * Created by admin on 2017/3/16.
 */

public class SubjectEntity {

    private String vertical_url;
    private String description;
    private boolean is_buy;
    private String content_model;
    private String bg_url;
    private String thumb_url;
    private int count;
    private String title;
    private String adlet_url;
    private String list_url;
    private String poster_url;
    private List<ObjectsBean> objects;

    public String getVertical_url() {
        return vertical_url;
    }

    public void setVertical_url(String vertical_url) {
        this.vertical_url = vertical_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIs_buy() {
        return is_buy;
    }

    public void setIs_buy(boolean is_buy) {
        this.is_buy = is_buy;
    }

    public String getContent_model() {
        return content_model;
    }

    public void setContent_model(String content_model) {
        this.content_model = content_model;
    }

    public String getBg_url() {
        return bg_url;
    }

    public void setBg_url(String bg_url) {
        this.bg_url = bg_url;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getPoster_url() {
        return poster_url;
    }

    public void setPoster_url(String poster_url) {
        this.poster_url = poster_url;
    }

    public List<ObjectsBean> getObjects() {
        return objects;
    }

    public void setObjects(List<ObjectsBean> objects) {
        this.objects = objects;
    }

    public static class ObjectsBean {

        private String msg1;
        private String description;
        private String msg2;
        private String image;
        private boolean is_complex;
        private String focus;
        private int rated;
        private String content_model;
        private int pk;
        private String vertical_url;
        private AttributesBean attributes;
        private int quality;
        private int episode;
        private String thumb_url;
        private String publish_date;
        private String item_url;
        private boolean live_video;
        private String title;
        private String url;
        private String adlet_url;
        private String list_url;
        private double bean_score;
        private String caption;
        private String poster_url;
        private int position;
        private ExpenseBean expense;
        private int item_pk;
        private String model_name;
        private List<?> tags;

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

        public String getMsg2() {
            return msg2;
        }

        public void setMsg2(String msg2) {
            this.msg2 = msg2;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public boolean isIs_complex() {
            return is_complex;
        }

        public void setIs_complex(boolean is_complex) {
            this.is_complex = is_complex;
        }

        public String getFocus() {
            return focus;
        }

        public void setFocus(String focus) {
            this.focus = focus;
        }

        public int getRated() {
            return rated;
        }

        public void setRated(int rated) {
            this.rated = rated;
        }

        public String getContent_model() {
            return content_model;
        }

        public void setContent_model(String content_model) {
            this.content_model = content_model;
        }

        public int getPk() {
            return pk;
        }

        public void setPk(int pk) {
            this.pk = pk;
        }

        public String getVertical_url() {
            return vertical_url;
        }

        public void setVertical_url(String vertical_url) {
            this.vertical_url = vertical_url;
        }

        public AttributesBean getAttributes() {
            return attributes;
        }

        public void setAttributes(AttributesBean attributes) {
            this.attributes = attributes;
        }

        public int getQuality() {
            return quality;
        }

        public void setQuality(int quality) {
            this.quality = quality;
        }

        public int getEpisode() {
            return episode;
        }

        public void setEpisode(int episode) {
            this.episode = episode;
        }

        public String getThumb_url() {
            return thumb_url;
        }

        public void setThumb_url(String thumb_url) {
            this.thumb_url = thumb_url;
        }

        public String getPublish_date() {
            return publish_date;
        }

        public void setPublish_date(String publish_date) {
            this.publish_date = publish_date;
        }

        public String getItem_url() {
            return item_url;
        }

        public void setItem_url(String item_url) {
            this.item_url = item_url;
        }

        public boolean isLive_video() {
            return live_video;
        }

        public void setLive_video(boolean live_video) {
            this.live_video = live_video;
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

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public String getPoster_url() {
            return poster_url;
        }

        public void setPoster_url(String poster_url) {
            this.poster_url = poster_url;
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
            private List<List<Object>> director;
            private List<List<Object>> genre;
            private List<List<Object>> actor;
            private List<Object> area;

            public String getAir_date() {
                return air_date;
            }

            public void setAir_date(String air_date) {
                this.air_date = air_date;
            }

            public List<List<Object>> getDirector() {
                return director;
            }

            public void setDirector(List<List<Object>> director) {
                this.director = director;
            }

            public List<List<Object>> getGenre() {
                return genre;
            }

            public void setGenre(List<List<Object>> genre) {
                this.genre = genre;
            }

            public List<List<Object>> getActor() {
                return actor;
            }

            public void setActor(List<List<Object>> actor) {
                this.actor = actor;
            }

            public List<Object> getArea() {
                return area;
            }

            public void setArea(List<Object> area) {
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
