package tv.ismar.homepage.fragment;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.account.ActiveService;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.ad.AdsUpdateService;
import tv.ismar.app.ad.AdvertiseManager;
import tv.ismar.app.ad.Advertisement;
import tv.ismar.app.db.AdvertiseTable;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.widget.DaisyVideoView;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/11
 * @DESC: 广告fragment(暂且拿以前的代码堆在这里吧,有时间了再整理)
 */

public class AdvertiseActivity extends BaseActivity implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener{

    private static final int MSG_AD_COUNTDOWN = 0x01;

    private DaisyVideoView mVideoView;
    private ImageView mPicImg;
    private SeekBar mSeekBar;

    private int currentImageAdCountDown = 0;
    private boolean isStartImageCountDown = false;
    private List<AdvertiseTable> mAdsList;
    private AdvertiseManager mAdvertiseManager;
    private Advertisement mAdvertisement;
    private int mPlayIndex;
    private boolean mIsPlayingVideo = false;
    private int mCountAdTime = 0;
    private int mTotleTime = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AD_COUNTDOWN:
                    if (!mIsPlayingVideo && mCountAdTime == 0) {
                        mHandler.removeMessages(MSG_AD_COUNTDOWN);
                        mSeekBar.setProgress(mTotleTime);
                        return;
                    }
                    mSeekBar.setProgress(mTotleTime - mCountAdTime);
                    int refreshTime;
                    if (!mIsPlayingVideo) {
                        refreshTime = 1000;
                        if (currentImageAdCountDown == 0 && !isStartImageCountDown) {
                            currentImageAdCountDown = mAdsList.get(mPlayIndex).duration;
                            isStartImageCountDown = true;
                        } else {
                            if (currentImageAdCountDown == 0) {
                                mPlayIndex += 1;
                                playLaunchAd(mPlayIndex);
                                isStartImageCountDown = false;
                            } else {
                                currentImageAdCountDown--;
                            }
                        }
                        mCountAdTime--;
                        if(mCountAdTime <= 0){
                            go2HomeActivity();
                        }
                    } else {
                        refreshTime = 500;
                        mCountAdTime = getAdCountDownTime();
                    }
                    sendEmptyMessageDelayed(MSG_AD_COUNTDOWN, refreshTime);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advertise_activity_layout);
        initView();
        initListener();
        initData();
        initServer();
    }

    private void initServer(){
        startAdsService();
        startIntervalActive();
    }

    private void initView(){
        mVideoView = (DaisyVideoView) findViewById(R.id.home_ad_video);
        mPicImg = (ImageView) findViewById(R.id.home_ad_pic);
        mSeekBar = (SeekBar) findViewById(R.id.home_ad_seekbar);
    }

    private void initListener(){
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
    }

    private void initData(){
        mAdvertiseManager = new AdvertiseManager(this);
        mAdsList = mAdvertiseManager.getAppLaunchAdvertisement();
        mAdvertisement = new Advertisement(this);
        for (AdvertiseTable tab : mAdsList) {
            totalAdsMills = totalAdsMills + tab.duration * 1000;
        }
        for (AdvertiseTable adTable : mAdsList) {
            int duration = adTable.duration;
            mCountAdTime += duration;
        }
        mSeekBar.setMax(mCountAdTime);
        playLaunchAd(0);
    }

    private void playLaunchAd(final int index) {
        if(index >= mAdsList.size()){
            return;
        }
        mPlayIndex = index;
        if (!mAdsList.get(index).location.equals(AdvertiseManager.DEFAULT_ADV_PICTURE)) {
            new CallaPlay().boot_ad_play(mAdsList.get(index).title, mAdsList.get(index).media_id,
                    mAdsList.get(index).media_url, String.valueOf(mAdsList.get(index).duration));
        }
        if (mAdsList.get(index).media_type.equals(AdvertiseManager.TYPE_VIDEO)) {
            mIsPlayingVideo = true;
        }
        if (mIsPlayingVideo) {
            if (mVideoView.getVisibility() != View.VISIBLE) {
                mPicImg.setVisibility(View.GONE);
                mVideoView.setVisibility(View.VISIBLE);
            }
            mVideoView.setVideoPath(mAdsList.get(index).location);
        } else {
            if (mPicImg.getVisibility() != View.VISIBLE) {
                mVideoView.setVisibility(View.GONE);
                mPicImg.setVisibility(View.VISIBLE);
                mSeekBar.setVisibility(View.VISIBLE);
            }
            Picasso.with(this)
                    .load(mAdsList.get(index).location)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_CACHE)
                    .into(mPicImg, new Callback() {
                        @Override
                        public void onSuccess() {//图片加载成功启动倒计时
                            if (mPlayIndex == 0) {
                                mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
                            }
                            if (mAdsList.get(mPlayIndex).media_id != null) {
                                int media_id = Integer.parseInt(mAdsList.get(mPlayIndex).media_id);
                                mAdvertisement.getRepostAdUrl(media_id, "startAd");
                            }
                        }

                        @Override
                        public void onError(Exception e) {//图片加载失败启动倒计时
                            Picasso.with(AdvertiseActivity.this)
                                    .load("file:///android_asset/poster.png")
                                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_CACHE)
                                    .into(mPicImg);
                            if (mPlayIndex == 0) {
                                mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
                            }
                        }
                    });
        }
    }

    private int getAdCountDownTime() {
        if (mAdsList == null || mAdsList.isEmpty() || !mIsPlayingVideo) {
            return 0;
        }
        int totalAdTime = 0;
        int currentAd = mPlayIndex;
        if (currentAd == mAdsList.size() - 1) {
            totalAdTime = mAdsList.get(mAdsList.size() - 1).duration;
        } else {
            for (int i = currentAd; i < mAdsList.size(); i++) {
                totalAdTime += mAdsList.get(i).duration;
            }
        }
        int countTime = totalAdTime - mVideoView.getCurrentPosition() / 1000 - 1;
        if (countTime < 0) {
            countTime = 0;
        }
        return countTime;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mVideoView.start();
        if (!mHandler.hasMessages(MSG_AD_COUNTDOWN)) {//开始播放，启动倒计时
            mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
        }
        if (mAdsList.get(mPlayIndex).media_id != null) {
            int media_id = Integer.parseInt(mAdsList.get(mPlayIndex).media_id);
            mAdvertisement.getRepostAdUrl(media_id, "startAd");
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {//广告视频播放完成
        if(playNextVideo()){
            go2HomeActivity();
        }
    }

    /**
     * 播放下一个视频或跳转
     * @return false跳转到首页
     */
    private boolean playNextVideo(){
        if (mPlayIndex == mAdsList.size() - 1) {//所有广告播放完，就释放掉当前页
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
            return false;
        } else {
            mPlayIndex += 1;
            playLaunchAd(mPlayIndex);
            return true;
        }
    }

    private void go2HomeActivity(){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {//播放出现错误
        if(playNextVideo()){
            go2HomeActivity();
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mHandler!=null && mHandler.hasMessages(MSG_AD_COUNTDOWN)){
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
            mHandler = null;
        }
    }

    private void startIntervalActive() {
        Intent intent = new Intent();
        intent.setClass(this, ActiveService.class);
        startService(intent);
    }

    private void startAdsService() {
        Intent intent = new Intent();
        intent.setClass(this, AdsUpdateService.class);
        startService(intent);
    }
}
