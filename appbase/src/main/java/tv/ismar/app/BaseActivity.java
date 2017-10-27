package tv.ismar.app;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import java.util.Stack;

import cn.ismartv.truetime.TrueTime;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observer;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.player.OnNoNetConfirmListener;
import tv.ismar.app.ui.ToastTip;
import tv.ismar.app.update.UpdateService;
import tv.ismar.app.util.NetworkUtils;
import tv.ismar.app.widget.ItemOffLinePopWindow;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.app.widget.Login_hint_dialog;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.app.widget.NetErrorPopWindow;
import tv.ismar.app.widget.UpdatePopupWindow;
import tv.ismar.library.exception.ExceptionUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static tv.ismar.app.update.UpdateService.APP_UPDATE_ACTION;

/**
 * Created by beaver on 16-8-19.
 */
public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private UpdatePopupWindow updatePopupWindow;
    private LoadingDialog mLoadingDialog;
    private ModuleMessagePopWindow netErrorPopWindow;
    private ModuleMessagePopWindow itemOffLinePop;
    private ModuleMessagePopWindow dialog;
    public SkyService mSkyService;
    public SkyService mWeatherSkyService;
    public SkyService mWxApiService;
    public SkyService mIrisService;
    public SkyService mSpeedCallaService;
    public SkyService mLilyHostService;
    public SkyService mCarnationService;
    public long app_start_time;
    public long start_time;
    private Login_hint_dialog login_hint_dialog;

    public static final String NO_NET_CONNECT_ACTION = "cn.ismartv.vod.action.nonet";
    //    public static SmartPlayer mSmartPlayer;// 由于目前需要在详情页实现预加载功能，故写此变量
    public static String brandName;

    public static Stack<Bundle> updateInfo = new Stack<>();

    private boolean activityIsAlive = false;

    public static boolean isCheckoutUpdate = true;

    public int totalAdsMills;

    private Handler updateHandler;

    public boolean isExpireAccessToken = false;

    private Handler updateAgainHandler;
    private Runnable updateAgainRunnable;
    private static final int EXIT_PLAY=200;

    /**
     * 日志新加字段相关，定义为全局静态变量
     */
    public static String baseSection = "";
    public static String baseChannel = "";
    public static boolean goLogin = false;

    private Bundle updateBundle;

    /**
     * so文件的加载，由于SmartPlayer.jar需要判断，为了不重复加载so，用此变量判断
     */
    public static boolean wasLoadSmartPlayerSo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSkyService = SkyService.ServiceManager.getService();
        mWeatherSkyService = SkyService.ServiceManager.getWeatherService();
        mWxApiService = SkyService.ServiceManager.getWxApiService();
        mIrisService = SkyService.ServiceManager.getIrisService();
        mSpeedCallaService = SkyService.ServiceManager.getSpeedCallaService();
        mLilyHostService = SkyService.ServiceManager.getLilyHostService();
        mCarnationService = SkyService.ServiceManager.getCarnationService();
        app_start_time = TrueTime.now().getTime();
        registerNoNetReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityIsAlive = true;
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //checkout update
        if (isCheckoutUpdate) {
            checkUpgrade();
            isCheckoutUpdate = false;
        } else {
            if (!updateInfo.isEmpty()) {
                if (updatePopupWindow!=null && updatePopupWindow.isShowing()){
                    return;
                }
                updateAgainHandler = new Handler();
                updateAgainRunnable = (new Runnable() {
                    @Override
                    public void run() {
                        showUpdatePopup(getRootView(), updateInfo);
                    }
                });
                updateAgainHandler.postDelayed(updateAgainRunnable, 4000);
            }
        }

        registerConnectionReceiver();
    }

    /**
     * Dialog样式Activity进入其它页面时，不执行onStop
     */

    @Override
    protected void onStart() {
        super.onStart();
        registerUpdateReceiver();
    }

    @Override
    protected void onPause() {
        unregisterConnectionReceiver();
        activityIsAlive = false;
        try {
            unregisterReceiver(mUpdateReceiver);
        } catch (Exception e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }
        if (updateAgainHandler != null) {
            updateAgainHandler.removeCallbacks(updateAgainRunnable);
        }

        if (noNetConnectHandler != null) {
            noNetConnectHandler.removeCallbacks(noNetConnectRunnable);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (updatePopupWindow != null) {
            updatePopupWindow.dismiss();
            updatePopupWindow = null;
        }
        super.onStop();
    }

    protected <T extends View> T findView(int resId) {
        return (T) (findViewById(resId));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void showDialog(String msg) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this, R.style.LoadingDialog);
        }
        if (msg != null) {
            mLoadingDialog.setTvText(msg);
        }
        mLoadingDialog.showDialog();
    }

    public void dismissDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
            mLoadingDialog.setTvText(getString(R.string.loading_text));
        }
    }

    public boolean isDialogShow() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            return true;
        }
        return false;
    }

    public View getRootView() {
        return ((ViewGroup) (getWindow().getDecorView().findViewById(android.R.id.content))).getChildAt(0);
    }
    long showTime=0;

    public void dismissNoNetConnectDialog() {
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isNoNetDialogShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void showNoNetConnectDialog(final OnNoNetConfirmListener onNoNetConfirmListener) {
        if(dialog!=null&&dialog.isShowing()){
            return;
        }
        Log.i("onNoNet", "showNet!!!");
        final String act = getCurrentActivityName(BaseActivity.this);
        dialog = new NetErrorPopWindow(this);
        dialog.setMessage(getString(R.string.no_connectNet));
        dialog.setConfirmBtn(getString(R.string.setting_network));
        dialog.setCancelBtn(getString(R.string.exit_app));
        dialog.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!(act.contains("HomeActivity") || act.contains("WordSearchActivity") || act.contains("FilmStar")||act.contains("UserCenterActivity")||act.contains("PlaybackActivity"))) {
                    if(act.contains("PlayFinishedActivity"))
                        setResult(EXIT_PLAY);
                    finish();
                }
            }
        });
        try {
            dialog.showAtLocation(getRootView(), Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                        @Override
                        public void confirmClick(View view) {
//                            dialog.dismiss();
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(intent);
                            if(onNoNetConfirmListener != null){
                                onNoNetConfirmListener.onNoNetConfirm();
                            }

                        }
                    },
                    new ModuleMessagePopWindow.CancelListener() {
                        @Override
                        public void cancelClick(View view) {
                            dialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(NO_NET_CONNECT_ACTION);
                            sendBroadcast(intent);
                        }
                    });
            showTime=System.currentTimeMillis();
        } catch (Exception e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }

    }

    public void showNoNetConnectDialog() {
        if(dialog!=null&&dialog.isShowing()){
            return;
        }
        final String act = getCurrentActivityName(BaseActivity.this);
        Log.i("onNoNet", "showNet!!!");
        dialog = new NetErrorPopWindow(this);
        dialog.setMessage(getString(R.string.no_connectNet));
        dialog.setConfirmBtn(getString(R.string.setting_network));
        dialog.setCancelBtn(getString(R.string.exit_app));
        dialog.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!(act.contains("HomeActivity") || act.contains("WordSearchActivity") || act.contains("FilmStar")||act.contains("UserCenterActivity")||act.contains("PlaybackActivity"))) {
                    if(act.contains("PlayFinishedActivity"))
                        setResult(EXIT_PLAY);
                    finish();
                }
            }
        });
        try {
            dialog.showAtLocation(getRootView(), Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                        @Override
                        public void confirmClick(View view) {
//                            dialog.dismiss();
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(intent);

                        }
                    },
                    new ModuleMessagePopWindow.CancelListener() {
                        @Override
                        public void cancelClick(View view) {
                            dialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(NO_NET_CONNECT_ACTION);
                            sendBroadcast(intent);
                        }
                    });
            showTime=System.currentTimeMillis();
        } catch (Exception e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }
    }
    public boolean isshowNetWorkErrorDialog() {
        return netErrorPopWindow != null && netErrorPopWindow.isShowing();
    }

    public abstract class BaseObserver<T> implements Observer<T> {
        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            if (!activityIsAlive) {
                return;
            }
            Log.i("onNoNet", "onerror" + NetworkUtils.isConnected(BaseActivity.this));
            if (!NetworkUtils.isConnected(BaseActivity.this) && !NetworkUtils.isWifi(BaseActivity.this)) {
                Log.i("onNoNet", "" + NetworkUtils.isConnected(BaseActivity.this));
                showNoNetConnectDelay();
            } else if (e instanceof HttpException) {
                HttpException httpException = (HttpException) e;
                if (httpException.code() == 401) {
                    showExpireAccessTokenPop();
                } else if(httpException.code() == 408){
                    ToastTip.showToast(BaseActivity.this,"网络连接超时，请重试");
                }else if(httpException.code() == 504){
                    ToastTip.showToast(BaseActivity.this,"服务器繁忙，请稍后再试");
                }else{
                    ToastTip.showToast(BaseActivity.this,"网络连接失败，请重试");
                }
            }
        }
    }
    public void showExpireAccessTokenPop() {
        ToastTip.showToast(this,getString(R.string.access_token_expire));
    }

    public void showItemOffLinePop() {
        itemOffLinePop = new ItemOffLinePopWindow(this);
        itemOffLinePop.setMessage(getString(R.string.item_offline));
        itemOffLinePop.setConfirmBtn(getString(R.string.confirm));
        itemOffLinePop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                finish();
            }
        });
        itemOffLinePop.showAtLocation(getRootView(), Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        itemOffLinePop.dismiss();
                    }
                },
                null);


    }

    public void historyShowItemOffLinePop() {
        itemOffLinePop = new ItemOffLinePopWindow(this);
        itemOffLinePop.setMessage(getString(R.string.item_offline));
        itemOffLinePop.setConfirmBtn(getString(R.string.confirm));
        itemOffLinePop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                itemOffLinePop.dismiss();
            }
        });
        itemOffLinePop.showAtLocation(getRootView(), Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        itemOffLinePop.dismiss();
                    }
                },
                null);


    }

    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Log.d("UpdateReceiver", intent.getBundleExtra("data").toString());
            Bundle bundle = intent.getBundleExtra("data");

            boolean isExsit = false;
            for (Bundle b : updateInfo) {
                if (b.get("path").equals(bundle.get("path")) &&
                        b.get("msgs").equals(bundle.get("msgs"))) {
                    isExsit = true;

                }
            }

            if (updateBundle!=null) {
                if (updateBundle.get("path").equals(bundle.get("path")) &&
                        updateBundle.get("msgs").equals(bundle.get("msgs"))) {
                    isExsit = true;

                }
            }


            if (!isExsit) {
                updateInfo.push(bundle);
                updateAgainHandler = new Handler();
                updateAgainRunnable = (new Runnable() {
                    @Override
                    public void run() {
                        if (updatePopupWindow == null || !updatePopupWindow.isShowing()) {
                            if (activityIsAlive) {
                                showUpdatePopup(getRootView(), updateInfo);
                            }
                        }
                    }
                });
                updateAgainHandler.postDelayed(updateAgainRunnable, 4000);
            }
        }
    };
    private BroadcastReceiver onNetConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    private void registerNoNetReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NO_NET_CONNECT_ACTION);
        registerReceiver(onNetConnectReceiver, intentFilter);
    }


    private void registerUpdateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(APP_UPDATE_ACTION);
        registerReceiver(mUpdateReceiver, intentFilter);
    }

    private void showUpdatePopup(final View view, final Stack<Bundle> stack) {
        String currentActivityName = getCurrentActivityName(this);
        if (!stack.isEmpty() && !currentActivityName.equals("tv.ismar.player.gui.PlaybackActivity")
                && !currentActivityName.equals("tv.ismar.daisy.PlayFinishedActivity")) {
            updateBundle = stack.pop();
            updatePopupWindow = new UpdatePopupWindow(this, updateBundle);
            updatePopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
            updatePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (!activityIsAlive) {
                        updateInfo.push(updateBundle);
                    }
                    showUpdatePopup(view, stack);
                }
            });
        }
    }


    @Override
    protected void onDestroy() {
        if (updateHandler != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
        try {
            unregisterReceiver(onNetConnectReceiver);
        } catch (Exception e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }
        super.onDestroy();

//        RefWatcher refWatcher = VodApplication.getRefWatcher(this);
//        refWatcher.watch(this);

    }

    private void checkUpgrade() {
        updateHandler = new Handler();
        updateHandler.postDelayed(updateRunnable, (1000 * 3) + totalAdsMills);
    }

    Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), UpdateService.class);
            intent.putExtra("install_type", 0);
            startService(intent);
        }
    };

    public String getCurrentActivityName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        android.util.Log.i(TAG, "getCurrentActivityName : pkg --->" + cn.getPackageName());
        android.util.Log.i(TAG, "getCurrentActivityName : cls ---> " + cn.getClassName());
        return cn.getClassName();
    }


    Handler noNetConnectHandler;
    Runnable noNetConnectRunnable;

    public void showNoNetConnectDelay() {
        noNetConnectHandler = new Handler();
        noNetConnectRunnable = new Runnable() {
            @Override
            public void run() {
                showNoNetConnectDialog();
            }
        };

        noNetConnectHandler.postDelayed(noNetConnectRunnable, 1000);
    }
    public void showLoginHint(){
        SharedPreferences sp=getSharedPreferences("Daisy",0);
        int code=fetchInstallVersionCode();
        if(sp!=null&&sp.getInt("installCode",-1)!=code){
            SharedPreferences.Editor editor=sp.edit();
            editor.putInt("installCode",code);
            editor.commit();
            login_hint_dialog=new Login_hint_dialog(this);
            if(login_hint_dialog!=null&&!login_hint_dialog.isShowing()){
                login_hint_dialog.showAtLocation(getRootView(),Gravity.CENTER,0,0);
            }
        }
    }
    private int fetchInstallVersionCode() {
        int versionCode = 0;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "can't find this application!!!");
        }
        return versionCode;
    }


    private ConnectionChangeReceiver connectionChangeReceiver;

    private void registerConnectionReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        connectionChangeReceiver = new ConnectionChangeReceiver();
        registerReceiver(connectionChangeReceiver, filter);
    }

    private void unregisterConnectionReceiver() {
        if (connectionChangeReceiver != null) {
            unregisterReceiver(connectionChangeReceiver);
        }
    }

    private class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                ConnectivityManager   mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
                if(netInfo != null && netInfo.isAvailable()) {
                    if (dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    /////////////网络连接
                    String name = netInfo.getTypeName();

                    if(netInfo.getType()==ConnectivityManager.TYPE_WIFI){
                        /////WiFi网络

                    }else if(netInfo.getType()==ConnectivityManager.TYPE_ETHERNET){
                        /////有线网络

                    }else if(netInfo.getType()==ConnectivityManager.TYPE_MOBILE){
                        /////////3g网络

                    }
                } else {
                    ////////网络断开
                    showNoNetConnectDialog();
                }
            }
        }
    }
}
