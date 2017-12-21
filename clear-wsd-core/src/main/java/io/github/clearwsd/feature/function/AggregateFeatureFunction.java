package io.github.clearwsd.feature.function;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.StringFeature;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Aggregation of multiple feature functions.
 *
 * @author jamesgung
 */
@NoArgsConstructor
@AllArgsConstructor
public class AggregateFeatureFunction<InputT extends NlpInstance> implements FeatureFunction<InputT> {

    private static final long serialVersionUID = 7273553475535366584L;

    private List<FeatureFunction<InputT>> functions = new ArrayList<>();

    public AggregateFeatureFunction<InputT> add(FeatureFunction<InputT> function) {
        functions.add(function);
        return this;
    }

    @Override
    public List<StringFeature> apply(InputT input) {
        return functions.stream()
                .map(f -> f.apply(input))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

}
