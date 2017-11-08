package tv.ismar.app.network;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.account.IsmartvDns;
import tv.ismar.account.IsmartvHttpLoggingInterceptor;
import tv.ismar.app.VodApplication;
import tv.ismar.app.core.OfflineCheckManager;
import tv.ismar.app.entity.ChannelEntity;
import tv.ismar.app.entity.ClipEntity;
import tv.ismar.app.entity.FilterNoresultPoster;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.app.entity.HomePagerEntity;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemList;
import tv.ismar.app.entity.ListSectionEntity;
import tv.ismar.app.entity.SectionList;
import tv.ismar.app.entity.Subject;
import tv.ismar.app.entity.VideoEntity;
import tv.ismar.app.entity.banner.AccountsItemSubscribeExistsEntity;
import tv.ismar.app.entity.banner.BannerEntity;
import tv.ismar.app.entity.banner.BannerRecommend;
import tv.ismar.app.entity.banner.ConnerEntity;
import tv.ismar.app.entity.banner.HomeEntity;
import tv.ismar.app.models.ActorRelateRequestParams;
import tv.ismar.app.models.FilterConditions;
import tv.ismar.app.models.Game;
import tv.ismar.app.models.HotWords;
import tv.ismar.app.models.PersonEntitiy;
import tv.ismar.app.models.PlayRecommend;
import tv.ismar.app.models.Recommend;
import tv.ismar.app.models.SemanticSearchResponseEntity;
import tv.ismar.app.models.Sport;
import tv.ismar.app.models.SubjectEntity;
import tv.ismar.app.models.VodFacetEntity;
import tv.ismar.app.models.VodSearchRequestEntity;
import tv.ismar.app.network.entity.AccountBalanceEntity;
import tv.ismar.app.network.entity.AccountPlayAuthEntity;
import tv.ismar.app.network.entity.AccountsLoginEntity;
import tv.ismar.app.network.entity.AccountsOrdersEntity;
import tv.ismar.app.network.entity.ActiveEntity;
import tv.ismar.app.network.entity.AgreementEntity;
import tv.ismar.app.network.entity.BannerIconMarkEntity;
import tv.ismar.app.network.entity.BindedCdnEntity;
import tv.ismar.app.network.entity.ChatMsgEntity;
import tv.ismar.app.network.entity.DpiEntity;
import tv.ismar.app.network.entity.Empty;
import tv.ismar.app.network.entity.ExplainEntity;
import tv.ismar.app.network.entity.GoodsRenewStatusEntity;
import tv.ismar.app.network.entity.IpLookUpEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.OpenRenewEntity;
import tv.ismar.app.network.entity.PayLayerEntity;
import tv.ismar.app.network.entity.PayLayerPackageEntity;
import tv.ismar.app.network.entity.PayLayerVipEntity;
import tv.ismar.app.network.entity.PayVerifyEntity;
import tv.ismar.app.network.entity.PayWhStatusEntity;
import tv.ismar.app.network.entity.ProblemEntity;
import tv.ismar.app.network.entity.QiyiCheckEntity;
import tv.ismar.app.network.entity.SubjectPayLayerEntity;
import tv.ismar.app.network.entity.TeleEntity;
import tv.ismar.app.network.entity.UpgradeRequestEntity;
import tv.ismar.app.network.entity.VersionInfoV2Entity;
import tv.ismar.app.network.entity.WeatherEntity;
import tv.ismar.app.network.entity.YouHuiDingGouEntity;
import tv.ismar.library.exception.ExceptionUtils;
import tv.ismar.library.network.UserAgentInterceptor;

/**
 * Created by huibin on 8/3/16.
 */
public interface SkyService {

    @FormUrlEncoded
    @POST("accounts/orders/")
    Observable<AccountsOrdersEntity> accountsOrders(
            @Field("timestamp") String timestamp,
            @Field("sign") String sign
    );

    @FormUrlEncoded
    @POST("accounts/playauths/")
    Observable<AccountPlayAuthEntity> accountsPlayauths(
            @Field("timestamp") String timestamp,
            @Field("sign") String sign
    );

    @GET("api/tv/section/youhuidinggou/")
    Observable<YouHuiDingGouEntity> apiYouhuidinggou(

    );

