package tv.ismar.homepage.view;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.entity.ChannelEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.homepage.R;
import tv.ismar.homepage.widget.HorizontalTabView;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/22
 * @DESC: 主页
 */

public class HomePageActivity extends BaseActivity {
    private Subscription channelsSub;

    private HorizontalTabView channelTab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_activity_layout);
        initViews();
    }


    private void initViews() {
        channelTab = (HorizontalTabView) findViewById(R.id.channel_tab);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchChannels();
    }


    @Override
    protected void onPause() {
        if (channelsSub != null && channelsSub.isUnsubscribed()) {
            channelsSub.unsubscribe();
        }
        super.onPause();
    }

    private void fetchChannels() {

        channelsSub = SkyService.ServiceManager.getCacheSkyService().apiTvChannels()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ChannelEntity[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ChannelEntity[] channelEntities) {
                        fillChannelTab(channelEntities);
                    }
                });
    }

    private void fillChannelTab(ChannelEntity[] channelEntities) {
        List<HorizontalTabView.Tab> tabs = new ArrayList<>();
        for (ChannelEntity entity : channelEntities){
            HorizontalTabView.Tab tab = new HorizontalTabView.Tab("", entity.getName());
            tabs.add(tab);
        }
        for (ChannelEntity entity : channelEntities){
            HorizontalTabView.Tab tab = new HorizontalTabView.Tab("", entity.getName());
            tabs.add(tab);
        }
        channelTab.addAllViews(tabs, 0);

    }
}
