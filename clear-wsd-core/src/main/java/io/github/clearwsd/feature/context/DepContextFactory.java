package io.github.clearwsd.feature.context;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.NlpFocus;

/**
 * Context factory over dependency trees.
 *
 * @author jamesgung
 */
public abstract class DepContextFactory implements NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode> {

    private static final long serialVersionUID = -3387961962284032456L;

}
