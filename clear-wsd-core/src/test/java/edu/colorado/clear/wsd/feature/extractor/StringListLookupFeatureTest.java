package edu.colorado.clear.wsd.feature.extractor;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import edu.colorado.clear.wsd.type.DefaultDepNode;
import edu.colorado.clear.wsd.type.NlpInstance;

import static junit.framework.TestCase.assertEquals;

/**
 * @author jamesgung
 */
public class StringListLookupFeatureTest {

    @Test
    public void testStringListLookup() {
        ListLookupFeatureExtractor<NlpInstance> listFeature = new ListLookupFeatureExtractor<>("test");
        DefaultDepNode depNode = new DefaultDepNode(0);
        depNode.addFeature("test", Arrays.asList("one", "two"));
        List<String> result = listFeature.extract(depNode);
        assertEquals(2, result.size());
        assertEquals("one", result.get(0));
        assertEquals("two", result.get(1));
    }

}