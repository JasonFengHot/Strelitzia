package tv.ismar.library.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

    /**
     * Format date use default pattern (yyyy-MM-dd)
     *
     * @param date
     * @return String
     */
    public static String formatDate(Date date) {
        return formatDate(date, "yyyy-MM-dd");
    }

    /**
     * Format data use pattern yyyy-MM-dd HH:mm:ss.SSS
     *
     * @param date
     * @param datePattern
     * @return String
     */

    public static String formatDate(Date date, String datePattern) {
        String ret = null;
        if (date == null) {
            return "";
        }
        try {
            DateFormat dateFormat = new SimpleDateFormat(datePattern);
            ret = dateFormat.format(date);
        } catch (Exception e) {
        } finally {
            return ret;
        }
    }

    public static Date str2Date(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date str2DateDefaultToday(String dateStr) {
        if (dateStr == null) {
            return new Date();
        }
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static Date nextDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1); //明天
        return calendar.getTime();
    }

    public static Date previousDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1); //昨天
        return calendar.getTime();
    }

    public static Date parseDate(String dateVal, String datePattern) {
        Date dateFormatted = null;
        try {
            DateFormat dateFormat = new SimpleDateFormat(datePattern);
            dateFormatted = dateFormat.parse(dateVal);
        } catch (ParseException e) {
            // return dateFormatted;
        } finally {
            return dateFormatted;
        }
    }

    public static boolean isDateEqual(Date d1, Date d2) {
        String d1Str = formatDate(d1);
        String d2Str = formatDate(d2);
        if (d1Str.equals(d2Str)) {
            return true;
        } else {
            return false;
        }
    }

    public static int daysBetween(String startdate, String enddate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        cal.setTime(sdf.parse(startdate));
        long time1 = cal.getTimeInMillis() + 2000;
        cal.setTime(sdf.parse(enddate));
        long time2 = cal.getTimeInMillis();
        long remain = time2 - time1;
        if (remain <= 0) {
            return -1;
        } else {
            long between_days = (long) Math.floor((time2 - time1) / (1000 * 3600 * 24));
            return Integer.parseInt(String.valueOf(between_days));
        }

    }

    public static String getTime() {
        Calendar c = Calendar.getInstance();
        c.setTime(TrueTime.now());
        c.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH) + 1;
        int d = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);

        String year = null, month = null, day = null;
        String h = null, min = null, sec = null;
        year = String.valueOf(y);
        if (m < 10) {
            month = "0" + m;
        } else {
            month = "" + m;
        }
        if (d < 10) {
            day = "0" + d;
        } else {
            day = "" + d;
        }
        if (hour < 10) {
            h = "0" + hour;
        } else {
            h = "" + hour;
        }
        if (minute < 10) {
            min = "0" + minute;
        } else {
            min = "" + minute;
        }
        if (second < 10) {
            sec = "0" + second;
        } else {
            sec = "" + second;
        }
        String strDate = year + "-" + month + "-" + day + " " + h + ":"
                + min + ":" + sec;
        return strDate;
    }


}
