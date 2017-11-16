package edu.colorodo.clear.wsd.feature.annotator;

import java.util.List;

import edu.colorodo.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorodo.clear.wsd.feature.resource.FeatureResource;
import edu.colorodo.clear.wsd.type.NlpInstance;
import edu.colorodo.clear.wsd.type.NlpTokenSequence;
import lombok.AllArgsConstructor;

/**
 * Annotates tokens in a sequence with lists.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class ListAnnotator<T extends NlpInstance, S extends NlpTokenSequence<T>> implements Annotator<S> {

    private FeatureExtractor<T, String> baseExtractor;
    private FeatureResource<String, List<String>> listAnnotator;

    @Override
    public S annotate(S instance) {
        for (T token : instance.tokens()) {
            String key = baseExtractor.extract(token);
            token.addFeature(listAnnotator.key(), listAnnotator.lookup(key));
        }
        return instance;
    }

}
