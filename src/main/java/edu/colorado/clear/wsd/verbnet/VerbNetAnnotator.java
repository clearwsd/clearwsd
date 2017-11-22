package edu.colorado.clear.wsd.verbnet;

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
 * Annotates a dependency tree with VerbNet class annotations. Identifies predicates, and classifies each predicate to a VerbNet
 * class.
 *
 * @author jamesgung
 */
@Getter
@Setter
@Accessors(fluent = true)
public class VerbNetAnnotator implements Annotator<DependencyTree> {

    private static final long serialVersionUID = 1756016409763214122L;

    private Classifier<FocusInstance<DepNode, DependencyTree>, String> classifier;
    private Annotator<DependencyTree> predicateAnnotator;
    private String annotationType = FeatureType.Sense.name();

    public VerbNetAnnotator(Classifier<FocusInstance<DepNode, DependencyTree>, String> classifier,
                            Annotator<DependencyTree> predicateAnnotator) {
        this.classifier = classifier;
        this.predicateAnnotator = predicateAnnotator;
    }

    @Override
    public DependencyTree annotate(DependencyTree instance) {
        instance = predicateAnnotator.annotate(instance);
        for (DepNode token : instance.tokens()) {
            String predicate = token.feature(FeatureType.Predicate);
            if (predicate != null) {
                FocusInstance<DepNode, DependencyTree> input = new FocusInstance<>(token.index(), token, instance);
                token.addFeature(annotationType, classifier.classify(input));
            }
        }
        return instance;
    }

}
