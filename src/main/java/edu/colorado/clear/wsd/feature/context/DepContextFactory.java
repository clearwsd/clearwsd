package edu.colorado.clear.wsd.feature.context;

import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.FocusInstance;
import edu.colorado.clear.wsd.type.DependencyTree;

/**
 * Context factory over dependency trees.
 *
 * @author jamesgung
 */
public abstract class DepContextFactory implements NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode> {


}