    @GET
    Observable<ItemEntity> apiItemByUrl(
            @Url String urlP
    );

    @GET
    Observable<ListSectionEntity> getListChannel(
            @Url String url
    );

    @GET
    Observable<ItemList> getItemListChannel(
            @Url String url
    );

    @GET("api/item/{pk}/")
    Observable<ItemEntity> apiItem(
            @Path("pk") String pk
    );

    @GET("api/item/{pk}/")
    Observable<OfflineCheckManager.OfflineCheckEntity> apiItemIsOffline(
            @Path("pk") String pk
    );

    @GET("api/package/{pk}/")
    Observable<OfflineCheckManager.OfflineCheckEntity> apiPKGIsOffline(
            @Path("pk") String pk
    );

    @GET("api/tv/{section}/{channel}")
    Observable<SectionList> getSectionlist(
            @Path("channel") String channel,
            @Path("section") String Section
    );

    @GET("api/tv/retrieval/{channel}")
    Observable<FilterConditions> getFilters(
            @Path("channel") String channel
    );

    @GET("api/tv/filtrate/${content_model}/{filterCondition}/1/")
    Observable<ItemList> getFilterRequest(
            @Path("content_model") String channel,
            @Path("filterCondition") String filterCondition
    );

    @GET("api/tv/filtrate/${movie}/{area}/{page}/")
    Observable<ItemList> getFilterRequestNodata(
            @Path("movie") String movie,
            @Path("area") String area,
            @Path("page") int page
    );

    @GET("http://sky.test.tvxio.com/wheat/v3_0/UF30/toq0/api/top/{channel}/")
    Observable<List<FilterNoresultPoster>> getFilterRecommend(
       @Path("channel") String channel,
       @Query("sn") String sn,
       @Query("count") int count
    );

    @GET
    Observable<List<BannerRecommend>> getHomeRecommend(
            @Url String url
            );

    @GET("api/tv/filtrate/{movie}/{genre}/{page}/")
    Observable<ResponseBody> fetchFiltrate(
            @Path("movie") String movie,
            @Path("genre") String genre,
            @Path("page") String page

    );

    @GET("api/tv/filtrate/${content_model}/{filterCondition}/{page}/")
    Observable<ItemList> getFilterRequestHaveData(
            @Path("content_model") String channel,
            @Path("filterCondition") String filterCondition,
            @Path("page") int page
    );

    @GET("api/tv/sections/{channel}/")
    Observable<SectionList> getSections(
            @Path("channel") String channel
    );

    @GET("api/v3/histories/")
    Observable<Item[]> getHistoryByNet(
    );
    @GET("api/v3/histories/")
    Observable<ResponseBody> getHistoryByNetV3(
    );

    @FormUrlEncoded
    @POST("api/histories/empty/")
    Observable<ResponseBody> emptyHistory(
            @Field("token") String token
    );

    @GET("api/bookmarks/")
    Observable<Item[]> getBookmarks(
    );

    @FormUrlEncoded
    @POST("api/bookmark/check/")
    Observable<Integer> checkBookmark(
            @Field("device_token") String device_token,
            @Field("access_token") String access_token,
            @Field("item") int pk
    );

    @GET("api/v3/bookmarks/")
    Observable<ResponseBody> getBookmarksV3(
    );

    @FormUrlEncoded
    @POST("api/bookmarks/empty/")
    Observable<ResponseBody> emptyBookmarks(
            @Field("token") String token
    );

    @GET("api/tv/relate/{pk}/")
    Observable<Item[]> getRelatedArray(
            @Path("pk") Integer pk
    );

    @GET("api/tv/filtrate/${content_model}/{slug}*{template}/")
    Observable<ItemList> getRelatedItemByInfo(
            @Path("content_model") String content_model,
            @Path("slug") String slug,
            @Path("template") int template
    );

    @GET("api/tv/section/tvhome/")
    Observable<VideoEntity> getTvHome(
    );

    @GET("api/item/{pk}")
    Observable<Item> getClickItem(
            @Path("pk") int pk
    );

    @GET("api/subitem/{pk}/")
    Observable<ItemEntity> apiSubItem(
            @Path("pk") int pk
    );

