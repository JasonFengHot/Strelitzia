package tv.ismar.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;

import tv.ismar.view.IsmartvLinearLayout;

/**
 * Created by admin on 2017/6/20.
 * 自定义recyclerview的layoutmanager
 * 定制焦点的特殊移动规则、控制recyclerview是否可以滚动、设置nextleftFocusView
 */

public class FocusGridLayoutManager extends GridLayoutManager {
	private final String TAG = this.getClass().getSimpleName();
    private int spanCount;
    private ArrayList<SpecialPos> specialPos;
    private int mItemCount=1;
    private View leftFocusView;
    private Context context;
    private boolean scroll=false;
    private boolean isFavorite=false;
    private int nextPos;
    private View nextView;
    private RecyclerView mRecyclerView;


    public FocusGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FocusGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
        this.context=context;
        this.spanCount = spanCount;
    }

    public FocusGridLayoutManager(Context context, int spanCount, boolean needNewLine) {
        super(context, spanCount);
        this.context=context;
    }


    public FocusGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

	@Override
	public void onAttachedToWindow(RecyclerView view) {
		super.onAttachedToWindow(view);
		mRecyclerView = view;
	}

	@Override
	public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
		mRecyclerView = null;
		super.onDetachedFromWindow(view, recycler);
	}

	public void setFavorite(boolean favorite){
        isFavorite=favorite;
    }
    @Override
    public int getChildCount() {
        return super.getChildCount();
    }

    public void setmItemCount(int mItemCount) {
        this.mItemCount = mItemCount;
    }

    @Override
    public View getChildAt(int index) {
        return super.getChildAt(index);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }


    @Override
    public View getFocusedChild() {
        return super.getFocusedChild();
    }

    @Override
    public int getPosition(View view) {
        if(view.getLayoutParams() instanceof RecyclerView.LayoutParams) {
            return super.getPosition(view);
        }else{
            return -1;
        }
    }

    @Override
    public int getSpanCount() {
        return super.getSpanCount();
    }

    @Override
    public View onInterceptFocusSearch(View focused, int direction) {
    	if (isFavorite) {
    		View nextFocus = null;
    		if (mRecyclerView != null) {
				int fromPos = mRecyclerView.getChildAdapterPosition(focused);
				if (fromPos != RecyclerView.NO_POSITION) {
					int nextPos = getFavoriteNextViewPos(fromPos, direction);
					Log.d(TAG, "onInterceptFocusSearch nextPos = " + nextPos);
					if (nextPos != -1) {
						RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(nextPos);
						if (viewHolder != null && viewHolder.itemView != null) {
							nextFocus = viewHolder.itemView;
						}
					}
				}
				if (nextFocus == null) {
					if (direction == View.FOCUS_LEFT) {
						nextFocus = focused;
						if (findFirstCompletelyVisibleItemPosition() == 0) {
							YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(nextFocus);
						}
					} else if (direction == View.FOCUS_RIGHT) {
						nextFocus = focused;
						if (findLastCompletelyVisibleItemPosition() == getItemCount() - 1) {
							YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(nextFocus);
						}
					} else if (direction == View.FOCUS_DOWN) {
						nextFocus = focused;
						if (findLastCompletelyVisibleItemPosition() == getItemCount() - 1) {
							YoYo.with(Techniques.VerticalShake).duration(1000).playOn(nextFocus);
						}
					} else if (direction == View.FOCUS_UP) {
						if (findFirstCompletelyVisibleItemPosition() != 0) {
							nextFocus = focused;
						}
					}
				}
			}
			Log.d(TAG, "onInterceptFocusSearch nextFocus = " + nextFocus);
			return nextFocus;
		} else {
			int index=getPosition(focused);
			if (direction == View.FOCUS_RIGHT) {
				if (specialPos != null && specialPos.contains(new SpecialPos(index + 1))) {
					nextPos = getFavoriteNextViewPos(getPosition(focused), direction);
//                if(findLastVisibleItemPosition()==mItemCount-1||mItemCount- nextPos <getSpanCount()) {
//                    scroll=false;
//                }else{
//                    scroll=true;
//                }
//                if(mItemCount - nextPos < getSpanCount()){
//                    scroll=true;
//                }
//                scrollToPositionWithOffset(nextPos, 0);
					nextView = findViewByPosition(nextPos);
					return nextView;
				}
	            if(!isFavorite && specialPos != null){
	                if(index== specialPos.get(specialPos.size() - 1).endPosition){
	                    YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
	                    return focused;
	                }
	            }else{
	                if(index== getItemCount()-1){
	                    YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
	                    return focused;
	                }
	            }
			} else if (direction == View.FOCUS_UP) {
				if (!isFavorite) {
					if (specialPos != null) {
						if (index <= getSpanCount()) {
							if (specialPos.size() > 1) {
								if (index < specialPos.get(0).startPosition) {
									YoYo.with(Techniques.VerticalShake).duration(1000).playOn(focused);
								} else {
									return null;
								}
							} else if (specialPos.size() == 1) {
								YoYo.with(Techniques.VerticalShake).duration(1000).playOn(focused);
							}
							return focused;
						}
					} else if (index < getSpanCount()) {
						YoYo.with(Techniques.VerticalShake).duration(1000).playOn(focused);
						return focused;
					}
				}
			} else if (direction == View.FOCUS_LEFT) {
				if (isFavorite && index == 0) {
					YoYo.with(Techniques.HorizontalShake).duration(1000).playOn(focused);
					return focused;
				}
			}
			return super.onInterceptFocusSearch(focused, direction);
		}
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        if (!isFavorite) {
			if (scroll) {
				scroll = false;
				if (mItemCount - nextPos - 1 <= getSpanCount() && specialPos != null) {
					View view = findViewByPosition(specialPos.get(specialPos.size() - 1).startPosition + 1);
					if (view != null) {
						view.requestFocus();
					}
				} else {
					if (nextView == null)
						findViewByPosition(findFirstCompletelyVisibleItemPosition() + 1).requestFocus();
				}
			}
		}
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
		if (isFavorite) {
			return onInterceptFocusSearch(focused, focusDirection);
		} else {
			// Need to be called in order to layout new row/column
			View nextFocus = super.onFocusSearchFailed(focused, focusDirection, recycler, state);
			if (!isFavorite) {
				if (nextFocus == null && focusDirection == View.FOCUS_LEFT) {
					return leftFocusView;
				}
			}
			/**
			 * 获取当前焦点的位置
			 */
			int fromPos = getPosition(focused);
			/**
			 * 获取我们希望的下一个焦点的位置
			 */
			int nextPos = -1;
			if (isFavorite) {
				nextPos = getFavoriteNextViewPos(fromPos, focusDirection);
			} else {
				nextPos = getFliterNextViewPos(fromPos, focusDirection);
			}
			View nextView = null;
			if (nextPos != -1) {
				nextView = findViewByPosition(nextPos);
			}
			if (focusDirection == View.FOCUS_DOWN) {
				for (int i = fromPos; i < nextPos; i++) {
					if (specialPos != null && specialPos.contains(new SpecialPos(i))) {
						int nextSpecialPos = specialPos.indexOf(new SpecialPos(i));
						int lastColumnCount = (i - specialPos.get(nextSpecialPos - 1).startPosition - 1) % getSpanCount();
						if (lastColumnCount == 0) {
							lastColumnCount = getSpanCount();
						}
						int currentLine = fromPos - specialPos.get(nextSpecialPos).startPosition + lastColumnCount + 1;
						if (i + currentLine > mItemCount - 1) {
							nextView = findViewByPosition(mItemCount - 1);
						} else if (nextSpecialPos + 1 < specialPos.size() && i + currentLine >= specialPos.get(nextSpecialPos + 1).startPosition) {
							nextView = findViewByPosition(specialPos.get(nextSpecialPos + 1).startPosition - 1);
						} else {
							nextView = findViewByPosition(i + currentLine);
						}
						break;
					}
				}
			} else if (focusDirection == View.FOCUS_UP) {
				if (fromPos < getSpanCount()) {
					return focused;
				}
				for (int i = fromPos; i >= nextPos; i--) {
					if (specialPos != null && specialPos.contains(new SpecialPos(i))) {
						int nextSpecialPos = specialPos.indexOf(new SpecialPos(i));
						int lastColumnCount;
						if (nextSpecialPos > 0) {
							lastColumnCount = (i - specialPos.get(nextSpecialPos - 1).startPosition - 1) % getSpanCount();
						} else {
							return focused;
						}
						if (lastColumnCount == 0) {
							lastColumnCount = getSpanCount();
						}
						int currentLine = fromPos - i;
						if (currentLine > lastColumnCount) {
							nextView = findViewByPosition(i - 1);
						} else {
							nextView = findViewByPosition(i - (lastColumnCount - currentLine) - 1);
						}
						break;
					}
				}
			}
			if (nextView instanceof TextView) {
				nextView = findViewByPosition(nextPos + 1);
			}
		/*modify by dragontec for bug 4482 start*/
			if (nextView == null && focusDirection == View.FOCUS_RIGHT) {
				RecyclerView.Adapter adapter = ((RecyclerView) focused.getParent()).getAdapter();
				if (adapter instanceof HistoryFavoriteListAdapter) {
					((HistoryFavoriteListAdapter) adapter).setBindingViewRequestFocusPosition(nextPos);
					((RecyclerView) focused.getParent()).smoothScrollToPosition(nextPos);
				} else {
					nextView = focused;
				}
			}
			if (nextView == null && focusDirection == View.FOCUS_LEFT) {
				RecyclerView.Adapter adapter = ((RecyclerView) focused.getParent()).getAdapter();
				if (adapter instanceof HistoryFavoriteListAdapter) {
					((HistoryFavoriteListAdapter) adapter).setBindingViewRequestFocusPosition(nextPos);
					((RecyclerView) focused.getParent()).smoothScrollToPosition(nextPos);
				}
			}
		/*modify by dragontec for bug 4482 end*/
			if (nextView == null && focusDirection == View.FOCUS_DOWN) {
				nextView = focused;
				YoYo.with(Techniques.VerticalShake).duration(1000).playOn(nextView);
			}
			return nextView;
		}
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return super.scrollVerticallyBy(dy, recycler, state);
    }

    protected int getFavoriteNextViewPos(int fromPos, int direction) {
        int offset = 0;
        int spanCount = getDefaultSpanCount();
        int orientation = getOrientation();
        if (orientation == VERTICAL) {
            switch (direction) {
                case View.FOCUS_DOWN:
                    offset = spanCount;
                    break;
                case View.FOCUS_UP:
                    offset = -spanCount;
                    break;
                case View.FOCUS_RIGHT:
                    offset = 1;
                    break;
                case View.FOCUS_LEFT:
                    offset = -1;
                    break;
                default:
                    break;
            }
        } else if (orientation == HORIZONTAL) {
            switch (direction) {
                case View.FOCUS_DOWN:
                    offset = 1;
                    break;
                case View.FOCUS_UP:
                    offset = -1;
                    break;
                case View.FOCUS_RIGHT:
                    offset = spanCount;
                    break;
                case View.FOCUS_LEFT:
                    offset = -spanCount;
                    break;
                default:
                    break;
            }
        }
        return fromPos + offset;
    }

    /**
     * Calculates position offset.
     *
     *
     * @param fromPos
     * @param direction regular {@code View.FOCUS_*}.
     * @return position according to {@code direction}.
     */
    protected int getFliterNextViewPos(int fromPos, int direction) {
        int spanCount = getDefaultSpanCount();
        int fromIndex = 0;
        if(specialPos == null){
            return -1;
        }
        for (int i = 0; i < specialPos.size(); i++) {
            if(fromPos < specialPos.get(i).endPosition){
                fromIndex = i;
                break;
            }
        }
        int count = specialPos.get(fromIndex).endPosition - specialPos.get(fromIndex).startPosition + 1;
        int sectionStart = fromPos - specialPos.get(fromIndex).startPosition;
        int sectionFinalLinePos = sectionStart%spanCount;
        sectionFinalLinePos = sectionFinalLinePos == 0?spanCount:sectionFinalLinePos;
        switch (direction) {
            case View.FOCUS_DOWN:
                if((fromPos + spanCount) > specialPos.get(fromIndex).endPosition){
                    if((fromPos + spanCount) > specialPos.get(specialPos.size() - 1).endPosition){
                        //已经是最后一行
                        return -1;
                    }else{
                        if(sectionStart < count - sectionFinalLinePos){
                            //此section在fromPos后还有一行，定位到该section最后一个item
                            return specialPos.get(fromIndex).endPosition;
                        }else{
                            //定位到后一个section的相应位置
                            int lineStart = sectionFinalLinePos - (count -sectionStart) ;
                            int nextCount = specialPos.get(fromIndex + 1).endPosition - specialPos.get(fromIndex + 1).startPosition;
                            if(lineStart > nextCount -1){
                                //后一个section在相应位置没有item，定位到该section最后一个
                                return specialPos.get(fromIndex + 1).endPosition;
                            }else{
                                return specialPos.get(fromIndex + 1).startPosition + lineStart;
                            }
                        }
                    }
                }else{
                    return fromPos + spanCount;
                }
            case View.FOCUS_UP:
                if((fromPos - spanCount) < specialPos.get(fromIndex).startPosition){
                    if(fromPos - spanCount < 0){
                        //已经是第一行
                        return -1;
                    }else{
                        int lastSectionCount = specialPos.get(fromIndex - 1).endPosition - specialPos.get(fromIndex - 1).startPosition;
                        int lastSectionFinalLineCount = lastSectionCount%spanCount;
                        if(sectionStart > lastSectionFinalLineCount){
                            //前一个section在相应位置没有item，定位到该section最后一个
                            return specialPos.get(fromIndex - 1).endPosition;
                        }else{
                            return lastSectionCount - lastSectionFinalLineCount + sectionStart;
                        }
                    }
                }else{
                    return fromPos - spanCount;
                }
            case View.FOCUS_RIGHT: {
                int targetPos = fromPos + 1;
                if (targetPos > specialPos.get(specialPos.size() - 1).endPosition) {
                    targetPos = specialPos.get(specialPos.size() - 1).endPosition;
                }
                return targetPos;
            }
            case View.FOCUS_LEFT: {
                int targetPos = fromPos - 1;
                if (targetPos < 0) {
                    targetPos = 0;
                }
                return targetPos;
            }
            default:
                break;
        }
        return 0;
    }

    /**
     * Checks if we hit borders.
     *
     * @param from from what position.
     * @param offset offset to new position.
     * @return {@code true} if we hit border.
     */
    private boolean hitBorder(int from, int offset) {
        int spanCount = getSpanCount();

        if (offset== -1) {
            int spanIndex = from % spanCount;
            int newSpanIndex = spanIndex + offset;
            return newSpanIndex < 0 || newSpanIndex >= spanCount;
        } else {
            int newPos = from + offset;
            return newPos < 0 && newPos >= spanCount;
        }
    }

    public boolean isCanScroll() {
        return canScroll;
    }

    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    private boolean canScroll=false;
    @Override
    public boolean canScrollVertically() {
        return canScroll;
    }

    public void setSpecialPos(ArrayList<SpecialPos> specialPos) {
        this.specialPos = specialPos;
    }

    public ArrayList<SpecialPos> getSpecialPos() {
        return this.specialPos;
    }

    public void setLeftFocusView(View leftFocusView) {
        this.leftFocusView = leftFocusView;
    }

    public int getDefaultSpanCount(){
        return spanCount;
    }
}
