package tv.ismar.player.gui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qiyi.sdk.player.AdItem;
import com.qiyi.sdk.player.IAdController;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.ad.Advertisement;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.entity.ClipEntity;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.player.OnNoNetConfirmListener;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.library.network.HttpManager;
import tv.ismar.library.util.LogUtils;
import tv.ismar.library.util.NetworkUtils;
import tv.ismar.library.util.StringUtils;
import tv.ismar.player.IsmartvPlayer;
import tv.ismar.player.R;
import tv.ismar.player.listener.EpisodeOnclickListener;
import tv.ismar.player.listener.OnMenuListItmeClickListener;
import tv.ismar.player.model.QuailtyEntity;
import tv.ismar.player.widget.AdImageDialog;
import tv.ismar.player.widget.ExitToast;

public class PlaybackFragment extends Fragment implements PlaybackService.Client.Callback,
        PlayerMenu.OnCreateMenuListener, Advertisement.OnPauseVideoAdListener, PlaybackService.ServiceCallback ,EpisodeOnclickListener,OnMenuListItmeClickListener{

    private final String TAG = "LH/PlaybackFragment";
    public static final int PAYMENT_REQUEST_CODE = 0xd6;
    public static final int PAYMENT_SUCCESS_CODE = 0x5c;

    private static final byte POP_TYPE_BUFFERING_LONG = 1;// 播放过程中,缓冲时间过长
    private static final byte POP_TYPE_PLAYER_ERROR = 3;// 底层onError回调
    private static final byte POP_TYPE_PLAYER_NET_ERROR = 4;// 首次进入，播放器初始化50S以后未见onPrepared回调

    private static final String ARG_PK = "ARG_PK";
    private static final String ARG_SUB_PK = "ARG_SUB_PK";
    private static final String ARG_SOURCE = "ARG_SOURCE";
    private static final String HISTORYCONTINUE = "上次放映：";
    private static final String PlAYSTART = "即将放映：";
    private static final int MSG_SEK_ACTION = 103;
    private static final int MSG_AD_COUNTDOWN = 104;
    private static final int MSG_SHOW_BUFFERING_LONG = 105;
    private static final int MSG_UPDATE_PROGRESS = 106;
    private static final int MSG_HIDE_PANEL = 107;
    private static final int EVENT_CLICK_VIP_BUY = 0x10;
    private static final int EVENT_CLICK_KEFU = 0x11;
    private static final int EVENT_COMPLETE_BUY = 0x12;
    private static final int EVENT_PLAY_EXIT = 0x13;
    // 以下为弹出菜单id
    private static final int MENU_QUALITY_ID_START = 0;// 码率起始id
    private static final int MENU_QUALITY_ID_END = 8;// 码率结束id
    private static final int MENU_TELEPLAY_ID_START = 100;// 电视剧等多集影片起始id
    private static final int MENU_KEFU_ID = 20;// 客服中心
    private static final int MENU_RESTART = 30;// 从头播放

    private int extraItemPk = 0;// 当前影片pk值,通过/api/item/{pk}可获取详细信息
    private int extraSubItemPk = 0;// 当前多集片pk值,通过/api/subitem/{pk}可获取详细信息
    private String extraSource = "";
    // 播放器UI
    private FrameLayout player_container;
    private SurfaceView player_surface;
    private LinearLayout panel_layout;
    private TextView player_timer, player_quality, player_title;
    private PlayerSettingMenu settingMenu;
    private View parentView;
    private SeekBar player_seekBar;
    private ImageView player_logo_image;
    private TextView ad_count_text, ad_vip_text;
    private View ad_vip_layout;
    private ListView player_menu;
    private LinearLayout player_buffer_layout;
    private LinearLayout player_top_panel;
    private ImageView player_buffer_img;
    private TextView player_buffer_text;
    private ImageView player_previous, player_forward, player_start;
    private ImageView player_shadow;

    private AnimationDrawable animationDrawable;
    private Animation panelShowAnimation;
    private Animation panelHideAnimation;
    private Animation top_fly_up,top_fly_down;
    private AdImageDialog adImageDialog;
    private Advertisement mAdvertisement;

    private PlaybackService.Client mClient;
    public PlaybackService mPlaybackService;
    private int mCurrentPosition;
    private boolean sharpSetupKeyClick = false; // 夏普电视“设置”Activity样式为Dialog样式
    public boolean mounted;// 播放影片时插拔SD卡，夏普585会弹出系统Dialog，播放器会进入onPause,但是仍需正常播放
    private boolean mIsExiting;// 正在退出播放器页面
    private boolean isSeeking = false;// 空鼠拖动进度条,左右键快进快退,切换码率
    private boolean canShowMenuOrPannel = false;// 是否可以显示菜单或控制栏
    private boolean closePopup = false;// 网速由不正常到正常时判断，关闭弹窗后不做任何操作
    private boolean isClickBufferLong;// 夏普s3相关适配，限速切换码率后，恢复网速，导致timerStart无法正常开启
    private boolean mIsOnPaused = false;// 调用pause()之后部分机型会执行BufferStart(701)
    private int historyPosition;// 人为操控断网，再连接网络进入播放器，可能导致进入播放器起播后，网络获取到的是未连接情况
    private boolean mIsInAdDetail;// 是否在广告详情页
    private boolean mIsClickKefu;// 点击客服中心，返回不应再加载广告
    private int mAdCount;// 广告总倒计时，广告倒计时从3位数变为2位数时，2位数前面添0
    private boolean isErrorPopUp;// 播放器同时回调多个onError情况

    private PlaybackHandler mHandler;
    private boolean backpress=false;
    private boolean isqiyi;
    private String contentMode="";
    private boolean isPlayExitLayerShow;

    public PlaybackFragment() {
        // Required empty public constructor
    }

    public static PlaybackFragment newInstance(int pk, int subPk, String source,boolean isqiyi,String contentMode) {
        PlaybackFragment fragment = new PlaybackFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PK, pk);
        args.putInt(ARG_SUB_PK, subPk);
        args.putString(ARG_SOURCE, source);
        args.putBoolean(PageIntentInterface.QIYIFLAG, isqiyi);
        args.putString("contentMode",contentMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            LogUtils.e(TAG, "PlayerFragment error.");
            getActivity().finish();
            return;
        }
        if (!(getActivity() instanceof BaseActivity)) {
            getActivity().finish();
            LogUtils.e(TAG, "Activity must be extends BaseActivity.");
            return;
        }
        contentMode=bundle.getString("contentMode");
        extraItemPk = bundle.getInt(ARG_PK);
        extraSubItemPk = bundle.getInt(ARG_SUB_PK);
        extraSource = bundle.getString(ARG_SOURCE);
        isqiyi = bundle.getBoolean(PageIntentInterface.QIYIFLAG,false);
        panelShowAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.fly_up);
        panelHideAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.fly_down);
        top_fly_down=AnimationUtils.loadAnimation(getActivity(),R.anim.top_fly_down);
        top_fly_up=AnimationUtils.loadAnimation(getActivity(),R.anim.top_fly_up);
        mAdvertisement = new Advertisement(getActivity());
        mHandler = new PlaybackHandler(this);
        mClient = new PlaybackService.Client(getActivity(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtils.i(TAG, "onCreateView");
        View contentView = inflater.inflate(R.layout.fragment_playback, container, false);
        parentView=contentView;
        initView(contentView);
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isSetupNetClick && mPlaybackService != null) {
            isSetupNetClick = false;
            mPlaybackService.resetPreload();
            mPlaybackService.preparePlayer(extraItemPk, extraSubItemPk, extraSource);
            showBuffer(null);
            return;
        }
        isPlayExitLayerShow=false;
        if(backpress && mPlaybackService != null){
            mPlaybackService.startPlayer();
            showBuffer(null);
            timerStart(0);
        }
        backpress=false;
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtils.i(TAG, "onStart > setup : " + sharpSetupKeyClick + " sdcard : " + mounted);
        registerConnectionReceiver();
        registerClosePlayerReceiver();
        mAdvertisement.setOnPauseVideoAdListener(this);
        mClient.connect();
        showBuffer(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtils.i(TAG, "onPause > setup : " + sharpSetupKeyClick + " sdcard : " + mounted);
//        if (sharpSetupKeyClick || mounted) {
//            sharpSetupKeyClick = false;
//            mounted = false;
//            return;
//        }
//        if (mPlaybackService != null && !mPlaybackService.isPlayerStopping()&&!backpress) {
//            // 不能放在onStop()中，SmartPlayer在该生命周期中调用会有onError回调产生
//            mPlaybackService.stopPlayer(false);
//        }
        // handler消息需要立即删除，广告请求停止
//        mHandler.removeCallbacksAndMessages(null);
//        mPlaybackService.addHistory(mCurrentPosition, true);
//        if (mAdvertisement != null) {
//            mAdvertisement.stopSubscription();
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtils.i(TAG, "onStop > setup : " + sharpSetupKeyClick + " sdcard : " + mounted);
//        if (sharpSetupKeyClick || mounted) {r
//            sharpSetupKeyClick = false;
//            mounted = false;
//            return;
//        }
        if (adImageDialog != null && adImageDialog.isShowing()) {
            adImageDialog.dismiss();
        }
        if (mPlaybackService != null && !mPlaybackService.isPlayerStopping()&&!backpress) {
            mPlaybackService.stopPlayer(true);
        }
        if (mPlaybackService != null) {
            mPlaybackService.setCallback(null);
            if (!mPlaybackService.isPlayingAd() && !isPlayExitLayerShow && mCurrentPosition > 0) {
                mPlaybackService.addHistory(mCurrentPosition, false);// 在非按返回键退出应用时需添加历史记录，此时无需发送至服务器，addHistory不能统一写到此处
            }
        }
        mHandler.removeCallbacksAndMessages(null);

        unregisterConnectionReceiver();
        unRegisterClosePlayerReceiver();
        if (mAdvertisement != null) {
            mAdvertisement.stopSubscription();
            mAdvertisement.setOnPauseVideoAdListener(null);
        }
        if (isPopWindowShow()) {
            // 底层报错导致Activity被销毁，如果再次显示弹出框，会报错
            popDialog.dismiss();
            popDialog = null;
        }
        if(settingMenu!=null){
            settingMenu.dismiss();
            settingMenu.removeAllMsg();
            settingMenu=null;
        }
        mClient.disconnect();

    }

    @Override
    public void onConnected(PlaybackService service) {
        mPlaybackService = service;
        mPlaybackService.setCallback(this);
        mPlaybackService.setSurfaceView(player_surface);
        mPlaybackService.setQiyiContainer(player_container);
        player_shadow.setVisibility(View.VISIBLE);
        if (mPlaybackService.isPreload() && !isqiyi) {
            // 视云影片详情页预加载
            mPlaybackService.startPlayWhenPrepared();
        } else {
            if (mIsClickKefu) {
                // 点击客服后返回，不加载广告
                mPlaybackService.onResumeFromKefu();
            } else {
                mPlaybackService.resetPreload();
                mPlaybackService.preparePlayer(extraItemPk, extraSubItemPk, extraSource);
            }
        }
    }

    @Override
    public void onDisconnected() {
        mPlaybackService = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.i(TAG, "resultCode:" + resultCode + " request:" + requestCode);
        // 从播放完成页面退出播放器
        if (resultCode == 200) {
            if (mPlaybackService.isPreview()) {
                mPlaybackService.logExpenseVideoPreview(mCurrentPosition, "cancel");
            }
            ExitToast.createToastConfig().dismiss();
            mHandler.removeCallbacksAndMessages(null);
            timerStop();
            removeBufferingLongTime();
            hideBuffer();
            hidePanel();
            mPlaybackService.addHistory(mCurrentPosition,true);
            closeActivity("source");
            return;
        }
        mPlaybackService.initUserInfo();
        if (requestCode == PAYMENT_REQUEST_CODE) {
            if (resultCode == PAYMENT_SUCCESS_CODE) {
                // 成功购买后继续播放
                ad_vip_layout.setVisibility(View.GONE);
                player_shadow.setVisibility(View.VISIBLE);
                showBuffer(null);
                mPlaybackService.preparePlayer(extraItemPk, extraSubItemPk, extraSource);
                settingMenu=null;
            } else {
                // 没有购买，退出播放器
                closeActivity("finish");
            }
        }
    }

    private void closeActivity(String to) {
        mIsExiting = true;
        if (mPlaybackService != null) {
            mPlaybackService.logVideoExit(mCurrentPosition, to);
            mPlaybackService.stopPlayer(true);
        }
        getActivity().finish();
    }

    private void initView(View contentView) {
        player_container = (FrameLayout) contentView.findViewById(R.id.player_container);
        player_surface = (SurfaceView) contentView.findViewById(R.id.player_surface);
        panel_layout = (LinearLayout) contentView.findViewById(R.id.panel_layout);
        player_top_panel= (LinearLayout) contentView.findViewById(R.id.player_top_panel);
        player_timer = (TextView) contentView.findViewById(R.id.player_timer);
        player_quality = (TextView) contentView.findViewById(R.id.player_quality);
        player_title = (TextView) contentView.findViewById(R.id.player_title);

        player_seekBar = (SeekBar) contentView.findViewById(R.id.player_seekBar);
        player_logo_image = (ImageView) contentView.findViewById(R.id.player_logo_image);
        ad_vip_layout = contentView.findViewById(R.id.ad_vip_layout);
        ad_count_text = (TextView) contentView.findViewById(R.id.ad_count_text);
        ad_vip_text = (TextView) contentView.findViewById(R.id.ad_vip_text);
        player_menu = (ListView) contentView.findViewById(R.id.player_menu);
        player_buffer_layout = (LinearLayout) contentView.findViewById(R.id.player_buffer_layout);
        player_buffer_img = (ImageView) contentView.findViewById(R.id.player_buffer_img);
        player_buffer_text = (TextView) contentView.findViewById(R.id.player_buffer_text);
        player_previous = (ImageView) contentView.findViewById(R.id.player_previous);
        player_forward = (ImageView) contentView.findViewById(R.id.player_forward);
        player_start = (ImageView) contentView.findViewById(R.id.player_start);
        player_shadow = (ImageView) contentView.findViewById(R.id.player_shadow);

        player_previous.setOnClickListener(onClickListener);
        player_forward.setOnClickListener(onClickListener);
        player_start.setOnClickListener(onClickListener);
        player_container.setOnHoverListener(onHoverListener);
        player_container.setOnClickListener(onClickListener);
        player_surface.setOnHoverListener(onHoverListener);
        player_surface.setOnClickListener(onClickListener);
        ad_vip_text.setOnClickListener(onClickListener);
        player_seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        player_buffer_img.setBackgroundResource(R.drawable.module_loading);
        animationDrawable = (AnimationDrawable) player_buffer_img.getBackground();
        ad_vip_text.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        ad_vip_text.setTextColor(getResources().getColor(R.color.module_color_focus));
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        ad_vip_text.setTextColor(getResources().getColor(R.color.module_color_white));
                        break;
                }
                return false;
            }
        });
        ad_vip_text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ad_vip_text.setTextColor(getResources().getColor(R.color.module_color_focus));
                } else {
                    ad_vip_text.setTextColor(getResources().getColor(R.color.module_color_white));
                }
            }
        });

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.player_previous) {
                previousClick(v);
            } else if (i == R.id.player_forward) {
                forwardClick(v);
            } else if (i == R.id.player_start) {
                playPauseVideo();
            } else if (i == R.id.player_surface || i == R.id.player_container) {
                if (mIsExiting || mPlaybackService == null || !mPlaybackService.isPlayerPrepared()
                        || isPopWindowShow() || !canShowMenuOrPannel) {
                    return;
                }
                if (isMenuShow()) {
                    hideMenu();
                    return;
                }
                playPauseVideo();
            } else if (i == R.id.ad_vip_text) {
                goOtherPage(EVENT_CLICK_VIP_BUY);
            }

        }
    };

    private View.OnHoverListener onHoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            if (mIsExiting || mPlaybackService == null || !mPlaybackService.isPlayerPrepared()
                    || !canShowMenuOrPannel || mPlaybackService.getItemEntity().getLiveVideo()) {
                return true;
            }
            int what = event.getAction();
            switch (what) {
                case MotionEvent.ACTION_HOVER_MOVE:
                //    showPannelDelayOut();
                    break;
            }
            return false;
        }
    };

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mPlaybackService == null || mPlaybackService.getItemEntity() == null
                    || mPlaybackService.getItemEntity().getLiveVideo() || mIsExiting) {
                return;
            }
            updateTimer(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (mPlaybackService.getItemEntity() == null || mPlaybackService.getItemEntity().getLiveVideo()) {
                return;
            }
            timerStop();
            // 拖动进度条是需要一直显示Panel
            mHandler.removeMessages(MSG_HIDE_PANEL);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mPlaybackService.getItemEntity() == null || mPlaybackService.getMediaPlayer() == null) {
                return;
            }
            isSeeking = true;
            showBuffer(null);
            int seekProgress = seekBar.getProgress();
            int maxSeek = mPlaybackService.getMediaPlayer().getDuration() - 3 * 1000;
            if (seekProgress >= maxSeek) {
                seekProgress = maxSeek;
            }
            mCurrentPosition = seekProgress;
            mPlaybackService.getMediaPlayer().seekTo(seekProgress);
        }
    };

    @Override
    public void loadPauseAd(List<AdElementEntity> adList) {
        if (adList == null || adList.isEmpty() || mIsExiting) {
            LogUtils.i(TAG, "Get pause ad null.");
            return;
        }
        LogUtils.i(TAG, "Show pause ad.");
        // 视频暂停广告
        adImageDialog = new AdImageDialog(getActivity(), R.style.PauseAdDialog, adList);
        try {
            adImageDialog.show();
        } catch (android.view.WindowManager.BadTokenException e) {
            LogUtils.i(TAG, "Pause advertisement dialog show error.");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMenuClicked(PlayerMenu playerMenu, int id) {
        if (mPlaybackService == null || mPlaybackService.getMediaPlayer() == null || mPlaybackService.getItemEntity() == null) {
            return false;
        }
        boolean ret = false;
        if (id > MENU_QUALITY_ID_START && id <= MENU_QUALITY_ID_END) {
            if (!NetworkUtils.isConnected(getActivity())) {
                ((BaseActivity) getActivity()).showNoNetConnectDialog(null);
                mPlaybackService.pausePlayer();
                LogUtils.e(TAG, "Network error switch quality.");
                return true;
            }
            // id值为quality值+1
            int qualityValue = id - 1;
            final ClipEntity.Quality clickQuality = ClipEntity.Quality.getQuality(qualityValue);
            if (clickQuality == null || clickQuality == mPlaybackService.getCurrentQuality()) {
                // 为空或者点击的码率和当前设置码率相同
                return false;
            }
            mIsOnPaused = false;// 暂停以后切换画质
            if (mPlaybackService.getMediaPlayer().getPlayerMode() == IsmartvPlayer.MODE_SMART_PLAYER) {
                timerStop();
                showBuffer(null);
            }
            if (!mPlaybackService.getItemEntity().getLiveVideo()) {
                mCurrentPosition = mPlaybackService.getMediaPlayer().getCurrentPosition();
            }
            // 点击菜单后延迟400ms处理，菜单不会有卡顿现象
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPlaybackService.switchQuality(mCurrentPosition, clickQuality);
                    updateQuality(clickQuality);
                }
            }, 400);
            ret = true;
        } else if (id > MENU_TELEPLAY_ID_START) {
            // id值为subItem pk值
            if (id == mPlaybackService.getSubItemPk()) {
                return false;
            }
            if (!NetworkUtils.isConnected(getActivity())) {
                ((BaseActivity) getActivity()).showNoNetConnectDialog(null);
                mPlaybackService.pausePlayer();
                LogUtils.e(TAG, "Network error switch quality.");
                return true;
            }
            for (ItemEntity subItem : mPlaybackService.getItemEntity().getSubitems()) {
                if (subItem.getPk() == id) {
                    mPlaybackService.logVideoExit(mCurrentPosition, "next");
                    timerStop();
                    final ItemEntity subItemDelay = subItem;
                    // 点击菜单后延迟400ms处理，菜单不会有卡顿现象
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ItemEntity.Clip clip = subItemDelay.getClip();
                            mPlaybackService.getItemEntity().setTitle(subItemDelay.getTitle());
                            mPlaybackService.getItemEntity().setClip(clip);
                            player_logo_image.setVisibility(View.GONE);

                            mPlaybackService.stopPlayer(false);// 此处不能设置为true
                            showBuffer(PlAYSTART + mPlaybackService.getItemEntity().getTitle());
                            updateTitle(subItemDelay.getTitle());
                            mPlaybackService.switchTelevision(mCurrentPosition, subItemDelay.getPk(), clip.getUrl());

                            mCurrentPosition = 0;
                        }
                    }, 400);
                    ret = true;
                    break;
                }
            }
        } else if (id == MENU_KEFU_ID) {
            mCurrentPosition = mPlaybackService.getMediaPlayer().getCurrentPosition();
            timerStop();
            // 点击菜单后延迟400ms处理，菜单不会有卡顿现象
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    goOtherPage(EVENT_CLICK_KEFU);
                }
            }, 400);
            ret = true;
        } else if (id == MENU_RESTART) {
            isSeeking = true;
            mCurrentPosition = 0;
            player_seekBar.setProgress(0);
            mPlaybackService.getMediaPlayer().seekTo(0);
            showPannelDelayOut();
            showBuffer(null);
            ret = true;
        }
        return ret;
    }

    @Override
    public void onMenuCloseed(PlayerMenu playerMenu) {

    }

    @Override
    public void tipsToShowMiddleAd(boolean end) {
        if (!end) {
            // 即将进入爱奇艺广告,不可快进操作
            canShowMenuOrPannel = false;
            hidePanel();
            hideMenu();
        } else {
            canShowMenuOrPannel = true;
        }
    }
    int AdIndex=0;
    @Override
    public void showAdvertisement(boolean isShow) {
        if (isShow) {
            canShowMenuOrPannel = false;
            player_shadow.setVisibility(View.GONE);
            hideBuffer();
            ad_vip_layout.setVisibility(View.VISIBLE);
            ad_vip_text.setFocusable(true);
            ad_vip_text.requestFocus();
            if (mPlaybackService != null && mPlaybackService.getMediaPlayer() != null) {
                mAdCount = mPlaybackService.getMediaPlayer().getAdCountDownTime() / 1000;
            }
            mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
            Advertisement advertisement=new Advertisement(getActivity());
            advertisement.getQiantieAdUrl(AdIndex,"qiantieAd");
            AdIndex++;
        } else {
            Log.i("AdeverSende","play ad!!!");
            mAdCount = 0;
            canShowMenuOrPannel = true;
            ad_vip_layout.setVisibility(View.GONE);
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
            AdIndex=0;
        }

    }

    @Override
    public void updatePlayerStatus(PlaybackService.PlayerStatus status, Object args) {
        if (status == PlaybackService.PlayerStatus.RESPONSE_ERROR) {
            Throwable throwable = (Throwable) args;
            BaseActivity baseActivity = ((BaseActivity) getActivity());
            if (baseActivity == null) {
                return;
            }
            if (NetworkUtils.isConnected(getActivity())) {
                baseActivity.showNetWorkErrorDialog(throwable);
            } else {
                baseActivity.showNoNetConnectDialog(onNoNetConfirmListener);
            }
            hideBuffer();
            return;
        }
        if (mPlaybackService == null || mPlaybackService.getMediaPlayer() == null) {
            return;
        }
        switch (status) {
            case CREATING:
                // 重新创建播放器，初始化变量
                player_logo_image.setVisibility(View.GONE);
                ad_vip_layout.setVisibility(View.GONE);
                hideMenu();
                hidePanel();
                sharpSetupKeyClick = false; // 夏普电视“设置”Activity样式为Dialog样式
                mounted = false;// 播放影片时插拔SD卡，夏普585会弹出系统Dialog，播放器会进入onPause,但是仍需正常播放
                mIsExiting = false;// 正在退出播放器页面
                isSeeking = false;// 空鼠拖动进度条,左右键快进快退,切换码率
                canShowMenuOrPannel = false;// 是否可以显示菜单或控制栏
                closePopup = false;// 网速由不正常到正常时判断，关闭弹窗后不做任何操作
                isClickBufferLong = false;// 夏普s3相关适配，限速切换码率后，恢复网速，导致timerStart无法正常开启
                mIsOnPaused = false;// 调用pause()之后部分机型会执行BufferStart(701)
                historyPosition = 0;// 人为操控断网，再连接网络进入播放器，可能导致进入播放器起播后，网络获取到的是未连接情况
                mIsInAdDetail = false;// 是否在广告详情页
                break;
            case START:
                if (mCurrentPosition > 0) {
                    historyPosition = mCurrentPosition;
                }
                player_shadow.setVisibility(View.GONE);
                hideBuffer();
                updateTitle(mPlaybackService.getItemEntity().getTitle());
                Log.i("PlayerTitle",mPlaybackService.getItemEntity().getTitle());
                updateQuality(mPlaybackService.getCurrentQuality());
                updateTimer(mCurrentPosition);
                player_seekBar.setMax(mPlaybackService.getMediaPlayer().getDuration());
                player_seekBar.setPadding(0, 0, 0, 0);
                String logo = mPlaybackService.getItemEntity().getLogo();
                LogUtils.i(TAG, "clipLength:" + mPlaybackService.getMediaPlayer().getDuration() + " logo:" + logo);
                if (!StringUtils.isEmpty(logo) && mPlaybackService.getMediaPlayer().getPlayerMode() == IsmartvPlayer.MODE_SMART_PLAYER) {
                    Picasso.with(getActivity()).load(logo).into(player_logo_image, new Callback() {
                        @Override
                        public void onSuccess() {
                            player_logo_image.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
                }
                break;
            case PLAY:
                canShowMenuOrPannel = true;
                LogUtils.i(TAG, "Play : " + isSeeking);
                if (mPlaybackService.getItemEntity().getLiveVideo()) {
                    hideBuffer();
                    isSeeking = false;
                }
                if (!mPlaybackService.isPlayingAd()) {
                    updatePlayerPause();
                    if (!isSeeking) {
                        timerStart(0);
                    }
                }
                if (adImageDialog != null && adImageDialog.isShowing()) {
                    adImageDialog.dismiss();
                }
                break;
            case PAUSE:
                timerStop();
                updatePlayerPause();
                if(!backpress)
                showPannelDelayOut();
                break;
            case SEEK_COMPLETED:
                if (!mPlaybackService.getMediaPlayer().isPlaying()&&!isPlayExitLayerShow) {
                    mPlaybackService.startPlayer();
                }
                timerStart(500);
                break;
            case COMPLETED:
                hideMenu();
                hidePanel();
                timerStop();
                boolean isPreview = (boolean) args;
                if (isPreview) {
                    // 试看影片保存历史记录应该是试看片源的duration,mCurrentPosition值不能改变
                    if (mPlaybackService.getItemEntity().getLiveVideo() && "sport".equals(mPlaybackService.getItemEntity().getContentModel())) {
                        closeActivity("finish");
                    } else {
                        goOtherPage(EVENT_COMPLETE_BUY);
                    }
                } else {
                    ItemEntity[] subItems = mPlaybackService.getItemEntity().getSubitems();
                    if (subItems != null) {
                        for (int i = 0; i < subItems.length; i++) {
                            if (mPlaybackService.getSubItemPk() == subItems[i].getPk() && i < subItems.length - 1) {
                                ItemEntity nextItem = subItems[i + 1];
                                if (nextItem != null && nextItem.getClip() != null) {
                                    mPlaybackService.logVideoExit(mCurrentPosition, "next");

                                    ItemEntity.Clip clip = nextItem.getClip();
                                    mPlaybackService.getItemEntity().setTitle(nextItem.getTitle());
                                    mPlaybackService.getItemEntity().setClip(clip);
                                    player_logo_image.setVisibility(View.GONE);

                                    mPlaybackService.stopPlayer(true);
                                    player_surface.setVisibility(View.GONE);
                                    player_container.setVisibility(View.GONE);
                                    player_container.removeAllViews();
                                    mPlaybackService.setSurfaceView(player_surface);
                                    mPlaybackService.setQiyiContainer(player_container);
                                    showBuffer(PlAYSTART + mPlaybackService.getItemEntity().getTitle());
                                    updateTitle(nextItem.getTitle());
                                    mPlaybackService.switchTelevision(mCurrentPosition, nextItem.getPk(), clip.getUrl());
                                    mCurrentPosition = 0;
                                    return;
                                }
                            }
                        }
                    }
                    PageIntent pageIntent=new PageIntent();
                    pageIntent.toPlayFinish(this,mPlaybackService.getItemEntity().getContentModel(),extraItemPk,100,mPlaybackService.hasHistory,"player");
//                    String itemJson = null;
//                    try {
//                        itemJson = JacksonUtils.toJson(mPlaybackService.getItemEntity());
//                    } catch (JsonProcessingException e) {
//                        e.printStackTrace();
//                    }

//                    Intent intent = new Intent("tv.ismar.daisy.PlayFinished");
//                    intent.putExtra("itemJson", itemJson);
//                    intent.putExtra("source", extraSource);
//                    startActivity(intent);
                    // 播放完成，下次从头播放
                    mCurrentPosition = 0;
                }
                break;
            case ERROR:
                String errMsg = (String) args;
                LogUtils.e(TAG, "onError:" + errMsg);
                if (mIsExiting || isDetached() || isErrorPopUp) {
                    return;
                }
                removeBufferingLongTime();
                isErrorPopUp = true;
                if (mCurrentPosition > 0) {
                    mPlaybackService.addHistory(mCurrentPosition, true);
                }
                if (isPopWindowShow()) {
                    popDialog.dismiss();
                }
                showPopup(POP_TYPE_PLAYER_ERROR);
                break;
            case S3DEVICE_VIDEO_SIZE:
                if (!mIsExiting && isClickBufferLong) {
                    isClickBufferLong = false;
                    timerStart(0);
                }
                break;
            case CONTINUE_BUFFERING:
                if (mIsExiting || isPopWindowShow() || isDetached()) {
                    return;
                }
                int position = (int) args;
                showBuffer(HISTORYCONTINUE + getTimeString(position));
                break;
        }

    }

    @Override
    public void showBuffering(boolean showBuffer) {
        if (mPlaybackService == null || mPlaybackService.getMediaPlayer() == null) {
            return;
        }
        LogUtils.i(TAG, "showBuffering : " + showBuffer);
        if (showBuffer && !isSeeking) {
            showBuffer(null);
        } else if (!showBuffer) {
            hideBuffer();
        }
    }

    /**
     *
     * @param value bufferd value,unit seccond
     */
    int direction=0;
    @Override
    public void onBufferUpdate(long value) {
        if (mPlaybackService == null || mPlaybackService.getMediaPlayer() == null) {
            return;
        }
        Log.i("cacheTime","value: "+value+"Max: "+player_seekBar.getMax());
         player_seekBar.setSecondaryProgress((int) value*1000);

        /**
         * do some uo update action.
         */
    }

    private static boolean isQuit = false;
    private Timer quitTimer = new Timer();

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mIsExiting) {
            return true;
        }
        if ("lcd_s3a01".equals(getModelName())) {
            if (keyCode == 707 || keyCode == 774 || keyCode == 253) {
                sharpSetupKeyClick = true;
            }
        } else if ("lx565ab".equals(getModelName())) {
            if (keyCode == 82 || keyCode == 707 || keyCode == 253) {
                sharpSetupKeyClick = true;
            }
        } else if ("lcd_xxcae5a_b".equals(getModelName())) {
            if (keyCode == 497 || keyCode == 498 || keyCode == 490) {
                sharpSetupKeyClick = true;
            }
        } else {
            if (keyCode == 223 || keyCode == 499 || keyCode == 480) {
                sharpSetupKeyClick = true;
            }
        }
        LogUtils.i(TAG, "onKeyDown : " + keyCode);
        if (mPlaybackService == null || mPlaybackService.getMediaPlayer() == null || !mPlaybackService.isPlayerPrepared()) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                backpress = true;
                if (mPlaybackService != null && mPlaybackService.getMediaPlayer() != null) {
                    mPlaybackService.stopPlayer(true);
                }
                getActivity().finish();
            }
            return true;
        }
        if (mPlaybackService.isPlayingAd()) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                    || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
                return false;

            } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                mIsExiting = true;
                getActivity().finish();
                return true;
            }
        }
        if (isMenuShow()) {
            return true;
        }
        IAdController adController = mPlaybackService.getMediaPlayer().getAdController();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (mPlaybackService.isPlayingAd()) {
                    return true;
                }
                showPannelDelayOut();
                return true;
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                // TODO 暂停广告按下消除
                // TODO 悦享看广告一定时间后可以消除
                LogUtils.d(TAG, "DOWN:" + adController + " onPaused:" + mIsOnPaused);
                //隐藏暂停广告
                if (adController != null && mIsOnPaused) {
                    LogUtils.d(TAG, "Invisible pause ad.");
                    adController.hideAd(AdItem.AdType.PAUSE);
                }
                //跳过悦享看广告
                else if (adController != null && adController.isEnableSkipAd()) {
                    LogUtils.d(TAG, "Jump over ad.");
                    adController.skipAd();
                }
                showMenu(0);
                return true;
            case KeyEvent.KEYCODE_BACK:
