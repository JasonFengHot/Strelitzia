package tv.ismar.account.statistics;

import android.os.SystemClock;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import tv.ismar.library.util.C;

/**
 * Created by huibin on 6/9/17.
 */

public class LogDataPackage {
    private long mTime;
    private long mSize;

    private List<LogEntity> mDataPackage;

    public LogDataPackage() {
        mDataPackage = new ArrayList<>();
        mTime = SystemClock.elapsedRealtime();
        mSize = 0;
    }

    public List<LogEntity> getDataPackage() {
        return mDataPackage;
    }

    public void put(LogEntity entity) {
        mDataPackage.add(entity);
        mSize += sizeOf(entity);
    }

    private boolean isTimeOut() {
        return SystemClock.elapsedRealtime() - mTime > 1000 * C.report_log_time_interval;
    }

    private boolean isSizeOut() {
        return mSize > C.report_log_size * 1024;
    }

    public boolean isFull() {
        return (isSizeOut() || isTimeOut()) && !mDataPackage.isEmpty();
    }

    private long sizeOf(Object object) {
        if (object == null)
            return -1;

        // Special output stream use to write the content
        // of an output stream to an internal byte array.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Output stream that can write object
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            // Write object and close the output stream
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            objectOutputStream.close();

            // Get the byte array
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // TODO can the toByteArray() method return a
            // null array ?
            return byteArray == null ? 0 : byteArray.length;
        } catch (IOException e) {
            Log.e("sizeof", e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
}
