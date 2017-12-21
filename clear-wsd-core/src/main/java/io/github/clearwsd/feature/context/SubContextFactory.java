package io.github.clearwsd.feature.context;

import java.util.List;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.NlpFocus;

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
    public List<NlpContext<DepNode>> apply(NlpFocus<DepNode, DepTree> instance) {
        return nestedContextFactory.apply(baseContextFactory.apply(instance));
    }
}
