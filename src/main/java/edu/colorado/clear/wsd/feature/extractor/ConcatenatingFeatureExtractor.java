package edu.colorado.clear.wsd.feature.extractor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.feature.util.FeatureUtils;
import edu.colorado.clear.wsd.type.NlpInstance;

/**
 * Feature extractor concatenating the results of multiple different extractors. Concatenates resulting features.
 *
 * @author jamesgung
 */
public class ConcatenatingFeatureExtractor<T extends NlpInstance> extends NlpFeatureExtractor<T, String> {

    private static final long serialVersionUID = 2179984341749253937L;

    private List<FeatureExtractor<T, String>> extractors;

    public ConcatenatingFeatureExtractor(List<FeatureExtractor<T, String>> extractors) {
        this.extractors = extractors;
        id = extractors.stream().map(FeatureExtractor::id).collect(Collectors.joining(FeatureUtils.KEY_DELIM));
    }

    @SafeVarargs
    public ConcatenatingFeatureExtractor(FeatureExtractor<T, String>... extractors) {
        this(Arrays.asList(extractors));
    }

    @Override
    public String extract(T instance) {
        return String.join(FeatureUtils.CONCAT_DELIM, extractors.stream()
                .map(e -> e.extract(instance))
                .collect(Collectors.toList()));
    }

}
