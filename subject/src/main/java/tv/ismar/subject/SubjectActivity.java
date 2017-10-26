package tv.ismar.subject;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.InitializeProcess;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.VodUserAgent;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.app.util.SPUtils;
import tv.ismar.app.util.SystemFileUtil;
import tv.ismar.subject.fragment.MovieTVSubjectFragment;
import tv.ismar.subject.fragment.SportSubjectFragment;

import static tv.ismar.app.AppConstant.Payment.PAYMENT_SUCCESS_CODE;

/**
 * Created by liucan on 2017/3/1.
 */

public class SubjectActivity extends BaseActivity{


    public String gather_type;
    public int itemid;
    private String title;
    public String fromPage;
    public String channel;
    private SportSubjectFragment sportSubjectFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_main_activity);
        Intent intent=getIntent();
        gather_type = intent.getStringExtra("gather_type");
        itemid = intent.getIntExtra("itemid",709759);
        title = intent.getStringExtra("itemtitle");
        channel=intent.getStringExtra("channel");
        AppConstant.purchase_page = "gather";
        AppConstant.purchase_entrance_page = "gather";
        AppConstant.purchase_entrance_related_item = String.valueOf(itemid);
        AppConstant.purchase_entrance_related_title = title;
        AppConstant.purchase_entrance_keyword = title;

        fromPage = intent.getStringExtra("fromPage");
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        switch (gather_type){
            case "nbagather":
            case "premierleaguegather":
                sportSubjectFragment=new SportSubjectFragment();
                sportSubjectFragment.pk=itemid;
                sportSubjectFragment.channel=channel;
                sportSubjectFragment.from=fromPage;
                sportSubjectFragment.subjectTitle=title;
                fragmentTransaction.replace(R.id.subject_frame,sportSubjectFragment);
                break;
            case "moviegather":
                fragmentTransaction.replace(R.id.subject_frame,new MovieTVSubjectFragment());
                break;
            case "teleplaygather":
                fragmentTransaction.replace(R.id.subject_frame,new MovieTVSubjectFragment());
                break;
        }
        fragmentTransaction.commit();
        if(fromPage!=null&&fromPage.equals("launcher")){
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put(EventProperty.TYPE, "item");
            properties.put(EventProperty.PK,itemid);
            properties.put(EventProperty.TITLE, title);
            properties.put(EventProperty.POSITION,-1);
            new NetworkUtils.DataCollectionTask().execute(NetworkUtils.LAUNCHER_VOD_CLICK, properties);

            CallaPlay callaPlay = new CallaPlay();
            String province = (String) SPUtils.getValue(InitializeProcess.PROVINCE_PY, "");
            String city = (String) SPUtils.getValue(InitializeProcess.CITY, "");
            String isp = (String) SPUtils.getValue(InitializeProcess.ISP, "");
            callaPlay.app_start(IsmartvActivator.getInstance().getSnToken(),
                    VodUserAgent.getModelName(), DeviceUtils.getScreenInch(this),
                    android.os.Build.VERSION.RELEASE,
                    SimpleRestClient.appVersion,
                    SystemFileUtil.getSdCardTotal(getApplicationContext()),
                    SystemFileUtil.getSdCardAvalible(getApplicationContext()),
                    IsmartvActivator.getInstance().getUsername(), province, city, isp, fromPage,
                    DeviceUtils.getLocalMacAddress(getApplicationContext()),
                    SimpleRestClient.app, getPackageName()
            );

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("OnActivity",resultCode+" code  "+PAYMENT_SUCCESS_CODE);
        if(resultCode== PAYMENT_SUCCESS_CODE){
            if(sportSubjectFragment!=null){
                sportSubjectFragment.clearPayState();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
