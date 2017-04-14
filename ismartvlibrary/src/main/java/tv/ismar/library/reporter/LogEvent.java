package tv.ismar.library.reporter;

import java.util.HashMap;

import tv.ismar.library.util.MD5;

/**
 * Created by LongHai on 17-4-7.
 */

public class LogEvent {

    /**
     * 设备启动
     */
    public static final String SYSTEM_ON = "system_on";
    /**
     * 播放器打开
     */
    public static final String VIDEO_START = "video_start";
    /**
     * 开始播放缓冲结束
     */
    public static final String VIDEO_PLAY_LOAD = "video_play_load";
    /**
     * 切换码流
     */
    public static final String VIDEO_SWITCH_STREAM = "video_switch_stream";
    /**
     * 开始播放
     */
    public static final String VIDEO_PLAY_START = "video_play_start";
    /**
     * 播放暂停
     */
    public static final String VIDEO_PLAY_PAUSE = "video_play_pause";
    /**
     * 播放继续
     */
    public static final String VIDEO_PLAY_CONTINUE = "video_play_continue";
    /**
     * 播放快进/快退
     */
    public static final String VIDEO_PLAY_SEEK = "video_play_seek";
    /**
     * 播放快进/快退缓冲结束
     */
    public static final String VIDEO_PLAY_SEEK_BLOCKEND = "video_play_seek_blockend";
    /**
     * 播放缓冲结束
     */
    public static final String VIDEO_PLAY_BLOCKEND = "video_play_blockend";
    /**
     * 播放时网速
     */
    public static final String VIDEO_PLAY_SPEED = "video_play_speed";
    /**
     * 播放时下载速度慢
     */
    public static final String VIDEO_LOW_SPEED = "video_low_speed";
    /**
     * 播放器退出
     */
    public static final String VIDEO_EXIT = "video_exit";
    /**
     * 视频收藏
     */
    public static final String VIDEO_COLLECT = "video_collect";
    /**
     * 进入收藏界面
     */
    public static final String VIDEO_COLLECT_IN = "video_collect_in";
    /**
     * 退出收藏界面
     */
    public static final String VIDEO_COLLECT_OUT = "video_collect_out";
    /**
     * 视频存入历史
     */
    public static final String VIDEO_HISTORY = "video_history";
    /**
     * 进入播放历史界面
     */
    public static final String VIDEO_HISTORY_IN = "video_history_in";
    /**
     * 退出播放历史界面
     */
    public static final String VIDEO_HISTORY_OUT = "video_history_out";
    /**
     * 视频评分
     */
    public static final String VIDEO_SCORE = "video_score";
    /**
     * 视频评论
     */
    public static final String VIDEO_COMMENT = "video_comment";

    /**
     * 启动某视频频道
     */
    public static final String VIDEO_CHANNEL_IN = "video_channel_in";

    /**
     * 退出某视频频道
     */
    public static final String VIDEO_CHANNEL_OUT = "video_channel_out";

    /**
     * 进入分类浏览
     */
    public static final String VIDEO_CATEGORY_IN = "video_category_in";

    /**
     * 退出分类浏览
     */
    public static final String VIDEO_CATEGORY_OUT = "video_category_out";

    /**
     * 进入媒体详情页
     */
    public static final String VIDEO_DETAIL_IN = "video_detail_in";

    /**
     * 退出媒体详情页
     */
    public static final String VIDEO_DETAIL_OUT = "video_detail_out";
    /**
     * 在详情页进入关联
     */
    public static final String VIDEO_RELATE = "video_relate";

    /**
     * 进入关联界面
     */
    public static final String VIDEO_RELATE_IN = "video_relate_in";
    /**
     * 退出关联界面
     */
    public static final String VIDEO_RELATE_OUT = "video_relate_out";
    /**
     * 进入专题浏览
     */
    public static final String VIDEO_TOPIC_IN = "video_topic_in";
    /**
     * 退出专题浏览
     */
    public static final String VIDEO_TOPIC_OUT = "video_topic_out";
    /**
     * 视频预约
     */
    public static final String VIDEO_NOTIFY = "video_notify";
    /**
     * 点击视频购买
     */
    public static final String VIDEO_EXPENSE_CLICK = "video_expense_click";
    /**
     * 视频购买
     */
    public static final String VIDEO_EXPENSE = "video_expense";
    /**
     * 搜索
     */
    public static final String VIDEO_SEARCH = "video_search";
    /**
     * 搜索结果命中
     */
    public static final String VIDEO_SEARCH_ARRIVE = "video_search_arrive";
    /**
     * 播放器异常
     */
    public static final String VIDEO_EXCEPT = "video_except";
    /**
     * 栏目页异常
     */
    public static final String CATEGORY_EXCEPT = "category_except";
    /**
     * 详情页异常
     */
    public static final String DETAIL_EXCEPT = "detail_except";
    /**
     * 用户点击某个推荐影片
     */
    public static final String LAUNCHER_VOD_CLICK = "launcher_vod_click";
    /**
     * 预告片播放
     */
    public static final String LAUNCHER_VOD_TRAILER_PLAY = "launcher_vod_trailer_play";
    /**
     * 用户登录
     */
    public static final String USER_LOGIN = "user_login";
    /**
     * 进入筛选界面
     */
    public static final String VIDEO_FILTER_IN = "video_filter_in";
    /**
     * 退出筛选界面
     */
    public static final String VIDEO_FILTER_OUT = "video_filter_out";
    /**
     * 使用筛选
     */
    public static final String VIDEO_FILTER = "video_filter";
    /**
     * 进入我的频道
     */
    public static final String VIDEO_MYCHANNEL_IN = "video_mychannel_in";
    /**
     * 退出我的频道
     */
    public static final String VIDEO_MYCHANNEL_OUT = "video_mychannel_out";
    /**
     * 进入剧集列表界面
     */
    public static final String VIDEO_DRAMALIST_IN = "video_dramalist_in";
    /**
     * 退出剧集列表界面
     */
    public static final String VIDEO_DRAMALIST_OUT = "video_dramalist_out";

