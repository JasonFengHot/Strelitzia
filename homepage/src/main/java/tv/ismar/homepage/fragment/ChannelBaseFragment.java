package tv.ismar.homepage.fragment;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.homepage.R;
import tv.ismar.app.entity.ChannelEntity;
import tv.ismar.app.entity.HomePagerEntity.Carousel;
import tv.ismar.app.entity.HomePagerEntity.Poster;
import tv.ismar.app.player.InitPlayerTool;
import tv.ismar.homepage.activity.HomePageActivity;

public class ChannelBaseFragment extends Fragment {
    protected ChannelEntity channelEntity;
    protected HomePageActivity mContext;
    protected boolean scrollFromBorder;
    protected View mLeftTopView;
    protected View mLeftBottomView;
    protected View mRightTopView;
    protected View mRightBottomView;
    protected boolean isRight;
    protected String bottomFlag;
    private InitPlayerTool tool;


    
    public void setRight(boolean isRight) {
		this.isRight = isRight;
	}

	public void setBottomFlag(String bottomFlag) {
		this.bottomFlag = bottomFlag;
	}

	public ChannelEntity getChannelEntity() {
        return channelEntity;
    }

    public void setScrollFromBorder(boolean scrollFromBorder) {
		this.scrollFromBorder = scrollFromBorder;
	}

	public void setChannelEntity(ChannelEntity channelEntity) {
        this.channelEntity = channelEntity;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = (HomePageActivity) activity;

    }

    @Override
    public void onDetach() {
    	if(tool != null)
    		tool.removeAsycCallback();
        mContext = null;
        ItemClickListener = null;
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != channelEntity
                && !TextUtils.isEmpty(channelEntity.getChannel()))
            mContext.channelRequestFocus(channelEntity.getChannel());
    }

    protected View.OnClickListener ItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String url = null;
            String contentMode = null;
            String title = null;
            String mode_name = null;
            String channel = "top";
            String type;
            int pk = 0;
            boolean expense = false;
            int position = -1;
            if(channelEntity != null)
                if (channelEntity.getChannel() != null && !("".equals(channelEntity.getChannel()))) {
                    channel = channelEntity.getChannel();
                    if("launcher".equals(channelEntity.getChannel())){
                        channel="top";
                    }
                }
            if (view.getTag() instanceof Poster) {
                Poster new_name = (Poster) view.getTag();
                contentMode = new_name.getContent_model();
                url = new_name.getUrl();
                title = new_name.getTitle();
                mode_name = new_name.getModel_name();
                expense = new_name.isExpense();
                position = new_name.getPosition();
            } else if (view.getTag(R.drawable.launcher_selector) instanceof Carousel) {
                Carousel new_name = (Carousel) view
                        .getTag(R.drawable.launcher_selector);
                contentMode = new_name.getContent_model();
                url = new_name.getUrl();
                title = new_name.getTitle();
                mode_name = new_name.getModel_name();
                expense = new_name.isExpense();
                position = new_name.getPosition();
            }
            type = mode_name;
            Intent intent = new Intent();
            intent.putExtra("channel", channel);
            if (url == null) {
                intent.setAction("tv.ismar.daisy.Channel");
                title = channelEntity.getName();
                pk = SimpleRestClient.getItemId(channelEntity.getUrl(), new boolean[1]);

                intent.putExtra("title", channelEntity.getName());
                intent.putExtra("url", channelEntity.getUrl());
                intent.putExtra("portraitflag", channelEntity.getStyle());

                mContext.startActivity(intent);
            } else {
                if ("item".equals(mode_name)) {
                pk = SimpleRestClient.getItemId(url, new boolean[1]);
                PageIntent pageIntent = new PageIntent();
                pageIntent.toDetailPage(mContext, "homepage", pk);

                } else if ("topic".equals(mode_name)) {
                    intent.putExtra("url", url);
                    intent.setAction("tv.ismar.daisy.Topic");
                    mContext.startActivity(intent);
                } else if ("section".equals(mode_name)) {
                    intent.putExtra("title", title);
                    intent.putExtra("itemlistUrl", url);
                    intent.putExtra("lableString", title);
                    intent.setAction("tv.ismar.daisy.packagelist");
                    mContext.startActivity(intent);
                } else if ("package".equals(mode_name)) {
                    intent.setAction("tv.ismar.daisy.packageitem");
                    intent.putExtra("url", url);
                    mContext.startActivity(intent);
                } else if ("clip".equals(mode_name)) {
                	if(tool == null)
                    tool = new InitPlayerTool(mContext);
                    tool.channel=channelEntity.getChannel();
                    tool.fromPage="homepage";
                    tool.initClipInfo(url, InitPlayerTool.FLAG_URL);
                }
            }
            CallaPlay play = new CallaPlay();
            play.homepage_vod_click(pk, title, channel, position, type);
        }
    };
    public void refreshData(){}
}