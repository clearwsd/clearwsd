package io.github.clearwsd.feature.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.type.NlpSequence;

/**
 * Context factory that returns the whole sequence of input tokens as a single context.
 *
 * @author jamesgung
 */
public class SequenceIdentifyContextFactory<S extends NlpSequence<T>, T extends NlpInstance>
        implements NlpContextFactory<S, T> {

    private static final long serialVersionUID = -6999855605864180292L;

    private static final String KEY = "I";

    @Override
    public List<NlpContext<T>> apply(S instance) {
        return Collections.singletonList(new NlpContext<>(KEY, new ArrayList<>(instance.tokens())));
    }

}
