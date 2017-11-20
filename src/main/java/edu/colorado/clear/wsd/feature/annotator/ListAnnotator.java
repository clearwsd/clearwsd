package edu.colorado.clear.wsd.feature.annotator;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import edu.colorado.clear.wsd.feature.resource.FeatureResource;
import edu.colorado.clear.wsd.type.NlpTokenSequence;
import edu.colorado.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorado.clear.wsd.feature.resource.FeatureResourceManager;
import edu.colorado.clear.wsd.type.NlpInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Annotates tokens in a sequence with lists.
 *
 * @author jamesgung
 */
@Getter
@AllArgsConstructor
public class ListAnnotator<T extends NlpInstance, S extends NlpTokenSequence<T>> implements Annotator<S> {

    private static final long serialVersionUID = -6170529305032382231L;
    @JsonProperty
    private FeatureExtractor<T, String> baseExtractor;
    @JsonProperty
    private String resourceKey;

    private FeatureResource<String, List<String>> resource;

    public ListAnnotator(FeatureExtractor<T, String> baseExtractor, String resourceKey) {
        this.baseExtractor = baseExtractor;
        this.resourceKey = resourceKey;
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
