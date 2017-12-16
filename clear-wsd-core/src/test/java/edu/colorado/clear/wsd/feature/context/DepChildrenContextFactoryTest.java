package edu.colorado.clear.wsd.feature.context;

import com.google.common.collect.Sets;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;

import edu.colorado.clear.wsd.feature.TestInstanceBuilder;
import edu.colorado.clear.wsd.type.DefaultNlpFocus;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DepTree;
import edu.colorado.clear.wsd.type.FeatureType;

import static junit.framework.TestCase.assertEquals;

/**
 * @author jamesgung
 */
public class DepChildrenContextFactoryTest {

    private DefaultNlpFocus<DepNode, DepTree> getTestInstance() {
        return new TestInstanceBuilder("the fox jumped over the fence", 2)
                .addHead(0, 1, "det")
                .addHead(1, 2, "nsubj")
                .addHead(3, 5, "prep")
                .addHead(4, 5, "det")
                .addHead(5, 2, "nmod")
                .root(2)
                .build();
    }

    @Test
    public void testAll() {
        DepChildrenContextFactory factory = new DepChildrenContextFactory();
        List<NlpContext<DepNode>> contexts = factory.apply(getTestInstance());
        assertEquals(2, contexts.size());
        assertEquals("fox", contexts.get(0).tokens().get(0).feature(FeatureType.Text));
        assertEquals(DepChildrenContextFactory.KEY + "[0]", contexts.get(0).identifier());
        assertEquals("fence", contexts.get(1).tokens().get(0).feature(FeatureType.Text));
    }

    @Test
    public void testExclude() {
        DepChildrenContextFactory factory = new DepChildrenContextFactory(Sets.newHashSet("nsubj"), new HashSet<>());
        List<NlpContext<DepNode>> contexts = factory.apply(getTestInstance());
        assertEquals(1, contexts.size());
        assertEquals("fence", contexts.get(0).tokens().get(0).feature(FeatureType.Text));
    }

    @Test
    public void testInclude() {
        DepChildrenContextFactory factory = new DepChildrenContextFactory(new HashSet<>(), Sets.newHashSet("nsubj"));
        List<NlpContext<DepNode>> contexts = factory.apply(getTestInstance());
        assertEquals(1, contexts.size());
        assertEquals("fox", contexts.get(0).tokens().get(0).feature(FeatureType.Text));
    }


}