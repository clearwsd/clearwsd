package edu.colorado.clear.wsd.utils;

import java.io.Serializable;
import java.util.Set;

import edu.colorado.clear.wsd.feature.resource.ExtJwnlWordNetResource;

/**
 * WordNet-based sense inventory.
 *
 * @author jamesgung
 */
public class WordNetSenseInventory implements SenseInventory, Serializable {

    private static final long serialVersionUID = -5075422456841090098L;

    private transient ExtJwnlWordNetResource<?> resource;

    @Override
    public Set<String> senses(String lemma) {
        if (resource == null) {
            resource = new ExtJwnlWordNetResource<>();
        }
        return resource.senses(lemma, "VB");
    }

    @Override
    public String defaultSense(String lemma) {
        if (resource == null) {
            resource = new ExtJwnlWordNetResource<>();
        }
        String sense = resource.mostFrequentSense(lemma, "VB");
        return sense == null ? "" : sense;
    }

    @Override
    public void addSense(String lemma, String sense) {
        // pass, do not update sense inventory
    }

}
