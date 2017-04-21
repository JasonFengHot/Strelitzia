package tv.ismar.library.util;

import android.text.TextUtils;

public class EmptyUtils {

    public static boolean isEmptyText(String str) {
        return TextUtils.isEmpty(str) || str.equalsIgnoreCase("null");
    }

}
