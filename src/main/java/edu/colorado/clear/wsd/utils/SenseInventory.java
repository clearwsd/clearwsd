package edu.colorado.clear.wsd.utils;

import java.util.Set;

/**
 * Sense inventory mapping word lemmas onto lists of senses.
 *
 * @author jamesgung
 */
public interface SenseInventory {

    /**
     * Return all labels associated with a particular word form.
     *
     * @param lemma word base form
     * @return set of labels/senses associated with the input word
     */
    Set<String> senses(String lemma);

    /**
     * Returns the most frequent sense/default sense associated with a particular input.
     *
     * @param lemma word base form
     * @return default sense for input word
     */
    String defaultSense(String lemma);

    /**
     * Add a sense to the sense inventory.
     *
     * @param lemma base word
     * @param sense sense of word
     */
    void addSense(String lemma, String sense);

}
