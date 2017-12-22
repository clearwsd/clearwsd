/*
 * Copyright 2017 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.clearwsd.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpInstance;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Default {@link DepNode} implementation.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class DefaultDepNode implements DepNode {

    private DepNode head;
    @Setter
    private NlpInstance nlpToken;
    @Setter
    private List<DepNode> children;

    public DefaultDepNode(int index) {
        this.nlpToken = new DefaultNlpInstance(index);
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
    public Map<String, Object> features() {
        return nlpToken.features();
    }

    @Override
    public <T> T feature(FeatureType featureType) {
        return nlpToken.feature(featureType);
    }


    @Override
    public <T> T feature(String feature) {
        return nlpToken.feature(feature);
    }

    @Override
    public <T> void addFeature(FeatureType featureType, T value) {
        nlpToken.addFeature(featureType, value);
    }

    @Override
    public <T> void addFeature(String featureKey, T value) {
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
