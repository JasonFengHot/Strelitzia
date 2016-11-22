package tv.ismar.app.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.open.androidtvwidget.view.LinearMainLayout;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.R;
import tv.ismar.app.core.WeatherInfoHandler;
import tv.ismar.app.network.entity.WeatherEntity;
import tv.ismar.app.widget.LaunchHeaderLayout;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;

public class HeadFragment extends Fragment implements View.OnClickListener, View.OnHoverListener, View.OnFocusChangeListener {
    public static final String HEADER_USERCENTER = "usercenter";
    public static final String HEADER_DETAILPAGE = "detailpage";
    public static final String HEADER_HOMEPAGE = "homepage";
    public static final String HEADER_LISTPAGE="listpage";
    private static final int[] INDICATOR_RES_LIST = {
            R.string.vod_movielist_title_history,
            R.string.guide_my_favorite,
            R.string.guide_user_center,
            R.string.guide_search
    };


    private String mHeaderType;
    private TextView titleTextView;
    private TextView subTitleTextView;
    private TextView weatherInfoTextView;
    private ImageView dividerImage;
    private LinearLayout guideLayout;
    private List<View> indicatorTableList;


    public HeadFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_head, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        indicatorTableList = new ArrayList<>();
        titleTextView = (TextView) view.findViewById(R.id.title);
        subTitleTextView = (TextView) view.findViewById(R.id.sub_title);
        weatherInfoTextView = (TextView) view.findViewById(R.id.weather_info);
        guideLayout = (LinearLayout) view.findViewById(R.id.indicator_layout);
        dividerImage = (ImageView) view.findViewById(R.id.divider);

        titleTextView.setText(R.string.app_name);
        subTitleTextView.setText(R.string.front_page);


        createGuideIndicator();

