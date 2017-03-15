package tv.ismar.subject;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;

import tv.ismar.app.BaseActivity;
import tv.ismar.subject.fragment.MovieTVSubjectFragment;
import tv.ismar.subject.fragment.SportSubjectFragment;

/**
 * Created by liucan on 2017/3/1.
 */

public class SubjectActivity extends BaseActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_main_activity);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SportSubjectFragment sportSubjectFragment=new SportSubjectFragment();
        fragmentTransaction.add(R.id.subject_frame,sportSubjectFragment);
        fragmentTransaction.commit();
     //   setContentView(R.layout.activity_subject);
     //   initView();
    }

    private void initView() {
        MovieTVSubjectFragment movieTVSubjectFragment=new MovieTVSubjectFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,movieTVSubjectFragment).commit();
    }
}
