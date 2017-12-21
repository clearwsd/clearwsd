package io.github.clearwsd.type;

/**
 * Syntactic dependency tree data structure used in feature extraction.
 *
 * @author jamesgung
 */
public interface DepTree extends NlpSequence<DepNode> {

    /**
     * Return the syntactic root of this dependency tree.
     */
    DepNode root();

}
