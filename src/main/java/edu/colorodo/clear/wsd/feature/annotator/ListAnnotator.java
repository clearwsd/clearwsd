package edu.colorodo.clear.wsd.feature.annotator;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import edu.colorodo.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorodo.clear.wsd.feature.resource.FeatureResource;
import edu.colorodo.clear.wsd.type.NlpInstance;
import edu.colorodo.clear.wsd.type.NlpTokenSequence;
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

    @JsonProperty
    private FeatureExtractor<T, String> baseExtractor;
    @JsonProperty
    private FeatureResource<String, List<String>> resource;

    @Override
    public S annotate(S instance) {
        for (T token : instance.tokens()) {
            String key = baseExtractor.extract(token);
            token.addFeature(resource.key(), resource.lookup(key));
        }
        return instance;
    }

}
