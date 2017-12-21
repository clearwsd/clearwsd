package io.github.clearwsd;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpFocus;
import io.github.clearwsd.classifier.Classifier;
import io.github.clearwsd.feature.annotator.Annotator;
import io.github.clearwsd.type.DefaultNlpFocus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Annotates a dependency tree with word sense annotations. Identifies word sense candidates using a target annotator,
 * and classifies each target word. This can be used to, for example, filter out auxiliary verbs as candidates for sense annotation.
 * Applies {@link FeatureType#Sense} annotations by default, but this is configurable.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class WordSenseAnnotator implements Annotator<DepTree> {

    private static final long serialVersionUID = 1756016409763214122L;

    private final Classifier<NlpFocus<DepNode, DepTree>, String> classifier;
    private final Annotator<DepTree> targetAnnotator;

    @Setter
    private String annotationType = FeatureType.Sense.name();

    /**
     * Initialize an {@link WordSenseAnnotator} with a given sense {@link Classifier} and {@link Annotator} used to identify
     * candidates for sense disambiguation.
     *
     * @param classifier      word sense classifier
     * @param targetAnnotator predicate identifier/annotator
     */
    public WordSenseAnnotator(Classifier<NlpFocus<DepNode, DepTree>, String> classifier,
                              Annotator<DepTree> targetAnnotator) {
        this.classifier = classifier;
        this.targetAnnotator = targetAnnotator;
    }

    @Override
    public DepTree annotate(DepTree instance) {
        // apply annotator
        instance = targetAnnotator.annotate(instance);
        // classify each resulting instance
        for (DepNode token : instance) {
            String predicate = token.feature(FeatureType.Predicate);
            if (predicate != null) {
                NlpFocus<DepNode, DepTree> input = new DefaultNlpFocus<>(token.index(), token, instance);
                token.addFeature(annotationType, classifier.classify(input));
            }
        }
        return instance;
    }

    @Override
    public boolean initialized() {
        return targetAnnotator.initialized();
    }

}
