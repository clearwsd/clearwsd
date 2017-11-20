package edu.colorado.clear.wsd.type;

import java.util.List;

/**
 * Node in a dependency tree.
 *
 * @author jamesgung
 */
public interface DepNode extends NlpInstance {

    /**
     * True if this dependency node is the root of a dependency tree.
     */
    boolean isRoot();

    /**
     * Dependency relation (to head) associated with this node.
     */
    String dep();

    /**
     * Syntactic head of this dependency node.
     */
    DepNode head();

    /**
     * Syntactic children of this dependency node.
     */
    List<DepNode> children();

}
