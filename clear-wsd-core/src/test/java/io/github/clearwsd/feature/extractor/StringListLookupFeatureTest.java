package io.github.clearwsd.feature.extractor;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.type.DefaultDepNode;

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