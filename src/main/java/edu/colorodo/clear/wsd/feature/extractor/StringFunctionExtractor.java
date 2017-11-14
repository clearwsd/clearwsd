package edu.colorodo.clear.wsd.feature.extractor;

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
public class StringFunctionExtractor<T extends NlpInstance> extends NlpFeatureExtractor<T> {

    private FeatureExtractor<T> baseExtractor;
    private List<StringFunction> stringFunctions;

    public StringFunctionExtractor(FeatureExtractor<T> baseExtractor, List<StringFunction> stringFunctions) {
        this.baseExtractor = baseExtractor;
        this.stringFunctions = stringFunctions;
        id = baseExtractor.id() + KEY_DELIM + this.stringFunctions.stream()
                .map(StringFunction::id)
                .collect(Collectors.joining(KEY_DELIM));
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
