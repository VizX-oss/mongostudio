package javax.security.sasl;

import javax.security.auth.callback.ChoiceCallback;

/**
 * This callback is used by {@code SaslClient} and {@code SaslServer}
 * to obtain a realm given a list of realm choices.
 */
public class RealmChoiceCallback extends ChoiceCallback {

    private static final long serialVersionUID = -8591801047796336426L;

    public RealmChoiceCallback(String prompt, String[] choices, int defaultChoice, boolean multipleSelectionsAllowed) {
        super(prompt, choices, defaultChoice, multipleSelectionsAllowed);
    }
}
