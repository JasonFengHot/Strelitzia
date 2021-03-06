package tv.ismar.app.ad;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import cn.ismartv.injectdb.library.query.Delete;
import cn.ismartv.injectdb.library.query.Select;
import cn.ismartv.truetime.TrueTime;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tv.ismar.app.db.AdvertiseTable;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.util.FileUtils;
import tv.ismar.library.exception.ExceptionUtils;
import tv.ismar.library.network.UserAgentInterceptor;

public class AdvertiseManager {
    private static final String TAG = "LH/AdvertisementManager";

    public static final String DEFAULT_ADV_PICTURE = "file:///android_asset/posters.png";
    private static final String LAUNCH_APP_ADVERTISEMENT = "launch_app";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_VIDEO = "video";
    public static final String AD_DIR = "ad";
    public static final String BOOT_ADV_DOWNLOAD_EXCEPTION_CODE = "801";
    public static final String BOOT_ADV_DOWNLOAD_EXCEPTION_STRING = "获取广告物料失败";
    public static final String BOOT_ADV_PLAY_EXCEPTION_CODE = "802";
    public static final String BOOT_ADV_PLAY_EXCEPTION_STRING = "展示广告失败";

    private DateFormat dateFormat;
    private Context mContext;
    private OkHttpClient mOkHttpClient;
    private FileUtils fileUtils = new FileUtils();

    public AdvertiseManager(Context context) {
        mContext = context;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        if (!fileUtils.isFileExist(context.getFilesDir() + "/" + AdvertiseManager.AD_DIR)) {
            try {
                fileUtils.createDir(context.getFilesDir() + "/" + AdvertiseManager.AD_DIR);
            } catch (IOException e) {
                ExceptionUtils.sendProgramError(e);
                e.printStackTrace();
            }
        }
    }

    public List<AdvertiseTable> getAppLaunchAdvertisement() {
        Date todayDate = TrueTime.now();
        long todayDateTime = todayDate.getTime();
        List<AdvertiseTable> advertisementTables = new Select().from(AdvertiseTable.class)
                .where(AdvertiseTable.START_DATE + " < ?", todayDateTime)
                .where(AdvertiseTable.END_DATE + " > ?", todayDateTime)
                .execute();
        if (advertisementTables == null || advertisementTables.isEmpty()) {
            Log.i(TAG, "advertisementTables null");
            advertisementTables = new ArrayList<>();
            AdvertiseTable advTable = new AdvertiseTable();
            advTable.duration = 5;
            advTable.media_type = TYPE_IMAGE;
            advTable.location = DEFAULT_ADV_PICTURE;
            advertisementTables.add(advTable);
        } else {
            for (AdvertiseTable advTable : advertisementTables) {
                String location = advTable.location;
                // 判断/data/data/tv.ismar.daisy/ad 目录下的文件是否被测试人员删除
                File file = new File(mContext.getFilesDir() + "/" + AD_DIR + "/" + location);
                if(!file.exists()){
                    advTable.delete();
                    AdvertiseTable tepAdvTable = new AdvertiseTable();
                    tepAdvTable.duration = 5;
                    tepAdvTable.media_type = TYPE_IMAGE;
                    tepAdvTable.location = DEFAULT_ADV_PICTURE;
                    advertisementTables.add(tepAdvTable);
                    break;
                }
            }
        }
        return advertisementTables;
    }

