package tv.ismar.app.entity;

import java.io.Serializable;

/**
 * Created by liucan on 2017/8/28.
 */

public class HistoryFavoriteEntity implements Serializable{
    private String detail_url_old;

    private String thumb_url;

    private String content_model;

    private String detail_url;

    private int rating_average;

    private int item_pk;

    private String focus;

    private String description;

    private String caption;

    private int counting_count;

    private int episode;

    private String source;

    private String title;

    private String poster_url_old;

//    private Points points;

    private boolean is_complex;

    private String logo_3d;

    private boolean live_video;

    private String model_name;

    private String logo;

    private HistoryFavoriteEntity[] subitems;

    private int offset;

    private String adlet_url;

    private String poster_url;

    private boolean finished;

    private String classification;

    private int quality;

    private int rating_count;


    private Expense expense;
    private String start_time;

    private String subitem_show;


    private String vertical_url;

    private int pk;

    private boolean is_3d;

    private String publish_date;

    private Clip clip;

    private double bean_score;
    private String url;
    private String date;
    private boolean showDate;
    private int type;  //1表示正常item，2表示更多按钮

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isShowDate() {
        return showDate;
    }

    public void setShowDate(boolean showDate) {
        this.showDate = showDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDetail_url_old(String detail_url_old){
        this.detail_url_old = detail_url_old;
    }
    public String getDetail_url_old(){
        return this.detail_url_old;
    }
    public void setThumb_url(String thumb_url){
        this.thumb_url = thumb_url;
    }
    public String getThumb_url(){
        return this.thumb_url;
    }
    public void setContent_model(String content_model){
        this.content_model = content_model;
    }
    public String getContent_model(){
        return this.content_model;
    }
    public void setDetail_url(String detail_url){
        this.detail_url = detail_url;
    }
    public String getDetail_url(){
        return this.detail_url;
    }
    public void setRating_average(int rating_average){
        this.rating_average = rating_average;
    }
    public int getRating_average(){
        return this.rating_average;
    }
    public void setItem_pk(int item_pk){
        this.item_pk = item_pk;
    }
    public int getItem_pk(){
        return this.item_pk;
    }
    public void setFocus(String focus){
        this.focus = focus;
    }
    public String getFocus(){
        return this.focus;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public String getDescription(){
        return this.description;
    }
    public void setCaption(String caption){
        this.caption = caption;
    }
    public String getCaption(){
        return this.caption;
    }
    public void setCounting_count(int counting_count){
        this.counting_count = counting_count;
    }
    public int getCounting_count(){
        return this.counting_count;
    }
    public void setEpisode(int episode){
        this.episode = episode;
    }
    public int getEpisode(){
        return this.episode;
    }
    public void setSource(String source){
        this.source = source;
    }
    public String getSource(){
        return this.source;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return this.title;
    }
    public void setPoster_url_old(String poster_url_old){
        this.poster_url_old = poster_url_old;
    }
    public String getPoster_url_old(){
        return this.poster_url_old;
    }
//    public void setPoints(Points points){
//        this.points = points;
//    }
//    public Points getPoints(){
//        return this.points;
//    }
    public void setIs_complex(boolean is_complex){
        this.is_complex = is_complex;
    }
    public boolean getIs_complex(){
        return this.is_complex;
    }
    public void setLogo_3d(String logo_3d){
        this.logo_3d = logo_3d;
    }
    public String getLogo_3d(){
        return this.logo_3d;
    }
    public void setLive_video(boolean live_video){
        this.live_video = live_video;
    }
    public boolean getLive_video(){
        return this.live_video;
    }
    public void setModel_name(String model_name){
        this.model_name = model_name;
    }
    public String getModel_name(){
        return this.model_name;
    }
    public void setLogo(String logo){
        this.logo = logo;
    }
    public String getLogo(){
        return this.logo;
    }
    public void setOffset(int offset){
        this.offset = offset;
    }
    public int getOffset(){
        return this.offset;
    }
    public void setAdlet_url(String adlet_url){
        this.adlet_url = adlet_url;
    }
    public String getAdlet_url(){
        return this.adlet_url;
    }
    public void setPoster_url(String poster_url){
        this.poster_url = poster_url;
    }
    public String getPoster_url(){
        return this.poster_url;
    }
    public void setFinished(boolean finished){
        this.finished = finished;
    }
    public boolean getFinished(){
        return this.finished;
    }
    public void setClassification(String classification){
        this.classification = classification;
    }
    public String getClassification(){
        return this.classification;
    }
    public void setQuality(int quality){
        this.quality = quality;
    }
    public int getQuality(){
        return this.quality;
    }
    public void setRating_count(int rating_count){
        this.rating_count = rating_count;
    }
    public int getRating_count(){
        return this.rating_count;
    }
//    public void setTags(Tags tags){
//        this.tags = tags;
//    }
//    public Tags getTags(){
//        return this.tags;
//    }

    public HistoryFavoriteEntity[] getSubitems() {
        return subitems;
    }

    public void setSubitems(HistoryFavoriteEntity[] subitems) {
        this.subitems = subitems;
    }

    public void setStart_time(String start_time){
        this.start_time = start_time;
    }
    public String getStart_time(){
        return this.start_time;
    }
    public void setSubitem_show(String subitem_show){
        this.subitem_show = subitem_show;
    }
    public String getSubitem_show(){
        return this.subitem_show;
    }
    public void setVertical_url(String vertical_url){
        this.vertical_url = vertical_url;
    }
    public String getVertical_url(){
        return this.vertical_url;
    }
    public void setPk(int pk){
        this.pk = pk;
    }
    public int getPk(){
        return this.pk;
    }
    public void setIs_3d(boolean is_3d){
        this.is_3d = is_3d;
    }
    public boolean getIs_3d(){
        return this.is_3d;
    }
    public void setPublish_date(String publish_date){
        this.publish_date = publish_date;
    }
    public String getPublish_date(){
        return this.publish_date;
    }
    public void setClip(Clip clip){
        this.clip = clip;
    }
    public Clip getClip(){
        return this.clip;
    }
    public void setBean_score(double bean_score){
        this.bean_score = bean_score;
    }
    public double getBean_score(){
        return this.bean_score;
    }

}
