package tv.ismar.app.entity.banner;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: banner Carousels
 */

public class BannerCarousels {
    public String video_image;//轮播用的视频图片地址
    public String video_url;//轮播用的视频地址
    public String content_model;//媒体类型
    public int pause_time;//图片暂停时间
    public String title;//影片标题
    public String url;//详情页地址
	/*modify by dragontec for bug 卖点文字不正确的问题 start*/
    public String focus;//简介
	/*modify by dragontec for bug 卖点文字不正确的问题 end*/
    public int pk;
    public boolean expense;
    public String model_name;

    public String getVideo_image() {
        return video_image;
    }

    public void setVideo_image(String video_image) {
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
	/*modify by dragontec for bug 卖点文字不正确的问题 start*/
    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }
	/*modify by dragontec for bug 卖点文字不正确的问题 end*/
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