    public void updateAppLaunchAdvertisement(List<AdElementEntity> adElementEntityList) {
        for (AdElementEntity adElementEntity : adElementEntityList) {
            String downlaodUrl = adElementEntity.getMedia_url();
            Log.i(TAG, "downlaodUrl:" + downlaodUrl);
            String filePath = mContext.getFilesDir() + "/" + AD_DIR + "/" + FileUtils.getFileByUrl(adElementEntity.getMedia_url());
            File localFile = new File(filePath);
            if (!localFile.exists()) {
                new CallaPlay().bootAdvDownload(
                        adElementEntity.getTitle(),
                        String.valueOf(adElementEntity.getMedia_id()),
                        adElementEntity.getMedia_url()
                );
                downLoadFile(downlaodUrl, filePath, adElementEntity);
            } else {
                String md5Code = adElementEntity.getMd5();
                if (!md5Code.equalsIgnoreCase(FileUtils.getMd5ByFile(localFile))) {
                    localFile.delete();
                    new CallaPlay().bootAdvDownload(
                            adElementEntity.getTitle(),
                            String.valueOf(adElementEntity.getMedia_id()),
                            adElementEntity.getMedia_url()
                    );
                    downLoadFile(downlaodUrl, filePath, adElementEntity);
                }
            }

        }
    }

    private void saveToDb(AdElementEntity adElementEntity) {
        AdvertiseTable advertiseTable = new AdvertiseTable();
        advertiseTable.title = adElementEntity.getTitle();
        try {
            String start_date = adElementEntity.getStart_date() + " " + adElementEntity.getStart_time();
            String end_date = adElementEntity.getEnd_date() + " " + adElementEntity.getEnd_time();
            advertiseTable.start_date = dateFormat.parse(start_date).getTime();
            advertiseTable.end_date = dateFormat.parse(end_date).getTime();
//            advertiseTable.everyday_time_from = timeFormat.parse().getTime();
//            advertiseTable.everyday_time_to = timeFormat.parse().getTime();
        } catch (ParseException e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }

        advertiseTable.media_url = adElementEntity.getMedia_url();
        advertiseTable.media_type = adElementEntity.getMedia_type();
        advertiseTable.duration = adElementEntity.getDuration();
        advertiseTable.location = FileUtils.getFileByUrl(adElementEntity.getMedia_url());
        advertiseTable.md5 = adElementEntity.getMd5();
        advertiseTable.type = LAUNCH_APP_ADVERTISEMENT;
        advertiseTable.media_id = String.valueOf(adElementEntity.getMedia_id());
        advertiseTable.save();

        Log.i(TAG, "downloadStartAd success------>");

    }

    private void downLoadFile(String downloadUrl, final String filePath, final AdElementEntity adElementEntity) {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(6, TimeUnit.SECONDS)
                    .readTimeout(1, TimeUnit.HOURS)
                    .addInterceptor(new UserAgentInterceptor())
                    .build();
        }
        Request request = new Request.Builder().url(downloadUrl).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new CallaPlay().bootAdvExcept(
                        BOOT_ADV_DOWNLOAD_EXCEPTION_CODE, BOOT_ADV_DOWNLOAD_EXCEPTION_STRING
                );
                tv.ismar.library.util.FileUtils.deleteFile(filePath);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    int responseCode = response.code();
                    if (responseCode == 200) {
                        long total = response.body().contentLength();
                        Log.i(TAG, "total------>" + total);
                        long current = 0;
                        is = response.body().byteStream();
                        fos = new FileOutputStream(filePath);
                        while ((len = is.read(buf)) != -1) {
                            current += len;
                            fos.write(buf, 0, len);
                            Log.i(TAG, "download------>" + current);
                        }
                        saveToDb(adElementEntity);
                        fos.flush();
                    } else {
                        tv.ismar.library.util.FileUtils.deleteFile(filePath);
                    }
                } catch (IOException e) {
                    new CallaPlay().bootAdvExcept(
                            BOOT_ADV_DOWNLOAD_EXCEPTION_CODE, BOOT_ADV_DOWNLOAD_EXCEPTION_STRING
                    );
                    Log.e(TAG, e.toString());
                    tv.ismar.library.util.FileUtils.deleteFile(filePath);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        ExceptionUtils.sendProgramError(e);
                        Log.e(TAG, e.toString());
                    }
                }

            }
        });
    }

}
