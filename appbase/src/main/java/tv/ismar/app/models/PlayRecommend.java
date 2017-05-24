package tv.ismar.app.models;

import java.util.ArrayList;

/**
 * Created by admin on 2017/5/18.
 */

public class PlayRecommend {

    private String recommend_title;
    private ArrayList<PlayfinishedRecommend.RecommendItem> recommend_items;

    public ArrayList<PlayfinishedRecommend.RecommendItem> getRecommend_items() {
        return recommend_items;
    }

    public void setRecommend_items(ArrayList<PlayfinishedRecommend.RecommendItem> recommend_items) {
        this.recommend_items = recommend_items;
    }

    public String getRecommend_title() {
        return recommend_title;
    }

    public void setRecommend_title(String recommend_title) {
        this.recommend_title = recommend_title;
    }
}
