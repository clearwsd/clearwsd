package edu.colorado.clear.wsd.type;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import edu.colorado.clear.type.NlpInstance;
import edu.colorado.clear.type.NlpSequence;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Default {@link NlpSequence} implementation.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class DefaultNlpSequence<T extends NlpInstance> extends DefaultNlpInstance implements NlpSequence<T> {

    private List<T> tokens;

    public DefaultNlpSequence(int index, List<T> tokens) {
        super(index);
        this.tokens = tokens;
    }

    @Override
    public T get(int index) {
        return tokens.get(index);
    }

    @Override
    public int size() {
        return tokens.size();
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return tokens.iterator();
    }

    @Override
    public String toString() {
        return tokens.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }
}
