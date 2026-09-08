package javax.security.sasl;

/**
 * This exception is thrown by a SASL mechanism implementation to indicate that
 * the SASL exchange has failed due to reasons related to authentication.
 */
public class AuthenticationException extends SaslException {

    private static final long serialVersionUID = -3579708765382676738L;

    /**
     * Constructs a new instance of AuthenticationException.
     */
    public AuthenticationException() {
        super();
    }

    /**
     * Constructs a new instance of AuthenticationException with a detailed message.
     *
     * @param detail A possibly null string containing details of the exception.
     */
    public AuthenticationException(String detail) {
        super(detail);
    }

    /**
     * Constructs a new instance of AuthenticationException with a detailed message and a root exception.
     *
     * @param detail A possibly null string containing details of the exception.
     * @param ex A possibly null root exception that caused this exception.
     */
    public AuthenticationException(String detail, Throwable ex) {
        super(detail, ex);
    }
}
