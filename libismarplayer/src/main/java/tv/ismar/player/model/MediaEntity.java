package tv.ismar.player.model;

import java.util.List;

public class MediaEntity {

    // 必要属性
    private int pk;
    private int subItemPk;
    private ClipEntity clipEntity;
    // 前贴片广告
    private List<AdvEntity> advStreamList;
    // 历史记录
    private int startPosition;
    private ClipEntity.Quality initQuality;

    public MediaEntity(int pk, int subItemPk, ClipEntity clipEntity) {
        this.pk = pk;
        this.subItemPk = subItemPk;
        this.clipEntity = clipEntity;
    }

    public int getPk() {
        return pk;
    }

    public int getSubItemPk() {
        return subItemPk;
    }

    public ClipEntity getClipEntity() {
        return clipEntity;
    }

    public List<AdvEntity> getAdvStreamList() {
        return advStreamList;
    }

    public void setAdvStreamList(List<AdvEntity> advStreamList) {
        this.advStreamList = advStreamList;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public ClipEntity.Quality getInitQuality() {
        return initQuality;
    }

    public void setInitQuality(ClipEntity.Quality initQuality) {
        this.initQuality = initQuality;
    }
}
