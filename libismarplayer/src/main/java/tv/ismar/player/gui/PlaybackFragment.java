package tv.ismar.player.gui;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
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

import java.util.List;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.ad.Advertisement;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.library.util.LogUtils;
import tv.ismar.player.IsmartvPlayer;
import tv.ismar.player.R;
import tv.ismar.player.widget.AdImageDialog;

public class PlaybackFragment extends Fragment implements PlaybackService.Client.Callback,
        PlayerMenu.OnCreateMenuListener, Advertisement.OnPauseVideoAdListener, PlaybackService.ServiceCallback {

    private final String TAG = "LH/PlayerFragment";
    public static final int PAYMENT_REQUEST_CODE = 0xd6;
    public static final int PAYMENT_SUCCESS_CODE = 0x5c;

    private static final byte POP_TYPE_BUFFERING_LONG = 1;// 播放过程中,缓冲时间过长
    private static final byte POP_TYPE_PLAYER_ERROR = 3;// 底层onError回调
    private static final byte POP_TYPE_PLAYER_NET_ERROR = 4;// 首次进入，播放器初始化50S以后未见onPrepared回调

    private static final String ARG_PK = "ARG_PK";
    private static final String ARG_SUB_PK = "ARG_SUB_PK";
    private static final String ARG_SOURCE = "ARG_SOURCE";
    private static final String ARG_CLICK_PLAY = "ARG_CLICK_PLAY";
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
    // 以下为弹出菜单id
    private static final int MENU_QUALITY_ID_START = 0;// 码率起始id
    private static final int MENU_QUALITY_ID_END = 8;// 码率结束id
    private static final int MENU_TELEPLAY_ID_START = 100;// 电视剧等多集影片起始id
    private static final int MENU_KEFU_ID = 20;// 客服中心
    private static final int MENU_RESTART = 30;// 从头播放

    private int itemPK = 0;// 当前影片pk值,通过/api/item/{pk}可获取详细信息
    private int subItemPk = 0;// 当前多集片pk值,通过/api/subitem/{pk}可获取详细信息
    private String source = "";
    private boolean clickDetailPlay;
    // 播放器UI
    private FrameLayout player_container;
    private SurfaceView player_surface;
    private LinearLayout panel_layout;
    private SeekBar player_seekBar;
    private PlayerMenu playerMenu;
    private ImageView player_logo_image;
    private TextView ad_count_text, ad_vip_text;
    private View ad_vip_layout;
    private ListView player_menu;
    private LinearLayout player_buffer_layout;
    private ImageView player_buffer_img;
    private TextView player_buffer_text;
    private ImageView player_previous, player_forward, player_start;
    private ImageView player_shadow;

    private AnimationDrawable animationDrawable;
    private Animation panelShowAnimation;
    private Animation panelHideAnimation;
    private AdImageDialog adImageDialog;
    private Advertisement mAdvertisement;

    private boolean sharpSetupKeyClick = false; // 夏普电视“设置”Activity样式为Dialog样式
    public boolean mounted;// 播放影片时插拔SD卡，夏普585会弹出系统Dialog，播放器会进入onPause,但是仍需正常播放
    private boolean mIsExiting;// 正在退出播放器页面
    private PlaybackService.Client mClient;
    private PlaybackService mPlaybackService;
    private boolean mIsPlayingAd;// 判断是否正在播放广告
    private boolean mIsInAdDetail;// 是否在广告详情页
    private boolean isSeeking = false;// 空鼠拖动进度条,左右键快进快退,切换码率
    private boolean mIsOnPaused;// 调用pause()之后部分机型会执行BufferStart(701)

    private PlaybackHandler mHandler;

    public PlaybackFragment() {
        // Required empty public constructor
    }

    public static PlaybackFragment newInstance(int pk, int subPk, String source, boolean clickDetailPlay) {
        PlaybackFragment fragment = new PlaybackFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PK, pk);
        args.putInt(ARG_SUB_PK, subPk);
        args.putString(ARG_SOURCE, source);
        args.putBoolean(ARG_CLICK_PLAY, clickDetailPlay);
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
        itemPK = bundle.getInt(ARG_PK);
        subItemPk = bundle.getInt(ARG_SUB_PK);
        source = bundle.getString(ARG_SOURCE);
        clickDetailPlay = bundle.getBoolean(ARG_CLICK_PLAY, false);
        panelShowAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.fly_up);
        panelHideAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.fly_down);
        mAdvertisement = new Advertisement(getActivity());
        mHandler = new PlaybackHandler();
        mClient = new PlaybackService.Client(getActivity(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtils.i(TAG, "onCreateView");
        View contentView = inflater.inflate(R.layout.fragment_playback, container, false);
        initView(contentView);
        return contentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtils.i(TAG, "onStart > setup : " + sharpSetupKeyClick + " sdcard : " + mounted);
        mClient.connect();
        mAdvertisement.setOnPauseVideoAdListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtils.i(TAG, "onPause > setup : " + sharpSetupKeyClick + " sdcard : " + mounted);
        if (sharpSetupKeyClick || mounted) {
            sharpSetupKeyClick = false;
            mounted = false;
            return;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtils.i(TAG, "onStop > setup : " + sharpSetupKeyClick + " sdcard : " + mounted);
        mClient.disconnect();
        mAdvertisement.setOnPauseVideoAdListener(null);
        // TODO remove all callbacks
        mHandler.removeCallbacksAndMessages(null);

    }

    @Override
    public void onConnected(PlaybackService service) {
        mPlaybackService = service;
        mPlaybackService.setCallback(this);
        if (!clickDetailPlay) {
            // 点击海报直接进入播放
            mPlaybackService.preparePlayer(itemPK, subItemPk, source);
        }
        mPlaybackService.startPlayWhenPrepared(player_surface, player_container);

    }

    @Override
    public void onDisconnected() {
        mPlaybackService.setCallback(null);
        mPlaybackService = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.i(TAG, "resultCode:" + resultCode + " request:" + requestCode);
        if (requestCode == PAYMENT_REQUEST_CODE) {
            if (resultCode != PAYMENT_SUCCESS_CODE) {
                finishActivity("finish");
            }
        }
    }

    private void finishActivity(String to) {
        mIsExiting = true;
        if (mPlaybackService != null) {
            mPlaybackService.exitPlayerUI(to);
        }
        getActivity().finish();
    }

    private void initView(View contentView) {
        player_container = (FrameLayout) contentView.findViewById(R.id.player_container);
        player_surface = (SurfaceView) contentView.findViewById(R.id.player_surface);
        panel_layout = (LinearLayout) contentView.findViewById(R.id.panel_layout);
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
                        || isPopWindowShow() || mIsPlayingAd) {
                    return;
                }
                if (isMenuShow()) {
                    hideMenu();
                    return;
                }
                playPauseVideo();
            } else if (i == R.id.ad_vip_text) {
                mHandler.removeMessages(MSG_AD_COUNTDOWN);
                ad_vip_layout.setVisibility(View.GONE);
                goOtherPage(EVENT_CLICK_VIP_BUY);
            }

        }
    };

    private View.OnHoverListener onHoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            return false;
        }
    };

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    public void loadPauseAd(List<AdElementEntity> adList) {

    }

    @Override
    public boolean onMenuClicked(PlayerMenu playerMenu, int id) {
        return false;
    }

    @Override
    public void onMenuCloseed(PlayerMenu playerMenu) {

    }

    @Override
    public void showAdCountDownTime(int count) {

    }

    @Override
    public void updatePlayPause(boolean isPlaying) {
        player_shadow.setVisibility(View.GONE);

    }

    @Override
    public void showBuffering(boolean showBuffer) {

    }

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
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finishActivity("finish");
                return true;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    private void playPauseVideo() {
        if (mIsExiting || mIsPlayingAd || mPlaybackService == null || mPlaybackService.getMediaPlayer() == null
                || mPlaybackService.getItemEntity().getLiveVideo() || !mPlaybackService.isPlayerPrepared()) {
            return;
        }
        if (mPlaybackService.getMediaPlayer().isPlaying()) {
            mIsOnPaused = true;
            mPlaybackService.getMediaPlayer().pause();
            if (mPlaybackService.getMediaPlayer().getPlayerMode() == IsmartvPlayer.MODE_SMART_PLAYER) {
                mAdvertisement.fetchVideoStartAd(mPlaybackService.getItemEntity(), Advertisement.AD_MODE_ONPAUSE, source);
            }
        } else {
            mIsOnPaused = false;
            mPlaybackService.getMediaPlayer().start();
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
        if (mHandler.hasMessages(MSG_SEK_ACTION)) {
            mHandler.removeMessages(MSG_SEK_ACTION);
        }
        switch (type) {
            case EVENT_CLICK_VIP_BUY:
                if (mPlaybackService.getMediaPlayer().getPlayerMode() == IsmartvPlayer.MODE_SMART_PLAYER) {
                    toPayPage(mPlaybackService.getItemEntity().getPk(), 2, 3, null);
                } else {
                    toPayPage(mPlaybackService.getItemEntity().getPk(), 2, 2, null);
                }
                break;
            case EVENT_CLICK_KEFU:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PageIntent page = new PageIntent();
                        page.toHelpPage(getActivity());
                    }
                }, 400);
                break;
            case EVENT_COMPLETE_BUY:
                // TODO
