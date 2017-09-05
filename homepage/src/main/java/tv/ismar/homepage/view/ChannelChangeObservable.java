package tv.ismar.homepage.view;

import android.view.View;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;
import tv.ismar.homepage.widget.HorizontalTabView;

import static rx.android.MainThreadSubscription.verifyMainThread;

/**
 * Created by huibin on 28/08/2017.
 */

public class ChannelChangeObservable implements Observable.OnSubscribe<Integer> {
    final HorizontalTabView view;

    public ChannelChangeObservable(HorizontalTabView view) {
        this.view = view;
    }

    @Override
    public void call(final Subscriber<? super Integer> subscriber) {
        verifyMainThread();

        HorizontalTabView.OnItemSelectedListener listener = new HorizontalTabView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(View v, int position) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(position);
                }
            }
        };

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                view.setOnItemSelectedListener(null);
            }
        });

        view.setOnItemSelectedListener(listener);
    }
}
