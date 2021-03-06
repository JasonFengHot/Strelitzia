package tv.ismar.app.core;

import android.app.Activity;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.List;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.query.Delete;
import cn.ismartv.injectdb.library.query.Select;
import rx.Observer;
import rx.schedulers.Schedulers;
import tv.ismar.app.database.BannerIconMarkTable;
import tv.ismar.app.database.DpiTable2;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.BannerIconMarkEntity;
import tv.ismar.app.network.entity.DpiEntity;
import tv.ismar.library.exception.ExceptionUtils;

/**
 * Created by huibin on 8/30/16.
 */
public class VipMark {
    private static final String TAG = "VipMark";
    private static VipMark mInstance;
    private int height;

    public void setHeight(int height) {
        this.height = height;
    }

    private VipMark() {
        fetchDpi();
        apiNewDpi();
    }


    public static VipMark getInstance() {
        if (mInstance == null) {
            mInstance = new VipMark();
        }
        return mInstance;
    }


    private void fetchDpi() {
        SkyService.ServiceManager.getService().fetchDpi()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<List<DpiEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<DpiEntity> dpiEntities) {
                        new Delete().from(DpiTable2.class).execute();
                        ActiveAndroid.beginTransaction();
                        try {
                            for (DpiEntity dpiEntity : dpiEntities) {
                                if (dpiEntity.getApp_name().equals("sky")) {
                                    DpiTable2 dpiTable = new DpiTable2();
                                    dpiTable.pay_type = dpiEntity.getPay_type();
                                    dpiTable.image = dpiEntity.getImage();
                                    dpiTable.cp = dpiEntity.getCp();
                                    dpiTable.name = Integer.parseInt(dpiEntity.getName());
                                    dpiTable.save();
                                }
                            }
                            ActiveAndroid.setTransactionSuccessful();
                        } finally {
                            ActiveAndroid.endTransaction();
                        }
                    }
                });

    }


    private void apiNewDpi(){
        SkyService.ServiceManager.getService().apiNewDpi()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<List<BannerIconMarkEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<BannerIconMarkEntity> bannerIconMarkEntities) {
                        new Delete().from(BannerIconMarkTable.class).execute();
                        ActiveAndroid.beginTransaction();
                        try {
                            for (BannerIconMarkEntity bannerIconMarkEntity : bannerIconMarkEntities) {
                                BannerIconMarkTable bannerIconMarkTable = new BannerIconMarkTable();
                                bannerIconMarkTable.pk = bannerIconMarkEntity.getPk();
                                bannerIconMarkTable.name = bannerIconMarkEntity.getName();
                                bannerIconMarkTable.image = bannerIconMarkEntity.getImage();
                                bannerIconMarkTable.save();
                            }
                            ActiveAndroid.setTransactionSuccessful();
                        } finally {
                            ActiveAndroid.endTransaction();
                        }
                    }
                });

    }

    public String getImage(Activity activity, int payType, int cpId) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;  // ????????????????????????
        int height = metric.heightPixels;  // ????????????????????????
        float density = metric.density;  // ???????????????0.75 / 1.0 / 1.5???
        int densityDpi = metric.densityDpi;  // ????????????DPI???120 / 160 / 240???
        String name = String.valueOf((int) (height / density));
        DpiTable2 dpiTable = null;
        try {

            dpiTable = new Select().from(DpiTable2.class)
                    .where("pay_type = ?", payType)
                    .where("cp = ?", cpId)
                    .orderBy("abs(" + height + " - name) asc")
                    .executeSingle();
        } catch (SQLiteException e) {
            ExceptionUtils.sendProgramError(e);
            Log.e(TAG, e.getMessage());
        }
        return dpiTable == null ? "test" : dpiTable.image;
    }

    public String getBannerIconMarkImage(String pk) {
        if (TextUtils.isEmpty(pk)) {
            return null;
        } else {
            BannerIconMarkTable bannerIconMarkTable = null;
            try {
                bannerIconMarkTable = new Select().from(BannerIconMarkTable.class)
                        .where("pk = ?", pk)
                        .orderBy("abs(" + height + " - name) asc")
                        .executeSingle();
            } catch (SQLiteException e) {
                ExceptionUtils.sendProgramError(e);
                Log.e(TAG, e.getMessage());
            }
            return bannerIconMarkTable == null ? null : bannerIconMarkTable.image;
        }
    }

}
