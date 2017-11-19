package edu.colorodo.clear.wsd.feature.extractor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import edu.colorodo.clear.wsd.feature.extractor.string.StringFunction;
import edu.colorodo.clear.wsd.type.NlpInstance;

import static edu.colorodo.clear.wsd.feature.util.FeatureUtils.KEY_DELIM;


/**
 * Extractor that performs a series of string functions to the output of a base extractor.
 *
 * @author jamesgung
 */
public class StringFunctionExtractor<T extends NlpInstance> extends NlpFeatureExtractor<T, String> {

    private static final long serialVersionUID = -6703070360030390063L;

    @JsonProperty
    private FeatureExtractor<T, String> baseExtractor;
    @JsonProperty
    private List<StringFunction> stringFunctions;

    @JsonCreator
    public StringFunctionExtractor(@JsonProperty("baseExtractor") FeatureExtractor<T, String> baseExtractor,
                                   @JsonProperty("stringFunctions") List<StringFunction> stringFunctions) {
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
    public String extract(T instance) {
        String result = baseExtractor.extract(instance);
        for (StringFunction function : stringFunctions) {
            result = function.apply(result);
        }
        return result;
    }

}
