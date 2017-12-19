package edu.colorado.clear.wsd.models;

import org.junit.Test;

import edu.colorado.clear.wsd.WordSenseClassifier;

import static junit.framework.TestCase.assertNotNull;

/**
 * Tests for loading serialized models.
 *
 * @author jamesgung
 */
public class ModelLoadingTest {

    private static final String SEMLINK_PATH = "models/verbnet/semlink.bin";

    @Test
    public void testSemlink() {
        WordSenseClassifier classifier = WordSenseClassifier.load(this.getClass().getClassLoader().getResource(SEMLINK_PATH));
        assertNotNull(classifier);
    }

}
