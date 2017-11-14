package edu.colorodo.clear.wsd.type;

/**
 * Dependency tree data structure used in feature extraction.
 *
 * @author jamesgung
 */
public interface DependencyTree extends NlpTokenSequence<DepNode> {

    /**
     * Return the syntactic root of this dependency tree.
     */
    DepNode root();

}
