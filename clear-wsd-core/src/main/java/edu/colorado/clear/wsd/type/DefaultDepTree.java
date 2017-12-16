package edu.colorado.clear.wsd.type;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Default {@link DepTree} implementation.
 *
 * @author jamesgung
 */
@Accessors(fluent = true)
public class DefaultDepTree extends DefaultNlpSequence<DepNode> implements DepTree {

    @Getter
    @Setter
    private DepNode root;

    public DefaultDepTree(int index, List<DepNode> tokens, DepNode root) {
        super(index, tokens);
        this.root = root;
    }

}
