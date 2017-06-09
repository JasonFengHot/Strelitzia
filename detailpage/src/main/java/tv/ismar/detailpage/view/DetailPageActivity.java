package tv.ismar.detailpage.view;
import com.google.gson.GsonBuilder;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.util.concurrent.TimeoutException;

import retrofit2.adapter.rxjava.HttpException;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.VodApplication;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.db.HistoryManager;
import tv.ismar.app.entity.ClipEntity;
import tv.ismar.app.entity.History;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.detailpage.R;
import tv.ismar.library.util.LogUtils;
import tv.ismar.library.util.StringUtils;
import tv.ismar.player.gui.PlaybackService;

import static tv.ismar.app.core.PageIntentInterface.DETAIL_TYPE_ITEM;
import static tv.ismar.app.core.PageIntentInterface.DETAIL_TYPE_PKG;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_ITEM_JSON;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_PK;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_SOURCE;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_TYPE;

/**
 * Created by huibin on 8/18/16.
 */
public class DetailPageActivity extends BaseActivity implements PlaybackService.Client.Callback {
    private static final String TAG = "DetailPageActivity";

    // 0秒起播
    private PlaybackService.Client mClient;
    private PlaybackService mPlaybackService;
    private Subscription apiClipSubsc;// 需要知道当前是视云还是爱奇艺影片，只有是视云影片才加载播放器
    private HistoryManager historyManager;// 多集影片，需要查询历史记录，历史剧集的片源
    private History mHistory;
    private boolean goPlayPage;// 是否点击播放，非点击播放，需要将当前预加载停止

    private Subscription apiItemSubsc;
    private String source;
    private ItemEntity mItemEntity;

