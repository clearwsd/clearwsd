package edu.colorodo.clear.wsd.feature.function;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.colorodo.clear.wsd.feature.StringFeature;
import edu.colorodo.clear.wsd.type.NlpInstance;
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

    @JsonProperty
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
