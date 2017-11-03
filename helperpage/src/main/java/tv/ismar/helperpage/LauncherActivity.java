package tv.ismar.helperpage;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
//import android.widget.ImageView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.ProblemEntity;
import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.helperpage.core.FeedbackProblem;
import tv.ismar.helperpage.ui.activity.HomeActivity;

public class LauncherActivity extends BaseActivity implements View.OnClickListener, View.OnHoverListener {
    private static final String TAG = "LauncherActivity";


    private RecyclerImageView indicatorNode;
    private RecyclerImageView indicatorFeedback;
    private RecyclerImageView indicatorHelp;
    private SkyService skyService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sakura_activity_launch);
        initViews();
        fetchProblems();
    }

    private void initViews() {
        indicatorNode = (RecyclerImageView) findViewById(R.id.indicator_node_image);
        indicatorFeedback = (RecyclerImageView) findViewById(R.id.indicator_feedback_image);
        indicatorHelp = (RecyclerImageView) findViewById(R.id.indicator_help_image);

        indicatorNode.setOnClickListener(this);
        indicatorFeedback.setOnClickListener(this);
        indicatorHelp.setOnClickListener(this);
        indicatorFeedback.setOnHoverListener(this);
        indicatorHelp.setOnHoverListener(this);

        skyService = SkyService.ServiceManager.getService();

    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        intent.setClass(this, HomeActivity.class);

        int i = view.getId();
        if (i == R.id.indicator_node_image) {
//            intent.putExtra("position", 0);

        } else if (i == R.id.indicator_feedback_image) {
            intent.putExtra("position", 0);

        } else if (i == R.id.indicator_help_image) {
            intent.putExtra("position", 1);

        }
        startActivity(intent);
    }


    /**
     * fetch tv problems from http server
     */
    private void fetchProblems() {
        mIrisService.Problems()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<ProblemEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(List<ProblemEntity> problemEntity) {
                        FeedbackProblem feedbackProblem = FeedbackProblem.getInstance();
                        feedbackProblem.saveCache(problemEntity);
                    }
                });

    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_MOVE:
            case MotionEvent.ACTION_HOVER_ENTER:
                v.requestFocus();
                v.requestFocusFromTouch();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                indicatorNode.requestFocusFromTouch();
                indicatorNode.requestFocus();
                break;
        }
        return true;
    }
}
