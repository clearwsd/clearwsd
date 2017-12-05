package edu.colorado.clear.wsd.feature.context;

import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FocusInstance;

/**
 * Context factory over dependency trees.
 *
 * @author jamesgung
 */
abstract class DepContextFactory implements NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode> {

    private static final long serialVersionUID = -3387961962284032456L;

}
