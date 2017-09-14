package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liucan on 2017/9/13.
 */

public class Payments {
    private String source;

    private ArrayList<String> descriptions ;

    private String title;

    public void setSource(String source){
        this.source = source;
    }
    public String getSource(){
        return this.source;
    }
    public void setString(ArrayList<String> descriptions){
        this.descriptions = descriptions;
    }
    public ArrayList<String> getString(){
        return this.descriptions;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return this.title;
    }

}
