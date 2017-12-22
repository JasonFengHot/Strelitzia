package tv.ismar.app.core.cache;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cn.ismartv.injectdb.library.query.Select;
import tv.ismar.app.VodApplication;
import tv.ismar.app.db.DownloadTable;
import tv.ismar.app.util.FileUtils;
import tv.ismar.app.util.HardwareUtils;

/**
 * Created by huaijie on 8/25/15.
 */
public class CacheManager {
    private static final String TAG = CacheManager.class.getSimpleName();

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

	private static String getFileMD5(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
	}

    /**
     * @param url       网络地址
     * @param saveName  保存文件名
     * @param storeType 保存在SD卡还是/data/data/package
     */
    public String doRequest(String url, String saveName, DownloadClient.StoreType storeType) {
        File downloadFile = null;
        switch (storeType) {
            case Internal:
                downloadFile = VodApplication.getModuleAppContext().getFileStreamPath(saveName);
                break;
            case External:
                downloadFile = new File(HardwareUtils.getSDCardCachePath(), saveName);
                break;
        }

		String serverMD5 = FileUtils.getFileByUrl(url).split("\\.")[0];
		DownloadTable downloadTable = new Select().from(DownloadTable.class).where(DownloadTable.LOCAL_MD5 + " = '" + serverMD5 + "'").executeSingle();
        if (downloadTable == null) {
			// 本地数据库为空，首次下载，直接返回网络地址
			Log.i(TAG, "本地数据库为空，首次下载，直接返回网络地址");
			addRequestToThreadPool(null, url, downloadFile);
		} else {
			String downloadState = downloadTable.download_state;
			if (downloadState.equals(DownloadClient.DownloadState.complete.name())) {
				File file = new File(downloadTable.download_path);
				if (file.exists()) {
					String fileMD5 = getFileMD5(file);
					if (fileMD5 != null && fileMD5.equals(serverMD5)) {
						if (downloadTable.download_path.equals(downloadFile.getAbsolutePath())) {
							Log.i(TAG, "本地文件已存在，直接返回文件地址");
							return "file://" + downloadTable.download_path;
						} else {
							Log.i(TAG, "本地文件已存在，但index不同，rename文件，直接返回文件地址");
							//视频index已变更
							if (downloadFile.exists()) {
								DownloadTable existTable = new Select().from(DownloadTable.class).where(DownloadTable.DOWNLOAD_PATH + "=?" , downloadFile.getAbsolutePath()).executeSingle();
								if (existTable != null) {
									existTable.delete();
								}
								downloadFile.delete();
							}
							file.renameTo(downloadFile.getAbsoluteFile());
							downloadTable.download_path = downloadFile.getAbsolutePath();
							downloadTable.save();
							return "file://" + downloadTable.download_path;
						}
					} else {
						Log.i(TAG, "本地文件已存在，但文件MD5与服务器不一致，重新下载");
						addRequestToThreadPool(null, url, downloadFile);
					}
				} else {
					// 本地文件已经被删除，需重新下载
					Log.i(TAG, "本地文件已经被删除，需重新下载");
					resetDownload(downloadTable);
					addRequestToThreadPool(null, url, downloadFile);
				}
			} else if (downloadState.equals(DownloadClient.DownloadState.run.name())) {
				// 当前url正在下载队列中，无需处理
				Future future = mFutureMap.get(url);
				if (future == null || future.isCancelled()) {
					Log.i(TAG, "当前url正在下载队列中，但已经被取消， 开始断点续传");
					addRequestToThreadPool(downloadTable, url, downloadFile);
				} else {
					Log.i(TAG, "当前url正在下载队列中，无需处理");
				}
			} else if (downloadState.equals(DownloadClient.DownloadState.pause.name())) {
				Log.i(TAG, "断点续传");
				// 断点续传
				addRequestToThreadPool(downloadTable, url, downloadFile);
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

/*add by dragontec for bug 4415 start*/
    public void stopRequest(String url) {
        if (url != null) {
            if (mFutureMap != null) {
                Future future = mFutureMap.get(url);
                if (future != null && !future.isCancelled()) {
                    future.cancel(true);
                }
            }
        }
    }
/*add by dragontec for bug 4415 end*/

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
