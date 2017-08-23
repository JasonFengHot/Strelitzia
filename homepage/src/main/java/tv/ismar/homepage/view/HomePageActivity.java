package tv.ismar.homepage.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import tv.ismar.homepage.R;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/22
 * @DESC: 主页
 */

public class HomePageActivity extends Activity{

    

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_activity_layout);
    }
}
