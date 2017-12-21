package io.github.clearwsd.feature.context;

import org.junit.Test;

import java.util.List;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.NlpFocus;
import io.github.clearwsd.feature.TestInstanceBuilder;

import static junit.framework.TestCase.assertEquals;

/**
 * @author jamesgung
 */
public class RootPathContextFactoryTest {

    private NlpFocus<DepNode, DepTree> getTestInstance() {
        return new TestInstanceBuilder("0 1 2 3", 0)
                .addHead(0, 1, "det")
                .addHead(1, 2, "nsubj")
                .addHead(2, 3, "prep")
                .root(3)
                .build();
    }

    @Test
    public void testAll() {
        RootPathContextFactory factory = new RootPathContextFactory(true, -1);
        List<NlpContext<DepNode>> contexts = factory.apply(getTestInstance());
        assertEquals(1, contexts.size());
        assertEquals(4, contexts.get(0).tokens().size());
        assertEquals(3, contexts.get(0).tokens().get(3).index());
    }


}