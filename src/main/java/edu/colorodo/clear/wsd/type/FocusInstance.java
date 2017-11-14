package edu.colorodo.clear.wsd.type;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Focus/sequence pair instance, such as for classification of a token within a larger sequence.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class FocusInstance<T extends NlpInstance, S extends NlpTokenSequence<T>> extends BaseNlpInstance {

    private T focus;
    private S sequence;

    public FocusInstance(int index, T focus, S sequence) {
        super(index);
        this.focus = focus;
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return focus.toString() + "\n\n" + sequence.toString();
    }
}
