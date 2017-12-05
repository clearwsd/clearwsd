package edu.colorado.clear.wsd.feature.annotator;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import edu.colorado.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorado.clear.wsd.feature.resource.FeatureResource;
import edu.colorado.clear.wsd.feature.resource.FeatureResourceManager;
import edu.colorado.clear.wsd.type.NlpInstance;
import edu.colorado.clear.wsd.type.NlpTokenSequence;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Annotates tokens in a sequence with lists.
 *
 * @author jamesgung
 */
@Getter
@AllArgsConstructor
public class DepNodeListAnnotator<T extends NlpInstance, S extends NlpTokenSequence<T>> implements Annotator<S> {

    private static final long serialVersionUID = 7456297953368403608L;
    @JsonProperty
    private FeatureExtractor<T, String> baseExtractor;
    @JsonProperty
    private String resourceKey;

    private FeatureResource<T, List<String>> resource;

    public DepNodeListAnnotator(FeatureExtractor<T, String> baseExtractor, String resourceKey) {
        this.baseExtractor = baseExtractor;
        this.resourceKey = resourceKey;
    }

    @Override
    public S annotate(S instance) {
        for (T token : instance.tokens()) {
            token.addFeature(resourceKey, resource.lookup(token));
        }
        return instance;
    }

    @Override
    public void initialize(FeatureResourceManager featureResourceManager) {
        this.resource = featureResourceManager.getResource(resourceKey);
    }
}
