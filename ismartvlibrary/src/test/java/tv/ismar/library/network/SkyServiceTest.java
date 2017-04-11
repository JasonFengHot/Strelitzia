package tv.ismar.library.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by LongHai on 17-4-11.
 */

public interface SkyServiceTest {

    @GET("api/tv/channels/")
    Call<ResponseBody> apiUrlTest();

}
