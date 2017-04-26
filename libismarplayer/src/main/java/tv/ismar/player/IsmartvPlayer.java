package tv.ismar.player;

import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import tv.ismar.player.media.DaisyPlayer;
import tv.ismar.player.media.QiyiPlayer;
import tv.ismar.player.model.ClipEntity;
import tv.ismar.player.model.MediaEntity;

/**
 * Created by LongHai on 17-4-26.
 */

public abstract class IsmartvPlayer implements IPlayer {

    private static final String TAG = "LH/IsmartvPlayer";
    private MediaEntity mediaEntity;
    protected ClipEntity.Quality mCurrentQuality;
    protected List<ClipEntity.Quality> mQualities;

    // 视云片源所需变量
    private String deviceToken;
    protected boolean isPlayingBestvAd;
    protected int[] mBestvAdTime;


    // 奇艺片源所需变量
    private String snToken;
    private String modelName;
    private String versionCode;
    private String qiyiUserType;// Qiyi login
    private String zDeviceToken;
    private String zUserToken;

    public void setMediaEntity(MediaEntity mediaEntity) {
        this.mediaEntity = mediaEntity;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public void setSnToken(String snToken) {
        this.snToken = snToken;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public void setQiyiUserType(String qiyiUserType) {
        this.qiyiUserType = qiyiUserType;
    }

    public void setzDeviceToken(String zDeviceToken) {
        this.zDeviceToken = zDeviceToken;
    }

    public void setzUserToken(String zUserToken) {
        this.zUserToken = zUserToken;
    }

    public static class Builder {

        // 视云片源
        public static final byte MODE_SMART_PLAYER = 0x01;
        // 奇艺片源
        public static final byte MODE_QIYI_PLAYER = 0x02;
        private byte playerMode = -1;
        private MediaEntity mediaEntity;

        // 视云片源所需变量
        private String deviceToken;

        // 奇艺片源所需变量
        private String snToken;
        private String modelName;
        private String versionCode;
        private String qiyiUserType;// Qiyi login
        private String zDeviceToken;
        private String zUserToken;

        // TODO

        public Builder setPlayerMode(byte playerMode) {
            this.playerMode = playerMode;
            return this;
        }

        public Builder setMediaEntity(MediaEntity mediaEntity) {
            this.mediaEntity = mediaEntity;
            return this;
        }

        public Builder setDeviceToken(String deviceToken) {
            this.deviceToken = deviceToken;
            return this;
        }

        public Builder setSnToken(String snToken) {
            this.snToken = snToken;
            return this;
        }

        public Builder setModelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder setVersionCode(String versionCode) {
            this.versionCode = versionCode;
            return this;
        }

        public Builder setQiyiUserType(String qiyiUserType) {
            this.qiyiUserType = qiyiUserType;
            return this;
        }

        public Builder setzDeviceToken(String zDeviceToken) {
            this.zDeviceToken = zDeviceToken;
            return this;
        }

        public Builder setzUserToken(String zUserToken) {
            this.zUserToken = zUserToken;
            return this;
        }

        public IsmartvPlayer build() {
            if (playerMode <= 0) {
                throw new IllegalAccessError("Must call setPlayerMode first.");
            }
            if (mediaEntity == null) {
                throw new IllegalAccessError("Must call setMediaEntity first.");
            }
            IsmartvPlayer tvPlayer = null;
            switch (playerMode) {
                case MODE_SMART_PLAYER:
                    if (TextUtils.isEmpty(deviceToken)) {
                        throw new IllegalAccessError("Must set deviceToken variable first.");
                    }
                    tvPlayer = new DaisyPlayer();
                    tvPlayer.setDeviceToken(deviceToken);
                    break;
                case MODE_QIYI_PLAYER:
                    if (TextUtils.isEmpty(snToken) || TextUtils.isEmpty(modelName)
                            || TextUtils.isEmpty(versionCode) || TextUtils.isEmpty(qiyiUserType)
                            || TextUtils.isEmpty(zDeviceToken) || TextUtils.isEmpty(zUserToken)) {
                        throw new IllegalAccessError("Must set qiyi variable first.");
                    }
                    tvPlayer = new QiyiPlayer();
                    tvPlayer.setSnToken(snToken);
                    tvPlayer.setModelName(modelName);
                    tvPlayer.setVersionCode(versionCode);
                    tvPlayer.setQiyiUserType(qiyiUserType);
                    tvPlayer.setzDeviceToken(zDeviceToken);
                    tvPlayer.setzUserToken(zUserToken);
                    break;
            }
            if (tvPlayer == null) {
                throw new IllegalAccessError("Not support player mode.");
            }
            tvPlayer.setMediaEntity(mediaEntity);
            Log.d(TAG, "New Player Success : " + playerMode);
            return tvPlayer;
        }

    }
}
