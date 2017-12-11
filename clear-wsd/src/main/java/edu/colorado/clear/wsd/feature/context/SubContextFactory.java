package edu.colorado.clear.wsd.feature.context;

import java.util.List;

import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FocusInstance;

/**
 * Apply nested context factories over a base context.
 *
 * @author jamesgung
 */
public class SubContextFactory extends DepContextFactory {

    private static final long serialVersionUID = -2602483077702771396L;

    private DepContextFactory baseContextFactory;
    private NlpContextFactory<List<NlpContext<DepNode>>, DepNode> nestedContextFactory;

    public SubContextFactory(DepContextFactory baseContextFactory,
                             NlpContextFactory<List<NlpContext<DepNode>>, DepNode> nestedContextFactory) {
        this.baseContextFactory = baseContextFactory;
        this.nestedContextFactory = nestedContextFactory;
    }

    @Override
    public List<NlpContext<DepNode>> apply(FocusInstance<DepNode, DependencyTree> instance) {
        return nestedContextFactory.apply(baseContextFactory.apply(instance));
    }
}
