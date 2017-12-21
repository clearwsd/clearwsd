package io.github.clearwsd.feature.annotator;

import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpFocus;
import io.github.clearwsd.feature.TestInstanceBuilder;
import io.github.clearwsd.feature.extractor.LookupFeatureExtractor;
import io.github.clearwsd.feature.resource.DefaultFeatureResourceManager;
import io.github.clearwsd.feature.resource.DefaultTsvResourceInitializer;

import static junit.framework.TestCase.assertEquals;

/**
 * @author jamesgung
 */
public class ListAnnotatorTest {

    private NlpFocus<DepNode, DepTree> getTestInstance() {
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
    public void testAnnotate() throws MalformedURLException {
        DefaultTsvResourceInitializer<String> testResource = new DefaultTsvResourceInitializer<>(
                "testResource", new File("src/test/resources/test_resource.tsv").toURI().toURL());
        ListAnnotator<DepNode, NlpFocus<DepNode, DepTree>> annotator = new ListAnnotator<>(
                "testResource", new LookupFeatureExtractor<DepNode>(FeatureType.Text.name()));
        annotator.initialize(new DefaultFeatureResourceManager().registerInitializer("testResource", testResource));
        NlpFocus<DepNode, DepTree> annotated = annotator.annotate(getTestInstance());
        assertEquals(Collections.singletonList("noun"), annotated.get(1).feature("testResource"));
        assertEquals(Collections.singletonList("verb"), annotated.get(2).feature("testResource"));
        assertEquals(Collections.singletonList("noun"), annotated.get(5).feature("testResource"));
    }

}