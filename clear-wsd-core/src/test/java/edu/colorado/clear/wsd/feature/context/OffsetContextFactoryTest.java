package edu.colorado.clear.wsd.feature.context;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.colorado.clear.wsd.feature.TestInstanceBuilder;
import edu.colorado.clear.wsd.type.DefaultNlpFocus;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DepTree;

import static junit.framework.TestCase.assertEquals;

/**
 * @author jamesgung
 */
public class OffsetContextFactoryTest {

    private DefaultNlpFocus<DepNode, DepTree> getTestInstance() {
        return new TestInstanceBuilder("0 1 2 3 4 5 6", 3).build();
    }

    @Test
    public void testContextFocus() {
        OffsetContextFactory factory = new OffsetContextFactory(Collections.singletonList(0));
        List<NlpContext<DepNode>> contexts = factory.apply(getTestInstance());
        assertEquals(1, contexts.size());
        assertEquals(1, contexts.get(0).tokens().size());
        assertEquals(3, contexts.get(0).tokens().get(0).index());
        assertEquals(OffsetContextFactory.KEY + "[0]", contexts.get(0).identifier());
    }

    @Test
    public void testContextSeparate() {
        OffsetContextFactory factory = new OffsetContextFactory(Arrays.asList(-1, 1));
        List<NlpContext<DepNode>> contexts = factory.apply(getTestInstance());
        assertEquals(2, contexts.size());
        assertEquals(1, contexts.get(0).tokens().size());
        assertEquals(OffsetContextFactory.KEY + "[-1]", contexts.get(0).identifier());
        assertEquals(OffsetContextFactory.KEY + "[1]", contexts.get(1).identifier());
        assertEquals(1, contexts.get(1).tokens().size());
        assertEquals(2, contexts.get(0).tokens().get(0).index());
        assertEquals(4, contexts.get(1).tokens().get(0).index());
    }

    @Test
    public void testContextConcatenated() {
        OffsetContextFactory factory = new OffsetContextFactory(Arrays.asList(-1, 1), true);
        List<NlpContext<DepNode>> contexts = factory.apply(getTestInstance());
        assertEquals(1, contexts.size());
        assertEquals(OffsetContextFactory.KEY + "[-1,1]", contexts.get(0).identifier());
        assertEquals(2, contexts.get(0).tokens().size());
        assertEquals(2, contexts.get(0).tokens().get(0).index());
        assertEquals(4, contexts.get(0).tokens().get(1).index());
    }

}