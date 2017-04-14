package tv.ismar.library.util;

import java.util.Date;

import cn.ismartv.truetime.TrueTime;

/**
 * Created by LongHai on 17-4-10.
 */

public class DateUtils {

    public static void initTimeZone() {
        System.setProperty("user.timezone", "Asia/Shanghai");
    }

    public static Date getDateToday() {
        return TrueTime.now();
    }

    public static long currentTimeMillis() {
        return TrueTime.now().getTime();
    }

}
