package tv.ismar.pay;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import tv.ismar.account.IsmartvActivator;

/**
 * Created by huibin on 07/11/2017.
 */

public class UniPayFragment extends Fragment {

    private View contentView;

    private WebView mWebView;

    private String mHtmlUrl;

    private PaymentActivity paymentActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        paymentActivity = (PaymentActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_unipay, null);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWebView = (WebView) contentView.findViewById(R.id.unipay_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setDefaultTextEncodingName("utf-8") ;
//        int   screenDensity   = getResources().getDisplayMetrics(). densityDpi ;
//        WebSettings.ZoomDensity   zoomDensity   = WebSettings.ZoomDensity. MEDIUM;
//        switch (screenDensity){
//            case   DisplayMetrics.DENSITY_LOW :
//                zoomDensity = WebSettings.ZoomDensity.CLOSE;
//                break ;
//            case   DisplayMetrics.DENSITY_MEDIUM :
//                zoomDensity = WebSettings.ZoomDensity.MEDIUM;
//                break ;
//            case   DisplayMetrics.DENSITY_HIGH :
//                zoomDensity = WebSettings.ZoomDensity.FAR;
//                break ;
//        }
//        mWebView .getSettings().setDefaultZoom(zoomDensity) ;
//
//
//        mWebView.setPadding(0, 0, 0, 0);
//        mWebView.setInitialScale(getScale());
        mWebView.setBackgroundColor(0); // 设置背景色
//        mWebView.getBackground().setAlpha(0); // 设置填充透明度 范围：0-255
        Bundle bundle = getArguments();
        mHtmlUrl = bundle.getString("url");
        mHtmlUrl = IsmartvActivator.getInstance().getApiDomain() + mHtmlUrl
                + "?device_token=" + IsmartvActivator.getInstance().getDeviceToken()
                + "&access_token=" + IsmartvActivator.getInstance().getAuthToken()
                + "&token_check=1";

        mWebView.loadUrl(appendProtocol(mHtmlUrl));
//        mWebView.loadDataWithBaseURL(null, "加载中。。", "text/html", "utf-8",null);
//        mWebView.loadDataWithBaseURL(mGetDetail.data.get("hostsUrl"), mGetDetail.data.get("description"), "text/html", "utf-8",null);
        mWebView.setVisibility(View.VISIBLE); // 加载完之后进行设置显示，以免加载时初始化效果不好看
    }

    private String appendProtocol(String host) {
        Uri uri = Uri.parse(host);
        String url = uri.toString();
        if (!uri.toString().startsWith("http://") && !uri.toString().startsWith("https://")) {
            url = "http://" + host;
        }

        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    private int getScale(){
        int PIC_WIDTH= mWebView.getRight()-mWebView.getLeft();
        Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        Double val = new Double(width)/new Double(PIC_WIDTH);
        val = val * 100d;
        return val.intValue();
    }

    @Override
    public void onResume() {
        super.onResume();
        paymentActivity.purchaseCheck(PaymentActivity.CheckType.OrderPurchase);
    }
}
