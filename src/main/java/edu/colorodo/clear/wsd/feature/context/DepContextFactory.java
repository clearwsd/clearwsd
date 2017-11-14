package edu.colorodo.clear.wsd.feature.context;

import edu.colorodo.clear.wsd.type.DepNode;
import edu.colorodo.clear.wsd.type.DependencyTree;
import edu.colorodo.clear.wsd.type.FocusInstance;

/**
 * Context factory over dependency trees.
 *
 * @author jamesgung
 */
public abstract class DepContextFactory implements NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode> {


}
