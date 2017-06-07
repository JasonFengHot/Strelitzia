package tv.ismar.player.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import tv.ismar.player.R;

/**
 * Created by liucan on 2017/5/26.
 */

public class MenuItemHoder extends RecyclerView.ViewHolder{
    TextView textView;
    LinearLayout line;
    LinearLayout focus_line;
    TextView focus_text;
    public MenuItemHoder(View itemView) {
        super(itemView);
        textView= (TextView) itemView.findViewById(R.id.resolution_text);
        line= (LinearLayout) itemView.findViewById(R.id.line);
        focus_line= (LinearLayout) itemView.findViewById(R.id.focus_line);
        focus_text= (TextView) itemView.findViewById(R.id.focus_text);
    }
}
