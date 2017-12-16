package edu.colorado.clear.wsd.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.type.DefaultDepNode;
import edu.colorado.clear.wsd.type.DefaultDepTree;
import edu.colorado.clear.wsd.type.DefaultNlpFocus;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DepTree;
import edu.colorado.clear.wsd.type.FeatureType;

/**
 * Builder for dependency tree focus instances.
 *
 * @author jamesgung
 */
public class TestInstanceBuilder {

    private List<DefaultDepNode> depNodes = new ArrayList<>();
    private DefaultDepNode root;
    private DefaultDepNode focus;

    public TestInstanceBuilder(String instance, int focus) {
        for (String string : instance.split("\\s+")) {
            DefaultDepNode depNode = new DefaultDepNode(depNodes.size());
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

    public DefaultNlpFocus<DepNode, DepTree> build() {
        return new DefaultNlpFocus<>(0, focus, new DefaultDepTree(0,
                depNodes.stream().map(s -> (DepNode) s).collect(Collectors.toList()), root));
    }

}
