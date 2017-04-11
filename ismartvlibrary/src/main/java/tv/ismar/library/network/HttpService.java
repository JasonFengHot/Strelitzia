package tv.ismar.library.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by LongHai on 17-4-10.
 */

public interface HttpService {

    @GET("api/channels")
    Call<ResponseBody> getChannel();

}
