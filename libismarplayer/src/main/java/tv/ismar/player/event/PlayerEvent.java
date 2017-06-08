package tv.ismar.player.event;

import android.os.AsyncTask;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import tv.ismar.app.core.client.MessageQueue;
import tv.ismar.library.util.DateUtils;
import tv.ismar.library.util.LogUtils;
import tv.ismar.library.util.MD5;

public class PlayerEvent {

//    private static ArrayList<String> playerEventList = new ArrayList<>();
//
//    public static synchronized ArrayList<String> getPlayerEventList() {
//        return playerEventList;
//    }

    public String title = "";
    public int pk;
    public int subItemPk;
    public int clipPk;
    public String channel = "";
    public String source = "";
    public String section = "";
    public int quality;
    public String snToken = "";
    public String username = "";
    public String sid = "";

    private static HashMap<String, Object> getPublicParams(PlayerEvent media, int speed, String playerFlag) {
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put(ITEM, media.pk);
        if (media.subItemPk > 0 && media.pk != media.subItemPk) {
            tempMap.put(SUBITEM, media.subItemPk);
        }
        tempMap.put(TITLE, media.title);
        tempMap.put(CLIP, media.clipPk);
        tempMap.put(QUALITY, switchQuality(media.quality));
        tempMap.put(CHANNEL, media.channel);
        tempMap.put(SPEED, speed + "KByte/s");
        tempMap.put(SID, media.sid);
        tempMap.put(PLAYER_FLAG, playerFlag);
        return tempMap;
    }

