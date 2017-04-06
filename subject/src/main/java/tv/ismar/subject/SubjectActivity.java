package tv.ismar.subject;

import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.concurrent.TimeoutException;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.subject.fragment.MovieTVSubjectFragment;
import tv.ismar.subject.fragment.SportSubjectFragment;

/**
 * Created by liucan on 2017/3/1.
 */

public class SubjectActivity extends BaseActivity{


    public String gather_type;
    public int itemid;
    public String frompage;
    public LoadingDialog mLoadingDialog;
    private Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
                showNetWorkErrorDialog(new TimeoutException());
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_main_activity);
     //   showDialog();
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

    public void showDialog() {
        handler.sendEmptyMessageDelayed(0,15000);
        start_time=System.currentTimeMillis();
        mLoadingDialog = new LoadingDialog(this, R.style.LoadingDialog);
        mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                finish();
            }
        });
        mLoadingDialog.showDialog();
    }
}
