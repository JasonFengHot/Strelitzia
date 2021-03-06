package tv.ismar.app.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.query.Select;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.R;
import tv.ismar.app.db.location.CityTable;
import tv.ismar.app.db.location.ProvinceTable;
import tv.ismar.app.network.entity.IpLookUpEntity;
import tv.ismar.app.util.NetworkUtils;
import tv.ismar.app.util.Utils;
import tv.ismar.library.network.UserAgentInterceptor;

public class InitializeProcess implements Runnable {
    private static final String TAG = "InitializeProcess";

    private static final int[] PROVINCE_STRING_ARRAY_RES = {
            R.array.china_north,
            R.array.china_east,
            R.array.china_south,
            R.array.china_center,
            R.array.china_southwest,
            R.array.china_northwest,
            R.array.china_northeast
    };

    public static final String PROVINCE = "province";
    public static final String CITY = "city";
    public static final String PROVINCE_PY = "province_py";
    public static final String ISP = "isp";
    public static final String IP = "ip";
    public static final String GEO_ID = "geo_id";

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private OkHttpClient mOkHttpClient;
    private final String[] mDistrictArray;
    public static boolean flag = false;

    Interceptor mHeaderInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request authorised = originalRequest.newBuilder()
                    .addHeader("Accept", "application/json")
                    .build();
            return chain.proceed(authorised);
        }
    };

    public InitializeProcess(Context context) {
        this.mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mDistrictArray = mContext.getResources().getStringArray(R.array.district);
        mOkHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(3000, TimeUnit.MILLISECONDS)
                .addNetworkInterceptor(mHeaderInterceptor)
                .addInterceptor(new UserAgentInterceptor())
                .build();
        flag = true;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initializeProvince();
        initalizeCity();
        String city = mSharedPreferences.getString(CITY, "");
        if (Utils.isEmptyText(city)) {
            fetchLocationByIP();
        }

    }

    private void initializeProvince() {
        if (new Select().from(ProvinceTable.class).executeSingle() == null) {

            ActiveAndroid.beginTransaction();
            try {
                for (int i = 0; i < mDistrictArray.length; i++) {
                    String[] provinceArray = mContext.getResources().getStringArray(PROVINCE_STRING_ARRAY_RES[i]);
                    for (String province : provinceArray) {
                        ProvinceTable provinceTable = new ProvinceTable();
                        String[] strs = province.split(",");
                        String provinceName = strs[0];
                        String provincePinYin = strs[1];

                        provinceTable.province_name = provinceName;
                        provinceTable.pinyin = provincePinYin;

                        provinceTable.province_id = Utils.getMd5Code(provinceName);
                        provinceTable.district_id = Utils.getMd5Code(mDistrictArray[i]);
                        provinceTable.save();
                    }
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    private void initalizeCity() {
        if (new Select().from(CityTable.class).executeSingle() == null) {
            ActiveAndroid.beginTransaction();
            try {
                InputStream inputStream = mContext.getResources().getAssets().open("location.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    if (null != s && !s.equals("")) {
                        String[] strings = s.split("\\,");
                        Long geoId = Long.parseLong(strings[0]);
                        String area = strings[1];
                        String city = strings[2];
                        String province = strings[3];
                        String provinceId = Utils.getMd5Code(province);

                        if (area.equals(city)) {
                            CityTable cityTable = new CityTable();
                            cityTable.geo_id = geoId;
                            cityTable.province_id = provinceId;
                            cityTable.city = city;
                            cityTable.save();
                        }
                    }
                }
                ActiveAndroid.setTransactionSuccessful();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    private void fetchLocationByIP() {
        if(!NetworkUtils.isConnected(mContext)){// ????????????????????????????????????
            return;
        }
        String resultString = null;
        Request request = new Request.Builder()
                .url("http://lily.tvxio.com/iplookup/")
                .build();
        Call call = mOkHttpClient.newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                resultString = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!Utils.isEmptyText(resultString)) {
		//modify by dragontec ?????????????????????crash
            try {
                IpLookUpEntity ipLookUpEntity = new GsonBuilder().create().fromJson(resultString, IpLookUpEntity.class);
                initializeLocation(ipLookUpEntity);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private void initializeLocation(IpLookUpEntity ipLookUpEntity) {
        CityTable cityTable = new Select().from(CityTable.class).where(CityTable.CITY + " = ?", ipLookUpEntity.getCity() == null ? "" : ipLookUpEntity.getCity()).executeSingle();
        IsmartvActivator activator = IsmartvActivator.getInstance();
        activator.setIp(ipLookUpEntity.getIp());
        activator.setIsp(ipLookUpEntity.getIsp());
        if (cityTable != null) {
            activator.setCity(ipLookUpEntity.getCity(), String.valueOf(cityTable.geo_id));
        }
        ProvinceTable provinceTable = new Select().from(ProvinceTable.class)
                .where(ProvinceTable.PROVINCE_NAME + " = ?", ipLookUpEntity.getProv() == null ? "" : ipLookUpEntity.getProv()).executeSingle();
        if (provinceTable != null) {
            activator.setProvince(ipLookUpEntity.getProv(), provinceTable.pinyin);
        }
    }
}
