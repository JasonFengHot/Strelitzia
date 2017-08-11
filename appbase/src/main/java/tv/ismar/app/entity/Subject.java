package tv.ismar.app.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by liucan on 2017/3/20.
 */

public class Subject implements Serializable{
   public String bg_url;
    public int count;
    public String title;
    public boolean is_buy;
    public String description;
    public String content_model;
    public ArrayList<Objects> objects;
}
