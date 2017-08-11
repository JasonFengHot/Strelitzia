package tv.ismar.library.statistics;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

/**
 * Created by huibin on 6/12/17.
 */

public interface UploadLogService {
    @Multipart
    @POST("Elderberry/client/uploadLog")
    Observable<ResponseBody> uploadLog(
            @Part("parameters") RequestBody parameters,
            @Part("data") RequestBody data
    );
}
