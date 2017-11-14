package edu.colorodo.clear.wsd.type;

import java.util.List;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Dependency tree data structure used for feature processing.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class BaseDependencyTree extends BaseNlpTokenSequence<DepNode> implements DependencyTree {

    private DepNode root;

    public BaseDependencyTree(int index, List<DepNode> tokens, DepNode root) {
        super(index, tokens);
        this.root = root;
    }

}
