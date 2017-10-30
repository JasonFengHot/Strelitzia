package tv.ismar.homepage.adapter;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;

/*add by dragontec for bug 4265 start*/
import android.view.KeyEvent;
/*add by dragontec for bug 4265 end*/
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/*add by dragontec for bug 4265 start*/
import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
/*add by dragontec for bug 4265 end*/

import tv.ismar.homepage.HomeActivity;
import tv.ismar.homepage.OnItemClickListener;
import tv.ismar.homepage.OnItemHoverListener;
import tv.ismar.homepage.OnItemSelectedListener;
import tv.ismar.homepage.R;

import static android.view.View.SCALE_X;
import static android.view.View.SCALE_Y;

/**
 * @AUTHOR: xi
 * @DATE: 2017/9/15
 * @DESC: ViewHolder基类
 */
public abstract class BaseViewHolder extends RecyclerView.ViewHolder implements
        View.OnFocusChangeListener, View.OnClickListener, View.OnHoverListener,
/*add by dragontec for bug 4265 start*/
		View.OnKeyListener
/*add by dragontec for bug 4265 end*/
{

    public int mPosition;//item位置
    private OnItemClickListener mClickListener = null;
    private OnItemSelectedListener mSelectedListener = null;
    private OnItemHoverListener mHoverListener = null;
	/*add by dragontec for bug 4265 start*/
    private RecyclerView mRecyclerView = null;
    private boolean isCenter = false;
	/*add by dragontec for bug 4265 end*/

    public BaseViewHolder(View itemView, BaseRecycleAdapter baseAdapter) {
        super(itemView);
        this.mClickListener = baseAdapter.mClickListener;
        this.mSelectedListener = baseAdapter.mSelectedListener;
        this.mHoverListener = baseAdapter.mHoverListener;
		/*add by dragontec for bug 4265 start*/
        this.mRecyclerView = baseAdapter.mRecyclerView;
        if (baseAdapter instanceof CenterAdapter) {
			isCenter = true;
		}
		/*add by dragontec for bug 4265 end*/
        initListener();
    }

    private void initListener(){
        if(itemView.findViewById(getScaleLayoutId()) != null){
            itemView.findViewById(getScaleLayoutId()).setOnFocusChangeListener(this);
            itemView.findViewById(getScaleLayoutId()).setOnClickListener(this);
            itemView.findViewById(getScaleLayoutId()).setOnHoverListener(this);
			/*add by dragontec for bug 4265 start*/
            itemView.findViewById(getScaleLayoutId()).setOnKeyListener(this);
			/*add by dragontec for bug 4265 end*/
        }
    }

    protected abstract int getScaleLayoutId();
	/*add by dragontec for bug 4325 start*/
    protected abstract int getTitleId();
	/*add by dragontec for bug 4325 end*/
    protected  float getScaleXY(){
        return 1.1F;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            scaleToLarge(v.findViewById(getScaleLayoutId()));
            if(mSelectedListener != null) {
                mSelectedListener.onItemSelect(v, mPosition);
            }
        } else {
            scaleToNormal(v.findViewById(getScaleLayoutId()));
        }
		/*add by dragontec for bug 4325,4316 start*/
		if (!(this instanceof CenterAdapter.CenterViewHolder)) {
			updateTitleText(hasFocus);
		}else{
			updateForCenter(hasFocus);
		}
		/*add by dragontec for bug 4325,4316 end*/
    }

    @Override
    public void onClick(View v) {
        if(mClickListener!=null && v.getId()==getScaleLayoutId()){//item选中事件
            mClickListener.onItemClick(v, mPosition);
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        Log.i("onHover", "ViewHolder action:"+event.getAction());
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_ENTER://鼠标放置到view上时 9
			/*delete by dragontec for bug 4169 start*/
//            case MotionEvent.ACTION_HOVER_MOVE://7 
			/*delete by dragontec for bug 4169 end*/
			/*modify by dragontec for bug 4277 start*/
			boolean needRequestFocus = true;
                if (mHoverListener!= null){
                    needRequestFocus= mHoverListener.onHover(v, mPosition, true);
                }
				//by dragontec 和其他地方保持一致
				/*add by dragontec for bug 4265 start*/
				if (needRequestFocus && !v.hasFocus()) {
					int[] location = new int[]{0, 0};
					v.getLocationOnScreen(location);
					int screenWidth = v.getResources().getDisplayMetrics().widthPixels;
					int screenHeight = v.getResources().getDisplayMetrics().heightPixels;
					if (location[0] >= 0 && location[1] >= 0 && location[0] + v.getWidth() <= screenWidth && location[1] + v.getHeight() <= screenHeight) {
						v.requestFocus();
						v.requestFocusFromTouch();
					}
				}
				/*add by dragontec for bug 4265 end*/
                break;
			/*modify by dragontec for bug 4277 end*/
            case MotionEvent.ACTION_HOVER_EXIT://10
                if (mHoverListener!= null){
                    mHoverListener.onHover(v, mPosition, false);
                }
				//by dragontec clearfocus应该和是否setListener无关
                /*modify by dragontec for bug 4057 start*/
//                    HomeActivity.mHoverView.requestFocus();//将焦点放置到一块隐藏view中
                if (event.getButtonState() != MotionEvent.BUTTON_PRIMARY) {
                    v.clearFocus();
                }
                /*modify by dragontec for bug 4057 end*/
                break;
        }
        return false;
    }
	/*add by dragontec for bug 4325 start*/
    private void updateTitleText(boolean hasFocus) {
        View view = itemView.findViewById(getTitleId());
        if(view != null && view instanceof TextView) {
            TextView textView = (TextView) itemView.findViewById(getTitleId());
            Object tag = itemView.findViewById(getTitleId()).getTag();
            if (tag != null && tag instanceof String[] && ((String[]) tag).length == 2) {
                String title = ((String[]) tag)[0];
                String focusTitle = ((String[]) tag)[1];
                if (hasFocus) {
                    textView.setText(focusTitle);
                    textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                    textView.setMarqueeRepeatLimit(-1);
                    textView.setHorizontallyScrolling(true);
                    textView.setSelected(true);
                } else {
                    textView.setText(title);
                    textView.setEllipsize(TextUtils.TruncateAt.END);
                    textView.setMarqueeRepeatLimit(0);
                    textView.setHorizontallyScrolling(false);
                    textView.setSelected(false);
                }
            }
        }
    }
	/*add by dragontec for bug 4325 end*/
	 /*add by dragontec for bug 4316 start*/
    private void updateForCenter(boolean hasFocus) {
        if (this instanceof CenterAdapter.CenterViewHolder) {
            ((CenterAdapter.CenterViewHolder) this).mIntroduction.setVisibility(hasFocus?View.VISIBLE:View.GONE);
            TextView title =  ((CenterAdapter.CenterViewHolder) this).mTitle;
            TextView introduction =  ((CenterAdapter.CenterViewHolder) this).mIntroduction;
            if(hasFocus){
				title.setVisibility(title.length() > 0 ? View.VISIBLE : View.GONE);
				introduction.setVisibility(introduction.length() > 0 ? View.VISIBLE : View.GONE);
                title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                title.setMarqueeRepeatLimit(-1);
                title.setHorizontallyScrolling(true);
                title.setSelected(true);
				title.setTextSize(title.getResources().getDimensionPixelSize(R.dimen.center_title_focused_size) / title.getResources().getDisplayMetrics().density);
				title.getLayoutParams().height = (int) (title.getResources().getDimensionPixelSize(R.dimen.center_title_focused_height) / title.getResources().getDisplayMetrics().density);
                introduction.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                introduction.setMarqueeRepeatLimit(-1);
                introduction.setHorizontallyScrolling(true);
                introduction.setSelected(true);
            }else{
            	title.setVisibility(View.VISIBLE);
				introduction.setVisibility(View.GONE);
                title.setEllipsize(TextUtils.TruncateAt.END);
                title.setMarqueeRepeatLimit(0);
                title.setHorizontallyScrolling(false);
                title.setSelected(false);
				title.setTextSize(title.getResources().getDimensionPixelSize(R.dimen.center_title_size) / title.getResources().getDisplayMetrics().density);
				title.getLayoutParams().height = (int) (title.getResources().getDimensionPixelSize(R.dimen.center_title_height) / title.getResources().getDisplayMetrics().density);
                introduction.setEllipsize(TextUtils.TruncateAt.END);
                introduction.setMarqueeRepeatLimit(0);
                introduction.setHorizontallyScrolling(false);
                introduction.setSelected(false);
            }
        }
	}
	/*add by dragontec for bug 4316 end*/

    /*add by dragontec for bug 4265 start*/
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
    	if (event.getAction() == KeyEvent.ACTION_UP) {
    		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ) {
    			if (mRecyclerView != null && mRecyclerView instanceof RecyclerViewTV) {
    				if (((RecyclerViewTV) mRecyclerView).isNotScrolling()) {
						//check item
						int[] location = new int[]{0, 0};
						v.getLocationOnScreen(location);
						int screenWidth = v.getResources().getDisplayMetrics().widthPixels;
						if (location[0] < 0 || location[0] + v.getWidth() > screenWidth) {
							if (mRecyclerView.getLayoutManager() != null) {
								if (isCenter) {
									if (mRecyclerView.getLayoutManager() instanceof LinearLayoutManagerTV) {
										if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
											int position = ((RecyclerViewTV)mRecyclerView).findFirstVisibleItemPosition();
											((LinearLayoutManagerTV) mRecyclerView.getLayoutManager()).setCanScroll(true);
											((LinearLayoutManagerTV) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset( ((RecyclerViewTV)mRecyclerView).findFirstVisibleItemPosition(), v.getContext().getResources().getDimensionPixelOffset(R.dimen.center_padding_offset));
											if (mSelectedListener != null) {
												mSelectedListener.onItemSelect(v, position);
											}
										}
										if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
											int position = ((RecyclerViewTV)mRecyclerView).findFirstVisibleItemPosition();
											((LinearLayoutManagerTV) mRecyclerView.getLayoutManager()).setCanScroll(true);
											((LinearLayoutManagerTV) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset( ((RecyclerViewTV)mRecyclerView).findLastVisibleItemPosition(), v.getContext().getResources().getDimensionPixelOffset(R.dimen.center_padding_offset));
											if (mSelectedListener != null) {
												mSelectedListener.onItemSelect(v, position);
											}
										}
									}
								} else {
									int position = getAdapterPosition();
									mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, getAdapterPosition());
									if (mSelectedListener != null) {
										mSelectedListener.onItemSelect(v, position);
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	/*add by dragontec for bug 4265 end*/

	/*缩放到1.1倍*/
    protected void scaleToLarge(View view) {
        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(view, SCALE_X, 1.0F, getScaleXY());
        objectAnimatorX.setDuration(100L);
        objectAnimatorX.start();
        ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(view, SCALE_Y, 1.0F, getScaleXY());
        objectAnimatorY.setDuration(100L);
        objectAnimatorY.start();
    }

    /*缩放到正常*/
    protected void scaleToNormal(View view) {
        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(view, SCALE_X, 1.1F, 1.0F);
        objectAnimatorX.setDuration(100L);
        objectAnimatorX.start();
        ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(view, SCALE_Y, 1.1F, 1.0F);
        objectAnimatorY.setDuration(100L);
        objectAnimatorY.start();
    }
}
