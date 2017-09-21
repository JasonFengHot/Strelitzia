package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import tv.ismar.app.core.PageIntent;
import tv.ismar.homepage.R;
import tv.ismar.homepage.fragment.ChannelFragment;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/21
 * @DESC: 更多内容
 */

public class TemplateMore extends Template implements View.OnClickListener{
    private View mMoreLayout;
    private String mChannel;//频道
    private String mTitle;//标题
    private int mStyle;//竖版或横版

    public TemplateMore(Context context) {
        super(context);
    }

    @Override
    public void getView(View view) {
        mMoreLayout = view.findViewById(R.id.more_ismartv_linear_layout);
    }

    @Override
    protected void initListener(View view) {
        super.initListener(view);
        mMoreLayout.setOnClickListener(this);
    }

    @Override
    public void initData(Bundle bundle) {
        mChannel = bundle.getString(ChannelFragment.MORE_CHANNEL_FLAG);
        mTitle = bundle.getString(ChannelFragment.MORE_TITLE_FLAG);
        mStyle = bundle.getInt(ChannelFragment.MORE_STYLE_FLAG);
    }

    @Override
    public void onClick(View v) {
        PageIntent intent = new PageIntent();
        intent.toListPage(mContext, mTitle, mChannel, mStyle);
    }
}
