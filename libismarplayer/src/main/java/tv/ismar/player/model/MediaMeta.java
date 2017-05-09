package tv.ismar.player.model;


public class MediaMeta {

    private String[] urls;
    private int startPosition;
    private int player264Type;
    private int player265Type;

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public int getPlayer264Type() {
        return player264Type;
    }

    public void setPlayer264Type(int player264Type) {
        this.player264Type = player264Type;
    }

    public int getPlayer265Type() {
        return player265Type;
    }

    public void setPlayer265Type(int player265Type) {
        this.player265Type = player265Type;
    }
}
