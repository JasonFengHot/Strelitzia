package tv.ismar.library.reporter;

import java.util.HashMap;

/**
 * Created by LongHai on 17-4-12.
 */

public class IsmarMedia {

    private int itemPk;
    private int subItemPk;
    private int clipPk;
    private String title;
    private int quality;
    private String channel = "";
    private String source = "";
    private String section = "";
    private HashMap<String, Integer> adIdMap;

    public IsmarMedia(int itemPk, int subItemPk) {
        this.itemPk = itemPk;
        this.subItemPk = subItemPk;
    }

    public int getItemPk() {
        return itemPk;
    }

    public void setItemPk(int itemPk) {
        this.itemPk = itemPk;
    }

    public int getSubItemPk() {
        return subItemPk;
    }

    public void setSubItemPk(int subItemPk) {
        this.subItemPk = subItemPk;
    }

    public int getClipPk() {
        return clipPk;
    }

    public void setClipPk(int clipPk) {
        this.clipPk = clipPk;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public HashMap<String, Integer> getAdIdMap() {
        return adIdMap;
    }

    public void setAdIdMap(HashMap<String, Integer> adIdMap) {
        this.adIdMap = adIdMap;
    }
}
