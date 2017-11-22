package edu.colorado.clear.wsd.feature.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FocusInstance;

/**
 * Context factory returning the list of dependency nodes in the path to the root of a dependency tree.
 *
 * @author jamesgung
 */
public class RootPathContextFactory extends DepContextFactory {

    public static final String KEY = "PATH";

    private static final long serialVersionUID = 1084074273199439311L;

    @Override
    public List<NlpContext<DepNode>> apply(FocusInstance<DepNode, DependencyTree> instance) {
        return Collections.singletonList(new NlpContext<>(KEY, getRootPath(instance.focus())));
    }

    /**
     * Return the list of {@link DepNode} from the given node to the root of the dependency parse.
     *
     * @param depNode starting dependency node
     * @return list of dependency nodes in root path
     */
    private static List<DepNode> getRootPath(DepNode depNode) {
        List<DepNode> rootPath = new ArrayList<>();
        rootPath.add(depNode);
        while (!depNode.isRoot()) {
            rootPath.add(depNode.head());
            depNode = depNode.head();
        }
        return rootPath;
    }

}
