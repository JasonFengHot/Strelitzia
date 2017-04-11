package tv.ismar.app.network.entity;

import java.util.List;

/**
 * Created by admin on 2017/4/7.
 */

public class SubjectPayLayerEntity {


    private int pk;
    private int cpid;
    private String cpname;
    private String pay_type;
    private List<PayLayerVipEntity.Vip_list> gather_vip_list;
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

    public String getPay_type() {
        return pay_type;
    }

    public void setPay_type(String pay_type) {
        this.pay_type = pay_type;
    }

    public List<PayLayerVipEntity.Vip_list> getGather_vip_list() {
        return gather_vip_list;
    }

    public void setGather_vip_list(List<PayLayerVipEntity.Vip_list> gather_vip_list) {
        this.gather_vip_list = gather_vip_list;
    }
}