        HashMap<String, String> hashMap = IsmartvActivator.getInstance().getCity();
        String geoId = hashMap.get("geo_id");
        fetchWeatherInfo(geoId);

    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        mHeaderType = bundle.getString("type");
        if (!TextUtils.isEmpty(mHeaderType)) {
            switch (mHeaderType) {
                case HEADER_USERCENTER:
                    subTitleTextView.setText("个人中心");
                    LinearMainLayout.LayoutParams layoutParams = new LinearMainLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER_VERTICAL;
//                    layoutParams.setMargins(getResources().getDimensionPixelSize(R.dimen.usercenter_subtitle_ml), 0, 0, getResources().getDimensionPixelSize(R.dimen.weather_guide_text_margin_margin_bottom));
                    subTitleTextView.setLayoutParams(layoutParams);
                    subTitleTextView.setGravity(Gravity.CENTER_VERTICAL);
                    hideIndicatorTable();
                    hideTitle();
                    break;
                case HEADER_DETAILPAGE:
                    subTitleTextView.setText(bundle.getString("channel_name"));
                    hideWeather();
                    hideIndicatorTable();
                    hideTitle();
                    break;
                case HEADER_LISTPAGE:
                    subTitleTextView.setText(bundle.getString("channel_name"));
                    RelativeLayout.LayoutParams listlayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    listlayoutParams.setMargins(getResources().getDimensionPixelSize(R.dimen.usercenter_subtitle_ml), 0, 0, getResources().getDimensionPixelSize(R.dimen.weather_guide_text_margin_margin_bottom));
                    listlayoutParams.addRule(ALIGN_PARENT_BOTTOM);
                    titleTextView.setLayoutParams(listlayoutParams);
                    RelativeLayout.LayoutParams weatherParams=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    weatherParams.setMargins(getResources().getDimensionPixelSize(R.dimen.head_weather_ml),0,0,getResources().getDimensionPixelSize(R.dimen.weather_guide_text_margin_margin_bottom));
                    weatherParams.addRule(ALIGN_PARENT_BOTTOM);
                    weatherInfoTextView.setLayoutParams(weatherParams);
                    hideIndicatorTable();
                    break;
                case HEADER_HOMEPAGE:
                    subTitleTextView.setText(bundle.getString("channel_name"));
                    break;
            }
        }

    }

    public void setHeadTitle(String title) {
        titleTextView.setVisibility(View.VISIBLE);
        titleTextView.setText(title);
    }


    private void createGuideIndicator() {
        int i = 0;
        indicatorTableList.clear();
        for (int res : INDICATOR_RES_LIST) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_header_indicator, null);
            TextView textView = (TextView) view.findViewById(R.id.weather_indicator);
            view.setOnClickListener(this);
            view.setOnFocusChangeListener(this);
            view.setOnHoverListener(this);
            textView.setText(res);
            view.setId(res);

            ImageView imageView = (ImageView) view.findViewById(R.id.indicator_image);
            String text = getString(res);
            int width = (int) (getResources().getDimensionPixelOffset(R.dimen.header_fragment_mine_indicator_w) / (4.0f / text.length()));
            ((LinearLayout.LayoutParams) imageView.getLayoutParams()).width = width;

            if (i == 0) {
                view.setNextFocusLeftId(view.getId());
            }
            if (i == INDICATOR_RES_LIST.length - 1) {
                view.setNextFocusRightId(view.getId());
            }
            guideLayout.addView(view);
            indicatorTableList.add(view);
            i++;
        }
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setSubTitle(String subTitle) {
        if (TextUtils.isEmpty(subTitle)) {
            hideSubTiltle();
        } else {
            subTitleTextView.setText(subTitle.replace(" ", ""));
        }
    }

    public void hideSubTiltle() {
        subTitleTextView.setVisibility(View.GONE);
        dividerImage.setVisibility(View.GONE);

    }

    private void hideTitle() {
        titleTextView.setVisibility(View.GONE);
        dividerImage.setVisibility(View.GONE);
    }

    public void hideIndicatorTable() {
        for (View textView : indicatorTableList) {
            textView.setVisibility(View.GONE);
        }
    }

    public void hideWeather() {
        weatherInfoTextView.setVisibility(View.INVISIBLE);
    }


    private void fetchWeatherInfo(String geoId) {
        ((BaseActivity) getActivity()).mWeatherSkyService.apifetchWeatherInfo(geoId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((BaseActivity) getActivity()).new BaseObserver<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String result = null;
                        try {
                            result = responseBody.string();
                            parseXml(result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    private void parseXml(String xml) {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            WeatherInfoHandler weatherInfoHandler = new WeatherInfoHandler();
            xmlReader.setContentHandler(weatherInfoHandler);
            InputSource inputSource = new InputSource(new StringReader(xml));
            xmlReader.parse(inputSource);

            WeatherEntity weatherEntity = weatherInfoHandler.getWeatherEntity();

            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
            String todayTime = dateFormat.format(now);

            weatherInfoTextView.setText("");
            weatherInfoTextView.append("   " + todayTime + "   ");
            weatherInfoTextView.append(weatherEntity.getToday().getCondition() + "   ");
            if (weatherEntity.getToday().getTemplow().equals(weatherEntity.getToday().getTemphigh())) {
                weatherInfoTextView.append(weatherEntity.getToday().getTemplow() + getText(R.string.degree));
            } else {
                weatherInfoTextView.append(weatherEntity.getToday().getTemplow() + " ~ " + weatherEntity.getToday().getTemphigh() + getText(R.string.degree));
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (mHeadItemClickListener != null) {
            int i = v.getId();
            if (i == R.string.vod_movielist_title_history) {

            } else if (i == R.string.guide_my_favorite) {

            } else if (i == R.string.guide_user_center) {
                mHeadItemClickListener.onUserCenterClick();

            } else if (i == R.string.guide_search) {

            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        ImageView imageView = (ImageView) v.findViewById(R.id.indicator_image);
        TextView textView = (TextView) v.findViewById(R.id.weather_indicator);
        if (hasFocus) {
            textView.setTextColor(getResources().getColor(R.color._ff9c3c));
            imageView.setVisibility(View.VISIBLE);

        } else {
            textView.setTextColor(getResources().getColor(R.color.association_normal));
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        ImageView imageView = (ImageView) v.findViewById(R.id.indicator_image);
        TextView textView = (TextView) v.findViewById(R.id.weather_indicator);
        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
            textView.setTextColor(getResources().getColor(R.color._ff9c3c));
            imageView.setVisibility(View.VISIBLE);
            v.requestFocus();
        } else if (event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
            textView.setTextColor(getResources().getColor(R.color._ff9c3c));
            imageView.setVisibility(View.VISIBLE);
        } else {
            textView.setTextColor(getResources().getColor(R.color.association_normal));
            imageView.setVisibility(View.INVISIBLE);
        }
        return false;
    }


    public interface HeadItemClickListener {
        void onUserCenterClick();

        void onHistoryClick();

        void onFavoriteClick();

        void onSearchClick();
    }

    private HeadItemClickListener mHeadItemClickListener;


    public void setHeadItemClickListener(HeadItemClickListener headItemClickListener) {
        mHeadItemClickListener = headItemClickListener;
    }
}
