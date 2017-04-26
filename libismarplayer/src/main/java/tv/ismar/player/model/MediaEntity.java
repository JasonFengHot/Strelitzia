package tv.ismar.player.model;

import java.util.List;

public class MediaEntity {

    private String title;
    private int pk;
    private int subItemPk;
    private long startPosition;
    private ClipEntity.Quality initQuality;
    private ClipEntity clipEntity;
    private List<AdvEntity> advStreamList;
    private boolean isStreamMedia;

    public MediaEntity(int pk, int subItemPk) {
        this.pk = pk;
        this.subItemPk = subItemPk;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public int getSubItemPk() {
        return subItemPk;
    }

    public void setSubItemPk(int subItemPk) {
        this.subItemPk = subItemPk;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public ClipEntity.Quality getInitQuality() {
        return initQuality;
    }

    public void setInitQuality(ClipEntity.Quality initQuality) {
        this.initQuality = initQuality;
    }

    public ClipEntity getClipEntity() {
        return clipEntity;
    }

    public void setClipEntity(ClipEntity clipEntity) {
        this.clipEntity = clipEntity;
    }

    public List<AdvEntity> getAdvStreamList() {
        return advStreamList;
    }

    public void setAdvStreamList(List<AdvEntity> advStreamList) {
        this.advStreamList = advStreamList;
    }

    public boolean isStreamMedia() {
        return isStreamMedia;
    }

    public void setStreamMedia(boolean streamMedia) {
        isStreamMedia = streamMedia;
    }
}
