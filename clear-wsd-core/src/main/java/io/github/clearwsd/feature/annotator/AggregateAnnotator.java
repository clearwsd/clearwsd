package io.github.clearwsd.feature.annotator;

import java.util.List;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.resource.FeatureResourceManager;
import lombok.AllArgsConstructor;

/**
 * Composite annotator (applies mulitple annotators sequentially).
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class AggregateAnnotator<S extends NlpInstance> implements Annotator<S> {

    private static final long serialVersionUID = -3218243259530880592L;

    private List<Annotator<S>> annotators;

    @Override
    public S annotate(S instance) {
        for (Annotator<S> annotator : annotators) {
            instance = annotator.annotate(instance);
        }
        return instance;
    }

    @Override
    public boolean initialized() {
        return annotators.stream().allMatch(Annotator::initialized);
    }

    @Override
    public void initialize(FeatureResourceManager featureResourceManager) {
        for (Annotator<S> annotator : annotators) {
            annotator.initialize(featureResourceManager);
        }
    }
}