//                if (!isPopWindowShow() && !mPlaybackService.isPlayingAd()) {
//                    if (!isQuit) {
//                        isQuit = true;
//                        ExitToast.createToastConfig().show(getActivity().getApplicationContext(), 5000);
//                        TimerTask task = new TimerTask() {
//                            @Override
//                            public void run() {
//                                isQuit = false;
//                            }
//                        };
//                        quitTimer.schedule(task, 5000);
//                    } else {
//                        if (mPlaybackService.isPreview()) {
//                            mPlaybackService.logExpenseVideoPreview(mCurrentPosition, "cancel");
//                        }
//                        ExitToast.createToastConfig().dismiss();
//                        mHandler.removeCallbacksAndMessages(null);
//                        timerStop();
//                        removeBufferingLongTime();
//                        hideBuffer();
//                        hidePanel();
//                        closeActivity("source");
//                    }
                    backpress = true;
                    removeBufferingLongTime();
                    timerStop();
                    mPlaybackService.pausePlayer();
                    goOtherPage(EVENT_PLAY_EXIT);
//                    return true;
//                }
//                if (isPopWindowShow()) {
//                    return true;
//                }
                    LogUtils.d(TAG, "BACK:" + adController);
                    if (adController != null && mIsInAdDetail) {
                        // TODO 广告详情页面返回键后继续播放视频
                        LogUtils.d(TAG, "From ad detail to player.");
                        mIsInAdDetail = false;
                        adController.hideAd(AdItem.AdType.CLICKTHROUGH);
                        ad_vip_layout.setVisibility(View.VISIBLE);
                        ad_vip_text.setFocusable(true);
                        ad_vip_text.requestFocus();
                        return true;
                    }
                    if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
                        mHandler.removeMessages(MSG_AD_COUNTDOWN);
                    }