    @GET("api/{opt}/{pk}/")
    Observable<ItemEntity> apiOptItem(
            @Path("pk") String pk,
            @Path("opt") String opt

    );


    @FormUrlEncoded
    @POST("api/bookmarks/create/")
    Observable<ResponseBody> apiBookmarksCreate(
            @Field("item") String item
    );

    @FormUrlEncoded
    @POST("api/bookmarks/remove/")
    Observable<ResponseBody> apiBookmarksRemove(
            @Field("item") String item
    );
    @FormUrlEncoded
    @POST("api/histories/remove/")
    Observable<ResponseBody> apiHistoryRemove(
            @Field("item") int item,
            @Field("subitem") int subitem
    );

    @GET("api/tv/relate/{pk}/")
    Observable<ItemEntity[]> apiTvRelate(
            @Path("pk") String item
    );

    @FormUrlEncoded
    @POST("api/play/check/")
    Observable<ResponseBody> apiPlayCheck(
            @Field("item") String item,
            @Field("package") String pkg,
            @Field("subitem") String subItem
    );

    @FormUrlEncoded
    @POST("api/check/")
    Observable<QiyiCheckEntity> apiQiyiCheck(
            @Field("verify_code") String qiyiCode
    );

    @FormUrlEncoded
    @POST("trust/security/active/")
    Observable<ActiveEntity> securityActive(
            @Field("sn") String sn,
            @Field("manufacture") String manufacture,
            @Field("kind") String kind,
            @Field("version") String version,
            @Field("sign") String sign,
            @Field("fingerprint") String fingerprint,
            @Field("api_version") String api_version,
            @Field("info") String deviceInfo
    );

    @FormUrlEncoded
    @POST("trust/get_licence/")
    Observable<ResponseBody> getLicence(
            @Field("fingerprint") String fingerprint,
            @Field("sn") String sn,
            @Field("manufacture") String manufacture,
            @Field("code") String code
    );

    @GET("api/dpi/")
    Observable<List<DpiEntity>> fetchDpi(

    );

    @GET
    Observable<ClipEntity> fetchMediaUrl(
            @Url String clipUrl,
            @Query("sign") String sign,
            @Query("code") String code
    );

    @FormUrlEncoded
    @POST("api/get/ad/")
    Observable<ResponseBody> fetchAdvertisement(
            @FieldMap HashMap<String, Object> paramsMap
    );

    @FormUrlEncoded
    @POST
    Observable<ResponseBody> sendPlayerLog(
            @Url String logUrl,
            @Field("sn") String sn,
            @Field("modelname") String modelname,
            @Field("data") String data
    );

    @FormUrlEncoded
    @POST("api/histories/create/")
    Call<ResponseBody> sendPlayHistory(
            @FieldMap HashMap<String, Object> paramsMap
    );


    @FormUrlEncoded
    @POST("accounts/login/")
    Observable<AccountsLoginEntity> accountsLogin(
            @Field("username") String userName,
            @Field("auth_number") String authNumber
    );

    @FormUrlEncoded
    @POST("accounts/auth/")
    Observable<ResponseBody> accountsAuth(
            @Field("username") String userName
    );

    @GET("accounts/balance/")
    Observable<AccountBalanceEntity> accountsBalance(
    );


    @GET("api/histories/")
    Observable<ResponseBody> apiHistories(
    );

    @GET("api/paylayer/{item_id}/")
    Observable<PayLayerEntity> apiPaylayer(
            @Path("item_id") String itemId
    );

    @GET("api/paylayer/vip/{cpid}/")
    Observable<PayLayerVipEntity> apiPaylayerVip(
            @Path("cpid") String cpid,
            @Query("item_id") String itemId
    );

    @GET("api/paylayer/package/{package_id}/")
    Observable<PayLayerPackageEntity> apiPaylayerPackage(
            @Path("package_id") String packageId
    );

    @POST("accounts/combine/")
    Observable<ResponseBody> accountsCombine(
            @Field("sharp_bestv") String sharpBestv,
            @Field("timestamp") String timestamp,
            @Field("sign") String sign
    );

    @FormUrlEncoded
    @POST("customer/pointlogs/")
    Observable<ResponseBody> UploadFeedback(
            @Header("User-Agent") String userAgent,
            @Field("q") String q
    );

