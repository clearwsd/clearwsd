package edu.colorado.clear.wsd.verbnet;

import edu.colorado.clear.type.DepNode;
import edu.colorado.clear.type.DepTree;
import edu.colorado.clear.wsd.feature.annotator.Annotator;
import edu.colorado.clear.wsd.feature.util.PosUtils;
import edu.colorado.clear.wsd.utils.LemmaDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static edu.colorado.clear.type.FeatureType.Pos;
import static edu.colorado.clear.type.FeatureType.Predicate;

/**
 * Default predicate annotator implementation. Uses heuristics based on POS-tags and dependency labels to determine whether or not a
 * verb is predicative.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class DefaultPredicateAnnotator implements Annotator<DepTree> {

    private static final long serialVersionUID = -2953300591005876159L;

    @Getter
    private final LemmaDictionary dictionary;

    @Override
    public DepTree annotate(DepTree instance) {
        for (DepNode token : instance) {
            if (PosUtils.isVerb(token.feature(Pos))) {
                token.addFeature(Predicate, dictionary.apply(token));
            }
        }
        return instance;
    }

    @Override
    public boolean initialized() {
        return true;
    }

}
