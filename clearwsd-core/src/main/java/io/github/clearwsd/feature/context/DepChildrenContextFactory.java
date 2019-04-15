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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.NlpFocus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Context factory returning the list of children of a particular dependency node. Can be applied to children multiple levels below
 * the focus node.
 *
 * @author jamesgung
 */
@Setter
@Getter
@Accessors(fluent = true)
public class DepChildrenContextFactory extends DepContextFactory {

    private static final long serialVersionUID = 8128479595556229276L;

    public static final String KEY = "D";

    private Set<String> exclude;
    private Set<String> include;
    private int level;
    private boolean includeRel = false;

    /**
     * Initialize a {@link DepChildrenContextFactory} with child dependency label exclusions and inclusions.
     *
     * @param exclude labels to exclude (if empty, exclude none)
     * @param include labels to include (if empty, include all)
     * @param level   level in dependency tree at which to extract contexts (0 for children, 1 for children's children, etc.)
     */
    public DepChildrenContextFactory(Set<String> exclude, Set<String> include, int level) {
        this.exclude = exclude;
        this.include = include;
        this.level = level;
    }

    public DepChildrenContextFactory(Set<String> exclude, Set<String> include) {
        this(exclude, include, 0);
    }

    public DepChildrenContextFactory(Set<String> include) {
        this(new HashSet<>(), include, 0);
    }

    public DepChildrenContextFactory(String... include) {
        this(new HashSet<>(), Arrays.stream(include).collect(Collectors.toSet()), 0);
    }

    public DepChildrenContextFactory() {
        this(new HashSet<>(), new HashSet<>(), 0);
    }

    @Override
    public List<NlpContext<DepNode>> apply(NlpFocus<DepNode, DepTree> instance) {
        List<NlpContext<DepNode>> results = new ArrayList<>();
        for (DepNode child : instance.focus().children().stream()
                .filter(c -> include.size() == 0 || include.contains(c.dep()))
                .filter(c -> exclude.size() == 0 || !exclude.contains(c.dep()))
                .collect(Collectors.toList())) {
            String key = includeRel ? String.format("%s:%s[%d]", KEY, child.dep(), level) : String.format("%s[%d]", KEY, level);
            if (level == 0) {
                results.add(new NlpContext<>(key, child));
            } else {
                getChildrenAtDepth(child, level - 1).stream()
                        .map(c -> new NlpContext<>(key, c))
                        .forEach(results::add);
            }
        }
        return results;
    }

    private List<DepNode> getChildrenAtDepth(DepNode parent, int depth) {
        if (depth <= 0) {
            return parent.children();
        }
        List<DepNode> results = new ArrayList<>();
        for (DepNode child : parent.children()) {
            results.addAll(getChildrenAtDepth(child, depth - 1));
        }
        return results;
    }

}
