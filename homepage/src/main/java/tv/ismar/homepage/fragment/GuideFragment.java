package tv.ismar.homepage.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.GuideAdapter;
import tv.ismar.homepage.control.GuideControl;
import tv.ismar.homepage.widget.scroll.VerticalBanner;
import tv.ismar.homepage.widget.scroll.listener.OnUseViewChange;


/**
 * 首页
 * 注：1.保证方法职责单一（只做一件事情） 2.业务相关的写到control里面，视图相关的写到fragment里面
 *     3.必要的注释要有奥，如flag标记，说清楚不同的值代表啥意思，其他的自己斟酌
 *     4.用工程化思维去写代码
 */
public class GuideFragment extends ChannelBaseFragment implements BaseControl.ControlCallBack, OnUseViewChange {

    private GuideControl mControl = null;//业务类引用

    private ListView mContainer;

    private GuideAdapter mAdapter = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mControl = new GuideControl(getContext(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guide, null);
        findViews(view);
        initListener();
        initData();
        return view;
    }

    /*获取控件实例*/
    private void findViews(View view){
        mContainer = (ListView) view.findViewById(R.id.guide_container);
    }

    /*初始化监听*/
    private void initListener(){

    }

    private void initData(){
        mControl.fetchBannerList("tv");
    }

    /*用于业务类回调控制UI*/
    @Override
    public void callBack(int flag, Object... args) {
        if(flag == GuideControl.FETCH_GUIDE_BANNERS_FLAG){//这里通过flag严格区分不同的业务流程，避免业务之间的耦合
            GuideBanner[] banners = (GuideBanner[]) args[0];
            return;
        }
    }

    /*VerticalBanner当前使用的视图*/
    @Override
    public void useViewChange(View view, int position) {

    }
}






class Flag {

    private ChangeCallback changeCallback;

    public Flag(ChangeCallback changeCallback) {
        this.changeCallback = changeCallback;
    }

    private int position;

    public void setPosition(int position) {
        this.position = position;
        changeCallback.change(position);

    }

    public int getPosition() {
        return position;
    }

    public interface ChangeCallback {
        void change(int position);
    }


}



