package edu.colorado.clear.wsd.feature.extractor;

import java.util.Map;

import edu.colorado.clear.wsd.type.NlpInstance;

/**
 * Extractor that maps the values output by a base extractor into pre-defined categories.
 *
 * @author jamesgung
 */
public class MappingExtractor<T extends NlpInstance> extends NlpFeatureExtractor<T, String> {

    private static final long serialVersionUID = -1364779820794809734L;

    private FeatureExtractor<T, String> baseExtractor;
    private Map<String, String> stringMap;

    public MappingExtractor(FeatureExtractor<T, String> baseExtractor, Map<String, String> stringMap) {
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
