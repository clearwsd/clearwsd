package io.github.clearwsd.models;

import org.junit.Test;

import io.github.clearwsd.WordSenseClassifier;

import static junit.framework.TestCase.assertNotNull;

/**
 * Tests for loading serialized models.
 *
 * @author jamesgung
 */
public class ModelLoadingTest {

    private static final String SEMLINK_PATH = "models/semlink.bin";
    private static final String ONTONOTES_PATH = "models/ontonotes.bin";
    private static final String SEMCOR_PATH = "models/semcor.bin";

    @Test
    public void testSemlink() {
        WordSenseClassifier classifier = WordSenseClassifier.load(this.getClass().getClassLoader().getResource(SEMLINK_PATH));
        assertNotNull(classifier);
    }

    @Test
    public void testOntoNotes() {
        WordSenseClassifier classifier = WordSenseClassifier.load(this.getClass().getClassLoader().getResource(ONTONOTES_PATH));
        assertNotNull(classifier);
    }

    @Test
    public void testSemcor() {
        WordSenseClassifier classifier = WordSenseClassifier.load(this.getClass().getClassLoader().getResource(SEMCOR_PATH));
        assertNotNull(classifier);
    }

}
