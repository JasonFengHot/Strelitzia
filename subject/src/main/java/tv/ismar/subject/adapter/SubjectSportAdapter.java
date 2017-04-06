package tv.ismar.subject.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import cn.ismartv.truetime.TrueTime;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.Objects;
import tv.ismar.subject.R;

/**
 * Created by liucan on 2017/3/15.
 */

public class SubjectSportAdapter extends RecyclerView.Adapter<SportViewHolder> {
    private ArrayList<Objects> itemList;
    private Context mContext;
    private OnItemFocusedListener mOnItemFocusedListener;
    private String TAG="subjectSportAdapter";
    private String type="NBA";
    private boolean isFirst=true;
    public SubjectSportAdapter(Context context){
        mContext=context;

    }
    public void setData(ArrayList<Objects> items,String type_sport){
        itemList=items;
        type=type_sport;
    }
    public void setOnItemFocusedListener(OnItemFocusedListener mOnItemFocusedListener) {
        this.mOnItemFocusedListener = mOnItemFocusedListener;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    SportViewHolder  lastview;
    int lastindex=0;
    @Override
    public SportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SportViewHolder holder=new SportViewHolder(LayoutInflater.from(mContext).inflate(R.layout.sport_list_item,parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final SportViewHolder holder, final int position) {
        Objects objects=itemList.get(position);
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                int pos =holder.getLayoutPosition();
                mOnItemFocusedListener.onItemfocused(v, pos, hasFocus);
                if (hasFocus) {
                    if (lastview != null&&lastindex!=pos) {
                        lastview.nomarl.setVisibility(View.VISIBLE);
                        lastview.focus_tobig.setVisibility(View.GONE);
                    }
                        holder.nomarl.setVisibility(View.GONE);
                        holder.focus_tobig.setVisibility(View.VISIBLE);


                } else {
                    lastview=holder;
                    lastindex=pos;
//                    Log.i("sportlist0","adapter false : "+lastindex);
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.requestFocus();
            }
        });
//        if(position==itemList.size()-1){
//            holder.itemView.setNextFocusDownId(holder.itemView.getId());
//        }
        if(type.equals("NBA")) {
            Picasso.with(mContext).load(objects.at_home_logo).into(holder.home_logo);
            Picasso.with(mContext).load(objects.at_home_logo).into(holder.big_home_logo);
            Picasso.with(mContext).load(objects.be_away_logo).into(holder.away_loga);
            Picasso.with(mContext).load(objects.be_away_logo).into(holder.big_away_logo);
            holder.away_name.setText(objects.be_away_name);
            holder.home_name.setText(objects.at_home_name);
        }else{
            holder.big_home.setText("(客)");
            holder.big_away.setText("(主)");
            Picasso.with(mContext).load(objects.at_home_logo).into(holder.away_loga);
            Picasso.with(mContext).load(objects.at_home_logo).into(holder.big_away_logo);
            Picasso.with(mContext).load(objects.be_away_logo).into(holder.home_logo);
            Picasso.with(mContext).load(objects.be_away_logo).into(holder.big_home_logo);
            holder.away_name.setText(objects.at_home_name);
            holder.home_name.setText(objects.be_away_name);
        }
        Boolean is_alive=videoIsStart(objects.start_time);
        Log.i("subject",is_alive+"");
        if(objects.start_time!=null) {
            if (is_alive) {
                holder.big_time.setText("直播中");
                holder.big_time.setTextColor(mContext.getResources().getColor(R.color._ff9c3c));
                holder.start_time_layout.setVisibility(View.GONE);
                holder.isalive.setText("直播中");
            } else {
                holder.isalive.setVisibility(View.GONE);
                holder.start_time_layout.setVisibility(View.VISIBLE);
                SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm");
                String time = formatter.format(objects.start_time);
                String times[] = time.split(" ");
                String month[]=times[0].split("-");
                holder.big_time.setText(month[0]+"月"+month[1]+"日"+" "+times[1]+" 未开始");
                holder.start_time_ym.setText(month[0]+"月"+month[1]+"日");
                holder.start_time.setText(times[1]);
            }
        }
        if(objects.recommend_status==1){
            holder.nomarl.setBackgroundResource(R.drawable.normal_game);
            holder.focus_tobig.setBackgroundResource(R.drawable.normal_game_focus);
        }else{
            holder.nomarl.setBackgroundResource(R.drawable.emphasis_game_normal);
            holder.focus_tobig.setBackgroundResource(R.drawable.emphasis_game_focus);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    private boolean videoIsStart(Date time) {
        if (time != null) {
            Calendar currentCalendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
            currentCalendar.setTime(TrueTime.now());
            Calendar startCalendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
            startCalendar.setTime(time);
            if (currentCalendar.after(startCalendar)) {
                if(currentCalendar.getTimeInMillis()-startCalendar.getTimeInMillis()<5400000){
                    return true;
                }else{
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
