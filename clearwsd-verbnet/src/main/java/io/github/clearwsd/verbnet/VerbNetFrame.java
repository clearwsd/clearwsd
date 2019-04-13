package io.github.clearwsd.verbnet;

import java.util.List;

/**
 * VerbNet class frame.
 *
 * @author jgung
 */
public interface VerbNetFrame {

    FrameDescription description();

    List<String> examples();

    List<SyntacticPhrase> syntax();

    List<SemanticPredicate> predicates();

}
