package tv.ismar.app.network.entity;

import java.util.List;

/**
 * Created by admin on 2017/4/7.
 */

public class SubjectPayLayerEntity {


    private int pk;
    private int cpid;
    private String cpname;
    private Object pay_type;
    private List<GatherVipListBean> gather_vip_list;
    public boolean gather_per;

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public int getCpid() {
        return cpid;
    }

    public void setCpid(int cpid) {
        this.cpid = cpid;
    }

    public String getCpname() {
        return cpname;
    }

    public void setCpname(String cpname) {
        this.cpname = cpname;
    }

    public Object getPay_type() {
        return pay_type;
    }

    public void setPay_type(Object pay_type) {
        this.pay_type = pay_type;
    }

    public List<GatherVipListBean> getGather_vip_list() {
        return gather_vip_list;
    }

    public void setGather_vip_list(List<GatherVipListBean> gather_vip_list) {
        this.gather_vip_list = gather_vip_list;
    }

    public static class GatherVipListBean {

        private String title;
        private int pk;
        private double price;
        private String duration;
        private String vertical_url;
        private String description;

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

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getVertical_url() {
            return vertical_url;
        }

        public void setVertical_url(String vertical_url) {
            this.vertical_url = vertical_url;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
