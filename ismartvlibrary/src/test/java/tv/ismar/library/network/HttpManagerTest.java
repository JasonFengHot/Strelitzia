package tv.ismar.library.network;

import junit.framework.Assert;

import org.junit.Test;

import tv.ismar.library.injectdb.util.Log;

/**
 * Created by LongHai on 17-4-11.
 */
public class HttpManagerTest {

    private static final String TAG = "LH/HttpManagerTest";

    @Test
    public void getDomainService() throws Exception {
        Log.i(TAG, "testGetDomainService");
        SkyServiceTest skyServiceTest = HttpManager.getDomainService(SkyServiceTest.class);
        Assert.assertNotNull(skyServiceTest);

    }

    @Test
    public void asyncGetRequest() throws Exception {
        Assert.assertEquals(2 + 2, 4);
    }

}