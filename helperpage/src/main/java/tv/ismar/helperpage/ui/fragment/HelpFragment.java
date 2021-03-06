package tv.ismar.helperpage.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.VodUserAgent;
import tv.ismar.app.network.entity.TeleEntity;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.helperpage.R;
import tv.ismar.helperpage.ui.activity.HomeActivity;
import tv.ismar.helperpage.utils.DeviceUtils;
import tv.ismar.library.exception.ExceptionUtils;

/**
 * Created by huaijie on 2015/4/8.
 */
public class HelpFragment extends Fragment {
    private static final String TAG = "HelpFragment";

    private String snCode = TextUtils.isEmpty(SimpleRestClient.sn_token) ? "sn is null" : SimpleRestClient.sn_token;

    private TextView ismartvTitle;
    private TextView ismartvTel;
    private TextView tvTitle;
    private TextView tvTel;
    private TextView deviceCode;
    private Context mContext;
    private BitmapDecoder bitmapDecoder;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.sakura_fragment_help, null);
        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(mContext, R.drawable.sakura_bg_fragment, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                view.setBackgroundDrawable(bitmapDrawable);
            }
        });
        view.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                if (newFocus != null) {
                    Log.i(TAG, "newFocus: " + newFocus.toString());
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ismartvTitle = (TextView) view.findViewById(R.id.ismartv_title);
        ismartvTel = (TextView) view.findViewById(R.id.ismartv_tel);
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvTel = (TextView) view.findViewById(R.id.tv_tel);
        deviceCode = (TextView) view.findViewById(R.id.device_code);
        try {
            deviceCode.setText(" " + DeviceUtils.ipToHex());
        } catch (Exception e) {
            ExceptionUtils.sendProgramError(e);
            e.printStackTrace();
        }

        fetchTel(VodUserAgent.getModelName(), snCode);

    }

    private void fetchTel(String model, String snCode) {
        String ACTION = "getContact";
        ((HomeActivity) getActivity()).mWxApiService.FetchTel(ACTION, model, snCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<TeleEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "FetchTel error!!!");
                        ((HomeActivity) getActivity()).showPop(e);
                    }


                    @Override
                    public void onNext(List<TeleEntity> teleEntities) {
                        ismartvTitle.setText(teleEntities.get(0).getTitle() + " : ");
                        ismartvTel.setText(teleEntities.get(0).getPhoneNo());
                        tvTitle.setText(teleEntities.get(1).getTitle() + " : ");
                        tvTel.setText(teleEntities.get(1).getPhoneNo());
                    }
                });
    }

}
