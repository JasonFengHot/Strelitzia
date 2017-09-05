package tv.ismar.homepage;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.homepage.adapter.HomeAdapter;
import tv.ismar.homepage.control.FetchDataControl;

/**
 * @AUTHOR: xi
 * @DATE: 2017/8/29
 * @DESC: 说明
 */

public class TestActivity extends Activity implements BaseControl.ControlCallBack{

    private final FetchDataControl mControl = new FetchDataControl(this, this);//业务类引用

    private ListView mListView;
    private HomeAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_layout);
        findViews();
        initData();
    }

    private ViewGroup mViewGroup;
    private BitmapDecoder mBitmapDecoder;

    /*获取控件实例*/
    private void findViews(){
        mViewGroup = (ViewGroup) findViewById(R.id.home_view_layout);
        mListView = (ListView) findViewById(R.id.guide_container);
        mListView.setItemsCanFocus(true);
        mBitmapDecoder = new BitmapDecoder();
        mBitmapDecoder.decode(this, R.drawable.homepage_background, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                mViewGroup.setBackground(bitmapDrawable);
                mBitmapDecoder = null;
            }
        });
    }

    private void initData(){
//        mControl.fetchBannerList();
    }

    public void go2Guide(View view){
        mControl.fetchBannerList();
    }

    public void refresh(View view){
        mAdapter.notifyDataSetChanged();
    }

    /*用于业务类回调控制UI*/
    @Override
    public void callBack(int flag, Object... args) {
        //这里通过flag严格区分不同的业务流程，避免业务之间的耦合
        if(flag == FetchDataControl.FETCH_HOME_BANNERS_FLAG){//处理获取首页banner列表
            GuideBanner[] banners = (GuideBanner[]) args;
            if(mAdapter == null){
                mAdapter = new HomeAdapter(this, banners);
                mListView.setAdapter(mAdapter);
            }else{
                mAdapter.notifyDataSetChanged();
            }
            return;
        }
    }
}
