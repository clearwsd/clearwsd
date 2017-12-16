package edu.colorado.clear.wsd.type;

/**
 * Dependency tree data structure used in feature extraction.
 *
 * @author jamesgung
 */
public interface DepTree extends NlpSequence<DepNode> {

    /**
     * Return the syntactic root of this dependency tree.
     */
    DepNode root();

}