    public static final String FRONT_PAGE_VIDEO = "frontpagevideo";
    /**
     * 用户点击推荐影片
     */
    public static final String HOMEPAGE_VOD_CLICK = "homepage_vod_click";
    /**
     * 广告播放缓冲结束
     */
    public static final String AD_PLAY_LOAD = "ad_play_load";
    /**
     * 广告播放卡顿
     */
    public static final String AD_PLAY_BLOCKEND = "ad_play_blockend";
    /**
     * 广告播放结束
     */
    public static final String AD_PLAY_EXIT = "ad_play_exit";
    /**
     * 暂停广告播放
     */
    public static final String PAUSE_AD_PLAY = "pause_ad_play";
    /**
     * 暂停广告下载
     */
    public static final String PAUSE_AD_DOWNLOAD = "pause_ad_download";
    /**
     * 暂停广告异常
     */
    public static final String PAUSE_AD_EXCEPT = "pause_ad_except";
    /**
     * 应用启动
     */
    public static final String APP_START = "app_start";
    /**
     * 应用退出
     */
    public static final String APP_EXIT = "app_exit";

    public static final String BOOT_AD_PLAY = "boot_ad_play";

    public static final String BOOT_AD_DOWNLOAD = "boot_ad_download";

    public static final String BOOT_AD_EXCEPT = "boot_ad_except";
    public static final String HOMEPAGE_VOD_TRAILER_PLAY = "homepage_vod_trailer_play";
    public static final String EXCEPTION_EXIT = "epg_except";
    /**
     * 进入包详情
     */
    public static final String PACKAGE_DETAIL_IN = "package_detail_in";
    /**
     * 详情页缓冲完成
     */
    public static final String DETAIL_PLAY_LOAD = "detail_play_load";

    // 日志发送和原先Daisy项目相同，由于现有播放器没有使用Item对象
    private static HashMap<String, Object> getPublicParams(IsmarMedia ismarMedia, int speed, String snToken, String playerFlag) {
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put(EventProperty.ITEM, ismarMedia.getItemPk());
        if (ismarMedia.getSubItemPk() > 0 && ismarMedia.getItemPk() != ismarMedia.getSubItemPk()) {
            tempMap.put(EventProperty.SUBITEM, ismarMedia.getSubItemPk());
        }
        tempMap.put(EventProperty.CLIP, ismarMedia.getClipPk());
        tempMap.put(EventProperty.TITLE, ismarMedia.getTitle());
        tempMap.put(EventProperty.QUALITY, switchQuality(ismarMedia.getQuality()));
        tempMap.put(EventProperty.CHANNEL, ismarMedia.getChannel());
        tempMap.put(EventProperty.SPEED, speed + "KByte/s");
        tempMap.put(EventProperty.SID, MD5.getMd5ByString(snToken + System.currentTimeMillis()));
        tempMap.put(EventProperty.PLAYER_FLAG, playerFlag);
        return tempMap;
    }

    /**
     * 播放器打开 video_start
     *
     * @param media  (媒体)       Item
     *               quality (视频清晰度 normal   medium  high  ultra  adaptive) STRING
     * @param userId (用户ID) STRING
     * @param speed  (网速, 单位KB/s) INTEGER
     * @return HashMap
     */
    public static HashMap<String, Object> videoStart(IsmarMedia media, String userId, int speed, String snToken, String playerFlag) {
        if (media == null) {
            return null;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, snToken, playerFlag);
        tempMap.put("userid", userId);
        tempMap.put("source", media.getSource());
        tempMap.put("section", media.getSection());
        new MessageQueue.DataCollectionTask().execute(VIDEO_START, tempMap);
        return tempMap;
    }

    private static String switchQuality(int currQuality) {
        String quality = "";
        switch (currQuality) {
            case 0:
                quality = "low";
                break;
            case 1:
                quality = "adaptive";
                break;
            case 2:
                quality = "normal";
                break;
            case 3:
                quality = "medium";
                break;
            case 4:
                quality = "high";
                break;
            case 5:
                quality = "ultra";
                break;
            case 6:
                quality = "blueray";
                break;
            case 7:
                quality = "4k";
                break;
        }
        return quality;
    }

}