    /**
     * 播放器打开 video_start
     *
     * @param media (媒体)       Item
     *              quality (视频清晰度 normal   medium  high  ultra  adaptive) STRING
     * @param speed (网速, 单位KB/s) INTEGER
     */
    public static void videoStart(PlayerEvent media, int speed, String playerFlag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, playerFlag);
        String userId = TextUtils.isEmpty(media.username) ? media.snToken : media.username;
        tempMap.put("userid", userId);
        tempMap.put("source", media.source);
        tempMap.put("section", media.section);
        new DataCollectionTask().execute(VIDEO_START, tempMap);
    }

    /**
     * 开始播放缓冲结束 video_play_load
     *
     * @param media    (媒体)Item
     *                 quality  (视频清晰度)normal |  medium | high | ultra | adaptive | adaptive_norma l | adaptive_medium | adaptive_high | adaptive_ultra) STRING
     * @param duration (缓存时间,单位s)INTEGER
     * @param speed    (网速,单位KB/s)INTEGER
     * @param mediaIP  (媒体IP)STRING
     */
    public static void videoPlayLoad(PlayerEvent media,
                                     long duration, int speed, String mediaIP, String playerUrl, String playerFlag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, playerFlag);
        tempMap.put(DURATION, duration / 1000);
        tempMap.put(MEDIAIP, mediaIP);
        tempMap.put("play_url", playerUrl);
        new DataCollectionTask().execute(VIDEO_PLAY_LOAD, tempMap);

    }


    /**
     * 开始播放 video_play_start
     *
     * @param media (媒体)Item
     *              quality (视频清晰度) normal |  medium | high | ultra | adaptive) STRING
     * @param speed (网速, 单位KB/s) INTEGER
     */
    public static void videoPlayStart(PlayerEvent media, int speed, String playerFlag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, playerFlag);
        new DataCollectionTask().execute(VIDEO_PLAY_START, tempMap);

    }

    /**
     * 播放暂停 video_play_pause
     *
     * @param media    (媒体)Item
     *                 quality  (视频清晰度)normal |  medium | high | ultra | adaptive) STRING
     * @param position (位置，单位s) INTEGER
     * @param speed    (网速, 单位KB/s) INTEGER
     */
    public static void videoPlayPause(PlayerEvent media, int speed, Integer position, String playerFlag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, playerFlag);
        tempMap.put(POSITION, position / 1000);
        new DataCollectionTask().execute(VIDEO_PLAY_PAUSE, tempMap);

    }

    /**
     * 播放继续 video_play_continue
     *
     * @param media    (媒体)Item
     *                 quality  (视频清晰度:    normal |  medium | high | ultra | adaptive) STRING
     * @param position (位置，单位s)  INTEGER
     * @param speed    (网速, 单位KB/s) INTEGER
     */

    public static void videoPlayContinue(PlayerEvent media, int speed, Integer position, String playerFlag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, playerFlag);
        tempMap.put(POSITION, position / 1000);
        new DataCollectionTask().execute(VIDEO_PLAY_CONTINUE, tempMap);

    }

    /**
     * 播放快进/快退 video_play_seek
     *
     * @param media    (媒体)Item
     *                 quality  (视频清晰度:     normal |  medium | high | ultra | adaptive) STRING
     * @param position (目标位置,单位s) INTEGER
     * @param speed    (网速, 单位KB/s) INTEGER
     */

    public static void videoPlaySeek(PlayerEvent media, int speed, Integer position, String playerFlag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, playerFlag);
        tempMap.put(POSITION, position / 1000);
        new DataCollectionTask().execute(VIDEO_PLAY_SEEK, tempMap);

    }

    /**
     * 播放快进/快退缓冲结束 video_play_seek_blockend
     *
     * @param media    (媒体) Item
     *                 quality  (视频清晰度:      normal |  medium | high | ultra | adaptive | adaptive_norma l | adaptive_medium | adaptive_high | adaptive_ultra) STRING
     * @param position (缓冲位置，单位s)  INTEGER
     * @param speed    (网速, 单位KB/s) INTEGER
     * @param duration (缓存时间,单位s)  INTEGER
     * @param mediaIP  (媒体IP)STRING
     */

    public static void videoPlaySeekBlockend(PlayerEvent media, int speed, Integer position, long duration, String mediaIP, String playerFlag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, playerFlag);
        tempMap.put(DURATION, duration / 1000);
        tempMap.put(POSITION, position / 1000);
        tempMap.put(MEDIAIP, mediaIP);
        new DataCollectionTask().execute(VIDEO_PLAY_SEEK_BLOCKEND, tempMap);

    }

    /**
     * 播放缓冲结束 video_play_blockend
     *
     * @param media    (媒体) Item
     *                 quality  (视频清晰度:      normal |  medium | high | ultra | adaptive | adaptive_normal | adaptive_medium | adaptive_high | adaptive_ultra) STRING
     * @param speed    (网速, 单位KB/s) INTEGER
     * @param duration (缓存时间,单位s)  INTEGER
     * @param mediaIP  (媒体IP)STRING
     */
    public static void videoPlayBlockend(PlayerEvent media, int speed, long duration, String mediaIP, String playerFlag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, playerFlag);
        tempMap.put(DURATION, duration / 1000);
        tempMap.put(MEDIAIP, mediaIP);
        new DataCollectionTask().execute(VIDEO_PLAY_BLOCKEND, tempMap);

    }

    /**
     * 播放时网速 video_play_speed
     *
     * @param media   (媒体) INTEGER
     *                quality (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
     * @param speed   (网速, 单位KB/s) INTEGER
     * @param mediaIP (媒体IP) STRING
     */
    public static void videoPlaySpeed(PlayerEvent media, int speed, String mediaIP, String playerFlag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, playerFlag);
        tempMap.put(MEDIAIP, mediaIP);
        new DataCollectionTask().execute(VIDEO_PLAY_SPEED, tempMap);

    }

    /**
     * 播放时下载速度慢  video_low_speed
     *
     * @param media   (媒体) INTEGER
     *                quality (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
     * @param speed   (网速, 单位KB/s) INTEGER
     * @param mediaIP (媒体IP) STRING
     */

    public static void videoLowSpeed(PlayerEvent media, int speed, String mediaIP, String playerFlag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, playerFlag);
        tempMap.put(MEDIAIP, mediaIP);
        new DataCollectionTask().execute(VIDEO_LOW_SPEED, tempMap);

    }

    /**
     * 播放器退出 video_exit
     *
     * @param media (媒体) INTEGER
     *              quality (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
     * @param speed (网速, 单位KB/s) INTEGER
     * @param to    (去向：detail | end) STRING
     */

    public static void videoExit(PlayerEvent media, int speed, String to, Integer position, long duration, String playerFlag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, playerFlag);
        tempMap.put(TO, to);
        tempMap.put(POSITION, position / 1000);
        tempMap.put(DURATION, duration / 1000);
        tempMap.put(SECTION, media.section);
        tempMap.put(SOURCE, media.source);
        new DataCollectionTask().execute(VIDEO_EXIT, tempMap);

    }

    /**
     * 播放器异常 videoExcept
     *
     * @param code     (异常码servertimeout|servertimeout|noplayaddress|mediaexception|mediatimeout|filenotfound|nodetail|debuggingexception|noextras) STRING
     * @param content  (异常内容)                                                                                                                    STRING
     * @param media    (媒体) INTEGER
     *                 quality  (视频清晰度:     normal |  medium | high | ultra | adaptive | adaptive_normal | adaptive_medium | adaptive_high | adaptive_ultra) STRING
     * @param position (播放位置，单位s) INTEGER
     */

    public static void videoExcept(String code, String content, PlayerEvent media, int speed, Integer position, String playerFlag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, playerFlag);
        tempMap.put(CODE, code == null ? "" : code);
        tempMap.put(CONTENT, content == null ? "" : content);
        tempMap.put(POSITION, position / 1000);
        tempMap.put(PLAYER_FLAG, playerFlag);
        new DataCollectionTask().execute(VIDEO_EXCEPT, tempMap);
    }


    /**
     * 切换码流 video_switch_stream
     *
     * @param media   (媒体) INTEGER
     *                quality (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
     * @param mode    (切换模式：auto | manual) STRING
     * @param speed   (网速, 单位KB/s) INTEGER
     * @param mediaip STRING
     */

    public static void videoSwitchStream(PlayerEvent media, String mode, int speed, String mediaip, String playerFlag) {
        if (media == null) {
            return;
        }
        String userId = TextUtils.isEmpty(media.username) ? media.snToken : media.username;
        HashMap<String, Object> tempMap = getPublicParams(media, speed, playerFlag);
        tempMap.put(MODE, mode);
        tempMap.put("userid", userId);
        tempMap.put(MEDIAIP, mediaip);
        tempMap.put(LOCATION, "detail");
        new DataCollectionTask().execute(VIDEO_SWITCH_STREAM, tempMap);

    }

    public static void ad_play_load(PlayerEvent media, long duration, String mediaip, int ad_id, String mediaflag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put(SOURCE, media.source);
        tempMap.put(CHANNEL, media.channel);
        tempMap.put(SECTION, media.section);
        tempMap.put(DURATION, duration / 1000);
        tempMap.put(MEDIAIP, mediaip);
        tempMap.put(ITEM, media.pk);
        tempMap.put(AD_ID, ad_id);
        tempMap.put(PLAYER_FLAG, mediaflag);
        new DataCollectionTask().execute(AD_PLAY_LOAD, tempMap);
    }

    public static void ad_play_blockend(PlayerEvent media, long duration, String mediaip, int ad_id, String mediaflag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put(SOURCE, media.source);
        tempMap.put(CHANNEL, media.channel);
        tempMap.put(SECTION, media.section);
        tempMap.put(DURATION, duration / 1000);
        tempMap.put(MEDIAIP, mediaip);
        tempMap.put(ITEM, media.pk);
        tempMap.put(AD_ID, ad_id);
        tempMap.put(PLAYER_FLAG, mediaflag);
        new DataCollectionTask().execute(AD_PLAY_BLOCKEND, tempMap);
    }

    public static void ad_play_exit(PlayerEvent media, long duration, String mediaip, int ad_id, String mediaflag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put(SOURCE, media.source);
        tempMap.put(CHANNEL, media.channel);
        tempMap.put(SECTION, media.section);
        tempMap.put(DURATION, duration / 1000);
        tempMap.put(MEDIAIP, mediaip);
        tempMap.put(ITEM, media.pk);
        tempMap.put(AD_ID, ad_id);
        tempMap.put(PLAYER_FLAG, mediaflag);
        new DataCollectionTask().execute(AD_PLAY_EXIT, tempMap);
    }

    public static void pause_ad_play(String title, int media_id, String media_url, long duration, String mediaflag) {
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put(TITLE, title);
        tempMap.put(MEDIA_ID, media_id);
        tempMap.put(MEDIA_URL, media_url);
        tempMap.put(DURATION, duration / 1000);
        tempMap.put(PLAYER_FLAG, mediaflag);
        new DataCollectionTask().execute(PAUSE_AD_PLAY, tempMap);
    }

    public static void pause_ad_download(String title, int media_id, String media_url, String mediaflag) {
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put(TITLE, title);
        tempMap.put(MEDIA_ID, media_id);
        tempMap.put(MEDIA_URL, media_url);
        tempMap.put(PLAYER_FLAG, mediaflag);
        new DataCollectionTask().execute(PAUSE_AD_DOWNLOAD, tempMap);
    }

    public static void pause_ad_except(Integer errcode, String errorContent) {
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put(CODE, errcode);
        tempMap.put(CONTENT, errorContent);
        new DataCollectionTask().execute(PAUSE_AD_EXCEPT, tempMap);
    }

    private static String switchQuality(Integer currQuality) {
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

    public static class DataCollectionTask extends AsyncTask<Object, Void, Void> {

        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(Object... params) {
            if (params != null && params.length == 2) {
                String eventName = (String) params[0];
                HashMap<String, Object> properties = (HashMap<String, Object>) params[1];
                if (!TextUtils.isEmpty(eventName) && !properties.isEmpty()) {
                    try {
                        String event = getContentJson(eventName, properties);
                        LogUtils.d("LH/Event", "event:\n" + event);
//                        playerEventList.add(event);
                        // 添加到原先项目日志
                        MessageQueue.addQueue(event);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        private String getContentJson(String eventName, HashMap<String, Object> propertiesMap) throws JSONException {
            JSONObject propertiesJson = new JSONObject();
            propertiesJson.put("time", DateUtils.currentTimeMillis() / 1000);
            if (propertiesMap != null) {
                Set<String> set = propertiesMap.keySet();
                for (String key : set) {
                    propertiesJson.put(key, propertiesMap.get(key));
                }
            }
            JSONObject logJson = new JSONObject();
            logJson.put("event", eventName);
            logJson.put("properties", propertiesJson);
            return logJson.toString();
        }

    }

    // Video event property
    private final static String ITEM = "item";
    private final static String SUBITEM = "subitem";
    private final static String CLIP = "clip";
    private final static String TITLE = "title";
    private final static String QUALITY = "quality";
    private final static String TO = "to";
    private final static String DURATION = "duration";
    private final static String POSITION = "position";
    private final static String SPEED = "speed";
    private final static String SID = "sid";
    private final static String SECTION = "section";
    private final static String CHANNEL = "channel";
    private final static String SOURCE = "source";
    private final static String LOCATION = "location";
    private final static String CODE = "code";
    private final static String CONTENT = "content";
    private final static String MEDIAIP = "mediaip";
    private final static String MODE = "mode";
    private final static String MEDIA_ID = "media_id";
    private final static String MEDIA_URL = "media_url";
    private final static String PLAYER_FLAG = "player";
    private final static String AD_ID = "ad_id";

    /**
     * 播放器打开
     */
    private static final String VIDEO_START = "video_start";
    /**
     * 开始播放缓冲结束
     */
    private static final String VIDEO_PLAY_LOAD = "video_play_load";
    /**
     * 切换码流
     */
    private static final String VIDEO_SWITCH_STREAM = "video_switch_stream";
    /**
     * 开始播放
     */
    private static final String VIDEO_PLAY_START = "video_play_start";
    /**
     * 播放暂停
     */
    private static final String VIDEO_PLAY_PAUSE = "video_play_pause";
    /**
     * 播放继续
     */
    private static final String VIDEO_PLAY_CONTINUE = "video_play_continue";
    /**
     * 播放快进/快退
     */
    private static final String VIDEO_PLAY_SEEK = "video_play_seek";
    /**
     * 播放快进/快退缓冲结束
     */
    private static final String VIDEO_PLAY_SEEK_BLOCKEND = "video_play_seek_blockend";
    /**
     * 播放缓冲结束
     */
    private static final String VIDEO_PLAY_BLOCKEND = "video_play_blockend";
    /**
     * 播放时网速
     */
    private static final String VIDEO_PLAY_SPEED = "video_play_speed";
    /**
     * 播放时下载速度慢
     */
    private static final String VIDEO_LOW_SPEED = "video_low_speed";
    /**
     * 播放器退出
     */
    private static final String VIDEO_EXIT = "video_exit";
    /**
     * 播放器异常
     */
    private static final String VIDEO_EXCEPT = "video_except";
    /**
     * 广告播放缓冲结束
     */
    private static final String AD_PLAY_LOAD = "ad_play_load";
    /**
     * 广告播放卡顿
     */
    private static final String AD_PLAY_BLOCKEND = "ad_play_blockend";
    /**
     * 广告播放结束
     */
    private static final String AD_PLAY_EXIT = "ad_play_exit";
    /**
     * 暂停广告播放
     */
    private static final String PAUSE_AD_PLAY = "pause_ad_play";
    /**
     * 暂停广告下载
     */
    private static final String PAUSE_AD_DOWNLOAD = "pause_ad_download";
    /**
     * 暂停广告异常
     */
    private static final String PAUSE_AD_EXCEPT = "pause_ad_except";

}
