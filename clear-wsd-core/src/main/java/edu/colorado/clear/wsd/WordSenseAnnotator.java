package edu.colorado.clear.wsd;

import edu.colorado.clear.wsd.classifier.Classifier;
import edu.colorado.clear.wsd.feature.annotator.Annotator;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.FocusInstance;
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
public class WordSenseAnnotator implements Annotator<DependencyTree> {

    private static final long serialVersionUID = 1756016409763214122L;

    private final Classifier<FocusInstance<DepNode, DependencyTree>, String> classifier;
    private final Annotator<DependencyTree> targetAnnotator;

    @Setter
    private String annotationType = FeatureType.Sense.name();

    /**
     * Initialize an {@link WordSenseAnnotator} with a given sense {@link Classifier} and {@link Annotator} used to identify
     * candidates for sense disambiguation.
     *
     * @param classifier      word sense classifier
     * @param targetAnnotator predicate identifier/annotator
     */
    public WordSenseAnnotator(Classifier<FocusInstance<DepNode, DependencyTree>, String> classifier,
                              Annotator<DependencyTree> targetAnnotator) {
        this.classifier = classifier;
        this.targetAnnotator = targetAnnotator;
    }

    @Override
    public DependencyTree annotate(DependencyTree instance) {
        // apply annotator
        instance = targetAnnotator.annotate(instance);
        // classify each resulting instance
        for (DepNode token : instance.tokens()) {
            String predicate = token.feature(FeatureType.Predicate);
            if (predicate != null) {
                FocusInstance<DepNode, DependencyTree> input = new FocusInstance<>(token.index(), token, instance);
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
