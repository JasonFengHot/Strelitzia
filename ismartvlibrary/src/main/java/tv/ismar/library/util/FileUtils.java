package tv.ismar.library.util;

import android.os.Environment;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by LongHai on 17-4-7.
 */

public class FileUtils {

    public static String getFileByUrl(String httpUrl) {
        try {
            URL url = new URL(httpUrl);
            String file = url.getFile();
            File localFile = new File(file);
            String fileName = localFile.getName();
            if (fileName.contains("?")) {
                int index = fileName.indexOf("?");
                fileName = fileName.substring(0, index);
            }
            return fileName;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getSDCardCachePath() {
        return new File(Environment.getExternalStorageDirectory(), "/Daisy/").getAbsolutePath();
    }

}
