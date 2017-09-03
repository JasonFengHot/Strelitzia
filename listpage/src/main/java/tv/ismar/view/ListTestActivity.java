package tv.ismar.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.entity.ChannelEntity;
import tv.ismar.listpage.R;

/**
 * Created by Administrator on 2017-09-03.
 */

public class ListTestActivity extends BaseActivity {


    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listtest);
        root = (LinearLayout) findViewById(R.id.root);
        initData();
    }

    private void initData() {
        mSkyService.apiTvChannels()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ChannelEntity[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ChannelEntity[] channelEntities) {
                        processData(channelEntities);
                    }
                });

    }

    private void processData(final ChannelEntity[] channelEntities) {
        for (int i = 0; i <channelEntities.length ; i++) {
            Button btn=new Button(this);
            btn.setText(channelEntities[i].getName());
            final int finalI = i;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent();
                    intent.setAction("tv.ismar.daisy.Filter");
                    intent.putExtra("title",channelEntities[finalI].getName());
                    intent.putExtra("channel",channelEntities[finalI].getChannel());
                    intent.putExtra("style",channelEntities[finalI].getStyle());
                    startActivity(intent);
                }
            });
            root.addView(btn);
        }
    }
}
