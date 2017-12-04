package edu.colorado.clear.wsd.feature.context;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FocusInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Context factory returning the list of dependency nodes in the path to the root of a dependency tree.
 *
 * @author jamesgung
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RootPathContextFactory extends DepContextFactory {

    public static final String KEY = "PATH";

    private static final long serialVersionUID = 1084074273199439311L;

    @JsonProperty
    private boolean includeNode = false;
    @JsonProperty
    private int maxLength = -1;

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
    private List<DepNode> getRootPath(DepNode depNode) {
        List<DepNode> rootPath = new ArrayList<>();
        if (includeNode) {
            rootPath.add(depNode);
        }
        while (!depNode.isRoot() && (maxLength < 0 || rootPath.size() < maxLength)) {
            rootPath.add(depNode.head());
            depNode = depNode.head();
        }
        return rootPath;
    }

}
