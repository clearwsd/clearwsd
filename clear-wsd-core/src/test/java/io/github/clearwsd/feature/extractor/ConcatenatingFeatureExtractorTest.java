package io.github.clearwsd.feature.extractor;

import org.junit.Test;

import java.util.Arrays;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.feature.util.FeatureUtils;
import io.github.clearwsd.type.DefaultDepNode;

import static junit.framework.TestCase.assertEquals;

/**
 * @author jamesgung
 */
public class ConcatenatingFeatureExtractorTest {

    @Test
    public void testConcatenate() {
        ConcatenatingFeatureExtractor<DepNode> extractor = new ConcatenatingFeatureExtractor<>(Arrays.asList(
                new LookupFeatureExtractor<>(FeatureType.Pos.name()),
                new LookupFeatureExtractor<>(FeatureType.Dep.name())));
        DefaultDepNode depNode = new DefaultDepNode(0);
        depNode.addFeature(FeatureType.Pos, "NN");
        depNode.addFeature(FeatureType.Dep, "nsubj");
        assertEquals(String.format("NN%snsubj", FeatureUtils.CONCAT_DELIM), extractor.extract(depNode));
    }

}