//                expenseVideoPreview("purchase");
//                if (mIsmartvPlayer != null) {
//                    mIsmartvPlayer.logVideoExit(mCurrentPosition, "finish");
//                }
                ItemEntity.Expense expense = mPlaybackService.getItemEntity().getExpense();
                PageIntentInterface.ProductCategory mode = null;
                if (1 == expense.getJump_to()) {
                    mode = PageIntentInterface.ProductCategory.item;
                }
                toPayPage(mPlaybackService.getItemEntity().getPk(), expense.getJump_to(), expense.getCpid(), mode);
                break;
        }
    }

    private int offsets = 0; // 进度条变化
    private int offn = 1;
    private static final int SHORT_STEP = 1000;
    private int seekPosition;

    private void previousClick(View view) {
        if (mPlaybackService != null && !mPlaybackService.getItemEntity().getLiveVideo()) {
            if (!isSeeking) {
                // 拖动进度条是需要一直显示Panel
                mHandler.removeMessages(MSG_HIDE_PANEL);
                if (panel_layout.getVisibility() != View.VISIBLE) {
                    panel_layout.startAnimation(panelShowAnimation);
                    panel_layout.setVisibility(View.VISIBLE);
                }
                timerStop();
                isSeeking = true;
                seekPosition = mPlaybackService.getMediaPlayer().getCurrentPosition();
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
                }
                timerStop();
                isSeeking = true;
                seekPosition = mPlaybackService.getMediaPlayer().getCurrentPosition();
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
        if (seekPosition >= clipLength) {
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
                seekPosition += clipLength * offn * 0.01;
            } else {
                seekPosition += clipLength * 0.1;
            }
        } else {
            seekPosition += 10000;
        }

        if (seekPosition > clipLength) {
            seekPosition = clipLength - 3000;
        }
        player_seekBar.setProgress(seekPosition);
    }

    private void fastBackward(int step) {
        int clipLength = mPlaybackService.getMediaPlayer().getDuration();
        if (seekPosition <= 0) {
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
                seekPosition -= clipLength * offn * 0.01;
            } else {
                seekPosition -= clipLength * 0.1;
            }
        } else {
            seekPosition -= 10000;
        }
        if (seekPosition <= 0)
            seekPosition = 0;
        player_seekBar.setProgress(seekPosition);
    }

    private boolean createMenu() {
        if (mPlaybackService == null || mPlaybackService.getMediaPlayer() == null) {
            return true;
        }
        if (playerMenu == null) {
            playerMenu = new PlayerMenu(getActivity(), player_menu);
            playerMenu.setOnCreateMenuListener(this);
            // 添加电视剧子集
            PlayerMenuItem subMenu;
            ItemEntity[] subItems = mPlaybackService.getItemEntity().getSubitems();
            if (subItems != null && subItems.length > 0 && !mPlaybackService.isPreview()) {
                subMenu = playerMenu.addSubMenu(MENU_TELEPLAY_ID_START, getResources().getString(R.string.player_menu_teleplay));
                for (ItemEntity subItem : subItems) {
                    boolean isSelected = false;
                    if (subItemPk == subItem.getPk()) {
                        isSelected = true;
                    }
                    String subItemTitle = subItem.getTitle();
                    if (subItemTitle.contains("第")) {
                        int ind = subItemTitle.indexOf("第");
                        subItemTitle = subItemTitle.substring(ind);
                    }
                    subMenu.addItem(subItem.getPk(), subItemTitle, isSelected);
                }
            }
            // 添加分辨率
            subMenu = playerMenu.addSubMenu(MENU_QUALITY_ID_START, getResources().getString(R.string.player_menu_quality));
            List<ClipEntity.Quality> qualities = mPlaybackService.getMediaPlayer().getQualities();
            if (qualities != null && !qualities.isEmpty()) {
                for (int i = 0; i < qualities.size(); i++) {
                    ClipEntity.Quality quality = qualities.get(i);
                    String qualityName = ClipEntity.Quality.getString(quality);
                    boolean isSelected = false;
                    if (mPlaybackService.getMediaPlayer().getCurrentQuality() == quality) {
                        isSelected = true;
                    }
                    // quality id从0开始,此处加1
                    subMenu.addItem(quality.getValue() + 1, qualityName, isSelected);
                }
            }
            // 添加客服
            playerMenu.addItem(MENU_KEFU_ID, getResources().getString(R.string.player_menu_kefu));
            // 添加从头播放
            if (mPlaybackService.getItemEntity() != null && !mPlaybackService.getItemEntity().getLiveVideo()) {
                playerMenu.addItem(MENU_RESTART, getResources().getString(R.string.player_menu_restart));
            }
        }
        return true;
    }

    private void showMenu() {
        if (mIsExiting || mIsPlayingAd || mPlaybackService == null || mPlaybackService.getMediaPlayer() == null) {
            return;
        }
        if (!isMenuShow()) {
            if (isPanelShow()) {
                hidePanel();
            }
            createMenu();
            playerMenu.show();
        }
    }

    private void hideMenu() {
        if (isMenuShow()) {
            playerMenu.hide();
        }
    }

    public boolean isMenuShow() {
        if (playerMenu == null) {
            return false;
        }
        return playerMenu.isVisible();
    }

    public void showPannelDelayOut() {
        if (mIsExiting || mIsPlayingAd || mPlaybackService == null || mPlaybackService.getMediaPlayer() == null
                || isPopWindowShow() || isMenuShow()
                || !mPlaybackService.isPlayerPrepared()) {
            return;
        }
        if (panel_layout.getVisibility() != View.VISIBLE) {
            panel_layout.startAnimation(panelShowAnimation);
            panel_layout.setVisibility(View.VISIBLE);
            mHandler.sendEmptyMessageDelayed(MSG_HIDE_PANEL, 3000);
        } else {
            mHandler.removeMessages(MSG_HIDE_PANEL);
            mHandler.sendEmptyMessageDelayed(MSG_HIDE_PANEL, 3000);
        }
    }

    private void hidePanel() {
        if (panel_layout != null && panel_layout.getVisibility() == View.VISIBLE) {
            panel_layout.startAnimation(panelHideAnimation);
            panel_layout.setVisibility(View.GONE);
            mHandler.removeMessages(MSG_HIDE_PANEL);
        }
    }

    private boolean isPanelShow() {
        return panel_layout != null && panel_layout.getVisibility() == View.VISIBLE;
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

    private void showExitPopup(final byte popType) {
        if (mIsExiting || isPopWindowShow()) {
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
                            finishActivity("source");
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
//                        if (mIsmartvPlayer != null && (mIsmartvPlayer.getDuration() - mCurrentPosition <= value)) {
//                            mCurrentPosition = 0;
//                        }
//                        if (!mIsPlayingAd) {
//                            addHistory(mCurrentPosition, true, false);
//                        }
//                        mIsmartvPlayer = null;
//                        finishActivity("source");
                        break;
                    case POP_TYPE_BUFFERING_LONG:
//                        if (closePopup) {
//                            closePopup = false;
//                            return;
//                        }
//                        if (!isExit) {
//                            isClickBufferLong = true;
//                            if (mCurrentQuality == null) {
//                                Log.e(TAG, "mCurrentQuality:" + mCurrentQuality);
//                                return;
//                            }
//                            if (!popDialog.isConfirmClick) {
//                                showBuffer(null);
////                                isClickBufferLongSwitch = true;
//                                if (!isMenuShow()) {
//                                    if (isPanelShow()) {
//                                        hidePanel();
//                                    }
//                                    createMenu();
//                                    ItemEntity[] subItems = mItemEntity.getSubitems();
//                                    if (subItems != null && subItems.length > 0 && !mIsPreview) {
//                                        // 电视剧
//                                        playerMenu.showQuality(1);
//                                    } else {
//                                        // 电影
//                                        playerMenu.showQuality(0);
//                                    }
//                                }
//                            } else {
//                                // 重新加载
//                                timerStop();
//                                showBuffer(null);
//                                mIsmartvPlayer.switchQuality(mCurrentPosition, mCurrentQuality);
//                            }
//                        }
                        break;
                    case POP_TYPE_PLAYER_NET_ERROR:
                        if (popDialog.isConfirmClick) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(intent);
                        }
                        break;
                }
            }
        });
    }

    private static class PlaybackHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEK_ACTION:
                    break;
                case MSG_AD_COUNTDOWN:
                    break;
                case MSG_SHOW_BUFFERING_LONG:
                    break;
                case MSG_UPDATE_PROGRESS:
                    break;
                case MSG_HIDE_PANEL:
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
}