//                closeActivity("source");
                return true;
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (isMenuShow() || isPopWindowShow() || mPlaybackService.isPlayingAd() || mPlaybackService.getItemEntity().getLiveVideo()) {
                    return true;
                }
                playPauseVideo();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                if (isMenuShow() || isPopWindowShow() || mPlaybackService.isPlayingAd() || mPlaybackService.getItemEntity().getLiveVideo()) {
                    return true;
                }
                if (!mPlaybackService.getMediaPlayer().isPlaying()&&!isPlayExitLayerShow) {
                    mIsOnPaused = false;
                    mPlaybackService.getMediaPlayer().start();
                    hidePanel();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                if (isMenuShow() || isPopWindowShow() || mPlaybackService.isPlayingAd() || mPlaybackService.getItemEntity().getLiveVideo()) {
                    return true;
                }
                if (mPlaybackService.getMediaPlayer().isPlaying()) {
                    mIsOnPaused = true;
                    mPlaybackService.getMediaPlayer().pause();
                    if (mPlaybackService.getMediaPlayer().getPlayerMode() == IsmartvPlayer.MODE_SMART_PLAYER) {
                        mAdvertisement.fetchVideoStartAd(mPlaybackService.getItemEntity(), Advertisement.AD_MODE_ONPAUSE, extraSource);
                    }
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                if (isMenuShow() || isPopWindowShow() || mPlaybackService.isPlayingAd() || mPlaybackService.getItemEntity().getLiveVideo()) {
                    return true;
                }
                previousClick(null);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_FORWARD:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                if (mPlaybackService.isPlayingAd()) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        // TODO 前贴片,中插广告按右键跳转至图片或H5,需要指明类型
                        LogUtils.d(TAG, "RIGHT:" + adController);
                        // 从前贴/中插广告跳转到图片或H5
                        if (adController != null && adController.isEnableClickThroughAd()) {
                            LogUtils.d(TAG, "Jump to ad detail.");
                            mIsInAdDetail = true;
                            ad_vip_layout.setVisibility(View.GONE);
                            adController.showAd(AdItem.AdType.CLICKTHROUGH);
                        }
                    }
                    return true;
                }
                if (isMenuShow() || isPopWindowShow() || mPlaybackService.getItemEntity().getLiveVideo()) {
                    return true;
                }
                forwardClick(null);
                return true;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (isSeeking) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_FORWARD:
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
                    return true;
            }
        }
        return false;
    }
    @Override
    public void onItemClick(int id) {
        if (id == mPlaybackService.getSubItemPk()) {
            return ;
        }
        if (!NetworkUtils.isConnected(getActivity())) {
            ((BaseActivity) getActivity()).showNoNetConnectDialog(null);
            mPlaybackService.pausePlayer();
            LogUtils.e(TAG, "Network error switch quality.");
        }
        for (ItemEntity subItem : mPlaybackService.getItemEntity().getSubitems()) {
            if (subItem.getPk() == id) {
                mPlaybackService.logVideoExit(mCurrentPosition, "next");
                timerStop();
                final ItemEntity subItemDelay = subItem;
                // 点击菜单后延迟400ms处理，菜单不会有卡顿现象
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ItemEntity.Clip clip = subItemDelay.getClip();
                        mPlaybackService.getItemEntity().setTitle(subItemDelay.getTitle());
                        mPlaybackService.getItemEntity().setClip(clip);
                        player_logo_image.setVisibility(View.GONE);

                        mPlaybackService.stopPlayer(true);
                        player_surface.setVisibility(View.GONE);
                        player_container.setVisibility(View.GONE);
                        player_container.removeAllViews();
                        mPlaybackService.setSurfaceView(player_surface);
                        mPlaybackService.setQiyiContainer(player_container);
                        player_shadow.setVisibility(View.VISIBLE);
                        showBuffer(PlAYSTART + mPlaybackService.getItemEntity().getTitle());
                        updateTitle(subItemDelay.getTitle());
                        mPlaybackService.switchTelevision(mCurrentPosition, subItemDelay.getPk(), clip.getUrl());
                        mCurrentPosition = 0;
                    }
                }, 400);
            }
        }
        settingMenu=null;
        AdIndex=0;
    }

    @Override
    public void onMenuItemClick(int value, String name) {
        if (value > MENU_QUALITY_ID_START && value <= MENU_QUALITY_ID_END) {
            if (!NetworkUtils.isConnected(getActivity())) {
                ((BaseActivity) getActivity()).showNoNetConnectDialog(null);
                mPlaybackService.pausePlayer();
                LogUtils.e(TAG, "Network error switch quality.");
                return;
            }
            // id值为quality值+1
            int qualityValue = value - 1;
            final ClipEntity.Quality clickQuality = ClipEntity.Quality.getQuality(qualityValue);
            if (clickQuality == null || clickQuality == mPlaybackService.getCurrentQuality()) {
                // 为空或者点击的码率和当前设置码率相同
                if(settingMenu!=null){
                    settingMenu.dismiss();
                }
                return;
            }
            if(settingMenu!=null){
                settingMenu.dismiss();
                settingMenu=null;
            }
            mIsOnPaused = false;// 暂停以后切换画质
            if (mPlaybackService.getMediaPlayer().getPlayerMode() == IsmartvPlayer.MODE_SMART_PLAYER) {
                timerStop();
                showBuffer(null);
            }
            if (!mPlaybackService.getItemEntity().getLiveVideo()) {
                mCurrentPosition = mPlaybackService.getMediaPlayer().getCurrentPosition();
            }
            // 点击菜单后延迟400ms处理，菜单不会有卡顿现象
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPlaybackService.switchQuality(mCurrentPosition, clickQuality);
                    updateQuality(clickQuality);
                }
            }, 400);
        }
    }
    private void playPauseVideo() {
        if (mIsExiting || mPlaybackService.isPlayingAd() || mPlaybackService == null || mPlaybackService.getMediaPlayer() == null
                || mPlaybackService.getItemEntity().getLiveVideo() || !mPlaybackService.isPlayerPrepared()) {
            return;
        }
        if (mPlaybackService.getMediaPlayer().isPlaying()) {
            mIsOnPaused = true;
            mPlaybackService.pausePlayer();
            if (mPlaybackService.getMediaPlayer().getPlayerMode() == IsmartvPlayer.MODE_SMART_PLAYER) {
                mAdvertisement.fetchVideoStartAd(mPlaybackService.getItemEntity(), Advertisement.AD_MODE_ONPAUSE, extraSource);
            }
        } else {
            mIsOnPaused = false;
            if(!isPlayExitLayerShow)
            mPlaybackService.startPlayer();
        }
    }

    /**
     * 1.点击会员去广告进入购买页面
     * 2.点击菜单'客服中心'
     * 3.试看完成,自动进入购买页面.需要释放播放器,重新获取Clip接口数据
     */
    private void goOtherPage(int type) {
        if (mPlaybackService == null || mPlaybackService.getItemEntity() == null || mPlaybackService.getMediaPlayer() == null) {
            LogUtils.e(TAG, "service is null when go other page");
            return;
        }
        hideMenu();
        hidePanel();
        switch (type) {
            case EVENT_CLICK_VIP_BUY:
                if (mPlaybackService.getMediaPlayer().getPlayerMode() == IsmartvPlayer.MODE_SMART_PLAYER) {
                    toPayPage(mPlaybackService.getItemEntity().getPk(), 2, 3, null);
                } else {
                    toPayPage(mPlaybackService.getItemEntity().getPk(), 2, 2, null);
                }
                mPlaybackService.getMediaPlayer().logExpenseAdClick();
                break;
            case EVENT_CLICK_KEFU:
                mIsClickKefu = true;
                // 点击客服，添加历史记录
                mPlaybackService.addHistory(mCurrentPosition, true);
                PageIntent page = new PageIntent();
                page.toHelpPage(getActivity());
                break;
            case EVENT_COMPLETE_BUY:
                mPlaybackService.addHistory(mCurrentPosition, true);
                mPlaybackService.logExpenseVideoPreview(mCurrentPosition, "purchase");
                mPlaybackService.logVideoExit(mCurrentPosition, "finish");
                ItemEntity.Expense expense = mPlaybackService.getItemEntity().getExpense();
                PageIntentInterface.ProductCategory mode = null;
                if (1 == expense.getJump_to()) {
                    mode = PageIntentInterface.ProductCategory.item;
                }
                toPayPage(mPlaybackService.getItemEntity().getPk(), expense.getJump_to(), expense.getCpid(), mode);
                break;
            case EVENT_PLAY_EXIT:
                mIsClickKefu = true;
                isPlayExitLayerShow = true;
                PageIntent pageIntent=new PageIntent();
                pageIntent.toPlayFinish(this,mPlaybackService.getItemEntity().getContentModel(),extraItemPk, (int) ((((double)mCurrentPosition)/((double)mPlaybackService.getMediaPlayer().getDuration()))*100),mPlaybackService.hasHistory,"player");
                break;
        }
    }

    private int offsets = 0; // 进度条变化
    private int offn = 1;
    private static final int SHORT_STEP = 1000;

    private void previousClick(View view) {
        if (mPlaybackService != null && !mPlaybackService.getItemEntity().getLiveVideo()) {
            if (!isSeeking) {
                // 拖动进度条是需要一直显示Panel
                mHandler.removeMessages(MSG_HIDE_PANEL);
                if (panel_layout.getVisibility() != View.VISIBLE) {
                    panel_layout.startAnimation(panelShowAnimation);
                    panel_layout.setVisibility(View.VISIBLE);

                    player_top_panel.startAnimation(top_fly_down);
                    player_top_panel.setVisibility(View.VISIBLE);
                }
                timerStop();
                isSeeking = true;
            }
            if (mHandler.hasMessages(MSG_SEK_ACTION))
                mHandler.removeMessages(MSG_SEK_ACTION);
            fastBackward(SHORT_STEP);
            if (view != null) {
                mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
            }
        }
    }

    private void forwardClick(View view) {
        if (mPlaybackService != null && !mPlaybackService.getItemEntity().getLiveVideo()) {
            if (!isSeeking) {
                // 拖动进度条是需要一直显示Panel
                mHandler.removeMessages(MSG_HIDE_PANEL);
                if (panel_layout.getVisibility() != View.VISIBLE) {
                    panel_layout.startAnimation(panelShowAnimation);
                    panel_layout.setVisibility(View.VISIBLE);

                    player_top_panel.startAnimation(top_fly_down);
                    player_top_panel.setVisibility(View.VISIBLE);
                }
                timerStop();
                isSeeking = true;
            }
            if (mHandler.hasMessages(MSG_SEK_ACTION))
                mHandler.removeMessages(MSG_SEK_ACTION);
            fastForward(SHORT_STEP);
            if (view != null) {
                mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
            }
        }
    }

    private void fastForward(int step) {
        int clipLength = mPlaybackService.getMediaPlayer().getDuration();
        if (mCurrentPosition >= clipLength) {
            player_seekBar.setProgress(clipLength - 3000);
            return;
        }
        if (clipLength > 1000000) {
            if (offsets != 1 && offsets % 5 != 0) {
                offsets += step;
            } else {
                if (offsets > 0) {
                    offn = offsets / 5;
                }
            }
            if (offn < 11) {
                mCurrentPosition += clipLength * offn * 0.01;
            } else {
                mCurrentPosition += clipLength * 0.1;
            }
        } else {
            mCurrentPosition += 10000;
        }

        if (mCurrentPosition > clipLength) {
            mCurrentPosition = clipLength - 3000;
        }
        player_seekBar.setProgress(mCurrentPosition);
    }

    private void fastBackward(int step) {
        int clipLength = mPlaybackService.getMediaPlayer().getDuration();
        if (mCurrentPosition <= 0) {
            player_seekBar.setProgress(0);
            return;
        }
        if (clipLength > 1000000) {
            if (offsets != 1 && offsets % 5 != 0) {
                offsets += step;
            } else {
                if (offsets > 0) {
                    offn = offsets / 5;
                }
            }
            if (offn < 11) {
                mCurrentPosition -= clipLength * offn * 0.01;
            } else {
                mCurrentPosition -= clipLength * 0.1;
            }
        } else {
            mCurrentPosition -= 10000;
        }
        if (mCurrentPosition <= 0)
            mCurrentPosition = 0;
        player_seekBar.setProgress(mCurrentPosition);
    }
    private void createMenu(){
        List<ItemEntity> list = new ArrayList<>();
        if(!mPlaybackService.isPreview()) {
            ItemEntity[] subItems = mPlaybackService.getItemEntity().getSubitems();
            if (subItems != null) {
                for (int i = 0; i < subItems.length; i++) {
                    list.add(subItems[i]);
                }
            }
        }
            ArrayList<QuailtyEntity> quailtyEntities = new ArrayList<>();
            int currentQuality = 0;
            List<ClipEntity.Quality> qualities = mPlaybackService.getMediaPlayer().getQualities();
            if (qualities != null && !qualities.isEmpty()) {
                for (int i = 0; i < qualities.size(); i++) {
                    ClipEntity.Quality quality = qualities.get(i);
                    QuailtyEntity quailtyEntity = new QuailtyEntity();
                    quailtyEntity.setName(ClipEntity.Quality.getString(quality));
                    quailtyEntity.setValue(quality.getValue() + 1);
                    if (mPlaybackService.getMediaPlayer().getCurrentQuality() == quality) {
                        currentQuality = i;
                    }
//                    QuailtyEntity quailtyEntity=new QuailtyEntity();
//                    quailtyEntity.setName("高清"+i);
//                    quailtyEntity.setValue(i);
                    quailtyEntities.add(quailtyEntity);
                }
            }
            settingMenu = new PlayerSettingMenu(getActivity().getApplicationContext(), list, mPlaybackService.getSubItemPk(), this, quailtyEntities, currentQuality, this,contentMode);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    settingMenu.sendMsg();
                }
            }, 1000);
    }

    private void showMenu(int type) {
        if (mIsExiting || !canShowMenuOrPannel || mPlaybackService == null || mPlaybackService.getMediaPlayer() == null) {
            return;
        }
        settingMenu=null;
        createMenu();
        if (isPanelShow()) {
            hidePanel();
        }
        if(type==1){
            settingMenu.showQuality();
        }
        settingMenu.setAnimationStyle(R.style.PopupAnimation);
        settingMenu.showAtLocation(parentView,Gravity.BOTTOM,0,0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                settingMenu.sendMsg();
            }
        },1000);
    }

    private void hideMenu() {
        if (isMenuShow()) {
            settingMenu.dismiss();
        }
    }

    public boolean isMenuShow() {
        if (settingMenu == null) {
            return false;
        }
        return settingMenu.isShowing();
    }

    public void showPannelDelayOut() {
        if (mIsExiting || !canShowMenuOrPannel || mPlaybackService == null || mPlaybackService.getMediaPlayer() == null
                || isPopWindowShow() || isMenuShow()
                || !mPlaybackService.isPlayerPrepared()) {
            return;
        }
        if (panel_layout.getVisibility() != View.VISIBLE) {
            panel_layout.startAnimation(panelShowAnimation);
            panel_layout.setVisibility(View.VISIBLE);

            player_top_panel.startAnimation(top_fly_down);
            player_top_panel.setVisibility(View.VISIBLE);

            mHandler.sendEmptyMessageDelayed(MSG_HIDE_PANEL, 5000);
        } else {
            mHandler.removeMessages(MSG_HIDE_PANEL);
            mHandler.sendEmptyMessageDelayed(MSG_HIDE_PANEL, 5000);
        }
    }

    private void hidePanel() {
        if (panel_layout != null && panel_layout.getVisibility() == View.VISIBLE) {
            panel_layout.startAnimation(panelHideAnimation);
            panel_layout.setVisibility(View.GONE);

            player_top_panel.startAnimation(top_fly_up);
            player_top_panel.setVisibility(View.GONE);

            mHandler.removeMessages(MSG_HIDE_PANEL);
        }
    }

    private boolean isPanelShow() {
        return panel_layout != null && panel_layout.getVisibility() == View.VISIBLE;
    }

    private void timerStart(int delay) {
        if (mPlaybackService == null || !mPlaybackService.isPlayerPrepared() || mPlaybackService.isPlayingAd()) {
            return;
        }
        LogUtils.d(TAG, "progressTimerStart: " + delay);
        if (delay > 0) {
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, delay);
        } else {
            mHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
        }

    }

    private void timerStop() {
        if (mHandler.hasMessages(MSG_UPDATE_PROGRESS)) {
            mHandler.removeMessages(MSG_UPDATE_PROGRESS);
        }
    }

    private ModuleMessagePopWindow popDialog;
    private int popShowType;

    private boolean isPopWindowShow() {
        return popDialog != null && popDialog.isShowing();
    }

    private void showPopup(final byte popType) {
        if (mIsExiting || isPopWindowShow() || isDetached()||isPlayExitLayerShow) {
            return;
        }
        popShowType = popType;
        String message = getString(R.string.player_error);
        String cancelText = getString(R.string.player_pop_cancel);
        String confirmText = getString(R.string.player_pop_ok);
        boolean hideCancel = false;
        switch (popType) {
            case POP_TYPE_BUFFERING_LONG:
                if (mHandler.hasMessages(MSG_SHOW_BUFFERING_LONG)) {
                    mHandler.removeMessages(MSG_SHOW_BUFFERING_LONG);
                }
                message = getString(R.string.player_buffering_long);
                confirmText = getString(R.string.player_pop_cancel);
                cancelText = getString(R.string.player_pop_switch_quality);
                break;
            case POP_TYPE_PLAYER_ERROR:
                timerStop();
                hidePanel();
                hideCancel = true;
                break;
            case POP_TYPE_PLAYER_NET_ERROR:
                message = getString(R.string.player_net_data_error);
                confirmText = getString(R.string.player_pop_set_net);
                cancelText = getString(R.string.player_pop_back);
                break;
        }
        popDialog = new ModuleMessagePopWindow(getActivity());
        popDialog.setConfirmBtn(confirmText);
        popDialog.setCancelBtn(cancelText);
        popDialog.setFirstMessage(message);
        if (hideCancel) {
            popDialog.hideCancelBtn();
        }
        popDialog.showAtLocation(((BaseActivity) getActivity()).getRootView(), Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        popDialog.dismiss();
                    }
                },
                new ModuleMessagePopWindow.CancelListener() {
                    @Override
                    public void cancelClick(View view) {
                        popDialog.dismiss();
                        if (popType == POP_TYPE_PLAYER_NET_ERROR) {
                            closeActivity("source");
                        }
                    }
                });
        popDialog.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                switch (popType) {
                    case POP_TYPE_PLAYER_ERROR:
                        // 播放器异常情况,判断播放进度临界值,剩余时长8分钟为界,小于8分钟下次从头播放
                        int value = 8 * 1000 * 60;
                        if (mPlaybackService != null && mPlaybackService.getMediaPlayer() != null
                                && (mPlaybackService.getMediaPlayer().getDuration() - mCurrentPosition <= value)) {
                            mCurrentPosition = 0;
                        }
                        isErrorPopUp = false;
                        closeActivity("source");
                        break;
                    case POP_TYPE_BUFFERING_LONG:
                        if (mPlaybackService == null || mIsExiting || isErrorPopUp) {
                            return;
                        }
                        if (closePopup) {
                            closePopup = false;
                            return;
                        }
                        isClickBufferLong = true;
                        if (!mPlaybackService.isPlayerPrepared()) {
                            LogUtils.e(TAG, "Player has not prepared");
                            return;
                        }
                        if (!popDialog.isConfirmClick) {
                            showBuffer(null);
                            if (!isMenuShow()) {
                                if (isPanelShow()) {
                                    hidePanel();
                                }
                              //  createMenu();
//                                ItemEntity[] subItems = mPlaybackService.getItemEntity().getSubitems();
//                                if (subItems != null && subItems.length > 0 && !mPlaybackService.isPreview()) {
//                                    // 电视剧
//                                //    playerMenu.showQuality(1);
//
//                                } else {
//                                    // 电影
//                                //    playerMenu.showQuality(0);
//                                    showMenu(1);
//                                }
                                showMenu(1);
                            }
                        } else {
                            // 重新加载
                            timerStop();
                            showBuffer(null);
                            mPlaybackService.switchQuality(mCurrentPosition, mPlaybackService.getCurrentQuality());
                            updateQuality(mPlaybackService.getCurrentQuality());
                        }
                        break;
                    case POP_TYPE_PLAYER_NET_ERROR:
                        if (popDialog.isConfirmClick && !isErrorPopUp) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(intent);
                        }
                        break;
                }
            }
        });
    }

    private void showBuffer(String msg) {
        LogUtils.d(TAG, "showBuffer:" + msg + " " + mPlaybackService);
        if (mIsExiting) {
            return;
        }
        // 播放过程中，断开网络，操作后显示buffer前先判断网络是否连接
        if (!NetworkUtils.isConnected(getActivity()) && mPlaybackService != null) {
            // 断开网络，连接网络后会在广播接收中恢复
            mPlaybackService.addHistory(mCurrentPosition, false);
            hidePanel();
            timerStop();
            ((BaseActivity) getActivity()).showNoNetConnectDialog(null);
            return;
        }
        if ((mIsOnPaused && !isSeeking) || isPopWindowShow()) {
            return;
        }
        if (msg != null) {
            player_buffer_text.setText(msg);
        }
        if (player_buffer_layout.getVisibility() != View.VISIBLE) {
            player_buffer_layout.setVisibility(View.VISIBLE);
            if (animationDrawable != null && !animationDrawable.isRunning()) {
                animationDrawable.start();
            }
        }
        if (mPlaybackService != null && mPlaybackService.getMediaPlayer() != null) {
            mPlaybackService.getMediaPlayer().setLogBufferStartTime();
        }
        // 显示buffer,就需要发送50延时消息，显示加载时间过长
        if (mHandler.hasMessages(MSG_SHOW_BUFFERING_LONG)) {
            mHandler.removeMessages(MSG_SHOW_BUFFERING_LONG);
        }
        mHandler.sendEmptyMessageDelayed(MSG_SHOW_BUFFERING_LONG, 50 * 1000);

    }

    private void hideBuffer() {
        // buffer消失，就需要remove50秒延时消息
        removeBufferingLongTime();

        if (player_buffer_layout != null && player_buffer_layout.getVisibility() == View.VISIBLE) {
            player_buffer_layout.setVisibility(View.GONE);
            player_buffer_text.setText(getString(R.string.loading_text));
            if (animationDrawable != null && animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
        }

    }

    private boolean isBufferShow() {
        if (player_buffer_layout != null && player_buffer_layout.getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }

    private void removeBufferingLongTime() {
        if (mHandler.hasMessages(MSG_SHOW_BUFFERING_LONG)) {
            mHandler.removeMessages(MSG_SHOW_BUFFERING_LONG);
        }
        LogUtils.i(TAG, "removeBufferingLongTime : " + popShowType);
        if (popDialog != null && popDialog.isShowing() && popShowType == POP_TYPE_BUFFERING_LONG) {
            closePopup = true;
            popDialog.dismiss();
            popDialog = null;
        }

    }

    private void updateTitle(String title) {
        player_title.setText(title);
    }

    private void updateQuality(ClipEntity.Quality quality) {
        player_quality.setBackground(getQualityResource(quality));
    }

    private void updateTimer(int position) {
        if (mPlaybackService == null) {
            return;
        }
        String text = getTimeString(position) + "/"
                + getTimeString(mPlaybackService.getMediaPlayer().getDuration());
        player_timer.setText(text);
    }

    private void updatePlayerPause() {
        if (player_start != null && mPlaybackService != null && mPlaybackService.getMediaPlayer() != null
                && mPlaybackService.isPlayerPrepared()) {
            LogUtils.d("LH/", "updatePlayPause:" + mPlaybackService.getMediaPlayer().isPlaying());
            if (mPlaybackService.getMediaPlayer().isPlaying()) {
                player_start.setImageResource(R.drawable.selector_player_pause);
            } else {
                player_start.setImageResource(R.drawable.selector_player_play);
            }
        }
    }



    private static class PlaybackHandler extends Handler {

        private WeakReference<PlaybackFragment> weakReference;

        public PlaybackHandler(PlaybackFragment playbackFragment) {
            weakReference = new WeakReference<>(playbackFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            PlaybackFragment fragment = weakReference.get();
            if (fragment == null || fragment.mPlaybackService == null) {
                return;
            }
            PlaybackService service = fragment.mPlaybackService;
            switch (msg.what) {
                case MSG_SEK_ACTION:
                    if (fragment.mIsExiting || service.getMediaPlayer() == null || !service.isPlayerPrepared()) {
                        return;
                    }
                    LogUtils.d("LH/PlaybackHandler", "MSG_SEK_ACTION seek to " + fragment.mCurrentPosition);
                    fragment.player_seekBar.setProgress(fragment.mCurrentPosition);
                    service.getMediaPlayer().seekTo(fragment.mCurrentPosition);
                    fragment.offsets = 0;
                    fragment.offn = 1;
                    fragment.mIsOnPaused = false;
                    fragment.showBuffer(null);
                    break;
                case MSG_AD_COUNTDOWN:
                    if (fragment.mIsExiting || service.getMediaPlayer() == null) {
                        return;
                    }
                    int countDownTime = service.getMediaPlayer().getAdCountDownTime() / 1000;
                    if (countDownTime < 0) {
                        countDownTime = 0;
                    }
                    String time = String.valueOf(countDownTime);
                    if (countDownTime < 10 || (countDownTime >= 10 && countDownTime <= 99 && fragment.mAdCount > 99)) {
                        time = "0" + time;
                    }
                    fragment.ad_count_text.setText("" + time);
                    sendEmptyMessageDelayed(MSG_AD_COUNTDOWN, 1000);
                    break;
                case MSG_SHOW_BUFFERING_LONG:
                    if (fragment.getActivity() != null && !fragment.mIsExiting) {
                        if (!NetworkUtils.isConnected(fragment.getActivity())) {// 网络断开情况下无需显示切换分辨率
                            service.addHistory(fragment.mCurrentPosition, true);
                            ((BaseActivity) fragment.getActivity()).showNoNetConnectDialog(null);
                            LogUtils.d("LH/PlaybackHandler", "Network error on MSG_SHOW_BUFFERING_LONG.");
                            return;
                        }
                        LogUtils.d("LH/PlaybackHandler", "Show buffering long time:" + service.getMediaPlayer());
                        if (service.getMediaPlayer() != null && service.isPlayerPrepared()) {
                            fragment.showPopup(POP_TYPE_BUFFERING_LONG);
                        } else {
                            fragment.showPopup(POP_TYPE_PLAYER_NET_ERROR);
                        }
                    }
                    break;
                case MSG_UPDATE_PROGRESS:
                    if (service.getMediaPlayer() == null || service.getItemEntity() == null || service.getItemEntity().getLiveVideo() || fragment.mIsExiting) {
                        removeMessages(MSG_UPDATE_PROGRESS);
                        return;
                    }
                    LogUtils.i("LH/PlaybackHandler", "isPlaying : " + service.getMediaPlayer().isPlaying());
                    if (service.getMediaPlayer().isPlaying()) {
                        int mediaPosition = service.getMediaPlayer().getCurrentPosition();
                        // 播放过程中网络相关
                        if (!fragment.isSeeking && fragment.mCurrentPosition == mediaPosition && mediaPosition != fragment.historyPosition) {
                            LogUtils.d("LH/PlaybackHandler", "Network videoBufferingShow：" + fragment.isBufferShow() + " " + mediaPosition + " " + fragment.mCurrentPosition);
                            if (!NetworkUtils.isConnected(fragment.getActivity())) {
                                // 断开网络，连接网络后会在广播接收中恢复
                                service.addHistory(fragment.mCurrentPosition, true);
                                fragment.hidePanel();
                                ((BaseActivity) fragment.getActivity()).showNoNetConnectDialog(null);
                                LogUtils.d("LH/PlaybackHandler", "Network error on timer runnable.");
                                return;
                            } else {
                                // 画面卡住不动，显示loading,由于网速恢复后timerRunnable需要继续显示,故此处需要不断postDelayed
                                // 由于部分机型，画面停止后，多次调用getCurrentPosition会导致onError回调，故时间间隔尽可能长
                                // 还应注意不能一直显示，让buffering的handler清除计时消息
                                if (!fragment.isBufferShow()) {
                                    fragment.showBuffer(null);
                                }
                                sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 2000);
                                return;
                            }
                        }
                        // 播放过程中网络相关End

                        if (fragment.isBufferShow()) {
                            // 画面开始播放，buffer就需要消失
                            fragment.hideBuffer();
                        }
                        // 显示切换画质提示框后，恢复网络，弹窗需要消失
                        if (fragment.isPopWindowShow()) {
                            fragment.removeBufferingLongTime();
                        }

                        if (service.getMediaPlayer().getPlayerMode() == IsmartvPlayer.MODE_SMART_PLAYER) {
                            // 视云播放器，onSeekComplete回调完成后，getCurrentPosition获取位置不是最新seekTo的位置,2s以后再更新进度条
                            if (fragment.isSeeking) {
                                fragment.isSeeking = false;
                                fragment.showPannelDelayOut();
                                sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 2000);
                                return;
                            }
                        } else {
                            if (fragment.isSeeking) {// 奇艺视频seek结束后需要置为false
                                fragment.isSeeking = false;
                                fragment.showPannelDelayOut();
                            }
                        }

                        // 更新进度条
                        fragment.mCurrentPosition = mediaPosition;
                        fragment.player_seekBar.setProgress(fragment.mCurrentPosition);
                    }
                    sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 1000);// 部分机型在500ms内获取当前播放进度，与上一次相同，此处每1s更新一次进度条
                    break;
                case MSG_HIDE_PANEL:
                    fragment.hidePanel();
                    break;
            }
        }
    }

    private void toPayPage(int pk, int jumpTo, int cpid, PageIntentInterface.ProductCategory model) {
        LogUtils.d(TAG, "toPayPage:" + pk + " to:" + jumpTo + " cpid:" + cpid);
        PageIntentInterface.PaymentInfo paymentInfo = new PageIntentInterface.PaymentInfo(model, pk, jumpTo, cpid);
        Intent intent = new Intent();
        switch (paymentInfo.getJumpTo()) {
            case PageIntentInterface.PAYMENT:
                intent.setAction("tv.ismar.pay.payment");
                intent.putExtra(PageIntentInterface.EXTRA_PK, paymentInfo.getPk());
                intent.putExtra(PageIntentInterface.EXTRA_PRODUCT_CATEGORY, paymentInfo.getCategory().toString());
                break;
            case PageIntentInterface.PAY:
                intent.setAction("tv.ismar.pay.pay");
                intent.putExtra("item_id", paymentInfo.getPk());
                break;
            case PageIntentInterface.PAYVIP:
                intent.setAction("tv.ismar.pay.payvip");
                intent.putExtra("cpid", paymentInfo.getCpid());
                intent.putExtra("item_id", paymentInfo.getPk());
                break;
            default:
                throw new IllegalArgumentException();
        }
        startActivityForResult(intent, PAYMENT_REQUEST_CODE);

    }

    private String getModelName() {
        if (Build.PRODUCT.length() > 20) {
            return Build.PRODUCT.replaceAll(" ", "_").toLowerCase().substring(0, 19);
        } else {
            return Build.PRODUCT.replaceAll(" ", "_").toLowerCase();
        }
    }

    private String getTimeString(int ms) {
        int left = ms;
        int hour = left / 3600000;
        left %= 3600000;
        int min = left / 60000;
        left %= 60000;
        int sec = left / 1000;
        return String.format("%1$02d:%2$02d:%3$02d", hour, min, sec);
    }

    public Drawable getQualityResource(ClipEntity.Quality quality) {
        if (quality == null) {
            return new ColorDrawable(0);
        }
        LogUtils.i("LH/", "quality:" + quality);
        switch (quality) {
            case QUALITY_NORMAL:// 流畅
                return getResources().getDrawable(R.drawable.player_stream_normal);
            case QUALITY_MEDIUM:// 高清
                return getResources().getDrawable(R.drawable.player_stream_high);
            case QUALITY_HIGH:// 超清
                return getResources().getDrawable(R.drawable.player_stream_super);
            case QUALITY_ULTRA:// 1080P
                return getResources().getDrawable(R.drawable.player_stream_1080p);
            case QUALITY_BLUERAY:// 蓝光
                return getResources().getDrawable(R.drawable.player_stream_blueray);
            case QUALITY_4K:// 4K
                return getResources().getDrawable(R.drawable.player_stream_4k);
            default:
                return getResources().getDrawable(R.drawable.player_quality_back);
        }
    }

    private ConnectionChangeReceiver connectionChangeReceiver;

    private void registerConnectionReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        connectionChangeReceiver = new ConnectionChangeReceiver();
        getActivity().registerReceiver(connectionChangeReceiver, filter);
    }

    private void unregisterConnectionReceiver() {
        if (connectionChangeReceiver != null) {
            getActivity().unregisterReceiver(connectionChangeReceiver);
        }
    }

    private class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            BaseActivity baseActivity = ((BaseActivity) getActivity());
            if (baseActivity == null || mPlaybackService == null || mIsExiting||isPlayExitLayerShow) {
                return;
            }
            if (mPlaybackService.getMediaPlayer() != null) {
                if (NetworkUtils.isConnected(context)) {
                    baseActivity.dismissNoNetConnectDialog();
                    if (!mPlaybackService.isPlayerPrepared()) {
                        mPlaybackService.preparePlayer(mPlaybackService.getItemPk(), mPlaybackService.getSubItemPk(), extraSource);
                    } else {
                        timerStart(0);
                        if (mPlaybackService.isPlayerPrepared() && !mPlaybackService.getMediaPlayer().isPlaying() && !isSeeking) {
                            mPlaybackService.startPlayer();
                        }
                    }
                } else if (isBufferShow() && !isPopWindowShow()) {
                    hideBuffer();
                    hidePanel();
                    timerStop();
                    mPlaybackService.addHistory(mCurrentPosition, true);
                    baseActivity.showNoNetConnectDialog(null);
                }
            } else if (baseActivity.isNoNetDialogShowing() && NetworkUtils.isConnected(context)) {
                baseActivity.dismissNoNetConnectDialog();
                mPlaybackService.resetPreload();
                mPlaybackService.preparePlayer(extraItemPk, extraSubItemPk, extraSource);
                showBuffer(null);
            }
        }
    }

    private boolean isSetupNetClick;// 断网起播

    private OnNoNetConfirmListener onNoNetConfirmListener = new OnNoNetConfirmListener() {
        @Override
        public void onNoNetConfirm() {
            isSetupNetClick = true;
        }
    };

    private ClosePlayerReceiver closePlayerReceiver;

    private void registerClosePlayerReceiver() {
        IntentFilter filter = new IntentFilter("tv.ismar.daisy.closeplayer");
        closePlayerReceiver = new ClosePlayerReceiver();
        getActivity().registerReceiver(closePlayerReceiver, filter);
    }

    private void unRegisterClosePlayerReceiver() {
        if (closePlayerReceiver != null) {
            getActivity().unregisterReceiver(closePlayerReceiver);
        }
    }

    private class ClosePlayerReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPlaybackService.isPreview()) {
                mPlaybackService.logExpenseVideoPreview(mCurrentPosition, "cancel");
            }
            ExitToast.createToastConfig().dismiss();
            mHandler.removeCallbacksAndMessages(null);
            timerStop();
            removeBufferingLongTime();
            hideBuffer();
            hidePanel();
            mPlaybackService.addHistory(mCurrentPosition,true);
            closeActivity("source");
        }
    }
}
