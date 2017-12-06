package edu.colorado.clear.wsd.feature.annotator;

import edu.colorado.clear.wsd.feature.context.NlpContext;
import edu.colorado.clear.wsd.feature.context.NlpContextFactory;
import edu.colorado.clear.wsd.feature.context.SequenceIdentifyContextFactory;
import edu.colorado.clear.wsd.type.NlpInstance;
import edu.colorado.clear.wsd.type.NlpTokenSequence;

/**
 * Annotates tokens in a sequence with lists.
 *
 * @author jamesgung
 */
public class DepNodeListAnnotator<T extends NlpInstance, S extends NlpTokenSequence<T>> extends ResourceAnnotator<T, S> {

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
