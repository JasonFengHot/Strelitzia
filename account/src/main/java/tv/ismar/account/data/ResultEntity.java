package tv.ismar.account.data;


public class ResultEntity {
    private String device_token = "";
    private String domain = "1.1.1.1";
    private String ad_domain = "1.1.1.2";
    private String sn_token = "";
    private String log_domain = "1.1.1.4";
    private String upgrade_domain = "1.1.1.3";
    private String zdevice_token = "";
    private String carnation = "1.1.1.1";
    private int h264_player;
    private int h265_player;
    private int live_player;
    private int is_report_log;
    private int report_log_time_interval;
    private int report_log_size;
    private BestvObject bestv_config;


    public int getReport_log_time_interval() {
        return report_log_time_interval;
    }

    public void setReport_log_time_interval(int report_log_time_interval) {
        this.report_log_time_interval = report_log_time_interval;
    }

    public int getReport_log_size() {
        return report_log_size;
    }

    public void setReport_log_size(int report_log_size) {
        this.report_log_size = report_log_size;
    }

    public int getIs_report_log() {
        return is_report_log;
    }

    public void setIs_report_log(int is_report_log) {
        this.is_report_log = is_report_log;
    }

    private long smart_post_next_request_time = 30;

    public void setLive_player(int live_player) {
        this.live_player = live_player;
    }

    public int getLive_player() {
        return live_player;
    }

    public void setH265_player(int h265_player) {
        this.h265_player = h265_player;
    }
    public int getH265_player() {
        return h265_player;
    }

    public long getSmart_post_next_request_time() {
        return smart_post_next_request_time * 1000;
    }

    public void setSmart_post_next_request_time(long smart_post_next_request_time) {
        this.smart_post_next_request_time = smart_post_next_request_time;
    }

    public String getCarnation() {
        return carnation;
    }

    public void setCarnation(String carnation) {
        this.carnation = carnation;
    }

    public String getZdevice_token() {
        return zdevice_token;
    }

    public void setZdevice_token(String zdevice_token) {
        this.zdevice_token = zdevice_token;
    }

    public String getUpgrade_domain() {
        return upgrade_domain;
    }

    public void setUpgrade_domain(String upgrade_domain) {
        this.upgrade_domain = upgrade_domain;
    }

    public String getSn_Token() {
        return sn_token;
    }

    public void setSn_Token(String sn) {
        this.sn_token = sn;
    }

    public String getLog_Domain() {
        return log_domain;
    }

    public void setLog_Domain(String log) {
        this.log_domain = log;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAd_domain() {
        return ad_domain;
    }

    public void setAd_domain(String ad_domain) {
        this.ad_domain = ad_domain;
    }

    public int getH264_player() {
        return h264_player;
    }

    public void setH264_player(int player) {
        this.h264_player = player;
    }

    public BestvObject getBestv_config() {
        return bestv_config;
    }

    public void setBestv_config(BestvObject bestv_config) {
        this.bestv_config = bestv_config;
    }
}
