package edu.colorado.clear.wsd.type;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Focus/sequence pair instance, such as for classification of a token within a larger sequence.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class DefaultNlpFocus<T extends NlpInstance, S extends NlpSequence<T>> extends DefaultNlpInstance
        implements NlpFocus<T, S> {

    private T focus;
    private S sequence;

    public DefaultNlpFocus(int index, T focus, S sequence) {
        super(index);
        this.focus = focus;
        this.sequence = sequence;
    }

    @Override
    public List<T> tokens() {
        return sequence.tokens();
    }

    @Override
    public T get(int index) {
        return sequence.get(index);
    }

    @Override
    public int size() {
        return sequence.size();
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return tokens().iterator();
    }

    @Override
    public String toString() {
        return focus.toString() + "\n\n" + sequence.toString();
    }

}
