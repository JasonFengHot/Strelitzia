package tv.ismar.homepage.control;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import cn.ismartv.truetime.TrueTime;
import com.orhanobut.logger.Logger;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import tv.ismar.account.IsmartvHttpLoggingInterceptor;
import tv.ismar.app.BaseControl;
import tv.ismar.app.service.TrueTimeService;
import tv.ismar.homepage.view.ChannelChangeObservable;
import tv.ismar.homepage.widget.HorizontalTabView;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/20
 * @DESC: 首页activity业务类
 */

public class HomeControl extends BaseControl{
    private static final String TAG = "HomeControl";
    public HomeControl(Context context,ControlCallBack callBack) {
        super(context, callBack);
    }

    /*监听频道tab变化*/
    public void setChannelChange(HorizontalTabView view){
        Observable.create(new ChannelChangeObservable(view))
                .throttleWithTimeout(200, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Integer position) {
                        Logger.t(TAG).d("channel change position: " + position);
                        mCallBack.callBack(TAB_CHANGE_FALG, position);//0-搜索，1-首页
                        Logger.t(TAG).d("channel change position after: " + position);
                    }
                });

        view.setOnItemClickedListener(new HorizontalTabView.OnItemClickedListener() {
            @Override
            public void onItemClicked(View v, int position) {
                mCallBack.callBack(TAB_CHANGE_FALG, position);//0-搜索，1-首页
            }
        });

    }

    /*获取当前时间*/
    public String getNowTime(){
        DateFormat format=new SimpleDateFormat("HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        long time= TrueTime.now().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return format.format(calendar.getTime());
    }

    /*同步服务器时间*/
    public void startTrueTimeService() {
        Intent intent = new Intent();
        intent.setClass(mContext, TrueTimeService.class);
        mContext.startService(intent);
    }
}