    @FormUrlEncoded
    @POST("api/play/check/")
    Call<ResponseBody> playcheck(
            @Field("item") String item,
            @Field("package") String pkg,
            @Field("subitem") String subItem
    );


    @FormUrlEncoded
    @POST("api/order/purchase/")
    Call<ResponseBody> orderpurchase(
            @Field("item") String item,
            @Field("package") String pkg,
            @Field("subitem") String subItem
    );

    @FormUrlEncoded
    @POST("api/order/{type}/")
    Observable<ResponseBody> apiOrderCreate(
            @Path("type") String type,
            @Field("event_id") String eventId,
            @Field("wares_id") String waresId,
            @Field("wares_type") String waresType,
            @Field("source") String source,
            @Field("timestamp") String timestamp,
            @Field("sign") String sign,
            @Field("action") String action
    );


    @FormUrlEncoded
    @POST("https://order.tvxio.com/api/pay/verify/")
    Observable<PayVerifyEntity> apiPayVerify(
            @Field("card_secret") String card_secret,
            @Field("app_name") String app_name,
            @Field("user") String user,
            @Field("user_id") String user_id,
            @Field("timestamp") String timestamp,
            @Field("sid") String sid
    );

    @FormUrlEncoded
    @POST("api/play/check/")
    Observable<ResponseBody> videoPlayCheck(
            @Field("item") String item
    );

    @FormUrlEncoded
    @POST("api/play/check/")
    Call<ResponseBody> playCheck(
            @Field("item") String item,
            @Field("package") String pkg,
            @Field("subitem") String subItem
    );

    @POST("api/v2/upgrade/")
    Observable<VersionInfoV2Entity> appUpgrade(
            @Body List<UpgradeRequestEntity> upgradeRequestEntities
    );

    @GET
    @Streaming
    Observable<ResponseBody> download(
            @Url String url,
            @Header("RANGE") String range
    );

    @GET
    @Streaming
    Observable<ResponseBody> image(
            @Url String url
    );

    @GET
    Observable<ResponseBody> openRenew(
            @Url String url
    );

    @GET("{geoId}.xml")
    Observable<WeatherEntity> apifetchWeatherInfo(
            @Path("geoId") String geoId
    );

    @GET
    Observable<Item> apifetchItem(
            @Url String url
    );

    @GET
    Observable<ResponseBody> apiCheckItem(
            @Url String url
    );


    @GET
    Observable<HomePagerEntity> fetchHomePage(
            @Url String url
    );

    @GET
    Observable<ItemList> getPackageList(
            @Url String url
    );

    @GET
    Observable<ItemList> getPackageListItem(
            @Url String url
    );

    @GET("api/tv/homepage/top/")
    Observable<HomePagerEntity> TvHomepageTop();

    @GET("api/tv/living_video/sport/")
    Observable<Sport> apiSport();

    @GET("api/tv/living_video/game/")
    Observable<Game> apiGame();

    @GET("api/tv/channels/")
    Observable<ChannelEntity[]> apiTvChannels();

    @GET("api/tv/homepage/banner/")
    Observable<GuideBanner[]> getGuideBanners(
    );

    @GET("api/new_dpi/")
    Observable<List<ConnerEntity>> getConner();

    @GET("api/tv/banners/{channel}/")
    Observable<GuideBanner[]> getChannelBanners(
            @Path("channel") String channel
    );

    @GET("api/tv/banner/{banner}/{page}/")
    Observable<HomeEntity> getBanners(
            @Path("banner") String banner,
            @Path("page") int page
    );

	/*modify by dragontec for bug 4362 start*/
    @GET("api/tv/mbanner/{banner}/{page}/")
    Observable<HomeEntity[]> getMBanners(
            @Path("banner") String banner,
            @Path("page") int page
    );
	/*modify by dragontec for bug 4362 end*/

    @GET
    Observable<ArrayList<HomePagerEntity.Poster>> smartRecommendPost(
            @Url String url,
            @Query("sn") String snCode
    );

    @GET("api/tv/hotwords/")
    Observable<ArrayList<HotWords>> apiSearchHotwords();

    @GET("api/tv/homepage/sharphotwords/8/")
    Observable<Recommend> apiSearchRecommend();

