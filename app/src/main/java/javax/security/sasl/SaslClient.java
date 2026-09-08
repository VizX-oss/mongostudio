package javax.security.sasl;

/**
 * Performs SASL authentication as a client.
 * Provided for Android environments where javax.security.sasl is omitted from the platform SDK.
 */
public interface SaslClient {

    /**
     * Returns the IANA-registered mechanism name of this SASL client.
     * (e.g. "CRAM-MD5", "GSSAPI", "SCRAM-SHA-256").
     *
     * @return A non-null string representing the IANA-registered mechanism name.
     */
    String getMechanismName();

    /**
     * Determines whether this mechanism has an optional initial response.
     *
     * @return true if this mechanism has an initial response.
     */
    boolean hasInitialResponse();

    /**
     * Evaluates the challenge data and generates a response.
     *
     * @param challenge The non-null challenge sent from the server.
     * @return The possibly null response to send to the server.
     * @throws SaslException If an error occurred while processing the challenge.
     */
    byte[] evaluateChallenge(byte[] challenge) throws SaslException;

    /**
     * Determines whether the authentication exchange has completed.
     *
     * @return true if the authentication exchange has completed; false otherwise.
     */
    boolean isComplete();

    /**
     * Unwraps a byte array received from the server.
     *
     * @param incoming The non-null byte array containing the encoded bytes from the server.
     * @param offset The starting position at incoming of the bytes to read.
     * @param len The number of bytes from incoming to read.
     * @return A non-null byte array containing the unwrapped bytes.
     * @throws SaslException if incoming cannot be successfully unwrapped.
     */
    byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException;

    /**
     * Wraps a byte array to be sent to the server.
     *
     * @param outgoing The non-null byte array containing the bytes to encode.
     * @param offset The starting position at outgoing of the bytes to read.
     * @param len The number of bytes from outgoing to read.
     * @return A non-null byte array containing the wrapped bytes.
     * @throws SaslException if outgoing cannot be successfully wrapped.
     */
    byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException;

    /**
     * Retrieves the negotiated property.
     *
     * @param propName The non-null property name.
     * @return The value of the negotiated property. If null, the property was not negotiated.
     */
    Object getNegotiatedProperty(String propName);

    /**
     * Disposes of any system resources or security-sensitive information the SaslClient might be using.
     *
     * @throws SaslException If a problem was encountered while disposing the resources.
     */
    void dispose() throws SaslException;
}
