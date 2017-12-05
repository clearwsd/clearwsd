package edu.colorado.clear.wsd.feature.annotator;

import edu.colorado.clear.wsd.type.NlpInstance;
import edu.colorado.clear.wsd.type.NlpTokenSequence;

/**
 * Annotates tokens in a sequence with lists.
 *
 * @author jamesgung
 */
public class DepNodeListAnnotator<T extends NlpInstance, S extends NlpTokenSequence<T>> extends ResourceAnnotator<T, S> {

    private static final long serialVersionUID = 7456297953368403608L;

    public DepNodeListAnnotator(String resourceKey) {
        super(resourceKey);
    }

    @Override
    public S annotate(S instance) {
        for (T token : instance.tokens()) {
            token.addFeature(resourceKey, resource.lookup(token));
        }
        return instance;
    }

}
