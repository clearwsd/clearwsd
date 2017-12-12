package edu.colorado.clear.wsd.feature.extractor;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.BaseDepNode;
import edu.colorado.clear.wsd.type.DepNode;

import static junit.framework.TestCase.assertEquals;

/**
 * @author jamesgung
 */
public class LookupFeatureExtractorTest {

    @Test
    public void testLookup() {
        LookupFeatureExtractor<DepNode> lookupExtractor = new LookupFeatureExtractor<>(FeatureType.Text.name());
        BaseDepNode depNode = new BaseDepNode(0);
        depNode.addFeature(FeatureType.Text, "cat");
        String feat = lookupExtractor.extract(depNode);
        assertEquals("cat", feat);
    }

    @Test
    public void testFallback() {
        LookupFeatureExtractor<DepNode> lookupExtractor = new LookupFeatureExtractor<>(
                Collections.singletonList(FeatureType.Text.name()), new LookupFeatureExtractor<DepNode>(FeatureType.Lemma.name()));
        BaseDepNode depNode = new BaseDepNode(0);
        depNode.addFeature(FeatureType.Lemma, "cat");
        String feat = lookupExtractor.extract(depNode);
        assertEquals("cat", feat);
    }

    @Test
    public void testMultiple() {
        LookupFeatureExtractor<DepNode> lookupExtractor = new LookupFeatureExtractor<>(
                Arrays.asList(FeatureType.Text.name(), FeatureType.Lemma.name(), FeatureType.Pos.name()));
        BaseDepNode depNode = new BaseDepNode(0);
        depNode.addFeature(FeatureType.Lemma, "cat");
        depNode.addFeature(FeatureType.Pos, "NN");
        String feat = lookupExtractor.extract(depNode);
        assertEquals("cat", feat); // extract the value for the first key present
    }

}