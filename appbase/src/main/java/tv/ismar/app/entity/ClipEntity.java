package tv.ismar.app.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by longhai on 16-9-9.
 */
public class ClipEntity {

    public enum Quality {

        QUALITY_NORMAL(1), QUALITY_MEDIUM(2), QUALITY_HIGH(3), QUALITY_ULTRA(4), QUALITY_BLUERAY(5), QUALITY_4K(6);

        private int value;

        Quality(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Quality getQuality(int value) {
            switch (value) {
                case 1:
                    return QUALITY_NORMAL;
                case 2:
                    return QUALITY_MEDIUM;
                case 3:
                    return QUALITY_HIGH;
                case 4:
                    return QUALITY_ULTRA;
                case 5:
                    return QUALITY_BLUERAY;
                case 6:
                    return QUALITY_4K;
                default:
                    return null;
            }
        }

        public static String getString(Quality type) {
            switch (type) {
                case QUALITY_NORMAL:
                    return "流畅";
                case QUALITY_MEDIUM:
                    return "高清";
                case QUALITY_HIGH:
                    return "超清";
                case QUALITY_ULTRA:
                    return "1080P";
                case QUALITY_BLUERAY:
                    return "蓝光";
                case QUALITY_4K:
                    return "4K";
            }
            return "Error";
        }
    }

    /**
     * 流畅
     */
    private String normal;
    /**
     * 高清
     */
    private String medium;
    /**
     * 超清
     */
    private String high;
    /**
     * 1080P
     */
    private String ultra;
    /**
     * 蓝光
     */
    private String blueray;
    /**
     * 4K
     */
    @SerializedName("4k")
    private String _4k;
    /**
     * 爱奇艺
     */
    private String iqiyi_4_0;
    /**
     * 是否爱奇艺会员
     */
    private boolean is_vip;
    /**
     * 奇艺2.1SDK需要新传入字段
     */
    private String drm;

    /**
     * 编解码格式 (h264/h265)
     */

    private String code_version;

    public String getNormal() {
        return normal;
    }

    public void setNormal(String normal) {
        this.normal = normal;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getUltra() {
        return ultra;
    }

    public String getCode_version() {
        return code_version;
    }

    public void setCode_version(String code_version) {
        this.code_version = code_version;
    }

    public void setUltra(String ultra) {
        this.ultra = ultra;
    }

    public String getBlueray() {
        return blueray;
    }

    public void setBlueray(String blueray) {
        this.blueray = blueray;
    }

    public String get_4k() {
        return _4k;
    }

    public void set_4k(String _4k) {
        this._4k = _4k;
    }

    public String getIqiyi_4_0() {
        return iqiyi_4_0;
    }

    public void setIqiyi_4_0(String iqiyi_4_0) {
        this.iqiyi_4_0 = iqiyi_4_0;
    }

    public boolean is_vip() {
        return is_vip;
    }

    public void setIs_vip(boolean is_vip) {
        this.is_vip = is_vip;
    }

    public String getDrm() {
        return drm;
    }

    public void setDrm(String drm) {
        this.drm = drm;
    }



    @Override
    public String toString() {
        return "normal    : " + normal
                + "\nmedium   : " + medium
                + "\nhigh     : " + high
                + "\nultra    : " + ultra
                + "\nblueray  : " + blueray
                + "\n_4k      : " + _4k
                + "\niqiyi_4_0: " + iqiyi_4_0
                + "\nis_vip   : " + is_vip
                + "\ndir      : " + drm;
    }
}
