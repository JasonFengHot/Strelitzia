package tv.ismar.homepage.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import tv.ismar.app.BaseControl;
import tv.ismar.app.entity.GuideBanner;
import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.HomeAdapter;
import tv.ismar.homepage.adapter.HomeReclAdapter;
import tv.ismar.homepage.control.FetchDataControl;
import tv.ismar.library.util.StringUtils;

import static tv.ismar.homepage.control.FetchDataControl.FETCH_CHANNEL_BANNERS_FLAG;
import static tv.ismar.homepage.control.FetchDataControl.FETCH_HOME_BANNERS_FLAG;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/8
 * @DESC: 频道fragemnt
 */

public class ChannelFragment extends BaseFragment implements BaseControl.ControlCallBack{
    private FetchDataControl mControl = null;//业务类引用
//    private RecyclerView mRecycleView;
    private ListView mListView;
    private HomeAdapter mAdapter;

    private String mChannel;//频道

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mControl = new FetchDataControl(getContext(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.channel_fragment_layout, null);
        findView(view);
        initData();
        return view;
    }

    private void findView(View view){
//        mRecycleView = (RecyclerView) view.findViewById(R.id.home_fragment_recycleview);
        mListView = (ListView) view.findViewById(R.id.home_fragment_listview);
        mListView.setItemsCanFocus(true);
//        LinearLayoutManager linear = new LinearLayoutManager(getContext());
//        linear.setOrientation(LinearLayoutManager.VERTICAL);
//        mRecycleView.setLayoutManager(linear);
    }

    public void setChannel(String channel){
        this.mChannel = channel;
    }

    private void initData(){
        if(!StringUtils.isEmpty(this.mChannel)){
            if(this.mChannel.equals(HomeActivity.HOME_PAGE_CHANNEL_TAG)){
                mControl.fetchHomeBanners();
            } else {
                mControl.fetchChannelBanners(mChannel);
            }
        }
    }

    private void initAdapter(GuideBanner[] banners){
        if(mAdapter == null){
//            mAdapter = new HomeReclAdapter(getContext(), banners);
//            mRecycleView.setAdapter(mAdapter);
            mAdapter = new HomeAdapter(getContext(), banners);
            mListView.setAdapter(mAdapter);
        }else{
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void callBack(int flags, Object... args) {
        //这里通过flag严格区分不同的业务流程，避免业务之间的耦合
        if(flags == FETCH_HOME_BANNERS_FLAG){//获取到首页下的banners
        }else if(flags == FETCH_CHANNEL_BANNERS_FLAG){//获取到频道下的banners
        }
        GuideBanner[] banners = (GuideBanner[]) args;
        initAdapter(banners);
    }
}
