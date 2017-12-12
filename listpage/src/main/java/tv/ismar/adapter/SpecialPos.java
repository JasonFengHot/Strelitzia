package tv.ismar.adapter;

/**
 * Created by zhaoji on 2017/11/17.
 */

public class SpecialPos {
    public int count = 0;
    public int startPosition = 0;
    public int endPosition = 0;
    public String sections = "";

    public SpecialPos(){
    }

    public SpecialPos(int startPosition){
        this.startPosition = startPosition;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof SpecialPos) {
            boolean isEquals = (startPosition == ((SpecialPos) o).startPosition);
            return isEquals;
        }else if(o instanceof Integer){
            boolean isEquals = (startPosition == (int)o);
            return isEquals;
        }
        return super.equals(o);
    }
}
