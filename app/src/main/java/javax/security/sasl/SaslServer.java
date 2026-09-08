package javax.security.sasl;

/**
 * Performs SASL authentication as a server.
 */
public interface SaslServer {

    /**
     * Returns the IANA-registered mechanism name of this SASL server.
     *
     * @return A non-null string representing the IANA-registered mechanism name.
     */
    String getMechanismName();

    /**
     * Evaluates the response data and generates a challenge.
     *
     * @param response The non-null response sent from the client.
     * @return The possibly null challenge to send to the client.
     * @throws SaslException If an error occurred while processing the response.
     */
    byte[] evaluateResponse(byte[] response) throws SaslException;

    /**
     * Determines whether the authentication exchange has completed.
     *
     * @return true if the authentication exchange has completed; false otherwise.
     */
    boolean isComplete();

    /**
     * Reports the authorization ID in effect for the client of this session.
     *
     * @return The authorization ID of the client.
     */
    String getAuthorizationID();

    /**
     * Unwraps a byte array received from the client.
     *
     * @param incoming The non-null byte array containing the encoded bytes from the client.
     * @param offset The starting position at incoming of the bytes to read.
     * @param len The number of bytes from incoming to read.
     * @return A non-null byte array containing the unwrapped bytes.
     * @throws SaslException if incoming cannot be successfully unwrapped.
     */
    byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException;

    /**
     * Wraps a byte array to be sent to the client.
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
     * Disposes of any system resources or security-sensitive information the SaslServer might be using.
     *
     * @throws SaslException If a problem was encountered while disposing the resources.
     */
    void dispose() throws SaslException;
}
