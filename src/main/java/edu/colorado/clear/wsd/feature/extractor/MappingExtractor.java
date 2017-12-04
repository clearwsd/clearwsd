package edu.colorado.clear.wsd.feature.extractor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import edu.colorado.clear.wsd.type.NlpInstance;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Extractor that maps the values output by a base extractor into pre-defined categories.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class MappingExtractor<T extends NlpInstance> extends NlpFeatureExtractor<T, String> {

    private static final long serialVersionUID = -1364779820794809734L;

    @JsonProperty
    private FeatureExtractor<T, String> baseExtractor;
    @JsonProperty
    private Map<String, String> stringMap;

    @JsonCreator
    public MappingExtractor(@JsonProperty("baseExtractor") FeatureExtractor<T, String> baseExtractor,
                            @JsonProperty("stringMap") Map<String, String> stringMap) {
        this.id = baseExtractor.id();
        this.baseExtractor = baseExtractor;
        this.stringMap = stringMap;
    }

    @Override
    public String extract(T instance) {
        String input = baseExtractor.extract(instance);
        return stringMap.getOrDefault(input, input);
    }

}
