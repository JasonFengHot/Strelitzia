package tv.ismar.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by huaijie on 3/9/15.
 */
public class AppConstant {


    public static final String KIND = "sky";
    public static final String VERSION = "1.0";
    public static final String MANUFACTURE = "sky";

    public static final String APP_UPDATE_ACTION = "cn.ismartv.vod.action.app_update";

    public static final boolean DEBUG = true;


    public static String purchase_referer = "launcher";
    public static String purchase_page = "launcher";
    public static String purchase_channel;
    public static String purchase_tab;

    public static String purchase_entrance_page = "launcher";
    public static String purchase_entrance_channel;
    public static String purchase_entrance_keyword;
    public static String purchase_entrance_related_item;
    public static String purchase_entrance_related_title;
    public static String purchase_entrance_related_channel;
    public static String purchase_entrance_tab;
    public static String purchase_entrance_location;

    public static class Payment{
        public static final int PAYMENT_REQUEST_CODE = 0xd6;
        public static final int PAYMENT_SUCCESS_CODE = 0x5c;
        public static final int PAYMENT_FAILURE_CODE = 0xd2;
    }
}
