package io.github.clearwsd.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpFocus;
import io.github.clearwsd.type.DefaultDepNode;
import io.github.clearwsd.type.DefaultDepTree;
import io.github.clearwsd.type.DefaultNlpFocus;

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

    public NlpFocus<DepNode, DepTree> build() {
        return new DefaultNlpFocus<>(0, focus, new DefaultDepTree(0,
                depNodes.stream().map(s -> (DepNode) s).collect(Collectors.toList()), root));
    }

}
