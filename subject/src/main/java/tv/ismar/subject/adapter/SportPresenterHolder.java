package tv.ismar.subject.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.ismar.subject.R;

public class SportPresenterHolder {

    public TextView isalive;
    public TextView start_time_ym;
    public TextView start_time;
    public TextView big_away_name;
    public TextView big_home_name;
    public ImageView home_logo;
    public ImageView away_loga;
    public ImageView big_away_logo;
    public ImageView big_home_logo;
    public RelativeLayout nomarl;
    public RelativeLayout focus_tobig;
    public TextView away_name;
    public TextView home_name;
    public TextView big_time;
    public TextView big_home,big_away;
    public LinearLayout start_time_layout;

    public SportPresenterHolder(View itemView) {
        isalive= (TextView) itemView.findViewById(R.id.isalive);
        start_time= (TextView) itemView.findViewById(R.id.start_time);
        start_time_ym= (TextView) itemView.findViewById(R.id.start_time_ym);
        away_name= (TextView) itemView.findViewById(R.id.away_name);
        away_loga= (ImageView) itemView.findViewById(R.id.away_logo);
        home_logo= (ImageView) itemView.findViewById(R.id.home_logo);
        home_name= (TextView) itemView.findViewById(R.id.home_name);
        nomarl= (RelativeLayout) itemView.findViewById(R.id.nomarl);
        focus_tobig= (RelativeLayout) itemView.findViewById(R.id.focus_tobig);
        big_away_logo= (ImageView) itemView.findViewById(R.id.big_away_logo);
        big_away_name= (TextView) itemView.findViewById(R.id.big_away_name);
        big_home_logo= (ImageView) itemView.findViewById(R.id.big_home_logo);
        big_home_name= (TextView) itemView.findViewById(R.id.big_home_name);
        big_time= (TextView) itemView.findViewById(R.id.big_time);
        start_time_layout= (LinearLayout) itemView.findViewById(R.id.start_time_layout);
        big_home= (TextView) itemView.findViewById(R.id.big_home);
        big_away= (TextView) itemView.findViewById(R.id.big_away);
    }

}
