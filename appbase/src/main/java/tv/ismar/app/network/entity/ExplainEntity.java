package tv.ismar.app.network.entity;

import java.util.ArrayList;

/**
 * Created by liucan on 2017/7/28.
 */

public class ExplainEntity {
    public int code;
    public String msg;
    public ArrayList<String> info;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public ArrayList<String> getInfo() {
        return info;
    }

    public void setInfo(ArrayList<String> info) {
        this.info = info;
    }
}
