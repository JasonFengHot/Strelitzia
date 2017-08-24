package tv.ismar.homepage.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tv.ismar.app.BaseControl;
import tv.ismar.homepage.R;
import tv.ismar.homepage.control.GuideControl;
import tv.ismar.homepage.widget.scroll.VerticalBanner;


/**
 * 首页
 */
public class GuideFragment extends ChannelBaseFragment implements BaseControl.ControlCallBack {

    private GuideControl mControl = null;

    private VerticalBanner mContainer;

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
        return view;
    }

    /*获取控件实例*/
    private void findViews(View view){
        mContainer = (VerticalBanner) view.findViewById(R.id.guide_container);
    }

    /*初始化监听*/
    private void initListener(){

    }

    private View createView(int layoutId){
        return LayoutInflater.from(getContext()).inflate(layoutId, null);
    }


    /*用于业务类回调控制UI*/
    @Override
    public void callBack(int flag) {

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



