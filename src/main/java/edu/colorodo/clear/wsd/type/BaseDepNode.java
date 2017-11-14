package edu.colorodo.clear.wsd.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Base dependency node.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class BaseDepNode implements DepNode {

    private DepNode head;
    @Setter
    private NlpInstance nlpToken;
    @Setter
    private List<DepNode> children;

    public BaseDepNode(int index) {
        this.nlpToken = new BaseNlpInstance(index);
        children = new ArrayList<>();
    }

    @Override
    public String dep() {
        return feature(FeatureType.Dep);
    }

    @Override
    public int index() {
        return nlpToken.index();
    }

    public void head(DepNode depNode) {
        this.head = depNode;
        depNode.children().add(this);
    }

    @Override
    public Map<String, String> features() {
        return nlpToken.features();
    }

    @Override
    public String feature(FeatureType featureType) {
        return nlpToken.feature(featureType);
    }


    @Override
    public String feature(String feature) {
        return nlpToken.feature(feature);
    }

    @Override
    public void addFeature(FeatureType featureType, String value) {
        nlpToken.addFeature(featureType, value);
    }

    @Override
    public void addFeature(String featureKey, String value) {
        nlpToken.addFeature(featureKey, value);
    }

    @Override
    public boolean isRoot() {
        return null == head;
    }

    @Override
    public String toString() {
        return nlpToken.toString();
    }
}
