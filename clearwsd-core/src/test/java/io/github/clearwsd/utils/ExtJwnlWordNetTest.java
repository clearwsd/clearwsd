package io.github.clearwsd.utils;

import org.junit.Test;

import java.util.Set;

import static junit.framework.TestCase.assertEquals;

/**
 * {@link ExtJwnlWordNet} tests.
 *
 * @author jamesgung
 */
public class ExtJwnlWordNetTest {

    private final ExtJwnlWordNet wordNet = new ExtJwnlWordNet();

    @Test(timeout = 1_000)
    public void lookup$IgnoreLongTokens() {
        Set<String> senses = wordNet.senses(
                "href=\"https://very.long.url.local/with/many/sub/parts/ext-jwnl-will/try/to-lemmatize-and-cause-a"
                        + "-combinatorial-explosion.pdf\"", "NN");
        assertEquals(0, senses.size());
    }

    @Test(timeout = 1_000)
    public void lookup$HyphenatedWord() {
        Set<String> senses = wordNet.senses("air-conditioner", "NN");
        assertEquals(1, senses.size());
    }

}