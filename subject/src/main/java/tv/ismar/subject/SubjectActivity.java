package tv.ismar.subject;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import tv.ismar.app.BaseActivity;
import tv.ismar.subject.fragment.MovieTVSubjectFragment;
import tv.ismar.subject.fragment.SportSubjectFragment;

/**
 * Created by liucan on 2017/3/1.
 */

public class SubjectActivity extends BaseActivity{


    public String gather_type;
    public int itemid;
    public String frompage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_main_activity);
        Intent intent=getIntent();
        gather_type = intent.getStringExtra("gather_type");
        itemid = intent.getIntExtra("itemid",709759);
        frompage = intent.getStringExtra("frompage");
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        switch (gather_type){
            case "sportgather":
                fragmentTransaction.replace(R.id.subject_frame,new SportSubjectFragment());
                break;
            case "moviegather":
                fragmentTransaction.replace(R.id.subject_frame,new MovieTVSubjectFragment());
                break;
            case "teleplaygather":
                fragmentTransaction.replace(R.id.subject_frame,new MovieTVSubjectFragment());
                break;
        }
        fragmentTransaction.commit();
    }
}
