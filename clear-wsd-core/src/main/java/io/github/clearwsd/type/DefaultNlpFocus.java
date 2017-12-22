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

import javax.annotation.Nonnull;

import io.github.clearwsd.type.NlpFocus;
import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.type.NlpSequence;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Default {@link NlpFocus} implementation.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class DefaultNlpFocus<T extends NlpInstance, S extends NlpSequence<T>> extends DefaultNlpInstance
        implements NlpFocus<T, S> {

    private T focus;
    private S sequence;

    public DefaultNlpFocus(int index, T focus, S sequence) {
        super(index);
        this.focus = focus;
        this.sequence = sequence;
    }

    @Override
    public List<T> tokens() {
        return sequence.tokens();
    }

    @Override
    public T get(int index) {
        return sequence.get(index);
    }

    @Override
    public int size() {
        return sequence.size();
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return tokens().iterator();
    }

    @Override
    public String toString() {
        return focus.toString() + "\n\n" + sequence.toString();
    }

}
