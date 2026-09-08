package javax.security.sasl;

import java.io.Serializable;
import javax.security.auth.callback.Callback;

/**
 * This callback is used by {@code SaslServer} to determine whether one entity
 * (identified by an authenticated authentication id) can act on
 * behalf of another entity (identified by an authorization id).
 */
public class AuthorizeCallback implements Callback, Serializable {

    private static final long serialVersionUID = -2353344186490470805L;

    private String authnID;
    private String authzID;
    private String authorizedID;
    private boolean authorized;

    public AuthorizeCallback(String authnID, String authzID) {
        this.authnID = authnID;
        this.authzID = authzID;
    }

    public String getAuthenticationID() {
        return authnID;
    }

    public String getAuthorizationID() {
        return authzID;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean ok) {
        this.authorized = ok;
    }

    public String getAuthorizedID() {
        if (!authorized) {
            return null;
        }
        return (authorizedID == null) ? authzID : authorizedID;
    }

    public void setAuthorizedID(String id) {
        this.authorizedID = id;
    }
}
