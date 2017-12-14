package edu.colorado.clear.wsd.utils;

import java.io.Serializable;
import java.util.Set;

/**
 * WordNet-based sense inventory.
 *
 * @author jamesgung
 */
public class WordNetSenseInventory implements SenseInventory, Serializable {

    private static final long serialVersionUID = -5075422456841090098L;

    private transient WordNetFacade wordNet;

    @Override
    public Set<String> senses(String lemma) {
        if (wordNet == null) {
            wordNet = new ExtJwnlWordNet();
        }
        return wordNet.senses(lemma, "VB");
    }

    @Override
    public String defaultSense(String lemma) {
        if (wordNet == null) {
            wordNet = new ExtJwnlWordNet();
        }
        return wordNet.mfs(lemma, "VB").orElse("");
    }

    @Override
    public void addSense(String lemma, String sense) {
        // pass, do not update sense inventory
    }

}
