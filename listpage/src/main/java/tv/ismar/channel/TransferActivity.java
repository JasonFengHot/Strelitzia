package tv.ismar.channel;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.listpage.R;

/**
 * Created by liucan on 2017/10/17.
 */

public class TransferActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String title = null;
        String url = null;
        String channel = null;
        String fromPage=null;
        String homepage_template=null;
        int portraitflag =1;
        if(intent!=null){
            Bundle bundle = intent.getExtras();
            if(bundle!=null){
                url =bundle.getString("url");

                title = bundle.getString("title");

                channel = bundle.getString("channel");
                portraitflag = bundle.getInt("portraitflag");
                fromPage=bundle.getString("fromPage");
                homepage_template=bundle.getString("homepage_template");
            }else{
                url =intent.getStringExtra("url");

                title = intent.getStringExtra("title");

                channel = intent.getStringExtra("channel");
                portraitflag = intent.getIntExtra("portraitflag",0);
                fromPage=intent.getStringExtra("fromPage");
                homepage_template=intent.getStringExtra("homepage_template");
            }
        }
        if(title==null) {
            title = "华语电影";
        }
        if(channel==null) {
            channel = "$histories_dd";
        }
        PageIntent pageIntent=new PageIntent();
        if(channel!=null) {
            if(channel.equals("$bookmarks")||channel.equals("histories")) {
                pageIntent.toHistory(this,fromPage);
                finish();
            }else{
                pageIntent.toListPage(this,title,channel,portraitflag);
                finish();
            }
        }
    }
}
