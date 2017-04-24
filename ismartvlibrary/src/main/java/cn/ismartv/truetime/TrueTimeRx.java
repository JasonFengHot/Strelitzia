//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.ismartv.truetime;

import android.content.Context;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class TrueTimeRx extends TrueTime {
    private static final TrueTimeRx RX_INSTANCE = new TrueTimeRx();
    private int _retryCount = 0;

    public TrueTimeRx() {
    }

    public static TrueTimeRx build() {
        return RX_INSTANCE;
    }

    public TrueTimeRx withSharedPreferences(Context context) {
        super.withSharedPreferences(context);
        return this;
    }

    public TrueTimeRx withConnectionTimeout(int timeout) {
        super.withConnectionTimeout(timeout);
        return this;
    }

    public TrueTimeRx withLoggingEnabled(boolean isLoggingEnabled) {
        super.withLoggingEnabled(isLoggingEnabled);
        return this;
    }

    public TrueTimeRx withRetryCount(int retryCount) {
        this._retryCount = retryCount;
        return this;
    }

    public Observable<Date> initialize(List<String> ntpHosts) {
        return Observable.from(ntpHosts).flatMap(new Func1<String, Observable<Date>>() {
            public Observable<Date> call(String ntpHost) {
                return Observable.just(ntpHost).subscribeOn(Schedulers.io()).flatMap(new Func1<String, Observable<Date>>() {
                    public Observable<Date> call(String ntpHost) {
                        try {
                            TrueTimeRx.this.initialize(ntpHost);
                        } catch (IOException var3) {
                            return Observable.error(var3);
                        }

                        return Observable.just(TrueTime.now());
                    }
                }).retry((long) TrueTimeRx.this._retryCount).onErrorReturn(new Func1<Throwable, Date>() {
                    public Date call(Throwable throwable) {
                        throwable.printStackTrace();
                        return null;
                    }
                }).take(1).doOnNext(new Action1<Date>() {
                    public void call(Date date) {
                        TrueTime.cacheTrueTimeInfo();
                    }
                });
            }
        }).filter(new Func1<Date, Boolean>() {
            public Boolean call(Date date) {
                return date != null;
            }
        }).take(1);
    }
}
