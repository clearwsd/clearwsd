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

import java.util.List;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Default {@link DepTree} implementation.
 *
 * @author jamesgung
 */
@Accessors(fluent = true)
public class DefaultDepTree extends DefaultNlpSequence<DepNode> implements DepTree {

    @Getter
    @Setter
    private DepNode root;

    public DefaultDepTree(int index, List<DepNode> tokens, DepNode root) {
        super(index, tokens);
        this.root = root;
    }

}
