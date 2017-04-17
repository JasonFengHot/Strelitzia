package tv.ismar.library.network;

import android.util.Log;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.IOException;
import java.net.URISyntaxException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import tv.ismar.library.BuildConfig;

/**
 * Created by LongHai on 17-4-11.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class HttpManagerTest {

    private static final String TAG = "LH/HttpManagerTest";
    private static final String domain = "http://sky.tvxio.com/v3_0/SKY2/tou";
    private static final String upgrade_domain = "";
    private static final String deviceToken = "__Ntksg9LjmpHH4Bx6wkjNKk8v6zzhQYu-erQaGzc7D0lUKTjwbH8GimsLJuRLEhaP";

    static {
        HttpManager.getInstance().init(domain, upgrade_domain, deviceToken);
    }

    @Before
    public void setUp() throws URISyntaxException {
        //输出日志
        ShadowLog.stream = System.out;
    }

    @Test
    public void testGetDomainService() throws Exception {
        SkyServiceTest skyServiceTest = HttpManager.getDomainService(SkyServiceTest.class);
        Assert.assertNotNull(skyServiceTest);
        Call<ResponseBody> call = skyServiceTest.apiUrlTest();
        Response<ResponseBody> response = call.execute();
        try {
            Log.d(TAG, "result : " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSyncGetRequest() throws Exception {
        String result = HttpManager.syncGetRequest(domain + "/api/tv/channels/");
        Log.d(TAG, "result : " + result);

    }

}