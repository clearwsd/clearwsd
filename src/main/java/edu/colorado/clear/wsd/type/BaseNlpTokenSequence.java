package edu.colorado.clear.wsd.type;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Default NLP sequence implementation.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class BaseNlpTokenSequence<T extends NlpInstance> extends BaseNlpInstance implements NlpTokenSequence<T> {

    private List<T> tokens;

    public BaseNlpTokenSequence(int index, List<T> tokens) {
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

    @Override
    public String toString() {
        return super.toString() + "\n" + tokens.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }
}
