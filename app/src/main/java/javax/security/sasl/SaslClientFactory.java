package javax.security.sasl;

import java.util.Map;
import javax.security.auth.callback.CallbackHandler;

/**
 * An interface for creating instances of {@code SaslClient}.
 */
public interface SaslClientFactory {

    /**
     * Creates a SaslClient using the parameters supplied.
     */
    SaslClient createSaslClient(
            String[] mechanisms,
            String authorizationId,
            String protocol,
            String serverName,
            Map<String, ?> props,
            CallbackHandler cbh) throws SaslException;

    /**
     * Returns an array of names of mechanisms that match the specified mechanism selection policies.
     */
    String[] getMechanismNames(Map<String, ?> props);
}
