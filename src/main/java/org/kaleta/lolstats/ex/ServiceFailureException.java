package org.kaleta.lolstats.ex;

/**
 * Created with IntelliJ IDEA.
 * User: Stanislav Kaleta
 * Date: 23.3.14
 * Modif: 17.7.14
 */
@Deprecated
public class ServiceFailureException extends RuntimeException {

    public ServiceFailureException() {
        super();
    }

    public ServiceFailureException(String msg) {
        super(msg);
    }

    public ServiceFailureException(Throwable cause) {
        super(cause);
    }

    public ServiceFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
