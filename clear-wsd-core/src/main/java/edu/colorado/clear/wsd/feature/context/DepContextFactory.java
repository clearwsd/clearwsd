package edu.colorado.clear.wsd.feature.context;

import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DepTree;
import edu.colorado.clear.wsd.type.NlpFocus;

/**
 * Context factory over dependency trees.
 *
 * @author jamesgung
 */
public abstract class DepContextFactory implements NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode> {

    private static final long serialVersionUID = -3387961962284032456L;

}
