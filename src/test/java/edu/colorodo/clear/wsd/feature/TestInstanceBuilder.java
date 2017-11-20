package edu.colorodo.clear.wsd.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.colorodo.clear.wsd.type.BaseDepNode;
import edu.colorodo.clear.wsd.type.BaseDependencyTree;
import edu.colorodo.clear.wsd.type.DepNode;
import edu.colorodo.clear.wsd.type.DependencyTree;
import edu.colorodo.clear.wsd.type.FeatureType;
import edu.colorodo.clear.wsd.type.FocusInstance;

/**
 * Builder for dependency tree focus instances.
 *
 * @author jamesgung
 */
public class TestInstanceBuilder {

    private List<BaseDepNode> depNodes = new ArrayList<>();
    private BaseDepNode root;
    private BaseDepNode focus;

    public TestInstanceBuilder(String instance, int focus) {
        for (String string : instance.split("\\s+")) {
            BaseDepNode depNode = new BaseDepNode(depNodes.size());
            depNode.addFeature(FeatureType.Text, string);
            depNodes.add(depNode);
        }
        this.focus = depNodes.get(focus);
    }

    public TestInstanceBuilder addHead(int index, int head, String label) {
        depNodes.get(index).head(depNodes.get(head));
        depNodes.get(index).addFeature(FeatureType.Dep, label);
        return this;
    }

    public TestInstanceBuilder root(int index) {
        root = depNodes.get(index);
        return this;
    }

    public FocusInstance<DepNode, DependencyTree> build() {
        return new FocusInstance<>(0, focus, new BaseDependencyTree(0,
                depNodes.stream().map(s -> (DepNode) s).collect(Collectors.toList()), root));
    }

}
