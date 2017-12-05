package edu.colorado.clear.wsd.feature.annotator;

import edu.colorado.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorado.clear.wsd.feature.resource.FeatureResourceManager;
import edu.colorado.clear.wsd.type.NlpInstance;
import edu.colorado.clear.wsd.type.NlpTokenSequence;

/**
 * Annotates tokens in a sequence with lists.
 *
 * @author jamesgung
 */
public class ListAnnotator<T extends NlpInstance, S extends NlpTokenSequence<T>> extends ResourceAnnotator<String, S> {

    private static final long serialVersionUID = -6170529305032382231L;

    private FeatureExtractor<T, String> baseExtractor;

    public ListAnnotator(String resourceKey, FeatureExtractor<T, String> baseExtractor) {
        super(resourceKey);
        this.baseExtractor = baseExtractor;
    }

    @Override
    public S annotate(S instance) {
        for (T token : instance.tokens()) {
            String key = baseExtractor.extract(token);
            token.addFeature(resourceKey, resource.lookup(key));
        }
        return instance;
    }

    @Override
    public void initialize(FeatureResourceManager featureResourceManager) {
        this.resource = featureResourceManager.getResource(resourceKey);
    }

}
