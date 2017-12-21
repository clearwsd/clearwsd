package io.github.clearwsd.feature.function;

import java.util.Collections;
import java.util.List;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.StringFeature;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Bias feature function (always applied).
 *
 * @author jamesgung
 */
@NoArgsConstructor
@AllArgsConstructor
public class BiasFeatureFunction<T extends NlpInstance> implements FeatureFunction<T> {

    private static final String BIAS = "<BIAS>";

    private static final long serialVersionUID = -2067474355880710744L;

    private List<StringFeature> bias = Collections.singletonList(new StringFeature(BIAS, BIAS));

    @Override
    public List<StringFeature> apply(T input) {
        return bias;
    }
}
