package io.github.clearwsd.feature.annotator;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.type.NlpSequence;
import io.github.clearwsd.feature.context.NlpContext;
import io.github.clearwsd.feature.context.NlpContextFactory;
import io.github.clearwsd.feature.context.SequenceIdentifyContextFactory;

/**
 * Annotates tokens in a sequence with lists.
 *
 * @author jamesgung
 */
public class DepNodeListAnnotator<T extends NlpInstance, S extends NlpSequence<T>> extends ResourceAnnotator<T, S> {

    private static final long serialVersionUID = 7456297953368403608L;

    private NlpContextFactory<S, T> contextFactory;

    public DepNodeListAnnotator(String resourceKey, NlpContextFactory<S, T> contextFactory) {
        super(resourceKey);
        this.contextFactory = contextFactory;
    }

    public DepNodeListAnnotator(String resourceKey) {
        this(resourceKey, new SequenceIdentifyContextFactory<>());
    }

    @Override
    public S annotate(S instance) {
        for (NlpContext<T> context : contextFactory.apply(instance)) {
            for (T token : context.tokens()) {
                token.addFeature(resourceKey, resource.lookup(token));
            }
        }
        return instance;
    }

}
