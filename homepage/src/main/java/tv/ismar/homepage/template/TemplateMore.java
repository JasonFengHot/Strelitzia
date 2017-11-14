package tv.ismar.homepage.template;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tv.ismar.app.core.PageIntent;
import tv.ismar.homepage.R;
import tv.ismar.homepage.fragment.ChannelFragment;

/** @AUTHOR: xi @DATE: 2017/9/21 @DESC: 更多内容 */
public class TemplateMore extends Template implements View.OnClickListener, View.OnKeyListener, View.OnHoverListener, View.OnFocusChangeListener {
  private Button mButton;
  private String mChannel; // 频道
  private String mTitle; // 标题
  private int mStyle; // 竖版或横版

	/*modify by dragontec for bug 4334 start*/
  public TemplateMore(Context context, int position) {
    super(context, position);
  }
  /*modify by dragontec for bug 4334 end*/

  @Override
  public void getView(View view) {
    mButton = (Button) view.findViewById(R.id.get_more_btn);
  }

  @Override
  protected void initListener(View view) {
    super.initListener(view);
    mButton.setOnClickListener(this);
    /*add by dragontec for bug 4221 start*/
    mButton.setOnKeyListener(this);
    mButton.setOnHoverListener(this);
    mButton.setOnFocusChangeListener(this);
    /*add by dragontec for bug 4221 end*/
  }

  @Override
  public void onCreate() {}

  @Override
  public void onStart() {

  }

  @Override
  public void onResume() {}

  @Override
  public void onPause() {
	/*add by dragontec for bug 4077 start*/
	  super.onPause();
	/*add by dragontec for bug 4077 end*/
  }

  @Override
  public void onStop() {}

	/*modify by dragontec for bug 4362 start*/
  @Override
  public void onDestroy() {
	  super.onDestroy();
  }
  /*modify by dragontec for bug 4362 end*/

  @Override
  public void initData(Bundle bundle) {
    mChannel = bundle.getString(ChannelFragment.MORE_CHANNEL_FLAG);
    mTitle = bundle.getString(ChannelFragment.MORE_TITLE_FLAG);
    mStyle = bundle.getInt(ChannelFragment.MORE_STYLE_FLAG);

    switch (mChannel) {
      case "homepage":
        setTemplateVisibility(false);
        break;
      default:
        setTemplateVisibility(true);
        break;
    }
  }

/*add by dragontec for bug 4200 start*/
	@Override
	public void fetchData() {
		//do nothing
	}
	/*modify by dragontec for bug 4200 end*/
	/*modify by dragontec for bug 4334 start*/
	@Override
	public void fillData() {
		//do nothing
	}
	/*modify by dragontec for bug 4334 end*/

  @Override
  public void onClick(View v) {
    PageIntent intent = new PageIntent();
    intent.toListPage(mContext, mTitle, mChannel, mStyle,"");
  }

  private void setTemplateVisibility(boolean visibility) {
    if (visibility) {
      mButton.setVisibility(View.VISIBLE);
    } else {
      mButton.setVisibility(View.INVISIBLE);
    }
  }

  /*add by dragontec for bug 4221 start*/
  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {
    if(event.getAction() == KeyEvent.ACTION_DOWN){
      if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP){
        View focusView = findMoreUpFocus((ViewGroup) mParentView);
        if(focusView != null) {
          focusView.requestFocus();
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean onHover(View v, MotionEvent event) {
  		/*delete by dragontec for bug 4169 start*/
    if(event.getAction() == MotionEvent.ACTION_HOVER_ENTER /*|| event.getAction() == MotionEvent.ACTION_HOVER_MOVE*/){
  	  /*delete by dragontec for bug 4169 end*/
      mButton.requestFocus();
      mButton.requestFocusFromTouch();
    }else if(event.getAction() == MotionEvent.ACTION_HOVER_EXIT){
      mButton.clearFocus();
    }
    return false;
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if(!hasFocus){
      mButton.clearFocus();
      mButton.setHovered(false);
    }
  }

	/*modify by dragontec for bug 4362 start*/
	@Override
	public void callBack(int flags, Object... args) {

	}
	/*modify by dragontec for bug 4362 end*/
  /*add by dragontec for bug 4221 end*/

	@Override
	public void onFetchDataFinish(String bannerPK) {
		//do nothing
	}
}
