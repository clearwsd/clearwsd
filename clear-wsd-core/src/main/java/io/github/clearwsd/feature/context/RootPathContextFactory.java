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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.NlpFocus;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Context factory returning the list of dependency nodes in the path to the root of a dependency tree.
 *
 * @author jamesgung
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class RootPathContextFactory extends DepContextFactory {

    public static final String KEY = "PATH";

    private static final long serialVersionUID = 1084074273199439311L;

    private boolean includeNode = false;
    private int maxLength = -1;

    @Override
    public List<NlpContext<DepNode>> apply(NlpFocus<DepNode, DepTree> instance) {
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
        Set<Integer> visited = new HashSet<>();
        if (includeNode) {
            rootPath.add(depNode);
            visited.add(depNode.index());
        }
        while (!depNode.isRoot() && (maxLength < 0 || rootPath.size() < maxLength)) {
            if (visited.contains(depNode.head().index())) {
                log.warn("Cycle in dependency tree: {}", depNode.toString());
                break;
            }
            rootPath.add(depNode.head());
            depNode = depNode.head();
            visited.add(depNode.index());
        }
        return rootPath;
    }

}
