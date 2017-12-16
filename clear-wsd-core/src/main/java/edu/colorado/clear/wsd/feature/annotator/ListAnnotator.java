package edu.colorado.clear.wsd.feature.annotator;

import edu.colorado.clear.wsd.feature.context.NlpContext;
import edu.colorado.clear.wsd.feature.context.NlpContextFactory;
import edu.colorado.clear.wsd.feature.context.SequenceIdentifyContextFactory;
import edu.colorado.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorado.clear.wsd.feature.resource.FeatureResourceManager;
import edu.colorado.clear.wsd.type.NlpInstance;
import edu.colorado.clear.wsd.type.NlpSequence;

/**
 * Annotates tokens in a sequence with lists.
 *
 * @author jamesgung
 */
public class ListAnnotator<T extends NlpInstance, S extends NlpSequence<T>> extends ResourceAnnotator<String, S> {

    private static final long serialVersionUID = -6170529305032382231L;

    private FeatureExtractor<T, String> baseExtractor;
    private NlpContextFactory<S, T> contextFactory;

    public ListAnnotator(String resourceKey,
                         FeatureExtractor<T, String> baseExtractor,
                         NlpContextFactory<S, T> contextFactory) {
        super(resourceKey);
        this.baseExtractor = baseExtractor;
        this.contextFactory = contextFactory;
    }

    public ListAnnotator(String resourceKey, FeatureExtractor<T, String> baseExtractor) {
        this(resourceKey, baseExtractor, new SequenceIdentifyContextFactory<>());
        this.baseExtractor = baseExtractor;
    }

    @Override
    public S annotate(S instance) {
        for (NlpContext<T> context : contextFactory.apply(instance)) {
            for (T token : context.tokens()) {
                String key = baseExtractor.extract(token);
                token.addFeature(resourceKey, resource.lookup(key));
            }
        }
        return instance;
    }

    @Override
    public void initialize(FeatureResourceManager featureResourceManager) {
        this.resource = featureResourceManager.getResource(resourceKey);
    }

}
