package tv.ismar.library.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by LongHai on 17-4-10.
 */

public class DateUtils {

    private static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {
        @ Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
}
