package edu.colorado.clear.wsd.corpus;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import edu.colorado.clear.type.DepTree;
import edu.colorado.clear.type.FeatureType;

import static org.junit.Assert.assertEquals;

/**
 * {@link CoNllDepTreeReader} tests.
 *
 * @author jamesgung
 */
public class CoNllDepTreeReaderTest {

    private static final String TEST_PATH = "src/test/resources/test.dep";

    @Test
    public void testReadInstances() throws IOException {
        List<DepTree> depTrees = new CoNllDepTreeReader().readInstances(new FileInputStream(TEST_PATH));
        assertEquals(2, depTrees.size());
        assertEquals(16, depTrees.get(0).size());
        assertEquals(17, depTrees.get(1).size());
        assertEquals("stop", depTrees.get(0).root().feature(FeatureType.Text));
        assertEquals("calling", depTrees.get(1).root().feature(FeatureType.Text));
    }

}