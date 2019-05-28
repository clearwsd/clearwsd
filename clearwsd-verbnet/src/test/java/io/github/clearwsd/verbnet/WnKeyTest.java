package io.github.clearwsd.verbnet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * {@link WnKey} unit tests.
 *
 * @author jgung
 */
public class WnKeyTest {

    @Test
    public void testParseWordNetKey() {
        String payKey = "pay%2:40:00::";
        WnKey wnKey = WnKey.parseWordNetKey(payKey).orElseThrow(IllegalArgumentException::new);
        assertEquals(WnKey.SynsetType.VERB, wnKey.type());
        assertEquals(40, wnKey.lexicalFileNumber());
        assertEquals(0, wnKey.lexicalId());
    }

    @Test
    public void testToString() {
        String payKey = "pay%2:40:00::";
        assertEquals(payKey, WnKey.toSenseKey(WnKey.parseWordNetKey(payKey).orElseThrow(IllegalArgumentException::new)));
    }

}