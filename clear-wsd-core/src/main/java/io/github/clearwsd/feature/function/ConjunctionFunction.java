package io.github.clearwsd.feature.function;

import java.util.ArrayList;
import java.util.List;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.StringFeature;
import io.github.clearwsd.feature.util.FeatureUtils;
import lombok.AllArgsConstructor;

/**
 * Conjoin the results of two feature functions.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class ConjunctionFunction<InputT extends NlpInstance> implements FeatureFunction<InputT> {

    private static final long serialVersionUID = 218606640626375039L;

    private FeatureFunction<InputT> first;
    private FeatureFunction<InputT> second;

    @Override
    public List<StringFeature> apply(InputT input) {
        List<StringFeature> results = new ArrayList<>();
        for (StringFeature first : first.apply(input)) {
            for (StringFeature second : second.apply(input)) {
                if (first.equals(second)) {
                    continue;
                }
                results.add(new StringFeature(first.id() + FeatureUtils.CONCAT_DELIM + second.id(),
                        first.value() + FeatureUtils.CONCAT_DELIM + second.value()));
            }
        }
        return results;
    }

}
