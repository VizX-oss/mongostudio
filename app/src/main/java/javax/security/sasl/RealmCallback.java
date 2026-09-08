package javax.security.sasl;

import javax.security.auth.callback.TextInputCallback;

/**
 * This callback is used by {@code SaslClient} and {@code SaslServer}
 * to retrieve realm information.
 */
public class RealmCallback extends TextInputCallback {

    private static final long serialVersionUID = -8571360186277634907L;

    public RealmCallback(String prompt) {
        super(prompt);
    }

    public RealmCallback(String prompt, String defaultRealmInfo) {
        super(prompt, defaultRealmInfo);
    }
}