    private DetailPageFragment detailPageFragment;
    private PackageDetailFragment mPackageDetailFragment;
    public LoadingDialog mLoadingDialog;
    private int itemPK;
    private boolean isqiyi;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
                showNetWorkErrorDialog(new TimeoutException());
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_detailpage);
        Intent intent = getIntent();

        itemPK = intent.getIntExtra(EXTRA_PK, -1);
        String itemJson = intent.getStringExtra(EXTRA_ITEM_JSON);
        source = intent.getStringExtra(EXTRA_SOURCE);
        if (source != null && source.equals("launcher")) {
            AppConstant.purchase_entrance_page = "launcher";
        }
        int type = intent.getIntExtra(EXTRA_TYPE, 0);
        String url = intent.getStringExtra("url");

        if (TextUtils.isEmpty(itemJson) && itemPK == -1 && TextUtils.isEmpty(url)) {
            finish();
            return;
        }

        showDialog();

        //解析来至launcher的参数
        if (!TextUtils.isEmpty(url)) {
            Log.e("launcher_url", url);
            String[] arrayTmp = url.split("/");
            itemPK = Integer.parseInt(arrayTmp[arrayTmp.length - 1]);
            switch (arrayTmp[arrayTmp.length - 2]) {
                case "item":
                    type = DETAIL_TYPE_ITEM;
                    break;
                case "package":
                    type = DETAIL_TYPE_PKG;
                    break;
            }
            Log.e("launcher_type", type + "");
        }

        if (!TextUtils.isEmpty(itemJson)) {
            mItemEntity = new GsonBuilder().create().fromJson(itemJson, ItemEntity.class);
            loadFragment(type);
        } else {
            fetchItem(String.valueOf(itemPK), type);
        }
        mClient = new PlaybackService.Client(this, this);

    }

    public void goPlayer() {
        goPlayPage = true;
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.Player");
        intent.putExtra(PageIntentInterface.EXTRA_PK, mItemEntity.getPk());
//        intent.putExtra(PageIntentInterface.EXTRA_SUBITEM_PK, mSubItemPk);
        intent.putExtra(PageIntentInterface.EXTRA_SOURCE, source);
        intent.putExtra(PageIntentInterface.QIYIFLAG, isqiyi);
        startActivity(intent);

    }

    public void fetchItem(final String pk, final int type) {
        if (apiItemSubsc != null && !apiItemSubsc.isUnsubscribed()) {
            apiItemSubsc.unsubscribe();
        }
        String opt = "";
        switch (type) {
            case DETAIL_TYPE_ITEM:
                opt = "item";
                break;
            case DETAIL_TYPE_PKG:
                opt = "package";
                break;
        }

        apiItemSubsc = mSkyService.apiOptItem(pk, opt)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ItemEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mLoadingDialog != null)
                            mLoadingDialog.dismiss();
                        if (e instanceof HttpException && ((HttpException) e).code() == 404) {
                            showItemOffLinePop();
                        } else {
                            super.onError(e);
                        }
                    }

                    @Override
                    public void onNext(ItemEntity itemEntity) {
                        mItemEntity = itemEntity;
                        loadFragment(type);
                    }
                });
    }

    private void loadFragment(int type) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (type) {
            case PageIntent.DETAIL_TYPE_ITEM:
                String itemJson = new GsonBuilder().create().toJson(mItemEntity);
                detailPageFragment = DetailPageFragment.newInstance(source, itemJson);
                fragmentTransaction.replace(R.id.activity_detail_container, detailPageFragment);
                fragmentTransaction.commit();
                break;
            case PageIntent.DETAIL_TYPE_PKG:
                String packJson = new GsonBuilder().create().toJson(mItemEntity);
                mPackageDetailFragment = PackageDetailFragment.newInstance(source, packJson);
                fragmentTransaction.replace(R.id.activity_detail_container, mPackageDetailFragment);
                fragmentTransaction.commit();
                break;
        }

    }

    // 详情页检查权限后触发
    public void playCheckResult(boolean permission) {
        Log.i(TAG, "playCheckResult:" + permission);
        if(mItemEntity == null)
            return;
        String clipUrl = mItemEntity.getClip().getUrl();
        if (permission || mItemEntity.getExpense() == null) {
            // 检查历史记录
            if (historyManager == null) {
                historyManager = VodApplication.getModuleAppContext().getModuleHistoryManager();
            }
            String historyUrl = Utils.getItemUrl(mItemEntity.getPk());
            String isLogin = "no";
            if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken())) {
                isLogin = "yes";
            }
            mHistory = historyManager.getHistoryByUrl(historyUrl, isLogin);
            ItemEntity[] subItems = mItemEntity.getSubitems();
            if (mHistory != null && subItems != null && subItems.length > 0) {
                int history_sub_pk = Utils.getItemPk(mHistory.sub_url);
                Log.i(TAG, "PreloadHistorySubPk : " + history_sub_pk);
                if (history_sub_pk > 0) {
                    // 获取当前要播放的电视剧Clip
                    for (ItemEntity subItem : subItems) {
                        int _subItemPk = subItem.getPk();
                        if (history_sub_pk == _subItemPk) {
                            clipUrl = subItem.getClip().getUrl();
                            break;
                        }
                    }
                }
            }
        } else if (mItemEntity.getPreview() != null) {
            clipUrl = mItemEntity.getPreview().getUrl();
        }
        if (StringUtils.isEmpty(clipUrl)) {
            LogUtils.e(TAG, "clipUrl is null");
            return;
        }
        fetchClip(clipUrl);

    }

    private void fetchClip(String clipUrl) {
        if (apiClipSubsc != null && !apiClipSubsc.isUnsubscribed()) {
            apiClipSubsc.unsubscribe();
        }
        apiClipSubsc = mSkyService.fetchMediaUrl(clipUrl, "", "1")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ClipEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "fetchClip onError");
                    }

                    @Override
                    public void onNext(ClipEntity clipEntity) {
                        String iqiyi = clipEntity.getIqiyi_4_0();
                        if (Utils.isEmptyText(iqiyi)) {
                            // 片源为视云,实现预加载功能
                            // 详情页预加载，绑定服务，必须在mItemEntity不为空时执行connect操作
                            LogUtils.d("LH/", "Preload true.");
                            int h264PlayerType =IsmartvActivator.getInstance().getH264PlayerType();
                            if(h264PlayerType != 1)
                            mClient.connect();
                        }else {
                            isqiyi = true;
                        }
                    }
                });

    }

    public void showDialog() {
        handler.sendEmptyMessageDelayed(0, 15000);
        start_time = System.currentTimeMillis();
        mLoadingDialog = new LoadingDialog(this, R.style.LoadingDialog);
        mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                finish();
            }
        });
        mLoadingDialog.showDialog();
    }

    @Override
    protected void onResume() {
//        isClickPlay = false;
//        isActivityStoped = false;
//        mHasPreLoad = false;
        super.onResume();
        AppConstant.purchase_referer = "video";
        AppConstant.purchase_page = "detail";
        goPlayPage = false;

    }

    @Override
    protected void onStop() {
        if (apiItemSubsc != null && apiItemSubsc.isUnsubscribed()) {
            apiItemSubsc.unsubscribe();
        }
        if (apiClipSubsc != null && !apiClipSubsc.isUnsubscribed()) {
            apiClipSubsc.unsubscribe();
        }
        super.onStop();
        mClient.disconnect();
    }

    @Override
    public void onConnected(PlaybackService service) {
        mPlaybackService = service;
        mPlaybackService.preparePlayer(mItemEntity, source);

    }

    @Override
    public void onDisconnected() {
        LogUtils.e(TAG, "service disconnected : " + goPlayPage);
        if (!goPlayPage) {
            if (mPlaybackService != null) {
                mPlaybackService.stopPlayer(false);
            }
        }
        mPlaybackService = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mPackageDetailFragment != null) {
            mPackageDetailFragment.onActivityBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent();
        intent.putExtra("pk", itemPK);
        setResult(1, intent);
        handler.removeMessages(0);
        handler = null;
        super.onDestroy();
    }
}
