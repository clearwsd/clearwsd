package io.github.clearwsd.feature.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Default/base feature model implementation.
 *
 * @author jamesgung
 */
@Setter
@Getter
@Accessors(fluent = true)
@AllArgsConstructor
@NoArgsConstructor
public class BaseFeatureModel implements FeatureModel {

    private static final long serialVersionUID = 8326546437890576186L;

    private Vocabulary labels;
    private Vocabulary features;

    @Override
    public String label(int index) {
        return labels.value(index);
    }

    @Override
    public Integer labelIndex(String label) {
        return labels.index(label);
    }

    @Override
    public String feature(int index) {
        return features.value(index);
    }

    @Override
    public Integer featureIndex(String feature) {
        return features.index(feature);
    }

}
