package edu.colorado.clear.wsd.feature.context;

import org.junit.Test;

import java.util.List;

import edu.colorado.clear.wsd.feature.TestInstanceBuilder;
import edu.colorado.clear.wsd.type.DefaultNlpFocus;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DepTree;

import static junit.framework.TestCase.assertEquals;

/**
 * @author jamesgung
 */
public class RootPathContextFactoryTest {

    private DefaultNlpFocus<DepNode, DepTree> getTestInstance() {
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