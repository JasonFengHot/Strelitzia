package tv.ismar.library.injectdb;

import android.util.Log;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.List;

import tv.ismar.library.BuildConfig;
import tv.ismar.library.injectdb.query.Delete;
import tv.ismar.library.injectdb.query.Select;
import tv.ismar.library.injectdb.query.Update;
import tv.ismar.library.util.JacksonUtils;

/**
 * Created by LongHai on 17-4-14.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ActiveAndroidTest {

    private static final String TAG = "LH/ActiveAndroidTest";

    static {
        ShadowLog.stream = System.out; //Android logcat output.
    }

    @Before
    public void setUp() throws Exception {
        Configuration configuration = new Configuration.Builder(RuntimeEnvironment.application)
                .setDatabaseName("active_android_test.db")
                .setDatabaseVersion(1)
                .create();
        ActiveAndroid.initialize(configuration);
        Log.i(TAG, "initialize");
    }

    @After
    public void tearDown() {
        ActiveAndroid.dispose();
        Log.i(TAG, "dispose");
    }

    @Test
    public void testActiveAndroid() throws Exception {
        // 当条数据
        AccountDao accountDao = new AccountDao();
        accountDao.username = "zhangsan";
        accountDao.password = "123456";
        accountDao.save();
        Log.i(TAG, "------------------saved--------------------");

        // 多条数据插入
        ActiveAndroid.beginTransaction();
        try {
            for (int i = 0; i < 10; i++) {
                AccountDao account = new AccountDao();
                account.username = "Example " + i;
                account.password = "pwd" + i;
                account.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }

        // 查询
        List<AccountDao> selectDao = new Select().from(AccountDao.class)
                .execute();
        Log.i(TAG, "Query:\n" + JacksonUtils.toJson(selectDao));
        AccountDao selectSingle = new Select().from(AccountDao.class)
                .where("username=?", "zhangsan")
                .executeSingle();
        Assert.assertNotNull(selectSingle);
        Log.i(TAG, "QuerySingle:\n" + selectSingle.password);

        new Delete().from(AccountDao.class)
                .where("username=?", "Example 4")
                .execute();
        List<AccountDao> deleteDao = new Select().from(AccountDao.class)
                .execute();
        Log.i(TAG, "DeleteQuery:\n" + JacksonUtils.toJson(deleteDao));

        new Update(AccountDao.class)
                .set("username='Example LongHai'").where("username='Example 5'").execute();
        List<AccountDao> updateDao = new Select().from(AccountDao.class)
                .execute();
        Log.i(TAG, "UpdateQuery:\n" + JacksonUtils.toJson(updateDao));

        selectSingle.password = "pwd zhangsan";
        selectSingle.save();
        List<AccountDao> saveSingleDao = new Select().from(AccountDao.class)
                .execute();
        Log.i(TAG, "SaveSingleQuery:\n" + JacksonUtils.toJson(saveSingleDao));
        selectSingle.delete();
        List<AccountDao> deleteSingleDao = new Select().from(AccountDao.class)
                .execute();
        Log.i(TAG, "DeleteSingleDao:\n" + JacksonUtils.toJson(deleteSingleDao));

    }

}