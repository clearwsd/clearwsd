package io.github.clearwsd.verbnet;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.feature.annotator.Annotator;
import io.github.clearwsd.feature.util.DepUtils;
import io.github.clearwsd.feature.util.PosUtils;
import io.github.clearwsd.utils.LemmaDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static io.github.clearwsd.type.FeatureType.Dep;
import static io.github.clearwsd.type.FeatureType.Pos;
import static io.github.clearwsd.type.FeatureType.Predicate;

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
            if (PosUtils.isVerb(token.feature(Pos)) && !DepUtils.isAux(token.feature(Dep))) {
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
