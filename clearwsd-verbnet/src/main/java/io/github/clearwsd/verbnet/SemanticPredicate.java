package io.github.clearwsd.verbnet;

import java.util.List;

/**
 * VerbNet semantic predicate.
 *
 * @author jgung
 */
public interface SemanticPredicate {

    String bool();

    String value();

    List<SemanticArgument> semanticArguments();

}
