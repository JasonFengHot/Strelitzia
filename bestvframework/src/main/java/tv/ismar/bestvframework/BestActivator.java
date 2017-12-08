package tv.ismar.bestvframework;

import android.content.Context;
import android.util.Log;

import com.bestv.ott.intf.AdapterManager;
import com.bestv.ott.intf.ILoader;
import com.bestv.ott.proxy.authen.AuthParam;
import com.bestv.ott.proxy.authen.AuthResult;
import com.bestv.ott.proxy.authen.AuthenProxy;
import com.bestv.ott.proxy.loader.LoaderProxy;

/**
 * Created by zhaoj on 17-12-5.
 */

public class BestActivator {
    private static BestActivator mInstance;
    private static final String StartLoaderFailureException = "StartLoaderFailureException";
    private static final String AuthProxyFailureException = "AuthProxyFailureException";
    private final String TAG = "BestActivator";
    private final int AuthProxyTimeout = 5000;
    private Context mContext;
    private static boolean mEnable = false;
    private BestLoaderResultListener mBestLoaderResultListener;

    public static BestActivator getInstance(Context context) {
        if (mInstance == null) {
            synchronized (BestActivator.class) {
                if (mInstance == null) {
                    mInstance = new BestActivator(context);
                }
            }
        }
        return mInstance;
    }

    private BestActivator(Context context) {
        mContext = context;
        AdapterManager.getInstance().init(mContext);
    }

    public static void setEnable(boolean enable) {
        mEnable = enable;
    }

    public static boolean isEnabled() {
//        return mEnable;
        return false;
    }

    private ILoader.ILoaderListener mOttLoadListener = new ILoader.ILoaderListener(){
        @Override
        public void onError(int arg0, String arg1) {
            Log.e(TAG, "startLoader onError error code : " + arg0 + " error msg:" + arg1);
            startLoaderResultCallback(StartLoaderResultE.StartLoaderFailure);
        }

        //如没有登录显示的需求,可忽略 onInfo 回调
        @Override
        public void onInfo(int arg0, String arg1) {
            Log.d(TAG, "startLoader onInfo code = " + arg0 + ", desc = " + arg1);
            if (arg0 == 1) {
                //正在激活中
            } else if (arg0 == 2) {
                //正在登陆中
            }
        }
        @Override
        public void onLoaded(Object arg0) {
            //百视通账号登录成功
            Log.d(TAG, "startLoader onLoaded");
            startLoaderResultCallback(StartLoaderResultE.StartLoaderSuccess);
        }
    };

    public void startLoader() {
        try {
            Log.d(TAG, "startLoader");
            LoaderProxy.getInstance().startLoader(mOttLoadListener);
        } catch (Exception e) {
            e.printStackTrace();
            //异常的场合下认为是成功
            startLoaderResultCallback(StartLoaderResultE.StartLoaderSuccess);
        }
    }

    public AuthProxyResultE authProxy(String itemCode, String episodeNum) {
        AuthProxyResultE result = AuthProxyResultE.AuthResultFailure;
        try {
            Log.d(TAG, "authProxy itemCode = " + itemCode + " episodeNum = " + episodeNum);
            AuthParam param = new AuthParam();
            param.setItemCode(itemCode);
            param.setEpisodeCode(episodeNum);
            AuthResult authResult = AuthenProxy.getInstance().auth(param, AuthProxyTimeout);
            Log.d(TAG, "auth result = " + authResult);
            result = convertAuthResult(authResult);
        } catch (Exception e) {
            e.printStackTrace();
            //异常的场合下认为是成功
            result = AuthProxyResultE.AuthResultSuccess;
        }
        return result;
    }

    public static boolean startLoaderResultISuccess(StartLoaderResultE result) {
        if (result == StartLoaderResultE.StartLoaderSuccess) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean authProxyResultISuccess(AuthProxyResultE result) {
        if (result == AuthProxyResultE.AuthResultSuccess) {
            return true;
        } else {
            return false;
        }
    }

    public static void actionStartLoaderFailure() {
//        throw new RuntimeException(StartLoaderFailureException);
    }

    public static void actionAuthProxyFailure() {
//        throw new RuntimeException(AuthProxyFailureException);
    }

    private AuthProxyResultE convertAuthResult(AuthResult authResult) {
        AuthProxyResultE result = AuthProxyResultE.AuthResultFailure;
        if (authResult != null) {
            switch (authResult.getReturnCode()) {
            case AuthResult.AUTH_RESULT_FAILED:
                result = AuthProxyResultE.AuthResultFailure;
                break;
            case AuthResult.AUTH_RESULT_NOT_ORDERED:
                result = AuthProxyResultE.AuthResultNotOrder;
                break;
            case AuthResult.AUTH_RESULT_AUTH_SUCCESS:
                result = AuthProxyResultE.AuthResultSuccess;
                break;
            case AuthResult.AUTH_RESULT_ORDER_SUCCESS:
                result = AuthProxyResultE.AuthResultOrderSuccess;
                break;
            case AuthResult.AUTH_RESULT_ORDER_NOT_ENOUGH:
                result = AuthProxyResultE.AuthResultOrderNotEnough;
                break;
            case AuthResult.AUTH_RESULT_AUTH_EXCEPTION:
                result = AuthProxyResultE.AuthResultAuthException;
                break;
            case AuthResult.AUTH_RESULT_ORDER_EXCEPTION:
                result = AuthProxyResultE.AuthResultOrderException;
                break;
            case AuthResult.AUTH_RESULT_OPERAUTH_FIALED:
                result = AuthProxyResultE.AuthResultAuthFailure;
                break;
            default:
                result = AuthProxyResultE.AuthResultFailure;
                break;
            }
        } else {
            result = AuthProxyResultE.AuthResultFailure;
        }
        return result;
    }

    private void startLoaderResultCallback(StartLoaderResultE result) {
        if (mBestLoaderResultListener != null) {
            mBestLoaderResultListener.startLoaderResult(result);
        } else {
            Log.e(TAG, "startLoaderResultCallback listener null");
        }
    }

    public enum StartLoaderResultE {
        StartLoaderSuccess,
        StartLoaderFailure,
    }

    public enum AuthProxyResultE {
        AuthResultFailure,              //网络异常
        AuthResultNotOrder,             //鉴权失败/没有订购
        AuthResultSuccess,              //鉴权成功 (只有这种情况下才认为是成功)
        AuthResultOrderSuccess,         //订购成功
        AuthResultOrderNotEnough,       //余额不足
        AuthResultAuthException,        //鉴权信息异常
        AuthResultOrderException,       //订购支付异常
        AuthResultAuthFailure,          //鉴权失败
    }

    public void setBestLoaderResultListener(BestLoaderResultListener listener) {
        mBestLoaderResultListener = listener;
    }

    public interface BestLoaderResultListener {
        void startLoaderResult(StartLoaderResultE result);
    }
}
