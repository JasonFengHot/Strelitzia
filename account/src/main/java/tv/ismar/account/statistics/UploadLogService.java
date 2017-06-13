package tv.ismar.account.statistics;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by huibin on 6/12/17.
 */

public interface UploadLogService {
    @Multipart
    @POST
    Observable<ResponseBody> uploadLog(
            @Url String url,
            @Part("parameters") RequestBody parameters,
            @Part("data") RequestBody data
    );
}
