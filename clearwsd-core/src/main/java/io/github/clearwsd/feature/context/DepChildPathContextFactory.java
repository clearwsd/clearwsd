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

package io.github.clearwsd.feature.context;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpFocus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Context factory returning children at a given dependency path (e.g. prep to pobj).
 *
 * @author jamesgung
 */
@Setter
@Getter
@Accessors(fluent = true)
public class DepChildPathContextFactory extends DepContextFactory {

    private static final long serialVersionUID = -3835825108723119138L;

    private static final class TrieNode implements Serializable {

        private static final long serialVersionUID = 5631207110545780759L;

        private String name;
        @Getter
        private Map<String, TrieNode> children;

        TrieNode() {
            this.children = new HashMap<>();
        }
    }

    @AllArgsConstructor(staticName = "of")
    private static final class Node implements Serializable {

        private static final long serialVersionUID = -8184255654312904995L;

        private TrieNode node;
        private DepNode token;

    }

    public static final String KEY = "D";

    private TrieNode trieNode;

    /**
     * Initialize a {@link DepChildPathContextFactory} with a list of paths.
     *
     * @param depPaths dep paths
     */
    public DepChildPathContextFactory(Map<String, List<List<String>>> depPaths) {
        trieNode = new TrieNode();
        for (Map.Entry<String, List<List<String>>> pathEntry : depPaths.entrySet()) {
            for (List<String> path : pathEntry.getValue()) {
                TrieNode entry = trieNode.children.computeIfAbsent(path.get(0), k -> new TrieNode());
                for (int i = 1; i < path.size(); ++i) {
                    entry = entry.children.computeIfAbsent(path.get(i), k -> new TrieNode());
                    entry.name = pathEntry.getKey();
                }
            }
        }
    }

    @Override
    public List<NlpContext<DepNode>> apply(NlpFocus<DepNode, DepTree> instance) {
        List<NlpContext<DepNode>> results = new ArrayList<>();

        Stack<Node> stack = new Stack<>();
        stack.push(Node.of(trieNode, instance.focus()));

        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (node.node.children.isEmpty()) {
                String name = node.node.name;
                if (!node.token.isRoot()
                        && node.token.head().feature(FeatureType.Text).toString().equalsIgnoreCase("to")) {
                    name += ":" + "to";
                }
                results.add(new NlpContext<>(name, node.token));
                continue;
            }
            for (DepNode child : Lists.reverse(node.token.children())) {
                if (node.node.children.containsKey(child.dep())) {
                    stack.push(Node.of(node.node.children.get(child.dep()), child));
                }
            }
        }

        return results;
    }

}
