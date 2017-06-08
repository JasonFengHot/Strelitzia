package tv.ismar.account.core.http;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;
import tv.ismar.account.data.ResultEntity;

/**
 * Created by huaijie on 1/14/16.
 */
public interface HttpService {


    @Headers({
            "Pragma: no-cache",
            "Cache-Control: no-cache"
    })
    @FormUrlEncoded
    @POST("/trust/security/active/")
    Call<ResultEntity> trustSecurityActive(
            @Field("sn") String sn,
            @Field("manufacture") String manufacture,
            @Field("kind") String kind,
            @Field("version") String version,
            @Field("sign") String sign,
            @Field("fingerprint") String fingerprint,
            @Field("api_version") String api_version,
            @Field("info") String deviceInfo,
            @Field("wireless_mac") String wifiMac,
            @Field("wired_mac") String wiredMac
    );

    @FormUrlEncoded
    @POST("/trust/get_licence/")
    Call<ResponseBody> trustGetlicence(
            @Field("fingerprint") String fingerprint,
            @Field("sn") String sn,
            @Field("manufacture") String manufacture,
            @Field("code") String code
    );
    @GET
    Call<ResultEntity> weixinIp(
            @Url String url,
            @Query("client_ip") String ip,
            @Query("sn") String sn,
            @Query("tvmode") String tvmode,
            @Query ("macaddress") String macaddress
    );
}
