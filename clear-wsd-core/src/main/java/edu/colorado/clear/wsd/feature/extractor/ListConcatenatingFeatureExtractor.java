package edu.colorado.clear.wsd.feature.extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.feature.util.FeatureUtils;
import edu.colorado.clear.wsd.type.NlpInstance;

/**
 * Feature extractor concatenating the results of multiple different extractors, the base extractor extracting multiple strings as
 * features.
 *
 * @author jamesgung
 */
public class ListConcatenatingFeatureExtractor<T extends NlpInstance> extends NlpFeatureExtractor<T, List<String>> {

    private static final long serialVersionUID = -8997941881477825340L;

    private FeatureExtractor<T, List<String>> baseExtractor;
    private List<FeatureExtractor<T, String>> extractors;

    public ListConcatenatingFeatureExtractor(FeatureExtractor<T, List<String>> baseExtractor,
                                             List<FeatureExtractor<T, String>> extractors) {
        this.baseExtractor = baseExtractor;
        this.extractors = extractors;
        id = baseExtractor.id() + FeatureUtils.KEY_DELIM
                + extractors.stream().map(FeatureExtractor::id).collect(Collectors.joining(FeatureUtils.KEY_DELIM));
    }

    @SafeVarargs
    public ListConcatenatingFeatureExtractor(FeatureExtractor<T, List<String>> baseExtractor,
                                             FeatureExtractor<T, String>... extractors) {
        this(baseExtractor, Arrays.asList(extractors));
    }

    @Override
    public List<String> extract(T instance) {
        List<String> results = new ArrayList<>();
        for (String result : baseExtractor.extract(instance)) {
            results.add(result + FeatureUtils.CONCAT_DELIM + String.join(FeatureUtils.CONCAT_DELIM, extractors.stream()
                    .map(e -> e.extract(instance))
                    .collect(Collectors.toList())));
        }
        return results;
    }

}
