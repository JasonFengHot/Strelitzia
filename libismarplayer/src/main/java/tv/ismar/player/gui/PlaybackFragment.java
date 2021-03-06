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
import android.text.TextUtils;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.ad.Advertisement;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.core.Source;
import tv.ismar.app.entity.ClipEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.player.OnNoNetConfirmListener;
import tv.ismar.app.ui.ToastTip;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.library.exception.ExceptionUtils;
import tv.ismar.library.util.LogUtils;
import tv.ismar.library.util.NetworkUtils;
import tv.ismar.library.util.StringUtils;
import tv.ismar.player.IsmartvPlayer;
import tv.ismar.player.R;
import tv.ismar.player.listener.EpisodeOnclickListener;
import tv.ismar.player.listener.OnMenuListItmeClickListener;
import tv.ismar.player.model.QuailtyEntity;
import tv.ismar.player.widget.AdImageDialog;

//import android.widget.ImageView;

public class PlaybackFragment extends Fragment implements PlaybackService.Client.Callback,
        PlayerMenu.OnCreateMenuListener, Advertisement.OnPauseVideoAdListener, PlaybackService.ServiceCallback ,EpisodeOnclickListener,OnMenuListItmeClickListener{

    private final String TAG = "LH/PlaybackFragment";
    public static final int PAYMENT_REQUEST_CODE = 0xd6;
    public static final int PAYMENT_SUCCESS_CODE = 0x5c;

    private static final byte POP_TYPE_BUFFERING_LONG = 1;// ???????????????,??????????????????
    private static final byte POP_TYPE_PLAYER_ERROR = 3;// ??????onError??????
    private static final byte POP_TYPE_PLAYER_NET_ERROR = 4;// ?????????????????????????????????50S????????????onPrepared??????

    private static final String ARG_PK = "ARG_PK";
    private static final String ARG_SUB_PK = "ARG_SUB_PK";
    private static final String ARG_SOURCE = "ARG_SOURCE";
    private static final String HISTORYCONTINUE = "???????????????";
    private static final String PlAYSTART = "???????????????";
    private static final int MSG_SEK_ACTION = 103;
    private static final int MSG_AD_COUNTDOWN = 104;
    private static final int MSG_SHOW_BUFFERING_LONG = 105;
    private static final int MSG_UPDATE_PROGRESS = 106;
    private static final int MSG_HIDE_PANEL = 107;
    private static final int MSG_DELAY_PLAY = 108;
    private static final int MENU_HIDE=109;
    private static final int EVENT_CLICK_VIP_BUY = 0x10;
    private static final int EVENT_CLICK_KEFU = 0x11;
    private static final int EVENT_COMPLETE_BUY = 0x12;
    private static final int EVENT_PLAY_EXIT = 0x13;
    // ?????????????????????id
    private static final int MENU_QUALITY_ID_START = 0;// ????????????id
    private static final int MENU_QUALITY_ID_END = 8;// ????????????id
    private static final int MENU_TELEPLAY_ID_START = 100;// ??????????????????????????????id
    private static final int MENU_KEFU_ID = 20;// ????????????
    private static final int MENU_RESTART = 30;// ????????????

    private int extraItemPk = 0;// ????????????pk???,??????/api/item/{pk}?????????????????????
    private int extraSubItemPk = 0;// ???????????????pk???,??????/api/subitem/{pk}?????????????????????
    private String extraSource = "";
    // ?????????UI
    private FrameLayout player_container;
    private SurfaceView player_surface;
    private LinearLayout panel_layout;
    private TextView player_timer, player_quality, player_title;
    private PlayerSettingMenu settingMenu;
    private View parentView;
    private SeekBar player_seekBar;
    private RecyclerImageView player_logo_image;
    private TextView ad_count_text, ad_vip_text;
    private View ad_vip_layout;
    private ListView player_menu;
    private LinearLayout player_buffer_layout;
    private LinearLayout player_top_panel;
    private RecyclerImageView player_buffer_img;
    private TextView player_buffer_text;
    private RecyclerImageView player_previous, player_forward, player_start;
    private RecyclerImageView player_shadow;

    private AnimationDrawable animationDrawable;
    private Animation panelShowAnimation;
    private Animation panelHideAnimation;
    private Animation top_fly_up,top_fly_down;
    private AdImageDialog adImageDialog;
    private Advertisement mAdvertisement;

    private PlaybackService.Client mClient;
    public PlaybackService mPlaybackService;
    private int mCurrentPosition;
    private boolean sharpSetupKeyClick = false; // ????????????????????????Activity?????????Dialog??????
    public boolean mounted;// ?????????????????????SD????????????585???????????????Dialog?????????????????????onPause,????????????????????????
    private boolean mIsExiting;// ???????????????????????????
    private boolean isSeeking = false;// ?????????????????????,?????????????????????,????????????
    private boolean canShowMenuOrPannel = false;// ????????????????????????????????????
    private boolean closePopup = false;// ????????????????????????????????????????????????????????????????????????
    private boolean isClickBufferLong;// ??????s3????????????????????????????????????????????????????????????timerStart??????????????????
    private boolean mIsOnPaused = false;// ??????pause()???????????????????????????BufferStart(701)
    private int historyPosition;// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    private boolean mIsInAdDetail;// ????????????????????????
    private boolean mIsClickKefu;// ????????????????????????????????????????????????
    private int mAdCount;// ???????????????????????????????????????3????????????2????????????2???????????????0
    private boolean isErrorPopUp;// ???????????????????????????onError??????
    private boolean isSeekingExit;
    private int mSeekToPosition;
    private boolean isPreloadIn;
    private int touchPosition;

    private PlaybackHandler mHandler;
    private boolean backpress=false;
    private boolean isqiyi;
    private String contentMode="";
    private boolean isPlayExitLayerShow;
    private String to="";

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
        to = getActivity().getIntent().getStringExtra("to");
        String frompage = getActivity().getIntent().getStringExtra(PageIntentInterface.EXTRA_SOURCE);
        if(TextUtils.isEmpty(to)&&frompage!=null) {
            if (!(frompage.equals(Source.RELATED.getValue()) || frompage.equals(Source.FINISHED.getValue()) || frompage.equals(Source.EXIT_LIKE.getValue()) || frompage.equals(Source.EXIT_NOT_LIKE.getValue()))) {
                to = frompage;
            }
        }
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
            if (!mPlaybackService.getItemEntity().getLiveVideo() && !isSeekingExit) {
                showBuffer(null);
            }
            timerStart(0);
            if (isSeekingExit) {
                isSeekingExit = false;
//                mHandler.sendEmptyMessageDelayed(MSG_DELAY_PLAY, 1000);
            }
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
        if (mPlaybackService != null) {
            if (!mPlaybackService.isPlayingAd() && !isPlayExitLayerShow && mCurrentPosition > 0) {
                mPlaybackService.addHistory(mCurrentPosition, false);// ??????????????????????????????????????????????????????????????????????????????????????????addHistory????????????????????????
            }
        }
//        if (sharpSetupKeyClick || mounted) {
//            sharpSetupKeyClick = false;
//            mounted = false;
//            return;
//        }
//        if (mPlaybackService != null && !mPlaybackService.isPlayerStopping()&&!backpress) {
//            // ????????????onStop()??????SmartPlayer?????????????????????????????????onError????????????
//            mPlaybackService.stopPlayer(false);
//        }
        // handler?????????????????????????????????????????????
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
            if (!mPlaybackService.isPlayingAd() && !isPlayExitLayerShow && mCurrentPosition > 0) {
                mPlaybackService.addHistory(mCurrentPosition, false);// ??????????????????????????????????????????????????????????????????????????????????????????addHistory????????????????????????
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
            // ??????????????????Activity???????????????????????????????????????????????????
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
		/*add by dragontec for bug 4405 start*/
        this.releasePlayService(true);
		/*add by dragontec for bug 4405 end*/
        mPlaybackService.setCallback(this);
        mPlaybackService.setSurfaceView(player_surface);
        mPlaybackService.setQiyiContainer(player_container);
        player_shadow.setVisibility(View.VISIBLE);
        if (mPlaybackService.isPreload() && !isqiyi) {
            // ??????????????????????????????
            isPreloadIn = true;
            mPlaybackService.startPlayWhenPrepared();
        } else {
            if (mIsClickKefu) {
                // ???????????????????????????????????????
                mPlaybackService.onResumeFromKefu();
            } else {
                isPreloadIn = false;
                mPlaybackService.resetPreload();
                mPlaybackService.preparePlayer(extraItemPk, extraSubItemPk, extraSource);
            }
        }
    }

    @Override
    public void onDisconnected() {
/*add by dragontec for bug 4205 start*/
        if (mPlaybackService != null) {
            mPlaybackService.setCallback(null);
            mPlaybackService.setQiyiContainer(null);
            mPlaybackService.setSurfaceView(null);
        }
/*add by dragontec for bug 4205 end*/
        mPlaybackService = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.i(TAG, "resultCode:" + resultCode + " request:" + requestCode);
        // ????????????????????????????????????
        if (resultCode == 200) {
            if (mPlaybackService == null) {
                return;
            }
            if (mPlaybackService.getMediaPlayer() != null && !IsmartvPlayer.isPreloadCompleted && isPreloadIn) {
                mPlaybackService.getMediaPlayer().logPreloadEnd();
            }
            if (mPlaybackService.isPreview()) {
                mPlaybackService.logExpenseVideoPreview(mCurrentPosition, "cancel");
            }
            mHandler.removeCallbacksAndMessages(null);
            timerStop();
            removeBufferingLongTime();
            hideBuffer();
            hidePanel();
            mPlaybackService.addHistory(mCurrentPosition,true);
            closeActivity("source");
            return;
        }
        if(mPlaybackService!=null)
        mPlaybackService.initUserInfo();
        if (requestCode == PAYMENT_REQUEST_CODE) {
            if (resultCode == PAYMENT_SUCCESS_CODE) {
                // ???????????????????????????
                ad_vip_layout.setVisibility(View.GONE);
                player_shadow.setVisibility(View.VISIBLE);
                showBuffer(null);
                mPlaybackService.preparePlayer(extraItemPk, extraSubItemPk, extraSource);
                settingMenu=null;
            } else {
                // ??????????????????????????????
                closeActivity("finish");
            }
        }
    }

    private void closeActivity(String to) {
        mIsExiting = true;
        if (mPlaybackService != null) {
            mPlaybackService.logVideoExit(mCurrentPosition, to);
            mPlaybackService.stopPlayer(true);
            mPlaybackService.setCallback(null);
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
        player_logo_image = (RecyclerImageView) contentView.findViewById(R.id.player_logo_image);
        ad_vip_layout = contentView.findViewById(R.id.ad_vip_layout);
        ad_count_text = (TextView) contentView.findViewById(R.id.ad_count_text);
        ad_vip_text = (TextView) contentView.findViewById(R.id.ad_vip_text);
        player_menu = (ListView) contentView.findViewById(R.id.player_menu);
        player_buffer_layout = (LinearLayout) contentView.findViewById(R.id.player_buffer_layout);
        player_buffer_img = (RecyclerImageView) contentView.findViewById(R.id.player_buffer_img);
        player_buffer_text = (TextView) contentView.findViewById(R.id.player_buffer_text);
        player_previous = (RecyclerImageView) contentView.findViewById(R.id.player_previous);
        player_forward = (RecyclerImageView) contentView.findViewById(R.id.player_forward);
        player_start = (RecyclerImageView) contentView.findViewById(R.id.player_start);
        player_shadow = (RecyclerImageView) contentView.findViewById(R.id.player_shadow);

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
            // ????????????????????????????????????Panel
            mHandler.removeMessages(MSG_HIDE_PANEL);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mPlaybackService.getItemEntity() == null || mPlaybackService.getMediaPlayer() == null) {
                return;
            }
            touchPosition = mPlaybackService.getMediaPlayer().getCurrentPosition();
            isSeeking = true;
            mIsOnPaused = false;
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
        // ??????????????????
        adImageDialog = new AdImageDialog(getActivity(), R.style.PauseAdDialog, adList);
        try {
            adImageDialog.show();
        } catch (android.view.WindowManager.BadTokenException e) {
            ExceptionUtils.sendProgramError(e);
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
            // id??????quality???+1
            int qualityValue = id - 1;
            final ClipEntity.Quality clickQuality = ClipEntity.Quality.getQuality(qualityValue);
            if (clickQuality == null || clickQuality == mPlaybackService.getCurrentQuality()) {
                // ??????????????????????????????????????????????????????
                return false;
            }
            mIsOnPaused = false;// ????????????????????????
            if (mPlaybackService.getMediaPlayer().getPlayerMode() == IsmartvPlayer.MODE_SMART_PLAYER) {
                timerStop();
                showBuffer(null);
            }
            if (!mPlaybackService.getItemEntity().getLiveVideo()) {
                mCurrentPosition = mPlaybackService.getMediaPlayer().getCurrentPosition();
            }
            // ?????????????????????400ms????????????????????????????????????
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPlaybackService.switchQuality(mCurrentPosition, clickQuality);
                    updateQuality(clickQuality);
                }
            }, 400);
            ret = true;
        } else if (id > MENU_TELEPLAY_ID_START) {
            // id??????subItem pk???
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
                    // ?????????????????????400ms????????????????????????????????????
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ItemEntity.Clip clip = subItemDelay.getClip();
                            mPlaybackService.getItemEntity().setTitle(subItemDelay.getTitle());
                            mPlaybackService.getItemEntity().setClip(clip);
                            player_logo_image.setVisibility(View.GONE);

                            mPlaybackService.stopPlayer(false);// ?????????????????????true
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
            // ?????????????????????400ms????????????????????????????????????
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
            // ???????????????????????????,??????????????????
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
//            Advertisement advertisement=new Advertisement(getActivity());
//            advertisement.getQiantieAdUrl(AdIndex,"qiantieAd");
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
    public void sendAdlog(List<AdElementEntity> adlist) {
        if(AdIndex<=adlist.size()-1) {
            int length = adlist.get(AdIndex).getMonitor().size();
            for (int i = 0; i < length; i++) {
                Log.i("adverSendLog", "ADIndex: " + AdIndex);
                Log.i("adverSendLog", adlist.get(AdIndex).getMonitor().get(i).getMonitor_url());
                repostAdLog(adlist.get(AdIndex).getMonitor().get(i).getMonitor_url());
            }
            AdIndex++;
        }
    }

/*add by dragontec for bug 4322 start*/
    @Override
    public boolean isPrepared() {
        return getActivity() != null && !getActivity().isFinishing();
    }
/*add by dragontec for bug 4322 end*/

    private void repostAdLog(String url) {
        SkyService skyService = SkyService.ServiceManager.getAdService();
        skyService.repostAdLog(url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.i("ADSMon", throwable.toString() + "  onerror");
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }
                });

    }

    @Override
    public void updatePlayerStatus(PlaybackService.PlayerStatus status, Object args) {
		/*modify by dragontec for bug 4418 start*/
        if(getActivity() == null || !isAdded() || getActivity().isFinishing()){
            return;
        }
		/*modify by dragontec for bug 4418 end*/
        if (status == PlaybackService.PlayerStatus.RESPONSE_ERROR) {
            Throwable throwable = (Throwable) args;
            BaseActivity baseActivity = ((BaseActivity) getActivity());
            if (baseActivity == null) {
                return;
            }
            if (mPlaybackService != null) {
                mPlaybackService.addHistory(0, true);
            }
            if (NetworkUtils.isConnected(getActivity())) {
                ToastTip.showToast(getContext(),"??????????????????????????????");
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
                // ???????????????????????????????????????
                player_logo_image.setVisibility(View.GONE);
                ad_vip_layout.setVisibility(View.GONE);
                hideMenu();
                hidePanel();
                sharpSetupKeyClick = false; // ????????????????????????Activity?????????Dialog??????
                mounted = false;// ?????????????????????SD????????????585???????????????Dialog?????????????????????onPause,????????????????????????
                mIsExiting = false;// ???????????????????????????
                isSeeking = false;// ?????????????????????,?????????????????????,????????????
                canShowMenuOrPannel = false;// ????????????????????????????????????
                closePopup = false;// ????????????????????????????????????????????????????????????????????????
                isClickBufferLong = false;// ??????s3????????????????????????????????????????????????????????????timerStart??????????????????
                mIsOnPaused = false;// ??????pause()???????????????????????????BufferStart(701)
                historyPosition = 0;// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                mIsInAdDetail = false;// ????????????????????????
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
                timerStart(2000);
                break;
            case COMPLETED:
                hideMenu();
                hidePanel();
                timerStop();
                boolean isPreview = (boolean) args;
                if (isPreview) {
                    // ??????????????????????????????????????????????????????duration,mCurrentPosition???????????????
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
                    pageIntent.toPlayFinish(this,mPlaybackService.getItemEntity().getContentModel(),extraItemPk,100,mPlaybackService.hasHistory,"player",to);
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
                    // ?????????????????????????????????
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
        /*add by dragontec for bug 4138 start*/
        mCurrentPosition = mPlaybackService.getMediaPlayer().getCurrentPosition();
        /*add by dragontec for bug 4138 end*/
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
                    mPlaybackService.setCallback(null);
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
                mPlaybackService.getMediaPlayer().logAdExit();
                mPlaybackService.getMediaPlayer().logVideoExit(mPlaybackService.getStartPosition(), "source");
                mPlaybackService.setCallback(null);
                getActivity().finish();
                return true;
            }
        }
        if (isMenuShow()) {
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (mPlaybackService.isPlayingAd()) {
                    return true;
                }
                showPannelDelayOut();
                return true;
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                // TODO ????????????????????????
                // TODO ??????????????????????????????????????????
                //??????????????????
                //?????????????????????
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
                    isPlayExitLayerShow = true;
                    isSeekingExit = isSeeking;
                    mPlaybackService.pausePlayer();
                    goOtherPage(EVENT_PLAY_EXIT);
//                    return true;
//                }
//                if (isPopWindowShow()) {
//                    return true;
//                }
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
                        // TODO ?????????,???????????????????????????????????????H5,??????????????????
                        // ?????????/??????????????????????????????H5
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
        isPreloadIn = false;
        for (ItemEntity subItem : mPlaybackService.getItemEntity().getSubitems()) {
            if (subItem.getPk() == id) {
                mPlaybackService.logVideoExit(mCurrentPosition, "next");
                timerStop();
                final ItemEntity subItemDelay = subItem;
                // ?????????????????????400ms????????????????????????????????????
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
            // id??????quality???+1
            int qualityValue = value - 1;
            final ClipEntity.Quality clickQuality = ClipEntity.Quality.getQuality(qualityValue);
            if (clickQuality == null || clickQuality == mPlaybackService.getCurrentQuality()) {
                // ??????????????????????????????????????????????????????
                if(settingMenu!=null){
                    settingMenu.dismiss();
                }
                return;
            }
            if(settingMenu!=null){
                settingMenu.wheelIsShow=false;
                settingMenu.dismiss();
                settingMenu=null;
            }
            mIsOnPaused = false;// ????????????????????????
            if (mPlaybackService.getMediaPlayer().getPlayerMode() == IsmartvPlayer.MODE_SMART_PLAYER) {
                timerStop();
                showBuffer(null);
            }
            if (!mPlaybackService.getItemEntity().getLiveVideo()) {
                mCurrentPosition = mPlaybackService.getMediaPlayer().getCurrentPosition();
            }
            isPreloadIn = false;
            // ?????????????????????400ms????????????????????????????????????
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
		/*modify by dragontec for bug 4418 start*/
        if (mIsExiting || mPlaybackService.isPlayingAd() || mPlaybackService == null || mPlaybackService.getMediaPlayer() == null
                || mPlaybackService.getItemEntity().getLiveVideo() || !mPlaybackService.isPlayerPrepared()){
            return;
        }
		/*modify by dragontec for bug 4418 end*/
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
            timerStart(0);
        }
    }

    /**
     * 1.???????????????????????????????????????
     * 2.????????????'????????????'
     * 3.????????????,????????????????????????.?????????????????????,????????????Clip????????????
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
                // ?????????????????????????????????
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
				/*modify by dragontec for bug 4420 start*/
                if(expense != null){
                    if (1 == expense.getJump_to()) {
                        mode = PageIntentInterface.ProductCategory.item;
                    }
                    toPayPage(mPlaybackService.getItemEntity().getPk(), expense.getJump_to(), expense.getCpid(), mode);
                }
				/*modify by dragontec for bug 4420 end*/
                break;
            case EVENT_PLAY_EXIT:
                mIsClickKefu = true;
                PageIntent pageIntent=new PageIntent();
                pageIntent.toPlayFinish(this,mPlaybackService.getItemEntity().getContentModel(),extraItemPk, (int) ((((double)mCurrentPosition)/((double)mPlaybackService.getMediaPlayer().getDuration()))*100),mPlaybackService.hasHistory,"player",to);
                break;
        }
    }

    private int offsets = 0; // ???????????????
    private int offn = 1;
    private static final int SHORT_STEP = 1000;

    private void previousClick(View view) {
        if (mPlaybackService != null && !mPlaybackService.getItemEntity().getLiveVideo()) {
            if (!isSeeking) {
                // ????????????????????????????????????Panel
                mHandler.removeMessages(MSG_HIDE_PANEL);
                if (panel_layout.getVisibility() != View.VISIBLE) {
                    panel_layout.startAnimation(panelShowAnimation);
                    panel_layout.setVisibility(View.VISIBLE);

                    player_top_panel.startAnimation(top_fly_down);
                    player_top_panel.setVisibility(View.VISIBLE);
                }
            }
            timerStop();
            isSeeking = true;
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
                // ????????????????????????????????????Panel
                mHandler.removeMessages(MSG_HIDE_PANEL);
                if (panel_layout.getVisibility() != View.VISIBLE) {
                    panel_layout.startAnimation(panelShowAnimation);
                    panel_layout.setVisibility(View.VISIBLE);

                    player_top_panel.startAnimation(top_fly_down);
                    player_top_panel.setVisibility(View.VISIBLE);
                }
            }
            timerStop();
            isSeeking = true;
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
        mSeekToPosition = mCurrentPosition;
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
        mSeekToPosition = mCurrentPosition;
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
                    if(quality!=null) {
                        quailtyEntity.setName(ClipEntity.Quality.getString(quality));
                        quailtyEntity.setValue(quality.getValue() + 1);
                        quailtyEntities.add(quailtyEntity);
                    }
                    if (mPlaybackService.getMediaPlayer().getCurrentQuality() == quality) {
                        currentQuality = i;
                    }
                }
            }
            settingMenu = new PlayerSettingMenu(getActivity().getApplicationContext(), list, mPlaybackService.getSubItemPk(), this, quailtyEntities, currentQuality, this,contentMode);
            mHandler.sendEmptyMessageDelayed(MENU_HIDE,1000);
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
        mHandler.sendEmptyMessageDelayed(MENU_HIDE,1000);
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
        boolean hideCancel = true;
        switch (popType) {
            case POP_TYPE_BUFFERING_LONG:
                if (mHandler.hasMessages(MSG_SHOW_BUFFERING_LONG)) {
                    mHandler.removeMessages(MSG_SHOW_BUFFERING_LONG);
                }
                message = getString(R.string.player_buffering_long);
                confirmText = getString(R.string.player_pop_cancel);
                cancelText = getString(R.string.player_pop_switch_quality);
                hideCancel=false;
                break;
            case POP_TYPE_PLAYER_ERROR:
                timerStop();
                hidePanel();
                hideCancel = true;
                break;
            case POP_TYPE_PLAYER_NET_ERROR:
                ToastTip.showToast(getActivity(),"??????????????????????????????");
                return;
        }
        popDialog = new ModuleMessagePopWindow(getActivity());
        popDialog.setConfirmBtn(confirmText);
        popDialog.setCancelBtn(cancelText);
        popDialog.setMessage(message);
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
                        // ?????????????????????,???????????????????????????,????????????8????????????,??????8????????????????????????
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
//                                    // ?????????
//                                //    playerMenu.showQuality(1);
//
//                                } else {
//                                    // ??????
//                                //    playerMenu.showQuality(0);
//                                    showMenu(1);
//                                }
                                showMenu(1);
                            }
                        } else {
                            // ????????????
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
        // ????????????????????????????????????????????????buffer??????????????????????????????
        if (!NetworkUtils.isConnected(getActivity()) && mPlaybackService != null) {
            // ?????????????????????????????????????????????????????????
            mPlaybackService.addHistory(mCurrentPosition, false);
            hidePanel();
            timerStop();
            try {
                ((BaseActivity) getActivity()).showNoNetConnectDialog(null);
            }catch (Exception e){
                e.printStackTrace();
            }
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
        // ??????buffer,???????????????50???????????????????????????????????????
        if (mHandler.hasMessages(MSG_SHOW_BUFFERING_LONG)) {
            mHandler.removeMessages(MSG_SHOW_BUFFERING_LONG);
        }
        mHandler.sendEmptyMessageDelayed(MSG_SHOW_BUFFERING_LONG, 50 * 1000);

    }

    private void hideBuffer() {
        // buffer??????????????????remove50???????????????
        removeBufferingLongTime();
        try {
            if (player_buffer_layout != null && player_buffer_layout.getVisibility() == View.VISIBLE) {
                player_buffer_layout.setVisibility(View.GONE);
                player_buffer_text.setText(getString(R.string.loading_text));
                if (animationDrawable != null && animationDrawable.isRunning()) {
                    animationDrawable.stop();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
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
        private int bufferingCount;

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
                        if (!NetworkUtils.isConnected(fragment.getActivity())) {// ????????????????????????????????????????????????
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
                    if (fragment.isPlayExitLayerShow) {
//                        if (service.getMediaPlayer().isPlaying()) {
                            service.pausePlayer();
//                        }
                        fragment.timerStop();
                        return;
                    }
                    if (service.getMediaPlayer().isPlaying()) {
                        int mediaPosition = service.getMediaPlayer().getCurrentPosition();
                        // ???????????????????????????
                        if (!fragment.isSeeking && fragment.mSeekToPosition == 0 && fragment.mCurrentPosition == mediaPosition && mediaPosition != fragment.historyPosition) {
                            LogUtils.d("LH/PlaybackHandler", "Network videoBufferingShow???" + fragment.isBufferShow() + " " + mediaPosition + " " + fragment.mCurrentPosition);
                            if (!NetworkUtils.isConnected(fragment.getActivity())) {
                                // ?????????????????????????????????????????????????????????
                                service.addHistory(fragment.mCurrentPosition, true);
                                fragment.hidePanel();
                                ((BaseActivity) fragment.getActivity()).showNoNetConnectDialog(null);
                                LogUtils.d("LH/PlaybackHandler", "Network error on timer runnable.");
                                return;
                            }
                            // ???????????????????????????loading,?????????????????????timerRunnable??????????????????,?????????????????????postDelayed
                            // ???????????????????????????????????????????????????getCurrentPosition?????????onError????????????????????????????????????
                            // ????????????????????????????????????buffering???handler??????????????????
                            LogUtils.d("LH/PlaybackHandler", "bufferingCount???" + bufferingCount);
                            if (bufferingCount > 2 && !fragment.isBufferShow()) {
                                fragment.showBuffer(null);
                            }
                            sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 2000);
                            bufferingCount++;
                            return;
                        }
                        // ???????????????????????????End

                        if (service.getMediaPlayer().getPlayerMode() == IsmartvPlayer.MODE_SMART_PLAYER && fragment.isSeeking && fragment.mSeekToPosition > 0) {
                            boolean flag = (Math.abs(fragment.mSeekToPosition - mediaPosition) > 10000)
                                    || (fragment.mSeekToPosition == mediaPosition)
                                    || (fragment.touchPosition > 0 && Math.abs(fragment.touchPosition - mediaPosition) < 6000);
                            LogUtils.d("LH/PlaybackHandler", "seek : " + flag + " - " + mediaPosition + " - " + fragment.mSeekToPosition + " - " + fragment.touchPosition);
                            if (flag) {
                                if (!fragment.isBufferShow()) {
                                    fragment.showBuffer(null);
                                }
                                sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 500);
                                return;
                            }
                        }

                        if (fragment.isSeeking) {
                            fragment.isSeeking = false;
                            fragment.mSeekToPosition = 0;
                            fragment.touchPosition = 0;
                            fragment.showPannelDelayOut();
                        }

                        bufferingCount = 0;
                        if (fragment.isBufferShow()) {
                            // ?????????????????????buffer???????????????
                            fragment.hideBuffer();
                        }
                        // ??????????????????????????????????????????????????????????????????
                        if (fragment.isPopWindowShow()) {
                            fragment.removeBufferingLongTime();
                        }

                        // ???????????????
                        fragment.mCurrentPosition = mediaPosition;
                        fragment.player_seekBar.setProgress(fragment.mCurrentPosition);
                        fragment.updatePlayerPause();
                    }
                    sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 1000);// ???????????????500ms????????????????????????????????????????????????????????????1s?????????????????????
                    break;
                case MSG_HIDE_PANEL:
                    fragment.hidePanel();
                    break;
                case MSG_DELAY_PLAY:
                    fragment.mPlaybackService.pausePlayer();
                    fragment.mPlaybackService.startPlayer();
                    break;
                case MENU_HIDE:
                    if(fragment.settingMenu!=null)
                    fragment.settingMenu.sendMsg();
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
            case QUALITY_NORMAL:// ??????
                return getResources().getDrawable(R.drawable.player_stream_normal);
            case QUALITY_MEDIUM:// ??????
                return getResources().getDrawable(R.drawable.player_stream_high);
            case QUALITY_HIGH:// ??????
                return getResources().getDrawable(R.drawable.player_stream_super);
            case QUALITY_ULTRA:// 1080P
                return getResources().getDrawable(R.drawable.player_stream_1080p);
            case QUALITY_BLUERAY:// ??????
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
                } else {
/*add by dragontec for bug 4413 start*/
                    mPlaybackService.pausePlayer();
/*add by dragontec for bug 4413 end*/
                }
            } else if (baseActivity.isNoNetDialogShowing() && NetworkUtils.isConnected(context)) {
                if(!baseActivity.isFinishing())
                baseActivity.dismissNoNetConnectDialog();
                mPlaybackService.resetPreload();
                mPlaybackService.preparePlayer(extraItemPk, extraSubItemPk, extraSource);
                showBuffer(null);
            }
        }
    }

    private boolean isSetupNetClick;// ????????????

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
            if (mPlaybackService == null) {
                return;
            }
            if (mPlaybackService.isPreview()) {
                mPlaybackService.logExpenseVideoPreview(mCurrentPosition, "cancel");
            }
            if (mPlaybackService.getMediaPlayer() != null && !IsmartvPlayer.isPreloadCompleted && isPreloadIn) {
                mPlaybackService.getMediaPlayer().logPreloadEnd();
            }
            mHandler.removeCallbacksAndMessages(null);
            timerStop();
            removeBufferingLongTime();
            hideBuffer();
            hidePanel();
            mPlaybackService.addHistory(mCurrentPosition,true);
            closeActivity("source");
        }
    }

    @Override
    public void onDestroy() {
/*add by dragontec for bug 4205 start*/
		/*add by dragontec for bug 4405 start*/
        releasePlayService(false);
		/*add by dragontec for bug 4405 end*/
        if (player_container != null) {
            player_container.setOnHoverListener(null);
            player_container.setOnClickListener(null);
            player_container.removeAllViews();
            player_container = null;
        }
        if (player_surface != null) {
            player_surface.setOnHoverListener(null);
            player_surface.setOnClickListener(null);
            player_surface = null;
        }
        panel_layout = null;
        mAdvertisement = null;
/*add by dragontec for bug 4205 end*/
        mClient=null;
        super.onDestroy();
    }
	/*add by dragontec for bug 4405 start*/
    private void releasePlayService(boolean isDirect)
    {
        if (mPlaybackService != null) {
            PlaybackService.ServiceCallback cb = mPlaybackService.getCallback();
            if(isDirect || (cb != null && cb == this))
            {
                mPlaybackService.setCallback(null);
                mPlaybackService.setQiyiContainer(null);
                mPlaybackService.setSurfaceView(null);
            }
        }
    }
	/*add by dragontec for bug 4405 end*/
}
