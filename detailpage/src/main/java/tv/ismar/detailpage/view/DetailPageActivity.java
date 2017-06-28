package tv.ismar.detailpage.view;
import com.google.gson.GsonBuilder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

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
import tv.ismar.app.core.Source;
import tv.ismar.app.db.HistoryManager;
import tv.ismar.app.entity.ClipEntity;
import tv.ismar.app.entity.History;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.detailpage.R;
import tv.ismar.library.util.DateUtils;
import tv.ismar.library.util.LogUtils;
import tv.ismar.library.util.StringUtils;
import tv.ismar.player.IsmartvPlayer;
import tv.ismar.player.gui.PlaybackFragment;
import tv.ismar.player.gui.PlaybackService;
import tv.ismar.player.widget.ExitToast;
import tv.ismar.statistics.DetailPageStatistics;

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
    private boolean sharpSetupKeyClick; // 部分夏普设备弹出设置菜单是Dialog Activity样式
    private long preloadStartTime;

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
    public String to;
    private DetailPageStatistics mPageStatistics;
    public boolean sendLog=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        mPageStatistics = new DetailPageStatistics();
        setContentView(R.layout.activity_detailpage);
        Intent intent = getIntent();

        itemPK = intent.getIntExtra(EXTRA_PK, -1);
        String itemJson = intent.getStringExtra(EXTRA_ITEM_JSON);
        source = intent.getStringExtra(EXTRA_SOURCE);
        to = intent.getStringExtra("to");
        if (source != null && source.equals("launcher")) {
            AppConstant.purchase_entrance_page = "launcher";
        }
        int type = intent.getIntExtra(EXTRA_TYPE, 0);
        String url = intent.getStringExtra("url");
        if(TextUtils.isEmpty(to)) {
            if (!TextUtils.isEmpty(source)) {
                if (!(source.equals(Source.RELATED.getValue()) || source.equals(Source.FINISHED.getValue()) || source.equals(Source.EXIT_LIKE.getValue()) || source.equals(Source.EXIT_NOT_LIKE.getValue()))) {
                    to = source;
                }
            }
        }

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
            registerClosePlayerReceiver();
            loadFragment(type);
        } else {
            fetchItem(String.valueOf(itemPK), type);
        }
        mClient = new PlaybackService.Client(this, this);

    }

    public void goPlayer() {
        Log.i("contentMode","contentMode : "+mItemEntity.getContentModel());
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.Player");
        intent.putExtra(PageIntentInterface.EXTRA_PK, mItemEntity.getPk());
//        intent.putExtra(PageIntentInterface.EXTRA_SUBITEM_PK, mSubItemPk);
        intent.putExtra(PageIntentInterface.EXTRA_SOURCE, source);
        intent.putExtra(PageIntentInterface.QIYIFLAG, isqiyi);
        intent.putExtra(PageIntentInterface.EXTRA_TO, to);
        intent.putExtra("contentMode",mItemEntity.getContentModel());
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
                        registerClosePlayerReceiver();
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

    // 此方法必须在clic事件之后调用
    public void stopPreload() {
        if (mPlaybackService != null) {
            if (!IsmartvPlayer.isPreloadCompleted && mPlaybackService.getMediaPlayer() != null) {
                mPlaybackService.getMediaPlayer().logPreloadEnd();
            }
            mPlaybackService.stopPlayer(false);
        }
    }

    @Override
    protected void onResume() {
//        isClickPlay = false;
//        isActivityStoped = false;
//        mHasPreLoad = false;
        super.onResume();
        AppConstant.purchase_referer = "video";
        AppConstant.purchase_page = "detail";
            new Handler().postDelayed(mRunnable,1000);
    }

    Runnable mRunnable=new Runnable() {
        @Override
        public void run() {
            showLoginHint();
        }
    };
    @Override
    protected void onPause() {
        super.onPause();
        if (sharpSetupKeyClick) {
            sharpSetupKeyClick = false;
            return;
        }
        if (apiClipSubsc != null && !apiClipSubsc.isUnsubscribed()) {
            apiClipSubsc.unsubscribe();
        }
        mClient.disconnect();
        mPlaybackService = null;
    }

    @Override
    protected void onStop() {
        if (apiItemSubsc != null && apiItemSubsc.isUnsubscribed()) {
            apiItemSubsc.unsubscribe();
        }
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ("lcd_s3a01".equals(getModelName())) {
            if (keyCode == 707 || keyCode == 774 || keyCode == 253) {
                sharpSetupKeyClick = true;
            }
        } else if ("lx565ab".equals(getModelName())) {
            if (keyCode == 82 || keyCode == 707 || keyCode == 253) {
                sharpSetupKeyClick = true;
            }
        } else if ("lcd_xxcae5a_b".equals(getModelName())) {
            if (keyCode == 497 || keyCode == 498 || keyCode == 490) {
                sharpSetupKeyClick = true;
            }
        } else {
            if (keyCode == 223 || keyCode == 499 || keyCode == 480) {
                sharpSetupKeyClick = true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private String getModelName() {
        if (Build.PRODUCT.length() > 20) {
            return Build.PRODUCT.replaceAll(" ", "_").toLowerCase().substring(0, 19);
        } else {
            return Build.PRODUCT.replaceAll(" ", "_").toLowerCase();
        }
    }

    @Override
    public void onConnected(PlaybackService service) {
        LogUtils.d(TAG, "service connected : ");
        preloadStartTime = DateUtils.currentTimeMillis();
        mPlaybackService = service;
        mPlaybackService.preparePlayer(mItemEntity, source);

    }

    @Override
    public void onDisconnected() {
        LogUtils.e(TAG, "service disconnected : ");
    }

    @Override
    public void onBackPressed() {
        stopPreload();
        if (mPackageDetailFragment != null) {
            mPackageDetailFragment.onActivityBackPressed();
        }
        sendLog=true;
        if (TextUtils.isEmpty(to)) {
            to=Source.RELATED.getValue();
        }
        mPageStatistics.videoDetailOut(mItemEntity,to);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent();
        intent.putExtra("pk", itemPK);
        setResult(1, intent);
        handler.removeMessages(0);
        handler.removeCallbacks(mRunnable);
        handler = null;
        unRegisterClosePlayerReceiver();
        mLoadingDialog=null;
        mClient=null;
        super.onDestroy();
    }
    private ClosePlayerReceiver closePlayerReceiver;

    private void registerClosePlayerReceiver() {
        IntentFilter filter = new IntentFilter("tv.ismar.daisy.closeplayer");
        closePlayerReceiver = new ClosePlayerReceiver(this,mItemEntity);
        registerReceiver(closePlayerReceiver, filter);
    }

    private void unRegisterClosePlayerReceiver() {
        if (closePlayerReceiver != null) {
            unregisterReceiver(closePlayerReceiver);
            closePlayerReceiver=null;
        }
    }

}
