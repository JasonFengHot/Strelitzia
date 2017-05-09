package tv.ismar.library.exception;

/**
 * Created by beaver on 17-4-13.
 */

public class ParameterException extends DaisyException {

    private static final String message = "Parameters Or Variables NULL : ";

    public ParameterException(String where) {
        super(message + where);
    }

}
