package tv.ismar.library.exception;

import android.text.format.DateFormat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

import tv.ismar.library.util.FileUtils;

/**
 * Created by LongHai on 17-4-7.
 */

public class CrashUtils implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "LH/CrashUtils";
    private static CrashUtils INSTANCE;
    private String outputDir;
    private Thread.UncaughtExceptionHandler defaultUEH;

    private CrashUtils() {
    }

    public static CrashUtils getInstance() {
        if (INSTANCE == null)
            INSTANCE = new CrashUtils();
        return INSTANCE;
    }

    public void init(File contextFilesDir) {
        //获取系统默认的UncaughtException处理器
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        outputDir = contextFilesDir.getPath() + "/crash";
        if (!FileUtils.isFileExists(outputDir)) {
            FileUtils.createOrExistsDir(outputDir);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        // Inject some info about android version and the device, since google can't provide them in the developer console
        StackTraceElement[] trace = ex.getStackTrace();
        ex.setStackTrace(trace);
        ex.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        Log.e(TAG, stacktrace);

        if (FileUtils.isFileExists(outputDir)) {
            writeLog(stacktrace, outputDir);
        }
        defaultUEH.uncaughtException(thread, ex);
    }

    private void writeLog(String log, String dir) {
        CharSequence timestamp = DateFormat.format("yyyy-MM-dd", System.currentTimeMillis());
        String filename = dir + "/" + timestamp + ".log";

        FileOutputStream stream;
        try {
            stream = new FileOutputStream(filename, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        OutputStreamWriter output = new OutputStreamWriter(stream);
        BufferedWriter bw = new BufferedWriter(output);

        try {
            bw.write(new Date() + "\n");
            bw.write(log);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(bw);
            close(output);
        }
    }

    private boolean close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
