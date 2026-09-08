package javax.security.sasl;

import java.util.Map;
import javax.security.auth.callback.CallbackHandler;

/**
 * An interface for creating instances of {@code SaslServer}.
 */
public interface SaslServerFactory {

    /**
     * Creates a SaslServer for the specified mechanism.
     */
    SaslServer createSaslServer(
            String mechanism,
            String protocol,
            String serverName,
            Map<String, ?> props,
            CallbackHandler cbh) throws SaslException;

    /**
     * Returns an array of names of mechanisms that match the specified mechanism selection policies.
     */
    String[] getMechanismNames(Map<String, ?> props);
}
