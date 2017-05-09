package tv.ismar.library.util;

public class StringUtils {

    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0 || s.toString().equalsIgnoreCase("null");
    }

    public static boolean isSpace(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) {
            return true;
        } else {
            int length;
            if (a != null && b != null && (length = a.length()) == b.length()) {
                if (a instanceof String && b instanceof String) {
                    return a.equals(b);
                } else {
                    for (int i = 0; i < length; ++i) {
                        if (a.charAt(i) != b.charAt(i)) {
                            return false;
                        }
                    }

                    return true;
                }
            } else {
                return false;
            }
        }
    }

    public static int length(CharSequence s) {
        return s == null ? 0 : s.length();
    }

}
