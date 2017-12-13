package edu.colorado.clear.wsd.feature.annotator;

import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;

import edu.colorado.clear.wsd.feature.TestInstanceBuilder;
import edu.colorado.clear.wsd.feature.extractor.LookupFeatureExtractor;
import edu.colorado.clear.wsd.feature.resource.DefaultFeatureResourceManager;
import edu.colorado.clear.wsd.feature.resource.DefaultTsvResourceInitializer;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.FocusInstance;

import static junit.framework.TestCase.assertEquals;

/**
 * @author jamesgung
 */
public class ListAnnotatorTest {

    private FocusInstance<DepNode, DependencyTree> getTestInstance() {
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
        ListAnnotator<DepNode, FocusInstance<DepNode, DependencyTree>> annotator = new ListAnnotator<>(
                "testResource", new LookupFeatureExtractor<DepNode>(FeatureType.Text.name()));
        annotator.initialize(new DefaultFeatureResourceManager().registerInitializer("testResource", testResource));
        FocusInstance<DepNode, DependencyTree> annotated = annotator.annotate(getTestInstance());
        assertEquals(Collections.singletonList("noun"), annotated.get(1).feature("testResource"));
        assertEquals(Collections.singletonList("verb"), annotated.get(2).feature("testResource"));
        assertEquals(Collections.singletonList("noun"), annotated.get(5).feature("testResource"));
    }

}