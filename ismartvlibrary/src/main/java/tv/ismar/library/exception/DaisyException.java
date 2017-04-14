package tv.ismar.library.exception;

public class DaisyException extends RuntimeException {

    private static final long serialVersionUID = -5500277806647175487L;

    private String url;

    public DaisyException() {
        super();
    }

    public DaisyException(String detailMessage) {
        super(detailMessage);
    }

    public DaisyException(String url, String detailMessage) {
        super(detailMessage);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
