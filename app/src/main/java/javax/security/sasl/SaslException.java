package javax.security.sasl;

import java.io.IOException;

/**
 * This class represents an error that has occurred when using SASL.
 * Provided for Android environments where javax.security.sasl is omitted from the platform SDK.
 */
public class SaslException extends IOException {

    private static final long serialVersionUID = -8913930771463866183L;

    /**
     * The possibly null root cause exception.
     */
    private Throwable _exception;

    /**
     * Constructs a default instance of SaslException.
     */
    public SaslException() {
        super();
    }

    /**
     * Constructs an instance of SaslException with a detailed message.
     *
     * @param detail A possibly null string containing details of the exception.
     */
    public SaslException(String detail) {
        super(detail);
    }

    /**
     * Constructs an instance of SaslException with a detailed message and a root exception.
     *
     * @param detail A possibly null string containing details of the exception.
     * @param ex A possibly null root exception that caused this exception.
     */
    public SaslException(String detail, Throwable ex) {
        super(detail);
        if (ex != null) {
            initCause(ex);
        }
    }

    /**
     * Returns the cause of this throwable or null if the cause is nonexistent or unknown.
     */
    @Override
    public Throwable getCause() {
        return _exception;
    }

    /**
     * Initializes the cause of this throwable to the specified value.
     */
    @Override
    public Throwable initCause(Throwable cause) {
        super.initCause(cause);
        _exception = cause;
        return this;
    }

    /**
     * Returns the string representation of this exception.
     */
    @Override
    public String toString() {
        String answer = super.toString();
        if (_exception != null && _exception != this) {
            answer += " [Caused by " + _exception.toString() + "]";
        }
        return answer;
    }
}
