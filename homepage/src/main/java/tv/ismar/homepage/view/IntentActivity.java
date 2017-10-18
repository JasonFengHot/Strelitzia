package tv.ismar.homepage.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import tv.ismar.homepage.R;

/**
 * Created by huibin on 18/10/2017.
 */

public class IntentActivity extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent);
    }
}
