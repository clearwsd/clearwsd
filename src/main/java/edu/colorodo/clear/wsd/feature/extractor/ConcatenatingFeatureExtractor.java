package edu.colorodo.clear.wsd.feature.extractor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

import edu.colorodo.clear.wsd.type.NlpInstance;
import lombok.Getter;
import lombok.experimental.Accessors;

import static edu.colorodo.clear.wsd.feature.util.FeatureUtils.CONCAT_DELIM;
import static edu.colorodo.clear.wsd.feature.util.FeatureUtils.KEY_DELIM;

/**
 * Feature extractor concatenating the results of multiple different extractors. Concatenates resulting features.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class ConcatenatingFeatureExtractor<T extends NlpInstance> extends NlpFeatureExtractor<T, String> {

    private static final long serialVersionUID = 2179984341749253937L;

    @JsonProperty
    private List<FeatureExtractor<T, String>> extractors;

    public ConcatenatingFeatureExtractor(@JsonProperty("extractors") List<FeatureExtractor<T, String>> extractors) {
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
