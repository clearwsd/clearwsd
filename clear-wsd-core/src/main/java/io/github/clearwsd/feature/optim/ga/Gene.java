package io.github.clearwsd.feature.optim.ga;

/**
 * Gene in chromosome with specific mutation behavior.
 *
 * @author jamesgung
 */
public interface Gene {

    /**
     * Mutates this gene.
     */
    void mutate();

    /**
     * Make a copy of this gene.
     */
    Gene copy();

}