    @GET("api/tv/suggest/{word}/?device_token==&access_token=/")
    Observable<List<String>> apiSearchSuggest(
            @Path("word") String word
    );

    @POST("api/tv/vodsearch/")
    Observable<VodFacetEntity> apiSearchResult(
            @Body VodSearchRequestEntity requestEntity
    );

    @GET("api/package/relate/{pkg}/")
    Observable<ItemEntity[]> packageRelate(
            @Path("pkg") long pk);


    @GET("api/person/{id}/")
    Observable<PersonEntitiy> apiFetchPersonBG(
            @Path("id") String id
    );

    @POST("api/tv/actorrelate/")
    Observable<SemanticSearchResponseEntity> apiFetchActorRelate(
            @Body ActorRelateRequestParams params
    );

    @GET("customer/points/")
    Observable<List<ProblemEntity>> Problems();


    @GET("customer/getfeedback/")
    Observable<ChatMsgEntity> Feedback(
            @Query("sn") String sn,
            @Query("topn") String topn
    );


    @GET("shipinkefu/getCdninfo?actiontype=getBindcdn")
    Observable<BindedCdnEntity> GetBindCdn(
            @Query("sn") String snCode
    );

    @GET("shipinkefu/getCdninfo?actiontype=bindecdn")
    Observable<Empty> BindCdn(
            @Query("sn") String snCode,
            @Query("cdn") int cdnId
    );

    @GET("shipinkefu/getCdninfo?actiontype=unbindCdn")
    Observable<Empty> UnbindNode(
            @Query("sn") String sn

    );

    @GET("shipinkefu/getCdninfo")
    Observable<List<TeleEntity>> FetchTel(
            @Query("actiontype") String actiontype,
            @Query("ModeName") String modeName,
            @Query("sn") String sn
    );


    @GET("log")
    Observable<Empty> DeviceLog(
            @Query("data") String data,
            @Query("sn") String sn,
            @Query("modelname") String modelName
    );

    @FormUrlEncoded
    @POST("shipinkefu/getCdninfo")
    Observable<Empty> UploadResult(
            @Field("actiontype") String actionType,
            @Field("snCode") String snCode,
            @Field("nodeId") String nodeId,
            @Field("nodeSpeed") String nodeSpeed
    );


    @GET
    Observable<IpLookUpEntity> fetchIP(
            @Url String url
    );


    @GET("accounts/pay_wh_status/")
    Observable<PayWhStatusEntity> accountsPayWhStatus(
            @Query("package_id") int packageId
    );

    @GET("accounts/goods_renew_status/")
    Observable<GoodsRenewStatusEntity> accountsGoodsRenewStatus(
            @Query("package_id") int packageId
    );


    @FormUrlEncoded
    @POST("accounts/open_renew/")
    Observable<OpenRenewEntity> accountsOpenRenew(
            @Field("pay_type") String payType,
            @Field("package_id") int packageId
    );

    @GET("api/agreement/")
    Observable<AgreementEntity> agreement(
            @Query("source") String source
    );
    @GET("api/renew/explain")
    Observable<ExplainEntity> explain(
    );

    @GET("api/item/{pk}/")
    Observable<Subject> getSportSubjectInfo(
            @Path("pk") int pk
    );

    @GET("api/item/{pk}/")
    Observable<SubjectEntity> apiSubject(
            @Path("pk") String pk
    );

    @GET("api/paylayer/{item_id}/")
    Observable<SubjectPayLayerEntity> apiPaylayerVipSubject(
            @Path("item_id") String item_id
    );

    @GET("api/tv/banner/{banner_name}/{page}/")
    Observable<BannerEntity> apiTvBanner(
            @Path("banner_name") String banner,
            @Path("page") int page
    );


    @GET("accounts/sports/adapter/")
    Observable<ResponseBody> getSubscribeImage(
            @Query("item_id") int pk,
            @Query("type") String type
    );

    @GET
    Observable<ResponseBody> repostAdLog(
            @Url String url
    );

    @GET
    Observable<ResponseBody> weixinIp(
            @Url String url,
            @Query("client_ip") String ip,
            @Query("sn") String sn,
            @Query("tvmode") String tvmode,
            @Query("macaddress") String macaddress
    );
    @GET
    Observable<ResponseBody>payChannel(
            @Url String url
    );

