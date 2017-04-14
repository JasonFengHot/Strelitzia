package tv.ismar.library.downloader;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import tv.ismar.library.downloader.model.DownloadTable;
import tv.ismar.library.injectdb.query.Select;
import tv.ismar.library.util.FileUtils;
import tv.ismar.library.util.MD5;

/**
 * Created by huaijie on 6/19/15.
 */
public class DownloadClient implements Runnable {
    private static final String TAG = "LH/DownloadClient";

    private OkHttpClient mClient;
    private String mDownloadUrl;
    private File mDownloadFile;

    public DownloadClient(String downloadUrl, File downloadFile) {
        mDownloadUrl = downloadUrl;
        mDownloadFile = downloadFile;

        OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
        okBuilder.connectTimeout(10, TimeUnit.SECONDS);
        okBuilder.readTimeout(10, TimeUnit.SECONDS);
        okBuilder.writeTimeout(10, TimeUnit.SECONDS);
        mClient = okBuilder.build();
    }

    @Override
    public void run() {
        RandomAccessFile randomAccessFile = null;
        InputStream inputStream = null;
        BufferedInputStream bis = null;
        long currentLocation = 0;
        long contentLength = 0;
        boolean downloadComplete = false;
        DownloadTable downloadTable = new Select()
                .from(DownloadTable.class)
                .where(DownloadTable.DOWNLOAD_PATH + " =? ", mDownloadFile.getAbsolutePath())
                .executeSingle();
        if (downloadTable != null) {
            currentLocation = downloadTable.start_position;
            contentLength = downloadTable.content_length;
        }
        Log.d(TAG, "线程_" + mDownloadUrl + "_正在下载【" + "开始位置 : " + currentLocation + " contentLength : " + contentLength + "】");
        if (contentLength != 0 && currentLocation == contentLength) {
            CacheManager.getInstance().mFutureMap.remove(mDownloadUrl);
            saveToDb(downloadTable, true, currentLocation, contentLength);
            return;
        }
        try {
            randomAccessFile = new RandomAccessFile(mDownloadFile, "rwd");
            // 开始下载
            Request request = new Request.Builder().url(mDownloadUrl)
                    .header("RANGE", "bytes=" + currentLocation + "-") // Http value set breakpoints RANGE
                    .build();
            // 文件跳转到指定位置开始写入
            randomAccessFile.seek(currentLocation);
            Response response = mClient.newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                Log.d(TAG, "Download ContentLength : " + responseBody.contentLength());
                if (contentLength <= 0) {
                    contentLength = responseBody.contentLength();
                }
                inputStream = responseBody.byteStream();
                bis = new BufferedInputStream(inputStream);
                byte[] buffer = new byte[2 * 1024];
                int length;
                while ((length = bis.read(buffer)) > 0) {
                    if (Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "+++++ thread stopped +++++");
                        break;
                    }
                    randomAccessFile.write(buffer, 0, length);
                    currentLocation += length;
                    if (currentLocation == contentLength) {
                        downloadComplete = true;
                    }
                }
            } else {
                Log.e(TAG, "ResponseBody null.");
            }
        } catch (IOException e) {
            Log.e(TAG, "Download error : " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (bis != null) {
                close(bis);
            }
            if (inputStream != null) {
                close(inputStream);
            }
            if (randomAccessFile != null) {
                close(randomAccessFile);
            }
        }
        CacheManager.getInstance().mFutureMap.remove(mDownloadUrl);
        saveToDb(downloadTable, downloadComplete, currentLocation, contentLength);
        Log.d(TAG, "downloadTable saved : " +
                "\ndownloadComplete : " + downloadComplete +
                "\ndownload_state   : " + downloadTable.download_state +
                "\ncurrentLocation  : " + currentLocation +
                "\ncontentLength    : " + contentLength +
                "\nlocalMd5         : " + downloadTable.local_md5);
    }

    private void saveToDb(DownloadTable downloadTable, boolean downloadComplete, long currentLocation, long contentLength) {
        if (downloadTable == null) {
            downloadTable = new DownloadTable();
            downloadTable.file_name = mDownloadFile.getName();
            downloadTable.download_path = mDownloadFile.getAbsolutePath();
            downloadTable.url = mDownloadUrl;
            downloadTable.server_md5 = FileUtils.getFileByUrl(mDownloadUrl).split("\\.")[0];
        }
        if (downloadComplete) {
            downloadTable.download_state = DownloadState.complete.name();
            downloadTable.start_position = contentLength;
            downloadTable.content_length = contentLength;
            downloadTable.local_md5 = MD5.getMd5ByFile(mDownloadFile);
            downloadTable.save();
        } else {
            downloadTable.download_state = DownloadState.pause.name();
            downloadTable.start_position = currentLocation;
            downloadTable.content_length = contentLength;
            downloadTable.local_md5 = "";
            downloadTable.save();
        }

    }

    private void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum StoreType {
        Internal,
        External
    }

    enum DownloadState {
        run,
        pause,
        complete
    }

}