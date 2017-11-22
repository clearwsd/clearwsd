package edu.colorado.clear.wsd.verbnet;

import edu.colorado.clear.wsd.feature.annotator.Annotator;
import edu.colorado.clear.wsd.feature.util.PosUtils;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static edu.colorado.clear.wsd.type.FeatureType.Pos;
import static edu.colorado.clear.wsd.type.FeatureType.Predicate;

/**
 * Default predicate annotator implementation. Uses heuristics based on POS-tags and dependency labels to determine whether or not a
 * verb is predicative.
 *
 * @author jamesgung
 */
@NoArgsConstructor
@AllArgsConstructor
public class DefaultPredicateAnnotator implements Annotator<DependencyTree> {

    private static final long serialVersionUID = -2953300591005876159L;

    @Getter
    @Setter
    private PredicateDictionary dictionary = new PredicateDictionary();

    @Override
    public DependencyTree annotate(DependencyTree instance) {
        for (DepNode token : instance.tokens()) {
            if (PosUtils.isVerb(token.feature(Pos))) {
                token.addFeature(Predicate, dictionary.apply(token));
            }
        }
        return instance;
    }

}