    @GET("wheat/api/recommend/exits_play/")
    Observable<PlayRecommend> apiPlayExitRecommend(
            @Query("sn") String sn,
            @Query("item_id") int item_id,
            @Query("channel") String channel,
            @Query("play_scale") int play_scale
    );

    @GET("accounts/item/subscribe/")
    Observable<ResponseBody> accountsItemSubscribe(
            @Query("item_id") int itemId,
            @Query("content_model") String contentModel
    );

    @GET("accounts/item/subscribe/exists/")
    Observable<AccountsItemSubscribeExistsEntity> accountsItemSubscribeExists(
            @Query("item_id") int itemId
    );

    @GET("api/new_dpi/")
    Observable<List<BannerIconMarkEntity>> apiNewDpi(

    );

    class ServiceManager {
        private volatile static ServiceManager serviceManager;
        private static final int DEFAULT_CONNECT_TIMEOUT = 6;
        private static final int DEFAULT_READ_TIMEOUT = 15;

        private static final String API_HOST = "http://wx.api.tvxio.com/";
        private static final String IRIS_TVXIO_HOST = "http://iris.tvxio.com/";
        private static final String SPEED_CALLA_TVXIO_HOST = "http://speed.calla.tvxio.com/";
        private static final String LILY_TVXIO_HOST = "http://lily.tvxio.com/";

        private final SkyService mCarnationService;
        private static String[] domain = new String[]{
                "1.1.1.1",  //sky域名
                "1.1.1.2",  //广告域名
                "1.1.1.3",  //更新域名
                "1.1.1.4",  //日志域名
                "1.1.1.5",  //未知
                "1.1.1.6"   //爱奇艺购买域名
        };
        private final SkyService mSkyService;
        private final SkyService mForceCacheSkyService;
        private final SkyService adSkyService;
        private final SkyService upgradeService;
        private final SkyService weatherService;
        private final SkyService wxApiService;
        private final SkyService irisService;
        private final SkyService speedCallaService;

