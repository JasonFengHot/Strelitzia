package tv.ismar.subject.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
//import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.ismar.app.widget.RecyclerImageView;
import tv.ismar.subject.R;

/**
 * Created by liucan on 2017/3/15.
 */

public class SportViewHolder extends RecyclerView.ViewHolder {
    public TextView isalive;
    public TextView start_time_ym;
    public TextView start_time;
    public TextView big_away_name;
    public TextView big_home_name;
    public RecyclerImageView home_logo;
    public RecyclerImageView away_loga;
    public RecyclerImageView big_away_logo;
    public RecyclerImageView big_home_logo;
    public RelativeLayout nomarl;
    public RelativeLayout focus_tobig;
    public TextView away_name;
    public TextView home_name;
    public TextView big_time;
    public TextView big_home,big_away;
    public LinearLayout start_time_layout;

    public SportViewHolder(View itemView) {
        super(itemView);
        isalive= (TextView) itemView.findViewById(R.id.isalive);
        start_time= (TextView) itemView.findViewById(R.id.start_time);
        start_time_ym= (TextView) itemView.findViewById(R.id.start_time_ym);
        away_name= (TextView) itemView.findViewById(R.id.away_name);
        away_loga= (RecyclerImageView) itemView.findViewById(R.id.away_logo);
        home_logo= (RecyclerImageView) itemView.findViewById(R.id.home_logo);
        home_name= (TextView) itemView.findViewById(R.id.home_name);
        nomarl= (RelativeLayout) itemView.findViewById(R.id.nomarl);
        focus_tobig= (RelativeLayout) itemView.findViewById(R.id.focus_tobig);
        big_away_logo= (RecyclerImageView) itemView.findViewById(R.id.big_away_logo);
        big_away_name= (TextView) itemView.findViewById(R.id.big_away_name);
        big_home_logo= (RecyclerImageView) itemView.findViewById(R.id.big_home_logo);
        big_home_name= (TextView) itemView.findViewById(R.id.big_home_name);
        big_time= (TextView) itemView.findViewById(R.id.big_time);
        start_time_layout= (LinearLayout) itemView.findViewById(R.id.start_time_layout);
        big_home= (TextView) itemView.findViewById(R.id.big_home);
        big_away= (TextView) itemView.findViewById(R.id.big_away);
    }
}
