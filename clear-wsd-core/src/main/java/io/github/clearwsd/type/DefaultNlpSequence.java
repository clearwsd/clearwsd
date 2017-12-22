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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.type.NlpSequence;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Default {@link NlpSequence} implementation.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class DefaultNlpSequence<T extends NlpInstance> extends DefaultNlpInstance implements NlpSequence<T> {

    private List<T> tokens;

    public DefaultNlpSequence(int index, List<T> tokens) {
        super(index);
        this.tokens = tokens;
    }

    @Override
    public T get(int index) {
        return tokens.get(index);
    }

    @Override
    public int size() {
        return tokens.size();
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return tokens.iterator();
    }

    @Override
    public String toString() {
        return tokens.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }
}
