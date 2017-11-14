package edu.colorodo.clear.wsd.feature.extractor;

import java.util.List;
import java.util.stream.Collectors;

import edu.colorodo.clear.wsd.type.NlpInstance;

import static edu.colorodo.clear.wsd.feature.util.FeatureUtils.CONCAT_DELIM;
import static edu.colorodo.clear.wsd.feature.util.FeatureUtils.KEY_DELIM;

/**
 * Feature extractor concatenating the results of multiple different extractors. Concatenates resulting features.
 *
 * @author jamesgung
 */
public class AggregateFeatureExtractor<T extends NlpInstance> extends NlpFeatureExtractor<T> {

    private List<FeatureExtractor<T>> extractors;

    public AggregateFeatureExtractor(List<FeatureExtractor<T>> extractors) {
        this.extractors = extractors;
        id = extractors.stream().map(FeatureExtractor::id).collect(Collectors.joining(KEY_DELIM));
    }

    @Override
    public String extract(T instance) {
        return String.join(CONCAT_DELIM, extractors.stream()
                .map(e -> e.extract(instance))
                .collect(Collectors.toList()));
    }

}
