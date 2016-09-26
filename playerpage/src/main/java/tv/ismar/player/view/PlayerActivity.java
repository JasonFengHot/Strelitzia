package tv.ismar.player.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.VodApplication;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.core.PlayCheckManager;
import tv.ismar.app.db.HistoryManager;
import tv.ismar.app.entity.DBQuality;
import tv.ismar.app.entity.History;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.player.PlayerPageContract;
import tv.ismar.player.R;
import tv.ismar.player.databinding.ActivityPlayerBinding;
import tv.ismar.player.media.IPlayer;
import tv.ismar.player.media.IsmartvPlayer;
import tv.ismar.player.media.PlayerBuilder;
import tv.ismar.player.media.PlayerMenu;
import tv.ismar.player.media.PlayerMenuItem;
import tv.ismar.player.presenter.PlayerPagePresenter;
import tv.ismar.player.viewmodel.PlayerPageViewModel;

public class PlayerActivity extends BaseActivity implements PlayerPageContract.View,
        IPlayer.OnVideoSizeChangedListener, IPlayer.OnStateChangedListener, IPlayer.OnBufferChangedListener {

    private final String TAG = "LH/PlayerActivity";
    private static final String HISTORYCONTINUE = "上次放映：";
    private static final String PlAYSTART = "即将放映：";
    private static final int MSG_SEK_ACTION = 103;
    private static final int MSG_AD_COUNTDOWN = 104;
    // 以下为弹出菜单id
    private static final int MENU_QUALITY_ID_START = 0;// 码率起始id
    private static final int MENU_QUALITY_ID_END = 8;// 码率结束id
    private static final int MENU_TELEPLAY_ID_START = 100;// 电视剧等多集影片起始id
    private static final int MENU_KEFU_ID = 20;// 客服中心
    private static final int MENU_RESTART = 30;// 从头播放

    private int itemPK = 0;// 当前影片pk值,通过/api/item/{pk}可获取详细信息
    private int subItemPk = 0;// 当前多集片pk值,通过/api/subitem/{pk}可获取详细信息
    private int mCurrentPosition;// 当前播放位置
    //    private int mCurrentTeleplayIndex = 0;
    //    private int mCurrentQualityIndex = 0;
    private ItemEntity mItemEntity;
    //    private ClipEntity mClipEntity;
    private boolean mIsPreview;
    private boolean isNeedResume = false;

    // 历史记录
    private HistoryManager historyManager;
    private History mHistory;
    private int mediaHistoryPosition;// 起播位置
    private boolean mIsContinue;

    // 播放器
    private IsmartvPlayer mIsmartvPlayer;
    private SurfaceView surfaceView;
    private FrameLayout player_container;
    private LinearLayout panel_layout;
    private SeekBar player_seekBar;
    private PlayerMenu playerMenu;
    private static final int SHORT_STEP = 1000;
    private boolean isSeeking = false;
    private boolean isFastFBClick = false;
    private ImageView player_logo_image;
    private ImageView ad_vip_btn;
    private TextView ad_count_text;
    private boolean isInit = false;
    private boolean mIsPlayingAd;
    // loading UI 由于需求改为当前Activity浮层
    private LinearLayout player_loading;
    private ImageView dialog_back_img;
    private TextView tipTextView;
    private AnimationDrawable animationDrawable;

    private PlayerPageViewModel mModel;
    private PlayerPageContract.Presenter mPresenter;
    private PlayerPagePresenter mPlayerPagePresenter;
    private ActivityPlayerBinding mBinding;

    private Animation panelShowAnimation;
    private Animation panelHideAnimation;

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        itemPK = intent.getIntExtra(PageIntentInterface.EXTRA_PK, 0);
        subItemPk = intent.getIntExtra(PageIntentInterface.EXTRA_SUBITEM_PK, 0);

        if (itemPK <= 0) {
            finish();
            Log.i(TAG, "itemId can't be null.");
            return;
        }

        mPlayerPagePresenter = new PlayerPagePresenter(getApplicationContext(), this);
        mModel = new PlayerPageViewModel(this, mPlayerPagePresenter);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_player);
        mBinding.setTasks(mModel);
        mBinding.setActionHandler(mPresenter);

        surfaceView = findView(R.id.surfaceView);
        player_container = findView(R.id.player_container);
        panel_layout = findView(R.id.panel_layout);
        player_seekBar = findView(R.id.player_seekBar);
        player_logo_image = findView(R.id.player_logo_image);
        ad_vip_btn = findView(R.id.ad_vip_btn);
        ad_count_text = findView(R.id.ad_count_text);
        player_loading = findView(R.id.player_loading);
        dialog_back_img = findView(R.id.dialog_back_img);
        tipTextView = findView(R.id.tipTextView);

        panelShowAnimation = AnimationUtils.loadAnimation(this,
                R.anim.fly_up);
        panelHideAnimation = AnimationUtils.loadAnimation(this,
                R.anim.fly_down);

        ad_vip_btn.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        ad_vip_btn.setImageResource(R.drawable.ad_vip_btn_focus);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        ad_vip_btn.setImageResource(R.drawable.ad_vip_btn_normal);
                        break;
                }
                return false;
            }
        });

        mGestureDetector = new GestureDetector(this, onGestureListener);
        surfaceView.setOnHoverListener(onHoverListener);
        surfaceView.setOnClickListener(onClickListener);
        player_container.setOnHoverListener(onHoverListener);
        player_container.setOnClickListener(onClickListener);
        dialog_back_img.setBackgroundResource(R.drawable.module_loading);
        animationDrawable = (AnimationDrawable) dialog_back_img.getBackground();
        player_seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        mPresenter.start();
        mPresenter.fetchItem(String.valueOf(itemPK));
        showBuffer(null);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedResume) {
            isNeedResume = false;
            if (mItemEntity == null || mPresenter == null) {
                finish();
                return;
            }
            showBuffer(null);
            mPresenter.fetchItem(String.valueOf(mItemEntity.getPk()));
        }
    }

    @Override
    protected void onStop() {
        hidePanel();
        timerStop();
        hideMenu();
        if (mHandler.hasMessages(MSG_SEK_ACTION)) {
            mHandler.removeMessages(MSG_SEK_ACTION);
        }
        if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
        }
        if (!mIsPlayingAd) {
            createHistory(mCurrentPosition);
            addHistory(mCurrentPosition);
        }
        if (!isNeedResume) {
            mPresenter.stop();
            if (mIsmartvPlayer != null) {
                mIsmartvPlayer.release();
                mIsmartvPlayer = null;
            }
        }
        super.onStop();
    }

    public void onBuyVip(View view) {
        if (mIsmartvPlayer == null) {
            return;
        }
        mHandler.removeMessages(MSG_AD_COUNTDOWN);
        ad_vip_btn.setVisibility(View.GONE);
        ad_count_text.setVisibility(View.GONE);
        isNeedResume = true;
        mIsmartvPlayer.release();
        mIsmartvPlayer = null;
        PageIntent pageIntent = new PageIntent();
        pageIntent.toPayment(this,
                String.valueOf(mItemEntity.getPk()),
                "2", "2", null);
    }

    @Override
    public void loadItem(ItemEntity itemEntity) {
        if (itemEntity == null) {
            Toast.makeText(PlayerActivity.this, "不存在该影片.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mItemEntity = itemEntity;
        ItemEntity.Clip clip = itemEntity.getClip();
        ItemEntity.SubItem[] subItems = itemEntity.getSubitems();
        if (subItemPk > 0 && subItems != null) {
            int history_sub_item = initHistorySubItemPk();
            if (history_sub_item > 0) {
                subItemPk = history_sub_item;
            }
            // 获取当前要播放的电视剧Clip
            for (int i = 0; i < subItems.length; i++) {
                int _subItemPk = subItems[i].getPk();
                if (subItemPk == _subItemPk) {
                    clip = subItems[i].getClip();
                    mItemEntity.setTitle(subItems[i].getTitle());
                    mItemEntity.setClip(clip);
                    break;
                }
            }
        }
        final String sign = "";
        final String code = "1";
        final ItemEntity.Clip playCheckClip = clip;
        mIsPreview = false;
        if (mItemEntity.getExpense() != null) {
            PlayCheckManager.getInstance(mSkyService).check(String.valueOf(mItemEntity.getPk()), new PlayCheckManager.Callback() {
                @Override
                public void onSuccess(boolean isBuy, int remainDay) {
                    Log.e(TAG, "play check isBuy:" + isBuy + " " + remainDay);
                    if (isBuy) {
                        mPresenter.fetchMediaUrl(playCheckClip.getUrl(), sign, code);
                    } else {
                        ItemEntity.Preview preview = mItemEntity.getPreview();
                        mPresenter.fetchMediaUrl(preview.getUrl(), sign, code);
                        mIsPreview = true;
                    }
                }

                @Override
                public void onFailure() {
                    Log.e(TAG, "play check fail");
                    ItemEntity.Preview preview = mItemEntity.getPreview();
                    mPresenter.fetchMediaUrl(preview.getUrl(), sign, code);
                    mIsPreview = true;
                }
            });
        } else {
            mPresenter.fetchMediaUrl(clip.getUrl(), sign, code);
        }

    }

    @Override
    public void loadClip(ClipEntity clipEntity) {
        if (clipEntity == null) {
            Toast.makeText(PlayerActivity.this, "获取播放地址错误,将退出播放器.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, clipEntity.toString());
        String iqiyi = clipEntity.getIqiyi_4_0();
        byte playerMode;
        if (Utils.isEmptyText(iqiyi)) {
            // 片源为视云
            playerMode = PlayerBuilder.MODE_SMART_PLAYER;
        } else {
            // 片源为爱奇艺
            playerMode = PlayerBuilder.MODE_QIYI_PLAYER;
        }
        ClipEntity.Quality initQuality = initPlayerData(playerMode);
        mIsmartvPlayer = PlayerBuilder.getInstance()
                .setActivity(PlayerActivity.this)
                .setPlayerMode(playerMode)
                .setItemEntity(mItemEntity)
                .setSurfaceView(surfaceView)
                .setContainer(player_container)
                .build();
        mIsmartvPlayer.setOnBufferChangedListener(this);
        mIsmartvPlayer.setOnStateChangedListener(this);
        mIsmartvPlayer.setOnVideoSizeChangedListener(this);
        mIsmartvPlayer.setDataSource(clipEntity, initQuality, new IPlayer.OnDataSourceSetListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "player init success.");
                if (mediaHistoryPosition > 0) {
                    showBuffer(HISTORYCONTINUE + getTimeString(mediaHistoryPosition));
                }
                mIsmartvPlayer.prepareAsync();
            }

            @Override
            public void onFailed(String message) {
                Log.i(TAG, "player init fail: " + message);
                Toast.makeText(PlayerActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private int initHistorySubItemPk() {
        if (historyManager == null) {
            historyManager = VodApplication.getModuleAppContext().getModuleHistoryManager();
        }
        if (itemPK != subItemPk) {
            String historyUrl = Utils.getSubItemUrl(subItemPk);
            String isLogin = "no";
            if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken())) {
                isLogin = "yes";
            }
            mHistory = historyManager.getHistoryByUrl(historyUrl, isLogin);
            if (mHistory != null) {
                int sub_item_pk = Utils.getItemPk(mHistory.sub_url);
                if (sub_item_pk > 0) {
                    return sub_item_pk;
                }
            }
        }
        return -1;
    }

    private ClipEntity.Quality initPlayerData(byte playerMode) {
        if (historyManager == null) {
            historyManager = VodApplication.getModuleAppContext().getModuleHistoryManager();
        }
        ClipEntity.Quality initQuality = null;
        isInit = false;
        String historyUrl = Utils.getItemUrl(itemPK);
        if (itemPK != subItemPk) {
            historyUrl = Utils.getSubItemUrl(subItemPk);
        }
        String isLogin = "no";
        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken())) {
            isLogin = "yes";
        }
        mHistory = historyManager.getHistoryByUrl(historyUrl, isLogin);
        if (mHistory != null) {
            initQuality = ClipEntity.Quality.getQuality(mHistory.last_quality);
            mIsContinue = mHistory.is_continue;
            mediaHistoryPosition = (int) mHistory.last_position;
        }
        showBuffer(PlAYSTART + mItemEntity.getTitle());
        return initQuality;

    }

    @Override
    public void setPresenter(PlayerPageContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onHttpFailure(Throwable e) {
    }

    @Override
    public void onHttpInterceptor(Throwable e) {
    }

    @Override
    public void onBufferStart() {
        Log.i(TAG, "onBufferStart");
        if (!isSeeking) {
            timerStop();
            showBuffer(null);
        }

    }

    @Override
    public void onBufferEnd() {
        Log.i(TAG, "onBufferEnd");
        if (!isSeeking || mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_QIYI_PLAYER) {
            timerStart(500);
            hideBuffer();
        }

    }

    @Override
    public void onPrepared() {
        if (mIsmartvPlayer == null) {
            return;
        }
        Log.i(TAG, "onPrepared:" + mediaHistoryPosition);
        mModel.setPanelData(mIsmartvPlayer, mItemEntity.getTitle());
        if (mediaHistoryPosition > 0 && !mIsPreview) {
            mIsmartvPlayer.seekTo(mediaHistoryPosition);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsmartvPlayer != null && !mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.start();
                }
            }
        }, 500);

    }

    @Override
    public void onAdStart() {
        Log.i(TAG, "onAdStart");
        mIsPlayingAd = true;
        switch (mIsmartvPlayer.getPlayerMode()) {
            case PlayerBuilder.MODE_SMART_PLAYER:
                ad_vip_btn.setVisibility(View.GONE);
                ad_count_text.setVisibility(View.VISIBLE);
                break;
            case PlayerBuilder.MODE_QIYI_PLAYER:
                ad_vip_btn.setVisibility(View.VISIBLE);
                ad_count_text.setVisibility(View.VISIBLE);
                ad_vip_btn.setFocusable(true);
                ad_vip_btn.requestFocus();
                break;
        }
        mHandler.removeMessages(MSG_AD_COUNTDOWN);
        mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
    }

    @Override
    public void onAdEnd() {
        Log.i(TAG, "onAdEnd");
        mIsPlayingAd = false;
        ad_vip_btn.setVisibility(View.GONE);
        ad_count_text.setVisibility(View.GONE);
        mHandler.removeMessages(MSG_AD_COUNTDOWN);
    }

    // 奇艺播放器在onPrepared时无法获取到影片时长
    @Override
    public void onStarted() {
        Log.i(TAG, "onStarted");
        if (!isInit) {
            String logo = mItemEntity.getLogo();
            Log.i(TAG, "clipLength:" + mIsmartvPlayer.getDuration() + " logo:" + logo);
            if (!Utils.isEmptyText(logo) && mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                Picasso.with(this).load(logo).into(player_logo_image, new Callback() {
                    @Override
                    public void onSuccess() {
                        player_logo_image.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                    }
                });

            }
            player_seekBar.setMax(mIsmartvPlayer.getDuration());
            player_seekBar.setPadding(0, 0, 0, 0);
            isInit = true;
        }
        if (!mIsPlayingAd) {
            mModel.updatePlayerPause();
            if (!isSeeking) {
                timerStart(0);
            }
            showPannelDelayOut();
        }

    }

    @Override
    public void onPaused() {
        Log.i(TAG, "onPaused");
        timerStop();
        mModel.updatePlayerPause();
        showPannelDelayOut();
    }

    @Override
    public void onSeekComplete() {
        Log.i(TAG, "onSeekComplete");
        if (isSeeking) {
//            if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        dismissProgressDialog();
//                    }
//                }, 500);
//            } else {
//                dismissProgressDialog();
//            }
            timerStart(500);
            if (mIsmartvPlayer != null && !mIsmartvPlayer.isPlaying()) {
                mIsmartvPlayer.start();
            }
        }

    }

    @Override
    public void onCompleted() {
        hidePanel();
        timerStop();
        hideMenu();
        if (mIsPreview) {
            mIsPreview = false;
            // new_vip中传递的serialItem都为空
//            if (mItemEntity.getExpense() == null) {
//                if (mItemEntity.getPk() != mItemEntity.getItemPk()) {
//                    Intent intent = new Intent();
//                    intent.setAction("tv.ismar.daisy.DramaList");
//                    intent.putExtra("item", serialItem);
//                    startActivity(intent);
//                }
//                finish();
//                return;
//            }
            if (mHandler.hasMessages(MSG_SEK_ACTION)) {
                mHandler.removeMessages(MSG_SEK_ACTION);
            }
            if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
                mHandler.removeMessages(MSG_AD_COUNTDOWN);
            }
            mIsmartvPlayer.release();
            mIsmartvPlayer = null;
            if (mItemEntity.getLiveVideo() && "sport".equals(mItemEntity.getContentModel())) {
                finish();
            } else {
                isNeedResume = true;
                ItemEntity.Expense expense = mItemEntity.getExpense();
                String mode = null;
                if (1 == mItemEntity.getExpense().getJump_to()) {
                    mode = "item";
                }
                PageIntent pageIntent = new PageIntent();
                pageIntent.toPayment(this, String.valueOf(mItemEntity.getPk()), String.valueOf(expense.getJump_to()),
                        String.valueOf(expense.getCpid()), mode);
            }
        } else {
            ItemEntity.SubItem[] subItems = mItemEntity.getSubitems();
            if (subItems != null) {
                for (int i = 0; i < subItems.length; i++) {
                    if (subItemPk == subItems[i].getPk() && i < subItems.length - 1) {
                        ItemEntity.SubItem nextItem = subItems[i + 1];
                        if (nextItem != null && nextItem.getClip() != null) {
                            String sign = "";
                            String code = "1";
                            mItemEntity.setTitle(nextItem.getTitle());
                            mItemEntity.setClip(nextItem.getClip());
                            subItemPk = nextItem.getPk();
                            mPresenter.fetchMediaUrl(nextItem.getClip().getUrl(), sign, code);
                            showBuffer(null);
                            addHistory(0);
                            return;
                        }
                    }
                }
            }
            Intent intent = new Intent("tv.ismar.daisy.PlayFinished");
            intent.putExtra("itemPk", String.valueOf(mItemEntity.getPk()));
            startActivity(intent);
            mCurrentPosition = 0;
            addHistory(0);
            finish();
        }
    }

    @Override
    public boolean onError(String message) {
        return false;
    }

    @Override
    public void onVideoSizeChanged(int videoWidth, int videoHeight) {

    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mItemEntity.getLiveVideo()) {
                return;
            }
            mModel.updateTimer(progress, mIsmartvPlayer.getDuration());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.i(TAG, "onStartTrackingTouch");
            if (mItemEntity.getLiveVideo()) {
                return;
            }
            timerStop();
            // 拖动进度条是需要一直显示Panel
            mHidePanelHandler.removeCallbacks(mHidePanelRunnable);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.i(TAG, "onStopTrackingTouch");
            if (mItemEntity.getLiveVideo()) {
                return;
            }
            isSeeking = true;
            showBuffer(null);
            int seekProgress = seekBar.getProgress();
            int maxSeek = mIsmartvPlayer.getDuration() - 3 * 1000;
            if (seekProgress >= maxSeek) {
                seekProgress = maxSeek;
            }
            mCurrentPosition = seekProgress;
            mIsmartvPlayer.seekTo(seekProgress);
        }
    };

    private void timerStart(int delay) {
        if (mIsmartvPlayer == null) {
            Log.e(TAG, "checkTaskStart: mIsmartvPlayer is null.");
            return;
        }
        if (mIsPlayingAd) {
            return;
        }
        if (delay > 0) {
            mTimerHandler.postDelayed(timerRunnable, delay);
        } else {
            mTimerHandler.post(timerRunnable);
        }
    }

    private void timerStop() {
        mTimerHandler.removeCallbacks(timerRunnable);
    }

    private Handler mTimerHandler = new Handler();

    private Runnable timerRunnable = new Runnable() {
        public void run() {
            if (mItemEntity.getLiveVideo() || !mIsmartvPlayer.isPlaying()) {
                return;
            }
            if (isSeeking) {
                isSeeking = false;
                mTimerHandler.postDelayed(timerRunnable, 500);
                return;
            }
            int mediaPosition = mIsmartvPlayer.getCurrentPosition();
            if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                if (mCurrentPosition == mediaPosition) {
                    mTimerHandler.postDelayed(timerRunnable, 500);
                    return;
                }
                if (isBufferShow()) {
                    hideBuffer();
                }
            }
            mCurrentPosition = mediaPosition;
            player_seekBar.setProgress(mCurrentPosition);
            mTimerHandler.postDelayed(timerRunnable, 500);
        }
    };

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEK_ACTION:
                    Log.d(TAG, "MSG_SEK_ACTION seek to " + mCurrentPosition);
                    player_seekBar.setProgress(mCurrentPosition);
                    mIsmartvPlayer.seekTo(mCurrentPosition);
                    offsets = 0;
                    offn = 1;
                    if (isFastFBClick) {
                        isFastFBClick = false;
                        showBuffer(null);
                    }
                    break;
                case MSG_AD_COUNTDOWN:
                    int countDownTime = mIsmartvPlayer.getAdCountDownTime() / 1000;
                    String time = String.valueOf(countDownTime);
                    if (countDownTime < 10) {
                        time = "0" + time;
                    }
                    if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                        ad_count_text.setText("广告倒计时" + time);
                    } else {
                        ad_count_text.setText("" + time);
                    }
                    sendEmptyMessageDelayed(MSG_AD_COUNTDOWN, 1000);
                    break;
            }
        }
    };

    private int offsets = 0; // 进度条变化
    private int offn = 1;

    private void fastForward(int step) {
        int clipLength = mIsmartvPlayer.getDuration();
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
        int clipLength = mIsmartvPlayer.getDuration();
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

    private boolean isPanelShow() {
        return panel_layout != null && panel_layout.getVisibility() == View.VISIBLE;
    }

    private void showPannelDelayOut() {
        if (panel_layout == null || mIsmartvPlayer == null || isPopWindowShow() || isMenuShow()
                || mIsPlayingAd || !mIsmartvPlayer.isInPlaybackState()) {
            return;
        }
        if (panel_layout.getVisibility() != View.VISIBLE) {
            panel_layout.startAnimation(panelShowAnimation);
            panel_layout.setVisibility(View.VISIBLE);
            mHidePanelHandler.postDelayed(mHidePanelRunnable, 3000);
        } else {
            mHidePanelHandler.removeCallbacks(mHidePanelRunnable);
            mHidePanelHandler.postDelayed(mHidePanelRunnable, 3000);
        }
    }

    private void hidePanel() {
        if (panel_layout.getVisibility() == View.VISIBLE) {
            panel_layout.startAnimation(panelHideAnimation);
            panel_layout.setVisibility(View.GONE);
            mHidePanelHandler.removeCallbacks(mHidePanelRunnable);
        }
    }

    private Handler mHidePanelHandler = new Handler();

    private Runnable mHidePanelRunnable = new Runnable() {
        @Override
        public void run() {
            hidePanel();
        }
    };

    public void previousClick(View view) {
        if (!mItemEntity.getLiveVideo()) {
            if (!isSeeking) {
                if (mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.pause();
                }
                // 拖动进度条是需要一直显示Panel
                mHidePanelHandler.removeCallbacks(mHidePanelRunnable);
                if (panel_layout.getVisibility() != View.VISIBLE) {
                    panel_layout.startAnimation(panelShowAnimation);
                    panel_layout.setVisibility(View.VISIBLE);
                }
                timerStop();
                isSeeking = true;
            }
            if (mHandler.hasMessages(MSG_SEK_ACTION))
                mHandler.removeMessages(MSG_SEK_ACTION);
            fastBackward(SHORT_STEP);
            if (view != null) {
                isFastFBClick = true;
                mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
            }
        }
    }

    public void forwardClick(View view) {
        if (!mItemEntity.getLiveVideo()) {
            if (!isSeeking) {
                if (mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.pause();
                }
                // 拖动进度条是需要一直显示Panel
                mHidePanelHandler.removeCallbacks(mHidePanelRunnable);
                if (panel_layout.getVisibility() != View.VISIBLE) {
                    panel_layout.startAnimation(panelShowAnimation);
                    panel_layout.setVisibility(View.VISIBLE);
                }
                timerStop();
                isSeeking = true;
            }
            if (mHandler.hasMessages(MSG_SEK_ACTION))
                mHandler.removeMessages(MSG_SEK_ACTION);
            fastForward(SHORT_STEP);
            if (view != null) {
                isFastFBClick = true;
                mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown");
        if (mItemEntity == null || mIsmartvPlayer == null) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
            }
            return true;
        }
        if (isMenuShow()) {
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                hidePanel();
                if (!isMenuShow()) {
                    showMenu();
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (!isPopWindowShow() && mIsmartvPlayer != null && mIsmartvPlayer.isInPlaybackState() && !mIsPlayingAd) {
                    showExitPopup();
                    return true;
                }
                if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
                    mHandler.removeMessages(MSG_AD_COUNTDOWN);
                }
                finish();
                return true;
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (isMenuShow() || isPopWindowShow()) {
                    return true;
                }
                if (mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.pause();
                } else {
                    mIsmartvPlayer.start();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                if (isMenuShow() || isPopWindowShow()) {
                    return true;
                }
                if (!mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.start();
                    hidePanel();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                if (isMenuShow() || isPopWindowShow()) {
                    return true;
                }
                if (mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.pause();
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                if (isMenuShow() || isPopWindowShow()) {
                    return true;
                }
                previousClick(null);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_FORWARD:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                if (isMenuShow() || isPopWindowShow()) {
                    return true;
                }
                forwardClick(null);
                return true;
        }
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL &&
                !isMenuShow() &&
                !isPopWindowShow();
        if (isKeyCodeSupported) {
            showPannelDelayOut();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (isSeeking) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_FORWARD:
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    showBuffer(null);
                    mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
                    return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isBufferShow()) {
            return true;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    GestureDetector.OnGestureListener onGestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (playerMenu.isVisible()) {
                return true;
            }
            showPannelDelayOut();
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    };

    private void addHistory(int last_position) {
        if (mItemEntity == null && historyManager == null || mIsmartvPlayer == null || !mIsmartvPlayer.isInPlaybackState()) {
            return;
        }
        History history = new History();
        history.title = mItemEntity.getTitle();
        ItemEntity.Expense expense = mItemEntity.getExpense();
        if (expense != null) {
            history.price = (int) expense.getPrice();
            history.paytype = expense.getPay_type();
            history.cptitle = expense.getCptitle();
            history.cpid = expense.getCpid();
            history.cpname = expense.getCpname();
        } else
            history.price = 0;
        history.adlet_url = mItemEntity.getAdletUrl();
        history.content_model = mItemEntity.getContentModel();
        history.is_complex = mItemEntity.getIsComplex();
        history.last_position = last_position;
        history.last_quality = mIsmartvPlayer.getCurrentQuality().getValue();
        String apiDomain = IsmartvActivator.getInstance().getApiDomain();
        if (!apiDomain.startsWith("http://") && !apiDomain.startsWith("https://")) {
            apiDomain = "http://" + apiDomain;
        }
        history.url = apiDomain + "/api/item/" + itemPK;
        ItemEntity.SubItem[] subItems = mItemEntity.getSubitems();
        if (subItems != null && subItems.length > 0) {
            history.sub_url = apiDomain + "/api/subitem/" + subItemPk;
        }
        history.is_continue = mIsContinue;
        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken()))
            historyManager.addHistory(history, "yes");
        else
            historyManager.addHistory(history, "no");
    }

    private void createHistory(int length) {
        if (mIsmartvPlayer == null || !mIsmartvPlayer.isInPlaybackState() || "".equals(IsmartvActivator.getInstance().getAuthToken())) {
            return;// 不登录不必上传
        }
        int offset = length;
        if (length == mIsmartvPlayer.getDuration()) {
            offset = -1;
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("offset", offset);
        if (itemPK != subItemPk) {
            params.put("subitem", subItemPk);
        } else {
            params.put("item", itemPK);
        }
        mPlayerPagePresenter.sendHistory(params);

    }

    private boolean createMenu() {
        playerMenu = new PlayerMenu(this);
        // 添加电视剧子集
        PlayerMenuItem subMenu;
        ItemEntity.SubItem[] subItems = mItemEntity.getSubitems();
        if (subItems != null && subItems.length > 0) {
            subMenu = playerMenu.addSubMenu(MENU_TELEPLAY_ID_START, getResources().getString(R.string.player_menu_teleplay));
            for (ItemEntity.SubItem subItem : subItems) {
                boolean isSelected = false;
                if (subItemPk == subItem.getPk()) {
                    isSelected = true;
                }
                subMenu.addItem(subItem.getPk(), subItem.getTitle(), isSelected);
            }
        }
        // 添加分辨率
        subMenu = playerMenu.addSubMenu(MENU_QUALITY_ID_START, getResources().getString(R.string.player_menu_quality));
        List<ClipEntity.Quality> qualities = mIsmartvPlayer.getQulities();
        if (!qualities.isEmpty()) {
            for (int i = 0; i < qualities.size(); i++) {
                ClipEntity.Quality quality = qualities.get(i);
                String qualityName = ClipEntity.Quality.getString(quality);
                boolean isSelected = false;
                if (mIsmartvPlayer.getCurrentQuality() == quality) {
                    isSelected = true;
                }
                // quality id从0开始,此处加1
                subMenu.addItem(quality.getValue() + 1, qualityName, isSelected);
            }
        }
        // 添加客服
        playerMenu.addItem(MENU_KEFU_ID, getResources().getString(R.string.player_menu_kefu));
        // 添加从头播放
        if (mItemEntity != null && !mItemEntity.getLiveVideo()) {
            playerMenu.addItem(MENU_RESTART, getResources().getString(R.string.player_menu_restart));
        }
        return true;
    }

    public boolean onMenuClicked(PlayerMenu menu, int id) {
        if (mIsmartvPlayer == null) {
            return false;
        }
        boolean ret = false;
        if (id > MENU_QUALITY_ID_START && id <= MENU_QUALITY_ID_END) {
            // id值为quality值+1
            int qualityValue = id - 1;
            ClipEntity.Quality clickQuality = ClipEntity.Quality.getQuality(qualityValue);
            if (clickQuality == null || clickQuality == mIsmartvPlayer.getCurrentQuality()) {
                // 为空或者点击的码率和当前设置码率相同
                return false;
            }
            mediaHistoryPosition = mIsmartvPlayer.getCurrentPosition();
            mIsmartvPlayer.switchQuality(clickQuality);
            if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                timerStop();
                showBuffer(null);
            }
            mModel.updateQuality();
            // 写入数据库
            historyManager.addOrUpdateQuality(new DBQuality(0, "", mIsmartvPlayer.getCurrentQuality().getValue()));
            ret = true;
        } else if (id > MENU_TELEPLAY_ID_START) {
            // id值为subItem pk值
            if (id == subItemPk) {
                return false;
            }
            for (ItemEntity.SubItem subItem : mItemEntity.getSubitems()) {
                if (subItem.getPk() == id) {
                    mediaHistoryPosition = 0;
                    timerStop();
                    mIsmartvPlayer.release();
                    mIsmartvPlayer = null;
                    ItemEntity.Clip clip = subItem.getClip();
                    String sign = "";
                    String code = "1";
                    mItemEntity.setTitle(subItem.getTitle());
                    mItemEntity.setClip(clip);
                    subItemPk = subItem.getPk();
                    if (clip != null) {
                        mPresenter.fetchMediaUrl(clip.getUrl(), sign, code);
                    }
                    showBuffer(null);
                    ret = true;
                    break;
                }
            }
        } else if (id == MENU_KEFU_ID) {
            Intent intent = new Intent();
            try {
                intent.setAction("cn.ismartv.speedtester.feedback");
            } catch (ActivityNotFoundException e) {
                intent.setAction("cn.ismar.sakura.launcher");
            }
            startActivity(intent);
            ret = true;
        } else if (id == MENU_RESTART) {
            showPannelDelayOut();
            player_seekBar.setProgress(0);
            mIsmartvPlayer.seekTo(0);
            mediaHistoryPosition = 0;
            ret = true;
        }
        return ret;
    }

    public void onMenuCloseed(PlayerMenu menu) {
        // do nothing
    }

    private void showMenu() {
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

    private boolean isMenuShow() {
        if (playerMenu == null) {
            return false;
        }
        return playerMenu.isVisible();
    }

//    private Handler hideMenuHandler = new Handler();
//
//    private Runnable hideMenuRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (playerMenu != null) {
//                playerMenu.hide();
//            }
//            hideMenuHandler.removeCallbacks(hideMenuRunnable);
//        }
//    };

    private ModuleMessagePopWindow popDialog;

    private boolean isPopWindowShow() {
        return popDialog != null && popDialog.isShowing();
    }

    private void showExitPopup() {
        if (mIsPlayingAd) {
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
            finish();
            return;
        }
        if (mHandler.hasMessages(MSG_SEK_ACTION)) {
            mHandler.removeMessages(MSG_SEK_ACTION);
        }
        timerStop();
        mIsmartvPlayer.pause();
        popDialog = new ModuleMessagePopWindow(this);
        popDialog.setFirstMessage(getString(R.string.player_exit));
        popDialog.showAtLocation(getRootView(), Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        popDialog.dismiss();
                        finish();
                    }
                },
                new ModuleMessagePopWindow.CancelListener() {
                    @Override
                    public void cancelClick(View view) {
                        popDialog.dismiss();
                    }
                });
        popDialog.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!popDialog.isConfirmClick) {
                    timerStart(0);
                    mIsmartvPlayer.start();
                }
            }
        });
    }

    private View getRootView() {
        return ((ViewGroup) (getWindow().getDecorView().findViewById(android.R.id.content))).getChildAt(0);
    }

    private View.OnHoverListener onHoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            int what = event.getAction();
            switch (what) {
                case MotionEvent.ACTION_HOVER_MOVE:
                    if (!mIsPlayingAd && isInit) {
                        showPannelDelayOut();
                    }
                    break;
            }
            return false;
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIsmartvPlayer == null || isPopWindowShow() ||
                    mIsPlayingAd || !mIsmartvPlayer.isInPlaybackState()
                    || isBufferShow() || isMenuShow()) {
                return;
            }
            if (mIsmartvPlayer.isPlaying()) {
                mIsmartvPlayer.pause();
            } else {
                mIsmartvPlayer.start();
            }
        }
    };

    private String getTimeString(int ms) {
        int left = ms;
        int hour = left / 3600000;
        left %= 3600000;
        int min = left / 60000;
        left %= 60000;
        int sec = left / 1000;
        return String.format("%1$02d:%2$02d:%3$02d", hour, min, sec);
    }

    private void showBuffer(String msg) {
        if (player_loading.getVisibility() != View.VISIBLE) {
            if (msg != null) {
                tipTextView.setText(msg);
            }
            player_loading.setVisibility(View.VISIBLE);
            if (animationDrawable != null && !animationDrawable.isRunning()) {
                animationDrawable.start();
            }
        }
    }

    private void hideBuffer() {
        if (player_loading.getVisibility() == View.VISIBLE) {
            player_loading.setVisibility(View.GONE);
            tipTextView.setText(getString(R.string.loading_text));
            if (animationDrawable != null && animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
        }
    }

    public boolean isBufferShow() {
        if (player_loading != null && player_loading.getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }

}