        public static boolean executeActive = true;
        private final SkyService lilyHostService;

        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i("TrustManager", "checkClientTrusted");
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i("TrustManager", "checkServerTrusted");
            }
        }};

        private ServiceManager() {
            IsmartvHttpLoggingInterceptor interceptor = new IsmartvHttpLoggingInterceptor();
            interceptor.setLevel(IsmartvHttpLoggingInterceptor.Level.BODY);

            SSLContext sc = null;
            try {
                sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
            } catch (NoSuchAlgorithmException e) {
                ExceptionUtils.sendProgramError(e);
                e.printStackTrace();
            } catch (KeyManagementException e) {
                ExceptionUtils.sendProgramError(e);
                e.printStackTrace();
            }

            File cacheFile = new File(VodApplication.getModuleAppContext().getCacheDir(), "okhttp_cache");
            Cache cache = new Cache(cacheFile, 1024 * 1024 * 100); //100Mb
            final OkHttpClient mClient = new OkHttpClient.Builder()
                    .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
//                    .addInterceptor(new IsmartvHttpCheckInterceptor())
                    .addInterceptor(VodApplication.getHttpParamsInterceptor())
//                    .addInterceptor(VodApplication.getModuleAppContext().getCacheInterceptor())
                    .addInterceptor(interceptor)
                    .addInterceptor(new UserAgentInterceptor())
                    .dns(new IsmartvDns())
                    .cache(cache)
                    .sslSocketFactory(sc.getSocketFactory())
                    .build();

            final OkHttpClient mForceClient = new OkHttpClient.Builder()
                    .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
//                    .addInterceptor(VodApplication.getHttpParamsInterceptor())
                    .addInterceptor(new HttpForceCacheInterceptor())
                    .cache(cache)
                    .build();

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd hh:mm:ss")
                    .create();

            /**
             * ==================================主域名======================================
             */
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(appendProtocol(domain[0]))
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mClient)
                    .build();
            mSkyService = retrofit.create(SkyService.class);

            Retrofit forceCacheRetrofit = new Retrofit.Builder()
                    .baseUrl(appendProtocol(domain[0]))
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mForceClient)
                    .build();
            mForceCacheSkyService = forceCacheRetrofit.create(SkyService.class);
            //=============================================================================


            /**
             * ==================================广告域名======================================
             */
            Retrofit adRetrofit = new Retrofit.Builder()
                    .baseUrl(appendProtocol(domain[1]))
                    //                    .baseUrl("http://103.254.66.8:8082/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mClient)
                    .build();
            adSkyService = adRetrofit.create(SkyService.class);
            //=============================================================================

            /**
             * ==================================应用升级域名======================================
             */
            Retrofit upgradeRetrofit = new Retrofit.Builder()
                    .baseUrl(appendProtocol(domain[2]))
                    //               .baseUrl(appendProtocol("http://124.42.65.66/"))
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mClient)
                    .build();
            upgradeService = upgradeRetrofit.create(SkyService.class);
            //=============================================================================

            /**
             * ==================================天气预报域名======================================
             */

            Retrofit weatherRetrofit = new Retrofit.Builder()
                    .baseUrl(appendProtocol("http://media.lily.tvxio.com/"))
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mClient)
                    .build();
            weatherService = weatherRetrofit.create(SkyService.class);
            //=============================================================================

            /**
             * ==================================微信域名======================================
             */
            Retrofit wxApiServiceRetrofit = new Retrofit.Builder()
                    .baseUrl(API_HOST)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mClient)
                    .build();
            wxApiService = wxApiServiceRetrofit.create(SkyService.class);
            //=============================================================================

            /**
             * ==================================视云客服域名======================================
             */
            Retrofit irisServiceRetrofit = new Retrofit.Builder()
                    .baseUrl(IRIS_TVXIO_HOST)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mClient)
                    .build();
            irisService = irisServiceRetrofit.create(SkyService.class);
            //=============================================================================

            /**
             * ==================================视云客服日志上报域名======================================
             */
            Retrofit speedCallaServiceRetrofit = new Retrofit.Builder()
                    .baseUrl(SPEED_CALLA_TVXIO_HOST)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mClient)
                    .build();
            speedCallaService = speedCallaServiceRetrofit.create(SkyService.class);
            //=============================================================================

            /**
             * ==================================GEO_ID域名======================================
             */
            Retrofit lilyHostServiceRetrofit = new Retrofit.Builder()
                    .baseUrl(LILY_TVXIO_HOST)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mClient)
                    .build();
            lilyHostService = lilyHostServiceRetrofit.create(SkyService.class);
            //=============================================================================

            /**
             * ==================================爱奇艺购买域名======================================
             */
            Retrofit carnationRetrofit = new Retrofit.Builder()
                    .baseUrl(appendProtocol(domain[5]))
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mClient)
                    .build();
            mCarnationService = carnationRetrofit.create(SkyService.class);
            //=============================================================================
        }


        private String appendProtocol(String host) {
            Uri uri = Uri.parse(host);
            String url = uri.toString();
            if (!uri.toString().startsWith("http://") && !uri.toString().startsWith("https://")) {
                url = "http://" + host;
            }

            if (!url.endsWith("/")) {
                url = url + "/";
            }
            return url;
        }

        private static ServiceManager getInstance() {    //对获取实例的方法进行同步
            synchronized (ServiceManager.class) {
                if (serviceManager == null || domain[0].endsWith("1.1.1.1")) {
                    serviceManager = new ServiceManager();
                }
            }
            return serviceManager;
        }

        public static SkyService getService() {

            return getInstance().mSkyService;
        }

        public static SkyService getForceCacheService() {

            return getInstance().mForceCacheSkyService;
        }

        public static SkyService getAdService() {

            return getInstance().adSkyService;
        }

        public static SkyService getUpgradeService() {

            return getInstance().upgradeService;
        }

        public static SkyService getWeatherService() {

            return getInstance().weatherService;
        }

        public static SkyService getWxApiService() {

            return getInstance().wxApiService;
        }

        public static SkyService getIrisService() {

            return getInstance().irisService;
        }

        public static SkyService getSpeedCallaService() {

            return getInstance().speedCallaService;
        }

        public static SkyService getLilyHostService() {

            return getInstance().lilyHostService;
        }

        public static SkyService getCarnationService() {
            return getInstance().mCarnationService;
        }
    }
}
