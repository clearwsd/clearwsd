package edu.colorado.clear.wsd.feature.extractor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import edu.colorado.clear.type.NlpInstance;
import edu.colorado.clear.wsd.feature.extractor.string.StringFunction;

import static edu.colorado.clear.wsd.feature.util.FeatureUtils.KEY_DELIM;

/**
 * Extractor that performs a series of string functions to the output of a base extractor.
 *
 * @author jamesgung
 */
public class StringFunctionExtractor<T extends NlpInstance> implements StringExtractor<T> {

    private static final long serialVersionUID = -6703070360030390063L;

    private FeatureExtractor<T, String> baseExtractor;
    private List<StringFunction> stringFunctions;
    private String id;

    public StringFunctionExtractor(FeatureExtractor<T, String> baseExtractor, List<StringFunction> stringFunctions) {
        this.baseExtractor = baseExtractor;
        this.stringFunctions = stringFunctions;
        id = baseExtractor.id() + KEY_DELIM + this.stringFunctions.stream()
                .map(StringFunction::id)
                .collect(Collectors.joining(KEY_DELIM));
    }

    public StringFunctionExtractor(FeatureExtractor<T, String> baseExtractor, StringFunction stringFunction) {
        this(baseExtractor, Collections.singletonList(stringFunction));
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String extract(T instance) {
        String result = baseExtractor.extract(instance);
        for (StringFunction function : stringFunctions) {
            result = function.apply(result);
        }
        return result;
    }

}
