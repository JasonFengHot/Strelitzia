package tv.ismar.library.downloader;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import tv.ismar.library.downloader.model.DownloadTable;
import tv.ismar.library.util.FileUtils;
import tv.ismar.library.injectdb.query.Select;

/**
 * Created by huaijie on 8/25/15.
 */
public class CacheManager {
    private static final String TAG = "LH/CacheManager";

    private static CacheManager instance;
    private ExecutorService executorService;
    Map<String, Future> mFutureMap;

    private CacheManager() {
        // 初始化线程池
        executorService = Executors.newSingleThreadExecutor();
        mFutureMap = new HashMap<>();
    }

    public static CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }

    /**
     * @param url       网络地址
     * @param saveName  保存文件名
     * @param storeType 保存在SD卡还是/data/data/package
     */
    public String doRequest(Context context, String url, String saveName, DownloadClient.StoreType storeType) {
        File downloadFile = null;
        switch (storeType) {
            case Internal:
                downloadFile = context.getFileStreamPath(saveName);
                break;
            case External:
                downloadFile = new File(FileUtils.getSDCardCachePath(), saveName);
                break;
        }

        DownloadTable downloadTable = new Select()
                .from(DownloadTable.class)
                .where(DownloadTable.DOWNLOAD_PATH + " =? ", downloadFile.getAbsolutePath())
                .executeSingle();
        if (downloadTable == null) {
            // 本地数据库为空，首次下载，直接返回网络地址
            addRequestToThreadPool(null, url, downloadFile);
            Log.i(TAG, "first download.");
        } else {
            String serverMD5 = FileUtils.getFileByUrl(url).split("\\.")[0];
            String localMD5 = downloadTable.local_md5;
            String downloadState = downloadTable.download_state;
            if (serverMD5.equalsIgnoreCase(localMD5)) {
                File file = new File(downloadTable.download_path);
                if (file.exists()) {
                    return "file://" + downloadTable.download_path;
                } else {
                    // 本地文件已经被删除，需重新下载
                    resetDownload(downloadTable);
                    addRequestToThreadPool(null, url, downloadFile);
                    Log.i(TAG, "local file deleted.");
                }
            } else if (downloadState.equals(DownloadClient.DownloadState.run.name())) {
                // 当前url正在下载队列中，无需处理
                Log.i(TAG, "current task is running.");
                Future future = mFutureMap.get(url);
                if (future == null || future.isCancelled()) {
                    addRequestToThreadPool(downloadTable, url, downloadFile);
                }
            } else if (downloadState.equals(DownloadClient.DownloadState.pause.name())) {
                // 断点续传
                addRequestToThreadPool(downloadTable, url, downloadFile);
                Log.i(TAG, "last download paused.");
            } else if (downloadState.equals(DownloadClient.DownloadState.complete.name())) {
                // 本地文件md5与服务器文件md5不同，需删除重新下载
                resetDownload(downloadTable);
                downloadFile.delete();
                addRequestToThreadPool(null, url, downloadFile);
                Log.i(TAG, "server file changed.");
            }
        }
        return url;
    }

    public void stopAllRequest() {
        for (Future future : mFutureMap.values()) {
            if (!future.isCancelled()) {
                future.cancel(true);
            }

        }
    }

    // new Delete cannot delete a row data
    private void resetDownload(DownloadTable downloadTable) {
        if (downloadTable == null) {
            return;
        }
        downloadTable.start_position = 0;
        downloadTable.content_length = 0;
        downloadTable.local_md5 = "";
        downloadTable.download_state = DownloadClient.DownloadState.run.name();
        downloadTable.save();
    }

    // 当前下载处于下载队列中
    private void addRequestToThreadPool(DownloadTable downloadTable, String url, File downloadFile) {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadExecutor();
        }
        if (downloadTable == null) {
            downloadTable = new DownloadTable();
            downloadTable.file_name = downloadFile.getName();
            downloadTable.download_path = downloadFile.getAbsolutePath();
            downloadTable.url = url;
            downloadTable.start_position = 0;
            downloadTable.content_length = 0;
            downloadTable.server_md5 = FileUtils.getFileByUrl(url).split("\\.")[0];
            downloadTable.local_md5 = "";
            downloadTable.download_state = DownloadClient.DownloadState.run.name();
            downloadTable.save();
            try {
                if (!downloadFile.exists()) {
                    downloadFile.getParentFile().mkdirs();
                    downloadFile.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            downloadTable.download_state = DownloadClient.DownloadState.run.name();
            downloadTable.save();
        }
        DownloadClient downloadClient = new DownloadClient(url, downloadFile);
        Future future = executorService.submit(downloadClient);
        mFutureMap.put(url, future);
    }

}
