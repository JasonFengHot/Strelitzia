package tv.ismar.player.model;

import java.util.List;

import tv.ismar.app.entity.ClipEntity;
import tv.ismar.app.network.entity.AdElementEntity;

public class MediaEntity {

    // 必要属性
    private int pk;
    private int subItemPk;
    private ClipEntity clipEntity;
    private boolean isLivingVideo;
    // 前贴片广告
    private List<AdElementEntity> advStreamList;
    // 历史记录
    private int startPosition;
    private ClipEntity.Quality initQuality;

    public MediaEntity(int pk, int subItemPk, boolean isLivingVideo, ClipEntity clipEntity) {
        this.pk = pk;
        this.subItemPk = subItemPk;
        this.isLivingVideo = isLivingVideo;
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

    public boolean isLivingVideo() {
        return isLivingVideo;
    }

    public List<AdElementEntity> getAdvStreamList() {
        return advStreamList;
    }

    public void setAdvStreamList(List<AdElementEntity> advStreamList) {
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
